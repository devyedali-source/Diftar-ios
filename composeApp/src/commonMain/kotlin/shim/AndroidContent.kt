@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch")

package android.content

import android.net.Uri
import com.example.compat.PlatformApi

/**
 * بديل android.content.Context على الآيفون.
 * يوفّر فقط ما يستعمله التطبيق: التخزين، الملفات المرفقة، فتح الروابط، المشاركة.
 */
open class Context {
    open val applicationContext: Context get() = AppContextHolder.context
    open val baseContext: Context get() = this
    val packageName: String get() = PlatformApi.bundleId()
    val assets: android.content.res.AssetManager get() = android.content.res.AssetManager
    val resources: android.content.res.Resources get() = android.content.res.Resources
    val contentResolver: ContentResolver get() = ContentResolver
    val packageManager: android.content.pm.PackageManager get() = android.content.pm.PackageManager
    val cacheDir: java.io.File get() = java.io.File(PlatformApi.cacheDir())
    val filesDir: java.io.File get() = java.io.File(PlatformApi.filesDir())

    fun getSharedPreferences(name: String, mode: Int): SharedPreferences = SharedPreferencesImpl.get(name)

    fun getString(id: Int): String = com.example.R.stringValue(id)

    fun startActivity(intent: Intent) {
        IntentDispatcher.dispatch(intent)
    }

    fun getSystemService(name: String): Any? = when (name) {
        CONNECTIVITY_SERVICE -> android.net.ConnectivityManager
        CLIPBOARD_SERVICE -> ClipboardManager
        else -> null
    }

    companion object {
        const val MODE_PRIVATE = 0
        const val CONNECTIVITY_SERVICE = "connectivity"
        const val CLIPBOARD_SERVICE = "clipboard"
        const val PRINT_SERVICE = "print"
        const val NOTIFICATION_SERVICE = "notification"
    }
}

object AppContextHolder {
    val context: Context get() = com.example.compat.AppApplication
}

object ContentResolver

// ---------- SharedPreferences ----------

interface SharedPreferences {
    fun getString(key: String, defValue: String?): String?
    fun getStringSet(key: String, defValues: Set<String>?): Set<String>?
    fun getInt(key: String, defValue: Int): Int
    fun getLong(key: String, defValue: Long): Long
    fun getFloat(key: String, defValue: Float): Float
    fun getBoolean(key: String, defValue: Boolean): Boolean
    fun contains(key: String): Boolean
    val all: Map<String, *>
    fun edit(): Editor
    fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {}
    fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {}

    fun interface OnSharedPreferenceChangeListener {
        fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?)
    }

    interface Editor {
        fun putString(key: String, value: String?): Editor
        fun putStringSet(key: String, values: Set<String>?): Editor
        fun putInt(key: String, value: Int): Editor
        fun putLong(key: String, value: Long): Editor
        fun putFloat(key: String, value: Float): Editor
        fun putBoolean(key: String, value: Boolean): Editor
        fun remove(key: String): Editor
        fun clear(): Editor
        fun commit(): Boolean
        fun apply()
    }
}

/** يتيح كتابة prefs.edit { putString(...) } كما في core-ktx */
inline fun SharedPreferences.edit(commit: Boolean = false, action: SharedPreferences.Editor.() -> Unit) {
    val editor = edit()
    action(editor)
    if (commit) editor.commit() else editor.apply()
}

internal class SharedPreferencesImpl private constructor(private val store: String) : SharedPreferences {
    companion object {
        private val cache = mutableMapOf<String, SharedPreferencesImpl>()
        fun get(name: String): SharedPreferences = cache.getOrPut(name) { SharedPreferencesImpl(name) }
    }

    private fun raw(key: String): Any? = PlatformApi.prefGet(store, key)

    override fun getString(key: String, defValue: String?): String? = (raw(key) as? String) ?: defValue

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        val v = raw(key) ?: return defValues
        @Suppress("UNCHECKED_CAST")
        return when (v) {
            is Set<*> -> v.filterIsInstance<String>().toSet()
            is List<*> -> v.filterIsInstance<String>().toSet()
            else -> defValues
        }
    }

    override fun getInt(key: String, defValue: Int): Int = (raw(key) as? Number)?.toInt() ?: defValue
    override fun getLong(key: String, defValue: Long): Long = (raw(key) as? Number)?.toLong() ?: defValue
    override fun getFloat(key: String, defValue: Float): Float = (raw(key) as? Number)?.toFloat() ?: defValue
    override fun getBoolean(key: String, defValue: Boolean): Boolean = (raw(key) as? Boolean) ?: defValue
    override fun contains(key: String): Boolean = raw(key) != null
    override val all: Map<String, *> get() = PlatformApi.prefKeys(store).associateWith { raw(it) }

    override fun edit(): SharedPreferences.Editor = EditorImpl()

    private inner class EditorImpl : SharedPreferences.Editor {
        private val changes = LinkedHashMap<String, Any?>()
        private var clearAll = false
        override fun putString(key: String, value: String?) = apply { changes[key] = value }
        override fun putStringSet(key: String, values: Set<String>?) = apply { changes[key] = values?.toList() }
        override fun putInt(key: String, value: Int) = apply { changes[key] = value }
        override fun putLong(key: String, value: Long) = apply { changes[key] = value }
        override fun putFloat(key: String, value: Float) = apply { changes[key] = value }
        override fun putBoolean(key: String, value: Boolean) = apply { changes[key] = value }
        override fun remove(key: String) = apply { changes[key] = null }
        override fun clear() = apply { clearAll = true }
        override fun commit(): Boolean {
            if (clearAll) PlatformApi.prefClear(store)
            for ((k, v) in changes) PlatformApi.prefPut(store, k, v)
            return true
        }
        override fun apply() { commit() }
    }
}

