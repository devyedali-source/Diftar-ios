package com.example.ui

import com.example.compat.*
import kotlinx.coroutines.IO

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.TeacherDatabase
import com.example.data.models.ClassSection
import com.example.data.models.Grade
import com.example.data.models.Student
import com.example.data.models.Subject
import com.example.data.models.sortedByOfficialOrder
import com.example.data.models.ClassSubjectCustomization
import com.example.data.models.TeacherExchangePost
import com.example.data.models.LocalNotification
import com.example.data.repository.TeacherRepository
import com.example.data.backup.GoogleDriveBackupManager
import com.example.data.backup.BackupResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.tasks.Task

// Data structure representing a student's performance (Average, Rank, Subject Grades)
data class StudentPerformance(
    val student: Student,
    val averageScore: Double,
    val rank: Int,
    val gradesMap: Map<Long, Double> // subjectId to score
)

// Data structures representing results entry registration progress
enum class SyncContext {
    GENERAL,
    LOGIN,
    LOGOUT
}

data class UnifiedSyncUiState(
    val isShowing: Boolean = false,
    val progress: Int = 0,
    val message: String = "",
    val title: String = "تحديث ومزامنة البيانات 🔄☁️",
    val isFinished: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val syncContext: SyncContext = SyncContext.GENERAL
)

data class TermProgress(
    val termId: Int,
    val name: String,
    val enteredCount: Int,
    val totalRequired: Int,
    val percentage: Float
)

data class AnnualProgress(
    val termProgresses: List<TermProgress>,
    val totalEntered: Int,
    val totalRequired: Int,
    val percentage: Float
)

class TeacherViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("teacher_settings_prefs", android.content.Context.MODE_PRIVATE)

    private val _appLanguage = MutableStateFlow("ar")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun setAppLanguage(lang: String) {
        _appLanguage.value = "ar"
        prefs.edit().putString("app_language", "ar").apply()
    }

    private var currentRepository: TeacherRepository? = null

    private val activeDatabase: TeacherDatabase
        get() = TeacherDatabase.getDatabase(getApplication(), _currentUser.value?.uid)

    private val repository: TeacherRepository
        get() {
            val repo = currentRepository
            if (repo != null) return repo
            val newRepo = TeacherRepository(activeDatabase.teacherDao())
            currentRepository = newRepo
            return newRepo
        }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _exchangePosts = MutableStateFlow<List<TeacherExchangePost>>(emptyList())
    val exchangePosts: StateFlow<List<TeacherExchangePost>> = _exchangePosts.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(prefs.getLong("last_sync_time", 0L))
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _isGlobalLoading = MutableStateFlow(false)
    val isGlobalLoading: StateFlow<Boolean> = _isGlobalLoading.asStateFlow()

    private val _loadingMessage = MutableStateFlow("جاري العمل...")
    val loadingMessage: StateFlow<String> = _loadingMessage.asStateFlow()

    fun showLoading(message: String = "جاري العمل...") {
        _loadingMessage.value = message
        _isGlobalLoading.value = true
    }

    fun hideLoading() {
        _isGlobalLoading.value = false
    }

    fun runWithLoading(message: String = "جاري العمل...", action: suspend () -> Unit) {
        viewModelScope.launch {
            showLoading(message)
            try {
                action()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                hideLoading()
            }
        }
    }

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _showCancelLogoutButton = MutableStateFlow(false)
    val showCancelLogoutButton: StateFlow<Boolean> = _showCancelLogoutButton.asStateFlow()

    private val _isBackupRestoreLoading = MutableStateFlow(false)
    val isBackupRestoreLoading: StateFlow<Boolean> = _isBackupRestoreLoading.asStateFlow()

    private val _driveProgressPercent = MutableStateFlow(0)
    val driveProgressPercent: StateFlow<Int> = _driveProgressPercent.asStateFlow()

    private val _driveProgressMessage = MutableStateFlow("")
    val driveProgressMessage: StateFlow<String> = _driveProgressMessage.asStateFlow()

    private val _lastDriveBackupTime = MutableStateFlow(prefs.getLong("last_google_drive_backup_time", 0L))
    val lastDriveBackupTime: StateFlow<Long> = _lastDriveBackupTime.asStateFlow()

    private val _pendingDriveConsentIntent = MutableStateFlow<android.content.Intent?>(null)
    val pendingDriveConsentIntent: StateFlow<android.content.Intent?> = _pendingDriveConsentIntent.asStateFlow()

    fun clearPendingDriveConsentIntent() {
        _pendingDriveConsentIntent.value = null
    }

    fun performGoogleDriveSync(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isBackupRestoreLoading.value = true
            _driveProgressPercent.value = 5
            _driveProgressMessage.value = "جاري بدء عمليات المزامنة..."
            showLoading("جاري مزامنة البيانات مع Google Drive ☁️🔄...")
            try {
                val backupManager = GoogleDriveBackupManager(getApplication(), repository)
                val curUser = _currentUser.value

                _driveProgressMessage.value = "1/2: جاري رفع النسخة الاحتياطية..."
                val backupResult = backupManager.createAndUploadBackup(
                    currentUserId = curUser?.uid,
                    currentUserEmail = curUser?.email,
                    onProgress = { percent, msg ->
                        _driveProgressPercent.value = (percent * 0.5f).toInt()
                        _driveProgressMessage.value = "1/2: $msg"
                    }
                )

                if (backupResult is BackupResult.Error) {
                    onResult(false, backupResult.errorMessage)
                    return@launch
                }

                val now = System.currentTimeMillis()
                prefs.edit().putLong("last_google_drive_backup_time", now).apply()
                _lastDriveBackupTime.value = now

                _driveProgressMessage.value = "2/2: جاري استعادة وتحديث البيانات..."
                val restoreResult = backupManager.downloadAndRestoreBackup(
                    currentUserId = curUser?.uid,
                    currentUserEmail = curUser?.email,
                    onProgress = { percent, msg ->
                        _driveProgressPercent.value = 50 + (percent * 0.5f).toInt()
                        _driveProgressMessage.value = "2/2: $msg"
                    }
                )

                when (restoreResult) {
                    is BackupResult.Success -> {
                        updateActiveRepositoryAndCollect(_currentUser.value?.uid)
                        onResult(true, "تمت المزامنة بنجاح مع Google Drive (رفع واستعادة) 🎉")
                    }
                    is BackupResult.Error -> {
                        onResult(true, "تم رفع النسخة الاحتياطية بنجاح إلى Google Drive 📤")
                    }
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "حدث خطأ غير متوقع أثناء عملية المزامنة.")
            } finally {
                _isBackupRestoreLoading.value = false
                hideLoading()
            }
        }
    }

    private val _logoutConflictMessage = MutableStateFlow<String?>(null)
    val logoutConflictMessage: StateFlow<String?> = _logoutConflictMessage.asStateFlow()

    fun clearLogoutConflictMessage() {
        _logoutConflictMessage.value = null
    }

    fun performGoogleDriveBackup(silent: Boolean = false, isLogout: Boolean = false, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (!silent) {
                _isBackupRestoreLoading.value = true
                _driveProgressPercent.value = 5
                _driveProgressMessage.value = "جاري تحضير عمليات النسخ الاحتياطي..."
                showLoading("جاري إنشاء ورفع النسخة الاحتياطية إلى Google Drive 📤☁️...")
            }
            try {
                val backupManager = GoogleDriveBackupManager(getApplication(), repository)
                val curUser = _currentUser.value
                val result = backupManager.createAndUploadBackup(
                    currentUserId = curUser?.uid,
                    currentUserEmail = curUser?.email,
                    isLogout = isLogout,
                    onProgress = { percent, msg ->
                        _driveProgressPercent.value = percent
                        _driveProgressMessage.value = msg
                    }
                )
                when (result) {
                    is BackupResult.Success -> {
                        val now = System.currentTimeMillis()
                        prefs.edit().putLong("last_google_drive_backup_time", now).apply()
                        _lastDriveBackupTime.value = now
                        onResult(true, result.message)
                    }
                    is BackupResult.Error -> {
                        val cause = result.throwable
                        if (cause is com.example.data.backup.GoogleDriveConsentRequiredException) {
                            _pendingDriveConsentIntent.value = cause.consentIntent
                        }
                        onResult(false, result.errorMessage)
                    }
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "حدث خطأ غير متوقع أثناء عملية النسخ الاحتياطي.")
            } finally {
                if (!silent) {
                    _isBackupRestoreLoading.value = false
                    hideLoading()
                }
            }
        }
    }

    private var isAutoDriveBackupRunning = false

    fun performAutoDriveBackup(minIntervalMs: Long = 3600000L) {
        if (isAutoDriveBackupRunning) return
        if (!isNetworkAvailable()) return
        val user = _currentUser.value ?: FirebaseAuth.getInstance().currentUser ?: return
        val lastBackupTime = prefs.getLong("last_google_drive_backup_time", 0L)
        val now = System.currentTimeMillis()
        if (now - lastBackupTime < minIntervalMs) return

        isAutoDriveBackupRunning = true

        fun attemptBackup(attempt: Int) {
            if (!isNetworkAvailable()) {
                isAutoDriveBackupRunning = false
                return
            }
            performGoogleDriveBackup(silent = true) { success, _ ->
                if (success) {
                    isAutoDriveBackupRunning = false
                } else {
                    if (attempt == 1) {
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(120000L)
                            if (!isNetworkAvailable()) {
                                isAutoDriveBackupRunning = false
                                return@launch
                            }
                            attemptBackup(attempt = 2)
                        }
                    } else if (attempt == 2) {
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(600000L)
                            if (!isNetworkAvailable()) {
                                isAutoDriveBackupRunning = false
                                return@launch
                            }
                            attemptBackup(attempt = 3)
                        }
                    } else {
                        isAutoDriveBackupRunning = false
                    }
                }
            }
        }

        attemptBackup(attempt = 1)
    }

    private suspend fun syncActivationStatesFromServer() {
        try {
            val uid = _currentUser.value?.uid ?: return
            if (!isNetworkAvailable()) return

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val db = FirebaseFirestore.getInstance()
                val snapshot = com.google.android.gms.tasks.Tasks.await(
                    db.collection("teachers").document(uid).collection("classes").get()
                )
                for (doc in snapshot.documents) {
                    val classId = doc.getLong("id") ?: doc.getString("id")?.toLongOrNull() ?: doc.id.toLongOrNull() ?: continue
                    val localClass = _classSections.value.firstOrNull { it.id == classId }
                    val expired = isTermActivationExpired(localClass?.academicYear ?: "")
                    val isAct = doc.getBoolean("isActivated") ?: false
                    val isT1 = doc.getBoolean("isTerm1Activated") ?: true
                    val isT2 = (doc.getBoolean("isTerm2Activated") ?: false) && !expired
                    val isT3 = (doc.getBoolean("isTerm3Activated") ?: false) && !expired
                    val isT4 = (doc.getBoolean("isTerm4Activated") ?: false) && !expired
                    if (localClass != null && (
                        localClass.isActivated != isAct ||
                        localClass.isTerm1Activated != isT1 ||
                        localClass.isTerm2Activated != isT2 ||
                        localClass.isTerm3Activated != isT3 ||
                        localClass.isTerm4Activated != isT4
                    )) {
                        val updatedSection = localClass.copy(
                            isActivated = isAct,
                            isTerm1Activated = isT1,
                            isTerm2Activated = isT2,
                            isTerm3Activated = isT3,
                            isTerm4Activated = isT4
                        )
                        repository.insertClassSection(updatedSection)
                    }
                }
            }
        } catch (e: Exception) {
            // اخرج بصمت
        }
    }

    fun performGoogleDriveRestore(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isBackupRestoreLoading.value = true
            _driveProgressPercent.value = 5
            _driveProgressMessage.value = "جاري تحضير عمليات الاستعادة من Google Drive..."
            showLoading("جاري استعادة النسخة الاحتياطية من Google Drive 📥...")
            try {
                val backupManager = GoogleDriveBackupManager(getApplication(), repository)
                val curUser = _currentUser.value
                val result = backupManager.downloadAndRestoreBackup(
                    currentUserId = curUser?.uid,
                    currentUserEmail = curUser?.email,
                    onProgress = { percent, msg ->
                        _driveProgressPercent.value = percent
                        _driveProgressMessage.value = msg
                    }
                )
                when (result) {
                    is BackupResult.Success -> {
                        updateActiveRepositoryAndCollect(_currentUser.value?.uid)
                        syncActivationStatesFromServer()
                        onResult(true, result.message)
                    }
                    is BackupResult.Error -> {
                        onResult(false, result.errorMessage)
                    }
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "حدث خطأ غير متوقع أثناء عملية الاستعادة.")
            } finally {
                _isBackupRestoreLoading.value = false
                hideLoading()
            }
        }
    }

    fun performAutoRestoreIfEmpty(onRestored: (String) -> Unit) {
        val user = _currentUser.value ?: FirebaseAuth.getInstance().currentUser ?: return
        if (!isNetworkAvailable()) return
        val restoreKey = "auto_restore_done_${user.uid}"
        if (prefs.getBoolean(restoreKey, false)) return
        if (_classSections.value.isNotEmpty()) return

        var loadingShown = false
        _driveProgressPercent.value = 0
        val backupManager = GoogleDriveBackupManager(getApplication(), repository)
        viewModelScope.launch {
            try {
                val dbIsEmpty = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        repository.getAllClassSectionsList().isEmpty() &&
                            repository.getAllStudentsList().isEmpty() &&
                            repository.getAllGradesList().isEmpty()
                    } catch (e: Exception) {
                        false
                    }
                }
                if (!dbIsEmpty) {
                    return@launch
                }
                val result = backupManager.downloadAndRestoreBackup(
                    currentUserId = user.uid,
                    currentUserEmail = user.email,
                    onProgress = { percent, msg ->
                        if (!loadingShown) {
                            showLoading(msg)
                            loadingShown = true
                        }
                        _driveProgressPercent.value = percent
                        _driveProgressMessage.value = msg
                    }
                )
                if (result is BackupResult.Success) {
                    updateActiveRepositoryAndCollect(user.uid)
                    prefs.edit().putBoolean(restoreKey, true).apply()
                    try {
                        syncActivationStatesFromServer()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val lastBackup = prefs.getLong("last_google_drive_backup_time", 0L)
                    if (lastBackup > 0L) {
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US)
                        val dateFormatted = sdf.format(java.util.Date(lastBackup))
                        onRestored(dateFormatted)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (loadingShown) {
                    hideLoading()
                }
            }
        }
    }

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _syncUiState = MutableStateFlow(UnifiedSyncUiState())
    val syncUiState: StateFlow<UnifiedSyncUiState> = _syncUiState.asStateFlow()

    private val _classSections = MutableStateFlow<List<ClassSection>>(emptyList())
    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    private val personalSubjectsList = MutableStateFlow<List<Subject>>(emptyList())
    private val globalSubjectsList = MutableStateFlow<List<Subject>>(emptyList())
    private val deletedGlobalSubjectIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    private val _grades = MutableStateFlow<List<Grade>>(emptyList())
    private val _manualTermAverages = MutableStateFlow<List<com.example.data.models.StudentManualTermAverage>>(emptyList())
    val manualTermAverages: StateFlow<List<com.example.data.models.StudentManualTermAverage>> = _manualTermAverages.asStateFlow()
    private val _customizations = MutableStateFlow<List<ClassSubjectCustomization>>(emptyList())
    private val _localNotifications = MutableStateFlow<List<LocalNotification>>(emptyList())
    val localNotifications: StateFlow<List<LocalNotification>> = _localNotifications.asStateFlow()

    // --- UI State Management ---
    private val _currentTab = MutableStateFlow(0) // 0: Classes/Students, 1: Grades Entry, 2: Reports, 3: Subjects
    val currentTab = _currentTab.asStateFlow()

    private val _selectedClassId = MutableStateFlow<Long?>(null)
    val selectedClassId = _selectedClassId.asStateFlow()

    private val _selectedTermId = MutableStateFlow(1) // 1: الفصل الأول, 2: الفصل الثاني, 3: الفصل الثالث
    val selectedTermId = _selectedTermId.asStateFlow()

    private val _selectedSubjectId = MutableStateFlow<Long?>(null)
    val selectedSubjectId = _selectedSubjectId.asStateFlow()

    private val _selectedStudentId = MutableStateFlow<Long?>(null)
    val selectedStudentId = _selectedStudentId.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _hasPendingWrites = MutableStateFlow(false)
    val hasPendingWrites: StateFlow<Boolean> = _hasPendingWrites.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private fun isFinalTermOnlySeasonOpen(): Boolean {
        return try {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            // Trial period: visible to everyone until 1 October 2026
            if (year < 2026 || (year == 2026 && month < 10)) {
                return true
            }
            // Seasonal rule: from 15 April to 1 August
            if (month in 5..7) return true
            if (month == 4 && day >= 15) return true
            false
        } catch (e: Exception) {
            true
        }
    }

    // Global feature toggle controlled by Class Manager / Super Admin for Final Term Only
    private val _isFinalTermOnlyFeatureEnabled = MutableStateFlow(isFinalTermOnlySeasonOpen() || prefs.getBoolean("is_final_term_only_feature_enabled", false))
    val isFinalTermOnlyFeatureEnabled: StateFlow<Boolean> = _isFinalTermOnlyFeatureEnabled.asStateFlow()

    // --- Remarks and Note configurations (saved securely in SharedPreferences) ---
    private val _isOutOfTen = MutableStateFlow(prefs.getBoolean("is_out_of_ten", false))
    val isOutOfTen = _isOutOfTen.asStateFlow()

    private val _failBound = MutableStateFlow(prefs.getFloat("fail_bound", 9.0f))
    val failBound = _failBound.asStateFlow()

    private val _passBound = MutableStateFlow(prefs.getFloat("pass_bound", 10.0f))
    val passBound = _passBound.asStateFlow()

    private val _acceptableBound = MutableStateFlow(prefs.getFloat("acceptable_bound", 12.0f))
    val acceptableBound = _acceptableBound.asStateFlow()

    private val _goodBound = MutableStateFlow(prefs.getFloat("good_bound", 14.0f))
    val goodBound = _goodBound.asStateFlow()

    private val _veryGoodBound = MutableStateFlow(prefs.getFloat("very_good_bound", 17.0f))
    val veryGoodBound = _veryGoodBound.asStateFlow()

    // --- Term-specific boundary flows for notes based on average ---
    private val _failBoundT1 = MutableStateFlow(prefs.getFloat("fail_bound_t1", prefs.getFloat("fail_bound", 9.0f)))
    val failBoundT1 = _failBoundT1.asStateFlow()
    private val _passBoundT1 = MutableStateFlow(prefs.getFloat("pass_bound_t1", prefs.getFloat("pass_bound", 10.0f)))
    val passBoundT1 = _passBoundT1.asStateFlow()
    private val _acceptableBoundT1 = MutableStateFlow(prefs.getFloat("acceptable_bound_t1", prefs.getFloat("acceptable_bound", 12.0f)))
    val acceptableBoundT1 = _acceptableBoundT1.asStateFlow()
    private val _goodBoundT1 = MutableStateFlow(prefs.getFloat("good_bound_t1", prefs.getFloat("good_bound", 14.0f)))
    val goodBoundT1 = _goodBoundT1.asStateFlow()
    private val _veryGoodBoundT1 = MutableStateFlow(prefs.getFloat("very_good_bound_t1", prefs.getFloat("very_good_bound", 17.0f)))
    val veryGoodBoundT1 = _veryGoodBoundT1.asStateFlow()

    private val _failBoundT2 = MutableStateFlow(prefs.getFloat("fail_bound_t2", prefs.getFloat("fail_bound", 9.0f)))
    val failBoundT2 = _failBoundT2.asStateFlow()
    private val _passBoundT2 = MutableStateFlow(prefs.getFloat("pass_bound_t2", prefs.getFloat("pass_bound", 10.0f)))
    val passBoundT2 = _passBoundT2.asStateFlow()
    private val _acceptableBoundT2 = MutableStateFlow(prefs.getFloat("acceptable_bound_t2", prefs.getFloat("acceptable_bound", 12.0f)))
    val acceptableBoundT2 = _acceptableBoundT2.asStateFlow()
    private val _goodBoundT2 = MutableStateFlow(prefs.getFloat("good_bound_t2", prefs.getFloat("good_bound", 14.0f)))
    val goodBoundT2 = _goodBoundT2.asStateFlow()
    private val _veryGoodBoundT2 = MutableStateFlow(prefs.getFloat("very_good_bound_t2", prefs.getFloat("very_good_bound", 17.0f)))
    val veryGoodBoundT2 = _veryGoodBoundT2.asStateFlow()

    private val _failBoundT3 = MutableStateFlow(prefs.getFloat("fail_bound_t3", prefs.getFloat("fail_bound", 9.0f)))
    val failBoundT3 = _failBoundT3.asStateFlow()
    private val _passBoundT3 = MutableStateFlow(prefs.getFloat("pass_bound_t3", prefs.getFloat("pass_bound", 10.0f)))
    val passBoundT3 = _passBoundT3.asStateFlow()
    private val _acceptableBoundT3 = MutableStateFlow(prefs.getFloat("acceptable_bound_t3", prefs.getFloat("acceptable_bound", 12.0f)))
    val acceptableBoundT3 = _acceptableBoundT3.asStateFlow()
    private val _goodBoundT3 = MutableStateFlow(prefs.getFloat("good_bound_t3", prefs.getFloat("good_bound", 14.0f)))
    val goodBoundT3 = _goodBoundT3.asStateFlow()
    private val _veryGoodBoundT3 = MutableStateFlow(prefs.getFloat("very_good_bound_t3", prefs.getFloat("very_good_bound", 17.0f)))
    val veryGoodBoundT3 = _veryGoodBoundT3.asStateFlow()

    private val _useFinalExamFormula = MutableStateFlow(prefs.getBoolean("use_final_exam_formula", true))
    val useFinalExamFormula = _useFinalExamFormula.asStateFlow()

    private val _failNoteExam1and2 = MutableStateFlow(prefs.getString("fail_note_exam_1_and_2", "راسب") ?: "راسب")
    val failNoteExam1and2 = _failNoteExam1and2.asStateFlow()

    // --- Official Stamps (ختم المعلم و ختم المدير) ---
    private val _teacherStampName = MutableStateFlow(prefs.getString("stamp_teacher_name", "") ?: "")
    val teacherStampName = _teacherStampName.asStateFlow()

    private val _teacherStampFinancialId = MutableStateFlow(prefs.getString("stamp_teacher_financial_id", "") ?: "")
    val teacherStampFinancialId = _teacherStampFinancialId.asStateFlow()

    private val _teacherStampShape = MutableStateFlow(prefs.getString("stamp_teacher_shape", "rectangle") ?: "rectangle")
    val teacherStampShape = _teacherStampShape.asStateFlow()

    private val _teacherStampRole = MutableStateFlow(
        when (val stored = prefs.getString("stamp_teacher_role", "المعلم")) {
            "معلم", "المعلم" -> "المعلم"
            "مدير", "المدير" -> "المدير"
            else -> stored ?: "المعلم"
        }
    )
    val teacherStampRole = _teacherStampRole.asStateFlow()

    private val _showTeacherStampInReports = MutableStateFlow(prefs.getBoolean("stamp_teacher_show_in_reports", false))
    val showTeacherStampInReports = _showTeacherStampInReports.asStateFlow()

    private val _principalStampName = MutableStateFlow(prefs.getString("stamp_principal_name", "") ?: "")
    val principalStampName = _principalStampName.asStateFlow()

    private val _principalStampFinancialId = MutableStateFlow(prefs.getString("stamp_principal_financial_id", "") ?: "")
    val principalStampFinancialId = _principalStampFinancialId.asStateFlow()

    private val _principalStampShape = MutableStateFlow(prefs.getString("stamp_principal_shape", "circle") ?: "circle")
    val principalStampShape = _principalStampShape.asStateFlow()

    private val _principalStampRole = MutableStateFlow(
        when (val stored = prefs.getString("stamp_principal_role", "المدير")) {
            "معلم", "المعلم" -> "المعلم"
            "مدير", "المدير" -> "المدير"
            else -> stored ?: "المدير"
        }
    )
    val principalStampRole = _principalStampRole.asStateFlow()

    private val _showPrincipalStampInReports = MutableStateFlow(prefs.getBoolean("stamp_principal_show_in_reports", false))
    val showPrincipalStampInReports = _showPrincipalStampInReports.asStateFlow()

    private val activeListeners = java.util.Collections.synchronizedList(mutableListOf<ListenerRegistration>())
    private val insertingSubjects = java.util.concurrent.ConcurrentHashMap<Pair<String, Int>, Long>()
    private var activationListener: ListenerRegistration? = null

    @Volatile private var isSubjectsSynced = false
    @Volatile private var isCustomizationsSynced = false
    @Volatile private var isClassesSynced = false
    @Volatile private var isActivationsSynced = false
    private val initialStudentsSyncedClasses = java.util.concurrent.ConcurrentHashMap<Long, Boolean>()

    private val _isSyncingCloudData = MutableStateFlow(false)
    val isSyncingCloudData: StateFlow<Boolean> = _isSyncingCloudData.asStateFlow()

    private val _syncMessage = MutableStateFlow("جاري التحقق من حسابك وحالة تفعيل أقسامك...")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    private fun checkInitialSyncComplete(classesList: List<ClassSection> = _classSections.value) {
        val allMainSynced = isSubjectsSynced && isCustomizationsSynced && isClassesSynced && isActivationsSynced
        if (!allMainSynced) return
        
        // Check if all classes have had their students synced (if any exist)
        val allStudentsSynced = classesList.all { initialStudentsSyncedClasses[it.id] == true }
        if (allStudentsSynced) {
            _isSyncingCloudData.value = false
            deduplicateSubjects()
        }
    }

    private var dbCollectJob: kotlinx.coroutines.Job? = null

    private fun updateActiveRepositoryAndCollect(uid: String?) {
        dbCollectJob?.cancel()
        
        val activeDb = TeacherDatabase.getDatabase(getApplication(), uid)
        val activeRepo = TeacherRepository(activeDb.teacherDao())
        currentRepository = activeRepo

        dbCollectJob = viewModelScope.launch {
            launch {
                activeRepo.allClassSections.collect { list ->
                    val migratedList = list.map { section ->
                        var updatedSection = section
                        var needsDbUpdate = false
                        if (updatedSection.name.contains(" - د")) {
                            val updatedName = updatedSection.name
                                .replace(" - د1", " - س1")
                                .replace(" - د2", " - س2")
                                .replace(" - د3", " - س3")
                                .replace(" - د4", " - س4")
                                .replace(" - د5", " - س5")
                                .replace(" - د6", " - س6")
                            updatedSection = updatedSection.copy(name = updatedName)
                            needsDbUpdate = true
                        }

                        // One-time safe migration for legacy sections without stored level only
                        if (updatedSection.level !in 1..5) {
                            val inferred = com.example.data.planning.AnnualPlanningRepository.parseLevelFromClassName(updatedSection.name)
                            if (inferred in 1..5) {
                                updatedSection = updatedSection.copy(level = inferred)
                                needsDbUpdate = true
                            }
                        }

                        if (needsDbUpdate) {
                            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                try {
                                    activeRepo.insertClassSection(updatedSection)
                                    val currentUid = _currentUser.value?.uid
                                    if (currentUid != null) {
                                        val classData = mapOf(
                                            "id" to updatedSection.id,
                                            "name" to updatedSection.name,
                                            "wilaya" to updatedSection.wilaya,
                                            "moughataa" to updatedSection.moughataa,
                                            "schoolName" to updatedSection.schoolName,
                                            "level" to updatedSection.level,
                                            "sectionName" to updatedSection.sectionName,
                                            "academicYear" to updatedSection.academicYear
                                        )
                                        FirebaseFirestore.getInstance().collection("teachers").document(currentUid)
                                            .collection("classes").document(updatedSection.id.toString())
                                            .set(classData, com.google.firebase.firestore.SetOptions.merge())
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        updatedSection
                    }
                    _classSections.value = migratedList
                }
            }
            launch {
                activeRepo.allSubjects.collect { list ->
                    _subjects.value = list.filter { it.id !in deletedGlobalSubjectIds.value }
                }
            }
            launch {
                activeRepo.allStudents.collect { list ->
                    _students.value = list
                }
            }
            launch {
                activeRepo.allGrades.collect { list ->
                    _grades.value = list
                }
            }
            launch {
                activeRepo.allManualTermAverages.collect { list ->
                    _manualTermAverages.value = list
                }
            }
            launch {
                activeRepo.allCustomizations.collect { list ->
                    _customizations.value = list
                }
            }
            launch {
                activeRepo.getAllLocalNotifications().collect { list ->
                    _localNotifications.value = list
                }
            }
            // Ensure standard default subjects exist for all levels (1..6)
            seedDefaultSubjects(activeRepo)
        }
    }

    private fun seedDefaultSubjects(repo: TeacherRepository) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val defaultSubjectsList = listOf(
                    // Level 1 (س1)
                    Subject(id = 101L, name = "التربية الإسلامية - Education Islamique", level = 1, maxPoints = 40),
                    Subject(id = 102L, name = "اللغة العربية - Langue arabe", level = 1, maxPoints = 80),
                    Subject(id = 103L, name = "الرياضيات - Mathématiques", level = 1, maxPoints = 40),
                    Subject(id = 104L, name = "التربية المدنية - Education Civique", level = 1, maxPoints = 10),
                    Subject(id = 105L, name = "التربية الفنية - Education Technique", level = 1, maxPoints = 10),
                    Subject(id = 106L, name = "العلوم الطبيعية - Sciences Naturelles", level = 1, maxPoints = 10),
                    Subject(id = 107L, name = "الرياضة البدنية - Education Sportive", level = 1, maxPoints = 10),

                    // Level 2 (س2)
                    Subject(id = 201L, name = "التربية الإسلامية - Education Islamique", level = 2, maxPoints = 30),
                    Subject(id = 202L, name = "اللغة العربية - Langue arabe", level = 2, maxPoints = 50),
                    Subject(id = 203L, name = "الرياضيات - Mathématiques", level = 2, maxPoints = 40),
                    Subject(id = 204L, name = "التربية المدنية - Education Civique", level = 2, maxPoints = 10),
                    Subject(id = 205L, name = "التربية الفنية - Education Technique", level = 2, maxPoints = 10),
                    Subject(id = 206L, name = "الفرنسية - Français", level = 2, maxPoints = 40),
                    Subject(id = 207L, name = "العلوم الطبيعية - Sciences Naturelles", level = 2, maxPoints = 10),
                    Subject(id = 208L, name = "الرياضة البدنية - Education Sportive", level = 2, maxPoints = 10),

                    // Level 3 (س3)
                    Subject(id = 301L, name = "التربية الإسلامية - Education Islamique", level = 3, maxPoints = 30),
                    Subject(id = 302L, name = "اللغة العربية - Langue arabe", level = 3, maxPoints = 50),
                    Subject(id = 303L, name = "الرياضيات - Mathématiques", level = 3, maxPoints = 40),
                    Subject(id = 304L, name = "التربية المدنية - Education Civique", level = 3, maxPoints = 10),
                    Subject(id = 305L, name = "التربية الفنية - Education Technique", level = 3, maxPoints = 10),
                    Subject(id = 306L, name = "الفرنسية - Français", level = 3, maxPoints = 30),
                    Subject(id = 307L, name = "التاريخ والجغرافيا - Histoire Géographie", level = 3, maxPoints = 10),
                    Subject(id = 308L, name = "العلوم الطبيعية - Sciences Naturelles", level = 3, maxPoints = 10),
                    Subject(id = 309L, name = "الرياضة البدنية - Education Sportive", level = 3, maxPoints = 10),

                    // Level 4 (س4)
                    Subject(id = 401L, name = "التربية الإسلامية - Education Islamique", level = 4, maxPoints = 30),
                    Subject(id = 402L, name = "اللغة العربية - Langue arabe", level = 4, maxPoints = 50),
                    Subject(id = 403L, name = "الرياضيات - Mathématiques", level = 4, maxPoints = 40),
                    Subject(id = 404L, name = "التربية المدنية - Education Civique", level = 4, maxPoints = 10),
                    Subject(id = 405L, name = "التربية الفنية - Education Technique", level = 4, maxPoints = 10),
                    Subject(id = 406L, name = "الفرنسية - Français", level = 4, maxPoints = 30),
                    Subject(id = 407L, name = "التاريخ والجغرافيا - Histoire Géographie", level = 4, maxPoints = 10),
                    Subject(id = 408L, name = "العلوم الطبيعية - Sciences Naturelles", level = 4, maxPoints = 10),
                    Subject(id = 409L, name = "الرياضة البدنية - Education Sportive", level = 4, maxPoints = 10),

                    // Level 5 (س5)
                    Subject(id = 501L, name = "التربية الإسلامية - Education Islamique", level = 5, maxPoints = 30),
                    Subject(id = 502L, name = "اللغة العربية - Langue arabe", level = 5, maxPoints = 50),
                    Subject(id = 503L, name = "الرياضيات - Mathématiques", level = 5, maxPoints = 40),
                    Subject(id = 504L, name = "التربية المدنية - Education Civique", level = 5, maxPoints = 10),
                    Subject(id = 505L, name = "التربية الفنية - Education Technique", level = 5, maxPoints = 10),
                    Subject(id = 506L, name = "الفرنسية - Français", level = 5, maxPoints = 30),
                    Subject(id = 507L, name = "التاريخ والجغرافيا - Histoire Géographie", level = 5, maxPoints = 10),
                    Subject(id = 508L, name = "العلوم الطبيعية - Sciences Naturelles", level = 5, maxPoints = 10),
                    Subject(id = 509L, name = "الرياضة البدنية - Education Sportive", level = 5, maxPoints = 10),

                    // Level 6 (س6)
                    Subject(id = 601L, name = "التربية الإسلامية - Education Islamique", level = 6, maxPoints = 30),
                    Subject(id = 602L, name = "اللغة العربية - Langue arabe", level = 6, maxPoints = 50),
                    Subject(id = 603L, name = "الرياضيات - Mathématiques", level = 6, maxPoints = 40),
                    Subject(id = 604L, name = "التربية المدنية - Education Civique", level = 6, maxPoints = 10),
                    Subject(id = 605L, name = "التربية الفنية - Education Technique", level = 6, maxPoints = 10),
                    Subject(id = 606L, name = "الفرنسية - Français", level = 6, maxPoints = 30),
                    Subject(id = 607L, name = "التاريخ والجغرافيا - Histoire Géographie", level = 6, maxPoints = 10),
                    Subject(id = 608L, name = "العلوم الطبيعية - Sciences Naturelles", level = 6, maxPoints = 10),
                    Subject(id = 609L, name = "الرياضة البدنية - Education Sportive", level = 6, maxPoints = 10)
                )

                val validDefaultIds = defaultSubjectsList.map { it.id }.toSet()
                val existingSubjects = repo.allSubjects.first()

                for (sub in defaultSubjectsList) {
                    repo.insertSubject(sub)
                }

                val oldSeedRange = 100L..699L
                for (existing in existingSubjects) {
                    if (existing.id in oldSeedRange && existing.id !in validDefaultIds) {
                        repo.deleteSubject(existing.id)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        // Load deleted global subject IDs
        try {
            val savedDeletedIds = prefs.getStringSet("deleted_global_subject_ids", emptySet()) ?: emptySet()
            deletedGlobalSubjectIds.value = savedDeletedIds.mapNotNull { it.toLongOrNull() }.toSet()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Explicitly initialize FirebaseApp
        try {
            com.google.firebase.FirebaseApp.initializeApp(application)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Explicitly enable offline persistence for Firestore with UNLIMITED cache size
        try {
            val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            com.google.firebase.firestore.FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (e: Exception) {
            // Settings must be set before any database operations are performed
        }

        _isFinalTermOnlyFeatureEnabled.value = isFinalTermOnlySeasonOpen() || prefs.getBoolean("is_final_term_only_feature_enabled", false)

        viewModelScope.launch {
            try {
                repository.getAllLocalNotifications().collect { list ->
                    _localNotifications.value = list
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Initial active repository setup: use saved UID if Firebase Auth user is still resolving locally
        val savedUid = try { prefs.getString("last_user_uid", null) } catch (e: Exception) { null }
        val currentAuthUser = try { FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
        val initialUid = currentAuthUser?.uid ?: savedUid

        if (initialUid != null) {
            updateActiveRepositoryAndCollect(initialUid)
            _isSyncingCloudData.value = false
            listenToClassesAndStudents(initialUid)
        }
        
        try {
            val auth = FirebaseAuth.getInstance()
            _currentUser.value = auth.currentUser
            auth.addAuthStateListener { firebaseAuth ->
                _currentUser.value = firebaseAuth.currentUser
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _currentUser.value = null
        }
        
        viewModelScope.launch {
            _currentUser.collect { user ->
                val cachedUid = try { prefs.getString("last_user_uid", null) } catch (e: Exception) { null }
                val activeUid = user?.uid ?: FirebaseAuth.getInstance().currentUser?.uid ?: cachedUid

                if (activeUid != null) {
                    updateActiveRepositoryAndCollect(activeUid)

                    // Ensure teacher profile document exists in Firestore teachers collection
                    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            val db = FirebaseFirestore.getInstance()
                            val rawDName = user?.displayName ?: FirebaseAuth.getInstance().currentUser?.displayName
                            val emailStr = user?.email ?: FirebaseAuth.getInstance().currentUser?.email ?: ""
                            val dName = when {
                                !rawDName.isNullOrBlank() -> rawDName
                                emailStr.isNotBlank() -> emailStr.substringBefore("@")
                                else -> "معلم (${activeUid.take(8)})"
                            }
                            val teacherData = mapOf(
                                "displayName" to dName,
                                "email" to emailStr,
                                "syncedAt" to System.currentTimeMillis()
                            )
                            db.collection("teachers").document(activeUid).set(teacherData, com.google.firebase.firestore.SetOptions.merge())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    try {
                        val previousUid = prefs.getString("last_user_uid", null)
                        if (previousUid != null && previousUid != activeUid) {
                            _selectedClassId.value = null
                            _selectedStudentId.value = null
                        }
                        prefs.edit().putString("last_user_uid", activeUid).apply()

                        // 100% Offline-first: Bypass cloud downloading, data is loaded from local Room DB
                        _isSyncingCloudData.value = false
                        _syncError.value = null
                        listenToClassesAndStudents(activeUid)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _isSyncingCloudData.value = false
                    }
                } else if (cachedUid == null) {
                    // Only reset data if there is no user logged in AND no saved UID (e.g. user manually logged out)
                    _isSyncingCloudData.value = false
                    synchronized(activeListeners) {
                        activeListeners.forEach { try { it.remove() } catch (e: Exception) {} }
                        activeListeners.clear()
                    }
                    
                    // Reset memory state flows immediately
                    _classSections.value = emptyList()
                    _students.value = emptyList()
                    _grades.value = emptyList()
                    _subjects.value = emptyList()
                    _customizations.value = emptyList()
                    _selectedClassId.value = null
                    _selectedStudentId.value = null

                    // Reset sync flags for the next user session
                    isSubjectsSynced = false
                    isCustomizationsSynced = false
                    isClassesSynced = false
                    isActivationsSynced = false
                    initialStudentsSyncedClasses.clear()
                    
                    // Reset last sync state in preferences and memory
                    _lastSyncTime.value = 0L
                    prefs.edit()
                        .remove("last_sync_time")
                        .remove("last_user_uid")
                        .apply()
                }
            }
        }
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            deduplicateSubjects()
        }

        // Load local cached exchange posts from Room database for offline browsing
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val cachedPosts = activeDatabase.teacherDao().getAllExchangePosts()
                if (cachedPosts.isNotEmpty()) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        _exchangePosts.value = cachedPosts
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Automatic cloud sync once connection becomes available
        try {
            val connectivityManager = application.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            if (connectivityManager != null) {
                val networkRequest = android.net.NetworkRequest.Builder()
                    .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                connectivityManager.registerNetworkCallback(networkRequest, object : android.net.ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: android.net.Network) {
                        super.onAvailable(network)
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(4000) // Small delay to allow interface to stabilize
                            if (isNetworkAvailable()) {
                                try {
                                    if (prefs.getBoolean("pending_teacher_registration", false)) {
                                        val u = _currentUser.value
                                        if (u != null) {
                                            registerTeacherInFirestore(u.uid, u.displayName ?: "", u.email ?: "")
                                        }
                                    }
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                                syncPendingExchangePosts()
                                resendUnsyncedClasses()
                            }
                        }
                    }
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        listenToExchangePosts()
    }

    private fun listenToClassesAndStudents(uid: String) {
        try {
            synchronized(activeListeners) {
                activeListeners.forEach { try { it.remove() } catch (e: Exception) {} }
                activeListeners.clear()
            }
            
            val db = FirebaseFirestore.getInstance()

            isSubjectsSynced = true
            isCustomizationsSynced = true
            isClassesSynced = true
            isActivationsSynced = true
            val currentClasses = _classSections.value
            checkInitialSyncComplete(currentClasses)
            if (currentClasses.isNotEmpty()) {
                syncStudentListeners(uid, currentClasses)
            }
            resendUnsyncedClasses()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resendUnsyncedClasses() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val uid = _currentUser.value?.uid ?: FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                if (!isNetworkAvailable()) return@launch
                val unsyncedClasses = _classSections.value.filter { !it.isSyncedToServer }
                if (unsyncedClasses.isEmpty()) return@launch

                val db = FirebaseFirestore.getInstance()
                unsyncedClasses.forEach { section ->
                    try {
                        val classData = mapOf(
                            "id" to section.id,
                            "name" to section.name,
                            "wilaya" to section.wilaya,
                            "moughataa" to section.moughataa,
                            "schoolName" to section.schoolName,
                            "level" to section.level,
                            "sectionName" to section.sectionName,
                            "academicYear" to section.academicYear
                        )
                        db.collection("teachers").document(uid)
                            .collection("classes").document(section.id.toString())
                            .set(classData, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener {
                                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    try {
                                        val syncedClass = section.copy(isSyncedToServer = true)
                                        repository.insertClassSection(syncedClass)
                                    } catch (e: Exception) {
                                        // عند الفشل: لا تفعل شيئا، اترك القسم كما هو
                                    }
                                }
                            }
                    } catch (e: Exception) {
                        // عند الفشل: لا تفعل شيئا، اترك القسم كما هو
                    }
                }
            } catch (e: Exception) {
                // الدالة صامتة تماما
            }
        }
    }

    private fun isTermActivationExpired(academicYear: String): Boolean {
        return try {
            val endYear = academicYear.split("-").getOrNull(1)?.trim()?.toIntOrNull() ?: return false
            val calendar = java.util.Calendar.getInstance()
            val currentYear = calendar.get(java.util.Calendar.YEAR)
            val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
            currentYear > endYear || (currentYear == endYear && currentMonth >= 8)
        } catch (e: Exception) {
            false
        }
    }

    private val activationNotifyMutex = kotlinx.coroutines.sync.Mutex()

    fun startActivationListener() {
        val uid = _currentUser.value?.uid ?: return
        if (activationListener != null) return

        try {
            val db = FirebaseFirestore.getInstance()
            val listener = db.collection("teachers").document(uid).collection("classes")
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                    if (error != null || snapshot == null) {
                        return@addSnapshotListener
                    }
                    try {
                        data class TermActInfo(val isAct: Boolean, val isT1: Boolean, val isT2: Boolean, val isT3: Boolean, val isT4: Boolean)
                        val activationsMap = snapshot.documents.mapNotNull { doc ->
                            try {
                                val classId = doc.id.toLongOrNull() ?: return@mapNotNull null
                                val isAct = doc.getBoolean("isActivated") ?: false
                                val isT1 = doc.getBoolean("isTerm1Activated") ?: (doc.getBoolean("isActivated") ?: true)
                                val rawT2 = doc.getBoolean("isTerm2Activated") ?: (doc.getBoolean("isActivated") ?: false)
                                val rawT3 = doc.getBoolean("isTerm3Activated") ?: (doc.getBoolean("isActivated") ?: false)
                                val rawT4 = doc.getBoolean("isTerm4Activated") ?: (doc.getBoolean("isActivated") ?: false)
                                val localYear = _classSections.value.firstOrNull { it.id == classId }?.academicYear ?: ""
                                val expired = isTermActivationExpired(localYear)
                                val isT2 = rawT2 && !expired
                                val isT3 = rawT3 && !expired
                                val isT4 = rawT4 && !expired
                                classId to TermActInfo(isAct, isT1, isT2, isT3, isT4)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }.toMap()

                        if (activationsMap.isNotEmpty()) {
                            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                              activationNotifyMutex.withLock {
                                try {
                                    activationsMap.forEach { (classId, info) ->
                                        val localClass = repository.getClassSectionById(classId)
                                        if (localClass != null && (localClass.isActivated != info.isAct || localClass.isTerm1Activated != info.isT1 || localClass.isTerm2Activated != info.isT2 || localClass.isTerm3Activated != info.isT3 || localClass.isTerm4Activated != info.isT4)) {
                                            try {
                                                if (!localClass.isTerm1Activated && info.isT1) {
                                                    repository.insertLocalNotification(
                                                        com.example.data.models.LocalNotification(
                                                            title = "تم تفعيل الفصل ✅",
                                                            message = "تم تفعيل الفصل الأول للقسم: ${localClass.name}",
                                                            createdAt = System.currentTimeMillis(),
                                                            isRead = false,
                                                            classId = classId
                                                        )
                                                    )
                                                }
                                                if (!localClass.isTerm2Activated && info.isT2) {
                                                    repository.insertLocalNotification(
                                                        com.example.data.models.LocalNotification(
                                                            title = "تم تفعيل الفصل ✅",
                                                            message = "تم تفعيل الفصل الثاني للقسم: ${localClass.name}",
                                                            createdAt = System.currentTimeMillis(),
                                                            isRead = false,
                                                            classId = classId
                                                        )
                                                    )
                                                }
                                                if (!localClass.isTerm3Activated && info.isT3) {
                                                    repository.insertLocalNotification(
                                                        com.example.data.models.LocalNotification(
                                                            title = "تم تفعيل الفصل ✅",
                                                            message = "تم تفعيل الفصل الثالث للقسم: ${localClass.name}",
                                                            createdAt = System.currentTimeMillis(),
                                                            isRead = false,
                                                            classId = classId
                                                        )
                                                    )
                                                }
                                                if (!localClass.isTerm4Activated && info.isT4) {
                                                    repository.insertLocalNotification(
                                                        com.example.data.models.LocalNotification(
                                                            title = "تم تفعيل الفصل ✅",
                                                            message = "تم تفعيل الفصل الأخير للقسم: ${localClass.name}",
                                                            createdAt = System.currentTimeMillis(),
                                                            isRead = false,
                                                            classId = classId
                                                        )
                                                    )
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }

                                            // Update local database which will automatically update UI state flows
                                            val updatedClass = localClass.copy(
                                                isActivated = info.isAct,
                                                isTerm1Activated = info.isT1,
                                                isTerm2Activated = info.isT2,
                                                isTerm3Activated = info.isT3,
                                                isTerm4Activated = info.isT4
                                            )
                                            repository.insertClassSection(updatedClass)
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                              }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            activationListener = listener
            synchronized(activeListeners) {
                activeListeners.add(listener)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopActivationListener() {
        try {
            activationListener?.let { listener ->
                try {
                    listener.remove()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                synchronized(activeListeners) {
                    activeListeners.remove(listener)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            activationListener = null
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.markLocalNotificationRead(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.markAllLocalNotificationsRead()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.deleteAllLocalNotifications()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun syncStudentListeners(uid: String, classesList: List<ClassSection>) {
        classesList.forEach { classSection ->
            initialStudentsSyncedClasses[classSection.id] = true
        }
        checkInitialSyncComplete(classesList)
    }

    fun registerTeacherInFirestore(uid: String, displayName: String, email: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val teacherDoc = db.collection("teachers").document(uid)
            val data = mapOf(
                "uid" to uid,
                "displayName" to displayName,
                "email" to email,
                "syncedAt" to System.currentTimeMillis()
            )
            teacherDoc.set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    try {
                        prefs.edit().remove("pending_teacher_registration").apply()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
                .addOnFailureListener {
                    try {
                        prefs.edit().putBoolean("pending_teacher_registration", true).apply()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                prefs.edit().putBoolean("pending_teacher_registration", true).apply()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun updateSubjectsList() {
        val combined = (globalSubjectsList.value + personalSubjectsList.value).associateBy { it.id }.values.toList()
        val filtered = combined.filter { it.id !in deletedGlobalSubjectIds.value }
        _subjects.value = filtered
    }

    fun normalizeInput(input: String): String {
        var result = input.trim()
        val arabicIndicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        for (i in 0..9) {
            result = result.replace(arabicIndicDigits[i], (i + '0'.code).toChar())
        }
        return result
    }

    fun mapAuthExceptionToArabic(exception: Exception?): String {
        if (exception == null) return "حدث خطأ غير متوقع أثناء عملية المصادقة."
        
        return when (exception) {
            is com.google.firebase.FirebaseNetworkException -> {
                "🌐 خطأ اتصال بالشبكة! يرجى التحقق من تشغيل الواي فاي أو بيانات الهاتف، فقد يكون اتصال الإنترنت ضعيفاً أو منقطعاً مؤقتاً مما يمنع التحقق من كلمة المرور مع خادم قاعدة البيانات الفورية."
            }
            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                "👤 هذا الحساب غير موجود! البريد الإلكتروني الذي أدخلته غير مسجل كحساب معلم لدينا. يرجى التأكد من كتابة البريد بشكل صحيح أو الانتقال لخيار 'حساب جديد' للتسجيل لأول مرة."
            }
            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                "🔑 كلمة المرور التي قمت بإدخالها غير صحيحة! يرجى التحقق من لغة لوحة المفاتيح وحالة الأحرف (كبيرة/صغيرة). وإذا كنت متأكداً من كلمة المرور، يرجى المحاولة بعد قليل أو استخدام خيار 'نسيت كلمة المرور'."
            }
            is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> {
                "🔒 كلمة المرور ضعيفة جداً! يجب أن تتكون كلمة المرور من 6 خانات أو أكثر لتأمين حساب المعلم الخاص بك."
            }
            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                "📧 هذا البريد الإلكتروني مسجل بالفعل لحساب معلم آخر! يمكنك استخدام نفس الحساب لتسجيل الدخول مباشرة أو إدخال بريد إلكتروني جديد."
            }
            is com.google.firebase.auth.FirebaseAuthException -> {
                val errorCode = exception.errorCode
                val className = exception.javaClass.simpleName
                if (errorCode == "ERROR_TOO_MANY_REQUESTS" || className.contains("TooManyRequests")) {
                    "🛡️ تم حظر محاولات الدخول مؤقتاً لحماية حسابك بسبب تكرار إدخال كلمة المرور بشكل خاطئ. يرجى الانتظار لمدة دقيقة واحدة كاملة ثم المحاولة مرة أخرى أو استخدام ميزة إعادة تعيين كلمة المرور."
                } else if (errorCode == "ERROR_WRONG_PASSWORD") {
                    "🔑 كلمة المرور التي قمت بإدخالها غير صحيحة! يرجى التحقق من لغة لوحة المفاتيح وحالة الأحرف."
                } else {
                    "⚠️ تنبيه من خادم الأمان (رمز: $errorCode): ${exception.localizedMessage ?: "حدث خطأ أثناء الاتصال بالخادم."}"
                }
            }
            else -> {
                val msg = exception.localizedMessage ?: ""
                val className = exception.javaClass.simpleName
                if (className.contains("TooManyRequests")) {
                    "🛡️ تم حظر محاولات الدخول مؤقتاً لحماية حسابك بسبب تكرار إدخال كلمة المرور بشكل خاطئ. يرجى الانتظار دقيقة كاملة ثم المحاولة مرة أخرى."
                } else if (msg.contains("network", ignoreCase = true) || msg.contains("timeout", ignoreCase = true) || msg.contains("connection", ignoreCase = true)) {
                    "🌐 انقطاع أو ضعف في اتصال الإنترنت! تعذر التحقق من بيانات الدخول مع خادم قاعدة البيانات. يرجى الانتظار ثوانٍ والمحاولة مرة أخرى."
                } else {
                    "⚠️ تنبيه من خادم الأمان: $msg\nيرجى التأكد من اتصال الإنترنت وصحة كلمة المرور والمحاولة مجدداً."
                }
            }
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun loginWithGoogleCredential(idToken: String, onSuccess: () -> Unit) {
        _isAuthLoading.value = true
        _authError.value = null
        try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    try {
                        _isAuthLoading.value = false
                        if (task.isSuccessful) {
                            val user = FirebaseAuth.getInstance().currentUser
                            _currentUser.value = user
                            if (user != null) {
                                registerTeacherInFirestore(user.uid, user.displayName ?: "معلم", user.email ?: "")
                            }
                            startLoginSync()
                            onSuccess()
                        } else {
                            _authError.value = mapAuthExceptionToArabic(task.exception)
                        }
                    } catch (taskEx: Exception) {
                        _isAuthLoading.value = false
                        _authError.value = "حدث خطأ غير متوقع أثناء معالجة تسجيل الدخول بحساب جوجل: ${taskEx.localizedMessage}"
                        taskEx.printStackTrace()
                    }
                }
        } catch (e: Exception) {
            _isAuthLoading.value = false
            _authError.value = "تعذر الاتصال بخدمات الدخول باستخدام جوجل. يرجى المحاولة لاحقاً."
            e.printStackTrace()
        }
    }

    fun canSignOut(onResult: (Boolean, String?) -> Unit) {
        val uid = _currentUser.value?.uid
        if (uid == null) {
            onResult(true, null)
            return
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            var hasPending = false
            try {
                val unsyncedPosts = activeDatabase.teacherDao().getUnsyncedExchangePosts()
                if (unsyncedPosts.isNotEmpty()) {
                    hasPending = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (!hasPending && _hasPendingWrites.value) {
                hasPending = true
            }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (hasPending) {
                    onResult(false, "⚠️ توجد بيانات محلية لم تُرفع للخادم، يرجى الاتصال بالإنترنت والضغط على زر الرفع قبل الخروج")
                } else {
                    onResult(true, null)
                }
            }
        }
    }

    @Volatile private var logoutCancelled = false

    fun cancelLogout() {
        logoutCancelled = true
        _showCancelLogoutButton.value = false
        hideLoading()
    }

    fun signOutTeacherWithCheck(onBlocked: (String) -> Unit = {}, onSuccess: () -> Unit = {}) {
        if (!isNetworkAvailable()) {
            onBlocked("⚠️ لا يمكن تسجيل الخروج. لا يوجد اتصال بالإنترنت. يرجى توفير الإنترنت لحفظ نسختك الاحتياطية قبل الخروج")
            return
        }

        logoutCancelled = false
        showLoading("جاري حفظ ومزامنة البيانات قبل الخروج 🔄☁️...")
        _showCancelLogoutButton.value = true
        performGoogleDriveBackup(isLogout = true) { success, message ->
            if (logoutCancelled) {
                _showCancelLogoutButton.value = false
                hideLoading()
                return@performGoogleDriveBackup
            }
            if (success) {
                performUnifiedSync(SyncContext.LOGOUT) { _, _ ->
                    _showCancelLogoutButton.value = false
                    hideLoading()
                    if (logoutCancelled) {
                        return@performUnifiedSync
                    }
                    signOutTeacher()
                    onSuccess()
                }
            } else {
                _showCancelLogoutButton.value = false
                hideLoading()
                if (message.contains("جهاز آخر زامن هذا الحساب")) {
                    _logoutConflictMessage.value = message
                } else {
                    onBlocked("⚠️ تعذّر حفظ نسختك الاحتياطية على Drive. لم يتم تسجيل الخروج. حاول مرة أخرى")
                }
            }
        }
    }

    private fun wipeAllLocalUserData() {
        try {
            val keepFinalTermOnly = prefs.getBoolean("is_final_term_only_feature_enabled", false)
            prefs.edit().clear().apply()
            prefs.edit().putBoolean("is_final_term_only_feature_enabled", keepFinalTermOnly).apply()

            try {
                val appContext = getApplication<Application>().applicationContext
                listOf("teacher_prefs", "annual_planning_prefs", "app_settings", "school_legislation_prefs", "last_place_prefs").forEach { prefsName ->
                    appContext.getSharedPreferences(prefsName, android.content.Context.MODE_PRIVATE)
                        .edit().clear().apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val uid = _currentUser.value?.uid
            val localDb = TeacherDatabase.getDatabase(getApplication(), uid)
            localDb.clearAllTables()

            _classSections.value = emptyList()
            _students.value = emptyList()
            _grades.value = emptyList()
            _subjects.value = emptyList()
            _customizations.value = emptyList()
            _exchangePosts.value = emptyList()
            _manualTermAverages.value = emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun signOutTeacher() {
        wipeAllLocalUserData()
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _currentUser.value = null
        _selectedClassId.value = null
        _selectedStudentId.value = null
        _syncUiState.value = UnifiedSyncUiState()
        _isSyncingCloudData.value = false
        try {
            deletedGlobalSubjectIds.value = emptySet()
            updateSubjectsList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Local user database files are preserved on the phone (100% offline persistence)
        // Each user's database is safely isolated as teacher_database_<userId>
        updateActiveRepositoryAndCollect(null)
    }

    fun deleteTeacherAccount(onComplete: (Boolean, String) -> Unit) {
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                onComplete(false, "لا يوجد مستخدم مسجل حالياً")
                return
            }
            val uid = user.uid
            _isGlobalLoading.value = true
            _loadingMessage.value = "جاري حذف حسابك وكافة البيانات المرتبطة به..."

            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                var serverDeletionFailures = 0
                val db = FirebaseFirestore.getInstance()
                // 1. Read all documents in teachers/{uid}/classes and save their IDs
                val classIds = mutableListOf<String>()
                try {
                    val classesSnapshot = com.google.android.gms.tasks.Tasks.await(
                        db.collection("teachers").document(uid).collection("classes").get()
                    )
                    classesSnapshot.documents.forEach { doc ->
                        val cId = doc.getLong("id")?.toString() ?: doc.id
                        classIds.add(cId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    serverDeletionFailures++
                }

                // 2. For each class ID, delete class_activations/{classId} from root collection
                for (cId in classIds) {
                    try {
                        com.google.android.gms.tasks.Tasks.await(
                            db.collection("class_activations").document(cId).delete()
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        serverDeletionFailures++
                    }
                }

                // 3. Delete all documents in teachers/{uid}/classes
                try {
                    val classesSnapshot = com.google.android.gms.tasks.Tasks.await(
                        db.collection("teachers").document(uid).collection("classes").get()
                    )
                    for (doc in classesSnapshot.documents) {
                        try {
                            com.google.android.gms.tasks.Tasks.await(doc.reference.delete())
                        } catch (e: Exception) {
                            e.printStackTrace()
                            serverDeletionFailures++
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    serverDeletionFailures++
                }

                // 4. Delete document teachers/{uid}
                try {
                    com.google.android.gms.tasks.Tasks.await(
                        db.collection("teachers").document(uid).delete()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    serverDeletionFailures++
                }

                // 5. Delete this teacher's exchange posts
                try {
                    val postsSnapshot = com.google.android.gms.tasks.Tasks.await(
                        db.collection("teacher_exchanges").whereEqualTo("userId", uid).get()
                    )
                    for (doc in postsSnapshot.documents) {
                        try {
                            com.google.android.gms.tasks.Tasks.await(doc.reference.delete())
                        } catch (e: Exception) {
                            e.printStackTrace()
                            serverDeletionFailures++
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    serverDeletionFailures++
                }

                if (serverDeletionFailures > 0) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        _isGlobalLoading.value = false
                        _loadingMessage.value = ""
                        onComplete(false, "تعذر حذف بعض بياناتك من الخادم. لم يتم حذف الحساب ولم تُمس بياناتك. تحقق من اتصالك وأعد المحاولة.")
                    }
                    return@launch
                }

                // 6. Clear user local database
                wipeAllLocalUserData()

                // Delete FirebaseAuth user
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    try {
                        user.delete()
                            .addOnCompleteListener { authTask ->
                                _isGlobalLoading.value = false
                                _loadingMessage.value = ""
                                if (authTask.isSuccessful) {
                                    _currentUser.value = null
                                    onComplete(true, "تم حذف حسابك وكافة البيانات بنجاح.")
                                } else {
                                    // If delete fails, force sign out anyway as we cleared local DB and started the deletion
                                    try {
                                        FirebaseAuth.getInstance().signOut()
                                    } catch (ex: Exception) {
                                        ex.printStackTrace()
                                    }
                                    _currentUser.value = null
                                    onComplete(true, "تم حذف البيانات محلياً وتسجيل الخروج. قد تحتاج لإعادة تسجيل الدخول لحذف الحساب نهائياً من الخادم بسبب الحماية الأمنية.")
                                }
                            }
                    } catch (e: Exception) {
                        _isGlobalLoading.value = false
                        _loadingMessage.value = ""
                        try {
                            FirebaseAuth.getInstance().signOut()
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                        _currentUser.value = null
                        onComplete(true, "تم حذف البيانات محلياً وتسجيل الخروج بنجاح.")
                    }
                }
            }
        } catch (e: Exception) {
            _isGlobalLoading.value = false
            _loadingMessage.value = ""
            _currentUser.value = null
            onComplete(false, "حدث خطأ أثناء الاتصال بالخادم لحذف الحساب: ${e.localizedMessage}")
        }
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (email.isBlank()) {
            onFailure("الرجاء إدخال البريد الإلكتروني أولاً")
            return
        }
        val normalizedEmail = normalizeInput(email).lowercase(Locale.US)
        FirebaseAuth.getInstance().sendPasswordResetEmail(normalizedEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(mapAuthExceptionToArabic(task.exception))
                }
            }
    }

    suspend fun syncAllToCloudInternal() {
        val uid = _currentUser.value?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // Sync pending exchange posts safely
        try {
            syncPendingExchangePosts()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Always read the complete dataset directly from Room DB to prevent syncing stale/partial state flows
        val localClasses = try { repository.allClassSections.first() } catch (e: Exception) { _classSections.value }

        var batch = db.batch()
        var batchOpCount = 0

        fun commitBatchIfNeeded() {
            if (batchOpCount >= 300) {
                try {
                    Tasks.await(batch.commit(), 15, java.util.concurrent.TimeUnit.SECONDS)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                batch = db.batch()
                batchOpCount = 0
            }
        }

        // 1. Sync profile info
        val teacherDoc = db.collection("teachers").document(uid)
        val teacherData = mapOf(
            "displayName" to (_currentUser.value?.displayName ?: ""),
            "email" to (_currentUser.value?.email ?: ""),
            "syncedAt" to System.currentTimeMillis()
        )
        batch.set(teacherDoc, teacherData, com.google.firebase.firestore.SetOptions.merge())
        batchOpCount++

        // 2. Sync hierarchical classes (descriptive fields only - activation state is written by the admin only)
        localClasses.forEach { schoolClass ->
            val classData = mapOf(
                "id" to schoolClass.id,
                "name" to schoolClass.name,
                "wilaya" to schoolClass.wilaya,
                "moughataa" to schoolClass.moughataa,
                "schoolName" to schoolClass.schoolName,
                "level" to schoolClass.level,
                "sectionName" to schoolClass.sectionName,
                "academicYear" to schoolClass.academicYear
            )
            val classRef = db.collection("teachers").document(uid)
                .collection("classes").document(schoolClass.id.toString())
            batch.set(classRef, classData, com.google.firebase.firestore.SetOptions.merge())
            batchOpCount++
            commitBatchIfNeeded()
        }

        if (batchOpCount > 0) {
            try {
                Tasks.await(batch.commit(), 15, java.util.concurrent.TimeUnit.SECONDS)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val sysTime = System.currentTimeMillis()
        prefs.edit().putLong("last_sync_time", sysTime).apply()
        _lastSyncTime.value = sysTime
        _hasPendingWrites.value = false
    }

    // --- Firestore Sync States ---
    // Sync all local Room data to Firestore using the specified hierarchy
    fun syncAllToCloud(onComplete: (Boolean, String) -> Unit) {
        val uid = _currentUser.value?.uid ?: run {
            onComplete(false, "يجب تسجيل الدخول أولاً")
            return
        }
        _isSyncing.value = true
        _syncError.value = null
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                syncAllToCloudInternal()
                _isSyncing.value = false
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete(true, "تم الرفع بنجاح")
                }
            } catch (e: Exception) {
                _isSyncing.value = false
                _syncError.value = e.localizedMessage
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete(false, "فشلت المزامنة السحابية: ${e.localizedMessage}")
                }
            }
        }
    }


    fun triggerUnifiedSync(context: SyncContext = SyncContext.GENERAL) {
        if (!isNetworkAvailable() && context == SyncContext.LOGOUT) {
            return
        }

        val title = when (context) {
            SyncContext.LOGIN -> "التحقق من الحساب وحالة التفعيل 🔄"
            SyncContext.LOGOUT -> "تحديث بيانات الأقسام قبل الخروج 🔄"
            SyncContext.GENERAL -> "تحديث بيانات الأقسام 🔄"
        }

        val initialMsg = when (context) {
            SyncContext.LOGIN -> "جاري التحقق من حسابك وحالة تفعيل أقسامك..."
            SyncContext.LOGOUT -> "جاري تحديث بيانات أقسامك قبل الخروج..."
            SyncContext.GENERAL -> "جاري فحص البيانات والتعديلات المعلقة..."
        }

        _syncUiState.value = UnifiedSyncUiState(
            isShowing = true,
            progress = 0,
            message = initialMsg,
            title = title,
            isFinished = false,
            isSuccess = false,
            errorMessage = null,
            syncContext = context
        )

        performUnifiedSync(
            syncContext = context,
            onProgress = { prog, msg ->
                _syncUiState.value = _syncUiState.value.copy(
                    progress = prog,
                    message = msg
                )
            },
            onComplete = { success, msg ->
                _syncUiState.value = _syncUiState.value.copy(
                    isFinished = true,
                    isSuccess = success,
                    errorMessage = if (!success) msg else null
                )
            }
        )
    }

    fun dismissSyncDialog() {
        val currentContext = _syncUiState.value.syncContext
        val wasSuccess = _syncUiState.value.isSuccess
        _syncUiState.value = _syncUiState.value.copy(isShowing = false)

        if (currentContext == SyncContext.LOGOUT && wasSuccess) {
            signOutTeacher()
        }
    }

    fun startLoginSync() {
        triggerUnifiedSync(SyncContext.LOGIN)
        if (isNetworkAvailable()) {
        }
    }

    fun startGeneralSync() {
        if (!isNetworkAvailable()) {
            try {
                android.widget.Toast.makeText(
                    getApplication(),
                    "🌐 لا يوجد اتصال بالإنترنت. يلزم توفر الاتصال بالإنترنت لتحديث بيانات أقسامك وحالة تفعيلها مع الخادم.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }
        triggerUnifiedSync(SyncContext.GENERAL)
    }

    // Unified Sync Method (Phase-based Progress: 20% -> 60% -> 90% -> 100%)
    fun performUnifiedSync(
        syncContext: SyncContext = SyncContext.GENERAL,
        onProgress: (progress: Int, message: String) -> Unit = { _, _ -> },
        onComplete: (Boolean, String) -> Unit
    ) {
        val uid = _currentUser.value?.uid ?: run {
            onComplete(false, "يجب تسجيل الدخول أولاً")
            return
        }
        if (!isNetworkAvailable()) {
            onComplete(false, "⚠️ لا يوجد اتصال بالإنترنت. يرجى توفير اتصال بالإنترنت لمزامنة البيانات مع الخادم.")
            return
        }

        _isSyncing.value = true
        _syncError.value = null

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val phase1Msg = when (syncContext) {
                    SyncContext.LOGIN -> "20% - جاري فحص الحساب والبيانات..."
                    SyncContext.LOGOUT -> "20% - جاري فحص الدرجات والبيانات المحلية..."
                    SyncContext.GENERAL -> "20% - جاري فحص البيانات المحلية..."
                }
                // Phase 1 (20%): Check local data & pending writes
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onProgress(20, phase1Msg)
                }

                val db = FirebaseFirestore.getInstance()

                val phase2Msg = when (syncContext) {
                    SyncContext.LOGIN -> "60% - جاري التحقق من حالة تفعيل أقسامك..."
                    SyncContext.LOGOUT -> "60% - جاري تحديث بيانات أقسامك قبل الخروج..."
                    SyncContext.GENERAL -> "60% - جاري تحديث بيانات أقسامك على الخادم..."
                }
                // Phase 2 (60%): Upload local modifications (waitForPendingWrites)
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onProgress(60, phase2Msg)
                }

                try {
                    kotlinx.coroutines.withTimeoutOrNull(30000L) {
                        syncPendingExchangePosts()
                        syncAllToCloudInternal()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }


                val currentClasses = _classSections.value
                val currentSubjects = _subjects.value
                val currentCustomizations = _customizations.value
                val currentStudents = _students.value
                val currentGrades = _grades.value

                val sysTime = System.currentTimeMillis()
                prefs.edit().putLong("last_sync_time", sysTime).apply()
                _lastSyncTime.value = sysTime
                _hasPendingWrites.value = false
                _isSyncing.value = false

                // Phase 4 (100%): Success & Post-sync cache refresh
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _classSections.value = currentClasses
                    _subjects.value = currentSubjects
                    _customizations.value = currentCustomizations
                    _students.value = currentStudents
                    _grades.value = currentGrades
                    onProgress(100, "100% - تمت المزامنة بنجاح!")
                    onComplete(true, "100% - تمت المزامنة بنجاح!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isSyncing.value = false
                _syncError.value = e.localizedMessage
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete(false, "حدث خطأ أثناء المزامنة: ${e.localizedMessage}")
                }
            }
        }
    }


    fun retryInitialSync() {
        val user = _currentUser.value ?: return
        if (!isNetworkAvailable()) {
            _syncMessage.value = "⚠️ لا يوجد اتصال بالإنترنت لتنزيل بيانات الحساب. يرجى توفير الإنترنت للمتابعة."
            _syncError.value = "لا يوجد اتصال بالإنترنت"
            _isSyncingCloudData.value = true
            return
        }
        _syncError.value = null
        _syncMessage.value = "جاري التحقق من الحساب وحالة تفعيل الأقسام... 🔄"
        _isSyncingCloudData.value = true
        performUnifiedSync { success, msg ->
            if (success) {
                prefs.edit().putBoolean("is_initial_sync_done_${user.uid}", true).apply()
                _isSyncingCloudData.value = false
                _syncError.value = null
                listenToClassesAndStudents(user.uid)
            } else {
                _syncMessage.value = "⚠️ فشل تحديث المعلومات من الخادم: $msg"
                _syncError.value = msg
                _isSyncingCloudData.value = true
            }
        }
    }


    // --- Core Database Flows ---
    val classSections: StateFlow<List<ClassSection>> = _classSections.asStateFlow()

    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    val students: StateFlow<List<Student>> = _students.asStateFlow()

    val grades: StateFlow<List<Grade>> = _grades.asStateFlow()

    val customizations: StateFlow<List<ClassSubjectCustomization>> = _customizations.asStateFlow()


    fun updateOutOfTen(value: Boolean) {
        _isOutOfTen.value = value
        prefs.edit().putBoolean("is_out_of_ten", value).apply()
        // Reset bounds to logical scales automatically so the user doesn't get weird limits
        if (value) {
            updateBoundsForTerm(1, 4.5f, 5.0f, 6.0f, 7.0f, 8.5f)
            updateBoundsForTerm(2, 4.5f, 5.0f, 6.0f, 7.0f, 8.5f)
            updateBoundsForTerm(3, 4.5f, 5.0f, 6.0f, 7.0f, 8.5f)
        } else {
            updateBoundsForTerm(1, 9.0f, 10.0f, 12.0f, 14.0f, 17.0f)
            updateBoundsForTerm(2, 9.0f, 10.0f, 12.0f, 14.0f, 17.0f)
            updateBoundsForTerm(3, 9.0f, 10.0f, 12.0f, 14.0f, 17.0f)
        }
    }

    fun getBoundsForTerm(termId: Int): List<Double> {
        val is10 = _isOutOfTen.value
        val defaultFail = if (is10) 4.5 else 9.0
        val defaultPass = if (is10) 5.0 else 10.0
        val defaultAcc = if (is10) 6.0 else 12.0
        val defaultGood = if (is10) 7.0 else 14.0
        val defaultVeryGood = if (is10) 8.5 else 17.0

        val termSuffix = when (termId) {
            1 -> "_t1"
            2 -> "_t2"
            else -> "_t3"
        }
        val fail = prefs.getFloat("fail_bound$termSuffix", prefs.getFloat("fail_bound", defaultFail.toFloat())).toDouble()
        val pass = prefs.getFloat("pass_bound$termSuffix", prefs.getFloat("pass_bound", defaultPass.toFloat())).toDouble()
        val acc = prefs.getFloat("acceptable_bound$termSuffix", prefs.getFloat("acceptable_bound", defaultAcc.toFloat())).toDouble()
        val good = prefs.getFloat("good_bound$termSuffix", prefs.getFloat("good_bound", defaultGood.toFloat())).toDouble()
        val veryGood = prefs.getFloat("very_good_bound$termSuffix", prefs.getFloat("very_good_bound", defaultVeryGood.toFloat())).toDouble()

        return listOf(fail, pass, acc, good, veryGood)
    }

    fun updateBoundsForTerm(termId: Int, fail: Float, pass: Float, acc: Float, good: Float, veryGood: Float) {
        val suffix = when (termId) {
            1 -> "_t1"
            2 -> "_t2"
            else -> "_t3"
        }
        
        prefs.edit()
            .putFloat("fail_bound$suffix", fail)
            .putFloat("pass_bound$suffix", pass)
            .putFloat("acceptable_bound$suffix", acc)
            .putFloat("good_bound$suffix", good)
            .putFloat("very_good_bound$suffix", veryGood)
            .apply()
            
        when (termId) {
            1 -> {
                _failBoundT1.value = fail
                _passBoundT1.value = pass
                _acceptableBoundT1.value = acc
                _goodBoundT1.value = good
                _veryGoodBoundT1.value = veryGood
            }
            2 -> {
                _failBoundT2.value = fail
                _passBoundT2.value = pass
                _acceptableBoundT2.value = acc
                _goodBoundT2.value = good
                _veryGoodBoundT2.value = veryGood
            }
            3 -> {
                _failBoundT3.value = fail
                _passBoundT3.value = pass
                _acceptableBoundT3.value = acc
                _goodBoundT3.value = good
                _veryGoodBoundT3.value = veryGood
            }
        }
        
        // Update compatibility global variables
        updateBounds(fail, pass, acc, good, veryGood)
    }

    fun updateBounds(fail: Float, pass: Float, acc: Float, good: Float, veryGood: Float) {
        _failBound.value = fail
        _passBound.value = pass
        _acceptableBound.value = acc
        _goodBound.value = good
        _veryGoodBound.value = veryGood
        prefs.edit()
            .putFloat("fail_bound", fail)
            .putFloat("pass_bound", pass)
            .putFloat("acceptable_bound", acc)
            .putFloat("good_bound", good)
            .putFloat("very_good_bound", veryGood)
            .apply()
    }

    fun updateUseFinalExamFormula(value: Boolean) {
        _useFinalExamFormula.value = value
        prefs.edit().putBoolean("use_final_exam_formula", value).apply()
    }

    fun updateFailNoteExam1and2(value: String) {
        _failNoteExam1and2.value = value
        prefs.edit().putString("fail_note_exam_1_and_2", value).apply()
    }

    // --- Official Stamps Management (ختم المعلم و ختم المدير) ---

    fun updateTeacherStamp(
        name: String,
        shape: String = "rectangle",
        role: String = "المعلم",
        showInReports: Boolean,
        financialId: String = ""
    ) {
        val normalizedRole = when (role.trim()) {
            "معلم", "المعلم" -> "المعلم"
            "مدير", "المدير" -> "المدير"
            else -> role.trim().ifBlank { "المعلم" }
        }
        val normalizedShape = "rectangle"

        _teacherStampName.value = name.trim()
        _teacherStampFinancialId.value = financialId.trim()
        _teacherStampShape.value = normalizedShape
        _teacherStampRole.value = normalizedRole
        _showTeacherStampInReports.value = showInReports

        prefs.edit()
            .putString("stamp_teacher_name", name.trim())
            .putString("stamp_teacher_financial_id", financialId.trim())
            .putString("stamp_teacher_shape", normalizedShape)
            .putString("stamp_teacher_role", normalizedRole)
            .putBoolean("stamp_teacher_show_in_reports", showInReports)
            .apply()
    }

    fun setTeacherStampShowInReports(show: Boolean) {
        _showTeacherStampInReports.value = show
        prefs.edit().putBoolean("stamp_teacher_show_in_reports", show).apply()
    }

    fun updatePrincipalStamp(
        name: String,
        shape: String = "circle",
        role: String = "المدير",
        showInReports: Boolean,
        financialId: String = ""
    ) {
        val normalizedRole = when (role.trim()) {
            "معلم", "المعلم" -> "المعلم"
            "مدير", "المدير" -> "المدير"
            else -> role.trim().ifBlank { "المدير" }
        }
        val normalizedShape = if (shape.equals("rectangle", ignoreCase = true)) "rectangle" else "circle"

        _principalStampName.value = name.trim()
        _principalStampFinancialId.value = financialId.trim()
        _principalStampShape.value = normalizedShape
        _principalStampRole.value = normalizedRole
        _showPrincipalStampInReports.value = showInReports

        prefs.edit()
            .putString("stamp_principal_name", name.trim())
            .putString("stamp_principal_financial_id", financialId.trim())
            .putString("stamp_principal_shape", normalizedShape)
            .putString("stamp_principal_role", normalizedRole)
            .putBoolean("stamp_principal_show_in_reports", showInReports)
            .apply()
    }

    fun setPrincipalStampShowInReports(show: Boolean) {
        _showPrincipalStampInReports.value = show
        prefs.edit().putBoolean("stamp_principal_show_in_reports", show).apply()
    }

    // --- Per-Class Custom Remarks and Bounds Settings ---

    fun getFailBoundForClass(classId: Long, level: Int): Double {
        val key = "${classId}_fail_bound"
        if (prefs.contains(key)) {
            val v = prefs.getFloat(key, 10.0f).toDouble()
            return if (isOutOfTen.value) v / 2.0 else v
        }
        // Defaults based on Level
        val rawDefault = when (level) {
            1 -> 5.0f // السنة الأولى لا رسوب فيها (مستوى الترفيع المعتمد)
            2 -> 8.0f // السنة الثانية 8/20
            3, 4, 5 -> 9.0f // السنة الثالثة إلى الخامسة 9/20
            else -> 10.0f
        }
        return (if (isOutOfTen.value) rawDefault / 2.0 else rawDefault).toDouble()
    }

    fun getPassBoundForClass(classId: Long): Double {
        val key = "${classId}_pass_bound"
        val rawDefault = if (isOutOfTen.value) 5.0f else 10.0f
        val v = prefs.getFloat(key, rawDefault).toDouble()
        return if (isOutOfTen.value && v > 10.0) v / 2.0 else v
    }

    fun getAcceptableBoundForClass(classId: Long): Double {
        val key = "${classId}_acceptable_bound"
        val rawDefault = if (isOutOfTen.value) 6.0f else 12.0f
        val v = prefs.getFloat(key, rawDefault).toDouble()
        return if (isOutOfTen.value && v > 10.0) v / 2.0 else v
    }

    fun getGoodBoundForClass(classId: Long): Double {
        val key = "${classId}_good_bound"
        val rawDefault = if (isOutOfTen.value) 7.0f else 14.0f
        val v = prefs.getFloat(key, rawDefault).toDouble()
        return if (isOutOfTen.value && v > 10.0) v / 2.0 else v
    }

    fun getVeryGoodBoundForClass(classId: Long): Double {
        val key = "${classId}_very_good_bound"
        val rawDefault = if (isOutOfTen.value) 8.5f else 17.0f
        val v = prefs.getFloat(key, rawDefault).toDouble()
        return if (isOutOfTen.value && v > 10.0) v / 2.0 else v
    }

    fun getRemarkTextForClass(classId: Long, level: Int, rangeIndex: Int): String {
        // rangeIndex:
        // 0: below fail bound
        // 1: fail to pass
        // 2: pass to acceptable
        // 3: acceptable to good
        // 4: good to very good
        // 5: very good and above
        val defaultRemarks = when (rangeIndex) {
            0 -> if (level == 1) "متجاوز" else "راسب"
            1 -> "ناجح"
            2 -> "مقبول"
            3 -> "جيد"
            4 -> "جيد جداً"
            else -> "ممتاز"
        }
        val suffix = when (rangeIndex) {
            0 -> "fail"
            1 -> "pass"
            2 -> "acceptable"
            3 -> "good"
            4 -> "very_good"
            else -> "excellent"
        }
        return prefs.getString("${classId}_remark_$suffix", defaultRemarks) ?: defaultRemarks
    }

    fun updateClassCustomBoundsAndRemarks(
        classId: Long,
        fail: Float,
        pass: Float,
        acceptable: Float,
        good: Float,
        veryGood: Float,
        remarkFail: String,
        remarkPass: String,
        remarkAcceptable: String,
        remarkGood: String,
        remarkVeryGood: String,
        remarkExcellent: String
    ) {
        prefs.edit()
            .putFloat("${classId}_fail_bound", fail)
            .putFloat("${classId}_pass_bound", pass)
            .putFloat("${classId}_acceptable_bound", acceptable)
            .putFloat("${classId}_good_bound", good)
            .putFloat("${classId}_very_good_bound", veryGood)
            .putString("${classId}_remark_fail", remarkFail)
            .putString("${classId}_remark_pass", remarkPass)
            .putString("${classId}_remark_acceptable", remarkAcceptable)
            .putString("${classId}_remark_good", remarkGood)
            .putString("${classId}_remark_very_good", remarkVeryGood)
            .putString("${classId}_remark_excellent", remarkExcellent)
            .apply()
    }

    fun clearClassCustomBoundsAndRemarks(classId: Long) {
        prefs.edit()
            .remove("${classId}_fail_bound")
            .remove("${classId}_pass_bound")
            .remove("${classId}_acceptable_bound")
            .remove("${classId}_good_bound")
            .remove("${classId}_very_good_bound")
            .remove("${classId}_remark_fail")
            .remove("${classId}_remark_pass")
            .remove("${classId}_remark_acceptable")
            .remove("${classId}_remark_good")
            .remove("${classId}_remark_very_good")
            .remove("${classId}_remark_excellent")
            .apply()
    }

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    fun selectClass(classId: Long?) {
        _selectedClassId.value = classId
        if (classId != null) {
            try {
                prefs.edit().putLong("last_selected_class_id", classId).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val cls = _classSections.value.find { it.id == classId }
            if (cls != null && !cls.isActivated && (_selectedTermId.value == 2 || _selectedTermId.value == 3)) {
                _selectedTermId.value = 1
            }
        } else {
            try {
                prefs.edit().remove("last_selected_class_id").apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectTerm(termId: Int) {
        _selectedTermId.value = termId
    }

    /**
     * Checks term lock status when selecting a term.
     * - If NOT locked locally: selects term immediately without contacting the server.
     * - If locked locally: checks Firestore server immediately. If activated on server,
     *   removes local lock immediately (updates Room DB) and selects the term.
     */
    fun checkTermLockAndSelect(
        termId: Int,
        classId: Long? = _selectedClassId.value,
        onResult: ((isUnlocked: Boolean) -> Unit)? = null
    ) {
        val targetClassId = classId ?: _selectedClassId.value
        val localClass = if (targetClassId != null) _classSections.value.find { it.id == targetClassId } else null

        // 1. If not locked locally (or no class selected), DO NOT check server again! Select immediately.
        if (localClass == null || localClass.isTermUnlocked(termId)) {
            selectTerm(termId)
            onResult?.invoke(true)
            return
        }

        // 2. If locked locally, check server immediately!
        try {
            val uid = _currentUser.value?.uid ?: return
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("teachers").document(uid).collection("classes").document(targetClassId.toString())
                .get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        val expired = isTermActivationExpired(localClass.academicYear)
                        val isAct = doc.getBoolean("isActivated") ?: false
                        val isT1 = doc.getBoolean("isTerm1Activated") ?: true
                        val isT2 = (doc.getBoolean("isTerm2Activated") ?: false) && !expired
                        val isT3 = (doc.getBoolean("isTerm3Activated") ?: false) && !expired
                        val isT4 = (doc.getBoolean("isTerm4Activated") ?: false) && !expired

                        val updatedClass = localClass.copy(
                            isActivated = isAct,
                            isTerm1Activated = isT1,
                            isTerm2Activated = isT2,
                            isTerm3Activated = isT3,
                            isTerm4Activated = isT4
                        )

                        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                repository.insertClassSection(updatedClass)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            val isUnlockedNow = updatedClass.isTermUnlocked(termId)
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                if (isUnlockedNow) {
                                    selectTerm(termId)
                                    onResult?.invoke(true)
                                } else {
                                    onResult?.invoke(false)
                                }
                            }
                        }
                    } else {
                        onResult?.invoke(false)
                    }
                }
                .addOnFailureListener {
                    onResult?.invoke(false)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult?.invoke(false)
        }
    }

    /**
     * Checks lock status when selecting a class section.
     * - If NOT locked locally for current term: selects class immediately without server call.
     * - If locked locally: checks Firestore server immediately, updates local DB if activated, and unlocks.
     */
    fun selectClassWithLockCheck(
        classId: Long,
        onResult: ((isUnlocked: Boolean) -> Unit)? = null
    ) {
        val localClass = _classSections.value.find { it.id == classId }
        val currentTerm = _selectedTermId.value

        // 1. If not locked locally, select class immediately without server call!
        if (localClass == null || localClass.isTermUnlocked(currentTerm)) {
            selectClass(classId)
            onResult?.invoke(true)
            return
        }

        // 2. If locked locally, check server immediately!
        try {
            val uid = _currentUser.value?.uid ?: return
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("teachers").document(uid).collection("classes").document(classId.toString())
                .get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        val expired = isTermActivationExpired(localClass.academicYear)
                        val isAct = doc.getBoolean("isActivated") ?: false
                        val isT1 = doc.getBoolean("isTerm1Activated") ?: true
                        val isT2 = (doc.getBoolean("isTerm2Activated") ?: false) && !expired
                        val isT3 = (doc.getBoolean("isTerm3Activated") ?: false) && !expired
                        val isT4 = (doc.getBoolean("isTerm4Activated") ?: false) && !expired

                        val updatedClass = localClass.copy(
                            isActivated = isAct,
                            isTerm1Activated = isT1,
                            isTerm2Activated = isT2,
                            isTerm3Activated = isT3,
                            isTerm4Activated = isT4
                        )

                        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                repository.insertClassSection(updatedClass)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            val isUnlockedNow = updatedClass.isTermUnlocked(currentTerm)
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                selectClass(classId)
                                onResult?.invoke(isUnlockedNow)
                            }
                        }
                    } else {
                        selectClass(classId)
                        onResult?.invoke(false)
                    }
                }
                .addOnFailureListener {
                    selectClass(classId)
                    onResult?.invoke(false)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            selectClass(classId)
            onResult?.invoke(false)
        }
    }

    fun selectSubject(subjectId: Long?) {
        _selectedSubjectId.value = subjectId
    }

    fun selectStudent(studentId: Long?) {
        _selectedStudentId.value = studentId
    }

    // --- Dynamic Computations (Averages & Ranking) ---
    // Calculates averages and ranks of students in the currently selected Class Section and active Term
    val currentClassPerformance: StateFlow<List<StudentPerformance>> = combine(
        students,
        subjects,
        grades,
        customizations,
        combine(_selectedClassId, _selectedTermId) { classId, termId -> classId to termId }
    ) { allStuds, allSubs, allGrades, allCusts, classAndTerm ->
        val activeClassId = classAndTerm.first
        val activeTermId = classAndTerm.second
        if (activeClassId == null) return@combine emptyList()

        val clazz = classSections.value.find { it.id == activeClassId }
        val targetLevel = clazz?.level ?: 1
        
        // Resolve subjects and maxPoints for this class level
        val resolvedSubjects = buildClassSubjects(activeClassId, targetLevel, allSubs, allCusts)

        val classStudents = allStuds.filter { it.classId == activeClassId }
        val classStudentIds = classStudents.map { it.id }.toSet()
        // Filter grades for selected class AND selected term (Term 3 if in Final Term Only mode)!
        val targetTermForGrades = if (activeTermId == 4) 3 else activeTermId
        val classGrades = allGrades.filter { it.studentId in classStudentIds && it.termId == targetTermForGrades }

        // Map containing student grades grouped by student id
        val studentGradesGroup = classGrades.groupBy { it.studentId }

        // Convert key-value details for each student
        val studentsWithAverages = classStudents.map { student ->
            val gradMap = studentGradesGroup[student.id]?.associate { it.subjectId to it.score } ?: emptyMap()
            
            // Sum student's scores and total max points for resolved subjects
            var scoreSum = 0.0
            var maxPointsSum = 0
            resolvedSubjects.forEach { sub ->
                val score = gradMap[sub.id] ?: 0.0
                scoreSum += score
                maxPointsSum += sub.maxPoints
            }
            
            // Calculate average out of 20: (scoreSum / maxPointsSum) * 20.0
            val average = if (maxPointsSum > 0) {
                (scoreSum / maxPointsSum) * 20.0
            } else {
                0.0
            }

            student to Pair(average, gradMap)
        }

        // Separate students with grades from those without grades
        val studentsWithGrades = studentsWithAverages.filter { it.second.second.isNotEmpty() }
        val studentsWithoutGrades = studentsWithAverages.filter { it.second.second.isEmpty() }

        // Sort students with grades by average descending to compute rank
        val sortedByPerf = studentsWithGrades.sortedByDescending { it.second.first }

        // Compute ranks (handle duplicate averages sharing the same rank)
        var currentRank = 1
        var previousAverage = -1.0

        val rankedPerformanceList = sortedByPerf.mapIndexed { index, (student, scorePair) ->
            val average = scorePair.first
            if (average != previousAverage) {
                currentRank = index + 1
                previousAverage = average
            }
            StudentPerformance(
                student = student,
                averageScore = average,
                rank = currentRank,
                gradesMap = scorePair.second
            )
        }

        val unrankedPerformanceList = studentsWithoutGrades.map { (student, scorePair) ->
            StudentPerformance(
                student = student,
                averageScore = 0.0,
                rank = 0, // 0 indicates unranked
                gradesMap = emptyMap()
            )
        }

        val performanceList = rankedPerformanceList + unrankedPerformanceList

        performanceList.sortedBy { it.student.name } // return sorted alphabetically or by Rank depending on screen preference
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flow calculating registration progress for each term and annually
    val classProgress: StateFlow<AnnualProgress?> = combine(
        combine(students, classSections, customizations) { st, sec, cust -> Triple(st, sec, cust) },
        subjects,
        grades,
        _selectedClassId
    ) { (allStudents, sections, custs), allSubjects, allGrades, activeClassId ->
        if (activeClassId == null) return@combine null

        val clazz = sections.find { it.id == activeClassId } ?: return@combine null
        val targetLevel = clazz.level
        val classStudents = allStudents.filter { it.classId == activeClassId }
        val resolvedSubjects = buildClassSubjects(activeClassId, targetLevel, allSubjects, custs)

        val numStudents = classStudents.size
        val numSubjects = resolvedSubjects.size
        val totalPerTerm = numStudents * numSubjects

        val studentIds = classStudents.map { it.id }.toSet()
        val subjectIds = resolvedSubjects.map { it.id }.toSet()

        // Filter grades of these students and subjects, and deduplicate by (studentId, subjectId, termId)
        val classGrades = allGrades
            .filter { it.studentId in studentIds && it.subjectId in subjectIds }
            .distinctBy { Triple(it.studentId, it.subjectId, it.termId) }

        val terms = listOf(
            1 to "الفصل الأول",
            2 to "الفصل الثاني",
            3 to "الفصل الثالث"
        )

        val termProgresses = terms.map { (termId, termName) ->
            val entered = classGrades.count { it.termId == termId }
            val pct = if (totalPerTerm > 0) entered.toFloat() / totalPerTerm else 0f
            TermProgress(termId, termName, entered, totalPerTerm, pct)
        }

        val annualRequired = totalPerTerm * 3
        val annualEntered = classGrades.count { it.termId in 1..3 }
        val annualPct = if (annualRequired > 0) annualEntered.toFloat() / annualRequired else 0f

        AnnualProgress(termProgresses, annualEntered, annualRequired, annualPct)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- CRUD Actions ---

    fun getAutomaticAcademicYear(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1 // 1-indexed (Jan is 1, Dec is 12)
        // Starts in December (12). So if month is 12 or Jan-July (month <= 7) we format it.
        // If progress is between Aug and Nov (8 to 11), they register for the *next* school year.
        val startYear = if (month >= 8) year else year - 1
        val endYear = startYear + 1
        return "$startYear-$endYear"
    }

    // Classes
    fun updateClassSectionLevel(classId: Long, newLevel: Int) {
        if (newLevel !in 1..6) return
        val existing = _classSections.value.find { it.id == classId } ?: return
        val updated = existing.copy(level = newLevel)
        _classSections.value = _classSections.value.map { if (it.id == classId) updated else it }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.insertClassSection(updated)
                val uid = _currentUser.value?.uid
                if (uid != null) {
                    val classData = mapOf(
                        "id" to updated.id,
                        "name" to updated.name,
                        "wilaya" to updated.wilaya,
                        "moughataa" to updated.moughataa,
                        "schoolName" to updated.schoolName,
                        "level" to updated.level,
                        "sectionName" to updated.sectionName,
                        "academicYear" to updated.academicYear
                    )
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("teachers").document(uid)
                        .collection("classes").document(classId.toString())
                        .set(classData, com.google.firebase.firestore.SetOptions.merge())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addClassSection(
        schoolName: String,
        level: Int,
        sectionName: String,
        wilaya: String = "",
        moughataa: String = "",
        academicYear: String = ""
    ): Boolean {
        if (schoolName.isBlank() || sectionName.isBlank()) return false
        val finalAcademicYear = if (academicYear.isNotBlank()) academicYear else getAutomaticAcademicYear()

        // الفحص الأول - عدد الأقسام في نفس السنة
        val sameYearCount = _classSections.value.count { it.academicYear == finalAcademicYear }
        if (sameYearCount >= 12) {
            try {
                android.widget.Toast.makeText(
                    getApplication(),
                    "بلغت الحد الأقصى 12 قسماً في السنة الدراسية $finalAcademicYear. احذف قسماً قديماً لإضافة قسم جديد.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

        // الفحص الثاني - عدد السنوات الدراسية
        val existingYears = _classSections.value.map { it.academicYear }.filter { it.isNotBlank() }.distinct()
        if (finalAcademicYear !in existingYears && existingYears.size >= 2) {
            try {
                android.widget.Toast.makeText(
                    getApplication(),
                    "لا يمكن إضافة سنة دراسية ثالثة. التطبيق يحتفظ بسنتين دراسيتين فقط. احذف أقساماً من سنة قديمة لبدء سنة جديدة.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

        val displayName = "${schoolName.trim()} - س$level (${sectionName.trim()}) [$finalAcademicYear]"
        val id = System.currentTimeMillis() * 1000L + (0..999).random()
        
        val classSection = ClassSection(
            id = id,
            name = displayName,
            wilaya = wilaya.trim(),
            moughataa = moughataa.trim(),
            schoolName = schoolName.trim(),
            level = level,
            sectionName = sectionName.trim(),
            academicYear = finalAcademicYear,
            isSyncedToServer = false
        )

        // Update in-memory state immediately so UI and selectedClassId have zero race conditions
        _classSections.value = _classSections.value.filterNot { it.id == id } + classSection
        
        // Save locally first ALWAYS
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.insertClassSection(classSection)
        }
        
        // Open newly created class section immediately
        selectClass(id)
        
        val uid = _currentUser.value?.uid ?: return true
        
        // Save to Firestore
        val db = FirebaseFirestore.getInstance()
        val user = _currentUser.value
        val dName = user?.displayName ?: "معلم ($uid)"
        val emailStr = user?.email ?: ""
        val teacherData = mapOf(
            "displayName" to dName,
            "email" to emailStr,
            "syncedAt" to System.currentTimeMillis()
        )
        db.collection("teachers").document(uid).set(teacherData, com.google.firebase.firestore.SetOptions.merge())

        val classData = mapOf(
            "id" to classSection.id,
            "name" to classSection.name,
            "wilaya" to classSection.wilaya,
            "moughataa" to classSection.moughataa,
            "schoolName" to classSection.schoolName,
            "level" to classSection.level,
            "sectionName" to classSection.sectionName,
            "academicYear" to classSection.academicYear
        )

        db.collection("teachers").document(uid)
            .collection("classes").document(id.toString())
            .set(classData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val syncedClass = classSection.copy(isSyncedToServer = true)
                        repository.insertClassSection(syncedClass)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        return true
    }

    fun fillActiveClassWithDemoStudents(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val userEmail = _currentUser.value?.email?.trim()?.lowercase() ?: ""
        if (userEmail != "elyedalimoctar@gmail.com") {
            onFailure("غير مصرح لك")
            return
        }

        val activeClassId = _selectedClassId.value
        val activeClass = _classSections.value.find { it.id == activeClassId }
        if (activeClassId == null || activeClass == null) {
            onFailure("افتح قسما أولا")
            return
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val targetLevel = activeClass.level
                val allSubs = try { repository.allSubjects.first() } catch (e: Exception) { emptyList() }
                val resolvedSubjects = buildClassSubjects(activeClassId, targetLevel, allSubs, _customizations.value)

                val boyNames = listOf("أحمد", "محمد", "سيدي", "السالك", "أبي", "الأمين", "اعلي", "المختار", "البكاي", "الطيب", "بونا", "الحسن", "الشيخ", "العربي", "يحيى", "ابراهيم", "بلال", "عثمان", "محمود", "إسماعيل", "يعقوب", "يوسف", "هارون", "داوود", "سليمان", "إدريس", "نوح", "موسى", "عيسى", "حمزة", "عمار", "ياسر", "سلمان", "خالد", "طارق", "زياد")
                val girlNames = listOf("فاطمة", "عيشة", "مريم", "زينب", "خديجة", "توتو", "ميمونة", "رقية", "آمنة", "سالمة", "سلمى", "لالة", "أم كلثوم", "هند", "أسماء", "صفية", "حفصة", "جويرية", "سودة", "فدوى", "سميرة", "نجاة", "سعاد", "وفاء", "هدى", "منى", "لبنى", "رجاء", "إيمان", "حنان", "نجلاء")
                val fatherNames = listOf("أحمد", "بابه", "أعل", "بلال", "الشيخ", "بكار", "أعبيدي", "مسعود", "الطالب", "السالك", "المختار", "صو", "فال", "محمذن", "جدو", "سيديا", "أحمدو", "محمدن", "بداه", "بابا", "سيداتي", "الداه", "محمد الأمين", "الحسن", "المرابط", "سيدي محمد", "لمرابط", "المصطفى")

                val baseTime = System.currentTimeMillis()
                for (i in 1..70) {
                    val isMale = (i % 2 != 0)
                    val gender = if (isMale) "ذكر" else "أنثى"
                    val firstName = if (isMale) boyNames[(i * 7) % boyNames.size] else girlNames[(i * 7) % girlNames.size]
                    val father = fatherNames[(i * 13) % fatherNames.size]
                    val fullName = if (isMale) "$firstName ولد $father" else "$firstName بنت $father"
                    val studentId = baseTime + i * 1000L + (1..999).random()

                    val student = Student(
                        id = studentId,
                        classId = activeClassId,
                        name = fullName,
                        gender = gender,
                        schoolId = "${1000 + i}",
                        nationalId = "2025000${100 + i}",
                        healthNotes = "",
                        parentPhone1 = "",
                        parentPhone2 = ""
                    )

                    val gradesList = mutableListOf<Grade>()
                    for (subject in resolvedSubjects) {
                        val maxPoints = subject.maxPoints.toDouble()
                        for (termId in 1..3) {
                            val randomFactor = (0..1000).random() / 1000.0
                            val rawScore = randomFactor * maxPoints
                            val score = kotlin.math.round(rawScore * 10.0) / 10.0
                            gradesList.add(Grade(studentId = studentId, subjectId = subject.id, termId = termId, score = score))
                        }
                    }

                    repository.insertStudent(student)
                    if (gradesList.isNotEmpty()) {
                        repository.insertGrades(gradesList)
                    }
                }

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onFailure(e.localizedMessage ?: "حدث خطأ أثناء توليد بيانات الطلاب")
                }
            }
        }
    }

    fun generateDemoDataForClassSection(classId: Long, level: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val userEmail = _currentUser.value?.email?.trim()?.lowercase() ?: ""
            if (userEmail != "elyedalimoctar@gmail.com") {
                return@launch
            }
            val uid = _currentUser.value?.uid ?: return@launch
            
            // Query existing subjects for this level instead of auto-creating them
            val existingSubjects = try {
                repository.allSubjects.first().filter { it.level == level }
            } catch (e: Exception) {
                emptyList()
            }
            
            val classSubjects = existingSubjects.map { Pair(it.id, it.maxPoints) }
            
            // Mauritanian names generator
            val boyNames = listOf("أحمد", "محمد", "سيدي", "السالك", "أبي", "الأمين", "اعلي", "المختار", "البكاي", "الطيب", "بونا", "الحسن", "الشيخ", "العربي", "يحيى", "ابراهيم")
            val girlNames = listOf("فاطمة", "عيشة", "مريم", "زينب", "خديجة", "توتو", "ميمونة", "رقية", "آمنة", "سالمة", "سلمى", "لالة")
            val fatherNames = listOf("أحمد", "بابه", "أعل", "بلال", "الشيخ", "بكار", "أعبيدي", "مسعود", "الطالب", "السالك", "المختار", "صو")
            
            // Generate 15 students for the created class
            for (i in 1..15) {
                val isMale = (i % 2 != 0)
                val gender = if (isMale) "ذكر" else "أنثى"
                val firstName = if (isMale) boyNames[i % boyNames.size] else girlNames[i % girlNames.size]
                val father = fatherNames[(i * 3) % fatherNames.size]
                val fullName = if (isMale) "$firstName ولد $father" else "$firstName بنت $father"
                
                val studentId = System.currentTimeMillis() + i * 1000L + (0..999).random()
                
                val student = Student(
                    id = studentId,
                    classId = classId,
                    name = fullName,
                    gender = gender,
                    schoolId = "${25000 + i}",
                    nationalId = "217030${1000 + i}",
                    healthNotes = "",
                    parentPhone1 = "",
                    parentPhone2 = ""
                )
                
                val gradesMap = mutableMapOf<String, Double>()
                val studentFactor = 0.45 + (i % 8) * 0.07 // 0.45 to 0.94
                val gradesList = mutableListOf<Grade>()
                
                for (subjectPair in classSubjects) {
                    val subjectId = subjectPair.first
                    val maxPoints = subjectPair.second
                    
                    for (termId in 1..3) {
                        val variation = (((i * termId + subjectId) % 11) - 5) / 100.0
                        val scoreRatio = (studentFactor + variation).coerceIn(0.15, 1.0)
                        val rawScore = scoreRatio * maxPoints
                        val roundedScore = (rawScore * 2).toInt() / 2.0
                        
                        gradesMap["${subjectId}_${termId}"] = roundedScore
                        gradesList.add(Grade(studentId = studentId, subjectId = subjectId, termId = termId, score = roundedScore))
                    }
                }
                
                val studentData = mapOf(
                    "id" to student.id,
                    "name" to student.name,
                    "classId" to student.classId,
                    "frenchName" to student.frenchName,
                    "schoolId" to student.schoolId,
                    "nationalId" to student.nationalId,
                    "healthNotes" to student.healthNotes,
                    "parentPhone1" to student.parentPhone1,
                    "parentPhone2" to student.parentPhone2,
                    "gender" to student.gender,
                    "grades" to gradesMap
                )
                
                try {
                    // Save locally in Room database
                    repository.insertStudent(student)
                    repository.insertGrades(gradesList)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun adminLockAllTermsOnServer(onProgress: (Int) -> Unit, onComplete: (Boolean, String) -> Unit) {
        val userEmail = _currentUser.value?.email?.trim()?.lowercase() ?: ""
        if (userEmail != "elyedalimoctar@gmail.com") {
            onComplete(false, "غير مصرح")
            return
        }

        if (!isNetworkAvailable()) {
            onComplete(false, "لا يوجد اتصال بالإنترنت")
            return
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val snapshot = com.google.android.gms.tasks.Tasks.await(
                    db.collectionGroup("classes").get()
                )

                val filteredDocs = snapshot.documents.filter { doc ->
                    val t2 = doc.getBoolean("isTerm2Activated") ?: false
                    val t3 = doc.getBoolean("isTerm3Activated") ?: false
                    val t4 = doc.getBoolean("isTerm4Activated") ?: false
                    t2 || t3 || t4
                }

                if (filteredDocs.isEmpty()) {
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onComplete(true, "تم بنجاح! لا توجد فصول مفتوحة تتطلب القفل على الخادم (0 فصول).")
                    }
                    return@launch
                }

                val updateMap = mapOf(
                    "isTerm2Activated" to false,
                    "isTerm3Activated" to false,
                    "isTerm4Activated" to false,
                    "isActivated" to false
                )

                val chunks = filteredDocs.chunked(400)
                var processedCount = 0

                for (chunk in chunks) {
                    val batch = db.batch()
                    for (doc in chunk) {
                        batch.set(doc.reference, updateMap, com.google.firebase.firestore.SetOptions.merge())
                    }
                    com.google.android.gms.tasks.Tasks.await(batch.commit())
                    processedCount += chunk.size
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onProgress(processedCount)
                    }
                }

                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete(true, "تم قفل جميع الفصول على الخادم بنجاح! الإجمالي: $processedCount فصلاً.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete(false, e.localizedMessage ?: "حدث خطأ غير متوقع أثناء القفل على الخادم.")
                }
            }
        }
    }

    fun deleteClassSection(id: Long, onBlocked: (String) -> Unit = {}) {
        if (!isNetworkAvailable()) {
            onBlocked("حذف القسم يتطلب اتصالاً بالإنترنت ليتم حذفه من الخادم أيضاً. تحقق من اتصالك وأعد المحاولة.")
            return
        }

        val uid = _currentUser.value?.uid
        if (uid == null) {
            onBlocked("تعذر تحديد حسابك. أعد تسجيل الدخول ثم حاول مجدداً.")
            return
        }

        FirebaseFirestore.getInstance().collection("teachers").document(uid)
            .collection("classes").document(id.toString())
            .delete()
            .addOnSuccessListener {
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        repository.deleteManualTermAveragesByClass(id)
                        repository.deleteGradesByClass(id)
                        repository.deleteStudentsByClass(id)
                        repository.deleteCustomizationsByClass(id)
                        repository.deleteClassSection(id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (_selectedClassId.value == id) {
                    _selectedClassId.value = null
                }
            }
            .addOnFailureListener { e ->
                onBlocked("تعذر حذف القسم من الخادم: ${e.message}. لم يُحذف شيء من جهازك. أعد المحاولة.")
            }
    }

    // Students (Kept local in Room DB & Google Drive)
    fun addStudent(
        name: String,
        classId: Long,
        frenchName: String = "",
        schoolId: String = "",
        nationalId: String = "",
        healthNotes: String = "",
        parentPhone1: String = "",
        parentPhone2: String = "",
        gender: String = "ذكر"
    ) {
        if (name.isBlank()) return
        val id = System.currentTimeMillis()
        val student = Student(
            id = id,
            name = name.trim(),
            classId = classId,
            frenchName = frenchName.trim(),
            schoolId = schoolId.trim(),
            nationalId = nationalId.trim(),
            healthNotes = healthNotes.trim(),
            parentPhone1 = parentPhone1.trim(),
            parentPhone2 = parentPhone2.trim(),
            gender = gender
        )

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.insertStudent(student)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addStudent(name: String, classId: Long, gender: String = "ذكر") {
        if (name.isBlank()) return
        val id = System.currentTimeMillis()
        
        val student = Student(
            id = id,
            name = name.trim(),
            classId = classId,
            gender = gender
        )

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.insertStudent(student)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteStudent(id: Long) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.deleteGradesByStudent(id)
                repository.deleteManualTermAveragesByStudent(id)
                repository.deleteStudent(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
            
        if (_selectedStudentId.value == id) {
            _selectedStudentId.value = null
        }
    }

    fun updateStudent(
        id: Long,
        name: String,
        frenchName: String = "",
        schoolId: String = "",
        nationalId: String = "",
        healthNotes: String = "",
        parentPhone1: String = "",
        parentPhone2: String = "",
        gender: String = "ذكر"
    ) {
        if (name.isBlank()) return
        val student = students.value.find { it.id == id }
        val classId = student?.classId ?: return
        
        val updatedStudent = Student(
            id = id,
            name = name.trim(),
            classId = classId,
            frenchName = frenchName.trim(),
            schoolId = schoolId.trim(),
            nationalId = nationalId.trim(),
            healthNotes = healthNotes.trim(),
            parentPhone1 = parentPhone1.trim(),
            parentPhone2 = parentPhone2.trim(),
            gender = gender
        )

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.insertStudent(updatedStudent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isNetworkAvailable(): Boolean {
        try {
            val connectivityManager = getApplication<Application>().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            if (connectivityManager != null) {
                val activeNetwork = connectivityManager.activeNetwork
                if (activeNetwork != null) {
                    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                    if (capabilities != null) {
                        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private var networkCallback: android.net.ConnectivityManager.NetworkCallback? = null

    fun startNetworkWatcher() {
        if (networkCallback != null) return
        try {
            val connectivityManager = getApplication<Application>().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager ?: return
            val callback = object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    super.onAvailable(network)
                    performAutoDriveBackup(300000L)
                    resendUnsyncedClasses()
                }
            }
            connectivityManager.registerDefaultNetworkCallback(callback)
            networkCallback = callback
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopNetworkWatcher() {
        val callback = networkCallback ?: return
        try {
            val connectivityManager = getApplication<Application>().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            connectivityManager?.unregisterNetworkCallback(callback)
            networkCallback = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Subjects
    fun addSubject(name: String, level: Int = 1, maxPoints: Int = 20, classId: Long = 0L, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (name.isBlank()) {
            onFailure("اسم المادة لا يمكن أن يكون فارغاً.")
            return
        }
        val id = System.currentTimeMillis() * 1000L + (0..999).random()
        val subject = Subject(id = id, name = name.trim(), level = level, maxPoints = maxPoints, classId = classId)
        
        // Save locally first
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.insertSubject(subject)
        }
        onSuccess()
    }

    fun updateSubject(id: Long, name: String, level: Int, maxPoints: Int, classId: Long = 0L, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (name.isBlank()) {
            onFailure("اسم المادة لا يمكن أن يكون فارغاً.")
            return
        }
        val subject = Subject(id = id, name = name.trim(), level = level, maxPoints = maxPoints, classId = classId)
        
        // Save locally first
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.insertSubject(subject)
        }
        onSuccess()
    }

    fun deleteSubject(id: Long, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // Save locally first
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.deleteGradesBySubject(id)
            repository.deleteSubject(id)
        }
        if (_selectedSubjectId.value == id) {
            _selectedSubjectId.value = null
        }
        
        // Add to local deleted subjects list
        try {
            val newSet = deletedGlobalSubjectIds.value + id
            deletedGlobalSubjectIds.value = newSet
            prefs.edit().putStringSet("deleted_global_subject_ids", newSet.map { it.toString() }.toSet()).apply()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        updateSubjectsList()
        onSuccess()
    }

    private fun resolvedMaxPointsFor(studentId: Long, subjectId: Long): Int? {
        val subject = subjects.value.find { it.id == subjectId } ?: return null
        val classId = _students.value.find { it.id == studentId }?.classId ?: return subject.maxPoints
        val cust = customizations.value.find { it.classId == classId && it.subjectId == subjectId }
        return cust?.maxPoints ?: subject.maxPoints
    }

    // Save grades list (bulk entry for subject or student)
    fun saveGrades(gradesList: List<Grade>) {
        if (gradesList.isEmpty()) return

        // Coerce grades to be within [0, maxPoints] of their subject in this student's class
        val validatedGradesList = gradesList.map { grade ->
            val limit = resolvedMaxPointsFor(grade.studentId, grade.subjectId)
            if (limit != null) {
                grade.copy(score = grade.score.coerceIn(0.0, limit.toDouble()))
            } else {
                grade
            }
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.NonCancellable) {
            try {
                repository.insertGrades(validatedGradesList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Single grade saving
    fun saveSingleGrade(studentId: Long, subjectId: Long, score: Double, termId: Int) {
        val limit = resolvedMaxPointsFor(studentId, subjectId)
        val finalScore = if (limit != null) {
            score.coerceIn(0.0, limit.toDouble())
        } else {
            score
        }

        val grade = Grade(studentId = studentId, subjectId = subjectId, termId = termId, score = finalScore)

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.NonCancellable) {
            try {
                repository.insertGrade(grade)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteGrade(studentId: Long, subjectId: Long, termId: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.NonCancellable) {
            try {
                repository.deleteGrade(studentId, subjectId, termId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Report / Text Exports Generation ---

    // 1. Export student list for selected class
    fun exportStudentListText(className: String, studentsInClass: List<Student>): String {
        val builder = java.lang.StringBuilder()
        builder.append("📖 لائحة طلاب القسم: $className\n")
        builder.append("-----------------------------\n")
        if (studentsInClass.isEmpty()) {
            builder.append("لا يوجد طلاب مسجلين في هذا القسم.\n")
        } else {
            val sorted = studentsInClass.sortedBy { it.id }
            sorted.forEachIndexed { index, student ->
                builder.append("${index + 1}. ${student.name}\n")
            }
        }
        builder.append("-----------------------------\n")
        builder.append("تم التصدير بواسطة تطبيق دفتر المعلم")
        return builder.toString()
    }

    // 2. Export hymns/singers groups
    fun exportHymnsGroupsText(className: String, studentsInClass: List<Student>): String {
        val builder = java.lang.StringBuilder()
        builder.append("🎵 لائحة وتوزيع مجموعات الأناشيد - قسم: $className\n")
        builder.append("-----------------------------\n")
        if (studentsInClass.isEmpty()) {
            builder.append("لا يوجد طلاب مسجلين في هذا القسم.\n")
        } else {
            val sorted = studentsInClass.sortedBy { it.id }
            val groups = List(5) { mutableListOf<Student>() }
            sorted.forEachIndexed { i, s ->
                groups[i % 5].add(s)
            }
            val groupNames = listOf("المجموعة الأولى 🎤", "المجموعة الثانية 🎺", "المجموعة الثالثة 🥁", "المجموعة الرابعة 🎻", "المجموعة الخامسة 📣")
            groups.forEachIndexed { index, list ->
                builder.append("\n📌 ${groupNames[index]} (${list.size} طالب):\n")
                if (list.isEmpty()) {
                    builder.append("  - فارغ\n")
                } else {
                    list.forEachIndexed { sIdx, student ->
                        builder.append("  ${sIdx + 1}. ${student.name}\n")
                    }
                }
            }
        }
        builder.append("\n-----------------------------\n")
        builder.append("تم التصدير بواسطة تطبيق دفتر المعلم")
        return builder.toString()
    }

    // 3. Export sweepers/cleaning groups
    fun exportCleaningGroupsText(className: String, studentsInClass: List<Student>): String {
        val builder = java.lang.StringBuilder()
        builder.append("🧹 لائحة وجدول الكناسة والنظافة الدوري - قسم: $className\n")
        builder.append("-----------------------------\n")
        if (studentsInClass.isEmpty()) {
            builder.append("لا يوجد طلاب مسجلين في هذا القسم.\n")
        } else {
            val sorted = studentsInClass.sortedBy { it.id }
            val groups = List(5) { mutableListOf<Student>() }
            sorted.forEachIndexed { i, s ->
                groups[i % 5].add(s)
            }
            val days = listOf("يوم الاثنين 📅", "يوم الثلاثاء 📅", "يوم الأربعاء 📅", "يوم الخميس 📅", "يوم الجمعة 📅")
            groups.forEachIndexed { index, list ->
                builder.append("\n📌 ${days[index]} (${list.size} طالب):\n")
                if (list.isEmpty()) {
                    builder.append("  - فارغ\n")
                } else {
                    list.forEachIndexed { sIdx, student ->
                        builder.append("  ${sIdx + 1}. ${student.name}\n")
                    }
                }
            }
        }
        builder.append("\n-----------------------------\n")
        builder.append("تم التصدير بواسطة تطبيق دفتر المعلم")
        return builder.toString()
    }

    fun getTermName(termId: Int): String {
        return when (termId) {
            1 -> "الفصل الأول"
            2 -> "الفصل الثاني"
            3 -> "الفصل الثالث"
            4 -> "الفصل الأخير فقط"
            else -> "الفصل الدراسي"
        }
    }

    fun hasSubjectGradesForTerm(studentId: Long, termId: Int): Boolean {
        return grades.value.any { it.studentId == studentId && it.termId == termId }
    }

    fun calculateStudentTermAverage(studentId: Long, classId: Long, termId: Int): Double {
        val allGrades = grades.value
        val termGrades = allGrades.filter { it.studentId == studentId && it.termId == termId }
        val activeClass = classSections.value.find { it.id == classId } ?: return 0.0
        val targetLevel = activeClass.level
        
        val resolvedSubjects = buildClassSubjects(classId, targetLevel)

        // In Final-Term-Only mode, the manual average for terms 1 and 2 takes priority over any stray grades
        if (activeClass.isTerm4Activated && termId in 1..2) {
            val manualFirst = _manualTermAverages.value.find { it.studentId == studentId && it.termId == termId }
            if (manualFirst != null && manualFirst.averageScore > 0.0) {
                return manualFirst.averageScore
            }
        }

        // If subject grades were recorded for this term, calculate the average directly from subjects
        if (termGrades.isNotEmpty()) {
            val gradMap = termGrades.associate { it.subjectId to it.score }
            var scoreSum = 0.0
            var maxPointsSum = 0
            resolvedSubjects.forEach { sub ->
                val score = gradMap[sub.id] ?: 0.0
                scoreSum += score
                maxPointsSum += sub.maxPoints
            }
            
            if (maxPointsSum > 0) {
                return (scoreSum / maxPointsSum) * 20.0
            }
        }

        // If no subject grades exist for this term (e.g. Final Term Only mode), fallback to manual term average
        val manualAvg = _manualTermAverages.value.find { it.studentId == studentId && it.termId == termId }
        if (manualAvg != null && manualAvg.averageScore > 0.0) {
            return manualAvg.averageScore
        }

        return 0.0
    }

    fun calculateStudentTermTotalScore(studentId: Long, classId: Long, termId: Int): Pair<Double, Int> {
        val allGrades = grades.value
        val termGrades = allGrades.filter { it.studentId == studentId && it.termId == termId }
        val activeClass = classSections.value.find { it.id == classId } ?: return Pair(0.0, 0)
        val targetLevel = activeClass.level
        
        val resolvedSubjects = buildClassSubjects(classId, targetLevel)

        // In Final-Term-Only mode, the manual average for terms 1 and 2 takes priority over any stray grades
        if (activeClass.isTerm4Activated && termId in 1..2) {
            val manualFirst = _manualTermAverages.value.find { it.studentId == studentId && it.termId == termId }
            if (manualFirst != null && manualFirst.averageScore > 0.0) {
                val maxPointsSumFirst = resolvedSubjects.sumOf { it.maxPoints }
                val scoreSumFirst = (manualFirst.averageScore / 20.0) * maxPointsSumFirst
                return Pair(scoreSumFirst, maxPointsSumFirst)
            }
        }

        if (termGrades.isEmpty()) {
            val manualAvg = _manualTermAverages.value.find { it.studentId == studentId && it.termId == termId }
            if (manualAvg != null && manualAvg.averageScore > 0.0) {
                val maxPointsSum = resolvedSubjects.sumOf { it.maxPoints }
                val scoreSum = (manualAvg.averageScore / 20.0) * maxPointsSum
                return Pair(scoreSum, maxPointsSum)
            }
        }

        val gradMap = termGrades.associate { it.subjectId to it.score }
        var scoreSum = 0.0
        var maxPointsSum = 0
        resolvedSubjects.forEach { sub ->
            val score = gradMap[sub.id] ?: 0.0
            scoreSum += score
            maxPointsSum += sub.maxPoints
        }
        return Pair(scoreSum, maxPointsSum)
    }

    fun saveStudentManualTermAverage(studentId: Long, termId: Int, score: Double) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val manualAvg = com.example.data.models.StudentManualTermAverage(studentId, termId, score.coerceIn(0.0, 20.0))
            repository.insertManualTermAverage(manualAvg)
            val current = _manualTermAverages.value.toMutableList()
            current.removeAll { it.studentId == studentId && it.termId == termId }
            current.add(manualAvg)
            _manualTermAverages.value = current
        }
    }

    fun deleteStudentManualTermAverage(studentId: Long, termId: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.deleteManualTermAverage(studentId, termId)
            val current = _manualTermAverages.value.toMutableList()
            current.removeAll { it.studentId == studentId && it.termId == termId }
            _manualTermAverages.value = current
        }
    }

    fun saveStudentFinalOnlyGrades(
        studentId: Long,
        term3Grades: Map<Long, Double>,
        term1Avg: Double?,
        term2Avg: Double?,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            term3Grades.forEach { (subjectId, score) ->
                val limit = resolvedMaxPointsFor(studentId, subjectId)
                val finalScore = if (limit != null) score.coerceIn(0.0, limit.toDouble()) else score
                val grade = Grade(studentId = studentId, subjectId = subjectId, termId = 3, score = finalScore)
                repository.insertGrade(grade)
            }
            val term1AvgSafe = term1Avg?.coerceIn(0.0, 20.0)
            val term2AvgSafe = term2Avg?.coerceIn(0.0, 20.0)
            if (term1Avg != null) {
                val m1 = com.example.data.models.StudentManualTermAverage(studentId = studentId, termId = 1, averageScore = term1AvgSafe!!)
                repository.insertManualTermAverage(m1)
            }
            if (term2Avg != null) {
                val m2 = com.example.data.models.StudentManualTermAverage(studentId = studentId, termId = 2, averageScore = term2AvgSafe!!)
                repository.insertManualTermAverage(m2)
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onSuccess?.invoke()
            }
        }
    }

    fun saveManualTermAveragesBulk(
        termId: Int,
        averagesMap: Map<Long, Double>,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val list = averagesMap.map { (studentId, score) ->
                com.example.data.models.StudentManualTermAverage(studentId, termId, score.coerceIn(0.0, 20.0))
            }
            list.forEach { repository.insertManualTermAverage(it) }
            val current = _manualTermAverages.value.toMutableList()
            list.forEach { newItem ->
                current.removeAll { it.studentId == newItem.studentId && it.termId == newItem.termId }
                current.add(newItem)
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _manualTermAverages.value = current
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onSuccess?.invoke()
            }
        }
    }

    @Volatile private var generalCacheClassId: Long? = null
    @Volatile private var generalCacheKey: String = ""
    private var generalAvgCache: Map<Long, Double> = emptyMap()
    private var generalRankCache: Map<Long, Int> = emptyMap()

    private fun computeStudentGeneralAverageRaw(studentId: Long, classId: Long): Double {
        val avg1 = calculateStudentTermAverage(studentId, classId, 1)
        val avg2 = calculateStudentTermAverage(studentId, classId, 2)
        val avg3 = calculateStudentTermAverage(studentId, classId, 3)
        return if (useFinalExamFormula.value) {
            (avg1 * 1.0 + avg2 * 2.0 + avg3 * 3.0) / 6.0
        } else {
            (avg1 + avg2 + avg3) / 3.0
        }
    }

    @Synchronized
    private fun ensureGeneralCache(classId: Long) {
        val allG = grades.value
        val allM = _manualTermAverages.value
        val classStudents = students.value.filter { it.classId == classId }
        val gradesFp = allG.sumOf { g ->
            (g.studentId.hashCode().toLong() * 1000003L + g.subjectId.hashCode().toLong() * 10007L + g.termId.toLong()) * (Math.round(g.score * 100.0) + 1L)
        }
        val manualFp = allM.sumOf { m ->
            (m.studentId.hashCode().toLong() * 1000003L + m.termId.toLong()) * (Math.round(m.averageScore * 100.0) + 1L)
        }
        val subjectsFp = subjects.value.sumOf { s ->
            s.id.hashCode().toLong() * 1000003L * (s.maxPoints.toLong() + 1L)
        }
        val custFp = customizations.value.sumOf { c ->
            (c.classId.hashCode().toLong() * 1000003L + c.subjectId.hashCode().toLong() * 10007L) * (c.maxPoints.toLong() + 1L + (if (c.isHidden) 500L else 0L))
        }
        val key = "${allG.size}_${gradesFp}_${allM.size}_${manualFp}_${classStudents.size}_${customizations.value.size}_${custFp}_${subjects.value.size}_${subjectsFp}_${useFinalExamFormula.value}"
        if (generalCacheClassId == classId && generalCacheKey == key) return

        val avgMap = classStudents.associate { it.id to computeStudentGeneralAverageRaw(it.id, classId) }
        val sorted = avgMap.entries.filter { it.value > 0.0 }.sortedByDescending { it.value }
        var rank = 1
        var previousAvg = -1.0
        val rankMap = sorted.mapIndexed { index, entry ->
            if (entry.value != previousAvg) {
                rank = index + 1
                previousAvg = entry.value
            }
            entry.key to rank
        }.toMap()

        generalAvgCache = avgMap
        generalRankCache = rankMap
        generalCacheClassId = classId
        generalCacheKey = key
    }

    fun calculateStudentGeneralAverage(studentId: Long, classId: Long): Double {
        ensureGeneralCache(classId)
        return generalAvgCache[studentId] ?: computeStudentGeneralAverageRaw(studentId, classId)
    }

    fun calculateStudentGeneralRank(studentId: Long, classId: Long): Int {
        ensureGeneralCache(classId)
        return generalRankCache[studentId] ?: 0
    }

    // 2. Export detailed report card of a single student
    fun exportStudentReportCardText(
        className: String,
        studentName: String,
        performances: List<StudentPerformance>,
        selectedId: Long,
        subsList: List<Subject>,
        termId: Int
    ): String {
        val perf = performances.find { it.student.id == selectedId }
        val builder = java.lang.StringBuilder()
        builder.append("📄 كشف درجات الطالب: $studentName\n")
        builder.append("🏫 القسم: $className | 📅 الفصل: ${getTermName(termId)}\n")
        builder.append("-----------------------------\n")
        if (perf == null) {
            builder.append("لا توجد بيانات درجات متوفرة لهذا الطالب في هذا الفصل.\n")
        } else {
            builder.append("الدرجات والمواد:\n")
            subsList.forEach { sub ->
                val score = perf.gradesMap[sub.id]
                val scoreStr = if (score != null) "${HtmlReportHelper.formatCleanNumber(score)}/${sub.maxPoints}" else "غير مسجل"
                builder.append("- ${sub.name}: $scoreStr\n")
            }
            builder.append("-----------------------------\n")
            if (termId == 3 || termId == 4) {
                val classId = perf.student.classId
                val avg1 = calculateStudentTermAverage(selectedId, classId, 1)
                val avg2 = calculateStudentTermAverage(selectedId, classId, 2)
                val avg3 = perf.averageScore
                val genAvg = calculateStudentGeneralAverage(selectedId, classId)
                val genRank = calculateStudentGeneralRank(selectedId, classId)
                
                builder.append("📝 معدل الامتحان الأول: ${HtmlReportHelper.formatCleanNumber(avg1)} / 20\n")
                builder.append("📝 معدل الامتحان الثاني: ${HtmlReportHelper.formatCleanNumber(avg2)} / 20\n")
                builder.append("📝 معدل الامتحان الثالث: ${HtmlReportHelper.formatCleanNumber(avg3)} / 20\n")
                builder.append("📊 المعدل العام: ${HtmlReportHelper.formatCleanNumber(genAvg)} / 20\n")
                builder.append("🏆 الرتبة العامة: $genRank / ${performances.size}\n")
            } else {
                builder.append("📊 المعدل العام: ${HtmlReportHelper.formatCleanNumber(perf.averageScore)} / 20\n")
                builder.append("🏆 الرتبة في القسم: ${perf.rank} / ${performances.size}\n")
            }
        }
        builder.append("-----------------------------\n")
        builder.append("تم التصدير بواسطة تطبيق دفتر المعلم")
        return builder.toString()
    }

    // 3. Export entire detailed class ledger/results
    fun exportClassLedgerText(
        className: String,
        performances: List<StudentPerformance>,
        subsList: List<Subject>,
        termId: Int
    ): String {
        val sortedSubsList = subsList.sortedByOfficialOrder()
        val builder = java.lang.StringBuilder()
        builder.append("📊 لائحة النتائج التفصيلية لجميع طلاب قسم: $className\n")
        builder.append("📅 الفصل الدراسي: ${getTermName(termId)}\n")
        builder.append("=========================================\n\n")

        if (performances.isEmpty()) {
            builder.append("لا توجد بيانات طلاب أو درجات متوفرة.\n")
        } else {
            // Header
            builder.append(String.format("%-20s", "اسم الطالب"))
            sortedSubsList.forEach { sub ->
                builder.append(" | ").append(sub.name).append(" (").append(sub.maxPoints).append(")")
            }
            if (termId == 3 || termId == 4) {
                builder.append(" | المعدل 1 | المعدل 2 | المعدل 3 | المعدل العام | الرتبة العامة\n")
            } else {
                builder.append(" | المعدل (20/) | الرتبة\n")
            }
            builder.append("-".repeat(50 + sortedSubsList.size * 15 + (if (termId == 3 || termId == 4) 35 else 0))).append("\n")

            // Rows (ordered by target Rank)
            val sortedList = if (termId == 3 || termId == 4) {
                performances.sortedWith(
                    compareBy<StudentPerformance> {
                        val r = calculateStudentGeneralRank(it.student.id, it.student.classId)
                        if (r == 0) Int.MAX_VALUE else r
                    }.thenBy { it.student.name }
                )
            } else {
                performances.sortedWith(
                    compareBy<StudentPerformance> { if (it.rank == 0) Int.MAX_VALUE else it.rank }
                        .thenBy { it.student.name }
                )
            }

            sortedList.forEach { perf ->
                builder.append(String.format("%-20s", perf.student.name))
                sortedSubsList.forEach { sub ->
                    val score = perf.gradesMap[sub.id]
                    val scoreStr = if (score != null) HtmlReportHelper.formatCleanNumber(score) else "-"
                    builder.append(" | ").append(scoreStr)
                }
                if (termId == 3 || termId == 4) {
                    val classId = perf.student.classId
                    val avg1 = calculateStudentTermAverage(perf.student.id, classId, 1)
                    val avg2 = calculateStudentTermAverage(perf.student.id, classId, 2)
                    val avg3 = perf.averageScore
                    val genAvg = calculateStudentGeneralAverage(perf.student.id, classId)
                    val genRank = calculateStudentGeneralRank(perf.student.id, classId)

                    builder.append(" | ").append(HtmlReportHelper.formatCleanNumber(avg1))
                    builder.append(" | ").append(HtmlReportHelper.formatCleanNumber(avg2))
                    builder.append(" | ").append(HtmlReportHelper.formatCleanNumber(avg3))
                    builder.append(" | ").append(HtmlReportHelper.formatCleanNumber(genAvg))
                    builder.append(" | ").append(genRank)
                } else {
                    builder.append(" | ").append(HtmlReportHelper.formatCleanNumber(perf.averageScore))
                    builder.append(" | ").append(perf.rank)
                }
                builder.append("\n")
            }
        }
        builder.append("\n=========================================\n")
        builder.append("تم التصدير بواسطة تطبيق دفتر المعلم")
        return builder.toString()
    }

    // --- Class Subject Customizations Actions ---
    fun buildClassSubjects(
        classId: Long,
        level: Int,
        allSubjects: List<Subject> = subjects.value,
        allCustomizations: List<ClassSubjectCustomization> = customizations.value
    ): List<Subject> {
        val classCusts = allCustomizations.filter { it.classId == classId }.associateBy { it.subjectId }
        return allSubjects
            .filter { it.level == level && (it.classId == 0L || it.classId == classId) }
            .filter { classCusts[it.id]?.isHidden != true }
            .map { rawSub ->
                val cust = classCusts[rawSub.id]
                if (cust != null) {
                    Subject(
                        id = rawSub.id,
                        name = if (cust.name.isBlank()) rawSub.name else cust.name,
                        level = rawSub.level,
                        maxPoints = cust.maxPoints,
                        classId = rawSub.classId
                    )
                } else {
                    rawSub
                }
            }
    }

    fun hideSubjectInClass(classId: Long, subjectId: Long, onDone: () -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val existing = customizations.value.find { it.classId == classId && it.subjectId == subjectId }
                val subject = subjects.value.find { it.id == subjectId }
                val cust = ClassSubjectCustomization(
                    classId = classId,
                    subjectId = subjectId,
                    name = existing?.name ?: (subject?.name ?: ""),
                    maxPoints = existing?.maxPoints ?: (subject?.maxPoints ?: 20),
                    isHidden = true
                )
                repository.insertCustomization(cust)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onDone()
            }
        }
    }

    fun unhideSubjectInClass(classId: Long, subjectId: Long, onDone: () -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val existing = customizations.value.find { it.classId == classId && it.subjectId == subjectId }
                if (existing != null) {
                    repository.insertCustomization(existing.copy(isHidden = false))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onDone()
            }
        }
    }

    fun saveSubjectCustomization(classId: Long, subjectId: Long, name: String, maxPoints: Int) {
        val existingCust = customizations.value.find { it.classId == classId && it.subjectId == subjectId }
        val cust = ClassSubjectCustomization(
            classId = classId,
            subjectId = subjectId,
            name = name.trim(),
            maxPoints = maxPoints,
            isHidden = existingCust?.isHidden ?: false
        )
        
        // Save locally first ALWAYS
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.insertCustomization(cust)
        }
    }

    fun rescaleGradesForSubjectInClass(
        classId: Long,
        subjectId: Long,
        oldMaxPoints: Int,
        newMaxPoints: Int,
        onDone: (Int) -> Unit
    ) {
        if (oldMaxPoints <= 0 || newMaxPoints <= 0 || oldMaxPoints == newMaxPoints) {
            onDone(0)
            return
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            var count = 0
            try {
                val classStudentIds = _students.value.filter { it.classId == classId }.map { it.id }.toSet()
                if (classStudentIds.isNotEmpty()) {
                    val ratio = newMaxPoints.toDouble() / oldMaxPoints.toDouble()
                    val affected = _grades.value.filter {
                        it.subjectId == subjectId && it.studentId in classStudentIds
                    }
                    affected.forEach { g ->
                        val newScore = (g.score * ratio).coerceIn(0.0, newMaxPoints.toDouble())
                        repository.insertGrade(g.copy(score = newScore))
                        count++
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onDone(count)
            }
        }
    }

    fun removeSubjectCustomization(classId: Long, subjectId: Long) {
        // Save locally first ALWAYS
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.deleteCustomization(classId, subjectId)
        }
    }

    fun deduplicateSubjects() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val allSubs = repository.allSubjects.first()
                if (allSubs.isEmpty()) return@launch
                
                val grouped = allSubs.groupBy { Pair(it.name.trim().lowercase(), it.level) }
                
                grouped.forEach { (key, list) ->
                    if (list.size > 1) {
                        val keep = list.first()
                        val duplicates = list.drop(1)
                        
                        duplicates.forEach { dup ->
                            // 1. Move grades from duplicate subject ID to keep subject ID
                            val allG = repository.allGrades.first()
                            val dupGrades = allG.filter { it.subjectId == dup.id }
                            if (dupGrades.isNotEmpty()) {
                                val updatedGrades = dupGrades.map { it.copy(subjectId = keep.id) }
                                repository.insertGrades(updatedGrades)
                                dupGrades.forEach { oldGrade ->
                                    repository.deleteGrade(oldGrade.studentId, oldGrade.subjectId, oldGrade.termId)
                                }
                            }
                            
                            // 2. Move customizations
                            val allCust = repository.allCustomizations.first()
                            val dupCustomizations = allCust.filter { it.subjectId == dup.id }
                            if (dupCustomizations.isNotEmpty()) {
                                dupCustomizations.forEach { cust ->
                                    val updatedCust = cust.copy(subjectId = keep.id)
                                    repository.insertCustomization(updatedCust)
                                    repository.deleteCustomization(cust.classId, cust.subjectId)
                                }
                            }
                            
                            // 3. Delete duplicate subject locally
                            repository.deleteSubject(dup.id)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Toggle term activation of a class for a specific teacher
    fun toggleClassTermActivation(
        teacherId: String,
        classId: Long,
        termId: Int,
        currentTermStatus: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val connectivityManager = getApplication<Application>().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            var isOnline = false
            if (connectivityManager != null) {
                val activeNetwork = connectivityManager.activeNetwork
                if (activeNetwork != null) {
                    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                    if (capabilities != null) {
                        isOnline = capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    }
                }
            }

            if (!isOnline) {
                onFailure("🌐 لا يوجد اتصال بالإنترنت حالياً! لتفعيل الفصل الدراسي بشكل فوري وصادق، يجب أن يكون جهازك متصلاً بالشبكة لمزامنة ترخيص التفعيل مباشرة مع خوادم السحابة وجهاز المعلم.")
                return
            }

            val newStatus = !currentTermStatus
            val db = FirebaseFirestore.getInstance()
            val termField = when(termId) {
                1 -> "isTerm1Activated"
                2 -> "isTerm2Activated"
                3 -> "isTerm3Activated"
                4 -> "isTerm4Activated"
                else -> "isTerm1Activated"
            }
            
            val classDocRefRead = db.collection("teachers").document(teacherId)
                .collection("classes").document(classId.toString())

            classDocRefRead.get().addOnFailureListener { e ->
                onFailure("تعذر قراءة حالة القسم من الخادم: ${e.message}")
            }.addOnSuccessListener { existingDoc ->

            val curT1 = existingDoc.getBoolean("isTerm1Activated") ?: true
            val curT2 = existingDoc.getBoolean("isTerm2Activated") ?: false
            val curT3 = existingDoc.getBoolean("isTerm3Activated") ?: false
            val curT4 = existingDoc.getBoolean("isTerm4Activated") ?: false

            val newT1 = if (termId == 1) newStatus else curT1
            val newT2 = if (termId == 2) newStatus else curT2
            val newT3 = if (termId == 3) newStatus else curT3
            val newT4 = if (termId == 4) newStatus else curT4
            val newIsAct = newT1 && newT2 && (newT3 || newT4)
            

            val previousStatsList = _allClassesForStats.value
            _allClassesForStats.value = previousStatsList.map { cls ->
                if (cls.id == classId) {
                    cls.copy(
                        isActivated = newIsAct,
                        isTerm1Activated = newT1,
                        isTerm2Activated = newT2,
                        isTerm3Activated = newT3,
                        isTerm4Activated = newT4
                    )
                } else cls
            }

            val classDocRef = db.collection("teachers").document(teacherId)
                .collection("classes").document(classId.toString())

            val classUpdateMap = mapOf(
                "id" to classId,
                "teacherId" to teacherId,
                "isTerm1Activated" to newT1,
                "isTerm2Activated" to newT2,
                "isTerm3Activated" to newT3,
                "isTerm4Activated" to newT4,
                "isActivated" to newIsAct
            )
            classDocRef.set(classUpdateMap, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener {
                    _allClassesForStats.value = previousStatsList
                    onFailure("تعذّر تأكيد التفعيل على الخادم. لم يتم التفعيل، حاول مرة أخرى.")
                }
            }
        } catch (e: Exception) {
            onFailure("❌ حدث خطأ غير متوقع: ${e.message}")
        }
    }

    fun activateAllYearTerms(
        teacherId: String,
        classId: Long,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val dbRead = FirebaseFirestore.getInstance()
            val classDocRefRead = dbRead.collection("teachers").document(teacherId)
                .collection("classes").document(classId.toString())

            classDocRefRead.get().addOnFailureListener { e ->
                onFailure("تعذر قراءة حالة القسم من الخادم: ${e.message}")
            }.addOnSuccessListener { existingDoc ->

            val curT1 = existingDoc.getBoolean("isTerm1Activated") ?: true
            val curT4 = existingDoc.getBoolean("isTerm4Activated") ?: false
            val newIsActivated = curT1 && curT4

            val previousStatsList = _allClassesForStats.value
            _allClassesForStats.value = previousStatsList.map { cls ->
                if (cls.id == classId) {
                    cls.copy(
                        isTerm2Activated = true,
                        isTerm3Activated = true,
                        isActivated = newIsActivated
                    )
                } else cls
            }

            val db = FirebaseFirestore.getInstance()
            val classUpdateMap = mapOf(
                "id" to classId,
                "teacherId" to teacherId,
                "isTerm1Activated" to curT1,
                "isTerm2Activated" to true,
                "isTerm3Activated" to true,
                "isTerm4Activated" to curT4,
                "isActivated" to newIsActivated
            )

            db.collection("teachers").document(teacherId)
                .collection("classes").document(classId.toString())
                .set(classUpdateMap, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    _allClassesForStats.value = previousStatsList
                    onFailure(e.message ?: "فشل تفعيل فصول السنة")
                }
            }
        } catch (e: Exception) {
            onFailure(e.message ?: "خطأ غير متوقع")
        }
    }

    // Toggle activation of a class for a specific teacher
    fun toggleClassActivation(
        teacherId: String,
        classId: Long,
        currentStatus: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        toggleClassTermActivation(teacherId, classId, 2, currentStatus, onSuccess, onFailure)
    }

    // Toggle global visibility of Final Term Only feature (Admin controlled)
    fun setFinalTermOnlyFeatureEnabled(
        enabled: Boolean,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ) {
        try {
            _isFinalTermOnlyFeatureEnabled.value = enabled
            prefs.edit().putBoolean("is_final_term_only_feature_enabled", enabled).apply()
            if (!enabled && _selectedTermId.value == 4) {
                _selectedTermId.value = 1
            }
            val db = FirebaseFirestore.getInstance()
            db.collection("app_config").document("global_settings")
                .set(mapOf("isFinalTermOnlyFeatureEnabled" to enabled), com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    onComplete?.invoke(true, null)
                }
                .addOnFailureListener { e ->
                    onComplete?.invoke(false, e.message)
                }
        } catch (e: Exception) {
            onComplete?.invoke(false, e.message)
        }
    }

    private val _allClassesForStats = MutableStateFlow<List<ClassSectionForStats>>(emptyList())
    val allClassesForStats: StateFlow<List<ClassSectionForStats>> = _allClassesForStats.asStateFlow()

    private val _isLoadingStats = MutableStateFlow(false)
    val isLoadingStats: StateFlow<Boolean> = _isLoadingStats.asStateFlow()

    fun loadStatsData() {
        try {
            _isLoadingStats.value = true

            // Fast safety net: ensure loading spinner disappears quickly
            viewModelScope.launch {
                kotlinx.coroutines.delay(2500)
                _isLoadingStats.value = false
            }

            val db = FirebaseFirestore.getInstance()

            // 2. Secondary one-time fetch on collectionGroup("classes")
            db.collectionGroup("classes")
                .get()
                .addOnSuccessListener { snapshot ->
                    _isLoadingStats.value = false
                    if (snapshot == null) return@addOnSuccessListener
                    try {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                val idVal = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: return@mapNotNull null
                                val pathParts = doc.reference.path.split("/")
                                val teacherId = pathParts.getOrNull(1) ?: doc.getString("teacherId") ?: ""
                                val academicYear = doc.getString("academicYear") ?: "2025-2026"
                                val wilayaVal = doc.getString("wilaya") ?: ""
                                val moughataaVal = doc.getString("moughataa") ?: ""
                                val schoolNameVal = doc.getString("schoolName") ?: ""
                                val levelVal = (doc.get("level") as? Number)?.toInt() ?: 1
                                val sectionNameVal = doc.getString("sectionName") ?: ""
                                val isTerm1CompletedVal = doc.getBoolean("isTerm1Completed") ?: doc.getBoolean("term1Completed") ?: false
                                val isTerm2CompletedVal = doc.getBoolean("isTerm2Completed") ?: doc.getBoolean("term2Completed") ?: false
                                val isTerm3CompletedVal = doc.getBoolean("isTerm3Completed") ?: doc.getBoolean("term3Completed") ?: false

                                val isActVal = doc.getBoolean("isActivated") ?: false
                                val isT1Val = doc.getBoolean("isTerm1Activated") ?: true
                                val isT2Val = doc.getBoolean("isTerm2Activated") ?: false
                                val isT3Val = doc.getBoolean("isTerm3Activated") ?: false
                                val isT4Val = doc.getBoolean("isTerm4Activated") ?: false

                                ClassSectionForStats(
                                    id = idVal,
                                    name = doc.getString("name") ?: "",
                                    academicYear = if (academicYear.isBlank()) "2025-2026" else academicYear,
                                    isActivated = isActVal,
                                    isTerm1Activated = isT1Val,
                                    isTerm2Activated = isT2Val,
                                    isTerm3Activated = isT3Val,
                                    isTerm4Activated = isT4Val,
                                    teacherId = teacherId,
                                    studentCount = 0,
                                    wilaya = wilayaVal,
                                    moughataa = moughataaVal,
                                    schoolName = schoolNameVal,
                                    level = levelVal,
                                    sectionName = sectionNameVal,
                                    isTerm1Completed = isTerm1CompletedVal,
                                    isTerm2Completed = isTerm2CompletedVal,
                                    isTerm3Completed = isTerm3CompletedVal
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        _statsLoadError.value = null
                        if (list.isNotEmpty()) {
                            val currentMap = _allClassesForStats.value.associateBy { it.id }.toMutableMap()
                            list.forEach { currentMap[it.id] = it }
                            _allClassesForStats.value = currentMap.values.toList()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                .addOnFailureListener { e ->
                    _isLoadingStats.value = false
                    _statsLoadError.value = "تعذر جلب الأقسام من الخادم: ${e.message}"
                    e.printStackTrace()
                }
        } catch (e: Exception) {
            _isLoadingStats.value = false
            e.printStackTrace()
        }
    }

    // --- TEACHER EXCHANGE / TRANSFER FIREBASE & OFFLINE METHODS (Cloud-Sync Mode) ---
    private var exchangePostsLimit = 4L
    private var exchangePostsListener: ListenerRegistration? = null

    private val _hasMoreExchangePosts = MutableStateFlow(true)
    val hasMoreExchangePosts: StateFlow<Boolean> = _hasMoreExchangePosts.asStateFlow()

    /**
     * Cloud-Sync Mode for Teacher Exchanges (المزامنة السحابية المباشرة لصفحات التبادل).
     * Explicitly queries the cloud server (Source.SERVER) to fetch the latest exchange posts
     * from all teachers across wilayas and moughataas in real-time when connected to internet.
     */
    fun fetchExchangePostsFromCloud(resetLimit: Boolean = false, force: Boolean = false) {
        if (resetLimit) {
            exchangePostsLimit = 4L
            _hasMoreExchangePosts.value = true
        }

        val nowMs = System.currentTimeMillis()
        val lastFetchMs = prefs.getLong("last_exchange_fetch_time", 0L)
        val withinDailyWindow = (nowMs - lastFetchMs) < 86400000L
        val skipServer = withinDailyWindow && !force
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val localPosts = activeDatabase.teacherDao().getAllExchangePosts()
                val sorted = localPosts.sortedByDescending { it.timestamp }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _exchangePosts.value = sorted
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        if (skipServer) return
        if (!isNetworkAvailable()) return
        prefs.edit().putLong("last_exchange_fetch_time", nowMs).apply()
        try {
            FirebaseFirestore.getInstance().collection("teacher_exchanges")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(exchangePostsLimit)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener { snapshot ->
                    if (snapshot == null) return@addOnSuccessListener
                    _hasMoreExchangePosts.value = snapshot.documents.size >= exchangePostsLimit
                    val remotePosts = snapshot.documents.mapNotNull { doc ->
                        try {
                            TeacherExchangePost(
                                id = doc.id,
                                userId = doc.getString("userId") ?: "",
                                authorName = doc.getString("authorName") ?: "معلم",
                                authorEmail = doc.getString("authorEmail") ?: doc.getString("userEmail") ?: "",
                                offerType = doc.getString("offerType") ?: "تبادل مقاعد ودي دون دفع",
                                priceAmount = doc.getString("priceAmount") ?: "",
                                isNegotiable = doc.getBoolean("isNegotiable") ?: false,
                                specialty = doc.getString("specialty") ?: "معلم عربية",
                                currentWilaya = doc.getString("currentWilaya") ?: "",
                                currentMoughataa = doc.getString("currentMoughataa") ?: "",
                                currentCommune = doc.getString("currentCommune") ?: "",
                                currentWorkPlace = doc.getString("currentWorkPlace") ?: "",
                                schoolName = doc.getString("schoolName") ?: "",
                                whatsappPhone = doc.getString("whatsappPhone") ?: "",
                                targetWilayas = doc.getString("targetWilayas") ?: "",
                                targetMoughataas = doc.getString("targetMoughataas") ?: "",
                                isMoughataaTeacher = doc.getBoolean("isMoughataaTeacher") ?: false,
                                isSchoolPrincipal = doc.getBoolean("isSchoolPrincipal") ?: false,
                                isApproved = doc.getBoolean("isApproved") ?: false,
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                isSynced = true
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            try {
                                val myUid = _currentUser.value?.uid ?: ""
                                if (myUid.isNotBlank()) {
                                    val previousMine = _exchangePosts.value.filter { it.userId == myUid }
                                    remotePosts.filter { it.userId == myUid && it.isApproved }.forEach { newPost ->
                                        val old = previousMine.find { it.id == newPost.id }
                                        if (old != null && !old.isApproved) {
                                            repository.insertLocalNotification(
                                                com.example.data.models.LocalNotification(
                                                    title = "تم نشر إعلانك ✅",
                                                    message = "تمت الموافقة على إعلان التبادل الخاص بك وأصبح منشوراً للمعلمين.",
                                                    createdAt = System.currentTimeMillis(),
                                                    isRead = false,
                                                    classId = 0L
                                                )
                                            )
                                        }
                                    }
                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }

                            activeDatabase.teacherDao().deleteAllSyncedExchangePosts()
                            activeDatabase.teacherDao().insertExchangePosts(remotePosts)
                            val unsyncedLocal = activeDatabase.teacherDao().getUnsyncedExchangePosts()
                            val merged = (remotePosts + unsyncedLocal).distinctBy { it.id }.sortedByDescending { it.timestamp }
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                _exchangePosts.value = merged
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            val localPosts = activeDatabase.teacherDao().getAllExchangePosts()
                            if (localPosts.isNotEmpty()) {
                                val sorted = localPosts.sortedByDescending { it.timestamp }
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    _exchangePosts.value = sorted
                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun listenToExchangePosts() {
        try {
            exchangePostsListener?.remove()

            // First load cached local posts if currently empty for instant display
            if (_exchangePosts.value.isEmpty()) {
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val localPosts = activeDatabase.teacherDao().getAllExchangePosts()
                        if (localPosts.isNotEmpty()) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                _exchangePosts.value = localPosts
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadMoreExchangePosts() {
        exchangePostsLimit += 4
        exchangePostsListener?.remove()
        listenToExchangePosts()
        fetchExchangePostsFromCloud(force = true)
    }

    fun syncPendingExchangePosts() {
        if (!isNetworkAvailable()) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val unsyncedPosts = activeDatabase.teacherDao().getUnsyncedExchangePosts()
                if (unsyncedPosts.isEmpty()) return@launch

                for (post in unsyncedPosts) {
                    val isNewDoc = post.id.startsWith("offline_")
                    val docRef = if (isNewDoc) {
                        FirebaseFirestore.getInstance().collection("teacher_exchanges").document()
                    } else {
                        FirebaseFirestore.getInstance().collection("teacher_exchanges").document(post.id)
                    }
                    val finalSyncedPost = post.copy(id = docRef.id, isSynced = true)
                    val postData = mapOf(
                        "userId" to finalSyncedPost.userId,
                        "authorName" to finalSyncedPost.authorName,
                        "offerType" to finalSyncedPost.offerType,
                        "priceAmount" to finalSyncedPost.priceAmount,
                        "isNegotiable" to finalSyncedPost.isNegotiable,
                        "specialty" to finalSyncedPost.specialty,
                        "currentWilaya" to finalSyncedPost.currentWilaya,
                        "currentMoughataa" to finalSyncedPost.currentMoughataa,
                        "currentCommune" to finalSyncedPost.currentCommune,
                        "currentWorkPlace" to finalSyncedPost.currentWorkPlace,
                        "schoolName" to finalSyncedPost.schoolName,
                        "whatsappPhone" to finalSyncedPost.whatsappPhone,
                        "targetWilayas" to finalSyncedPost.targetWilayas,
                        "targetMoughataas" to finalSyncedPost.targetMoughataas,
                        "isMoughataaTeacher" to finalSyncedPost.isMoughataaTeacher,
                        "isSchoolPrincipal" to finalSyncedPost.isSchoolPrincipal,
                        "timestamp" to finalSyncedPost.timestamp
                    )

                    docRef.set(postData)
                        .addOnSuccessListener {
                            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                if (isNewDoc) {
                                    activeDatabase.teacherDao().deleteExchangePost(post.id)
                                }
                                activeDatabase.teacherDao().insertExchangePost(finalSyncedPost)

                                val currentList = _exchangePosts.value.filterNot { it.id == post.id }
                                val updatedList = (listOf(finalSyncedPost) + currentList).distinctBy { it.id }.sortedByDescending { it.timestamp }
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    _exchangePosts.value = updatedList
                                    android.widget.Toast.makeText(
                                        getApplication(),
                                        "🌐 تم نشر إعلان التبادل للعامة بنجاح بعد توفر الاتصال بالإنترنت!",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createExchangePost(
        post: TeacherExchangePost,
        onSuccess: (isOffline: Boolean) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = _currentUser.value
        val isOnline = isNetworkAvailable()

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val docRef = if (post.id.isNotBlank() && !post.id.startsWith("offline_")) {
                    db.collection("teacher_exchanges").document(post.id)
                } else {
                    db.collection("teacher_exchanges").document()
                }

                val finalPost = post.copy(
                    id = docRef.id,
                    userId = user?.uid ?: "",
                    authorName = user?.displayName?.ifBlank { user.email ?: "معلم" } ?: "معلم",
                    authorEmail = user?.email ?: "",
                    isApproved = false,
                    timestamp = System.currentTimeMillis(),
                    isSynced = isOnline
                )

                // 1. Instantly store in Room database
                activeDatabase.teacherDao().insertExchangePost(finalPost)

                // 2. Instantly update in-memory StateFlow so list reflects changes immediately
                val updatedList = (listOf(finalPost) + _exchangePosts.value).distinctBy { it.id }.sortedByDescending { it.timestamp }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _exchangePosts.value = updatedList
                }

                var cloudSuccess = false
                if (isOnline) {
                    try {
                        val postData = mapOf(
                            "userId" to finalPost.userId,
                            "authorName" to finalPost.authorName,
                            "authorEmail" to finalPost.authorEmail,
                            "offerType" to finalPost.offerType,
                            "priceAmount" to finalPost.priceAmount,
                            "isNegotiable" to finalPost.isNegotiable,
                            "specialty" to finalPost.specialty,
                            "currentWilaya" to finalPost.currentWilaya,
                            "currentMoughataa" to finalPost.currentMoughataa,
                            "currentCommune" to finalPost.currentCommune,
                            "currentWorkPlace" to finalPost.currentWorkPlace,
                            "schoolName" to finalPost.schoolName,
                            "whatsappPhone" to finalPost.whatsappPhone,
                            "targetWilayas" to finalPost.targetWilayas,
                            "targetMoughataas" to finalPost.targetMoughataas,
                            "isMoughataaTeacher" to finalPost.isMoughataaTeacher,
                            "isSchoolPrincipal" to finalPost.isSchoolPrincipal,
                            "isApproved" to finalPost.isApproved,
                            "timestamp" to finalPost.timestamp
                        )

                        // Wait up to 5 seconds for Firestore set task
                        kotlinx.coroutines.withTimeoutOrNull(5000L) {
                            Tasks.await(docRef.set(postData), 5, java.util.concurrent.TimeUnit.SECONDS)
                        }
                        cloudSuccess = true
                        val syncedPost = finalPost.copy(isSynced = true)
                        activeDatabase.teacherDao().insertExchangePost(syncedPost)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val unsyncedPost = finalPost.copy(isSynced = false)
                        activeDatabase.teacherDao().insertExchangePost(unsyncedPost)
                    }
                }

                // 3. Return success callback instantly to close dialog / reset loading spinner
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onSuccess(!isOnline || !cloudSuccess)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onFailure(e.localizedMessage ?: "تعذر حفظ إعلان التبادل")
                }
            }
        }
    }

    fun updateExchangePost(
        post: TeacherExchangePost,
        onSuccess: (isOffline: Boolean) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (post.id.isBlank()) {
            onFailure("معرف الإعلان غير صالح")
            return
        }

        val user = _currentUser.value
        val isOnline = isNetworkAvailable()

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection("teacher_exchanges").document(post.id)
                val updatedPost = post.copy(
                    userId = if (post.userId.isNotBlank()) post.userId else (user?.uid ?: ""),
                    authorName = if (post.authorName.isNotBlank()) post.authorName else (user?.displayName?.ifBlank { user.email ?: "معلم" } ?: "معلم"),
                    authorEmail = if (post.authorEmail.isNotBlank()) post.authorEmail else (user?.email ?: ""),
                    timestamp = System.currentTimeMillis(),
                    isSynced = isOnline
                )

                // 1. Instantly store in Room database
                activeDatabase.teacherDao().insertExchangePost(updatedPost)

                // 2. Instantly update in-memory StateFlow
                val updatedList = _exchangePosts.value.map { if (it.id == post.id) updatedPost else it }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _exchangePosts.value = updatedList
                }

                var cloudSuccess = false
                if (isOnline) {
                    try {
                        val postData = mapOf(
                            "userId" to updatedPost.userId,
                            "authorName" to updatedPost.authorName,
                            "authorEmail" to updatedPost.authorEmail,
                            "offerType" to updatedPost.offerType,
                            "priceAmount" to updatedPost.priceAmount,
                            "isNegotiable" to updatedPost.isNegotiable,
                            "specialty" to updatedPost.specialty,
                            "currentWilaya" to updatedPost.currentWilaya,
                            "currentMoughataa" to updatedPost.currentMoughataa,
                            "currentCommune" to updatedPost.currentCommune,
                            "currentWorkPlace" to updatedPost.currentWorkPlace,
                            "schoolName" to updatedPost.schoolName,
                            "whatsappPhone" to updatedPost.whatsappPhone,
                            "targetWilayas" to updatedPost.targetWilayas,
                            "targetMoughataas" to updatedPost.targetMoughataas,
                            "isMoughataaTeacher" to updatedPost.isMoughataaTeacher,
                            "isSchoolPrincipal" to updatedPost.isSchoolPrincipal,
                            "isApproved" to updatedPost.isApproved,
                            "timestamp" to updatedPost.timestamp
                        )

                        kotlinx.coroutines.withTimeoutOrNull(5000L) {
                            Tasks.await(docRef.set(postData), 5, java.util.concurrent.TimeUnit.SECONDS)
                        }
                        cloudSuccess = true
                        val syncedPost = updatedPost.copy(isSynced = true)
                        activeDatabase.teacherDao().insertExchangePost(syncedPost)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val unsyncedPost = updatedPost.copy(isSynced = false)
                        activeDatabase.teacherDao().insertExchangePost(unsyncedPost)
                    }
                }

                // 3. Return success callback instantly
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onSuccess(!isOnline || !cloudSuccess)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onFailure(e.localizedMessage ?: "تعذر تحديث الإعلان")
                }
            }
        }
    }

    fun deleteExchangePost(
        postId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (postId.isBlank()) {
            onFailure("معرف الإعلان غير صالح")
            return
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                activeDatabase.teacherDao().deleteExchangePost(postId)
                val updatedList = _exchangePosts.value.filterNot { it.id == postId }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _exchangePosts.value = updatedList
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!postId.startsWith("offline_") && isNetworkAvailable()) {
            try {
                FirebaseFirestore.getInstance().collection("teacher_exchanges").document(postId)
                    .delete()
                    .addOnSuccessListener { }
                    .addOnFailureListener { }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun approveExchangePost(
        postId: String,
        isApproved: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (postId.isBlank()) {
            onFailure("معرف الإعلان غير صالح")
            return
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (isNetworkAvailable()) {
                    FirebaseFirestore.getInstance().collection("teacher_exchanges")
                        .document(postId)
                        .update("isApproved", isApproved)
                }

                val currentPosts = activeDatabase.teacherDao().getAllExchangePosts()
                val existing = currentPosts.find { it.id == postId }
                if (existing != null) {
                    val updated = existing.copy(isApproved = isApproved)
                    activeDatabase.teacherDao().insertExchangePost(updated)
                }

                val updatedList = _exchangePosts.value.map {
                    if (it.id == postId) it.copy(isApproved = isApproved) else it
                }

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _exchangePosts.value = updatedList
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onFailure(e.localizedMessage ?: "تعذر تغيير حالة الإعلان")
                }
            }
        }
    }

    fun resolveActivationTarget(
        classId: Long?,
        email: String?,
        onResult: (teacherUid: String?, className: String?) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        if (classId != null) {
            db.collectionGroup("classes")
                .whereEqualTo("id", classId)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    val doc = snapshot?.documents?.firstOrNull()
                    if (doc != null) {
                        val parentTeacherUid = doc.reference.parent.parent?.id
                            ?: doc.reference.path.split("/").getOrNull(1)
                        val className = doc.getString("name")
                        onResult(parentTeacherUid, className)
                    } else {
                        onResult(null, null)
                    }
                }
                .addOnFailureListener {
                    onResult(null, null)
                }
        } else if (!email.isNullOrBlank()) {
            db.collection("teachers")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    val doc = snapshot?.documents?.firstOrNull()
                    if (doc != null) {
                        val uid = doc.getString("uid") ?: doc.id
                        onResult(uid, null)
                    } else {
                        onResult(null, null)
                    }
                }
                .addOnFailureListener {
                    onResult(null, null)
                }
        } else {
            onResult(null, null)
        }
    }

    private val _statsTeachersCount = MutableStateFlow(0)
    val statsTeachersCount: StateFlow<Int> = _statsTeachersCount.asStateFlow()

    private val _statsClassesCount = MutableStateFlow(0)
    val statsClassesCount: StateFlow<Int> = _statsClassesCount.asStateFlow()

    private val _statsExchangesApprovedCount = MutableStateFlow(0)
    val statsExchangesApprovedCount: StateFlow<Int> = _statsExchangesApprovedCount.asStateFlow()

    private val _statsExchangesPendingCount = MutableStateFlow(0)
    val statsExchangesPendingCount: StateFlow<Int> = _statsExchangesPendingCount.asStateFlow()

    private val _statsCountsLoading = MutableStateFlow(false)
    val statsCountsLoading: StateFlow<Boolean> = _statsCountsLoading.asStateFlow()

    private val _statsLoadError = MutableStateFlow<String?>(null)
    val statsLoadError: StateFlow<String?> = _statsLoadError.asStateFlow()

    private val _courseActivationStats = MutableStateFlow<List<CourseActivationStat>>(emptyList())
    val courseActivationStats: StateFlow<List<CourseActivationStat>> = _courseActivationStats.asStateFlow()

    private val _courseActivationStatsError = MutableStateFlow(false)
    val courseActivationStatsError: StateFlow<Boolean> = _courseActivationStatsError.asStateFlow()

    private fun loadCourseActivationStats() {
        try {
            FirebaseFirestore.getInstance().collection("course_activation_requests")
                .whereEqualTo("status", "completed")
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener { snap ->
                    _courseActivationStats.value = snap.documents.map { d ->
                        CourseActivationStat(
                            specId = d.getString("specId") ?: "",
                            expiresAt = d.getLong("expiresAt") ?: 0L
                        )
                    }
                    _courseActivationStatsError.value = false
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    _courseActivationStatsError.value = true
                }
        } catch (e: Exception) {
            e.printStackTrace()
            _courseActivationStatsError.value = true
        }
    }

    fun loadStatsCounts() {
        _statsCountsLoading.value = true
        loadCourseActivationStats()
        val firestore = FirebaseFirestore.getInstance()

        val teachersTask = firestore.collection("teachers")
            .count()
            .get(AggregateSource.SERVER)

        val classesTask = FirebaseFirestore.getInstance().collectionGroup("classes")
            .count()
            .get(AggregateSource.SERVER)

        val approvedExchangesTask = firestore.collection("teacher_exchanges")
            .whereEqualTo("isApproved", true)
            .count()
            .get(AggregateSource.SERVER)

        val pendingExchangesTask = firestore.collection("teacher_exchanges")
            .whereEqualTo("isApproved", false)
            .count()
            .get(AggregateSource.SERVER)

        Tasks.whenAllComplete(teachersTask, classesTask, approvedExchangesTask, pendingExchangesTask)
            .addOnCompleteListener {
                try {
                    if (teachersTask.isSuccessful) {
                        _statsTeachersCount.value = teachersTask.result?.count?.toInt() ?: _statsTeachersCount.value
                    }
                    if (classesTask.isSuccessful) {
                        _statsClassesCount.value = classesTask.result?.count?.toInt() ?: _statsClassesCount.value
                    }
                    if (approvedExchangesTask.isSuccessful) {
                        _statsExchangesApprovedCount.value = approvedExchangesTask.result?.count?.toInt() ?: _statsExchangesApprovedCount.value
                    }
                    if (pendingExchangesTask.isSuccessful) {
                        _statsExchangesPendingCount.value = pendingExchangesTask.result?.count?.toInt() ?: _statsExchangesPendingCount.value
                    }
                } finally {
                    _statsCountsLoading.value = false
                }
            }
    }

    // ===== استرجاع «آخر مكان»: القسم المفتوح والفصل النشط =====
    init {
        viewModelScope.launch {
            val ctx = getApplication<Application>().applicationContext
            try {
                val savedClassId = com.example.data.state.LastPlaceStore.getLong(ctx, "class_id")
                val savedTermId = com.example.data.state.LastPlaceStore.getInt(ctx, "term_id")
                if (savedClassId != null) {
                    val loaded = _classSections.first { it.isNotEmpty() }
                    val cls = loaded.find { it.id == savedClassId }
                    if (cls != null && _selectedClassId.value == null) {
                        if (savedTermId != null && savedTermId in 1..4 && cls.isTermUnlocked(savedTermId) &&
                            (savedTermId != 4 || _isFinalTermOnlyFeatureEnabled.value)) {
                            _selectedTermId.value = savedTermId
                        }
                        _selectedClassId.value = savedClassId
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            launch {
                _selectedClassId.collect { id ->
                    com.example.data.state.LastPlaceStore.putLong(ctx, "class_id", id)
                }
            }
            launch {
                _selectedTermId.collect { term ->
                    com.example.data.state.LastPlaceStore.putInt(ctx, "term_id", term)
                }
            }
        }
    }
}

// Data class for teachers list representation in Administrator board
data class TeacherInfo(
    val uid: String,
    val displayName: String = "",
    val email: String = "",
    val syncedAt: Long = 0L
)

data class CourseActivationStat(
    val specId: String,
    val expiresAt: Long
)

data class ClassSectionForStats(
    val id: Long,
    val name: String,
    val academicYear: String,
    val isActivated: Boolean,
    val isTerm1Activated: Boolean = true,
    val isTerm2Activated: Boolean = false,
    val isTerm3Activated: Boolean = false,
    val isTerm4Activated: Boolean = false,
    val teacherId: String,
    val studentCount: Int = 0,
    val wilaya: String = "",
    val moughataa: String = "",
    val schoolName: String = "",
    val level: Int = 1,
    val sectionName: String = "",
    val isTerm1Completed: Boolean = false,
    val isTerm2Completed: Boolean = false,
    val isTerm3Completed: Boolean = false
) {
    fun isTermUnlocked(termId: Int): Boolean {
        if (termId == 1) {
            return true
        }
        if (termId == 2) {
            return isTerm2Activated
        }
        if (termId == 3) {
            return isTerm3Activated
        }
        if (termId == 4) {
            return isTerm4Activated
        }
        return true
    }

    fun getPaidTerms(): List<Int> {
        val paid = mutableListOf<Int>()
        if (isTermUnlocked(1)) paid.add(1)
        if (isTermUnlocked(2)) paid.add(2)
        if (isTermUnlocked(3)) paid.add(3)
        if (isTermUnlocked(4)) paid.add(4)
        return paid
    }
}

