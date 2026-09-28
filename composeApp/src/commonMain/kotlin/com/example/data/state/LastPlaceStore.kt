package com.example.data.state

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import android.content.SharedPreferences

/**
 * مخزن «آخر مكان»: يحفظ أين كان المستخدم ليعود إليه بعد إغلاق التطبيق.
 * يحفظ قيمًا صغيرة فقط (أرقام ورموز)، لا محتوى.
 * يُمسح كاملًا عند تسجيل الخروج.
 */
object LastPlaceStore {

    const val PREFS_NAME = "last_place_prefs"

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun putString(context: Context, key: String, value: String?) {
        val editor = prefs(context).edit()
        if (value == null) editor.remove(key) else editor.putString(key, value)
        editor.apply()
    }

    fun getString(context: Context, key: String): String? {
        return try { prefs(context).getString(key, null) } catch (e: Exception) { null }
    }

    fun putLong(context: Context, key: String, value: Long?) {
        val editor = prefs(context).edit()
        if (value == null) editor.remove(key) else editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(context: Context, key: String): Long? {
        return try {
            val p = prefs(context)
            if (p.contains(key)) p.getLong(key, 0L) else null
        } catch (e: Exception) { null }
    }

    fun putInt(context: Context, key: String, value: Int?) {
        val editor = prefs(context).edit()
        if (value == null) editor.remove(key) else editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(context: Context, key: String): Int? {
        return try {
            val p = prefs(context)
            if (p.contains(key)) p.getInt(key, 0) else null
        } catch (e: Exception) { null }
    }

    fun putBoolean(context: Context, key: String, value: Boolean) {
        prefs(context).edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context, key: String): Boolean {
        return try { prefs(context).getBoolean(key, false) } catch (e: Exception) { false }
    }

    fun remove(context: Context, key: String) {
        prefs(context).edit().remove(key).apply()
    }

    fun removeWithPrefix(context: Context, prefix: String) {
        val p = prefs(context)
        val editor = p.edit()
        p.all.keys.filter { it.startsWith(prefix) }.forEach { editor.remove(it) }
        editor.apply()
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
