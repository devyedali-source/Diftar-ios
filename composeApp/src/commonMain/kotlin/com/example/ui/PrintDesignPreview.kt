package com.example.ui

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context

/**
 * معاينة حقيقية لكل تصميم: نفس CSS الذي يُستعمل في الطباعة، على بيانات تجريبية.
 * kind: 0 رأسية لائحة، 1 كشف، 2 شهادة.
 */
object PrintDesignPreview {
    /** عرض/ارتفاع صفحة المعاينة بالبكسل (لحساب النسبة) */
    fun size(kind: Int): Pair<Int, Int> = when (kind) {
        0 -> 560 to 150
        1 -> 430 to 390
        else -> 720 to 510
    }

    private const val BASE = """
        *{box-sizing:border-box}
        body{margin:0;padding:6px;background:#fff;color:#000;direction:rtl;font-family:'Cairo','Noto Sans Arabic',Arial,sans-serif;-webkit-print-color-adjust:exact;print-color-adjust:exact}
        .header-section{display:flex;justify-content:space-between;align-items:flex-start;width:100%;margin-bottom:4px}
        .header-column{flex:1;font-size:10px;line-height:1.45}
        .header-column.right{text-align:right;font-weight:bold}
        .header-column.center{text-align:center;flex:0.8}
        .header-column.left{text-align:left;font-weight:bold}
        .header-column img{width:44px;height:44px;object-fit:contain}
        .header-title-main{font-size:13px;font-weight:800;margin-top:2px}
        .sheet-card{width:100%;padding:12px;border:2px solid #000;background:#fff}
        .report-card-container{width:100%;height:372px;border:2px solid #000;padding:8px;display:flex;flex-direction:column;gap:4px;background:#fff;overflow:hidden}
        .info{border-top:1.5px solid #000;border-bottom:1.5px solid #000;font-weight:bold;font-size:11px;padding:3px 6px;display:flex;justify-content:space-between}
        table{width:100%;border-collapse:collapse;font-size:11px}
        td,th{border:1.2px solid #000;padding:2px 4px;text-align:center}
        th{font-weight:bold}
    """

    private fun headerBlock(seal: String, title: String) = """
        <div class="header-section">
          <div class="header-column right">الجمهورية الإسلامية الموريتانية<br>وزارة التربية وإصلاح النظام التعليمي<br>الإدارة الجهوية للتربية: غوركل<br>مفتشية مقاطعة: لكصيبه 1<br>مدرسة: الطلحايه 1</div>
          <div class="header-column center">$seal<div class="header-title-main">$title</div></div>
          <div class="header-column left">شرف - إخاء - عدالة<br>السنة الدراسية: 2025 - 2026<br>القسم: 3AP<br>المادة: الرياضيات</div>
        </div>"""

    fun html(context: Context, kind: Int, design: Int): String {
        val (w, _) = size(kind)
        val seal = HtmlReportHelper.getMauritanianSealImg(context)
        val head = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=$w\"><style>$BASE</style>"
        return when (kind) {
            0 -> head + PrintDesign.listCss(design) +
                "</head><body><div class=\"sheet-card\">${headerBlock(seal, "دفتر متابعة القسم")}</div></body></html>"
            1 -> head + PrintDesign.cardCss(design) + "</head><body><div class=\"report-card-container\">" +
                headerBlock(seal, "كشف درجات الفصل الأخير") +
                "<div class=\"info\"><span>الاسم الكامل: تلميذ تجريبي</span><span>الرقم المدرسي: 1001</span></div>" +
                "<table><tr><th>المادة</th><th>الدرجة</th></tr>" +
                "<tr><td>التربية الإسلامية</td><td>15 / 20</td></tr><tr><td>اللغة العربية</td><td>14 / 20</td></tr>" +
                "<tr><td>الرياضيات</td><td>16 / 20</td></tr><tr><td>الفرنسية</td><td>13 / 20</td></tr>" +
                "<tr><td>العلوم الطبيعية</td><td>12 / 20</td></tr></table>" +
                "<div class=\"info\" style=\"margin-top:auto\"><span>توقيع المعلم</span><span>توقيع المدير</span></div>" +
                "</div></body></html>"
            else -> head + PrintDesign.certificateCss(design) + "<style>.cert{height:490px !important;padding:22px 30px !important}.cert h1{font-size:28px !important}.cert .nm{font-size:24px !important}.cert .txt{font-size:13px !important;line-height:1.8 !important}.cert .top{font-size:9px !important}.cert .sigs{bottom:16px !important;left:30px !important;right:30px !important;font-size:11px !important}</style>" +
                "</head><body><div class=\"cert\"><div class=\"top\"><div class=\"r\">الجمهورية الإسلامية الموريتانية<br>وزارة التربية وإصلاح النظام التعليمي<br>مدرسة: الطلحايه 1</div><div class=\"c\">$seal</div><div class=\"l\">شرف - إخاء - عدالة<br>السنة الدراسية: 2025 - 2026<br>القسم: 3AP</div></div>" +
                "<h1>شهادة تفوق وتكريم</h1><div class=\"give\">تمنح إدارة المدرسة هذه الشهادة إلى التلميذ:</div><div class=\"nm\">اسم التلميذ هنا</div>" +
                "<div class=\"txt\">تقديراً لتفوّقه وجدّه واجتهاده، وحصوله على <span class=\"rk\">المرتبة الأولى</span> في القسم بمعدل سنوي قدره <span class=\"rk\">17.5 / 20</span></div>" +
                "<div class=\"sigs\"><div>المعلم</div><div>مدير المدرسة</div></div></div></body></html>"
        }
    }
}
