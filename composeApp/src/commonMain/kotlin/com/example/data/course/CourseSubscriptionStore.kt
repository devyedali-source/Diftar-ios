package com.example.data.course

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CourseSubscriptionStore {

    private const val PREFS_NAME = "course_subscription_prefs"

    private const val KEY_EXPIRY_PREFIX = "course_expiry_"
    private const val KEY_OWNER_PREFIX = "course_owner_"
    private const val KEY_LAST_REQUEST_PREFIX = "course_last_request_"
    private const val WARN_DAYS = 7

    const val SUBSCRIPTION_DAYS = 45
    const val SUBSCRIPTION_PRICE = "200 أوقية"

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveActivation(context: Context, specId: String, ownerUid: String, days: Int) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        prefs(context).edit()
            .putLong(KEY_EXPIRY_PREFIX + specId, calendar.timeInMillis)
            .putString(KEY_OWNER_PREFIX + specId, ownerUid)
            .apply()
    }

    fun saveActivationUntil(context: Context, specId: String, ownerUid: String, expiryMillis: Long) {
        prefs(context).edit()
            .putLong(KEY_EXPIRY_PREFIX + specId, expiryMillis)
            .putString(KEY_OWNER_PREFIX + specId, ownerUid)
            .apply()
    }

    fun getExpiryMillis(context: Context, specId: String): Long {
        return prefs(context).getLong(KEY_EXPIRY_PREFIX + specId, 0L)
    }

    fun getOwnerUid(context: Context, specId: String): String {
        return prefs(context).getString(KEY_OWNER_PREFIX + specId, "") ?: ""
    }

    fun isUnlocked(context: Context, specId: String, currentUid: String): Boolean {
        val expiry = getExpiryMillis(context, specId)
        if (expiry <= 0L) return false
        val owner = getOwnerUid(context, specId)
        if (owner.isNotBlank() && currentUid.isNotBlank() && owner != currentUid) return false
        return System.currentTimeMillis() < expiry
    }

    fun remainingDays(context: Context, specId: String): Int {
        val expiry = getExpiryMillis(context, specId)
        if (expiry <= 0L) return 0
        val diff = expiry - System.currentTimeMillis()
        if (diff <= 0L) return 0
        val days = diff / (24L * 60L * 60L * 1000L)
        return (days + 1).toInt()
    }

    fun formatExpiryDate(context: Context, specId: String): String {
        val expiry = getExpiryMillis(context, specId)
        if (expiry <= 0L) return ""
        val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.US)
        return formatter.format(Date(expiry))
    }

    fun previewExpiryDateFromNow(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.US)
        return formatter.format(calendar.time)
    }

    fun saveLastRequestId(context: Context, specId: String, requestId: String) {
        prefs(context).edit()
            .putString(KEY_LAST_REQUEST_PREFIX + specId, requestId)
            .apply()
    }

    fun getLastRequestId(context: Context, specId: String): String {
        return prefs(context).getString(KEY_LAST_REQUEST_PREFIX + specId, "") ?: ""
    }

    fun isExpiringSoon(context: Context, specId: String, currentUid: String): Boolean {
        if (!isUnlocked(context, specId, currentUid)) return false
        val left = remainingDays(context, specId)
        return left in 1..WARN_DAYS
    }

    fun clearSpec(context: Context, specId: String) {
        prefs(context).edit()
            .remove(KEY_EXPIRY_PREFIX + specId)
            .remove(KEY_OWNER_PREFIX + specId)
            .apply()
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
