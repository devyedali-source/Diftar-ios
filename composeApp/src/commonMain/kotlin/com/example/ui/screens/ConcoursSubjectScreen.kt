package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.concours.ConcoursRepository
import com.example.ui.theme.BentoBg
import com.example.ui.theme.BentoGrayOutline
import com.example.ui.theme.BentoLightLavender
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDesc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val CourseBadColor = Color(0xFFC62828)
private val ConcoursInkBlue = Color(0xFF1A3A8F)
private val ConcoursLineBlue = Color(0xFF2B4C7E)
private val ConcoursPaperWhite = Color(0xFFFFFFFF)
private val ConcoursDeskGray = Color(0xFFF1F5F9)

@Composable
fun ConcoursSubjectScreen(subjectCode: String, onBack: () -> Unit) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val ok = withContext(Dispatchers.IO) {
            ConcoursRepository.load(context) != null &&
                ConcoursRepository.getSubject(context, subjectCode) != null
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

    val subject = remember(context, subjectCode) {
        ConcoursRepository.getSubject(context, subjectCode)
    }

    val solutionLabel = if (subjectCode.equals("FR", ignoreCase = true))
        "Solution : " else "الحل: "

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
                color = CourseBadColor
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

                Text(
                    text = subject.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(ConcoursDeskGray)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(4.dp)),
                color = ConcoursPaperWhite,
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // أ) لكل مجال في domains
                    subject.domains.forEach { domain ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = domain.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ConcoursInkBlue
                            )
                            Text(
                                text = "${domain.items.size} تمرين",
                                fontSize = 12.5.sp,
                                color = ConcoursInkBlue
                            )
                        }

                        HorizontalDivider(
                            color = ConcoursLineBlue.copy(alpha = 0.5f),
                            thickness = 0.8.dp
                        )

                        domain.items.forEach { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (item.txt.isNotBlank()) {
                                    Text(
                                        text = item.txt,
                                        fontSize = 14.sp,
                                        color = ConcoursInkBlue,
                                        lineHeight = 22.sp
                                    )
                                }

                                if (item.q.isNotBlank()) {
                                    Text(
                                        text = item.q,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ConcoursInkBlue,
                                        lineHeight = 22.sp
                                    )
                                }

                                if (item.sol.isNotBlank()) {
                                    Text(
                                        text = solutionLabel + item.sol,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1B5E20),
                                        lineHeight = 21.sp
                                    )
                                }
                            }
                        }
                    }

                    // ب) بعد كل المجالات، وفقط إذا كانت لائحة situations غير فارغة
                    if (subject.situations.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "وضعيات عامة",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ConcoursInkBlue
                            )
                            Text(
                                text = "${subject.situations.size} وضعية",
                                fontSize = 12.5.sp,
                                color = ConcoursInkBlue
                            )
                        }

                        HorizontalDivider(
                            color = ConcoursLineBlue.copy(alpha = 0.5f),
                            thickness = 0.8.dp
                        )

                        subject.situations.forEach { situation ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (situation.title.isNotBlank()) {
                                    Text(
                                        text = situation.title,
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ConcoursInkBlue
                                    )
                                }

                                if (situation.ctx.isNotBlank()) {
                                    Text(
                                        text = situation.ctx,
                                        fontSize = 14.sp,
                                        color = ConcoursInkBlue,
                                        lineHeight = 22.sp
                                    )
                                }

                                if (situation.tasks.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        situation.tasks.forEachIndexed { taskIndex, task ->
                                            Text(
                                                text = "${taskIndex + 1}. $task",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = ConcoursInkBlue,
                                                lineHeight = 21.sp
                                            )
                                        }
                                    }
                                }

                                if (situation.sol.isNotBlank()) {
                                    Text(
                                        text = solutionLabel + situation.sol,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1B5E20),
                                        lineHeight = 21.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
