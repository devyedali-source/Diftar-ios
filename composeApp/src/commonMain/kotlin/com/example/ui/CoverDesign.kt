package com.example.ui

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import com.example.data.models.ClassSection

/** بيانات صفحة الغلاف التي يدخلها المعلم (المدرسة والقسم والسنة تؤخذ من إعدادات القسم). */
data class CoverInfo(
    val teacher: String = "",
    val finance: String = "",
    val phone: String = "",
    val photoB64: String? = null
)

/**
 * غلاف (الصفحة الأولى) لدفتر الغياب ودفتر المتابعة: عدة تصاميم بشعار الدولة وعلم موريتانيا.
 * الغلاف صفحة أفقية تُدرج في أول ملف PDF، ولا تغيّر أي بيانات أخرى.
 */
object CoverDesign {
    val names = listOf(
        "الزمردي الذهبي",
        "راية موريتانيا",
        "الكلاسيكي الأنيق",
        "العصري بشريط جانبي"
    )

    /** آخر بيانات أُدخلت؛ تُحفظ على الجهاز وتُستعمل في دفتر الغياب ودفتر المتابعة معاً. */
    var lastInfo: CoverInfo = CoverInfo()

    private const val PREFS = "cover_info_prefs"

    fun load(context: android.content.Context): CoverInfo {
        try {
            val p = context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
            if (p.contains("teacher") || p.contains("photo")) {
                lastInfo = CoverInfo(
                    teacher = p.getString("teacher", "") ?: "",
                    finance = p.getString("finance", "") ?: "",
                    phone = p.getString("phone", "") ?: "",
                    photoB64 = p.getString("photo", null)
                )
            }
        } catch (_: Exception) {}
        return lastInfo
    }

