package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.MainThread
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory.*
import de.marmaro.krt.ffupdater.app.entity.LatestVersion
import de.marmaro.krt.ffupdater.device.ABI
import de.marmaro.krt.ffupdater.device.DeviceAbiExtractor
import de.marmaro.krt.ffupdater.network.exceptions.NetworkException
import de.marmaro.krt.ffupdater.network.file.FileDownloader
import de.marmaro.krt.ffupdater.settings.DeviceSettingsHelper
import io.github.g00fy2.versioncompare.Version

/**
 * https://www.torproject.org/download/alpha/
 * https://dist.torproject.org/torbrowser
 * https://www.apkmirror.com/apk/the-tor-project/tor-browser-for-android-alpha/
 */
@Keep
object TorBrowserAlpha : AppBase() {
    override val app = App.TOR_BROWSER_ALPHA
    override val packageName = "org.torproject.torbrowser_alpha"
    override val title = R.string.tor_browser_alpha__title
    override val description = R.string.tor_browser_alpha__description
    override val installationWarning = R.string.generic_app_warning__beta_version
    override val downloadSource = "https://dist.torproject.org/torbrowser"
    override val icon = R.drawable.ic_logo_tor_browser_alpha
    override val minApiLevel = Build.VERSION_CODES.LOLLIPOP
    override val supportedAbis = ARM32_ARM64_X86_X64

    @Suppress("SpellCheckingInspection")
    override val signatureHash = "15f760b41acbe4783e667102c9f67119be2af62fab07763f9d57f01e5e1074e1"
    override val projectPage = "https://www.torproject.org/download/alpha/"
    override val displayCategory = listOf(BASED_ON_FIREFOX, GOOD_PRIVACY_BROWSER, GOOD_SECURITY_BROWSER)
    override val hostnameForInternetCheck = "https://dist.torproject.org"

    override suspend fun getInstalledVersion(packageManager: PackageManager): de.marmaro.krt.ffupdater.app.entity.Version? {
        val rawVersionText = super.getInstalledVersion(packageManager)?.versionText ?: return null
        val newVersionText = rawVersionText.split(" ").first()
        return de.marmaro.krt.ffupdater.app.entity.Version(newVersionText)
    }

    @MainThread
    @Throws(NetworkException::class)
    override suspend fun fetchLatestUpdate(context: Context): LatestVersion {
        val (version, dateTime) = findLatestVersion()
        return LatestVersion(
                downloadUrl = getDownloadFileUrl(version),
            version = de.marmaro.krt.ffupdater.app.entity.Version(version),
                publishDate = dateTime,
                exactFileSizeBytesOfDownload = null,
                fileHash = null,
        )
    }

    private suspend fun findLatestVersion(): Pair<String, String> {
        // search for <a href="tor-browser-android-armv7-14.0a7.apk">tor-browser-android-armv7-14.0a7.apk</a>
        val pattern = Regex.escape("<a href=\"") +
                VERSION_PATTERN + Regex.escape("""/">""") +
                VERSION_PATTERN +
                Regex.escape("/</a>")
        val content = FileDownloader.downloadString("$MAIN_BASE_URL/")

        val versions = Regex(pattern).findAll(content) // find all potential version values from content
            .filter { it.groups[1]?.value == it.groups[4]?.value } // check if version value is valid
            .mapNotNull { it.groups[1]?.value } // use only non null version values
            .sortedByDescending { Version(it) } // sort and use only the highest version value
            .toList()

        check(versions.isNotEmpty()) { "Found no versions" }

        // find the latest version with a valid APK file
        val (version, filesListHtml) = versions.map { it to FileDownloader.downloadString(getFilesListUrl(it)) }
            .first { (version, fileListHtml) -> fileListHtml.contains(getDownloadFileName(version)) }

        val dateTime = extractDateTime(getDownloadFileName(version), filesListHtml)
        return version to dateTime
    }

    @Throws(IllegalStateException::class)
    private fun extractDateTime(fileName: String, fileListHtml: String): String {
        // 2024-09-27 03:13  103M
        val pattern = Regex.escape("$fileName</a>") + """\s+(\d+-\d+-\d+)\s+(\d+:\d+)\s+(\d+)M\s+\n"""
        val match = Regex(pattern).find(fileListHtml)
        checkNotNull(match) { "Can't find creation date and file size of $fileName with regex pattern: $pattern" }
        val date = match.groups[1]
        checkNotNull(date) { "Can't extract date from regex match for $fileName." }
        val time = match.groups[2]
        checkNotNull(time) { "Can't extract time from regex match for $fileName." }

        return "${date.value}T${time.value}:00Z"
    }

    private fun getAbiString(): String {
        return when (DeviceAbiExtractor.findBestAbi(supportedAbis, DeviceSettingsHelper.prefer32BitApks)) {
            ABI.ARM64_V8A -> "aarch64"
            ABI.ARMEABI_V7A -> "armv7"
            ABI.X86_64 -> "x86_64"
            ABI.X86 -> "x86"
            else -> throw IllegalArgumentException("ABI is not supported")
        }
    }

    private fun getDownloadFileUrl(version: String): String {
        return getFilesListUrl(version) + getDownloadFileName(version)
    }

    private fun getDownloadFileName(version: String): String {
        return "tor-browser-android-${getAbiString()}-$version.apk"
    }

    private fun getFilesListUrl(version: String): String {
        return "$MAIN_BASE_URL/$version/"
    }

    private const val MAIN_BASE_URL = "https://dist.torproject.org/torbrowser"
    private const val VERSION_PATTERN = """(([\d\.]+)(\d+a\d+))"""
}