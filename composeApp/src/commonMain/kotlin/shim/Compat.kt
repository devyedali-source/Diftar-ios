@file:Suppress("unused", "NOTHING_TO_INLINE", "FunctionName")

package com.example.compat

import kotlin.math.abs
import kotlin.math.pow
import kotlin.reflect.KClass

/*
 * دوالّ بديلة لما توفره بيئة جافا في أندرويد ولا يوجد في الآيفون.
 * تُستورد في كل ملفات الشيفرة المنقولة (import com.example.compat.*).
 */

object System {
    fun currentTimeMillis(): Long = PlatformApi.currentTimeMillis()
    fun nanoTime(): Long = PlatformApi.currentTimeMillis() * 1_000_000L
    fun getProperty(key: String): String? = null
    fun lineSeparator(): String = "\n"
    fun gc() {}
    val out = PrintStream()
    val err = PrintStream()
}

class PrintStream {
    fun println(x: Any?) = kotlin.io.println(x)
    fun print(x: Any?) = kotlin.io.print(x)
}

object Math {
    fun max(a: Int, b: Int) = kotlin.math.max(a, b)
    fun max(a: Long, b: Long) = kotlin.math.max(a, b)
    fun max(a: Double, b: Double) = kotlin.math.max(a, b)
    fun max(a: Float, b: Float) = kotlin.math.max(a, b)
    fun min(a: Int, b: Int) = kotlin.math.min(a, b)
    fun min(a: Long, b: Long) = kotlin.math.min(a, b)
    fun min(a: Double, b: Double) = kotlin.math.min(a, b)
    fun min(a: Float, b: Float) = kotlin.math.min(a, b)
    fun abs(a: Int) = kotlin.math.abs(a)
    fun abs(a: Long) = kotlin.math.abs(a)
    fun abs(a: Double) = kotlin.math.abs(a)
    fun abs(a: Float) = kotlin.math.abs(a)
    fun round(a: Double): Long = kotlin.math.floor(a + 0.5).toLong()
    fun round(a: Float): Int = kotlin.math.floor(a + 0.5f).toInt()
    fun floor(a: Double) = kotlin.math.floor(a)
    fun ceil(a: Double) = kotlin.math.ceil(a)
    fun sqrt(a: Double) = kotlin.math.sqrt(a)
    fun pow(a: Double, b: Double) = a.pow(b)
    fun sin(a: Double) = kotlin.math.sin(a)
    fun cos(a: Double) = kotlin.math.cos(a)
    fun atan2(y: Double, x: Double) = kotlin.math.atan2(y, x)
    fun toRadians(a: Double) = a * kotlin.math.PI / 180.0
    fun toDegrees(a: Double) = a * 180.0 / kotlin.math.PI
    fun random(): Double = kotlin.random.Random.nextDouble()
    const val PI = kotlin.math.PI
}

object Integer {
    const val MAX_VALUE = Int.MAX_VALUE
    const val MIN_VALUE = Int.MIN_VALUE
    fun parseInt(s: String): Int = s.trim().toInt()
    fun parseInt(s: String, radix: Int): Int = s.trim().toInt(radix)
    fun valueOf(s: String): Int = s.trim().toInt()
    fun toString(i: Int): String = i.toString()
    fun toHexString(i: Int): String = i.toUInt().toString(16)
}

object Thread {
    fun sleep(millis: Long) { PlatformApi.sleepMillis(millis) }
    fun currentThread(): ThreadInfo = ThreadInfo
}

object ThreadInfo {
    val name: String get() = if (PlatformApi.isMainThread()) "main" else "worker"
}

typealias Volatile = kotlin.concurrent.Volatile

/** بديل synchronized في جافا — يكفي هنا لأن الاستعمال لا يتطلّب قفلاً حقيقيًا */
inline fun <R> synchronized(@Suppress("UNUSED_PARAMETER") lock: Any, block: () -> R): R = block()

/** يسمح بكتابة Foo::class.java كما في أندرويد */
val <T : Any> KClass<T>.java: KClass<T> get() = this

