package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@kotlinx.serialization.Serializable
@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val level: Int = 1,
    val maxPoints: Int = 20,
    val classId: Long = 0L
)

fun getOfficialSubjectRank(name: String): Int {
    val clean = name.trim().lowercase()
    return when {
        clean.contains("إسلامية") || clean.contains("اسلامية") || clean.contains("islamique") -> 1
        clean.contains("عربية") || clean.contains("arabe") -> 2
        clean.contains("رياضيات") || clean.contains("math") -> 3
        clean.contains("مدنية") || clean.contains("civique") -> 4
        clean.contains("فنية") || clean.contains("technique") || clean.contains("art") -> 5
        clean.contains("فرنسية") || clean.contains("français") || clean.contains("francais") -> 6
        clean.contains("تاريخ") || clean.contains("جغرافيا") || clean.contains("histoire") || clean.contains("géographie") || clean.contains("geographie") -> 7
        clean.contains("علوم") || clean.contains("science") -> 8
        clean.contains("بدنية") || clean.contains("رياضة") || clean.contains("sport") -> 9
        else -> 100
    }
}

fun List<Subject>.sortedByOfficialOrder(): List<Subject> {
    return this.sortedWith(
        compareBy<Subject> { getOfficialSubjectRank(it.name) }
            .thenBy { it.id }
    )
}


