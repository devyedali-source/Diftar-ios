package com.example.ui

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import android.util.Base64

/**
 * تصاميم رأسيات اللوائح وإطارات الكشوف وشهادات التفوق.
 * الزخرفة تُرسم بالـ CSS/SVG خلف النص فقط، فلا يتغير أي نص أو بيان.
 * القيمة 0 = الوضع الأصلي (التلقائي).
 * ألوان خفيفة جداً وخطوط رفيعة ليبقى النص واضحاً في الطباعة بالأبيض والأسود.
 */
object PrintDesign {
    private const val PREFS = "app_settings"
    private const val K_HEADER = "print_header_design_v1"
    private const val K_CARD = "print_card_design_v1"
    private const val K_CERT = "print_cert_design_v1"

    @Volatile var header: Int = 0
    @Volatile var card: Int = 0
    @Volatile var cert: Int = 1

    val headerNames = listOf(
        "الوضع الأصلي (التلقائي)",
        "إطار أزرق بزوايا مزخرفة",
        "زوايا زرقاء مائلة",
        "شريط أخضر بفواصل",
        "أمواج زرقاء ومعين",
        "إطار ذهبي مع أغصان"
    )
    val cardNames = listOf(
        "الوضع الأصلي (التلقائي)",
        "موج أزرق خفيف",
        "إطار ذهبي مزخرف",
        "موج أزرق مع كتاب",
        "أخضر خفيف مع قبعة وكتب",
        "إطار بنفسجي خفيف",
        "أخضر وذهبي مع أغصان"
    )
    val certNames = listOf(
        "ذهبي كلاسيكي",
        "أخضر وطني",
        "أزرق عصري",
        "بنفسجي أنيق",
        "بسيط للطباعة بالأبيض والأسود"
    )

    fun load(context: Context) {
        try {
            val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            header = p.getInt(K_HEADER, 0).coerceIn(0, headerNames.lastIndex)
            card = p.getInt(K_CARD, 0).coerceIn(0, cardNames.lastIndex)
            cert = p.getInt(K_CERT, 1).coerceIn(1, certNames.size)
        } catch (_: Exception) {
        }
    }

    fun save(context: Context, header: Int? = null, card: Int? = null, cert: Int? = null) {
        header?.let { this.header = it }
        card?.let { this.card = it }
        cert?.let { this.cert = it }
        try {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt(K_HEADER, this.header).putInt(K_CARD, this.card).putInt(K_CERT, this.cert).apply()
        } catch (_: Exception) {
        }
    }

    // ---------- أدوات الرسم ----------
    private const val B = "#185FA5"
    private const val BL = "#B5D4F4"
    private const val BX = "#E6F1FB"
    private const val G = "#3B6D11"
    private const val GL = "#C0DD97"
    private const val GX = "#EAF3DE"
    private const val Y = "#BA7517"
    private const val V = "#534AB7"

