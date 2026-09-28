@file:Suppress("unused", "PackageDirectoryMismatch", "UNCHECKED_CAST")

package androidx.lifecycle

open class AndroidViewModel(private val application: android.app.Application) : ViewModel() {
    fun <T : android.app.Application> getApplication(): T = application as T
}
