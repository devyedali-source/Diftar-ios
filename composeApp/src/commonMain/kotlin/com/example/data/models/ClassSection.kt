package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_sections")
data class ClassSection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val wilaya: String = "",
    val moughataa: String = "",
    val schoolName: String = "",
    val level: Int = 1,
    val sectionName: String = "",
    val academicYear: String = "",
    val isActivated: Boolean = false,
    val isTerm1Activated: Boolean = true,
    val isTerm2Activated: Boolean = false,
    val isTerm3Activated: Boolean = false,
    val isTerm4Activated: Boolean = false,
    val isSyncedToServer: Boolean = false
) {
    fun isActivationExpired(): Boolean {
        return try {
            val endYear = academicYear.split("-").getOrNull(1)?.trim()?.toIntOrNull() ?: return false
            val now = com.example.platform.currentYearMonth()
            val currentYear = now.first
            val currentMonth = now.second
            currentYear > endYear || (currentYear == endYear && currentMonth >= 8)
        } catch (e: Exception) {
            false
        }
    }

    fun isTermUnlocked(termId: Int): Boolean {
        if (termId == 1) {
            return true
        }
        val expired = isActivationExpired()
        if (termId == 2) {
            return isTerm2Activated && !expired
        }
        if (termId == 3) {
            return isTerm3Activated && !expired
        }
        if (termId == 4) {
            return isTerm4Activated && !expired
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

    fun getFormattedName(): String {
        var result = name
        for (i in 1..6) {
            result = result
                .replace(" - د$i", " - س$i")
                .replace("-د$i", "-س$i")
                .replace(" د$i", " س$i")
        }
        return result
    }
}
