@file:Suppress("unused", "PackageDirectoryMismatch")

package androidx.activity.result

import android.content.Intent

class ActivityResult(val resultCode: Int, val data: Intent?)

abstract class ActivityResultLauncher<I> {
    abstract fun launch(input: I)
    fun launch(input: I, options: Any?) = launch(input)
    fun unregister() {}
}
