package com.example.data.planning.canonical

import com.example.compat.*
import kotlinx.coroutines.IO

import com.squareup.moshi.JsonClass

@kotlinx.serialization.Serializable
data class PreparationFileRoot(
    val niveau: String = "",
    val source_file: String? = null,
    val preparations: List<PreparationItem> = emptyList()
)

@kotlinx.serialization.Serializable
data class PreparationItem(
    val schema_version: String = "1.0",
    val content_version: Int? = null,
    val preparation_id: String,
    val official: PreparationOfficial,
    val fiche: PreparationFiche,
    val deroulement: PreparationDeroulement,
    val evaluation: PreparationEvaluation? = null,
    val continuity: PreparationContinuity? = null
)

@kotlinx.serialization.Serializable
data class PreparationOfficial(
    val source_type: String? = null,
    val lesson_unit_id: String,
    val niveau: String,
    val fasl: Int? = null,
    val matiere: String,
    val matiere_label: String? = null,
    val source_matiere: String? = null,
    val domaine: String,
    val domaine_label: String? = null,
    val source_domaine: String? = null,
    val source_parent_subject: String? = null,
    val competence: String? = null,
    val semaine: Int,
    val titre_officiel: String,
    val partie: Int = 1,
    val nombre_parties: Int = 1,
    val est_suite: Boolean = false,
    val competence_enonce: String? = null,
    val official_habiletes: List<String> = emptyList(),
    val official_activites: List<String> = emptyList()
)

@kotlinx.serialization.Serializable
data class PreparationFiche(
    val source_type: String? = null,
    val pedagogical_profile: String? = null,
    val objectif_specifique: String = "",
    val duree_minutes: Int = 45,
    val prerequis: List<String> = emptyList(),
    val moyens: List<String> = emptyList(),
    val vocabulaire_cle: List<String> = emptyList(),
    val date_hijri: String? = null,
    val date_gregorien: String? = null
)

@kotlinx.serialization.Serializable
data class PreparationDeroulement(
    val source_type: String? = null,
    val etapes: List<PreparationEtape> = emptyList()
)

@kotlinx.serialization.Serializable
data class PreparationEtape(
    val code: String? = null,
    val libelle: String,
    val duree_minutes: Int = 0,
    val activites_enseignant: List<String> = emptyList(),
    val activites_eleve: List<String> = emptyList(),
    val consigne: String? = null
)

@kotlinx.serialization.Serializable
data class PreparationEvaluation(
    val source_type: String? = null,
    val evaluation_formative: List<String> = emptyList(),
    val indicateurs_reussite: List<String> = emptyList(),
    val remediation: List<String> = emptyList(),
    val devoir: String? = null,
    val evaluation_officielle_ref: String? = null
)

@kotlinx.serialization.Serializable
data class PreparationContinuity(
    val lesson_unit_id: String? = null,
    val partie: Int? = null,
    val nombre_parties: Int? = null,
    val est_suite: Boolean? = null,
    val previous_week_focus: String? = null,
    val current_week_focus: String? = null,
    val next_week_focus: String? = null,
    val role_partie: String? = null
)
