package com.example.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    indices = [Index(value = ["classId"])]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val classId: Long,
    val frenchName: String = "",
    val schoolId: String = "",
    val nationalId: String = "",
    val healthNotes: String = "",
    val parentPhone1: String = "",
    val parentPhone2: String = "",
    val gender: String = "ذكر"
)
