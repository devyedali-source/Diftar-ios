package com.example.data.exams

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

object ExamsRepository {

    private const val ASSET_PATH = "exams/exams_runtime.json.gz"
    private const val ASSET_PATH_RAW = "exams/exams_runtime.json"

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val rootAdapter by lazy { moshi.adapter(ExamsRoot::class.java) }

    private val loadLock = Any()

    @Volatile
    private var cachedRoot: ExamsRoot? = null

    @Volatile
    var lastError: String? = null

    fun load(context: Context): ExamsRoot? {
        cachedRoot?.let { return it }
        synchronized(loadLock) {
            cachedRoot?.let { return it }
            val parsed: ExamsRoot? = try {
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

    fun release() {
        synchronized(loadLock) {
            cachedRoot = null
        }
    }

    fun getExam(context: Context, levelCode: String, semaine: Int): ExamTerm? {
        val root = load(context) ?: return null
        val level = root.levels.firstOrNull { it.code.equals(levelCode, ignoreCase = true) } ?: return null
        return level.exams.firstOrNull { it.semaine == semaine }
    }

    fun getSubjects(context: Context, levelCode: String, semaine: Int): List<ExamSubject> {
        return getExam(context, levelCode, semaine)?.subjects ?: emptyList()
    }

    fun getSubject(context: Context, levelCode: String, semaine: Int, subjectCode: String): ExamSubject? {
        return getSubjects(context, levelCode, semaine).firstOrNull { it.code.equals(subjectCode, ignoreCase = true) }
    }

    fun getProposalsCount(context: Context, levelCode: String, semaine: Int): Int {
        return getSubjects(context, levelCode, semaine).sumOf { it.proposals.size }
    }
}
