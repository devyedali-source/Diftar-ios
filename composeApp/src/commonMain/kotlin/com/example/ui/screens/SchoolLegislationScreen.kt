package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class LegislationArticle(
    val id: String,
    val documentId: String,
    val documentTitle: String,
    val documentSubtitle: String? = null,
    val source: String,
    val sourceDetails: String? = null,
    val section: String,
    val articleNumber: Int,
    val articleLabel: String,
    val articleText: String,
    val sourcePage: Int? = null,
    val officialGazetteIssue: String? = null,
    val decreeDate: String? = null,
    val publicationDate: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolLegislationScreen(
    context: Context,
    appLanguage: String
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var articles by remember { mutableStateOf<List<LegislationArticle>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // SharedPreferences setup for saved materials and last read
    val sharedPrefs = remember { context.getSharedPreferences("school_legislation_prefs", Context.MODE_PRIVATE) }
    var savedArticleIds by remember {
        mutableStateOf(sharedPrefs.getStringSet("saved_articles", emptySet()) ?: emptySet())
    }
    var lastReadId by remember {
        mutableStateOf(sharedPrefs.getString("last_read_article_id", null))
    }

    // Load static data from JSON files offline
    LaunchedEffect(Unit) {
        try {
            val loadedList = mutableListOf<LegislationArticle>()

            // Load verified school legislation JSON with fallback if the new file is not yet uploaded
            val jsonText = try {
                context.assets.open("school_legislation_verified_v2.json.gz").use { base ->
                    java.util.zip.GZIPInputStream(base).use { gz ->
                        java.io.InputStreamReader(gz, Charsets.UTF_8).use { reader ->
                            reader.readText()
                        }
                    }
                }
            } catch (e1: Exception) {
                try {
                    context.assets.open("school_legislation_FINAL_VERIFIED.json").bufferedReader().use { it.readText() }
                } catch (e2: Exception) {
                    context.assets.open("school_legislation_verified_v2.json").bufferedReader().use { it.readText() }
                }
            }
            val rootObj = JSONObject(jsonText)
            val documentsArray = rootObj.getJSONArray("documents")
            for (i in 0 until documentsArray.length()) {
                val docObj = documentsArray.getJSONObject(i)
                val articlesArray = docObj.getJSONArray("articles")
                for (j in 0 until articlesArray.length()) {
                    val artObj = articlesArray.getJSONObject(j)
                    loadedList.add(parseArticle(artObj))
                }
            }

            articles = loadedList
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Internal Regulation, 2: Decree, 3: Saved

    val filteredArticles = remember(articles, searchQuery, selectedTab, savedArticleIds) {
        articles.filter { article ->
            val matchesTab = when (selectedTab) {
                1 -> article.documentId == "internal_school_regulation"
                2 -> article.documentId == "decree_2021_078"
                3 -> savedArticleIds.contains(article.id)
                4 -> article.documentId == "decision_2026_school_directors"
                else -> true
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                article.articleText.contains(searchQuery, ignoreCase = true) ||
                        article.section.contains(searchQuery, ignoreCase = true) ||
                        article.articleLabel.contains(searchQuery, ignoreCase = true) ||
                        article.documentTitle.contains(searchQuery, ignoreCase = true)
            }

            matchesTab && matchesSearch
        }
    }

    var expandedArticleId by remember { mutableStateOf<String?>(null) }

    val lastReadArticle = remember(lastReadId, articles) {
        articles.find { it.id == lastReadId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBg)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث في المواد، الأبواب، أو النصوص... 🔍", fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = BentoPrimary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "مسح",
                            tint = BentoPrimaryDesc
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BentoPrimary,
                unfocusedBorderColor = BentoGrayOutline,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Tabs to filter documents
        val tabs = listOf(
            "الكل 📋" to 0,
            "النظام الداخلي 🏫" to 1,
            "المرسوم 2021-078 ⚖️" to 2,
            "مقرر تعيين المديرين 🎓" to 4,
            "المحفوظات ⭐" to 3
        )
        val selectedTabIndex = tabs.indexOfFirst { it.second == selectedTab }

        ScrollableTabRow(
            selectedTabIndex = if (selectedTabIndex >= 0) selectedTabIndex else 0,
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            contentColor = BentoPrimary,
            divider = {},
            indicator = { tabPositions ->
                val tabIdx = tabs.indexOfFirst { it.second == selectedTab }
                if (tabIdx >= 0 && tabIdx < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabIdx]),
                        color = BentoPrimary
                    )
                }
            }
        ) {
            tabs.forEach { (title, index) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BentoPrimary)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // "Last Read" Banner
                if (lastReadArticle != null && searchQuery.isEmpty()) {
                    item {
                        Card(
                            onClick = {
                                expandedArticleId = lastReadArticle.id
                                coroutineScope.launch {
                                    val index = filteredArticles.indexOfFirst { it.id == lastReadArticle.id }
                                    if (index >= 0) {
                                        listState.animateScrollToItem(index)
                                    }
                                }
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = BentoSoftPurpleContainer.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📖", fontSize = 20.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "تابع القراءة من حيث توقفت:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoPrimary
                                    )
                                    Text(
                                        text = "${lastReadArticle.articleLabel}: ${lastReadArticle.section}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "انتقل",
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Header info card
                if (searchQuery.isEmpty() && selectedTab != 3) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🇲🇷", fontSize = 20.sp)
                                    Text(
                                        text = "التشريع المدرسي الموريتاني",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = BentoSecondary
                                    )
                                }
                                Text(
                                    text = "دليل التشريعات والأنظمة الرسمية الصادرة عن وزارة التهذيب الوطني وإصلاح النظام التعليمي. يضم 131 مادة كاملة بدون إنترنت لتمكين المعلم من حقوقه وواجباته وتنظيم عمله اليومي.",
                                    fontSize = 11.5.sp,
                                    color = BentoPrimaryDesc,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                if (filteredArticles.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🔍", fontSize = 36.sp)
                                Text(
                                    text = if (selectedTab == 3) "لا توجد مواد محفوظة بعد." else "لم يتم العثور على أي نتائج تطابق بحثك.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = BentoPrimaryDesc,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredArticles, key = { it.id }) { article ->
                        val isExpanded = expandedArticleId == article.id
                        val isSaved = savedArticleIds.contains(article.id)

                        ArticleCard(
                            article = article,
                            isExpanded = isExpanded,
                            isSaved = isSaved,
                            onToggleExpand = {
                                expandedArticleId = if (isExpanded) null else article.id
                                if (!isExpanded) {
                                    // Save to Last Read
                                    sharedPrefs.edit().putString("last_read_article_id", article.id).apply()
                                    lastReadId = article.id
                                }
                            },
                            onToggleSave = {
                                val currentSet = sharedPrefs.getStringSet("saved_articles", emptySet()) ?: emptySet()
                                val newSet = if (currentSet.contains(article.id)) {
                                    currentSet - article.id
                                } else {
                                    currentSet + article.id
                                }
                                sharedPrefs.edit().putStringSet("saved_articles", newSet).apply()
                                savedArticleIds = newSet
                                Toast.makeText(
                                    context,
                                    if (newSet.contains(article.id)) "تم حفظ المادة في المفضلة ⭐" else "تمت الإزالة من المفضلة 🗑️",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onShare = {
                                val shareText = buildString {
                                    appendLine("🇲🇷 [تطبيق دفتر المعلم - التشريع المدرسي]")
                                    appendLine("📄 ${article.documentTitle}")
                                    appendLine("🏷️ الباب/القسم: ${article.section}")
                                    appendLine("📌 ${article.articleLabel}")
                                    appendLine("----------------------------------------")
                                    appendLine(article.articleText)
                                    appendLine("----------------------------------------")
                                    appendLine("المصدر: ${article.source}")
                                    if (article.sourcePage != null) {
                                        appendLine("الصفحة: ${article.sourcePage}")
                                    }
                                    if (!article.officialGazetteIssue.isNullOrBlank()) {
                                        appendLine("العدد: ${article.officialGazetteIssue}")
                                    }
                                    if (!article.decreeDate.isNullOrBlank()) {
                                        appendLine("تاريخ المرسوم: ${article.decreeDate}")
                                    }
                                    appendLine("تمت المشاركة من تطبيق «دفتر المعلم» للتعليم الأساسي")
                                }

                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "${article.articleLabel} - ${article.documentTitle}")
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "شارك المادة عبر:"))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleCard(
    article: LegislationArticle,
    isExpanded: Boolean,
    isSaved: Boolean,
    onToggleExpand: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) Color.White else Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 3.dp else 1.dp),
        shape = RoundedCornerShape(12.dp),
        border = if (isExpanded) BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.4f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            // Upper Header (Label + Star toggle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BentoSoftPurpleContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = article.articleLabel,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.5.sp,
                            color = BentoPrimary
                        )
                    }
                    Text(
                        text = when (article.documentId) {
                            "internal_school_regulation" -> "النظام الداخلي"
                            "decision_2026_school_directors" -> "مقرر تعيين المديرين"
                            else -> "المرسوم 2021-078"
                        },
                        fontSize = 10.5.sp,
                        color = BentoPrimaryDesc,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { onToggleSave() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "حفظ",
                        tint = if (isSaved) Color(0xFFFFB300) else BentoGrayOutline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Section Info
            Text(
                text = article.section,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BentoSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle or Doc full Title
            Text(
                text = article.documentTitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = BentoPrimaryDesc,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Article Content (Truncated or Full text depending on isExpanded)
            if (!isExpanded) {
                Text(
                    text = article.articleText,
                    fontSize = 12.sp,
                    color = BentoPrimaryDesc,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Justify
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اضغط لعرض المادة كاملة... 👇",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )
                }
            } else {
                Text(
                    text = article.articleText,
                    fontSize = 13.sp,
                    color = BentoText,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Justify
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = BentoGrayOutline.copy(alpha = 0.4f)
                )

                // Extra details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BentoLightLavender.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🏛️ المصدر:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                        Text(article.source, fontSize = 10.sp, color = BentoPrimaryDesc)
                    }

                    if (article.sourcePage != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📄 الصفحة:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                            Text(article.sourcePage.toString(), fontSize = 10.sp, color = BentoPrimaryDesc)
                        }
                    }

                    if (!article.officialGazetteIssue.isNullOrBlank()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🗞️ العدد:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                            Text(article.officialGazetteIssue, fontSize = 10.sp, color = BentoPrimaryDesc)
                        }
                    }

                    if (!article.decreeDate.isNullOrBlank()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📅 تاريخ المرسوم:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                            Text(article.decreeDate, fontSize = 10.sp, color = BentoPrimaryDesc)
                        }
                    }

                    if (!article.sourceDetails.isNullOrBlank()) {
                        Text(
                            text = article.sourceDetails,
                            fontSize = 9.5.sp,
                            color = BentoPrimaryDesc.copy(alpha = 0.8f),
                            lineHeight = 13.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons (Collapse, Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onToggleExpand() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoPrimary),
                        border = BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طي المادة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onShare() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة المادة 🤝", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun parseArticle(obj: JSONObject): LegislationArticle {
    return LegislationArticle(
        id = obj.getString("id"),
        documentId = obj.getString("documentId"),
        documentTitle = obj.getString("documentTitle"),
        documentSubtitle = obj.optString("documentSubtitle", "").takeIf { it.isNotBlank() },
        source = obj.getString("source"),
        sourceDetails = obj.optString("sourceDetails", "").takeIf { it.isNotBlank() },
        section = obj.getString("section"),
        articleNumber = obj.getInt("articleNumber"),
        articleLabel = obj.getString("articleLabel"),
        articleText = obj.getString("articleText"),
        sourcePage = if (obj.has("sourcePage")) obj.getInt("sourcePage") else null,
        officialGazetteIssue = obj.optString("officialGazetteIssue", "").takeIf { it.isNotBlank() },
        decreeDate = obj.optString("decreeDate", "").takeIf { it.isNotBlank() },
        publicationDate = obj.optString("publicationDate", "").takeIf { it.isNotBlank() }
    )
}
