package de.marmaro.krt.ffupdater.device

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.Keep

/**
 * This class makes SDK checks testable because Mockk can't mock/change Android classes.
 */
@Keep
object DeviceSdkTester {
    val sdkInt = SDK_INT

    /**
     * API level 23
     */
    fun supportsAndroidMarshmallow(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.M
    }

    /**
     * API level 24
     */
    fun supportsAndroidNougat(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.N
    }

    /**
     * API level 26
     */
    fun supportsAndroidOreo(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.O
    }

    /**
     * API level 28
     */
    fun supportsAndroid9(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.P
    }

    /**
     * API level 29
     */
    fun supportsAndroid10(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.Q
    }

    /**
     * API level 31
     */
    fun supportsAndroid12(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.S
    }

    /**
     * API level 33
     */
    fun supportsAndroid13(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    fun supportsAndroid14(): Boolean {
        return SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }
}