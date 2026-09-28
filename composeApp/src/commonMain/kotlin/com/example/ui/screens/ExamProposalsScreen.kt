package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.exams.ExamsRepository
import com.example.ui.theme.BentoBg
import com.example.ui.theme.BentoGrayOutline
import com.example.ui.theme.BentoLightLavender
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDesc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val ExamErrorColor = Color(0xFFC62828)
private val ExamPaperInk = Color(0xFF1A3A8F)
private val ExamPaperLine = Color(0xFF2B4C7E)
private val ExamPaperDesk = Color(0xFFF1F5F9)

@Composable
fun ExamProposalsScreen(
    levelCode: String,
    semaine: Int,
    subjectCode: String,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val isFrench = subjectCode.equals("FR", ignoreCase = true)
    var loading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }

    LaunchedEffect(levelCode, semaine, subjectCode) {
        val ok = withContext(Dispatchers.IO) {
            ExamsRepository.load(context) != null &&
                ExamsRepository.getSubject(context, levelCode, semaine, subjectCode) != null
        }
        loadFailed = !ok
        loading = false
    }

    if (loading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "جارٍ تحميل المحتوى...", fontSize = 14.sp)
        }
        return
    }

    val subject = remember(context, levelCode, semaine, subjectCode) {
        ExamsRepository.getSubject(context, levelCode, semaine, subjectCode)
    }

    if (loadFailed || subject == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "تعذّر تحميل المحتوى",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ExamErrorColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(BentoLightLavender, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "الرجوع",
                    tint = BentoPrimary
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBg)
    ) {
        // Top bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(BentoLightLavender, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "الرجوع",
                        tint = BentoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = subject.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )
                    val label = if (subject.proposals.size > 10) "مقترحاً" else "مقترحات"
                    Text(
                        text = "مقترحات امتحان الأسبوع $semaine • ${subject.proposals.size} $label",
                        fontSize = 12.sp,
                        color = BentoPrimaryDesc
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(ExamPaperDesk)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides (if (isFrench) LayoutDirection.Ltr else LocalLayoutDirection.current)) {
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(4.dp)).border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(4.dp)),
                    color = Color.White,
                    contentColor = ExamPaperInk,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        subject.proposals.forEachIndexed { index, proposal ->
                            if (index > 0) {
                                HorizontalDivider(color = ExamPaperLine.copy(alpha = 0.4f), thickness = 0.8.dp)
                            }
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // رقم المقترح وعنوانه (الحقل title)
                                Text(
                                    text = proposal.title.ifBlank { "المقترح ${index + 1}" },
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExamPaperInk
                                )

                                // الحقل ctx إذا لم يكن فارغًا، بعنوان صغير: "النص أو السياق"
                                if (proposal.ctx.isNotBlank()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(
                                            text = if (isFrench) "Texte / Contexte" else "النص أو السياق",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExamPaperInk
                                        )
                                        Text(
                                            text = proposal.ctx,
                                            fontSize = 14.sp,
                                            color = ExamPaperInk,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }

                                // الحقل task، بعنوان صغير: "التعليمة"
                                if (proposal.task.isNotBlank()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(
                                            text = if (isFrench) "Consigne" else "التعليمة",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExamPaperInk
                                        )
                                        Text(
                                            text = proposal.task,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ExamPaperInk,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
