package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun ConcoursGuideScreen(onBack: () -> Unit) {
    val placeContext = LocalContext.current
    var openedSubject by remember {
        mutableStateOf<String?>(com.example.data.state.LastPlaceStore.getString(placeContext, "concours_subject"))
    }
    LaunchedEffect(openedSubject) {
        com.example.data.state.LastPlaceStore.putString(placeContext, "concours_subject", openedSubject)
    }

    if (openedSubject != null) {
        ConcoursSubjectScreen(subjectCode = openedSubject!!, onBack = { openedSubject = null })
        return
    }

    BackHandler { onBack() }

    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val ok = withContext(Dispatchers.IO) {
            ConcoursRepository.load(context) != null
        }
        loadFailed = !ok
        loading = false
    }

    DisposableEffect(Unit) { onDispose { try { ConcoursRepository.release() } catch (e: Exception) { e.printStackTrace() } } }

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

    if (loadFailed) {
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

    val subjects = remember(context) { ConcoursRepository.getSubjects(context) }
    val totalExercises = remember(context) { ConcoursRepository.getTotalExercisesCount(context) }

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
                    text = "دليل النجاح في مسابقة دخول سنة أولى إعدادية",
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoPrimary
                )
            }
        }

        // Subtitle line: number of subjects and total exercises count
        Text(
            text = "${subjects.size} مواد • $totalExercises تمرين",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = BentoPrimaryDesc,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // LazyColumn with subject cards
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(subjects, key = { it.code }) { subject ->
                val exercisesCount = remember(subject.code) {
                    ConcoursRepository.getSubjectExercisesCount(context, subject.code)
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            openedSubject = subject.code
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, BentoGrayOutline.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subject.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$exercisesCount تمرين",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = BentoPrimary
                        )
                    }
                }
            }
        }
    }
}
