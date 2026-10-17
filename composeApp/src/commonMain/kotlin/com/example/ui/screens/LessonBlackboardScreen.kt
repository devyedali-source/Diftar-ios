package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.planning.LessonProposalData

private val BoardBlack = Color(0xFF050505)
private val ChalkWhite = Color(0xFFFAFAF5)
private val BoardFrame = Color(0xFF6B4A2B)

/** نقاط فارغة يكتب فيها المعلم التاريخ بنفسه، كما في ورقة التحضير. */
private const val BOARD_DOTS = "................"

/**
 * شاشة السبورة: خلفية سوداء وكتابة بيضاء غليظة بالطبشور. الأقسام فوق بعضها: الترويسة، الدرس،
 * الإدماج (إن وجد)، والتطبيق. الخط ثابت ومقروء وكل المعلومات ظاهرة، وإن طالت يمرّر المستخدم للأسفل.
 */
@Composable
fun LessonBlackboardScreen(
    data: LessonProposalData,
    isFrench: Boolean,
    subjectFallback: String,
    domainFallback: String,
    topicFallback: String,
    onClose: () -> Unit
) {
    val board = data.board ?: return
    BackHandler(onBack = onClose)

    val direction = if (isFrench) LayoutDirection.Ltr else LayoutDirection.Rtl
    val subject = data.subject.ifBlank { subjectFallback }
    val domain = data.domain.ifBlank { domainFallback }
    val topic = data.topic.ifBlank { topicFallback }

    val localCtx = androidx.compose.ui.platform.LocalContext.current
    val (hijriLine, gregLine) = remember(isFrench) { com.example.ui.utils.LocalDates.today(localCtx, isFrench) }
    val headerText = listOf(
        (if (isFrench) "Date (hégire) : " else "التاريخ الهجري: ") + BOARD_DOTS,
        (if (isFrench) "Date : " else "التاريخ الميلادي: ") + BOARD_DOTS,
        (if (isFrench) "Matière : " else "المادة: ") + subject,
        (if (isFrench) "Domaine : " else "المجال: ") + domain,
        (if (isFrench) "Thème : " else "الموضوع: ") + topic
    ).joinToString("\n") + (if (board.aux) "\n\n" + (if (isFrench) "(Proposition pédagogique d'appui)" else "(مقترح تربوي مساعد)") else "")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BoardBlack)
            .systemBarsPadding()
    ) {
        // تقريب السبورة بإصبعين وسحبها، والنقر مرتين للعودة
        var scale by remember { mutableStateOf(1f) }
        var offX by remember { mutableStateOf(0f) }
        var offY by remember { mutableStateOf(0f) }
        var boxSize by remember { mutableStateOf(IntSize.Zero) }
        CompositionLocalProvider(LocalLayoutDirection provides direction) {
          Column(Modifier.fillMaxSize()) {
            // تاريخا اليوم المسجّلان محلياً فوق السبورة (خارج إطارها)
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 52.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(hijriLine, style = ChalkStyle, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
                Text(gregLine, style = ChalkStyle, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onSizeChanged { boxSize = it }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val ns = (scale * zoom).coerceIn(1f, 5f)
                            val maxX = (ns - 1f) * boxSize.width / 2f
                            val maxY = (ns - 1f) * boxSize.height / 2f
                            scale = ns
                            offX = (offX + pan.x * ns).coerceIn(-maxX, maxX)
                            offY = (offY + pan.y * ns).coerceIn(-maxY, maxY)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = { scale = 1f; offX = 0f; offY = 0f })
                    }
                    .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offX, translationY = offY)
                    .padding(6.dp)
                    .border(3.dp, BoardFrame, RoundedCornerShape(6.dp))
                    .padding(4.dp)
            ) {
                val sections: List<Triple<String?, String, Float>> = buildList {
                    add(Triple(null, headerText, 0.9f))
                    add(Triple(if (isFrench) "Leçon" else "الدرس", board.lesson, 2f))
                    if (board.hasIntegration) add(Triple(if (isFrench) "Intégration" else "الإدماج", board.integration, 1.1f))
                    add(Triple(if (isFrench) "Application" else "التطبيق", board.application, 1.3f))
                }
                // كل السبورة في صفحة واحدة بخط ثابت مقروء؛ إن طال المحتوى يمرّر المستخدم للأسفل حتى تكتمل
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    sections.forEachIndexed { idx, (title, text, _) ->
                        if (idx > 0) BoardHDivider()
                        BoardSection(title, text, Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
          }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .size(34.dp)
                .background(Color.White.copy(alpha = 0.18f), CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✕", color = ChalkWhite, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun BoardSection(title: String?, text: String, modifier: Modifier) {
    Column(modifier = modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
        if (title != null) {
            Text(
                text = title,
                style = ChalkStyle,
                fontSize = 20.sp,
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            )
        }
        if (title == null) {
            BoardHeader(text, Modifier.fillMaxWidth())
        } else {
            ChalkContent(text, Modifier.fillMaxWidth())
        }
    }
}

/**
 * ترويسة السبورة: التاريخان، ثم المادة والمجال، ثم الموضوع (عنوان الدرس). تُعرض بخط ثابت مقروء
 * دون خوارزمية ملاءمة، حتى لا تغيب أبدًا.
 */
@Composable
private fun BoardHeader(text: String, modifier: Modifier) {
    val lines = text.split('\n')
    val cell: @Composable (String, Modifier) -> Unit = { t, m ->
        Text(
            text = t,
            style = ChalkStyle,
            fontSize = 17.sp,
            lineHeight = 24.sp,
            modifier = m
        )
    }
    Column(modifier = modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (lines.size >= 2) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                cell(lines[0], Modifier.weight(1f))
                cell(lines[1], Modifier.weight(1f))
            }
        }
        if (lines.size >= 4) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                cell(lines[2], Modifier.weight(1f))
                cell(lines[3], Modifier.weight(1f))
            }
        }
        if (lines.size >= 5) cell(lines[4], Modifier.fillMaxWidth())
        val extra = lines.drop(5).filter { it.isNotBlank() }
        if (extra.isNotEmpty()) cell(extra.joinToString(" "), Modifier.fillMaxWidth())
    }
}

