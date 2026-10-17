package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.PrintDesign
import com.example.ui.PrintDesignPreview

/** معاينة حقيقية (نفس CSS الطباعة) لتصميم واحد، غير قابلة للتمرير. */
@Composable
private fun DesignPreview(kind: Int, design: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val (w, h) = PrintDesignPreview.size(kind)
    val html = remember(kind, design) { PrintDesignPreview.html(context, kind, design) }
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val heightDp = maxWidth * (h.toFloat() / w.toFloat())
        HtmlPreview(html = html, modifier = Modifier.fillMaxWidth().height(heightDp))
    }
}

/** إعدادات تصميم رأسيات اللوائح وإطارات الكشوف وشهادات التفوق، مع معاينة حقيقية لكل خيار. */
@Composable
fun PrintDesignDialog(onDismiss: () -> Unit) {
    val context: Context = LocalContext.current
    var header by remember { mutableStateOf(PrintDesign.header) }
    var card by remember { mutableStateOf(PrintDesign.card) }
    var cert by remember { mutableStateOf(PrintDesign.cert) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.96f).fillMaxHeight(0.94f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Text("تصميم اللوائح والكشوف 🎨", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "اضغط على الشكل الذي يعجبك. التصميم يغيّر الزخرفة فقط ولا يغيّر أي معلومة. «الوضع الأصلي» هو التلقائي ويمكن الرجوع إليه في أي وقت.",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { SectionTitle("رأسية اللوائح (تُطبَّق على كل اللوائح)") }
                    itemsIndexed(PrintDesign.headerNames) { i, n ->
                        OptionCard(n, header == i, onPick = { header = i; PrintDesign.save(context, header = i) }) {
                            DesignPreview(0, i)
                        }
                    }
                    item { SectionTitle("تصميم الكشوف (يُطبَّق على كل الكشوف)") }
                    itemsIndexed(PrintDesign.cardNames) { i, n ->
                        OptionCard(n, card == i, onPick = { card = i; PrintDesign.save(context, card = i) }) {
                            DesignPreview(1, i)
                        }
                    }
                    item { SectionTitle("تصميم شهادات التفوق والتكريم") }
                    itemsIndexed(PrintDesign.certNames) { i, n ->
                        val idx = i + 1
                        OptionCard(n, cert == idx, onPick = { cert = idx; PrintDesign.save(context, cert = idx) }) {
                            DesignPreview(2, idx)
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("تم") }
            }
        }
    }
}

@Composable
private fun SectionTitle(t: String) {
    Text(t, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun OptionCard(name: String, selected: Boolean, onPick: () -> Unit, preview: @Composable () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else Color(0xFFBBBBBB), shape)
            .clickable { onPick() }
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onPick)
            Text(name, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(Color.White)) {
            preview()
            Box(modifier = Modifier.matchParentSize().clickable { onPick() })
        }
    }
}
