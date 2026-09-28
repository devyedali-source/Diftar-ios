package com.example.data.planning

import com.example.compat.*
import kotlinx.coroutines.IO

data class AnnualLessonItem(
    val id: String,
    val level: Int, // 1, 2, 3, 4, 5
    val week: Int, // 1..38
    val weekEnd: Int = week, // For multi-week spanning (e.g. week 2 to 5)
    val subject: String, // e.g. "التربية الإسلامية"
    val domain: String = "", // e.g. "القرآن الكريم"
    val title: String, // المعارف / العنوان
    val competency: String = "", // الكفاية
    val skills: String = "", // المهارات
    val activities: String = "", // الأنشطة والوسائل
    val isFrench: Boolean = false, // Language direction (LTR if French)
    val type: String = "TEACHING" // TEACHING, DIAGNOSTIC, INTEGRATION_ASSESSMENT_REMEDIATION, EXAM, HOLIDAY, ADMIN
) {
    fun getWeekDisplay(): String {
        return if (week == weekEnd) {
            "الأسبوع $week"
        } else {
            "الأسابيع من $week إلى $weekEnd"
        }
    }

    fun isTeachingLesson(): Boolean {
        if (type != "TEACHING") return false
        val sub = subject.trim()
        val tit = title.trim()
        if (sub.contains("عطلة") || tit.contains("عطلة")) return false
        if (sub.contains("اختبار") || tit.contains("اختبار")) return false
        if (sub.contains("امتحان") || tit.contains("امتحان")) return false
        if (sub.contains("المسابقة الوطنية") || tit.contains("المسابقة الوطنية")) return false
        if (sub.contains("إدماج") || tit.contains("إدماج") || sub.contains("محطة") || tit.contains("محطة")) return false
        if (sub.contains("علاج") || tit.contains("علاج")) return false
        if (sub.contains("التهيئة") || tit.contains("التهيئة") || sub.contains("تشخيص") || tit.contains("تشخيص")) return false
        if (sub.contains("مداولات") || tit.contains("مداولات")) return false
        return true
    }

    fun getCategoryType(): String {
        if (type == "HOLIDAY" || subject.contains("عطلة") || title.contains("عطلة")) return "HOLIDAY"
        if (type == "EXAM" || subject.contains("اختبار") || title.contains("اختبار") || subject.contains("امتحان") || title.contains("امتحان") || subject.contains("مسابقة") || title.contains("مسابقة")) return "EXAM"
        if (type == "INTEGRATION_ASSESSMENT_REMEDIATION" || subject.contains("إدماج") || title.contains("إدماج") || subject.contains("محطة") || title.contains("محطة") || subject.contains("علاج") || title.contains("علاج")) return "INTEGRATION"
        if (type == "DIAGNOSTIC" || subject.contains("التهيئة") || title.contains("التهيئة") || subject.contains("تشخيص") || title.contains("تشخيص")) return "DIAGNOSTIC"
        return "TEACHING"
    }
}

data class PlanningUnit(
    val id: String,
    val subject: String,
    val domain: String,
    val weekStart: Int,
    val weekEnd: Int,
    val isFrench: Boolean,
    val type: String,
    val lesson: AnnualLessonItem
) {
    val isTeachingUnit: Boolean get() = type == "TEACHING" && lesson.isTeachingLesson()
    val teachingWeeks: List<Int> get() = if (isTeachingUnit) (weekStart..weekEnd).toList() else emptyList()
}

data class AnnualPlanningProgress(
    val completedWeeks: Set<Int> = emptySet(),
    val taughtLessonIds: Set<String> = emptySet(),
    val lastVisitedWeek: Int? = null
) {
    val taughtLessons: Set<String> get() = taughtLessonIds
}

enum class LessonItemType {
    REGULAR_LESSON,
    PEDAGOGICAL_STATION,
    EXAM,
    HOLIDAY
}

data class WeeklyLessonInstance(
    val instanceId: String,
    val lesson: AnnualLessonItem,
    val week: Int,
    val subWeekIndex: Int = 0,
    val totalWeeksSpan: Int = 1,
    val itemType: LessonItemType = LessonItemType.REGULAR_LESSON
) {
    val level: Int get() = lesson.level
    val subject: String get() = lesson.subject
    val domain: String get() = lesson.domain
    val title: String get() = lesson.title
    val isFrench: Boolean get() = lesson.isFrench
}

data class IntegrationProposal(
    val title: String,
    val situationContext: String,
    val tasks: List<String>,
    val targetedSkills: String
)

data class AssessmentProposal(
    val title: String,
    val questionType: String,
    val assessmentItems: List<String>,
    val successCriteria: String
)

data class RemediationProposal(
    val expectedDifficulty: String,
    val remediationActivity: String,
    val pedagogicalSupport: String
)

data class PedagogicalStationData(
    val levelTitle: String,
    val subject: String,
    val domain: String,
    val stationTitle: String,
    val week: Int,
    val term: Int,
    val isFrench: Boolean,
    val integrationProposals: List<IntegrationProposal>,
    val assessmentProposals: List<AssessmentProposal>,
    val remediationProposals: List<RemediationProposal>
)

data class LessonProcedureStep(
    val phaseName: String, // "التقديم", "تنمية التعلم", "التطبيق"
    val teacherActivity: String, // ماذا يفعل المعلم
    val studentActivity: String, // ماذا يفعل التلميذ
    val durationText: String // مدة النشاط
)

data class LessonProposalData(
    val levelTitle: String, // e.g. "المستوى الأول"
    val domain: String, // المجال
    val sectionModule: String, // المقطع
    val subject: String, // المادة
    val topic: String, // موضوع الدرس
    val currentWeekSubPhase: String = "", // التدرج البيداغوجي للأسبوع المختار
    val sessionInfo: String, // الحصة
    val durationText: String, // المدة
    val referenceText: String, // المرجع
    val teachingAids: String, // الوسائل التعليمية
    val specificObjective: String, // الهدف المميز / الخاص للحصة
    val procedureSteps: List<LessonProcedureStep> // جدول سير الحصة
)

enum class TimetablePeriodType {
    MORNING_BEFORE_RECESS, // الصباحية (قبل الراحة)
    MORNING_AFTER_RECESS,  // الصباحية (بعد الراحة)
    AFTERNOON              // المسائية (فترة ما بعد الظهر)
}

enum class DayOfWeekAr(val code: String, val arabicName: String, val frenchName: String) {
    LUNDI("MON", "الإثنين", "Lundi"),
    MARDI("TUE", "الثلاثاء", "Mardi"),
    MERCREDI("WED", "الأربعاء", "Mercredi"),
    JEUDI("THU", "الخميس", "Jeudi"),
    VENDREDI("FRI", "الجمعة", "Vendredi")
}

data class OfficialTimetableSlot(
    val id: String,
    val level: Int,
    val day: DayOfWeekAr,
    val periodType: TimetablePeriodType,
    val slotIndex: Int,
    val subject: String,
    val branch: String = "",
    val durationMinutes: Int,
    val isFrench: Boolean = false,
    val isDeprecatedOldTimetable: Boolean = false
)

data class OfficialSessionMapping(
    val level: Int,
    val week: Int,
    val subject: String,
    val domain: String,
    val officialTitle: String,
    val sessionNumber: Int,
    val totalSessions: Int,
    val durationMinutes: Int,
    val day: DayOfWeekAr,
    val slotId: String,
    val isFrench: Boolean = false,
    val isDeprecatedOldTimetable: Boolean = false
)

