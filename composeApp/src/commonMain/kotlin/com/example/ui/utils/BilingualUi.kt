package com.example.ui.utils

import com.example.compat.*
import kotlinx.coroutines.IO

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

/**
 * واجهة ثنائية اللغة: العربية هي الأساس، والفرنسية تحتها بحجم أصغر (لمعلمي الفرنسية غير الناطقين بالعربية).
 * القاعدة: لا تُترجم الأرقام ولا الأسماء ولا البيانات، فقط عبارات الواجهة.
 */
object BiFr {
    /** زر «Traduire»: إذا كان مفعّلاً تظهر الفرنسية تحت العربية في الواجهات والكشوف، وإلا تبقى كما كانت. */
    var enabled by mutableStateOf(false)

    private val emojiRegex = Regex("[\\u2190-\\u2BFF\\uFE0F\\u200D]|[\\uD83C-\\uD83E][\\uDC00-\\uDFFF]")

    private fun norm(s: String): String =
        s.replace(emojiRegex, "").replace(Regex("\\s+"), " ").trim()

    private val extra: Map<String, String> = mapOf(
        "الرقم (التسلسلي) يظهر دائماً. الاسم:" to "Le numéro (de série) s'affiche toujours. Nom :",
        "الملاحظات الصحية" to "Remarques de santé",
        "باقي الأعمدة:" to "Autres colonnes :",
        "سحب اللائحة" to "Exporter la liste",
        "طالب جديد" to "Nouvel élève",
        "ما الذي تريد إظهاره في اللائحة؟" to "Que voulez-vous afficher dans la liste ?",
        "هاتف الوكيل 1" to "Téléphone du tuteur 1",
        "هاتف الوكيل 2" to "Téléphone du tuteur 2",
        // الشريط السفلي
        "الطلاب" to "Élèves",
        "النتائج" to "Résultats",
        "الكشوف" to "Relevés",
        "التخطيط السنوي" to "Planif. annuelle",
        "الجدول الزمني" to "Emploi du temps",
        // الصفحة الرئيسية
        "إنشاء قسم" to "Créer classe",
        "دفتر المعلم" to "Cahier de l'enseignant",
        "دفتر المعلم - اختيار السنة الدراسية" to "Cahier de l'enseignant – Choix de l'année scolaire",
        "اختر السنة الدراسية للعمل عليها" to "Choisissez l'année scolaire de travail",
        "العام الدراسي" to "Année scolaire",
        "العام الدراسي:" to "Année scolaire :",
        "السنة الدراسية" to "Année scolaire",
        "السنة الدراسية:" to "Année scolaire :",
        "جارية" to "En cours",
        "أقسام دراسية مسجلة" to "classes enregistrées",
        "تطبيقي المدرسي" to "Mon application scolaire",
        "لوحة تسيير مدير الأقسام" to "Tableau de gestion des classes",
        // عناوين الأقسام الخارجية
        "تبادل المعلمين (بين الولايات)" to "Échanges d'enseignants (entre wilayas)",
        "تبادلات المعلمين (بين الولايات)" to "Échanges d'enseignants (entre wilayas)",
        "حول التطبيق" to "À propos de l'application",
        "التشريع المدرسي" to "Législation scolaire",
        "دورة تحضير مسابقات المعلمين" to "Préparation aux concours des enseignants",
        // القائمة الجانبية
        "القائمة الجانبية للتطبيق" to "Menu de l'application",
        "الصفحة الرئيسية والأقسام" to "Accueil et classes",
        "تصميم اللوائح والكشوف" to "Design des listes et relevés",
        "كتبي" to "Mes livres",
        "تسجيل الخروج" to "Se déconnecter",
        // الجدول الزمني
        "الجدول الزمني للقسم" to "Emploi du temps de la classe",
        "رسمي" to "Officiel",
        "شخصي" to "Personnel",
        "الفترة الصباحية الأولى" to "Première période du matin",
        "الفترة الصباحية الثانية" to "Deuxième période du matin",
        "الفترة المسائية" to "Période de l'après-midi",
        "مشاركة PDF" to "Partager en PDF",
        "العودة للجدول الرسمي" to "Retour à l'emploi du temps officiel",
        "عدّل التاريخ الهجري بعد انتهاء كل اجتماع للجنة الأهلة ليبقى التاريخ الهجري صحيحًا ومحليًا دون إنترنت" to
            "Ajustez la date hégirienne après chaque réunion du comité des croissants pour qu'elle reste exacte, en local et sans internet",
        "استراحة مبرمجة بيداغوجياً" to "Récréation programmée",
        "لا توجد حصص مبرمجة في هذه الفترة" to "Aucune séance programmée dans cette période",
        "لا توجد حصص مبرمجة في الفترة المسائية" to "Aucune séance programmée l'après-midi",
        "15 دقيقة" to "15 min",
        "الإثنين" to "Lundi",
        "الثلاثاء" to "Mardi",
        "الأربعاء" to "Mercredi",
        "الخميس" to "Jeudi",
        "الجمعة" to "Vendredi",
        "السبت" to "Samedi",
        "الأحد" to "Dimanche"
    )

