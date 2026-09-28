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

private var cachedTypeface: Typeface? = null

private fun arabicBold(): Typeface? {
    cachedTypeface?.let { return it }
    val tf = FontMgr.default.matchFamilyStyleCharacter(null, FontStyle.BOLD, arrayOf("ar"), 'ج'.code)
        ?: FontMgr.default.matchFamilyStyle(null, FontStyle.BOLD)
    cachedTypeface = tf
    return tf
}

/**
 * يرسم سطرًا مُشكَّلًا على قوس دائري.
 * [startDeg] زاوية بداية القوس (0 = يمين، باتجاه عقارب الساعة)، [sweepDeg] طول القوس (سالب = عكس الاتجاه).
 * مثل Path.addArc + drawTextOnPath في أندرويد.
 */
private fun drawOnArc(
    canvas: org.jetbrains.skia.Canvas, text: String, font: Font, paint: Paint,
    cx: Float, cy: Float, r: Float, startDeg: Float, sweepDeg: Float, hOffset: Float, vOffset: Float
) {
    val line = TextLine.make(text, font)
    val glyphs = line.glyphs
    if (glyphs.isEmpty()) return
    val positions = line.positions // x,y أزواج
    val widths = font.getWidths(glyphs)
    val dir = if (sweepDeg >= 0) 1f else -1f
    // المسار يمرّ على نصف القطر r؛ الإزاحة العمودية للخارج عند القوس العلوي
    val rr = r - dir * vOffset
    val xforms = Array(glyphs.size) { i ->
        val gx = positions[i * 2]
        val center = hOffset + gx + widths[i] / 2f
        val angle = (startDeg * PI / 180.0) + dir * (center / r)
        val px = cx + rr * cos(angle).toFloat()
        val py = cy + rr * sin(angle).toFloat()
        val tangent = angle + dir * PI / 2.0
        val scos = cos(tangent).toFloat()
        val ssin = sin(tangent).toFloat()
        val half = widths[i] / 2f
        RSXform(scos, ssin, px - scos * half, py - ssin * half)
    }
    val blob = TextBlob.makeFromRSXform(glyphs, xforms, font) ?: return
    canvas.drawTextBlob(blob, 0f, 0f, paint)
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
        val tf = arabicBold()
        var font = Font(tf, size)
        while (size > minSize &&
            (TextLine.make(topText, font).width > arcLen * 0.96f || TextLine.make(bottomText, font).width > arcLen * 0.96f)
        ) {
            size -= 0.25f * density
            font = Font(tf, size)
        }
        val topW = TextLine.make(topText, font).width
        val botW = TextLine.make(bottomText, font).width
        drawOnArc(canvas, topText, font, paint, cx, cy, radius, 190f, 160f, ((arcLen - topW) / 2f).coerceAtLeast(0f), size * 0.35f)
        drawOnArc(canvas, bottomText, font, paint, cx, cy, radius, 170f, -160f, ((arcLen - botW) / 2f).coerceAtLeast(0f), size * 0.35f)

        val sepFont = Font(tf, 13f * density)
        val dashW = TextLine.make("-", sepFont).width
        val yOff = 13f * density * 0.32f
        canvas.drawTextLine(TextLine.make("-", sepFont), cx - radius - dashW / 2f, cy + yOff, paint)
        canvas.drawTextLine(TextLine.make("-", sepFont), cx + radius - dashW / 2f, cy + yOff, paint)
    }
}
