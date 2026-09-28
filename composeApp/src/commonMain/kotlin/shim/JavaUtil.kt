@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName", "FunctionName")

package java.util

import com.example.compat.PlatformApi

class Locale(val language: String, val country: String = "") {
    override fun toString(): String = if (country.isEmpty()) language else "${language}_$country"
    val displayLanguage: String get() = language
    fun toLanguageTag(): String = if (country.isEmpty()) language else "$language-$country"

    companion object {
        val US = Locale("en", "US")
        val ENGLISH = Locale("en")
        val FRENCH = Locale("fr")
        val FRANCE = Locale("fr", "FR")
        val ROOT = Locale("")
        val UK = Locale("en", "GB")
        fun getDefault(): Locale = Locale("ar")
        fun setDefault(l: Locale) {}
        fun forLanguageTag(tag: String): Locale {
            val parts = tag.split('-', '_')
            return Locale(parts.getOrElse(0) { "" }, parts.getOrElse(1) { "" })
        }
    }
}

open class Date(var time: Long = PlatformApi.currentTimeMillis()) : Comparable<Date> {
    fun before(other: Date) = time < other.time
    fun after(other: Date) = time > other.time
    override fun compareTo(other: Date): Int = time.compareTo(other.time)
    override fun equals(other: Any?) = other is Date && other.time == time
    override fun hashCode() = time.hashCode()
    override fun toString(): String = PlatformApi.formatDate(time, "EEE MMM dd HH:mm:ss yyyy", "en_US")
}

object TimeZone {
    fun getDefault(): TimeZone = this
    fun getTimeZone(id: String): TimeZone = this
    val id: String get() = "local"
}

object UUID {
    fun randomUUID(): UUIDValue = UUIDValue(PlatformApi.randomUuid().lowercase())
    fun nameUUIDFromBytes(bytes: ByteArray): UUIDValue {
        val md5 = PlatformApi.digest("MD5", bytes)
        md5[6] = ((md5[6].toInt() and 0x0f) or 0x30).toByte()
        md5[8] = ((md5[8].toInt() and 0x3f) or 0x80).toByte()
        val hex = md5.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
        return UUIDValue("${hex.substring(0, 8)}-${hex.substring(8, 12)}-${hex.substring(12, 16)}-${hex.substring(16, 20)}-${hex.substring(20)}")
    }
}

class UUIDValue(private val s: String) {
    override fun toString(): String = s
}

/**
 * بديل java.util.Calendar بالقدر الذي يستعمله التطبيق (الحقول الأساسية والإضافة).
 * الأشهر تبدأ من 0 كما في جافا.
 */
class Calendar private constructor(private var millis: Long) {
    private var fields: IntArray = PlatformApi.localFields(millis)

    var timeInMillis: Long
        get() = millis
        set(value) { millis = value; fields = PlatformApi.localFields(value) }

    var time: Date
        get() = Date(millis)
        set(value) { timeInMillis = value.time }


    var firstDayOfWeek: Int = SUNDAY

    fun get(field: Int): Int = when (field) {
        YEAR -> fields[0]
        MONTH -> fields[1] - 1
        DAY_OF_MONTH -> fields[2]
        HOUR_OF_DAY -> fields[3]
        HOUR -> fields[3] % 12
        AM_PM -> if (fields[3] >= 12) PM else AM
        MINUTE -> fields[4]
        SECOND -> fields[5]
        MILLISECOND -> fields[6]
        DAY_OF_WEEK -> fields[7]
        DAY_OF_YEAR -> fields[8]
        WEEK_OF_YEAR -> (fields[8] - 1) / 7 + 1
        ERA -> 1
        else -> 0
    }

    fun set(field: Int, value: Int) {
        val f = fields.copyOf()
        when (field) {
            YEAR -> f[0] = value
            MONTH -> f[1] = value + 1
            DAY_OF_MONTH -> f[2] = value
            HOUR_OF_DAY, HOUR -> f[3] = value
            MINUTE -> f[4] = value
            SECOND -> f[5] = value
            MILLISECOND -> f[6] = value
            DAY_OF_YEAR -> { f[1] = 1; f[2] = value }
            DAY_OF_WEEK -> {
                val diff = value - fields[7]
                timeInMillis = millis + diff * DAY_MS
                return
            }
            else -> return
        }
        rebuild(f)
    }

    fun set(year: Int, month: Int, date: Int) {
        val f = fields.copyOf(); f[0] = year; f[1] = month + 1; f[2] = date; rebuild(f)
    }

    fun set(year: Int, month: Int, date: Int, hourOfDay: Int, minute: Int) {
        val f = fields.copyOf(); f[0] = year; f[1] = month + 1; f[2] = date; f[3] = hourOfDay; f[4] = minute; rebuild(f)
    }