    private val subjects: Map<String, String> = mapOf(
        "التربية الإسلامية" to "Éducation islamique",
        "القرآن الكريم" to "Coran",
        "الأخلاق" to "Morale",
        "الحديث الشريف" to "Hadith",
        "السيرة النبوية" to "Biographie du Prophète",
        "العبادات" to "Cultes",
        "العقيدة" to "Dogme",
        "العقيدة / الأخلاق" to "Dogme / Morale",
        "التاريخ" to "Histoire",
        "الجغرافيا" to "Géographie",
        "التاريخ والجغرافيا" to "Histoire et géographie",
        "التربية البدنية" to "Éducation physique",
        "التربية العلمية والتكنولوجية" to "Éducation scientifique et technologique",
        "التقنيات" to "Techniques",
        "تقنيات الإعلام والاتصال" to "TIC",
        "التربية الفنية" to "Éducation artistique",
        "الأعمال اليدوية والرسم" to "Travaux manuels et dessin",
        "الرسم / الأعمال اليدوية" to "Dessin / Travaux manuels",
        "التربية المدنية" to "Éducation civique",
        "التربية على المواطنة" to "Éducation à la citoyenneté",
        "السلوك المدني" to "Comportement civique",
        "السلوك المدني / المهارات الحياتية" to "Comportement civique / Compétences de vie",
        "المهارات الحياتية" to "Compétences de vie",
        "الرياضيات" to "Mathématiques",
        "الحساب" to "Calcul",
        "القياس" to "Mesure",
        "الهندسة" to "Géométrie",
        "العلوم" to "Sciences",
        "البيئة" to "Environnement",
        "البيئة / التجريب" to "Environnement / Expérimentation",
        "الصحة" to "Santé",
        "الصحة والتغذية" to "Santé et nutrition",
        "قواعد السلامة" to "Règles de sécurité",
        "اللغة العربية" to "Langue arabe",
        "الإملاء والخط العربي" to "Dictée et calligraphie arabe",
        "التعبير" to "Expression",
        "القراءة" to "Lecture",
        "القراءة والفهم" to "Lecture et compréhension",
        "الكتابة" to "Écriture",
        "قواعد النحو والصرف" to "Grammaire arabe"
    )


