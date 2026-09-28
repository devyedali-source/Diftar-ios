@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName")

package android.widget

import android.content.Context
import com.example.compat.ToastHost

class Toast private constructor(private val text: String, private val duration: Int) {
    fun show() { ToastHost.show(text, duration == LENGTH_LONG) }
    fun cancel() {}

    companion object {
        const val LENGTH_SHORT = 0
        const val LENGTH_LONG = 1
        fun makeText(context: Context?, text: CharSequence?, duration: Int): Toast = Toast(text?.toString() ?: "", duration)
        fun makeText(context: Context?, resId: Int, duration: Int): Toast = Toast(com.example.R.stringValue(resId), duration)
    }
}
