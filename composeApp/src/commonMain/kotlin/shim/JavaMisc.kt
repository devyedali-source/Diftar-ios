@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName", "FunctionName")

package java.util.zip

import com.example.compat.PlatformApi

class GZIPInputStream(private val source: java.io.InputStream) : java.io.InputStream() {
    constructor(source: java.io.InputStream, size: Int) : this(source)
    override fun readAllBytesInternal(): ByteArray = PlatformApi.gunzip(source.readAllBytesInternal())
}
