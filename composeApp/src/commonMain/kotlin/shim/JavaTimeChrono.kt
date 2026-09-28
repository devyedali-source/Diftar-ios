@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName", "FunctionName")

package java.time.chrono

import com.example.compat.PlatformApi
import java.time.LocalDate
import java.time.temporal.ChronoField

interface ChronoLocalDate {
    fun gregorian(): LocalDate
}

/** التاريخ الهجري (تقويم أم القرى على الآيفون، كما في جافا) */
class HijrahDate private constructor(private val g: LocalDate) : ChronoLocalDate {
    private val h: IntArray by lazy { PlatformApi.hijriFromGregorian(g.year, g.monthValue, g.dayOfMonth) }

    fun get(field: ChronoField): Int = when (field) {
        ChronoField.YEAR -> h[0]
        ChronoField.MONTH_OF_YEAR -> h[1]
        ChronoField.DAY_OF_MONTH -> h[2]
        ChronoField.DAY_OF_WEEK -> g.dayOfWeek.value
        else -> 0
    }

    fun lengthOfMonth(): Int {
        val first = of(h[0], h[1], 1).g.toEpochDay()
        val nextY = if (h[1] == 12) h[0] + 1 else h[0]
        val nextM = if (h[1] == 12) 1 else h[1] + 1
        return (of(nextY, nextM, 1).g.toEpochDay() - first).toInt()
    }

    override fun gregorian(): LocalDate = g
    override fun toString(): String = "Hijrah-umalqura AH ${h[0]}-${h[1]}-${h[2]}"

    companion object {
        fun now(): HijrahDate = HijrahDate(LocalDate.now())
        fun from(date: LocalDate): HijrahDate = HijrahDate(date)
        fun from(date: ChronoLocalDate): HijrahDate = HijrahDate(date.gregorian())
        fun of(year: Int, month: Int, day: Int): HijrahDate {
            val gr = PlatformApi.gregorianFromHijri(year, month, day)
            if (gr[0] == 0) throw java.time.DateTimeException("Invalid Hijrah date: $year-$month-$day")
            val back = PlatformApi.hijriFromGregorian(gr[0], gr[1], gr[2])
            if (back[0] != year || back[1] != month || back[2] != day) throw java.time.DateTimeException("Invalid Hijrah date: $year-$month-$day")
            return HijrahDate(LocalDate.of(gr[0], gr[1], gr[2]))
        }
    }
}
