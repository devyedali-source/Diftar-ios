package com.example

import com.example.data.content.AppContent
import com.example.data.db.TeacherDatabase
import com.example.data.models.ClassSection
import com.example.data.models.Grade
import com.example.data.models.Student
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Step2Test {
    // الحجم بعد فكّ الضغط لكل ملف، محسوب من ملفات أندرويد الأصلية
    private val expected = mapOf(
        "concours/concours_guide_runtime.json.gz" to 160671,
        "course/course_content_runtime.json.gz" to 6311720,
        "exams/exams_runtime.json.gz" to 516675,
        "planning/plan_annuel_1AP_canonical.json.gz" to 160940,
        "planning/plan_annuel_2AP_canonical.json.gz" to 209262,
        "planning/plan_annuel_3AP_canonical.json.gz" to 208095,
        "planning/plan_annuel_4AP_canonical.json.gz" to 224218,
        "planning/plan_annuel_5AP_canonical.json.gz" to 238226,
        "planning/plan_annuel_6AP_canonical.json.gz" to 359519,
        "preparations/1AP_PREPARATIONS_RUNTIME_LIGHT.json.gz" to 2909252,
        "preparations/2AP_PREPARATIONS_RUNTIME_LIGHT.json.gz" to 2642421,
        "preparations/3AP_PREPARATIONS_RUNTIME_LIGHT.json.gz" to 3549956,
        "preparations/4AP_PREPARATIONS_RUNTIME_LIGHT.json.gz" to 3825473,
        "preparations/5AP_PREPARATIONS_RUNTIME_LIGHT.json.gz" to 3643171,
        "preparations/6AP_PREPARATIONS_RUNTIME_LIGHT.json.gz" to 4178849,
        "school_legislation_verified_v2.json.gz" to 204460,
    )

    @Test
    fun contentFilesAreIdenticalToAndroid() = runTest {
        for ((path, size) in expected) {
            val text = AppContent.readText(path)
            val bytes = text.encodeToByteArray()
            assertEquals(size, bytes.size, "size mismatch: $path")
            val root = Json.parseToJsonElement(text)
            assertTrue(root is JsonObject, "not a JSON object: $path")
            println("OK $path ${bytes.size}")
        }
    }

    @Test
    fun databaseWorks() = runTest {
        val db = Room.inMemoryDatabaseBuilder<TeacherDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
        val dao = db.teacherDao()
        val classId = dao.insertClassSection(ClassSection(name = "قسم تجريبي", level = 5, academicYear = "2026-2027"))
        val studentId = dao.insertStudent(Student(name = "طالب", classId = classId))
        dao.insertGrade(Grade(studentId = studentId, subjectId = 1, termId = 1, score = 15.5))
        assertEquals(1, dao.getAllClassSections().first().size)
        assertEquals(15.5, dao.getGradesForClass(classId).first().single().score)
        assertTrue(dao.getClassSectionById(classId)!!.isTermUnlocked(1))
        db.close()
        println("OK database")
    }
}
