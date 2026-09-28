package com.example.data.content

import com.example.platform.gunzip
import com.example.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * قراءة ملفات المحتوى المنقولة كما هي من مجلد assets في نسخة أندرويد.
 * المسار نفسه المستعمل في أندرويد، مثل: "preparations/1AP_PREPARATIONS_RUNTIME_LIGHT.json.gz"
 */
object AppContent {
    val LEVELS = listOf("1AP", "2AP", "3AP", "4AP", "5AP", "6AP")

    fun preparationsPath(level: String) = "preparations/${level}_PREPARATIONS_RUNTIME_LIGHT.json.gz"
    fun planningPath(level: String) = "planning/plan_annuel_${level}_canonical.json.gz"
    const val COURSE = "course/course_content_runtime.json.gz"
    const val CONCOURS = "concours/concours_guide_runtime.json.gz"
    const val EXAMS = "exams/exams_runtime.json.gz"
    const val LEGISLATION = "school_legislation_verified_v2.json.gz"

    @OptIn(ExperimentalResourceApi::class)
    suspend fun readBytes(assetPath: String): ByteArray = Res.readBytes("files/$assetPath")

    /** يعادل openAssetAsText في أندرويد: يفكّ الضغط إن كان الملف .gz */
    suspend fun readText(assetPath: String): String {
        val raw = readBytes(assetPath)
        val bytes = if (assetPath.endsWith(".gz")) gunzip(raw) else raw
        return bytes.decodeToString()
    }
}