    fun set(year: Int, month: Int, date: Int, hourOfDay: Int, minute: Int, second: Int) {
        val f = fields.copyOf(); f[0] = year; f[1] = month + 1; f[2] = date; f[3] = hourOfDay; f[4] = minute; f[5] = second; rebuild(f)
    }

    private fun rebuild(f: IntArray) {
        // تطبيع الشهر خارج المجال
        var y = f[0]; var m = f[1]
        while (m > 12) { m -= 12; y++ }
        while (m < 1) { m += 12; y-- }
        timeInMillis = PlatformApi.epochFromLocal(y, m, f[2], f[3], f[4], f[5], f[6])
    }

    fun add(field: Int, amount: Int) {
        when (field) {
            DAY_OF_MONTH, DAY_OF_YEAR, DAY_OF_WEEK -> {
                val f = fields.copyOf(); f[2] += amount; rebuild(f)
            }
            WEEK_OF_YEAR -> { val f = fields.copyOf(); f[2] += amount * 7; rebuild(f) }
            MONTH -> {
                val f = fields.copyOf()
                var total = (f[0] * 12 + (f[1] - 1)) + amount
                val ny = total / 12
                val nm = total % 12 + 1
                f[0] = ny; f[1] = nm
                val maxDay = daysInMonth(ny, nm)
                if (f[2] > maxDay) f[2] = maxDay
                rebuild(f)
            }
            YEAR -> {
                val f = fields.copyOf(); f[0] += amount
                val maxDay = daysInMonth(f[0], f[1]); if (f[2] > maxDay) f[2] = maxDay
                rebuild(f)
            }
            HOUR_OF_DAY, HOUR -> timeInMillis = millis + amount * 3_600_000L
            MINUTE -> timeInMillis = millis + amount * 60_000L
            SECOND -> timeInMillis = millis + amount * 1_000L
            MILLISECOND -> timeInMillis = millis + amount
        }
    }

    fun getActualMaximum(field: Int): Int = when (field) {
        DAY_OF_MONTH -> daysInMonth(fields[0], fields[1])
        DAY_OF_YEAR -> if (isLeap(fields[0])) 366 else 365
        MONTH -> 11
        else -> 0
    }

    fun before(other: Any?): Boolean = other is Calendar && millis < other.millis
    fun after(other: Any?): Boolean = other is Calendar && millis > other.millis
    fun clone(): Any = Calendar(millis)
    fun clear() { timeInMillis = PlatformApi.epochFromLocal(1970, 1, 1, 0, 0, 0, 0) }

    companion object {
        const val ERA = 0
        const val YEAR = 1
        const val MONTH = 2
        const val WEEK_OF_YEAR = 3
        const val DATE = 5
        const val DAY_OF_MONTH = 5
        const val DAY_OF_YEAR = 6
        const val DAY_OF_WEEK = 7
        const val AM_PM = 9
        const val HOUR = 10
        const val HOUR_OF_DAY = 11
        const val MINUTE = 12
        const val SECOND = 13
        const val MILLISECOND = 14

        const val JANUARY = 0
        const val FEBRUARY = 1
        const val MARCH = 2
        const val APRIL = 3
        const val MAY = 4
        const val JUNE = 5
        const val JULY = 6
        const val AUGUST = 7
        const val SEPTEMBER = 8
        const val OCTOBER = 9
        const val NOVEMBER = 10
        const val DECEMBER = 11

        const val SUNDAY = 1
        const val MONDAY = 2
        const val TUESDAY = 3
        const val WEDNESDAY = 4
        const val THURSDAY = 5
        const val FRIDAY = 6
        const val SATURDAY = 7

        const val AM = 0
        const val PM = 1

        private const val DAY_MS = 86_400_000L

        fun getInstance(): Calendar = Calendar(PlatformApi.currentTimeMillis())
        fun getInstance(locale: Locale): Calendar = getInstance()
        fun getInstance(tz: TimeZone): Calendar = getInstance()
        fun getInstance(tz: TimeZone, locale: Locale): Calendar = getInstance()

        internal fun isLeap(y: Int) = (y % 4 == 0 && y % 100 != 0) || y % 400 == 0
        internal fun daysInMonth(y: Int, m: Int) = when (m) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            else -> if (isLeap(y)) 29 else 28
        }
    }
}

object Collections {
    fun <T> synchronizedList(list: MutableList<T>): MutableList<T> = list
    fun <K, V> synchronizedMap(map: MutableMap<K, V>): MutableMap<K, V> = map
    fun <T> emptyList(): List<T> = kotlin.collections.emptyList()
    fun <T> unmodifiableList(list: List<T>): List<T> = list
}

object Objects {
    fun equals(a: Any?, b: Any?): Boolean = a == b
    fun hash(vararg values: Any?): Int = values.contentHashCode()
    fun <T> requireNonNull(obj: T?): T = obj!!
}
