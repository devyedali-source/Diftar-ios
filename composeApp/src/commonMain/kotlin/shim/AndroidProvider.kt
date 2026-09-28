@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName")

package android.provider

import com.example.compat.PlatformApi

object Settings {
    object Secure {
        const val ANDROID_ID = "android_id"
        fun getString(resolver: Any?, name: String): String? = if (name == ANDROID_ID) PlatformApi.deviceId() else null
    }
}
