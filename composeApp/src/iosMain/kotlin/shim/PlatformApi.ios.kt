@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.example.compat

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.CoreCrypto.CC_MD5
import platform.CoreCrypto.CC_MD5_DIGEST_LENGTH
import platform.CoreCrypto.CC_SHA1
import platform.CoreCrypto.CC_SHA1_DIGEST_LENGTH
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSCalendarIdentifierIslamicUmmAlQura
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitNanosecond
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitWeekday
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSLocale
import platform.Foundation.NSMutableData
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSThread
import platform.Foundation.NSTimeZone
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSValue
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetPDFContextBounds
import platform.UIKit.UIPasteboard
import platform.UIKit.UIPrintInfo
import platform.UIKit.UIPrintInfoOrientation
import platform.UIKit.UIPrintInfoOutputType
import platform.UIKit.UIPrintInteractionController
import platform.UIKit.UIPrintPageRenderer
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.valueWithCGRect
import platform.UIKit.popoverPresentationController
import platform.UIKit.viewPrintFormatter
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.DISPATCH_TIME_FOREVER
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSObject
import platform.darwin.dispatch_after
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_semaphore_create
import platform.darwin.dispatch_semaphore_signal
import platform.darwin.dispatch_semaphore_wait
import platform.darwin.dispatch_time
import platform.posix.memcpy

// ---------------------------------------------------------------------------
// أدوات تحويل
// ---------------------------------------------------------------------------

internal fun NSData.toByteArray(): ByteArray {
    val len = this.length.toInt()
    if (len == 0) return ByteArray(0)
    val out = ByteArray(len)
    out.usePinned { memcpy(it.addressOf(0), this.bytes, this.length) }
    return out
}

internal fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return usePinned { NSData.create(bytes = it.addressOf(0), length = this.size.convert()) }
}

internal fun topViewController(): UIViewController? {
    var vc = keyWindow()?.rootViewController
    while (vc?.presentedViewController != null) vc = vc.presentedViewController
    return vc
}

@Suppress("DEPRECATION")
internal fun keyWindow(): UIWindow? = UIApplication.sharedApplication.keyWindow
    ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow

private val retained = mutableListOf<Any>()

private class OAuthAnchor : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
    override fun presentationAnchorForWebAuthenticationSession(session: ASWebAuthenticationSession): ASPresentationAnchor =
        keyWindow() ?: UIWindow()
}

