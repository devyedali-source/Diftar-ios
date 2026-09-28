package com.example.data.planning

import com.example.compat.*
import kotlinx.coroutines.IO

object LessonProposalRepository {

    /**
     * Classifies a lesson item into its explicit pedagogical category.
     */
    fun classifyLessonItem(lesson: AnnualLessonItem, week: Int = lesson.week): LessonItemType {
        val type = lesson.type.trim().uppercase()
        val title = lesson.title.trim()
        val subject = lesson.subject.trim()

        if (type == "HOLIDAY" || title.contains("عطلة") || subject.contains("عطلة")) {
            return LessonItemType.HOLIDAY
        }
        if (type == "EXAM" || title.contains("اختبار") || subject.contains("اختبار") ||
            title.contains("امتحان") || subject.contains("امتحان") ||
            title.contains("مسابقة") || subject.contains("مسابقة") ||
            title.contains("مداولات") || subject.contains("مداولات") ||
            title.contains("فرز النتائج") || title.contains("اختتام السنة")) {
            return LessonItemType.EXAM
        }
        if (type == "INTEGRATION_ASSESSMENT_REMEDIATION" || type == "DIAGNOSTIC" ||
            title.contains("إدماج") || subject.contains("إدماج") ||
            title.contains("تقويم") || subject.contains("تقويم") ||
            title.contains("علاج") || subject.contains("علاج") ||
            title.contains("تشخيص") || subject.contains("تشخيص") ||
            title.contains("محطة") || subject.contains("محطة")) {
            return LessonItemType.PEDAGOGICAL_STATION
        }
        return LessonItemType.REGULAR_LESSON
    }

    /**
     * Returns the unique identifier for a weekly lesson instance.
     */
    fun getWeeklyInstanceId(lesson: AnnualLessonItem, week: Int): String {
        return "${lesson.id}_w${week}"
    }

    /**
     * Breaks down any multi-week AnnualLessonItem into its distinct WeeklyLessonInstances.
     */
    fun getWeeklyInstances(lesson: AnnualLessonItem): List<WeeklyLessonInstance> {
        val startW = lesson.week
        val endW = lesson.weekEnd.coerceAtLeast(startW)
        val totalSpan = endW - startW + 1
        val itemType = classifyLessonItem(lesson, startW)

        return (startW..endW).mapIndexed { index, w ->
            WeeklyLessonInstance(
                instanceId = getWeeklyInstanceId(lesson, w),
                lesson = lesson,
                week = w,
                subWeekIndex = index,
                totalWeeksSpan = totalSpan,
                itemType = itemType
            )
        }
    }

    /**
     * Determines whether an item is an actual instructional lesson
     * (excluding pedagogical stations, holidays and official exams).
     */
    fun isActualLesson(lesson: AnnualLessonItem): Boolean {
        val cat = classifyLessonItem(lesson)
        return cat == LessonItemType.REGULAR_LESSON || cat == LessonItemType.PEDAGOGICAL_STATION
    }

    /**
     * Checks if the item is an evaluation / integration / diagnostic station
     */
    fun isSpecialStation(lesson: AnnualLessonItem): Boolean {
        val cat = classifyLessonItem(lesson)
        return cat == LessonItemType.PEDAGOGICAL_STATION || cat == LessonItemType.EXAM || cat == LessonItemType.HOLIDAY
    }

    /**
     * Generates a comprehensive, authentic pedagogical lesson proposal for any lesson across Levels 1-5,
     * taking into account multi-week progression, subject-specific methodologies, and exact time budgeting.
     */
    fun getProposalForLesson(lesson: AnnualLessonItem, selectedWeek: Int = lesson.week): LessonProposalData {
        val custom = getCustomSampleLessonProposal(lesson, selectedWeek)
        if (custom != null) return custom

        val levelTitle = getLevelTitle(lesson.level)
        val sectionModule = getSectionModule(lesson, selectedWeek)
        val domain = OfficialPlanningHierarchyAdapter.getCleanDomain(lesson).ifBlank { "المجال التعليمي العام" }
        val subject = lesson.subject
        val topic = lesson.title

        val currentWeekProgression = computeWeekProgression(lesson, selectedWeek)
        val sessionInfo = getSessionInfo(lesson, selectedWeek)
        val durationText = getDurationText(lesson)
        val referenceText = getReferenceBook(lesson)
        val teachingAids = getTeachingAids(lesson, selectedWeek)
        val specificObjective = getSpecificObjective(lesson, selectedWeek, currentWeekProgression)
        val procedureSteps = getProcedureSteps(lesson, selectedWeek, durationText)

        return LessonProposalData(
            levelTitle = levelTitle,
            domain = domain,
            sectionModule = sectionModule,
            subject = subject,
            topic = topic,
            currentWeekSubPhase = currentWeekProgression,
            sessionInfo = sessionInfo,
            durationText = durationText,
            referenceText = referenceText,
            teachingAids = teachingAids,
            specificObjective = specificObjective,
            procedureSteps = procedureSteps
        )
    }

