package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.models.ClassSection
import com.example.data.models.Grade
import com.example.data.models.Student
import com.example.data.models.Subject
import com.example.data.models.ClassSubjectCustomization
import com.example.data.models.TeacherExchangePost
import com.example.data.models.StudentManualTermAverage
import com.example.data.models.LocalNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {

    // --- Class Sections ---
    @Query("SELECT * FROM class_sections ORDER BY name ASC")
    fun getAllClassSections(): Flow<List<ClassSection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassSection(section: ClassSection): Long

    @Query("SELECT * FROM class_sections WHERE id = :sectionId LIMIT 1")
    suspend fun getClassSectionById(sectionId: Long): ClassSection?

    @Query("DELETE FROM class_sections WHERE id = :sectionId")
    suspend fun deleteClassSection(sectionId: Long)

    // --- Students ---
    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY id ASC")
    fun getStudentsByClass(classId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY id ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudent(studentId: Long)

    @Query("DELETE FROM students WHERE classId = :classId")
    suspend fun deleteStudentsByClass(classId: Long)

    @Query("SELECT * FROM students WHERE id = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: Long): Student?

    @Transaction
    suspend fun syncClassStudentsAndGrades(classId: Long, students: List<Student>, grades: List<Grade>) {
        students.forEach { insertStudent(it) }
        if (grades.isNotEmpty()) {
            insertGrades(grades)
        }
    }

    // --- Subjects ---
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Query("DELETE FROM subjects WHERE id = :subjectId")
    suspend fun deleteSubject(subjectId: Long)

    // --- Grades ---
    @Query("SELECT * FROM grades")
    fun getAllGrades(): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE studentId = :studentId")
    fun getGradesForStudent(studentId: Long): Flow<List<Grade>>

    @Query("SELECT g.* FROM grades g INNER JOIN students s ON g.studentId = s.id WHERE s.classId = :classId")
    fun getGradesForClass(classId: Long): Flow<List<Grade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: Grade)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<Grade>)

    @Query("DELETE FROM grades WHERE studentId = :studentId AND subjectId = :subjectId AND termId = :termId")
    suspend fun deleteGrade(studentId: Long, subjectId: Long, termId: Int)

    @Query("DELETE FROM grades WHERE studentId IN (SELECT id FROM students WHERE classId = :classId)")
    suspend fun deleteGradesByClass(classId: Long)

    @Query("DELETE FROM grades WHERE studentId = :studentId")
    suspend fun deleteGradesByStudent(studentId: Long)

    @Query("DELETE FROM grades WHERE subjectId = :subjectId")
    suspend fun deleteGradesBySubject(subjectId: Long)

    // --- Subject Customizations ---
    @Query("SELECT * FROM class_subject_customizations")
    fun getAllCustomizations(): Flow<List<ClassSubjectCustomization>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomization(customization: ClassSubjectCustomization)

    @Query("DELETE FROM class_subject_customizations WHERE classId = :classId AND subjectId = :subjectId")
    suspend fun deleteCustomization(classId: Long, subjectId: Long)

    @Query("DELETE FROM class_subject_customizations WHERE classId = :classId")
    suspend fun deleteCustomizationsByClass(classId: Long)

    // --- Backup & Restore Direct Queries ---
    @Query("SELECT * FROM class_sections")
    suspend fun getAllClassSectionsList(): List<ClassSection>

    @Query("SELECT * FROM students")
    suspend fun getAllStudentsList(): List<Student>

    @Query("SELECT * FROM subjects")
    suspend fun getAllSubjectsList(): List<Subject>

    @Query("SELECT * FROM grades")
    suspend fun getAllGradesList(): List<Grade>

    @Query("SELECT * FROM class_subject_customizations")
    suspend fun getAllCustomizationsList(): List<ClassSubjectCustomization>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassSections(sections: List<ClassSection>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomizations(customizations: List<ClassSubjectCustomization>)

    @Query("DELETE FROM class_sections")
    suspend fun deleteAllClassSections()

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()

    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()

    @Query("DELETE FROM grades")
    suspend fun deleteAllGrades()

    @Query("DELETE FROM class_subject_customizations")
    suspend fun deleteAllCustomizations()

    @Transaction
    suspend fun restoreDatabase(
        sections: List<ClassSection>,
        students: List<Student>,
        subjects: List<Subject>,
        grades: List<Grade>,
        customizations: List<ClassSubjectCustomization>,
        manualTermAverages: List<StudentManualTermAverage>,
        localNotifications: List<LocalNotification>
    ) {
        deleteAllClassSections()
        deleteAllStudents()
        deleteAllSubjects()
        deleteAllGrades()
        deleteAllCustomizations()
        deleteAllManualTermAverages()
        deleteAllLocalNotifications()

        insertClassSections(sections)
        insertStudents(students)
        insertSubjects(subjects)
        insertGrades(grades)
        insertCustomizations(customizations)
        insertManualTermAverages(manualTermAverages)
        insertLocalNotifications(localNotifications)
    }

    // --- Teacher Exchange Posts ---
    @Query("SELECT * FROM teacher_exchange_posts ORDER BY timestamp DESC")
    fun getAllExchangePostsFlow(): Flow<List<TeacherExchangePost>>

    // --- Student Manual Term Averages (For Final Term Only Mode) ---
    @Query("SELECT * FROM student_manual_term_averages")
    fun getAllManualTermAverages(): Flow<List<StudentManualTermAverage>>

    @Query("SELECT * FROM student_manual_term_averages")
    suspend fun getAllManualTermAveragesList(): List<StudentManualTermAverage>

    @Query("SELECT * FROM student_manual_term_averages WHERE studentId = :studentId")
    fun getManualTermAveragesForStudent(studentId: Long): Flow<List<StudentManualTermAverage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManualTermAverage(manualAvg: StudentManualTermAverage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManualTermAverages(manualAvgs: List<StudentManualTermAverage>)

    @Query("DELETE FROM student_manual_term_averages WHERE studentId = :studentId AND termId = :termId")
    suspend fun deleteManualTermAverage(studentId: Long, termId: Int)

    @Query("DELETE FROM student_manual_term_averages WHERE studentId = :studentId")
    suspend fun deleteManualTermAveragesByStudent(studentId: Long)

    @Query("DELETE FROM student_manual_term_averages WHERE studentId IN (SELECT id FROM students WHERE classId = :classId)")
    suspend fun deleteManualTermAveragesByClass(classId: Long)

    @Query("DELETE FROM student_manual_term_averages")
    suspend fun deleteAllManualTermAverages()

    @Query("SELECT * FROM teacher_exchange_posts ORDER BY timestamp DESC")
    suspend fun getAllExchangePosts(): List<TeacherExchangePost>

    @Query("SELECT * FROM teacher_exchange_posts WHERE isSynced = 0")
    suspend fun getUnsyncedExchangePosts(): List<TeacherExchangePost>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangePost(post: TeacherExchangePost)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangePosts(posts: List<TeacherExchangePost>)

    @Query("DELETE FROM teacher_exchange_posts WHERE id = :postId")
    suspend fun deleteExchangePost(postId: String)

    @Query("DELETE FROM teacher_exchange_posts WHERE isSynced = 1")
    suspend fun deleteAllSyncedExchangePosts()

    // --- Local Notifications ---
    @Query("SELECT * FROM local_notifications ORDER BY createdAt DESC LIMIT 50")
    fun getAllLocalNotifications(): kotlinx.coroutines.flow.Flow<List<LocalNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalNotification(notification: LocalNotification)

    @Query("UPDATE local_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markLocalNotificationRead(id: Long)

    @Query("UPDATE local_notifications SET isRead = 1")
    suspend fun markAllLocalNotificationsRead()

    @Query("DELETE FROM local_notifications")
    suspend fun deleteAllLocalNotifications()

    @Query("SELECT * FROM local_notifications")
    suspend fun getAllLocalNotificationsList(): List<LocalNotification>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalNotifications(notifications: List<LocalNotification>)
}
