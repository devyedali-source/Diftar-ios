package com.example.ui.components

import com.example.compat.*
import kotlinx.coroutines.IO

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.example.data.models.ClassSection
import com.example.ui.TeacherViewModel
import com.example.ui.theme.*

/** Shrinks a font size just enough for the text to fit the given width (no clipping, no overflow). */
private fun fitSp(text: String, maxWidthDp: Float, baseSp: Float, minSp: Float = 6f): Float {
    val estimated = text.length * baseSp * 0.40f
    if (estimated <= maxWidthDp || estimated <= 0f) return baseSp
    return (baseSp * maxWidthDp / estimated).coerceAtLeast(minSp)
}

val StampInkBlue = Color(0xFF153E90)
val StampInkBlueLight = Color(0xFFEBF2FF)

/**
 * Authentic Blue Official Stamp Icon evoking an official blue ink stamp seal on paper.
 */
@Composable
fun PaperStampSettingsIcon(
    modifier: Modifier = Modifier,
    tint: Color = StampInkBlue
) {
    Box(
        modifier = modifier.size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().rotate(-6f)) {
            val w = size.width
            val h = size.height

            // 1. Outer circle (thick official ink stamp border)
            drawCircle(
                color = tint,
                radius = w * 0.44f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.08f)
            )

            // 2. Inner concentric circle (fine stamp ring)
            drawCircle(
                color = tint.copy(alpha = 0.9f),
                radius = w * 0.33f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.035f)
            )

            // 3. Center 5-pointed official star
            val starCenter = center
            val outerR = w * 0.12f
            val innerR = w * 0.05f
            val starPath = androidx.compose.ui.graphics.Path()
            for (i in 0 until 10) {
                val angle = Math.toRadians((i * 36 - 90).toDouble())
                val r = if (i % 2 == 0) outerR else innerR
                val x = (starCenter.x + r * Math.cos(angle)).toFloat()
                val y = (starCenter.y + r * Math.sin(angle)).toFloat()
                if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
            }
            starPath.close()
            drawPath(starPath, color = tint)

            // 4. Top and bottom official seal text simulation bars
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(w * 0.27f, h * 0.27f),
                end = androidx.compose.ui.geometry.Offset(w * 0.73f, h * 0.27f),
                strokeWidth = w * 0.04f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(w * 0.27f, h * 0.73f),
                end = androidx.compose.ui.geometry.Offset(w * 0.73f, h * 0.73f),
                strokeWidth = w * 0.04f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // 5. Left & right official stamp dots
            drawCircle(
                color = tint,
                radius = w * 0.035f,
                center = androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.5f)
            )
            drawCircle(
                color = tint,
                radius = w * 0.035f,
                center = androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.5f)
            )
        }
    }
}

/**
 * High-fidelity Jetpack Compose stamp preview matching the authentic blue ink stamp exactly
 * as rendered on grade slips and report sheets.
 */