// ---------- Intent ----------

class Intent() {
    var action: String? = null
    var data: Uri? = null
    var type: String? = null
    var flags: Int = 0
    var `package`: String? = null
    val extras: MutableMap<String, Any?> = mutableMapOf()
    internal var chooserTarget: Intent? = null
    internal var chooserTitle: String? = null

    constructor(action: String?) : this() { this.action = action }
    constructor(action: String?, uri: Uri?) : this() { this.action = action; this.data = uri }
    constructor(other: Intent?) : this() {
        if (other != null) {
            action = other.action; data = other.data; type = other.type; flags = other.flags
            extras.putAll(other.extras)
        }
    }

    fun putExtra(name: String, value: Any?): Intent { extras[name] = value; return this }
    fun getStringExtra(name: String): String? = extras[name]?.toString()
    fun <T> getParcelableExtra(name: String): T? {
        @Suppress("UNCHECKED_CAST")
        return extras[name] as? T
    }
    fun addFlags(f: Int): Intent { flags = flags or f; return this }
    fun setFlags(f: Int): Intent { flags = f; return this }
    fun setPackage(p: String?): Intent { `package` = p; return this }
    fun setData(uri: Uri?): Intent { data = uri; return this }
    fun setType(t: String?): Intent { type = t; return this }
    fun setDataAndType(uri: Uri?, t: String?): Intent { data = uri; type = t; return this }
    fun resolveActivity(pm: android.content.pm.PackageManager): Any? = this

    companion object {
        const val ACTION_VIEW = "android.intent.action.VIEW"
        const val ACTION_SEND = "android.intent.action.SEND"
        const val ACTION_SENDTO = "android.intent.action.SENDTO"
        const val ACTION_DIAL = "android.intent.action.DIAL"
        const val ACTION_CHOOSER = "android.intent.action.CHOOSER"
        const val EXTRA_TEXT = "android.intent.extra.TEXT"
        const val EXTRA_SUBJECT = "android.intent.extra.SUBJECT"
        const val EXTRA_STREAM = "android.intent.extra.STREAM"
        const val EXTRA_EMAIL = "android.intent.extra.EMAIL"
        const val FLAG_ACTIVITY_NEW_TASK = 0x10000000
        const val FLAG_GRANT_READ_URI_PERMISSION = 0x00000001
        const val FLAG_ACTIVITY_CLEAR_TOP = 0x04000000

        fun createChooser(target: Intent, title: CharSequence?): Intent =
            Intent(ACTION_CHOOSER).also { it.chooserTarget = target; it.chooserTitle = title?.toString() }
    }
}

/** تنفيذ Intent على الآيفون: فتح رابط، أو مشاركة نص/ملف */
object IntentDispatcher {
    fun dispatch(intent: Intent) {
        val target = intent.chooserTarget ?: intent
        when (target.action) {
            Intent.ACTION_SEND -> {
                val stream = target.extras[Intent.EXTRA_STREAM]
                if (stream is Uri && stream.scheme == "file") {
                    PlatformApi.shareFile(stream.path ?: "", target.type)
                } else {
                    val text = target.extras[Intent.EXTRA_TEXT]?.toString() ?: ""
                    PlatformApi.shareText(text, target.extras[Intent.EXTRA_SUBJECT]?.toString())
                }
            }
            else -> {
                val url = target.data?.toString()
                if (!url.isNullOrBlank()) {
                    val opened = PlatformApi.openUrl(url)
                    if (!opened) throw ActivityNotFoundException("No app can open $url")
                }
            }
        }
    }
}

class ActivityNotFoundException(message: String? = null) : RuntimeException(message)

// ---------- Clipboard ----------

object ClipboardManager {
    private var current: ClipData? = null
    fun setPrimaryClip(clip: ClipData) {
        current = clip
        PlatformApi.copyToClipboard(clip.text)
    }
    val primaryClip: ClipData? get() = PlatformApi.readClipboard()?.let { ClipData("", it) }
    fun hasPrimaryClip(): Boolean = PlatformApi.readClipboard() != null
}

class ClipData(val label: String, val text: String) {
    fun getItemAt(index: Int): Item = Item(text)
    val itemCount: Int get() = 1
    class Item(val text: CharSequence?)
    companion object {
        fun newPlainText(label: CharSequence?, text: CharSequence?): ClipData = ClipData(label?.toString() ?: "", text?.toString() ?: "")
    }
}

/** للتوافق مع الشيفرة التي تفحص ContextWrapper */
open class ContextWrapper(private val base: Context?) : Context() {
    override val baseContext: Context get() = base ?: this
}