    private fun uri(svg: String): String {
        val full = if (svg.contains("xmlns")) svg else svg.replaceFirst("<svg", "<svg xmlns=\"http://www.w3.org/2000/svg\"")
        val b64 = Base64.encodeToString(full.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "url(\"data:image/svg+xml;base64,$b64\")"
    }

    /** زاوية مزخرفة 28×28: corner 0=أعلى يسار 1=أعلى يمين 2=أسفل يسار 3=أسفل يمين */
    private fun cornerSvg(c: String, w: String, corner: Int, size: Int = 28): String {
        val tf = when (corner) {
            0 -> ""
            1 -> "translate(28,0) scale(-1,1)"
            2 -> "translate(0,28) scale(1,-1)"
            else -> "translate(28,28) scale(-1,-1)"
        }
        return "<svg viewBox=\"0 0 28 28\" width=\"$size\" height=\"$size\"><g transform=\"$tf\" fill=\"none\" stroke=\"$c\" stroke-width=\"$w\">" +
            "<path d=\"M1,27 V9 Q1,1 9,1 H27\"/><path d=\"M6,27 V13 Q6,6 13,6 H27\" stroke-width=\"0.7\"/><circle cx=\"12\" cy=\"12\" r=\"2.4\"/></g></svg>"
    }

    private fun fourCorners(c: String, w: String, size: Int, inset: Int): String {
        val s = "${size}px ${size}px"
        return listOf(
            "${uri(cornerSvg(c, w, 0, size))} left ${inset}px top ${inset}px/$s no-repeat",
            "${uri(cornerSvg(c, w, 1, size))} right ${inset}px top ${inset}px/$s no-repeat",
            "${uri(cornerSvg(c, w, 2, size))} left ${inset}px bottom ${inset}px/$s no-repeat",
            "${uri(cornerSvg(c, w, 3, size))} right ${inset}px bottom ${inset}px/$s no-repeat"
        ).joinToString(",")
    }

    private fun leafArc(cx: Double, cy: Double, r: Double, a0: Double, a1: Double, n: Int, col: String, flip: Boolean): String {
        val sb = StringBuilder()
        for (i in 0 until n) {
            val a = Math.toRadians(a0 + (a1 - a0) * i / (n - 1))
            val x = cx + r * Math.cos(a)
            val y = cy + r * Math.sin(a)
            val rot = Math.toDegrees(a) + if (flip) -90 else 90
            sb.append("<ellipse cx=\"%.1f\" cy=\"%.1f\" rx=\"2.2\" ry=\"5\" transform=\"rotate(%.0f %.1f %.1f)\" fill=\"$col\" fill-opacity=\"0.3\" stroke=\"$col\" stroke-width=\"0.6\"/>".format(java.util.Locale.US, x, y, rot, x, y))
        }
        return sb.toString()
    }

    private fun laurelRing(col: String): String =
        "<svg viewBox=\"0 0 90 90\" width=\"90\" height=\"90\">" +
            leafArc(45.0, 45.0, 36.0, 100.0, 250.0, 8, col, false) +
            leafArc(45.0, 45.0, 36.0, -70.0, 80.0, 8, col, true) + "</svg>"

    private fun laurelSide(col: String, left: Boolean): String {
        val a0 = if (left) -70.0 else 110.0
        val a1 = if (left) 70.0 else 250.0
        val cx = if (left) 0.0 else 22.0
        return "<svg viewBox=\"0 0 22 80\" width=\"22\" height=\"80\">" +
            leafArc(cx, 40.0, 34.0, a0, a1, 8, col, !left) + "</svg>"
    }

    // ---------- رأسيات اللوائح (تُطبَّق على .header-section) ----------
    fun listCss(design: Int = header): String {
        if (design <= 0 || design >= PrintDesignArt.LIST.size) return ""
        return "<style>${PrintDesignArt.LIST[design]}</style>"
    }

    // ---------- إطارات الكشوف (تُطبَّق على بطاقات الكشوف) ----------
    fun cardCss(design: Int = card): String {
        if (design <= 0 || design >= PrintDesignArt.CARD.size) return ""
        return "<style>${PrintDesignArt.CARD[design]}</style>"
    }

    // ---------- شهادات التفوق ----------
    /** يرجع CSS الصفحة (.cert) حسب التصميم 1..5 */
    fun certificateCss(design: Int): String {
        val base = ".cert{position:relative;width:100%;height:190mm;box-sizing:border-box;page-break-after:always;break-after:page;page-break-inside:avoid;break-inside:avoid;overflow:hidden;text-align:center;color:#000;padding:12mm 16mm;}" +
            ".cert:last-child{page-break-after:auto;break-after:auto;}" +
            ".cert .top{display:flex;justify-content:space-between;align-items:flex-start;font-size:11px;font-weight:bold;line-height:1.6;}" +
            ".cert .top .c{flex:1}.cert .top .r{text-align:right}.cert .top .l{text-align:left}" +
            ".cert h1{font-size:34px;margin:6px 0 2px;font-weight:900;}" +
            ".cert .sub{font-size:14px;margin:0 0 10px;}" +
            ".cert .give{font-size:16px;margin:10px 0 4px;}" +
            ".cert .nm{font-size:30px;font-weight:900;margin:4px auto 8px;display:inline-block;padding:2px 28px;border-bottom:1.5px solid #000;}" +
            ".cert .txt{font-size:16px;line-height:2;max-width:200mm;margin:0 auto;}" +
            ".cert .rk{font-weight:900;font-size:20px;}" +
            ".cert .sigs{position:absolute;left:16mm;right:16mm;bottom:12mm;display:flex;justify-content:space-between;font-size:13px;font-weight:bold;}" +
            ".cert .sigs div{min-width:55mm;text-align:center;}" +
            ".cert img{max-height:58px;}"
        val frame = when (design) {
            1 -> ".cert{border:2px solid $Y;box-shadow:inset 0 0 0 5px #fff,inset 0 0 0 5.8px $Y;background:${fourCorners(Y, "1.8", 44, 3)},#FFFDF6;}.cert h1{color:#7a4a08}"
            2 -> ".cert{border:2px solid $G;box-shadow:inset 0 0 0 5px #fff,inset 0 0 0 5.8px $Y;background:${uri("<svg viewBox=\"0 0 200 14\" preserveAspectRatio=\"none\"><rect width=\"200\" height=\"14\" fill=\"$GX\"/><rect y=\"11\" width=\"200\" height=\"1.6\" fill=\"$Y\"/></svg>")} left bottom/100% 14mm no-repeat,${uri("<svg viewBox=\"0 0 200 14\" preserveAspectRatio=\"none\"><rect width=\"200\" height=\"14\" fill=\"$GX\"/><rect y=\"1.4\" width=\"200\" height=\"1.6\" fill=\"$Y\"/></svg>")} left top/100% 14mm no-repeat,#fff;}.cert h1{color:$G}"
            3 -> ".cert{border:1px solid $BL;background:${uri("<svg viewBox=\"0 0 200 60\" preserveAspectRatio=\"none\"><path d=\"M0,0 H200 C130,8 50,24 0,60Z\" fill=\"$BX\" stroke=\"$BL\"/></svg>")} left top/60% 60mm no-repeat,${uri("<svg viewBox=\"0 0 200 60\" preserveAspectRatio=\"none\"><path d=\"M200,60 H0 C60,52 140,36 200,0Z\" fill=\"$BX\" stroke=\"$BL\"/></svg>")} right bottom/60% 60mm no-repeat,#fff;}.cert h1{color:$B}"
            4 -> ".cert{border:2px solid #7F77DD;border-radius:14px;box-shadow:inset 0 0 0 6px #fff,inset 0 0 0 6.8px #AFA9EC;background:${uri("<svg viewBox=\"0 0 60 60\"><path d=\"M1,58 V16 Q1,1 16,1 H58\" fill=\"none\" stroke=\"$V\" stroke-width=\"2.6\"/></svg>")} left 4px top 4px/60px 60px no-repeat,${uri("<svg viewBox=\"0 0 60 60\"><path d=\"M59,2 V44 Q59,59 44,59 H2\" fill=\"none\" stroke=\"$V\" stroke-width=\"2.6\"/></svg>")} right 4px bottom 4px/60px 60px no-repeat,#fff;}.cert h1{color:$V}"
            else -> ".cert{border:2.5px solid #000;box-shadow:inset 0 0 0 5px #fff,inset 0 0 0 6px #000;background:#fff;}.cert h1{letter-spacing:1px}"
        }
        return "<style>$base$frame</style>"
    }
}
