package com.example.compat

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.RSXform
import org.jetbrains.skia.TextBlob
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private var cachedArabic: Typeface? = null
private var cachedLatin: Typeface? = null

/** خطّ Noto Naskh Arabic العريض (خطّ أندرويد العربي) من ملفات التطبيق */
private fun arabicBold(): Typeface? {
    cachedArabic?.let { return it }
    val base = platform.Foundation.NSBundle.mainBundle.resourcePath
    val bytes = listOf(
        "$base/compose-resources/composeResources/com.example.resources/font/noto_naskh_arabic.ttf",
        "$base/composeResources/com.example.resources/font/noto_naskh_arabic.ttf"
    ).firstNotNullOfOrNull { p -> platform.Foundation.NSData.dataWithContentsOfFile(p)?.toByteArray() }
    val tf = bytes?.let { b ->
        FontMgr.default.makeFromData(org.jetbrains.skia.Data.makeFromBytes(b))
            ?.makeClone(arrayOf(org.jetbrains.skia.FontVariation("wght", 700f)))
    } ?: FontMgr.default.matchFamilyStyleCharacter(null, FontStyle.BOLD, arrayOf("ar"), 'ج'.code)
    cachedArabic = tf
    return tf
}

private fun latinBold(): Typeface? {
    cachedLatin?.let { return it }
    val tf = FontMgr.default.matchFamilyStyle(null, FontStyle.BOLD)
    cachedLatin = tf
    return tf
}

private fun isArabic(c: Char) = c.code in 0x0600..0x06FF || c.code in 0x0750..0x077F || c.code in 0xFB50..0xFDFF || c.code in 0xFE70..0xFEFF

/** يقسم النص إلى مقاطع عربية وغير عربية بترتيب العرض (من اليسار إلى اليمين) */
private fun visualSegments(text: String): List<Pair<String, Boolean>> {
    val segs = mutableListOf<Pair<String, Boolean>>()
    val sb = StringBuilder()
    var cur: Boolean? = null
    for (ch in text) {
        val a = if (ch == ' ') (cur ?: false) else isArabic(ch)
        if (cur != null && a != cur) { segs.add(sb.toString() to cur); sb.clear() }
        cur = a; sb.append(ch)
    }
    if (sb.isNotEmpty() && cur != null) segs.add(sb.toString() to cur)
    return segs.reversed() // فقرة عربية: المقطع الأول يظهر في أقصى اليمين
}

/**
 * يرسم سطرًا مُشكَّلًا على قوس دائري.
 * [startDeg] زاوية بداية القوس (0 = يمين، باتجاه عقارب الساعة)، [sweepDeg] طول القوس (سالب = عكس الاتجاه).
 * مثل Path.addArc + drawTextOnPath في أندرويد.
 */
private class Glyphs(val glyphs: ShortArray, val xs: FloatArray, val widths: FloatArray, val font: Font)

private fun shapeLine(text: String, size: Float): Pair<List<Glyphs>, Float> {
    val out = mutableListOf<Glyphs>()
    var x = 0f
    for ((seg, arabic) in visualSegments(text)) {
        val font = Font(if (arabic) arabicBold() else latinBold(), size)
        val line = TextLine.make(seg, font)
        val g = line.glyphs
        if (g.isEmpty()) { x += line.width; continue }
        val pos = line.positions
        val xs = FloatArray(g.size) { i -> x + pos[i * 2] }
        out.add(Glyphs(g, xs, font.getWidths(g), font))
        x += line.width
    }
    return out to x
}

/**
 * يرسم سطرًا مُشكَّلًا على قوس دائري (مثل Path.addArc + drawTextOnPath في أندرويد).
 * [startDeg] بداية القوس (0 = يمين، باتجاه عقارب الساعة)، [sweepDeg] سالب = عكس الاتجاه.
 */
private fun drawOnArc(
    canvas: org.jetbrains.skia.Canvas, text: String, size: Float, paint: Paint,
    cx: Float, cy: Float, r: Float, startDeg: Float, sweepDeg: Float, hOffset: Float, vOffset: Float
) {
    val dir = if (sweepDeg >= 0) 1f else -1f
    val rr = r - dir * vOffset
    for (run in shapeLine(text, size).first) {
        val xforms = Array(run.glyphs.size) { i ->
            val center = hOffset + run.xs[i] + run.widths[i] / 2f
            val angle = (startDeg * PI / 180.0) + dir * (center / r)
            val px = cx + rr * cos(angle).toFloat()
            val py = cy + rr * sin(angle).toFloat()
            val tangent = angle + dir * PI / 2.0
            val scos = cos(tangent).toFloat()
            val ssin = sin(tangent).toFloat()
            val half = run.widths[i] / 2f
            RSXform(scos, ssin, px - scos * half, py - ssin * half)
        }
        val blob = TextBlob.makeFromRSXform(run.glyphs, xforms, run.font) ?: continue
        canvas.drawTextBlob(blob, 0f, 0f, paint)
    }
}

actual fun drawStampRingText(
    scope: DrawScope,
    topText: String,
    bottomText: String,
    cx: Float,
    cy: Float,
    radius: Float,
    density: Float,
    color: Color
) {
    scope.drawIntoCanvas { c ->
        val canvas = c.nativeCanvas
        val paint = Paint().apply { this.color = color.toArgb(); isAntiAlias = true }
        val arcLen = (PI * radius * (160.0 / 180.0)).toFloat()
        var size = 10f * density
        val minSize = 5f * density
        while (size > minSize &&
            (shapeLine(topText, size).second > arcLen * 0.96f || shapeLine(bottomText, size).second > arcLen * 0.96f)
        ) {
            size -= 0.25f * density
        }
        val topW = shapeLine(topText, size).second
        val botW = shapeLine(bottomText, size).second
        drawOnArc(canvas, topText, size, paint, cx, cy, radius, 190f, 160f, ((arcLen - topW) / 2f).coerceAtLeast(0f), size * 0.35f)
        drawOnArc(canvas, bottomText, size, paint, cx, cy, radius, 170f, -160f, ((arcLen - botW) / 2f).coerceAtLeast(0f), size * 0.35f)

        val sepFont = Font(latinBold(), 13f * density)
        val dashW = TextLine.make("-", sepFont).width
        val yOff = 13f * density * 0.32f
        canvas.drawTextLine(TextLine.make("-", sepFont), cx - radius - dashW / 2f, cy + yOff, paint)
        canvas.drawTextLine(TextLine.make("-", sepFont), cx + radius - dashW / 2f, cy + yOff, paint)
    }
}
