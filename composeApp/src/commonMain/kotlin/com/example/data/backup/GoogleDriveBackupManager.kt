package com.example.data.backup

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.repository.TeacherRepository
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class BackupResult<out T> {
    data class Success<out T>(val data: T, val message: String) : BackupResult<T>()
    data class Error(val errorMessage: String, val throwable: Throwable? = null) : BackupResult<Nothing>()
}

class GoogleDriveAuthException(val statusCode: Int, val rawError: String) : IOException("Google Drive Auth Error $statusCode: $rawError")

class GoogleDriveConsentRequiredException(val consentIntent: android.content.Intent) : IOException("Google Drive consent required")

class GoogleDriveBackupManager(
    private val context: Context,
    private val repository: TeacherRepository
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val backupAdapter = moshi.adapter(BackupData::class.java)

    private var lastFoundFileMarker: String? = null

    private fun currentDeviceId(): String {
        return try {
            android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: "unknown_device"
        } catch (e: Exception) {
            "unknown_device"
        }
    }

    private fun currentDeviceName(): String {
        return try {
            val brand = android.os.Build.MANUFACTURER ?: ""
            val model = android.os.Build.MODEL ?: ""
            val full = "$brand $model".trim()
            if (full.isBlank()) "جهاز غير معروف" else full
        } catch (e: Exception) {
            "جهاز غير معروف"
        }
    }

    private fun readCachedDevices(): List<BackupDeviceInfo> {
        return try {
            val p = context.getSharedPreferences("teacher_settings_prefs", Context.MODE_PRIVATE)
            val raw = p.getString("drive_devices_list", null) ?: return emptyList()
            val arr = org.json.JSONArray(raw)
            val out = mutableListOf<BackupDeviceInfo>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val id = o.optString("deviceId", "")
                if (id.isBlank()) continue
                out.add(
                    BackupDeviceInfo(
                        deviceId = id,
                        deviceName = o.optString("deviceName", ""),
                        lastSyncAt = o.optLong("lastSyncAt", 0L)
                    )
                )
            }
            out
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveDevicesToPrefs(list: List<BackupDeviceInfo>) {
        try {
            val arr = org.json.JSONArray()
            for (d in list) {
                val o = JSONObject()
                o.put("deviceId", d.deviceId)
                o.put("deviceName", d.deviceName)
                o.put("lastSyncAt", d.lastSyncAt)
                arr.put(o)
            }
            val p = context.getSharedPreferences("teacher_settings_prefs", Context.MODE_PRIVATE)
            p.edit().putString("drive_devices_list", arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun withCurrentDevice(base: List<BackupDeviceInfo>): List<BackupDeviceInfo> {
        val myId = currentDeviceId()
        val myName = currentDeviceName()
        val others = base.filter { it.deviceId != myId && it.deviceName != myName }
        val mine = BackupDeviceInfo(
            deviceId = myId,
            deviceName = myName,
            lastSyncAt = System.currentTimeMillis()
        )
        return (listOf(mine) + others).sortedByDescending { it.lastSyncAt }.take(10)
    }

    private fun mergeCurrentDevice(): List<BackupDeviceInfo> {
        return withCurrentDevice(readCachedDevices())
    }

    private fun devicesWithoutCurrent(): List<BackupDeviceInfo> {
        val myId = currentDeviceId()
        val myName = currentDeviceName()
        return readCachedDevices().filter { it.deviceId != myId && it.deviceName != myName }.sortedByDescending { it.lastSyncAt }.take(10)
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun getOAuthAccessToken(currentTokenToInvalidate: String? = null): String {
        val account = GoogleSignIn.getLastSignedInAccount(context)
            ?: throw IllegalStateException("لم يتم تسجيل الدخول بحساب Google. يرجى تسجيل الدخول أولاً.")
        
        val googleAccount = account.account
            ?: throw IllegalStateException("تعذر الحصول على حساب Google لطلب تصريح Drive.")

        val scope = "oauth2:https://www.googleapis.com/auth/drive.file"

        if (!currentTokenToInvalidate.isNullOrBlank()) {
            try {
                GoogleAuthUtil.clearToken(context.applicationContext, currentTokenToInvalidate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return try {
            GoogleAuthUtil.getToken(context.applicationContext, googleAccount, scope)
        } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
            val recoveryIntent = e.intent
            if (recoveryIntent != null) {
                throw GoogleDriveConsentRequiredException(recoveryIntent)
            } else {
                throw IOException("تعذر الحصول على رمز وصول Google Drive (OAuth Token): ${e.localizedMessage}", e)
            }
        } catch (e: Exception) {
            throw IOException("تعذر الحصول على رمز وصول Google Drive (OAuth Token): ${e.localizedMessage}", e)
        }
    }

    private fun getBackupFileName(userEmail: String?, userId: String?): String {
        val rawId = when {
            !userEmail.isNullOrBlank() -> userEmail
            !userId.isNullOrBlank() -> userId
            else -> "default"
        }
        val cleanId = rawId.replace(Regex("[^a-zA-Z0-9_]"), "_").lowercase()
        return "teacher_notebook_backup_$cleanId.json"
    }

    private fun findBackupFileId(accessToken: String, fileName: String): String? {
        lastFoundFileMarker = null
        val url = "https://www.googleapis.com/drive/v3/files?q=name%3D%27$fileName%27%20and%20trashed%3Dfalse&fields=files(id,modifiedTime)"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                if (response.code == 403 || response.code == 401) {
                    throw GoogleDriveAuthException(response.code, errBody)
                }
                throw IOException("فشل البحث عن ملف النسخة الاحتياطية على Google Drive (${response.code}): $errBody")
            }
            val bodyStr = response.body?.string() ?: return null
            val json = JSONObject(bodyStr)
            val filesArray = json.optJSONArray("files")
            if (filesArray != null && filesArray.length() > 0) {
                val fileObj = filesArray.getJSONObject(0)
                val mt = fileObj.optString("modifiedTime", "")
                if (mt.isNotBlank()) {
                    lastFoundFileMarker = mt
                }
                return fileObj.optString("id", null)
            }
        }
        return null
    }

    private fun createBackupFileMetadata(accessToken: String, fileName: String): String {
        val url = "https://www.googleapis.com/drive/v3/files"
        val metadataJson = JSONObject().apply {
            put("name", fileName)
            put("mimeType", "application/json")
            put("description", "دفتر المعلم الموريتاني - نسخة احتياطية لسجلات المعلم والدرجات والأقسام")
            put("parents", org.json.JSONArray().apply { put("root") })
        }.toString()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .post(metadataJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                if (response.code == 403 || response.code == 401) {
                    throw GoogleDriveAuthException(response.code, bodyStr)
                }
                throw IOException("فشل إنشاء ملف النسخة الاحتياطية على Google Drive (${response.code}): $bodyStr")
            }
            val json = JSONObject(bodyStr)
            return json.getString("id")
        }
    }

    private fun uploadBackupFileContent(accessToken: String, fileId: String, jsonContent: String): Long? {
        val url = "https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media&fields=id,size,modifiedTime"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .patch(jsonContent.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val okBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                if (response.code == 403 || response.code == 401) {
                    throw GoogleDriveAuthException(response.code, okBody)
                }
                throw IOException("فشل رفع محتوى النسخة الاحتياطية على Google Drive (${response.code}): $okBody")
            }
            return try {
                val obj = JSONObject(okBody)
                val mt = obj.optString("modifiedTime", "")
                if (mt.isNotBlank()) {
                    lastFoundFileMarker = mt
                }
                obj.optString("size", "").toLongOrNull()
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun downloadBackupFileContent(accessToken: String, fileId: String): String {
        val url = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                if (response.code == 403 || response.code == 401) {
                    throw GoogleDriveAuthException(response.code, errBody)
                }
                throw IOException("فشل تنزيل ملف النسخة الاحتياطية من Google Drive (${response.code}): $errBody")
            }
            return response.body?.string() ?: throw IOException("ملف النسخة الاحتياطية فارغ")
        }
    }

    suspend fun createAndUploadBackup(
        currentUserId: String? = null,
        currentUserEmail: String? = null,
        isLogout: Boolean = false,
        onProgress: ((Int, String) -> Unit)? = null
    ): BackupResult<Unit> = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext BackupResult.Error("عذراً، لا يوجد اتصال بالإنترنت لإجراء النسخ الاحتياطي على Google Drive.")
        }

        val localIsEmpty = try {
            repository.getAllClassSectionsList().isEmpty() &&
                repository.getAllStudentsList().isEmpty() &&
                repository.getAllGradesList().isEmpty()
        } catch (e: Exception) {
            false
        }
        if (localIsEmpty) {
            return@withContext BackupResult.Success(
                data = Unit,
                message = "لا توجد بيانات محلية لرفعها. تم الإبقاء على النسخة الاحتياطية الموجودة في Google Drive دون تغيير."
            )
        }

        val account = GoogleSignIn.getLastSignedInAccount(context)
        val effectiveEmail = currentUserEmail ?: account?.email
        val effectiveUserId = currentUserId ?: account?.id
        val fileName = getBackupFileName(effectiveEmail, effectiveUserId)

        onProgress?.invoke(15, "جاري الحصول على الاتصال وتفويض Google Drive...")
        var token = try {
            getOAuthAccessToken()
        } catch (e: Exception) {
            return@withContext BackupResult.Error(e.localizedMessage ?: "فشل التفويض لحساب Google.", e)
        }

        suspend fun executeBackupFlow(authToken: String): BackupResult<Unit> {
            onProgress?.invoke(35, "جاري تجميع سجلاتك من الجهاز...")
            val classSections = repository.getAllClassSectionsList()
            val students = repository.getAllStudentsList()
            val subjects = repository.getAllSubjectsList()
            val grades = repository.getAllGradesList()
            val customizations = repository.getAllCustomizationsList()
            val manualTermAverages = repository.getAllManualTermAveragesList()
            val localNotifications = repository.getAllLocalNotificationsList()

            val settingsKeys = listOf(
                "stamp_teacher_name", "stamp_teacher_role", "stamp_teacher_shape", "stamp_teacher_financial_id", "stamp_teacher_show_in_reports",
                "stamp_principal_name", "stamp_principal_role", "stamp_principal_shape", "stamp_principal_financial_id", "stamp_principal_show_in_reports",
                "fail_bound", "pass_bound", "acceptable_bound", "good_bound", "very_good_bound",
                "fail_bound_t1", "pass_bound_t1", "acceptable_bound_t1", "good_bound_t1", "very_good_bound_t1",
                "fail_bound_t2", "pass_bound_t2", "acceptable_bound_t2", "good_bound_t2", "very_good_bound_t2",
                "fail_bound_t3", "pass_bound_t3", "acceptable_bound_t3", "good_bound_t3", "very_good_bound_t3",
                "is_out_of_ten", "use_final_exam_formula", "fail_note_exam_1_and_2", "app_language"
            )
            val teacherPrefs = context.getSharedPreferences("teacher_settings_prefs", Context.MODE_PRIVATE)
            val teacherSettings = mutableMapOf<String, String>()
            for (k in settingsKeys) {
                val v = teacherPrefs.all[k]
                if (v != null) {
                    teacherSettings[k] = v.toString()
                }
            }

            onProgress?.invoke(55, "جاري تحزيم وتشفير بيانات النسخة الاحتياطية...")
            val mergedDevices = if (isLogout) devicesWithoutCurrent() else mergeCurrentDevice()
            val backupData = BackupData(
                version = 1,
                timestamp = System.currentTimeMillis(),
                userId = effectiveUserId,
                userEmail = effectiveEmail,
                deviceId = currentDeviceId(),
                deviceName = currentDeviceName(),
                devices = mergedDevices,
                classSections = classSections,
                students = students,
                subjects = subjects,
                grades = grades,
                customizations = customizations,
                manualTermAverages = manualTermAverages,
                teacherSettings = teacherSettings,
                localNotifications = localNotifications
            )

            val jsonContent = backupAdapter.toJson(backupData)

            onProgress?.invoke(75, "جاري تجهيز الملف المخصص في Google Drive...")
            var fileId = findBackupFileId(authToken, fileName)
            val remoteMarker = lastFoundFileMarker
            if (fileId.isNullOrBlank()) {
                fileId = createBackupFileMetadata(authToken, fileName)
            }

            val markerPrefs = context.getSharedPreferences("teacher_settings_prefs", Context.MODE_PRIVATE)
            val storedMarker = markerPrefs.getString("drive_backup_marker", null)
            if (!storedMarker.isNullOrBlank() && !remoteMarker.isNullOrBlank() && storedMarker != remoteMarker) {
                return BackupResult.Error(
                    "لم يتم رفع أي شيء حفاظا على بياناتك: جهاز آخر زامن هذا الحساب بعد آخر مزامنة على هذا الجهاز. افتح التطبيق على ذلك الجهاز، أو سجّل الخروج من أحد الجهازين، حتى لا يمحو جهاز عمل جهاز آخر."
                )
            }

            onProgress?.invoke(90, "جاري رفع محتوى السجلات بالسحابة...")

            fun uploadAndVerify(): Boolean {
                return try {
                    val expectedSize = jsonContent.toByteArray(Charsets.UTF_8).size.toLong()
                    val reportedSize = uploadBackupFileContent(authToken, fileId, jsonContent)
                    if (reportedSize != null) {
                        reportedSize == expectedSize
                    } else {
                        val downloadedContent = downloadBackupFileContent(authToken, fileId)
                        val downloadedBackupData = backupAdapter.fromJson(downloadedContent)
                        if (downloadedBackupData == null) {
                            false
                        } else {
                            downloadedBackupData.classSections.size == classSections.size &&
                                downloadedBackupData.students.size == students.size &&
                                downloadedBackupData.grades.size == grades.size &&
                                downloadedBackupData.subjects.size == subjects.size
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            var verified = uploadAndVerify()
            if (!verified) {
                onProgress?.invoke(92, "تعذر التحقق من النسخة، جاري إعادة المحاولة...")
                verified = uploadAndVerify()
            }
            if (!verified) {
                return BackupResult.Error("تعذر رفع نسخة سليمة إلى Google Drive بعد محاولتين. تحقق من اتصالك وأعد المحاولة.")
            }

            onProgress?.invoke(100, "اكتمل الرفع بنجاح 100%!")

            val prefs = context.getSharedPreferences("teacher_settings_prefs", Context.MODE_PRIVATE)
            prefs.edit().putLong("last_google_drive_backup_time", System.currentTimeMillis()).apply()
            val uploadedMarker = lastFoundFileMarker
            if (!uploadedMarker.isNullOrBlank()) {
                prefs.edit().putString("drive_backup_marker", uploadedMarker).apply()
            }
            saveDevicesToPrefs(mergedDevices)

            return BackupResult.Success(
                data = Unit,
                message = "تم إنشاء ورفع النسخة الاحتياطية بنجاح لحساب (${effectiveEmail ?: "الحالي"}) على Google Drive 📤☁️\n(الأقسام: ${classSections.size}، الطلاب: ${students.size}، الدرجات: ${grades.size})"
            )
        }

        try {
            executeBackupFlow(token)
        } catch (authEx: GoogleDriveAuthException) {
            // Attempt auto-invalidation of cached token and retry once
            try {
                val newToken = getOAuthAccessToken(currentTokenToInvalidate = token)
                executeBackupFlow(newToken)
            } catch (e: Exception) {
                e.printStackTrace()
                BackupResult.Error(
                    "⚠️ فشل الاتصال بـ Google Drive (رمز الخطأ 403: تم رفض الوصول).\n\n" +
                    "السبب:\n" +
                    "1. لم تقم بمنح إذن الوصول لـ Google Drive عند تسجيل الدخول.\n" +
                    "2. أو أن مكتبة (Google Drive API) غير مفعلة في Google Cloud Console للمشروع.\n\n" +
                    "الحل:\n" +
                    "• يرجى تسجيل الخروج ثم إعادة تسجيل الدخول واختيار السماح بالوصول لـ Drive."
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            BackupResult.Error(
                errorMessage = e.localizedMessage ?: "حدث خطأ غير متوقع أثناء النسخ الاحتياطي.",
                throwable = e
            )
        }
    }

    suspend fun downloadAndRestoreBackup(
        currentUserId: String? = null,
        currentUserEmail: String? = null,
        onProgress: ((Int, String) -> Unit)? = null
    ): BackupResult<Unit> = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext BackupResult.Error("عذراً، لا يوجد اتصال بالإنترنت لاستعادة النسخة الاحتياطية من Google Drive.")
        }

        val account = GoogleSignIn.getLastSignedInAccount(context)
        val effectiveEmail = currentUserEmail ?: account?.email
        val effectiveUserId = currentUserId ?: account?.id
        val fileName = getBackupFileName(effectiveEmail, effectiveUserId)

        onProgress?.invoke(15, "جاري التحقق من الهوية والاتصال بحساب Google...")
        var token = try {
            getOAuthAccessToken()
        } catch (e: Exception) {
            return@withContext BackupResult.Error(e.localizedMessage ?: "فشل التفويض لحساب Google.", e)
        }

        suspend fun executeRestoreFlow(authToken: String): BackupResult<Unit> {
            onProgress?.invoke(35, "جاري البحث عن ملف النسخة الاحتياطية الخاص بك على Google Drive...")
            var fileId = findBackupFileId(authToken, fileName)
            if (fileId.isNullOrBlank()) {
                fileId = findBackupFileId(authToken, "teacher_notebook_backup.json")
            }

            if (fileId.isNullOrBlank()) {
                return BackupResult.Error("لم يتم العثور على أي ملف نسخة احتياطية خاص بحسابك (${effectiveEmail ?: "الحالي"}) في Google Drive.")
            }

            onProgress?.invoke(60, "جاري تنزيل ملف السجلات والبيانات...")
            val jsonContent = downloadBackupFileContent(authToken, fileId)

            onProgress?.invoke(80, "جاري قراءة البيانات وتأكيد سلامتها...")
            val backupData = backupAdapter.fromJson(jsonContent)
                ?: return BackupResult.Error("ملف النسخة الاحتياطية التابع لـ Google Drive غير صالح أو تالف.")

            if (!effectiveEmail.isNullOrBlank() && !backupData.userEmail.isNullOrBlank()) {
                if (!effectiveEmail.equals(backupData.userEmail, ignoreCase = true)) {
                    return BackupResult.Error("⚠️ تنبيه أمان: النسخة الاحتياطية الموجودة تخص حساباً آخر (${backupData.userEmail}).\nلا يمكن استعادتها للحساب الحالي ($effectiveEmail) لمنع تداخل بيانات الحسابين.")
                }
            }

            onProgress?.invoke(90, "جاري استعادة وتثبيت السجلات في التطبيق...")
            repository.restoreDatabase(
                sections = backupData.classSections,
                students = backupData.students,
                subjects = backupData.subjects,
                grades = backupData.grades,
                customizations = backupData.customizations,
                manualTermAverages = backupData.manualTermAverages,
                localNotifications = backupData.localNotifications
            )

            try {
                val restorePrefs = context.getSharedPreferences("teacher_settings_prefs", Context.MODE_PRIVATE)
                val ed = restorePrefs.edit()
                for ((k, v) in backupData.teacherSettings) {
                    when {
                        v == "true" || v == "false" -> ed.putBoolean(k, v.toBoolean())
                        v.toFloatOrNull() != null && v.contains(".") -> ed.putFloat(k, v.toFloat())
                        v.toIntOrNull() != null -> ed.putInt(k, v.toInt())
                        else -> ed.putString(k, v)
                    }
                }
                ed.apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                val syncTimePrefs = context.getSharedPreferences("teacher_settings_prefs", Context.MODE_PRIVATE)
                val storedTime = syncTimePrefs.getLong("last_google_drive_backup_time", 0L)
                if (backupData.timestamp > storedTime) {
                    syncTimePrefs.edit().putLong("last_google_drive_backup_time", backupData.timestamp).apply()
                }
                val restoredMarker = lastFoundFileMarker
                if (!restoredMarker.isNullOrBlank()) {
                    syncTimePrefs.edit().putString("drive_backup_marker", restoredMarker).apply()
                }
                saveDevicesToPrefs(withCurrentDevice(backupData.devices))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            onProgress?.invoke(100, "اكتملت الاستعادة بنجاح 100%!")

            return BackupResult.Success(
                data = Unit,
                message = "تمت استعادة جميع البيانات بنجاح من Google Drive لحساب (${effectiveEmail ?: "الحالي"}) 📥🎉\n(الأقسام: ${backupData.classSections.size}، الطلاب: ${backupData.students.size}، الدرجات: ${backupData.grades.size})"
            )
        }

        try {
            executeRestoreFlow(token)
        } catch (authEx: GoogleDriveAuthException) {
            try {
                val newToken = getOAuthAccessToken(currentTokenToInvalidate = token)
                executeRestoreFlow(newToken)
            } catch (e: Exception) {
                e.printStackTrace()
                BackupResult.Error(
                    "⚠️ فشل الاتصال بـ Google Drive (رمز الخطأ 403: تم رفض الوصول).\n\n" +
                    "السبب:\n" +
                    "1. لم تقم بمنح إذن الوصول لـ Google Drive عند تسجيل الدخول.\n" +
                    "2. أو أن مكتبة (Google Drive API) غير مفعلة في Google Cloud Console للمشروع.\n\n" +
                    "الحل:\n" +
                    "• يرجى تسجيل الخروج ثم إعادة تسجيل الدخول واختيار السماح بالوصول لـ Drive."
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            BackupResult.Error(
                errorMessage = e.localizedMessage ?: "حدث خطأ أثناء استعادة النسخة الاحتياطية.",
                throwable = e
            )
        }
    }
}
