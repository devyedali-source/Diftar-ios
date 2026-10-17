package com.example.data.models

import com.example.compat.*
import kotlinx.coroutines.IO

import androidx.room.Entity
import androidx.room.Index

/**
 * تقييم الكفايات (دفتر متابعة القسم).
 * - صف تلميذ: studentId > 0، value = 1 (+ امتلك) أو -1 (− لم يمتلك)، key = "المادة|المجال|رقم الكفاية".
 * - صف دعم: studentId = 0، note = أرقام أفراد المجموعة / ملاحظة الدعم المدرسي.
 */
@Entity(
    tableName = "competency_entries",
    primaryKeys = ["classId", "studentId", "entryKey"],
    indices = [Index(value = ["classId"]), Index(value = ["studentId"])]
)
@kotlinx.serialization.Serializable
data class CompetencyEntry(
    val classId: Long,
    val studentId: Long,
    val entryKey: String,
    val value: Int = 0,
    val note: String = ""
)
