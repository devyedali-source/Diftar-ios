package com.example.data.planning

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class AnnualPlanningProgressManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("annual_planning_prefs", Context.MODE_PRIVATE)

    private val _progressFlow = MutableStateFlow<Map<Long, AnnualPlanningProgress>>(emptyMap())
    val progressFlow: StateFlow<Map<Long, AnnualPlanningProgress>> = _progressFlow.asStateFlow()

    fun getProgress(classId: Long): AnnualPlanningProgress {
        val completedWeeksJson = prefs.getString("completed_weeks_$classId", "[]") ?: "[]"
        val taughtLessonsJson = prefs.getString("taught_lessons_$classId", "[]") ?: "[]"
        val lastWeek = if (prefs.contains("last_week_$classId")) prefs.getInt("last_week_$classId", 1) else null

        val completedWeeks = mutableSetOf<Int>()
        try {
            val arr = JSONArray(completedWeeksJson)
            for (i in 0 until arr.length()) {
                completedWeeks.add(arr.getInt(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val taughtLessons = mutableSetOf<String>()
        try {
            val arr = JSONArray(taughtLessonsJson)
            for (i in 0 until arr.length()) {
                taughtLessons.add(arr.getString(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return AnnualPlanningProgress(
            completedWeeks = completedWeeks,
            taughtLessonIds = taughtLessons,
            lastVisitedWeek = lastWeek
        )
    }

    fun toggleWeekCompletion(classId: Long, week: Int): Boolean {
        val current = getProgress(classId)
        val updatedWeeks = current.completedWeeks.toMutableSet()
        val isNowCompleted: Boolean
        if (updatedWeeks.contains(week)) {
            updatedWeeks.remove(week)
            isNowCompleted = false
        } else {
            updatedWeeks.add(week)
            isNowCompleted = true
        }

        val arr = JSONArray()
        updatedWeeks.forEach { arr.put(it) }
        prefs.edit().putString("completed_weeks_$classId", arr.toString()).apply()

        updateFlow(classId, current.copy(completedWeeks = updatedWeeks))
        return isNowCompleted
    }

    fun toggleLessonTaught(classId: Long, lessonId: String): Boolean {
        val current = getProgress(classId)
        val updatedLessons = current.taughtLessonIds.toMutableSet()
        val isNowTaught: Boolean
        if (updatedLessons.contains(lessonId)) {
            updatedLessons.remove(lessonId)
            isNowTaught = false
        } else {
            updatedLessons.add(lessonId)
            isNowTaught = true
        }

        val arr = JSONArray()
        updatedLessons.forEach { arr.put(it) }
        prefs.edit().putString("taught_lessons_$classId", arr.toString()).apply()

        updateFlow(classId, current.copy(taughtLessonIds = updatedLessons))
        return isNowTaught
    }

    fun setLastVisitedWeek(classId: Long, week: Int) {
        prefs.edit().putInt("last_week_$classId", week).apply()
        val current = getProgress(classId)
        updateFlow(classId, current.copy(lastVisitedWeek = week))
    }

    fun getSchoolStartDateString(classId: Long, academicYear: String): String {
        val customClassDate = prefs.getString("school_start_date_$classId", "")
        if (!customClassDate.isNullOrBlank()) return customClassDate

        val customYearDate = prefs.getString("school_start_date_year_${normalizeYearKey(academicYear)}", "")
        if (!customYearDate.isNullOrBlank()) return customYearDate

        // Dynamic default: First Monday of October of academic start year
        val startYear = parseAcademicStartYear(academicYear)
        return "$startYear-10-06" // Default October opening
    }

    fun setSchoolStartDate(classId: Long, academicYear: String, dateString: String) {
        prefs.edit()
            .putString("school_start_date_$classId", dateString)
            .putString("school_start_date_year_${normalizeYearKey(academicYear)}", dateString)
            .apply()
    }

    fun calculateCurrentWeekFromStartDate(classId: Long, academicYear: String): Int {
        val dateStr = getSchoolStartDateString(classId, academicYear)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return try {
            val startDate = sdf.parse(dateStr) ?: return 1
            val now = java.util.Date()
            val diffMs = now.time - startDate.time
            if (diffMs < 0) return 1
            val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
            val calculatedWeek = (diffDays / 7) + 1
            calculatedWeek.coerceIn(1, 38)
        } catch (e: Exception) {
            1
        }
    }

    fun getWeekDateRangeDisplay(classId: Long, academicYear: String, week: Int): String {
        val dateStr = getSchoolStartDateString(classId, academicYear)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val arabicFormat = java.text.SimpleDateFormat("d MMMM", java.util.Locale("ar"))
        return try {
            val startDate = sdf.parse(dateStr) ?: return "الأسبوع $week"
            val calendar = java.util.Calendar.getInstance()
            calendar.time = startDate
            // Add (week - 1) weeks
            calendar.add(java.util.Calendar.DAY_OF_YEAR, (week - 1) * 7)
            val weekStartDate = calendar.time
            // Add 4 days for Friday of the school week
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 4)
            val weekEndDate = calendar.time

            "من ${arabicFormat.format(weekStartDate)} إلى ${arabicFormat.format(weekEndDate)}"
        } catch (e: Exception) {
            "الأسبوع $week"
        }
    }

    private fun normalizeYearKey(year: String): String {
        return year.trim().replace(" ", "").replace("/", "-")
    }

    private fun parseAcademicStartYear(academicYear: String): Int {
        val regex = Regex("""\b(20\d{2})\b""")
        val match = regex.find(academicYear)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 2025
    }

    private fun updateFlow(classId: Long, progress: AnnualPlanningProgress) {
        val map = _progressFlow.value.toMutableMap()
        map[classId] = progress
        _progressFlow.value = map
    }

    companion object {
        @Volatile
        private var instance: AnnualPlanningProgressManager? = null

        fun getInstance(context: Context): AnnualPlanningProgressManager {
            return instance ?: synchronized(this) {
                instance ?: AnnualPlanningProgressManager(context.applicationContext).also { instance = it }
            }
        }

        fun getProgress(context: Context, classId: Long): AnnualPlanningProgress =
            getInstance(context).getProgress(classId)

        fun toggleWeekCompletion(context: Context, classId: Long, week: Int): Boolean =
            getInstance(context).toggleWeekCompletion(classId, week)

        fun toggleLessonTaught(context: Context, classId: Long, lessonId: String): Boolean =
            getInstance(context).toggleLessonTaught(classId, lessonId)

        fun setLastVisitedWeek(context: Context, classId: Long, week: Int) =
            getInstance(context).setLastVisitedWeek(classId, week)

        fun getSchoolStartDateString(context: Context, classId: Long, academicYear: String): String =
            getInstance(context).getSchoolStartDateString(classId, academicYear)

        fun setSchoolStartDate(context: Context, classId: Long, academicYear: String, dateString: String) =
            getInstance(context).setSchoolStartDate(classId, academicYear, dateString)

        fun calculateCurrentWeekFromStartDate(context: Context, classId: Long, academicYear: String): Int =
            getInstance(context).calculateCurrentWeekFromStartDate(classId, academicYear)

        fun getWeekDateRangeDisplay(context: Context, classId: Long, academicYear: String, week: Int): String =
            getInstance(context).getWeekDateRangeDisplay(classId, academicYear, week)
    }
}