actual object PlatformApi {

    // ------------------------------------------------------------ التخزين

    private val defaults get() = NSUserDefaults.standardUserDefaults
    private fun k(store: String, key: String) = "$store::$key"

    actual fun prefGet(store: String, key: String): Any? {
        val raw = defaults.stringForKey(k(store, key)) ?: return null
        if (raw.length < 2 || raw[1] != ':') return raw
        val body = raw.substring(2)
        return when (raw[0]) {
            's' -> body
            'i' -> body.toIntOrNull()
            'l' -> body.toLongOrNull()
            'f' -> body.toFloatOrNull()
            'd' -> body.toDoubleOrNull()
            'b' -> body == "true"
            'a' -> org.json.JSONArray(body).toList().map { it.toString() }
            else -> body
        }
    }

    actual fun prefPut(store: String, key: String, value: Any?) {
        val encoded = when (value) {
            null -> null
            is String -> "s:$value"
            is Int -> "i:$value"
            is Long -> "l:$value"
            is Float -> "f:$value"
            is Double -> "d:$value"
            is Boolean -> "b:$value"
            is Collection<*> -> "a:" + org.json.JSONArray(value.map { it.toString() }).toString()
            else -> "s:$value"
        }
        if (encoded == null) defaults.removeObjectForKey(k(store, key)) else defaults.setObject(encoded, forKey = k(store, key))
    }

    actual fun prefKeys(store: String): Set<String> {
        val prefix = "$store::"
        return defaults.dictionaryRepresentation().keys.mapNotNull { (it as? String)?.takeIf { s -> s.startsWith(prefix) }?.removePrefix(prefix) }.toSet()
    }

    actual fun prefClear(store: String) {
        prefKeys(store).forEach { defaults.removeObjectForKey(k(store, it)) }
    }

    // ------------------------------------------------------------ الملفات

    actual fun readBundledFile(path: String): ByteArray? {
        val base = NSBundle.mainBundle.resourcePath ?: return null
        val candidates = listOf(
            "$base/compose-resources/composeResources/com.example.resources/files/$path",
            "$base/composeResources/com.example.resources/files/$path",
            "$base/files/$path",
            "$base/$path"
        )
        for (c in candidates) {
            val data = NSData.dataWithContentsOfFile(c) ?: continue
            return data.toByteArray()
        }
        return null
    }

    private fun dir(kind: ULong): String =
        (NSSearchPathForDirectoriesInDomains(kind, NSUserDomainMask, true).firstOrNull() as? String) ?: "/tmp"

    actual fun cacheDir(): String = dir(NSCachesDirectory)
    actual fun filesDir(): String = dir(NSDocumentDirectory)
    actual fun fileExists(path: String): Boolean = NSFileManager.defaultManager.fileExistsAtPath(path)
    actual fun readFile(path: String): ByteArray? = NSData.dataWithContentsOfFile(path)?.toByteArray()
    actual fun writeFile(path: String, data: ByteArray): Boolean {
        makeDirs(path.substringBeforeLast('/'))
        return data.toNSData().writeToFile(path, atomically = true)
    }
    actual fun deleteFile(path: String): Boolean = NSFileManager.defaultManager.removeItemAtPath(path, error = null)
    actual fun makeDirs(path: String): Boolean =
        NSFileManager.defaultManager.createDirectoryAtPath(path, withIntermediateDirectories = true, attributes = null, error = null)
    actual fun listFiles(path: String): List<String> =
        NSFileManager.defaultManager.contentsOfDirectoryAtPath(path, error = null)?.mapNotNull { it as? String } ?: emptyList()

    // ------------------------------------------------------------ الروابط والمشاركة

    actual fun openUrl(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: NSURL.URLWithString(url.replace(" ", "%20")) ?: return false
        runOnMain {
            UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any?>(), completionHandler = null)
        }
        return true
    }

    private fun presentShare(items: List<Any>) {
        runOnMain {
            val top = topViewController() ?: return@runOnMain
            val vc = UIActivityViewController(activityItems = items, applicationActivities = null)
            vc.popoverPresentationController?.let { pop ->
                pop.sourceView = top.view
                top.view.bounds.useContents {
                    pop.sourceRect = CGRectMake(size.width / 2.0, size.height / 2.0, 1.0, 1.0)
                }
            }
            top.presentViewController(vc, animated = true, completion = null)
        }
    }

    actual fun shareText(text: String, subject: String?) = presentShare(listOf(text))

    actual fun shareFile(path: String, mimeType: String?) = presentShare(listOf(NSURL.fileURLWithPath(path)))

    actual fun copyToClipboard(text: String) { UIPasteboard.generalPasteboard.string = text }
    actual fun readClipboard(): String? = UIPasteboard.generalPasteboard.string

    // ------------------------------------------------------------ الجهاز والتطبيق

    actual fun deviceId(): String = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown_device"
    actual fun deviceName(): String {
        val d = UIDevice.currentDevice
        return "${d.model} (${d.systemName} ${d.systemVersion})"
    }
    actual fun appVersionName(): String = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "0.0.0"
    actual fun appVersionCode(): Long = (NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String)?.toLongOrNull() ?: 1L
    actual fun bundleId(): String = NSBundle.mainBundle.bundleIdentifier ?: "com.elyedali.schoolmanager"

    private val googleConfig: Map<String, String> by lazy {
        val path = NSBundle.mainBundle.pathForResource("GoogleService-Info", ofType = "plist") ?: return@lazy emptyMap()
        val data = NSData.dataWithContentsOfFile(path) ?: return@lazy emptyMap()
        val parsed = platform.Foundation.NSPropertyListSerialization.propertyListWithData(data, options = 0u, format = null, error = null)
        val map = parsed as? Map<*, *> ?: return@lazy emptyMap()
        val out = HashMap<String, String>()
        for ((key, v) in map) {
            val ks = key as? String ?: continue
            if (v is String) out[ks] = v
        }
        out
    }

    actual fun googleServiceValue(key: String): String? = googleConfig[key]?.takeIf { it.isNotBlank() }

    // ------------------------------------------------------------ الشبكة

    private var online = true
    private val netListeners = mutableListOf<(Boolean) -> Unit>()
    private var monitorStarted = false

    private fun startMonitor() {
        if (monitorStarted) return
        monitorStarted = true
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_update_handler(monitor) { path ->
            val now = nw_path_get_status(path) == nw_path_status_satisfied
            val changed = now != online
            online = now
            if (changed) netListeners.toList().forEach { it(now) }
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
        retained.add(monitor as Any)
    }

    actual fun isOnline(): Boolean { startMonitor(); return online }

    actual fun addNetworkListener(listener: (Boolean) -> Unit) {
        startMonitor()
        netListeners.add(listener)
    }

    private class HttpBox { var code = 0; var body: ByteArray = ByteArray(0); var headers: Map<String, String> = emptyMap() }

    actual fun httpExecute(method: String, url: String, headers: Map<String, String>, body: ByteArray?, timeoutSeconds: Double): HttpResult {
        val nsUrl = NSURL.URLWithString(url) ?: return HttpResult(0, ByteArray(0), emptyMap())
        val request = NSMutableURLRequest.requestWithURL(nsUrl)
        request.setHTTPMethod(method)
        request.setTimeoutInterval(timeoutSeconds)
        for ((k, v) in headers) request.setValue(v, forHTTPHeaderField = k)
        if (body != null) request.setHTTPBody(body.toNSData())
        val box = HttpBox()
        val sem = dispatch_semaphore_create(0)
        val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
            if (error == null) {
                val http = response as? NSHTTPURLResponse
                box.code = http?.statusCode?.toInt() ?: 0
                box.body = data?.toByteArray() ?: ByteArray(0)
                val h = HashMap<String, String>()
                http?.allHeaderFields?.forEach { (kk, vv) -> h[kk.toString()] = vv.toString() }
                box.headers = h
            }
            dispatch_semaphore_signal(sem)
        }
        task.resume()
        dispatch_semaphore_wait(sem, DISPATCH_TIME_FOREVER)
        return HttpResult(box.code, box.body, box.headers)
    }

    // ------------------------------------------------------------ التاريخ

    private val gregorian: NSCalendar by lazy {
        NSCalendar.calendarWithIdentifier(NSCalendarIdentifierGregorian)!!.also { it.timeZone = NSTimeZone.localTimeZone }
    }
    private val hijri: NSCalendar by lazy {
        NSCalendar.calendarWithIdentifier(NSCalendarIdentifierIslamicUmmAlQura)!!.also { it.timeZone = NSTimeZone.localTimeZone }
    }

    actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()

    actual fun localFields(epochMillis: Long): IntArray {
        val date = NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0)
        val units = NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or NSCalendarUnitHour or
            NSCalendarUnitMinute or NSCalendarUnitSecond or NSCalendarUnitNanosecond or NSCalendarUnitWeekday
        val c = gregorian.components(units, fromDate = date)
        val doy = gregorian.ordinalityOfUnit(NSCalendarUnitDay, inUnit = NSCalendarUnitYear, forDate = date).toInt()
        val ms = (((epochMillis % 1000) + 1000) % 1000).toInt()
        return intArrayOf(c.year.toInt(), c.month.toInt(), c.day.toInt(), c.hour.toInt(), c.minute.toInt(), c.second.toInt(), ms, c.weekday.toInt(), doy)
    }

    actual fun epochFromLocal(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int, millis: Int): Long {
        val c = NSDateComponents()
        c.year = year.toLong(); c.month = month.toLong(); c.day = day.toLong()
        c.hour = hour.toLong(); c.minute = minute.toLong(); c.second = second.toLong()
        val d = gregorian.dateFromComponents(c) ?: return currentTimeMillis()
        return (d.timeIntervalSince1970 * 1000.0).toLong() + millis
    }

    actual fun hijriFromGregorian(year: Int, month: Int, day: Int): IntArray {
        val c = NSDateComponents()
        c.year = year.toLong(); c.month = month.toLong(); c.day = day.toLong(); c.hour = 12
        val d = gregorian.dateFromComponents(c) ?: return intArrayOf(0, 0, 0)
        val h = hijri.components(NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay, fromDate = d)
        return intArrayOf(h.year.toInt(), h.month.toInt(), h.day.toInt())
    }

    actual fun gregorianFromHijri(year: Int, month: Int, day: Int): IntArray {
        val c = NSDateComponents()
        c.year = year.toLong(); c.month = month.toLong(); c.day = day.toLong(); c.hour = 12
        val d = hijri.dateFromComponents(c) ?: return intArrayOf(0, 0, 0)
        val g = gregorian.components(NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay, fromDate = d)
        return intArrayOf(g.year.toInt(), g.month.toInt(), g.day.toInt())
    }

    private fun formatter(pattern: String, localeId: String): NSDateFormatter {
        val f = NSDateFormatter()
        val id = when {
            localeId.isBlank() -> "en_US_POSIX"
            localeId.startsWith("ar") -> "ar@numbers=latn"
            else -> localeId
        }
        f.locale = NSLocale(localeIdentifier = id)
        f.calendar = gregorian
        f.timeZone = NSTimeZone.localTimeZone
        f.dateFormat = pattern
        return f
    }

    actual fun formatDate(epochMillis: Long, pattern: String, localeId: String): String =
        formatter(pattern, localeId).stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0))

    actual fun parseDate(text: String, pattern: String, localeId: String): Long? =
        formatter(pattern, localeId).dateFromString(text)?.let { (it.timeIntervalSince1970 * 1000.0).toLong() }

    // ------------------------------------------------------------ التشفير

    actual fun digest(algorithm: String, data: ByteArray): ByteArray {
        val alg = algorithm.uppercase().replace("-", "")
        val len = when (alg) {
            "MD5" -> CC_MD5_DIGEST_LENGTH
            "SHA1" -> CC_SHA1_DIGEST_LENGTH
            else -> CC_SHA256_DIGEST_LENGTH
        }
        val out = UByteArray(len)
        val input = if (data.isEmpty()) ByteArray(1) else data
        input.usePinned { inp ->
            out.usePinned { o ->
                val n = data.size.convert<UInt>()
                when (alg) {
                    "MD5" -> CC_MD5(inp.addressOf(0), n, o.addressOf(0))
                    "SHA1" -> CC_SHA1(inp.addressOf(0), n, o.addressOf(0))
                    else -> CC_SHA256(inp.addressOf(0), n, o.addressOf(0))
                }
            }
        }
        return out.asByteArray()
    }

    actual fun gunzip(data: ByteArray): ByteArray = com.example.platform.gunzip(data)

    actual fun randomUuid(): String = NSUUID().UUIDString

    // ------------------------------------------------------------ الطباعة وPDF

    private fun a4(landscape: Boolean): Pair<Double, Double> =
        if (landscape) 841.89 to 595.28 else 595.28 to 841.89

    /** يحمّل HTML في WKWebView خفيّ ثم يستدعي [ready] بعد اكتمال التحميل */
    private fun loadHtml(html: String, landscape: Boolean, ready: (WKWebView) -> Unit) {
        runOnMain {
            val (w, h) = a4(landscape)
            val web = WKWebView(frame = CGRectMake(0.0, 0.0, w, h), configuration = WKWebViewConfiguration())
            web.alpha = 0.01
            keyWindow()?.addSubview(web)
            retained.add(web)
            web.loadHTMLString(html, baseURL = NSURL.URLWithString("https://localhost/"))
            var waited = 0L
            fun check() {
                if (!web.loading || waited > 20_000) {
                    runOnMainDelayed(700) { ready(web) }
                } else {
                    waited += 150
                    runOnMainDelayed(150) { check() }
                }
            }
            runOnMainDelayed(300) { check() }
        }
    }

    private fun releaseWeb(web: WKWebView) {
        web.removeFromSuperview()
        retained.remove(web)
    }

    actual fun printHtml(html: String, jobName: String, landscape: Boolean, onDone: (Boolean, String?) -> Unit) {
        loadHtml(html, landscape) { web ->
            try {
                val info = UIPrintInfo.printInfo()
                info.outputType = UIPrintInfoOutputType.UIPrintInfoOutputGeneral
                info.jobName = jobName
                info.orientation = if (landscape) UIPrintInfoOrientation.UIPrintInfoOrientationLandscape else UIPrintInfoOrientation.UIPrintInfoOrientationPortrait
                val pc = UIPrintInteractionController.sharedPrintController
                pc.printInfo = info
                pc.printFormatter = web.viewPrintFormatter()
                onDone(true, null)
                pc.presentAnimated(true) { _, _, _ -> releaseWeb(web) }
            } catch (e: Throwable) {
                releaseWeb(web)
                onDone(false, e.message)
            }
        }
    }

    actual fun shareHtmlAsPdf(html: String, fileName: String, landscape: Boolean, onDone: (Boolean, String?) -> Unit) {
        loadHtml(html, landscape) { web ->
            try {
                val (w, h) = a4(landscape)
                val renderer = UIPrintPageRenderer()
                renderer.addPrintFormatter(web.viewPrintFormatter(), startingAtPageAtIndex = 0)
                val paper = CGRectMake(0.0, 0.0, w, h)
                renderer.setValue(NSValue.valueWithCGRect(paper), forKey = "paperRect")
                renderer.setValue(NSValue.valueWithCGRect(paper), forKey = "printableRect")
                val data = NSMutableData()
                UIGraphicsBeginPDFContextToData(data, paper, null)
                val pages = renderer.numberOfPages().toInt()
                renderer.prepareForDrawingPages(platform.Foundation.NSMakeRange(0u, pages.convert()))
                for (i in 0 until pages) {
                    UIGraphicsBeginPDFPage()
                    renderer.drawPageAtIndex(i.convert(), inRect = UIGraphicsGetPDFContextBounds())
                }
                UIGraphicsEndPDFContext()
                val dirPath = cacheDir() + "/generated_pdfs"
                makeDirs(dirPath)
                val safeName = if (fileName.endsWith(".pdf", true)) fileName else "$fileName.pdf"
                val path = "$dirPath/$safeName"
                data.writeToFile(path, atomically = true)
                releaseWeb(web)
                onDone(true, null)
                shareFile(path, "application/pdf")
            } catch (e: Throwable) {
                releaseWeb(web)
                onDone(false, e.message)
            }
        }
    }

    // ------------------------------------------------------------ الإشعارات

    actual fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, _ -> onResult(granted) }
    }

    actual fun showLocalNotification(title: String, body: String) {
        val content = UNMutableNotificationContent()
        content.setTitle(title)
        content.setBody(body)
        content.setSound(UNNotificationSound.defaultSound)
        val req = UNNotificationRequest.requestWithIdentifier(randomUuid(), content, null)
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(req, withCompletionHandler = null)
    }

    // ------------------------------------------------------------ تسجيل الدخول

    actual fun startGoogleOAuth(authUrl: String, callbackScheme: String, onResult: (String?, String?) -> Unit) {
        runOnMain {
            val url = NSURL.URLWithString(authUrl)
            if (url == null) { onResult(null, "bad url"); return@runOnMain }
            val anchor = OAuthAnchor()
            var session: ASWebAuthenticationSession? = null
            session = ASWebAuthenticationSession(uRL = url, callbackURLScheme = callbackScheme) { callback, error ->
                runOnMain {
                    onResult(callback?.absoluteString, error?.localizedDescription)
                    session?.let { retained.remove(it) }
                    retained.remove(anchor)
                }
            }
            session.presentationContextProvider = anchor
            session.prefersEphemeralWebBrowserSession = false
            retained.add(session)
            retained.add(anchor)
            if (!session.start()) onResult(null, "cannot start")
        }
    }

    // ------------------------------------------------------------ الخيوط

    actual fun runOnMain(block: () -> Unit) {
        if (NSThread.isMainThread) block() else dispatch_async(dispatch_get_main_queue()) { block() }
    }

    actual fun runOnMainDelayed(delayMillis: Long, block: () -> Unit) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, delayMillis * 1_000_000L), dispatch_get_main_queue()) { block() }
    }

    actual fun isMainThread(): Boolean = NSThread.isMainThread

    actual fun sleepMillis(millis: Long) { NSThread.sleepForTimeInterval(millis / 1000.0) }

    actual fun log(tag: String, message: String) { println("[$tag] $message") }

    actual fun ciFlag(name: String): Boolean {
        val env = platform.Foundation.NSProcessInfo.processInfo.environment
        return env["SIMULATOR_DEVICE_NAME"] != null && env[name] == "1"
    }
}
