package de.marmaro.krt.ffupdater.app.maintained

import android.net.Uri
import android.os.Build
import android.util.Log
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.entity.LatestUpdate
import de.marmaro.krt.ffupdater.network.ApiConsumer
import de.marmaro.krt.ffupdater.network.github.GithubConsumer

/**
 * https://api.github.com/repos/brave/brave-browser/releases
 */
class FFUpdater(
    private val apiConsumer: ApiConsumer = ApiConsumer.INSTANCE,
) : AppBase() {
    override val packageName = "de.marmaro.krt.ffupdater"
    override val displayTitle = R.string.app_name
    override val displayDescription = R.string.app_description
    override val displayDownloadSource = R.string.github
    override val displayIcon = R.mipmap.ic_launcher
    override val minApiLevel = Build.VERSION_CODES.N
    override val supportedAbis = ALL_ABIS
    override val installableWithDefaultPermission = false
    override val projectPage: Uri = Uri.parse("https://github.com/Tobi823/ffupdater")

    @Suppress("SpellCheckingInspection")
    override val signatureHash = "f4e642bb85cbbcfd7302b2cbcbd346993a41067c27d995df492c9d0d38747e62"

    override suspend fun findLatestUpdate(): LatestUpdate {
        Log.d(LOG_TAG, "check for latest version")
        val githubConsumer = GithubConsumer(
            repoOwner = "Tobi823",
            repoName = "ffupdater",
            resultsPerPage = 5,
            isValidRelease = { release -> !release.isPreRelease },
            isCorrectAsset = { asset -> asset.name.endsWith(".apk") },
            apiConsumer = apiConsumer,
        )
        val result = githubConsumer.updateCheck()
        Log.i(LOG_TAG, "found latest version ${result.tagName}")
        return LatestUpdate(
            downloadUrl = result.url,
            version = result.tagName,
            publishDate = result.releaseDate,
            fileSizeBytes = result.fileSizeBytes,
            fileHash = null,
            firstReleaseHasAssets = result.firstReleaseHasAssets,
        )
    }

    companion object {
        private const val LOG_TAG = "FFUpdater"
    }
}