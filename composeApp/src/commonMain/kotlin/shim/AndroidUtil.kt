@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName", "FunctionName")

package android.util

import com.example.compat.PlatformApi
import kotlin.io.encoding.ExperimentalEncodingApi

object Log {
    fun d(tag: String?, msg: String?): Int { PlatformApi.log(tag ?: "", msg ?: ""); return 0 }
    fun i(tag: String?, msg: String?): Int { PlatformApi.log(tag ?: "", msg ?: ""); return 0 }
    fun w(tag: String?, msg: String?): Int { PlatformApi.log(tag ?: "", msg ?: ""); return 0 }
    fun w(tag: String?, msg: String?, tr: Throwable?): Int { PlatformApi.log(tag ?: "", (msg ?: "") + " " + (tr?.message ?: "")); return 0 }
    fun e(tag: String?, msg: String?): Int { PlatformApi.log(tag ?: "", msg ?: ""); return 0 }
    fun e(tag: String?, msg: String?, tr: Throwable?): Int { PlatformApi.log(tag ?: "", (msg ?: "") + " " + (tr?.message ?: "")); return 0 }
    fun v(tag: String?, msg: String?): Int = 0
}

object Patterns {
    val EMAIL_ADDRESS: java.util.regex.Pattern = java.util.regex.Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    )
}

@OptIn(ExperimentalEncodingApi::class)
object Base64 {
    const val DEFAULT = 0
    const val NO_WRAP = 2
    const val URL_SAFE = 8
    const val NO_PADDING = 1
    fun encodeToString(input: ByteArray, flags: Int): String {
        var s = if (flags and URL_SAFE != 0) kotlin.io.encoding.Base64.UrlSafe.encode(input) else kotlin.io.encoding.Base64.Default.encode(input)
        if (flags and NO_PADDING != 0) s = s.trimEnd('=')
        return s
    }
    fun encode(input: ByteArray, flags: Int): ByteArray = encodeToString(input, flags).encodeToByteArray()
    fun decode(str: String, flags: Int): ByteArray {
        val clean = str.filterNot { it == '\n' || it == '\r' || it == ' ' }
        return if (flags and URL_SAFE != 0) kotlin.io.encoding.Base64.UrlSafe.withPadding(kotlin.io.encoding.Base64.PaddingOption.PRESENT_OPTIONAL).decode(clean)
        else kotlin.io.encoding.Base64.Default.withPadding(kotlin.io.encoding.Base64.PaddingOption.PRESENT_OPTIONAL).decode(clean)
    }
    fun decode(input: ByteArray, flags: Int): ByteArray = decode(input.decodeToString(), flags)
}
