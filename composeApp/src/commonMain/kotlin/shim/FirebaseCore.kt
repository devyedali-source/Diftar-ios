@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch")

package com.google.firebase

import com.example.compat.PlatformApi

object FirebaseApp {
    fun initializeApp(context: Any?): FirebaseApp = this
    fun getInstance(): FirebaseApp = this
}

open class FirebaseException(message: String?, cause: Throwable? = null) : Exception(message, cause)
class FirebaseNetworkException(message: String?) : FirebaseException(message)
class FirebaseTooManyRequestsException(message: String?) : FirebaseException(message)

/** طابع زمني Firestore */
class Timestamp(val seconds: Long, val nanoseconds: Int) : Comparable<Timestamp> {
    constructor(date: java.util.Date) : this(
        kotlin.math.floorDiv(date.time, 1000L),
        (kotlin.math.floorMod(date.time, 1000L) * 1_000_000L).toInt()
    )

    fun toDate(): java.util.Date = java.util.Date(seconds * 1000L + nanoseconds / 1_000_000)
    val millis: Long get() = seconds * 1000L + nanoseconds / 1_000_000

    override fun compareTo(other: Timestamp): Int =
        if (seconds != other.seconds) seconds.compareTo(other.seconds) else nanoseconds.compareTo(other.nanoseconds)
    override fun equals(other: Any?) = other is Timestamp && other.seconds == seconds && other.nanoseconds == nanoseconds
    override fun hashCode() = seconds.hashCode() * 31 + nanoseconds
    override fun toString() = "Timestamp(seconds=$seconds, nanoseconds=$nanoseconds)"

    /** بصيغة RFC3339 بالتوقيت العالمي */
    fun toRfc3339(): String {
        val days = kotlin.math.floorDiv(seconds, 86400L)
        val secOfDay = kotlin.math.floorMod(seconds, 86400L)
        val d = java.time.LocalDate.ofEpochDay(days)
        val h = secOfDay / 3600; val m = (secOfDay % 3600) / 60; val s = secOfDay % 60
        val ms = nanoseconds / 1_000_000
        return "${d}T${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}.${ms.toString().padStart(3, '0')}Z"
    }

    companion object {
        fun now(): Timestamp {
            val ms = PlatformApi.currentTimeMillis()
            return Timestamp(ms / 1000, ((ms % 1000) * 1_000_000).toInt())
        }

        fun parseRfc3339(text: String): Timestamp {
            // 2026-09-28T08:00:00.123456Z أو مع إزاحة +01:00
            val datePart = text.substringBefore('T')
            var timePart = text.substringAfter('T')
            var offsetSec = 0L
            if (timePart.endsWith("Z")) timePart = timePart.dropLast(1)
            else {
                val idx = maxOf(timePart.lastIndexOf('+'), timePart.lastIndexOf('-'))
                if (idx > 0) {
                    val off = timePart.substring(idx)
                    timePart = timePart.substring(0, idx)
                    val sign = if (off[0] == '-') -1 else 1
                    val hh = off.substring(1, 3).toLong(); val mm = off.substring(4, 6).toLong()
                    offsetSec = sign * (hh * 3600 + mm * 60)
                }
            }
            val d = java.time.LocalDate.parse(datePart)
            val hms = timePart.substringBefore('.').split(':')
            val frac = if (timePart.contains('.')) timePart.substringAfter('.') else ""
            val nanos = (frac + "000000000").substring(0, 9).toInt()
            val secs = d.toEpochDay() * 86400L + hms[0].toLong() * 3600 + hms[1].toLong() * 60 + hms.getOrElse(2) { "0" }.toLong() - offsetSec
            return Timestamp(secs, nanos)
        }
    }
}
