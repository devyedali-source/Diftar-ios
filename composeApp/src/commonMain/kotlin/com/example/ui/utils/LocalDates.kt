package com.example.ui.utils

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

/** تاريخ اليوم الميلادي والهجري كما هو مسجَّل محلياً (مع تعديل الهجري الذي يضبطه المعلم في الجدول الزمني). */
object LocalDates {
    private val hijriAr = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )
    private val hijriFr = listOf(
        "Mouharram", "Safar", "Rabi' al-awwal", "Rabi' ath-thani", "Joumada al-oula", "Joumada ath-thania",
        "Rajab", "Cha'ban", "Ramadan", "Chawwal", "Dhou al-qi'da", "Dhou al-hijja"
    )
    private val gregAr = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )
    private val gregFr = listOf(
        "janvier", "février", "mars", "avril", "mai", "juin",
        "juillet", "août", "septembre", "octobre", "novembre", "décembre"
    )

    /** (الهجري، الميلادي) */
    fun today(context: Context, french: Boolean): Pair<String, String> {
        val offset = try {
            context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).getLong("hijri_offset_days_v2", 0L)
        } catch (e: Exception) { 0L }
        val g = LocalDate.now()
        val h = try { HijrahDate.from(g.plusDays(offset)) } catch (e: Exception) { HijrahDate.now() }
        val hd = h.get(ChronoField.DAY_OF_MONTH)
        val hm = h.get(ChronoField.MONTH_OF_YEAR)
        val hy = h.get(ChronoField.YEAR)
        val dayAr = when (g.dayOfWeek) {
            DayOfWeek.MONDAY -> "الإثنين"; DayOfWeek.TUESDAY -> "الثلاثاء"; DayOfWeek.WEDNESDAY -> "الأربعاء"
            DayOfWeek.THURSDAY -> "الخميس"; DayOfWeek.FRIDAY -> "الجمعة"; DayOfWeek.SATURDAY -> "السبت"; else -> "الأحد"
        }
        val dayFr = when (g.dayOfWeek) {
            DayOfWeek.MONDAY -> "lundi"; DayOfWeek.TUESDAY -> "mardi"; DayOfWeek.WEDNESDAY -> "mercredi"
            DayOfWeek.THURSDAY -> "jeudi"; DayOfWeek.FRIDAY -> "vendredi"; DayOfWeek.SATURDAY -> "samedi"; else -> "dimanche"
        }
        return if (french) {
            Pair(
                "Date (hégire) : $hd ${hijriFr.getOrElse(hm - 1) { "$hm" }} $hy H",
                "Date : $dayFr ${g.dayOfMonth} ${gregFr[g.monthValue - 1]} ${g.year}"
            )
        } else {
            Pair(
                "التاريخ الهجري: $hd ${hijriAr.getOrElse(hm - 1) { "$hm" }} $hy هـ",
                "الموافق الميلادي: $dayAr ${g.dayOfMonth} ${gregAr[g.monthValue - 1]} ${g.year} م"
            )
        }
    }
}
