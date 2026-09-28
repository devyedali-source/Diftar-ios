@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName", "FunctionName")

package android.os

import com.example.compat.PlatformApi

object Build {
    val MANUFACTURER: String get() = "Apple"
    val MODEL: String get() = PlatformApi.deviceName()
    val BRAND: String get() = "Apple"
    val DEVICE: String get() = PlatformApi.deviceName()
    object VERSION {
        const val SDK_INT = 36
        const val RELEASE = "iOS"
    }
    object VERSION_CODES {
        const val M = 23
        const val N = 24
        const val O = 26
        const val P = 28
        const val Q = 29
        const val R = 30
        const val S = 31
        const val TIRAMISU = 33
        const val UPSIDE_DOWN_CAKE = 34
    }
}

class Bundle

object Looper {
    fun getMainLooper(): Looper = this
    fun myLooper(): Looper? = if (PlatformApi.isMainThread()) this else null
}

class Handler(looper: Looper? = null) {
    fun post(r: () -> Unit): Boolean { PlatformApi.runOnMain(r); return true }
    fun postDelayed(r: () -> Unit, delayMillis: Long): Boolean { PlatformApi.runOnMainDelayed(delayMillis, r); return true }
    fun removeCallbacksAndMessages(token: Any?) {}
}

class CancellationSignal
