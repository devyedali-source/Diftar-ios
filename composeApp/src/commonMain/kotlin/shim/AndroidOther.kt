@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName", "FunctionName")

package android.net

import com.example.compat.PlatformApi

class Uri private constructor(private val raw: String) {
    val scheme: String? get() = raw.substringBefore(':', "").ifEmpty { null }
    private val afterScheme: String get() = if (scheme != null) raw.substring(scheme!!.length + 1) else raw
    val host: String? get() {
        val a = afterScheme
        if (!a.startsWith("//")) return null
        val auth = a.substring(2).substringBefore('/').substringBefore('?').substringBefore('#')
        return auth.substringAfter('@').substringBefore(':').ifEmpty { null }
    }
    val path: String? get() {
        var a = afterScheme
        if (a.startsWith("//")) {
            a = a.substring(2)
            val slash = a.indexOf('/')
            if (slash < 0) return ""
            a = a.substring(slash)
        }
        return decode(a.substringBefore('?').substringBefore('#'))
    }
    val query: String? get() = if (raw.contains('?')) raw.substringAfter('?').substringBefore('#') else null
    val fragment: String? get() = if (raw.contains('#')) raw.substringAfter('#') else null
    val pathSegments: List<String> get() = (path ?: "").split('/').filter { it.isNotEmpty() }
    val lastPathSegment: String? get() = pathSegments.lastOrNull()
    val queryParameterNames: Set<String> get() = parseQuery().keys

    private fun parseQuery(): Map<String, String> {
        val q = query ?: return emptyMap()
        val out = LinkedHashMap<String, String>()
        for (part in q.split('&')) {
            if (part.isEmpty()) continue
            val k = decode(part.substringBefore('='))
            val v = if (part.contains('=')) decode(part.substringAfter('=')) else ""
            if (!out.containsKey(k)) out[k] = v
        }
        return out
    }

    fun getQueryParameter(key: String): String? = parseQuery()[key]
    fun getBooleanQueryParameter(key: String, def: Boolean): Boolean =
        getQueryParameter(key)?.let { it == "true" || it == "1" } ?: def

    override fun toString(): String = raw
    override fun equals(other: Any?): Boolean = other is Uri && other.raw == raw
    override fun hashCode(): Int = raw.hashCode()

    fun buildUpon(): Builder = Builder(raw)

    class Builder(private var base: String = "") {
        private val params = mutableListOf<Pair<String, String>>()
        fun scheme(s: String) = apply { base = "$s://" }
        fun authority(a: String) = apply { base += a }
        fun appendPath(p: String) = apply { base = base.trimEnd('/') + "/" + encode(p) }
        fun appendQueryParameter(k: String, v: String) = apply { params.add(k to v) }
        fun build(): Uri {
            if (params.isEmpty()) return parse(base)
            val sep = if (base.contains('?')) "&" else "?"
            return parse(base + sep + params.joinToString("&") { encode(it.first) + "=" + encode(it.second) })
        }
    }

    companion object {
        val EMPTY = Uri("")
        fun parse(s: String?): Uri = Uri(s ?: "")
        fun fromFile(file: java.io.File): Uri = Uri("file://" + file.absolutePath)
        fun encode(s: String?): String = if (s == null) "" else percentEncode(s, "-_.!~*'()")
        fun encode(s: String?, allow: String?): String = if (s == null) "" else percentEncode(s, "-_.!~*'()" + (allow ?: ""))
        fun decode(s: String?): String = if (s == null) "" else percentDecode(s, plusAsSpace = false)
    }
}

internal fun percentEncode(s: String, unreserved: String): String {
    val sb = StringBuilder()
    for (b in s.encodeToByteArray()) {
        val c = (b.toInt() and 0xFF)
        val ch = c.toChar()
        if (c < 0x80 && (ch.isLetterOrDigit() || unreserved.indexOf(ch) >= 0)) sb.append(ch)
        else {
            sb.append('%')
            sb.append("0123456789ABCDEF"[c shr 4])
            sb.append("0123456789ABCDEF"[c and 0xF])
        }
    }
    return sb.toString()
}

internal fun percentDecode(s: String, plusAsSpace: Boolean): String {
    val bytes = ArrayList<Byte>(s.length)
    var i = 0
    while (i < s.length) {
        val c = s[i]
        when {
            c == '%' && i + 2 < s.length -> {
                val hex = s.substring(i + 1, i + 3).toIntOrNull(16)
                if (hex != null) { bytes.add(hex.toByte()); i += 3; continue }
                bytes.addAll(c.toString().encodeToByteArray().toList()); i++
            }
            c == '+' && plusAsSpace -> { bytes.add(' '.code.toByte()); i++ }
            else -> { bytes.addAll(c.toString().encodeToByteArray().toList()); i++ }
        }
    }
    return bytes.toByteArray().decodeToString()
}

// ---------- الشبكة ----------

object ConnectivityManager {
    val activeNetwork: Network? get() = if (PlatformApi.isOnline()) Network else null
    val activeNetworkInfo: NetworkInfo? get() = if (PlatformApi.isOnline()) NetworkInfo else null
    fun getNetworkCapabilities(network: Network?): NetworkCapabilities? = if (network != null && PlatformApi.isOnline()) NetworkCapabilities else null

    private val callbacks = mutableListOf<NetworkCallback>()
    private var installed = false

    private fun ensureListener() {
        if (installed) return
        installed = true
        PlatformApi.addNetworkListener { online ->
            val copy = callbacks.toList()
            for (cb in copy) {
                if (online) cb.onAvailable(Network) else cb.onLost(Network)
            }
        }
    }

    fun registerNetworkCallback(request: NetworkRequest, callback: NetworkCallback) {
        callbacks.add(callback); ensureListener()
    }
    fun registerDefaultNetworkCallback(callback: NetworkCallback) {
        callbacks.add(callback); ensureListener()
    }
    fun unregisterNetworkCallback(callback: NetworkCallback) {
        callbacks.remove(callback)
    }

    open class NetworkCallback {
        open fun onAvailable(network: Network) {}
        open fun onLost(network: Network) {}
        open fun onUnavailable() {}
        open fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {}
    }

    const val TYPE_WIFI = 1
    const val TYPE_MOBILE = 0
}

object Network

object NetworkInfo {
    val isConnected: Boolean get() = PlatformApi.isOnline()
    fun isConnected(): Boolean = PlatformApi.isOnline()
    val isConnectedOrConnecting: Boolean get() = PlatformApi.isOnline()
}

object NetworkCapabilities {
    const val NET_CAPABILITY_INTERNET = 12
    const val NET_CAPABILITY_VALIDATED = 16
    const val TRANSPORT_WIFI = 1
    const val TRANSPORT_CELLULAR = 0
    const val TRANSPORT_ETHERNET = 3
    fun hasCapability(c: Int): Boolean = PlatformApi.isOnline()
    fun hasTransport(t: Int): Boolean = PlatformApi.isOnline()
}

class NetworkRequest {
    class Builder {
        fun addCapability(c: Int) = this
        fun addTransportType(t: Int) = this
        fun build() = NetworkRequest()
    }
}
