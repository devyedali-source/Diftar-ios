package com.example.data.repository

import com.example.compat.*
import kotlinx.coroutines.IO

import com.example.data.dao.TeacherDao
import com.example.data.models.ClassSection
import com.example.data.models.Grade
import com.example.data.models.Student
import com.example.data.models.Subject
import com.example.data.models.ClassSubjectCustomization
import com.example.data.models.StudentManualTermAverage
import com.example.data.models.LocalNotification
import kotlinx.coroutines.flow.Flow

class TeacherRepository(private val teacherDao: TeacherDao) {

    // --- Class Sections ---
    val allClassSections: Flow<List<ClassSection>> = teacherDao.getAllClassSections()

    suspend fun insertClassSection(section: ClassSection): Long {
        return teacherDao.insertClassSection(section)
    }

    suspend fun getClassSectionById(sectionId: Long): ClassSection? {
        return teacherDao.getClassSectionById(sectionId)
    }

    suspend fun deleteClassSection(sectionId: Long) {
        teacherDao.deleteClassSection(sectionId)
    }

    // --- Students ---
    val allStudents: Flow<List<Student>> = teacherDao.getAllStudents()

    fun getStudentsByClass(classId: Long): Flow<List<Student>> {
        return teacherDao.getStudentsByClass(classId)
    }

    suspend fun insertStudent(student: Student): Long {
        return teacherDao.insertStudent(student)
    }

    suspend fun deleteStudent(studentId: Long) {
        teacherDao.deleteStudent(studentId)
    }

    suspend fun deleteStudentsByClass(classId: Long) {
        teacherDao.deleteStudentsByClass(classId)
    }

    suspend fun getStudentById(studentId: Long): Student? {
        return teacherDao.getStudentById(studentId)
    }

    suspend fun syncClassStudentsAndGrades(classId: Long, students: List<Student>, grades: List<Grade>) {
        teacherDao.syncClassStudentsAndGrades(classId, students, grades)
    }

    suspend fun deleteGradesByClass(classId: Long) {
        teacherDao.deleteGradesByClass(classId)
    }

    suspend fun deleteGradesByStudent(studentId: Long) {
        teacherDao.deleteGradesByStudent(studentId)
    }

    suspend fun deleteGradesBySubject(subjectId: Long) {
        teacherDao.deleteGradesBySubject(subjectId)
    }

    // --- Subjects ---
    val allSubjects: Flow<List<Subject>> = teacherDao.getAllSubjects()

    suspend fun insertSubject(subject: Subject): Long {
        return teacherDao.insertSubject(subject)
    }

    suspend fun deleteSubject(subjectId: Long) {
        teacherDao.deleteSubject(subjectId)
    }

    // --- Grades ---
    val allGrades: Flow<List<Grade>> = teacherDao.getAllGrades()

    fun getGradesForStudent(studentId: Long): Flow<List<Grade>> {
        return teacherDao.getGradesForStudent(studentId)
    }

    fun getGradesForClass(classId: Long): Flow<List<Grade>> {
        return teacherDao.getGradesForClass(classId)
    }

    suspend fun insertGrade(grade: Grade) {
        teacherDao.insertGrade(grade)
    }

    suspend fun insertGrades(grades: List<Grade>) {
        teacherDao.insertGrades(grades)
    }

    suspend fun deleteGrade(studentId: Long, subjectId: Long, termId: Int) {
        teacherDao.deleteGrade(studentId, subjectId, termId)
    }

    // --- Subject Customizations ---
    val allCustomizations: Flow<List<ClassSubjectCustomization>> = teacherDao.getAllCustomizations()

    suspend fun insertCustomization(customization: ClassSubjectCustomization) {
        teacherDao.insertCustomization(customization)
    }

    suspend fun deleteCustomization(classId: Long, subjectId: Long) {
        teacherDao.deleteCustomization(classId, subjectId)
    }

    suspend fun deleteCustomizationsByClass(classId: Long) {
        teacherDao.deleteCustomizationsByClass(classId)
    }

    // --- Student Manual Term Averages ---
    val allManualTermAverages: Flow<List<StudentManualTermAverage>> = teacherDao.getAllManualTermAverages()

    suspend fun getAllManualTermAveragesList(): List<StudentManualTermAverage> {
        return teacherDao.getAllManualTermAveragesList()
    }

    fun getManualTermAveragesForStudent(studentId: Long): Flow<List<StudentManualTermAverage>> {
        return teacherDao.getManualTermAveragesForStudent(studentId)
    }

    suspend fun insertManualTermAverage(manualAvg: StudentManualTermAverage) {
        teacherDao.insertManualTermAverage(manualAvg)
    }

    suspend fun insertManualTermAverages(manualAvgs: List<StudentManualTermAverage>) {
        teacherDao.insertManualTermAverages(manualAvgs)
    }

    suspend fun deleteManualTermAverage(studentId: Long, termId: Int) {
        teacherDao.deleteManualTermAverage(studentId, termId)
    }

    suspend fun deleteManualTermAveragesByStudent(studentId: Long) {
        teacherDao.deleteManualTermAveragesByStudent(studentId)
    }

    suspend fun deleteManualTermAveragesByClass(classId: Long) {
        teacherDao.deleteManualTermAveragesByClass(classId)
    }

    // --- Backup & Restore ---
    suspend fun getAllClassSectionsList() = teacherDao.getAllClassSectionsList()
    suspend fun getAllStudentsList() = teacherDao.getAllStudentsList()
    suspend fun getAllSubjectsList() = teacherDao.getAllSubjectsList()
    suspend fun getAllGradesList() = teacherDao.getAllGradesList()
    suspend fun getAllCustomizationsList() = teacherDao.getAllCustomizationsList()

    suspend fun restoreDatabase(
        sections: List<ClassSection>,
        students: List<Student>,
        subjects: List<Subject>,
        grades: List<Grade>,
        customizations: List<ClassSubjectCustomization>,
        manualTermAverages: List<StudentManualTermAverage>,
        localNotifications: List<LocalNotification>
    ) {
        teacherDao.restoreDatabase(sections, students, subjects, grades, customizations, manualTermAverages, localNotifications)
    }

    // --- Local Notifications ---
    fun getAllLocalNotifications(): Flow<List<LocalNotification>> {
        return teacherDao.getAllLocalNotifications()
    }

    suspend fun insertLocalNotification(notification: LocalNotification) {
        teacherDao.insertLocalNotification(notification)
    }

    suspend fun markLocalNotificationRead(id: Long) {
        teacherDao.markLocalNotificationRead(id)
    }

    suspend fun markAllLocalNotificationsRead() {
        teacherDao.markAllLocalNotificationsRead()
    }

    suspend fun deleteAllLocalNotifications() {
        teacherDao.deleteAllLocalNotifications()
    }

    suspend fun getAllLocalNotificationsList(): List<LocalNotification> {
        return teacherDao.getAllLocalNotificationsList()
    }
}
