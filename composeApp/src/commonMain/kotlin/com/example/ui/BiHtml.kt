package com.example.ui

import com.example.compat.*
import kotlinx.coroutines.IO

import com.example.ui.utils.BiFr

/**
 * ترجمة فرنسية صغيرة لكشوف الدرجات ورأسيات اللوائح داخل ملفات PDF/الطباعة.
 * تُضاف الفرنسية تحت العبارة العربية بحجم أصغر دون تغيير أي رقم أو اسم أو بيان، وتُطبَّق مرة واحدة عند الطباعة.
 */
object BiHtml {
    private const val MARK = "bi-fr-css"

    private const val CSS = "<style id=\"$MARK\">" +
        ".bi-fr{display:block;direction:ltr;unicode-bidi:isolate;font-size:.64em;font-weight:normal;line-height:1.15;text-align:inherit;color:#222;}" +
        ".bi-hdr{display:block;direction:ltr;unicode-bidi:isolate;font-size:.66em;font-weight:normal;line-height:1.15;color:#222;}" +
        ".bi-hdr bdi{direction:rtl;unicode-bidi:isolate;}" +
        ".remarks-header{height:auto !important;min-height:30px !important;}" +
        ".bi-i{direction:ltr;unicode-bidi:isolate;font-size:.62em;font-weight:normal;color:#222;}" +
        "</style>"

    private val titles: Map<String, String> = mapOf(
        "كشف الامتحان الأول" to "Relevé du 1er examen",
        "كشف الامتحان الثاني" to "Relevé du 2e examen",
        "كشف الامتحان الثالث" to "Relevé du 3e examen",
        "كشف درجات الفصل الأخير" to "Relevé de notes du dernier trimestre",
        "(امتحان التجاوز)" to "(Examen de passage)",
        "لائحة التلاميذ" to "Liste des élèves",
        "الجدول الزمني الأسبوعي" to "Emploi du temps hebdomadaire",
        "سجل النداء" to "Registre d'appel",
        "سجل معلم الفرنسية" to "Registre de l'enseignant de français",
        "مجموعات النشيد المدرسي" to "Groupes de l'hymne scolaire",
        "جدول النظافة والكناسة الدوري" to "Tableau périodique de nettoyage",
        "لائحة نتائج امتحان التجاوز" to "Liste des résultats de l'examen de passage"
    )

    private val cells: Map<String, String> = mapOf(
        "المادة" to "Matière",
        "النقاط" to "Notes",
        "مجموع النقاط" to "Total des points",
        "معدل الامتحان الأول" to "Moyenne du 1er examen",
        "معدل الامتحان الثاني" to "Moyenne du 2e examen",
        "معدل الامتحان الثالث" to "Moyenne du 3e examen",
        "المعدل العام (السنوي)" to "Moyenne générale (annuelle)",
        "القرار أو الملاحظة" to "Décision / Observation",
        "الرتبة العامة في القسم" to "Rang général dans la classe",
        "معدل الفصل" to "Moyenne du trimestre",
        "النتيجة" to "Résultat",
        "الرتبة في القسم" to "Rang dans la classe",
        "الملاحظات والقرار" to "Observations et décision",
        "توقيع المعلم" to "Signature de l'enseignant",
        "توقيع الوكيل" to "Signature de l'adjoint",
        "توقيع المدير" to "Signature du directeur"
    )

    private val inlineLabels: Map<String, String> = mapOf(
        "الاسم الكامل:" to "Nom complet",
        "رقم النداء:" to "N° d'appel",
        "الرقم المدرسي:" to "RIM"
    )

    private val headerColumn = Regex(
        "(<div class=\"header-column (?:right|left)\"[^>]*>)(.*?)(</div>)",
        RegexOption.DOT_MATCHES_ALL
    )
    private val brTag = Regex("<br\\s*/?>", RegexOption.IGNORE_CASE)
    private val anyTag = Regex("<[^>]*>")

    private fun term(v: String): String = when (v.trim()) {
        "الأول" -> "1er trimestre"
        "الثاني" -> "2e trimestre"
        "الثالث" -> "3e trimestre"
        "الأخير" -> "Dernier trimestre"
        else -> "Trimestre : " + v.trim()
    }


