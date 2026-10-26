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
        "النقاط" to "Points",
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
        "الرقم المدرسي:" to "Matricule"
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
        value(listOf("مدرسة"))?.let { return "École : <bdi>$it</bdi>" }
        value(listOf("السنة الدراسية"))?.let { return "Année scolaire : <bdi>$it</bdi>" }
        value(listOf("القسم"))?.let { return "Classe : <bdi>$it</bdi>" }
        value(listOf("المادة"))?.let { return "Matière : <bdi>${BiFr.subjectFr(it) ?: it}</bdi>" }
        value(listOf("الفصل"))?.let { return "<bdi>${term(it)}</bdi>" }
        return null
    }

    fun translate(html: String): String {
        if (html.contains(MARK)) return html
        var out = html

        // 1) أعمدة الرأسية (يمين/يسار): تحت كل سطر عربي معروف سطر فرنسي صغير
        out = headerColumn.replace(out) { m ->
            val lines = m.groupValues[2].split(brTag)
            val sb = StringBuilder()
            lines.forEachIndexed { i, raw ->
                val plain = raw.replace(anyTag, "").replace("&nbsp;", " ")
                val fr = headerLine(plain)
                if (fr == null) {
                    sb.append(raw)
                    if (i < lines.size - 1) sb.append("<br>")
                } else {
                    // السطر الفرنسي كتلة مستقلة، فلا حاجة إلى <br> بعده
                    sb.append(raw.trimEnd()).append("<span class=\"bi-hdr\">").append(fr).append("</span>")
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
