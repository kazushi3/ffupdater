package de.marmaro.krt.ffupdater.installer

import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.R.string.install_activity__downloaded_application_is_not_verified
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.device.DeviceSdkTester
import de.marmaro.krt.ffupdater.installer.AppInstaller.ExtendedInstallResult
import de.marmaro.krt.ffupdater.installer.AppInstaller.InstallResult
import de.marmaro.krt.ffupdater.security.FingerprintValidator
import de.marmaro.krt.ffupdater.settings.GeneralSettingsHelper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File

interface AppInstaller {
    data class InstallResult(val success: Boolean, val errorCode: Int?, val errorMessage: String?)
    data class ExtendedInstallResult(
        val success: Boolean,
        val certificateHash: String?,
        val errorCode: Int?,
        val errorMessage: String?,
    )

    suspend fun installAsync(context: Context): Deferred<ExtendedInstallResult>
}

abstract class SecureAppInstaller(
    private val app: App,
    private val file: File,
) : AppInstaller {

    protected abstract suspend fun uncheckInstallAsync(context: Context): Deferred<InstallResult>

    override suspend fun installAsync(context: Context): Deferred<ExtendedInstallResult> {
        return withContext(Dispatchers.IO) {
            async {
                secureInstallAsync(context)
            }
        }
    }

    private suspend fun secureInstallAsync(context: Context): ExtendedInstallResult {
        val validator = FingerprintValidator(context.packageManager)
        val fileResult = validator.checkApkFile(file, app.detail)
        val fileCertHash = fileResult.hexString
        if (!fileResult.isValid) {
            val errorMessage = context.getString(install_activity__downloaded_application_is_not_verified)
            return ExtendedInstallResult(false, fileCertHash, -100, errorMessage)
        }

        val installResult = uncheckInstallAsync(context).await()
        val installCode = installResult.errorCode
        val installMessage = installResult.errorMessage
        if (!installResult.success) {
            return ExtendedInstallResult(false, fileCertHash, installCode, installMessage)
        }

        val appResult = validator.checkInstalledApp(app.detail)
        if (!appResult.isValid || fileCertHash != appResult.hexString) {
            val errorMessage = context.getString(R.string.installed_app_is_not_verified)
            return ExtendedInstallResult(false, appResult.hexString, -101, errorMessage)
        }
        return ExtendedInstallResult(true, fileCertHash, installCode, installMessage)
    }
}

interface BackgroundAppInstaller : AppInstaller {
    companion object {
        @RequiresApi(Build.VERSION_CODES.N)
        fun create(context: Context, app: App, file: File): BackgroundAppInstaller {
            return when {
                GeneralSettingsHelper(context).isRootUsageEnabled -> RootInstaller(app, file)
                else -> BackgroundSessionInstaller(context, app, file)
            }
        }
    }
}

interface ForegroundAppInstaller : AppInstaller, DefaultLifecycleObserver {
    companion object {
        fun create(activity: ComponentActivity, app: App, file: File): ForegroundAppInstaller {
            return when {
                GeneralSettingsHelper(activity).isRootUsageEnabled -> RootInstaller(app, file)
                DeviceSdkTester.supportsAndroidNougat() -> ForegroundSessionInstaller(activity, app, file)
                else -> IntentInstaller(activity, activity.activityResultRegistry, app, file)
            }
        }
    }
}