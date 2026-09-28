@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName")

package android.view

/** على الآيفون لا يمكن منع لقطات الشاشة بهذه الطريقة؛ هذه الدوالّ لا تفعل شيئًا */
class Window {
    fun addFlags(flags: Int) {}
    fun clearFlags(flags: Int) {}
    fun setFlags(flags: Int, mask: Int) {}
}

object WindowManager {
    object LayoutParams {
        const val FLAG_SECURE = 0x2000
        const val FLAG_KEEP_SCREEN_ON = 0x80
    }
}
