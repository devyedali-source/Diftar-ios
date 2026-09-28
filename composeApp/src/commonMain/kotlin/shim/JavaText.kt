@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName", "FunctionName")

package java.text

import com.example.compat.PlatformApi
import java.util.Date
import java.util.Locale

/** تنسيق التواريخ عبر NSDateFormatter على الآيفون (نفس رموز الأنماط) */
class SimpleDateFormat(private val pattern: String, private val locale: Locale = Locale.getDefault()) {
    var timeZone: java.util.TimeZone = java.util.TimeZone
    var isLenient: Boolean = true
    fun format(date: Date): String = PlatformApi.formatDate(date.time, pattern, locale.toString())
    fun format(millis: Long): String = PlatformApi.formatDate(millis, pattern, locale.toString())
    fun format(obj: Any?): String = when (obj) {
        is Date -> format(obj)
        is Number -> format(obj.toLong())
        else -> ""
    }
    fun parse(text: String): Date? {
        val ms = PlatformApi.parseDate(text, pattern, locale.toString()) ?: throw ParseException("Unparseable date: \"$text\"", 0)
        return Date(ms)
    }
    fun toPattern(): String = pattern
}

class ParseException(message: String?, val errorOffset: Int) : Exception(message)

class DecimalFormatSymbols(locale: Locale = Locale.US) {
    var decimalSeparator: Char = '.'
    var groupingSeparator: Char = ','
    companion object {
        fun getInstance(locale: Locale): DecimalFormatSymbols = DecimalFormatSymbols(locale)
    }
}

/** يدعم أنماط مثل "0.##" و"#.##" و"0.00" و"#,##0.00" */
class DecimalFormat(private val pattern: String, private val symbols: DecimalFormatSymbols = DecimalFormatSymbols()) {
    var roundingMode: Any? = null
    fun format(value: Double): String {
        val p = pattern.substringBefore(';')
        val intPart = p.substringBefore('.')
        val fracPart = if (p.contains('.')) p.substringAfter('.') else ""
        val minFrac = fracPart.count { it == '0' }
        val maxFrac = fracPart.count { it == '0' || it == '#' }
        val grouping = intPart.contains(',')
        val minInt = intPart.count { it == '0' }
        var s = com.example.compat.formatFixed(kotlin.math.abs(value), maxFrac)
        if (s.contains('.')) {
            var frac = s.substringAfter('.')
            var ip = s.substringBefore('.')
            while (frac.length > minFrac && frac.endsWith('0')) frac = frac.dropLast(1)
            if (ip == "0" && minInt == 0 && frac.isNotEmpty()) ip = ""
            if (grouping) ip = group(ip)
            s = if (frac.isEmpty()) (ip.ifEmpty { "0" }) else ip + symbols.decimalSeparator + frac
        } else if (grouping) {
            s = group(s)
        }
        val negative = value < 0 && s.any { it in '1'..'9' }
        return if (negative) "-$s" else s
    }
    fun format(value: Float): String = format(value.toDouble())
    fun format(value: Long): String = format(value.toDouble())
    fun format(value: Int): String = format(value.toDouble())
    fun format(value: Any?): String = when (value) {
        is Number -> format(value.toDouble())
        else -> value.toString()
    }
    private fun group(s: String): String {
        val out = StringBuilder()
        for ((i, c) in s.withIndex()) {
            out.append(c)
            val rem = s.length - i - 1
            if (rem > 0 && rem % 3 == 0) out.append(symbols.groupingSeparator)
        }
        return out.toString()
    }
}

object NumberFormat {
    fun getInstance(locale: Locale): DecimalFormat = DecimalFormat("#,##0.###")
    fun getNumberInstance(locale: Locale): DecimalFormat = DecimalFormat("#,##0.###")
}
