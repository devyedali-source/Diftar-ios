@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName", "FunctionName")

package java.time

import com.example.compat.PlatformApi

enum class DayOfWeek(val value: Int) {
    MONDAY(1), TUESDAY(2), WEDNESDAY(3), THURSDAY(4), FRIDAY(5), SATURDAY(6), SUNDAY(7);
    fun getValue(): Int = value
    companion object {
        fun of(v: Int): DayOfWeek = entries[(v - 1).mod(7)]
    }
}

/** تاريخ ميلادي بسيط يعتمد على عدد الأيام منذ 1970-01-01 */
class LocalDate private constructor(val year: Int, val monthValue: Int, val dayOfMonth: Int) : java.time.chrono.ChronoLocalDate, Comparable<LocalDate> {

    fun toEpochDay(): Long = daysFromCivil(year, monthValue, dayOfMonth)

    val dayOfWeek: DayOfWeek get() = DayOfWeek.of(((toEpochDay() + 3).mod(7L)).toInt() + 1)
    val dayOfYear: Int get() = (toEpochDay() - daysFromCivil(year, 1, 1)).toInt() + 1
    val month: Month get() = Month.of(monthValue)

    fun plusDays(n: Long): LocalDate = ofEpochDay(toEpochDay() + n)
    fun minusDays(n: Long): LocalDate = ofEpochDay(toEpochDay() - n)
    fun plusWeeks(n: Long): LocalDate = plusDays(n * 7)
    fun minusWeeks(n: Long): LocalDate = minusDays(n * 7)
    fun plusMonths(n: Long): LocalDate {
        val total = year * 12L + (monthValue - 1) + n
        val y = (total / 12).toInt(); val m = (total % 12).toInt() + 1
        return of(y, m, minOf(dayOfMonth, lengthOf(y, m)))
    }
    fun withDayOfMonth(d: Int): LocalDate = of(year, monthValue, d)
    fun lengthOfMonth(): Int = lengthOf(year, monthValue)
    fun isBefore(other: LocalDate): Boolean = toEpochDay() < other.toEpochDay()
    fun isAfter(other: LocalDate): Boolean = toEpochDay() > other.toEpochDay()
    fun isEqual(other: LocalDate): Boolean = toEpochDay() == other.toEpochDay()
    override fun compareTo(other: LocalDate): Int = toEpochDay().compareTo(other.toEpochDay())
    override fun equals(other: Any?): Boolean = other is LocalDate && other.toEpochDay() == toEpochDay()
    override fun hashCode(): Int = toEpochDay().hashCode()
    override fun toString(): String = "${year.toString().padStart(4, '0')}-${monthValue.toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"
    override fun gregorian(): LocalDate = this

    companion object {
        fun now(): LocalDate {
            val f = PlatformApi.localFields(PlatformApi.currentTimeMillis())
            return LocalDate(f[0], f[1], f[2])
        }
        fun of(year: Int, month: Int, day: Int): LocalDate = LocalDate(year, month, day)
        fun ofEpochDay(epochDay: Long): LocalDate {
            val (y, m, d) = civilFromDays(epochDay)
            return LocalDate(y, m, d)
        }
        fun from(temporal: java.time.chrono.ChronoLocalDate): LocalDate = temporal.gregorian()
        fun parse(text: String): LocalDate {
            val p = text.trim().split('-')
            return of(p[0].toInt(), p[1].toInt(), p[2].toInt())
        }

        internal fun lengthOf(y: Int, m: Int): Int = when (m) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            else -> if ((y % 4 == 0 && y % 100 != 0) || y % 400 == 0) 29 else 28
        }

        // خوارزمية Howard Hinnant
        internal fun daysFromCivil(y0: Int, m: Int, d: Int): Long {
            val y = if (m <= 2) y0 - 1 else y0
            val era = (if (y >= 0) y else y - 399) / 400
            val yoe = y - era * 400
            val mp = (m + 9) % 12
            val doy = (153 * mp + 2) / 5 + d - 1
            val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
            return era * 146097L + doe - 719468L
        }

        internal fun civilFromDays(z0: Long): Triple<Int, Int, Int> {
            val z = z0 + 719468
            val era = (if (z >= 0) z else z - 146096) / 146097
            val doe = (z - era * 146097).toInt()
            val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
            val y = yoe + era.toInt() * 400
            val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
            val mp = (5 * doy + 2) / 153
            val d = doy - (153 * mp + 2) / 5 + 1
            val m = if (mp < 10) mp + 3 else mp - 9
            return Triple(if (m <= 2) y + 1 else y, m, d)
        }
    }
}

enum class Month {
    JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER;
    val value: Int get() = ordinal + 1
    companion object { fun of(v: Int): Month = entries[v - 1] }
}

class DateTimeException(message: String?) : RuntimeException(message)
