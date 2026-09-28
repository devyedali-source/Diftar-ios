package com.example.data.course

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import android.content.SharedPreferences

object CourseProgressStore {

    private const val PREFS_NAME = "course_progress_prefs"

    private const val KEY_LESSON_SCORE_PREFIX = "lesson_score_"
    private const val KEY_EXAM_SCORE_PREFIX = "exam_score_"

    private const val LESSON_PASS_MARK = 10
    private const val EXAM_UNLOCK_PERCENT = 20

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveLessonScore(context: Context, lessonId: String, score: Int) {
        val current = getLessonScore(context, lessonId)
        if (current != null && current >= score) return
        prefs(context).edit()
            .putInt(KEY_LESSON_SCORE_PREFIX + lessonId, score)
            .apply()
    }

    fun getLessonScore(context: Context, lessonId: String): Int? {
        val p = prefs(context)
        val key = KEY_LESSON_SCORE_PREFIX + lessonId
        if (!p.contains(key)) return null
        return p.getInt(key, 0)
    }

    fun isLessonPassed(context: Context, lessonId: String): Boolean {
        val score = getLessonScore(context, lessonId) ?: return false
        return score >= LESSON_PASS_MARK
    }

    fun saveExamScore(context: Context, examId: String, score: Int) {
        val current = getExamScore(context, examId)
        if (current != null && current >= score) return
        prefs(context).edit()
            .putInt(KEY_EXAM_SCORE_PREFIX + examId, score)
            .apply()
    }

    fun getExamScore(context: Context, examId: String): Int? {
        val p = prefs(context)
        val key = KEY_EXAM_SCORE_PREFIX + examId
        if (!p.contains(key)) return null
        return p.getInt(key, 0)
    }

    fun countPassedLessons(context: Context, subjectName: String): Int {
        val lessons = CourseRepository.getLessons(context, subjectName)
        var passed = 0
        for (lesson in lessons) {
            if (isLessonPassed(context, lesson.id)) passed++
        }
        return passed
    }

    fun subjectProgressPercent(context: Context, subjectName: String): Int {
        val total = CourseRepository.getLessons(context, subjectName).size
        if (total == 0) return 0
        return (countPassedLessons(context, subjectName) * 100) / total
    }

    fun areSubjectExamsUnlocked(context: Context, subjectName: String): Boolean {
        return subjectProgressPercent(context, subjectName) >= EXAM_UNLOCK_PERCENT
    }

    fun lessonsNeededToUnlockExams(context: Context, subjectName: String): Int {
        val total = CourseRepository.getLessons(context, subjectName).size
        if (total == 0) return 0
        val needed = (total * EXAM_UNLOCK_PERCENT + 99) / 100
        val passed = countPassedLessons(context, subjectName)
        val remaining = needed - passed
        return if (remaining > 0) remaining else 0
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
