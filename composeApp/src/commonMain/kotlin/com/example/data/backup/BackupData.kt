package com.example.data.backup

import com.example.compat.*
import kotlinx.coroutines.IO

import com.example.data.models.ClassSection
import com.example.data.models.ClassSubjectCustomization
import com.example.data.models.Grade
import com.example.data.models.Student
import com.example.data.models.StudentManualTermAverage
import com.example.data.models.Subject
import com.example.data.models.LocalNotification
import com.squareup.moshi.JsonClass

@kotlinx.serialization.Serializable
data class BackupDeviceInfo(
    val deviceId: String = "",
    val deviceName: String = "",
    val lastSyncAt: Long = 0L
)

@kotlinx.serialization.Serializable
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String? = null,
    val userEmail: String? = null,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val devices: List<BackupDeviceInfo> = emptyList(),
    val classSections: List<ClassSection> = emptyList(),
    val students: List<Student> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val grades: List<Grade> = emptyList(),
    val customizations: List<ClassSubjectCustomization> = emptyList(),
    val manualTermAverages: List<StudentManualTermAverage> = emptyList(),
    val teacherSettings: Map<String, String> = emptyMap(),
    val localNotifications: List<LocalNotification> = emptyList()
)
