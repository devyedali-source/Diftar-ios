package com.example.data.planning

import com.example.compat.*
import kotlinx.coroutines.IO

/**
 * Official Mauritanian Primary School Timetable Repository (استعمال الزمن الرسمي)
 * Based exclusively on the official Mauritanian national timetable circulars:
 * - Level 1 (1AP): 40 slots/week (1,125 min/week)
 * - Level 2 (2°AP): 49 slots/week (1,485 min/week)
 * - Level 3 (3ème et 4ème): 51 slots/week (1,605 min/week)
 * - Level 4 (3ème et 4ème): 51 slots/week (1,605 min/week)
 * - Level 5 (5ème et 6AP): 42 slots/week (1,605 min/week)
 * - Level 6: Data structure supported (curriculum to be populated when official document is provided)
 *
 * All old timetable structures are marked DEPRECATED and excluded from active calculations.
 */
object OfficialTimetableRepository {

    // =========================================================================
    // LEVEL 1 (1AP - السنة الأولى)
    // 40 slots/week, 1,125 minutes/week
    // =========================================================================
    private val level1Slots = listOf(
        // الإثنين (Lundi)
        OfficialTimetableSlot("L1_MON_1", 1, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "التربية الإسلامية", "القرآن الكريم", 30),
        OfficialTimetableSlot("L1_MON_2", 1, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "اللغة العربية", "القراءة", 45),
        OfficialTimetableSlot("L1_MON_3", 1, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "التربية المدنية", "التربية على المواطنة", 20),
        OfficialTimetableSlot("L1_MON_4", 1, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "الرياضيات", "الحساب", 25),
        OfficialTimetableSlot("L1_MON_5", 1, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "اللغة العربية", "القراءة", 40),
        OfficialTimetableSlot("L1_MON_6", 1, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "التربية الإسلامية", "الحديث الشريف", 30),
        OfficialTimetableSlot("L1_MON_7", 1, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "الرياضيات", "الهندسة", 20),
        OfficialTimetableSlot("L1_MON_8", 1, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_AFTER_RECESS, 8, "العلوم", "الصحة", 15),

        // الثلاثاء (Mardi)
        OfficialTimetableSlot("L1_TUE_1", 1, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "الرياضيات", "الحساب", 25),
        OfficialTimetableSlot("L1_TUE_2", 1, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "التربية الإسلامية", "العبادات", 30),
        OfficialTimetableSlot("L1_TUE_3", 1, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "التربية المدنية", "المهارات الحياتية", 20),
        OfficialTimetableSlot("L1_TUE_4", 1, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "اللغة العربية", "الكتابة", 45),
        OfficialTimetableSlot("L1_TUE_5", 1, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "الرياضيات", "الهندسة", 20),
        OfficialTimetableSlot("L1_TUE_6", 1, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "اللغة العربية", "الكتابة", 30),
        OfficialTimetableSlot("L1_TUE_7", 1, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "التربية الفنية", "الأعمال اليدوية والرسم", 30),
        OfficialTimetableSlot("L1_TUE_8", 1, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_AFTER_RECESS, 8, "اللغة العربية", "التعبير", 25),

        // الأربعاء (Mercredi)
        OfficialTimetableSlot("L1_WED_1", 1, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "التربية البدنية", "التربية البدنية", 35),
        OfficialTimetableSlot("L1_WED_2", 1, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "اللغة العربية", "القراءة", 35),
        OfficialTimetableSlot("L1_WED_3", 1, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "الرياضيات", "الحساب", 20),
        OfficialTimetableSlot("L1_WED_4", 1, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "التربية الإسلامية", "الأخلاق", 30),
        OfficialTimetableSlot("L1_WED_5", 1, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "اللغة العربية", "القراءة", 35),
        OfficialTimetableSlot("L1_WED_6", 1, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "العلوم", "قواعد السلامة", 15),
        OfficialTimetableSlot("L1_WED_7", 1, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "الرياضيات", "القياس", 20),
        OfficialTimetableSlot("L1_WED_8", 1, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 8, "اللغة العربية", "التعبير", 35),

        // الخميس (Jeudi)
        OfficialTimetableSlot("L1_THU_1", 1, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "التربية الإسلامية", "القرآن الكريم", 30),
        OfficialTimetableSlot("L1_THU_2", 1, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "اللغة العربية", "الكتابة", 40),
        OfficialTimetableSlot("L1_THU_3", 1, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "الرياضيات", "الحساب", 20),
        OfficialTimetableSlot("L1_THU_4", 1, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "التربية الإسلامية", "العقيدة", 30),
        OfficialTimetableSlot("L1_THU_5", 1, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "العلوم", "البيئة", 15),
        OfficialTimetableSlot("L1_THU_6", 1, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "التربية المدنية", "السلوك المدني", 20),
        OfficialTimetableSlot("L1_THU_7", 1, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "اللغة العربية", "التعبير", 40),
        OfficialTimetableSlot("L1_THU_8", 1, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_AFTER_RECESS, 8, "الرياضيات", "القياس", 30),

        // الجمعة (Vendredi)
        OfficialTimetableSlot("L1_FRI_1", 1, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "التربية البدنية", "التربية البدنية", 25),
        OfficialTimetableSlot("L1_FRI_2", 1, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "اللغة العربية", "القراءة", 40),
        OfficialTimetableSlot("L1_FRI_3", 1, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "اللغة العربية", "الكتابة", 35),
        OfficialTimetableSlot("L1_FRI_4", 1, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "الرياضيات", "الحساب", 20),
        OfficialTimetableSlot("L1_FRI_5", 1, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "اللغة العربية", "التعبير", 35),
        OfficialTimetableSlot("L1_FRI_6", 1, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "الرياضيات", "الهندسة", 20),
        OfficialTimetableSlot("L1_FRI_7", 1, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "التربية العلمية والتكنولوجية", "التقنيات", 30),
        OfficialTimetableSlot("L1_FRI_8", 1, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 8, "الرياضيات", "القياس", 20)
    )

