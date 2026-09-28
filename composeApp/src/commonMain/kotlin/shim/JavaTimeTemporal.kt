@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName", "FunctionName")

package java.time.temporal

import java.time.chrono.ChronoLocalDate

enum class ChronoField { YEAR, MONTH_OF_YEAR, DAY_OF_MONTH, DAY_OF_WEEK, DAY_OF_YEAR }

enum class ChronoUnit {
    DAYS, WEEKS, MONTHS, YEARS;
    fun between(a: ChronoLocalDate, b: ChronoLocalDate): Long {
        val da = a.gregorian().toEpochDay()
        val db = b.gregorian().toEpochDay()
        return when (this) {
            DAYS -> db - da
            WEEKS -> (db - da) / 7
            MONTHS -> {
                val ga = a.gregorian(); val gb = b.gregorian()
                var m = (gb.year - ga.year) * 12L + (gb.monthValue - ga.monthValue)
                if (m > 0 && gb.dayOfMonth < ga.dayOfMonth) m-- else if (m < 0 && gb.dayOfMonth > ga.dayOfMonth) m++
                m
            }
            YEARS -> MONTHS.between(a, b) / 12
        }
    }
}
