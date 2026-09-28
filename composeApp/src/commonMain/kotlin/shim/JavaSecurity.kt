@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName", "FunctionName")

package java.security

import com.example.compat.PlatformApi

class MessageDigest private constructor(private val algorithm: String) {
    private var buffer = ByteArray(0)
    fun update(bytes: ByteArray) { buffer += bytes }
    fun digest(): ByteArray = PlatformApi.digest(algorithm, buffer).also { buffer = ByteArray(0) }
    fun digest(bytes: ByteArray): ByteArray { update(bytes); return digest() }
    companion object {
        fun getInstance(algorithm: String): MessageDigest = MessageDigest(algorithm.uppercase().replace("-", ""))
    }
}