    // =========================================================================
    // LEVEL 2 (2°AP - السنة الثانية)
    // 49 slots/week, 1,485 minutes/week
    // =========================================================================
    private val level2Slots = listOf(
        // الإثنين (Lundi)
        OfficialTimetableSlot("L2_MON_1", 2, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "التربية الإسلامية", "القرآن الكريم", 30),
        OfficialTimetableSlot("L2_MON_2", 2, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "التربية المدنية", "التربية على المواطنة", 20),
        OfficialTimetableSlot("L2_MON_3", 2, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "اللغة العربية", "القراءة", 30),
        OfficialTimetableSlot("L2_MON_4", 2, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "الرياضيات", "الحساب", 40),
        OfficialTimetableSlot("L2_MON_5", 2, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "اللغة العربية", "القراءة", 30),
        OfficialTimetableSlot("L2_MON_6", 2, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "التربية الإسلامية", "الحديث الشريف", 30),
        OfficialTimetableSlot("L2_MON_7", 2, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "الرياضيات", "الهندسة", 30),
        OfficialTimetableSlot("L2_MON_8", 2, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_AFTER_RECESS, 8, "العلوم", "الصحة", 15),
        OfficialTimetableSlot("L2_MON_9", 2, DayOfWeekAr.LUNDI, TimetablePeriodType.AFTERNOON, 9, "Français", "Compréhension de l'écrit", 30, isFrench = true),
        OfficialTimetableSlot("L2_MON_10", 2, DayOfWeekAr.LUNDI, TimetablePeriodType.AFTERNOON, 10, "Français", "Compréhension de l'oral", 40, isFrench = true),
        OfficialTimetableSlot("L2_MON_11", 2, DayOfWeekAr.LUNDI, TimetablePeriodType.AFTERNOON, 11, "Français", "Production de l'écrit", 50, isFrench = true),

        // الثلاثاء (Mardi)
        OfficialTimetableSlot("L2_TUE_1", 2, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "الرياضيات", "الحساب", 40),
        OfficialTimetableSlot("L2_TUE_2", 2, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "التربية الإسلامية", "العبادات", 30),
        OfficialTimetableSlot("L2_TUE_3", 2, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "التربية الفنية", "الأعمال اليدوية والرسم", 30),
        OfficialTimetableSlot("L2_TUE_4", 2, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "اللغة العربية", "الكتابة", 20),
        OfficialTimetableSlot("L2_TUE_5", 2, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "اللغة العربية", "الكتابة", 20),
        OfficialTimetableSlot("L2_TUE_6", 2, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "الرياضيات", "الهندسة", 20),
        OfficialTimetableSlot("L2_TUE_7", 2, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "التربية المدنية", "المهارات الحياتية", 20),
        OfficialTimetableSlot("L2_TUE_8", 2, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_AFTER_RECESS, 8, "اللغة العربية", "التعبير", 45),
        OfficialTimetableSlot("L2_TUE_9", 2, DayOfWeekAr.MARDI, TimetablePeriodType.AFTERNOON, 9, "Français", "Compréhension de l'écrit", 30, isFrench = true),
        OfficialTimetableSlot("L2_TUE_10", 2, DayOfWeekAr.MARDI, TimetablePeriodType.AFTERNOON, 10, "Français", "Compréhension de l'oral", 40, isFrench = true),
        OfficialTimetableSlot("L2_TUE_11", 2, DayOfWeekAr.MARDI, TimetablePeriodType.AFTERNOON, 11, "Français", "Production de l'écrit", 50, isFrench = true),

        // الأربعاء (Mercredi)
        OfficialTimetableSlot("L2_WED_1", 2, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "التربية البدنية", "التربية البدنية", 30),
        OfficialTimetableSlot("L2_WED_2", 2, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "اللغة العربية", "القراءة", 30),
        OfficialTimetableSlot("L2_WED_3", 2, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "الرياضيات", "الحساب", 30),
        OfficialTimetableSlot("L2_WED_4", 2, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "التربية الإسلامية", "الأخلاق", 30),
        OfficialTimetableSlot("L2_WED_5", 2, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "اللغة العربية", "القراءة", 30),
        OfficialTimetableSlot("L2_WED_6", 2, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "العلوم", "قواعد السلامة", 15),
        OfficialTimetableSlot("L2_WED_7", 2, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "الرياضيات", "القياس", 25),
        OfficialTimetableSlot("L2_WED_8", 2, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 8, "اللغة العربية", "التعبير", 35),

        // الخميس (Jeudi)
        OfficialTimetableSlot("L2_THU_1", 2, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "التربية الإسلامية", "القرآن الكريم", 30),
        OfficialTimetableSlot("L2_THU_2", 2, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "اللغة العربية", "الكتابة", 30),
        OfficialTimetableSlot("L2_THU_3", 2, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "الرياضيات", "الحساب", 30),
        OfficialTimetableSlot("L2_THU_4", 2, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "التربية الإسلامية", "العقيدة", 30),
        OfficialTimetableSlot("L2_THU_5", 2, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "العلوم", "البيئة", 15),
        OfficialTimetableSlot("L2_THU_6", 2, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "اللغة العربية", "الكتابة", 25),
        OfficialTimetableSlot("L2_THU_7", 2, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "التربية العلمية والتكنولوجية", "التقنيات", 30),
        OfficialTimetableSlot("L2_THU_8", 2, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_AFTER_RECESS, 8, "الرياضيات", "القياس", 35),
        OfficialTimetableSlot("L2_THU_9", 2, DayOfWeekAr.JEUDI, TimetablePeriodType.AFTERNOON, 9, "Français", "Compréhension de l'oral", 30, isFrench = true),
        OfficialTimetableSlot("L2_THU_10", 2, DayOfWeekAr.JEUDI, TimetablePeriodType.AFTERNOON, 10, "Français", "Compréhension de l'oral", 50, isFrench = true),
        OfficialTimetableSlot("L2_THU_11", 2, DayOfWeekAr.JEUDI, TimetablePeriodType.AFTERNOON, 11, "Français", "Production de l'écrit", 40, isFrench = true),

        // الجمعة (Vendredi)
        OfficialTimetableSlot("L2_FRI_1", 2, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "التربية البدنية", "التربية البدنية", 30),
        OfficialTimetableSlot("L2_FRI_2", 2, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "اللغة العربية", "القراءة", 35),
        OfficialTimetableSlot("L2_FRI_3", 2, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "الرياضيات", "الحساب", 25),
        OfficialTimetableSlot("L2_FRI_4", 2, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "اللغة العربية", "الكتابة", 30),
        OfficialTimetableSlot("L2_FRI_5", 2, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "اللغة العربية", "الكتابة", 20),
        OfficialTimetableSlot("L2_FRI_6", 2, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "الرياضيات", "الهندسة", 25),
        OfficialTimetableSlot("L2_FRI_7", 2, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "اللغة العربية", "التعبير", 40),
        OfficialTimetableSlot("L2_FRI_8", 2, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 8, "التربية المدنية", "السلوك المدني", 20)
    )

