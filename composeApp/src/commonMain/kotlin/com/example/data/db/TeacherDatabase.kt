package com.example.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.example.data.dao.TeacherDao
import com.example.data.models.ClassSection
import com.example.data.models.ClassSubjectCustomization
import com.example.data.models.Grade
import com.example.data.models.LocalNotification
import com.example.data.models.Student
import com.example.data.models.StudentManualTermAverage
import com.example.data.models.Subject
import com.example.data.models.TeacherExchangePost

// نفس جداول نسخة أندرويد ونفس رقم الإصدار (18)
@Database(
    entities = [
        ClassSection::class,
        Student::class,
        Subject::class,
        Grade::class,
        ClassSubjectCustomization::class,
        TeacherExchangePost::class,
        StudentManualTermAverage::class,
        LocalNotification::class
    ],
    version = 18,
    exportSchema = false
)
@ConstructedBy(TeacherDatabaseConstructor::class)
abstract class TeacherDatabase : RoomDatabase() {
    abstract fun teacherDao(): TeacherDao

    /** بديل clearAllTables (غير متوفر في Room على الآيفون) */
    fun clearAllTables() {
        val dao = teacherDao()
        kotlinx.coroutines.runBlocking {
            dao.deleteAllGrades()
            dao.deleteAllManualTermAverages()
            dao.deleteAllStudents()
            dao.deleteAllCustomizations()
            dao.deleteAllSubjects()
            dao.deleteAllClassSections()
            dao.deleteAllLocalNotifications()
            dao.deleteAllSyncedExchangePosts()
        }
    }

    companion object {
        /** نفس توقيع أندرويد: قاعدة بيانات مستقلة لكل مستخدم */
        fun getDatabase(context: android.content.Context, userId: String? = null): TeacherDatabase = openTeacherDatabase(userId)
    }
}

expect fun openTeacherDatabase(userId: String?): TeacherDatabase

@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object TeacherDatabaseConstructor : RoomDatabaseConstructor<TeacherDatabase> {
    override fun initialize(): TeacherDatabase
}

/** اسم ملف القاعدة لكل مستخدم — نفس القاعدة المتبعة في أندرويد */
fun teacherDatabaseName(userId: String?): String =
    if (!userId.isNullOrEmpty()) {
        "teacher_database_${userId.lowercase().replace(Regex("[^a-z0-9_]"), "_")}"
    } else {
        "teacher_database_guest"
    }