    // ---- نقل اسم المدرسة بالحروف اللاتينية (تقريبي، لأن العربية تُكتب بلا حركات) ----
    private val cons: Map<Char, String> = mapOf(
        'ب' to "b", 'ت' to "t", 'ث' to "th", 'ج' to "dj", 'ح' to "h", 'خ' to "kh", 'د' to "d", 'ذ' to "dh",
        'ر' to "r", 'ز' to "z", 'س' to "s", 'ش' to "ch", 'ص' to "s", 'ض' to "d", 'ط' to "t", 'ظ' to "z",
        'ع' to "a", 'غ' to "gh", 'ف' to "f", 'ق' to "k", 'ك' to "k", 'ل' to "l", 'م' to "m", 'ن' to "n", 'ه' to "h"
    )
    private val vows: Map<Char, String> = mapOf(
        'ا' to "a", 'أ' to "a", 'إ' to "i", 'آ' to "a", 'ى' to "a", 'ء' to "", 'ؤ' to "ou", 'ئ' to "i", 'و' to "ou", 'ي' to "i"
    )
    private val knownWords: Map<String, String> = mapOf(
        "محمد" to "Mohamed", "احمد" to "Ahmed", "أحمد" to "Ahmed", "عبد" to "Abdel", "الله" to "Allah",
        "بن" to "Ben", "ابن" to "Ibn", "علي" to "Ali", "عثمان" to "Othmane", "عمر" to "Omar",
        "ابو" to "Abou", "أبو" to "Abou", "عائشة" to "Aïcha", "فاطمة" to "Fatima", "خديجة" to "Khadija",
        "الجمهورية" to "République"
    )

    private fun translitWord(w0: String): String {
        knownWords[w0]?.let { return it }
        var w = w0
        var pre = ""
        if (w.startsWith("ال") && w.length > 3) { pre = "El "; w = w.substring(2) }
        val out = StringBuilder()
        var cnt = 0
        var prevVowel = true
        val n = w.length
        for (i in 0 until n) {
            val ch = w[i]
            val nxt = if (i + 1 < n) w[i + 1] else ' '
            if ((ch == 'ة' || ch == 'ه') && i == n - 1) { out.append("a"); cnt = 0; prevVowel = true; continue }
            val v = vows[ch]
            if (v != null) {
                var sv: String = v
                if (prevVowel && out.isNotEmpty() && ch == 'ي') sv = "y"
                else if (prevVowel && out.isNotEmpty() && ch == 'و') sv = "w"
                out.append(sv); cnt = 0
                if (sv.isNotEmpty()) prevVowel = true
                continue
            }
            val c = cons[ch]
            if (c != null) {
                out.append(c); cnt++; prevVowel = false
                val nextIsFinalH = (nxt == 'ة' || nxt == 'ه') && i + 1 == n - 1
                if (cnt == 1 && cons.containsKey(nxt) && !nextIsFinalH) out.append("a")
                continue
            }
            out.append(ch); cnt = 0; prevVowel = true
        }
        val r = out.toString()
        return pre + (if (r.isEmpty()) r else r.substring(0, 1).uppercase() + r.substring(1))
    }

    /** يُحوّل كل كلمة عربية في اسم المدرسة إلى حروف لاتينية، ويُبقي الأرقام والحروف اللاتينية كما هي. */
    private fun translit(name: String): String =
        name.trim().split(Regex("\\s+")).joinToString(" ") { translitWord(it) }

    private val schoolBi = Regex("^(مدرسة\\s*:?.*?)\\s+[-–—]\\s+([A-Za-zÀ-ÿ][A-Za-zÀ-ÿ0-9 '’.\\-]*)$")

    private fun place(v: String): String = BiFr.geoFr(v.trim()) ?: v.trim()

