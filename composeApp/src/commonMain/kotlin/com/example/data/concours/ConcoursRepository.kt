package com.example.data.concours

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

object ConcoursRepository {

    private const val ASSET_PATH = "concours/concours_guide_runtime.json.gz"
    private const val ASSET_PATH_RAW = "concours/concours_guide_runtime.json"

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val rootAdapter by lazy { moshi.adapter(ConcoursRoot::class.java) }

    private val loadLock = Any()

    @Volatile
    private var cachedRoot: ConcoursRoot? = null

    @Volatile
    var lastError: String? = null

    fun load(context: Context): ConcoursRoot? {
        cachedRoot?.let { return it }
        synchronized(loadLock) {
            cachedRoot?.let { return it }
            val parsed: ConcoursRoot? = try {
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

    fun getSubjects(context: Context): List<ConcoursSubject> {
        return load(context)?.subjects ?: emptyList()
    }

    fun getSubject(context: Context, code: String): ConcoursSubject? {
        return getSubjects(context).firstOrNull { it.code.equals(code, ignoreCase = true) }
    }

    fun getTotalExercisesCount(context: Context): Int {
        return getSubjects(context).sumOf { subject ->
            subject.domains.sumOf { it.items.size } + subject.situations.size + subject.qa.size
        }
    }

    fun getSubjectExercisesCount(context: Context, code: String): Int {
        val subject = getSubject(context, code) ?: return 0
        return subject.domains.sumOf { it.items.size } + subject.situations.size + subject.qa.size
    }
}
