@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch")

package okhttp3

import com.example.compat.HttpResult
import com.example.compat.PlatformApi

class MediaType private constructor(val value: String) {
    override fun toString() = value
    companion object {
        fun String.toMediaType(): MediaType = MediaType(this)
        fun String.toMediaTypeOrNull(): MediaType? = MediaType(this)
        fun get(s: String) = MediaType(s)
        fun parse(s: String) = MediaType(s)
    }
}

class RequestBody internal constructor(val bytes: ByteArray, val contentType: MediaType?) {
    companion object {
        fun String.toRequestBody(contentType: MediaType? = null): RequestBody = RequestBody(encodeToByteArray(), contentType)
        fun ByteArray.toRequestBody(contentType: MediaType? = null): RequestBody = RequestBody(this, contentType)
        fun create(contentType: MediaType?, content: String): RequestBody = RequestBody(content.encodeToByteArray(), contentType)
    }
}

class Request private constructor(val url: String, val method: String, val headers: Map<String, String>, val body: RequestBody?) {
    class Builder {
        private var url = ""
        private var method = "GET"
        private val headers = LinkedHashMap<String, String>()
        private var body: RequestBody? = null
        fun url(u: String) = apply { url = u }
        fun addHeader(k: String, v: String) = apply { headers[k] = v }
        fun header(k: String, v: String) = apply { headers[k] = v }
        fun get() = apply { method = "GET"; body = null }
        fun post(b: RequestBody) = apply { method = "POST"; body = b }
        fun patch(b: RequestBody) = apply { method = "PATCH"; body = b }
        fun put(b: RequestBody) = apply { method = "PUT"; body = b }
        fun delete(b: RequestBody? = null) = apply { method = "DELETE"; body = b }
        fun build(): Request {
            val h = LinkedHashMap(headers)
            body?.contentType?.let { if (!h.keys.any { it.equals("Content-Type", true) }) h["Content-Type"] = it.value }
            return Request(url, method, h, body)
        }
    }
}

class ResponseBody internal constructor(private val data: ByteArray) {
    fun string(): String = data.decodeToString()
    fun bytes(): ByteArray = data
    fun close() {}
}

class Response internal constructor(private val result: HttpResult) : AutoCloseable {
    val code: Int get() = result.code
    val isSuccessful: Boolean get() = result.isSuccessful
    val body: ResponseBody? get() = ResponseBody(result.body)
    val message: String get() = ""
    fun header(name: String): String? = result.headers.entries.firstOrNull { it.key.equals(name, true) }?.value
    override fun close() {}
}

class Call internal constructor(private val client: OkHttpClient, private val request: Request) {
    /** تنفيذ مانع — يُستدعى من خيط خلفي كما في أندرويد */
    fun execute(): Response {
        val r = PlatformApi.httpExecute(request.method, request.url, request.headers, request.body?.bytes, client.timeoutSeconds)
        if (r.code == 0) throw java.io.IOException("Unable to resolve host / network error")
        return Response(r)
    }
    fun cancel() {}
}

class OkHttpClient private constructor(internal val timeoutSeconds: Double) {
    constructor() : this(30.0)
    fun newCall(request: Request): Call = Call(this, request)
    class Builder {
        private var timeout = 30.0
        fun connectTimeout(t: Long, unit: java.util.concurrent.TimeUnit) = apply { timeout = maxOf(timeout, unit.toMillis(t) / 1000.0) }
        fun readTimeout(t: Long, unit: java.util.concurrent.TimeUnit) = apply { timeout = maxOf(timeout, unit.toMillis(t) / 1000.0) }
        fun writeTimeout(t: Long, unit: java.util.concurrent.TimeUnit) = apply { timeout = maxOf(timeout, unit.toMillis(t) / 1000.0) }
        fun callTimeout(t: Long, unit: java.util.concurrent.TimeUnit) = apply { timeout = maxOf(timeout, unit.toMillis(t) / 1000.0) }
        fun build() = OkHttpClient(timeout)
    }
}