    /** الفرنسية لسطر من رأسية اللائحة، أو null إن لم يكن سطراً معروفاً. */
    private fun headerLine(plain: String): String? {
        val t = plain.trim()
        if (t.isEmpty()) return null
        if (t == "الجمهورية الإسلامية الموريتانية") return "République Islamique de Mauritanie"
        if (t == "وزارة التربية وإصلاح النظام التعليمي") return "Ministère de l'Éducation nationale et de la RSE"
        if (t.startsWith("شرف - إخاء - عدالة")) return "Honneur - Fraternité - Justice"
        fun value(prefixes: List<String>): String? {
            for (p in prefixes) if (t.startsWith(p)) {
                val rest = t.removePrefix(p).trimStart()
                if (rest.startsWith(":")) return rest.removePrefix(":").trim()
            }
            return null
        }
        value(listOf("الإدارة الجهوية للتربية في", "الإدارة الجهوية للتربية", "الإدارة الجهوية"))?.let { return "DREN : <bdi>${place(it)}</bdi>" }
        value(listOf("مفتشية مقاطعة", "مفتشية"))?.let { return "IDEN : <bdi>${place(it)}</bdi>" }
        value(listOf("مدرسة"))?.let { return "École : <bdi>${translit(it)}</bdi>" }
        value(listOf("السنة الدراسية"))?.let { return "Année scolaire : <bdi>$it</bdi>" }
        value(listOf("القسم"))?.let { return "Classe : <bdi>$it</bdi>" }
        value(listOf("المادة"))?.let { return "Matière : <bdi>${BiFr.subjectFr(it) ?: it}</bdi>" }
        value(listOf("الفصل"))?.let { return "<bdi>${term(it)}</bdi>" }
        return null
    }

    fun translate(html: String): String {
        if (!BiFr.enabled) return html
        if (html.contains(MARK)) return html
        var out = html

        // 1) أعمدة الرأسية (يمين/يسار): تحت كل سطر عربي معروف سطر فرنسي صغير
        out = headerColumn.replace(out) { m ->
            val lines = m.groupValues[2].split(brTag)
            val sb = StringBuilder()
            lines.forEachIndexed { i, raw ->
                var plain = raw.replace(anyTag, "").replace("&nbsp;", " ")
                var rawLine = raw
                var fr: String? = null
                // اسم المدرسة «العربي - الفرنسي»: العربي في السطر والفرنسي تحته
                val sm = schoolBi.find(plain.trim())
                if (sm != null) {
                    val frName = sm.groupValues[2].trim()
                    rawLine = raw.replace(Regex("\\s+[-–—]\\s+" + Regex.escape(frName)), "")
                    plain = sm.groupValues[1]
                    fr = "École : <bdi>$frName</bdi>"
                } else fr = headerLine(plain)
                if (fr == null) {
                    sb.append(rawLine)
                    if (i < lines.size - 1) sb.append("<br>")
                } else {
                    // السطر الفرنسي كتلة مستقلة، فلا حاجة إلى <br> بعده
                    sb.append(rawLine.trimEnd()).append("<span class=\"bi-hdr\">").append(fr).append("</span>")
                }
            }
            m.groupValues[1] + sb.toString() + m.groupValues[3]
        }

        // 2) عناوين الكشوف واللوائح في وسط الرأسية
        for ((ar, fr) in titles) {
            val re = Regex("(<div class=\"header-(?:title-main|subtitle)\"[^>]*>\\s*)" + Regex.escape(ar) + "(\\s*)(</div>)")
            out = re.replace(out) { m -> m.groupValues[1] + ar + "<span class=\"bi-fr\">" + fr + "</span>" + m.groupValues[2] + m.groupValues[3] }
        }

        // 3) عبارات جداول الكشوف وتوقيعاتها (النص كاملاً داخل الخانة)
        for ((ar, fr) in cells) {
            val re = Regex("(<(?:th|td|div)[^>]*>)(\\s*)" + Regex.escape(ar) + "(\\s*)(</(?:th|td|div)>)")
            out = re.replace(out) { m -> m.groupValues[1] + m.groupValues[2] + ar + "<span class=\"bi-fr\">" + fr + "</span>" + m.groupValues[3] + m.groupValues[4] }
        }

        // 4) عناوين خانات بيانات التلميذ: الفرنسية بحجم صغير بعد الكلمة العربية
        for ((ar, fr) in inlineLabels) {
            out = out.replace(ar, ar.removeSuffix(":") + " <span class=\"bi-i\">" + fr + "</span> :")
        }

        // 5) CSS مرة واحدة
        val idx = out.indexOf("</head>")
        // مع تصاميم الرأسيات المزخرفة: مساحة أسفل أكبر حتى لا يلامس السطر الفرنسي الأخير الإطار
        val extra = if (PrintDesign.header > 0) "<style>.header-section{padding-bottom:30px !important;}</style>" else ""
        out = if (idx >= 0) out.substring(0, idx) + CSS + extra + out.substring(idx) else CSS + extra + out
        return out
    }
}
