package com.example.data.course

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

object CourseRepository {

    private const val ASSET_PATH = "course/course_content_runtime.json.gz"
    private const val ASSET_PATH_RAW = "course/course_content_runtime.json"

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val rootAdapter by lazy { moshi.adapter(CourseRoot::class.java) }

    private val loadLock = Any()

    @Volatile
    private var cachedRoot: CourseRoot? = null

    @Volatile
    var lastError: String? = null

    fun load(context: Context): CourseRoot? {
        cachedRoot?.let { return it }
        synchronized(loadLock) {
            cachedRoot?.let { return it }
            val parsed: CourseRoot? = try {
                context.applicationContext.assets.open(ASSET_PATH).use { base ->
                    GZIPInputStream(base).use { gz ->
                        InputStreamReader(gz, Charsets.UTF_8).use { reader ->
                            rootAdapter.fromJson(reader.readText())
                        }
                    }
                }
            } catch (e1: Throwable) {
                try {
                    context.applicationContext.assets.open(ASSET_PATH_RAW).use { base ->
                        InputStreamReader(base, Charsets.UTF_8).use { reader ->
                            rootAdapter.fromJson(reader.readText())
                        }
                    }
                } catch (e2: Throwable) {
                    lastError = e2.javaClass.simpleName + ": " + (e2.message ?: "-") +
                        (e2.cause?.let { " | سبب: " + it.javaClass.simpleName + ": " + (it.message ?: "-") } ?: "")
                    null
                }
            }
            if (parsed != null) {
                lastError = null
            }
            cachedRoot = parsed
            return parsed
        }
    }

    fun isLoaded(): Boolean = cachedRoot != null

    fun release() {
        synchronized(loadLock) {
            cachedRoot = null
        }
    }

    fun getTracks(context: Context): List<CourseTrack> {
        return load(context)?.tracks ?: emptyList()
    }

    fun getTrack(context: Context, trackKey: String): CourseTrack? {
        return load(context)?.tracks?.firstOrNull { it.k == trackKey }
    }

    fun getSubjectNames(context: Context, trackKey: String): List<String> {
        return getTrack(context, trackKey)?.subs ?: emptyList()
    }

    fun getSubject(context: Context, subjectName: String): CourseSubject? {
        return load(context)?.subjects?.get(subjectName)
    }

    fun getLessons(context: Context, subjectName: String): List<CourseLesson> {
        return getSubject(context, subjectName)?.lessons ?: emptyList()
    }

    fun getLesson(context: Context, subjectName: String, lessonId: String): CourseLesson? {
        return getLessons(context, subjectName).firstOrNull { it.id == lessonId }
    }

    fun getSubjectExams(context: Context, subjectName: String): List<CourseExam> {
        return getSubject(context, subjectName)?.sub ?: emptyList()
    }

    fun getSimulatorExams(context: Context, subjectName: String): List<CourseExam> {
        return getSubject(context, subjectName)?.sim ?: emptyList()
    }

    fun getExam(context: Context, subjectName: String, examId: String): CourseExam? {
        val subject = getSubject(context, subjectName) ?: return null
        return subject.sub.firstOrNull { it.id == examId }
            ?: subject.sim.firstOrNull { it.id == examId }
    }

    fun getWritings(context: Context, subjectName: String): List<CourseWriting> {
        return getSubject(context, subjectName)?.wr ?: emptyList()
    }

    fun getWriting(context: Context, subjectName: String, writingId: String): CourseWriting? {
        return getWritings(context, subjectName).firstOrNull { it.id == writingId }
    }

    data class CourseStats(
        val subjects: Int,
        val lessons: Int,
        val lessonQuestions: Int,
        val exams: Int,
        val examQuestions: Int,
        val writings: Int
    )

    fun getStats(context: Context): CourseStats {
        val root = load(context) ?: return CourseStats(0, 0, 0, 0, 0, 0)
        var lessons = 0
        var lessonQuestions = 0
        var exams = 0
        var examQuestions = 0
        var writings = 0
        for (subject in root.subjects.values) {
            lessons += subject.lessons.size
            lessonQuestions += subject.lessons.sumOf { it.qs.size }
            exams += subject.sub.size + subject.sim.size
            examQuestions += subject.sub.sumOf { it.qs.size } + subject.sim.sumOf { it.qs.size }
            writings += subject.wr.size
        }
        return CourseStats(
            subjects = root.subjects.size,
            lessons = lessons,
            lessonQuestions = lessonQuestions,
            exams = exams,
            examQuestions = examQuestions,
            writings = writings
        )
    }
}