    // =========================================================================
    // LEVEL 3 & 4 (3ème et 4ème - السنة الثالثة والرابعة)
    // 51 slots/week, 1,605 minutes/week
    // =========================================================================
    private fun getLevel3And4Slots(level: Int): List<OfficialTimetableSlot> = listOf(
        // الإثنين (Lundi)
        OfficialTimetableSlot("L${level}_MON_1", level, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "التربية الإسلامية", "القرآن الكريم", 30),
        OfficialTimetableSlot("L${level}_MON_2", level, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "الرياضيات", "الحساب", 30),
        OfficialTimetableSlot("L${level}_MON_3", level, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "التربية المدنية", "المهارات الحياتية", 30),
        OfficialTimetableSlot("L${level}_MON_4", level, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "الرياضيات", "القياس", 30),
        OfficialTimetableSlot("L${level}_MON_5", level, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "اللغة العربية", "القراءة والفهم", 45),
        OfficialTimetableSlot("L${level}_MON_6", level, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "التربية الإسلامية", "العقيدة", 30),
        OfficialTimetableSlot("L${level}_MON_7", level, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "التربية المدنية", "السلوك المدني", 30),
        OfficialTimetableSlot("L${level}_MON_8", level, DayOfWeekAr.LUNDI, TimetablePeriodType.AFTERNOON, 8, "Français", "Compréhension de l'oral", 30, isFrench = true),
        OfficialTimetableSlot("L${level}_MON_9", level, DayOfWeekAr.LUNDI, TimetablePeriodType.AFTERNOON, 9, "Français", "Compréhension de l'écrit", 30, isFrench = true),
        OfficialTimetableSlot("L${level}_MON_10", level, DayOfWeekAr.LUNDI, TimetablePeriodType.AFTERNOON, 10, "Français", "Production de l'oral", 30, isFrench = true),
        OfficialTimetableSlot("L${level}_MON_11", level, DayOfWeekAr.LUNDI, TimetablePeriodType.AFTERNOON, 11, "Français", "Production de l'écrit", 30, isFrench = true),

        // الثلاثاء (Mardi)
        OfficialTimetableSlot("L${level}_TUE_1", level, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "التربية الإسلامية", "السيرة النبوية", 30),
        OfficialTimetableSlot("L${level}_TUE_2", level, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "الرياضيات", "الحساب", 30),
        OfficialTimetableSlot("L${level}_TUE_3", level, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "العلوم", "البيئة", 30),
        OfficialTimetableSlot("L${level}_TUE_4", level, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "الرياضيات", "الهندسة", 30),
        OfficialTimetableSlot("L${level}_TUE_5", level, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "التاريخ والجغرافيا", "التاريخ والجغرافيا", 45),
        OfficialTimetableSlot("L${level}_TUE_6", level, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "التربية الإسلامية", "الأخلاق", 30),
        OfficialTimetableSlot("L${level}_TUE_7", level, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "التربية العلمية والتكنولوجية", "تقنيات الإعلام والاتصال", 30),
        OfficialTimetableSlot("L${level}_TUE_8", level, DayOfWeekAr.MARDI, TimetablePeriodType.AFTERNOON, 8, "اللغة العربية", "النحو والصرف", 30),
        OfficialTimetableSlot("L${level}_TUE_9", level, DayOfWeekAr.MARDI, TimetablePeriodType.AFTERNOON, 9, "العلوم", "التجريب", 30),
        OfficialTimetableSlot("L${level}_TUE_10", level, DayOfWeekAr.MARDI, TimetablePeriodType.AFTERNOON, 10, "الرياضيات", "القياس", 30),
        OfficialTimetableSlot("L${level}_TUE_11", level, DayOfWeekAr.MARDI, TimetablePeriodType.AFTERNOON, 11, "التربية البدنية", "الرياضة", 30),

        // الأربعاء (Mercredi)
        OfficialTimetableSlot("L${level}_WED_1", level, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "التربية البدنية", "الرياضة", 30),
        OfficialTimetableSlot("L${level}_WED_2", level, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "اللغة العربية", "الإملاء والخط", 30),
        OfficialTimetableSlot("L${level}_WED_3", level, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "الرياضيات", "الهندسة", 30),
        OfficialTimetableSlot("L${level}_WED_4", level, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "اللغة العربية", "التعبير", 30),
        OfficialTimetableSlot("L${level}_WED_5", level, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "اللغة العربية", "القراءة والفهم", 45),
        OfficialTimetableSlot("L${level}_WED_6", level, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "الرياضيات", "الحساب", 30),
        OfficialTimetableSlot("L${level}_WED_7", level, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "اللغة العربية", "النحو والصرف", 30),
        OfficialTimetableSlot("L${level}_WED_8", level, DayOfWeekAr.MERCREDI, TimetablePeriodType.AFTERNOON, 8, "Français", "Compréhension de l'oral", 30, isFrench = true),
        OfficialTimetableSlot("L${level}_WED_9", level, DayOfWeekAr.MERCREDI, TimetablePeriodType.AFTERNOON, 9, "Français", "Production de l'oral", 30, isFrench = true),
        OfficialTimetableSlot("L${level}_WED_10", level, DayOfWeekAr.MERCREDI, TimetablePeriodType.AFTERNOON, 10, "Français", "Compréhension de l'écrit", 30, isFrench = true),
        OfficialTimetableSlot("L${level}_WED_11", level, DayOfWeekAr.MERCREDI, TimetablePeriodType.AFTERNOON, 11, "Français", "Compréhension de l'oral", 30, isFrench = true),

        // الخميس (Jeudi)
        OfficialTimetableSlot("L${level}_THU_1", level, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "التربية الإسلامية", "القرآن الكريم", 30),
        OfficialTimetableSlot("L${level}_THU_2", level, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "اللغة العربية", "التعبير", 30),
        OfficialTimetableSlot("L${level}_THU_3", level, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "اللغة العربية", "الإملاء والخط", 30),
        OfficialTimetableSlot("L${level}_THU_4", level, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "الرياضيات", "الحساب", 30),
        OfficialTimetableSlot("L${level}_THU_5", level, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "اللغة العربية", "النحو والصرف", 45),
        OfficialTimetableSlot("L${level}_THU_6", level, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "العلوم", "الغذاء والصحة", 30),
        OfficialTimetableSlot("L${level}_THU_7", level, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "التربية الفنية", "الرسم والأعمال", 30),
        OfficialTimetableSlot("L${level}_THU_8", level, DayOfWeekAr.JEUDI, TimetablePeriodType.AFTERNOON, 8, "Français", "Compréhension de l'écrit", 30, isFrench = true),
        OfficialTimetableSlot("L${level}_THU_9", level, DayOfWeekAr.JEUDI, TimetablePeriodType.AFTERNOON, 9, "Français", "Compréhension de l'oral", 30, isFrench = true),
        OfficialTimetableSlot("L${level}_THU_10", level, DayOfWeekAr.JEUDI, TimetablePeriodType.AFTERNOON, 10, "Français", "Compréhension de l'écrit", 30, isFrench = true),
        OfficialTimetableSlot("L${level}_THU_11", level, DayOfWeekAr.JEUDI, TimetablePeriodType.AFTERNOON, 11, "Français", "Compréhension de l'écrit", 30, isFrench = true),

        // الجمعة (Vendredi)
        OfficialTimetableSlot("L${level}_FRI_1", level, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "التربية الإسلامية", "الحديث الشريف", 30),
        OfficialTimetableSlot("L${level}_FRI_2", level, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "التربية المدنية", "التربية على المواطنة", 30),
        OfficialTimetableSlot("L${level}_FRI_3", level, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "اللغة العربية", "التعبير", 30),
        OfficialTimetableSlot("L${level}_FRI_4", level, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 4, "الرياضيات", "الحساب", 30),
        OfficialTimetableSlot("L${level}_FRI_5", level, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "اللغة العربية", "القراءة والفهم", 45),
        OfficialTimetableSlot("L${level}_FRI_6", level, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "التربية الإسلامية", "العبادات", 30),
        OfficialTimetableSlot("L${level}_FRI_7", level, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 7, "الرياضيات", "الهندسة", 30)
    )

