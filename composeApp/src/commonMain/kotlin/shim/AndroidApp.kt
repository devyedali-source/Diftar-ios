@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName")

package android.app

import android.content.Context
import com.example.compat.PlatformApi

open class Application : Context() {
    override val applicationContext: Context get() = this
}

open class Activity : Context() {
    val window: android.view.Window = android.view.Window()
    fun runOnUiThread(action: () -> Unit) { PlatformApi.runOnMain(action) }
    open fun finish() {}
    val isFinishing: Boolean get() = false
    val isDestroyed: Boolean get() = false
    companion object {
        const val RESULT_OK = -1
        const val RESULT_CANCELED = 0
    }
}