    private fun getCustomSampleLessonProposal(lesson: AnnualLessonItem, selectedWeek: Int): LessonProposalData? {
        val topic = lesson.title.trim()
        val levelTitle = getLevelTitle(lesson.level)
        val sectionModule = getSectionModule(lesson, selectedWeek)
        val currentWeekProgression = computeWeekProgression(lesson, selectedWeek)
        val sessionInfo = getSessionInfo(lesson, selectedWeek)
        val durationText = getDurationText(lesson)

        // 1. Math Lesson: "الأعداد إلى 699"
        if (topic == "الأعداد إلى 699") {
            return LessonProposalData(
                levelTitle = levelTitle,
                domain = "الحساب",
                sectionModule = sectionModule,
                subject = "الرياضيات",
                topic = "الأعداد إلى 699",
                currentWeekSubPhase = currentWeekProgression,
                sessionInfo = sessionInfo,
                durationText = durationText,
                referenceText = "دليل معطيات كتاب الرياضيات للسنة الثالثة ابتدائي (المنهاج الموريتاني الرسمي)",
                teachingAids = "جدول مراتب الأعداد (مئات، عشرات، آحاد)، بطاقات الأعداد الملونة، الألواح الفردية، المعداد الحسابي الملموس، والقطع والصفائح البلاستيكية الممثلة للوحدات والعشرات والمئات.",
                specificObjective = "أن يكون التلميذ في نهاية الحصة قادراً على قراءة الأعداد إلى 699 وكتابتها بالأرقام وبالحروف، وتفكيكها إلى مئات وعشرات وآحاد ومقارنتها وترتيبها بدقة.",
                procedureSteps = listOf(
                    LessonProcedureStep(
                        phaseName = "1. التقديم (التهيئة والانطلاق)",
                        teacherActivity = "يبدأ المعلم بنشاط الحساب الذهني السريع: يطلب كتابة العدد (354) على الألواح، وتفكيكه ذهنياً إلى مئات وعشرات وآحاد. بعد ذلك يعرض وضعية انطلاق ملموسة: 'اشترى مزارع موريتاني 6 مئات و8 عشرات و9 وحدات من النخيل لغرسها في واحة آدرار. اكتب العدد الإجمالي للفسائل بالأرقام وبالحروف'.",
                        studentActivity = "ينجز المتعلمون الحساب الذهني على ألواحهم ويرفعونها فوراً. يتأملون وضعية الانطلاق، يحللون المعطيات (6 مئات، 8 عشرات، 9 آحاد)، ويقترحون العدد 689.",
                        durationText = "10 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "2. تنمية التعلم (بناء المفهوم والاستكشاف)",
                        teacherActivity = "يوجه المعلم التلاميذ لتمثيل العدد 689 في جدول المراتب على السبورة. يطرح الأسئلة: 'كم مائة لدينا؟ ما رقم العشرات؟ ما رقم الآحاد؟ كيف نفكك هذا العدد نموذجياً؟'. يدربهم على قراءة العدد باتباع الترتيب البيداغوجي (آحاد ثم عشرات ثم مئات)، ثم يكتبون صيغاً أخرى مثل: 689 = 600 + 80 + 9. يطرح تحدياً بمقارنته مع العدد 698.",
                        studentActivity = "يشارك المتعلمون في الصعود للسبورة لتمثيل المراتب، ويجيبون: 'لدينا 6 مئات، 8 عشرات، و9 آحاد'. يقرؤون ويكتبون العدد بالحروف 'ستمائة وتسعة وثمانون'. ينجزون التفكيك النموذجي على كراساتهم، ويقارنون 689 < 698 مستنتجين أن مقارنة المئات متساوية فننتقل لمقارنة العشرات (8 < 9).",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "3. التطبيق (التقويم والاستثمار)",
                        teacherActivity = "يطرح المعلم تمريناً تقييمياً مستهدفاً: 'اكتب الأعداد التالية بالحروف أو بالأرقام: 642، 509، ستمائة وسبعة وسبعون. ثم رتبها تصاعدياً'. يدور بين الصفوف لتقديم معالجة بيداغوجية فورية للمتعثرين في الانتقال بين التعبير الحرفي والرمزي.",
                        studentActivity = "يحل التلاميذ التمرين فردياً على كراس القسم بتركيز وهدوء. يدونون الحلول: 'ستمائة واثنان وأربعون، خمسمائة وتسعة، 677'، ثم يرتبونها تصاعدياً: 509 < 642 < 677. يشاركون في التصحيح الجماعي الذاتي.",
                        durationText = "15 دقيقة"
                    )
                )
            )
        }

        // 2. Arabic Lesson: "ـ نصوص نثرية وشعرية تتعلق بالقيم الإسلامية والوطنية - مخارج الحروف."
        if (topic.contains("نصوص نثرية وشعرية تتعلق بالقيم الإسلامية والوطنية")) {
            return LessonProposalData(
                levelTitle = levelTitle,
                domain = "القراءة والفهم",
                sectionModule = sectionModule,
                subject = "اللغة العربية",
                topic = "ـ نصوص نثرية وشعرية تتعلق بالقيم الإسلامية والوطنية - مخارج الحروف.",
                currentWeekSubPhase = currentWeekProgression,
                sessionInfo = sessionInfo,
                durationText = durationText,
                referenceText = "كتاب قراءتي للغة العربية - السنة الثالثة ابتدائي (المنهاج الموريتاني الوطني)",
                teachingAids = "لوحة نصية مكبرة مكتوبة بخط واضح، بطاقات المفردات (الوطن، العلم، النشيد)، صور تعبيرية تجسد التضامن الاجتماعي وحب الوطن، وجهاز التسجيل الصوتي لسماع القراءة النموذجية.",
                specificObjective = "أن يكون التلميذ في نهاية الحصة قادراً على قراءة النص النثري قراءة مسترسلة ومعبرة مع مراعاة مخارج الحروف الصحيحة، واستيعاب المفردات المرتبطة بالقيم الوطنية، واستخراج الفكرة العامة للنص.",
                procedureSteps = listOf(
                    LessonProcedureStep(
                        phaseName = "1. التقديم (التهيئة والانطلاق)",
                        teacherActivity = "يعرض المعلم صورة العلم الوطني الموريتاني ويردد البيتين الأولين من النشيد الوطني، ثم يطرح أسئلة تمهيدية: 'ما لون علمنا الوطني؟ وإلى ماذا يرمز؟ كيف نساهم في خدمة وطننا الحبيب؟'. يدون عنوان الدرس على السبورة تمهيداً لعرض النص.",
                        studentActivity = "يشاهد المتعلمون الصورة بحماس ويجيبون: 'أخضر يتوسطه هلال ونجم ذهبيان، يرمز للدين الإسلامي والسلام والأمل'. يعبرون عن حبهم للوطن بعبارات مبسطة ويستعدون لقراءة النص.",
                        durationText = "10 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "2. تنمية التعلم (بناء المفهوم والاستكشاف)",
                        teacherActivity = "يؤدي المعلم قراءة نموذجية جهورية معبرة للنص مع الضبط بالشكل ومراعاة مخارج الحروف وخاصة حروف الإطباق والتنبيه على التنوين. يدعو التلاميذ المميزين ثم البقية للقراءة الفردية الهمسية والجهرية. يشرح المفردات الصعبة كـ (التآزر، التماسك، الممتلكات) ويستخرج معهم فكرة النص الأساسية وهي أهمية حب الوطن وتطبيق القيم النبيلة.",
                        studentActivity = "يستمع المتعلمون بخشوع للقراءة النموذجية، يتابعون بأصابعهم على كراساتهم، ويقومون بالقراءة الفردية محاكين مخارج الحروف السليمة. يشاركون في شرح المفردات بالمرادف والضد، ويستنتجون أن الوطن يبنى بتضامن أبنائه وصيانة ممتلكاته العامة.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "3. التطبيق (التقويم والاستثمار)",
                        teacherActivity = "يكلف المعلم التلاميذ بالإجابة عن نشاط الفهم والإنتاج على الكراس: 'ابحث في النص عن مرادف كلمة (مساعدة) وضد كلمة (تفرق). اكتب جملة مفيدة تحث فيها زملاءك على احترام العلم الوطني وممتلكات المدرسة مع مراعاة الضبط الصحيح للحروف'.",
                        studentActivity = "ينفذ المتعلمون المطلوب فردياً في كراساتهم مستخرجين: مرادف مساعدة (تآزر/عون)، ضد تفرق (تماسك/وحدة). يكتبون جملة مفيدة معبرة مثل: 'يجب علينا أن نحافظ على طاولات مدرستنا احتراما للوطن'، ثم يشاركون في تصحيح الأخطاء الإملائية والقرائية.",
                        durationText = "15 دقيقة"
                    )
                )
            )
        }

        // 3. Quran Lesson: "سورة البلد"
        if (topic == "سورة البلد") {
            return LessonProposalData(
                levelTitle = levelTitle,
                domain = "القرآن الكريم",
                sectionModule = sectionModule,
                subject = "التربية الإسلامية",
                topic = "سورة البلد",
                currentWeekSubPhase = currentWeekProgression,
                sessionInfo = sessionInfo,
                durationText = durationText,
                referenceText = "كتاب التربية الإسلامية للسنة الثالثة ابتدائي - المعهد التربوي الوطني الموريتاني",
                teachingAids = "المصحف المدرسي، اللوحة المكتوبة للآيات مصحوبة بعلامات الضبط والتجويد، تسجيل صوتي لأحد القراء المشاهير، وصور توضيحية لمعالم مكة المكرمة والبلد الحرام لتوضيح سياق القسم.",
                specificObjective = "أن يكون التلميذ في نهاية الحصة قادراً على تلاوة الآيات (1-10) من سورة البلد تلاوة مجودة خاشعة، وحفظها حفظاً صحيحاً خاضعاً لأحكام التجويد الأساسية، واستخلاص دلالة القسم الإلهي بمكة المكرمة وقدرة الخالق.",
                procedureSteps = listOf(
                    LessonProcedureStep(
                        phaseName = "1. التقديم (التهيئة والانطلاق)",
                        teacherActivity = "يبدأ المعلم بتمهيد وجداني مفعم بالإيمان: 'ما هو البلد الحرام المذكور في القرآن الكريم الذي ولد فيه نبينا محمد صلى الله عليه وسلم وتحج إليه القلوب؟'. يربط الإجابات بموضوع السورة، ويسجل عنوان الدرس 'سورة البلد' على السبورة بخط رقعة متميز.",
                        studentActivity = "يتفاعل التلاميذ بروح إيمانية ويجيبون: 'مكة المكرمة والبيت الحرام'. يستمعون باهتمام للتمهيد ويبدون استعدادهم لتلقي كلام الله وتلاوته بخشوع.",
                        durationText = "10 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "2. تنمية التعلم (بناء المفهوم والاستكشاف)",
                        teacherActivity = "يتلو المعلم الآيات الأولى من السورة تلاوة نموذجية مرتلة ببطء وخشوع مراعياً مخارج الحروف والقلقلة والمدود. يعيد تلاوتها مع ترديد جماعي من التلاميذ، ثم يشرع في تسميع فردي للمتعلمين. يشرح معاني الآيات مثل: (لا أقسم بهذا البلد) أي مكة الحبيبة، (لقد خلقنا الإنسان في كبد) أي في مشقة وتعب لمواجهة الابتلاء، (ألم نجعل له عينين ولساناً وشفتين) للتدبر وشكر النعم.",
                        studentActivity = "ينصت المتعلمون بإنصات وخشوع تام لأداء المعلم والتسجيل الصوتي، يرددون جماعياً بدقة تامة محاكين الغنن والمدود والقلقلة. يتلون الآيات فرادى مصححين مخارج الحروف، ويتدبرون النعم الإلهية المستخلصة وأهمية العمل الصالح واجتياز العقبة.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "3. التطبيق (التقويم والاستثمار)",
                        teacherActivity = "يطلب المعلم من التلاميذ استظهار الآيات الخمس الأولى غيباً على الألواح أو شفاهة للتأكد من رسوخ الحفظ وصحته. يطرح سؤالاً تقييمياً سلوكياً: 'ما هي أهم النعم التي وهبنا الله إياها في هذه السورة؟ وكيف نشكره عليها؟'.",
                        studentActivity = "يستظهر المتعلمون الآيات غيباً أمام زملائهم بخشوع وبلا تردد. يجيبون عن السؤال التقييمي: 'نعم العينين واللسان والشفتين والهداية لسبيل الخير، ونشكره باستعمالها في طاعته ومساعدة الفقراء والمحتاجين'. يدونون السورة في دفاترهم مسبوغة بالتشكيل الإملائي الصحيح.",
                        durationText = "15 دقيقة"
                    )
                )
            )
        }

        // 4. Civics Lesson: "الممتلكات المدرسية"
        if (topic == "الممتلكات المدرسية" || topic == "الحفاظ على الممتلكات المدرسية") {
            return LessonProposalData(
                levelTitle = levelTitle,
                domain = "المواطنة",
                sectionModule = sectionModule,
                subject = "التربية على المواطنة",
                topic = "الممتلكات المدرسية",
                currentWeekSubPhase = currentWeekProgression,
                sessionInfo = sessionInfo,
                durationText = durationText,
                referenceText = "منهاج التربية على المواطنة للسنة الثالثة ابتدائي - المعهد التربوي الوطني الموريتاني",
                teachingAids = "مجموعة من الصور المقارنة (فصل منظم بطاولات نظيفة وسبورة ممسوحة مقابل فصل مهمل ذي طاولات مكسرة وجدران مشوهة بالكتابة)، ميثاق مكتوب يعبر عن التزامات الحفاظ على ممتلكات الصف، وملصقات تشجيعية.",
                specificObjective = "أن يكون التلميذ في نهاية الحصة قادراً على تحديد مكونات الممتلكات المدرسية كطاولات الدراسة والكتب والسبورات وجدران الفصول والمرافق الصحية، وتوضيح مسؤوليتنا وواجبنا الوطني والأخلاقي في صيانتها والمحافظة عليها لخدمة الأجيال المتعاقبة.",
                procedureSteps = listOf(
                    LessonProcedureStep(
                        phaseName = "1. التقديم (التهيئة والانطلاق)",
                        teacherActivity = "يوجه المعلم انتباه التلاميذ إلى الفصل المحيط بهم ويطرح أسئلة بيداغوجية: 'من أين نحصل على هذه الطاولات والكتب والسبورات؟ من يملكها؟ هل هي ملك لشخص واحد أم هي ملكية عامة لجميع التلاميذ؟'. يسجل الإجابات على السبورة ويصوغ موضوع الدرس 'الممتلكات المدرسية والمحافظة عليها'.",
                        studentActivity = "يلتفت المتعلمون حولهم، يتأملون طاولاتهم وكتبهم ويجيبون: 'توفرها لنا الدولة الموريتانية، وهي ملك لجميع التلاميذ الذين يدرسون والذين سيدرسون بعدنا، وليست ملكاً خاصاً'.",
                        durationText = "10 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "2. تنمية التعلم (بناء المفهوم والاستكشاف)",
                        teacherActivity = "يعرض المعلم صورتين متناقضتين لفصلين ويقود نقاشاً جماعياً: 'أي الفصلين تفضل الدراسة فيه؟ لماذا؟ كيف تشوه جدران المدرسة بالكتابة وطاولاتها بالتخريب؟'. يشرح مفهوم (الملكية العامة) وأن تخريب الممتلكات المدرسية يعيق تحصيلنا العلمي ويخالف تعاليم ديننا وقيم المواطنة الصالحة. يدون خلاصة الدرس: 'المحافظة على الممتلكات المدرسية واجب ديني ووطني، فمدرستي بيتي الثاني وصيانة أثاثها يضمن استمرار خدماتها التربوية'.",
                        studentActivity = "يقارن المتعلمون الصورتين بتمعن: 'نفضل الفصل المنظم والنظيف لأنه يفتح النفس للدراسة'. يستنتجون الأثر السلبي للتخريب ويعدون قائمة بمظاهر الإهمال ويقترحون سلوكيات بديلة وإيجابية كالنظافة وتجنب الحك على الخشب والطلاء، ثم ينقلون خلاصة الدرس على الكراسات بخط منسق وبترتيب بيداغوجي سليم.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "3. التطبيق (التقويم والاستثمار)",
                        teacherActivity = "يقدم المعلم نشاطاً تطبيقياً تشاركياً: 'صغ ميثاقاً من ثلاث قواعد لحماية ممتلكات فصلنا وصنف السلوكيات التالية إلى (حافظ/مخرب): الكتابة على الجدران، ترتيب الكراسي، رمي الأوراق في سلة المهملات، خدش الطاولات بالمسامير'.",
                        studentActivity = "يتعاون التلاميذ في مجموعات لصياغة ميثاق الفصل (مثال: 'لا نكتب على الطاولات، نحافظ على نظافة الجدران، ونرتب كراسينا بحب'). يصنفون السلوكيات فردياً على الكراس، ويلتزمون بممارستها عملياً في حياتهم المدرسية اليومية.",
                        durationText = "15 دقيقة"
                    )
                )
            )
        }

        // 5. Science Lesson: "ـ اللحوم والألبان"
        if (topic.contains("اللحوم والألبان")) {
            return LessonProposalData(
                levelTitle = levelTitle,
                domain = "الغذاء والصحة",
                sectionModule = sectionModule,
                subject = "النشاط العلمي",
                topic = "ـ اللحوم والألبان",
                currentWeekSubPhase = currentWeekProgression,
                sessionInfo = sessionInfo,
                durationText = durationText,
                referenceText = "كتاب النشاط العلمي للسنة الثالثة ابتدائي - المعهد التربوي الوطني الموريتاني",
                teachingAids = "صور فوتوغرافية لمنتجات اللحوم والألبان المتوفرة محلياً (الحليب الطازج، اللبن، الزبدة، القشطة، لحم الإبل، الغنم، البقر، الدجاج والسمك)، هرم بيداغوجي للأغذية ومصادرها، وملصقات المجموعات الغذائية.",
                specificObjective = "أن يكون التلميذ في نهاية الحصة قادراً على تصنيف اللحوم والألبان كأغذية من مصدر حيواني، وتوضيح قيمتها الغذائية لبناء ونمو الجسم الموريتاني وحمايته من الأمراض، وذكر المنتجات الأساسية المشتقة من الحليب واللحوم في البيئة الموريتانية.",
                procedureSteps = listOf(
                    LessonProcedureStep(
                        phaseName = "1. التقديم (التهيئة والانطلاق)",
                        teacherActivity = "يمهد المعلم بطرح سؤال صحي حافز: 'ماذا شربتم وتناولتم في وجبة الإفطار هذا الصباح؟ من أين نحصل على الحليب واللحوم التي تزين موائدنا الموريتانية؟'. يجمع الإجابات ويربطها بأهمية الغذاء المتوازن لنمو العضلات والعظام، ويكتب العنوان 'ـ اللحوم والألبان' على السبورة.",
                        studentActivity = "يجيب التلاميذ بحيوية: 'شربنا حليباً طازجاً، وتناولنا وجبة فيها لحم أو مشتقات الحليب'. يحددون مصدرها وهو الحيوانات (الإبل، البقر، الغنم، الدجاج، الأسماك) التي تشتهر بها بلادنا ويستعدون للدرس.",
                        durationText = "10 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "2. تنمية التعلم (بناء المفهوم والاستكشاف)",
                        teacherActivity = "يعرض المعلم صور اللحوم والألبان ويطرح التساؤل العلمي: 'ماذا يحدث لأجسامنا إذا لم نأكل اللحوم ولا نشرب الألبان؟ ما هي مشتقات الحليب التي يمكننا صناعتها محلياً؟'. يشرح بالتفصيل دور البروتينات والكالسيوم في بناء العضلات والأسنان القوية. يدون الخلاصة: 'اللحوم والألبان أغذية بانية من مصدر حيواني، تمد أجسامنا بالبروتينات الضرورية للنمو والكالسيوم لتقوية العظام، ويجب علينا تناولها بانتظام ونظافة لضمان صحتنا'.",
                        studentActivity = "يتأمل المتعلمون الصور ويفكرون: 'إذا لم نتناولها سنصبح ضعفاء وتصيبنا الأمراض وتتأثر عظامنا وأسناننا'. يستعرضون مشتقات الحليب المحلية (اللبن، الزبدة، الدهن، الدهن الحر، الجبن) ويستنتجون دورها البنائي الهام، ثم ينقلون الخلاصة في دفاترهم بخط واضح وجميل.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "3. التطبيق (التقويم والاستثمار)",
                        teacherActivity = "يطرح المعلم تمرين التحقق البيداغوجي: 'صنف الأغذية التالية في جدول حسب مصدرها الحيواني والنباتي: لحم إبل، حليب بقر، تمر، خبز قمح، سمك، زبدة، جزر. واذكر فائدة واحدة لتناول الحليب واللحوم في بناء جسمك'.",
                        studentActivity = "ينجز التلاميذ التمرين فردياً على الكراس بتنسيق منظم: يصنفون الأغذية الحيوانية (لحم إبل، حليب بقر، سمك، زبدة) والأغذية النباتية (تمر، خبز قمح، جزر). يكتبون الفائدة: 'تساعد اللحوم والألبان في نمو جسمي وتقوية عظامي وأسناني'، ويصححون كراساتهم بنشاط وتفاعل صفي ممتاز.",
                        durationText = "15 دقيقة"
                    )
                )
            )
        }

        return null
    }

    private fun getLevelTitle(level: Int): String {
        return when (level) {
            1 -> "المستوى الأول (السنة الأولى ابتدائي)"
            2 -> "المستوى الثاني (السنة الثانية ابتدائي)"
            3 -> "المستوى الثالث (السنة الثالثة ابتدائي)"
            4 -> "المستوى الرابع (السنة الرابعة ابتدائي)"
            5 -> "المستوى الخامس (السنة الخامسة ابتدائي)"
            6 -> "المستوى السادس (السنة السادسة ابتدائي)"
            else -> "المستوى $level"
        }
    }

    private fun getSectionModule(lesson: AnnualLessonItem, week: Int): String {
        if (lesson.isFrench) {
            return when (week) {
                in 1..5 -> "Projet 1 : Vie scolaire et amitié"
                in 6..9 -> "Projet 2 : Famille et environnement proche"
                in 10..13 -> "Projet 3 : Métiers, sciences et découvertes"
                in 14..17 -> "Projet 4 : Histoire, voyages et culture"
                in 18..21 -> "Projet 5 : Protection de la nature et écologie"
                in 22..25 -> "Projet 6 : Santé, nutrition et hygiène"
                in 26..30 -> "Projet 7 : Sports, arts et citoyenneté"
                else -> "Projet 8 : Bilan, synthèse et évaluation finale"
            }
        }

        return when (week) {
            in 1..5 -> "المقطع 1 / الوحدة 1: مدرستي وبيئتي والاندماج الاجتماعي"
            in 6..9 -> "المقطع 2 / الوحدة 2: الأسرة والحي والمحيط القريب"
            in 10..13 -> "المقطع 3 / الوحدة 3: التغذية والصحة والنشاط البدني"
            in 14..17 -> "المقطع 4 / الوحدة 4: الطبيعة والمناخ والبيئة وحمايتها"
            in 18..21 -> "المقطع 5 / الوحدة 5: العلوم والتكنولوجيا والمهن والابتكار"
            in 22..25 -> "المقطع 6 / الوحدة 6: الوطن والهوية والتراث التاريخي"
            in 26..30 -> "المقطع 7 / الوحدة 7: الفنون والرياضة والتواصل الإنساني"
            else -> "المقطع 8 / الوحدة 8: الإدماج الشامل والحصيلة العامة"
        }
    }

