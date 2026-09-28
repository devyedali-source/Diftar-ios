package com.example.data.exams

import com.example.compat.*
import kotlinx.coroutines.IO

import com.squareup.moshi.JsonClass

@kotlinx.serialization.Serializable
data class ExamsRoot(
    val version: Int = 1,
    val title: String = "",
    val levels: List<ExamLevel> = emptyList()
)

@kotlinx.serialization.Serializable
data class ExamLevel(
    val code: String = "",
    val exams: List<ExamTerm> = emptyList()
)

@kotlinx.serialization.Serializable
data class ExamTerm(
    val fasl: Int = 0,
    val semaine: Int = 0,
    val subjects: List<ExamSubject> = emptyList()
)

@kotlinx.serialization.Serializable
data class ExamSubject(
    val code: String = "",
    val name: String = "",
    val proposals: List<ExamProposal> = emptyList()
)

@kotlinx.serialization.Serializable
data class ExamProposal(
    val id: String = "",
    val title: String = "",
    val ctx: String = "",
    val task: String = "",
    val score: String = ""
)
