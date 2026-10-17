package com.example.ui.screens

import androidx.compose.ui.text.drawText
import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import org.json.JSONObject

/**
 * رسومات السبورة: أشكال بسيطة (علم، خريطة، مسطرة، ميزان، ساعة، أشكال هندسية…) معرَّفة في الملف
 * assets/board_figures.json بإحداثيات خاصة بكل رسم، وتُرسم بالطبشور على السبورة.
 * يُستدعى الرسم في نص الدرس بسطر مستقل: [[fig:اسم_الرسم]]
 */
class BoardFig(val w: Float, val h: Float, val items: List<JSONObject>)

object BoardFigureStore {
    private var raw: JSONObject? = null
    private val cache = HashMap<String, BoardFig?>()

    @Synchronized
    fun get(context: Context, name: String): BoardFig? {
        if (cache.containsKey(name)) return cache[name]
        if (raw == null) {
            raw = try {
                context.assets.open("board_figures.json").bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
            } catch (e: Exception) {
                null
            }
        }
        val o = raw?.optJSONObject(name)
        val fig = if (o == null) null else {
            val arr = o.getJSONArray("items")
            BoardFig(
                o.getDouble("w").toFloat(),
                o.getDouble("h").toFloat(),
                List(arr.length()) { arr.getJSONObject(it) }
            )
        }
        cache[name] = fig
        return fig
    }
}

private fun figColor(s: String?, fallback: Color): Color =
    if (s.isNullOrBlank() || s == "null") fallback else try {
        parseHexColor(s)
    } catch (e: Exception) {
        fallback
    }

private fun parseHexColor(s: String): Color {
    val h = s.trim().removePrefix("#")
    val v = h.toLong(16)
    return when (h.length) {
        6 -> Color(((0xFF000000L) or v).toInt())
        8 -> Color(v.toInt())
        else -> throw IllegalArgumentException(s)
    }
}

@Composable
fun BoardFigure(name: String, maxHeight: Dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fig = remember(name) { BoardFigureStore.get(context, name) } ?: return
    val limitH = maxHeight
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        // الرسم يُلائَم داخل العرض المتاح والارتفاع الأقصى معاً حتى لا يتجاوز مساحته ويتداخل مع النص
        val ratio = fig.w / fig.h
        val drawW = minOf(this.maxWidth, limitH * ratio)
        Canvas(
            modifier = Modifier
                .width(drawW)
                .height(drawW / ratio)
        ) {
            val k = size.width / fig.w
            for (it in fig.items) {
                val fill = if (it.isNull("fill")) null else figColor(it.optString("fill"), Color.Transparent)
                val stroke = if (it.isNull("stroke")) null else figColor(it.optString("stroke"), Color.White)
                val sw = (it.optDouble("sw", 2.0).toFloat() * k).coerceAtLeast(1f)
                when (it.getString("t")) {
                    "rect" -> {
                        val tl = Offset(it.getDouble("x").toFloat() * k, it.getDouble("y").toFloat() * k)
                        val sz = Size(it.getDouble("w").toFloat() * k, it.getDouble("h").toFloat() * k)
                        if (fill != null) drawRect(fill, tl, sz)
                        if (stroke != null) drawRect(stroke, tl, sz, style = Stroke(sw))
                    }
                    "circle" -> {
                        val c = Offset(it.getDouble("x").toFloat() * k, it.getDouble("y").toFloat() * k)
                        val r = it.getDouble("r").toFloat() * k
                        if (fill != null) drawCircle(fill, r, c)
                        if (stroke != null) drawCircle(stroke, r, c, style = Stroke(sw))
                    }
                    "line" -> {
                        drawLine(
                            stroke ?: Color.White,
                            Offset(it.getDouble("x1").toFloat() * k, it.getDouble("y1").toFloat() * k),
                            Offset(it.getDouble("x2").toFloat() * k, it.getDouble("y2").toFloat() * k),
                            strokeWidth = sw
                        )
                    }
                    "poly" -> {
                        val pts = it.getJSONArray("pts")
                        if (pts.length() > 1) {
                            val path = Path()
                            for (i in 0 until pts.length()) {
                                val p = pts.getJSONArray(i)
                                val x = p.getDouble(0).toFloat() * k
                                val y = p.getDouble(1).toFloat() * k
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            if (it.optBoolean("closed", true)) path.close()
                            if (fill != null) drawPath(path, fill)
                            if (stroke != null) drawPath(path, stroke, style = Stroke(sw))
                        }
                    }
                    "crescent" -> {
                        val cx = it.getDouble("x").toFloat() * k
                        val cy = it.getDouble("y").toFloat() * k
                        val r = it.getDouble("r").toFloat() * k
                        val r2 = it.getDouble("r2").toFloat() * k
                        val dy = it.getDouble("dy").toFloat() * k
                        val big = Path().apply { addOval(Rect(Offset(cx - r, cy - r), Size(2 * r, 2 * r))) }
                        val small = Path().apply { addOval(Rect(Offset(cx - r2, cy - dy - r2), Size(2 * r2, 2 * r2))) }
                        val cut = Path.combine(PathOperation.Difference, big, small)
                        drawPath(cut, figColor(it.optString("fill"), Color.White))
                    }
                    "text" -> {
                        val layout = measurer.measure(
                            it.getString("s"),
                            androidx.compose.ui.text.TextStyle(
                                color = figColor(it.optString("color"), Color.White),
                                fontSize = (it.getDouble("size").toFloat() * k).toSp(),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        )
                        drawText(
                            layout,
                            topLeft = Offset(
                                it.getDouble("x").toFloat() * k - layout.size.width / 2f,
                                it.getDouble("y").toFloat() * k - layout.firstBaseline
                            )
                        )
                    }
                }
            }
        }
    }
}
