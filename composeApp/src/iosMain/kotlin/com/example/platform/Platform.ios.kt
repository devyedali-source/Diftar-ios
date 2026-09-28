package com.example.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.zlib.ZLIB_VERSION
import platform.zlib.Z_BUF_ERROR
import platform.zlib.Z_NO_FLUSH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2_
import platform.zlib.z_stream

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()

actual fun currentYearMonth(): Pair<Int, Int> {
    val comps = NSCalendar.currentCalendar.components(
        NSCalendarUnitYear or NSCalendarUnitMonth,
        fromDate = NSDate()
    )
    return comps.year.toInt() to comps.month.toInt()
}

@OptIn(ExperimentalForeignApi::class)
actual fun gunzip(data: ByteArray): ByteArray {
    if (data.isEmpty()) return data
    val chunks = ArrayList<ByteArray>()
    var total = 0
    memScoped {
        val stream = alloc<z_stream>()
        stream.zalloc = null
        stream.zfree = null
        stream.opaque = null
        stream.next_in = null
        stream.avail_in = 0u
        // 15 + 32: قراءة رأس gzip تلقائيًا
        var rc = inflateInit2_(stream.ptr, 15 + 32, ZLIB_VERSION, sizeOf<z_stream>().toInt())
        check(rc == Z_OK) { "inflateInit2 failed: $rc" }
        try {
            data.usePinned { input ->
                stream.next_in = input.addressOf(0).reinterpret()
                stream.avail_in = data.size.convert()
                val buffer = ByteArray(256 * 1024)
                while (true) {
                    val produced = buffer.usePinned { out ->
                        stream.next_out = out.addressOf(0).reinterpret()
                        stream.avail_out = buffer.size.convert()
                        rc = inflate(stream.ptr, Z_NO_FLUSH)
                        buffer.size - stream.avail_out.toInt()
                    }
                    if (produced > 0) {
                        chunks.add(buffer.copyOf(produced))
                        total += produced
                    }
                    if (rc == Z_STREAM_END) break
                    check(rc == Z_OK || (rc == Z_BUF_ERROR && produced > 0)) { "inflate failed: $rc" }
                }
            }
        } finally {
            inflateEnd(stream.ptr)
        }
    }
    val result = ByteArray(total)
    var pos = 0
    for (c in chunks) {
        c.copyInto(result, pos)
        pos += c.size
    }
    return result
}
