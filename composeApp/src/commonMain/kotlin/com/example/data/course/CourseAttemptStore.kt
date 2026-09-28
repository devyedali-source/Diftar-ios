package com.example.data.course

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import android.content.SharedPreferences

object CourseAttemptStore {

    private const val PREFS_NAME = "course_attempt_prefs"

    private const val KEY_START_PREFIX = "sim_start_"
    private const val KEY_ANSWERS_PREFIX = "sim_answers_"
    private const val KEY_MARKED_PREFIX = "sim_marked_"

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getStartMillis(context: Context, examId: String): Long {
        return prefs(context).getLong(KEY_START_PREFIX + examId, 0L)
    }

    fun startAttempt(context: Context, examId: String): Long {
        val now = System.currentTimeMillis()
        prefs(context).edit()
            .putLong(KEY_START_PREFIX + examId, now)
            .remove(KEY_ANSWERS_PREFIX + examId)
            .remove(KEY_MARKED_PREFIX + examId)
            .apply()
        return now
    }

    fun hasActiveAttempt(context: Context, examId: String): Boolean {
        return getStartMillis(context, examId) > 0L
    }

    fun remainingSeconds(context: Context, examId: String, durationSeconds: Int): Int {
        val start = getStartMillis(context, examId)
        if (start <= 0L) return durationSeconds
        val elapsed = (System.currentTimeMillis() - start) / 1000L
        val left = durationSeconds - elapsed
        if (left <= 0L) return 0
        if (left > durationSeconds) return durationSeconds
        return left.toInt()
    }

    fun saveAnswers(context: Context, examId: String, answers: Map<Int, Set<Int>>) {
        val builder = StringBuilder()
        for (entry in answers) {
            if (entry.value.isEmpty()) continue
            if (builder.isNotEmpty()) builder.append(";")
            builder.append(entry.key)
            builder.append(":")
            builder.append(entry.value.sorted().joinToString(","))
        }
        prefs(context).edit()
            .putString(KEY_ANSWERS_PREFIX + examId, builder.toString())
            .apply()
    }

    fun loadAnswers(context: Context, examId: String): Map<Int, Set<Int>> {
        val raw = prefs(context).getString(KEY_ANSWERS_PREFIX + examId, "") ?: ""
        if (raw.isBlank()) return emptyMap()
        val result = HashMap<Int, Set<Int>>()
        for (part in raw.split(";")) {
            if (part.isBlank()) continue
            val pieces = part.split(":")
            if (pieces.size != 2) continue
            val questionIndex = pieces[0].toIntOrNull() ?: continue
            val options = HashSet<Int>()
            for (piece in pieces[1].split(",")) {
                val optionIndex = piece.toIntOrNull()
                if (optionIndex != null) options.add(optionIndex)
            }
            if (options.isNotEmpty()) result[questionIndex] = options
        }
        return result
    }

    fun saveMarked(context: Context, examId: String, marked: Set<Int>) {
        prefs(context).edit()
            .putString(KEY_MARKED_PREFIX + examId, marked.sorted().joinToString(","))
            .apply()
    }

    fun loadMarked(context: Context, examId: String): Set<Int> {
        val raw = prefs(context).getString(KEY_MARKED_PREFIX + examId, "") ?: ""
        if (raw.isBlank()) return emptySet()
        val result = HashSet<Int>()
        for (piece in raw.split(",")) {
            val value = piece.toIntOrNull()
            if (value != null) result.add(value)
        }
        return result
    }

    fun clearAttempt(context: Context, examId: String) {
        prefs(context).edit()
            .remove(KEY_START_PREFIX + examId)
            .remove(KEY_ANSWERS_PREFIX + examId)
            .remove(KEY_MARKED_PREFIX + examId)
            .apply()
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }

    // ===== موضع اختبار الدرس وامتحان المادة (رقم السؤال + عدد الإجابات الصحيحة) =====
    private const val KEY_POS_INDEX_PREFIX = "pos_index_"
    private const val KEY_POS_CORRECT_PREFIX = "pos_correct_"

    fun saveTestPosition(context: Context, testId: String, index: Int, correctCount: Int) {
        prefs(context).edit()
            .putInt(KEY_POS_INDEX_PREFIX + testId, index)
            .putInt(KEY_POS_CORRECT_PREFIX + testId, correctCount)
            .apply()
    }

    /** يعيد (رقم السؤال، عدد الإجابات الصحيحة) أو null إن لم يوجد موضع محفوظ. */
    fun loadTestPosition(context: Context, testId: String): Pair<Int, Int>? {
        return try {
            val p = prefs(context)
            if (!p.contains(KEY_POS_INDEX_PREFIX + testId)) return null
            val index = p.getInt(KEY_POS_INDEX_PREFIX + testId, 0)
            val correct = p.getInt(KEY_POS_CORRECT_PREFIX + testId, 0)
            Pair(index, correct)
        } catch (e: Exception) {
            null
        }
    }

    fun clearTestPosition(context: Context, testId: String) {
        prefs(context).edit()
            .remove(KEY_POS_INDEX_PREFIX + testId)
            .remove(KEY_POS_CORRECT_PREFIX + testId)
            .apply()
    }
}