    // =========================================================================
    // LEVEL 5 (5ème et 6AP - السنة الخامسة)
    // 42 slots/week, 1,605 minutes/week
    // =========================================================================
    private val level5Slots = listOf(
        // الإثنين (Lundi)
        OfficialTimetableSlot("L5_MON_1", 5, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "التربية البدنية", "التربية البدنية", 30),
        OfficialTimetableSlot("L5_MON_2", 5, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "اللغة العربية", "القراءة والفهم", 45),
        OfficialTimetableSlot("L5_MON_3", 5, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "الرياضيات", "الحساب", 45),
        OfficialTimetableSlot("L5_MON_4", 5, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_AFTER_RECESS, 4, "التربية الإسلامية", "العبادات", 30),
        OfficialTimetableSlot("L5_MON_5", 5, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "التربية المدنية", "التربية على المواطنة", 30),
        OfficialTimetableSlot("L5_MON_6", 5, DayOfWeekAr.LUNDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "العلوم", "البيئة / التجريب", 45),
        OfficialTimetableSlot("L5_MON_7", 5, DayOfWeekAr.LUNDI, TimetablePeriodType.AFTERNOON, 7, "اللغة العربية", "قواعد النحو والصرف", 45),
        OfficialTimetableSlot("L5_MON_8", 5, DayOfWeekAr.LUNDI, TimetablePeriodType.AFTERNOON, 8, "التربية الإسلامية", "الحديث الشريف", 30),
        OfficialTimetableSlot("L5_MON_9", 5, DayOfWeekAr.LUNDI, TimetablePeriodType.AFTERNOON, 9, "الرياضيات", "الهندسة", 45),

        // الثلاثاء (Mardi)
        OfficialTimetableSlot("L5_TUE_1", 5, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "Français", "Compréhension de l'écrit", 45, isFrench = true),
        OfficialTimetableSlot("L5_TUE_2", 5, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "Français", "Production de l'écrit", 45, isFrench = true),
        OfficialTimetableSlot("L5_TUE_3", 5, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "Français", "Compréhension de l'oral", 30, isFrench = true),
        OfficialTimetableSlot("L5_TUE_4", 5, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_AFTER_RECESS, 4, "اللغة العربية", "القراءة والفهم", 45),
        OfficialTimetableSlot("L5_TUE_5", 5, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "الجغرافيا", "الجغرافيا", 30),
        OfficialTimetableSlot("L5_TUE_6", 5, DayOfWeekAr.MARDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "التربية الإسلامية", "العقيدة / الأخلاق", 30),
        OfficialTimetableSlot("L5_TUE_7", 5, DayOfWeekAr.MARDI, TimetablePeriodType.AFTERNOON, 7, "الرياضيات", "القياس", 45),
        OfficialTimetableSlot("L5_TUE_8", 5, DayOfWeekAr.MARDI, TimetablePeriodType.AFTERNOON, 8, "اللغة العربية", "الإملاء والخط العربي", 45),
        OfficialTimetableSlot("L5_TUE_9", 5, DayOfWeekAr.MARDI, TimetablePeriodType.AFTERNOON, 9, "اللغة العربية", "التعبير", 30),

        // الأربعاء (Mercredi)
        OfficialTimetableSlot("L5_WED_1", 5, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "الرياضيات", "الحساب", 45),
        OfficialTimetableSlot("L5_WED_2", 5, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "اللغة العربية", "التعبير", 45),
        OfficialTimetableSlot("L5_WED_3", 5, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "التربية الإسلامية", "القرآن الكريم", 30),
        OfficialTimetableSlot("L5_WED_4", 5, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 4, "اللغة العربية", "قواعد النحو والصرف", 45),
        OfficialTimetableSlot("L5_WED_5", 5, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "التربية الإسلامية", "السيرة النبوية", 30),
        OfficialTimetableSlot("L5_WED_6", 5, DayOfWeekAr.MERCREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "التربية المدنية", "السلوك المدني / المهارات الحياتية", 30),
        OfficialTimetableSlot("L5_WED_7", 5, DayOfWeekAr.MERCREDI, TimetablePeriodType.AFTERNOON, 7, "التاريخ", "التاريخ", 45),
        OfficialTimetableSlot("L5_WED_8", 5, DayOfWeekAr.MERCREDI, TimetablePeriodType.AFTERNOON, 8, "الرياضيات", "الهندسة", 45),
        OfficialTimetableSlot("L5_WED_9", 5, DayOfWeekAr.MERCREDI, TimetablePeriodType.AFTERNOON, 9, "التربية البدنية", "التربية البدنية", 30),

        // الخميس (Jeudi)
        OfficialTimetableSlot("L5_THU_1", 5, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "اللغة العربية", "القراءة والفهم", 45),
        OfficialTimetableSlot("L5_THU_2", 5, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "اللغة العربية", "التعبير", 45),
        OfficialTimetableSlot("L5_THU_3", 5, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "الرياضيات", "القياس", 30),
        OfficialTimetableSlot("L5_THU_4", 5, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_AFTER_RECESS, 4, "التربية الإسلامية", "القرآن الكريم", 30),
        OfficialTimetableSlot("L5_THU_5", 5, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "الرياضيات", "الحساب", 45),
        OfficialTimetableSlot("L5_THU_6", 5, DayOfWeekAr.JEUDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "التربية العلمية والتكنولوجية", "تقنيات الإعلام والاتصال", 30),
        OfficialTimetableSlot("L5_THU_7", 5, DayOfWeekAr.JEUDI, TimetablePeriodType.AFTERNOON, 7, "Français", "Compréhension de l'écrit", 45, isFrench = true),
        OfficialTimetableSlot("L5_THU_8", 5, DayOfWeekAr.JEUDI, TimetablePeriodType.AFTERNOON, 8, "Français", "Production de l'écrit", 45, isFrench = true),
        OfficialTimetableSlot("L5_THU_9", 5, DayOfWeekAr.JEUDI, TimetablePeriodType.AFTERNOON, 9, "Français", "Compréhension de l'oral", 30, isFrench = true),

        // الجمعة (Vendredi)
        OfficialTimetableSlot("L5_FRI_1", 5, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 1, "Français", "Compréhension de l'écrit", 45, isFrench = true),
        OfficialTimetableSlot("L5_FRI_2", 5, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 2, "Français", "Production de l'écrit", 45, isFrench = true),
        OfficialTimetableSlot("L5_FRI_3", 5, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_BEFORE_RECESS, 3, "Français", "Production de l'oral", 30, isFrench = true),
        OfficialTimetableSlot("L5_FRI_4", 5, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 4, "التربية الفنية", "الرسم / الأعمال اليدوية", 30),
        OfficialTimetableSlot("L5_FRI_5", 5, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 5, "العلوم", "الصحة والتغذية", 45),
        OfficialTimetableSlot("L5_FRI_6", 5, DayOfWeekAr.VENDREDI, TimetablePeriodType.MORNING_AFTER_RECESS, 6, "اللغة العربية", "الإملاء والخط العربي", 30)
    )