    /** أسماء الولايات والمقاطعات بالفرنسية (حسب القائمة الرسمية). */
    private val geo: Map<String, String> = mapOf(
        "الحوض الشرقي" to "Hodh Ech Chargui", "الحوض الغربي" to "Hodh El Gharbi", "لعصابه" to "Assaba",
        "كوركول" to "Gorgol", "لبراكنه" to "Brakna", "اترارزه" to "Trarza", "آدرار" to "Adrar",
        "داخلت نواذيبو" to "Dakhlet Nouadhibou", "تكانت" to "Tagant", "كيدي ماغا" to "Guidimagha",
        "تيرس زمور" to "Tiris Zemmour", "إنشيري" to "Inchiri",
        "نواكشوط الشمالية" to "Nouakchott-Nord", "نواكشوط الغربية" to "Nouakchott-Ouest", "نواكشوط الجنوبية" to "Nouakchott-Sud",
        "النعمة" to "Néma", "تمبدغة" to "Timbédra", "آمرج" to "Amourj", "باسكنو" to "Bassikounou", "جكني" to "Djigueni",
        "ولاته" to "Oualata", "عدل بكرو" to "Adel Bagrou", "انبيكت لحواش" to "N'Beiket Lehwach",
        "لعيون" to "Aïoun", "الطينطان" to "Tintane", "كوبني" to "Koubenni", "تامشكط" to "Tamchekett", "اطويل" to "Touil",
        "كيفه" to "Kiffa", "كرو" to "Guerou", "كنكوصة" to "Kankossa", "بومديد" to "Boumdeid", "باركيول" to "Barkéol",
        "كيهيدي" to "Kaédi", "مونغل" to "Monguel", "مقامة" to "Maghama", "إمبود" to "M'Bout", "لكصيبة" to "Lexeiba",
        "ألاك" to "Aleg", "بوكى" to "Boghé", "بابابى" to "Bababé", "مبان" to "M'Bagne", "مقطع لحجار" to "Magta Lahjar", "مال" to "Male",
        "روصو" to "Rosso", "بوتلميت" to "Boutilimit", "المذرذرة" to "Méderdra", "واد الناقة" to "Ouad Naga",
        "اركيز" to "R'Kiz", "كرمسين" to "Keur Macène", "انتيكان" to "Tékane",
        "أطار" to "Atar", "شنقيط" to "Chinguetti", "وادان" to "Ouadane", "أوجفت" to "Aoujeft",
        "نواذيبو" to "Nouadhibou", "الشامي" to "Chami",
        "تجكجة" to "Tidjikja", "المجرية" to "Moudjéria", "تيشيت" to "Tichitt",
        "سيلبابي" to "Sélibaby", "ولد ينجة" to "Ould Yengé", "غابو" to "Ghabou", "ومبو" to "Wompou",
        "ازويرات" to "Zouérate", "فديرك" to "F'Déirick", "بير أم اكرين" to "Bir Moghrein",
        "أكجوجت" to "Akjoujt", "بنشاب" to "Bénichab",
        "دار النعيم" to "Dar Naïm", "تيارت" to "Teyarett", "توجنين" to "Toujounine",
        "تفرغ زينة" to "Tevragh Zeina", "لكصر" to "Ksar", "السبخة" to "Sebkha",
        "عرفات" to "Arafat", "الميناء" to "El Mina", "الرياض" to "Riyad"
    )

    /** الاسم الفرنسي لولاية أو مقاطعة إن عُرف. */
    fun geoFr(ar: String): String? {
        val t = ar.trim()
        geo[t]?.let { return it }
        // أرقام لاحقة مثل «لكصيبه 1»: نترجم الاسم ونُبقي الرقم
        val m = Regex("^(.*?)\\s*([0-9٠-٩]+)$").find(t)
        val base = m?.groupValues?.get(1)?.trim() ?: t
        val suffix = m?.groupValues?.get(2)
        val hit = geoNorm[geoKey(base)] ?: return null
        return if (suffix != null) "$hit $suffix" else hit
    }

    private fun geoKey(s: String): String =
        s.trim().replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ة', 'ه').replace('ى', 'ي')

    private val geoAliases: Map<String, String> = mapOf("غوركل" to "Gorgol")

    private val geoNorm: Map<String, String> by lazy {
        val m = HashMap<String, String>()
        for ((k, v) in geo) m[geoKey(k)] = v
        for ((k, v) in geoAliases) m[geoKey(k)] = v
        m
    }

    /** الاسم الفرنسي لمادة إن عُرف. */
    fun subjectFr(ar: String): String? = subjects[norm(ar)]

    /** «الحوض الشرقي - Hodh Ech Chargui»؛ وإن لم يوجد اسم فرنسي يبقى العربي وحده. */
    fun geoLabel(ar: String): String {
        if (!enabled) return ar
        val f = geo[ar.trim()] ?: return ar
        return "$ar - $f"
    }