private sealed class BoardBlock {
    data class Para(val text: String) : BoardBlock()
    data class Table(val rows: List<List<String>>) : BoardBlock()
    data class Fig(val name: String) : BoardBlock()
}

/** يفكّك نص القسم إلى فقرات وجداول (أسطر تبدأ بـ |) ورسومات (سطر [[fig:اسم]]). */
private fun parseBoardBlocks(text: String): List<BoardBlock> {
    val out = mutableListOf<BoardBlock>()
    val para = StringBuilder()
    val rows = mutableListOf<List<String>>()
    fun flushPara() {
        val t = para.toString().trim('\n')
        if (t.isNotEmpty()) out.add(BoardBlock.Para(t))
        para.clear()
    }
    fun flushRows() {
        if (rows.isNotEmpty()) out.add(BoardBlock.Table(rows.toList()))
        rows.clear()
    }
    for (line in text.split('\n')) {
        val t = line.trim()
        if (t.startsWith("[[fig:") && t.endsWith("]]")) {
            flushPara(); flushRows()
            out.add(BoardBlock.Fig(t.removePrefix("[[fig:").removeSuffix("]]").trim()))
        } else if (t.startsWith("|")) {
            flushPara()
            rows.add(t.trim('|').split('|').map { it.trim() })
        } else {
            flushRows()
            para.append(line).append('\n')
        }
    }
    flushPara(); flushRows()
    return out
}

/**
 * يعرض محتوى القسم (نص وجداول ورسومات) بخط ثابت مقروء دون تصغير ولا قصّ؛ التمرير للأسفل يتكفّل بالباقي.
 */
@Composable
private fun ChalkContent(text: String, modifier: Modifier) {
    val blocks = remember(text) { parseBoardBlocks(text) }
    // النصوص الطويلة (سور القرآن كاملة، جداول الضرب) تصغر درجة مناسبة ليظهر أكثرها دفعة واحدة؛ والنص كله يبقى كما هو
    val size = when {
        text.length > 1300 -> 14f
        text.length > 900 -> 15f
        text.length > 700 -> 17f
        else -> 20f
    }
    Column(modifier = modifier) {
        blocks.forEach { b ->
            when (b) {
                is BoardBlock.Para -> Text(
                    text = b.text,
                    style = ChalkStyle,
                    fontSize = size.sp,
                    lineHeight = (size * 1.5f).sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
                is BoardBlock.Table -> BoardTable(b.rows, size)
                is BoardBlock.Fig -> BoardFigure(
                    name = b.name,
                    maxHeight = 220.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun BoardTable(rows: List<List<String>>, size: Float) {
    val cols = rows.maxOf { it.size }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(2.dp, ChalkWhite)) {
        rows.forEach { r ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                for (i in 0 until cols) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(1.dp, ChalkWhite)
                            .padding(horizontal = 3.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = r.getOrElse(i) { "" },
                            style = ChalkStyle,
                            fontSize = (size * 0.85f).sp,
                            lineHeight = (size * 1.3f).sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardVDivider() {
    Box(Modifier.width(2.dp).fillMaxHeight().background(ChalkWhite.copy(alpha = 0.85f)))
}

@Composable
private fun BoardHDivider() {
    Box(Modifier.fillMaxWidth().height(2.dp).background(ChalkWhite.copy(alpha = 0.85f)))
}

private val ChalkStyle = TextStyle(
    color = ChalkWhite,
    fontWeight = FontWeight.ExtraBold,
    shadow = Shadow(color = ChalkWhite.copy(alpha = 0.35f), offset = Offset(0f, 0f), blurRadius = 4f)
)