// ---------- String.format ----------

fun String.Companion.format(format: String, vararg args: Any?): String = formatPrintf(format, args)
fun String.Companion.format(locale: java.util.Locale?, format: String, vararg args: Any?): String = formatPrintf(format, args)
fun String.format(vararg args: Any?): String = formatPrintf(this, args)
fun String.format(locale: java.util.Locale?, vararg args: Any?): String = formatPrintf(this, args)

fun String.lowercase(@Suppress("UNUSED_PARAMETER") locale: java.util.Locale): String = this.lowercase()
fun String.uppercase(@Suppress("UNUSED_PARAMETER") locale: java.util.Locale): String = this.uppercase()
fun String.toLowerCase(@Suppress("UNUSED_PARAMETER") locale: java.util.Locale): String = this.lowercase()
fun String.toUpperCase(@Suppress("UNUSED_PARAMETER") locale: java.util.Locale): String = this.uppercase()
fun String.capitalize(@Suppress("UNUSED_PARAMETER") locale: java.util.Locale): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

fun String.getBytes(@Suppress("UNUSED_PARAMETER") charset: Any? = null): ByteArray = encodeToByteArray()
fun String.toByteArray(@Suppress("UNUSED_PARAMETER") charset: Any?): ByteArray = encodeToByteArray()
fun ByteArray.toString(@Suppress("UNUSED_PARAMETER") charset: Any?): String = decodeToString()

object Charsets {
    val UTF_8: Any = "UTF-8"
    val US_ASCII: Any = "US-ASCII"
}

/** تنفيذ مبسّط لـ printf يكفي الأنماط المستعملة في التطبيق */
fun formatPrintf(format: String, args: Array<out Any?>): String {
    val sb = StringBuilder()
    var argIndex = 0
    var i = 0
    while (i < format.length) {
        val c = format[i]
        if (c != '%') {
            sb.append(c); i++; continue
        }
        if (i + 1 < format.length && format[i + 1] == '%') {
            sb.append('%'); i += 2; continue
        }
        if (i + 1 < format.length && format[i + 1] == 'n') {
            sb.append('\n'); i += 2; continue
        }
        // %[argument_index$][flags][width][.precision]conversion
        var j = i + 1
        var explicitIndex: Int? = null
        val startDigits = j
        while (j < format.length && format[j].isDigit()) j++
        if (j < format.length && format[j] == '$' && j > startDigits) {
            explicitIndex = format.substring(startDigits, j).toInt() - 1
            j++
        } else {
            j = startDigits
        }
        val flags = StringBuilder()
        while (j < format.length && format[j] in "-+ 0,#(") { flags.append(format[j]); j++ }
        val wStart = j
        while (j < format.length && format[j].isDigit()) j++
        val width = if (j > wStart) format.substring(wStart, j).toInt() else 0
        var precision = -1
        if (j < format.length && format[j] == '.') {
            j++
            val pStart = j
            while (j < format.length && format[j].isDigit()) j++
            precision = if (j > pStart) format.substring(pStart, j).toInt() else 0
        }
        if (j >= format.length) { sb.append(format.substring(i)); break }
        val conv = format[j]
        val arg = if (explicitIndex != null) args.getOrNull(explicitIndex) else args.getOrNull(argIndex++)
        val leftAlign = flags.contains('-')
        val zeroPad = flags.contains('0')
        val plus = flags.contains('+')
        val grouping = flags.contains(',')
        var body = when (conv) {
            'd' -> {
                val v = when (arg) { is Number -> arg.toLong(); else -> arg.toString().toLongOrNull() ?: 0L }
                var s = abs(v).toString()
                if (grouping) s = groupDigits(s)
                (if (v < 0) "-" else if (plus) "+" else "") + s
            }
            'f', 'e', 'g' -> {
                val v = when (arg) { is Number -> arg.toDouble(); else -> arg.toString().toDoubleOrNull() ?: 0.0 }
                val p = if (precision < 0) 6 else precision
                var s = formatFixed(abs(v), p)
                if (grouping) {
                    val dot = s.indexOf('.')
                    s = if (dot >= 0) groupDigits(s.substring(0, dot)) + s.substring(dot) else groupDigits(s)
                }
                val negative = v < 0 && s.any { it in '1'..'9' }
                (if (negative) "-" else if (plus) "+" else "") + s
            }
            's', 'S' -> {
                var s = arg?.toString() ?: "null"
                if (precision >= 0 && s.length > precision) s = s.substring(0, precision)
                if (conv == 'S') s.uppercase() else s
            }
            'c' -> arg?.toString() ?: ""
            'b' -> (arg != null && arg != false).toString()
            'x', 'X' -> {
                val v = when (arg) { is Number -> arg.toLong(); else -> 0L }
                val s = v.toULong().toString(16)
                if (conv == 'X') s.uppercase() else s
            }
            else -> "%$conv"
        }
        if (body.length < width) {
            val pad = width - body.length
            body = when {
                leftAlign -> body + " ".repeat(pad)
                zeroPad && conv in "dfxX" -> {
                    val signLen = if (body.startsWith("-") || body.startsWith("+")) 1 else 0
                    body.substring(0, signLen) + "0".repeat(pad) + body.substring(signLen)
                }
                else -> " ".repeat(pad) + body
            }
        }
        sb.append(body)
        i = j + 1
    }
    return sb.toString()
}

