@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName")

package android.content.res

import com.example.compat.PlatformApi

object AssetManager {
    fun open(path: String): java.io.InputStream {
        val bytes = PlatformApi.readBundledFile(path) ?: throw java.io.FileNotFoundException(path)
        return java.io.ByteArrayInputStream(bytes)
    }
    fun list(path: String): Array<String> = emptyArray()
}

object Resources {
    fun openRawResource(id: Int): java.io.InputStream {
        val bytes = com.example.R.rawBytes(id) ?: throw java.io.FileNotFoundException("raw/$id")
        return java.io.ByteArrayInputStream(bytes)
    }
    fun getString(id: Int): String = com.example.R.stringValue(id)
    val displayMetrics = DisplayMetrics()
    val configuration = Configuration()
}

class DisplayMetrics {
    val density: Float = 3f
    val widthPixels: Int = 1170
    val heightPixels: Int = 2532
}

class Configuration {
    val screenWidthDp: Int = 390
    val screenHeightDp: Int = 844
}
