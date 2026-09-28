package com.example.data.models

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "grades",
    primaryKeys = ["studentId", "subjectId", "termId"],
    indices = [Index(value = ["studentId"]), Index(value = ["subjectId"])]
)
data class Grade(
    val studentId: Long,
    val subjectId: Long,
    val termId: Int, // 1 for الفصل الأول, 2 for الفصل الثاني, 3 for الفصل الثالث
    val score: Double
)