    private fun getLevel6Slots(): List<OfficialTimetableSlot> {
        return level5Slots.map { slot ->
            slot.copy(
                id = slot.id.replace("L5_", "L6_"),
                level = 6
            )
        }
    }

    /**
     * Returns the active official timetable slots for any level (1..6).
     */
    fun getTimetableForLevel(level: Int): List<OfficialTimetableSlot> {
        return when (level) {
            1 -> level1Slots
            2 -> level2Slots
            3 -> getLevel3And4Slots(3)
            4 -> getLevel3And4Slots(4)
            5 -> level5Slots
            6 -> getLevel6Slots()
            else -> emptyList()
        }
    }

    /**
     * Weekly slot count for a given level
     */
    fun getWeeklySlotCount(level: Int): Int = getTimetableForLevel(level).size

    /**
     * Weekly teaching minutes sum for a given level
     */
    fun getWeeklyTeachingMinutes(level: Int): Int = getTimetableForLevel(level).sumOf { it.durationMinutes }

    /**
     * Annual teaching sessions (across 31 actual instructional weeks)
     */
    fun getAnnualTeachingSessionsCount(level: Int): Int = getWeeklySlotCount(level) * 31

    /**
     * Total annual teaching sessions across all active levels (1..5)
     */
    fun getTotalAnnualTeachingSessionsAllLevels(): Int {
        return (1..5).sumOf { getAnnualTeachingSessionsCount(it) }
    }

