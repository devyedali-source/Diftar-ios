package com.example.ui.screens

import com.example.compat.*
import kotlinx.coroutines.IO

import com.example.ui.utils.AppLocalization
import com.example.ui.TeacherInfo
import com.example.ui.components.OfficialStampSettingsSection
import com.example.ui.components.OfficialStampVisual
import com.example.ui.components.PaperStampSettingsIcon
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DrawerState
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.HtmlReportHelper
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.ClassSection
import com.example.data.models.Grade
import com.example.data.models.Student
import com.example.data.models.Subject
import com.example.data.models.sortedByOfficialOrder
import com.example.data.models.ClassSubjectCustomization
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Refresh
import com.example.ui.StudentPerformance
import com.example.ui.TeacherViewModel
import com.example.ui.TermProgress
import com.example.ui.AnnualProgress
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.ui.theme.*
import java.util.Locale

fun getLevelArabicName(level: Int, lang: String = "ar"): String {
    if (lang == "fr") {
        return when (level) {
            1 -> "1ère Année (AF1)"
            2 -> "2ème Année (AF2)"
            3 -> "3ème Année (AF3)"
            4 -> "4ème Année (AF4)"
            5 -> "5ème Année (AF5)"
            6 -> "6ème Année (AF6)"
            else -> "${level}ème Année"
        }
    }
    return when (level) {
        1 -> "السنة الأولى"
        2 -> "السنة الثانية"
        3 -> "السنة الثالثة"
        4 -> "السنة الرابعة"
        5 -> "السنة الخامسة"
        6 -> "السنة السادسة"
        else -> "السنة $level"
    }
}


@Composable
fun CustomTopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    appLanguage: String = "ar",
    onLanguageToggle: (() -> Unit)? = null
) {
    Surface(
        color = BentoPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Navigation Menu Drawer Button (☰)
            if (onMenuClick != null) {
                IconButton(onClick = onMenuClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Menu,
                        contentDescription = "القائمة الجانبية",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            // App Logo Image at the start of the AppBar
            Image(
                painter = painterResource(id = com.example.R.drawable.teacher_logo),
                contentDescription = "شعار التطبيق",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (navigationIcon != null) {
                navigationIcon()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                title()
            }
            if (actions != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    actions()
                }
            }
        }
    }
}

@Composable
fun MauritaniaSeal(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .background(Color(0xFF1B5E20), CircleShape)
            .border(2.dp, Color(0xFFD4AF37), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val sizePx = minOf(maxWidth, maxHeight)
        val starSize = sizePx * 0.35f
        val crescentSize = sizePx * 0.45f
        val crescentOffset = sizePx * 0.08f
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Star
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFD4AF37),
                modifier = Modifier.size(starSize)
            )
            
            Spacer(modifier = Modifier.height(sizePx * 0.02f))
            
            // Crescent
            Box(
                modifier = Modifier
                    .size(crescentSize)
                    .background(Color(0xFFD4AF37), CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = -crescentOffset)
                        .background(Color(0xFF1B5E20), CircleShape)
                )
            }
        }
    }
}


private fun isNetworkAvailable(context: android.content.Context): Boolean {
    return try {
        val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        if (cm != null) {
            val activeNetwork = cm.activeNetwork
            if (activeNetwork != null) {
                val capabilities = cm.getNetworkCapabilities(activeNetwork)
                capabilities != null && capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                false
            }
        } else {
            false
        }
    } catch (e: Exception) {
        true
    }
}