    fun save(context: android.content.Context, info: CoverInfo) {
        lastInfo = info
        try {
            context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE).edit()
                .putString("teacher", info.teacher).putString("finance", info.finance)
                .putString("phone", info.phone).putString("photo", info.photoB64).apply()
        } catch (_: Exception) {}
    }

    /** أبعاد صفحة الغلاف بالملّيمتر (داخل هوامش الصفحة الأفقية). */
    const val W_MM = 281
    const val H_MM = 194

    private const val GREEN = "#0B6B3A"
    private const val DARK = "#064E2B"
    private const val GOLD = "#C9A227"
    private const val RED = "#D01C1F"
    private const val CREAM = "#FFF8E1"

    private fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    private fun v(s: String): String = if (s.isBlank()) "………………………" else esc(s.trim())

    private fun star(cx: Double, cy: Double, ro: Double, ri: Double): String {
        val pts = (0 until 10).joinToString(" ") { i ->
            val a = Math.toRadians(-90.0 + 36.0 * i)
            val r = if (i % 2 == 0) ro else ri
            "%.2f,%.2f".format(java.util.Locale.US, cx + r * Math.cos(a), cy + r * Math.sin(a))
        }
        return "<polygon points=\"$pts\" fill=\"#FFC400\"/>"
    }

    /** علم موريتانيا: حقل أخضر وهلال ونجمة ذهبيان وشريطان أحمران. */
    fun flagSvg(widthMm: Int): String =
        "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 150 100\" style=\"width:${widthMm}mm;height:${widthMm * 2 / 3}mm;display:block;border:0.3mm solid #444\">" +
            "<rect width=\"150\" height=\"100\" fill=\"#006233\"/>" +
            "<rect width=\"150\" height=\"14\" fill=\"$RED\"/><rect y=\"86\" width=\"150\" height=\"14\" fill=\"$RED\"/>" +
            "<circle cx=\"75\" cy=\"58\" r=\"26\" fill=\"#FFC400\"/><circle cx=\"75\" cy=\"48\" r=\"23\" fill=\"#006233\"/>" +
            star(75.0, 37.0, 9.0, 3.6) + "</svg>"

    private fun photoHtml(info: CoverInfo, sizeMm: Int, border: String): String =
        if (info.photoB64.isNullOrEmpty()) "" else
            "<img src=\"data:image/jpeg;base64,${info.photoB64}\" style=\"width:${sizeMm}mm;height:${sizeMm}mm;object-fit:cover;border-radius:50%;border:1.2mm solid $border;display:block\"/>"

    private fun css(d: Int): String {
        val base = """
        .cv{box-sizing:border-box;width:100%;height:${H_MM}mm;overflow:hidden;position:relative;direction:rtl;
            font-family:'Cairo','Noto Sans Arabic',Arial,sans-serif;color:#1b1b1b;page-break-after:always;break-after:page;
            -webkit-print-color-adjust:exact;print-color-adjust:exact}
        .cv *{box-sizing:border-box}
        .cv .top{display:flex;align-items:flex-start;justify-content:space-between;padding:0 4mm}
        .cv .tr{flex:1;text-align:right;font-weight:800;font-size:13pt;line-height:1.5}
        .cv .tc{flex:0 0 auto;text-align:center}
        .cv .tl{flex:1;text-align:left;font-weight:800;font-size:13pt;line-height:1.5}
        .cv .school{display:grid;grid-template-columns:1fr 1fr;gap:1.5mm 8mm;font-size:12.5pt;font-weight:700;padding:3mm 6mm}
        .cv .ttl{text-align:center;font-weight:800;font-size:32pt;margin:3mm auto;padding:2mm 14mm;width:max-content;max-width:90%}
        .cv .me{display:flex;align-items:center;justify-content:center;gap:8mm;margin:0 auto;padding:3mm 8mm;width:88%}
        .cv .me .rows{flex:1;font-size:14pt;font-weight:700;line-height:2.1}
        .cv .fl{position:absolute;top:6mm}
        """
        val theme = when (d) {
            0 -> """
        .cv{background:linear-gradient(180deg,$CREAM,#fff 60%,$CREAM);border:3mm solid $GREEN;outline:1mm solid $GOLD;outline-offset:-5mm;padding:7mm 5mm;display:flex;flex-direction:column;justify-content:space-around}
        .cv .school{background:#fff;border:0.6mm solid $GOLD;border-radius:3mm;margin:2mm auto;width:92%}
        .cv .ttl{background:$DARK;color:$CREAM;border:1mm solid $GOLD;border-radius:8mm;box-shadow:0 0 0 1.2mm $DARK}
        .cv .me{background:#fff;border:0.6mm solid $GOLD;border-radius:4mm}
        .cv .fl.a{right:10mm}.cv .fl.b{left:10mm}
        """
            1 -> """
        .cv{background:#fff;padding:16mm 8mm 16mm;display:flex;flex-direction:column;justify-content:space-around}
        .cv:before{content:"";position:absolute;top:0;left:0;right:0;height:11mm;background:linear-gradient(180deg,$RED 0 3.5mm,$GREEN 3.5mm 11mm)}
        .cv:after{content:"";position:absolute;bottom:0;left:0;right:0;height:11mm;background:linear-gradient(0deg,$RED 0 3.5mm,$GREEN 3.5mm 11mm)}
        .cv .school{background:#F1F8F3;border-right:1.6mm solid $GREEN;border-left:1.6mm solid $RED;margin:2mm auto;width:92%}
        .cv .ttl{background:$GOLD;color:$DARK;border-radius:3mm;border-bottom:1.6mm solid $DARK}
        .cv .me{background:#F1F8F3;border-right:1.6mm solid $GOLD;border-left:1.6mm solid $GOLD}
        .cv .fl.a{right:12mm;top:14mm}.cv .fl.b{left:12mm;top:14mm}
        .cv .top{margin-top:4mm}
        """
            2 -> """
        .cv{background:#fffefb;border:0.5mm solid $GOLD;padding:9mm 7mm;outline:0.5mm solid $GOLD;outline-offset:-3.2mm;display:flex;flex-direction:column;justify-content:space-around}
        .cv .corner{position:absolute;width:16mm;height:16mm;border:0 solid $GREEN}
        .cv .c1{top:6mm;right:6mm;border-top-width:1.2mm;border-right-width:1.2mm}
        .cv .c2{top:6mm;left:6mm;border-top-width:1.2mm;border-left-width:1.2mm}
        .cv .c3{bottom:6mm;right:6mm;border-bottom-width:1.2mm;border-right-width:1.2mm}
        .cv .c4{bottom:6mm;left:6mm;border-bottom-width:1.2mm;border-left-width:1.2mm}
        .cv .school{border-top:0.5mm solid $GOLD;border-bottom:0.5mm solid $GOLD;margin:2mm auto;width:90%}
        .cv .ttl{color:$GREEN;border-top:0.8mm solid $GOLD;border-bottom:0.8mm solid $GOLD;font-size:34pt}
        .cv .me{border:0.5mm solid $GOLD}
        .cv .fl.a{display:none}.cv .fl.b{display:none}
        """
            else -> """
        .cv{background:#fff;display:flex;flex-direction:row;padding:0}
        .cv .side{flex:0 0 70mm;background:linear-gradient(180deg,$DARK,$GREEN);color:$CREAM;display:flex;flex-direction:column;align-items:center;justify-content:space-between;padding:8mm 4mm;text-align:center;font-weight:800;font-size:11.5pt;line-height:1.6}
        .cv .side .band{width:100%;height:2mm;background:$GOLD}
        .cv .main{flex:1;padding:8mm 8mm;display:flex;flex-direction:column;justify-content:space-between}
        .cv .school{background:#F6F6F0;border-right:1.6mm solid $GOLD;margin:0;width:100%}
        .cv .ttl{color:$DARK;border-bottom:1.2mm solid $GOLD;margin:0 0 0 auto;font-size:34pt;padding:0 0 1mm}
        .cv .me{background:#F6F6F0;border-radius:3mm;width:100%;padding:3mm 6mm}
        """
        }
        return base + theme
    }

    /** يبني الغلاف كشريحة HTML تُدرج داخل <body> (مع أنماطها)، بحجم صفحة واحدة. */
    fun body(context: Context, section: ClassSection, title: String, info: CoverInfo, design: Int, stampHtml: String = ""): String {
        val seal = HtmlReportHelper.getMauritanianSealImg(context)
            .replace("width: 76px; height: 76px", "width: 34mm; height: 34mm")
        val cls = HtmlReportHelper.getDisplayClassName(section)
        val rows = """
            <div>الإدارة الجهوية للتربية: ${v(section.wilaya)}</div>
            <div>مفتشية مقاطعة: ${v(section.moughataa)}</div>
            <div>المدرسة: ${v(section.schoolName)}</div>
            <div>السنة الدراسية: ${v(section.academicYear)}</div>
            <div>القسم المسند: ${v(cls)}</div>"""
        val me = """
            <div class="me">
              <div class="rows">
                <div>اسم المعلم(ة): ${v(info.teacher)}</div>
                <div>الدليل المالي: ${v(info.finance)}</div>
                <div>رقم الهاتف: ${v(info.phone)}</div>
              </div>
              ${photoHtml(info, 34, if (design == 1) GREEN else GOLD)}
              ${if (stampHtml.isBlank()) "" else "<div style=\"flex:0 0 auto\">$stampHtml</div>"}
            </div>"""
        val inner = if (design == 3) {
            """
            <div class="side">
              <div class="band"></div>
              <div>$seal<div style="margin-top:3mm">الجمهورية الإسلامية الموريتانية</div><div>وزارة التربية وإصلاح النظام التعليمي</div></div>
              <div style="display:flex;justify-content:center">${flagSvg(40)}</div>
              <div>شرف - إخاء - عدالة</div>
              <div class="band"></div>
            </div>
            <div class="main">
              <div class="ttl">$title</div>
              <div class="school" style="display:block;line-height:1.9">$rows</div>
              $me
            </div>"""
        } else {
            """
            ${if (design == 2) "<div class=\"corner c1\"></div><div class=\"corner c2\"></div><div class=\"corner c3\"></div><div class=\"corner c4\"></div>" else ""}
            <div class="top">
              <div class="tr">الجمهورية الإسلامية الموريتانية<br>وزارة التربية وإصلاح النظام التعليمي</div>
              <div class="tc">${if (design == 0 || design == 1) "<div style=\"display:flex;align-items:center;justify-content:center;gap:5mm\">${flagSvg(24)}$seal${flagSvg(24)}</div>" else seal}</div>
              <div class="tl">شرف - إخاء - عدالة</div>
            </div>
            <div class="school">$rows</div>
            <div style="text-align:center"><div class="ttl">$title</div></div>
            $me"""
        }
        return "<style>${css(design)}</style><div class=\"cv\">$inner</div>"
    }

    /** صفحة كاملة للمعاينة داخل الواجهة. */
    fun previewHtml(context: Context, section: ClassSection, title: String, info: CoverInfo, design: Int, stampHtml: String = ""): String =
        "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=1062\">" +
            "<style>body{margin:0;width:${W_MM}mm;background:#fff}</style></head><body>" +
            body(context, section, title, info, design, stampHtml).replace("page-break-after:always;break-after:page;", "") +
            "</body></html>"

    /** يدرج الغلاف في أول ملف HTML مُجمَّع (قبل أول صفحة). */
    fun insert(html: String, coverBody: String): String {
        val m = Regex("<body[^>]*>").find(html) ?: return html
        // الغلاف ليس ضمن ترقيم الصفحات: يبدأ الترقيم من 1 بعده، ولا يظهر تذييل في صفحة الغلاف
        // (يُحذف «من N» لأن العدد الكلي يشمل الغلاف)
        val hasFooter = html.contains("counter(pages)")
        val numbering = "<style>@page :first { counter-reset: page 0; " +
            "@bottom-left { content: none; } @bottom-center { content: none; } @bottom-right { content: none; } }" +
            (if (hasFooter) "@page { @bottom-left { content: \"صفحة \" counter(page); font-family: 'Cairo', sans-serif; font-size: 8pt; } }" else "") +
            "</style>"
        return html.substring(0, m.range.last + 1) + numbering + coverBody + html.substring(m.range.last + 1)
    }
}