    /**
     * Group timetable slots by day in official sequence (Lundi to Vendredi)
     */
    fun getTimetableGroupedByDay(level: Int): Map<DayOfWeekAr, List<OfficialTimetableSlot>> {
        val slots = getTimetableForLevel(level)
        val map = linkedMapOf<DayOfWeekAr, List<OfficialTimetableSlot>>()
        DayOfWeekAr.values().forEach { day ->
            val daySlots = slots.filter { it.day == day }.sortedBy { it.slotIndex }
            if (daySlots.isNotEmpty()) {
                map[day] = daySlots
            }
        }
        return map
    }

    /**
     * Get slots for a specific day
     */
    fun getSlotsForDay(level: Int, day: DayOfWeekAr): List<OfficialTimetableSlot> {
        return getTimetableForLevel(level).filter { it.day == day }.sortedBy { it.slotIndex }
    }

    /**
     * Calculate total duration in minutes for a specific day
     */
    fun getDayTotalMinutes(level: Int, day: DayOfWeekAr): Int {
        return getSlotsForDay(level, day).sumOf { it.durationMinutes }
    }

    /**
     * Checks if a lesson slot matches the new timetable
     */
    fun getMatchingSlotsForSubject(level: Int, subject: String, branch: String = ""): List<OfficialTimetableSlot> {
        val slots = getTimetableForLevel(level)
        return slots.filter { slot ->
            slot.subject.contains(subject, ignoreCase = true) ||
            subject.contains(slot.subject, ignoreCase = true) ||
            (branch.isNotBlank() && (slot.branch.contains(branch, ignoreCase = true) || branch.contains(slot.branch, ignoreCase = true)))
        }
    }
}
