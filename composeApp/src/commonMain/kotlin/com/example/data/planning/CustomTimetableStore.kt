package com.example.data.planning

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * الجدول الزمني الشخصي للمعلم (محلي على هذا الجهاز فقط، لكل قسم).
 * الجدول الرسمي لا يُعدَّل أبداً؛ إن لم يوجد جدول شخصي يُستعمل الرسمي.
 */
object CustomTimetableStore {
    private const val PREFS = "custom_timetables_v1"

    private fun key(classId: Long) = "class_$classId"

    fun load(context: Context, classId: Long): List<OfficialTimetableSlot>? {
        return try {
            val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key(classId), null)
                ?: return null
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                OfficialTimetableSlot(
                    id = o.getString("id"),
                    level = o.optInt("level", 1),
                    day = DayOfWeekAr.valueOf(o.getString("day")),
                    periodType = TimetablePeriodType.valueOf(o.getString("period")),
                    slotIndex = o.optInt("index", 0),
                    subject = o.optString("subject", ""),
                    branch = o.optString("branch", ""),
                    durationMinutes = o.optInt("duration", 30),
                    isFrench = o.optBoolean("french", false)
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun save(context: Context, classId: Long, slots: List<OfficialTimetableSlot>) {
        try {
            val arr = JSONArray()
            slots.forEach { s ->
                arr.put(JSONObject().apply {
                    put("id", s.id)
                    put("level", s.level)
                    put("day", s.day.name)
                    put("period", s.periodType.name)
                    put("index", s.slotIndex)
                    put("subject", s.subject)
                    put("branch", s.branch)
                    put("duration", s.durationMinutes)
                    put("french", s.isFrench)
                })
            }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(key(classId), arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clear(context: Context, classId: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(key(classId)).apply()
    }
}