    /** الترجمة الفرنسية لعبارة واجهة عربية، أو null إن لم تتوفر (فيظهر العربي وحده). */
    fun of(ar: String): String? {
        val k = norm(ar)
        if (k.isEmpty()) return null
        // نص فيه حروف لاتينية أصلاً (مثل Français) لا يُترجم
        if (k.any { it in 'a'..'z' || it in 'A'..'Z' }) return null
        return extra[k] ?: subjects[k] ?: UiFrench.map[ar] ?: UiFrench.map[k]
    }

    /** نفس of لكن لنصوص فيها متغيرات {0} {1} ... */
    fun f(ar: String, vararg args: Any?): String? {
        var out = of(ar) ?: return null
        args.forEachIndexed { i, a -> out = out.replace("{" + i + "}", a?.toString() ?: "") }
        return out
    }

    /** اسم المادة مع فرعها بالفرنسية: «Éducation islamique (Coran)» */
    fun subjectLine(subject: String, branch: String): String? {
        val s = subjects[norm(subject)] ?: return null
        val b = branch.trim()
        if (b.isEmpty() || b == subject.trim()) return s
        val fb = subjects[norm(b)] ?: return s
        return "$s ($fb)"
    }

    private val frMonths = listOf(
        "janvier", "février", "mars", "avril", "mai", "juin",
        "juillet", "août", "septembre", "octobre", "novembre", "décembre"
    )
    private val frDays = listOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche")
    private val frHijriMonths = listOf(
        "Mouharram", "Safar", "Rabi' al-awwal", "Rabi' ath-thani",
        "Joumada al-oula", "Joumada ath-thania", "Rajab", "Chaabane",
        "Ramadan", "Chawwal", "Dhou al-qi'da", "Dhou al-hijja"
    )

    /** «Lundi 26 octobre 2026» (الأرقام كما هي). */
    fun gregorian(d: LocalDate): String =
        "${frDays[d.dayOfWeek.value - 1]} ${d.dayOfMonth} ${frMonths[d.monthValue - 1]} ${d.year}"

    /** «15 Joumada al-oula 1448 H». */
    fun hijri(day: Int, month: Int, year: Int): String =
        "$day ${frHijriMonths.getOrElse(month - 1) { month.toString() }} $year H"
}

/**
 * نص عربي أساسي وتحته الفرنسية بحجم أصغر. إن لم توجد ترجمة يظهر العربي وحده.
 * [fr] يُمرَّر يدوياً للنصوص الديناميكية (التواريخ مثلاً).
 */
@Composable
fun BiText(
    ar: String,
    modifier: Modifier = Modifier,
    fr: String? = BiFr.of(ar),
    fontSize: TextUnit = 14.sp,
    frSize: TextUnit = fontSize * 0.72f,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    center: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = ar,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (center) TextAlign.Center else null
        )
        if (BiFr.enabled && !fr.isNullOrBlank()) {
            Text(
                text = fr,
                fontSize = frSize,
                fontWeight = FontWeight.Medium,
                color = if (color == Color.Unspecified) color else color.copy(alpha = color.alpha * 0.85f),
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (center) TextAlign.Center else null
            )
        }
    }
}


/** مثل BiText لكن يرث حجم الخط من السياق إن لم يُحدَّد (للتحويل التلقائي للأزرار والنصوص). */
@Composable
fun BiTextA(
    ar: String,
    modifier: Modifier = Modifier,
    fr: String? = BiFr.of(ar),
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    center: Boolean = false
) {
    val base = if (fontSize != TextUnit.Unspecified) fontSize
        else androidx.compose.material3.LocalTextStyle.current.fontSize.let { if (it == TextUnit.Unspecified) 14.sp else it }
    BiText(
        ar = ar, modifier = modifier, fr = fr, fontSize = base, frSize = base * 0.72f,
        fontWeight = fontWeight, color = color, maxLines = maxLines, center = center
    )
}