private fun groupDigits(s: String): String {
    val out = StringBuilder()
    val len = s.length
    for ((idx, ch) in s.withIndex()) {
        out.append(ch)
        val remaining = len - idx - 1
        if (remaining > 0 && remaining % 3 == 0) out.append(',')
    }
    return out.toString()
}

/** تقريب نصفي إلى أعلى كما في جافا (HALF_UP) */
fun formatFixed(value: Double, precision: Int): String {
    if (value.isNaN()) return "NaN"
    if (value.isInfinite()) return "Infinity"
    if (precision == 0) {
        return kotlin.math.floor(value + 0.5).toLong().toString()
    }
    var factor = 1L
    repeat(precision) { factor *= 10 }
    // تصحيح أخطاء التمثيل الثنائي قبل التقريب (مثل 2.675)
    val scaled = value * factor
    val rounded = kotlin.math.floor(scaled + 0.5 + 1e-9).toLong()
    val intPart = rounded / factor
    val frac = rounded % factor
    return intPart.toString() + "." + frac.toString().padStart(precision, '0')
}

/** بديل java.lang.Void */
class Void private constructor()

/** إعدادات Firebase وGoogle (من ملف GoogleService-Info.plist إن وُجد) */
object FirebaseConfig {
    private const val ANDROID_API_KEY = "AIzaSyAcRECbnaJMUyUqTPj9xmmA1lX-pISKInA"
    val projectId: String get() = PlatformApi.googleServiceValue("PROJECT_ID") ?: "enseignatprincipal"
    val apiKey: String get() = PlatformApi.googleServiceValue("API_KEY") ?: ANDROID_API_KEY
    val hasIosConfig: Boolean get() = PlatformApi.googleServiceValue("API_KEY") != null
    val iosClientId: String? get() = PlatformApi.googleServiceValue("CLIENT_ID")
    val reversedClientId: String? get() = PlatformApi.googleServiceValue("REVERSED_CLIENT_ID")

    /** ترويسات تسمح بمفتاح API مقيَّد بمعرّف تطبيق الآيفون */
    fun apiHeaders(): Map<String, String> = mapOf("X-Ios-Bundle-Identifier" to PlatformApi.bundleId())
}

/** Throwable.localizedMessage كما في جافا */
val Throwable.localizedMessage: String? get() = message

/** obj.javaClass.simpleName كما في جافا */
val <T : Any> T.javaClass: KClass<out T> get() = this::class

fun <K, V> MutableMap<K, V>.putIfAbsent(key: K, value: V): V? {
    val existing = get(key)
    if (existing == null) put(key, value)
    return existing
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class Synchronized
