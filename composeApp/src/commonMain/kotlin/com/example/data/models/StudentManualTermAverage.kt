package com.example.data.models

import androidx.room.Entity
import androidx.room.Index

@kotlinx.serialization.Serializable
@Entity(
    tableName = "student_manual_term_averages",
    primaryKeys = ["studentId", "termId"],
    indices = [Index(value = ["studentId"])]
)
data class StudentManualTermAverage(
    val studentId: Long,
    val termId: Int, // 1 for الفصل الأول, 2 for الفصل الثاني
    val averageScore: Double // Direct average score out of 20
)