@Composable
fun TeacherAppScreen(
    viewModel: TeacherViewModel,
    deepLinkUri: android.net.Uri? = null,
    onDeepLinkConsumed: (() -> Unit)? = null
) {
    val isGlobalLoading by viewModel.isGlobalLoading.collectAsState()
    val loadingMessage by viewModel.loadingMessage.collectAsState()
    val isBackupRestoreLoading by viewModel.isBackupRestoreLoading.collectAsState()
    val driveProgressPercent by viewModel.driveProgressPercent.collectAsState()
    val driveProgressMessage by viewModel.driveProgressMessage.collectAsState()
    val showCancelLogoutButton by viewModel.showCancelLogoutButton.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    // Check Google Play for In-App Updates once per session after main screen loads
    androidx.compose.runtime.LaunchedEffect(Unit) {
        com.example.ui.update.InAppUpdateManager.checkForUpdatesOncePerSession(context)
    }

    // Force Right-to-Left (RTL) Layout Direction specifically for Arabic user audience
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
            ) {
                InnerTeacherAppScreenContent(
                    viewModel = viewModel,
                    deepLinkUri = deepLinkUri,
                    onDeepLinkConsumed = onDeepLinkConsumed
                )
            }

            if (isGlobalLoading) {
                // Beautiful and professional loading dialog overlay
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = {},
                    properties = androidx.compose.ui.window.DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    )
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .width(280.dp)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = loadingMessage,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            if (showCancelLogoutButton) {
                                OutlinedButton(
                                    onClick = { viewModel.cancelLogout() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "إلغاء الخروج",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isBackupRestoreLoading) {
                UnifiedSyncProgressDialog(
                    progress = driveProgressPercent,
                    progressMessage = driveProgressMessage.ifBlank { "جاري المعالجة..." },
                    isFinished = false,
                    syncSuccess = true,
                    errorMessage = null,
                    onDismiss = {},
                    dialogTitle = "مزامنة ونسخ احتياطي (Google Drive) ☁️"
                )
            }

            // In-App Update Prompt Dialog
            if (com.example.ui.update.InAppUpdateManager.showUpdatePromptDialog) {
                com.example.ui.update.InAppUpdateDialog(
                    onUpdateNow = {
                        if (activity != null) {
                            com.example.ui.update.InAppUpdateManager.startFlexibleUpdate(activity)
                        } else {
                            com.example.ui.update.InAppUpdateManager.openPlayStore(context)
                        }
                    },
                    onLater = {
                        com.example.ui.update.InAppUpdateManager.dismissUpdateDialog()
                    }
                )
            }

            // In-App Update Downloaded Notification Banner
            if (com.example.ui.update.InAppUpdateManager.isUpdateDownloadedState) {
                com.example.ui.update.UpdateDownloadedNotificationCard(
                    onInstallNow = {
                        com.example.ui.update.InAppUpdateManager.completeUpdate()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun UnifiedSyncProgressDialog(
    progress: Int,
    progressMessage: String,
    isFinished: Boolean,
    syncSuccess: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    dialogTitle: String = "تحديث ومزامنة البيانات 🔄☁️"
) {
    LaunchedEffect(isFinished, syncSuccess) {
        if (isFinished && syncSuccess) {
            kotlinx.coroutines.delay(1000)
            onDismiss()
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { if (isFinished) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = dialogTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1B5E20),
                    textAlign = TextAlign.Center
                )

                if (!isFinished) {
                    CircularProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.size(64.dp),
                        color = Color(0xFF1B5E20),
                        strokeWidth = 6.dp
                    )
                    Text(
                        text = "$progress%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1B5E20)
                    )
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = progressMessage,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = BentoText,
                        textAlign = TextAlign.Center
                    )
                } else {
                    if (syncSuccess) {
                        Text("🟢", fontSize = 44.sp)
                        Text(
                            text = "100% - تمت المزامنة بنجاح!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1B5E20),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text("❌", fontSize = 44.sp)
                        Text(
                            text = errorMessage ?: "حدث خطأ أثناء المزامنة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFC62828),
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إغلاق", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun InnerTeacherAppScreenContent(
    viewModel: TeacherViewModel,
    deepLinkUri: android.net.Uri? = null,
    onDeepLinkConsumed: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isSyncingCloudData by viewModel.isSyncingCloudData.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val syncUiState by viewModel.syncUiState.collectAsState()

    if (currentUser == null) {
        TeacherLoginSignupScreen(viewModel = viewModel)
    } else {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val appLanguage by viewModel.appLanguage.collectAsState()

            val rawClassSections by viewModel.classSections.collectAsState()
            val userEmail = currentUser?.email?.trim()?.lowercase() ?: ""
            val isUserAdmin = userEmail == "elyedalimoctar@gmail.com"
            val classSections = rawClassSections
            val subjects by viewModel.subjects.collectAsState()
            val students by viewModel.students.collectAsState()
            val grades by viewModel.grades.collectAsState()
            val customizations by viewModel.customizations.collectAsState()

            val context = LocalContext.current
            val selectedClassId by viewModel.selectedClassId.collectAsState()

            var showClassManagerInside by remember { mutableStateOf(false) }
            var targetActivationEmail by remember { mutableStateOf<String?>(null) }
            var targetActivationClassId by remember { mutableStateOf<Long?>(null) }
            var activeActivationRequestId by remember { mutableStateOf<String?>(null) }
            var activeExchangeApprovalRequestId by remember { mutableStateOf<String?>(null) }
            var activeCourseActivationRequestId by remember { mutableStateOf<String?>(null) }
            var currentOuterTab by remember {
                mutableStateOf(
                    com.example.data.state.LastPlaceStore.getInt(context, "outer_tab")
                        ?.takeIf { it in listOf(0, 2, 3, 4, 5) } ?: 0
                )
            } // 0: Portal, 2: Teacher Exchange, 3: About, 4: Legislation, 5: Course
            LaunchedEffect(currentOuterTab) {
                com.example.data.state.LastPlaceStore.putInt(context, "outer_tab", currentOuterTab)
            }

            var isNotificationsOpen by remember { mutableStateOf(false) }
            val localNotifs by viewModel.localNotifications.collectAsState()
            val notificationsList = remember(localNotifs) {
                localNotifs.map { n ->
                    mapOf<String, Any>(
                        "notificationId" to n.id.toString(),
                        "title" to n.title,
                        "message" to n.message,
                        "read" to n.isRead,
                        "createdAt" to n.createdAt
                    )
                }
            }
            val unreadCount = remember(notificationsList) { notificationsList.count { !(it["read"] as? Boolean ?: false) } }

            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted -> }

            val driveConsentLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
            ) { _ ->
                viewModel.performAutoDriveBackup(0L)
            }

            val pendingDriveConsent by viewModel.pendingDriveConsentIntent.collectAsState()

            LaunchedEffect(pendingDriveConsent) {
                val consentIntent = pendingDriveConsent
                if (consentIntent != null) {
                    viewModel.clearPendingDriveConsentIntent()
                    try {
                        driveConsentLauncher.launch(consentIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            LaunchedEffect(currentUser?.uid) {
                if (currentUser?.uid != null) {
                    kotlinx.coroutines.delay(2000)
                    viewModel.performAutoRestoreIfEmpty { lastSyncDate ->
                        android.widget.Toast.makeText(
                            context,
                            "تمت استعادة بياناتك من Google Drive ✅\nآخر مزامنة: $lastSyncDate",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            LaunchedEffect(currentUser?.uid) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            val logoutConflict by viewModel.logoutConflictMessage.collectAsState()
            if (logoutConflict != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearLogoutConflictMessage() },
                    title = {
                        Text(
                            text = "تعذّر حفظ نسختك ⚠️",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = logoutConflict ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "إن خرجت الآن فستُمحى بيانات هذا الجهاز، وسيضيع منها كل ما لم يُرفع.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.clearLogoutConflictMessage()
                                viewModel.signOutTeacher()
                            }
                        ) {
                            Text(
                                text = "خروج على كل حال",
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.clearLogoutConflictMessage() }
                        ) {
                            Text(text = "إلغاء", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            val appLifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            DisposableEffect(appLifecycleOwner) {
                val appObserver = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_START) {
                        viewModel.startNetworkWatcher()
                    }
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        viewModel.performAutoDriveBackup()
                        try {
                            viewModel.resendUnsyncedClasses()
                            viewModel.syncPendingExchangePosts()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                        viewModel.performAutoDriveBackup(300000L)
                        // على الآيفون: لا يوجد WorkManager؛ النسخ الاحتياطي يتمّ عند مغادرة التطبيق (السطر أعلاه)
                    }
                }
                appLifecycleOwner.lifecycle.addObserver(appObserver)
                onDispose {
                    viewModel.stopNetworkWatcher()
                    appLifecycleOwner.lifecycle.removeObserver(appObserver)
                }
            }

            LaunchedEffect(deepLinkUri, currentUser) {
                val uri = deepLinkUri ?: return@LaunchedEffect

                if ((uri.host == "diftar-almoaalim.web.app" || uri.host == "daftarmeallim.app") && uri.path?.startsWith("/course-activate") == true) {
                    val courseReqId = uri.lastPathSegment
                    if (!courseReqId.isNullOrBlank()) {
                        activeCourseActivationRequestId = courseReqId
                        onDeepLinkConsumed?.invoke()
                        return@LaunchedEffect
                    }
                }

                if (uri.host == "diftar-almoaalim.web.app" && uri.path?.startsWith("/activate") == true) {
                    val reqId = uri.lastPathSegment
                    if (!reqId.isNullOrBlank()) {
                        activeActivationRequestId = reqId
                        onDeepLinkConsumed?.invoke()
                        return@LaunchedEffect
                    }
                }

                if ((uri.host == "diftar-almoaalim.web.app" || uri.host == "daftarmeallim.app") && uri.path?.startsWith("/exchange-approve") == true) {
                    val reqId = uri.lastPathSegment
                    if (!reqId.isNullOrBlank()) {
                        activeExchangeApprovalRequestId = reqId
                        onDeepLinkConsumed?.invoke()
                        return@LaunchedEffect
                    }
                }

                val isActivationLink = (uri.host == "daftarmeallim.app" && uri.path?.startsWith("/activate") == true) ||
                        (uri.scheme == "daftarmeallim")

                if (isActivationLink) {
                    val email = uri.getQueryParameter("email")
                    val classId = uri.getQueryParameter("classId")?.toLongOrNull()

                    if (!email.isNullOrBlank() || classId != null) {
                        val loggedInEmail = currentUser?.email?.trim()?.lowercase() ?: ""
                        val isAdminUser = loggedInEmail == "elyedalimoctar@gmail.com"

                        if (currentUser != null && !isAdminUser) {
                            // User is NOT Admin -> Deny access!
                            android.widget.Toast.makeText(
                                context,
                                "⛔ عذراً! هذا الرابط مخصص لمدير الأقسام فقط (elyedalimoctar@gmail.com).\nحسابك الحالي ليس له صلاحية مدير الأقسام.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            onDeepLinkConsumed?.invoke()
                        } else if (isAdminUser) {
                            // User IS Admin -> Open Admin Section activation view directly!
                            targetActivationEmail = email
                            targetActivationClassId = classId
                            showClassManagerInside = true
                            val targetInfo = if (!email.isNullOrBlank()) "المعلم: $email" else "القسم رقم: $classId"
                            android.widget.Toast.makeText(
                                context,
                                "✨ تم التوجيه التلقائي لوجهة مدير الأقسام لتفعيل $targetInfo",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            onDeepLinkConsumed?.invoke()
                        }
                    }
                }
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = true,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Color.White,
                        drawerShape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp, topStart = 24.dp, bottomStart = 24.dp),
                        modifier = Modifier.width(310.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Header Box with Gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(BentoPrimary, BentoSecondary)
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = com.example.R.drawable.teacher_logo),
                                            contentDescription = "شعار التطبيق",
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, Color.White, CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Column {
                                            Text(
                                                text = "تطبيقي المدرسي 🏫",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = currentUser?.displayName?.ifBlank { "المعلم المحترم" } ?: "المعلم المحترم",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = Color.White.copy(alpha = 0.9f)
                                            )
                                        }
                                    }

                                    Surface(
                                        color = Color.White.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isUserAdmin) Icons.Default.AdminPanelSettings else Icons.Default.VerifiedUser,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (isUserAdmin) "مشرف النظام الرئيسي 👑" else (currentUser?.email ?: "معلم معتمد 🏅"),
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = AppLocalization.tr("menu", "القائمة الجانبية للتطبيق 📌", appLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = BentoPrimaryDesc,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // 1. Home / Classes (الصفحة الرئيسية والأقسام)
                            NavigationDrawerItem(
                                label = { Text(AppLocalization.tr("home_classes", "الصفحة الرئيسية والأقسام 🏫", appLanguage), fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                selected = selectedClassId == null && currentOuterTab == 0 && !showClassManagerInside,
                                onClick = {
                                    viewModel.selectClass(null)
                                    showClassManagerInside = false
                                    currentOuterTab = 0
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Default.Home, contentDescription = null, tint = BentoPrimary) },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )

                            // 3. Teacher Exchange (Between Wilayas) (تبادلات المعلمين بين الولايات)
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = AppLocalization.tr("teacher_exchange_wilayas", "تبادلات المعلمين (بين الولايات) 🤝", appLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                },
                                selected = selectedClassId == null && currentOuterTab == 2 && !showClassManagerInside,
                                onClick = {
                                    viewModel.selectClass(null)
                                    showClassManagerInside = false
                                    currentOuterTab = 2
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = BentoPrimary) },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )

                            // 3.6. Competition Course (دورة تحضير مسابقات المعلمين)
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = "دورة تحضير مسابقات المعلمين 🎓",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                },
                                selected = selectedClassId == null && currentOuterTab == 5 && !showClassManagerInside,
                                onClick = {
                                    viewModel.selectClass(null)
                                    showClassManagerInside = false
                                    currentOuterTab = 5
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Default.School, contentDescription = null, tint = BentoPrimary) },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )

                            // 3.5. School Legislation (التشريع المدرسي)
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = "التشريع المدرسي ⚖️",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                },
                                selected = selectedClassId == null && currentOuterTab == 4 && !showClassManagerInside,
                                onClick = {
                                    viewModel.selectClass(null)
                                    showClassManagerInside = false
                                    currentOuterTab = 4
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Default.Assignment, contentDescription = null, tint = BentoPrimary) },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )

                            // 4. About App (حول التطبيق)
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = AppLocalization.tr("about_app", "حول التطبيق ℹ️", appLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                },
                                selected = selectedClassId == null && currentOuterTab == 3 && !showClassManagerInside,
                                onClick = {
                                    viewModel.selectClass(null)
                                    showClassManagerInside = false
                                    currentOuterTab = 3
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Default.Info, contentDescription = null, tint = BentoPrimary) },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                                color = BentoGrayOutline.copy(alpha = 0.5f)
                            )

                            // Google Drive Direct Sync Info in Drawer
                            val lastDriveTime by viewModel.lastDriveBackupTime.collectAsState()
                            val formattedDriveTimeStr = if (lastDriveTime > 0) {
                                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US)
                                sdf.format(java.util.Date(lastDriveTime))
                            } else "لم تتم المزامنة بعد"

                            val connectedDevices = remember(lastDriveTime) {
                                try {
                                    val p = context.getSharedPreferences("teacher_settings_prefs", android.content.Context.MODE_PRIVATE)
                                    val raw = p.getString("drive_devices_list", null)
                                    if (raw.isNullOrBlank()) {
                                        emptyList()
                                    } else {
                                        val arr = org.json.JSONArray(raw)
                                        val out = mutableListOf<Triple<String, String, Long>>()
                                        for (i in 0 until arr.length()) {
                                            val o = arr.getJSONObject(i)
                                            val id = o.optString("deviceId", "")
                                            if (id.isBlank()) continue
                                            out.add(Triple(id, o.optString("deviceName", ""), o.optLong("lastSyncAt", 0L)))
                                        }
                                        out.toList()
                                    }
                                } catch (e: Exception) {
                                    emptyList()
                                }
                            }

                            val thisDeviceId = remember {
                                try {
                                    android.provider.Settings.Secure.getString(
                                        context.contentResolver,
                                        android.provider.Settings.Secure.ANDROID_ID
                                    ) ?: ""
                                } catch (e: Exception) {
                                    ""
                                }
                            }

                            Surface(
                                color = Color(0xFFF1F8E9),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("☁️", fontSize = 16.sp)
                                        Text(
                                            text = "مزامنة Google Drive المباشرة",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF1B5E20)
                                        )
                                    }
                                    Text(
                                        text = "آخر مزامنة: $formattedDriveTimeStr",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF33691E)
                                    )

                                    if (connectedDevices.isNotEmpty()) {
                                        HorizontalDivider(color = Color(0xFF2E7D32).copy(alpha = 0.2f))
                                        Text(
                                            text = "الأجهزة المتصلة بهذا الحساب:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF1B5E20)
                                        )
                                        connectedDevices.forEach { dev ->
                                            val isThisDevice = dev.first == thisDeviceId
                                            val shownName = if (dev.second.isBlank()) "جهاز غير معروف" else dev.second
                                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                Text(
                                                    text = if (isThisDevice) "📱 $shownName (هذا الجهاز)" else "📱 $shownName",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = if (isThisDevice) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                    color = Color(0xFF1B5E20)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (isUserAdmin) {
                                NavigationDrawerItem(
                                    label = { Text(AppLocalization.tr("class_manager_admin", "لوحة تسيير مدير الأقسام 👑", appLanguage), fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color(0xFF1B5E20)) },
                                    selected = showClassManagerInside,
                                    onClick = {
                                        showClassManagerInside = true
                                        scope.launch { drawerState.close() }
                                    },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF1B5E20)) },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = BentoGrayOutline.copy(alpha = 0.5f)
                            )

                            // Footer: Logout & Login
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (currentUser != null) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.signOutTeacherWithCheck(
                                                onBlocked = { msg ->
                                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                                },
                                                onSuccess = {
                                                    scope.launch { drawerState.close() }
                                                }
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                                        border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(AppLocalization.tr("logout", "تسجيل الخروج", appLanguage), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            viewModel.signOutTeacher()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20), contentColor = Color.White),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تسجيل الدخول 🔑", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

                                Text(
                                    text = AppLocalization.tr("app_footer_copyright", "دفتر المعلم - v2.5\nجميع الحقوق محفوظة © المطور: المختار اليدالي 2026", appLanguage),
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            ) {

            if (activeCourseActivationRequestId != null) {
                val isCourseLinkOwner = currentUser?.email?.trim()?.lowercase() == "elyedalimoctar@gmail.com"
                if (!isCourseLinkOwner) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🔒", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "هذه الصفحة مخصصة لمدير الأقسام فقط.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { activeCourseActivationRequestId = null }) {
                            Text("إغلاق والعودة")
                        }
                    }
                } else {
                    CourseActivationAdminView(
                        requestId = activeCourseActivationRequestId!!,
                        onClose = { activeCourseActivationRequestId = null }
                    )
                }
            } else
            if (activeActivationRequestId != null || activeExchangeApprovalRequestId != null) {
                val isLinkOwner = currentUser?.email?.trim()?.lowercase() == "elyedalimoctar@gmail.com"
                if (!isLinkOwner) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🔒", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "هذه الصفحة مخصصة لمدير الأقسام فقط.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            activeActivationRequestId = null
                            activeExchangeApprovalRequestId = null
                        }) {
                            Text("إغلاق والعودة")
                        }
                    }
                } else if (activeActivationRequestId != null) {
                    SingleRequestActivationView(
                        requestId = activeActivationRequestId!!,
                        viewModel = viewModel,
                        currentUser = currentUser,
                        onClose = { activeActivationRequestId = null }
                    )
                } else {
                    ExchangeRequestApprovalView(
                        requestId = activeExchangeApprovalRequestId!!,
                        viewModel = viewModel,
                        currentUser = currentUser,
                        onClose = { activeExchangeApprovalRequestId = null }
                    )
                }
            } else if (isNotificationsOpen) {
                NotificationsListScreen(
                    notifications = notificationsList,
                    onClose = { isNotificationsOpen = false },
                    onMarkAsRead = { notifId ->
                        notifId.toLongOrNull()?.let { id ->
                            viewModel.markNotificationRead(id)
                        }
                    },
                    onMarkAllAsRead = {
                        viewModel.markAllNotificationsRead()
                    },
                    onDeleteAll = {
                        viewModel.deleteAllNotifications()
                    }
                )
            } else if (showClassManagerInside) {
                val email = currentUser?.email ?: ""
                val isAdmin = email.trim().lowercase() == "elyedalimoctar@gmail.com"
                SubjectsTab(
                    viewModel = viewModel,
                    subjects = subjects,
                    bypassLogin = isAdmin,
                    targetEmail = targetActivationEmail,
                    targetClassId = targetActivationClassId,
                    onClose = {
                        showClassManagerInside = false
                        targetActivationEmail = null
                        targetActivationClassId = null
                    }
                )
            } else {

            val resolvedSubjects = remember(selectedClassId, subjects, customizations, classSections) {
                val activeClass = classSections.find { it.id == selectedClassId }
                val list = if (activeClass == null) {
                    subjects
                } else {
                    val targetLevel = activeClass.level
                    viewModel.buildClassSubjects(selectedClassId ?: 0L, targetLevel, subjects, customizations)
                }
                list.sortedByOfficialOrder()
            }

            // Outer selections state
            var selectedYear by remember { mutableStateOf<String?>(null) }
            var showCreateClassDialogOuter by remember { mutableStateOf(false) }

            if (selectedClassId == null) {
                // Outer Layout: Year & Section Selection stage
                val registeredYears = remember(classSections) {
                    val currentYear = viewModel.getAutomaticAcademicYear()
                    classSections.map { it.academicYear.ifBlank { "2025-2026" } }
                        .distinct()
                        .sortedWith { y1, y2 ->
                            when {
                                y1 == currentYear && y2 != currentYear -> -1
                                y1 != currentYear && y2 == currentYear -> 1
                                else -> y2.compareTo(y1)
                            }
                        }
                }

                // If year is selected but doesn't exist anymore (deleted class), clear selection
                LaunchedEffect(registeredYears) {
                    if (selectedYear != null && !registeredYears.contains(selectedYear)) {
                        selectedYear = null
                    }
                }

                Scaffold(
                    topBar = {
                        CustomTopAppBar(
                            appLanguage = appLanguage,
                            title = {
                                Text(
                                    text = when (currentOuterTab) {
                                        2 -> AppLocalization.tr("teacher_exchange_wilayas", "تبادل المعلمين (بين الولايات) 🤝", appLanguage)
                                        3 -> AppLocalization.tr("about_app", "حول التطبيق ℹ️", appLanguage)
                                        4 -> "التشريع المدرسي ⚖️"
                                        5 -> "دورة تحضير مسابقات المعلمين 🎓"
                                        else -> if (selectedYear == null) AppLocalization.tr("select_academic_year", "دفتر المعلم - اختيار السنة الدراسية 📅", appLanguage) else "${AppLocalization.tr("academic_year", "السنة الدراسية", appLanguage)}: $selectedYear 🏫"
                                    },
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                            },
                            onMenuClick = { scope.launch { drawerState.open() } },
                            navigationIcon = {
                                if (selectedYear != null && currentOuterTab == 0) {
                                    IconButton(onClick = { selectedYear = null }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppLocalization.tr("back", "رجوع", appLanguage), tint = Color.White)
                                    }
                                } else if (currentOuterTab != 0) {
                                    IconButton(onClick = { currentOuterTab = 0 }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppLocalization.tr("back", "رجوع", appLanguage), tint = Color.White)
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = { isNotificationsOpen = true }) {
                                    Box {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "الإشعارات",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        if (unreadCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = 4.dp, y = (-4).dp)
                                                    .background(Color.Red, CircleShape)
                                                    .size(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = unreadCount.toString(),
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    ) {
                        if (currentOuterTab == 2) {
                            TeacherExchangeScreen(viewModel = viewModel, isInterMoughataaMode = false)
                        } else if (currentOuterTab == 3) {
                            AboutAppScreen(appLanguage = appLanguage)
                        } else if (currentOuterTab == 4) {
                            SchoolLegislationScreen(context = context, appLanguage = appLanguage)
                        } else if (currentOuterTab == 5) {
                            CourseScreen()
                        } else {
                            if (selectedYear == null) {
                            // SHOW REGISTERED ACADEMIC YEARS
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    BentoDashboard(
                                        viewModel = viewModel,
                                        classSections = classSections,
                                        allStudents = students,
                                        showStats = false
                                    )
                                }

                                item {
                                }

                                if (currentUser?.email?.trim()?.lowercase() == "elyedalimoctar@gmail.com") {
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { showClassManagerInside = true }
                                                .border(2.dp, Color(0xFF1B5E20), RoundedCornerShape(24.dp)),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                            shape = RoundedCornerShape(24.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF1B5E20)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Settings,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "لوحة تسيير مدير الأقسام 🛠️",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1B5E20)
                                                    )
                                                    Text(
                                                        text = "مرحباً بك يا مدير الأقسام! اضغط هنا لإدارة الأقسام وتسيير المواد.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFF2E7D32)
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.ArrowForward,
                                                    contentDescription = null,
                                                    tint = Color(0xFF1B5E20)
                                                )
                                            }
                                        }
                                    }
                                }

                                item {
                                    Text(
                                        text = AppLocalization.tr("select_academic_year_prompt", "اختر السنة الدراسية للعمل عليها 📅", appLanguage),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoText,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                if (registeredYears.isEmpty()) {
                                    item {
                                        EmptyStateCard(
                                            title = AppLocalization.tr("no_academic_years_yet", "لا توجد سنوات دراسية مسجلة بعد", appLanguage),
                                            description = AppLocalization.tr("no_academic_years_desc", "اضغط على زر الإضافة (+) العائم أسفل الشاشة لإنشاء أول قسم دراسي والبدء فوراً.", appLanguage)
                                        )
                                    }
                                } else {
                                    items(registeredYears) { year ->
                                        val countInYear = classSections.count { (it.academicYear.ifBlank { "2025-2026" }) == year }
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedYear = year }
                                                .border(1.dp, BentoGrayOutline, RoundedCornerShape(20.dp)),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(20.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(18.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(44.dp)
                                                            .clip(CircleShape)
                                                            .background(BentoSecondaryContainer),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Book, contentDescription = null, tint = BentoSecondary)
                                                    }
                                                    Column {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = "${AppLocalization.tr("academic_year_prefix", "العام الدراسي:", appLanguage)} $year",
                                                                fontWeight = FontWeight.Bold,
                                                                color = BentoText
                                                            )
                                                            if (year == viewModel.getAutomaticAcademicYear()) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                                                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                    ) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .size(6.dp)
                                                                                .clip(CircleShape)
                                                                                .background(Color(0xFF4CAF50))
                                                                        )
                                                                        Text(
                                                                            text = AppLocalization.tr("ongoing_year_badge", "جارية", appLanguage),
                                                                            color = Color(0xFF2E7D32),
                                                                            fontSize = 10.sp,
                                                                            fontWeight = FontWeight.Bold
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        Text(
                                                            text = "$countInYear ${AppLocalization.tr("registered_classes_suffix", "أقسام دراسية مسجلة", appLanguage)}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = BentoPrimaryDesc
                                                        )
                                                    }
                                                }
                                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = BentoPrimaryDesc)
                                            }
                                        }
                                    }
                                }

                                item {
                                    AppFooter()
                                }
                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        } else {
                            // SHOW CLASSES FOR THE SELECTED YEAR
                            val yearClasses = classSections.filter { (it.academicYear.ifBlank { "2025-2026" }) == selectedYear }
                            val yearStudents = students.filter { student -> yearClasses.any { it.id == student.classId } }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    StudentAndClassesStatsCard(
                                        classSections = yearClasses,
                                        allStudents = yearStudents,
                                        appLanguage = appLanguage
                                    )
                                }

                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "الأقسام المسجلة لعام $selectedYear (${yearClasses.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoText
                                        )
                                    }
                                }

                                if (yearClasses.isEmpty()) {
                                    item {
                                        EmptyStateCard(
                                            title = "لا توجد أقسام مسجلة لهذا العام",
                                            description = "انقر على زر \"إنشاء قسم\" بالأسفل لإضافة فصل دراسي مسجل في هذا العام."
                                        )
                                    }
                                } else {
                                    items(yearClasses) { section ->
                                        val studentCount = students.count { it.classId == section.id }
                                        
                                        // Confirm Dialog of Class deletion state
                                        var showDeleteClassConfirmDialog by remember { mutableStateOf(false) }
                                        var confirmDeleteCode by remember { mutableStateOf("") }
                                        if (showDeleteClassConfirmDialog) {
                                            AlertDialog(
                                                onDismissRequest = { showDeleteClassConfirmDialog = false },
                                                title = { Text("تأكيد حذف القسم ⚠️", fontWeight = FontWeight.Bold) },
                                                text = {
                                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                        Text("هل أنت متأكد من حذف هذا القسم الدراسي (${section.name}) بالكامل؟ سيؤدي ذلك أيضاً إلى حذف جميع بيانات الطلاب المرتبطين ودرجاتهم من هذا الجهاز بشكل نهائي ولا يمكن التراجع عنها!")
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "ادخل الرقم 9999 لحذف القسم فى الخانة:",
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = Color(0xFFD32F2F)
                                                        )
                                                        OutlinedTextField(
                                                            value = confirmDeleteCode,
                                                            onValueChange = { confirmDeleteCode = it },
                                                            placeholder = { Text("أدخل 9999 هنا") },
                                                            singleLine = true,
                                                            modifier = Modifier.fillMaxWidth(),
                                                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                                        )
                                                    }
                                                },
                                                confirmButton = {
                                                    if (confirmDeleteCode == "9999") {
                                                        TextButton(
                                                            onClick = {
                                                                viewModel.deleteClassSection(section.id) { msg ->
                                                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                                                }
                                                                showDeleteClassConfirmDialog = false
                                                            }
                                                        ) {
                                                            Text("نعم، احذف القسم", color = Color.Red, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                },
                                                dismissButton = {
                                                    TextButton(onClick = { showDeleteClassConfirmDialog = false }) {
                                                        Text("إلغاء")
                                                    }
                                                }
                                            )
                                        }

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.selectClassWithLockCheck(section.id) }
                                                .border(1.dp, BentoGrayOutline, RoundedCornerShape(20.dp)),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(20.dp),
                                            elevation = CardDefaults.cardElevation(0.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(CircleShape)
                                                            .background(BentoSecondaryContainer),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "${if (section.level > 0) section.level else "?"}",
                                                            color = BentoSecondary,
                                                            fontWeight = FontWeight.Black,
                                                            style = MaterialTheme.typography.titleMedium
                                                        )
                                                    }
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = section.getFormattedName(),
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = BentoText
                                                        )

                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            if (section.isSyncedToServer) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                                                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(
                                                                        text = "✅ موثّق",
                                                                        color = Color(0xFF2E7D32),
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                            } else {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clickable {
                                                                            Toast.makeText(
                                                                                context,
                                                                                "قسمك محفوظ في جهازك، وسيتم توثيقه تلقائياً عند توفر الإنترنت. بعد التوثيق يمكنك تفعيل فصوله.",
                                                                                Toast.LENGTH_LONG
                                                                            ).show()
                                                                        }
                                                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(
                                                                        text = "⏳",
                                                                        fontSize = 11.sp
                                                                    )
                                                                }
                                                            }

                                                            // Payment / Activation status badge
                                                            val paidTerms = section.getPaidTerms()
                                                            val isAnyPaid = paidTerms.isNotEmpty()
                                                            val badgeBgColor = if (isAnyPaid) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
                                                            val badgeDotColor = if (isAnyPaid) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                                                            val badgeTextColor = if (isAnyPaid) Color(0xFF2E7D32) else Color(0xFF616161)
                                                            val badgeText = if (isAnyPaid) "مدفوع (${paidTerms.joinToString("، ")})" else "غير مدفوع"

                                                            Box(
                                                                modifier = Modifier
                                                                    .background(badgeBgColor, RoundedCornerShape(6.dp))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(6.dp)
                                                                            .clip(CircleShape)
                                                                            .background(badgeDotColor)
                                                                    )
                                                                    Text(
                                                                        text = badgeText,
                                                                        color = badgeTextColor,
                                                                        fontSize = 10.sp,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        Text(
                                                            text = "${getLevelArabicName(section.level)}  |  المدرسة: ${section.schoolName}  |  $studentCount تلميذ وتلميذة",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = BentoPrimaryDesc
                                                        )
                                                    }
                                                }
                                                IconButton(
                                                    onClick = {
                                                        confirmDeleteCode = ""
                                                        showDeleteClassConfirmDialog = true
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "حذف القسم",
                                                        tint = Color(0xFFD32F2F)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    AppFooter()
                                }
                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }

                        // Floating Action Button to create a class
                        if (currentOuterTab == 0) {
                            androidx.compose.material3.ExtendedFloatingActionButton(
                                onClick = { showCreateClassDialogOuter = true },
                                containerColor = BentoPrimary,
                                contentColor = Color.White,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(24.dp)
                                    .testTag("fab_create_class"),
                                icon = {
                                    Icon(Icons.Default.Add, contentDescription = "إنشاء قسم", modifier = Modifier.size(24.dp))
                                },
                                text = {
                                    Text("إنشاء قسم", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            )
                        }
                    }
                }

                // Global Create Class Dialog integration
                if (showCreateClassDialogOuter) {
                    CreateClassDialog(
                        initialAcademicYear = selectedYear ?: viewModel.getAutomaticAcademicYear(),
                        autoAcademicYear = viewModel.getAutomaticAcademicYear(),
                        existingClasses = classSections,
                        onDismiss = { showCreateClassDialogOuter = false },
                        onClassCreated = { schoolName, level, sectionName, wilaya, moughataa, academicYear ->
                            val isClassCreated = viewModel.addClassSection(
                                schoolName = schoolName,
                                level = level,
                                sectionName = sectionName,
                                wilaya = wilaya,
                                moughataa = moughataa,
                                academicYear = academicYear
                            )
                            if (isClassCreated) {
                                selectedYear = academicYear
                                Toast.makeText(context, "🎉 تم إنشاء القسم الدراسي بنجاح!", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            } else {
                // INNER LAYOUT: User has entered a specific class section!
                val activeClass = classSections.find { it.id == selectedClassId }

                if (activeClass == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                } else {
                    val classStudents = remember(students, activeClass.id) { students.filter { it.classId == activeClass.id }.sortedBy { it.id } }
                    var insideTab by remember(activeClass.id) {
                        mutableStateOf(
                            com.example.data.state.LastPlaceStore.getInt(context, "inside_tab_${activeClass.id}")
                                ?.takeIf { it in listOf(0, 1, 2, 4, 5) } ?: 0
                        )
                    } // 0: Students, 1: Grades, 2: Reports, 4: Annual Planning, 5: Timetable
                    LaunchedEffect(activeClass.id, insideTab) {
                        com.example.data.state.LastPlaceStore.putInt(context, "inside_tab_${activeClass.id}", insideTab)
                    }

                Scaffold(
                    topBar = {
                        CustomTopAppBar(
                            appLanguage = appLanguage,
                            onMenuClick = { scope.launch { drawerState.close(); drawerState.open() } },
                            title = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Text(
                                        text = activeClass.getFormattedName().replace(Regex("\\[.*?\\]"), "").trim(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${AppLocalization.tr("academic_year", "السنة الدراسية", appLanguage)}: ${activeClass.academicYear.ifBlank { "2025-2026" }} 🏫",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 1
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { isNotificationsOpen = true }) {
                                    Box {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "الإشعارات",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        if (unreadCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = 4.dp, y = (-4).dp)
                                                    .background(Color.Red, CircleShape)
                                                    .size(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = unreadCount.toString(),
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = BentoLightLavender,
                            modifier = Modifier.border(width = 1.dp, color = BentoGrayOutline.copy(alpha = 0.5f))
                        ) {
                            NavigationBarItem(
                                selected = insideTab == 0,
                                onClick = { insideTab = 0 },
                                icon = { Icon(Icons.Default.Person, contentDescription = "تسيير الطلاب") },
                                label = { Text(AppLocalization.tr("tab_students", "الطلاب", appLanguage), fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                            )
                            NavigationBarItem(
                                selected = insideTab == 1,
                                onClick = { insideTab = 1 },
                                icon = { Icon(Icons.Default.EditCalendar, contentDescription = "تسيير النتائج") },
                                label = { Text(AppLocalization.tr("tab_grades", "النتائج", appLanguage), fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                            )
                            NavigationBarItem(
                                selected = insideTab == 2,
                                onClick = { insideTab = 2 },
                                icon = { Icon(Icons.Default.Print, contentDescription = "سحب اللوائح والكشوف") },
                                label = { Text(AppLocalization.tr("tab_reports", "الكشوف", appLanguage), fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                            )
                            NavigationBarItem(
                                selected = insideTab == 4,
                                onClick = { insideTab = 4 },
                                icon = { Icon(Icons.Default.EditCalendar, contentDescription = "التخطيط السنوي") },
                                label = { Text(AppLocalization.tr("tab_annual_planning", "التخطيط السنوي", appLanguage), fontSize = 8.5.sp, fontWeight = FontWeight.Bold) }
                            )
                            NavigationBarItem(
                                selected = insideTab == 5,
                                onClick = { insideTab = 5 },
                                icon = { Icon(Icons.Default.ListAlt, contentDescription = "الجدول الزمني") },
                                label = { Text(AppLocalization.tr("tab_timetable", "الجدول الزمني", appLanguage), fontSize = 8.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    ) {
                        when (insideTab) {
                            0 -> {
                                ClassStudentsSubview(
                                    viewModel = viewModel,
                                    activeSection = activeClass,
                                    classStudents = classStudents
                                )
                            }
                            1 -> {
                                GradesEntryTab(
                                    viewModel = viewModel,
                                    classSections = classSections,
                                    subjects = resolvedSubjects,
                                    students = students,
                                    grades = grades,
                                    insideClassMode = true
                                )
                            }
                            2 -> {
                                ReportsTab(
                                    viewModel = viewModel,
                                    classSections = classSections,
                                    subjects = resolvedSubjects,
                                    students = students
                                )
                            }
                            4 -> {
                                AnnualPlanningSubview(activeClass = activeClass, viewModel = viewModel)
                            }
                            5 -> {
                                ClassTimetableSubview(activeClass = activeClass, viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
}
}

// AnnualPlanningSubview is now implemented in AnnualPlanningSubview.kt


@Composable
fun ClassStudentsSubview(
    viewModel: TeacherViewModel,
    activeSection: ClassSection,
    classStudents: List<Student>
) {
    var studentName by remember { mutableStateOf("") }
    var studentFrenchName by remember { mutableStateOf("") }
    var studentSchoolId by remember { mutableStateOf("") }
    var studentNationalId by remember { mutableStateOf("") }
    var studentHealthNotes by remember { mutableStateOf("") }
    var studentParentPhone1 by remember { mutableStateOf("") }
    var studentParentPhone2 by remember { mutableStateOf("") }
    var studentGender by remember { mutableStateOf("ذكر") }
    val context = LocalContext.current
    val appLanguage by viewModel.appLanguage.collectAsState()

    val isSchoolIdError = false
    val isNationalIdError = false

    // Dialog state for adding a student
    var showAddStudentDialog by remember { mutableStateOf(false) }

    // Dialog state for Editing a Student's information
    var editingStudent by remember { mutableStateOf<Student?>(null) }

    // Dialog state for Viewing a Student's details
    var viewingStudent by remember { mutableStateOf<Student?>(null) }

    // Dialog state for confirming student deletion
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    // Search query state for student filtering
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    // Filtered student list by name or school number (الرقم المدرسي / الرقم التسلسلي)
    val filteredStudents = remember(classStudents, searchQuery) {
        if (searchQuery.isBlank()) {
            classStudents
        } else {
            classStudents.filter { student ->
                student.name.contains(searchQuery, ignoreCase = true) ||
                student.schoolId.contains(searchQuery, ignoreCase = true) ||
                student.frenchName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
        }

        // Smaller Add Student Button and Search Toggle in a Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Smaller Add Student Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showAddStudentDialog = true }
                        .border(1.2.dp, BentoPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = BentoLightLavender.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تسجيل طالب جديد",
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimary,
                            fontSize = 12.5.sp
                        )
                    }
                }

                // Small Search Button next to it
                IconButton(
                    onClick = { isSearchVisible = !isSearchVisible },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isSearchVisible) BentoPrimary else BentoLightLavender.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isSearchVisible) BentoPrimary else BentoPrimary.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "البحث",
                        tint = if (isSearchVisible) Color.White else BentoPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Conditionally Show Search Input Field
        if (isSearchVisible) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_search_input"),
                    placeholder = { Text("ابحث عن تلميذ بالاسم أو الرقم المدرسي...", color = BentoPrimaryDesc) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = BentoPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "مسح",
                                    tint = BentoPrimaryDesc
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoGrayOutline,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        }

        // Student Count and Header
        item {
            val boysCount = classStudents.count { it.gender == "ذكر" }
            val girlsCount = classStudents.count { it.gender == "أنثى" }
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (searchQuery.isBlank()) {
                        "جميع تلاميذ وتلميذات القسم (${classStudents.size})"
                    } else {
                        "نتائج البحث (${filteredStudents.size} من ${classStudents.size})"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoText
                )
                Text(
                    text = "👦 عدد الأولاد: $boysCount | 👧 عدد البنات: $girlsCount",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = BentoPrimaryDesc
                )
            }
        }

        if (filteredStudents.isEmpty()) {
            item {
                val currentUserForDemo by viewModel.currentUser.collectAsState()
                val isAdminForDemo = currentUserForDemo?.email?.trim()?.lowercase() == "elyedalimoctar@gmail.com"
                if (isAdminForDemo && classStudents.isEmpty()) {
                    Button(
                        onClick = {
                            viewModel.selectClass(activeSection.id)
                            viewModel.fillActiveClassWithDemoStudents(
                                onSuccess = {
                                    Toast.makeText(context, "تم ملء القسم ببيانات تجريبية كاملة", Toast.LENGTH_LONG).show()
                                },
                                onFailure = { err ->
                                    Toast.makeText(context, "خطأ: $err", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                    ) {
                        Text("🧪 ملء القسم ببيانات تجريبية", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                EmptyStateCard(
                    title = if (searchQuery.isBlank()) "لا يوجد تلاميذ مسجلين بالقسم" else "لم يتم العثور على نتائج",
                    description = if (searchQuery.isBlank()) {
                        "قم بتسجيل الطلاب من خلال النقر على كرت «طالب جديد ➕» بالأعلى."
                    } else {
                        "جرب البحث بكلمات أخرى أو تحقق من كتابة الاسم أو الرقم المدرسي بشكل صحيح."
                    }
                )
            }
        } else {
            items(filteredStudents) { student ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewingStudent = student }
                        .border(1.dp, BentoGrayOutline.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
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
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (student.gender == "ذكر") Color(0xFFE0F2FE) else Color(0xFFFCE7F3)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (student.gender == "ذكر") "👦" else "👧",
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "${classStudents.indexOf(student) + 1} - ${student.name}",
                                fontWeight = FontWeight.Bold,
                                color = BentoText,
                                fontSize = 13.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = { viewingStudent = student },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoPrimary.copy(alpha = 0.08f),
                                contentColor = BentoPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "عرض",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("عرض", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        item {
            AppFooter()
        }
    }

    // Deletion Confirmation dialog
    if (studentToDelete != null) {
        val student = studentToDelete!!
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("تأكيد حذف التلميذ ⚠️", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف التلميذ (${student.name}) نهائياً من هذا القسم؟ سيتم حذف جميع درجاته بشكل كامل ولا يمكن استرجاع البيانات!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteStudent(student.id)
                        studentToDelete = null
                        if (viewingStudent?.id == student.id) {
                            viewingStudent = null
                        }
                    }
                ) {
                    Text("نعم، احذف الطالب", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Modal View Details dialog for student
    if (viewingStudent != null) {
        val currentViewingStudent = classStudents.find { it.id == viewingStudent!!.id }
        if (currentViewingStudent != null) {
            Dialog(onDismissRequest = { viewingStudent = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .heightIn(max = 560.dp)
                        .border(1.dp, BentoGrayOutline, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Top Header Row with Edit (تعديل) and Close (إغلاق) - Delete button is moved to the bottom for safety
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "بطاقة التلميذ 👤",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = BentoText
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Edit button inside header
                                OutlinedButton(
                                    onClick = {
                                        editingStudent = currentViewingStudent
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = BentoPrimary
                                    ),
                                    border = BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.5f)),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تعديل", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                IconButton(
                                    onClick = { viewingStudent = null },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = BentoPrimaryDesc)
                                }
                            }
                        }

                        HorizontalDivider(color = BentoGrayOutline)

                        // Student Name (Arabic & French)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BentoLightLavender.copy(alpha = 0.2f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (currentViewingStudent.gender == "ذكر") Color(0xFFE0F2FE) else Color(0xFFFCE7F3)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (currentViewingStudent.gender == "ذكر") "👦" else "👧",
                                    fontSize = 18.sp
                                )
                            }
                            Column {
                                Text(
                                    text = currentViewingStudent.name,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoText,
                                    fontSize = 15.sp
                                )
                                if (currentViewingStudent.frenchName.isNotBlank()) {
                                    Text(
                                        text = currentViewingStudent.frenchName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BentoPrimaryDesc
                                    )
                                }
                            }
                        }

                        // Detailed fields using clean cards/items
                        DetailItem(icon = Icons.Default.Person, label = "الجنس", value = currentViewingStudent.gender)
                        
                        DetailItem(icon = Icons.Default.Info, label = "الرقم المدرسي الموحد", value = currentViewingStudent.schoolId)
                        
                        DetailItem(icon = Icons.Default.Info, label = "الرقم الوطني للتعريف", value = currentViewingStudent.nationalId)

                        DetailItem(icon = Icons.Default.Phone, label = "هاتف الولي (الأساسي)", value = currentViewingStudent.parentPhone1)

                        DetailItem(icon = Icons.Default.Phone, label = "هاتف الولي (الاحتياطي)", value = currentViewingStudent.parentPhone2)

                        DetailItem(icon = Icons.Default.Warning, label = "الملاحظات الصحية / الطبية", value = currentViewingStudent.healthNotes)

                        Spacer(modifier = Modifier.height(4.dp))

                        // Action Buttons Row: Delete on the right side, Back to student list on the left (RTL)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Delete button positioned at the bottom, far from the Close 'X' in the header
                            OutlinedButton(
                                onClick = {
                                    studentToDelete = currentViewingStudent
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(0.35f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.Red
                                ),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حذف", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewingStudent = null },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(0.65f),
                                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                            ) {
                                Text("رجوع للائحة الطلاب ↩️", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Edit dialog for student
    if (editingStudent != null) {
        val student = editingStudent!!
        var editName by remember { mutableStateOf(student.name) }
        var editFrenchName by remember { mutableStateOf(student.frenchName) }
        var editSchoolId by remember { mutableStateOf(student.schoolId) }
        var editNationalId by remember { mutableStateOf(student.nationalId) }
        var editHealthNotes by remember { mutableStateOf(student.healthNotes) }
        var editParentPhone1 by remember { mutableStateOf(student.parentPhone1) }
        var editParentPhone2 by remember { mutableStateOf(student.parentPhone2) }
        var editGender by remember { mutableStateOf(student.gender) }

        val isEditSchoolIdError = false

        val isEditNationalIdError = false

        Dialog(onDismissRequest = { editingStudent = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .heightIn(max = 560.dp)
                    .border(1.dp, BentoGrayOutline, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Fixed Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.EditCalendar, contentDescription = null, tint = BentoPrimary)
                            Text("تعديل معلومات الطالب ✏️", fontWeight = FontWeight.Bold, color = BentoText)
                        }
                        IconButton(onClick = { editingStudent = null }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = BentoPrimaryDesc)
                        }
                    }

                    HorizontalDivider(color = BentoGrayOutline)

                    // Scrollable Fields Body (takes up remaining space)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("الاسم الكامل") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editFrenchName,
                            onValueChange = { editFrenchName = it },
                            label = { Text("الاسم بالفرنسية") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editSchoolId,
                                onValueChange = { input ->
                                    editSchoolId = input
                                },
                                label = { Text("الرقم المدرسي") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = editNationalId,
                                onValueChange = { input ->
                                    editNationalId = input
                                },
                                label = { Text("الرقم الوطني") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("الجنس:", fontWeight = FontWeight.Bold, color = BentoText, fontSize = 13.sp)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoLightLavender),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("ذكر", "أنثى").forEach { g ->
                                    val isSel = editGender == g
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSel) BentoPrimary else Color.Transparent)
                                            .clickable { editGender = g }
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = g,
                                            color = if (isSel) Color.White else BentoText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = editParentPhone1,
                            onValueChange = { editParentPhone1 = it },
                            label = { Text("هاتف ولي الأمر الأول") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editParentPhone2,
                            onValueChange = { editParentPhone2 = it },
                            label = { Text("هاتف ولي الأمر الثاني") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editHealthNotes,
                            onValueChange = { editHealthNotes = it },
                            label = { Text("ملاحظات صحية") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Sticky Save button (Always visible at the bottom)
                    Button(
                        onClick = {
                            if (editName.isNotBlank()) {
                                viewModel.updateStudent(
                                    id = student.id,
                                    name = editName,
                                    frenchName = editFrenchName,
                                    schoolId = editSchoolId,
                                    nationalId = editNationalId,
                                    healthNotes = editHealthNotes,
                                    parentPhone1 = editParentPhone1,
                                    parentPhone2 = editParentPhone2,
                                    gender = editGender
                                )
                                editingStudent = null
                                viewingStudent = null // Closes both the edit dialog and the student profile view, returning to student list
                                Toast.makeText(context, "تم حفظ التعديلات بنجاح! 🟢", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = editName.isNotBlank() && !isEditSchoolIdError && !isEditNationalIdError,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                    ) {
                        Text("حفظ التعديلات", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    if (showAddStudentDialog) {
        val onSaveStudent = {
            if (studentName.isNotBlank()) {
                viewModel.addStudent(
                    name = studentName,
                    classId = activeSection.id,
                    frenchName = studentFrenchName,
                    schoolId = studentSchoolId,
                    nationalId = studentNationalId,
                    healthNotes = studentHealthNotes,
                    parentPhone1 = studentParentPhone1,
                    parentPhone2 = studentParentPhone2,
                    gender = studentGender
                )
                // Clear fields
                studentName = ""
                studentFrenchName = ""
                studentSchoolId = ""
                studentNationalId = ""
                studentHealthNotes = ""
                studentParentPhone1 = ""
                studentParentPhone2 = ""
                studentGender = "ذكر"
                showAddStudentDialog = false
                Toast.makeText(context, "تم تسجيل الطالب بنجاح! 🎉", Toast.LENGTH_SHORT).show()
            }
        }

        Dialog(onDismissRequest = { showAddStudentDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .heightIn(max = 620.dp)
                    .border(1.dp, BentoGrayOutline, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Fixed Sticky Header (Always visible at the top)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { showAddStudentDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = BentoPrimaryDesc)
                        }
                        Text(
                            text = "طالب جديد ➕",
                            fontWeight = FontWeight.Bold,
                            color = BentoText,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(
                            onClick = onSaveStudent,
                            enabled = studentName.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoPrimary,
                                disabledContainerColor = BentoGrayOutline.copy(alpha = 0.5f)
                            )
                        ) {
                            Text("حفظ", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.5f))

                    // Scrollable form fields container
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = studentName,
                            onValueChange = { studentName = it },
                            label = { Text("الاسم الكامل للتلميذ (بالعربية)") },
                            modifier = Modifier.fillMaxWidth().testTag("add_student_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = studentFrenchName,
                            onValueChange = { studentFrenchName = it },
                            label = { Text("الاسم بالفرنسية (اختياري)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = studentSchoolId,
                                onValueChange = { input ->
                                    studentSchoolId = input
                                },
                                label = { Text("الرقم المدرسي") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = studentNationalId,
                                onValueChange = { input ->
                                    studentNationalId = input
                                },
                                label = { Text("الرقم الوطني") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        // Gender Button Option
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("الجنس:", fontWeight = FontWeight.Bold, color = BentoText, fontSize = 13.sp)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoLightLavender),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("ذكر", "أنثى").forEach { g ->
                                    val isSel = studentGender == g
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSel) BentoPrimary else Color.Transparent)
                                            .clickable { studentGender = g }
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = g,
                                            color = if (isSel) Color.White else BentoText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = studentParentPhone1,
                            onValueChange = { studentParentPhone1 = it },
                            label = { Text("هاتف ولي الأمر الأول") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                        )

                        OutlinedTextField(
                            value = studentParentPhone2,
                            onValueChange = { studentParentPhone2 = it },
                            label = { Text("هاتف ولي الأمر الثاني (اختياري)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                        )

                        OutlinedTextField(
                            value = studentHealthNotes,
                            onValueChange = { studentHealthNotes = it },
                            label = { Text("ملاحظات صحية أو صعوبات تعلم (اختياري)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.5f))

                    // Fixed Sticky Footer (Always visible at the bottom)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Button(
                            onClick = onSaveStudent,
                            enabled = studentName.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoPrimary,
                                disabledContainerColor = BentoGrayOutline.copy(alpha = 0.5f)
                            )
                        ) {
                            Text("حفظ وتسجيل الطالب", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BentoLightLavender.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 11.sp, color = BentoPrimaryDesc, fontWeight = FontWeight.Normal)
            Text(text = value.ifBlank { "غير مسجل" }, fontSize = 13.5.sp, color = BentoText, fontWeight = FontWeight.Bold)
        }
    }
}



@Composable
fun BentoDashboard(
    viewModel: TeacherViewModel,
    classSections: List<ClassSection>,
    allStudents: List<Student>,
    showStats: Boolean = false
) {
    var showProfileDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }

    val currentUser by viewModel.currentUser.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val context = LocalContext.current

    // --- Profile Info Dialog ---
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.BentoSoftPurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = com.example.ui.theme.BentoSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "معلوماتي الشخصية والحساب 👤",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = com.example.ui.theme.BentoText
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Profile Info Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Name
                            Column {
                                Text(
                                    text = "الاسم الكامل للمعلم 📝",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = com.example.ui.theme.BentoPrimaryDesc,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (currentUser?.displayName.isNullOrBlank()) "غير محدد" else currentUser?.displayName ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.BentoText
                                )
                            }
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                            // Email
                            Column {
                                Text(
                                    text = "البريد الإلكتروني 📧",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = com.example.ui.theme.BentoPrimaryDesc,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentUser?.email ?: "لا يوجد بريد إلكتروني",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = com.example.ui.theme.BentoText
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                            // UID
                            Column {
                                Text(
                                    text = "معرف المعلم الفريد (UID) 🔑",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = com.example.ui.theme.BentoPrimaryDesc,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentUser?.uid ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = com.example.ui.theme.BentoText,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("UID", currentUser?.uid ?: "")
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "تم نسخ معرف المعلم (UID)", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "نسخ",
                                            tint = com.example.ui.theme.BentoPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Sign Out Button underneath info
                    Button(
                        onClick = {
                            viewModel.signOutTeacherWithCheck(
                                onBlocked = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                },
                                onSuccess = {
                                    showProfileDialog = false
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل الخروج من الحساب", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Delete Account Button
                    OutlinedButton(
                        onClick = {
                            showDeleteAccountConfirmDialog = true
                        },
                        border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حذف الحساب نهائياً ⚠️", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("إغلاق", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- Delete Account Warning & Confirmation Dialog ---
    if (showDeleteAccountConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirmDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                    Text(
                        text = "تحذير أمني هام جداً! ⚠️",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD32F2F)
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "هل أنت متأكد تماماً من رغبتك في حذف حسابك الدراسي بالكامل؟",
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BentoText
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ما يترتب على حذف الحساب 🔴:",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "• سيتم حذف كافة الأقسام الدراسية المسجلة باسمك.",
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = "• سيتم مسح جميع الطلاب المسجلين ودرجاتهم وتفاصيلهم.",
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = "• سيتم حذف أقسامك وحالة تفعيلها وإعلاناتك من الخادم نهائياً.",
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = "• سيتم مسح الذاكرة المحلية والملفات من هذا الهاتف بالكامل.",
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = "• لن تتمكن من استرجاع أي بيانات أو تراجع عن هذا الإجراء أبداً.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                    
                    Text(
                        text = "عند التأكيد، سيتم مسح كافة ملفاتك وصورك وحسابك نهائياً من النظام.",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.ui.theme.BentoPrimaryDesc
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountConfirmDialog = false
                        showProfileDialog = false
                        viewModel.deleteTeacherAccount { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("نعم، احذف حسابي وبياناتي نهائياً", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirmDialog = false }) {
                    Text("إلغاء والتراجع", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showProfileDialog = true }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(com.example.ui.theme.BentoSoftPurpleContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = com.example.ui.theme.BentoSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    val currentUserState by viewModel.currentUser.collectAsState()
                    Text(
                        text = if (currentUserState?.displayName.isNullOrBlank()) "دفتر المعلم" else "دفتر المعلم - ${currentUserState?.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BentoText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display UID dynamically in a small pill, copy to clipboard when clicked
                val currentUserState by viewModel.currentUser.collectAsState()
                val contextVal = LocalContext.current
                currentUserState?.uid?.let { uid ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9))
                            .clickable {
                                val clipboard = contextVal.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("UID", uid)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(contextVal, "تم نسخ معرف المعلم (UID)", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${AppLocalization.tr("teacher_id_prefix", "معرف: ", appLanguage)}${uid.take(5)}...",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
                
                IconButton(
                    onClick = { showProfileDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(com.example.ui.theme.BentoLightLavender)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "إعدادات الحساب ومعلوماتي",
                        tint = com.example.ui.theme.BentoPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (showStats) {
            StudentAndClassesStatsCard(
                classSections = classSections,
                allStudents = allStudents,
                appLanguage = appLanguage
            )
        }
    }
}

@Composable
fun StudentAndClassesStatsCard(
    classSections: List<ClassSection>,
    allStudents: List<Student>,
    appLanguage: String
) {
    val maleCount = allStudents.count { it.gender == "ذكر" }
    val femaleCount = allStudents.count { it.gender == "أنثى" }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card A: Total registered
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, com.example.ui.theme.BentoGrayOutline, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BentoLightLavender),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.BentoPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = com.example.ui.theme.BentoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${allStudents.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = com.example.ui.theme.BentoPrimary
                    )
                }
                Column {
                    Text(
                        text = AppLocalization.tr("total_registered_students", "الطلاب المسجلين كلياً", appLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BentoPrimaryDesc
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👦 $maleCount ${AppLocalization.tr("male_count_label", "ذكور", appLanguage)}", style = MaterialTheme.typography.bodyMedium, color = com.example.ui.theme.BentoPrimary, fontWeight = FontWeight.SemiBold)
                        Text("•", style = MaterialTheme.typography.bodyMedium, color = com.example.ui.theme.BentoPrimaryDesc)
                        Text("👧 $femaleCount ${AppLocalization.tr("female_count_label", "إناث", appLanguage)}", style = MaterialTheme.typography.bodyMedium, color = com.example.ui.theme.BentoSecondary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Card B: Sections Distribution / Counts per Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, com.example.ui.theme.BentoGrayOutline, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BentoSoftPurpleContainer),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.BentoSecondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = com.example.ui.theme.BentoSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${classSections.size} ${AppLocalization.tr("sections_count_label", "أقسام", appLanguage)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BentoSecondary
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = AppLocalization.tr("students_per_section_title", "عدد الطلاب لكل قسم:", appLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BentoPrimaryDesc
                    )
                    if (classSections.isEmpty()) {
                        Text(
                            text = AppLocalization.tr("no_sections_yet", "لا توجد أقسام حالياً", appLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            color = com.example.ui.theme.BentoSecondary.copy(alpha = 0.7f)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            classSections.forEach { section ->
                                val count = allStudents.count { it.classId == section.id }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "• ${section.getFormattedName()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = com.example.ui.theme.BentoText,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$count ${if (count == 1) AppLocalization.tr("student_unit_singular", "طالب", appLanguage) else AppLocalization.tr("student_unit_plural", "طلاب", appLanguage)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = com.example.ui.theme.BentoSecondary
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

@Composable
fun ClassProgressIndicatorSection(
    progress: AnnualProgress,
    modifier: Modifier = Modifier,
    appLanguage: String = "ar"
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, com.example.ui.theme.BentoGrayOutline, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Title & Icon Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(com.example.ui.theme.BentoLightLavender),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = com.example.ui.theme.BentoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = AppLocalization.tr("annual_results_progress", "تقدم رصد نتائج الفصل السنوي", appLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BentoText
                    )
                    Text(
                        text = AppLocalization.tr("results_progress_subtitle", "متابعة نسبية تظهر اكتمال إدخال علامات المواد والطلاب", appLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.ui.theme.BentoPrimaryDesc
                    )
                }
            }

            // Annual Indicator Container (Highlighted in Bento Secondary Theme)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = com.example.ui.theme.BentoSecondaryContainer.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.BentoSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(progress.percentage * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = AppLocalization.tr("overall_annual_progress", "معدل التقدم السنوي العام", appLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.BentoSecondary
                            )
                            Text(
                                text = "${progress.totalEntered} / ${progress.totalRequired} " + AppLocalization.tr("grade_points_unit", "درجة", appLanguage),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = com.example.ui.theme.BentoPrimaryDesc
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = progress.percentage)
                                    .clip(CircleShape)
                                    .background(com.example.ui.theme.BentoSecondary)
                            )
                        }
                    }
                }
            }

            // Terms Progress row (Each term detailed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                progress.termProgresses.forEach { termProg ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BentoBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val termDisplayName = when (termProg.termId) {
                                1 -> AppLocalization.tr("term_1_short", "الفصل الأول", appLanguage)
                                2 -> AppLocalization.tr("term_2_short", "الفصل الثاني", appLanguage)
                                3 -> AppLocalization.tr("term_3_short", "الفصل الثالث", appLanguage)
                                else -> termProg.name
                            }
                            Text(
                                text = termDisplayName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.BentoText
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${(termProg.percentage * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = com.example.ui.theme.BentoPrimary
                                )
                                Text(
                                    text = "${termProg.enteredCount}/${termProg.totalRequired}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = com.example.ui.theme.BentoPrimaryDesc,
                                    fontSize = 9.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = termProg.percentage)
                                        .clip(CircleShape)
                                        .background(com.example.ui.theme.BentoPrimary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 0: CLASSES AND STUDENTS ---
// Official Administrative Order of Mauritanian Wilayas (الترتيب الإداري الرسمي لولايات الجمهورية الإسلامية الموريتانية 01-15)
val mauritanianWilayas = listOf(
    "الحوض الشرقي", // 01
    "الحوض الغربي",  // 02
    "لعصابه",        // 03
    "كوركول",        // 04
    "لبراكنه",       // 05
    "اترارزه",       // 06
    "آدرار",         // 07
    "داخلت نواذيبو",  // 08
    "تكانت",         // 09
    "كيدي ماغا",     // 10
    "تيرس زمور",     // 11
    "إنشيري",        // 12
    "نواكشوط الشمالية", // 13
    "نواكشوط الغربية",  // 14
    "نواكشوط الجنوبية"  // 15
)

val moughataasByWilaya = mapOf(
    "الحوض الشرقي" to listOf("النعمة", "تمبدغة", "آمرج", "باسكنو", "جكني", "ولاته", "عدل بكرو", "ظهر"),
    "الحوض الغربي" to listOf("لعيون", "الطينطان", "كوبني", "تامشكط", "اطويل"),
    "لعصابه" to listOf("كيفه", "كرو", "كنكوصة", "بومديد", "باركيول"),
    "كوركول" to listOf("كيهيدي", "مونغل", "مقامة", "إمبود", "لكصيبة"),
    "لبراكنه" to listOf("ألاك", "بوكى", "بابابى", "مبان", "مقطع لحجار"),
    "اترارزه" to listOf("روصو", "بوتلميت", "المذرذرة", "واد الناقة", "اركيز", "كرمسين", "انتيكان"),
    "آدرار" to listOf("أطار", "شنقيط", "وادان", "أوجفت"),
    "داخلت نواذيبو" to listOf("نواذيبو", "الشامي"),
    "تكانت" to listOf("تجكجة", "المجرية", "تيشيت"),
    "كيدي ماغا" to listOf("سيلبابي", "ولد ينجة", "غابو"),
    "تيرس زمور" to listOf("ازويرات", "فديرك", "بير أم اكرين"),
    "إنشيري" to listOf("أكجوجت", "بنشاب"),
    "نواكشوط الشمالية" to listOf("دار النعيم", "تيارت", "توجنين"),
    "نواكشوط الغربية" to listOf("تفرغ زينة", "لكصر", "السبخة"),
    "نواكشوط الجنوبية" to listOf("عرفات", "الميناء", "الرياض")
)

@Composable
fun CreateClassDialog(
    initialAcademicYear: String,
    autoAcademicYear: String,
    existingClasses: List<ClassSection>,
    onDismiss: () -> Unit,
    onClassCreated: (schoolName: String, level: Int, sectionName: String, wilaya: String, moughataa: String, academicYear: String) -> Unit
) {
    var schoolName by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf(1) }
    val defaultWilaya = remember { existingClasses.lastOrNull()?.wilaya?.takeIf { it.isNotBlank() } ?: mauritanianWilayas.first() }
    val defaultMoughataa = remember { existingClasses.lastOrNull()?.moughataa?.takeIf { it.isNotBlank() } ?: (moughataasByWilaya[defaultWilaya]?.firstOrNull() ?: "") }
    var selectedWilaya by remember { mutableStateOf(defaultWilaya) }
    var selectedMoughataa by remember { mutableStateOf(defaultMoughataa) }
    var academicYear by remember { mutableStateOf(initialAcademicYear.ifBlank { autoAcademicYear }) }

    var showDuplicateConfirmDialog by remember { mutableStateOf(false) }

    val existingSchools = remember(existingClasses) {
        existingClasses.map { it.schoolName.trim() }.filter { it.isNotBlank() }.distinct()
    }

    fun findNextSectionName(level: Int, baseName: String, school: String, wilaya: String, moughataa: String, year: String): String {
        val sameGroupClasses = existingClasses.filter {
            it.level == level &&
            it.wilaya.trim().equals(wilaya.trim(), ignoreCase = true) &&
            it.moughataa.trim().equals(moughataa.trim(), ignoreCase = true) &&
            it.schoolName.trim().equals(school.trim(), ignoreCase = true) &&
            it.academicYear.trim().equals(year.trim(), ignoreCase = true)
        }
        val existingNames = sameGroupClasses.map { it.sectionName.trim() }.toSet()

        if (!existingNames.contains(baseName)) {
            return baseName
        }

        var counter = 2
        while (existingNames.contains("$baseName $counter")) {
            counter++
        }
        return "$baseName $counter"
    }

    fun handleAttemptCreate() {
        val baseName = "${selectedLevel}AP"
        val hasDuplicateGroup = existingClasses.any {
            it.level == selectedLevel &&
            it.wilaya.trim().equals(selectedWilaya.trim(), ignoreCase = true) &&
            it.moughataa.trim().equals(selectedMoughataa.trim(), ignoreCase = true) &&
            it.schoolName.trim().equals(schoolName.trim(), ignoreCase = true) &&
            it.academicYear.trim().equals(academicYear.trim(), ignoreCase = true)
        }

        if (hasDuplicateGroup) {
            showDuplicateConfirmDialog = true
        } else {
            val finalSectionName = findNextSectionName(
                level = selectedLevel,
                baseName = baseName,
                school = schoolName,
                wilaya = selectedWilaya,
                moughataa = selectedMoughataa,
                year = academicYear
            )
            onClassCreated(schoolName.trim(), selectedLevel, finalSectionName, selectedWilaya, selectedMoughataa, academicYear)
            onDismiss()
        }
    }

    if (showDuplicateConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateConfirmDialog = false },
            title = {
                Text(
                    text = "تنبيه: قسم مكرر ⚠️",
                    fontWeight = FontWeight.Bold,
                    color = BentoText
                )
            },
            text = {
                Text(
                    text = "يوجد لديك قسم بهذا المستوى !! هل تريد اضافة قسم آخر من هذا المستوى ؟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val baseName = "${selectedLevel}AP"
                        val finalSectionName = findNextSectionName(
                            level = selectedLevel,
                            baseName = baseName,
                            school = schoolName,
                            wilaya = selectedWilaya,
                            moughataa = selectedMoughataa,
                            year = academicYear
                        )
                        showDuplicateConfirmDialog = false
                        onClassCreated(schoolName.trim(), selectedLevel, finalSectionName, selectedWilaya, selectedMoughataa, academicYear)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) {
                    Text("نعم", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDuplicateConfirmDialog = false }
                ) {
                    Text("إلغاء", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 620.dp)
                .border(1.dp, BentoGrayOutline, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header of Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BentoSecondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = BentoSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "إنشاء قسم دراسي جديد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = BentoPrimaryDesc
                        )
                    }
                }

                HorizontalDivider(color = BentoGrayOutline)

                // 1. Wilaya & Moughataa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        MauritaniaDropdown(
                            label = "الولاية",
                            selectedValue = selectedWilaya,
                            options = mauritanianWilayas,
                            onValueChanged = { wilaya ->
                                selectedWilaya = wilaya
                                selectedMoughataa = moughataasByWilaya[wilaya]?.firstOrNull() ?: ""
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MauritaniaDropdown(
                            label = "المقاطعة التابعة لها",
                            selectedValue = selectedMoughataa,
                            options = moughataasByWilaya[selectedWilaya] ?: emptyList(),
                            onValueChanged = { moughataa ->
                                selectedMoughataa = moughataa
                            }
                        )
                    }
                }

                // 2. School Name input + Suggestions
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("إسم المدرسة") },
                        placeholder = { Text("مثال: مدرسة الجمهورية") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_school_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (existingSchools.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "💡 المدارس المسجلة لديك (اضغط للاختيار السريع):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimaryDesc
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(vertical = 2.dp)
                            ) {
                                items(existingSchools) { school ->
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { schoolName = school },
                                        color = BentoLightLavender,
                                        border = BorderStroke(1.dp, BentoGrayOutline)
                                    ) {
                                        Text(
                                            text = school,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BentoPrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Level selection row (1 to 6)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "المستوى الدراسي من 1 إلى 6",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimaryDesc,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..6).forEach { lvl ->
                            val isSelected = selectedLevel == lvl
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) BentoPrimary else BentoLightLavender)
                                    .border(1.dp, if (isSelected) BentoPrimary else BentoGrayOutline, CircleShape)
                                    .clickable { selectedLevel = lvl },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$lvl",
                                    color = if (isSelected) Color.White else BentoText,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // 4. Academic Year input
                AcademicYearSelectorDropdown(
                    label = "العام الدراسي",
                    selectedValue = academicYear,
                    onValueChanged = { academicYear = it },
                    autoYear = autoAcademicYear,
                    testTag = "dialog_academic_year_input"
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 5. Submit Button
                Button(
                    onClick = {
                        if (schoolName.isNotBlank()) {
                            handleAttemptCreate()
                        }
                    },
                    enabled = schoolName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dialog_add_class_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إنشاء القسم", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إنشاء القسم الدراسي الجديد", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MauritaniaDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = BentoPrimaryDesc,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BentoLightLavender)
                .border(1.dp, BentoGrayOutline, RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedValue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BentoText
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = BentoPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth(0.85f)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = option, 
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = BentoText
                            ) 
                        },
                        onClick = {
                            onValueChanged(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

fun normalizeAcademicYear(rawYear: String?): String {
    if (rawYear.isNullOrBlank()) return "2025-2026"
    val clean = rawYear.replace(" ", "").trim()
    val regex = Regex("""(\d{4})[-_–/](\d{4})""")
    val match = regex.find(clean)
    return if (match != null) {
        "${match.groupValues[1]}-${match.groupValues[2]}"
    } else {
        if (clean.length == 4 && clean.all { it.isDigit() }) {
            val y = clean.toInt()
            "$y-${y + 1}"
        } else {
            clean.ifBlank { "2025-2026" }
        }
    }
}

@Composable
fun AcademicYearSelectorDropdown(
    label: String = "العام الدراسي",
    selectedValue: String,
    onValueChanged: (String) -> Unit,
    autoYear: String = "2025-2026",
    testTag: String = "academic_year_selector"
) {
    var expanded by remember { mutableStateOf(false) }

    val normalizedSelected = normalizeAcademicYear(selectedValue)

    // Calculate base start year from autoYear e.g. "2025-2026"
    val baseStartYear = remember(autoYear) {
        val cleanAuto = normalizeAcademicYear(autoYear)
        val parts = cleanAuto.split("-").mapNotNull { it.toIntOrNull() }
        parts.firstOrNull() ?: 2025
    }

    val currentAcademicYear = "$baseStartYear-${baseStartYear + 1}"
    val nextAcademicYear = "${baseStartYear + 1}-${baseStartYear + 2}"
    val prevAcademicYear = "${baseStartYear - 1}-$baseStartYear"

    val options = listOf(
        currentAcademicYear to "العام الدراسي الحالي ($currentAcademicYear)",
        nextAcademicYear to "العام الدراسي المقبل ($nextAcademicYear)",
        prevAcademicYear to "العام الدراسي السابق ($prevAcademicYear)"
    )

    val currentDisplayLabel = options.find { it.first == normalizedSelected }?.second
        ?: "العام الدراسي ($normalizedSelected)"

    Column(modifier = Modifier.fillMaxWidth().testTag(testTag)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = BentoPrimaryDesc,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BentoLightLavender)
                .border(1.dp, BentoGrayOutline, RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📅", fontSize = 16.sp)
                    Text(
                        text = currentDisplayLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoText
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = BentoPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth(0.85f)
            ) {
                options.forEach { (yearVal, displayTitle) ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = displayTitle,
                                    fontWeight = if (yearVal == normalizedSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (yearVal == normalizedSelected) BentoPrimary else BentoText,
                                    fontSize = 13.sp
                                )
                                if (yearVal == normalizedSelected) {
                                    Text("✓", color = BentoPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        onClick = {
                            onValueChanged(yearVal)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


// --- TAB 1: GRADES ENTRY ---
@Composable
fun GradesEntryTab(
    viewModel: TeacherViewModel,
    classSections: List<ClassSection>,
    subjects: List<Subject>,
    students: List<Student>,
    grades: List<Grade>,
    insideClassMode: Boolean = true
) {
    val context = LocalContext.current
    val gradingPlaceKey = "grading_" + (viewModel.selectedClassId.value ?: 0L) + "_" + viewModel.selectedTermId.value + "_"
    var entryMode by remember {
        mutableStateOf(
            com.example.data.state.LastPlaceStore.getInt(context, gradingPlaceKey + "mode")?.takeIf { it == 0 || it == 1 } ?: 0
        )
    } // 0: By Subject for all students, 1: By Student for all subjects

    val selectedClassId by viewModel.selectedClassId.collectAsState()
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsState()
    val selectedStudentId by viewModel.selectedStudentId.collectAsState()
    val activeTermId by viewModel.selectedTermId.collectAsState()
    val classProgress by viewModel.classProgress.collectAsState()
    val customizations by viewModel.customizations.collectAsState()
    val performanceList by viewModel.currentClassPerformance.collectAsState()
    val isFinalTermOnlyFeatureEnabled by viewModel.isFinalTermOnlyFeatureEnabled.collectAsState()

    val manualTermAverages by viewModel.manualTermAverages.collectAsState()
    val availableSubjects = remember(subjects, activeTermId) {
        if (activeTermId == 4) {
            listOf(
                Subject(id = -101L, name = "📝 معدل الفصل الأول (لكل طالب)", level = 1, maxPoints = 20),
                Subject(id = -102L, name = "📝 معدل الفصل الثاني (لكل طالب)", level = 1, maxPoints = 20)
            ) + subjects
        } else {
            subjects
        }
    }
    val activeClass = classSections.find { it.id == selectedClassId }
    val activeSubject = availableSubjects.find { it.id == selectedSubjectId }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopActivationListener()
        }
    }

    val classStudents = remember(students, selectedClassId) { students.filter { it.classId == selectedClassId }.sortedBy { it.id } }
    val activeStudent = classStudents.find { it.id == selectedStudentId }

    val appLanguage by viewModel.appLanguage.collectAsState()

    var showSubjectDialog by remember { mutableStateOf(false) }
    var showStudentDialog by remember { mutableStateOf(false) }
    var showActivationPromoDialog by remember { mutableStateOf(false) }
    var targetTermNameForActivation by remember { mutableStateOf<String?>(null) }
    var showPullOptionsDialog by remember { mutableStateOf(false) }
    var showStudentSelectorInDialog by remember { mutableStateOf(false) }
    var shareOnSelect by remember { mutableStateOf(false) }

    var isGradingActive by remember {
        mutableStateOf(com.example.data.state.LastPlaceStore.getBoolean(context, gradingPlaceKey + "active"))
    }
    var gradingPlaceRestored by remember { mutableStateOf(false) }
    LaunchedEffect(availableSubjects, classStudents) {
        if (gradingPlaceRestored) return@LaunchedEffect
        if (availableSubjects.isEmpty() && classStudents.isEmpty()) return@LaunchedEffect
        val savedSubject = com.example.data.state.LastPlaceStore.getLong(context, gradingPlaceKey + "subject")
        val savedStudent = com.example.data.state.LastPlaceStore.getLong(context, gradingPlaceKey + "student")
        if (selectedSubjectId == null && savedSubject != null && availableSubjects.any { it.id == savedSubject }) {
            viewModel.selectSubject(savedSubject)
        }
        if (selectedStudentId == null && savedStudent != null && classStudents.any { it.id == savedStudent }) {
            viewModel.selectStudent(savedStudent)
        }
        gradingPlaceRestored = true
    }
    LaunchedEffect(gradingPlaceRestored, entryMode, isGradingActive, selectedSubjectId, selectedStudentId) {
        if (!gradingPlaceRestored) return@LaunchedEffect
        val store = com.example.data.state.LastPlaceStore
        store.putInt(context, gradingPlaceKey + "mode", entryMode)
        store.putBoolean(context, gradingPlaceKey + "active", isGradingActive)
        store.putLong(context, gradingPlaceKey + "subject", selectedSubjectId)
        store.putLong(context, gradingPlaceKey + "student", selectedStudentId)
    }

    val gradingProgressInfo = remember(classStudents, subjects, grades, manualTermAverages, activeTermId) {
        val totalStudents = classStudents.size
        if (totalStudents == 0) return@remember Triple(0, 0, 0)

        val studentIds = classStudents.map { it.id }.toSet()
        val subjectIds = subjects.map { it.id }.toSet()

        if (activeTermId == 4) {
            val totalRequired = totalStudents * (subjectIds.size + 2)
            if (totalRequired == 0) return@remember Triple(0, 0, 0)

            var prevTermsEntered = 0
            classStudents.forEach { student ->
                val hasT1Manual = manualTermAverages.any { it.studentId == student.id && it.termId == 1 && it.averageScore > 0.0 }
                val t1Grades = grades.filter { it.studentId == student.id && it.termId == 1 }
                val hasT1Grades = subjectIds.isNotEmpty() && subjectIds.all { sid -> t1Grades.any { it.subjectId == sid } }
                if (hasT1Manual || hasT1Grades) prevTermsEntered++

                val hasT2Manual = manualTermAverages.any { it.studentId == student.id && it.termId == 2 && it.averageScore > 0.0 }
                val t2Grades = grades.filter { it.studentId == student.id && it.termId == 2 }
                val hasT2Grades = subjectIds.isNotEmpty() && subjectIds.all { sid -> t2Grades.any { it.subjectId == sid } }
                if (hasT2Manual || hasT2Grades) prevTermsEntered++
            }
            val normalGradesCount = grades.count { it.termId == 3 && it.studentId in studentIds && it.subjectId in subjectIds }

            val totalEntered = (prevTermsEntered + normalGradesCount).coerceAtMost(totalRequired)
            val percent = ((totalEntered * 100) / totalRequired).coerceIn(0, 100)
            Triple(totalEntered, totalRequired, percent)
        } else {
            val totalRequired = totalStudents * subjectIds.size
            if (totalRequired == 0) return@remember Triple(0, 0, 0)

            val totalEntered = grades.count { it.termId == activeTermId && it.studentId in studentIds && it.subjectId in subjectIds }.coerceAtMost(totalRequired)
            val percent = ((totalEntered * 100) / totalRequired).coerceIn(0, 100)
            Triple(totalEntered, totalRequired, percent)
        }
    }
    val gradingProgress = gradingProgressInfo.third

    LaunchedEffect(activeClass?.isActivated) {
        if (activeClass?.isActivated == true) {
            showActivationPromoDialog = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (!isGradingActive) Modifier.verticalScroll(rememberScrollState()) else Modifier
            )
            .padding(16.dp)
    ) {
        if (isGradingActive && ((entryMode == 0 && activeSubject != null) || (entryMode == 1 && activeStudent != null))) {
            var globalSaveAction by remember { mutableStateOf<(() -> Unit)?>(null) }

            // Highly compact summary header to maximize screen space for the students list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val termText = when (activeTermId) {
                        1 -> AppLocalization.tr("term_1", "الأول", appLanguage)
                        2 -> AppLocalization.tr("term_2", "الثاني", appLanguage)
                        3 -> AppLocalization.tr("term_3", "الثالث", appLanguage)
                        4 -> "الفصل الأخير فقط ⭐"
                        else -> "$activeTermId"
                    }
                    Text(
                        text = "${AppLocalization.tr("tab_grades", "رصد الدرجات", appLanguage)} • $termText 📅",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (entryMode == 0) "${AppLocalization.tr("subject_name", "المادة", appLanguage)}: ${activeSubject?.name}" else "${AppLocalization.tr("tab_students", "الطالب", appLanguage)}: ${activeStudent?.name}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = BentoText
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { globalSaveAction?.invoke() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("save_grades_top_sticky_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "حفظ", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppLocalization.tr("save_results_short", "حفظ النتائج 💾", appLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { isGradingActive = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(AppLocalization.tr("back_modify_options", "رجوع ↩️", appLanguage), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (entryMode == 0) {
                // Bulk enter grades of a single Subject for all Students in a Section
                val currentScoresInput = remember(selectedClassId, selectedSubjectId, activeTermId) {
                    mutableStateMapOf<Long, String>()
                }

                val userTouchedScoreIds = remember(selectedClassId, selectedSubjectId, activeTermId) {
                    mutableStateMapOf<Long, Boolean>()
                }

                LaunchedEffect(selectedClassId, selectedSubjectId, activeTermId, grades, classStudents, manualTermAverages) {
                    if (activeSubject != null) {
                        val currClassId = selectedClassId ?: activeClass?.id ?: 0L
                        if (activeSubject.id == -101L) {
                            classStudents.forEach { student ->
                                val effectiveAvg = viewModel.calculateStudentTermAverage(student.id, currClassId, 1)
                                val formattedScore = if (effectiveAvg > 0.0) {
                                    if (effectiveAvg % 1.0 == 0.0) effectiveAvg.toLong().toString() else String.format(Locale.US, "%.2f", effectiveAvg)
                                } else ""
                                val currentVal = currentScoresInput[student.id]
                                if (currentVal == null || currentVal.isEmpty()) {
                                    currentScoresInput[student.id] = formattedScore
                                }
                            }
                        } else if (activeSubject.id == -102L) {
                            classStudents.forEach { student ->
                                val effectiveAvg = viewModel.calculateStudentTermAverage(student.id, currClassId, 2)
                                val formattedScore = if (effectiveAvg > 0.0) {
                                    if (effectiveAvg % 1.0 == 0.0) effectiveAvg.toLong().toString() else String.format(Locale.US, "%.2f", effectiveAvg)
                                } else ""
                                val currentVal = currentScoresInput[student.id]
                                if (currentVal == null || currentVal.isEmpty()) {
                                    currentScoresInput[student.id] = formattedScore
                                }
                            }
                        } else {
                            val targetGradeTerm = if (activeTermId == 4) 3 else activeTermId
                            classStudents.forEach { student ->
                                val score = grades.find { it.studentId == student.id && it.subjectId == activeSubject.id && it.termId == targetGradeTerm }
                                if (score != null) {
                                    val formattedScore = if (score.score % 1.0 == 0.0) score.score.toLong().toString() else String.format(Locale.US, "%.1f", score.score)
                                    val currentVal = currentScoresInput[student.id]
                                    if (currentVal == null || currentVal.isEmpty()) {
                                        currentScoresInput[student.id] = formattedScore
                                    }
                                } else if (!currentScoresInput.containsKey(student.id)) {
                                    currentScoresInput[student.id] = ""
                                }
                            }
                        }
                    }
                }

                val recentlySavedSubjectStudentIds = remember { mutableStateMapOf<Long, Long>() }

                LaunchedEffect(recentlySavedSubjectStudentIds.toMap()) {
                    if (recentlySavedSubjectStudentIds.isNotEmpty()) {
                        kotlinx.coroutines.delay(1000L)
                        val now = System.currentTimeMillis()
                        val expired = recentlySavedSubjectStudentIds.filter { now - it.value >= 900L }.keys
                        expired.forEach { recentlySavedSubjectStudentIds.remove(it) }
                    }
                }

                val saveSingleStudentScoreMode0: (Long) -> Unit = { studentId ->
                    val rawVal = currentScoresInput[studentId]?.trim() ?: ""
                    if (activeSubject != null) {
                        if (activeSubject.id == -101L) {
                            if (rawVal.isNotBlank()) {
                                val p = rawVal.toDoubleOrNull()
                                if (p != null && p in 0.0..20.0) {
                                    viewModel.saveStudentManualTermAverage(studentId, 1, p)
                                    recentlySavedSubjectStudentIds[studentId] = System.currentTimeMillis()
                                } else {
                                    Toast.makeText(context, "الرجاء كتابة معدل صحيح بين 0 و 20", Toast.LENGTH_SHORT).show()
                                }
                            } else if (userTouchedScoreIds[studentId] == true) {
                                viewModel.deleteStudentManualTermAverage(studentId, 1)
                            }
                        } else if (activeSubject.id == -102L) {
                            if (rawVal.isNotBlank()) {
                                val p = rawVal.toDoubleOrNull()
                                if (p != null && p in 0.0..20.0) {
                                    viewModel.saveStudentManualTermAverage(studentId, 2, p)
                                    recentlySavedSubjectStudentIds[studentId] = System.currentTimeMillis()
                                } else {
                                    Toast.makeText(context, "الرجاء كتابة معدل صحيح بين 0 و 20", Toast.LENGTH_SHORT).show()
                                }
                            } else if (userTouchedScoreIds[studentId] == true) {
                                viewModel.deleteStudentManualTermAverage(studentId, 2)
                            }
                        } else {
                            val targetGradeTerm = if (activeTermId == 4) 3 else activeTermId
                            if (rawVal.isNotBlank()) {
                                val p = rawVal.toDoubleOrNull()
                                val limit = activeSubject.maxPoints.toDouble()
                                if (p != null && p in 0.0..limit) {
                                    viewModel.saveSingleGrade(studentId, activeSubject.id, p, targetGradeTerm)
                                    recentlySavedSubjectStudentIds[studentId] = System.currentTimeMillis()
                                } else {
                                    Toast.makeText(context, "الرجاء كتابة درجة صحيحة بين 0 و ${activeSubject.maxPoints}", Toast.LENGTH_SHORT).show()
                                }
                            } else if (userTouchedScoreIds[studentId] == true) {
                                viewModel.deleteGrade(studentId, activeSubject.id, targetGradeTerm)
                            }
                        }
                    }
                }

                val saveSubjectAction = remember(currentScoresInput, activeSubject, activeTermId, context, viewModel) {
                    {
                        if (activeSubject != null) {
                            if (activeSubject.id == -101L) {
                                val averagesMap = mutableMapOf<Long, Double>()
                                var errorFound = false
                                val savedNow = mutableListOf<Long>()
                                currentScoresInput.forEach { (studentId, value) ->
                                    if (value.isNotBlank()) {
                                        val parseVal = value.toDoubleOrNull()
                                        if (parseVal != null && parseVal in 0.0..20.0) {
                                            averagesMap[studentId] = parseVal
                                            savedNow.add(studentId)
                                        } else {
                                            errorFound = true
                                        }
                                    }
                                }
                                if (errorFound) {
                                    Toast.makeText(context, "الرجاء كتابة معدلات صحيحة بين 0 و 20", Toast.LENGTH_LONG).show()
                                } else {
                                    viewModel.saveManualTermAveragesBulk(1, averagesMap) {
                                        val now = System.currentTimeMillis()
                                        savedNow.forEach { id -> recentlySavedSubjectStudentIds[id] = now }
                                        Toast.makeText(context, "تم حفظ معدلات الفصل الأول بنجاح!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else if (activeSubject.id == -102L) {
                                val averagesMap = mutableMapOf<Long, Double>()
                                var errorFound = false
                                val savedNow = mutableListOf<Long>()
                                currentScoresInput.forEach { (studentId, value) ->
                                    if (value.isNotBlank()) {
                                        val parseVal = value.toDoubleOrNull()
                                        if (parseVal != null && parseVal in 0.0..20.0) {
                                            averagesMap[studentId] = parseVal
                                            savedNow.add(studentId)
                                        } else {
                                            errorFound = true
                                        }
                                    }
                                }
                                if (errorFound) {
                                    Toast.makeText(context, "الرجاء كتابة معدلات صحيحة بين 0 و 20", Toast.LENGTH_LONG).show()
                                } else {
                                    viewModel.saveManualTermAveragesBulk(2, averagesMap) {
                                        val now = System.currentTimeMillis()
                                        savedNow.forEach { id -> recentlySavedSubjectStudentIds[id] = now }
                                        Toast.makeText(context, "تم حفظ معدلات الفصل الثاني بنجاح!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                val targetGradeTerm = if (activeTermId == 4) 3 else activeTermId
                                val parseList = mutableListOf<Grade>()
                                var errorFound = false
                                val savedNow = mutableListOf<Long>()
                                currentScoresInput.forEach { (studentId, value) ->
                                    if (value.isNotBlank()) {
                                        val parseVal = value.toDoubleOrNull()
                                        if (parseVal != null && parseVal in 0.0..activeSubject.maxPoints.toDouble()) {
                                            parseList.add(Grade(studentId, activeSubject.id, targetGradeTerm, parseVal))
                                            savedNow.add(studentId)
                                        } else {
                                            errorFound = true
                                        }
                                    }
                                }

                                if (errorFound) {
                                    Toast.makeText(
                                        context,
                                        "الرجاء كتابة درجات صحيحة بين 0 و ${activeSubject.maxPoints}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    viewModel.saveGrades(parseList)
                                    val now = System.currentTimeMillis()
                                    savedNow.forEach { id -> recentlySavedSubjectStudentIds[id] = now }
                                    Toast.makeText(context, "تم حفظ درجات المادة بنجاح!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

                SideEffect {
                    globalSaveAction = saveSubjectAction
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val activeProgressTotal = classStudents.size
                    val activeProgressCompleted = currentScoresInput.values.count { it.isNotBlank() }
                    val activeProgressPercent = if (activeProgressTotal > 0) activeProgressCompleted.toFloat() / activeProgressTotal else 0f

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "شريط تقدم رصد المادة للقسم 📊",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "تم رصد $activeProgressCompleted من $activeProgressTotal طالب (${(activeProgressPercent * 100).toInt()}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { activeProgressPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = BentoPrimary,
                                trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp)),
                        verticalArrangement = Arrangement.Top
                    ) {
                        if (classStudents.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = "لا يوجد طلاب في هذا القسم",
                                    description = "الرجاء إضافة بعض الطلاب للقسم أولاً لتتمكن من رصد نقاطهم."
                                )
                            }
                        } else {
                            itemsIndexed(classStudents) { idx, student ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (idx % 2 == 0) Color.White else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${idx + 1}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = student.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        val isSavedVisual = recentlySavedSubjectStudentIds.containsKey(student.id) &&
                                                (System.currentTimeMillis() - (recentlySavedSubjectStudentIds[student.id] ?: 0L) < 1000L)

                                        OutlinedTextField(
                                            value = currentScoresInput[student.id] ?: "",
                                            onValueChange = { newVal ->
                                                userTouchedScoreIds[student.id] = true
                                                if (newVal.isEmpty()) {
                                                    currentScoresInput[student.id] = newVal
                                                } else {
                                                    val parseVal = newVal.toDoubleOrNull()
                                                    if (parseVal != null || newVal.endsWith(".")) {
                                                        val limit = if (activeSubject!!.id < 0) 20.0 else activeSubject!!.maxPoints.toDouble()
                                                        if (parseVal == null || parseVal <= limit) {
                                                            currentScoresInput[student.id] = newVal
                                                        }
                                                    }
                                                }
                                            },
                                            placeholder = { Text(if (activeSubject!!.id < 0) "0-20" else "0-${activeSubject!!.maxPoints}", fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                            keyboardActions = KeyboardActions(
                                                onNext = {
                                                    saveSingleStudentScoreMode0(student.id)
                                                    defaultKeyboardAction(ImeAction.Next)
                                                },
                                                onDone = {
                                                    saveSingleStudentScoreMode0(student.id)
                                                }
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            textStyle = TextStyle(fontFamily = com.example.ui.theme.AppFonts.family, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                            modifier = Modifier
                                                .width(85.dp)
                                                .height(48.dp)
                                                .onFocusChanged { focusState ->
                                                    if (!focusState.isFocused) {
                                                        saveSingleStudentScoreMode0(student.id)
                                                    }
                                                }
                                                .testTag("score_input_${student.id}"),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = if (isSavedVisual) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                focusedBorderColor = if (isSavedVisual) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                                unfocusedContainerColor = if (isSavedVisual) Color(0xFFE8F5E9) else Color.White,
                                                focusedContainerColor = if (isSavedVisual) Color(0xFFE8F5E9) else Color.White
                                            )
                                        )
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    AppFooter()
                }
            } else {
                // Bulk enter subject results for a single Student
                if (subjects.isEmpty()) {
                    EmptyStateCard(
                        title = "لا توجد مواد مسجلة",
                        description = "مستوى المواد الدراسية فارغ. افتح تبويب المواد الدراسية وأضف المواد (مثل: الرياضيات، العربية) لرصدهم."
                    )
                } else {
                    // Gather existing grades for selected Student
                    val studentScoresInput = remember(selectedStudentId, activeTermId) {
                        mutableStateMapOf<Long, String>()
                    }
                    val term1AvgInput = remember(selectedStudentId, activeTermId) { mutableStateOf("") }
                    val term2AvgInput = remember(selectedStudentId, activeTermId) { mutableStateOf("") }
                    val userTouchedSubjectIds = remember(selectedStudentId, activeTermId) {
                        mutableStateMapOf<Long, Boolean>()
                    }
                    val userTouchedTerm1Avg = remember(selectedStudentId, activeTermId) { mutableStateOf(false) }
                    val userTouchedTerm2Avg = remember(selectedStudentId, activeTermId) { mutableStateOf(false) }
                    val targetGradeTerm = if (activeTermId == 4) 3 else activeTermId

                    LaunchedEffect(selectedStudentId, activeTermId, subjects, grades, manualTermAverages) {
                        if (activeStudent != null) {
                            if (activeTermId == 4) {
                                val effAvg1 = viewModel.calculateStudentTermAverage(activeStudent.id, activeStudent.classId, 1)
                                val effAvg2 = viewModel.calculateStudentTermAverage(activeStudent.id, activeStudent.classId, 2)
                                term1AvgInput.value = if (effAvg1 > 0.0) {
                                    if (effAvg1 % 1.0 == 0.0) effAvg1.toLong().toString() else String.format(Locale.US, "%.2f", effAvg1)
                                } else ""
                                term2AvgInput.value = if (effAvg2 > 0.0) {
                                    if (effAvg2 % 1.0 == 0.0) effAvg2.toLong().toString() else String.format(Locale.US, "%.2f", effAvg2)
                                } else ""
                            }
                            subjects.forEach { sub ->
                                val scoreItem = grades.find { it.studentId == activeStudent.id && it.subjectId == sub.id && it.termId == targetGradeTerm }
                                if (scoreItem != null) {
                                    val formattedScore = if (scoreItem.score % 1.0 == 0.0) scoreItem.score.toLong().toString() else String.format(Locale.US, "%.1f", scoreItem.score)
                                    val currentVal = studentScoresInput[sub.id]
                                    if (currentVal == null || currentVal.isEmpty()) {
                                        studentScoresInput[sub.id] = formattedScore
                                    }
                                } else if (!studentScoresInput.containsKey(sub.id)) {
                                    studentScoresInput[sub.id] = ""
                                }
                            }
                        }
                    }

                    val recentlySavedSubjectIds = remember { mutableStateMapOf<Long, Long>() }

                    LaunchedEffect(recentlySavedSubjectIds.toMap()) {
                        if (recentlySavedSubjectIds.isNotEmpty()) {
                            kotlinx.coroutines.delay(1000L)
                            val now = System.currentTimeMillis()
                            val expired = recentlySavedSubjectIds.filter { now - it.value >= 900L }.keys
                            expired.forEach { recentlySavedSubjectIds.remove(it) }
                        }
                    }

                    val saveSingleSubjectScoreMode1: (Long) -> Unit = { subjectId ->
                        val rawVal = studentScoresInput[subjectId]?.trim() ?: ""
                        val sub = subjects.find { it.id == subjectId }
                        val limit = sub?.maxPoints?.toDouble() ?: 20.0
                        val targetGradeTerm = if (activeTermId == 4) 3 else activeTermId
                        if (activeStudent != null) {
                            if (rawVal.isNotBlank()) {
                                val p = rawVal.toDoubleOrNull()
                                if (p != null && p in 0.0..limit) {
                                    viewModel.saveSingleGrade(activeStudent.id, subjectId, p, targetGradeTerm)
                                    recentlySavedSubjectIds[subjectId] = System.currentTimeMillis()
                                } else {
                                    Toast.makeText(context, "الرجاء كتابة درجة صحيحة بين 0 و $limit", Toast.LENGTH_SHORT).show()
                                }
                            } else if (userTouchedSubjectIds[subjectId] == true) {
                                viewModel.deleteGrade(activeStudent.id, subjectId, targetGradeTerm)
                            }
                        }
                    }

                    val saveSingleTerm1AvgMode1: () -> Unit = {
                        if (activeStudent != null) {
                            val rawVal = term1AvgInput.value.trim()
                            if (rawVal.isNotBlank()) {
                                val p = rawVal.toDoubleOrNull()
                                if (p != null && p in 0.0..20.0) {
                                    viewModel.saveStudentManualTermAverage(activeStudent.id, 1, p)
                                } else {
                                    Toast.makeText(context, "الرجاء كتابة معدل صحيح بين 0 و 20", Toast.LENGTH_SHORT).show()
                                }
                            } else if (userTouchedTerm1Avg.value) {
                                viewModel.deleteStudentManualTermAverage(activeStudent.id, 1)
                            }
                        }
                    }

                    val saveSingleTerm2AvgMode1: () -> Unit = {
                        if (activeStudent != null) {
                            val rawVal = term2AvgInput.value.trim()
                            if (rawVal.isNotBlank()) {
                                val p = rawVal.toDoubleOrNull()
                                if (p != null && p in 0.0..20.0) {
                                    viewModel.saveStudentManualTermAverage(activeStudent.id, 2, p)
                                } else {
                                    Toast.makeText(context, "الرجاء كتابة معدل صحيح بين 0 و 20", Toast.LENGTH_SHORT).show()
                                }
                            } else if (userTouchedTerm2Avg.value) {
                                viewModel.deleteStudentManualTermAverage(activeStudent.id, 2)
                            }
                        }
                    }

                    val saveStudentAction = remember(studentScoresInput, term1AvgInput.value, term2AvgInput.value, activeStudent, activeTermId, subjects, context, viewModel) {
                        {
                            val parseList = mutableListOf<Grade>()
                            var errorFound = false
                            val savedNow = mutableListOf<Long>()
                            studentScoresInput.forEach { (subjectId, value) ->
                                if (value.isNotBlank()) {
                                    val parseVal = value.toDoubleOrNull()
                                    val sub = subjects.find { it.id == subjectId }
                                    val limit = sub?.maxPoints?.toDouble() ?: 20.0
                                    if (parseVal != null && parseVal in 0.0..limit) {
                                        parseList.add(Grade(activeStudent!!.id, subjectId, targetGradeTerm, parseVal))
                                        savedNow.add(subjectId)
                                    } else {
                                        errorFound = true
                                    }
                                }
                            }

                            if (activeTermId == 4 && activeStudent != null) {
                                val t1Val = term1AvgInput.value.trim()
                                if (t1Val.isNotEmpty()) {
                                    val p1 = t1Val.toDoubleOrNull()
                                    if (p1 != null && p1 in 0.0..20.0) {
                                        viewModel.saveStudentManualTermAverage(activeStudent.id, 1, p1)
                                    } else {
                                        errorFound = true
                                    }
                                }
                                val t2Val = term2AvgInput.value.trim()
                                if (t2Val.isNotEmpty()) {
                                    val p2 = t2Val.toDoubleOrNull()
                                    if (p2 != null && p2 in 0.0..20.0) {
                                        viewModel.saveStudentManualTermAverage(activeStudent.id, 2, p2)
                                    } else {
                                        errorFound = true
                                    }
                                }
                            }

                            if (errorFound) {
                                Toast.makeText(
                                    context,
                                    "الرجاء كتابة درجات ومعدلات صحيحة لا تتعدى الحد المسموح (0 - 20)",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                viewModel.saveGrades(parseList)
                                val now = System.currentTimeMillis()
                                savedNow.forEach { id -> recentlySavedSubjectIds[id] = now }
                                Toast.makeText(context, "تم حفظ درجات الطالب بنجاح!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    SideEffect {
                        globalSaveAction = saveStudentAction
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        val activeProgressTotal = subjects.size
                        val activeProgressCompleted = studentScoresInput.values.count { it.isNotBlank() }
                        val activeProgressPercent = if (activeProgressTotal > 0) activeProgressCompleted.toFloat() / activeProgressTotal else 0f

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "شريط تقدم رصد الطالب للمواد 📊",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "تم رصد $activeProgressCompleted من $activeProgressTotal مادة (${(activeProgressPercent * 100).toInt()}%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { activeProgressPercent },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = BentoPrimary,
                                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp)),
                            verticalArrangement = Arrangement.Top
                        ) {
                            if (activeTermId == 4) {
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("⭐", fontSize = 15.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "معدلات الفصول السابقة (تُجلب تلقائياً أو تُدخل يدوياً)",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            val hasT1 = activeStudent != null && viewModel.hasSubjectGradesForTerm(activeStudent.id, 1)
                                            val hasT2 = activeStudent != null && viewModel.hasSubjectGradesForTerm(activeStudent.id, 2)
                                            if (hasT1 || hasT2) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "✨ ملاحظة: المعدلات المحسوبة من درجات فصولك السابقة بالتطبيق تم اعتمادها تلقائياً لتوحيد النتائج.",
                                                    fontSize = 10.5.sp,
                                                    color = Color(0xFF2E7D32),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = term1AvgInput.value,
                                                    onValueChange = { userTouchedTerm1Avg.value = true; term1AvgInput.value = it },
                                                    label = { Text(if (hasT1) "معدل ف1 (معتمد تلقائياً)" else "معدل ف1 (من 20)", fontSize = 11.sp) },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .onFocusChanged { focusState ->
                                                            if (!focusState.isFocused) {
                                                                saveSingleTerm1AvgMode1()
                                                            }
                                                        },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                                    keyboardActions = KeyboardActions(
                                                        onNext = {
                                                            saveSingleTerm1AvgMode1()
                                                            defaultKeyboardAction(ImeAction.Next)
                                                        }
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        unfocusedContainerColor = if (hasT1) Color(0xFFE8F5E9) else Color.White,
                                                        focusedContainerColor = Color.White
                                                    )
                                                )
                                                OutlinedTextField(
                                                    value = term2AvgInput.value,
                                                    onValueChange = { userTouchedTerm2Avg.value = true; term2AvgInput.value = it },
                                                    label = { Text(if (hasT2) "معدل ف2 (معتمد تلقائياً)" else "معدل ف2 (من 20)", fontSize = 11.sp) },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .onFocusChanged { focusState ->
                                                            if (!focusState.isFocused) {
                                                                saveSingleTerm2AvgMode1()
                                                            }
                                                        },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                                    keyboardActions = KeyboardActions(
                                                        onNext = {
                                                            saveSingleTerm2AvgMode1()
                                                            defaultKeyboardAction(ImeAction.Next)
                                                        }
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        unfocusedContainerColor = if (hasT2) Color(0xFFE8F5E9) else Color.White,
                                                        focusedContainerColor = Color.White
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            itemsIndexed(subjects) { idx, subject ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (idx % 2 == 0) Color.White else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                Icons.Default.Book,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = subject.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        val isSavedVisual = recentlySavedSubjectIds.containsKey(subject.id) &&
                                                (System.currentTimeMillis() - (recentlySavedSubjectIds[subject.id] ?: 0L) < 1000L)

                                        OutlinedTextField(
                                            value = studentScoresInput[subject.id] ?: "",
                                            onValueChange = { newVal ->
                                                userTouchedSubjectIds[subject.id] = true
                                                if (newVal.isEmpty()) {
                                                    studentScoresInput[subject.id] = newVal
                                                } else {
                                                    val parseVal = newVal.toDoubleOrNull()
                                                    if (parseVal != null || newVal.endsWith(".")) {
                                                        val limit = subject.maxPoints.toDouble()
                                                        if (parseVal == null || parseVal <= limit) {
                                                            studentScoresInput[subject.id] = newVal
                                                        }
                                                    }
                                                }
                                            },
                                            placeholder = { Text("0-${subject.maxPoints}", fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                            keyboardActions = KeyboardActions(
                                                onNext = {
                                                    saveSingleSubjectScoreMode1(subject.id)
                                                    defaultKeyboardAction(ImeAction.Next)
                                                },
                                                onDone = {
                                                    saveSingleSubjectScoreMode1(subject.id)
                                                }
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            textStyle = TextStyle(fontFamily = com.example.ui.theme.AppFonts.family, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                            modifier = Modifier
                                                .width(85.dp)
                                                .height(48.dp)
                                                .onFocusChanged { focusState ->
                                                    if (!focusState.isFocused) {
                                                        saveSingleSubjectScoreMode1(subject.id)
                                                    }
                                                }
                                                .testTag("score_input_sub_${subject.id}"),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = if (isSavedVisual) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                focusedBorderColor = if (isSavedVisual) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                                unfocusedContainerColor = if (isSavedVisual) Color(0xFFE8F5E9) else Color.White,
                                                focusedContainerColor = if (isSavedVisual) Color(0xFFE8F5E9) else Color.White
                                            )
                                        )
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        AppFooter()
                    }
                }
            }
        } else {
            // STEP 1: CONFIGURATION & SELECTION VIEW
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = AppLocalization.tr("grades_entry_title", "رصد وإدخال درجات الطلاب 📝", appLanguage),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = AppLocalization.tr("grades_entry_subtitle", "يرجى تحديد الفصل الدراسي وطريقة الإدخال ثم المادة لبدء واجهة رصد العلامات.", appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (activeClass != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                val (totalEntered, totalRequired, progressPercent) = gradingProgressInfo
                val progressColor = if (progressPercent == 100) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                val progressTrackColor = progressColor.copy(alpha = 0.15f)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grades_entry_progress_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = progressColor.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, progressColor.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📈", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "شريط تقدم تسجيل النتائج 📊",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = BentoText
                                )
                            }
                            Text(
                                text = "$progressPercent%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = progressColor
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = progressColor,
                            trackColor = progressTrackColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val detailsNote = if (activeTermId == 4) {
                            "تم رصد $totalEntered من أصل $totalRequired بند (درجات مواد الفصل 3 + معدلي الفصلين 1 و 2 لجميع الطلاب)."
                        } else {
                            "تم رصد $totalEntered من أصل $totalRequired درجة لجميع طلاب ومواد القسم في هذا الفصل."
                        }
                        
                        Text(
                            text = detailsNote,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Term Selector Dropdown Card
            var expandedGradeTermDropdown by remember { mutableStateOf(false) }
            val isTerm4Active = activeClass?.isTerm4Activated == true
            val isTerm3Active = activeClass?.isTerm3Activated == true

            val terms = when {
                isTerm4Active -> {
                    listOf(4 to "الفصل الأخير فقط ⭐")
                }
                isTerm3Active -> {
                    listOf(
                        1 to AppLocalization.tr("term_1", "الفصل الأول", appLanguage),
                        2 to AppLocalization.tr("term_2", "الفصل الثاني", appLanguage),
                        3 to AppLocalization.tr("term_3", "الفصل الثالث", appLanguage)
                    )
                }
                isFinalTermOnlyFeatureEnabled -> {
                    listOf(
                        1 to AppLocalization.tr("term_1", "الفصل الأول", appLanguage),
                        2 to AppLocalization.tr("term_2", "الفصل الثاني", appLanguage),
                        3 to AppLocalization.tr("term_3", "الفصل الثالث", appLanguage),
                        4 to "الفصل الأخير فقط ⭐"
                    )
                }
                else -> {
                    listOf(
                        1 to AppLocalization.tr("term_1", "الفصل الأول", appLanguage),
                        2 to AppLocalization.tr("term_2", "الفصل الثاني", appLanguage),
                        3 to AppLocalization.tr("term_3", "الفصل الثالث", appLanguage)
                    )
                }
            }

            LaunchedEffect(terms, activeTermId) {
                if (terms.none { it.first == activeTermId }) {
                    terms.firstOrNull()?.first?.let { firstTermId ->
                        viewModel.selectTerm(firstTermId)
                    }
                }
            }

            val currentGradeTermTuple = terms.find { it.first == activeTermId } ?: terms.first()
            val isCurrentGradeTermLocked = activeClass?.isTermUnlocked(currentGradeTermTuple.first) == false
            val currentGradeTermDisplay = if (isCurrentGradeTermLocked) "${currentGradeTermTuple.second} 🔒" else currentGradeTermTuple.second

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = AppLocalization.tr("select_current_term", "الفصل الدراسي", appLanguage),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = BentoText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedGradeTermDropdown = true
                                    if (activeClass?.isTermUnlocked(2) == false ||
                                        activeClass?.isTermUnlocked(3) == false ||
                                        activeClass?.isTermUnlocked(4) == false
                                    ) {
                                        viewModel.startActivationListener()
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, if (activeTermId == 4) Color(0xFFD32F2F) else Color(0xFF2E7D32)),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (activeTermId == 4) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("📅", fontSize = 16.sp)
                                    Text(
                                        text = currentGradeTermDisplay,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (activeTermId == 4) Color(0xFFC62828) else Color(0xFF1B5E20)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "اختر الفصل",
                                    tint = if (activeTermId == 4) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = expandedGradeTermDropdown,
                            onDismissRequest = { expandedGradeTermDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.88f)
                        ) {
                            terms.forEach { (termId, termName) ->
                                val isLocked = activeClass?.isTermUnlocked(termId) == false
                                val displayName = if (isLocked) "$termName 🔒" else termName
                                val isTerm4 = (termId == 4)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isTerm4) Color(0xFFFFEBEE) else Color(0xFFF1F8E9)
                                    ),
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isTerm4) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                    )
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = displayName,
                                                        fontWeight = if (activeTermId == termId) FontWeight.Black else FontWeight.Bold,
                                                        color = if (isTerm4) Color(0xFFC62828) else Color(0xFF1B5E20),
                                                        fontSize = 13.5.sp
                                                    )
                                                    if (activeTermId == termId) {
                                                        Text("✓", color = if (isTerm4) Color(0xFFC62828) else Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                if (isTerm4) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "مخصص للوافدين الجدد الذين لم يدرجو نتائج الفصل الأول والثانى",
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFC62828),
                                                        lineHeight = 14.sp
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            expandedGradeTermDropdown = false
                                            viewModel.checkTermLockAndSelect(termId, activeClass?.id) { isUnlocked ->
                                                if (!isUnlocked) {
                                                    if (activeClass != null && !activeClass.isSyncedToServer) {
                                                        viewModel.resendUnsyncedClasses()
                                                        Toast.makeText(
                                                            context,
                                                            "لم يصل قسمك إلى الخادم بعد. تأكد من اتصالك بالإنترنت وأعد المحاولة بعد لحظات.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    } else if (activeClass != null && activeClass.isActivationExpired()) {
                                                        Toast.makeText(
                                                            context,
                                                            "هذا القسم من سنة دراسية منتهية (${activeClass.academicYear})، والتفعيل يخصّ السنة الجارية فقط. أما كشوف هذا القسم فهي متاحة لك بدون تفعيل من صفحة الكشوف.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    } else {
                                                    targetTermNameForActivation = termName
                                                    showActivationPromoDialog = true
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }


                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selection Mode Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppLocalization.tr("grading_method", "طريقة رصد العلامات 🛠️", appLanguage),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = BentoText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (entryMode == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { entryMode = 0 }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = AppLocalization.tr("grade_by_subject", "رصد نتيجة مادة للقسم", appLanguage),
                                color = if (entryMode == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (entryMode == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { entryMode = 1 }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = AppLocalization.tr("grade_by_student", "رصد مواد لطالب واحد", appLanguage),
                                color = if (entryMode == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subject or Student Selector Card
            if (activeClass == null) {
                EmptyStateCard(
                    title = AppLocalization.tr("select_class_first", "الرجاء اختيار قسم أولاً 🏫", appLanguage),
                    description = AppLocalization.tr("select_class_first_desc", "قم باختيار قسم دراسي لتفعيل واجهة الخيارات الإضافية.", appLanguage)
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            if (entryMode == 0) showSubjectDialog = true else showStudentDialog = true 
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (entryMode == 0) Icons.Default.Book else Icons.Default.Person,
                                contentDescription = null,
                                tint = BentoPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (entryMode == 0) AppLocalization.tr("select_subject_prompt", "المادة الدراسية المطلوبة 📘", appLanguage) else AppLocalization.tr("select_student_prompt", "الطالب المستهدف 👨‍🎓", appLanguage),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (entryMode == 0) {
                                        activeSubject?.name ?: AppLocalization.tr("choose_subject_ph", "اختر المادة الدراسية...", appLanguage)
                                    } else {
                                        activeStudent?.name ?: AppLocalization.tr("choose_student_ph", "اختر الطالب...", appLanguage)
                                    },
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = BentoText
                                )
                            }
                        }
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val isReadyToStart = if (entryMode == 0) activeSubject != null else activeStudent != null
                Button(
                    onClick = { isGradingActive = true },
                    enabled = isReadyToStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_grading_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoPrimary,
                        disabledContainerColor = BentoPrimary.copy(alpha = 0.4f),
                        contentColor = Color.White,
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppLocalization.tr("start_grading_btn", "البدء في رصد الدرجات 🚀", appLanguage),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // LIST DIALOG: CHOOSE SUBJECT
    if (showSubjectDialog) {
        DialogSelection(
            title = "اختر المادة الدراسية",
            items = availableSubjects,
            itemName = { it.name },
            onItemSelected = {
                viewModel.selectSubject(it.id)
                showSubjectDialog = false
            },
            onDismiss = { showSubjectDialog = false }
        )
    }

    // LIST DIALOG: CHOOSE STUDENT
    if (showStudentDialog) {
        DialogSelection(
            title = "اختر الطالب",
            items = classStudents,
            itemName = { "${classStudents.indexOf(it) + 1} - ${it.name}" },
            onItemSelected = {
                viewModel.selectStudent(it.id)
                showStudentDialog = false
            },
            onDismiss = { showStudentDialog = false }
        )
    }

    if (showActivationPromoDialog) {
        val currentUser by viewModel.currentUser.collectAsState()
        val email = currentUser?.email ?: ""
        val classId = activeClass?.id ?: 0L
        val className = activeClass?.getFormattedName() ?: ""
        val academicYear = activeClass?.academicYear?.ifBlank { "2025-2026" } ?: "2025-2026"

        ActivationPromoDialog(
            email = email,
            classId = classId,
            className = className,
            academicYear = academicYear,
            targetTermName = targetTermNameForActivation ?: "الفصل الثاني",
            isFinalTermOnlyFeatureEnabled = isFinalTermOnlyFeatureEnabled,
            onDismiss = { showActivationPromoDialog = false }
        )
    }

    if (showPullOptionsDialog && activeClass != null) {
        AlertDialog(
            onDismissRequest = { showPullOptionsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "سحب الكشوف والتقارير 📋",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "القسم: ${activeClass.name} | الفصل الدراسي: الفصل $activeTermId",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "تم رصد جميع النتائج بنجاح! اختر نوع الكشف الذي ترغب في طباعته أو تصديره بصيغة PDF:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    // Option 1: سحب كشوف درجات القسم
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🗒️ كشوف درجات طلاب القسم للفصل المحدد (كشفين بالصفحة)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BentoText)
                        Button(
                            onClick = {
                                if (performanceList.isEmpty()) {
                                    Toast.makeText(context, "لا توجد علامات مرصودة بالقسم للمشاركة", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.runWithLoading("جاري تصدير كشوف درجات الطلاب كملف PDF للمشاركة...") {
                                        val html = HtmlReportHelper.generateReportCardsHtml(
                                            context = context,
                                            className = activeClass.name,
                                            performances = performanceList,
                                            subjects = subjects,
                                            activeTermId = activeTermId,
                                            activeClass = activeClass,
                                            viewModel = viewModel
                                        )
                                        val termName = when (activeTermId) {
                                            1 -> "الأول"
                                            2 -> "الثاني"
                                            3 -> "الثالث"
                                            else -> activeTermId.toString()
                                        }
                                        HtmlReportHelper.shareHtmlAsPdf(context, html, "كشوف_درجات_قسم_${activeClass.name}_الفصل_${termName}.pdf", isLandscape = true)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📥 سحب ومشاركة كشوف الدرجات (PDF)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }
                    }

                    // Option 2: سحب لائحة نتائج القسم التفصيلية
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("📊 لائحة النتائج وجدول علامات الفصل التفصيلي للقسم", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BentoText)
                        Button(
                            onClick = {
                                if (performanceList.isEmpty()) {
                                    Toast.makeText(context, "لا توجد علامات مرصودة بالقسم للمشاركة", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.runWithLoading("جاري تصدير لائحة النتائج التفصيلية كملف PDF للمشاركة...") {
                                        val html = HtmlReportHelper.generateDetailedTermLedgerHtml(
                                            context = context,
                                            className = activeClass.name,
                                            performances = performanceList,
                                            subjects = subjects,
                                            activeTermId = activeTermId,
                                            activeClass = activeClass,
                                            viewModel = viewModel
                                        )
                                        val termName = when (activeTermId) {
                                            1 -> "الأول"
                                            2 -> "الثاني"
                                            3 -> "الثالث"
                                            else -> activeTermId.toString()
                                        }
                                        HtmlReportHelper.shareHtmlAsPdf(context, html, "لائحة_النتائج_التفصيلية_قسم_${activeClass.name}_الفصل_${termName}.pdf", isLandscape = true)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📥 سحب ومشاركة كشف النتائج التفصيلي (PDF)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }
                    }

                    if (activeTermId == 3 || activeTermId == 4) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Text(
                            text = if (activeTermId == 4) "🎓 كشوف ونتائج الفصل الأخير فقط والنتائج السنوية:" else "🎓 كشوف ونتائج الفصل الثالث والنتائج السنوية:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // 1. سحب كشوف الدرجات للفصل الثالث/الأخير مع المعدلات و المعدل العام
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(if (activeTermId == 4) "🗒️ كشف درجات الفصل الأخير مع معدلات الفصول السابقة والمعدل العام (كشفين بالصفحة)" else "🗒️ كشف درجات الفصل الثالث مع المعدلات والمعدل العام (كشفين بالصفحة)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BentoText)
                            Button(
                                onClick = {
                                    if (performanceList.isEmpty()) {
                                        Toast.makeText(context, "لا توجد علامات مرصودة بالقسم للمشاركة", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val labelText = if (activeTermId == 4) "الفصل الأخير" else "الفصل الثالث"
                                        viewModel.runWithLoading("جاري تصدير كشوف درجات $labelText كملف PDF للمشاركة...") {
                                            val html = HtmlReportHelper.generateReportCardsHtml(
                                                context = context,
                                                className = activeClass.name,
                                                performances = performanceList,
                                                subjects = subjects,
                                                activeTermId = activeTermId,
                                                activeClass = activeClass,
                                                viewModel = viewModel
                                            )
                                            val fileNameSuffix = if (activeTermId == 4) "الفصل_الأخير_فقط" else "الفصل_الثالث"
                                            HtmlReportHelper.shareHtmlAsPdf(context, html, "كشوف_درجات_${fileNameSuffix}_قسم_${activeClass.name}.pdf", isLandscape = true)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (activeTermId == 4) "📥 سحب ومشاركة كشوف الفصل الأخير (PDF)" else "📥 سحب ومشاركة كشوف الفصل الثالث (PDF)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }

                        // 2. سحب كشوف الدرجات للمعدلات و الرتبه
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📑 كشف درجات المعدلات والرتبة السنوية فقط (3 بالورقة عمودياً)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BentoText)
                            Button(
                                onClick = {
                                    if (classStudents.isEmpty()) {
                                        Toast.makeText(context, "القسم فارغ لا يمكن سحب الكشوف", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.runWithLoading("جاري تصدير كشوف المعدلات السنوية كملف PDF للمشاركة...") {
                                            val html = HtmlReportHelper.generateAveragesOnlyReportCardsHtml(
                                                context = context,
                                                className = activeClass.name,
                                                students = classStudents,
                                                activeClass = activeClass,
                                                viewModel = viewModel
                                            )
                                            HtmlReportHelper.shareHtmlAsPdf(context, html, "كشوف_المعدلات_فقط_قسم_${activeClass.name}.pdf", isLandscape = false)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("📥 سحب ومشاركة كشوف المعدلات السنوية (PDF)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }

                        // 3. سحب كشوف الدرجات للمواد فى جميع الفصول مع المعدل العام و الرتبه
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📊 كشوف نقاط المواد في جميع الفصول مع المعدل العام والرتبة السنوية (كشفان بالصفحة)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BentoText)
                            Button(
                                onClick = {
                                    if (classStudents.isEmpty()) {
                                        Toast.makeText(context, "القسم فارغ لا يمكن سحب الكشوف", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.runWithLoading("جاري تصدير كشوف النقاط السنوية كملف PDF للمشاركة...") {
                                            val html = HtmlReportHelper.generateThreeTermGradesReportCardsHtml(
                                                context = context,
                                                className = activeClass.name,
                                                students = classStudents,
                                                subjects = subjects,
                                                activeClass = activeClass,
                                                viewModel = viewModel
                                            )
                                            HtmlReportHelper.shareHtmlAsPdf(context, html, "كشوف_النقاط_الفصول_الثلاثة_قسم_${activeClass.name}.pdf", isLandscape = true)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("📥 سحب ومشاركة كشوف النقاط السنوية (PDF)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPullOptionsDialog = false }) {
                    Text("إغلاق", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showStudentSelectorInDialog && activeClass != null) {
        DialogSelection(
            title = "اختر الطالب المطلوب",
            items = classStudents,
            itemName = { "${classStudents.indexOf(it) + 1} - ${it.name}" },
            onItemSelected = { student ->
                val studentPerf = performanceList.find { it.student.id == student.id }
                if (studentPerf != null) {
                    val html = HtmlReportHelper.generateReportCardsHtml(
                        context = context,
                        className = activeClass.name,
                        performances = performanceList,
                        subjects = subjects,
                        activeTermId = activeTermId,
                        activeClass = activeClass,
                        viewModel = viewModel,
                        targetStudentId = student.id
                    )
                    if (shareOnSelect) {
                        HtmlReportHelper.shareHtmlAsPdf(context, html, "كشف_درجات_${student.name.replace(" ", "_")}.pdf", isLandscape = true)
                    } else {
                        HtmlReportHelper.printHtml(context, html, "كشف_درجات_${student.name}", isLandscape = true)
                    }
                } else {
                    Toast.makeText(context, "الرجاء التأكد من رصد درجات هذا الطالب", Toast.LENGTH_SHORT).show()
                }
                showStudentSelectorInDialog = false
            },
            onDismiss = { showStudentSelectorInDialog = false }
        )
    }
}

// --- TAB 2: REPORTS & LEDGERS ---
@Composable
fun ReportsTab(
    viewModel: TeacherViewModel,
    classSections: List<ClassSection>,
    subjects: List<Subject>,
    students: List<Student>
) {
    EnhancedReportsTab(viewModel, classSections, subjects, students)
}

/*
    val selectedClassId by viewModel.selectedClassId.collectAsState()
    val activeClass = classSections.find { it.id == selectedClassId }
    val classStudents = students.filter { it.classId == selectedClassId }

    val performanceList by viewModel.currentClassPerformance.collectAsState()
    val activeTermId by viewModel.selectedTermId.collectAsState()

    var reportMode by remember { mutableStateOf(0) } // 0: Student Report Card, 1: Class Full Ledger
    val selectedStudentId by viewModel.selectedStudentId.collectAsState()
    val activeStudentPerf = performanceList.find { it.student.id == selectedStudentId }

    var showClassSelectionDialog by remember { mutableStateOf(false) }
    var showStudentSelectionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "استخراج التقارير وسحب الكشوف 📊",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "شاهد كشوف درجات الطلاب مع احتساب المعدل والرتبة، أو اسحب اللائحة التفصيلية للقسم للفصل المحدد.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Class Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showClassSelectionDialog = true }
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("المجموعة والصف", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = activeClass?.name ?: "الرجاء اختيار مجموعة معينة...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress of grades entry
        val progressPercent = gradingProgress
        val progressColor = if (progressPercent == 100) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
        val progressTrackColor = progressColor.copy(alpha = 0.15f)
        val progressText = if (progressPercent == 100) {
            "اكتمل إدخال النتائج: 100% ✓"
        } else {
            "تقدم إدخال النتائج: $progressPercent%"
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = progressText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = progressTrackColor
            )
        }

        // Term Selector (الفصول الدراسية)
        val isFinalTermOnlyFeatureEnabled by viewModel.isFinalTermOnlyFeatureEnabled.collectAsState()
        val isCardTerm4Active = activeClass?.isTerm4Activated == true
        val isCardTerm3Active = activeClass?.isTerm3Activated == true

        Text(
            text = "تحديد الفصل الدراسي للاستعراض والرصد 📅",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp),
            color = BentoText
        )

        // 1. المستطيل الأخضر للفصول الثلاثة العادية
        if (!isCardTerm4Active) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9).copy(alpha = 0.6f)),
                border = BorderStroke(1.5.dp, Color(0xFF2E7D32))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "فصول السنة العادية 🟢",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            1 to "الفصل الأول ❶",
                            2 to "الفصل الثاني ❷",
                            3 to "الفصل الثالث ❸"
                        ).forEach { (termId, termName) ->
                            val isSelected = activeTermId == termId
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF2E7D32) else Color.White)
                                    .border(1.dp, if (isSelected) Color(0xFF1B5E20) else Color(0xFFA5D6A7), RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.selectTerm(termId)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = termName,
                                        color = if (isSelected) Color.White else Color(0xFF1B5E20),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. المستطيل الأحمر المخصص للفصل الأخير فقط
        if (!isCardTerm3Active && (isFinalTermOnlyFeatureEnabled || isCardTerm4Active)) {
            if (!isCardTerm4Active) {
                Spacer(modifier = Modifier.height(10.dp))
            }
            val isFinalSelected = activeTermId == 4
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE).copy(alpha = 0.7f)),
                border = BorderStroke(1.5.dp, Color(0xFFD32F2F))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isFinalSelected) Color(0xFFD32F2F) else Color.White)
                            .border(1.dp, if (isFinalSelected) Color(0xFFB71C1C) else Color(0xFFEF9A9A), RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.selectTerm(4)
                            }
                            .padding(vertical = 11.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "الفصل الأخير فقط ⭐",
                                color = if (isFinalSelected) Color.White else Color(0xFFC62828),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // الرسالة الصغيرة أسفل خيار الفصل الأخير فقط في مستطيله الأحمر
                    Text(
                        text = "💡 مخصص للوافدين الجدد الذين لم يدرجو نتائج الفصل الأول والثانى",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeClass == null) {
            Spacer(modifier = Modifier.height(32.dp))
            EmptyStateCard(
                title = "لم يتم اختيار أي قسم",
                description = "الرجاء تحديد القسم المراد استخراج الكشوف والتقارير له من الأعلى."
            )
        } else {
            // Mode Selectors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (reportMode == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { reportMode = 0 }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "كشف درجات طالب فردي",
                        color = if (reportMode == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (reportMode == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { reportMode = 1 }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "التقرير التفصيلي العام",
                        color = if (reportMode == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (reportMode == 0) {
                // -- Option 0: Single Report Card --
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStudentSelectionDialog = true }
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("طالب كشف النقاط", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = activeStudentPerf?.student?.name ?: "اختر طالباً...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (activeStudentPerf == null) {
                    EmptyStateCard(
                        title = "الرجاء اختيار الطالب",
                        description = "اختر الطالب المطلوب عرض كشف درجات ومعدله وترتيبه."
                    )
                } else {
                    // Show Beautiful Mauritanian Pattern Report Card
                    val sortedStudents = remember(classStudents) { classStudents.sortedBy { it.id } }
                    val callNumber = remember(sortedStudents, activeStudentPerf) {
                        if (activeStudentPerf != null) {
                            sortedStudents.indexOfFirst { it.id == activeStudentPerf.student.id } + 1
                        } else {
                            1
                        }
                    }

                    val remarkAvg = if (activeTermId == 3 || activeTermId == 4) {
                        viewModel.calculateStudentGeneralAverage(activeStudentPerf.student.id, activeClass.id)
                    } else {
                        activeStudentPerf.averageScore
                    }
                    val remarkText = when {
                        remarkAvg >= 16.0 -> "ممتاز"
                        remarkAvg >= 14.0 -> "جيد جداً"
                        remarkAvg >= 12.0 -> "جيد"
                        remarkAvg >= 10.0 -> "حسن"
                        remarkAvg >= 8.0 -> "مقبول"
                        else -> "ضعيف"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, Color.Black),
                        elevation = CardDefaults.cardElevation(3.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            // --- Dual-sided official header in Arabic ---
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                // Right Column (aligned right)
                                Column(
                                    modifier = Modifier.weight(1.2f),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text("الجمهورية الإسلامية الموريتانية", fontSize = 8.5.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                    Text("وزارة التربية وإصلاح النظام التعليمي", fontSize = 7.5.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    Text("الإدارة الجهوية للتربية: ${activeClass.wilaya.ifBlank { "غوركل" }}", fontSize = 7.5.sp, color = Color.Black)
                                    Text("مفتشية مقاطعة: ${activeClass.moughataa.ifBlank { "لكصيبه 1" }}", fontSize = 7.5.sp, color = Color.Black)
                                    Text("مدرسة: ${activeClass.schoolName.ifBlank { "الطلحايه 1" }}", fontSize = 7.5.sp, color = Color.Black)
                                }

                                // Center Column (Emblem + Title)
                                Column(
                                    modifier = Modifier.weight(0.8f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    MauritaniaSeal(
                                        modifier = Modifier
                                            .size(75.dp)
                                            .padding(bottom = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (activeTermId) {
                                            1 -> "نتائج الامتحان الأول"
                                            2 -> "نتائج الامتحان الثاني"
                                            else -> "نتائج الامتحان الثالث"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // Left Column (aligned left)
                                Column(
                                    modifier = Modifier.weight(1.0f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text("شرف - إخاء - عدالة", fontSize = 8.5.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                    Text("السنة الدراسية: ${activeClass.academicYear.ifBlank { "2025 - 2026" }}", fontSize = 7.5.sp, color = Color.Black)
                                    Text("القسم: ${HtmlReportHelper.getDisplayClassName(activeClass)}", fontSize = 7.5.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    Text("الفصل: ${when(activeTermId) { 1 -> "الأول" 2 -> "الثاني" else -> "الثالث" }}", fontSize = 7.5.sp, color = Color.Black)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // --- Student ID Block ---
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "الاسم الكامل: ${activeStudentPerf.student.name}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "رقم النداء: $callNumber",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "الرقم المدرسي: ${activeStudentPerf.student.schoolId}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // --- Official Grading Table ---
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.Black)
                                    .height(androidx.compose.foundation.layout.IntrinsicSize.Max)
                            ) {
                                // Right 75%: Subjects, Scores, Totals, and Averages
                                Column(
                                    modifier = Modifier.weight(0.75f)
                                ) {
                                    // Header
                                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF2F2F2))) {
                                        Box(
                                            modifier = Modifier
                                                .weight(2f)
                                                .border(0.5.dp, Color.Black)
                                                .padding(vertical = 4.dp, horizontal = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("المادة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(0.5.dp, Color.Black)
                                                .padding(vertical = 4.dp, horizontal = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("النقاط", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }

                                    // Content
                                    var totalMax = 0
                                    var totalScoreValue = 0.0
                                    subjects.forEach { sub ->
                                        val score = activeStudentPerf.gradesMap[sub.id]
                                        val scoreStr = if (score != null) HtmlReportHelper.formatCleanNumber(score) else "-"
                                        totalMax += sub.maxPoints
                                        totalScoreValue += (score ?: 0.0)

                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(2f)
                                                    .border(0.5.dp, Color.Black)
                                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Text(sub.name, fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp), color = Color.Black)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(0.5.dp, Color.Black)
                                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${sub.maxPoints} / $scoreStr", fontSize = 10.sp, color = Color.Black, style = androidx.compose.ui.text.TextStyle(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr))
                                            }
                                        }
                                    }

                                    // Total (المجموع)
                                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFFAFAFA))) {
                                        Box(
                                            modifier = Modifier
                                                .weight(2f)
                                                .border(0.5.dp, Color.Black)
                                                .padding(vertical = 4.dp, horizontal = 6.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text("المجموع", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp), color = Color.Black)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(0.5.dp, Color.Black)
                                                .padding(vertical = 4.dp, horizontal = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("$totalMax / ${HtmlReportHelper.formatCleanNumber(totalScoreValue)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, style = androidx.compose.ui.text.TextStyle(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr))
                                        }
                                    }

                                    // Averages block
                                    if (activeTermId == 3 || activeTermId == 4) {
                                        val avg1 = viewModel.calculateStudentTermAverage(activeStudentPerf.student.id, activeClass.id, 1)
                                        val avg2 = viewModel.calculateStudentTermAverage(activeStudentPerf.student.id, activeClass.id, 2)
                                        val avg3 = activeStudentPerf.averageScore
                                        val generalAverage = viewModel.calculateStudentGeneralAverage(activeStudentPerf.student.id, activeClass.id)
                                        val generalRank = viewModel.calculateStudentGeneralRank(activeStudentPerf.student.id, activeClass.id)

                                        // Exam 1 Avg
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Box(modifier = Modifier.weight(2f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.CenterStart) {
                                                Text("معدل الامتحان الأول", fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp), color = Color.Black)
                                            }
                                            Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                                                Text("20 / ${HtmlReportHelper.formatCleanNumber(avg1)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                        // Exam 2 Avg
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Box(modifier = Modifier.weight(2f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.CenterStart) {
                                                Text("معدل الامتحان الثاني", fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp), color = Color.Black)
                                            }
                                            Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                                                Text("20 / ${HtmlReportHelper.formatCleanNumber(avg2)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                        // Exam 3 Avg
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Box(modifier = Modifier.weight(2f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.CenterStart) {
                                                Text("معدل الامتحان الثالث", fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp), color = Color.Black)
                                            }
                                            Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                                                Text("20 / ${HtmlReportHelper.formatCleanNumber(avg3)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                        // General Average (المعدل العام)
                                        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE8F5E9))) {
                                            Box(modifier = Modifier.weight(2f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.CenterStart) {
                                                Text("المعدل العام", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp), color = Color.Black)
                                            }
                                            Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                                                Text("20 / ${HtmlReportHelper.formatCleanNumber(generalAverage)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                        // Final status
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Box(modifier = Modifier.weight(2f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.CenterStart) {
                                                Text("القرار أو الملاحظة", fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp), color = Color.Black)
                                            }
                                            Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                                                Text(if (generalAverage >= 10.0) "ناجح" else "راسب", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                        // Rank (الرتبة)
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Box(modifier = Modifier.weight(2f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.CenterStart) {
                                                Text("الرتبة", fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp), color = Color.Black)
                                            }
                                            Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                                                Text(if (generalRank > 0) "$generalRank" else "-", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                    } else {
                                        val avg = activeStudentPerf.averageScore
                                        val rank = activeStudentPerf.rank

                                        // Average
                                        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE8F5E9))) {
                                            Box(modifier = Modifier.weight(2f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.CenterStart) {
                                                Text("المعدل", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp), color = Color.Black)
                                            }
                                            Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                                                Text("20 / ${HtmlReportHelper.formatCleanNumber(avg)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                        // Final status
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Box(modifier = Modifier.weight(2f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.CenterStart) {
                                                Text("النتيجة النهائية", fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp), color = Color.Black)
                                            }
                                            Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                                                Text(if (avg >= 10.0) "ناجح" else "راسب", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                        // Rank (الرتبة)
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Box(modifier = Modifier.weight(2f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.CenterStart) {
                                                Text("الرتبة", fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp), color = Color.Black)
                                            }
                                            Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.Black).padding(vertical = 4.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                                                Text(if (rank > 0) "$rank" else "-", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                    }
                                }

                                // Left 25%: Remarks Column (الملاحظات)
                                Column(
                                    modifier = Modifier
                                        .weight(0.25f)
                                        .fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Header
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(0.5.dp, Color.Black)
                                            .background(Color(0xFFF2F2F2))
                                            .padding(vertical = 4.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("الملاحظات", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                    // Value
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .border(0.5.dp, Color.Black)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = remarkText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // --- Footer Signatures ---
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp, start = 8.dp, end = 8.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("المدير", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("المعلم", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                            // Share / Copy buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val text = viewModel.exportStudentReportCardText(
                                            activeClass.name,
                                            activeStudentPerf.student.name,
                                            performanceList,
                                            activeStudentPerf.student.id,
                                            subjects,
                                            activeTermId
                                        )
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("كشف الدرجات", text)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "تم نسخ كشف الدرجات للحافظة", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "نسخ")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("نسخ الكشف", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val text = viewModel.exportStudentReportCardText(
                                            activeClass.name,
                                            activeStudentPerf.student.name,
                                            performanceList,
                                            activeStudentPerf.student.id,
                                            subjects,
                                            activeTermId
                                        )
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, text)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "مشاركة الكشف"))
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "مشاركة")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("مشاركة الكشف", fontWeight = FontWeight.Bold)
                                }
                            }
                }
            } else {
                // -- Option 1: GENERAL CLASS LEDGER --
                // Share Ledger buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val text = viewModel.exportClassLedgerText(activeClass.name, performanceList, subjects, activeTermId)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(intent, "سحب كشف النتائج التفصيلية"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_class_ledger_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة كشف تفصيلي", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            // Let's generate a clean CSV formatted spreadsheet of Class Marks!
                            val csvBuilder = java.lang.StringBuilder()
                            csvBuilder.append("اسم الطالب,المعدل العام,الرتبة في الصف")
                            subjects.forEach { csvBuilder.append(",").append(it.name) }
                            csvBuilder.append("\n")

                            performanceList.sortedWith(
                                compareBy<StudentPerformance> { if (it.rank == 0) Int.MAX_VALUE else it.rank }
                                    .thenBy { it.student.name }
                            ).forEach { perf ->
                                csvBuilder.append(perf.student.name).append(",")
                                csvBuilder.append(String.format(Locale.US, "%.2f", perf.averageScore)).append(",")
                                csvBuilder.append(if (perf.rank > 0) perf.rank.toString() else "-")
                                subjects.forEach { sub ->
                                    val mark = perf.gradesMap[sub.id]
                                    csvBuilder.append(",").append(mark?.let { String.format(Locale.US, "%.1f", it) } ?: "-")
                                }
                                csvBuilder.append("\n")
                            }

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/comma-separated-values"
                                putExtra(Intent.EXTRA_TEXT, csvBuilder.toString())
                            }
                            context.startActivity(Intent.createChooser(intent, "تصدير كجدول Excel / CSV"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = "CSV Excel")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة كملف CSV", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Ledger List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "لائحة النتائج والترتيب للطلاب (${performanceList.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (performanceList.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "لا توجد علامات مسجلة للطلاب",
                                description = "رتب المعلم للقسم فارغ. ارجع لرصد النقاط لتنقيط المواد الدراسية ومتابعة المعدل."
                            )
                        }
                    } else {
                        // Sort by rank ascending (first student of the class displayed on top, unranked at the bottom)
                        val sortedLedgerList = performanceList.sortedWith(
                            compareBy<StudentPerformance> { if (it.rank == 0) Int.MAX_VALUE else it.rank }
                                .thenBy { it.student.name }
                        )
                        items(sortedLedgerList) { perf ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Rank visual indicator badge
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    color = when (perf.rank) {
                                                        1 -> Color(0xFFFFD700) // Gold
                                                        2 -> Color(0xFFC0C0C0) // Silver
                                                        3 -> Color(0xFFCD7F32) // Bronze
                                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (perf.rank > 0) "${perf.rank}" else "-",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (perf.rank in 1..3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = perf.student.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            // Quick small breakdown of grades in row style
                                            val briefGrades = subjects.mapNotNull { sub ->
                                                val m = perf.gradesMap[sub.id]
                                                if (m != null) "${sub.name}: ${HtmlReportHelper.formatCleanNumber(m)}" else null
                                            }.joinToString(" | ")

                                            if (briefGrades.isNotBlank()) {
                                                Text(
                                                    text = briefGrades,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            } else {
                                                Text(
                                                    text = "لا توجد درجات مسجلة بعد",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        val totalMaxPoints = subjects.sumOf { it.maxPoints }
                                        val totalAchievedPoints = perf.gradesMap.values.sum()
                                        Text(
                                            text = "المجموع: ${HtmlReportHelper.formatCleanNumber(totalAchievedPoints)} / $totalMaxPoints",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "المعدل: ${HtmlReportHelper.formatCleanNumber(perf.averageScore)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.primary
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

    // DIALOG: CHOOSE CLASS SECTION
    if (showClassSelectionDialog) {
        DialogSelection(
            title = "انتقاء القسم الدراسي",
            items = classSections,
            itemName = { it.name },
            onItemSelected = {
                viewModel.selectClassWithLockCheck(it.id)
                viewModel.selectStudent(null) // reset student card selection when class changes
                showClassSelectionDialog = false
            },
            onDismiss = { showClassSelectionDialog = false }
        )
    }

    // DIALOG: CHOOSE INDIVIDUAL STUDENT FOR REPORT CARD
    if (showStudentSelectionDialog) {
        DialogSelection(
            title = "انتقاء الطالب",
            items = classStudents,
            itemName = { it.name },
            onItemSelected = {
                viewModel.selectStudent(it.id)
                showStudentSelectionDialog = false
            },
            onDismiss = { showStudentSelectionDialog = false }
        )
    }
}
*/

// --- TAB 3: SUBJECTS MANAGEMENT SETUP ---
@Composable
fun SubjectsTab(
    viewModel: TeacherViewModel,
    subjects: List<Subject>,
    bypassLogin: Boolean = false,
    initialTab: Int = 2,
    targetEmail: String? = null,
    targetClassId: Long? = null,
    onClose: (() -> Unit)? = null
) {
    var isLoggedIn by remember { mutableStateOf(bypassLogin) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        // LOGIN FORM FOR THE CLASS MANAGER SECTION
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (onClose != null) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoPrimary),
                    border = BorderStroke(1.dp, BentoPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("العودة إلى بوابة تسجيل المعلمين 🏫", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BentoGrayOutline, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(BentoSecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = BentoSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "مدير الأقسام",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = BentoPrimary
                    )

                    Text(
                        text = "هذه الصفحة محمية وخاصة بمدير الأقسام فقط. الرجاء إدخال اسم المستخدم وكلمة السر.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = BentoPrimaryDesc
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { 
                            username = it
                            loginError = false
                        },
                        label = { Text("اسم المستخدم") },
                        placeholder = { Text("") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_login_username"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = BentoPrimary)
                        }
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            loginError = false
                        },
                        label = { Text("كلمة السر") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_login_password"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = BentoPrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = BentoPrimary
                                )
                            }
                        }
                    )

                    if (loginError) {
                        Text(
                            text = "اسم المستخدم أو كلمة السر غير صحيحة!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            val currentEmailForLogin = viewModel.currentUser.value?.email?.trim()?.lowercase() ?: ""
                            if (currentEmailForLogin == "elyedalimoctar@gmail.com") {
                                isLoggedIn = true
                                loginError = false
                            } else {
                                loginError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_login_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("تسجيل الدخول الآمن", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    } else {
        // ADMIN DASHBOARD
        var adminTab by remember { mutableStateOf(2) } // 2: الإحصائيات, 4: إظهار الفصل الأخير

        var adminClassToDeleteFor9999 by remember { mutableStateOf<com.example.data.models.ClassSection?>(null) }
        var adminConfirmDeleteClassCode by remember { mutableStateOf("") }

        val context = LocalContext.current
        val scope = rememberCoroutineScope()


        val sharedPrefs = remember(context) { context.getSharedPreferences("teacher_settings_prefs", android.content.Context.MODE_PRIVATE) }
        var subjectName by remember { mutableStateOf("") }
        var selectedLevel by remember { mutableStateOf(sharedPrefs.getInt("last_subject_level", 1)) }
        var maxPoints by remember { mutableStateOf("20") }
        var editingSubjectId by remember { mutableStateOf<Long?>(null) }
        var filterLevelTab by remember { mutableStateOf(sharedPrefs.getInt("last_filter_level", 1)) }

        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        val allClasses by viewModel.allClassesForStats.collectAsState()

        LaunchedEffect(isLoggedIn) {
            if (isLoggedIn) {
                viewModel.loadStatsData()
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "صفحة مدير الأقسام 🛠️",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = BentoPrimary
                        )
                        Text(
                            text = "إضافة وتعديل كل مواد الأقسام ومجموع نقاطها.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoPrimaryDesc
                        )
                    }
                    Button(
                        onClick = { 
                            isLoggedIn = false
                            username = ""
                            password = ""
                            onClose?.invoke()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.1f),
                            contentColor = Color.Red
                        )
                    ) {
                        Text("خروج", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("الإحصائيات 📊" to 2).forEach { (title, idx) ->
                            val isSel = adminTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) BentoPrimary else BentoLightLavender)
                                    .clickable { adminTab = idx },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSel) Color.White else BentoText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }


            if (adminTab == 2) {
                item {
                    AdminStatsTabContent(viewModel = viewModel)
                }
            }





            item {
                AppFooter()
            }
        }







        Unit
    }
}

@Composable
fun PersonalClassSubjectsEditor(
    viewModel: TeacherViewModel,
    activeClass: ClassSection,
    onClose: () -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()
    val allCustomizations by viewModel.customizations.collectAsState()
    val allGradesForHide by viewModel.grades.collectAsState()
    val allStudentsForHide by viewModel.students.collectAsState()
    var subjectToHideConfirm by remember { mutableStateOf<Subject?>(null) }
    var subjectToHideCount by remember { mutableStateOf(0) }
    
    val targetLevel = activeClass.level
    val levelSubjects = remember(subjects, allCustomizations, targetLevel, activeClass.id) {
        viewModel.buildClassSubjects(activeClass.id, targetLevel, subjects, allCustomizations)
    }
    val classCusts = remember(allCustomizations, activeClass.id) {
        allCustomizations.filter { it.classId == activeClass.id }.associateBy { it.subjectId }
    }
    val hiddenSubjects = remember(subjects, allCustomizations, targetLevel, activeClass.id) {
        val hiddenIds = allCustomizations.filter { it.classId == activeClass.id && it.isHidden }.map { it.subjectId }.toSet()
        subjects.filter { it.level == targetLevel && (it.classId == 0L || it.classId == activeClass.id) && it.id in hiddenIds }
    }
    
    var showEditDialog by remember { mutableStateOf<Subject?>(null) }
    var editName by remember { mutableStateOf("") }
    var editMaxPoints by remember { mutableStateOf("20") }
    var subjectToDeleteFor9999 by remember { mutableStateOf<Subject?>(null) }
    var confirmDeleteSubjectCode by remember { mutableStateOf("") }
    var resetSubject by remember { mutableStateOf<Subject?>(null) }
    var resetOldPoints by remember { mutableStateOf(0) }
    var resetNewPoints by remember { mutableStateOf(0) }
    var resetAffectedCount by remember { mutableStateOf(0) }
    var rescaleSubject by remember { mutableStateOf<Subject?>(null) }
    var rescaleNewName by remember { mutableStateOf("") }
    var rescaleOldPoints by remember { mutableStateOf(0) }
    var rescaleNewPoints by remember { mutableStateOf(0) }
    var rescaleAffectedCount by remember { mutableStateOf(0) }
    
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectName by remember { mutableStateOf("") }
    var newSubjectMaxPoints by remember { mutableStateOf("20") }
    
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BentoPrimary.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تعديل مواد القسم: ${activeClass.getFormattedName()} 📚",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = BentoPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "يمكنك تعديل أسماء المواد والدرجات القصوى لقسمك بشكل شخصي لتظهر في الكشوف والتقارير.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoPrimaryDesc
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .background(BentoPrimary.copy(alpha = 0.1f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = BentoPrimary
                    )
                }
            }
        }
        
        Button(
            onClick = {
                newSubjectName = ""
                newSubjectMaxPoints = "20"
                showAddSubjectDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة مادة جديدة لهذا القسم فقط ➕", fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        if (levelSubjects.isEmpty()) {
            EmptyStateCard(
                title = "لا توجد مواد مضافة لهذا المستوى",
                description = "يمكنك إضافة مواد مخصصة للقسم بالنقر على الزر أعلاه."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(levelSubjects) { subject ->
                    val cust = classCusts[subject.id]
                    val currentName = cust?.name ?: subject.name
                    val currentMaxPoints = cust?.maxPoints ?: subject.maxPoints
                    val isCustomized = cust != null
                    val isPersonalCustomSubject = subject.classId != 0L
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isPersonalCustomSubject) BentoSecondary.copy(alpha = 0.5f) 
                                        else if (isCustomized) BentoPrimary.copy(alpha = 0.3f) 
                                        else BentoGrayOutline,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPersonalCustomSubject) BentoSecondaryContainer.copy(alpha = 0.3f)
                                             else if (isCustomized) BentoPrimary.copy(alpha = 0.05f) 
                                             else Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentName,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = BentoPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (isPersonalCustomSubject) {
                                        Box(
                                            modifier = Modifier
                                                .background(BentoSecondary, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "مادة مخصصة جديدة",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else if (isCustomized) {
                                        Box(
                                            modifier = Modifier
                                                .background(BentoPrimary, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "تم تعديلها",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "الدرجة القصوى: $currentMaxPoints",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BentoPrimaryDesc
                                )
                            }
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isPersonalCustomSubject) {
                                    IconButton(
                                        onClick = {
                                            subjectToDeleteFor9999 = subject
                                            showEditDialog = null
                                        },
                                        modifier = Modifier
                                            .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                                            .size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف المادة",
                                            tint = Color.Red
                                        )
                                    }
                                } else {
                                    if (isCustomized) {
                                        IconButton(
                                            onClick = {
                                                val custNow = allCustomizations.find { it.classId == activeClass.id && it.subjectId == subject.id }
                                                val oldPtsNow = custNow?.maxPoints ?: subject.maxPoints
                                                val newPtsNow = subject.maxPoints
                                                val classStudentIdsNow = allStudentsForHide.filter { it.classId == activeClass.id }.map { it.id }.toSet()
                                                val affectedNow = allGradesForHide.count { it.subjectId == subject.id && it.studentId in classStudentIdsNow }
                                                if (oldPtsNow != newPtsNow && affectedNow > 0) {
                                                    resetSubject = subject
                                                    resetOldPoints = oldPtsNow
                                                    resetNewPoints = newPtsNow
                                                    resetAffectedCount = affectedNow
                                                } else {
                                                    viewModel.removeSubjectCustomization(activeClass.id, subject.id)
                                                    Toast.makeText(context, "تمت استعادة الاسم والدرجة الافتراضية بنجاح", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier
                                                .background(Color.DarkGray.copy(alpha = 0.1f), CircleShape)
                                                .size(38.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "إلغاء التعديل",
                                                tint = Color.DarkGray
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            val classStudentIds = allStudentsForHide.filter { it.classId == activeClass.id }.map { it.id }.toSet()
                                            val gradesCount = allGradesForHide.count { it.subjectId == subject.id && it.studentId in classStudentIds }
                                            if (gradesCount > 0) {
                                                subjectToHideCount = gradesCount
                                                subjectToHideConfirm = subject
                                            } else {
                                            viewModel.hideSubjectInClass(activeClass.id, subject.id) {
                                                Toast.makeText(context, "تم إخفاء المادة من هذا القسم", Toast.LENGTH_SHORT).show()
                                            }
                                            }
                                        },
                                        modifier = Modifier
                                            .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                                            .size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VisibilityOff,
                                            contentDescription = "إخفاء المادة من هذا القسم",
                                            tint = Color.Red
                                        )
                                    }
                                }
                                
                                IconButton(
                                    onClick = {
                                        editName = currentName
                                        editMaxPoints = currentMaxPoints.toString()
                                        showEditDialog = subject
                                    },
                                    modifier = Modifier
                                        .background(BentoPrimary.copy(alpha = 0.1f), CircleShape)
                                        .size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "تعديل المادة",
                                        tint = BentoPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                if (hiddenSubjects.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "مواد مخفية في هذا القسم (${hiddenSubjects.size})",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray,
                                    fontSize = 14.sp
                                )
                                hiddenSubjects.forEach { hiddenSub ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = hiddenSub.name, color = Color.Gray, fontSize = 14.sp)
                                        TextButton(onClick = {
                                            viewModel.unhideSubjectInClass(activeClass.id, hiddenSub.id) {
                                                Toast.makeText(context, "تمت إعادة المادة إلى هذا القسم", Toast.LENGTH_SHORT).show()
                                            }
                                        }) {
                                            Text("إرجاع", color = BentoPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("إضافة مادة جديدة لقسمك 📚", fontWeight = FontWeight.Bold, color = BentoPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = newSubjectName,
                        onValueChange = { newSubjectName = it },
                        label = { Text("اسم المادة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSubjectMaxPoints,
                        onValueChange = { newSubjectMaxPoints = it },
                        label = { Text("الدرجة القصوى (مثال: 20)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val points = newSubjectMaxPoints.toIntOrNull() ?: 20
                        if (newSubjectName.isNotBlank()) {
                            viewModel.addSubject(
                                name = newSubjectName,
                                level = activeClass.level,
                                maxPoints = points,
                                classId = activeClass.id,
                                onSuccess = {
                                    showAddSubjectDialog = false
                                    Toast.makeText(context, "تمت إضافة المادة بنجاح", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { err ->
                                    showAddSubjectDialog = false
                                    Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "الرجاء إدخال اسم المادة", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) {
                    Text("إضافة وحفظ", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("إلغاء", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
    
    if (showEditDialog != null) {
        val subject = showEditDialog!!
        val isPersonalCustomSubject = subject.classId != 0L
        
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("تعديل معلومات المادة ✏️", fontWeight = FontWeight.Bold, color = BentoPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("اسم المادة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editMaxPoints,
                        onValueChange = { editMaxPoints = it },
                        label = { Text("الدرجة القصوى") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val points = editMaxPoints.toIntOrNull() ?: 20
                        val oldCustPre = allCustomizations.find { it.classId == activeClass.id && it.subjectId == subject.id }
                        val oldPointsPre = oldCustPre?.maxPoints ?: subject.maxPoints
                        val classStudentIdsPre = allStudentsForHide.filter { it.classId == activeClass.id }.map { it.id }.toSet()
                        val affectedCountPre = allGradesForHide.count { it.subjectId == subject.id && it.studentId in classStudentIdsPre }
                        if (editName.isNotBlank() && points != oldPointsPre && affectedCountPre > 0) {
                            rescaleSubject = subject
                            rescaleNewName = editName
                            rescaleOldPoints = oldPointsPre
                            rescaleNewPoints = points
                            rescaleAffectedCount = affectedCountPre
                            showEditDialog = null
                        } else if (editName.isNotBlank()) {
                            val oldCust = allCustomizations.find { it.classId == activeClass.id && it.subjectId == subject.id }
                            val oldPoints = oldCust?.maxPoints ?: subject.maxPoints
                            if (isPersonalCustomSubject) {
                                viewModel.updateSubject(
                                    id = subject.id,
                                    name = editName,
                                    level = subject.level,
                                    maxPoints = points,
                                    classId = activeClass.id,
                                    onSuccess = {
                                        showEditDialog = null
                                        viewModel.rescaleGradesForSubjectInClass(
                                            classId = activeClass.id,
                                            subjectId = subject.id,
                                            oldMaxPoints = oldPoints,
                                            newMaxPoints = points
                                        ) { changed ->
                                            if (changed > 0) {
                                                Toast.makeText(context, "تم تعديل المادة وتحويل $changed درجة إلى السلم الجديد", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "تم تعديل المادة بنجاح", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    onFailure = { err ->
                                        showEditDialog = null
                                        Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                viewModel.saveSubjectCustomization(
                                    classId = activeClass.id,
                                    subjectId = subject.id,
                                    name = editName,
                                    maxPoints = points
                                )
                                showEditDialog = null
                                viewModel.rescaleGradesForSubjectInClass(
                                    classId = activeClass.id,
                                    subjectId = subject.id,
                                    oldMaxPoints = oldPoints,
                                    newMaxPoints = points
                                ) { changed ->
                                    if (changed > 0) {
                                        Toast.makeText(context, "تم تعديل المادة وتحويل $changed درجة إلى السلم الجديد", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "تم تعديل المادة بنجاح", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, "يرجى كتابة اسم المادة", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) {
                    Text("حفظ التعديلات", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (rescaleSubject != null) {
        val rSubject = rescaleSubject!!
        val rIsPersonal = rSubject.classId != 0L
        AlertDialog(
            onDismissRequest = { rescaleSubject = null },
            title = { Text("تنبيه: ستتحوّل الدرجات ⚠️", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("غيّرت الدرجة القصوى لمادة (${rSubject.name}) من $rescaleOldPoints إلى $rescaleNewPoints.")
                    Text("سيؤدي هذا إلى تحويل $rescaleAffectedCount درجة مرصودة لتلاميذ هذا القسم إلى السلم الجديد.")
                    Text("يمكنك الرجوع لاحقاً بإعادة الدرجة القصوى إلى $rescaleOldPoints، فتعود الدرجات كما كانت.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rName = rescaleNewName
                        val rNew = rescaleNewPoints
                        val rOld = rescaleOldPoints
                        rescaleSubject = null
                        if (rIsPersonal) {
                            viewModel.updateSubject(
                                id = rSubject.id,
                                name = rName,
                                level = rSubject.level,
                                maxPoints = rNew,
                                classId = activeClass.id,
                                onSuccess = {
                                    viewModel.rescaleGradesForSubjectInClass(
                                        classId = activeClass.id,
                                        subjectId = rSubject.id,
                                        oldMaxPoints = rOld,
                                        newMaxPoints = rNew
                                    ) { changed ->
                                        Toast.makeText(context, "تم تعديل المادة وتحويل $changed درجة إلى السلم الجديد", Toast.LENGTH_LONG).show()
                                    }
                                },
                                onFailure = { err ->
                                    Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            viewModel.saveSubjectCustomization(
                                classId = activeClass.id,
                                subjectId = rSubject.id,
                                name = rName,
                                maxPoints = rNew
                            )
                            viewModel.rescaleGradesForSubjectInClass(
                                classId = activeClass.id,
                                subjectId = rSubject.id,
                                oldMaxPoints = rOld,
                                newMaxPoints = rNew
                            ) { changed ->
                                Toast.makeText(context, "تم تعديل المادة وتحويل $changed درجة إلى السلم الجديد", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) {
                    Text("متابعة التحويل", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rescaleSubject = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (resetSubject != null) {
        val resSubject = resetSubject!!
        AlertDialog(
            onDismissRequest = { resetSubject = null },
            title = { Text("تنبيه: ستتحوّل الدرجات ⚠️", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ستعود مادة (${resSubject.name}) إلى اسمها ودرجتها الافتراضية، فتتغير الدرجة القصوى من $resetOldPoints إلى $resetNewPoints.")
                    Text("وسيتم تحويل $resetAffectedCount درجة مرصودة لتلاميذ هذا القسم إلى السلم الجديد حتى تبقى النتائج صحيحة.")
                    Text("يمكنك الرجوع لاحقاً بتعديل المادة وإعادة الدرجة القصوى إلى $resetOldPoints.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rOld = resetOldPoints
                        val rNew = resetNewPoints
                        val rId = resSubject.id
                        resetSubject = null
                        viewModel.removeSubjectCustomization(activeClass.id, rId)
                        viewModel.rescaleGradesForSubjectInClass(
                            classId = activeClass.id,
                            subjectId = rId,
                            oldMaxPoints = rOld,
                            newMaxPoints = rNew
                        ) { changed ->
                            Toast.makeText(context, "تمت الاستعادة وتحويل $changed درجة إلى السلم الجديد", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) {
                    Text("متابعة الاستعادة", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { resetSubject = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (subjectToDeleteFor9999 != null) {
        val subject = subjectToDeleteFor9999!!
        AlertDialog(
            onDismissRequest = { 
                subjectToDeleteFor9999 = null
                confirmDeleteSubjectCode = ""
            },
            title = { Text("تأكيد حذف المادة ⚠️", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("هل أنت متأكد من حذف المادة (${subject.name}) نهائياً؟ سيؤدي هذا الإجراء إلى حذف كافة الدرجات المسجلة للتلاميذ في هذه المادة بشكل كامل ونهائي ولا يمكن استرجاعها!")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ادخل الرقم 9999 لحذف المادة في الخانة أدناه:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD32F2F)
                    )
                    OutlinedTextField(
                        value = confirmDeleteSubjectCode,
                        onValueChange = { confirmDeleteSubjectCode = it },
                        placeholder = { Text("أدخل 9999 هنا") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                }
            },
            confirmButton = {
                if (confirmDeleteSubjectCode == "9999") {
                    TextButton(
                        onClick = {
                            viewModel.deleteSubject(
                                id = subject.id,
                                onSuccess = {
                                    Toast.makeText(context, "تم حذف المادة بنجاح", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { err ->
                                    Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                                }
                            )
                            subjectToDeleteFor9999 = null
                            confirmDeleteSubjectCode = ""
                        }
                    ) {
                        Text("نعم، احذف المادة", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        subjectToDeleteFor9999 = null
                        confirmDeleteSubjectCode = ""
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }

    subjectToHideConfirm?.let { sub ->
        AlertDialog(
            onDismissRequest = { subjectToHideConfirm = null },
            title = { Text("إخفاء المادة من هذا القسم", fontWeight = FontWeight.Bold) },
            text = {
                Text("هذه المادة فيها $subjectToHideCount درجة مسجلة في هذا القسم. الإخفاء لا يحذف الدرجات، وتعود كما هي إذا أرجعت المادة.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.hideSubjectInClass(activeClass.id, sub.id) {
                        Toast.makeText(context, "تم إخفاء المادة من هذا القسم", Toast.LENGTH_SHORT).show()
                    }
                    subjectToHideConfirm = null
                }) {
                    Text("إخفاء", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToHideConfirm = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

// --- COMMON VISUAL COMPONENTS ---

@Composable
fun EmptyStateCard(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Dialog-based item picking selection interface (greatly superior to dropdown menus on compact mobile screens)
@Composable
fun <T> DialogSelection(
    title: String,
    items: List<T>,
    itemName: (T) -> String,
    onItemSelected: (T) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = items.filter { itemName(it).contains(searchQuery, ignoreCase = true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 450.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optional Search field if list is somewhat large (e.g. > 5 items)
                if (items.size > 5) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (filteredItems.isEmpty()) {
                        item {
                            Text(
                                text = "لا توجد نتائج مطابقة.",
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(filteredItems) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onItemSelected(item) }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = itemName(item),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (item is ClassSection) {
                                        val paidTerms = item.getPaidTerms()
                                        val isAnyPaid = paidTerms.isNotEmpty()
                                        val badgeText = if (isAnyPaid) "مدفوع (${paidTerms.joinToString("، ")})" else "غير مدفوع"
                                        val badgeColor = if (isAnyPaid) Color(0xFF2E7D32) else Color(0xFF616161)
                                        val badgeBg = if (isAnyPaid) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
                                        Box(
                                            modifier = Modifier
                                                .background(badgeBg, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = badgeText,
                                                color = badgeColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportExportActionCard(
    title: String,
    description: String,
    containerColor: Color,
    onPrintClick: suspend () -> Unit,
    onShareClick: suspend () -> Unit,
    viewModel: TeacherViewModel,
    printButtonText: String = "📥 سحب ومشاركة كملف PDF",
    shareButtonText: String = "💚 مشاركة كملف PDF بالواتساب",
    printMessage: String = "جاري سحب وتصدير ملف الـ PDF للمشاركة...",
    shareMessage: String = "جاري سحب وتصدير ملف الـ PDF للمشاركة...",
    showPrintButton: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Prominent Action Buttons in a vertical stack to prevent text clipping and maximize clarity
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showPrintButton) {
                    Button(
                        onClick = {
                            viewModel.runWithLoading(printMessage) {
                                onShareClick() // Trigger PDF sharing instead of system print
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(printButtonText, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.runWithLoading(shareMessage) {
                            onShareClick()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), // Beautiful rich WhatsApp Green
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(shareButtonText, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                    }
                }
            }

            // Helper Description at the bottom - removed as requested to avoid overlapping text
        }
    }
}

@Composable
fun EnhancedReportsTab(
    viewModel: TeacherViewModel,
    classSections: List<ClassSection>,
    subjects: List<Subject>,
    students: List<Student>
) {
    val selectedClassId by viewModel.selectedClassId.collectAsState()
    val activeClass = classSections.find { it.id == selectedClassId }
    val classStudents = remember(students, selectedClassId) { students.filter { it.classId == selectedClassId }.sortedBy { it.id } }

    val performanceList by viewModel.currentClassPerformance.collectAsState()
    val activeTermId by viewModel.selectedTermId.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isFinalTermOnlyFeatureEnabled by viewModel.isFinalTermOnlyFeatureEnabled.collectAsState()
    val allGrades by viewModel.grades.collectAsState()
    val manualTermAverages by viewModel.manualTermAverages.collectAsState()

    val isOutOfTen by viewModel.isOutOfTen.collectAsState()

    val failBound by viewModel.failBound.collectAsState()
    val passBound by viewModel.passBound.collectAsState()
    val acceptableBound by viewModel.acceptableBound.collectAsState()
    val goodBound by viewModel.goodBound.collectAsState()
    val veryGoodBound by viewModel.veryGoodBound.collectAsState()
    val useFinalExamFormula by viewModel.useFinalExamFormula.collectAsState()
    val failNoteExam1and2 by viewModel.failNoteExam1and2.collectAsState()

    val failBoundT1 by viewModel.failBoundT1.collectAsState()
    val passBoundT1 by viewModel.passBoundT1.collectAsState()
    val acceptableBoundT1 by viewModel.acceptableBoundT1.collectAsState()
    val goodBoundT1 by viewModel.goodBoundT1.collectAsState()
    val veryGoodBoundT1 by viewModel.veryGoodBoundT1.collectAsState()

    val failBoundT2 by viewModel.failBoundT2.collectAsState()
    val passBoundT2 by viewModel.passBoundT2.collectAsState()
    val acceptableBoundT2 by viewModel.acceptableBoundT2.collectAsState()
    val goodBoundT2 by viewModel.goodBoundT2.collectAsState()
    val veryGoodBoundT2 by viewModel.veryGoodBoundT2.collectAsState()

    val failBoundT3 by viewModel.failBoundT3.collectAsState()
    val passBoundT3 by viewModel.passBoundT3.collectAsState()
    val acceptableBoundT3 by viewModel.acceptableBoundT3.collectAsState()
    val goodBoundT3 by viewModel.goodBoundT3.collectAsState()
    val veryGoodBoundT3 by viewModel.veryGoodBoundT3.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var settingsTermId by remember { mutableStateOf(if (activeTermId in 1..3) activeTermId else 3) }

    var reportMode by remember { mutableStateOf(-1) } // -1: لا شيء مختار بعد
    var individualVariant by remember { mutableStateOf(0) }
    var selectedReportTermId by remember { mutableStateOf<Int?>(null) }
    val selectedStudentId by viewModel.selectedStudentId.collectAsState()
    val activeStudentPerf = performanceList.find { it.student.id == selectedStudentId }
    val failBoundValue = remember(activeClass, isOutOfTen) {
        if (activeClass != null) {
            viewModel.getFailBoundForClass(activeClass.id, activeClass.level)
        } else {
            10.0
        }
    }

    var showClassSelectionDialog by remember { mutableStateOf(false) }
    var showStudentSelectionDialog by remember { mutableStateOf(false) }

    val allCustomizations by viewModel.customizations.collectAsState()

    val targetLevel = activeClass?.level ?: 1
    val resolvedSubjects = remember(subjects, allCustomizations, targetLevel, selectedClassId) {
        viewModel.buildClassSubjects(selectedClassId ?: 0L, targetLevel, subjects, allCustomizations)
            .sortedByOfficialOrder()
    }

    val missingGradesList = remember(performanceList, resolvedSubjects) {
        val missing = mutableListOf<Pair<Student, Subject>>()
        performanceList.forEach { perf ->
            resolvedSubjects.forEach { sub ->
                if (!perf.gradesMap.containsKey(sub.id)) {
                    missing.add(Pair(perf.student, sub))
                }
            }
        }
        missing
    }

    LaunchedEffect(activeTermId) {
        val validModes = when (activeTermId) {
            1 -> listOf(0, 1, 5)
            2 -> listOf(0, 1, 5)
            4 -> listOf(0, 1, 5, 6, 7)
            else -> listOf(0, 6, 7, 8)
        }
        if (reportMode !in validModes && reportMode !in listOf(2, 3, 4, 9)) {
            reportMode = if (activeTermId == 3) 7 else 1
        }
    }

    val context = LocalContext.current

    var showSubjectsManagerInsideSettings by remember { mutableStateOf(false) }
    var showStampSettingsDialog by remember { mutableStateOf(false) }

    if (showSubjectsManagerInsideSettings) {
        if (activeClass != null) {
            PersonalClassSubjectsEditor(
                viewModel = viewModel,
                activeClass = activeClass,
                onClose = { showSubjectsManagerInsideSettings = false }
            )
        } else {
            SubjectsTab(
                viewModel = viewModel,
                subjects = subjects,
                bypassLogin = true,
                initialTab = 1,
                onClose = { showSubjectsManagerInsideSettings = false }
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = AppLocalization.tr("reports_title", "استخراج التقارير وسحب الكشوف 📊", appLanguage),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = AppLocalization.tr("reports_subtle_desc", "شاهد كشوف درجات الطلاب مع احتساب المعدل والرتبة، أو اسحب اللائحة التفصيلية للقسم للفصل المحدد.", appLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = AppLocalization.tr("settings_and_remarks", "الإعدادات والملاحظات", appLanguage),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = { showStampSettingsDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEBF2FF))
                        .border(1.5.dp, Color(0xFF153E90), RoundedCornerShape(10.dp))
                ) {
                    PaperStampSettingsIcon(
                        modifier = Modifier.size(26.dp),
                        tint = Color(0xFF153E90)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Settings Dialog implementation
        if (showSettingsDialog) {
            Dialog(
                onDismissRequest = { showSettingsDialog = false }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                    ) {
                        // Header with back arrow
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showSettingsDialog = false }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إعدادات القسم والملاحظات",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // --- Quick Subjects Editor ---
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showSettingsDialog = false
                                        showSubjectsManagerInsideSettings = true
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "تعديل مواد القسم 📚",
                                            fontWeight = FontWeight.ExtraBold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "إضافة، تعديل أو حذف المواد والدرجات القصوى ومعاملاتها مباشرة للأقسام.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }









                            // Toggle: هل تريد الدرجات من 10؟
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "هل تريد الدرجات من 10؟",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "عند تفعيل هذا الخيار، سيتم عرض كشوف النقاط والمعدلات من 10 نقاط بدلاً من 20.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = isOutOfTen,
                                        onCheckedChange = { viewModel.updateOutOfTen(it) }
                                    )
                                }
                            }

                            // Sliding Ranges Title
                            Text(
                                text = "تعديل مجالات التقييم والملاحظات لكل فصل 🎚️",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )

                            // Segmented row for term-specific remarks configuration
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1 to "الفصل الأول", 2 to "الفصل الثاني", 3 to "الفصل الثالث").forEach { (term, label) ->
                                    val isSelected = settingsTermId == term
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .clickable { settingsTermId = term }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            val scaleMax = if (isOutOfTen) 10f else 20f

                            val currentFail = when (settingsTermId) {
                                1 -> failBoundT1
                                2 -> failBoundT2
                                else -> failBoundT3
                            }
                            val currentPass = when (settingsTermId) {
                                1 -> passBoundT1
                                2 -> passBoundT2
                                else -> passBoundT3
                            }
                            val currentAcceptable = when (settingsTermId) {
                                1 -> acceptableBoundT1
                                2 -> acceptableBoundT2
                                else -> acceptableBoundT3
                            }
                            val currentGood = when (settingsTermId) {
                                1 -> goodBoundT1
                                2 -> goodBoundT2
                                else -> goodBoundT3
                            }
                            val currentVeryGood = when (settingsTermId) {
                                1 -> veryGoodBoundT1
                                2 -> veryGoodBoundT2
                                else -> veryGoodBoundT3
                            }

                            // Slider 1: راسب (من 0 إلى failBound)
                            BoundarySliderCard(
                                title = "من 0.0 إلى ${String.format(Locale.US, "%.1f", currentFail)} : راسب",
                                value = currentFail,
                                color = Color(0xFFE53935),
                                max = scaleMax
                            ) { newVal ->
                                val bounded = newVal.coerceIn(0f, currentPass)
                                viewModel.updateBoundsForTerm(settingsTermId, bounded, currentPass, currentAcceptable, currentGood, currentVeryGood)
                            }

                            // Slider 2: ناجح (من failBound إلى passBound)
                            BoundarySliderCard(
                                title = "من ${String.format(Locale.US, "%.1f", currentFail)} إلى ${String.format(Locale.US, "%.1f", currentPass)} : ناجح",
                                value = currentPass,
                                color = Color(0xFFFB8C00),
                                max = scaleMax
                            ) { newVal ->
                                val bounded = newVal.coerceIn(currentFail, currentAcceptable)
                                viewModel.updateBoundsForTerm(settingsTermId, currentFail, bounded, currentAcceptable, currentGood, currentVeryGood)
                            }

                            // Slider 3: مقبول (من passBound إلى acceptableBound)
                            BoundarySliderCard(
                                title = "من ${String.format(Locale.US, "%.1f", currentPass)} إلى ${String.format(Locale.US, "%.1f", currentAcceptable)} : مقبول",
                                value = currentAcceptable,
                                color = Color(0xFFFBC02D),
                                max = scaleMax
                            ) { newVal ->
                                val bounded = newVal.coerceIn(currentPass, currentGood)
                                viewModel.updateBoundsForTerm(settingsTermId, currentFail, currentPass, bounded, currentGood, currentVeryGood)
                            }

                            // Slider 4: جيد (من acceptableBound إلى goodBound)
                            BoundarySliderCard(
                                title = "من ${String.format(Locale.US, "%.1f", currentAcceptable)} إلى ${String.format(Locale.US, "%.1f", currentGood)} : جيد",
                                value = currentGood,
                                color = Color(0xFF43A047),
                                max = scaleMax
                            ) { newVal ->
                                val bounded = newVal.coerceIn(currentAcceptable, currentVeryGood)
                                viewModel.updateBoundsForTerm(settingsTermId, currentFail, currentPass, currentAcceptable, bounded, currentVeryGood)
                            }

                            // Slider 5: جيد جداً (من goodBound إلى veryGoodBound)
                            BoundarySliderCard(
                                title = "من ${String.format(Locale.US, "%.1f", currentGood)} إلى ${String.format(Locale.US, "%.1f", currentVeryGood)} : جيد جداً",
                                value = currentVeryGood,
                                color = Color(0xFF1B5E20),
                                max = scaleMax
                            ) { newVal ->
                                val bounded = newVal.coerceIn(currentGood, scaleMax)
                                viewModel.updateBoundsForTerm(settingsTermId, currentFail, currentPass, currentAcceptable, currentGood, bounded)
                            }

                            // Range displays: ممتاز
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "من ${String.format(Locale.US, "%.1f", currentVeryGood)} إلى ${String.format(Locale.US, "%.1f", scaleMax)} : ممتاز",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF00ACC1)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFF00ACC1).copy(alpha = 0.15f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("تلقائي", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00ACC1))
                                    }
                                }
                            }

                            // Section: إعدادات الفصل الثالث والامتحان النهائي
                            Text(
                                text = "إعدادات الفصل الثالث والامتحانات 📋",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    // Switch: استخدام صيغة الامتحان النهائي
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "استخدام صيغة الامتحان النهائي",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "في الامتحان الثالث، سيتم ضرب الامتحان الأول في 1، والثاني في 2، والثالث في 3.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = useFinalExamFormula,
                                            onCheckedChange = { viewModel.updateUseFinalExamFormula(it) }
                                        )
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                                    // Select note for failure in exam 1/2
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "ملاحظة الرسوب في الامتحان الأول والثاني:",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "اختر الملاحظة الافتراضية المناسبة للراسبين في الفترات الأولى والثانية قبل الامتحان النهائي.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf("راسب", "ضعيف", "مستحسن").forEach { option ->
                                                val isSel = failNoteExam1and2 == option
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                                        .clickable { viewModel.updateFailNoteExam1and2(option) }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = option,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Button(
                            onClick = { showSettingsDialog = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("تم وحفظ الإعدادات", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Dedicated Official Stamp Creator Dialog
        if (showStampSettingsDialog) {
            val stampClassSection = activeClass ?: classSections.firstOrNull() ?: ClassSection(name = "القسم", academicYear = "2025-2026", level = 1, schoolName = "الطلحايه 1")
            Dialog(
                onDismissRequest = { showStampSettingsDialog = false }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.92f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showStampSettingsDialog = false }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إنشاء وختم الطابع الرسمي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            OfficialStampSettingsSection(
                                viewModel = viewModel,
                                activeClass = stampClassSection
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { showStampSettingsDialog = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F4396))
                        ) {
                            Text("إغلاق والعودة", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))


        var expandedReportTermDropdown by remember { mutableStateOf(false) }
            val isReportTerm4Active = activeClass?.isTerm4Activated == true
            val isReportTerm3Active = activeClass?.isTerm3Activated == true

            val reportTerms = when {
                isReportTerm4Active -> {
                    listOf(4 to "الفصل الأخير فقط ⭐")
                }
                isReportTerm3Active -> {
                    listOf(
                        1 to AppLocalization.tr("term_1_full", "الفصل الأول", appLanguage),
                        2 to AppLocalization.tr("term_2_full", "الفصل الثاني", appLanguage),
                        3 to AppLocalization.tr("term_3_full", "الفصل الثالث والأخير", appLanguage)
                    )
                }
                isFinalTermOnlyFeatureEnabled -> {
                    listOf(
                        1 to AppLocalization.tr("term_1_full", "الفصل الأول", appLanguage),
                        2 to AppLocalization.tr("term_2_full", "الفصل الثاني", appLanguage),
                        3 to AppLocalization.tr("term_3_full", "الفصل الثالث والأخير", appLanguage),
                        4 to "الفصل الأخير فقط ⭐"
                    )
                }
                else -> {
                    listOf(
                        1 to AppLocalization.tr("term_1_full", "الفصل الأول", appLanguage),
                        2 to AppLocalization.tr("term_2_full", "الفصل الثاني", appLanguage),
                        3 to AppLocalization.tr("term_3_full", "الفصل الثالث والأخير", appLanguage)
                    )
                }
            }

            LaunchedEffect(reportTerms, selectedReportTermId) {
                if (selectedReportTermId != null && reportTerms.none { it.first == selectedReportTermId }) {
                    selectedReportTermId = reportTerms.firstOrNull()?.first
                }
            }
            val currentReportTermTuple = reportTerms.find { it.first == selectedReportTermId }
            val currentReportTermDisplay = if (selectedReportTermId == null) {
                AppLocalization.tr("select_term_prompt", "اختر الفصل الدراسي...", appLanguage)
            } else {
                currentReportTermTuple?.second ?: ""
            }

            // General Class Lists quick access card (لائحة الطلاب، المنشدين، الكناسة، إدراج الفرنسية)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = AppLocalization.tr("general_class_lists", "📋 لوائح وقوائم القسم العامة (سحب فوري)", appLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Students List
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        if (activeClass == null) {
                                            Toast.makeText(context, "الرجاء اختيار القسم أولاً من الأعلى", Toast.LENGTH_SHORT).show()
                                        } else if (classStudents.isEmpty()) {
                                            Toast.makeText(context, "الرجاء تسجيل الطلاب للقسم أولاً لسحب اللائحة", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.runWithLoading("جاري سحب وتصدير لائحة تلاميذ القسم...") {
                                                val html = HtmlReportHelper.generateStudentDirectoryHtml(
                                                    context = context,
                                                    className = activeClass.name,
                                                    students = classStudents,
                                                    activeClass = activeClass,
                                                    viewModel = viewModel
                                                )
                                                HtmlReportHelper.shareHtmlAsPdf(context, html, "لائحة_تلاميذ_قسم_${activeClass.name.replace(" ", "_")}.pdf")
                                            }
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "👥 لائحة الطلاب 📥",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            // 2. French Insertion Sheet
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        if (activeClass == null) {
                                            Toast.makeText(context, "الرجاء اختيار القسم أولاً من الأعلى", Toast.LENGTH_SHORT).show()
                                        } else if (classStudents.isEmpty()) {
                                            Toast.makeText(context, "الرجاء تسجيل الطلاب للقسم أولاً لسحب الورقة", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val frenchSub = resolvedSubjects.find { it.name.contains("فرنسية") || it.name.lowercase().contains("french") }
                                            val frenchMaxPoints = frenchSub?.maxPoints ?: 20
                                            viewModel.runWithLoading("جاري سحب وتصدير لائحة رصد الفرنسية...") {
                                                val html = HtmlReportHelper.generateFrenchGradesEntryHtml(
                                                    context = context,
                                                    className = activeClass.name,
                                                    students = classStudents,
                                                    activeClass = activeClass,
                                                    frenchMaxPoints = frenchMaxPoints,
                                                    viewModel = viewModel
                                                )
                                                HtmlReportHelper.shareHtmlAsPdf(context, html, "لائحة_رصد_الفرنسية_قسم_${activeClass.name.replace(" ", "_")}.pdf")
                                            }
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📝 إدراج الفرنسية 📥",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 3. Hymns/Singers List
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        if (activeClass == null) {
                                            Toast.makeText(context, "الرجاء اختيار القسم أولاً من الأعلى", Toast.LENGTH_SHORT).show()
                                        } else if (classStudents.isEmpty()) {
                                            Toast.makeText(context, "الرجاء تسجيل الطلاب للقسم أولاً للتقسيم", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.runWithLoading("جاري سحب وتصدير لائحة مجموعات المنشدين...") {
                                                val html = HtmlReportHelper.generateHymnsGroupsHtml(
                                                    context = context,
                                                    className = activeClass.name,
                                                    students = classStudents,
                                                    activeClass = activeClass,
                                                    viewModel = viewModel
                                                )
                                                HtmlReportHelper.shareHtmlAsPdf(context, html, "لائحة_مجموعات_المنشدين_قسم_${activeClass.name.replace(" ", "_")}.pdf")
                                            }
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🎤 المنشدين 📥",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            // 4. Sweeping/Cleaning List
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        if (activeClass == null) {
                                            Toast.makeText(context, "الرجاء اختيار القسم أولاً من الأعلى", Toast.LENGTH_SHORT).show()
                                        } else if (classStudents.isEmpty()) {
                                            Toast.makeText(context, "لا توجد أسماء بالقسم لبناء جدول النظافة", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.runWithLoading("جاري سحب وتصدير جدول النظافة والكناسة...") {
                                                val html = HtmlReportHelper.generateCleaningGroupsHtml(
                                                    context = context,
                                                    className = activeClass.name,
                                                    students = classStudents,
                                                    activeClass = activeClass,
                                                    viewModel = viewModel
                                                )
                                                HtmlReportHelper.shareHtmlAsPdf(context, html, "جدول_النظافة_والكناسة_قسم_${activeClass.name.replace(" ", "_")}.pdf")
                                            }
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🧹 الكناسة 📥",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section Header: كشوف الدرجات والنتائج التفصيلية
            Text(
                text = AppLocalization.tr("grade_slips_and_detailed_results", "كشوف الدرجات والنتائج التفصيلية", appLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = AppLocalization.tr("select_current_term", "الفصل الدراسي", appLanguage),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = BentoText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedReportTermDropdown = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, if (selectedReportTermId == 4) Color(0xFFD32F2F) else Color(0xFF2E7D32)),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (selectedReportTermId == 4) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("📅", fontSize = 16.sp)
                                    Text(
                                        text = currentReportTermDisplay,
                                        fontWeight = if (selectedReportTermId == null) FontWeight.Medium else FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (selectedReportTermId == null) MaterialTheme.colorScheme.onSurfaceVariant else if (selectedReportTermId == 4) Color(0xFFC62828) else Color(0xFF1B5E20)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "اختر الفصل",
                                    tint = if (selectedReportTermId == 4) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = expandedReportTermDropdown,
                            onDismissRequest = { expandedReportTermDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.88f)
                        ) {
                            reportTerms.forEach { (termId, termName) ->
                                val displayName = termName
                                val isTerm4 = (termId == 4)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isTerm4) Color(0xFFFFEBEE) else Color(0xFFF1F8E9)
                                    ),
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isTerm4) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                    )
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = displayName,
                                                        fontWeight = if (selectedReportTermId == termId) FontWeight.Black else FontWeight.Bold,
                                                        color = if (isTerm4) Color(0xFFC62828) else Color(0xFF1B5E20),
                                                        fontSize = 13.5.sp
                                                    )
                                                    if (selectedReportTermId == termId) {
                                                        Text("✓", color = if (isTerm4) Color(0xFFC62828) else Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                if (isTerm4) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "مخصص للوافدين الجدد الذين لم يدرجو نتائج الفصل الأول والثانى",
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFC62828),
                                                        lineHeight = 14.sp
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            expandedReportTermDropdown = false
                                            viewModel.selectTerm(termId)
                                            selectedReportTermId = termId
                                            if (termId in 1..2) {
                                                if (reportMode !in listOf(0, 1, 5)) {
                                                    reportMode = 1
                                                }
                                            } else if (termId == 4) {
                                                if (reportMode !in listOf(0, 1, 5, 6, 7)) {
                                                    reportMode = 1
                                                }
                                            } else {
                                                if (reportMode !in listOf(0, 6, 7, 8)) {
                                                    reportMode = 0
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }


                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeClass == null) {
            Spacer(modifier = Modifier.height(32.dp))
            EmptyStateCard(
                title = "لم يتم اختيار أي قسم",
                description = "الرجاء تحديد القسم المراد استخراج الكشوف والتقارير له من الأعلى."
            )
        } else if (selectedReportTermId == null) {
            // Friendly prompt to select a term before showing report selection buttons
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👆", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "الرجاء تحديد الفصل الدراسي أولاً",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "اضغط على قائمة «الفصل الدراسي» أعلاه واختبر الفصل المطلوب لإظهار أزرار الكشوف والنتائج التفصيلية.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Filter missing grades/averages for the warning banner based on term
            val studentMissingItems = remember(classStudents, resolvedSubjects, allGrades, manualTermAverages, selectedReportTermId, activeTermId) {
                val items = mutableListOf<Pair<Student, String>>()
                val currentTerm = selectedReportTermId ?: activeTermId
                val targetGradeTerm = if (currentTerm == 4) 3 else currentTerm

                classStudents.forEach { student ->
                    // 1. Check subject grades for active term
                    val studentTermGrades = allGrades.filter { it.studentId == student.id && it.termId == targetGradeTerm }
                    val studentGradedSubjectIds = studentTermGrades.map { it.subjectId }.toSet()
                    resolvedSubjects.forEach { sub ->
                        if (!studentGradedSubjectIds.contains(sub.id)) {
                            val termSuffix = if (currentTerm == 3) " (الفصل 3)" else ""
                            items.add(Pair(student, "درجة مادة «${sub.name}»$termSuffix"))
                        }
                    }

                    // 2. Check previous term results:
                    if (currentTerm == 4) {
                        // الفصل الثالث والأخير فقط هو الذي تظهر رسالة معدل الفصل الثاني لأنه لا يحتاج نقاط الفصل الثاني كلها
                        val hasT1Manual = manualTermAverages.any { it.studentId == student.id && it.termId == 1 && it.averageScore > 0.0 }
                        val t1Grades = allGrades.filter { it.studentId == student.id && it.termId == 1 }
                        val hasT1Grades = resolvedSubjects.isNotEmpty() && resolvedSubjects.all { sub -> t1Grades.any { it.subjectId == sub.id } }
                        if (!hasT1Manual && !hasT1Grades) {
                            items.add(Pair(student, "نتيجة الفصل الأول"))
                        }

                        val hasT2Manual = manualTermAverages.any { it.studentId == student.id && it.termId == 2 && it.averageScore > 0.0 }
                        val t2Grades = allGrades.filter { it.studentId == student.id && it.termId == 2 }
                        val hasT2Grades = resolvedSubjects.isNotEmpty() && resolvedSubjects.all { sub -> t2Grades.any { it.subjectId == sub.id } }
                        if (!hasT2Manual && !hasT2Grades) {
                            items.add(Pair(student, "نتيجة الفصل الثاني"))
                        }
                    } else if (currentTerm == 3) {
                        // الفصل الثالث العادي: يعتمد على درجات جميع المواد المسجلة في الفصلين 1 و 2
                        val t1Grades = allGrades.filter { it.studentId == student.id && it.termId == 1 }
                        val t1GradedIds = t1Grades.map { it.subjectId }.toSet()
                        resolvedSubjects.forEach { sub ->
                            if (!t1GradedIds.contains(sub.id)) {
                                items.add(Pair(student, "درجة مادة «${sub.name}» (الفصل 1)"))
                            }
                        }

                        val t2Grades = allGrades.filter { it.studentId == student.id && it.termId == 2 }
                        val t2GradedIds = t2Grades.map { it.subjectId }.toSet()
                        resolvedSubjects.forEach { sub ->
                            if (!t2GradedIds.contains(sub.id)) {
                                items.add(Pair(student, "درجة مادة «${sub.name}» (الفصل 2)"))
                            }
                        }
                    }
                }
                items
            }

            // Error Alert Banner for missing grades/averages (completely hides report buttons and cards until complete)
            if (studentMissingItems.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = BorderStroke(1.dp, Color(0xFFEF5350)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("⚠️", fontSize = 18.sp)
                            Text(
                                text = "تنبيه: الكشوف غير متاحة لعدم اكتمال النتائج!",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            modifier = Modifier
                                .heightIn(max = 240.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            studentMissingItems.forEach { (student, missingType) ->
                                Text(
                                    text = "• الطالب «${student.name}» لم يدرج $missingType",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB71C1C)
                                )
                            }
                        }
                    }
                }
            } else {

            // Mode Selectors - All options visible on screen
            Text(
                text = AppLocalization.tr("select_report_prompt", "اختر التقرير أو الكشف المطلوب 📋", appLanguage),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp, top = 4.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            val executeReportShare: (Int) -> Unit = { mode ->
                when (mode) {
                    1 -> {
                        if (performanceList.isEmpty()) {
                            Toast.makeText(context, "لا توجد علامات مرصودة بالقسم بعد للمشاركة", Toast.LENGTH_SHORT).show()
                        } else {
                            val html = HtmlReportHelper.generateReportCardsHtml(
                                context = context,
                                className = activeClass.name,
                                performances = performanceList,
                                subjects = resolvedSubjects,
                                activeTermId = activeTermId,
                                activeClass = activeClass,
                                viewModel = viewModel
                            )
                            val termName = when (activeTermId) {
                                1 -> "الأول"
                                2 -> "الثاني"
                                4 -> "الأخير_فقط"
                                else -> "الثالث"
                            }
                            HtmlReportHelper.shareHtmlAsPdf(context, html, "كشوف_درجات_قسم_${activeClass.name}_الفصل_${termName}.pdf", isLandscape = true)
                        }
                    }
                    10 -> {
                        if (performanceList.isEmpty()) {
                            Toast.makeText(context, "لا توجد علامات مرصودة بالقسم بعد للمشاركة", Toast.LENGTH_SHORT).show()
                        } else {
                            val html = HtmlReportHelper.generateReportCardsHtml(
                                context = context,
                                className = activeClass.name,
                                performances = performanceList,
                                subjects = resolvedSubjects,
                                activeTermId = activeTermId,
                                activeClass = activeClass,
                                viewModel = viewModel,
                                onePerPortraitPage = true
                            )
                            val termName = when (activeTermId) {
                                1 -> "الأول"
                                2 -> "الثاني"
                                4 -> "الأخير_فقط"
                                else -> "الثالث"
                            }
                            HtmlReportHelper.shareHtmlAsPdf(context, html, "كشوف_درجات_قسم_${activeClass.name}_الفصل_${termName}_ورقة_لكل_كشف.pdf", isLandscape = false)
                        }
                    }
                    8 -> {
                        if (classStudents.isEmpty()) {
                            Toast.makeText(context, "القسم فارغ لا يمكن سحب الكشوف", Toast.LENGTH_SHORT).show()
                        } else {
                            val html = HtmlReportHelper.generateThreeTermGradesReportCardsHtml(
                                context = context,
                                className = activeClass.name,
                                students = classStudents,
                                subjects = resolvedSubjects,
                                activeClass = activeClass,
                                viewModel = viewModel
                            )
                            HtmlReportHelper.shareHtmlAsPdf(context, html, "كشف_الدرجات_السنوي_قسم_${activeClass.name}.pdf", isLandscape = true)
                        }
                    }
                    11 -> {
                        if (classStudents.isEmpty()) {
                            Toast.makeText(context, "القسم فارغ لا يمكن سحب الكشوف", Toast.LENGTH_SHORT).show()
                        } else {
                            val html = HtmlReportHelper.generateThreeTermGradesReportCardsHtml(
                                context = context,
                                className = activeClass.name,
                                students = classStudents,
                                subjects = resolvedSubjects,
                                activeClass = activeClass,
                                viewModel = viewModel,
                                onePerPortraitPage = true
                            )
                            HtmlReportHelper.shareHtmlAsPdf(context, html, "كشف_الدرجات_السنوي_قسم_${activeClass.name}_ورقة_لكل_كشف.pdf", isLandscape = false)
                        }
                    }
                    7 -> {
                        if (classStudents.isEmpty()) {
                            Toast.makeText(context, "القسم فارغ لا يمكن سحب الكشوف", Toast.LENGTH_SHORT).show()
                        } else {
                            val html = HtmlReportHelper.generateAveragesOnlyReportCardsHtml(
                                context = context,
                                className = activeClass.name,
                                students = classStudents,
                                activeClass = activeClass,
                                viewModel = viewModel
                            )
                            HtmlReportHelper.shareHtmlAsPdf(context, html, "كشوف_المعدلات_فقط_قسم_${activeClass.name}.pdf", isLandscape = false)
                        }
                    }
                    5 -> {
                        if (performanceList.isEmpty()) {
                            Toast.makeText(context, "الرجاء رصد درجات ومواد القسم للحصول على نتائج", Toast.LENGTH_SHORT).show()
                        } else {
                            val html = HtmlReportHelper.generateDetailedTermLedgerHtml(
                                context = context,
                                className = activeClass.name,
                                performances = performanceList,
                                subjects = resolvedSubjects,
                                activeTermId = activeTermId,
                                activeClass = activeClass,
                                viewModel = viewModel
                            )
                            val termName = when (activeTermId) {
                                1 -> "الأول"
                                2 -> "الثاني"
                                3 -> "الثالث"
                                4 -> "الأخير_فقط"
                                else -> activeTermId.toString()
                            }
                            HtmlReportHelper.shareHtmlAsPdf(context, html, "لائحة_النتائج_التفصيلية_قسم_${activeClass.name}_الفصل_${termName}.pdf", isLandscape = true)
                        }
                    }
                    6 -> {
                        if (classStudents.isEmpty()) {
                            Toast.makeText(context, "القسم فارغ لا يمكن سحب المعدلات", Toast.LENGTH_SHORT).show()
                        } else {
                            val html = HtmlReportHelper.generateAnnualAveragesHtml(
                                context = context,
                                className = activeClass.name,
                                students = classStudents,
                                activeClass = activeClass,
                                viewModel = viewModel
                            )
                            HtmlReportHelper.shareHtmlAsPdf(context, html, "لائحة_المعدلات_السنوية_للسراج_قسم_${activeClass.name}.pdf", isLandscape = true)
                        }
                    }
                }
            }

            data class ReportGroup(val title: String, val options: List<Pair<Int, String>>)

            val reportGroups = when (activeTermId) {
                1, 2 -> listOf(
                    ReportGroup("🖨️ كشوف", listOf(
                        10 to "كشف لكل ورقة 📄",
                        1 to "كشفان في الورقة 🖨️"
                    )),
                    ReportGroup("📊 لائحة النتائج التفصيلية", listOf(5 to "")),
                    ReportGroup("👤 كشف فردي", listOf(0 to ""))
                )
                4 -> listOf(
                    ReportGroup("🖨️ كشوف", listOf(
                        10 to "كشف لكل ورقة 📄",
                        1 to "كشفان في الورقة 🖨️",
                        7 to "كشوف المعدلات السنوية 📑"
                    )),
                    ReportGroup("📊 لائحة النتائج التفصيلية", listOf(5 to "")),
                    ReportGroup("🏆 لائحة المعدلات السنوية للسراج", listOf(6 to "")),
                    ReportGroup("👤 كشف فردي", listOf(0 to ""))
                )
                else -> listOf(
                    ReportGroup("🖨️ كشوف", listOf(
                        11 to "كشف لكل ورقة 📄",
                        8 to "كشفان في الورقة 🖨️",
                        7 to "كشوف المعدلات السنوية 📑"
                    )),
                    ReportGroup("📊 لائحة النتائج التفصيلية", listOf(5 to "")),
                    ReportGroup("🏆 لائحة المعدلات السنوية للسراج", listOf(6 to "")),
                    ReportGroup("👤 كشف فردي", listOf(0 to ""))
                )
            }

            var openGroupIndex by remember(activeTermId) { mutableStateOf(-1) }
            var busyGroupIndex by remember(activeTermId) { mutableStateOf(-1) }
            var isGeneratingPdf by remember { mutableStateOf(false) }

            LaunchedEffect(isGeneratingPdf) {
                if (!isGeneratingPdf) {
                    busyGroupIndex = -1
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                reportGroups.forEachIndexed { gIndex, group ->
                    val isIndividualGroup = group.options.size == 1 && group.options.first().first == 0
                    val isGroupActive = busyGroupIndex == gIndex || (isIndividualGroup && reportMode == 0)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isGroupActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    if (group.options.size == 1) {
                                        val optId = group.options.first().first
                                        if (optId == 0) {
                                            reportMode = 0
                                        } else {
                                            reportMode = -1
                                            busyGroupIndex = gIndex
                                            executeReportShare(optId)
                                        }
                                        openGroupIndex = -1
                                    } else {
                                        openGroupIndex = if (openGroupIndex == gIndex) -1 else gIndex
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.title,
                                color = if (isGroupActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 2
                            )
                            if (group.options.size > 1) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = if (isGroupActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = openGroupIndex == gIndex,
                            onDismissRequest = { openGroupIndex = -1 }
                        ) {
                            group.options.forEach { (idx, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = label,
                                            fontWeight = if (reportMode == idx) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        openGroupIndex = -1
                                        reportMode = -1
                                        busyGroupIndex = gIndex
                                        executeReportShare(idx)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            when (reportMode) {
                0 -> {
                    // -- Mode 0: INDIVIDUAL STUDENT REPORT CARD --
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStudentSelectionDialog = true }
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("طالب كشف النقاط", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = activeStudentPerf?.student?.name ?: "اختر طالباً...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (activeStudentPerf == null) {
                        EmptyStateCard(
                            title = "الرجاء اختيار الطالب",
                            description = "اختر الطالب المطلوب عرض كشف درجات ومعدله وترتيبه."
                        )
                    } else {
                        // Show Beautiful Mauritanian Pattern Report Card
                        val sortedStudents = remember(classStudents) { classStudents.sortedBy { it.id } }
                        val callNumber = remember(sortedStudents, activeStudentPerf) {
                            sortedStudents.indexOfFirst { it.id == activeStudentPerf.student.id } + 1
                        }

                        val remarkAvg = if (activeTermId == 3) {
                            viewModel.calculateStudentGeneralAverage(activeStudentPerf.student.id, activeClass.id)
                        } else {
                            activeStudentPerf.averageScore
                        }
                        val remarkText = HtmlReportHelper.getRemarkString(remarkAvg, viewModel, activeClass.id, activeClass.level)

                        // Modern, elegant placeholder/summary card instead of the raw printed-sheet-like preview
                        val isOutOfTen = viewModel.isOutOfTen.value
                        val targetLevel = activeClass.level
                        val failBoundValue = viewModel.getFailBoundForClass(activeClass.id, activeClass.level)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "كشف درجات الطالب جاهز للتحميل والمشاركة",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "اختر صورة الكشف المطلوبة ليتم تجهيزها كملف PDF عالي الجودة ومصمم بشكل رسمي، جاهز للمشاركة أو الحفظ.",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    lineHeight = 18.sp
                                )
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(14.dp))

                                // Quick Student Summary Details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("اسم الطالب:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                        Text(activeStudentPerf.student.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("المعدل الحالي:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                        val displayAvg = if (activeTermId == 3 || activeTermId == 4) {
                                            viewModel.calculateStudentGeneralAverage(activeStudentPerf.student.id, activeClass.id)
                                        } else {
                                            activeStudentPerf.averageScore
                                        }
                                        Text("${String.format(Locale.US, "%.2f", displayAvg)} / 20", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("الرقم المدرسي:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                        Text(activeStudentPerf.student.schoolId.ifBlank { "غير مسجل" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("الرتبة:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                        val displayRank = if (activeTermId == 3 || activeTermId == 4) {
                                            viewModel.calculateStudentGeneralRank(activeStudentPerf.student.id, activeClass.id)
                                        } else {
                                            activeStudentPerf.rank
                                        }
                                        val rankedCount = performanceList.size
                                        val displayRankText = if (displayRank > 0) "$displayRank / $rankedCount" else "-"
                                        Text(displayRankText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("النتيجة:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                        val displayAvg = if (activeTermId == 3 || activeTermId == 4) {
                                            viewModel.calculateStudentGeneralAverage(activeStudentPerf.student.id, activeClass.id)
                                        } else {
                                            activeStudentPerf.averageScore
                                        }
                                        val isPass = (if (isOutOfTen) displayAvg / 2.0 else displayAvg) >= failBoundValue
                                        val resultText = if (isPass) "ناجح" else { if (targetLevel == 1) "متجاوز" else "راسب" }
                                        val resultColor = if (isPass) Color(0xFF2E7D32) else Color.Red
                                        Text(resultText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = resultColor)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("الملاحظة السلوكية:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                        Text(remarkText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Actions for the Individual Student Card - Share PDF
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val individualOptions = if (activeTermId == 3 || activeTermId == 4) {
                                listOf(1 to "📄 كشف ورقة تامة", 2 to "🖨️ كشف نصف ورقة", 3 to "📑 كشف المعدلات")
                            } else {
                                listOf(1 to "📄 كشف ورقة تامة", 2 to "🖨️ كشف نصف ورقة")
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                individualOptions.forEach { (optId, optLabel) ->
                                    val isOptSel = individualVariant == optId
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isOptSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable {
                                                individualVariant = optId
                                                when (optId) {
                                                    1 -> {
                                                        if (activeTermId == 3) {
                                                            val html = HtmlReportHelper.generateThreeTermGradesReportCardsHtml(
                                                                context = context,
                                                                className = activeClass.name,
                                                                students = classStudents,
                                                                subjects = resolvedSubjects,
                                                                activeClass = activeClass,
                                                                viewModel = viewModel,
                                                                targetStudentId = activeStudentPerf.student.id,
                                                                onePerPortraitPage = true
                                                            )
                                                            HtmlReportHelper.shareHtmlAsPdf(context, html, "كشف_الدرجات_السنوي_طالب_${activeStudentPerf.student.name}_ورقة_كاملة.pdf", isLandscape = false)
                                                        } else {
                                                            val html = HtmlReportHelper.generateReportCardsHtml(
                                                                context = context,
                                                                className = activeClass.name,
                                                                performances = performanceList,
                                                                subjects = resolvedSubjects,
                                                                activeTermId = activeTermId,
                                                                activeClass = activeClass,
                                                                viewModel = viewModel,
                                                                targetStudentId = activeStudentPerf.student.id,
                                                                onePerPortraitPage = true
                                                            )
                                                            val termName = when (activeTermId) {
                                                                1 -> "الأول"
                                                                2 -> "الثاني"
                                                                4 -> "الأخير_فقط"
                                                                else -> activeTermId.toString()
                                                            }
                                                            HtmlReportHelper.shareHtmlAsPdf(context, html, "كشف_درجات_طالب_${activeStudentPerf.student.name}_الفصل_${termName}_ورقة_كاملة.pdf", isLandscape = false)
                                                        }
                                                    }
                                                    2 -> {
                                                        if (activeTermId == 3) {
                                                            val html = HtmlReportHelper.generateThreeTermGradesReportCardsHtml(
                                                                context = context,
                                                                className = activeClass.name,
                                                                students = classStudents,
                                                                subjects = resolvedSubjects,
                                                                activeClass = activeClass,
                                                                viewModel = viewModel,
                                                                targetStudentId = activeStudentPerf.student.id
                                                            )
                                                            HtmlReportHelper.shareHtmlAsPdf(context, html, "كشف_الدرجات_السنوي_طالب_${activeStudentPerf.student.name}.pdf", isLandscape = true)
                                                        } else {
                                                            val html = HtmlReportHelper.generateReportCardsHtml(
                                                                context = context,
                                                                className = activeClass.name,
                                                                performances = performanceList,
                                                                subjects = resolvedSubjects,
                                                                activeTermId = activeTermId,
                                                                activeClass = activeClass,
                                                                viewModel = viewModel,
                                                                targetStudentId = activeStudentPerf.student.id
                                                            )
                                                            val termName = when (activeTermId) {
                                                                1 -> "الأول"
                                                                2 -> "الثاني"
                                                                4 -> "الأخير_فقط"
                                                                else -> activeTermId.toString()
                                                            }
                                                            HtmlReportHelper.shareHtmlAsPdf(context, html, "كشف_درجات_طالب_${activeStudentPerf.student.name}_الفصل_${termName}.pdf", isLandscape = true)
                                                        }
                                                    }
                                                    3 -> {
                                                        val html = HtmlReportHelper.generateAveragesOnlyReportCardsHtml(
                                                            context = context,
                                                            className = activeClass.name,
                                                            students = classStudents,
                                                            activeClass = activeClass,
                                                            viewModel = viewModel,
                                                            targetStudentId = activeStudentPerf.student.id
                                                        )
                                                        HtmlReportHelper.shareHtmlAsPdf(context, html, "كشف_معدلات_طالب_${activeStudentPerf.student.name}.pdf", isLandscape = false)
                                                    }
                                                }
                                            }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = optLabel,
                                            color = if (isOptSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // -- Mode 2: Student Directory (Excluding Parent Phone, bottom Teacher/Director signatures) --
                    ReportExportActionCard(
                        title = "📋 لائحة التلاميذ للقسم للطباعة",
                        description = "توليد لائحة رسمية لأسماء تلاميذ القسم مرتبة أبجدياً بدون أرقام الهواتف الشخصية، لعرضها في الصف أو تسليمها للإدارة مع خانات اعتماد الإدارة والمعلم بالأسفل.",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onPrintClick = {
                            if (classStudents.isEmpty()) {
                                Toast.makeText(context, "الرجاء تسجيل الطلاب للقسم أولاً للطباعة", Toast.LENGTH_SHORT).show()
                            } else {
                                val html = HtmlReportHelper.generateStudentDirectoryHtml(
                                    context = context,
                                    className = activeClass.name,
                                    students = classStudents,
                                    activeClass = activeClass,
                                    viewModel = viewModel
                                )
                                HtmlReportHelper.printHtml(context, html, "لائحة_تلاميذ_قسم_${activeClass.name}")
                            }
                        },
                        onShareClick = {
                            if (classStudents.isEmpty()) {
                                Toast.makeText(context, "الرجاء تسجيل الطلاب للقسم أولاً للمشاركة", Toast.LENGTH_SHORT).show()
                            } else {
                                val html = HtmlReportHelper.generateStudentDirectoryHtml(
                                    context = context,
                                    className = activeClass.name,
                                    students = classStudents,
                                    activeClass = activeClass,
                                    viewModel = viewModel
                                )
                                HtmlReportHelper.shareHtmlAsPdf(context, html, "لائحة_تلاميذ_قسم_${activeClass.name}.pdf")
                            }
                        },
                        viewModel = viewModel,
                        printButtonText = "📥 تحميل لائحة التلاميذ",
                        shareButtonText = "💚 مشاركة لائحة التلاميذ",
                        printMessage = "جاري توليد وتجهيز لائحة تلاميذ القسم للطباعة...",
                        shareMessage = "جاري تصدير ومشاركة لائحة تلاميذ القسم كملف PDF..."
                    )
                }
                3 -> {
                    // -- Mode 3: Hymns/Chants Groups (5 groups, bottom Signatures) --
                    ReportExportActionCard(
                        title = "🎤 لائحة مجموعات الأناشيد الدورية",
                        description = "تقسيم تلقائي لطلاب القسم بالتساوي إلى 5 مجموعات إنشادية دورية لتقديم الأناشيد والأمداح, مع خانات اعتماد الإدارة والمعلم بالأسفل.",
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        onPrintClick = {
                            if (classStudents.isEmpty()) {
                                Toast.makeText(context, "الرجاء تسجيل الطلاب للقسم أولاً للتقسيم والطباعة", Toast.LENGTH_SHORT).show()
                            } else {
                                val html = HtmlReportHelper.generateHymnsGroupsHtml(
                                    context = context,
                                    className = activeClass.name,
                                    students = classStudents,
                                    activeClass = activeClass,
                                    viewModel = viewModel
                                )
                                HtmlReportHelper.printHtml(context, html, "لائحة_مجموعات_الأناشيد")
                            }
                        },
                        onShareClick = {
                            if (classStudents.isEmpty()) {
                                Toast.makeText(context, "الرجاء تسجيل الطلاب للقسم أولاً للتقسيم", Toast.LENGTH_SHORT).show()
                            } else {
                                val html = HtmlReportHelper.generateHymnsGroupsHtml(
                                    context = context,
                                    className = activeClass.name,
                                    students = classStudents,
                                    activeClass = activeClass,
                                    viewModel = viewModel
                                )
                                HtmlReportHelper.shareHtmlAsPdf(context, html, "لائحة_مجموعات_الأناشيد.pdf")
                            }
                        },
                        viewModel = viewModel,
                        shareButtonText = "💚 مشاركة مجموعات الأناشيد كملف PDF",
                        shareMessage = "جاري تصدير مجموعات الأناشيد كملف PDF...",
                        showPrintButton = false
                    )
                }
                4 -> {
                    // -- Mode 4: Cleaning Groups (5 groups Mon-Fri, signatures bottom) --
                    ReportExportActionCard(
                        title = "🧹 لائحة وجدول النظافة والكناسة الدوري",
                        description = "توزيع وتنظيم طلاب القسم إلى 5 مجموعات نظافة دورية على مدار أيام الأسبوع الدراسي (الاثنين إلى الجمعة)، لغرس قيم التعاون والنظافة.",
                        containerColor = Color(0xFFFFF9C4),
                        onPrintClick = {
                            if (classStudents.isEmpty()) {
                                Toast.makeText(context, "لا توجد أسماء بالقسم لبناء جدول النظافة", Toast.LENGTH_SHORT).show()
                            } else {
                                val html = HtmlReportHelper.generateCleaningGroupsHtml(
                                    context = context,
                                    className = activeClass.name,
                                    students = classStudents,
                                    activeClass = activeClass,
                                    viewModel = viewModel
                                )
                                HtmlReportHelper.printHtml(context, html, "جدول_النظافة_والكناسة")
                            }
                        },
                        onShareClick = {
                            if (classStudents.isEmpty()) {
                                Toast.makeText(context, "لا توجد أسماء بالقسم لبناء جدول النظافة", Toast.LENGTH_SHORT).show()
                            } else {
                                val html = HtmlReportHelper.generateCleaningGroupsHtml(
                                    context = context,
                                    className = activeClass.name,
                                    students = classStudents,
                                    activeClass = activeClass,
                                    viewModel = viewModel
                                )
                                HtmlReportHelper.shareHtmlAsPdf(context, html, "جدول_النظافة_والكناسة.pdf")
                            }
                        },
                        viewModel = viewModel,
                        shareButtonText = "💚 مشاركة جدول النظافة كملف PDF",
                        shareMessage = "جاري تصدير ومشاركة جدول النظافة كملف PDF...",
                        showPrintButton = false
                    )
                }

                9 -> {
                    // -- Mode 9: French Grades Entry List (portrait) --
                    val frenchSub = resolvedSubjects.find { it.name.contains("فرنسية") || it.name.lowercase().contains("french") }
                    val frenchMaxPoints = frenchSub?.maxPoints ?: 20

                    ReportExportActionCard(
                        title = "📝 لائحة إدراج نتائج مادة اللغة الفرنسية يدوياً",
                        description = "توليد ورقة رصد درجات اللغة الفرنسية للقسم فارغة وتحتوي على رقم النداء والاسم بالعربية والفرنسية، ليقوم معلم الفرنسية بتسجيل العلامات عليها يدوياً وإعادتها لإدارجها في التطبيق (مجموع نقاط المادة لهذا المستوى: $frenchMaxPoints ن)",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        onPrintClick = {
                            if (classStudents.isEmpty()) {
                                Toast.makeText(context, "لا توجد أسماء طلاب بالقسم بعد للطباعة", Toast.LENGTH_SHORT).show()
                            } else {
                                val html = HtmlReportHelper.generateFrenchGradesEntryHtml(
                                    context = context,
                                    className = activeClass.name,
                                    students = classStudents,
                                    activeClass = activeClass,
                                    frenchMaxPoints = frenchMaxPoints,
                                    viewModel = viewModel
                                )
                                HtmlReportHelper.printHtml(context, html, "لائحة_رصد_الفرنسية_قسم_${activeClass.name.replace(" ", "_")}")
                            }
                        },
                        onShareClick = {
                            if (classStudents.isEmpty()) {
                                Toast.makeText(context, "لا توجد أسماء طلاب بالقسم بعد للمشاركة", Toast.LENGTH_SHORT).show()
                            } else {
                                val html = HtmlReportHelper.generateFrenchGradesEntryHtml(
                                    context = context,
                                    className = activeClass.name,
                                    students = classStudents,
                                    activeClass = activeClass,
                                    frenchMaxPoints = frenchMaxPoints,
                                    viewModel = viewModel
                                )
                                HtmlReportHelper.shareHtmlAsPdf(context, html, "لائحة_رصد_الفرنسية_قسم_${activeClass.name.replace(" ", "_")}.pdf")
                            }
                        },
                        viewModel = viewModel,
                        printButtonText = "📥 تحميل وطباعة لائحة رصد الفرنسية (PDF)",
                        shareButtonText = "💚 مشاركة لائحة الفرنسية كملف PDF",
                        printMessage = "جاري تحضير لائحة رصد علامات الفرنسية للطباعة...",
                        shareMessage = "جاري تصدير ومشاركة لائحة رصد علامات الفرنسية كملف PDF..."
                    )
                }
            }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AppFooter()
        Spacer(modifier = Modifier.height(100.dp))
    }
}

    // DIALOG: CHOOSE CLASS SECTION
    if (showClassSelectionDialog) {
        DialogSelection(
            title = "انتقاء القسم الدراسي",
            items = classSections,
            itemName = { it.name },
            onItemSelected = {
                viewModel.selectClassWithLockCheck(it.id)
                viewModel.selectStudent(null) // reset student card selection when class changes
                showClassSelectionDialog = false
            },
            onDismiss = { showClassSelectionDialog = false }
        )
    }

    // DIALOG: CHOOSE INDIVIDUAL STUDENT FOR REPORT CARD
    if (showStudentSelectionDialog) {
        DialogSelection(
            title = "انتقاء الطالب",
            items = classStudents,
            itemName = { "${classStudents.indexOf(it) + 1} - ${it.name}" },
            onItemSelected = {
                viewModel.selectStudent(it.id)
                showStudentSelectionDialog = false
            },
            onDismiss = { showStudentSelectionDialog = false }
        )
    }
}

@Composable
fun BoundarySliderCard(
    title: String,
    value: Float,
    color: Color,
    max: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = color,
                textAlign = TextAlign.Right
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..max,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.22f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TeacherLoginSignupScreen(viewModel: TeacherViewModel) {
    val appLanguage by viewModel.appLanguage.collectAsState()
    val tr = remember(appLanguage) {
        if (appLanguage == "fr") {
            mapOf(
                "title" to "Connexion de l'Enseignant",
                "welcome" to "Bienvenue à nouveau ! Connectez-vous rapidement pour lier vos données, suivre et gérer les notes et les élèves de vos classes en toute sécurité.",
                "google_sign_in" to "Connexion rapide avec Google 🚀",
                "footer" to "Tous droits réservés © Développeur : El Moctar Yedaly 🇲🇷 2026",
                "web_client_title" to "Configuration de la connexion Google",
                "web_client_desc" to "Pour activer la connexion directe et rapide avec Google, veuillez lier l'application à votre Web Client ID Firebase (sous les paramètres Google Sign-In).\n\n💡 Comment l'obtenir ?\n1. Allez sur la console Firebase -> Authentication -> Sign-in method -> Google.\n2. Cliquez sur Modifier pour voir le 'Web client ID'.\n3. Copiez et collez-le ci-dessous pour le sauvegarder sur l'appareil.",
                "web_client_label" to "Google Web Client ID",
                "web_client_placeholder" to "xxxxxx-xxxxxx.apps.googleusercontent.com",
                "save" to "Enregistrer",
                "cancel" to "Annuler",
                "saved_success" to "Identifiant enregistré avec succès ! Essayez de vous reconnecter 🚀",
                "auth_error_init" to "Erreur d'initialisation des services Google: "
            )
        } else {
            mapOf(
                "title" to "تسجيل دخول المعلم",
                "welcome" to "مرحباً بك مجدداً! قم بتسجيل الدخول السريع لربط بياناتك، للرصد وإدارة درجات وتلاميذ أقسامك بأمان ونظام.",
                "google_sign_in" to "الدخول السريع بحساب Google 🚀",
                "footer" to "جميع الحقوق محفوظة © المطور: المختار اليدالى 🇲🇷 2026",
                "web_client_title" to "إعداد الدخول بحساب Google",
                "web_client_desc" to "لتفعيل ميزة تسجيل الدخول المباشر والسريع بحساب جوجل، يجب ربط التطبيق بـ Web Client ID الخاص بك من لوحة تحكم Firebase (تحت إعدادات Google Sign-In).\n\n" +
                        "💡 كيف تحصل عليه؟\n" +
                        "1. اذهب للوحة Firebase -> Authentication -> Sign-in method -> Google.\n" +
                        "2. انقر تعديل وستجد 'Web client ID' مسجلاً هناك.\n" +
                        "3. قم بنسخه ولصقه في الحقل أدناه ليتم حفظه على جهازك للاختبار، أو قم بتحديث ملف google-services.json سحابياً.",
                "web_client_label" to "Web Client ID الخاص بـ Google",
                "web_client_placeholder" to "xxxxxx-xxxxxx.apps.googleusercontent.com",
                "save" to "حفظ وتأكيد",
                "cancel" to "إلغاء",
                "saved_success" to "تم حفظ المعرّف بنجاح! جرب النقر على الدخول مجدداً 🚀",
                "auth_error_init" to "حدث خطأ في تهيئة خدمات Google: "
            )
        }
    }

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }

    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    var showClassManager by remember { mutableStateOf(false) }
    
    // Forgot Password states
    var failedAttemptsCount by remember { mutableStateOf(0) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }

    LaunchedEffect(authError) {
        if (authError != null && isLoginMode) {
            failedAttemptsCount += 1
        }
    }

    LaunchedEffect(isLoginMode) {
        failedAttemptsCount = 0
    }
    
    val context = LocalContext.current

    var lastApiExceptionInfo by remember { mutableStateOf<String?>(null) }

    val diagnosticPackageName = remember(context) { context.applicationContext.packageName }
    val diagnosticBuildConfigId = remember {
        try {
            com.example.BuildConfig.APPLICATION_ID
        } catch (e: Throwable) {
            "Unknown (Class not found/Field missing)"
        }
    }
    
    val diagnosticDefaultWebClientId = remember(context) {
        try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
    
    val diagnosticVersionCodeAndName = remember(context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val vCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            val vName = packageInfo.versionName
            "VersionCode: $vCode, VersionName: $vName"
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
    
    val diagnosticSha1Signature = remember(context) {
        try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNATURES
                )
            }
            
            val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures != null && signatures.isNotEmpty()) {
                val signature = signatures[0]
                val md = java.security.MessageDigest.getInstance("SHA-1")
                val digest = md.digest(signature.toByteArray())
                digest.joinToString(":") { String.format("%02X", it) }
            } else {
                "No signatures found"
            }
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (!isNetworkAvailable(context)) {
            val netErr = "لا يوجد اتصال بالإنترنت أو الاتصال ضعيف"
            Toast.makeText(context, netErr, Toast.LENGTH_LONG).show()
            lastApiExceptionInfo = netErr
            return@rememberLauncherForActivityResult
        }

        if (result.data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    lastApiExceptionInfo = null
                    viewModel.clearAuthError()
                    viewModel.loginWithGoogleCredential(idToken) {
                        Toast.makeText(context, "تم تسجيل الدخول بنجاح بحساب Google! 🎉", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val err = "تعذر الحصول على رمز تعريف Google (ID Token)."
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    lastApiExceptionInfo = err
                }
            } catch (e: ApiException) {
                val msg = (e.localizedMessage ?: "") + " " + (e.statusMessage ?: "")
                val isNet = !isNetworkAvailable(context) || e.statusCode == 7 ||
                        msg.contains("network", ignoreCase = true) ||
                        msg.contains("connection", ignoreCase = true) ||
                        msg.contains("timeout", ignoreCase = true) ||
                        msg.contains("unknownhost", ignoreCase = true)

                val userMsg = when {
                    isNet -> "لا يوجد اتصال بالإنترنت أو الاتصال ضعيف"
                    e.statusCode == 12501 || e.statusCode == 12502 || msg.contains("cancel", ignoreCase = true) -> "تم إلغاء عملية الدخول"
                    else -> "فشل تسجيل الدخول بحساب Google (رمز: ${e.statusCode})"
                }
                Toast.makeText(context, userMsg, Toast.LENGTH_LONG).show()
                lastApiExceptionInfo = userMsg
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: ""
                val isNet = !isNetworkAvailable(context) ||
                        msg.contains("network", ignoreCase = true) ||
                        msg.contains("connection", ignoreCase = true) ||
                        msg.contains("timeout", ignoreCase = true) ||
                        msg.contains("unknownhost", ignoreCase = true)

                val userMsg = when {
                    isNet -> "لا يوجد اتصال بالإنترنت أو الاتصال ضعيف"
                    msg.contains("cancel", ignoreCase = true) || msg.contains("12501") -> "تم إلغاء عملية الدخول"
                    else -> "حدث خطأ أثناء المصادقة: ${e.localizedMessage ?: "فشل الاتصال"}"
                }
                Toast.makeText(context, userMsg, Toast.LENGTH_LONG).show()
                lastApiExceptionInfo = userMsg
            }
        } else {
            val isNet = !isNetworkAvailable(context)
            val userMsg = if (isNet) {
                "لا يوجد اتصال بالإنترنت أو الاتصال ضعيف"
            } else if (result.resultCode == android.app.Activity.RESULT_CANCELED) {
                "تم إلغاء عملية الدخول"
            } else {
                "تعذر الاتصال بخدمات الدخول"
            }
            Toast.makeText(context, userMsg, Toast.LENGTH_SHORT).show()
            lastApiExceptionInfo = userMsg
        }
    }

    val sharedPrefs = remember(context) { context.getSharedPreferences("teacher_prefs", Context.MODE_PRIVATE) }
    var customWebClientId by remember { mutableStateOf(sharedPrefs.getString("web_client_id", "") ?: "") }
    var showClientIdDialog by remember { mutableStateOf(false) }

    val activeWebClientId = remember(customWebClientId) {
        if (customWebClientId.isNotBlank()) {
            customWebClientId
        } else {
            try {
                context.getString(R.string.default_web_client_id)
            } catch (e: Exception) {
                ""
            }
        }
    }

    // Helper validation
    fun validateInputs(): Boolean {
        var isValid = true
        
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "الرجاء إدخال بريد إلكتروني صالح"
            isValid = false
        } else {
            emailError = null
        }

        if (password.length < 6) {
            passwordError = "يجب أن تتكون كلمة المرور من 6 أحرف على الأقل"
            isValid = false
        } else {
            passwordError = null
        }

        if (!isLoginMode && displayName.trim().isBlank()) {
            nameError = "الرجاء إدخال الاسم الكامل"
            isValid = false
        } else {
            nameError = null
        }

        return isValid
    }

    if (showClassManager) {
        SubjectsTab(
            viewModel = viewModel,
            subjects = subjects,
            onClose = { showClassManager = false }
        )
    } else {
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE8F5E9), // Light green tint
                                Color(0xFFFFFFFF)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 2. App Title
                Text(
                    text = tr["title"] ?: "تسجيل دخول المعلم",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B5E20),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = tr["welcome"] ?: "مرحباً بك مجدداً! قم بتسجيل الدخول السريع لربط بياناتك، للرصد وإدارة درجات وتلاميذ أقسامك بأمان ونظام.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Beautifully designed Google Sign-In button as the primary mechanism
                Button(
                    onClick = {
                        lastApiExceptionInfo = null
                        viewModel.clearAuthError()
                        if (!isNetworkAvailable(context)) {
                            val netErr = "لا يوجد اتصال بالإنترنت أو الاتصال ضعيف"
                            Toast.makeText(context, netErr, Toast.LENGTH_LONG).show()
                            lastApiExceptionInfo = netErr
                            return@Button
                        }
                        if (activeWebClientId.isBlank()) {
                            showClientIdDialog = true
                        } else {
                            try {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(activeWebClientId)
                                    .requestEmail()
                                    .requestScopes(com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/drive.file"))
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                googleSignInClient.signOut().addOnCompleteListener {
                                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                }
                            } catch (e: Exception) {
                                val msg = e.localizedMessage ?: "حدث خطأ في تهيئة خدمات Google"
                                val userMsg = if (msg.contains("network", ignoreCase = true) || msg.contains("connection", ignoreCase = true)) {
                                    "لا يوجد اتصال بالإنترنت أو الاتصال ضعيف"
                                } else {
                                    "${tr["auth_error_init"] ?: "حدث خطأ في تهيئة خدمات Google: "}$msg"
                                }
                                Toast.makeText(context, userMsg, Toast.LENGTH_LONG).show()
                                lastApiExceptionInfo = userMsg
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("google_signin_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1B5E20),
                        contentColor = Color.White
                    ),
                    enabled = !isAuthLoading
                ) {
                    if (isAuthLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_google),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = tr["google_sign_in"] ?: "الدخول السريع بحساب Google 🚀",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Clean privacy-focused status & error display card
                val displayError = lastApiExceptionInfo ?: authError
                if (displayError != null) {
                    val isCancellation = displayError.contains("إلغاء") || displayError.contains("canceled", ignoreCase = true) || displayError.contains("12501")
                    val isNetworkError = displayError.contains("انترنت") || displayError.contains("إنترنت") || displayError.contains("شبكة") || displayError.contains("ضعيف") || displayError.contains("اتصال") || displayError.contains("network", ignoreCase = true) || displayError.contains("connection", ignoreCase = true)

                    val bgColor = when {
                        isCancellation -> Color(0xFFFFF8E1) // Soft amber
                        isNetworkError -> Color(0xFFFFF3E0) // Soft orange
                        else -> Color(0xFFFFEBEE) // Soft red
                    }
                    val borderColor = when {
                        isCancellation -> Color(0xFFFFB300)
                        isNetworkError -> Color(0xFFFFB74D)
                        else -> Color(0xFFEF5350)
                    }
                    val textColor = when {
                        isCancellation -> Color(0xFFE65100)
                        isNetworkError -> Color(0xFFE65100)
                        else -> Color(0xFFC62828)
                    }
                    val icon = when {
                        isCancellation -> Icons.Default.Info
                        isNetworkError -> Icons.Default.Warning
                        else -> Icons.Default.Warning
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("auth_error_card"),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = BorderStroke(1.dp, borderColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = displayError,
                                color = textColor,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Text(
                    text = "إصدار التطبيق: ${diagnosticVersionCodeAndName.replace("VersionCode: ", "كود ").replace("VersionName: ", "نسخة ")}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))
                
                // Footer details
                Text(
                    text = tr["footer"] ?: "جميع الحقوق محفوظة © المطور: المختار اليدالى 🇲🇷 2026",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🔑", fontSize = 20.sp)
                    Text(
                        text = "استعادة كلمة المرور 🔐",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1B5E20)
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "أدخل عنوان البريد الإلكتروني المسجل وسنقوم بإرسال رابط آمن لإعادة تعيين كلمة المرور الخاصة بك.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = resetEmailInput,
                        onValueChange = { resetEmailInput = it },
                        label = { Text("البريد الإلكتروني") },
                        placeholder = { Text("email@example.com") },
                        modifier = Modifier.fillMaxWidth().testTag("reset_email_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color(0xFF1B5E20)
                            )
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmailInput.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(resetEmailInput).matches()) {
                            android.widget.Toast.makeText(context, "الرجاء إدخال بريد إلكتروني صالح", android.widget.Toast.LENGTH_LONG).show()
                        } else {
                            viewModel.sendPasswordResetEmail(
                                email = resetEmailInput,
                                onSuccess = {
                                    android.widget.Toast.makeText(context, "تم إرسال رابط استعادة كلمة المرور لبريدكم بنجاح! 📨", android.widget.Toast.LENGTH_LONG).show()
                                    showForgotPasswordDialog = false
                                    failedAttemptsCount = 0
                                },
                                onFailure = { errorMsg ->
                                    android.widget.Toast.makeText(context, "خطأ: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1B5E20),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("إرسال الرابط", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("إلغاء", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showClientIdDialog) {
        var tempClientId by remember { mutableStateOf(customWebClientId) }
        AlertDialog(
            onDismissRequest = { showClientIdDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⚙️", fontSize = 20.sp)
                    Text(
                        text = tr["web_client_title"] ?: "إعداد الدخول بحساب Google",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1B5E20)
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tr["web_client_desc"] ?: "لتفعيل ميزة تسجيل الدخول المباشر والسريع بحساب جوجل، يجب ربط التطبيق بـ Web Client ID الخاص بك من لوحة تحكم Firebase (تحت إعدادات Google Sign-In).\n\n💡 كيف تحصل عليه؟\n1. اذهب للوحة Firebase -> Authentication -> Sign-in method -> Google.\n2. انقر تعديل وستجد 'Web client ID' مسجلاً هناك.\n3. قم بنسخه ولصقه في الحقل أدناه ليتم حفظه على جهازك للاختبار، أو قم بتحديث ملف google-services.json سحابياً.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    OutlinedTextField(
                        value = tempClientId,
                        onValueChange = { tempClientId = it },
                        label = { Text(tr["web_client_label"] ?: "Web Client ID الخاص بـ Google") },
                        placeholder = { Text(tr["web_client_placeholder"] ?: "xxxxxx-xxxxxx.apps.googleusercontent.com") },
                        modifier = Modifier.fillMaxWidth().testTag("web_client_id_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = Color(0xFF1B5E20)
                            )
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanId = tempClientId.trim()
                        customWebClientId = cleanId
                        sharedPrefs.edit().putString("web_client_id", cleanId).apply()
                        showClientIdDialog = false
                        if (cleanId.isNotBlank()) {
                            Toast.makeText(context, tr["saved_success"] ?: "تم حفظ المعرّف بنجاح! جرب النقر على الدخول مجدداً 🚀", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1B5E20),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(tr["save"] ?: "حفظ وتأكيد", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClientIdDialog = false }) {
                    Text(tr["cancel"] ?: "إلغاء", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
}


@Composable
fun AdminStatsTabContent(viewModel: TeacherViewModel) {
    val allClasses by viewModel.allClassesForStats.collectAsState()
    val teachersCount by viewModel.statsTeachersCount.collectAsState()
    val classesCount by viewModel.statsClassesCount.collectAsState()
    val exchangesApproved by viewModel.statsExchangesApprovedCount.collectAsState()
    val exchangesPending by viewModel.statsExchangesPendingCount.collectAsState()
    val isLoadingCounts by viewModel.statsCountsLoading.collectAsState()
    val isLoadingStats by viewModel.isLoadingStats.collectAsState()

    var selectedYearFilter by remember { mutableStateOf("الكل") }
    val context = LocalContext.current

    var isAdminLocking by remember { mutableStateOf(false) }
    var adminLockProgressText by remember { mutableStateOf<String?>(null) }

    val currentCalendarMonth = remember {
        java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
    }

    val uniqueYears = remember(allClasses) {
        val list = allClasses.map { normalizeAcademicYear(it.academicYear) }.distinct().sorted()
        if (list.isEmpty()) listOf("2025-2026") else list
    }

    LaunchedEffect(Unit) {
        viewModel.loadStatsData()
        viewModel.loadStatsCounts()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BentoGrayOutline, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Refresh Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📊", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "إحصائيات المستخدمين والنشاط",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoText
                        )
                        Text(
                            text = "نظرة عامة على المعلمين والصفوف وإعلانات التبادل.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoPrimaryDesc
                        )
                    }
                }

                // Refresh Button
                TextButton(
                    onClick = {
                        viewModel.loadStatsData()
                        viewModel.loadStatsCounts()
                        android.widget.Toast.makeText(context, "تم تحديث البيانات والإحصائيات فورياً! 🔄", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = BentoPrimary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث", modifier = Modifier.size(16.dp))
                        Text("تحديث 🔄", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.5f))

            val statsError by viewModel.statsLoadError.collectAsState()
            if (statsError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "⚠️ $statsError",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFFC62828),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }


            if ((isLoadingStats || isLoadingCounts) && allClasses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BentoPrimary)
                }
            } else {
                // Main Overall Stats Grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "المؤشرات الإجمالية العامة 📈",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )

                    // Exchange Posts Statistics
                    val totalExchangePosts = exchangesApproved + exchangesPending
                    val approvedExchangePosts = exchangesApproved
                    val pendingExchangePosts = exchangesPending

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "عدد إعلانات التبادل",
                            value = "$totalExchangePosts",
                            icon = "🔄",
                            color = Color(0xFFEDE7F6),
                            textColor = BentoPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            title = "إعلانات مفعلة",
                            value = "$approvedExchangePosts",
                            icon = "✅",
                            color = Color(0xFFE8F5E9),
                            textColor = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            title = "إعلانات غير مفعلة",
                            value = "$pendingExchangePosts",
                            icon = "⏳",
                            color = Color(0xFFFFF3E0),
                            textColor = Color(0xFFE65100),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // ===== إحصائيات دورة تحضير مسابقات المعلمين =====
                    val courseStats by viewModel.courseActivationStats.collectAsState()
                    val courseStatsError by viewModel.courseActivationStatsError.collectAsState()
                    var courseSpecFilter by remember { mutableStateOf<String?>(null) }
                    val nowMs = remember(courseStats) { System.currentTimeMillis() }
                    val filteredCourseStats = courseStats.filter { courseSpecFilter == null || it.specId == courseSpecFilter }
                    val courseActivationsTotal = filteredCourseStats.size
                    val courseActivationsActive = filteredCourseStats.count { it.expiresAt > nowMs }

                    Text(
                        text = "دورة تحضير مسابقات المعلمين 🎓",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val chipOptions = listOf<Pair<String?, String>>(null to "الكل") + COURSE_SPECIALIZATIONS.map { it.id to it.title }
                        chipOptions.forEach { (specIdOption, label) ->
                            val isSel = courseSpecFilter == specIdOption
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSel) BentoPrimary else Color(0xFFEDE7F6),
                                modifier = Modifier.clickable { courseSpecFilter = specIdOption }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.White else BentoPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "عدد التفعيلات",
                            value = if (courseStatsError) "تعذّر" else courseActivationsTotal.toString(),
                            icon = "🎓",
                            color = Color(0xFFE8F5E9),
                            textColor = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "تفعيلات سارية",
                            value = if (courseStatsError) "تعذّر" else courseActivationsActive.toString(),
                            icon = "✅",
                            color = Color(0xFFE3F2FD),
                            textColor = Color(0xFF1565C0),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (courseStatsError) {
                        Text(
                            text = "تعذّر جلب إحصائيات الدورة من الخادم",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.5f))

                // Year Filter Chips Row
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "تصفية وتفصيل الإحصائيات حسب السنة الدراسية 📅",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            // "الكل" Chip
                            val isAllSelected = selectedYearFilter == "الكل"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isAllSelected) BentoPrimary else BentoLightLavender)
                                    .clickable { selectedYearFilter = "الكل" }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "كل السنوات الدراسية",
                                    color = if (isAllSelected) Color.White else BentoText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        items(uniqueYears) { year ->
                            val isSelected = selectedYearFilter == year
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) BentoPrimary else BentoLightLavender)
                                    .clickable { selectedYearFilter = year }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = year,
                                    color = if (isSelected) Color.White else BentoText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filtered Stats Card
                val filteredClasses = if (selectedYearFilter == "الكل") allClasses else allClasses.filter { normalizeAcademicYear(it.academicYear) == selectedYearFilter }
                val filteredTeachersCount = if (selectedYearFilter == "الكل") {
                    teachersCount
                } else {
                    // Count unique teacherIds present in classes of this academic year
                    filteredClasses.map { it.teacherId }.distinct().count { it.isNotBlank() }
                }

                val totalFilteredClasses = filteredClasses.size
                val term1ActivatedFilteredClasses = filteredClasses.count { it.isTermUnlocked(1) }
                val term2ActivatedFilteredClasses = filteredClasses.count { it.isTermUnlocked(2) }
                val term3ActivatedFilteredClasses = filteredClasses.count { it.isTermUnlocked(3) }
                val term4ActivatedFilteredClasses = filteredClasses.count { it.isTermUnlocked(4) }
                val activatedFilteredClasses = filteredClasses.count { it.isTermUnlocked(1) || it.isTermUnlocked(2) || it.isTermUnlocked(3) || it.isTermUnlocked(4) }
                val deactivatedFilteredClasses = filteredClasses.count { !it.isTermUnlocked(1) && !it.isTermUnlocked(2) && !it.isTermUnlocked(3) && !it.isTermUnlocked(4) }
                val term1CompletedFilteredClasses = filteredClasses.count { it.isTerm1Completed }
                val term2CompletedFilteredClasses = filteredClasses.count { it.isTerm2Completed }
                val term3CompletedFilteredClasses = filteredClasses.count { it.isTerm3Completed }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BentoGrayOutline.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = BentoLightLavender.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (selectedYearFilter == "الكل") "مؤشرات مجمّعة لكافة السنوات الدراسيّة" else "مؤشرات السنة الدراسية: $selectedYearFilter",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = BentoSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Year-specific teachers
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, BentoGrayOutline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("المستخدمون المعلمون", fontSize = 10.sp, color = BentoPrimaryDesc, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$filteredTeachersCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = BentoText)
                            }

                            // Year-specific total classes
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, BentoGrayOutline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("الأقسام المسجلة", fontSize = 10.sp, color = BentoPrimaryDesc, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$totalFilteredClasses", fontSize = 18.sp, fontWeight = FontWeight.Black, color = BentoText)
                            }
                        }

                        // Activated classes per term breakdown
                        Text(
                            text = "🔓 عدد الأقسام المفعلة حسب الفصل الدراسي:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Term 1
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("الفصل 1", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$term1ActivatedFilteredClasses قسم", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                            }

                            // Term 2
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("الفصل 2", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$term2ActivatedFilteredClasses قسم", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                            }

                            // Term 3
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("الفصل 3", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$term3ActivatedFilteredClasses قسم", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                            }

                            // Term 4 (Final Term Only)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFFFFFDE7), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("الأخير فقط ⭐", fontSize = 10.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$term4ActivatedFilteredClasses قسم", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFE65100))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                val classesByWilaya = remember(filteredClasses) {
                    filteredClasses
                        .groupBy { if (it.wilaya.trim().isBlank()) "غير محددة" else it.wilaya.trim() }
                        .mapValues { it.value.size }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🗺️ توزيع الأقسام حسب الولاية",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )

                    if (classesByWilaya.isEmpty()) {
                        Text(
                            text = "لا توجد أقسام مسجلة تحت أي ولاية حالياً.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BentoGrayOutline, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            classesByWilaya.entries.forEachIndexed { index, entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (index % 2 == 1) BentoLightLavender.copy(alpha = 0.15f) else Color.White)
                                        .padding(vertical = 10.dp, horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("📍", fontSize = 14.sp)
                                        Text(
                                            text = entry.key,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoText
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(BentoSecondaryContainer, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${entry.value} أقسام",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = BentoPrimary
                                        )
                                    }
                                }
                                if (index < classesByWilaya.size - 1) {
                                    HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, BentoGrayOutline.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = icon, fontSize = 20.sp)
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
            }
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun AppFooter(modifier: Modifier = Modifier, appLanguage: String = "ar") {
    Text(
        text = AppLocalization.tr("footer", "جميع الحقوق محفوظة © المطور: المختار اليدالي 🇲🇷 2026", appLanguage),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp)
    )
}

@Composable
fun DeveloperContactBar() {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoGrayOutline),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "تواصل ومتابعة جديد التطبيق 💡",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BentoPrimary,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "يمكنك التواصل مباشرة مع المطور للمساعدة، ومتابعة جديد تحديثات دفتر المعلم عبر منصاتنا الرسمية:",
                style = MaterialTheme.typography.bodySmall,
                color = BentoPrimaryDesc,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gmail Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:devyedali@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "تطبيق دفتر المعلم - استفسار")
                                }
                                context.startActivity(Intent.createChooser(intent, "إرسال بريد إلكتروني..."))
                            } catch (e: Exception) {
                                Toast.makeText(context, "لا يوجد تطبيق بريد إلكتروني مثبت", Toast.LENGTH_SHORT).show()
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.R.drawable.ic_gmail),
                            contentDescription = "Gmail",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "مراسلة المطور",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFC62828),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Facebook Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.facebook.com/share/1D9F6VUWDY/"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show()
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
                    border = BorderStroke(1.dp, Color(0xFFD2E3FC)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.R.drawable.ic_facebook),
                            contentDescription = "Facebook",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "صفحة الفيسبوك",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF1565C0),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // WhatsApp Channel Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://whatsapp.com/channel/0029Vb8xtQ80AgWAQMnIao1C"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show()
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = BorderStroke(1.dp, Color(0xFFC8E6C9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.R.drawable.ic_whatsapp),
                            contentDescription = "WhatsApp Channel",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "قناة الواتساب",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivationPromoDialog(
    email: String,
    classId: Long,
    className: String,
    academicYear: String,
    targetTermName: String? = null,
    isFinalTermOnlyFeatureEnabled: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var selectedPlan by remember { mutableStateOf("single") }

    val termId = remember(targetTermName) {
        when {
            targetTermName?.contains("الأول") == true || targetTermName?.contains("1") == true -> 1
            targetTermName?.contains("الثاني") == true || targetTermName?.contains("2") == true -> 2
            targetTermName?.contains("الثالث") == true || targetTermName?.contains("3") == true -> 3
            targetTermName?.contains("الرابع") == true || targetTermName?.contains("الأخير") == true || targetTermName?.contains("4") == true -> 4
            else -> 2
        }
    }

    val currentUidForRequest = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
    val requestId = remember(classId, termId, academicYear, selectedPlan) {
        val effectiveTermId = if (selectedPlan == "full") 23 else termId
        "${currentUidForRequest}_${classId}_${effectiveTermId}_${academicYear.replace("/", "-").replace(" ", "")}"
    }
    val activationLink = "https://diftar-almoaalim.web.app/activate/$requestId"

    val isFinalOnly = targetTermName?.contains("الأخير") == true
    val termLine = if (!targetTermName.isNullOrBlank()) "\nالفصل/الميزة المراد تفعيلها: $targetTermName" else ""

    val messageText = if (isFinalOnly) {
        "السلام عليكم،\n" +
                "أنا المعلم صاحب البريد: $email\n" +
                "أريد تفعيل مسار (استخدام الفصل الأخير فقط بـ 200 أوقية) للقسم: $className\n\n" +
                "🔑 رقم القسم الفريد (كود التفعيل):\n" +
                "$classId\n" +
                "------------------------------------\n" +
                "رابط طلب التفعيل:\n" +
                activationLink
    } else {
        if (selectedPlan == "full") {
            "السلام عليكم،\n" +
                    "أنا المعلم صاحب البريد: $email\n" +
                    "أريد تفعيل اشتراك جميع فصول السنة (الفصل الثاني والثالث) للقسم: $className (350 أوقية)\n\n" +
                    "🔑 رقم القسم الفريد (كود التفعيل):\n" +
                    "$classId\n" +
                    "------------------------------------\n" +
                    "رابط طلب التفعيل:\n" +
                    activationLink
        } else {
            val termPriceNote = when (termId) {
                2 -> " (190 أوقية)"
                3 -> " (190 أوقية)"
                else -> ""
            }
            "السلام عليكم،\n" +
                    "أنا المعلم صاحب البريد: $email\n" +
                    "أريد تفعيل اشتراك القسم: $className" + termLine + termPriceNote + "\n\n" +
                    "🔑 رقم القسم الفريد (كود التفعيل):\n" +
                    "$classId\n" +
                    "------------------------------------\n" +
                    "رابط طلب التفعيل:\n" +
                    activationLink
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "تنبيه",
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (isFinalOnly) "تفعيل مسار الفصل الأخير فقط 🔓" else "تفعيل القسم 🔓",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BentoPrimary
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isFinalOnly) {
                    Text(
                        text = "⭐ رسالة الاشتراك:\nقيمة تفعيل مسار (استخدام الفصل الأخير فقط) هي 200 أوقية جديدة للقسم.\nيرجى التواصل مع مدير الأقسام لتفعيل هذه الميزة.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoText
                    )

                    // Comparison info box only for Final Term Only
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4).copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, Color(0xFFFBC02D).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("💡 توضيح الفرق بين خياري الفصل الأخير:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFFE65100))
                            Text("• الفصل الثالث العادي: لرصد جميع مواد الفصل 3 بالاعتماد على نقاط الفصول السابقة لحساب المعدل السنوي.", fontSize = 10.5.sp, color = Color(0xFF37474F))
                            Text("• الفصل الأخير فقط ⭐: لرصد مواد الفصل الأخير فقط مع إدخال معدلي ف1 وف2 مباشرة.", fontSize = 10.5.sp, color = Color(0xFF37474F))
                            Text("• ملاحظة: لا يمكن تفعيل الخيارين معاً لنفس القسم في الفصل الأخير، وتفعيل أحدهما يلغي الآخر تلقائياً.", fontSize = 10.sp, color = Color(0xFFD84315), fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5).copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🏷️", fontSize = 16.sp)
                                Text("أسعار تفعيل فصول القسم:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoPrimary)
                            }
                            Text("• الفصل الأول: مجاني دائماً وبدون قفل 🎁", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            Text("• تفعيل الفصل الثاني: 190 أوقية جديدة", fontSize = 12.sp, color = BentoText, fontWeight = FontWeight.Medium)
                            Text("• تفعيل الفصل الثالث: 190 أوقية جديدة", fontSize = 12.sp, color = BentoText, fontWeight = FontWeight.Medium)
                            Text("• تفعيل جميع فصول السنة معاً: 350 أوقية جديدة", fontSize = 12.5.sp, color = BentoPrimary, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Text(
                    text = "اضغط على زر تواصل على الواتساب الموضح بالأسفل وسيتم توجيهك مباشرة وتجهيز بيانات تفعيل الاشتراك تلقائياً.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoPrimaryDesc
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = BentoLightLavender.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BentoGrayOutline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "📧 البريد: $email", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "🏫 القسم: $className", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        val displayedTargetTerm = if (selectedPlan == "full") "جميع فصول السنة (الثاني والثالث)" else targetTermName
                        if (!displayedTargetTerm.isNullOrBlank()) {
                            Text(text = "📚 الفصل المراد تفعيله: $displayedTargetTerm", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BentoPrimary)
                        }

                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("🔑 رقم القسم الفريد (الكود):", fontSize = 10.sp, color = BentoPrimaryDesc, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "$classId",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BentoPrimary,
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(classId.toString()))
                                        android.widget.Toast.makeText(context, "تم نسخ رقم القسم الفريد ($classId) بنجاح! 📋", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(14.dp), tint = BentoPrimary)
                                        Text("نسخ الكود 📋", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                if (termId == 2 && !isFinalOnly) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "اختر نوع الاشتراك:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = BentoPrimary
                            )

                            // الخيار الأول
                            Surface(
                                onClick = { selectedPlan = "single" },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedPlan == "single") BentoPrimary.copy(alpha = 0.08f) else Color.Transparent,
                                border = BorderStroke(
                                    width = if (selectedPlan == "single") 1.5.dp else 1.dp,
                                    color = if (selectedPlan == "single") BentoPrimary else Color.LightGray.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    androidx.compose.material3.RadioButton(
                                        selected = selectedPlan == "single",
                                        onClick = { selectedPlan = "single" },
                                        colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                            selectedColor = BentoPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "الفصل الثاني فقط (190 أوقية)",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedPlan == "single") FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedPlan == "single") BentoPrimary else BentoText
                                    )
                                }
                            }

                            // الخيار الثاني
                            Surface(
                                onClick = { selectedPlan = "full" },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedPlan == "full") BentoPrimary.copy(alpha = 0.08f) else Color.Transparent,
                                border = BorderStroke(
                                    width = if (selectedPlan == "full") 1.5.dp else 1.dp,
                                    color = if (selectedPlan == "full") BentoPrimary else Color.LightGray.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    androidx.compose.material3.RadioButton(
                                        selected = selectedPlan == "full",
                                        onClick = { selectedPlan = "full" },
                                        colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                            selectedColor = BentoPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "جميع فصول السنة - الثاني والثالث (350 أوقية)",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedPlan == "full") FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedPlan == "full") BentoPrimary else BentoText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        val requestDoc = mapOf(
                            "requestId" to requestId,
                            "userId" to currentUid,
                            "classId" to classId,
                            "termId" to termId,
                            "status" to "pending",
                            "createdAt" to System.currentTimeMillis(),
                            "className" to className,
                            "termName" to (targetTermName ?: "الفصل الثاني"),
                            "email" to email
                        )
                        db.collection("activation_requests").document(requestId).set(requestDoc)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        val encodedMessage = android.net.Uri.encode(messageText)
                        val url = "https://api.whatsapp.com/send?phone=22237786585&text=$encodedMessage"
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "لم نتمكن من فتح الواتساب، يرجى التأكد من توفر التطبيق على جهازك.", android.widget.Toast.LENGTH_LONG).show()
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "WhatsApp",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("تواصل على الواتساب", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)

// ==========================================
// ABOUT APP SCREEN (صفحة حول التطبيق ودليل الاستخدام)
// ==========================================
@Composable
fun AboutAppScreen(
    appLanguage: String = "ar"
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. App Banner & Identity Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(76.dp),
                        shape = CircleShape,
                        color = BentoPrimary.copy(alpha = 0.1f)
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.teacher_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "دفتر المعلم - Teacher Notebook",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = BentoText
                    )
                    Text(
                        text = "الإصدار المطور (2026 - 2027)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = BentoPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تطبيق ذكي وشامل مخصص للمعلمين لإدارة الأقسام المدرسية، رصد الدرجات، وحساب المعدلات والنتائج السنوية بدقة، مع حفظ تلقائي للبيانات في Google Drive وإمكانيات الطباعة المباشرة.",
                        fontSize = 11.5.sp,
                        textAlign = TextAlign.Center,
                        color = BentoPrimaryDesc,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        item {
        }

        // 2. Official Social Channels (قناة الواتساب وصفحة الفيسبوك)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📢 التواصل والقنوات الرسمية للتطبيق",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = BentoText
                    )
                    Text(
                        text = "انضم إلى مجتمع المعلمين وتابع الإعلانات والتحديثات مباشرة:",
                        fontSize = 11.5.sp,
                        color = BentoPrimaryDesc
                    )

                    // WhatsApp Channel Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://whatsapp.com/channel/0029Vb8xtQ80AgWAQMnIao1C"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show()
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.ic_whatsapp),
                                contentDescription = "WhatsApp Channel",
                                modifier = Modifier.size(32.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "قناة الواتساب الرسمية 💬",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = "اضغط للانضمام ومتابعة جديد التحديثات والإرشادات",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Telegram Channel Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/diftar_moualim"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show()
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
                        border = BorderStroke(1.dp, Color(0xFF0288D1))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.ic_telegram),
                                contentDescription = "Telegram Channel",
                                modifier = Modifier.size(32.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "قناة التلغرام الرسمية ✈️",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF01579B)
                                )
                                Text(
                                    text = "اضغط للانضمام ومتابعة الإعلانات والملفات والتحديثات",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF0288D1)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF0288D1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Facebook Page Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/share/1D9F6VUWDY/"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show()
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        border = BorderStroke(1.dp, Color(0xFF1565C0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.ic_facebook),
                                contentDescription = "Facebook Page",
                                modifier = Modifier.size(32.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "صفحة الفيسبوك الرسمية 👍",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0D47A1)
                                )
                                Text(
                                    text = "اضغط للمتابعة والتواصل وطرح المقترحات والأسئلة",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF1565C0)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF1565C0),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // YouTube Channel Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/@devyedali?si=5gxsOaP5eyYMhkQz"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show()
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        border = BorderStroke(1.dp, Color(0xFFD32F2F))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.ic_youtube),
                                contentDescription = "YouTube Channel",
                                modifier = Modifier.size(32.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "قناة اليوتيوب الرسمية 🎥",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFB71C1C)
                                )
                                Text(
                                    text = "اضغط لمتابعة الشروحات والتحديثات والفيديوهات التوضيحية",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFC62828)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    // Official Website Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://diftar-almoaalim.web.app/"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show()
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                        border = BorderStroke(1.dp, Color(0xFF7B1FA2))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = Color(0xFF7B1FA2).copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🌐", fontSize = 18.sp)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "موقع التطبيق الرسمي 🌐",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF4A148C)
                                )
                                Text(
                                    text = "https://diftar-almoaalim.web.app",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF7B1FA2)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF7B1FA2),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SingleRequestActivationView(
    requestId: String,
    viewModel: TeacherViewModel,
    currentUser: com.google.firebase.auth.FirebaseUser?,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isAdmin = currentUser?.email?.trim()?.lowercase() == "elyedalimoctar@gmail.com"

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Text(
                        text = "طلب تفعيل ذكي 🔓",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = BentoPrimary
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isAdmin) {
                // Not Admin Screen
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BentoGrayOutline, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEBEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "تنبيه الصلاحيات ⛔",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.Red
                        )

                        Text(
                            text = "هذا الرابط مخصص لمدير الأقسام فقط.\nيرجى تسجيل الدخول بحساب المشرف الرئيسي للتمكن من التفعيل.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = BentoPrimaryDesc,
                            lineHeight = 22.sp
                        )

                        Button(
                            onClick = onClose,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("العودة إلى التطبيق 🏠", fontWeight = FontWeight.Black)
                        }
                    }
                }
            } else {
                // Admin Flow
                var requestData by remember { mutableStateOf<Map<String, Any>?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var isActivating by remember { mutableStateOf(false) }

                LaunchedEffect(requestId) {
                    isLoading = true
                    errorMessage = null
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("activation_requests").document(requestId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                requestData = document.data
                            } else {
                                errorMessage = "طلب التفعيل هذا غير موجود أو منتهي الصلاحية!"
                            }
                            isLoading = false
                        }
                        .addOnFailureListener { e ->
                            errorMessage = "فشل تحميل تفاصيل الطلب: ${e.localizedMessage}"
                            isLoading = false
                        }
                }

                if (isLoading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = BentoSecondary)
                        Text("جاري تحميل تفاصيل طلب التفعيل...", color = BentoPrimaryDesc, fontSize = 14.sp)
                    }
                } else if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BentoGrayOutline, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "خطأ في المعالجة ⚠️",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = BentoPrimaryDesc
                            )
                            Button(
                                onClick = onClose,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("إغلاق والعودة 🏠", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    val className = requestData?.get("className") as? String ?: "غير محدد"
                    val teacherEmail = requestData?.get("email") as? String ?: "غير محدد"
                    val requestedTerm = requestData?.get("termName") as? String ?: "غير محدد"
                    val requestIdParts = requestId.split("_")
                    val isFullYearRequest = (requestIdParts.size >= 2 && requestIdParts[requestIdParts.size - 2] == "23") ||
                        ((requestData?.get("termId") as? Number)?.toInt() == 23)
                    val displayedTerm = if (isFullYearRequest) "جميع فصول السنة (الفصلان الثاني والثالث)" else requestedTerm
                    val status = requestData?.get("status") as? String ?: "pending"
                    val createdAt = requestData?.get("createdAt") as? Long ?: 0L

                    val dateStr = if (createdAt > 0L) {
                        val sdf = java.text.SimpleDateFormat("yyyy/MM/dd hh:mm a", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(createdAt))
                    } else {
                        "-"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BentoGrayOutline, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "تفاصيل طلب التفعيل 📄",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = BentoPrimary
                            )

                            HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.5f))

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("بريد المعلم:", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc, fontSize = 14.sp)
                                    Text(teacherEmail, fontWeight = FontWeight.Medium, color = BentoPrimary, fontSize = 14.sp)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("اسم القسم:", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc, fontSize = 14.sp)
                                    Text(className, fontWeight = FontWeight.Medium, color = BentoPrimary, fontSize = 14.sp)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("الفصل المطلوب:", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc, fontSize = 14.sp)
                                    Text(displayedTerm, fontWeight = FontWeight.Medium, color = BentoPrimary, fontSize = 14.sp)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("تاريخ الطلب:", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc, fontSize = 14.sp)
                                    Text(dateStr, fontWeight = FontWeight.Medium, color = BentoPrimary, fontSize = 14.sp)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("حالة الطلب:", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc, fontSize = 14.sp)
                                    val isPending = status == "pending"
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isPending) Color(0xFFFFF3E0) else Color(0xFFE8F5E9))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isPending) "قيد الانتظار ⏳" else "مكتمل ومفعّل ✅",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPending) Color(0xFFE65100) else Color(0xFF2E7D32),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (status == "pending") {
                                Button(
                                    onClick = {
                                        isActivating = true
                                        val tId = requestData?.get("userId") as? String ?: ""
                                        val cId = requestData?.get("classId") as? Long ?: 0L
                                        val termId = when (val t = requestData?.get("termId")) {
                                            is Long -> t.toInt()
                                            is Int -> t
                                            is Double -> t.toInt()
                                            else -> 2
                                        }

                                        val onActivationSuccess: () -> Unit = {
                                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            db.collection("activation_requests").document(requestId).update("status", "completed")
                                                .addOnSuccessListener {
                                                    isActivating = false
                                                    val updatedMap = requestData?.toMutableMap() ?: mutableMapOf()
                                                    updatedMap["status"] = "completed"
                                                    requestData = updatedMap
                                                    android.widget.Toast.makeText(context, "تم تفعيل الاشتراك وتحديث الطلب بنجاح! 🎉", android.widget.Toast.LENGTH_LONG).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    isActivating = false
                                                    errorMessage = "تم التفعيل بنجاح، لكن فشل تحديث حالة الرابط سحابياً: ${e.localizedMessage}"
                                                }
                                        }

                                        val onActivationFailure: (String) -> Unit = { err ->
                                            isActivating = false
                                            errorMessage = "فشل التفعيل: $err"
                                        }

                                        if (isFullYearRequest) {
                                            viewModel.activateAllYearTerms(
                                                teacherId = tId,
                                                classId = cId,
                                                onSuccess = onActivationSuccess,
                                                onFailure = onActivationFailure
                                            )
                                        } else {
                                            viewModel.toggleClassTermActivation(
                                                teacherId = tId,
                                                classId = cId,
                                                termId = termId,
                                                currentTermStatus = false,
                                                onSuccess = onActivationSuccess,
                                                onFailure = onActivationFailure
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoSecondary),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isActivating
                                ) {
                                    if (isActivating) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Text("تفعيل الاشتراك الآن 🔓", fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = onClose,
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("تم التفعيل مسبقاً - إغلاق 🏠", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun NotificationsListScreen(
    notifications: List<Map<String, Any>>,
    onClose: () -> Unit,
    onMarkAsRead: (String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onDeleteAll: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Text(
                        text = "الإشعارات 🔔",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (notifications.any { !(it["read"] as? Boolean ?: false) }) {
                        TextButton(onClick = onMarkAllAsRead) {
                            Text(
                                text = "تحديد الكل كمقروء",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (notifications.isNotEmpty()) {
                        TextButton(onClick = onDeleteAll) {
                            Text(
                                text = "مسح الكل",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = BentoPrimary
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "لا توجد إشعارات بعد",
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimaryDesc,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notif ->
                    val notifId = notif["notificationId"] as? String ?: ""
                    val title = notif["title"] as? String ?: ""
                    val message = notif["message"] as? String ?: ""
                    val isRead = notif["read"] as? Boolean ?: false
                    val createdAt = notif["createdAt"]
                    
                    val dateStr = if (createdAt is com.google.firebase.Timestamp) {
                        val sdf = java.text.SimpleDateFormat("yyyy/MM/dd hh:mm a", java.util.Locale.getDefault())
                        sdf.format(createdAt.toDate())
                    } else if (createdAt is Long && createdAt > 0L) {
                        val sdf = java.text.SimpleDateFormat("yyyy/MM/dd hh:mm a", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(createdAt))
                    } else {
                        "-"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isRead) {
                                    onMarkAsRead(notifId)
                                }
                            }
                            .border(
                                width = 1.dp,
                                color = if (isRead) BentoGrayOutline else BentoSecondary.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isRead) Color.White else BentoSecondary.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isRead) Color(0xFFF1F5F9) else BentoSecondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (isRead) Color.Gray else BentoSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = title,
                                        fontWeight = if (isRead) FontWeight.Bold else FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = BentoPrimary
                                    )
                                    if (!isRead) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(BentoSecondary)
                                        )
                                    }
                                }
                                Text(
                                    text = message,
                                    fontSize = 13.sp,
                                    color = BentoPrimaryDesc,
                                    lineHeight = 18.sp
                                )
                                Text(
                                    text = dateStr,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRequestApprovalView(
    requestId: String,
    viewModel: TeacherViewModel,
    currentUser: com.google.firebase.auth.FirebaseUser?,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isAdmin = currentUser?.email?.trim()?.lowercase() == "elyedalimoctar@gmail.com"

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Text(
                        text = "طلب نشر إعلان تبادل 🤝",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = BentoPrimary
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isAdmin) {
                // Not Admin Screen
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BentoGrayOutline, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEBEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "تنبيه الصلاحيات ⛔",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.Red
                        )

                        Text(
                            text = "هذا الرابط مخصص لمدير الأقسام فقط للموافقة على نشر إعلان التبادل.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = BentoPrimaryDesc,
                            lineHeight = 22.sp
                        )

                        Button(
                            onClick = onClose,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("العودة إلى التطبيق 🏠", fontWeight = FontWeight.Black)
                        }
                    }
                }
            } else {
                // Admin Flow
                var requestData by remember { mutableStateOf<Map<String, Any>?>(null) }
                var postData by remember { mutableStateOf<Map<String, Any>?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var isApproving by remember { mutableStateOf(false) }

                LaunchedEffect(requestId) {
                    isLoading = true
                    errorMessage = null
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("exchange_activation_requests").document(requestId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val reqData = document.data
                                requestData = reqData
                                val postId = reqData?.get("exchangePostId") as? String ?: ""
                                if (postId.isNotBlank()) {
                                    db.collection("teacher_exchanges").document(postId).get()
                                        .addOnSuccessListener { postDoc ->
                                            if (postDoc.exists()) {
                                                postData = postDoc.data
                                            } else {
                                                errorMessage = "منشور التبادل المرتبط بهذا الطلب غير موجود!"
                                            }
                                            isLoading = false
                                        }
                                        .addOnFailureListener { e ->
                                            errorMessage = "فشل تحميل تفاصيل الإعلان: ${e.localizedMessage}"
                                            isLoading = false
                                        }
                                } else {
                                    errorMessage = "معرف الإعلان المرتبط بالطلب غير صالح!"
                                    isLoading = false
                                }
                            } else {
                                errorMessage = "طلب موافقة التبادل هذا غير موجود!"
                                isLoading = false
                            }
                        }
                        .addOnFailureListener { e ->
                            errorMessage = "فشل تحميل تفاصيل الطلب: ${e.localizedMessage}"
                            isLoading = false
                        }
                }

                if (isLoading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = BentoSecondary)
                        Text("جاري تحميل تفاصيل طلب الموافقة...", color = BentoPrimaryDesc, fontSize = 14.sp)
                    }
                } else if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BentoGrayOutline, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "خطأ في المعالجة ⚠️",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = BentoPrimaryDesc
                            )
                            Button(
                                onClick = onClose,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("إغلاق والعودة 🏠", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    val status = requestData?.get("status") as? String ?: "pending"
                    val authorName = postData?.get("authorName") as? String ?: "غير محدد"
                    val authorEmail = postData?.get("authorEmail") as? String ?: "غير محدد"
                    val offerType = postData?.get("offerType") as? String ?: "غير محدد"
                    val specialty = postData?.get("specialty") as? String ?: "غير محدد"
                    val currentWilaya = postData?.get("currentWilaya") as? String ?: "غير محدد"
                    val currentMoughataa = postData?.get("currentMoughataa") as? String ?: "غير محدد"
                    val schoolName = postData?.get("schoolName") as? String ?: "غير محدد"
                    val targetWilayas = postData?.get("targetWilayas") as? String ?: "غير محدد"
                    val postId = requestData?.get("exchangePostId") as? String ?: ""

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BentoGrayOutline, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text(
                                    text = "طلب الموافقة على التبادل 📄",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = BentoPrimary
                                )
                                HorizontalDivider(color = BentoGrayOutline.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))
                            }

                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("اسم المعلم:", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc, fontSize = 14.sp)
                                        Text(authorName, fontWeight = FontWeight.Medium, color = BentoPrimary, fontSize = 14.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("بريد المعلم:", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc, fontSize = 14.sp)
                                        Text(authorEmail, fontWeight = FontWeight.Medium, color = BentoPrimary, fontSize = 14.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("نوع الإعلان:", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc, fontSize = 14.sp)
                                        Text(offerType, fontWeight = FontWeight.Medium, color = BentoPrimary, fontSize = 14.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("التخصص:", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc, fontSize = 14.sp)
                                        Text(specialty, fontWeight = FontWeight.Medium, color = BentoPrimary, fontSize = 14.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("الولاية الحالية:", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc, fontSize = 14.sp)
                                        Text(currentWilaya, fontWeight = FontWeight.Medium, color = BentoPrimary, fontSize = 14.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("المقاطعة الحالية:", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc, fontSize = 14.sp)
                                        Text(currentMoughataa, fontWeight = FontWeight.Medium, color = BentoPrimary, fontSize = 14.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("المدرسة:", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc, fontSize = 14.sp)
                                        Text(schoolName, fontWeight = FontWeight.Medium, color = BentoPrimary, fontSize = 14.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("الولايات المستهدفة:", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc, fontSize = 14.sp)
                                        Text(targetWilayas, fontWeight = FontWeight.Medium, color = BentoPrimary, fontSize = 14.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("حالة الطلب:", fontWeight = FontWeight.Bold, color = BentoPrimaryDesc, fontSize = 14.sp)
                                        val isPending = status == "pending"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isPending) Color(0xFFFFF3E0) else Color(0xFFE8F5E9))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isPending) "قيد الانتظار ⏳" else "تمت الموافقة والنشر مسبقاً ✅",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPending) Color(0xFFE65100) else Color(0xFF2E7D32),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            item {
                                if (status == "pending") {
                                    Button(
                                        onClick = {
                                            isApproving = true
                                            viewModel.approveExchangePost(
                                                postId = postId,
                                                isApproved = true,
                                                onSuccess = {
                                                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                    db.collection("exchange_activation_requests").document(requestId).update("status", "completed")
                                                        .addOnSuccessListener {
                                                            isApproving = false
                                                            val updatedMap = requestData?.toMutableMap() ?: mutableMapOf()
                                                            updatedMap["status"] = "completed"
                                                            requestData = updatedMap
                                                            android.widget.Toast.makeText(context, "تمت الموافقة على إعلان التبادل ونشره بنجاح! 🎉", android.widget.Toast.LENGTH_LONG).show()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            isApproving = false
                                                            errorMessage = "تم النشر بنجاح، لكن فشل تحديث حالة الرابط سحابياً: ${e.localizedMessage}"
                                                        }
                                                },
                                                onFailure = { err ->
                                                    isApproving = false
                                                    errorMessage = "فشل النشر والموافقة: $err"
                                                }
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BentoSecondary),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = !isApproving
                                    ) {
                                        if (isApproving) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                        } else {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Text("الموافقة على النشر للعامة ✅", fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = onClose,
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("تم النشر مسبقاً - إغلاق 🏠", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

