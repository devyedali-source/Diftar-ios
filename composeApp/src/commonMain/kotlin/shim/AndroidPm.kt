@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName")

package android.content.pm

import com.example.compat.PlatformApi

object PackageManager {
    const val GET_SIGNATURES = 64
    const val GET_SIGNING_CERTIFICATES = 0x08000000
    const val PERMISSION_GRANTED = 0
    const val PERMISSION_DENIED = -1
    fun getPackageInfo(packageName: String, flags: Int): PackageInfo = PackageInfo()
    fun getPackageInfo(packageName: String, flags: Any?): PackageInfo = PackageInfo()
    class PackageInfoFlags private constructor() {
        companion object { fun of(v: Long) = PackageInfoFlags() }
    }
}

class PackageInfo {
    val versionName: String? get() = PlatformApi.appVersionName()
    val longVersionCode: Long get() = PlatformApi.appVersionCode()
    val versionCode: Int get() = PlatformApi.appVersionCode().toInt()
    val signingInfo: SigningInfo? get() = null
    val signatures: Array<Signature>? get() = null
    val packageName: String get() = PlatformApi.bundleId()
}

class SigningInfo {
    val apkContentsSigners: Array<Signature>? get() = null
}

class Signature {
    fun toByteArray(): ByteArray = ByteArray(0)
}