    fun computeWeekProgression(lesson: AnnualLessonItem, selectedWeek: Int): String {
        if (lesson.week == lesson.weekEnd) {
            return if (lesson.isFrench) "Séance hebdomadaire ciblée (Semaine $selectedWeek)"
            else "حصة أسبوعية مستقلة (الأسبوع $selectedWeek)"
        }

        val totalSpan = (lesson.weekEnd - lesson.week + 1).coerceAtLeast(1)
        val wIndex = (selectedWeek - lesson.week).coerceIn(0, totalSpan - 1)

        if (lesson.isFrench) {
            return when (totalSpan) {
                4 -> when (wIndex) {
                    0 -> "Semaine $selectedWeek (Étape 1/4) : Découverte, situation d'amorce et acquisition des notions de base"
                    1 -> "Semaine $selectedWeek (Étape 2/4) : Construction, analyse approfondie et règles de fonctionnement"
                    2 -> "Semaine $selectedWeek (Étape 3/4) : Entraînement systématique et exercices d'application guidée"
                    else -> "Semaine $selectedWeek (Étape 4/4) : Consolidation, intégration partielle et évaluation formative"
                }
                3 -> when (wIndex) {
                    0 -> "Semaine $selectedWeek (Étape 1/3) : Découverte et amorce de la notion"
                    1 -> "Semaine $selectedWeek (Étape 2/3) : Approfondissement et entraînement"
                    else -> "Semaine $selectedWeek (Étape 3/3) : Intégration et évaluation"
                }
                2 -> when (wIndex) {
                    0 -> "Semaine $selectedWeek (Étape 1/2) : Découverte et construction des apprentissages"
                    else -> "Semaine $selectedWeek (Étape 2/2) : Entraînement, application et synthèse"
                }
                else -> "Semaine $selectedWeek : Phase d'apprentissage continu (${wIndex + 1}/$totalSpan)"
            }
        }

        return when (totalSpan) {
            4 -> when (wIndex) {
                0 -> "الأسبوع $selectedWeek (المرحلة 1 من 4): الانطلاق والاستكشاف والتقديم الأولي للوحدة"
                1 -> "الأسبوع $selectedWeek (المرحلة 2 من 4): بناء وتعميق التعلم وتفكيك المهارات الأساسية"
                2 -> "الأسبوع $selectedWeek (المرحلة 3 من 4): التدريب والتطبيق الممنهج والأنشطة الإثرائية"
                else -> "الأسبوع $selectedWeek (المرحلة 4 من 4): التثبيت والإدماج الجزئي والتقويم المرحلي للوحدة"
            }
            3 -> when (wIndex) {
                0 -> "الأسبوع $selectedWeek (المرحلة 1 من 3): الانطلاق والاستكشاف وبناء المفاهيم"
                1 -> "الأسبوع $selectedWeek (المرحلة 2 من 3): التدريب والتعميق والتطبيق الموجه"
                else -> "الأسبوع $selectedWeek (المرحلة 3 من 3): التثبيت والإدماج والتقويم المرحلي"
            }
            2 -> when (wIndex) {
                0 -> "الأسبوع $selectedWeek (المرحلة 1 من 2): الانطلاق، الاستكشاف وبناء التعلمات والمفاهيم"
                else -> "الأسبوع $selectedWeek (المرحلة 2 من 2): التدريب، التطبيق الممنهج، التثبيت والتقويم"
            }
            else -> "الأسبوع $selectedWeek: محطة نشاط وتطبيق متدرج (${wIndex + 1} من $totalSpan)"
        }
    }

    private fun getSessionInfo(lesson: AnnualLessonItem, selectedWeek: Int): String {
        val sub = lesson.subject.lowercase()
        val totalSpan = (lesson.weekEnd - lesson.week + 1).coerceAtLeast(1)
        val wIndex = (selectedWeek - lesson.week).coerceIn(0, totalSpan - 1) + 1

        if (lesson.isFrench || sub.contains("français")) {
            return if (lesson.week == lesson.weekEnd) "Séance 1" else "Séance $wIndex/$totalSpan"
        }
        return if (lesson.week == lesson.weekEnd) "الحصة 1" else "الحصة $wIndex/$totalSpan"
    }

    private fun getDurationText(lesson: AnnualLessonItem): String {
        val slots = OfficialTimetableRepository.getMatchingSlotsForSubject(lesson.level, lesson.subject, lesson.domain)
        val duration = if (slots.isNotEmpty()) {
            slots.first().durationMinutes
        } else {
            when {
                lesson.level == 1 && (lesson.subject.contains("علوم") || lesson.domain.contains("صحة") || lesson.domain.contains("بيئة") || lesson.domain.contains("سلامة")) -> 15
                lesson.level == 1 && (lesson.subject.contains("مدنية") || lesson.domain.contains("مواطنة") || lesson.domain.contains("مهارات") || lesson.domain.contains("سلوك")) -> 20
                lesson.level == 2 && (lesson.subject.contains("علوم") || lesson.domain.contains("صحة") || lesson.domain.contains("بيئة") || lesson.domain.contains("سلامة")) -> 15
                lesson.level == 2 && (lesson.subject.contains("مدنية") || lesson.domain.contains("مواطنة") || lesson.domain.contains("مهارات") || lesson.domain.contains("سلوك")) -> 20
                lesson.level == 5 && lesson.subject.contains("جغرافيا") -> 30
                lesson.level in 3..4 -> 30
                else -> 45
            }
        }
        return if (lesson.isFrench || lesson.subject.contains("français", ignoreCase = true)) {
            "$duration minutes"
        } else {
            "$duration دقيقة"
        }
    }

    private fun getReferenceBook(lesson: AnnualLessonItem): String {
        val sub = lesson.subject.lowercase()
        return when {
            lesson.isFrench || sub.contains("français") -> "Manuel de français et Guide pédagogique officiel de l'enseignant (IPN)"
            sub.contains("رياضيات") -> "كتاب الرياضيات ودليل المعلم وكراس الأنشطة المعتمد من المعهد التربوي الوطني (IPN)"
            sub.contains("إسلامية") -> "كتاب التربية الإسلامية والمصحف الشريف ودليل المعلم (IPN)"
            sub.contains("عرب") -> "كتاب اللغة العربية وكراس التمارين والدليل البيداغوجي المعتمد (IPN)"
            sub.contains("نشاط") || sub.contains("علم") -> "كتاب التربية العلمية والتكنولوجية ودليل المعلم (IPN)"
            sub.contains("تاريخ") || sub.contains("جغراف") -> "كتاب التاريخ والجغرافيا والوثائق والخرائط المعتمدة (IPN)"
            sub.contains("مدنية") -> "كتاب التربية المدنية والدليل البيداغوجي (IPN)"
            sub.contains("فنية") || sub.contains("بدنية") -> "دليل الأنشطة البدنية والفنية والموسيقية للمعلم (IPN)"
            else -> "الكتاب المدرسي المعتمد والدليل البيداغوجي للمعلم (IPN)"
        }
    }

    private fun getTeachingAids(lesson: AnnualLessonItem, selectedWeek: Int): String {
        if (lesson.activities.isNotBlank() && lesson.activities != "أنشطة رسمية معتمدة") {
            return lesson.activities
        }
        val sub = lesson.subject.lowercase()
        return when {
            lesson.isFrench -> "Planches murales, étiquettes-mots, tableau, cahiers d'activités, ardoises"
            sub.contains("رياضيات") -> "الألواح، مجسمات هندسية، جدول المراتب، قطع وأعمدة الحساب، بطاقات الأرقام والعمليات"
            sub.contains("إسلامية") -> "المصحف الشريف، لوحات الآيات والأحاديث، صور مشيرة للسلوك القويم، بطاقات الاستظهار"
            sub.contains("عرب") -> "مشاهد مصورة معبرة، بطاقات الحروف والمفردات والتراكيب، السبورة، كراسات التلاميذ"
            sub.contains("نشاط") || sub.contains("علم") -> "عينات طبيعية، مجسمات تشريحية، أدوات قياس وتجريب، صور توضيحية مكبرة"
            sub.contains("تاريخ") || sub.contains("جغراف") -> "خرائط جغرافية حائطية، صور معالم تاريخية، نصوص وسندات مصورة، بوصلة"
            sub.contains("مدنية") -> "صور ورسوم تمثيلية للسلوك المدني، بطاقات الحقوق والواجبات، نصوص قانونية مبسطة"
            sub.contains("فنية") || sub.contains("بدنية") -> "أدوات رسم وألوان، كراسات الرسم، صافرة، كرات، حبال، أطواق"
            else -> "وسائل إيضاح، صور توضيحية، بطاقات وسندات تربوية"
        }
    }

    private fun getSpecificObjective(lesson: AnnualLessonItem, selectedWeek: Int, progression: String): String {
        val topic = lesson.title
        val sub = lesson.subject.lowercase()
        val totalSpan = (lesson.weekEnd - lesson.week + 1).coerceAtLeast(1)
        val wIndex = (selectedWeek - lesson.week).coerceIn(0, totalSpan - 1)

        // French Subject Objective
        if (lesson.isFrench || sub.contains("français")) {
            return if (lesson.week == lesson.weekEnd) {
                "Permettre à l'élève d'identifier, comprendre et mobiliser correctement les notions et structures de : ($topic) dans des situations de communication réelles."
            } else {
                when (wIndex) {
                    0 -> "Découvrir et identifier les notions clés de ($topic) à travers une situation d'amorce significative."
                    1 -> "Construire et approfondir les mécanismes et règles d'utilisation de : ${lesson.skills.ifBlank { topic }}."
                    2 -> "S'entraîner systématiquement sur les cahiers d'activités et consolider les acquis de : ($topic)."
                    else -> "Intégrer l'ensemble des compétences de l'unité ($topic) et réussir l'évaluation sommative : ${lesson.competency}."
                }
            }
        }

        // Integration / Evaluation stations
        if (topic.contains("إدماج") || topic.contains("تقويم") || topic.contains("علاج") || topic.contains("تشخيص")) {
            return "أن يكون التلميذ في نهاية الحصة قادراً على تعبئة موارده المكتسبة لمعالجة الوضعية التقويمية والإدماجية الخاصة بـ ($topic) وتشخيص التعثرات وتجاوزها."
        }

        val cleanComp = lesson.competency
            .replace(Regex("^K\\s*\\d+\\s*:?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^\\s*\\d+\\s*:?\\s*"), "")
            .replace(Regex("K\\s*(\\d+)", RegexOption.IGNORE_CASE)) {
                if (lesson.isFrench) "Compétence ${it.groupValues[1]}" else "الكفاية ${it.groupValues[1]}"
            }.trim()

        // Multi-week stage adaptation for Arabic subjects
        if (lesson.week != lesson.weekEnd) {
            return when (wIndex) {
                0 -> "أن يتعرف التلميذ في هذه الحصة على المفاهيم التأسيسية لـ ($topic) ويستكشف وضعية الانطلاق ويبدي تفاعلاً أولياً."
                1 -> "أن يبني التلميذ القواعد الأساسية لـ ($topic) ويفكك المهارات المستهدفة (${lesson.skills.ifBlank { "الفهم والتحليل" }}) ويدون الخلاصة."
                2 -> "أن يتدرب التلميذ على تطبيق قواعد ومهارات ($topic) من خلال إنجاز التمارين المتنوعة وأنشطة كراس النشاطات."
                else -> "أن يوظف التلميذ كافة مكتسبات ($topic) في حل وضعيات إدماجية مركبة وتحقيق الكفاية المعتمدة: $cleanComp."
            }
        }

        // Single-week specific objective
        if (cleanComp.isNotBlank()) {
            return "أن يكون التلميذ في نهاية الحصة قادراً على توظيف المعارف المتعلقة بـ ($topic) لتحقيق: $cleanComp."
        }

        return when {
            sub.contains("رياضيات") -> "أن يستوعب التلميذ مفهوم ($topic) ويتمكن من استخدامه في حل وضعيات حسابية أو هندسية بدقة."
            sub.contains("إسلامية") -> "أن يستوعب التلميذ المعاني الإيمانية والخلقية لدرس ($topic) ويطبقها في سلوكه اليومي واستظهاره."
            sub.contains("عرب") -> "أن يستوعب التلميذ المعارف والظواهر الأساسية لموضوع ($topic) ويوظف الصيغ والتراكيب في سياقات لغوية سليمة."
            sub.contains("نشاط") || sub.contains("علم") -> "أن يلاحظ التلميذ الظاهرة العلمية في ($topic) وينجز خطوات التقصي والاستكشاف ويستنتج الخلاصة."
            sub.contains("تاريخ") || sub.contains("جغراف") -> "أن يتعرف التلميذ على المفاهيم والأحداث في ($topic) ويحلل السندات والخرائط بدقة."
            sub.contains("مدنية") -> "أن يكتسب التلميذ السلوك المدني القويم المرتبط بـ ($topic) ويميز بين الحقوق والواجبات."
            else -> "أن يكون التلميذ قادراً على توظيف معارف مادة ${lesson.subject} الخاصة بموضوع ($topic) في وضعيات تعليمية مناسبة."
        }
    }

