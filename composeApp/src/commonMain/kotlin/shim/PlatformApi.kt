package com.example.compat

/** نتيجة طلب شبكة */
class HttpResult(val code: Int, val body: ByteArray, val headers: Map<String, String>) {
    val isSuccessful: Boolean get() = code in 200..299
    fun text(): String = body.decodeToString()
}

/**
 * كل ما يحتاج إلى نظام الآيفون نفسه يمرّ من هنا.
 * التنفيذ الفعلي في iosMain (PlatformApi.ios.kt).
 */
expect object PlatformApi {
    // التخزين الدائم (بديل SharedPreferences)
    fun prefGet(store: String, key: String): Any?
    fun prefPut(store: String, key: String, value: Any?)
    fun prefKeys(store: String): Set<String>
    fun prefClear(store: String)

    // الملفات المرفقة مع التطبيق (بديل assets)
    fun readBundledFile(path: String): ByteArray?

    // ملفات التطبيق
    fun cacheDir(): String
    fun filesDir(): String
    fun fileExists(path: String): Boolean
    fun readFile(path: String): ByteArray?
    fun writeFile(path: String, data: ByteArray): Boolean
    fun deleteFile(path: String): Boolean
    fun makeDirs(path: String): Boolean
    fun listFiles(path: String): List<String>

    // فتح الروابط والمشاركة
    fun openUrl(url: String): Boolean
    fun shareText(text: String, subject: String?)
    fun shareFile(path: String, mimeType: String?)
    fun copyToClipboard(text: String)
    fun readClipboard(): String?

    // معلومات الجهاز والتطبيق
    fun deviceId(): String
    fun deviceName(): String
    fun appVersionName(): String
    fun appVersionCode(): Long
    fun bundleId(): String
    fun googleServiceValue(key: String): String?

    // الشبكة
    fun isOnline(): Boolean
    fun addNetworkListener(listener: (Boolean) -> Unit)
    fun httpExecute(method: String, url: String, headers: Map<String, String>, body: ByteArray?, timeoutSeconds: Double): HttpResult

    // التاريخ والوقت
    fun currentTimeMillis(): Long
    /** [year, month(1-12), day, hour, minute, second, millis, dayOfWeek(1=Sunday..7), dayOfYear] */
    fun localFields(epochMillis: Long): IntArray
    fun epochFromLocal(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int, millis: Int): Long
    fun hijriFromGregorian(year: Int, month: Int, day: Int): IntArray
    fun gregorianFromHijri(year: Int, month: Int, day: Int): IntArray
    fun formatDate(epochMillis: Long, pattern: String, localeId: String): String
    fun parseDate(text: String, pattern: String, localeId: String): Long?

    // التشفير
    fun digest(algorithm: String, data: ByteArray): ByteArray
    fun gunzip(data: ByteArray): ByteArray
    fun randomUuid(): String

    // الطباعة وPDF
    fun printHtml(html: String, jobName: String, landscape: Boolean, onDone: (Boolean, String?) -> Unit)
    fun shareHtmlAsPdf(html: String, fileName: String, landscape: Boolean, onDone: (Boolean, String?) -> Unit)

    // الإشعارات المحلية
    fun requestNotificationPermission(onResult: (Boolean) -> Unit)
    fun showLocalNotification(title: String, body: String)

    // تسجيل الدخول بحساب Google (OAuth)
    fun startGoogleOAuth(authUrl: String, callbackScheme: String, onResult: (String?, String?) -> Unit)

    // تنفيذ على الخيط الرئيسي
    fun runOnMain(block: () -> Unit)
    fun runOnMainDelayed(delayMillis: Long, block: () -> Unit)
    fun isMainThread(): Boolean
    fun sleepMillis(millis: Long)

    fun log(tag: String, message: String)

    /** مفاتيح اختبار آلي تعمل على المحاكي فقط */
    fun ciFlag(name: String): Boolean
}
