package com.example.data

import com.example.compat.*
import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

/**
 * دليل المدارس: الولاية ← المقاطعة ← قائمة المدارس (من موقع النتائج الرسمي، مسابقة 2026).
 * أسماء الولايات هي نفسها أسماء التطبيق (مثل «كوركول» و«نواكشوط الشمالية»).
 * يُحمَّل مرة واحدة من assets/geo/schools_directory.json.gz.
 */
object SchoolDirectory {

    private const val ASSET_PATH = "geo/schools_directory.json.gz"
    private const val ASSET_PATH_RAW = "geo/schools_directory.json"

    private val loadLock = Any()

    @Volatile
    private var cached: Map<String, Map<String, List<String>>>? = null

    private fun load(context: Context): Map<String, Map<String, List<String>>> {
        cached?.let { return it }
        synchronized(loadLock) {
            cached?.let { return it }
            val parsed: Map<String, Map<String, List<String>>> = try {
                // أدوات البناء في أندرويد تفكّ ضغط ملفات .gz وتحذف اللاحقة، فنجرّب الاسمين
                val text = try {
                    context.applicationContext.assets.open(ASSET_PATH).use { base ->
                        GZIPInputStream(base).use { gz ->
                            InputStreamReader(gz, Charsets.UTF_8).use { reader -> reader.readText() }
                        }
                    }
                } catch (e1: Throwable) {
                    context.applicationContext.assets.open(ASSET_PATH_RAW).use { base ->
                        InputStreamReader(base, Charsets.UTF_8).use { reader -> reader.readText() }
                    }
                }
                val out = LinkedHashMap<String, Map<String, List<String>>>()
                for ((w, mObj) in Json.parseToJsonElement(text).jsonObject) {
                    val inner = LinkedHashMap<String, List<String>>()
                    for ((m, arr) in mObj.jsonObject) inner[m] = arr.jsonArray.map { it.jsonPrimitive.content }
                    out[w] = inner
                }
                out
            } catch (e: Throwable) {
                emptyMap()
            }
            cached = parsed
            return parsed
        }
    }

    /** مقاطعات الولاية كما في الدليل (فارغة إن لم تُعرف الولاية). */
    fun moughataas(context: Context, wilaya: String): List<String> =
        load(context)[wilaya.trim()]?.keys?.toList() ?: emptyList()

    /** مدارس المقاطعة مرتبة أبجدياً (فارغة إن لم تُعرف). */
    fun schools(context: Context, wilaya: String, moughataa: String): List<String> =
        load(context)[wilaya.trim()]?.get(moughataa.trim()) ?: emptyList()

    /** هل الاسم موجود في دليل هذه المقاطعة؟ */
    fun contains(context: Context, wilaya: String, moughataa: String, school: String): Boolean =
        schools(context, wilaya, moughataa).contains(school.trim())
}