    private fun getProcedureSteps(
        lesson: AnnualLessonItem,
        selectedWeek: Int,
        durationText: String
    ): List<LessonProcedureStep> {
        val topic = lesson.title
        val sub = lesson.subject.lowercase()
        val skills = lesson.skills.ifBlank { "الفهم والتحليل والتطبيق" }
        val totalSpan = (lesson.weekEnd - lesson.week + 1).coerceAtLeast(1)
        val wIndex = (selectedWeek - lesson.week).coerceIn(0, totalSpan - 1)

        // 1. Integration / Remediation / Diagnostic stations
        if (topic.contains("إدماج") || topic.contains("تقويم") || topic.contains("علاج") || topic.contains("تشخيص")) {
            return listOf(
                LessonProcedureStep(
                    phaseName = "التقديم (التمهيد والتعليمات)",
                    teacherActivity = "التذكير السريع بالمعارف والمفاهيم المستهدفة بالتقويم والعلاج لموضوع ($topic)، وتوزيع أوراق النشاط وشرح معايير الإنجاز.",
                    studentActivity = "ينتبه التلاميذ للتوجيهات والتعليمات، ويستعدون لإنجاز الوضعية التقييمية والعلاجية بتركيز.",
                    durationText = "5 دقائق"
                ),
                LessonProcedureStep(
                    phaseName = "تنمية التعلم (الإنجاز الفردي والمعالجة الفارقية)",
                    teacherActivity = "متابعة إنجاز التلاميذ فردياً ورصد الصعوبات والتعثرات الشائعة. تقديم إرشادات علاجية مخصصة وموجهة للفئات المتعثرة دون تقديم الحل المباشر.",
                    studentActivity = "ينجز التلميذ الوضعية التقييمية فردياً، يستعين بالتوجيهات لتصحيح الأخطاء المفهومية وتجاوز الصعوبات بنجاح.",
                    durationText = "25 دقيقة"
                ),
                LessonProcedureStep(
                    phaseName = "التطبيق (التصحيح التفاعلي والحصيلة)",
                    teacherActivity = "إجراء التصحيح الجماعي التفاعلي على السبورة، وتدوين الملاحظات الفردية للتلاميذ وتأكيد المفاهيم المعالجة وتثبيت المكتسبات.",
                    studentActivity = "يشارك التلميذ في التصحيح الجماعي، يجري التصحيح الذاتي على كراسه ويدون الفوائد المكتسبة.",
                    durationText = "15 دقيقة"
                )
            )
        }

        // 2. French Subject Methodology (Français - LTR)
        if (lesson.isFrench || sub.contains("français")) {
            return when {
                lesson.week != lesson.weekEnd && wIndex == 0 -> listOf(
                    LessonProcedureStep(
                        phaseName = "1. Mise en train / Amorce",
                        teacherActivity = "Présenter le poster ou document visuel relatif au projet : ($topic). Poser des questions ouvertes d'amorce pour susciter l'intérêt et mobiliser les prérequis.",
                        studentActivity = "Observe attentivement le support, écoute la situation d'amorce et répond aux questions de découverte.",
                        durationText = "5 min"
                    ),
                    LessonProcedureStep(
                        phaseName = "2. Découverte et conceptualisation",
                        teacherActivity = "Lecture magistrale expressive du texte modèle. Guidage du repérage des notions cibles ($skills). Vérification de la compréhension globale et animation des lectures individuelles.",
                        studentActivity = "Suit sur son manuel, écoute la lecture modèle, effectue des lectures individuelles et repère les mots et structures cibles.",
                        durationText = "25 min"
                    ),
                    LessonProcedureStep(
                        phaseName = "3. Réinvestissement / Application",
                        teacherActivity = "Proposer un premier exercice oral et écrit d'application sur les ardoises/cahiers pour vérifier l'assimilation des notions découvertes.",
                        studentActivity = "Exécute l'exercice d'application sur son ardoise/cahier et participe à la correction collective immédiate.",
                        durationText = "15 min"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex == 1 -> listOf(
                    LessonProcedureStep(
                        phaseName = "1. Rappel et réactivation",
                        teacherActivity = "Faire un rappel rapide des notions découvertes la semaine précédente sur ($topic). Poser des questions de réactivation sur les ardoises.",
                        studentActivity = "Répond aux questions de rappel sur l'ardoise et réactive les notions acquises.",
                        durationText = "5 min"
                    ),
                    LessonProcedureStep(
                        phaseName = "2. Analyse approfondie et structuration",
                        teacherActivity = "Approfondir l'étude des faits de langue (grammaire, conjugaison, orthographe) : ($skills). Conduire les élèves à dégager et formuler la règle générale au tableau.",
                        studentActivity = "Participe activement à l'analyse, compare les structures, formule la règle avec ses propres mots et la copie sur son cahier.",
                        durationText = "25 min"
                    ),
                    LessonProcedureStep(
                        phaseName = "3. Entraînement guidé",
                        teacherActivity = "Faire réaliser une série d'exercices d'entraînement semi-guidés pour stabiliser les règles apprises.",
                        studentActivity = "Réalise les exercices d'entraînement individuellement avec rigueur et participe à la validation collective.",
                        durationText = "15 min"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex == 2 -> listOf(
                    LessonProcedureStep(
                        phaseName = "1. Consignes et cadrage des activités",
                        teacherActivity = "Expliquer clairement les consignes des exercices d'entraînement systématique et d'application relatifs à ($topic). Rappeler les critères de réussite.",
                        studentActivity = "Écoute attentivement les consignes et identifie les critères de réussite attendus.",
                        durationText = "5 min"
                    ),
                    LessonProcedureStep(
                        phaseName = "2. Entraînement systématique sur cahier",
                        teacherActivity = "Faire travailler les élèves sur le cahier d'activités : exercices d'application écrits, transformation de phrases et renforcement ($skills). Assurer un suivi individuel.",
                        studentActivity = "Résout les exercices sur son cahier d'activités avec autonomie, applique les règles et sollicite de l'aide si besoin.",
                        durationText = "25 min"
                    ),
                    LessonProcedureStep(
                        phaseName = "3. Correction collective et régulation",
                        teacherActivity = "Mener la correction collective au tableau, expliciter les erreurs courantes et procéder à la remédiation immédiate.",
                        studentActivity = "Participe à la correction, justifie ses réponses et rectifie ses erreurs sur son cahier.",
                        durationText = "15 min"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex >= 3 -> listOf(
                    LessonProcedureStep(
                        phaseName = "1. Cadrage de l'intégration et bilan",
                        teacherActivity = "Présenter la situation d'intégration finale de l'unité ($topic) et les critères de la production écrite ou de l'évaluation formative.",
                        studentActivity = "Prend connaissance de la tâche d'intégration et mobilise l'ensemble des acquis de l'unité.",
                        durationText = "5 min"
                    ),
                    LessonProcedureStep(
                        phaseName = "2. Production autonome et évaluation formative",
                        teacherActivity = "Guider la production autonome (rédaction de phrases, bilan de compétences : $skills). Observer les réalisations et évaluer le degré de maîtrise.",
                        studentActivity = "Rédige sa production ou réalise l'évaluation de synthèse individuellement en mobilisant le vocabulaire et les règles apprises.",
                        durationText = "25 min"
                    ),
                    LessonProcedureStep(
                        phaseName = "3. Bilan de l'unité et validation des acquis",
                        teacherActivity = "Faire la synthèse finale du projet/unité, valoriser les meilleures productions et clore l'unité pédagogique.",
                        studentActivity = "Participe au bilan final, prend note des appréciations et valide ses acquis de l'unité.",
                        durationText = "15 min"
                    )
                )
                else -> listOf(
                    LessonProcedureStep(
                        phaseName = "1. Mise en train / Présentation",
                        teacherActivity = "Présentation de la situation d'amorce liée à : ($topic). Poser des questions guidées pour mobiliser l'attention.",
                        studentActivity = "Observe le support visuel, écoute attentivement et répond aux questions d'amorce.",
                        durationText = "5 min"
                    ),
                    LessonProcedureStep(
                        phaseName = "2. Construction des notions",
                        teacherActivity = "Lecture modèle du support. Explication des notions cibles ($skills). Structuration de la règle et animation des échanges.",
                        studentActivity = "Écoute la lecture modèle, répond aux questions de compréhension, participe à la formalisation de la règle et la recopie.",
                        durationText = "25 min"
                    ),
                    LessonProcedureStep(
                        phaseName = "3. Application et évaluation",
                        teacherActivity = "Proposer des exercices d'application écrits sur les cahiers pour vérifier l'assimilation du thème : ($topic). Corriger collectivement.",
                        studentActivity = "Effectue les exercices individuellement sur son cahier, puis participe à la correction collective au tableau.",
                        durationText = "15 min"
                    )
                )
            }
        }

        // 3. Mathematics Methodology (الرياضيات)
        if (sub.contains("رياضيات")) {
            return when {
                lesson.week != lesson.weekEnd && wIndex == 0 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (الحساب الذهني ووضعية الانطلاق)",
                        teacherActivity = "تنظيم نشاط الحساب الذهني السريع على الألواح (5-10 عمليات)، ثم عرض الوضعية المشكلة الاستكشافية لموضوع ($topic) وقراءة التعليمات بدقة.",
                        studentActivity = "ينجز الحساب الذهني السريع على اللوح، يقرأ وضعية الانطلاق ويفهم المشكلة الرياضية المطروحة.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (البحث والاستكشاف وبناء المفهوم)",
                        teacherActivity = "توجيه البحث الفردي ثم في ثنائيات. تشجيع استراتيجيات التمثيل والرسم والترميز. مناقشة مقترحات الحل على السبورة وتجريد المفهوم الرياضي الجديد وصياغة القاعدة الأولى لـ ($topic).",
                        studentActivity = "يبحث عن الحل مستخدماً الوسائل والمخططات، يناقش زميله، يعرض حله على السبورة، يستنتج القاعدة ويثبت المفهوم في كراسه.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (التدريب الأولي والتثبيت)",
                        teacherActivity = "تقديم تطبيق مباشر على الألواح وكراس الأنشطة للتأكد من استيعاب المفهوم الجديد ومعالجة اللبس الفوري.",
                        studentActivity = "ينجز التطبيق الفوري على اللوح والكراس، ويشارك في التصحيح الجماعي والذاتي.",
                        durationText = "15 دقيقة"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex == 1 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (الحساب الذهني والربط)",
                        teacherActivity = "حساب ذهني موجه لخدمة الدرس. التذكير بالقاعدة الرياضية لموضوع ($topic) التي تم بناؤها في الأسبوع السابق.",
                        studentActivity = "ينجز الحساب الذهني، يستذكر القاعدة الرياضية وصيغتها الرمزية.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (التعميق والتحليل الرياضي)",
                        teacherActivity = "طرح وضعيات حسابية/هندسية أكثر عمقاً وتنوعاً ($skills). توضيح الخوارزميات وطرق الحساب المتقدمة أو خطوات الإنشاء الهندسي بدقة.",
                        studentActivity = "يحلل المسائل والوضعيات، يطبق الخطوات المنهجية والخوارزميات، ويشارك في البرهنة والاستدلال الرياضي.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (التدريب الممنهج على الكراس)",
                        teacherActivity = "تكليف التلاميذ بحل تمارين متدرجة الصعوبة من كراس الأنشطة، مع متابعة الفئات التي تحتاج دعماً إضافياً.",
                        studentActivity = "يحل التمارين فردياً على كراس الأنشطة، يحرص على الدقة في الحساب والتنظيم، ويصحح أخطاءه.",
                        durationText = "15 دقيقة"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex == 2 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (الحساب الذهني وتحديد مهام التمارين)",
                        teacherActivity = "حساب ذهني سريع. عرض محاور التمارين التدريبية والتطبيقية المعمقة لموضوع ($topic) وشرح أهدافها.",
                        studentActivity = "ينجز الحساب الذهني ويستعد لحل السلاسل التمرينية والأنشطة الإثرائية.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (التدريب الممنهج وحل التمارين الإثرائية)",
                        teacherActivity = "متابعة حل التلاميذ لسلاسل التمارين المتنوعة وأنشطة كراس القسم ($skills). تدريبهم على السرعة والدقة واستخدام المسودة بفاعلية.",
                        studentActivity = "يحل التمارين فردياً بتركيز، يطبق القواعد والخوارزميات بدقة، ويسجل الملاحظات الصعبة.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (التصحيح التفاعلي وتثبيت القواعد)",
                        teacherActivity = "إجراء التصحيح المشترك على السبورة مع مقارنة طرق الحل البديلة وتثبيت النتائج المعتمدة.",
                        studentActivity = "يشارك في عرض طرق الحل على السبورة، يصحح كراسه ويثبت النماذج الصحيحة.",
                        durationText = "15 دقيقة"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex >= 3 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (الحساب الذهني وعرض المسائل المركبة)",
                        teacherActivity = "حساب ذهني سريع يركز على العمليات المركبة. تقديم الوضعيات الإدماجية والمسائل الحياتية الشاملة لـ ($topic).",
                        studentActivity = "ينجز الحساب الذهني ويقرأ نص المسألة الإدماجية بتمعن لاستيعاب سياقها الواقعي.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (حل المسائل الإدماجية والتقويم المرحلي)",
                        teacherActivity = "توجيه التلاميذ لتفكيك المسائل المركبة إلى خطوات حسابية متسلسلة ($skills). تقويم قدرة المتعلم على تعبئة المكتسبات وحل المشكلات.",
                        studentActivity = "يستخرج المعطيات والمجاهيل، يرسم المخطط المناسب، ينجز العمليات ويكتب الإجابة بوحدات القياس الصحيحة.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (الحصيلة الختامية والتقويم الذاتي)",
                        teacherActivity = "مناقشة الحلول النموذجية، رصد الحصيلة العامة للوحدة التعليمية، وتثمين الكفاءات المحققة.",
                        studentActivity = "يقارن حله بالحل النموذجي، يجري التقويم الذاتي، ويدون حصيلة الوحدة في كراسه.",
                        durationText = "15 دقيقة"
                    )
                )
                else -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (الحساب الذهني ووضعية الانطلاق)",
                        teacherActivity = "تنظيم نشاط الحساب الذهني السريع على الألواح (5-10 عمليات)، ثم عرض الوضعية المشكلة الخاصة بـ ($topic) وقراءة التعليمات بوضوح.",
                        studentActivity = "ينجز الحساب الذهني على اللوح، يقرأ وضعية الانطلاق ويفهم المطلوب والمشكلة المطروحة.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (البحث والاكتشاف والصياغة)",
                        teacherActivity = "متابعة إنجاز التلاميذ فردياً ثم في ثنائيات. تشجيع استراتيجيات البحث المتنوعة. مناقشة المقترحات على السبورة وصياغة الخلاصة والقاعدة الحسابية/الهندسية لـ ($topic).",
                        studentActivity = "يبحث عن الحل فردياً، يناقش زميله، يعرض إنجازه على السبورة، يستنتج القاعدة المدونة ويثبت المفهوم الجديد.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (التدريب والتقويم)",
                        teacherActivity = "تقديم تمارين تطبيقية تدريبية من كراس النشاطات لموضوع ($topic)، ومراقبة الإنجاز مع تقديم الدعم والمساندة للمتعثرين.",
                        studentActivity = "حل التمارين فردياً على كراس النشاطات مع إجراء التصحيح الذاتي والتصحيح الجماعي على السبورة.",
                        durationText = "15 دقيقة"
                    )
                )
            }
        }

        // 4. Science & Technology Methodology (التربية العلمية والتكنولوجية)
        if (sub.contains("نشاط") || sub.contains("علم")) {
            return when {
                lesson.week != lesson.weekEnd && wIndex == 0 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (الملاحظة وإثارة التساؤل والفرضيات)",
                        teacherActivity = "عرض عينة طبيعية أو مجسم أو صورة توضيحية لظاهرة ($topic)، وطرح سؤال التساؤل الرئيسي لإثارة الفضول العلمي وجمع فرضيات المتعلمين على السبورة.",
                        studentActivity = "يلاحظ الوسيلة المعروضة بتمعن، يتساءل بفضول علمي، ويقترح فرضيات أولية لتفسير الظاهرة ويدونها.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (التقصي الاستكشافي الأولي)",
                        teacherActivity = "تنظيم الملاحظة والتجريب الأولي لاختبار الفرضيات. توجيه التلاميذ لتسجيل المشاهدات وتحديد المتغيرات الأساسية لموضوع ($topic).",
                        studentActivity = "ينفذ خطوات الملاحظة والتجريب البسيط، يسجل ما يراه في كراس البحث، ويقارن الملاحظات بالفرضيات المقترحة.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (التثبيت وتسجيل الملاحظات)",
                        teacherActivity = "طرح أسئلة شفهية للتأكد من فهم الظاهرة وتوجيه التلاميذ لتدوين الملاحظات والرسومات التوضيحية على دفاترهم.",
                        studentActivity = "يرسم المخطط العلمي البسيط ويدون الملاحظة الأولية ويشارك في النقاش الصفي.",
                        durationText = "15 دقيقة"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex == 1 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (استرجاع المكتسبات والربط)",
                        teacherActivity = "مراجعة نتائج الحصة السابقة حول ($topic) والتذكير بالتساؤل العلمي والفرضيات التي تم اختبارها.",
                        studentActivity = "يستذكر الملاحظات السابقة ويستعد للتعمق في التجريب والتحليل العلمي.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (التجريب المعمق واستخلاص القواعد العلمية)",
                        teacherActivity = "قيادة خطوات التجريب المنهجي أو التحليل الوثائقي المعمق ($skills). توجيه التلاميذ لاستخلاص القوانين والنتائج العلمية وصياغة الخلاصة على السبورة.",
                        studentActivity = "يحلل البيانات والوثائق العلمية، يستنتج العلاقة بين الأسباب والنتائج، ويشارك في صياغة القاعدة العلمية ونقلها لكراسه.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (التدريب والتطبيق العلمي)",
                        teacherActivity = "تقديم تمرين تطبيقي من كراس الأنشطة لاختبار استيعاب القاعدة العلمية وتطبيقها في سياقات جديدة.",
                        studentActivity = "ينجز التمرين التطبيقي فردياً ويفسر الظواهر بناءً على الاستنتاج العلمي الجديد.",
                        durationText = "15 دقيقة"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex >= 2 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (طرح المهمة التطبيقية أو الإدماجية)",
                        teacherActivity = "عرض نشاط إدماجي أو تجربة تركيبية أو مشروع علمي تكنولوجي يرتبط بموضوع ($topic).",
                        studentActivity = "يتعرف على معايير المهمة العلمية التطبيقية ويستعد لإنجازها بالتعاون أو فردياً.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (المشروع العلمي وحل الوضعيات الإدماجية)",
                        teacherActivity = "متابعة إنجاز التجارب التركيبية أو حل الوضعيات الإدماجية العلمية والبيئية ($skills). تقديم التوجيه والتقويم التكويني المستمر.",
                        studentActivity = "يطبق المعارف العلمية في تفسير مشكلات بيئية وصحية وتكنولوجية، وينجز التمارين الشاملة في كراسه.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (التقويم الختامي للوحدة والحصيلة)",
                        teacherActivity = "مناقشة النتائج العلمية، تأكيد المفاهيم الصحيحة وتصحيح التصورات البديلة الخاطئة، وتثبيت الحصيلة العامة للوحدة.",
                        studentActivity = "يعرض استنتاجاته، يصحح المفاهيم الخاطئة ذاتياً، ويدون الخلاصة الشاملة للوحدة.",
                        durationText = "15 دقيقة"
                    )
                )
                else -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (الملاحظة وإثارة التساؤل والفرضيات)",
                        teacherActivity = "عرض عينة طبيعية أو مجسم أو صورة توضيحية لظاهرة ($topic)، وطرح سؤال التساؤل الرئيسي لإثارة الفضول العلمي وجمع فرضيات المتعلمين على السبورة.",
                        studentActivity = "يلاحظ الوسيلة المعروضة بتمعن، يتساءل بفضول علمي، ويقترح فرضيات أولية لتفسير الظاهرة ويدونها.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (التقصي، التجريب وبناء الاستنتاج)",
                        teacherActivity = "تنظيم خطة الملاحظة والتجريب أو التحليل الوثائقي. توجيه التلاميذ لتسجيل الملاحظات ومقارنة النتائج بالفرضيات، ومساعدتهم في استخلاص النتيجة وصياغة الخلاصة العلمية لـ ($topic).",
                        studentActivity = "ينفذ خطوات التجريب والملاحظة ضمن مجموعته، يسجل النتائج في كراس البحث، ويشارك في صياغة وتدوين الخلاصة والاستنتاج العلمي المعتمد.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (استثمار المعرفة والتقويم العلمي)",
                        teacherActivity = "عرض وضعية علمية جديدة أو تمرين تطبيقي من كراس النشاطات لاستثمار وتطبيق المفاهيم العلمية المكتسبة في ($topic).",
                        studentActivity = "يجيب عن التمرين التطبيقي ويفسر ظواهر يومية مشابهة بناءً على الخلاصة العلمية الجديدة.",
                        durationText = "15 دقيقة"
                    )
                )
            }
        }

        // 5. Islamic Studies Methodology (التربية الإسلامية)
        if (sub.contains("إسلامية")) {
            return when {
                lesson.week != lesson.weekEnd && wIndex == 0 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (التمهيد الإيماني والربط)",
                        teacherActivity = "التمهيد بقصة هادفة أو استحضار موقف قيمي يخدم موضوع ($topic) لإثارة اهتمام التلاميذ وربط الدرس بمحبة الله ورسوله والقيم النبيلة.",
                        studentActivity = "يستمع للوضعية باهتمام، يتفاعل مع المعلم بإبداء مشاعره، ويتعرف على عنوان الدرس وغايته.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (التلاوة النموذجية والشرح الأولي)",
                        teacherActivity = "تلاوة الآيات الكريمة بخشوع أو قراءة الحديث الشريف قراءة نموذجية متقنة. شرح المفردات اللغوية الصعبة والمعاني العامة ($skills) وتوجيه القراءات الفردية.",
                        studentActivity = "يستمع بخشوع، يردد التلاوة ويحاكي القراءة النموذجية، ويشارك في تفسير الكلمات الصعبة.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (الفهم الشفهي والتثبيت الأولي)",
                        teacherActivity = "طرح أسئلة الفهم الإجمالي والتأكد من استيعاب المعنى العام لـ ($topic) وتصحيح النطق ومخارج الحروف.",
                        studentActivity = "يجيب عن أسئلة الفهم، يصحح نطقه، ويظهر استيعابه للمعنى الإجمالي.",
                        durationText = "15 دقيقة"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex == 1 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (مراجعة التلاوة والربط)",
                        teacherActivity = "تلاوة الآيات/الحديث للمراجعة، والتذكير بالمعاني العامة التي تم استكشافها في الأسبوع السابق حول ($topic).",
                        studentActivity = "يتلو الآيات مع المعلم ويستحضر معانيها العامة.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (استنباط القيم والأحكام والآداب)",
                        teacherActivity = "قيادة الحوار لاستنباط القيم الإيمانية والأحكام والآداب السلوكية المستهدفة ($skills). تأطير صياغة خلاصة الدرس التربوية على السبورة.",
                        studentActivity = "يشارك في استخراج العبر والفوائد والقيم الإسلامية، ويساهم في صياغة الخلاصة وتدوينها في كراسه.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (التطبيق السلوكي والاستثمار)",
                        teacherActivity = "طرح مواقف حياتية وسلوكية لقياس قدرة التلاميذ على تطبيق ما تعلموه في تعاملاتهم اليومية مع الأسرة والزملاء.",
                        studentActivity = "يعبر عن التزامه بالسلوك القويم ويوضح كيف يطبق الآداب في مدرسته وبيئته.",
                        durationText = "15 دقيقة"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex >= 2 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (التهيئة للاستظهار والتقويم)",
                        teacherActivity = "التذكير بأهمية حفظ كتاب الله وهدي رسوله صلى الله عليه وسلم، وتحديد معايير الاستظهار المتقن لـ ($topic).",
                        studentActivity = "يستعد للاستظهار الفردي والتسميع بإتقان وخشوع.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (الاستظهار الفردي والترسيخ القيمي)",
                        teacherActivity = "تسميع الآيات/الحديث فردياً للتلاميذ، تقويم الحفظ ومخارج الحروف وأحكام التجويد الأساسية، ومناقشة وضعيات إدماجية قيمية ($skills).",
                        studentActivity = "يستظهر السورة/الحديث بدقة، يصحح أخطاءه مع المعلم، ويتفاعل مع الوضعيات القيمية المطروحة.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (الحصيلة الإيمانية والتقويم الختامي)",
                        teacherActivity = "تثبيت حفظ التلاميذ، تشجيع المتميزين، والتأكيد على التحلي المستمر بالأخلاق الإسلامية المستفادة من الوحدة.",
                        studentActivity = "يردد الدعاء الختامي ويلتزم بالعمل الصالح والمحافظة على قيمه الإسلامية.",
                        durationText = "15 دقيقة"
                    )
                )
                else -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (التمهيد الإيماني والربط)",
                        teacherActivity = "التمهيد بقصة هادفة أو استحضار موقف قيمي يخدم موضوع ($topic) لإثارة اهتمام التلاميذ وربط الدرس بمحبة الله ورسوله والقيم النبيلة.",
                        studentActivity = "يستمع للوضعية باهتمام، يتفاعل مع المعلم بإبداء مشاعره، ويتعرف على عنوان الدرس وغايته.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (التلاوة والشرح واستنباط الهدي والآداب)",
                        teacherActivity = "تلاوة الآيات الكريمة بخشوع أو قراءة الحديث الشريف. شرح المفردات والمعاني بأسلوب ميسر. استنباط القيم الإيمانية والأحكام والآداب السلوكية المستهدفة ($skills) وتدوين الخلاصة.",
                        studentActivity = "يردد التلاوة ويحاكي النطق القرآني السليم، يستمع للشرح، يشارك في استخراج القيم والأخلاق الإيمانية، وينقل الخلاصة في كراسه.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (الاستظهار والتقويم السلوكي)",
                        teacherActivity = "توجيه التلاميذ لاستظهار الآيات/الأحاديث فردياً، وطرح أسئلة التثبيت وقياس مدى الالتزام بتطبيق السلوك الإيجابي في الحياة اليومية.",
                        studentActivity = "يستظهر السورة/الحديث الشريف فردياً، يجيب عن أسئلة التثبيت، ويلتزم بالعمل بما تعلم في مدرسته وبيته.",
                        durationText = "15 دقيقة"
                    )
                )
            }
        }

        // 6. History & Geography Methodology (التاريخ والجغرافيا)
        if (sub.contains("تاريخ") || sub.contains("جغراف")) {
            return when {
                lesson.week != lesson.weekEnd && wIndex == 0 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (ملاحظة السند وتوطين الموضوع)",
                        teacherActivity = "عرض خريطة جغرافية أو وثيقة تاريخية أو صورة معلم أثري يخص موضوع ($topic)، وطرح أسئلة تمهيدية لاستثارة المكتسبات وتوطين الموضوع في الزمان أو المكان.",
                        studentActivity = "يلاحظ السند المعروض، يتأمل الخريطة أو الوثيقة، ويجيب عن الأسئلة التمهيدية بتفاعل.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (قراءة السندات وبناء المفاهيم التأسيسية)",
                        teacherActivity = "توجيه التلاميذ لقراءة الوثائق والخرائط وتحديد المواقع والأحداث الأساسية ($skills). طرح أسئلة الفهم واستخراج الحقائق الأولى لـ ($topic).",
                        studentActivity = "يقرأ السندات، يحدد المواقع على الخريطة، ويشارك في استخراج الحقائق التاريخية أو الجغرافية الأولى.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (التثبيت الشفهي وتحديد المعالم)",
                        teacherActivity = "طرح أسئلة تثبيت فورية على الخريطة/السبورة للتأكد من استيعاب التوطين والمفاهيم الجديدة.",
                        studentActivity = "يعين المواقع أو الأحداث على السبورة ويشارك في النقاش الصفي.",
                        durationText = "15 دقيقة"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex == 1 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (التذكير والربط المكاني/الزماني)",
                        teacherActivity = "مراجعة ما تم تعلمه في الأسبوع السابق حول ($topic) والتذكير بالمواقع أو الأحداث المحورية.",
                        studentActivity = "يستحضر المعلومات السابقة ويحدد المعالم الأساسية على الخريطة أو الخط الزمني.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (التحليل والتفسير واستخلاص الخلاصة)",
                        teacherActivity = "تحليل العلاقات الجغرافية أو الأسباب والنتائج التاريخية ($skills). توجيه التلاميذ لصياغة خلاصة منظمة وشاملة وتدوينها على السبورة.",
                        studentActivity = "يحلل ويفسر الظواهر أو الأحداث، يستنتج الأثر والأسباب، ويدون الخلاصة في كراسه بخط واضح.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (التدريب على كراس الأنشطة)",
                        teacherActivity = "تكليف التلاميذ بحل أنشطة وتمارين كراس التطبيقات (ملء جداول، مقارنات، تفسيرات).",
                        studentActivity = "ينجز التمارين في كراسه ويشارك في التصحيح الجماعي والذاتي.",
                        durationText = "15 دقيقة"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex >= 2 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (تحديد المهام التطبيقية والإدماجية)",
                        teacherActivity = "عرض خريطة صماء أو جدول زمني للمقارنة وتقديم إرشادات الإنجاز والتقويم لموضوع ($topic).",
                        studentActivity = "يتعرف على خطوات الإنجاز والتعيين المطلوب على الخريطة أو المخطط الزمني.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (التعيين على الخرائط وحل الوضعيات الإدماجية)",
                        teacherActivity = "متابعة إنجاز التلاميذ على الخرائط الصماء، وتدريبهم على قراءة المفاتيح والرموز الجغرافية أو الخطوط الزمنية ($skills). تقويم الكفايات المحققة.",
                        studentActivity = "يعين الظواهر والمعالم على الخريطة الصماء بدقة، يحلل الوضعيات الإدماجية ويوظف المصطلحات السليمة.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (الحصيلة الشاملة والتقويم النهائي للوحدة)",
                        teacherActivity = "إجراء تقويم نهائي لمكتسبات الوحدة، تثبيت الخلاصات الكبرى، وتصحيح أي خلط في المفاهيم.",
                        studentActivity = "يقوم عمله ذاتياً ويثبت المعارف النهائية للوحدة في كراسه.",
                        durationText = "15 دقيقة"
                    )
                )
                else -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (ملاحظة السند والتمهيد)",
                        teacherActivity = "عرض خريطة جغرافية أو وثيقة تاريخية أو صورة معلم أثري يخص موضوع ($topic)، وطرح أسئلة تمهيدية لاستثارة المكتسبات القبلية وتوطين الموضوع.",
                        studentActivity = "يلاحظ السند المعروض، يتأمل الخريطة أو الوثيقة، ويجيب عن الأسئلة التمهيدية بتفاعل.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (قراءة وتحليل السندات واستخلاص المفاهيم)",
                        teacherActivity = "توجيه التلاميذ لقراءة الوثائق والخرائط وتفكيك عناصرها ($skills). طرح أسئلة التحليل والمقارنة والتفسير، وتأطير كتابة خلاصة مركزة على السبورة.",
                        studentActivity = "يحلل السندات والخرائط، يحدد المواقع والتواريخ والأسباب والنتائج، ويشارك في صياغة خلاصة الدرس وتدوينها في كراسه.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (التوطين والتقويم والتثبيت)",
                        teacherActivity = "طرح تمرين تطبيقي عملي (التعيين على خريطة صماء، ملء جدول أحداث، أو تصنيف ظواهر) بخصوص ($topic) لقياس الأثر المعرفي.",
                        studentActivity = "ينجز التمرين التطبيقي فردياً على كراسه ويشارك في التصحيح الجماعي والذاتي.",
                        durationText = "15 دقيقة"
                    )
                )
            }
        }

        // 7. Civic Education Methodology (التربية المدنية)
        if (sub.contains("مدنية")) {
            return when {
                lesson.week != lesson.weekEnd && wIndex == 0 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (عرض المشهد واستكشاف الموقف)",
                        teacherActivity = "عرض مشهد مصور أو حادثة واقعية تمس الحقوق والواجبات والمواطنة لدرس ($topic)، وطرح أسئلة لإثارة الحوار الصفي واستكشاف مشاعر التلاميذ.",
                        studentActivity = "يلاحظ المشهد، يستمع للوضعية، ويبدي رأيه وموقفه الأولي من السلوك المعروض.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (التحليل وتفكيك الموقف المدني)",
                        teacherActivity = "إدارة الحوار الصفي لمناقشة المشهد، توجيه التلاميذ للمقارنة بين السلوكيات الإيجابية والسلبية، وتحديد المفاهيم الأولية لـ ($topic).",
                        studentActivity = "يناقش مع زملائه، يميز بين التصرف السليم وغير السليم، ويحدد الآثار المترتبة على كل سلوك.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (التثبيت الشفهي وتحديد الموقف)",
                        teacherActivity = "طرح أسئلة سريعة لتأكيد الموقف المدني الصحيح وتشجيع التلاميذ على التعبير بثقة.",
                        studentActivity = "يعبر عن موقفه الإيجابي ويشارك في صياغة المبادئ الأولية للدرس.",
                        durationText = "15 دقيقة"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex == 1 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (التذكير بالموقف والربط)",
                        teacherActivity = "التذكير بالموقف المدني السابق لموضوع ($topic) وربطه بالواجبات المدرسية والمجتمعية.",
                        studentActivity = "يستذكر الموقف المدني ويستعد لبناء القواعد والضوابط القانونية والأخلاقية.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (استخلاص القواعد المدنية وتدوين الخلاصة)",
                        teacherActivity = "توجيه التلاميذ لاستنتاج القواعد القانونية والأخلاقية ($skills)، التمييز بين الحق والواجب، وتأطير كتابة خلاصة شاملة على السبورة.",
                        studentActivity = "يستنتج القاعدة المدنية، يميز حقوقه وواجباته، ويدون الخلاصة في كراسه.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (التدريب على كراس الأنشطة)",
                        teacherActivity = "تكليف التلاميذ بإنجاز تمارين كراس القسم (تصنيف سلوكيات، مواقف صحيحة وخاطئة).",
                        studentActivity = "ينجز التمارين فردياً ويشارك في التصحيح الجماعي والذاتي.",
                        durationText = "15 دقيقة"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex >= 2 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (عرض الوضعية الإدماجية المدنية)",
                        teacherActivity = "تقديم موقف مركب أو دراسة حالة واقعية تتطلب اتخاذ قرار مدني وممارسة المواطنة المسؤولة لـ ($topic).",
                        studentActivity = "يستمع للوضعية ويفهم عناصر المشكلة المدنية والخيارات المتاحة.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (حل الوضعيات الإدماجية والمحاكاة)",
                        teacherActivity = "تأطير مناقشات المجموعات أو المحاكاة وتمثيل الأدوار ($skills). تقويم التزام التلاميذ بالقيم المدنية واحترام النظام والقانون.",
                        studentActivity = "يشارك في تمثيل الأدوار أو حل الوضعية، يقدم مقترحات بناءة، ويلتزم بالسلوك المدني السليم.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (ميثاق السلوك والحصيلة الختامية)",
                        teacherActivity = "صياغة ميثاق سلوكي جماعي يعكس ما تم تعلمه في الوحدة وتثبيت السلوكيات الإيجابية في المدرسة والحي.",
                        studentActivity = "يلتزم ببنود الميثاق المدني ويدون الخلاصة الختامية في كراسه.",
                        durationText = "15 دقيقة"
                    )
                )
                else -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (عرض الموقف والانطلاق)",
                        teacherActivity = "عرض مشهد مصور أو حادثة واقعية تمس الحقوق والواجبات والمواطنة لدرس ($topic)، وطرح أسئلة لإثارة الحوار الصفي.",
                        studentActivity = "يلاحظ المشهد، يستمع للوضعية، ويبدي رأيه وموقفه الأولي من السلوك المعروض.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (تحليل الموقف واستنتاج القواعد المدنية)",
                        teacherActivity = "إدارة النقاش الهادف، توجيه التلاميذ لتحديد السلوكيات المدنية الإيجابية ومقارنتها بالسلبية، استخلاص القواعد القانونية والأخلاقية ($skills) وتدوين الخلاصة.",
                        studentActivity = "يشارك في الحوار، يميز بين الحق والواجب، يستنتج القيمة المدنية وينقل السلوك القويم وخلاصة الدرس إلى كراسه.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (الالتزام السلوكي والتقويم)",
                        teacherActivity = "طرح وضعية تقويمية لاختبار قدرة التلميذ على اتخاذ القرار المدني السليم وتطبيق السلوك الإيجابي في مدرسته ومجتمعه.",
                        studentActivity = "يجيب عن أسئلة الوضعية التقويمية، ويلتزم بتبني السلوك المدني الإيجابي قولا وعملا.",
                        durationText = "15 دقيقة"
                    )
                )
            }
        }

        // 8. Art and Physical Education (التربية الفنية والبدنية - المستويات 1-2)
        if (sub.contains("فنية") || sub.contains("بدنية")) {
            return when {
                lesson.week != lesson.weekEnd && wIndex % 3 == 0 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (التهيئة والإحماء والاستكشاف)",
                        teacherActivity = "شرح فكرة النشاط الفني أو الحركي لموضوع ($topic)، وتنظيم تمارين الإحماء البدني أو عرض النماذج والألوان والأناشيد.",
                        studentActivity = "يستمع للتعليمات، يشارك في حركات الإحماء أو يستعد بأدوات الرسم والنشيد بحماس.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (الممارسة الموجهة واكتساب التقنيات)",
                        teacherActivity = "تأطير تدريب التلاميذ على التقنيات الفنية (مزج الألوان، الخطوط، التنسيق) أو الحركية (التوازن، الوثب، الركض المنظم) مع مراعاة السلامة والتشجيع.",
                        studentActivity = "يمارس النشاط الفني أو الحركي بنشاط، يطبق التقنيات التأسيسية، ويتعاون مع زملائه.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (العرض الأولي والاسترخاء)",
                        teacherActivity = "استعراض الإنجازات الأولية وتوجيه تمارين التهدئة والاسترخاء واستخلاص الفوائد الصحية والتربوية.",
                        studentActivity = "يعرض رسمه أو يشارك في تمارين التهدئة والاسترخاء ويعبر عن سعادته بالمشاركة.",
                        durationText = "15 دقيقة"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex % 3 == 1 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (الإحماء والربط)",
                        teacherActivity = "تمارين إحماء بدني متقدمة أو تهيئة أدوات التعبير الفني لموضوع ($topic).",
                        studentActivity = "ينفذ الإحماء بنشاط ويستعد للممارسة المعمقة.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (التطبيق الفني والحركي الممنهج)",
                        teacherActivity = "قيادة الألعاب الجماعية والقواعد الرياضية أو إنجاز اللوحات التشكيلية المعبرة والأناشيد الإيقاعية ($skills). تنمية روح الفريق والإبداع.",
                        studentActivity = "يشارك في الألعاب الجماعية أو ينجز عمله الفني مع التركيز على التنسيق والجمالية والتعاون.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (التقييم الفردي والاسترخاء)",
                        teacherActivity = "تقييم الأداء الحركي والفني، تنظيم تمارين الاسترجاع البدني وجمع الأدوات بنظام.",
                        studentActivity = "يقوم بالتهدئة العضلية، ينظف مكانه ويرتب أدواته باعتزاز.",
                        durationText = "15 دقيقة"
                    )
                )
                lesson.week != lesson.weekEnd && wIndex % 3 == 2 -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (التهيئة للمنافسة أو المعرض)",
                        teacherActivity = "تنظيم فرق الأنشطة الرياضية أو ورشات العرض الفني التشكيلي لـ ($topic).",
                        studentActivity = "ينتظم في فريقه أو ورشته الفنية بحماس وانضباط.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (المنافسة الرياضية والإبداع الفني الشامل)",
                        teacherActivity = "إدارة المنافسات الرياضية التنافسية الودية أو إتمام المشاريع الفنية الكبرى ($skills) وتطبيق قواعد الروح الرياضية والذوق الجمالي.",
                        studentActivity = "يبدع في الرسم والنشيد أو يتنافس بروح رياضية عالية واحترام للزملاء والقوانين.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (معرض الأعمال وتتويج الفرق والاسترخاء)",
                        teacherActivity = "تنظيم معرض صفي للأعمال الفنية وتثمين أداء الفرق الرياضية وقيادة تمارين الاسترخاء الختامية.",
                        studentActivity = "يشارك في تقييم الأعمال الفنية، يهنئ زملاءه، ويمارس تمارين الاسترخاء الختامية.",
                        durationText = "15 دقيقة"
                    )
                )
                else -> listOf(
                    LessonProcedureStep(
                        phaseName = "التقديم (التهيئة والإحماء الفني/البدني)",
                        teacherActivity = "شرح فكرة النشاط الفني أو الحركي لموضوع ($topic)، وتنظيم تمارين الإحماء البدني أو عرض النماذج والألوان والأناشيد.",
                        studentActivity = "يستمع للتعليمات، يشارك في حركات الإحماء أو يستعد بأدوات الرسم والنشيد بحماس.",
                        durationText = "5 دقائق"
                    ),
                    LessonProcedureStep(
                        phaseName = "تنمية التعلم (الممارسة الموجهة والإبداع الحركي/الفني)",
                        teacherActivity = "تأطير إنجاز التلاميذ الفني (الرسم، التلوين، الإيقاع) أو الحركي (التوازن، الجري، القفز، التنسيق)، مع توفير شروط السلامة وتشجيع التعبير الحر والتعاون.",
                        studentActivity = "يمارس النشاط الفني أو الحركي بنشاط، يطبق القواعد الفنية والرياضية، ويتعاون مع زملائه في الفريق.",
                        durationText = "25 دقيقة"
                    ),
                    LessonProcedureStep(
                        phaseName = "التطبيق (العرض والاسترخاء والتقويم)",
                        teacherActivity = "عرض الأعمال الفنية المنجزة وتشجيع المواهب، أو قيادة تمارين الاسترخاء البدني واستخلاص الفوائد الصحية والتربوية للنشاط.",
                        studentActivity = "يعرض رسمه أو يشارك في تمارين التهدئة والاسترخاء، ويعبر عن مشاعره الإيجابية نحو النشاط.",
                        durationText = "15 دقيقة"
                    )
                )
            }
        }

        // 9. Standard / Arabic Language Methodology (اللغة العربية)
        return when {
            lesson.week != lesson.weekEnd && wIndex == 0 -> listOf(
                LessonProcedureStep(
                    phaseName = "التقديم (وضعية الانطلاق والتهيئة)",
                    teacherActivity = "عرض المشهد التعبيري المصور أو السند اللغوي التأسيسي لموضوع ($topic). طرح أسئلة الملاحظة والانطلاق لإثارة الرصيد اللغوي للتلاميذ.",
                    studentActivity = "يتأمل المشهد المعروض بتركيز، يعبر شفهياً عما يراه، ويجيب عن أسئلة التهيئة والانطلاق بجمل تامة.",
                    durationText = "5 دقائق"
                ),
                LessonProcedureStep(
                    phaseName = "تنمية التعلم (القراءة النموذجية وبناء المعارف الأولية)",
                    teacherActivity = "قراءة النص/الجمل الأساسية قراءة جهرية معبرة وسليمة. طرح أسئلة الفهم العام وتفكيك عناصر الظاهرة اللغوية أو الصوتية المستهدفة ($skills). توجيه قراءات التلاميذ مع التصحيح الفوري.",
                    studentActivity = "يتابع القراءة النموذجية، يقرأ قراءات فردية متدرجة، يشارك في الإجابة عن أسئلة الفهم، ويحدد الكلمات والتراكيب المحورية.",
                    durationText = "25 دقيقة"
                ),
                LessonProcedureStep(
                    phaseName = "التطبيق (الممارسة الشفهية والكتابية الأولية)",
                    teacherActivity = "توجيه التلاميذ لتوظيف الصيغ والمفردات الجديدة في جمل مفيدة شفهياً وكتابة كلمات/جمل على الألواح.",
                    studentActivity = "يركب جملاً شفهية مفيدة، يكتب النماذج على اللوح، ويشارك في التصحيح الجماعي مع زملائه.",
                    durationText = "15 دقيقة"
                )
            )
            lesson.week != lesson.weekEnd && wIndex == 1 -> listOf(
                LessonProcedureStep(
                    phaseName = "التقديم (التذكير والربط)",
                    teacherActivity = "مراجعة سريعة لما تم اكتسابه في الأسبوع السابق حول ($topic)، وقراءة جمل الشواهد اللغوية المنطلقة من النص.",
                    studentActivity = "يقرأ جمل الشواهد على السبورة، ويستحضر المفاهيم المكتسبة بدقة.",
                    durationText = "5 دقائق"
                ),
                LessonProcedureStep(
                    phaseName = "تنمية التعلم (التحليل وتجريد الظاهرة وتدوين القاعدة)",
                    teacherActivity = "تحليل الشواهد اللغوية (نحو، صرف، إملاء، أصوات). توجيه التلاميذ لاستخراج القاعدة اللغوية والضوابط المنهجية ($skills) وتأطير تدوينها على السبورة.",
                    studentActivity = "يشارك في تحليل الشواهد، يكتشف القاعدة النحوية أو الإملائية، يصيغها بأسلوبه، وينقل القاعدة إلى كراسه بخط واضح.",
                    durationText = "25 دقيقة"
                ),
                LessonProcedureStep(
                    phaseName = "التطبيق (التدريب الموجه على الألواح والكراس)",
                    teacherActivity = "طرح تمارين تطبيقية فورية لترسيخ القاعدة المكتسبة، ومتابعة الأداء مع تقديم التغذية الراجعة.",
                    studentActivity = "ينجز التمارين التطبيقية على اللوح وكراس القسم، ويصحح أخطاءه مع زملائه.",
                    durationText = "15 دقيقة"
                )
            )
            lesson.week != lesson.weekEnd && wIndex == 2 -> listOf(
                LessonProcedureStep(
                    phaseName = "التقديم (تحديد مهام التدريب والأنشطة)",
                    teacherActivity = "عرض معايير الإنجاز الخاصة بالأنشطة التمرينية والتطبيقات اللغوية المعمقة لـ ($topic).",
                    studentActivity = "يستمع للتعليمات ويتعرف على المطلوب ومعايير الجودة في التطبيق والحل.",
                    durationText = "5 دقائق"
                ),
                LessonProcedureStep(
                    phaseName = "تنمية التعلم (التدريب الممنهج على كراس الأنشطة)",
                    teacherActivity = "تكليف التلاميذ بإنجاز تمارين كراس الأنشطة اللغوية المعتمدة لترسيخ الظواهر المدروسة ($skills). التجول بين الصفوف لتقديم الدعم الفردي وتصحيح المسارات.",
                    studentActivity = "يحل التمارين اللغوية في كراسه بتركيز، يطبق القواعد النحوية والإملائية، ويطلب التوجيه عند الحاجة.",
                    durationText = "25 دقيقة"
                ),
                LessonProcedureStep(
                    phaseName = "التطبيق (التصحيح التفاعلي وتثبيت المكتسبات)",
                    teacherActivity = "إجراء التصحيح الجماعي للتمارين على السبورة، تثبيت القواعد، وتوجيه التلاميذ للتصحيح الذاتي.",
                    studentActivity = "يشارك في التصحيح الجماعي، يبرر إجاباته، ويصحح أخطاءه على كراسه بقلم مغاير.",
                    durationText = "15 دقيقة"
                )
            )
            lesson.week != lesson.weekEnd && wIndex >= 3 -> listOf(
                LessonProcedureStep(
                    phaseName = "التقديم (تحديد وضعية الإنتاج الكتابي والتعبير)",
                    teacherActivity = "عرض وضعية الإنتاج الكتابي والتعبير الكتابي الشامل للوحدة ($topic) وتحديد المعايير وعلامات الترقيم المطلوبة.",
                    studentActivity = "يستمع لتعليمات وضعية الإنتاج ويستحضر الرصيد المعجمي والتراكيب التي اكتسبها في الوحدة.",
                    durationText = "5 دقائق"
                ),
                LessonProcedureStep(
                    phaseName = "تنمية التعلم (التحرير والإنتاج الكتابي والإدماج الجزئي)",
                    teacherActivity = "متابعة تحرير التلاميذ لفقرات الإنتاج الكتابي وتوظيف الظواهر اللغوية ($skills). توفير المرافقة الفردية للمتعثرين.",
                    studentActivity = "يحرر فقرته في كراسه مراعياً تسلسل الأفكار، سلامة اللغة، علامات الترقيم وجمالية الخط العربي.",
                    durationText = "25 دقيقة"
                ),
                LessonProcedureStep(
                    phaseName = "التطبيق (قراءة النماذج والتقويم الذاتي والحصيلة)",
                    teacherActivity = "الاستماع لنماذج من إنتاجات التلاميذ وتثمين الأداء، وتدوين الحصيلة الختامية للوحدة في اللغة العربية.",
                    studentActivity = "يقرأ فقرته أمام زملائه بثقة، يتقبل الملاحظات البناءة، ويجري التقويم الذاتي لإنتاجه.",
                    durationText = "15 دقيقة"
                )
            )
            else -> listOf(
                LessonProcedureStep(
                    phaseName = "التقديم (التهيئة والانطلاق)",
                    teacherActivity = "تهيئة التلاميذ للدرس بعرض الصورة المعبرة أو مراجعة الدرس السابق، وطرح سؤال الانطلاق حول موضوع ($topic).",
                    studentActivity = "يلاحظ الصورة المعروضة، يستمع باهتمام لحديث المعلم، ويجيب عن أسئلة التهيئة والانطلاق شفهياً.",
                    durationText = "5 دقائق"
                ),
                LessonProcedureStep(
                    phaseName = "تنمية التعلم (البناء والتحليل واستخلاص القاعدة)",
                    teacherActivity = "عرض النموذج المكتوب أو السند المعتمد. قراءة النص/الشواهد قراءة سليمة معبرة. طرح أسئلة الفهم لمناقشة ($topic) واستخراج المعارف والمهارات ($skills). توجيه القراءات والمشاركات مع التصحيح وتدوين الخلاصة.",
                    studentActivity = "يتابع العرض والمثال، يقرأ ويشارك في المناقشة، يجيب عن الأسئلة، يكتشف الصيغ والتراكيب الجديدة، ويدون الخلاصة في كراسه.",
                    durationText = "25 دقيقة"
                ),
                LessonProcedureStep(
                    phaseName = "التطبيق (التقويم والاستثمار والتثبيت)",
                    teacherActivity = "تقديم وضعية تطبيقية أو تمرين كتابي/شفهي يستهدف قياس مدى توظيف المعارف والمهارات الجديدة المكتسبة في ($topic) ومتابعة التصحيح.",
                    studentActivity = "ينجز التمرين التطبيقي فردياً على الكراس أو اللوح، ثم يشارك في التصحيح الجماعي والذاتي مع المعلم والزملاء.",
                    durationText = "15 دقيقة"
                )
            )
        }
    }

    /**
     * Generates tailored Pedagogical Station proposals (Integration, Assessment, Remediation)
     * based on the subject, level, term, and previously covered competencies.
     */
    fun getPedagogicalStationData(lesson: AnnualLessonItem, selectedWeek: Int = lesson.week): PedagogicalStationData {
        val levelTitle = getLevelTitle(lesson.level)
        val subject = lesson.subject
        val domain = OfficialPlanningHierarchyAdapter.getCleanDomain(lesson).ifBlank { "محطة بيداغوجية للإدماج والتقويم والعلاج" }
        val stationTitle = lesson.title
        val term = when (selectedWeek) {
            in 1..13 -> 1
            in 14..26 -> 2
            else -> 3
        }
        val isFrench = lesson.isFrench || subject.contains("français", ignoreCase = true)

        val integrationList = generateIntegrationProposals(lesson, selectedWeek, term, isFrench)
        val assessmentList = generateAssessmentProposals(lesson, selectedWeek, term, isFrench)
        val remediationList = generateRemediationProposals(lesson, selectedWeek, term, isFrench)

        return PedagogicalStationData(
            levelTitle = levelTitle,
            subject = subject,
            domain = domain,
            stationTitle = stationTitle,
            week = selectedWeek,
            term = term,
            isFrench = isFrench,
            integrationProposals = integrationList,
            assessmentProposals = assessmentList,
            remediationProposals = remediationList
        )
    }

    private fun generateIntegrationProposals(
        lesson: AnnualLessonItem,
        week: Int,
        term: Int,
        isFrench: Boolean
    ): List<IntegrationProposal> {
        val sub = lesson.subject.lowercase()
        val tit = lesson.title

        if (isFrench) {
            return listOf(
                IntegrationProposal(
                    title = "Situation d'intégration communicative et linguistique (Projet Trimestriel $term)",
                    situationContext = "Dans le cadre de la clôture des apprentissages de l'étape (Semaine $week), les apprenants sont invités à mobiliser l'ensemble des ressources lexicales, phonétiques et syntaxiques construites durant les semaines précédentes.",
                    tasks = listOf(
                        "Tâche 1 : Écouter un document sonore ou lire un court dialogue situationnel et identifier les personnages et l'action principale.",
                        "Tâche 2 : Produire à l'oral 3 à 4 répliques cohérentes en réinvestissant les actes de parole acquis.",
                        "Tâche 3 : Rédiger sur le cahier 2 à 3 phrases correctes respectant la ponctuation et l'orthographe d'usage."
                    ),
                    targetedSkills = "Mobilisation intégrée des compétences de communication orale, de lecture expressive et de production d'écrits courts."
                ),
                IntegrationProposal(
                    title = "Mini-projet de groupe : La boîte aux mots et aux images",
                    situationContext = "Création d'une affiche murale ou d'un mini-album thématique récapitulant les mots-clés et structures de la période.",
                    tasks = listOf(
                        "Tâche 1 : Sélectionner et classer les images selon les thèmes abordés.",
                        "Tâche 2 : Associer chaque illustration à sa légende écrite sous la conduite du groupe.",
                        "Tâche 3 : Présentation orale devant la classe avec auto-correction collective guidée."
                    ),
                    targetedSkills = "Coopération, discrimination sémantique et réinvestissement créatif des acquis."
                )
            )
        }

        return when {
            sub.contains("رياضيات") -> listOf(
                IntegrationProposal(
                    title = "وضعية إدماجية دالة: تنظيم وحساب ميزانية المشتريات المدرسية",
                    situationContext = "في إطار حصيلة تعلمات الفصل $term (الأسبوع $week)، يُوضع التلميذ في سياق واقعي لحساب تكاليف تجهيز أدوات مدرسية أو قياس أبعاد فناء المدرسة وتحديد الأشكال الهندسية المحيطة به.",
                    tasks = listOf(
                        "المهمة 1: استخراج المعطيات العددية والهندسية من النص والسند البصري المصاحب.",
                        "المهمة 2: اختيار العمليات الحسابية المناسبة (جمع بالاحتفاظ، طرح، ضرب/قسمة بحسب المستوى) وإنجازها عمودياً بدقة.",
                        "المهمة 3: استخدام الأدوات الهندسية (المسطرة، الكوس) لرسم أو قياس المخطط وتبرير الحل بجملة تامة."
                    ),
                    targetedSkills = "دمج مهارات الحساب العددي، الهندسة الفضائية، والتحليل المنطقي لحل مشكلات مركبة من الحياة اليومية."
                ),
                IntegrationProposal(
                    title = "نشاط إدماجي تطبيقي: جدول البيانات والمخططات الإحصائية البسيطة",
                    situationContext = "قراءة جدول يعرض درجات الحرارة أو كميات الأمطار المسجلة في مدن موريتانية وتفسير نتائجها حسابياً وبيانياً.",
                    tasks = listOf(
                        "المهمة 1: تنظيم الأعداد والبيانات في جدول تصنيفي منتظم.",
                        "المهمة 2: حساب الفروق الإجمالية ومقارنة الكميات باستخدام علامات المقارنة (> ، < ، =).",
                        "المهمة 3: صياغة استنتاج رياضي واضح يعكس الفهم المدمج."
                    ),
                    targetedSkills = "قراءة وتأويل البيانات الرقمية، الحساب الذهني والكتابي، والربط الرياضي المنهجي."
                )
            )

            sub.contains("إسلامية") -> listOf(
                IntegrationProposal(
                    title = "وضعية إدماجية قيمية: السلوك الأخلاقي وأداء العبادات في حياة المسلم",
                    situationContext = "ربط التلميذ بين الآيات القرآنية والأحاديث الشريفة المحفوظة في الفصل $term، وأحكام الطهارة والصلاة، وتطبيقها في مواقف الحياة اليومية (بر الوالدين، الصدق، الأمانة، التعاون).",
                    tasks = listOf(
                        "المهمة 1: استحضار وتلاوة الشواهد القرآنية والحديثية المناسبة للوضعية الأخلاقية المعروضة.",
                        "المهمة 2: تحديد الحكم الفقهي أو السلوك الصحيح في موقف يومي (مثال: كيفية إسباغ الوضوء وأداء الصلاة في وقتها).",
                        "المهمة 3: صياغة نصيحة بأسلوب طيب لزميل يوضح فيها أثر الالتزام بالقيم الإسلامية النبيلة."
                    ),
                    targetedSkills = "دمج الحفظ القرآني والحديثي مع الفهم الفقهي والممارسة السلوكية والأخلاقية الرفيعة."
                )
            )

            sub.contains("علمية") || sub.contains("تكنولوجية") || sub.contains("نشاط علمي") || sub.contains("علوم") -> listOf(
                IntegrationProposal(
                    title = "وضعية استقصاء علمي إدماجية: حماية البيئة المدرسية وترشيد الموارد الحيوية",
                    situationContext = "مشروع علمي تطبيقي يدمج معارف التغذية والصحة والمحيط البيئي والمادة (الماء والكهرباء والنفايات) للأسبوع $week.",
                    tasks = listOf(
                        "المهمة 1: ملاحظة وتصنيف ممارسات صحية وبيئية إيجابية وسلبية في محيط المدرسة.",
                        "المهمة 2: اقتراح تجربة علمية بسيطة أو خطة عمل لترشيد استهلاك الماء وفرز النفايات القابلة للتدوير.",
                        "المهمة 3: تصميم ملصق إرشادي علمي يعلل أسباب الظاهرة ويقدم حلولاً قابلة للتطبيق داخل القسم."
                    ),
                    targetedSkills = "الملاحظة العلمية، التفسير السببي، المنهج التجريبي الاستقصائي، والتطبيق البيئي الواعي."
                )
            )

            sub.contains("تاريخ") || sub.contains("جغرافيا") -> listOf(
                IntegrationProposal(
                    title = "وضعية إدماجية مكانية وزمانية: قراءة تاريخ وجغرافية الوطن الموريتاني",
                    situationContext = "توظيف الخط الزمني والخريطة الجغرافية الوطنية للربط بين المعالم التاريخية والتضاريس والمناخ والنشاط البشري للوحدة المنتهية.",
                    tasks = listOf(
                        "المهمة 1: توطين المدن التاريخية والمناطق المناخية على خريطة موريتانيا الصماء بالاعتماد على المفتاح.",
                        "المهمة 2: ترتيب الأحداث الوطنية البارزة زمنياً على سلم الوقت تصاعدياً.",
                        "المهمة 3: تفسير العلاقة بين توفر المياه والموارد الطبيعية وتوزع السكان عبر فقرة موجزة."
                    ),
                    targetedSkills = "استخدام أدوات التوطين المكاني والتسلسل الزمني والتحليل الجغرافي التاريخي التركيبي."
                )
            )

            sub.contains("مدنية") -> listOf(
                IntegrationProposal(
                    title = "محاكاة إدماجية لمجلس القسم وحقوق وواجبات التلميذ المواطن",
                    situationContext = "جلسة تشاورية انتخابية داخل القسم لتنظيم الحياة المدرسية وترسيخ قيم المواطنة والانتماء الوطني ورموز الجمهورية.",
                    tasks = listOf(
                        "المهمة 1: التمييز بين الحقوق المكفولة والواجبات المترتبة في ميثاق القسم والمؤسسة.",
                        "المهمة 2: التعريف برموز السيادة الوطنية (العلم، النشيد، الشعار) ودور المؤسسات العمومية.",
                        "المهمة 3: حل نزاع افتراضي بالحوار والتشاور مع الاحتكام للقانون الداخلي للمدرسة."
                    ),
                    targetedSkills = "ممارسة السلوك المدني، الحوار البناء، الوعي بالمسؤولية المجتمعية والمواطنة النشطة."
                )
            )

            else -> listOf(
                IntegrationProposal(
                    title = "وضعية إدماجية لغوية وتواصلية شاملة (الوحدة المنتهية في الفصل $term)",
                    situationContext = "إنتاج نص تواصلي متكامل (فقرة وصفية / سردية / حوارية) يوظف المعجم الجديد وقواعد النحو والصرف والإملاء للأسبوع $week.",
                    tasks = listOf(
                        "المهمة 1: قراءة وفهم السند اللغوي واستخراج التراكيب والأساليب المدروسة.",
                        "المهمة 2: تحويل جمل واستخدام أدوات الربط وعلامات الترقيم بشكل صحيح.",
                        "المهمة 3: كتابة فقرة تعبيرية متناسقة ومحررة بخط عربي سليم تعكس الكفاية المستهدفة."
                    ),
                    targetedSkills = "دمج مهارات القراءة الاستيعابية، البناء اللغوي النظامي، والتعبير الكتابي والشفهي الهادف."
                )
            )
        }
    }

    private fun generateAssessmentProposals(
        lesson: AnnualLessonItem,
        week: Int,
        term: Int,
        isFrench: Boolean
    ): List<AssessmentProposal> {
        val sub = lesson.subject.lowercase()

        if (isFrench) {
            return listOf(
                AssessmentProposal(
                    title = "Évaluation formative 1 : Compréhension de l'écrit et fluidité",
                    questionType = "Lecture orale et questionnaire de compréhension ciblée",
                    assessmentItems = listOf(
                        "1. Lire à voix haute un paragraphe de 20 à 30 mots sans hésitation majeure en respectant les liaisons.",
                        "2. Répondre par 'Vrai' ou 'Faux' à deux questions portant sur les faits explicites du texte.",
                        "3. Relever dans le texte un mot contenant le son étudié et un synonyme simple."
                    ),
                    successCriteria = "Décodage précis, intonation respectée et compréhension d'au moins 80% des informations clés."
                ),
                AssessmentProposal(
                    title = "Évaluation formative 2 : Outils de la langue et écriture",
                    questionType = "Exercices d'application directe et dictée de mots/phrases",
                    assessmentItems = listOf(
                        "1. Compléter des phrases lacunaires avec les pronoms ou accords convenables.",
                        "2. Transformer une phrase affirmative en phrase négative ou interrogative.",
                        "3. Écrire sous la dictée 3 phrases simples intégrant les graphèmes révisés."
                    ),
                    successCriteria = "Application correcte des règles syntaxiques et orthographiques de base."
                )
            )
        }

        return when {
            sub.contains("رياضيات") -> listOf(
                AssessmentProposal(
                    title = "روائز التقويم التحصيلي 1: الحساب الذهني والعمليات العددية الأساسية",
                    questionType = "بطاقات الحساب السريع وإنجاز العمليات العمودية",
                    assessmentItems = listOf(
                        "1. إنجاز 4 عمليات جمع وطرح أفقية بحساب ذهني فوري خلال دقيقتين.",
                        "2. وضع وإنجاز عمليتين عموديتين بالاحتفاظ أو التحويل بدقة على كراس التمارين.",
                        "3. ترتيب سلسلة من 5 أعداد تصاعدياً وتنازلياً ومقارنتها باستخدام الرموز الرياضية."
                    ),
                    successCriteria = "دقة الحساب وخلو الإنجاز من أخطاء نقل المنازل أو إغفال الاحتفاظ بنسبة نجاح تفوق 75%."
                ),
                AssessmentProposal(
                    title = "روائز التقويم التحصيلي 2: حل الوضعيات المشكلة والهندسة والقياس",
                    questionType = "مسألة رياضية دالة ورسم هندسي موجه",
                    assessmentItems = listOf(
                        "1. قراءة نص المسألة، استخراج المعطيات والأسئلة وتحديد العملية المناسبة مع كتابة الحل النموذجي.",
                        "2. قياس أطوال قطع مستقيمة باستخدام المسطرة المدرجة بدقة السنتيمتر والمليمتر.",
                        "3. التعرف على الأشكال الهندسية ومجسماتها (المثلث، المربع، المستطيل، المكعب) وتسمية عناصرها."
                    ),
                    successCriteria = "سلامة المنهجية الرياضية، صحة الحساب، ووضوح التبرير الرياضي."
                )
            )

            sub.contains("إسلامية") -> listOf(
                AssessmentProposal(
                    title = "شبكة تقويم حفظ القرآن الكريم والحديث النبوي الشريف",
                    questionType = "تسميع شفوي فردي مع أحكام الترتيل ومخارج الحروف",
                    assessmentItems = listOf(
                        "1. استظهار السورة المقررة حفظاً متقناً مراعياً علامات الوقف ومخارج الحروف.",
                        "2. تسميع الحديث النبوي الشريف الشاهد على موضوع الوحدة وضبط مفرداته.",
                        "3. الإجابة عن سؤالين في شرح المعنى الإجمالي للآيات ومقاصدها الإيمانية."
                    ),
                    successCriteria = "الحفظ السلس بدون تعثرات متكررة، والقدرة على توضيح الفكرة الإيمانية الأساسية."
                ),
                AssessmentProposal(
                    title = "تقويم العبادات والسيرة النبوية والآداب الإسلامية",
                    questionType = "أسئلة موضوعية وبطاقات تصنيف الأحكام الفقهية",
                    assessmentItems = listOf(
                        "1. تصنيف أفعال الوضوء والصلاة إلى (فرائض / سنن / مبطلات) في جدول مخصص.",
                        "2. ذكر حدثين بارزين من السيرة النبوية الشريفة المدروسة في هذه المحطة.",
                        "3. التعليق على موقف سلوكي يبين أثر الصدق وبر الوالدين في المعاملات."
                    ),
                    successCriteria = "استيعاب الأحكام التكليفية والتحلي بالآداب الإسلامية القويمة."
                )
            )

            sub.contains("علمية") || sub.contains("تكنولوجية") || sub.contains("نشاط علمي") || sub.contains("علوم") -> listOf(
                AssessmentProposal(
                    title = "شبكة تقويم المعارف والمفاهيم العلمية والتكنولوجية",
                    questionType = "أسئلة اختيار من متعدد ورسم تخطيطي وتصنيف علمي",
                    assessmentItems = listOf(
                        "1. تسمية أعضاء الجهاز المدروس أو مكونات الدارة الكهربائية على رسم تخطيطي مرفق.",
                        "2. تصنيف الكائنات الحية أو المواد حسب الخصائص الفيزيائية والبيولوجية المكتسبة.",
                        "3. تفسير ظاهرة طبيعية (التنفس، الإنبات، دورة الماء، حالات المادة) بأسلوب علمي سليم."
                    ),
                    successCriteria = "صحة التسميات العلمية، دقة الرسم التخطيطي، ومنطقية التفسير السببي."
                )
            )

            else -> listOf(
                AssessmentProposal(
                    title = "شبكة تقويم القراءة والاستيعاب والتراكيب اللغوية",
                    questionType = "قراءة جهرية مقننة، أسئلة فهم صريح وضمني، وتطبيقات لغوية",
                    assessmentItems = listOf(
                        "1. قراءة فقرة من 30 كلمة قراءة جهرية سليمة ومعبرة ومضبوطة بالشكل.",
                        "2. استخراج الفكرة العامة للنص وتحديد معاني المفردات الجديدة بالمرادف والضد.",
                        "3. ضبط جمل بالشكل التام وتحديد موقع الكلمات الإعرابي أو الصرفي المطلوب.",
                        "4. كتابة 3 جمل تامة في تعبير كتابي تشتمل على الشواهد والمهارات المستهدفة."
                    ),
                    successCriteria = "احترام الحركات الإعرابية، الانطلاق القرائي السليم، وسلامة التراكيب الإنشائية."
                )
            )
        }
    }

    private fun generateRemediationProposals(
        lesson: AnnualLessonItem,
        week: Int,
        term: Int,
        isFrench: Boolean
    ): List<RemediationProposal> {
        val sub = lesson.subject.lowercase()

        if (isFrench) {
            return listOf(
                RemediationProposal(
                    expectedDifficulty = "Difficulté 1 : Confusion auditive et graphique entre les graphèmes proches (b/d, p/b, ou/on).",
                    remediationActivity = "Activité ciblée : Exercices de discrimination auditive avec 'chasse aux sons' et traçage gestuel des lettres sur l'ardoise et dans l'espace.",
                    pedagogicalSupport = "Étiquettes mobiles de syllabes, fiches phonologiques illustrées et travail en binôme tutoré."
                ),
                RemediationProposal(
                    expectedDifficulty = "Difficulté 2 : Oubli des accords grammaticaux simples (marque du pluriel -s, terminaisons verbales du présent).",
                    remediationActivity = "Activité ciblée : Jeu de substitution de sujets et transformation mécanique de phrases simples en surlignant les terminaisons à la craie de couleur.",
                    pedagogicalSupport = "Tableaux de conjugaison visuels simplifiés et fiches d'auto-correction guidée."
                ),
                RemediationProposal(
                    expectedDifficulty = "Difficulté 3 : Hésitation et manque de vocabulaire lors de la prise de parole spontanée.",
                    remediationActivity = "Activité ciblée : Jeu de rôle guidé à l'aide de marionnettes ou d'images séquentielles structurées avec une boîte à répliques.",
                    pedagogicalSupport = "Boîte à mots illustrée et valorisation bienveillante de chaque essai oral."
                )
            )
        }

        return when {
            sub.contains("رياضيات") -> listOf(
                RemediationProposal(
                    expectedDifficulty = "الصعوبة 1: الخلط في وضع الأعداد عمودياً وعدم محاذاة الوحدات تحت الوحدات والعشرات تحت العشرات، أو إغفال رقم الاحتفاظ.",
                    remediationActivity = "النشاط العلاجي المقترح: استخدام جدول المنازل الملون وأوراق الحساب المخططة شبكياً، مع التمثيل بالمحسوس (قطع الوحدات وأعمدة العشرات).",
                    pedagogicalSupport = "لوحة المنازل البلاستيكية الفردية، قوالب دينز المحسوسة، وبطاقات تذكيرية بقاعدة الاحتفاظ."
                ),
                RemediationProposal(
                    expectedDifficulty = "الصعوبة 2: العجز عن تفكيك المسألة الرياضية واختيار العملية الحسابية الصحيحة (جمع، طرح، ضرب).",
                    remediationActivity = "النشاط العلاجي المقترح: نمذجة المسألة برسم مخطط تمثيلي مبسط، وتلوين الكلمات المفتاحية الدالة (زاد، نقص، وزع بالتساوي، المجموع الإجمالي).",
                    pedagogicalSupport = "بطاقات استراتيجية حل المشكلات في 4 خطوات (اقرأ، مثل، احسب، تحقق)."
                ),
                RemediationProposal(
                    expectedDifficulty = "الصعوبة 3: عدم الدقة في القياس بالمسطرة والخلط بين نقطة البداية (الصفر) وحافة المسطرة الخشبية.",
                    remediationActivity = "النشاط العلاجي المقترح: تدريب حسي حركي ثنائي على تثبيت المسطرة بأصابع اليد اليسرى والتحقق من محاذاة الصفر قبل رسم القطعة المستقيمة.",
                    pedagogicalSupport = "مساطر تعليمية ذات تدريج مكبر ملون وتوجيه فردي مباشر من المعلم."
                )
            )

            sub.contains("إسلامية") -> listOf(
                RemediationProposal(
                    expectedDifficulty = "الصعوبة 1: تعثر في حفظ السورة القرآنية أو تداخل الآيات المتشابهة لدى بعض التلاميذ.",
                    remediationActivity = "النشاط العلاجي المقترح: تقسيم السورة إلى مقاطع قصيرة (آيتين بكل حصة علاجية)، التكرار الجماعي والثنائي، وتفعيل الاستماع الصوتي النقي.",
                    pedagogicalSupport = "المصحف المعلم المرتل، بطاقات الآيات المجزأة لترتيبها تسلسلياً."
                ),
                RemediationProposal(
                    expectedDifficulty = "الصعوبة 2: الخلط بين فرائض الوضوء والصلاة وسننها ومبطلاتها.",
                    remediationActivity = "النشاط العلاجي المقترح: التطبيق الحركي العملي أمام الزملاء مع لعبة فرز بطاقات الأحكام في عمودين (فرض / سنة).",
                    pedagogicalSupport = "ملصقات مصورة توضح هيئات الصلاة والوضوء بالترتيب الصحيح."
                )
            )

            sub.contains("علمية") || sub.contains("تكنولوجية") || sub.contains("نشاط علمي") || sub.contains("علوم") -> listOf(
                RemediationProposal(
                    expectedDifficulty = "الصعوبة 1: الخلط بين المصطلحات العلمية (مثال: الذوبان والانصهار، الجسم الناقل والعازل، الشهيق والزفير).",
                    remediationActivity = "النشاط العلاجي المقترح: إجراء تجارب مصغرة مباشرة في القسم بالمواد المحسوسة وصياغة الملاحظات الفورية في جدول مقارنة.",
                    pedagogicalSupport = "علبة أدوات تجريبية بسيطة، وبطاقات مفاهيمية ثنائية التناقض."
                ),
                RemediationProposal(
                    expectedDifficulty = "الصعوبة 2: صعوبة تتبع خطوات المنهج التجريبي واستخلاص النتائج بصورة منطقية.",
                    remediationActivity = "النشاط العلاجي المقترح: تزويد التلميذ ببطاقة استقصاء موجهة تحتوي على أسئلة مرحلية (ماذا ألاحظ؟ ماذا أفعل؟ ماذا أستنتج؟).",
                    pedagogicalSupport = "دفتر التقصي العلمي المنظم بشبكات توجيه بصرية."
                )
            )

            else -> listOf(
                RemediationProposal(
                    expectedDifficulty = "الصعوبة 1: التعثر في التهجئة والقراءة المسترسلة للكلمات التي تتضمن حروفاً متشابهة رسماً أو صوتاً (د/ض، ت/ط، س/ص).",
                    remediationActivity = "النشاط العلاجي المقترح: التدريب على المقاطع الصوتية المنفصلة (الوعي الفونولوجي)، واستخدام السلم القرائي المتدرج من الحرف إلى الكلمة إلى الجملة.",
                    pedagogicalSupport = "لوحة الحروف البارزة، بطاقات الكلمات المقطعية، وجلسات القراءة الثنائية التفاعلية."
                ),
                RemediationProposal(
                    expectedDifficulty = "الصعوبة 2: الوقوع في الأخطاء الإملائية الشائعة (التاء المربوطة والمفتوحة، التنوين والهمزات).",
                    remediationActivity = "النشاط العلاجي المقترح: الإملاء المنظور الذاتي، وتطبيق قاعدة الوقف بالسكون للتمييز بين الهاء والتاء، ومقارنة النماذج بصرياً.",
                    pedagogicalSupport = "جداول الضوابط الإملائية المبسطة، وبطاقات التمييز البصري الملونة."
                ),
                RemediationProposal(
                    expectedDifficulty = "الصعوبة 3: العجز عن صياغة جملة مفيدة تامة الأركان في التعبير الكتابي.",
                    remediationActivity = "النشاط العلاجي المقترح: استراتيجية ترتيب الكلمات المبعثرة لبناء جمل، ثم التوسع بإضافة النعت والجار والمجرور تدريجياً.",
                    pedagogicalSupport = "بنك الكلمات والتراكيب التعبيرية وقوالب الجمل النموذجية."
                )
            )
        }
    }
}