@Composable
fun OfficialStampVisual(
    roleTitle: String,
    personName: String,
    schoolName: String,
    shape: String = "rectangle",
    modifier: Modifier = Modifier,
    scaleSize: Dp = 220.dp,
    financialId: String = ""
) {
    val cleanSchool = schoolName.trim().ifBlank { "الطلحايه 1" }
    val formattedSchool = if (cleanSchool.startsWith("مدرسة")) cleanSchool else "مدرسة $cleanSchool"
    val cleanRole = when (roleTitle.trim()) {
        "معلم", "المعلم" -> "المعلم"
        "مدير", "المدير" -> "المدير"
        else -> roleTitle.trim().ifBlank { "المعلم" }
    }
    val cleanName = personName.trim()
    val cleanFinancialId = financialId.trim()

    val isCircle = shape.equals("circle", ignoreCase = true) || (cleanRole == "المدير" && !shape.equals("rectangle", ignoreCase = false))

    if (isCircle) {
        val baseDiameter = 190.dp
        val targetDiameter = if (scaleSize.isSpecified && scaleSize > 0.dp) scaleSize else baseDiameter
        val scaleFactor = (targetDiameter.value / baseDiameter.value).coerceAtLeast(0.1f)

        Box(
            modifier = modifier
                .size(targetDiameter)
                .rotate(-1.5f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(baseDiameter)
                    .graphicsLayer {
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // 1. Concentric circles & curved circular text
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val sizePx = size.minDimension
                            val cx = sizePx / 2f
                            val cy = sizePx / 2f

                            val rOuter = (sizePx / 2f) - 3.dp.toPx()
                            val rSecond = rOuter - 4.5.dp.toPx()
                            val rInner = sizePx * 0.31f
                            val rTextMid = (rSecond + rInner) / 2f

                            // Outer thick border
                            drawCircle(
                                color = StampInkBlue,
                                radius = rOuter,
                                center = center,
                                style = Stroke(width = 3.2.dp.toPx())
                            )

                            // Second thin border
                            drawCircle(
                                color = StampInkBlue,
                                radius = rSecond,
                                center = center,
                                style = Stroke(width = 1.2.dp.toPx())
                            )

                            // Inner circle border
                            drawCircle(
                                color = StampInkBlue,
                                radius = rInner,
                                center = center,
                                style = Stroke(width = 1.4.dp.toPx())
                            )

                            // Draw circular curved text strictly within the ring
                            // النص الدائري داخل الحلقة (رسم مخصّص للآيفون يطابق drawTextOnPath في أندرويد)
                            val topText = "الجمهورية الإسلامية الموريتانية"
                            val bottomLabel = if (cleanRole in listOf("مدير", "المدير")) "- المدير -" else "- $cleanRole -"
                            com.example.compat.drawStampRingText(this, topText, bottomLabel, cx, cy, rTextMid, density, StampInkBlue)
                        }

                        // 2. Inside the circle: School Name & Director Name
                        val innerSizeDp = (baseDiameter.value * 0.58f).dp
                        Column(
                            modifier = Modifier
                                .size(innerSizeDp)
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = formattedSchool,
                                fontSize = fitSp(formattedSchool, 98f, 11.5f).sp,
                                fontWeight = FontWeight.Black,
                                color = StampInkBlue,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 14.sp
                            )

                            Spacer(modifier = Modifier.height(3.dp))
                            HorizontalDivider(
                                modifier = Modifier.width(48.dp),
                                thickness = 1.dp,
                                color = StampInkBlue
                            )
                            Spacer(modifier = Modifier.height(3.dp))

                            val directorDisplayName = if (cleanName.isNotBlank()) cleanName else "المدير"
                            Text(
                                text = directorDisplayName,
                                fontSize = fitSp(directorDisplayName, 98f, 11f).sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = StampInkBlue,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 13.sp
                            )

                            if (cleanFinancialId.isNotBlank()) {
                                Text(
                                    text = "($cleanFinancialId)",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = StampInkBlue,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        val roleLine = when {
            cleanName.isNotBlank() && cleanFinancialId.isNotBlank() -> "$cleanRole: $cleanName ($cleanFinancialId)"
            cleanName.isNotBlank() -> "$cleanRole: $cleanName"
            else -> cleanRole
        }

        val baseWidth = 220.dp
        val baseHeight = 110.dp
        val targetWidth = if (scaleSize.isSpecified && scaleSize > 0.dp) scaleSize else baseWidth
        val scaleFactor = (targetWidth.value / baseWidth.value).coerceAtLeast(0.1f)
        val targetHeight = (baseHeight.value * scaleFactor).dp

        Box(
            modifier = modifier
                .size(width = targetWidth, height = targetHeight)
                .rotate(-1.5f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(width = baseWidth, height = baseHeight)
                    .graphicsLayer {
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp)
                            .border(BorderStroke(3.2.dp, StampInkBlue), RoundedCornerShape(6.dp))
                            .padding(3.dp)
                            .border(BorderStroke(1.4.dp, StampInkBlue), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "الجمهورية الإسلامية الموريتانية",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = StampInkBlue,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "★",
                                    fontSize = 14.sp,
                                    color = StampInkBlue,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Text(
                                    text = formattedSchool,
                                    fontSize = fitSp(formattedSchool, 150f, 12.5f).sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StampInkBlue,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "★",
                                    fontSize = 14.sp,
                                    color = StampInkBlue,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                            Text(
                                text = roleLine,
                                fontSize = fitSp(roleLine, 190f, 13f).sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = StampInkBlue,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Section for configuring official stamps (Teacher and Principal)
 * Supports collapsing the form upon save until the user clicks Edit.
 */
@Composable
fun OfficialStampSettingsSection(
    viewModel: TeacherViewModel,
    activeClass: ClassSection,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedStampTab by remember { mutableIntStateOf(0) } // 0: Teacher Stamp, 1: Principal Stamp

    // Teacher Stamp States from ViewModel
    val teacherName by viewModel.teacherStampName.collectAsState()
    val teacherRole by viewModel.teacherStampRole.collectAsState()
    val teacherShowInReports by viewModel.showTeacherStampInReports.collectAsState()

    // Editing mode state (collapsed when already saved)
    var isEditingTeacher by remember(teacherName) { mutableStateOf(teacherName.isBlank()) }

    var inputTeacherName by remember(teacherName) { mutableStateOf(teacherName) }
    var inputTeacherRole by remember(teacherRole) {
        mutableStateOf(
            "المعلم"
        )
    }
    var inputTeacherShowInReports by remember(teacherShowInReports) { mutableStateOf(teacherShowInReports) }

    // Principal Stamp States from ViewModel
    val principalName by viewModel.principalStampName.collectAsState()
    val principalRole by viewModel.principalStampRole.collectAsState()
    val principalFinancialId by viewModel.principalStampFinancialId.collectAsState()
    val principalShowInReports by viewModel.showPrincipalStampInReports.collectAsState()

    // Editing mode state for Principal
    var isEditingPrincipal by remember(principalName) { mutableStateOf(principalName.isBlank()) }

    var inputPrincipalName by remember(principalName) { mutableStateOf(principalName) }
    var inputPrincipalRole by remember(principalRole) {
        mutableStateOf(
            "المدير"
        )
    }
    var inputPrincipalShowInReports by remember(principalShowInReports) { mutableStateOf(principalShowInReports) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, StampInkBlue.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(StampInkBlueLight)
                        .border(1.dp, StampInkBlue.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    PaperStampSettingsIcon(
                        modifier = Modifier.size(24.dp),
                        tint = StampInkBlue
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "إنشاء وتخصيص ختم وطابع المدرسة 🖋️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = StampInkBlue
                    )
                    Text(
                        text = "طابع رسمي للجمهورية الإسلامية الموريتانية مع اسم مدرسة القسم (${activeClass.schoolName.ifBlank { "الطلحايه 1" }}).",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoPrimaryDesc
                    )
                }
            }

            // Tab Selector: ختم المعلم / ختم المدير
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Teacher Tab
                val isTeacherTab = selectedStampTab == 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isTeacherTab) StampInkBlue else Color.Transparent)
                        .clickable { selectedStampTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "👨‍🏫 ختم المعلم",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isTeacherTab) Color.White else Color.DarkGray
                        )
                        if (teacherName.isNotBlank()) {
                            Text(text = "✔", fontSize = 11.sp, color = if (isTeacherTab) Color(0xFF81C784) else Color(0xFF2E7D32))
                        }
                    }
                }

                // Principal Tab
                val isPrincipalTab = selectedStampTab == 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPrincipalTab) StampInkBlue else Color.Transparent)
                        .clickable { selectedStampTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "👔 ختم المدير",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isPrincipalTab) Color.White else Color.DarkGray
                        )
                        if (principalName.isNotBlank()) {
                            Text(text = "✔", fontSize = 11.sp, color = if (isPrincipalTab) Color(0xFF81C784) else Color(0xFF2E7D32))
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            if (selectedStampTab == 0) {
                // ==================== TEACHER STAMP ====================
                if (!isEditingTeacher && teacherName.isNotBlank()) {
                    // --- SAVED / CONFIRMED VIEW (Fields hidden) ---
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Success Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F5E9))
                                .border(1.dp, Color(0xFF81C784), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "طابع المعلم مسجل ومعتمد بنجاح ✔️",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1B5E20)
                                )
                                val displayRole = "المعلم"
                                Text(
                                    text = "صاحب الطابع: $teacherName ($displayRole) - شكل مستطيل",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        // Prominent Visual Stamp Display
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "صورة الطابع الرسمية المعتمدة في كشوف الدرجات:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = StampInkBlue,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            OfficialStampVisual(
                                roleTitle = "المعلم",
                                personName = teacherName,
                                schoolName = activeClass.schoolName,
                                shape = "rectangle"
                            )
                        }

                        // Toggle visibility in reports
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (teacherShowInReports) StampInkBlueLight else Color(0xFFF8FAFC))
                                .border(1.dp, if (teacherShowInReports) StampInkBlue.copy(alpha = 0.4f) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .clickable { viewModel.setTeacherStampShowInReports(!teacherShowInReports) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = teacherShowInReports,
                                onCheckedChange = { viewModel.setTeacherStampShowInReports(it) },
                                colors = CheckboxDefaults.colors(checkedColor = StampInkBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "إظهار الطابع في كشوف الدرجات والنتائج التفصيلية ✔️",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (teacherShowInReports) StampInkBlue else Color.DarkGray
                                )
                                Text(
                                    text = if (teacherShowInReports) "الطابع مفعل ويظهر تلقائياً أسفل توقيع المعلم في الكشوف." else "الطابع مخفي حالياً من الكشوف المطبوعة.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Actions: Edit and Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    inputTeacherName = teacherName
                                    inputTeacherRole = "المعلم"
                                    inputTeacherShowInReports = teacherShowInReports
                                    isEditingTeacher = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StampInkBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تعديل بيانات الطابع ✏️", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    inputTeacherName = ""
                                    inputTeacherShowInReports = false
                                    viewModel.updateTeacherStamp(
                                        name = "",
                                        shape = "rectangle",
                                        role = "المعلم",
                                        showInReports = false
                                    )
                                    isEditingTeacher = true
                                    Toast.makeText(context, "تم مسح بيانات طابع المعلم.", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                modifier = Modifier.weight(0.8f)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Red)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مسح الطابع", fontSize = 11.5.sp, color = Color.Red)
                            }
                        }
                    }
                } else {
                    // --- EDIT / INPUT FORM (Visible when editing or empty) ---
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Name Field
                        OutlinedTextField(
                            value = inputTeacherName,
                            onValueChange = { inputTeacherName = it },
                            label = { Text("اسم صاحب الطابع (${inputTeacherRole})") },
                            placeholder = { Text("مثال: سيدى سالم") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = StampInkBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Live Visual Preview
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "معاينة حية لطابع المعلم (كما يظهر على كشوف الدرجات):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OfficialStampVisual(
                                roleTitle = inputTeacherRole,
                                personName = inputTeacherName,
                                schoolName = activeClass.schoolName,
                                shape = "rectangle"
                            )
                        }

                        // Checkbox for reports
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (inputTeacherShowInReports) StampInkBlueLight else Color(0xFFF8FAFC))
                                .border(1.dp, if (inputTeacherShowInReports) StampInkBlue.copy(alpha = 0.4f) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .clickable { inputTeacherShowInReports = !inputTeacherShowInReports }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = inputTeacherShowInReports,
                                onCheckedChange = { inputTeacherShowInReports = it },
                                colors = CheckboxDefaults.colors(checkedColor = StampInkBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "إظهار الطابع في كشوف الدرجات والنتائج التفصيلية ✔️",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (inputTeacherShowInReports) StampInkBlue else Color.DarkGray
                                )
                                Text(
                                    text = "يظهر أسفل توقيع المعلم في كافة الكشوف المطبوعة والنتائج التفصيلية.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (inputTeacherName.trim().isBlank()) {
                                        Toast.makeText(context, "يرجى كتابة اسم صاحب الطابع", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.updateTeacherStamp(
                                            name = inputTeacherName.trim(),
                                            shape = "rectangle",
                                            role = inputTeacherRole,
                                            showInReports = inputTeacherShowInReports
                                        )
                                        isEditingTeacher = false
                                        Toast.makeText(context, "تم تسجيل واعتماد ختم المعلم بنجاح! 💮", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StampInkBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("حفظ واعتماد الطابع 💾", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                            }

                            if (teacherName.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        inputTeacherName = teacherName
                                        inputTeacherRole = "المعلم"
                                        inputTeacherShowInReports = teacherShowInReports
                                        isEditingTeacher = false
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(0.8f)
                                ) {
                                    Text("إلغاء", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                // ==================== PRINCIPAL STAMP ====================
                if (!isEditingPrincipal && principalName.isNotBlank()) {
                    // --- SAVED / CONFIRMED VIEW (Fields hidden) ---
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Success Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F5E9))
                                .border(1.dp, Color(0xFF81C784), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "طابع المدير مسجل ومعتمد بنجاح ✔️",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1B5E20)
                                )
                                val displayRole = "المدير"
                                Text(
                                    text = "صاحب الطابع: $principalName ($displayRole) - شكل دائرى",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        // Prominent Visual Stamp Display
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "صورة الطابع الرسمية المعتمدة في كشوف الدرجات:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = StampInkBlue,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            OfficialStampVisual(
                                roleTitle = "المدير",
                                personName = principalName,
                                schoolName = activeClass.schoolName,
                                shape = "circle",
                                financialId = principalFinancialId
                            )
                        }

                        // Toggle visibility in reports
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (principalShowInReports) StampInkBlueLight else Color(0xFFF8FAFC))
                                .border(1.dp, if (principalShowInReports) StampInkBlue.copy(alpha = 0.4f) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .clickable { viewModel.setPrincipalStampShowInReports(!principalShowInReports) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = principalShowInReports,
                                onCheckedChange = { viewModel.setPrincipalStampShowInReports(it) },
                                colors = CheckboxDefaults.colors(checkedColor = StampInkBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "إظهار الطابع في كشوف الدرجات والنتائج التفصيلية ✔️",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (principalShowInReports) StampInkBlue else Color.DarkGray
                                )
                                Text(
                                    text = if (principalShowInReports) "الطابع مفعل ويظهر تلقائياً أسفل توقيع المدير في الكشوف." else "الطابع مخفي حالياً من الكشوف المطبوعة.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Actions: Edit and Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    inputPrincipalName = principalName
                                    inputPrincipalRole = "المدير"
                                    inputPrincipalShowInReports = principalShowInReports
                                    isEditingPrincipal = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StampInkBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تعديل بيانات الطابع ✏️", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    inputPrincipalName = ""
                                    inputPrincipalShowInReports = false
                                    viewModel.updatePrincipalStamp(
                                        name = "",
                                        shape = "rectangle",
                                        role = "المدير",
                                        showInReports = false
                                    )
                                    isEditingPrincipal = true
                                    Toast.makeText(context, "تم مسح بيانات طابع المدير.", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                modifier = Modifier.weight(0.8f)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Red)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مسح الطابع", fontSize = 11.5.sp, color = Color.Red)
                            }
                        }
                    }
                } else {
                    // --- EDIT / INPUT FORM FOR PRINCIPAL ---
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Name Field
                        OutlinedTextField(
                            value = inputPrincipalName,
                            onValueChange = { inputPrincipalName = it },
                            label = { Text("اسم صاحب الطابع (${inputPrincipalRole})") },
                            placeholder = { Text("مثال: عبد الله أحمد") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = StampInkBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Live Visual Preview
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "معاينة حية لطابع المدير (كما يظهر على كشوف الدرجات):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OfficialStampVisual(
                                roleTitle = inputPrincipalRole,
                                personName = inputPrincipalName,
                                schoolName = activeClass.schoolName,
                                shape = "circle",
                                financialId = principalFinancialId
                            )
                        }

                        // Checkbox for reports
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (inputPrincipalShowInReports) StampInkBlueLight else Color(0xFFF8FAFC))
                                .border(1.dp, if (inputPrincipalShowInReports) StampInkBlue.copy(alpha = 0.4f) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .clickable { inputPrincipalShowInReports = !inputPrincipalShowInReports }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = inputPrincipalShowInReports,
                                onCheckedChange = { inputPrincipalShowInReports = it },
                                colors = CheckboxDefaults.colors(checkedColor = StampInkBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "إظهار الطابع في كشوف الدرجات والنتائج التفصيلية ✔️",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (inputPrincipalShowInReports) StampInkBlue else Color.DarkGray
                                )
                                Text(
                                    text = "يظهر أسفل توقيع المدير في كافة الكشوف المطبوعة والنتائج التفصيلية.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (inputPrincipalName.trim().isBlank()) {
                                        Toast.makeText(context, "يرجى كتابة اسم صاحب الطابع", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.updatePrincipalStamp(
                                            name = inputPrincipalName.trim(),
                                            shape = "circle",
                                            role = inputPrincipalRole,
                                            showInReports = inputPrincipalShowInReports,
                                            financialId = principalFinancialId
                                        )
                                        isEditingPrincipal = false
                                        Toast.makeText(context, "تم تسجيل واعتماد ختم المدير بنجاح! 💮", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StampInkBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("حفظ واعتماد الطابع 💾", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                            }

                            if (principalName.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        inputPrincipalName = principalName
                                        inputPrincipalRole = "المدير"
                                        inputPrincipalShowInReports = principalShowInReports
                                        isEditingPrincipal = false
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(0.8f)
                                ) {
                                    Text("إلغاء", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
