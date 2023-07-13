package de.marmaro.krt.ffupdater.app.entity

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class InstalledAppStatus(
    val latestVersion: LatestVersion,
    val isUpdateAvailable: Boolean,
    val displayVersion: String,
    val objectCreationTimestamp: Long = System.currentTimeMillis(),
) : Parcelable