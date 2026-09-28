package com.example.data.course

import com.example.compat.*
import kotlinx.coroutines.IO

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@kotlinx.serialization.Serializable
data class CourseRoot(
    val tracks: List<CourseTrack> = emptyList(),
    val subjects: Map<String, CourseSubject> = emptyMap()
)

@kotlinx.serialization.Serializable
data class CourseTrack(
    val k: String = "",
    val name: String = "",
    val subs: List<String> = emptyList()
)

@kotlinx.serialization.Serializable
data class CourseSubject(
    val name: String = "",
    val code: String = "",
    val lessons: List<CourseLesson> = emptyList(),
    val sub: List<CourseExam> = emptyList(),
    val sim: List<CourseExam> = emptyList(),
    val wr: List<CourseWriting> = emptyList()
)

@kotlinx.serialization.Serializable
data class CourseLesson(
    val id: String = "",
    val dom: String = "",
    val title: String = "",
    val no: Int = 0,
    val dno: Int = 0,
    val ino: Int = 0,
    val off: String = "",
    val ref: String = "",
    val st: String = "",
    val sum: String = "",
    val obj: List<String> = emptyList(),
    val met: List<String> = emptyList(),
    val err: List<String> = emptyList(),
    val ex: List<CourseExample> = emptyList(),
    val foc: String = "",
    val pass: Int = 0,
    val msg: String = "",
    val qs: List<CourseQuestion> = emptyList()
)

@kotlinx.serialization.Serializable
data class CourseExample(
    val prompt: String = "",
    val answer: String = "",
    val explanation: String = ""
)

@kotlinx.serialization.Serializable
data class CourseQuestion(
    val id: String = "",
    val q: String = "",
    val c: List<String> = emptyList(),
    val k: Int = 0,
    val ks: List<Int> = emptyList(),
    val m: String = "single",
    val d: String = "",
    val t: String = "",
    val e: String = "",
    val f: List<String> = emptyList(),
    val g: String? = null,
    val p: Int? = null
)

@kotlinx.serialization.Serializable
data class CourseExam(
    val id: String = "",
    val n: Int = 0,
    val count: Int = 0,
    val qs: List<CourseQuestion> = emptyList(),
    val meta: String = "",
    val struct: String = "",
    val groups: List<CourseGroup> = emptyList(),
    val pass: Int = 0,
    val msg: String = "",
    @kotlinx.serialization.SerialName("text_title") val textTitle: String? = null,
    val text: String? = null,
    @kotlinx.serialization.SerialName("situation_text") val situationText: String? = null
)

@kotlinx.serialization.Serializable
data class CourseGroup(
    val t: String = "",
    val n: Int = 0
)

@kotlinx.serialization.Serializable
data class CourseWriting(
    val id: String = "",
    val n: Int = 0,
    val t: String = "",
    val g: String = "",
    val m: Int = 0,
    val p: Int = 0,
    val mw: Int = 0,
    val q: String = "",
    val cr: List<List<@kotlinx.serialization.Serializable(with = com.example.compat.AnyValueSerializer::class) Any>> = emptyList(),
    val md: String = ""
)
