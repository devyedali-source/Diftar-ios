package com.example.data.models

import com.example.compat.*
import kotlinx.coroutines.IO

import androidx.room.Entity
import androidx.room.Index

/** سجل غياب تلميذ في يوم معين (حصة صباحية / حصة مسائية). التاريخ بصيغة yyyy-MM-dd */
@Entity(
    tableName = "attendance_records",
    primaryKeys = ["studentId", "date"],
    indices = [Index(value = ["classId", "date"])]
)
data class AttendanceRecord(
    val studentId: Long,
    val date: String,
    val classId: Long,
    val morningAbsent: Boolean = false,
    val afternoonAbsent: Boolean = false
)
