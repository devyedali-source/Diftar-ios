package com.example.data

import com.example.compat.*
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.io.InputStream

/**
 * صور صفحات الكتب: في أندرويد تأتي من حزمة Play Asset Delivery، ولا مقابل لها في الآيفون.
 * هنا نُبقي الواجهة نفسها دون تنزيل: لا صور حالياً، فتعيد open القيمة null ولا يظهر مؤشر تنزيل.
 */
object BookPack {
    const val PACK = "books"

    /** يفتح صفحة كتاب، أو يعيد null إذا لم تتوفر صور الكتب. */
    fun open(context: Context, book: String, page: Int): InputStream? = null

    /** لا تنزيل في الآيفون. */
    fun requestDownload(context: Context) {}

    /** لا يوجد تنزيل جارٍ في الآيفون. */
    fun downloadProgress(context: Context): Flow<Int?> = flowOf(null)
}
