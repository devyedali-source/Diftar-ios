@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName", "FunctionName")

package java.io

import com.example.compat.PlatformApi

open class IOException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    constructor(cause: Throwable?) : this(cause?.message, cause)
}

class FileNotFoundException(message: String? = null) : IOException(message)

abstract class InputStream : AutoCloseable {
    abstract fun readAllBytesInternal(): ByteArray
    fun readBytes(): ByteArray = readAllBytesInternal()
    fun readAllBytes(): ByteArray = readAllBytesInternal()
    fun bufferedReader(charset: Any? = null): BufferedReader = BufferedReader(InputStreamReader(this))
    fun reader(charset: Any? = null): InputStreamReader = InputStreamReader(this)
    override fun close() {}
}

class ByteArrayInputStream(private val bytes: ByteArray) : InputStream() {
    override fun readAllBytesInternal(): ByteArray = bytes
}

open class Reader : AutoCloseable {
    open fun readText(): String = ""
    override fun close() {}
}

open class InputStreamReader(private val stream: InputStream, charset: Any? = null) : Reader() {
    override fun readText(): String = stream.readAllBytesInternal().decodeToString()
    fun buffered(): BufferedReader = BufferedReader(this)
}

class BufferedReader(private val reader: Reader, size: Int = 0) : Reader() {
    override fun readText(): String = reader.readText()
    fun readLines(): List<String> = readText().lines()
    fun lineSequence(): Sequence<String> = readText().lineSequence()
    fun <T> useLines(block: (Sequence<String>) -> T): T = block(lineSequence())
}

open class OutputStream : AutoCloseable {
    open fun write(bytes: ByteArray) {}
    open fun flush() {}
    override fun close() {}
}

class FileOutputStream(private val file: File, private val append: Boolean = false) : OutputStream() {
    constructor(path: String) : this(File(path))
    private val buffer = ArrayList<ByteArray>()
    override fun write(bytes: ByteArray) { buffer.add(bytes) }
    override fun close() {
        var total = 0
        buffer.forEach { total += it.size }
        val out = ByteArray(total)
        var pos = 0
        buffer.forEach { it.copyInto(out, pos); pos += it.size }
        val existing = if (append) (PlatformApi.readFile(file.absolutePath) ?: ByteArray(0)) else ByteArray(0)
        PlatformApi.writeFile(file.absolutePath, existing + out)
    }
}

class FileInputStream(private val file: File) : InputStream() {
    override fun readAllBytesInternal(): ByteArray = PlatformApi.readFile(file.absolutePath) ?: throw FileNotFoundException(file.absolutePath)
}

class File(val path: String) {
    constructor(parent: File, child: String) : this(parent.path.trimEnd('/') + "/" + child)
    constructor(parent: String, child: String) : this(parent.trimEnd('/') + "/" + child)
    val absolutePath: String get() = path
    val name: String get() = path.substringAfterLast('/')
    val parentFile: File? get() = if (path.contains('/')) File(path.substringBeforeLast('/')) else null
    fun exists(): Boolean = PlatformApi.fileExists(path)
    fun mkdirs(): Boolean = PlatformApi.makeDirs(path)
    fun mkdir(): Boolean = PlatformApi.makeDirs(path)
    fun delete(): Boolean = PlatformApi.deleteFile(path)
    fun readText(): String = (PlatformApi.readFile(path) ?: ByteArray(0)).decodeToString()
    fun readBytes(): ByteArray = PlatformApi.readFile(path) ?: ByteArray(0)
    fun writeText(text: String) { PlatformApi.writeFile(path, text.encodeToByteArray()) }
    fun writeBytes(bytes: ByteArray) { PlatformApi.writeFile(path, bytes) }
    fun length(): Long = (PlatformApi.readFile(path)?.size ?: 0).toLong()
    fun listFiles(): Array<File>? = PlatformApi.listFiles(path).map { File(this, it) }.toTypedArray()
    val isDirectory: Boolean get() = PlatformApi.listFiles(path).isNotEmpty()
    override fun toString(): String = path
}
