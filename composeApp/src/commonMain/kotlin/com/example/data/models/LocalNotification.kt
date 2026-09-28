package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@kotlinx.serialization.Serializable
@Entity(tableName = "local_notifications")
data class LocalNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val createdAt: Long,
    val isRead: Boolean = false,
    val classId: Long = 0
)
