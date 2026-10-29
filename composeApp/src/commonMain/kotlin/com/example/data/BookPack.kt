package com.example.data

import android.content.Context
import com.example.compat.PlatformApi
import com.example.compat.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * صور صفحات الكتب وأدلة النسقية في الآيفون.
 * تُنزَّل كلها مرة واحدة (نحو 125 ميغابايت) على أجزاء صغيرة مع نسبة اكتمال،
 * وتُحفظ في مجلد التطبيق فتعمل بعدها دون إنترنت. التنزيل قابل للاستئناف: الأجزاء
 * التي نُزّلت لا تُنزَّل ثانية إذا انقطع الإنترنت.
 */
object BookPack {
    const val PACK = "books"

    private const val BASE_URL = "https://raw.githubusercontent.com/devyedali-source/Diftar-books/main/"
    private const val MANIFEST = "manifest.json"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val progress = MutableStateFlow<Int?>(null)
    private val lock = Any()
    private var running = false

    private class Manifest(
        val total: Long,
        val partSize: Long,
        val parts: List<Pair<String, Long>>,
        val index: Map<String, LongArray>
    )

    private var manifestCache: Manifest? = null
    private var lastPartName: String? = null
    private var lastPartBytes: ByteArray? = null

    private fun dir(): String = PlatformApi.filesDir() + "/bookpack"
    private fun path(name: String) = dir() + "/" + name

    private fun parseManifest(bytes: ByteArray): Manifest? = try {
        val root = Json.parseToJsonElement(bytes.decodeToString()).jsonObject
        val parts = root["parts"]!!.jsonArray.map {
            val o = it.jsonObject
            o["name"]!!.jsonPrimitive.content to o["size"]!!.jsonPrimitive.long
        }
        val idx = HashMap<String, LongArray>()
        for ((k, v) in root["index"]!!.jsonObject) {
            val a = v.jsonArray
            idx[k] = longArrayOf(a[0].jsonPrimitive.long, a[1].jsonPrimitive.long)
        }
        Manifest(root["total"]!!.jsonPrimitive.long, root["partSize"]!!.jsonPrimitive.long, parts, idx)
    } catch (e: Exception) {
        null
    }

    private fun localManifest(): Manifest? {
        manifestCache?.let { return it }
        val b = PlatformApi.readFile(path(MANIFEST)) ?: return null
        return parseManifest(b).also { manifestCache = it }
    }

    private fun partComplete(m: Manifest, name: String, size: Long): Boolean {
        val b = PlatformApi.readFile(path(name)) ?: return false
        return b.size.toLong() == size
    }

    private fun isComplete(): Boolean {
        val m = localManifest() ?: return false
        return m.parts.all { (n, s) -> PlatformApi.fileExists(path(n)) && partComplete(m, n, s) }
    }

    /** يفتح صفحة كتاب، أو يعيد null إذا لم تُنزَّل الصور بعد. */
    fun open(context: Context, book: String, page: Int): InputStream? {
        val m = localManifest() ?: return null
        val loc = m.index["$book/$page"] ?: return null
        val start = loc[0]
        val len = loc[1].toInt()
        val out = ByteArray(len)
        var copied = 0
        var pos = start
        while (copied < len) {
            val partIdx = (pos / m.partSize).toInt()
            val (name, _) = m.parts.getOrNull(partIdx) ?: return null
            val bytes = synchronized(lock) {
                if (lastPartName == name && lastPartBytes != null) lastPartBytes
                else PlatformApi.readFile(path(name))?.also { lastPartName = name; lastPartBytes = it }
            } ?: return null
            val inPart = (pos - partIdx.toLong() * m.partSize).toInt()
            val n = minOf(len - copied, bytes.size - inPart)
            if (n <= 0) return null
            bytes.copyInto(out, copied, inPart, inPart + n)
            copied += n
            pos += n
        }
        return ByteArrayInputStream(out)
    }

    private fun download(name: String): ByteArray? {
        var tries = 0
        while (tries < 3) {
            val r = try {
                PlatformApi.httpExecute("GET", BASE_URL + name, emptyMap(), null, 60.0)
            } catch (e: Exception) {
                null
            }
            if (r != null && r.isSuccessful && r.body.isNotEmpty()) return r.body
            tries++
            if (r == null || r.code == 0) return null // لا إنترنت
        }
        return null
    }

    /** يبدأ تنزيل كل الصور (مرة واحدة)؛ يُستأنف من حيث توقف إذا انقطع الإنترنت. */
    fun requestDownload(context: Context) {
        synchronized(lock) {
            if (running) return
            running = true
        }
        scope.launch {
            try {
                PlatformApi.makeDirs(dir())
                // الجزء الأول: قائمة الملفات
                var m = localManifest()
                val remoteBytes = if (m == null) download(MANIFEST) else null
                if (m == null) {
                    if (remoteBytes == null) return@launch
                    m = parseManifest(remoteBytes) ?: return@launch
                    PlatformApi.writeFile(path(MANIFEST), remoteBytes)
                    manifestCache = m
                }
                if (isComplete()) { progress.value = null; return@launch }
                var done = 0L
                for ((n, s) in m.parts) {
                    if (PlatformApi.fileExists(path(n)) && partComplete(m, n, s)) done += s
                }
                progress.value = (done * 100 / m.total).toInt().coerceIn(0, 100)
                for ((n, s) in m.parts) {
                    if (PlatformApi.fileExists(path(n)) && partComplete(m, n, s)) continue
                    val bytes = download(n)
                    if (bytes == null || bytes.size.toLong() != s) { progress.value = null; return@launch }
                    PlatformApi.writeFile(path(n), bytes)
                    done += s
                    progress.value = (done * 100 / m.total).toInt().coerceIn(0, 100)
                }
                progress.value = null
            } catch (e: Exception) {
                progress.value = null
            } finally {
                synchronized(lock) { running = false }
            }
        }
    }

    /** نسبة التنزيل (0..100) أو null حين لا يوجد تنزيل جارٍ أو عند الاكتمال. */
    fun downloadProgress(context: Context): Flow<Int?> = progress
}
