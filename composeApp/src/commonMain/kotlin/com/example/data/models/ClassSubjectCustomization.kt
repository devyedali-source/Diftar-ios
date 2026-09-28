package com.example.data.models

import androidx.room.Entity

@Entity(
    tableName = "class_subject_customizations",
    primaryKeys = ["classId", "subjectId"]
)
data class ClassSubjectCustomization(
    val classId: Long,
    val subjectId: Long,
    val name: String,
    val maxPoints: Int,
    val isHidden: Boolean = false
)
