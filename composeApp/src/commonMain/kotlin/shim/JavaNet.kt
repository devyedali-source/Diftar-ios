@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName", "FunctionName")

package java.net

object URLEncoder {
    fun encode(s: String, enc: String): String {
        val sb = StringBuilder()
        for (b in s.encodeToByteArray()) {
            val c = b.toInt() and 0xFF
            val ch = c.toChar()
            when {
                c < 0x80 && (ch.isLetterOrDigit() || ch in ".-*_") -> sb.append(ch)
                ch == ' ' -> sb.append('+')
                else -> { sb.append('%'); sb.append("0123456789ABCDEF"[c shr 4]); sb.append("0123456789ABCDEF"[c and 0xF]) }
            }
        }
        return sb.toString()
    }
    fun encode(s: String, charset: Any?): String = encode(s, "UTF-8")
}

object URLDecoder {
    fun decode(s: String, enc: String): String = android.net.Uri.decode(s.replace("+", " "))
    fun decode(s: String, charset: Any?): String = decode(s, "UTF-8")
}

class UnknownHostException(message: String? = null) : java.io.IOException(message)
class SocketTimeoutException(message: String? = null) : java.io.IOException(message)
class ConnectException(message: String? = null) : java.io.IOException(message)
