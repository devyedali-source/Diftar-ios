package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.TeacherExchangePost
import com.example.ui.TeacherViewModel
import com.example.ui.theme.*
import com.example.ui.utils.AppLocalization
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Constants for Offer Types
val offerTypes = listOf(
    "تبادل مقاعد ودي دون دفع",
    "بيع مقعد مكان عمله",
    "بحث عن شراء مقعد"
)

// Constants for Specialties
val teacherSpecialties = listOf(
    "معلم عربية",
    "معلم فرنسية",
    "معلم مزدوج",
    "مقدم خدمة عربي",
    "مقدم خدمة فرنسي",
    "مقدم خدمة مزدوج"
)

// Function to send exchange activation request to admin WhatsApp with quick approval link
fun sendExchangeActivationWhatsApp(
    context: Context,
    postId: String,
    userId: String,
    userEmail: String
) {
    if (postId.isBlank()) {
        Toast.makeText(context, "لم يكتمل نشر الإعلان بعد. انتظر لحظات ثم أعد المحاولة.", Toast.LENGTH_LONG).show()
        return
    }
    try {
        val requestId = java.util.UUID.randomUUID().toString()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val requestDoc = mapOf(
            "requestId" to requestId,
            "userId" to userId,
            "exchangePostId" to postId,
            "status" to "pending",
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        db.collection("exchange_activation_requests").document(requestId).set(requestDoc)

        val adminPhone = "22237786585"
        val emailText = userEmail.ifBlank { "غير مسجل" }
        val fullMessage = """
$emailText
أريد نشر وتفعيل إعلان تبادل بين جمهور المعلمين.

🔑 رقم الإعلان (كود الإعلان):
$postId
---------------------------------
رابط طلب التفعيل:
https://diftar-almoaalim.web.app/exchange-approve/$requestId
        """.trimIndent()
        val encoded = java.net.URLEncoder.encode(fullMessage, "UTF-8")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$adminPhone&text=$encoded"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "تعذر فتح تطبيق الواتساب تلقائياً", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun PostActivationDialog(
    postId: String,
    userId: String,
    userEmail: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    sendExchangeActivationWhatsApp(
                        context = context,
                        postId = postId,
                        userId = userId,
                        userEmail = userEmail
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("💬 تواصل عبر واتساب الإدارة", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("📢", fontSize = 24.sp)
                Text(
                    text = "تواصل مع الإدارة لنشر إعلانك",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF1B5E20)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "🏷️ سعر نشر الإعلان: 200 أوقية جديدة",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            text = "ملاحظة: تم حفظ إعلانك، ولن يظهر للعامة إلا بعد تفعيله من طرف مدير الأقسام.",
                            fontSize = 12.sp,
                            color = Color(0xFF5D4037)
                        )
                    }
                }

                Text(
                    text = "اضغط على الزر أدناه لإرسال طلب تفعيل الإعلان رقم (#$postId) مباشرة عبر واتساب الإدارة:",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherExchangeScreen(
    viewModel: TeacherViewModel,
    modifier: Modifier = Modifier,
    isInterMoughataaMode: Boolean = false
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: All Offers, 1: Target View, 2: Create Post Form, 3: My Posts
    var pendingActivationPostId by remember { mutableStateOf<String?>(null) }
    val posts by viewModel.exchangePosts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val context = LocalContext.current

    if (pendingActivationPostId != null) {
        val targetPost = posts.find { it.id == pendingActivationPostId }
        if (targetPost != null && !targetPost.isSynced) {
            LaunchedEffect(pendingActivationPostId) {
                viewModel.syncPendingExchangePosts()
                Toast.makeText(
                    context,
                    "لم يصل إعلانك إلى الخادم بعد. تأكد من اتصالك بالإنترنت وأعد المحاولة بعد لحظات.",
                    Toast.LENGTH_LONG
                ).show()
                pendingActivationPostId = null
            }
        } else {
            PostActivationDialog(
                postId = pendingActivationPostId!!,
                userId = currentUser?.uid ?: "",
                userEmail = currentUser?.email ?: "",
                onDismiss = { pendingActivationPostId = null }
            )
        }
    }

    // Cloud-Sync Mode: Trigger real-time listener and cloud server query for exchange posts
    LaunchedEffect(isInterMoughataaMode) {
        viewModel.listenToExchangePosts()
        viewModel.fetchExchangePostsFromCloud(resetLimit = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FE))
    ) {
        // Compact Header Banner with Segmented Tabs
        Surface(
            color = BentoPrimary,
            shadowElevation = 3.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Custom Segmented Tabs
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        val tabs = listOf(
                            AppLocalization.tr("offers_tab", "العروض 📋", appLanguage),
                            AppLocalization.tr("announcement_form_tab", "استمارة الإعلان ✍️", appLanguage),
                            AppLocalization.tr("my_posts_tab", "منشوراتي 👤", appLanguage)
                        )
                        tabs.forEachIndexed { index, label ->
                            val isSelected = selectedTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable { selectedTab = index }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) BentoPrimary else Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tab Body
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> AllOffersSubview(viewModel = viewModel, posts = posts, currentUserUid = currentUser?.uid ?: "", isInterMoughataaMode = isInterMoughataaMode, appLanguage = appLanguage)
                1 -> CreateExchangePostForm(
                    viewModel = viewModel,
                    isInterMoughataaMode = isInterMoughataaMode,
                    onPostSuccess = { isOffline, postId ->
                        selectedTab = 0
                        pendingActivationPostId = postId
                        if (isOffline) {
                            Toast.makeText(
                                context,
                                "هذا الاعلان لم يتم نشره بعد للعامة بسبب عدم توفر الانترنت سيتم نشره فور توفر الاتصال بالإنترنت",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
                2 -> {
                    val myUid = currentUser?.uid ?: ""
                    val myPosts = if (myUid.isNotBlank()) {
                        posts.filter { it.userId == myUid }
                    } else {
                        emptyList()
                    }
                    MyExchangePostsSubview(
                        viewModel = viewModel,
                        posts = myPosts,
                        isLoggedIn = myUid.isNotBlank(),
                        isInterMoughataaMode = isInterMoughataaMode,
                        appLanguage = appLanguage
                    )
                }
            }
        }
    }
}

// --- SUBVIEW 1: ALL EXCHANGE OFFERS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllOffersSubview(
    viewModel: TeacherViewModel,
    posts: List<TeacherExchangePost>,
    currentUserUid: String,
    isInterMoughataaMode: Boolean = false,
    appLanguage: String = "ar"
) {
    var hasClickedLoadMore by remember { mutableStateOf(false) }
    var previousPostsSize by remember { mutableStateOf(posts.size) }
    val hasMorePosts by viewModel.hasMoreExchangePosts.collectAsState()

    var selectedSpecialtyFilter by remember { mutableStateOf("الكل") }
    var selectedOfferTypeFilter by remember { mutableStateOf("الكل") }
    var selectedWilayaFilter by remember { mutableStateOf("جميع الولايات") }
    var selectedMoughataaFilter by remember { mutableStateOf("جميع المقاطعات") }
    var wilayaDirectionFilter by remember { mutableStateOf("ALL") } // "ALL", "LEAVING", "ENTERING"

    val filteredPosts = remember(posts, selectedSpecialtyFilter, selectedOfferTypeFilter, selectedWilayaFilter, selectedMoughataaFilter, wilayaDirectionFilter, isInterMoughataaMode, currentUserUid) {
        posts.filter { post ->
            // Only show approved posts in public offers
            if (!post.isApproved) return@filter false

            // 1. Separate Inter-Moughataa posts from Inter-Wilaya posts
            val isPostInterMoughataa = post.targetWilayas.trim().equals(post.currentWilaya.trim(), ignoreCase = true)
            val matchMode = if (isInterMoughataaMode) isPostInterMoughataa else !isPostInterMoughataa

            val matchSpecialty = selectedSpecialtyFilter == "الكل" || post.specialty == selectedSpecialtyFilter
            val matchType = selectedOfferTypeFilter == "الكل" || post.offerType == selectedOfferTypeFilter
            
            val matchWilayaAndMoughataa = if (isInterMoughataaMode) {
                // Inter-moughataa mode logic: Filter by Wilaya and optional Moughataa
                val matchW = if (selectedWilayaFilter == "جميع الولايات") {
                    true
                } else {
                    post.currentWilaya.equals(selectedWilayaFilter, ignoreCase = true)
                }
                val matchM = if (selectedMoughataaFilter == "جميع المقاطعات") {
                    true
                } else {
                    val isCurrentM = post.currentMoughataa.equals(selectedMoughataaFilter, ignoreCase = true)
                    val isTargetM = post.targetMoughataas.contains(selectedMoughataaFilter, ignoreCase = true)
                    when (wilayaDirectionFilter) {
                        "LEAVING" -> isCurrentM
                        "ENTERING" -> isTargetM
                        else -> isCurrentM || isTargetM
                    }
                }
                matchW && matchM
            } else {
                if (selectedWilayaFilter == "جميع الولايات") {
                    true
                } else {
                    val isCurrentMatch = post.currentWilaya.equals(selectedWilayaFilter, ignoreCase = true) || post.currentWilaya.contains(selectedWilayaFilter, ignoreCase = true)
                    val targetList = post.targetWilayas.split(",").map { it.trim() }
                    val isTargetMatch = targetList.any { it.equals(selectedWilayaFilter, ignoreCase = true) || it.contains(selectedWilayaFilter, ignoreCase = true) } || post.targetWilayas.contains(selectedWilayaFilter, ignoreCase = true)
                    when (wilayaDirectionFilter) {
                        "LEAVING" -> isCurrentMatch
                        "ENTERING" -> isTargetMatch
                        else -> isCurrentMatch || isTargetMatch
                    }
                }
            }

            matchMode && matchSpecialty && matchType && matchWilayaAndMoughataa
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Promotional Announcement Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BentoPrimary,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📢", fontSize = 18.sp)
                        }
                    }
                    Text(
                        text = "إعلانك للتبادل يصل جمهور المعلمين المعنيين بإعلان تبادلك بعيدا عن جمهور التواصل الاجتماعي الغير معنى .أنشأ إعلانك وجد فرصة التبادل التى ترضى الطرفين",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoText,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
        }

        // Filter Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(20.dp))
                        Text("تصفية وفلترة العروض", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BentoText)
                    }

                    // Specialty Filter Chips
                    Column {
                        Text("التخصص:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoPrimaryDesc)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedSpecialtyFilter == "الكل",
                                    onClick = { selectedSpecialtyFilter = "الكل" },
                                    label = { Text("الكل", fontSize = 11.sp) }
                                )
                            }
                            items(teacherSpecialties) { spec ->
                                FilterChip(
                                    selected = selectedSpecialtyFilter == spec,
                                    onClick = { selectedSpecialtyFilter = spec },
                                    label = { Text(spec, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Offer Type Filter Chips
                    Column {
                        Text("نوع العرض:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoPrimaryDesc)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedOfferTypeFilter == "الكل",
                                    onClick = { selectedOfferTypeFilter = "الكل" },
                                    label = { Text("الكل", fontSize = 11.sp) }
                                )
                            }
                            items(offerTypes) { type ->
                                FilterChip(
                                    selected = selectedOfferTypeFilter == type,
                                    onClick = { selectedOfferTypeFilter = type },
                                    label = { Text(type, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Wilaya Dropdown Filter
                    var expandedWilaya by remember { mutableStateOf(false) }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = expandedWilaya,
                            onExpandedChange = { expandedWilaya = !expandedWilaya }
                        ) {
                            OutlinedTextField(
                                value = selectedWilayaFilter,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الولاية الحالية أو المرغوبة", fontSize = 11.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWilaya) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedWilaya,
                                onDismissRequest = { expandedWilaya = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("جميع الولايات") },
                                    onClick = {
                                        selectedWilayaFilter = "جميع الولايات"
                                        expandedWilaya = false
                                    }
                                )
                                 mauritanianWilayas.forEach { w ->
                                    DropdownMenuItem(
                                        text = { Text(w) },
                                        onClick = {
                                            selectedWilayaFilter = w
                                            selectedMoughataaFilter = "جميع المقاطعات"
                                            expandedWilaya = false
                                        }
                                    )
                                }
                            }
                        }

                        if (isInterMoughataaMode && selectedWilayaFilter != "جميع الولايات") {
                            var expandedMoughataa by remember { mutableStateOf(false) }
                            val currentMoughataas = moughataasByWilaya[selectedWilayaFilter] ?: emptyList()
                            ExposedDropdownMenuBox(
                                expanded = expandedMoughataa,
                                onExpandedChange = { expandedMoughataa = !expandedMoughataa }
                            ) {
                                OutlinedTextField(
                                    value = selectedMoughataaFilter,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("المقاطعة المحددة (داخل $selectedWilayaFilter)", fontSize = 11.sp) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMoughataa) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedMoughataa,
                                    onDismissRequest = { expandedMoughataa = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("جميع المقاطعات") },
                                        onClick = {
                                            selectedMoughataaFilter = "جميع المقاطعات"
                                            expandedMoughataa = false
                                        }
                                    )
                                    currentMoughataas.forEach { m ->
                                        DropdownMenuItem(
                                            text = { Text(m) },
                                            onClick = {
                                                selectedMoughataaFilter = m
                                                expandedMoughataa = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedWilayaFilter != "جميع الولايات") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isLeaving = wilayaDirectionFilter == "LEAVING"
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { wilayaDirectionFilter = if (isLeaving) "ALL" else "LEAVING" }
                                        .border(
                                            width = if (isLeaving) 2.dp else 1.dp,
                                            color = if (isLeaving) Color(0xFFD32F2F) else BentoGrayOutline,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    color = if (isLeaving) Color(0xFFFFEBEE) else Color.White
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text("🚪 ", fontSize = 13.sp)
                                        Text(
                                            text = if (isInterMoughataaMode) "يريدون مغادرة المقاطعة" else "يريدون مغادرة الولاية",
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isLeaving) FontWeight.Black else FontWeight.Bold,
                                            color = if (isLeaving) Color(0xFFC62828) else BentoText,
                                            maxLines = 1
                                        )
                                    }
                                }

                                val isEntering = wilayaDirectionFilter == "ENTERING"
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { wilayaDirectionFilter = if (isEntering) "ALL" else "ENTERING" }
                                        .border(
                                            width = if (isEntering) 2.dp else 1.dp,
                                            color = if (isEntering) BentoPrimary else BentoGrayOutline,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    color = if (isEntering) BentoSoftPurpleContainer else Color.White
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text("🎯 ", fontSize = 13.sp)
                                        Text(
                                            text = if (isInterMoughataaMode) "يريدون دخول المقاطعة" else "يريدون دخول الولاية",
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isEntering) FontWeight.Black else FontWeight.Bold,
                                            color = if (isEntering) BentoPrimary else BentoText,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Count Header
        item {
            val countText = remember(selectedWilayaFilter, selectedMoughataaFilter, wilayaDirectionFilter, isInterMoughataaMode, filteredPosts.size) {
                if (isInterMoughataaMode) {
                    if (selectedMoughataaFilter != "جميع المقاطعات") {
                        when (wilayaDirectionFilter) {
                            "LEAVING" -> "عدد المعلمين الراغبين في التحويل عن مقاطعة $selectedMoughataaFilter: (${filteredPosts.size})"
                            "ENTERING" -> "عدد المعلمين الراغبين في التحويل إلى مقاطعة $selectedMoughataaFilter: (${filteredPosts.size})"
                            else -> "عدد المعلمين الراغبين في التبادل بمقاطعة $selectedMoughataaFilter: (${filteredPosts.size})"
                        }
                    } else if (selectedWilayaFilter != "جميع الولايات") {
                        when (wilayaDirectionFilter) {
                            "LEAVING" -> "عدد المعلمين الراغبين في التحويل عن ولاية $selectedWilayaFilter: (${filteredPosts.size})"
                            "ENTERING" -> "عدد المعلمين الراغبين في التحويل إلى ولاية $selectedWilayaFilter: (${filteredPosts.size})"
                            else -> "عدد المعلمين الراغبين في التبادل بـ $selectedWilayaFilter: (${filteredPosts.size})"
                        }
                    } else {
                        "عدد العروض المتاحة للتبادل داخل الولاية: (${filteredPosts.size})"
                    }
                } else {
                    if (selectedWilayaFilter != "جميع الولايات") {
                        when (wilayaDirectionFilter) {
                            "LEAVING" -> "عدد المعلمين الراغبين في التحويل عن $selectedWilayaFilter: (${filteredPosts.size})"
                            "ENTERING" -> "عدد المعلمين الراغبين في التحويل إلى $selectedWilayaFilter: (${filteredPosts.size})"
                            else -> "عدد المعلمين الراغبين في التبادل بـ $selectedWilayaFilter: (${filteredPosts.size})"
                        }
                    } else {
                        "عدد العروض المتاحة للتبادل بين الولايات: (${filteredPosts.size})"
                    }
                }
            }
            Text(
                text = countText,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = BentoPrimary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Disclaimer Note Banner
        item {
            Surface(
                color = Color(0xFFFFF8E1),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE082)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("ℹ️", fontSize = 13.sp)
                    Text(
                        text = AppLocalization.tr(
                            "disclaimer_exchange_responsibility",
                            "تطبيق دفتر المعلم غير مسؤول عن التفاهم بين الأشخاص فقط يوفر تواصل بين المعلمين",
                            appLanguage
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF795548),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Offers List
        if (filteredPosts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🔍", fontSize = 42.sp)
                        if (hasMorePosts) {
                            Text(
                                "لا توجد نتائج في المنشورات المعروضة — اضغط عرض المزيد للبحث في منشورات أقدم",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = BentoText,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    hasClickedLoadMore = true
                                    previousPostsSize = posts.size
                                    viewModel.loadMoreExchangePosts()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                            ) {
                                Text("عرض المزيد ⬇️", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            }
                        } else {
                            Text("لا توجد عروض تبادل تطابق نتائج البحث", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BentoText)
                            Text("حاول تغيير شروط الفلترة أو استخدم خيار استمارة الإعلان لإضافة عرضك الخاص.", fontSize = 12.sp, color = BentoPrimaryDesc, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        } else {
            items(filteredPosts, key = { it.id }) { post ->
                ExchangeOfferCard(
                    post = post,
                    isMyPost = post.userId == currentUserUid,
                    isInterMoughataaMode = isInterMoughataaMode,
                    appLanguage = appLanguage
                )
            }
            if (!hasMorePosts && posts.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "لا توجد إعلانات أخرى",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            }
            if (hasMorePosts) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                hasClickedLoadMore = true
                                previousPostsSize = posts.size
                                viewModel.loadMoreExchangePosts()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                        ) {
                            Text("عرض المزيد ⬇️", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// --- SUBVIEW 1.5: TARGET WILAYA / MOUGHATAA OFFERS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetWilayaOffersSubview(
    posts: List<TeacherExchangePost>,
    currentUserUid: String,
    isInterMoughataaMode: Boolean = false,
    appLanguage: String = "ar"
) {
    var selectedTargetWilaya by remember { mutableStateOf(mauritanianWilayas.firstOrNull() ?: "نواكشوط الغربية") }
    var selectedTargetMoughataa by remember(selectedTargetWilaya) {
        mutableStateOf(moughataasByWilaya[selectedTargetWilaya]?.firstOrNull() ?: "")
    }
    var selectedSpecialtyFilter by remember { mutableStateOf("الكل") }
    var expandedTargetWilayaDropdown by remember { mutableStateOf(false) }
    var expandedTargetMoughataaDropdown by remember { mutableStateOf(false) }
    var wilayaDirectionFilter by remember { mutableStateOf("ALL") } // "ALL", "LEAVING", "ENTERING"

    val filteredPosts = remember(posts, selectedTargetWilaya, selectedTargetMoughataa, selectedSpecialtyFilter, wilayaDirectionFilter, isInterMoughataaMode) {
        posts.filter { post ->
            if (!post.isApproved) return@filter false
            val matchSpecialty = selectedSpecialtyFilter == "الكل" || post.specialty == selectedSpecialtyFilter

            val matchLocation = if (isInterMoughataaMode) {
                val isCurrentMatch = post.currentWilaya.equals(selectedTargetWilaya, ignoreCase = true) &&
                        post.currentMoughataa.equals(selectedTargetMoughataa, ignoreCase = true)

                val isTargetMatch = (post.currentWilaya.equals(selectedTargetWilaya, ignoreCase = true) || post.targetWilayas.contains(selectedTargetWilaya, ignoreCase = true)) &&
                        post.targetMoughataas.contains(selectedTargetMoughataa, ignoreCase = true)

                when (wilayaDirectionFilter) {
                    "LEAVING" -> isCurrentMatch
                    "ENTERING" -> isTargetMatch
                    else -> isCurrentMatch || isTargetMatch
                }
            } else {
                val isCurrentMatch = post.currentWilaya.equals(selectedTargetWilaya, ignoreCase = true) ||
                        post.currentWilaya.contains(selectedTargetWilaya, ignoreCase = true)

                val targetList = post.targetWilayas.split(",").map { it.trim() }
                val isTargetMatch = targetList.any { it.equals(selectedTargetWilaya, ignoreCase = true) || it.contains(selectedTargetWilaya, ignoreCase = true) } ||
                        post.targetWilayas.contains(selectedTargetWilaya, ignoreCase = true)

                when (wilayaDirectionFilter) {
                    "LEAVING" -> isCurrentMatch
                    "ENTERING" -> isTargetMatch
                    else -> isCurrentMatch || isTargetMatch
                }
            }

            matchLocation && matchSpecialty
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Selection & Filtering Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(BentoSoftPurpleContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📍", fontSize = 18.sp)
                        }
                        Column {
                            Text(
                                text = if (isInterMoughataaMode) "تحديد المقاطعة المستهدفة للتحويل (داخل الولاية)" else "تحديد الولاية المرغوب التحويل إليها",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = BentoText
                            )
                            Text(
                                text = if (isInterMoughataaMode) "عرض المعلمين الراغبين في التحويل من وإلى هذه المقاطعة داخل نفس الولاية" else "عرض المعلمين الراغبين في الانتقال والتحويل إلى هذه الولاية",
                                fontSize = 11.sp,
                                color = BentoPrimaryDesc
                            )
                        }
                    }

                    HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.5f))

                    // 1. Select Target Wilaya Dropdown
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (isInterMoughataaMode) "اختر الولاية أولاً:" else "اختر الولاية المستهدفة للتحويل:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                        ExposedDropdownMenuBox(
                            expanded = expandedTargetWilayaDropdown,
                            onExpandedChange = { expandedTargetWilayaDropdown = !expandedTargetWilayaDropdown }
                        ) {
                            OutlinedTextField(
                                value = "📍 $selectedTargetWilaya",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الولاية المطلوبة") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTargetWilayaDropdown) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BentoPrimary,
                                    unfocusedBorderColor = BentoGrayOutline
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedTargetWilayaDropdown,
                                onDismissRequest = { expandedTargetWilayaDropdown = false }
                            ) {
                                mauritanianWilayas.forEach { wilaya ->
                                    DropdownMenuItem(
                                        text = { Text("📍 $wilaya", fontWeight = if (wilaya == selectedTargetWilaya) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            selectedTargetWilaya = wilaya
                                            selectedTargetMoughataa = moughataasByWilaya[wilaya]?.firstOrNull() ?: ""
                                            expandedTargetWilayaDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // If Inter-Moughataa Mode: Dropdown for selecting Target Moughataa inside Wilaya
                        if (isInterMoughataaMode) {
                            val availableMoughataas = moughataasByWilaya[selectedTargetWilaya] ?: emptyList()
                            Text("اختر المقاطعة المستهدفة داخل $selectedTargetWilaya:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                            ExposedDropdownMenuBox(
                                expanded = expandedTargetMoughataaDropdown,
                                onExpandedChange = { expandedTargetMoughataaDropdown = !expandedTargetMoughataaDropdown }
                            ) {
                                OutlinedTextField(
                                    value = "🏛️ $selectedTargetMoughataa",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("المقاطعة المستهدفة") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTargetMoughataaDropdown) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BentoPrimary,
                                        unfocusedBorderColor = BentoGrayOutline
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedTargetMoughataaDropdown,
                                    onDismissRequest = { expandedTargetMoughataaDropdown = false }
                                ) {
                                    availableMoughataas.forEach { moughataa ->
                                        DropdownMenuItem(
                                            text = { Text("🏛️ $moughataa", fontWeight = if (moughataa == selectedTargetMoughataa) FontWeight.Bold else FontWeight.Normal) },
                                            onClick = {
                                                selectedTargetMoughataa = moughataa
                                                expandedTargetMoughataaDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Filter Buttons: Leaving vs Entering Location
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isLeaving = wilayaDirectionFilter == "LEAVING"
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { wilayaDirectionFilter = if (isLeaving) "ALL" else "LEAVING" }
                                    .border(
                                        width = if (isLeaving) 2.dp else 1.dp,
                                        color = if (isLeaving) Color(0xFFD32F2F) else BentoGrayOutline,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                color = if (isLeaving) Color(0xFFFFEBEE) else Color.White
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("🚪 ", fontSize = 13.sp)
                                    Text(
                                        text = if (isInterMoughataaMode) "يريدون مغادرة المقاطعة" else "يريدون مغادرة الولاية",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isLeaving) FontWeight.Black else FontWeight.Bold,
                                        color = if (isLeaving) Color(0xFFC62828) else BentoText,
                                        maxLines = 1
                                    )
                                }
                            }

                            val isEntering = wilayaDirectionFilter == "ENTERING"
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { wilayaDirectionFilter = if (isEntering) "ALL" else "ENTERING" }
                                    .border(
                                        width = if (isEntering) 2.dp else 1.dp,
                                        color = if (isEntering) BentoPrimary else BentoGrayOutline,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                color = if (isEntering) BentoSoftPurpleContainer else Color.White
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("🎯 ", fontSize = 13.sp)
                                    Text(
                                        text = if (isInterMoughataaMode) "يريدون دخول المقاطعة" else "يريدون دخول الولاية",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isEntering) FontWeight.Black else FontWeight.Bold,
                                        color = if (isEntering) BentoPrimary else BentoText,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // 2. Select Specialty Filter Chips
                    Column {
                        Text("تصفية حسب تخصص المعلم:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = BentoPrimaryDesc)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedSpecialtyFilter == "الكل",
                                    onClick = { selectedSpecialtyFilter = "الكل" },
                                    label = { Text("جميع التخصصات", fontSize = 11.sp) }
                                )
                            }
                            items(teacherSpecialties) { spec ->
                                FilterChip(
                                    selected = selectedSpecialtyFilter == spec,
                                    onClick = { selectedSpecialtyFilter = spec },
                                    label = { Text(spec, fontSize = 11.sp) }
                                )
                            }
                        }
                    }


                }
            }
        }

        // Stats Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "عدد المعلمين الراغبين في التحويل إلى $selectedTargetWilaya: (${filteredPosts.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = BentoPrimary
                )
            }
        }

        // Offers List
        if (filteredPosts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🏜️", fontSize = 40.sp)
                        Text(
                            text = "لا يوجد معلمون يطلبون التحويل إلى $selectedTargetWilaya حالياً",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = BentoText,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "جرب اختيار ولاية أخرى أو تغيير تخصص المعلم للتأكد، أو قم بإضافة منشورك في استمارة الإعلان.",
                            fontSize = 12.sp,
                            color = BentoPrimaryDesc,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredPosts, key = { it.id }) { post ->
                ExchangeOfferCard(
                    post = post,
                    isMyPost = post.userId == currentUserUid,
                    isInterMoughataaMode = isInterMoughataaMode,
                    appLanguage = appLanguage
                )
            }
        }
    }
}

// --- CARD COMPONENT FOR EXCHANGE OFFER ---
@Composable
fun ExchangeOfferCard(
    post: TeacherExchangePost,
    isMyPost: Boolean = false,
    isInterMoughataaMode: Boolean = false,
    appLanguage: String = "ar",
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showContactDialog by remember { mutableStateOf(false) }

    if (showContactDialog) {
        ContactTeacherDialog(
            post = post,
            isInterMoughataaMode = isInterMoughataaMode,
            appLanguage = appLanguage,
            onDismiss = { showContactDialog = false },
            onConfirmWhatsApp = { specialty, wilaya, moughataa, commune, workPlace, schoolName ->
                showContactDialog = false
                val phone = post.whatsappPhone.filter { it.isDigit() }
                val fullPhone = if (phone.startsWith("222")) phone else "222$phone"
                val extraRoles = listOfNotNull(
                    if (post.isMoughataaTeacher) "معلم مقاطعي" else null,
                    if (post.isSchoolPrincipal) "مدير مدرسة" else null
                )
                val extraText = if (extraRoles.isNotEmpty()) "\nالصفة: ${extraRoles.joinToString(" - ")}" else ""

                val isIntraWilaya = isInterMoughataaMode || post.targetWilayas.contains("داخل الولاية") || (post.currentWilaya.isNotBlank() && post.targetWilayas.trim().equals(post.currentWilaya.trim(), ignoreCase = true))

                val messageText = buildString {
                    append("هذا المنشور من تطبيق دفتر المعلم يهمني 🤝\n\n")
                    append("تخصص المعلم: $specialty\n")
                    if (!isIntraWilaya) {
                        if (wilaya.isNotBlank()) append("الولاية: $wilaya\n")
                        if (moughataa.isNotBlank()) append("المقاطعة: $moughataa\n")
                    } else {
                        if (moughataa.isNotBlank()) append("المقاطعة الحالية: $moughataa\n")
                    }
                    if (commune.isNotBlank()) append("البلدية: $commune\n")
                    if (workPlace.isNotBlank()) append("مكان العمل: $workPlace\n")
                    if (schoolName.isNotBlank()) append("اسم المدرسة: $schoolName\n")
                    if (extraText.isNotBlank()) append(extraText)
                }

                val prefilledMessage = Uri.encode(messageText)
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$fullPhone?text=$prefilledMessage"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "تعذر فتح تطبيق الواتساب", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    val (badgeBg, badgeText, badgeIcon) = when (post.offerType) {
        "بيع مقعد مكان عمله" -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "🏷️")
        "بحث عن شراء مقعد" -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "💰")
        else -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "🤝")
    }

    val formattedDate = remember(post.timestamp) {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale("ar"))
            sdf.format(Date(post.timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BentoGrayOutline, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!post.isApproved) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("⏳", fontSize = 14.sp)
                                Text(
                                    text = "إعلان غير مفعل للعامة بعد",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFE65100)
                                )
                            }
                            Text(
                                text = "سعر نشر الإعلان 200 أوقية جديدة. تواصل مع الإدارة لتفعيل المنشور.",
                                fontSize = 11.sp,
                                color = Color(0xFF5D4037)
                            )
                        }
                        Button(
                            onClick = {
                                sendExchangeActivationWhatsApp(
                                    context = context,
                                    postId = post.id,
                                    userId = post.userId,
                                    userEmail = post.authorEmail
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("تفعيل 💬", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Header Row: Specialty & Role Badges, Offer Type Badge & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BentoSoftPurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👨‍🏫",
                            fontSize = 18.sp
                        )
                    }
                    Column {
                        Text(
                            text = "التخصص: ${post.specialty}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = BentoText
                        )
                        if (post.isMoughataaTeacher || post.isSchoolPrincipal) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                if (post.isMoughataaTeacher) {
                                    Surface(
                                        color = Color(0xFFFFF3E0),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
                                    ) {
                                        Text(
                                            text = "🏛️ معلم مقاطعي",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100),
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (post.isSchoolPrincipal) {
                                    Surface(
                                        color = Color(0xFFE0F2F1),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80CBC4))
                                    ) {
                                        Text(
                                            text = "🏫 مدير مدرسة",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00695C),
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Type Badge & Public Sync Status Indicator
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(badgeIcon, fontSize = 11.sp)
                            Text(post.offerType, color = badgeText, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    if (isMyPost) {
                        if (post.isSynced) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7))
                            ) {
                                Text(
                                    text = "🌐",
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Surface(
                                color = Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text("⏳", fontSize = 9.5.sp)
                                    Text("لم يُنشر للعامة بعد (بانتظار النت)", color = Color(0xFFE65100), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.6f))

            // Display Price / Negotiation for non-friendly offer types
            if (post.offerType != "تبادل مقاعد ودي دون دفع") {
                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(if (post.isNegotiable) "💬" else "💵", fontSize = 15.sp)
                        val priceText = when {
                            post.isNegotiable -> "المقابل المالي: للنقاش في الخاص"
                            post.priceAmount.isNotBlank() -> "المقابل المالي: ${post.priceAmount} أوقية"
                            else -> "المقابل المالي: للنقاش في الخاص"
                        }
                        Text(
                            text = priceText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            // Current Work Location Details
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(18.dp))
                    Text("مكان العمل الحالي:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BentoPrimary)
                }
                Text(
                    text = "الولاية: ${post.currentWilaya}  |  المقاطعة: ${post.currentMoughataa} ${if (post.currentCommune.isNotBlank()) " | البلدية: ${post.currentCommune}" else ""}",
                    fontSize = 12.sp,
                    color = BentoText,
                    fontWeight = FontWeight.Medium
                )
                if (post.currentWorkPlace.isNotBlank() || post.schoolName.isNotBlank()) {
                    Text(
                        text = "المنطقة/المدرسة: ${listOfNotNull(post.currentWorkPlace.ifBlank { null }, post.schoolName.ifBlank { null }).joinToString(" - ")}",
                        fontSize = 11.5.sp,
                        color = BentoPrimaryDesc
                    )
                }
            }

            // Desired Target Work Locations
            if (post.targetWilayas.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = BentoSecondary, modifier = Modifier.size(18.dp))
                        Text("الولايات والمقاطعات المرغوب العمل بها:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BentoSecondary)
                    }

                    val targetsList = post.targetWilayas.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(targetsList) { w ->
                            Surface(
                                color = BentoLightLavender,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "📍 $w",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (post.targetMoughataas.isNotBlank()) {
                        Text(
                            text = "تفاصيل المقاطعات: ${post.targetMoughataas}",
                            fontSize = 11.sp,
                            color = BentoPrimaryDesc
                        )
                    }
                }
            }

            HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.6f))

            // Action Row: WhatsApp Direct Chat Button & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (formattedDate.isNotBlank()) {
                    Text(
                        text = "⏱️ $formattedDate",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (isMyPost && onDeleteClick != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (onEditClick != null && !post.isApproved) {
                            OutlinedButton(
                                onClick = onEditClick,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تعديل", fontSize = 11.sp)
                            }
                        }
                        Button(
                            onClick = onDeleteClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف", color = Color.White, fontSize = 11.sp)
                        }
                    }
                } else if (post.whatsappPhone.isNotBlank()) {
                    Button(
                        onClick = { showContactDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("💬 تواصل عبر الواتساب", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- CONTACT TEACHER DIALOG FOR WHATSAPP DETAILS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactTeacherDialog(
    post: TeacherExchangePost,
    isInterMoughataaMode: Boolean = false,
    appLanguage: String = "ar",
    onDismiss: () -> Unit,
    onConfirmWhatsApp: (
        specialty: String,
        wilaya: String,
        moughataa: String,
        commune: String,
        workPlace: String,
        schoolName: String
    ) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("teacher_settings_prefs", Context.MODE_PRIVATE) }

    val isIntraWilaya = isInterMoughataaMode || post.targetWilayas.contains("داخل الولاية") || (post.currentWilaya.isNotBlank() && post.targetWilayas.trim().equals(post.currentWilaya.trim(), ignoreCase = true))

    var userSpecialty by remember {
        mutableStateOf(prefs.getString("user_contact_specialty", "") ?: "")
    }
    var userWilaya by remember {
        mutableStateOf(prefs.getString("user_contact_wilaya", "") ?: post.currentWilaya.ifBlank { mauritanianWilayas.firstOrNull() ?: "نواكشوط الغربية" })
    }
    var userMoughataa by remember {
        mutableStateOf(prefs.getString("user_contact_moughataa", "") ?: post.currentMoughataa.ifBlank { moughataasByWilaya[userWilaya]?.firstOrNull() ?: "" })
    }
    var userCommune by remember {
        mutableStateOf(prefs.getString("user_contact_commune", "") ?: post.currentCommune)
    }
    var userWorkPlace by remember {
        mutableStateOf(prefs.getString("user_contact_workplace", "") ?: post.currentWorkPlace)
    }
    var userSchoolName by remember {
        mutableStateOf(prefs.getString("user_contact_schoolname", "") ?: post.schoolName)
    }

    if (userSpecialty.isBlank()) {
        userSpecialty = teacherSpecialties.firstOrNull() ?: "معلم عربية"
    }

    val isWorkplaceInfoSaved = remember {
        val savedWork = prefs.getString("user_contact_workplace", "") ?: ""
        val savedSchool = prefs.getString("user_contact_schoolname", "") ?: ""
        val savedMough = prefs.getString("user_contact_moughataa", "") ?: ""
        savedWork.isNotBlank() && savedSchool.isNotBlank() && savedMough.isNotBlank()
    }
    var showEditForm by remember { mutableStateOf(!isWorkplaceInfoSaved) }

    var expandedSpecialtyDropdown by remember { mutableStateOf(false) }
    var expandedWilayaDropdown by remember { mutableStateOf(false) }
    var expandedMoughataaDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💬", fontSize = 18.sp)
                }
                Column {
                    Text(
                        text = "التواصل مع المعلم",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = BentoPrimary
                    )
                    Text(
                        text = "التخصص: ${post.specialty}",
                        fontSize = 11.5.sp,
                        color = BentoPrimaryDesc
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isWorkplaceInfoSaved && !showEditForm) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("✅", fontSize = 14.sp)
                                Text(
                                    text = "معلومات مكان عملك المسجلة حالياً:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                            Text(
                                text = "• التخصص: $userSpecialty\n• الموقع: ${if (!isIntraWilaya) "$userWilaya - " else ""}$userMoughataa (${userCommune.ifBlank { "غير محددة" }})\n• مكان العمل: $userWorkPlace\n• المدرسة: $userSchoolName",
                                fontSize = 11.5.sp,
                                color = BentoText,
                                lineHeight = 16.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { showEditForm = true }) {
                                    Text("تعديل البيانات ✏️", fontSize = 11.5.sp)
                                }
                                Button(
                                    onClick = {
                                        onConfirmWhatsApp(
                                            userSpecialty.trim(),
                                            userWilaya.trim(),
                                            userMoughataa.trim(),
                                            userCommune.trim(),
                                            userWorkPlace.trim(),
                                            userSchoolName.trim()
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("موافقة والتواصل 🚀", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFFF1F8E9),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFAED581)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isIntraWilaya)
                                "يرجى اكتمال معلومات مكان عملك الحالية (المقاطعة، البلدية، مكان العمل، واسم المدرسة) للتواصل عبر الواتساب."
                            else
                                "يرجى اكتمال معلومات مكان عملك الحالية (الولاية، المقاطعة، البلدية، مكان العمل، واسم المدرسة) للتواصل عبر الواتساب.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF33691E),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // 1. Specialty Dropdown
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("تخصص المعلم:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoText)
                    ExposedDropdownMenuBox(
                        expanded = expandedSpecialtyDropdown,
                        onExpandedChange = { expandedSpecialtyDropdown = !expandedSpecialtyDropdown }
                    ) {
                        OutlinedTextField(
                            value = userSpecialty,
                            onValueChange = { userSpecialty = it },
                            label = { Text("اختر أو اكتب تخصصك") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSpecialtyDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPrimary,
                                unfocusedBorderColor = BentoGrayOutline
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSpecialtyDropdown,
                            onDismissRequest = { expandedSpecialtyDropdown = false }
                        ) {
                            teacherSpecialties.forEach { spec ->
                                DropdownMenuItem(
                                    text = { Text(spec) },
                                    onClick = {
                                        userSpecialty = spec
                                        expandedSpecialtyDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Wilaya Dropdown (If Inter-Wilaya)
                if (!isIntraWilaya) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(AppLocalization.tr("wilaya_label", "الولاية:", appLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoText)
                        ExposedDropdownMenuBox(
                            expanded = expandedWilayaDropdown,
                            onExpandedChange = { expandedWilayaDropdown = !expandedWilayaDropdown }
                        ) {
                            OutlinedTextField(
                                value = userWilaya,
                                onValueChange = { userWilaya = it },
                                label = { Text("اختر الولاية الحالية") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWilayaDropdown) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BentoPrimary,
                                    unfocusedBorderColor = BentoGrayOutline
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedWilayaDropdown,
                                onDismissRequest = { expandedWilayaDropdown = false }
                            ) {
                                mauritanianWilayas.forEach { w ->
                                    DropdownMenuItem(
                                        text = { Text(w) },
                                        onClick = {
                                            userWilaya = w
                                            expandedWilayaDropdown = false
                                            val mList = moughataasByWilaya[w] ?: emptyList()
                                            if (!mList.contains(userMoughataa)) {
                                                userMoughataa = mList.firstOrNull() ?: ""
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Moughataa Dropdown
                val availableMoughataas = if (isIntraWilaya) {
                    moughataasByWilaya[post.currentWilaya] ?: mauritanianWilayas.flatMap { moughataasByWilaya[it] ?: emptyList() }
                } else {
                    moughataasByWilaya[userWilaya] ?: emptyList()
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isIntraWilaya) AppLocalization.tr("current_moughataa_label", "المقاطعة الحالية:", appLanguage) else AppLocalization.tr("moughataa_label", "المقاطعة:", appLanguage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoText
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedMoughataaDropdown,
                        onExpandedChange = { expandedMoughataaDropdown = !expandedMoughataaDropdown }
                    ) {
                        OutlinedTextField(
                            value = userMoughataa,
                            onValueChange = { userMoughataa = it },
                            label = { Text("اختر أو اكتب المقاطعة") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMoughataaDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPrimary,
                                unfocusedBorderColor = BentoGrayOutline
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMoughataaDropdown,
                            onDismissRequest = { expandedMoughataaDropdown = false }
                        ) {
                            availableMoughataas.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m) },
                                    onClick = {
                                        userMoughataa = m
                                        expandedMoughataaDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Commune Field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(AppLocalization.tr("commune_label", "البلدية:", appLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoText)
                    OutlinedTextField(
                        value = userCommune,
                        onValueChange = { userCommune = it },
                        placeholder = { Text("مثال: بلدية توجنين") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoGrayOutline
                        )
                    )
                }

                // Workplace Field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(AppLocalization.tr("workplace_label", "مكان العمل:", appLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoText)
                    OutlinedTextField(
                        value = userWorkPlace,
                        onValueChange = { userWorkPlace = it },
                        placeholder = { Text("مثال: تعليم ابتدائي / معلم") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoGrayOutline
                        )
                    )
                }

                // School Name Field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(AppLocalization.tr("school_name_label", "اسم المدرسة:", appLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoText)
                    OutlinedTextField(
                        value = userSchoolName,
                        onValueChange = { userSchoolName = it },
                        placeholder = { Text("مثال: مدرسة الطلحاية 1") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoGrayOutline
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (userSpecialty.isBlank() ||
                        (!isIntraWilaya && userWilaya.isBlank()) ||
                        userMoughataa.isBlank() ||
                        userCommune.isBlank() ||
                        userWorkPlace.isBlank() ||
                        userSchoolName.isBlank()
                    ) {
                        Toast.makeText(context, "يرجى تعبئة جميع بيانات التواصل الموضحة أولاً", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    prefs.edit()
                        .putString("user_contact_specialty", userSpecialty.trim())
                        .putString("user_contact_wilaya", userWilaya.trim())
                        .putString("user_contact_moughataa", userMoughataa.trim())
                        .putString("user_contact_commune", userCommune.trim())
                        .putString("user_contact_workplace", userWorkPlace.trim())
                        .putString("user_contact_schoolname", userSchoolName.trim())
                        .apply()

                    onConfirmWhatsApp(
                        userSpecialty.trim(),
                        userWilaya.trim(),
                        userMoughataa.trim(),
                        userCommune.trim(),
                        userWorkPlace.trim(),
                        userSchoolName.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حفظ ومتابعة للواتساب 💬", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", fontSize = 12.sp)
            }
        }
    )
}

// --- SUBVIEW 2: FORM FOR CREATING/EDITING EXCHANGE POST ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExchangePostForm(
    viewModel: TeacherViewModel,
    existingPost: TeacherExchangePost? = null,
    isInterMoughataaMode: Boolean = false,
    onPostSuccess: (isOffline: Boolean, postId: String) -> Unit
) {
    var offerType by remember { mutableStateOf(existingPost?.offerType ?: offerTypes[0]) }
    var priceAmount by remember { mutableStateOf(existingPost?.priceAmount ?: "") }
    var isNegotiable by remember { mutableStateOf(existingPost?.isNegotiable ?: false) }
    var specialty by remember { mutableStateOf(existingPost?.specialty ?: teacherSpecialties[0]) }
    var isMoughataaTeacher by remember { mutableStateOf(existingPost?.isMoughataaTeacher ?: false) }
    var isSchoolPrincipal by remember { mutableStateOf(existingPost?.isSchoolPrincipal ?: false) }
    
    var currentWilaya by remember { mutableStateOf(existingPost?.currentWilaya ?: mauritanianWilayas[0]) }
    var currentMoughataa by remember { mutableStateOf(existingPost?.currentMoughataa ?: (moughataasByWilaya[mauritanianWilayas[0]]?.firstOrNull() ?: "")) }
    var currentCommune by remember { mutableStateOf(existingPost?.currentCommune ?: "") }
    var currentWorkPlace by remember { mutableStateOf(existingPost?.currentWorkPlace ?: "") }
    var schoolName by remember { mutableStateOf(existingPost?.schoolName ?: "") }

    var whatsappPhone by remember { mutableStateOf(existingPost?.whatsappPhone ?: "") }

    // Inter-Moughataa multi-selection state
    var selectedTargetMoughataasInWilaya by remember {
        mutableStateOf(
            if (isInterMoughataaMode && !existingPost?.targetMoughataas.isNullOrBlank()) {
                existingPost!!.targetMoughataas.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
            } else {
                emptySet()
            }
        )
    }

    // Desired Wilayas multi-selection state
    var selectedTargetWilayas by remember {
        mutableStateOf(
            if (!existingPost?.targetWilayas.isNullOrBlank()) {
                existingPost!!.targetWilayas.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
            } else {
                emptySet()
            }
        )
    }

    // Map of target Wilaya to selected Moughataas or "الكل"
    var targetMoughataasMap by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var showTargetMoughataaDialogForWilaya by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    var isSubmitting by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (existingPost == null) "استمارة إعلان التبادل 📝" else "تعديل إعلان التبادل ✏️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = BentoPrimary
                    )

                    // 1. Offer Type Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "1️⃣ حدد نوع العرض والمشاركة:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BentoText
                        )
                        offerTypes.forEach { type ->
                            val isSelected = offerType == type
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { offerType = type }
                                    .border(1.dp, if (isSelected) BentoPrimary else BentoGrayOutline, RoundedCornerShape(12.dp)),
                                color = if (isSelected) BentoSoftPurpleContainer.copy(alpha = 0.5f) else Color.White
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    RadioButton(selected = isSelected, onClick = { offerType = type })
                                    Text(type, fontSize = 12.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = BentoText)
                                }
                            }
                        }

                        // If selected offer type is paid (Buy/Sell), show price / negotiation options
                        if (offerType != "تبادل مقاعد ودي دون دفع") {
                            Surface(
                                color = Color(0xFFFFF8E1),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "💵 تحديد المقابل المالي (بالأوقية الموريتانية MRU):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = Color(0xFFE65100)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { isNegotiable = false }
                                        ) {
                                            RadioButton(
                                                selected = !isNegotiable,
                                                onClick = { isNegotiable = false }
                                            )
                                            Text("تحديد المبلغ 💰", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoText)
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    isNegotiable = true
                                                    priceAmount = ""
                                                }
                                        ) {
                                            RadioButton(
                                                selected = isNegotiable,
                                                onClick = {
                                                    isNegotiable = true
                                                    priceAmount = ""
                                                }
                                            )
                                            Text("للنقاش في الخاص 💬", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoText)
                                        }
                                    }

                                    if (!isNegotiable) {
                                        OutlinedTextField(
                                            value = priceAmount,
                                            onValueChange = { priceAmount = it.filter { char -> char.isDigit() } },
                                            label = { Text("المبلغ بالأوقية") },
                                            placeholder = { Text("مثال: 50000") },
                                            suffix = { Text("أوقية", fontWeight = FontWeight.Bold, color = BentoPrimary) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BentoGrayOutline)

                    // 2. Specialty Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "2️⃣ تخصص المعلم / مقدم الخدمة:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BentoText
                        )
                        var expandedSpec by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandedSpec,
                            onExpandedChange = { expandedSpec = !expandedSpec }
                        ) {
                            OutlinedTextField(
                                value = specialty,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("اختر التخصص") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSpec) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedSpec,
                                onDismissRequest = { expandedSpec = false }
                            ) {
                                teacherSpecialties.forEach { spec ->
                                    DropdownMenuItem(
                                        text = { Text(spec) },
                                        onClick = {
                                            specialty = spec
                                            expandedSpec = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Checkboxes for Teacher Role & Position
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isMoughataaTeacher = !isMoughataaTeacher }
                                .border(1.dp, if (isMoughataaTeacher) BentoPrimary else BentoGrayOutline, RoundedCornerShape(12.dp)),
                            color = if (isMoughataaTeacher) BentoSoftPurpleContainer.copy(alpha = 0.5f) else Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = isMoughataaTeacher,
                                    onCheckedChange = { isMoughataaTeacher = it },
                                    colors = CheckboxDefaults.colors(checkedColor = BentoPrimary)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("معلم مقاطعي 🏛️", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = BentoText)
                                    Text("بعض المعلمين مقاطعيون ولا يتم التبادل إلا بينهم", fontSize = 10.5.sp, color = BentoPrimaryDesc)
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isSchoolPrincipal = !isSchoolPrincipal }
                                .border(1.dp, if (isSchoolPrincipal) Color(0xFF00897B) else BentoGrayOutline, RoundedCornerShape(12.dp)),
                            color = if (isSchoolPrincipal) Color(0xFFE0F2F1) else Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = isSchoolPrincipal,
                                    onCheckedChange = { isSchoolPrincipal = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00897B))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("مدير مدرسة 🏫", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = BentoText)
                                    Text("مدير مدرسة في الريف (ميزة تشجع غير المدراء للتبادل معك)", fontSize = 10.5.sp, color = Color(0xFF00695C))
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BentoGrayOutline)

                    // 3. Current Work Location
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "3️⃣ مكان العمل الحالي الكامل:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BentoText
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Current Wilaya
                            var expandedW by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedW,
                                    onExpandedChange = { expandedW = !expandedW }
                                ) {
                                    OutlinedTextField(
                                        value = currentWilaya,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("الولاية") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedW) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedW,
                                        onDismissRequest = { expandedW = false }
                                    ) {
                                        mauritanianWilayas.forEach { w ->
                                            DropdownMenuItem(
                                                text = { Text(w) },
                                                onClick = {
                                                    currentWilaya = w
                                                    currentMoughataa = moughataasByWilaya[w]?.firstOrNull() ?: ""
                                                    expandedW = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Current Moughataa
                            var expandedM by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedM,
                                    onExpandedChange = { expandedM = !expandedM }
                                ) {
                                    OutlinedTextField(
                                        value = currentMoughataa,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("المقاطعة") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedM) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedM,
                                        onDismissRequest = { expandedM = false }
                                    ) {
                                        (moughataasByWilaya[currentWilaya] ?: emptyList()).forEach { m ->
                                            DropdownMenuItem(
                                                text = { Text(m) },
                                                onClick = {
                                                    currentMoughataa = m
                                                    expandedM = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = currentCommune,
                            onValueChange = { currentCommune = it },
                            label = { Text("البلدية التابعة لها") },
                            placeholder = { Text("مثال: بلدية كيفه") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = currentWorkPlace,
                            onValueChange = { currentWorkPlace = it },
                            label = { Text("مكان العمل / القرية / المنطقة") },
                            placeholder = { Text("مثال: منطقة ميساح") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = schoolName,
                            onValueChange = { schoolName = it },
                            label = { Text("اسم المدرسة") },
                            placeholder = { Text("مثال: مدرسة أبتكيرات") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    HorizontalDivider(color = BentoGrayOutline)

                    // 4. Contact Info
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "4️⃣ معلومات التواصل مع المعلم:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BentoText
                        )
                        OutlinedTextField(
                            value = whatsappPhone,
                            onValueChange = { whatsappPhone = it },
                            label = { Text("رقم الواتساب 💬") },
                            placeholder = { Text("مثال: 46123456") },
                            leadingIcon = { Text("222+", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold, color = BentoPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    HorizontalDivider(color = BentoGrayOutline)

                    // 5. Target Desired Work Locations
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isInterMoughataaMode) "5️⃣ المقاطعات التي تود التحويل إليها داخل ولاية $currentWilaya:" else "5️⃣ الولايات والمقاطعات التي تود التحويل إليها:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BentoText
                        )
                        Text(
                            text = if (isInterMoughataaMode) "حدد المقاطعات التي تود الانتقال إليها داخل ولاية $currentWilaya (يمكنك اختيار مقاطعة واحدة أو أكثر):" else "انقر على الولاية لتحديدها أو إلغاء تحديدها. اضغط على خيار المقاطعات لتحديد الكل أو مقاطعة بعينها.",
                            fontSize = 11.sp,
                            color = BentoPrimaryDesc
                        )

                        if (isInterMoughataaMode) {
                            // Inter-moughataa mode UI: Checkboxes grid for Moughataas in current Wilaya (excluding current Moughataa)
                            val moughataaList = (moughataasByWilaya[currentWilaya] ?: emptyList()).filter { it != currentMoughataa }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                moughataaList.chunked(2).forEach { rowMoughataas ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowMoughataas.forEach { m ->
                                            val isSelected = selectedTargetMoughataasInWilaya.contains(m)
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        selectedTargetMoughataasInWilaya = if (isSelected) {
                                                            selectedTargetMoughataasInWilaya - m
                                                        } else {
                                                            selectedTargetMoughataasInWilaya + m
                                                        }
                                                    }
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) BentoPrimary else BentoGrayOutline,
                                                        shape = RoundedCornerShape(12.dp)
                                                    ),
                                                color = if (isSelected) BentoSoftPurpleContainer.copy(alpha = 0.5f) else Color.White
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Checkbox(
                                                        checked = isSelected,
                                                        onCheckedChange = { checked ->
                                                            selectedTargetMoughataasInWilaya = if (checked == true) {
                                                                selectedTargetMoughataasInWilaya + m
                                                            } else {
                                                                selectedTargetMoughataasInWilaya - m
                                                            }
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Text(m, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (selectedTargetMoughataasInWilaya.isNotEmpty()) {
                                Surface(
                                    color = BentoLightLavender,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("المقاطعات المختارة للتحويل داخل $currentWilaya:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                                        Text(selectedTargetMoughataasInWilaya.joinToString(" ، "), fontSize = 11.5.sp, color = BentoText)
                                    }
                                }
                            }
                        } else {
                            // Multi-select Wilayas Chips Grid (excluding current Wilaya)
                            val availableWilayas = mauritanianWilayas.filter { it != currentWilaya }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                availableWilayas.chunked(2).forEach { rowWilayas ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowWilayas.forEach { w ->
                                            val isSelected = selectedTargetWilayas.contains(w)
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        selectedTargetWilayas = if (isSelected) {
                                                            selectedTargetWilayas - w
                                                        } else {
                                                            selectedTargetWilayas + w
                                                        }
                                                    }
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) BentoPrimary else BentoGrayOutline,
                                                        shape = RoundedCornerShape(12.dp)
                                                    ),
                                                color = if (isSelected) BentoSoftPurpleContainer.copy(alpha = 0.5f) else Color.White
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    ) {
                                                        Checkbox(
                                                            checked = isSelected,
                                                            onCheckedChange = { checked ->
                                                                selectedTargetWilayas = if (checked == true) {
                                                                    selectedTargetWilayas + w
                                                                } else {
                                                                    selectedTargetWilayas - w
                                                                }
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                        Text(w, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                                    }

                                                    if (isSelected) {
                                                        TextButton(
                                                            onClick = { showTargetMoughataaDialogForWilaya = w },
                                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                                        ) {
                                                            Text("المقاطعات", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (selectedTargetWilayas.isNotEmpty()) {
                                Surface(
                                    color = BentoLightLavender,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("الولايات والمقاطعات المختارة للتحويل:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                                        val summaryText = selectedTargetWilayas.joinToString(", ") { wilaya ->
                                            val mList = targetMoughataasMap[wilaya]
                                            if (mList.isNullOrEmpty() || mList.contains("الكل")) {
                                                "$wilaya (الكل)"
                                            } else {
                                                "$wilaya (${mList.joinToString("، ")})"
                                            }
                                        }
                                        Text(summaryText, fontSize = 11.sp, color = BentoText)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (whatsappPhone.isBlank()) {
                                Toast.makeText(context, "الرجاء كتابة رقم الواتساب للتواصل!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val formattedTargets: String
                            val formattedMoughataas: String

                            if (isInterMoughataaMode) {
                                if (selectedTargetMoughataasInWilaya.isEmpty()) {
                                    Toast.makeText(context, "الرجاء اختيار مقاطعة واحدة على الأقل تود التحويل إليها داخل الولاية!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                formattedTargets = currentWilaya
                                formattedMoughataas = selectedTargetMoughataasInWilaya.joinToString(", ")
                            } else {
                                if (selectedTargetWilayas.isEmpty()) {
                                    Toast.makeText(context, "الرجاء اختيار ولاية واحدة على الأقل تود التحويل إليها!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                formattedTargets = selectedTargetWilayas.joinToString(", ")
                                formattedMoughataas = selectedTargetWilayas.joinToString("|") { w ->
                                    val list = targetMoughataasMap[w]
                                    if (list.isNullOrEmpty()) "$w:الكل" else "$w:${list.joinToString(",")}"
                                }
                            }

                            isSubmitting = true

                            val newPost = TeacherExchangePost(
                                id = existingPost?.id ?: "",
                                offerType = offerType,
                                priceAmount = if (offerType != "تبادل مقاعد ودي دون دفع" && !isNegotiable) priceAmount.trim() else "",
                                isNegotiable = if (offerType != "تبادل مقاعد ودي دون دفع") isNegotiable else false,
                                specialty = specialty,
                                currentWilaya = currentWilaya,
                                currentMoughataa = currentMoughataa,
                                currentCommune = currentCommune,
                                currentWorkPlace = currentWorkPlace,
                                schoolName = schoolName,
                                whatsappPhone = whatsappPhone,
                                targetWilayas = formattedTargets,
                                targetMoughataas = formattedMoughataas,
                                isMoughataaTeacher = isMoughataaTeacher,
                                isSchoolPrincipal = isSchoolPrincipal,
                                timestamp = System.currentTimeMillis()
                            )

                            if (existingPost == null) {
                                viewModel.createExchangePost(
                                    post = newPost,
                                    onSuccess = { isOffline ->
                                        isSubmitting = false
                                        onPostSuccess(isOffline, newPost.id)
                                    },
                                    onFailure = { msg ->
                                        isSubmitting = false
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                viewModel.updateExchangePost(
                                    post = newPost,
                                    onSuccess = { isOffline ->
                                        isSubmitting = false
                                        onPostSuccess(isOffline, newPost.id)
                                    },
                                    onFailure = { msg ->
                                        isSubmitting = false
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (existingPost == null) "نشر إعلان التبادل الآن 🚀" else "حفظ التعديلات 💾",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog for choosing specific Moughataas or "الكل" for a selected Wilaya
    showTargetMoughataaDialogForWilaya?.let { wilaya ->
        val mList = moughataasByWilaya[wilaya] ?: emptyList()
        var currentSelections by remember { mutableStateOf(targetMoughataasMap[wilaya] ?: listOf("الكل")) }

        AlertDialog(
            onDismissRequest = { showTargetMoughataaDialogForWilaya = null },
            title = { Text("اختر مقاطعات ولاية $wilaya 📍", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("يمكنك تحديد كافة المقاطعات (الكل) أو تخصيص مقاطعات معينة:", fontSize = 12.sp, color = BentoPrimaryDesc)
                    
                    // All Option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentSelections = listOf("الكل") }
                    ) {
                        RadioButton(selected = currentSelections.contains("الكل"), onClick = { currentSelections = listOf("الكل") })
                        Text("الكل - جميع المقاطعات التابعة لولاية $wilaya", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    HorizontalDivider(color = BentoGrayOutline)

                    // Specific Moughataas
                    mList.forEach { m ->
                        val isChecked = currentSelections.contains(m) && !currentSelections.contains("الكل")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val cleaned = currentSelections.filterNot { it == "الكل" }
                                    currentSelections = if (isChecked) {
                                        cleaned - m
                                    } else {
                                        cleaned + m
                                    }
                                    if (currentSelections.isEmpty()) currentSelections = listOf("الكل")
                                }
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    val cleaned = currentSelections.filterNot { it == "الكل" }
                                    currentSelections = if (checked == true) cleaned + m else cleaned - m
                                    if (currentSelections.isEmpty()) currentSelections = listOf("الكل")
                                }
                            )
                            Text(m, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    targetMoughataasMap = targetMoughataasMap + (wilaya to currentSelections)
                    showTargetMoughataaDialogForWilaya = null
                }) {
                    Text("حفظ الاختيار")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetMoughataaDialogForWilaya = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

// --- SUBVIEW 3: USER'S OWN POSTS ---
@Composable
fun MyExchangePostsSubview(
    viewModel: TeacherViewModel,
    posts: List<TeacherExchangePost>,
    isLoggedIn: Boolean = true,
    isInterMoughataaMode: Boolean = false,
    appLanguage: String = "ar"
) {
    val context = LocalContext.current
    var editingPost by remember { mutableStateOf<TeacherExchangePost?>(null) }
    var deletingPostId by remember { mutableStateOf<String?>(null) }

    if (editingPost != null) {
        CreateExchangePostForm(
            viewModel = viewModel,
            existingPost = editingPost,
            onPostSuccess = { isOffline, postId ->
                editingPost = null
                if (isOffline) {
                    Toast.makeText(
                        context,
                        "هذا الاعلان لم يتم نشره بعد للعامة بسبب عدم توفر الانترنت سيتم نشره فور توفر الاتصال بالإنترنت",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(context, "تم تعديل الإعلان بنجاح!", Toast.LENGTH_SHORT).show()
                }
            }
        )
        return
    }

    if (deletingPostId != null) {
        AlertDialog(
            onDismissRequest = { deletingPostId = null },
            title = { Text("حذف إعلان التبادل ⚠️", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف هذا الإعلان نهائياً من المنصة؟") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = deletingPostId!!
                        deletingPostId = null
                        viewModel.deleteExchangePost(
                            postId = id,
                            onSuccess = { Toast.makeText(context, "تم حذف الإعلان بنجاح", Toast.LENGTH_SHORT).show() },
                            onFailure = { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("نعم، احذف", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingPostId = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "الإعلانات التي قمت بنشرها على المنصة (${posts.size}):",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BentoText
            )
        }

        if (posts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(if (isLoggedIn) "📢" else "🔐", fontSize = 42.sp)
                        Text(
                            text = if (isLoggedIn) "لم تقم بنشر أي إعلان تبادل بعد" else "يرجى تسجيل الدخول",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isLoggedIn) "يمكنك الذهاب إلى تبويب \"استمارة الإعلان\" ونشر أول إعلان لك مجاناً ليصل لكافة المعلمين في موريتانيا."
                            else "يرجى تسجيل الدخول بحسابك لعرض وإدارة منشوراتك الخاصة (التعديل والحذف).",
                            fontSize = 12.sp,
                            color = BentoPrimaryDesc,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(posts, key = { it.id }) { post ->
                ExchangeOfferCard(
                    post = post,
                    isMyPost = true,
                    isInterMoughataaMode = isInterMoughataaMode,
                    appLanguage = appLanguage,
                    onEditClick = { editingPost = post },
                    onDeleteClick = { deletingPostId = post.id }
                )
            }
        }
    }
}
