package com.example.ui

import com.example.compat.*
import kotlinx.coroutines.IO

import android.content.Context
import com.example.data.models.ClassSection
import com.example.data.models.Student
import com.example.data.models.Subject
import com.example.data.models.sortedByOfficialOrder
import com.example.ui.StudentPerformance
import java.util.Locale
import android.net.Uri
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object HtmlReportHelper {

    fun formatCleanNumber(value: Double?, maxDecimals: Int = 2): String {
        if (value == null || value.isNaN() || value.isInfinite()) return "-"
        val df = java.text.DecimalFormat("0." + "#".repeat(maxDecimals), java.text.DecimalFormatSymbols(java.util.Locale.US))
        return df.format(value)
    }

    fun formatCleanNumber(value: Float?, maxDecimals: Int = 2): String {
        if (value == null || value.isNaN() || value.isInfinite()) return "-"
        return formatCleanNumber(value.toDouble(), maxDecimals)
    }

    fun getDisplayClassName(activeClass: ClassSection): String {
        val level = activeClass.level
        val sectionName = activeClass.sectionName.trim()
        val levelAp1 = "${level}AP"
        val levelAp2 = "${level} AP"
        
        return if (sectionName.equals(levelAp1, ignoreCase = true) || sectionName.equals(levelAp2, ignoreCase = true)) {
            "${level}AP"
        } else if (sectionName.startsWith(levelAp1, ignoreCase = true)) {
            "${level}AP ${sectionName.substring(levelAp1.length).trim()}"
        } else if (sectionName.startsWith(levelAp2, ignoreCase = true)) {
            "${level}AP ${sectionName.substring(levelAp2.length).trim()}"
        } else {
            "${level}AP ${sectionName.ifBlank { "أ" }}"
        }
    }

    private var cachedSealBase64: String? = null

    private fun getActivity(context: Context): android.app.Activity? {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is android.app.Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return ctx as? android.app.Activity
    }

    private fun getMauritanianSealImg(context: Context): String {
        try {
            synchronized(this) {
                if (cachedSealBase64 == null) {
                    var inputStream = try {
                    context.resources.openRawResource(com.example.R.raw.img_mauritania_seal)
                } catch (e: Exception) {
                    null
                }
                
                if (inputStream == null) {
                    inputStream = try {
                        context.resources.openRawResource(com.example.R.drawable.img_mauritania_seal)
                    } catch (e: Exception) {
                        null
                    }
                }
                
                inputStream?.use { stream ->
                    val bytes = stream.readBytes()
                    if (bytes.size > 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) {
                        cachedSealBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    }
                }

                // Absolute fail-safe: if the resource files were missing or failed to decode,
                // fallback to the 100% correct, uncorrupted official seal in Base64
                if (cachedSealBase64.isNullOrEmpty()) {
                    cachedSealBase64 = "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"
                }
            }
            
            } // Close synchronized block
            
            cachedSealBase64?.let { base64 ->
                return """<img src="data:image/jpeg;base64,$base64" style="width: 76px; height: 76px; display: block; margin: 0 auto; object-fit: contain;" alt="شعار موريتانيا" />"""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Return empty block to prevent any SVG fallback drawing
        return ""
    }

    // Helper for Mauritanian Emblem SVG (Falls back to the 100% correct, uncorrupted official seal in Base64)
        private fun getMauritanianSealSvg(): String {
        val base64 = "77+9UE5HDQoaCgAAAA1JSERSAAAA77+9AAAA77+9CAYAAADvv73vv71aPQAAAARnQU1BAADvv73vv70L77+9YQUAAAAgY0hSTQAAeiYAAO+/ve+/vQAA77+9AAAA77+977+9AAB1MAAA77+9YAAAOu+/vQAAF3Dvv73vv71RPAAAAAZiS0dEAO+/vQDvv70A77+977+977+977+977+9AAAAB3RJTUUH77+9BgMVLidSHhXvv70AAO+/vQBJREFUeO+/ve+/vV1nYFvvv73vv71+77+9Xe+/vdOS77+977+977+977+9IiQk77+9UShQ77+93pTvv73vv71eBe+/vS7vv71ZCmXvv73vv73vv73vv73vv71QKGXvv70dVhLvv73vv73vv71l77+977+977+9Q++/vSzZsi0n77+9JDTvv70f77+9JO+/vXnvv73vv73vv73vv70l77+9Le+/ve+/ve+/vXUQ77+977+9VO+/vQTVlO+/vWUETO+/vUrvv73vv73vv704CGgBBXEA77+9ABQaEDAALH1/KgAw77+977+9fxTvv73vv73vv73vv71BUO+/vSBIAu+/vUNA77+9FO+/vUtBPQwlHgrvv71LCNumUNqsQe+/ve+/vUxJ77+977+9bu+/vUXvv73vv71+BDvvv73vv703aO+/ve+/vULvv73vv73vv71O77+977+9BO+/vVQTSu+/vShBCQBmB13vv70K77+9AyDvv70AbSYUGxTvv73vv73vv70ga3Tvv70Szbvvv73vv71u77+977+977+9NDQFEe+/vUnvv73QhQAzBxTvv71A77+9NADvv71d77+9Vu+/vQBZA2At77+9Lu+/ve+/vXwjTBbvv70TAu+/ve+/vS3vv70G77+977+9Hu+/vdug77+977+977+9PFBm77+9Su+/ve+/vQALAO+/vX/vv73vv73vv73vv73vv70M77+977+9UNWvOO+/ve+/ve+/vVQj77+9exXvv70G77+977+9UhLvv70177+9Ge+/vUMp77+977+9Eu+/ve+/vQHvv71p77+977+9SDLvv73vv711Je+/vXnvv73vv73vv70n77+9Xlbvv708LO+/ve+/vSwjB1jvv71GGe+/ve+/vSTvv73vv73vv71AWSrvv73vv70fKu+/vQAAGO+/vQQA77+977+9OCFEAQBWQ++/vcSgJjnvv70icRZF77+9HQph77+9Cu+/vTsl77+977+9SirQlO+/vcWE77+977+9be+/vUgo77+9Ge+/ve+/vT3vv73vv71977+977+977+977+93atjN++/vXdd77+977+9KTjvv71IcwDvv73vv70RAO+/vQXvv73vv70xH0Pvv70i77+977+977+977+977+977+9Km1SbBd477+9T++/vTTKuO+/vUpcO++/ve+/vVHvv71QNzHvv71u77+9TkPvv70yUe+/vU9PaO+/vTMSxZpy77+977+977+9YO+/ve+/vVtA77+977+977+9MG9zPe+/vU/vv71+77+9d++/ve+/ve+/vUDvv73vv719bVbvv73vv73vv71LCT0B77+9RwMo77+9H++/vURO77+9FzYH77+9Mu+/ve+/ve+/vWgY77+977+9K1Dvv71M1bZq77+977+9UO+/vU5t1pQqHu+/ve+/vQTvv70s77+977+977+9E++/vTrvv71Sbgzvv73vv70D77+9fxDvv73vv73vv71NTnxOCO+/vd2rajfvv713GhHvv73vv709Ae+/vXQQ77+9GFDvv73vv73vv71v77+9OBMJf++/ve+/vRDvv73vv70Q77+977+977+9Wu+/vSAzHgPvv73vv73vv73vv71EGe+/ve+/ve+/vTglETQt77+977+9TO+/vRMTGe+/ve+/ve+/vT3vv70eAH8H77+9S++/vRTvv73dq2w377+9d++/ve+/vW7vv71FEu+/vRNA77+977+9Ae+/ve+/vdeG77+9xrXvv73vv70xN++/ve+/vTAY77+9ADMV77+977+977+977+977+9KO+/ve+/vW7vv73Pie+/ve+/vUfvv71c77+9KcmJIHnvv73vv73vv70EeO+/vWPvv71n77+9BHh277+977+977+9QO+/ve+/vUVa77+9Le+/ve+/vTkP77+977+9AO+/vSNuBhJJ77+9PjHvv73vv71tEu+/vU3vv70477+977+977+977+9TzBj77+9Me+/vVtbI2/vv70dGe+/vVrvv73vv73vv70gHB1t77+9A++/vTvvv73vv73vv70U77+977+977+9T3A30Lfvv73vv73vv70kN2h+77+9UHo977+9PUfvv73vv73vv71E77+977+977+9PW/vv73vv73vv73vv70p77+977+977+977+977+977+9xKDvv71SXms/Oijvv70J77+977+9AH3vv73vv71J77+977+977+977+9dj/vv73vv71A77+9eu+/vW/vv71Z77+9Ne+/vVJKLwVQMe+/vTPvv71S77+977+977+977+977+9H++/vSxMBSXvv73vv73vv73vv73vv70HR0Bl77+977+9J++/ve+/vXlV77+9dBPvv73vv71cZwbvv70Z77+9Ke+/ve+/ve+/vQUgD++/vUzvv71pMhHvv73dq3Y377+977+9B++/vXoU77+977+9cDUh77+9ECNU77+977+9UTbvv73vv73vv73vv70H77+926YSNUkmbu+/vQtgAO+/vRHvv70+N0BN77+977+936Rxfhzvv71VRu+/vUNT77+9UHXvv73vv70OD++/ve+/vSIf77+9EO+/vU0n77+9I++/vV7vv73Vq++/vRwU77+977+9X++/ve+/ve+/vV0wAm3vv70cGO+/vXbvv70R77+977+9WmTvv70IPw1S77+9Ee+/ve+/ve+/ve+/vSnvv73vv71exbvvv70+PO+/vRtgERPCle+/ve+/ve+/vQAeXe+/vVvvv73vv73vv70977+9bO+/ve+/veanj1UMM++/vXDvv73vv73vv71dIwIfGe+/vXwv77+9SRDvv73vv73vv73vv71S77+9Xu+/vXnvv70oAO+/ve+/ve+/vRXvv73vv71mY++/vTUpGO+/ve+/vQ4QNu+/ve+/vU3vv73vv71B77+9Du+/ve+/ve+/ve+/vQbvv708Ne+/ve+/ve+/vUvvv71s77+9d++/ve+/vRPvv71U77+9PxTvv73vv73vv71q77+9Oe+/ve+/vQ8S77+9BzhOfO+/ve+/vUNo77+977+977+9De+/vQHvv73vv73vv71B5oWLKXAt77+977+9Zu+/vQo177+9cXnvv702Vgow77+977+977+9WzDvv71O77+9fmwQ77+977+9Iu+/vR7vv70j77+977+9UO+/vVoo77+9UHrvv70XTe+/vRdlfe+/vTg+BO+/vVnvv73vv704AUHvv71vCxHvv73vv71k77+9Y++/vQvvv73vv71d77+977+9dNOlRe+/vW8a77+977+9L++/vVHvv73vv71OdD1YMO+/vVlPBApt77+9CN6lQO+/vQjvv70eFmIHDyrvv710S++/ve+/ve+/ve+/ve+/vRNC77+977+977+9Anvvv71Q77+977+9FA8L77+9eO+/vW7vv70D77+977+9NO+/vSlYaQN/77+9BO+/vQ7vv73vv73vv71vKu+/ve+/ve+/ve+/ve+/vW/cr++/vWsg77+9ee+/ve+/ve+/ve+/vUnvv71l77+9GO+/vUfvv70h77+9Se+/ve+/vdSi77+9Yu+/ve+/ve+/vV/vv73vv70QeO+/vQzvv71SUO+/vWTvv73dgzV877+977+9PjReXDxg77+9UyDvv73vv70BBceE77+977+9PRvvv71SF++/ve+/vVoNGO+/vTrvv71icSjvv71+77+9C++/vTPvv73vv70l77+9KO+/ve+/ve+/ve+/vey9qXEy77+977+977+9Be+/vS/vv73vv71s77+9DO+/vRbvv70V77+9RTRUP++/vWd2QnDvv70k77+9F++/ve+/ve+/ve+/ve+/vSQ9Swjvv73vv73Vte+/ve+/ve+/vd6477+9Xu+/ve+/ve+/vV5YBkrvv73vv71V77+9QiXvv73vv71577+977+977+977+9X1VE3a9YDu+/vURq77+977+9NlJh77+9N++/ve+/vVvvv70Y77+9Wgfvv70u77+9Qe+/vRPvv73vv73vv71C77+9de+/vRA7Oe+/ve+/ve+/ve+/vVE3IQnvv71A77+9ee+/vWjvv73vv71777+9Uu+/vdaIaVPvv71fMjU877+977+977+9Sz1877+977+977+977+977+9Uu+/vRDvv73vv70BBm13OnLvv73vv73vv73vv70wxr3Qge+/ve+/vWrvv71+77+977+977+9Y++/vTIp77+977+9y7zXhu+/vSoSHe+/vRd877+9Jkk877+9DVII77+977+977+977+977+9Mu+/ve+/ve+/vXvvv71G77+9H++/ve+/vRpdOe+/vSp3UO+/ve+/vTbvv73vv70R77+9L1nvv73vv71hHk9V77+9eO+/vVjvv70C77+977+9IAwzEu+/vU0QUxpZIu+/vXzvv73vv73vv73vv70mRH4YcO+/vTU1Iu+/ve+/ve+/ve+/vX1taO+/vSnvv71OC86hIO+/vV12VWzvv70DDlTvv73vv70N77+9OO+/vUzvv711WgDvv73vv70Nae+/vU7vv70D77+977+9Ehvvv73Hq0lADu+/ve+/vW5Q77+9H3Tvv71+77+9Du+/vRFhFBwTQu+/ve+/vUfvv70f77+9Q2IGAO+/vTw9AO+/ve+/ve+/ve+/vX8H77+9cm3vv71w77+9aV4c77+977+977+977+977+9x5Dvv71O77+977+9Mu+/vQc/NO+/vRPvv73vv73vv70cFO+/ve+/ve+/vRLvv71877+977+977+9J++/vUXvv71877+977+9F++/vU0577+9Mu+/ve+/vUTvv71u77+977+9HO+/ve+/vWXvv71l77+9cA3vv71yY++/vQAaVe+/ve+/vV/vv71+77+977+977+977+9FgoWb++/vXNrSiU4Twrvv73vv70E77+9dQJC77+9GRDvv73vv70AJcKkTXhd77+977+977+9Ci0cx4UQ77+9Wu+/ve+/vTjup60IL++/vUMZBEbvv73vv71F77+977+9RSjvv73Og++/vWYe77+977+9MXBOBULvv70M77+977+9BGJr77+9fRvOgO+/ve+/vUbvv71q77+977+9V3okGnPvv73vv73vv70EAyhA77+977+977+977+9Gih8b++/ve+/vX7OljMr77+977+9VDhOCA4KMibvv70b77+9HALvv73vv73vv716YDlg77+977+977+9Gu+/vQ/vv71EOO+/vR8a77+9CX1s77+9cO+/vRRsLDg577+9cO+/vQ9P77+9w4nvv70eIO+/vRdu77+977+977+977+9V++/vWjvv70n77+9Lu+/ve+/vWdK77+9PA1gTu+/ve+/ve+/vUt0K++/ve+/ve+/ve+/ve+/vQTvv71NANeOEyF7OO+/ve+/vWxt77+9aBLvv73vv73vv70yKCEmHe+/ve+/ve+/vSLvv73vv70GLe+/ve+/vTwK77+9Ce+/vXZ4GO+/vQ0aGGYk77+977+977+977+9QCXvv70g77+9PO+/vVbvv73vv70sWu+/vXPvv73vv70E77+9ZXpU77+977+9AwDvv73vv70bSwNdWyPvv71bLSnvv73vv70Y77+9E96h77+9MDcO77+9W2YEPzRC77+9I++/vVhQAgzvv70w77+9E++/vQ4A77+977+9LO+/ve+/ve+/ve+/ve+/ve+/vUJVUu+/vX7vv71a77+977+977+977+977+977+9Su+/vd+yX2zvv71777+9Ue+/ve+/ve+/vULvv70kcR09V++/vSJ977+977+9R9+V77+9eBt077+9eu+/vW4Q77+9LBfvv73vv71W77+977+977+977+977+9b++/vW53zaQJZu+/vVbvv73vv71977+977+9FO+/vXfvv71g77+9K23vv73vv73vv70b77+9Jnvvv73vv70eNisybe+/ve+/vUDvv73vv71DcBwfQu+/ve+/vSHvv71GFca5ce+/ve+/vTZBDu+/vVt377+9CgEoEO+/vUHvv73IshRoTe+/vWLvv71rNO+/vUrvv71p77+9R8ilXhZt77+977+977+977+977+9ZWTvv70D77+9A2zvv71877+9aX58UBzvv70Z77+9Nm3vv73vv70v77+977+977+9e++/vXM277+9Wu+/vVRs77+977+9cu+/vWQ6CO+/vUZc77+9ee+/ve+/ve+/vXHvv71G77+9Fe+/vW3vv73vv73vv71S77+9ee+/vQDvv71DFkDvv73vv73vv71udy7vv70s77+977+9Ce+/vXJr77+977+9F8uoe++/vRPvv711Gu+/vW08WO+/ve+/vWfvv73vv71QJTLvv70GYT0kFS0PfGhEbO+/vQZg77+977+9x7lTW++/vUtG77+9ZV7vv73vv70877+977+9Wu+/ve+/vUfeo++/ve+/vVtg77+977+9AGdR77+977+9SGA0NB0QYwTvv73vv73vv71H77+9QRPvv71+eBhK77+9Qe+/ve+/vVXvv71pKiRQ77+9QO+/ve+/ve+/vX7vv70K3aQkNO+/vRIICzBaFULvv70E77+9RE5nFe+/vUHvv705Oe+/ve+/ve+/vR3vv73vv73vv73vv71ybmnCuCDvv71Z77+9de+/ve+/ve+/ve+/veqylB3vv73vv71JVDhQWu+/ve+/ve+/vXPvv73vv73vv73vv71pdO+/vSnvv73vv70677+9Vgrvv70T77+9AHnvv70b77+977+9DceW77+9I++/vXTvv70A77+9Ju+/vRTvv71eMRDvv73vv704KwHvv73vv70E77+9GzXvv70077+977+9eu+/ve+/ve+/vQl8Yu+/ve+/ve+/vSHvv70R3Y/vv70R77+9yIjvv70Hxqzvv73vv73vv73vv71AUXbvv70H77+9OQnYjwjvv73vv73RrlHvv73vv73vv70177+9dEHvv73vv71cQu+/ve+/ve+/ve+/vW5ccsS/d++/vcaP77+977+9fO+/ve+/vUNdWMOMBGzvv73vv73vv73vv73vv71xYhA177+9du+/vTgv77+977+977+9SO+/vXbvv70T77+9FxdnbRTvv71qMe+/vVQf77+9eu+/vV/roYxg77+9CCUy77+977+9BO+/ve+/ve+/vWoCWm3vv70b77+977+9G++/vSnvv70E77+977+977+977+9Y2Pvv73vv73vv73vv70sWS/vv71N77+977+977+977+9Cn9WGj3vv71BWy0p77+9JULvv71eQ++/ve+/vQgbbO+/vd65Ou+/vUnvv70I26lQ77+977+9AO+/ve+/vU0T77+977+977+9ORTvv71477+977+977+9Th1W77+977+9I++/ve+/vT7vv70677+9eu+/ve+/vRvvv73vv70AdBPvv71Z77+977+977+9PQh/77+9R++/vR/vv73vv70F77+9EFnvv73vv73vv73vv70IHCfvv73vv70b77+9HHRMP1pvKBxiLhPvv73vv711RiDvv71v77+9b++/vQzys6Op77+977+9Jg3vv73vv71J77+977+977+9aUnvv71+E0hvKu+/vWceYu+/ve+/vQDvv73vv71G77+9fu+/ve+/vRg077+9cu+/vSBdOyF1Te+/ve+/vUXvv70K77+977+977+9DWLvv71R77+9Fu+/vWDvv71FBO+/ve+/vQnvv70A77+977+9cTRfUe+/vQ3qnZR377+977+9LO+/ve+/vSB/U3l/77+9BO+/vSJncu+/ve+/vQTvv71WYu+/ve+/vRPvv73klJ8T77+977+977+9Ru+/ve+/ve+/ve+/vdms77+977+9Rg7vv73vv73vv70yfe+/ve+/ve+/vTIx77+9Se+/vRjbsRrvv73vv71xOzbvv71Z77+977+977+977+9CH1mSO+/ve+/vUrvv71B77+977+9PO+/ve+/vWZbGgAj77+9floCdU9177+9cXRo77+977+9KTzXnwU677+977+9zakYFwNoKkdu77+9Sjbvv70U77+9YO+/ve+/ve+/vXISVe+/ve+/ve+/vXZYGO+/vQlJGO+/vSTvv708Pe+/ve+/ve+/vTrvv73vv70DAE3vv73vv70577+9dSLvv70v77+9AQRZ77+9ee+/vTTvv707KdGh77+9N++/vSTvv70D77+977+9KUDvv73vv73vv71AXHzvv70WSu+/vUnvv70V77+977+977+977+9MMqu77+977+9MCPvv71+a++/vVfvv73vv73vv73vv70lO++/vQXvv71OKu+/ve+/ve+Yvs2xSe+/vWNA77+977+977+977+9M3dr77+977+977+9VG/vv71W77+9ax4B77+977+9Q++/vXjvv70J77+977+9zrXvv70P77+9P04s77+977+977+977+977+977+977+9E2tUYe+/vTvvv73vv73vv71V4qi+eu+/vT4Q77+977+9eUYARO+/vSLvv71KC++/vUDvv73vv73vv71I77+977+977+977+977+9De+/vU0a77+977+977+9Qyjvv71G77+9SScs77+9UO+/ve+/vRHvv71BFu+/ve+/ve+/ve+/ve+/vXzWjU/vv73vv70EMu+/ve+/vWw/NBUSCEdh77+9Nwoi77+977+9be+/ve+/vRg077+9RlDvv73vv71xUnDvv73vv70y2Lrvv73vv73vv73CtCgG77+977+977+9Tj8lAdOCWMq677+9Vu+/ve+/ve+/vVsB77+977+9eu+/ve+/ve+/vVbvv70KV9ejdu+/ve+/vS3vv703VQ92T2bvv71KZu+/vQnvv708K++/vQvvv73vv70x77+9AjIX77+977+9QO+/vUESW++/vSvvv70S77+9PwA677+9NDXvv71S77+977+977+9Ru+/ve+/ve+/vWTvv71PeU3vv71ZCdiOCO+/vTXvv71mFO+/vdK0OO+/vTZ3CU/vv708JQjvv70S77+977+926IL77+9IBoK77+9LO+/ve+/ve+/ve+/ve+/vSEG77+9SEbvv70e77+9JyXvv73vv73vv73vv73vv70dau+/vUXFnT3vv73vv73vv73vv73vv73vv73vv70nPO+/ve+/ve+/ve+/ve+/vSvvv71977+9a++/vcaK77+977+9NQDvv73vv71777+9RO+/vTkU77+9Xu+/vQVIKj/vv70L77+977+977+9Me+/ve+/ve+/ve+/vQHvv70HRu+/ve+/vRDvv73vv73vv71A77+9Se+/vRLvv73vv71gTXZwCzYdX++/vVHvv717T++/vXFebO+/ve+/ve+/vU/vv73vv73vv73vv73vv70G77+9GO+/ve+/vUTvv71u77+977+9JxZp77+9dj/vv73vv73vv73vv70iC++/vVQhcu+/ve+/vQXvv73vv71cVzjvv73vv71kTBF177+9Umgq77+977+9w4A1Kyjvv73Fje+/ve+/vXpgWjjvv73vv73vv71OBe+/vdm5A2fvv71NRe+/vSjvv71977+9O13vv73vv71MNzHvv73vv73vv71U77+9C++/vWVeP0/vv704Pu+/ve+/vce777+977+977+977+9aRvvv71f77+9EV3vv71B77+9HQ7Ige+/ve+/vV7vv70w77+9YUbvv70FJe+/vUbvv71zSe+/vSPvv71h77+9atW+Ih/vv71SVO+/vdedBu+/ve+/vS3vv73vv71z77+9VO+/ve+/vWDvv70sH++/vSdTPynvv73vv73vv73vv71RdV8P77+977+9B++/vXTvv71UJe+/ve+/vTc5J++/ve+/ve+/ve+/ve+/vSpk77+9Iu+/vcWq77+9Umnvv73vv73vv71dFS/vv71k77+977+977+9Xu+/ve+/vVLvv70ByK5677+977+9XE/vv70FRW4177+9Ttua77+977+977+9C++/ve+/vQTvv70uK++/vVnvv705Esir77+977+9Se+/ve+/vSoQ77+977+9AO+/ve+/vQRY77+9MhBs77+924nvv73vv71UVF4o77+9Ye+/vTPvv73vv73vv71C77+9fe+/ve+/vWXvv70YaNq077+9Cu+/vXnNit6XLSnvv73vv73vv70ACu+/vQnvv701ce+/ve+/vRbvv73vv71sTQfvv70s77+9Ru+/vTghBO+/vShbC++/vSEW3Y/vv70R77+9Qj8k77+9Zu+/ve+/vQBfLO+/vSrvv73vv70uIO+/vVZIR++/vXUT77+9KDw3AN2EJO+/vQIkNmnvv71/77+9CO+/vcOi77+9Aj80NWLvv70m77+9ecKG77+977+9w6fvv73vv71nBO+/vTwp77+9Uu+/vQMsGu+/vS4Z77+9eU3vv71iKO+/ve+/vTd177+9MAs177+977+9FO+/ve+/ve+/vWLvv71sRu+/ve+/vT7vv70OT1kfVCRo77+977+9OF0l77+977+977+9fSzvv71zVe+/vV/vv71KOO+/vTI40qkQ77+977+977+9f++/vQ3vv70f77+9H++/vRTvv71U77+9eRjvv70XDO+/ve+/ve+/vde077+977+9cG0LL9u477+9OlLvv73vv71uDs2/Le+/vdSbX++/vUrvv70VXlgP77+977+977+9BO+/vTsRXe+/vUXvv70jXVkRaCBVEELvv73vv73vv73vv70aZ++/vXRpEWQfC++/vR5xcHYF0ZVaJBrvv73vv71d77+9RO+/vX5aAmIr77+9Ntu177+9Iu+/vW9177+977+977+9fu+/vQbvv73vv71F77+977+9R2Tvv70O77+9b3Rb77+977+9R1s177+9Ce+/vRzvv70SZRBb77+9Te+/ve+/ve+/ve+/ve+/vXFOAu+/vWdlbw5UIWjvv73ehe+/vSrvv70Q77+977+977+977+9Tgjvv73vv70N77+977+9JSvvv73vv73vv71ReO+/vR9t77+9H++/vWDvv70v77+9Ue+/vXQnCEvvv73vv70ZMBo1be+/vTbvv73vv70wXSHvv73vv70tMu+/ve+/ve+/ve+/vV7vv71xTjzvv73vv71477+977+9b++/vXYDfXvvv718He+/vSIRXifvv73vv73vv73vv73vv71577+977+977+977+9b++/ve+/vciDVVUo77+977+9Gyci77+9fkjvv71677+9a23vv71sajrvv73vv71677+9C++/vTZ+VHN/77+977+977+977+977+9VHjvv71h77+977+9FQtK77+977+9wrRXDFQhI++/ve+/vX1a77+977+9Zu+/ve+/vXjvv73vv70f77+9Cu+/ve+/vShs77+977+977+9ZgdU77+977+9UTvvv73vv70O77+977+9cxwX77+977+977+9VO+/vV/vv71l77+977+9PjdiaEVtTik4JgQ1SRDvv73UgNqnO++/ve+/vTPvv73vv70ebe+/vTnvv716G++/ve+/ve+/vciS77+9K++/ve+/ve+/vVjvv70f77+9Gu+/vVhS77+977+9bu+/vW8n77+977+977+9Ve+/vUTvv70377+977+977+9VU3vv71477+9JSU/JFrvv73vv73vv70277+9b++/vWnvv70YYu+/vWjvv73vv70DSmhAa09877+9De+/vU7vv73StN2P77+9EO+/vXjvv70gEGEpJu+/ve+/vQbvv73SlO+/vUsAzpI6Ru+/vU0uCO+/vTJcZ++/ve+/vWhy77+977+9FmPvv71TNu+/ve+/vQFt77+977+9FUFp77+977+9e++/vTINOQrvv71xaGtE77+9Ju+/ve+/ve+/vUzvv73vv70O77+9Xu+/ve+/vXEg77+9IwDvv73vv70WUe+/vVA3CE8BCu+/ve+/ve+/vULvv73vv73vv73vv71fUe+/ve+/vTAK77+977+9Ie+/vQ49L1jvv71s2rpn77+977+9Ee+/ve+/vXrvv7177pCOOO+/vVXvv73vv70e77+977+9EW/vv70N77+9be+/ve+/vQZhOu+/ve+/vT0K77+9ZO+/ve+/vQHvv73vv71wTlHvv70aYWfvv73vv71477+9X24H77+9SO+/ve+/ve+/vRYeLe+/vRZCDu+/vVbvv71/77+977+9fe+/vSzvv73vv709XRzvv71L77+977+977+9fnJ277+977+977+9Vwvvv73vv71b77+977+977+9Au+/ve+/vRjvv73vv71UOWjvv73vv71H77+9ez1kLwvCpu+/vWrvv70vDEg2Ce+/vX3vv70DQu+/ve+/ve+/vTp077+977+977+9O3Lvv73vv73vv70v77+9UXBsCO+/vRoJ77+9GjFr77+977+977+977+9Q18Y77+9fsmSKgwa77+9EAXvv73vv73vv73vv73vv71577+977+977+91Yvvv70FOO+/vQzvv71r25NL77+9QV1X77+9dFcJb++/vW3vv70A1KEC77+977+9TBbvv73vv70G77+9Vu+/ve+/ve+/ve+/vQPKvA/vv70sZgPvv73vv71rae+/ve+/ve+/vVLvv73vv70a77+9H++/vUJHVu+/vSbvv70o77+977+9QigRBsa5Ce+/ve+/ve+/vU5rX++/vcui77+977+9Qu+/ve+/ve+/ve+/ve+/vSkJVO+/ve+/vQtG77+977+977+9BO+/vS9ZBwpbRhHvv73vv70R77+9XO+/vQVo77+9Wu+/vUs877+9OO+/vc2n77+9DGlJ77+9Je+/vUkB77+9MylgDRTvv73vv73vv73vv73vv70NT3Tvv73vv73vv73vv73vv70RagQ+NO+/ve+/vQLvv707bO+/ve+/vVBQFe+/vdKP77+977+9CUdbax/vv71S77+9amlwae+/vR/vv70e77+9M0/vv73vv71p77+9Lu+/ve+/vRZRwrzvv71BJO+/ve+/ve+/vTVtN++/vUrvv73vv70v77+977+977+9UO+/ve+/vSrvv73vv70tM++/vS18OmJsXhxD77+9de+/vWnvv73vv73vv70PM3rvv70a77+977+977+9cyjvv71V77+977+9b++/vWTvv73vv71J77+9I13vv73Wpe+/vR3vv73vv70A1pzvv71i77+9Ye+/ve+/vRkbfg5COArXmQFYDu+/vQAKQWTvv70O3Y/vv73vv71BwqILfe+/vR/vv73vv73vv717Xjfvv73vv73vv70N77+977+9Pu+/ve+/ve+/ve+/vTrvv73vv73vv71MA++/vW08Ql8Z77+9f++/vQQ5yIBwFO+/ve+/vTlbcu+/ve+/vWbvv73vv73vv71WT++/vXF+bO+/vSUZIQxzJD8x77+977+9bu+/ve+/vQvvv71677+9QRTvv70tAFk5Ie+/vVvvv71v77+977+9Yu+/ve+/vXzvv71RSArvv71yZu+/ve+/vQBl77+977+9wrw477+977+9LNOi77+977+977+9LO+/vWE/Ku+/ve+/vQvvv70Y77+9Ke+/vXpDYRYL77+977+9FO+/vTgR77+9UwPvv71PTWLLmSVQQiw0VRLvv70edu+/vUfvv71377+9OO+/vQ3vv71177+9H++/vTfvv71gDRTmvaNo77+977+9BO+/ve+/ve+/ve+/ve+/vTDvv70u77+9Qe+/vWXvv73vv71QAe+/vUvvv73vv73vv73vv71TE++/ve+/vdudM++/ve+/vUQZBD4077+9elAU77+977+9aO+/ve+/vTVi77+977+9Vkjvv73vv70i77+9Su+/vRHvv73vv73vv70877+9BO+/vVHvv73vv73vv73vv70777+9O++/ve+/ve+/vUJ577+977+977+9Pu+/vWfvv71x77+977+9LF/vv708be+/vRNj77+9V1bvv73vv70eUO+/ve+/vTXvv71p77+9MO+/vR7vv71577+9NO+/vVAiQwnvv71ZUe+/ve+/vQYNNO+/vXLvv71fJO+/vX5yMhUhVu+/ve+/ve+/vSjvv71Y77+9PjPvv73vv73vv70Zah/vv73vv70SYO+/vXtv77+977+9FO+/ve+/vRjvv71T77+9UO+/vQzvv70e77+977+977+9NyvUvtiDcV4c77+977+9DO+/vXzvv70xau+/vSNTSu+/ve+/vUAoSe+/vWDvv71t77+977+9zYpE77+9AO+/vUQCX++/vUA/SQQjUBAO77+9fBxiGWk877+977+9BErvv73vv73vv73vv73vv71T77+977+9Ul0xCO+/vTVMT++/vWd8LgDvv73vv71bLu+/vVbvv73vv73vv73vv71TNe+/ve+/vVTvv71J77+977+9Hkjvv73vv73vv71BVE/vv71P27/vv73vv73dtO+/vWjvv73LmhVwFjVr77+9OE8K77+9eUYAAO+/vXpjYe+/vXHvv70rUFDvv71gFzjvv70CNe+/vWDLme+/vVnvv71i77+9TkXvv71D77+9EO+/vXLvv73dj++/ve+/vXvHtDPvv70o77+9CO+/ve+/vTXvv70+77+9SO+/ve+/vSoUSAzvv71SBO+/vSlrOe+/ve+/vWEtSmpF77+9Ze+/vSkrSe+/vSAG77+9KADvv70E2YgcYe+/vXIv77+9EHtYUe+/ve+/ve+/ve+/vTJSN++/ve+/vUNMAe+/vUgx77+9cnzvv73vv71cQu+/vQU+GO+/vSTvv73vv73vv73vv73vv70Z77+9L1vvv73vv73vv71rHgAg77+9WgPvv70/Le+/ve+/vSzMi++/vVlVde+/vSV677+977+977+9SO+/ve+/vXIJ77+9D3fvv73vv73vv70RPU/vv71AOArvv71kEe+/ve+/vVvvv73vv71UHceEPnfvv73vv73vv71vcHjvv73vv73vv70obkrvv73vv71AHwzvv73vv73vv70CSu+/vQ/vv71s77+977+977+9Bwo+D35g77+9Z++/vRthKe+/ve+/vQRfKO+/ve+/vV4HQn3vv73ohKUo77+977+9C8u+UURXae+/vXJdYVYQ77+977+9XjcIS++/vXvHhO+/vUfvv73vv73vv71045Oo77+9czfvv70AYjvvv70t77+977+9HSDvv70x77+9HUbvv70277+9Re+/vUc3We+/vQ3vv73vv71G77+977+9ZBnvv71Sfu+/ve+/ve+/ve+/ve+/ve+/ve+/vWs6Iit177+977+9ei0ndjFOKjI1GO+/vV/vv70v77+977+977+977+9TO+/ve+/ve+/vRQ7be+/vXND77+9O++/vQ3vv73vv71I77+9bBTvv714SVFW77+9RDpG77+9Au+/vQ0aCO+/vRLvv71I77+977+977+9Mu+/vQDvv70977+9SHZwY++/ve+/ve+/ve+/ve+/vSJf77+9XDok77+9HiNQD++/vSfvv71f77+9Bnrvv73vv73vv73vv73vv70H77+9fDw477+977+9ce+/ve+/ve+/vRfvv71977+9OQZvV1Dvv71CB++/ve+/ve+/vVBwTBBb77+9Ks2K77+9MhoKKmNI77+9TO+/ve+/vRLvv70QNO+/vVsy77+9U++/ve+/vRJf77+977+977+977+977+9Ie+/ve+/vT/vv71EOe+/ve+/ve+/vTAjGTEv77+9WO+/vXPvv73vv71W77+977+9aWHvv70HAe+/ve+/ve+/vXZORDXvv71EIyvvv73vv73Cnxvvv73RtVrvv73vv71lJu+/vSfvv73vv71u77+9Mw7vv70hYe+/ve+/vUQQXe+/ve+/vXLWgShe77+977+977+9Ie+/ve+/ve+/vUFF77+977+977+977+977+9PwHvv71DUu+/vTbvv71KC++/ve+/vSDvv73vv70WPU/vv70xVgZ477+977+977+977+9Su+/ve+/vR3vv71W77+9IO+/vQBh77+977+977+9fx7vv73vv70N77+9dO+/ve+/vVMAWe+/ve+/vSHvv70n77+977+977+977+9H8uxah7vv712XCrvv73vv71277+9K++/ve+/vQ5h77+977+977+977+977+977+977+9TO+/vXXvv73vv70O77+9JgXvv71Pd++/ve+/vSjvv7192brvv70m77+977+977+977+9du+/ve+/vWTvv73vv73QiN6yT2wi77+9R++/vSjvv73vv73vv71D1KFD0qFT1KFT0qFL77+9Iu+/ve+/vQjvv70877+977+977+977+977+9I0lZJO+/vdyBPQ3vv71CSxRYWAk2Vu+/ve+/ve+/vWBhJe+/ve+/vQnvv73vv71x77+9CDHvv73vv70J77+9CHHvv73vv73vv70zVCHvv73vv71y77+9Wu+/vdumUGTvv73vv70QSTLvv71cY0Lvv73vv73vv73vv70777+977+9bu+/ve+/vRjvv70tI++/vXTvv70nBWE7Ku+/ve+/vWYe77+9f1gQWe+/ve+/vUE777+977+977+9L++/ve+/ve+/vQ7vv73vv71SYO+/vR3vv71n36FA77+9bdSV77+977+9LSUo77+977+9yZ/vv71/77+977+977+9ae+/vWTvv73vv71O77+9bu+/vQjvv73vv70B77+977+9Mu+/ve+/ve+/ve+/vXzvv73vv71Lfe+/vR0WBhUJNhxbPmTvv71Q77+9yKPItzrvv73vv73vv73ars+A77+977+9RuOesU7vv71J77+9Su+/vUoqH++/vRHvv70s77+977+9LFgXN2Nz0ojvv70JE++/vSTvv70Q77+9TxPvv73vv70wKmrvv70I77+977+977+9GCfvv70xWRfvv71UfRA6Mjxz77+977+9zbZ4Xu+/vW4Ofe+/vXMocXbvv71977+977+9U++/ve+/ve+/vQbvv73Uuh/vv70P77+9Cu+/ve+/ve+/vRYcG++/ve+/ve+/ve+/vd6DQO+/vSkp77+977+9ae+/ve+/vTnvv73vv70NMElU77+9EhRZ77+977+93pctS3pf77+9LtyWaxvvv71iO3pf77+9ZnVT77+9enXvv73vv70C77+9UxQcFe+/ve+/vTfvv71077+9fO+/vRnvv71DKu+/vVjvv73vv71xL3VA77+977+977+977+9IRbvv73mhbFN77+9UwJV77+9Ku+/vSrvv71PIgrvv73vv712LO+/vdmxMmrvv73vv73vv70Z77+9Tu+/vQ3liIpJ77+9MGbvv70D77+9Z++/vWHvv73vv70HA++/ve+/vSUQ77+977+9Ju+/ve+/vdaW0Lfvv70JUO+/ve+/vW/vv70M77+977+9QRbvv70X77+9DO+/ve+/ve+/ve+/vUUtPCPvv71l77+9Se+/ve+/vT7vv71aXiMu77+9Ue+/ve+/vTtk77+977+9Ze+/vSU977+9LkAO77+977+977+977+977+977+977+9J++/vXtgG++/vX5C77+977+977+9Jzvvv73ptIFp77+977+9E++/vXDvv70R77+9EmMg77+977+9PSzvv71eTe+/vWAya++/vRMMNh5T77+977+977+977+977+9Ci/vv71TE++/vXJ277+9Vgfvv73vv70iee+/ve+/ve+/vSBj77+9LzI777+9cO+/vS1JIz4PO++/vVXEiRUxC2Tvv71r77+977+9Oe+/vWLvv70+77+9RUYP77+9Ne+/vVHvv73vv73vv70w77+9BO+/vWvvv73vv73vv71VFe+/ve+/vRxsZ++/ve+/ve+/vXzvv70kGgTvv73vv71Q77+977+977+977+9RxTvv73vv71i77+9Uu+/ve+/ve+/vXl2Su+/ve+/vS0e77+9I++/vUbvv70Q77+977+977+977+977+9B++/ve+/vUbvv73Xr27vv73vv705Ae+/ve+/ve+/ve+/vTg2BMaoZu+/vUnvv7091ZlO77+977+9JO+/vWdt77+9e1oya1Jg77+9ZWTvv70477+9PCTvv73vv73vv73vv73vv73vv70qT++/vVDvv73vv70M77+977+9Q++/ve+/vdOIE28HSlEfN++/ve+/vSR177+9CA42d++/vVBLN++/vXPvv71e77+9cj1d77+977+9NkTvv73vv71n77+9wrpdFjRLUe+/ve+/ve+/vSwyTe+/ve+/vUXng44h77+9dO+/vWLvv70jDsepQWgqRRAO77+9Nu+/vRYe77+977+9Wu+/ve+/ve+/vTBR77+977+977+9xrnvv71Bbe+/ve+/vRlhcu+/vT3vv73vv71II++/vRps77+9be+/ve+/ve+/vQpK77+977+977+977+9OCfvv73vv71i77+9A3c477+977+9VhDvv71Ccu+/vVDvv73vv73vv70+ce+/ve+/ve+/ve+/vRXvv71/77+977+9X++/ve+/vUXvv73vv73vv73IpsKz77+977+9We+/vWoabO+/ve+/vRsq77+9P3xl77+9T++/vXfvv70L77+977+977+977+9U3RBHGPvv73vv71h77+9IRF/JcKEeu+/ve+/ve+/vQjvv71v77+9QFVS77+977+977+977+9LCoq77+977+9Se+/ve+/ve+/vTIt77+9b1hTOe+/vQdgOSDvv73vv73vv71677+9HCfvv73Ko+G8ku+/vX/vv70E77+977+9Jzrvv71C77+977+977+9dEQo77+9LT8lee+/ve+/vRbvv719BTHvv71n77+977+9Uu+/ve+/vW0577+9NERlVO+/vRjvv73MjMS277+977+9RO+/vTs9xr3vv70O77+9QO+/ve+/vc6j77+9FQsi77+977+9cg4d3KoH77+977+9az0k77+977+977+977+9fHMZ77+9Zu+/ve+/ve+/ve+/vRnvv73vv73vv73vv71+77+9GHF177+9FCXvv73vv70FQAHvv73vv71bd++/ve+/ve+/ve+/ve+/vV9Y77+9ce+/ve+/vQ3vv73vv73vv710UVQmCe+/ve+/ve+/vSXvv71/77+977+9bgvvv71If0zvv73vv70SX++/vVEmax3vv71Z77+977+977+9I++/vUbvv73vv70GDe+/vXNjKO+/ve+/vTvvv71377+977+9Oe+/ve+/ve+/vdC2cS9177+9ee+/ve+/ve+/ve+/vVEJIe+/ve+/vR/vv73vv70e77+977+9Vu+/ve+/ve+/vTI777+9VO+/vSQ277+9VO+/vUXvv70yU++/ve+/ve+/vQxg77+977+9AO+/vSDvv715RFdo77+9fu+/ve+/vUDvv71wEe+/ve+/vXkrOO+/vQLvv73vv73vv71y77+977+9M++/vU7vv73vv716aHR177+977+977+9ee+/ve+/ve+/vVVK77+9RcSBF++/ve+/vVga77+977+9TSpXFW0E77+977+977+9PRN277+977+977+9N++/vXDvv73vv70J77+977+977+977+977+9SBNM77+977+9Mduy77+9R8aZ77+92Kp8Ju+/vVIz77+9cxIoPMeXRVs977+977+977+9BzXWmDYd77+977+9eu+/ve+/vR3vv70H77+977+977+9FO+/ve+/ve+/vVMRTu+/vSRA77+9be+/vUlh77+9KwDvv70zP28877+9dO+/vVhII++/vQN577+9fe+/ve+/ve+/vXLvv71B77+9E3YkNmlQ77+9SNeQ77+977+977+977+977+9CT1P2Lfvv73vv71k77+977+977+977+9ZTd577+9Zg4DUCnvv71/Qu+/vXjvv73vv70GTUkDdhZ5b9yXYEBx77+977+9Z1R477+9Ju+/ve+/ve+/ve+/ve+/vRsMGe+/ve+/ve+/vQDvv73vv73VsTbvv71B77+9F++/vQzvv70S77+977+9emgE77+9UwLvv73vv73vv70C77+9G++/vT4677+9OO+/vUfvv71h77+977+9GO+/ve+/ve+/vSoE77+977+9DXDvv71k77+977+92b4W77+977+9Ru+/ve+/ve+/ve+/ve+/vUXvv73Qt++/ve+/ve+/ve+/vWQc77+9Pwvvv73vv73vv71nQe+/vUXvv73vv73vv73vv73vv71z379M77+9bOuxtXUiah7vv73vv73vv73vv70aZ++/vQXvv73vv73Lou+/ve+/vSJI77+9D++/ve+/vTbvv73vv73vv73vv71777+977+9Ou+/vQ4Z77+977+9R++/vSI877+9RUNy77+977+9N1Eb77+977+9a++/vQYAHO+/vWUhNiZ377+977+977+9Ok0EF++/ve+/ve+/vUBzT++/ve+/vUxs77+9bGrvv73vv70w77+977+977+977+90Y5hXhxD77+9De+/vWnvv73vv714QQnvv70HRu+/vUQY77+977+9Me+/ve+/vSgwLe+/ve+/vTgnAU3vv70E77+9UkgBFu+/vTUaBD4wQe+/ve+/ve+/vRjvv70L77+9DX4xOO+/vQ7vv73vv73vv73vv73vv73vv712ee+/ve+/ve+/ve+/ve+/vQB5Oiso77+9blXvv73vv73vv71J77+977+9z7gy77+9SO+/ve+/vdSIYO+/ve+/vUVs77+9Fu+/vX3vv71ZdxbfoEHvv70tLjAmBULvv70CQijvv73vv73vv73vv70TIe+/ve+/ve+/vQLvv73vv73vv70bXe+/vQXvv71n77+9Z2nUhnvvv70nYn1i54yeX++/vdqCC++/vQ0A77+977+93bV477+977+9bu+/ve+/ve+/ve+/ve+/vRDvv70p3ojvv716X++/ve+/ve+/ve+/ve+/ve+/ve+/ve+/vSnbtO+/ve+/vQgIT1Hvv71077+977+9QU1k77+977+9Jwnvv73vv71V77+9Tu+/ve+/vSDvv70q77+90Lvvv704Nzbvv71V77+977+9C++/ve+/ve+/vX7vv70T77+977+977+977+977+9Gk0tQB7IsnN977+977+977+9Rmfvv71YQe+/ve+/vdScHGTJju+/ve+/vQPvv73vv71G77+977+977+9GDJEIO+/vVQLOe+/vUBs77+9EVnvv71FeO+/vW7vv71B77+9Ke+/ve+/vRnvv71q77+977+9On0A77+93ZIW77+9d0zvv71Z77+9e++/vSAH77+977+977+9PQPvv71v77+977+9ae+/ve+/vT5h77+9b++/ve+/ve+/ve+/ve+/ve+/vWgX77+9Zcyk77+977+90L7vv71eaVfvv71V77+9Ye+/vUTvv73vv73vv73vv70C77+977+9ZO+/ve+/vVNK070L77+977+977+9ekB077+9NO+/vQLvv704Ee+/vQopRUHvv71t77+977+9W29277+977+9Xu+/vT1oe3osUe+/vRnvv71LanTvv70M77+9ZBDvv71kDljvv70K77+9N++/vVjvv71+a3jvv73vv73vv70AVO+/ve+/vT8W77+977+977+9Zkjvv70D77+9Wu+/vdCnBiTvv714OE8NZO+/vRDvv73vv73Wre+/vX9nPygCX++/vSXvv73Ntu+/ve+/ve+/ve+/ve+/vTNl77+9Bu+/vXjvv73vv73vv70l77+9We+/vVITw7t1X2Z9dsSWRTtV77+9IO+/vWvvv70oOO+/vdGEcxxN77+9yIAb77+9f++/ve+/vW3vv71j77+9Ce+/ve+/vWRN77+977+9KO+/ve+/vQPPq1bvv73vv73vv70977+9du+/vXXvv71MR++/vXoaSFF2Sz0sIkt177+9fdG6Ve+/vRrWrO+/vRrvv71a77+977+9QUVT77+977+9Pe+/ve+/vT9WMc2P77+9OkXvv71w77+977+9KSoddzjvv70aDe+/ve+/vVnvv70cARZAKO+/vSHvv71J77+977+9Uw8Kfh3vv73aonrvv73vv73vv70q77+9ee+/vWhqQgfvv73vv70wI++/vSfdpu+/vWRVV9S977+9Lmbvv718ZcyK77+9Ghbvv70xd++/vU4PcgDvv73vv73vv71777+977+9N++/ve+/ve+/vdedUFk877+9w7ENC++/vSojJWg7Iu+/ve+/ve+/vVc6JO+/vUBdNu+/vd+H77+977+9YdOJ77+977+9bUrvv73vv70v77+9Rx1Q77+9Pu+/vSA2Xu+/vVEI77+9Mu+/vUfvv73vv71Mf++/vVfvv70QO++/ve+/vT7nl4M+XiDvv73vv70bdynvv71dWu+/vS0iFO+/vWbvv73vv73vv73vv71rQl/vv71H77+977+9Vu+/vX8p77+977+977+9Ygfvv70g77+977+9RTjvv73vv73vv719KO+/vcSX77+977+977+9ai1677+977+9DQnvv71F16Q077+977+9XMqa77+9Oe+/vVvvv70eFu+/vXzvv71rbTN577+9UtS/77+977+97p6I05vvv73vv73vv73vv70s77+9Nu+/vRxgHgrvv70D77+9Pe+/ve+/ve+/vW9JGnF677+9Hu+/ve+/vXtiemPvv73vv71r77+9S23vv70t77+9RD4H77+914Tvv70D77+977+977+9Qe+/vWXvv73vv73vv73vv71cCibvv73vv70ICu+/ve+/vWDdpu+/ve+/ve+/vU/vv73vv70iK3VrB1nvv703Se+/vX1+77+977+977+9IxN0FQwK77+9fe+/ve+/ve+/vVMlwoTvv70uKmJA77+9dVQn77+9QO+/ve+/ve+/vQDvv73vv71EEXjvv70ENe+/vdqFLe+/ve+/ve+/ve+/vQY677+9dO+/vURwdhXvv73vv70677+9Au+/vXoNZD8LTe+/ve+/vUQT77+977+9ex1g77+9NO+/vXbvv73vv73vv70i2Zo/VREh77+977+93Lvvv71xbGhP77+977+977+977+9JWnvv70F77+9c++/vWnvv70F77+9Cw3vv70pEeK4onDTkCt277+9Se+/vR0sRUjvv71377+977+977+9IFgV77+977+977+9Qu+/ve+/vQfvv73vv71E77+977+977+9F8SrdOOlte+/ve+/vQ0EdO+/vX54Je+/ve+/vde477+977+977+9fmQEQu+/vQzvv70BYCk477+9Cl3vv70Iy4FRFF3vv73vv71+Wnbvv71L77+9MWjvv73Tie+/ve+/ve+/vQw/Xu+/ve+/ve+/vQgsB0Tvv73vv70kQE3vv70/77+977+9Z++/ve+/ve+/ve+/vTDvv70M77+9IxPvv70s77+977+9Ou+/ve+/ve+/vT/vv719Te+/ve+/vQ7vv70bLu+/ve+/vQFI77+9NO+/ve+/vUtK77+977+9fjbvv73vv73vv73vv71GAe+/vRlJCO+/vTLvv70eLl1rbO+/ve+/vRhibu+/vcaJYO+/vRTvv70fUgHvv73vv71mDXxv77+9EO+/ve+/vQjvv73vv71BPzkJ77+977+91IsKf++/ve+/vXsoAmtQVu+/vT7dqe+/vU9M77+977+9KO+/vRc8Ve+/ve+/vX0G3LIW77+977+9HGPvv73vv71eRm/vv73vv71kHVbFrO+/ve+/ve+/ve+/vRUB77+9Cu+/ve+/ve+/vSjvv73vv70P77+9ABBK77+9Qu+/vUHvv71Q77+9A2Mz77+9Ru+/ve+/vRPvv715SG4O77+9RTFo77+9RO+/ve+/ve+/ve+/vX54BO+/ve+/vcOw77+9H++/vWFacu+/ve+/ve+/vXzvv71D77+977+9XUhk77+977+977+9JFDvv71QN++/ve+/ve+/ve+/vX3OimTvv70YYu+/vSrvv71E77+96r6377+9MlLvv73vv71dEVnvv70P77+9KtuVYHLvv73vv73vv73vv706fg/vv71c77+977+977+977+9Le+/vTfJlu+/ve+/vWLvv70477+977+9K++/ve+/ve+/ve+/ve+/ve+/vWnvv73vv70DcO+/vVJwdAjvv70F77+9YSLvv70DL0p256e177+9Ne+/vVfvv73vv73vv70x77+977+977+9Ke+/vXDvv73vv711Fu+/ve+/ve+/ve+/vX5vbznvv73vv70e4bueXe+/vSrvv73vv73vv70n77+9yrbvv73vv73vv73vv73vv70677+9Uu+/ve+/ve+/ve+/vX3vv73vv71y77+977+9fO+/ve+/ve+/ve+/vRFtf3BCDu+/ve+/vW4K77+977+977+977+9eu+/vQtSd3bvv73vv71xXAjvv71ORXTvv70G77+977+9Y1cC77+9RmHvv73vv70d07dZ77+9JHDvv71Y77+977+93p7vv71r77+92Z/vv70B77+977+977+9AGbvv71BG++/ve+/vRtOLO+/ve+/vcK577+977+977+91Ijvv71977+9C++/ve+/vVrvv73vv73vv73vv71xahDvv705Ze+/vWfvv73vv73KkO+/vce677+977+9Ee+/ve+/vRhsOu+/vWwI77+977+977+9WzAu77+9fVFxQ++/ve+/ve+/ve+/vXpz0oDvv73vv71m77+9dO+/vS9jEQcn77+977+9ZVXvv71lbe+/ve+/ve+/ve+/ve+/ve+/ve+/vVfvv711Z++/vVVqYnjvv718Be+/vTTvv71+G++/ve+/ve+/ve+/ve+/vSLvv71+77+9fO+/vThnU++/vTgxAO+/vRjvv70CBe+/ve+/ve+/vWERXe+/vUPvv70LPe+/ve+/vXXvv71SS1kP77+9DAzvv73vv73aqToM77+977+9X28XOO+/vWLvv71A77+9Kj4q77+977+9Xu+/vRzvv70b0KV677+9RgrcnmXvv71fWO+/vVXvv71RWO+/ve+/ve+/ve+/vTnvv71U77+9A03vv71V77+9Ou+/ve+/vQbvv73vv71qx4nvv715OFUx77+9fsaWKu+/vRxBQRTvv70TWGY/Nu+/ve+/vTw+De+/vXBt77+9NO+/vVQO77+977+977+9ZG/vv70N77+977+9I++/ve+/vQ9d77+977+9V1/vv70ufe+/vQZG77+9PWXvv73vv73vv73vv70377+977+977+9Ve+/vVfvv70XLXtgDDUb77+977+9AO+/vdGU77+9dhfvv7048auah++/vUtkCe+/vTXvv71rRO+/vXbvv71FE++/ve+/vTUU77+9Ie+/ve+/vRLvv73vv71477+977+977+977+9bj/vv73vv71lD++/vU7vv71F77+977+9Ze+/ve+/vTUP77+9UnLvv70WAABEV2nvv719c++/vVLvv71Y6bW9azNB77+977+977+9Au+/ve+/ve+/ve+/ve+/vUEOAAfvv71I77+9De+/vU3vv70uf++/vVHvv73vv71lbe+/ve+/vUrGhu+/vTg577+977+977+9Gu+/vWoA77+9LVPvv73vv73vv70AGFQ4Tx4bGQxnUe+/ve+/vRbvv70z77+9x4Tvv73vv73vv71e77+977+977+9K++/ve+/ve+/ve+/ve+/ve+/vTrvv7122YHvv73vv71KZmXvv70+77+977+977+9Tu+/vSTvv71x77+9w7k1FgNrVFPvv73vv73vv73vv73vv73vv70D0ZXvv73vv70977+977+977+9MO+/vUjvv73dte+/vWJ677+977+9eXEM77+9PeKIrdOi77+9Nu+/vUjvv70Q77+9ZTfvv71uMi/vv73vv70BAO+/vRLvv73vv717Il7vv73FtVvarGRF77+9Ne+/vUfvv73vv70877+9H3ZO77+9TxZ277+977+9VSjvv71dXe+/ve+/vSbvv71xTe+/vUYw77+9wrJ/ZC5rUFfvv73vv73vv73vv70gKu+/vRHvv70VY2A/Ju+/ve+/vWZhRO+/vVHvv71S2I/vv73vv712WDjvv73vv70k77+9Tu+/ve+/ve+/ve+/vUjvv73vv73vv70g77+9dzvvv73vv71/77+977+9GQ3vv70/77+977+9Je+/vT0ATu+/ve+/vRpdWu+/vd2XAu+/ve+/ve+/ve+/ve+/vXbvv71SKu+/ve+/vULvv71u77+9CO+/vVgG77+9VFBx77+9G9aDI++/ve+/ve+/vT9oByrvv71UAu+/vRQTTO+/vUVeBD4077+977+9Ou+/ve+/vX4TQe+/ve+/vW5377+9eXFsFgDIlO+/vTXvv73vv71/NiAH77+977+977+977+9Ue+/vVsB77+9JRTvv70Y77+9P++/ve+/vX7vv71b77+977+9Ou+/ve+/vTnvv73vv73vv71jMyrvv71wN++/vSDvv71/77+9Ce+/vW8SUHTvv71377+977+9KM6m77+977+977+9LhTvv73vv70b77+9YO+/ve+/ve+/vUDZte+/ve+/vcW5ZFR177+977+9GDzvv73vv70Eae+/vTZ2eG4r77+9KQVD77+977+9xZQh77+9c++/ve+/vWXvv70977+9elfvv73vv73vv73dsu+/vQovbO+/ve+/vXMH77+977+9BO+/vS91C++/ve+/vQrvv71DQe+/vWMF6Ly/YO+/ve+/vdGK77+9elsMM++/vVPvv71UKe+/vVXvv70z77+9Qe+/vQg/Jxnvv71Jfu+/ve+/ve+/ve+/ve+/ve+/ve+/vQTvv71xWe+/vXRx77+9cU58Wu+/vV3vv71WAO+/ve+/vd2BZTrvv71OBSXvv73vv71y2qbvv73vv73vv73vv71U77+9AB5b77+9Re+/vQ3vv71ZE1/vv73Cse+/ve+/vUNf77+9F++/ve+/vVxL77+9Su+/vSrvv70w77+924bvv71t77+9Y++/ve+/vT8X77+977+977+9z5rvv70v77+977+9NvC+iZ1cNu+/vREB77+9Lu+/ve+/ve+/vTA277+9A++/ve+/vS/vv73vv70kDCHvv70fJO+/ve+/vVvdm++/ve+/vWLTgRTvv73vv71F77+977+977+935Dvv71n77+977+9De+/ve+/ve+/ve+/ve+/vV8j77+9Au+/vRcmVu+/vVk977+977+977+9Tlzvv702G3Hvv71SAu+/ve+/ve+/vWU377+9bgbvv73vv70LdEvvv73vv73vv73vv71977+9KO+/ve+/vTfvv71K77+977+9DWh677+9QEbvv70HLe+/vR8PeAdi77+9VhUiaVpuKRzvv713PUvvv73vv73vv73vv70hQO+/ve+/vWAE77+927IezJfvv70V77+977+977+9Y++/ve+/ve+/vRA777+9dO+/ve+/vUDvv73vv70+cBZl77+9f++/ve+/vVRQ77+9IO+/vUFe77+9WHptb++/vWnvv73vv71MIO+/ve+/ve+/ve+/vXUW77+977+9FO+/ve+/vSbvv73vv71677+9Ye+/vR83IhAVe++/vTw/77+977+9JFLvv73vv71bZ++/vWsg77+9C2Pvv71K77+977+977+9AUZPW0nvv70cIt+k77+977+977+9MO+/ve+/ve+/vU4XYAHvv70i77+977+977+9K++/vVF+77+9G0UXDWzvv73vv73vv71sXVpW77+977+977+9DH/vv71f77+977+9N0Lvv73LoO+/vcmBLhPvv70yUO+/vVbvv71U77+977+9cV/vv712STjvv73vv73vv70nGu+/vXQxA2dWYDsi24TXjhNBOArvv73vv73vv718ZmVR77+9bwJLLe+/ve+/vQJvMmVwVe+/vcyfJciB77+91Lzvv713U++/ve+/ve+/vVks77+9FOCqthlp77+9bOuBkXlFZwfvv70iD++/ve+/ve+/vSdtUCLvv73vv73vv71NSO+/ve+/ve+/vW7vv71/77+9HWXvv73vv71CPyXvv70VAzLNj++/vdGkDhnvv73csE1UVB3vv70UaO+/vQIlw7At77+977+977+977+9flLvv73vv70V77+9Uu+/ve+/ve+/ve+/vS9Z77+977+9SSbvv73vv73vv73vv73vv73vv73vv73vv73vv71BP3Xvv70VFe+/vTg1Gu+/ve+/ve+/vSLvv73vv70wYFLvv73vv73vv73OkhhnJSB1ce+/vWQd77+977+977+977+9Yu+/vTtOTO+/ve+/vRRKcG3vv700fBbetjoeJ++/vUTvv70Q77+977+9Fu+/ve+/vSjvv73vv704du+/ve+/ve+/vRlx77+9yo7vv71yIQYn77+9bWxL77+977+9Xe+/ve+/vXMqVO+/vXLvv73vv73vv70FF++/vRfvv70+H1Xvv73vv70577+977+977+9Eh7vv73vv70577+9Au+/ve+/vTEU77+9Ogzvv70w77+9au+/vWtCx73vv71t77+9XjXvv71M77+9Zu+/vSYLd++/vTV0A0w/Ge+/vUVB77+9AkDvv71d77+977+9RO+/ve+/vWHvv73vv73vv73vv70dJwfvv708PQDOosKRK1dJ77+977+977+9Ru+/vUkC77+9ayAD77+977+977+977+977+977+9L++/ve+/ve+/vQfvv70977+977+9Wu+/ve+/vWsHBu+/ve+/ve+/vXvvv702Be+/vUzvv73vv71b77+977+977+90bgvMFEb77+96YCx77+95IWOGTtgde+/ve+/ve+/vSbvv71Odz/vv73vv70h77+9P++/vQtcUe+/vWnvv71JMO+/vci777+9Yu+/ve+/vR7vv73vv71377+9Te+/ve+/vWjvv73vv71R77+9Ze+/ve+/ve+/vWjvv73vv70I77+9d00577+9OO+/vUFfNe+/ve+/ve+/vWE777+9du+/vS8977+9Wu+/ve+/vUwyemHvv71DVu+/vcu277+9WGPvv71z77+977+9KO+/ve+/vdCI77+9yafvv71/77+9f++/vXsr77+9WWnvv71KCe+/ve+/vXcCTO+/ve+/vW3vv71vSu+/vSUE77+9HhxByZUD77+9b8OvKu+/ve+/vTDvv73FqA/vv70177+9a++/ve+/ve+/vX0C77+9Uh1CL++/vSpwd9ek77+9fu+/vXXvv70IHu+/ve+/vQFlQhzvv712T8SL77+9yp0OGO+/ve+/ve+/ve+/vS8tXVsH77+9YDHvv71t77+977+977+977+977+977+9BS3vv73vv71oA++/vUUdLm7vv73vv71NLe+/vTcW77+9x4nvv73vv71+77+9Jjfvv71Y77+9Re+/vTJT77+9AgwLcBYF77+9Ve+/vRJnIHtYWA/vv73vv73vv73vv70B37zvv73vv70iJO+/vXnvv71nJGHvv73vv70AZ1fvv73vv70VEBZI77+977+9CH3vv71HbG1+de+/ve+/ve+/vV8477+977+977+9C++/vVhN77+977+977+977+9Aj3vv71O77+977+9EFzvv70G77+9SO+/vRvvv70qD++/vUvvv73vv70VKCg4Jgjvv71BMe+/ve+/ve+/vSDvv71x77+9NO+/vX5KAu+/ve+/ve+/ve+/vSM577+9YO+/vQnvv71j77+9V3rvv709367vv70377+9fBp277+977+9UO+/vdaVN++/vWXvv73ivrJVMO+/vRLvv73vv73vv73vv73vv71xOx0gOO+/veKLie+/ve+/ve+/vWxdBD3vv73vv73vv717476Qdu+/vQbvv70L77+9W3Dvv73vv70BUe+/vcO177+907fvv73vv71iCe+/vUPvv70r77+9T1/vv73vv73vv73vv70677+977+9Xu+/vW4t77+9PGtQUe+/vUxH77+977+9Su+/vTJg77+9dFjvv71ZAO+/vXrvv70O77+977+977+9W+GEoT0T77+9au+/vTDvv70AVTQFbu+/vUwWb++/ve+/vUx377+9GWZC77+9f1kX77+9F++/ve+/ve+/ve+/ve+/vTXvv70Y77+9XAcK77+9Du+/vTUo77+977+977+9Zu+/ve+/ve+/vV7vv70877+9Be+/vRkBVO+/ve+/vUbvv709WT5Q77+9Me+/vR5JFe+/ve+/vXbvv73vv73vv705ae+/ve+/vTrvv71tNe+/vU8vaO+/vWMV77+9YWIl77+977+9X++/vUd2Qu+/vQPvv70C77+9b++/vUHvv73vv73vv71p77+977+977+977+9WG8d77+977+9Vu+/ve+/ve+/vXjvv718BU4taNmq77+9KO+/ve+/ve+/ve+/ve+/ve+/vUrvv71DLinvv73vv73vv70L2LoecCXKoO+/vV5n77+9Oics77+9HGAQXu+/vUPvv71TA++/ve+/vRjvv706MO+/vUfvv73vv73vv71R77+9FO+/vT5j77+9bu+/vXbvv73vv73vv73vv73vv71HA++/vSxr77+9BQYI77+9ae+/ve+/vQbvv70zTh3vv70D77+977+91aTvv71sHu+/vRvvv73vv70q77+9NSLvv70nBWHvv70z77+977+977+977+977+9OHhezZ/vv73vv716WO+/vWvvv73vv73YjH4tdXHvv70sRO+/ve+/vVfvv70SAFcWbu+/vTVFG++/vRLvv71P77+9Lu+/ve+/vTXvv70nH2nvv73vv70c77+9He+/ve+/vQ/aiWvvv73vv73vnoA3A++/vWAJ77+9dUUbcO+/vWvvv71WHSfvv71y77+977+9bUbvv73vv73VtFd877+977+977+977+9V1t7Xe+/vTk577+977+9Ze+/ve+/vVId77+977+9XO+/ve+/vXsdaO+/vcmZ7rCU77+977+9U0F477+9MU1S77+9JO+/vQRW77+9aO+/ve+/vVHvv71OPwVHKe+/ve+/vTXvv71577+977+9fCTvv71d3qnvv73vv71HHO+/vX5AK3te77+9Iu+/vV7vv73vv70777+977+977+9WzZw77+977+977+9N++/vWgQ77+977+977+9wqzvv73niJbvv71N77+977+977+977+9QHoK77+977+977+9KWjvv70KEkTvv71Q77+9XFzvv73vv70c77+9AO+/ve+/vVE7fu+/vT5j77+977+977+9H1tYQu+/ve+/vWnvv70r77+977+9Le+/ve+/vRFCAe+/ve+/vXNyOi1677+977+9CTfvv73vv70P24Y777+9NCTvv73vv71d77+977+977+977+977+9XO+/vd2b77+977+9K8a8TjQU77+9Q1IFd3IgIzrvv71QUHTvv70P77+9XmlH77+9PT3vv719PDVM77+9KgTvv73vv73vv71AVu+/ve+/ve+/ve+/ve+/vWUQNx7vv73vv71+77+9f++/vUrvv71AF13vv70JACoyTu+/ve+/ve+/vTFW77+977+977+977+9UH7vv70bdS9377+9eXoA77+977+977+977+916Dvv73vv70i77+977+977+9D++/ve+/vTLvv71YU++/vSDvv71W77+977+9Bwrvv710adGQ77+977+9YTUw77+977+977+977+977+977+977+977+977+9FzxV77+9KDR214sjKu+/vSld77+977+977+9be+/vUVxae+/vcyd77+9CHJOX3PKtu+/ve+/vRUx77+977+977+9ae+/vVPvv70rWe+/vWfvv70977+91obvv71K77+9bO+/ve+/ve+/vUnYhVfvv73vv70bGDDvv70Pdxbvv71gTO+/ve+/vW7vv70I1pha77+9XQ8UYO+/ve+/vQjvv713QO+/ve+/vUYV77+9GQlwBQpkP++/ve+/ve+/vVxjJu+/ve+/vWkq77+977+977+977+9Ku+/vVA4Zu+/vQPvv70w77+9Cu+/ve+/ve+/vWPvv73vv73vv71kRO+/ve+/vT4177+977+9YTvvv73vv71YYe+/ve+/vUTvv70jXe+/vdaJI0Y005rvv70NM++/ve+/vS5E77+977+9FEXvv73vv71ORO+/vUU+FO+/vTfvv707KO+/vcS377+977+9L9+YNOGhre+/vRlCcUfvv71aHO+/vRfvv71O77+9DO+/vWnvv73vv73vv71Ozq1277+9djTvv70PyqPvv711R0pY77+9cUXvv73vv710ee+/vS/vv71d77+977+9fBXYre+/ve+/ve+/ve+/vUxIR++/vXnvv71SXHzvv71vTO+/ve+/vW7vv71A77+977+9CBTvv73vv71D77+9fQ4k77+9B++/vUvvv71JQO+/vVM2bDnvv700Ne+/vXfvv73vv73vv73vv71UBT80Lhvvv71WXu+/ve+/ve+/ve+/vSvvv73vv73vv71bTAdRN++/ve+/ve+/ve+/vWoL77+9Se+/ve+/vSzvv73vv70177+9MCMJ77+9cSHvv73vv73vv73vv71Z77+977+9OzJyZe+/vXnvv70VKO+/vUtUIe+/vW7vv70577+9XlMhfW07LO+/ve+/vR/vv73XtU8bM++/vRMB77+977+977+9de+/ve+/vXUgPe+/ve+/ve+/ve+/vXnvv70vKu+/vRPvv73vv710DSbvv73vv73vv73vv73vv73vv70gOe+/vT1ta1FJ77+9HG3vv73vv73vv71h77+9bjREHVN577+9WGVjwoTvv70777+977+9SxoZEFvvv71G77+977+9NRHvv73vv73vv71L77+9Fm4q77+977+977+9O++/ve+/vUnvv70nVQZX77+977+977+9X2vvv73vv73vv71RYDsi77+977+977+9Le+/vTdiB++/vSDvv70177+9D++/ve+/vTgv77+90Jd677+9BhVsX++/vXZ877+9Bk3vv71ue++/vVTvv70zVu+/ve+/vRdZPTlz77+9De+/vV7vv73vv73Rqe+/ve+/vQI6JUzvv71s77+977+9Su+/vVolyox5CEN077+9Bu+/vVXvv71hEe+/vR0377+9UO+/ve+/vQ7vv71UZ1pi77+977+9KnllKQxz77+9We+/vVHvv73vv71ece+/vTs9Iu+/vQHvv714bE7vv73vv73vv73vv73vv73vv71677+977+92pHvv73vv70777+9Eu+/vRko77+977+9b++/vSQtLmjvv73vv70rCzfvv71ke++/ve+/vRPvv70TTRTvv70wKsqEOH5K77+9AF7vv71V77+977+977+9CXnvv71I77+977+977+9Ye+/ve+/ve+/ve+/vRs477+91p7vv73vv70e77+9bEka77+9SG8d77+9LtyY77+977+977+977+9Xe+/ve+/vVLvv71oAyIAQO+/vRnvv73vv73vv73vv71I77+9N++/vUzvv71qesm1Oe+/vWYV77+977+977+9e1jvv70SZmdEV2nvv70ZZiTvv705f++/vTLvv70CGBXvv70+Te+/vQJWAEdnBe+/vR7vv73vv71I77+9YT8i77+977+977+977+9UHFHD1xn77+977+977+9MO+/ve+/ve+/vU1I77+9dw5odO+/vS8w77+9We+/vVQ6zqog77+977+9Dm0377+9EO+/vWzvv70x77+9PCvvv73vv71/Bu+/ve+/vRXvv71tRV/vv71577+9Ru+/ve+/ve+/vQNA77+977+977+9H++/vVhc77+9VBnvv73vv701Ce+/vcyFZxcncu+/vSnvv70vC++/ve+/vXU277+977+9NO+/vTjvv73vv707J++/vTEj77+9empBSzrvv706Fnnvv71b77+977+9fe+/ve+/ve+/vV1xOU8J77+977+9y43vv71KD++/vc2mFVbvv70/77+91qbvv73vv70u77+977+977+977+9Fe+/ve+/vTHvv73vv71t77+9H++/ve+/vQXvv71+77+9Njh4cjxdDe+/vTYDXRI077+9AwNdM0rvv70NJlrvv71RGSrvv73vv71EGO+/vSTvv704Lu+/ve+/vQfvv71R77+9eFfvv73VlOS+iu+/vXMD2YBpTu+/ve+/ve+/ve+/vR4U77+977+9A++/vVQQ77+977+9EeGluiHvv71w77+9Te+/ve+/vXFc77+977+9Xe+/vQxu77+9Mu+/ve+/ve+/vSHvv71uXFLvv71577+9Ke+/ve+/vTMYRu+/vSpfRwpwXO+/vUJ8Ey3Yje+/vVHvv71oAe+/vW5Y77+977+9BTAx77+977+9b++/vWZkFe+/vVzvv73vv70077+9NO+/vUoJbuuanG5+ce+/vRLcizUr77+977+9cVHvv73vv70s77+977+9HRPvv73vv73vv70IfGhE77+9B++/vXFzTiUr77+9JAdZFF/vv73vv70WMDxF77+977+977+977+977+977+9G++/ve+/vXLvv73vv70E77+9Tu+/ve+/ve+/ve+/vRnvv71g77+9We+/ve+/ve+/ve+/vWRZCUA30p8EPjIM77+977+977+9Vu+/vSjvv73vv70XNQ/vv70A77+977+977+977+9ORTvv73vv73vv70N77+977+977+977+9Fu+/ve+/vU/vv73vv73vv70sGzjvv715Qe+/vQYS77+977+9dDnvv73Tnu+/vTHvv73vv71O77+9BfGHsrVZe++/vVPvv70abEps77+9WO+/vV5Z77+977+977+977+977+977+9O2Xvv73ZjhYFBO+/ve+/vWtxfu+/vRzvv73vv73vv73vv71C77+9MWnvv70z77+96qyA77+9Xe+/vWswQxcY77+9cTYlTHjvv71b1a/vv71J77+9HW4t77+9KO+/ve+/vT0s77+9H++/ve+/ve+/ve+/vUJ077+9X++/ve+/vX/vv71pCjPvv71R77+977+9He+/vTQU77+9eHQ+UADTghjvv73vv73vv73vv73vv70yb++/ve+/ve+/ve+/vWpaPe+/vRE4P++/ve+/ve+/ve+/vUc977+9J0Dvv706aO+/vR9Z77+977+977+977+977+9Ue+/vWZSEu+/vQUpQO+/vWYV77+977+9yazvv71IVCFQEyRn77+9T++/vV/vv73vv70C77+9UCrvv73vv70yL++/vV/vv70R77+977+9YUhT77+9fmrii6rvv71677+9Bu+/vXZR77+9XzUsGu+/ve+/ve+/ve+/vXjvv73vv73vv73vv73Ujwx/77+9Be+/vRXvv71T77+9Qe+/vVvvv70aFTth77+9247vv71WUe+/vWvapmNtwrLdjikQFe+/ve+/ve+/vRrVmmjvv71me1zvv73vv70x77+9UVpG77+9O3Vf77+977+9Tynvv73vv71L77+977+977+9b++/vSwe77+977+9FO+/ve+/vQ/vv71f77+977+9Wu+/ve+/vRLvv73vv73vv70177+977+9VWhqRUgeLu+/ve+/vWtE77+9au+/vU3vv73vv71q77+977+9IO+/vXPvv71J77+977+9Cu+/ve+/vXQRfwPvv73vv73vv71B77+977+9B8a877+9Vu+/ve+/vSHvv71C77+977+9Mh3vv71L77+977+977+9y4zvv71qHe+/vQIZQu+/vQzvv70A77+9MDQKEd+oQe+/ve+/vXbvv73vv73vv71W77+9DO+/vR8ZEd+oGVoTTxDvv71+77+977+977+9aFUdAO+/ve+/vTl1TAE4FhTvv71W77+977+9Ok0277+977+9FW0z77+9Lu+/ve+/ve+/vXbvv71a77+9FShFEe+/ve+/vVHvv73yu4K8Eyjvv70l77+977+90LHvv73vv73vv70C77+977+977+9Ee+/ve+/vXbvv70/MzAK77+977+9Qu+/vXfvv70k77+9MVoyZe+/ve+/vTXvv71077+9cc+E77+977+9T++/ve+/vQwC77+9fCTvv71CB23vv70ETe+/vQRGS2Hvv70j77+977+9Dzoo77+977+9ehHvv73vv71B77+977+9Qe+/vcikQDc977+9Xijvv73vv70ZWEjvv73vv73vv73vv73emE13Su+/ve+/ve+/vVlBOO+/ve+/ve+/vW3vv73vv73vv73vv73WoO+/ve+/vUI0X1Xvv73vv73vv73vv73vv73vv70LVCYIfmpA77+9de+/ve+/vUfvv71aD++/ve+/ve+/vSzvv70dAO+/vUZt77+9eO+/ve+/vTFXFG7vv70cfe+/vS3vv70fLO+/ve+/ve+/vUdZ77+9Ue+/ve+/ve+/vTrvv73vv73KthkI77+977+977+9HO+/ve+/ve+/ve+/vXcd07Y677+9Me+/vXwf77+9D1kH77+9DD5c77+977+9PO+/ve+/vR8sxorvv71177+977+977+9Au+/ve+/ve+/vTFX77+9UQXvv71OSyDvv71mXe+/vXgn77+9Lhp577+9Y15u77+9c++/ve+/vWzvv73vv71jR++/vRsWUVLvv73vv71ACu+/ve+/vQxTW1p/eEUYdGB44rCa0qhCUy4h77+977+9D++/ve+/ve+/vXVy77+977+9eu+/vTlA77+9Zu+/vS5x77+926Lck++/vWw1Eg3vv71TAO+/vTcs77+977+977+9Oe+/vT/vv73vv73vv73vv71DFSvvv70eQFJlcO+/ve+/vcWj77+9b++/ve+/ve+/vQsx77+9W++/vRrvv71077+977+9Ce+/ve+/ve+/vVtwTe+/vXTvv73vv73vv70f77+9XGVCHO+/ve+/ve+/vSor77+9TwFc77+9Ohvvv73vv70h77+9N1Ub77+9a++/vd+CAFDvv71MdO+/ve+/ve+/vXFQ77+977+9KO+/ve+/vWTvv70977+9Ce+/vVFQ77+977+9cAMW77+9HGDvv714fu+/veOghu+/vQ7vv71v77+9W82E77+977+977+9Z++/vX7vv73vv73vv71nY++/ve+/ve+/vWfvv70777+977+9YWU+IAfvv73vv7073Kh677+9GxPvv73Wju+/ve+/vdyw77+9F++/vdmyRxXvv71E77+977+977+9ZmFsIAdgPzLvv73vv71o77+9HgA+DBXvv70J77+9Nk7vv70tJe+/ve+/vXJ/77+977+977+9JCAH77+9NlHvv71TG++/ve+/vTF377+9Tls777+9Pe+/vSLvv70BcHrvv70ePwnvv73vv71jNW8EyoZo77+977+9S9ai77+9zb9EeG3Cgk/vv70p77+9I0bvv70a77+977+9RtaNae+/vR4VSu+/ve+/ve+/vSI077+977+9MO+/vX5yVgXvv71577+9Ou+/ve+/ve+/ve+/ve+/vVZnf++/ve+/ve+/vVHvv73vv71XLH9U77+9Z96/77+977+9Te+/vWvHiVnvv73vv73vv71UAhEoTHvvv71Reu+/vQfvv71e77+977+977+977+977+977+9DcOi77+977+9bO+/vR5A77+977+977+9MXfvv73vv73vv73vv73vv73vv73vv73vv70bAO+/vVUW77+9eWt+UhAoIHjvv73vv70O77+9zIV7F++/vX0277+9eGQB77+977+9Mgd3d03vv73Jsw5P77+91qbvv71j77+977+977+9RO+/vVI6Ju+/ve+/vRF3Xe+/vX7vv73vv71877+9Hu+/ve+/vXnvv73vv70M77+9BiDvv73vv70V77+9BO+/ve+/ve+/ve+/ve+/ve+/vSDZtm3vv73vv70Nc3Tvv71H77+9Du+/ve+/ve+/ve+/ve+/vUXvv70DAO+/ve+/vfCfk4kmIWc777+9aVEM77+9f++/vUbvv73vv709ae+/ve+/ve+/ve+/vdCAWH8nT++/vW09YO+/vQM777+977+977+9MO+/vXPvv73vv71NTEJT77+9Xy7vv73vv73vv73vv70mwqca77+977+9CxXvv70p77+977+9S0tX77+977+96qu+77+9HVbvv73vv7191I5jGhduM++/ve+/ve+/vSTvv73vv73vv73vv71177+9Xu+/vSrvv73vv71h77+977+937xDSUrvv70377+977+9PQzvv73Ipu+/vQkf77+9Uz4/77+977+9Ou+/ve+/vQ1577+9zLwDG03vv73vv70+77+9PNqV77+9bHtf77+9Du+/vXfvv71qEe+/ve+/ve+/ve+/vR4/77+9RTPvv73vv70kAzTvv70pF2rvv73vv71f77+9U++/ve+/vSzvv73vv71j77+9KlAM77+9Mk3vv73vv73vv70bekEECu+/vTBFdwsC77+977+9MyHvv70h77+977+9Mu+/vU3vv73vv70abzo6EO+/vUHvv73vv70bPO+/ve+/vXM3Ku+/ve+/vQPvv71UHu+/vTcJF++/vRnvv73vv71vFj7Rm++/vRbvv71zIu+/vS9eP++/vXPvv70yeHkHU0Lvv71X77+977+977+9PWnvv73vv70bTe+/vSrvv70777+9J++/ve+/ve+/ve+/ve+/ve+/ve+/ve+/vRd9VTkt77+9G++/vTfvv706BhPvv71xd23vv71K77+977+977+977+9fCfvv73vv73vv71o77+9HErvv73vv71wFO+/vS/vv70UW++/vUFs77+977+9f++/vRgSKSvvv73Gi++/vWs977+9Vu+/ve+/ve+/vUXvv73vv70X77+9bFpoKO+/ve+/ve+/vVNyaO+/ve+/ve+/ve+/vSnvv70Z77+9bTzvv73vv70O77+9Hu+/ve+/vXwKfSrvv73vv70HZ2oYA1Lvv70w77+9bWnvv73EtDAG77+9Xu+/vXzvv73vv73vv73vv73vv706F++/vW08WO+/vQrvv70BIw/vv73vv73Piu+/ve+/vRpVEwB8Hnbvv71bFe+/vSsKN8OSY0jvv73vv73vv73vv71tLu+/ve+/vV7vv73vv73vv70KHN+4YO+/ve+/vXV277+9NCYNOO+/vWlP77+977+977+9Ke+/ve+/vd2SBu+/vQXvv70277+9WFkR77+977+977+977+9fe+/vS1JI++/ve+/vVkm77+9Xu+/vRhmJO+/ve+/ve+/vTdp77+9cE4x77+9H++/ve+/ve+/vU7vv70C07Hvv70Da++/ve+/vVIv77+977+977+9ybJme++/vWIDKGA/eu+/vTTvv73vv70t46CG77+9bO+/vQ4L77+977+9eu+/vXgQ77+9ZEbvv71N77+977+977+9Vmbvv70DBQh/77+9Q++/vSsWdO+/vWnvv73vv71T77+977+9aO+/ve+/vRjvv71/77+977+9fu+/vQbvv73vv73vv73vv73vv71jIGDvv704fmBT77+9b9Sg77+977+9Eu+/vT/vv71iRO+/vQbvv71ceu+/vTd9LS9477+9Ju+/ve+/vUx9AEdZ77+9c37vv73vv71ORu+/vdiQNO+/ve+/ve+/vXjvv71f77+9y4Dvv711fznvv71vXO+/vQ0777+9Bu+/ve+/ve+/vSrvv73vv73vv73vv73vv73HlO+/vXg+77+977+977+9Mk8d77+9UkIsfO+/vVIj77+977+9Su+/vdW+77+9L++/ve+/vQQs77+9R++/ve+/ve+/vUDvv73vv73vv70wzY/vv73vv73vv71aeO+/vWHvv73vv73vv70I77+977+9E++/ve+/ve+/vU3vv73vv70177+977+9Ue+/ve+/vVBk77+9CO+/vU3vv73vv70j77+977+9Ue+/ve+/ve+/vWYXel/vv70O77+9wpHvv70Meu+/ve+/vUPvv73Ml9ia77+977+977+9blxy77+977+9de+/vQlfJC/nipQSAO+/ve+/ve+/ve+/vTTvv71f77+977+9JRQ3FO+/vc+ZS1wZ77+977+977+9M++/ve+/ve+/ve+/vXMyLmvvv73vv73vv71O77+9Bx9UeFzvv702C++/vXVORkJld++/ve+/vduYNGF1fO+/ve+/vWAIxY3vv73vv73vv71m77+977+9NmpPZ3bvv71i77+977+9dyk/77+977+9Ojjvv73vv70m77+9YO+/ve+/ve+/ve+/vVnvv73vv73vv73vv71oVe+/ve+/vdybLgXvv719zobvv70SPUpv77+9Ye+/ve+/vU1hVEVp77+9UWjIujcW77+9GBXvv73vv70s77+9B++/vW3vv73vv71eD++/vWQC77+9AO+/vXFsBu+/ve+/ve+/vWDvv71677+90p1277+977+9E++/ve+/ve+/vd2qf++/ve+/vXZqTe+/vd+QC0dv77+9C0vvv73vv73vv73vv73vv71Wxa0477+9ce+/vU4/77+977+977+977+977+977+9T++/vQXvv71E77+977+9N++/ve+/vVpx77+9HBjvv70177+977+9AQNAU++/ve+/vX/vv73LliwSCu+/vVLvv71e77+9Te+/ve+/vRXvv73vv71OJ3xv77+9UXpDb++/ve+/vSrvv73vv73vv71vU3vvv73vv71NRwY6be+/ve+/vRBk8bOHv9ON77+9Nu+/ve+/vVLvv70v77+9TSoj77+9Ze+/ve+/vTpv77+9ECjvv70xBO+/vVh077+977+977+977+9BlIVVh/vv71ZBScQFRcO77+977+9VA7vv73vv70CE1R7ZA3vv71p77+977+9R++/vXXvv71R77+9O1IUEDzvv73vv73vv71Ne++/vUPvv73vv73vv73vv73vv70/77+977+9Ie+/ve+/vX7vv73YuQU8Ue+/vT5Of0Xvv73vv73vv73vv71s77+90LxpeEjvv71S77+9TUwCJO+/ve+/ve+/ve+/vRzvv70dAO+/ve+/vTIs77+9R9Ouce+/vWsWNO+/vVnvv70qLhsmOBfvv71a77+9Ze+/vRJgP++/vTl777+9aRbvv73vv70I77+9BzI605QoE1ZC77+977+9EU3vv73vv70CZC/vv73vv70777+9I++/ve+/vUUx77+977+977+9Zu+/ve+/vUJWee+/vWhiXhjvv71I77+9VG3vv73vv73vv73FiO+/vVlCee+/ve+/vT3dnDBYPgs7Ee+/vQnvv73NnO+/vQR/77+977+9GU3vv71+77+977+977+9fCTvv71yOO+/vWke77+90rvvv70U77+977+9VA5fDu+/ve+/vSwV77+9OO+/vd6e77+9ce+/ve+/vXvvv73vv73vv71p77+9eO+/ve+/ve+/vWA2aWQ/R++/vRJm77+9fyc7PWwa1KXvv71EGe+/ve+/vWNC77+9KXcWQwowU2jvv73vv73vv73vv70ZZO+/ve+/vTvvv73vv71O77+9Re+/ve+/ve+/vXzvv73vv70IYFQH77+977+977+9Bdq6JFxnZe+/vTPvv71LRu+/vQU+VO+/vdOD77+9G3rvv73vv73vv71777+9PO+/ve+/ve+/vTjVn++/ve+/ve+/vcOX77+977+977+9YxTvv73vv71sGO+/ve+/vVwR2Z1d2pIGaHfvv718NC1R0LYV77+977+9O1pGeu+/ve+/vTka77+9HmHvv73vv73vv705Tw/vv70dLe+/vT1s77+977+9GgAKfjUQUe+/vQ0i77+91JTvv73vv71eXhNa77+93Zjvv73vv73vv73vv701LO+/vVVgYe+/ve+/ve+/ve+/vWHUoe+/vVPvv73vv70sPwhHB0zvv70M77+9248K77+9MCPvv73vv70rDH/vv71P77+9MDfvv71J77+977+9Sm9WHe+/vRDvv71CSzdoK++/vSogNe+/ve+/vT7vv71y77+9Y2zvv71DKu+/vTJ356+j77+9XW7vv70eYO+/ve+/vSoC77+977+9JSzvv70477+977+977+9TV3vv70y77+9GO+/vXx3cCLvv73JoA8bSe+/ve+/vS1o77+9K++/ve+/vVTvv73vv71E77+977+977+9Le+/ve+/ve+/ve+/ve+/vUTvv73vv73vv71M77+977+9We+/ve+/vWHspbHvv70vDe+/vUEo77+977+9QO+/vRTvv70Ae2bvv71pa3Xvv73vv71O77+977+9CO+/vQtjICwQXu+/vV3vv70cW++/vUV0Ve+/vW4V77+977+9IUUO77+9d++/veW3uVPvv73Vru+/vQ7vv71iY++/vTvvv70z77+9J+WnhVlQ77+9Ue+/vTzvv73vv73vv71F77+9ZGHvv71B77+9HQ/vv70Dd0Hvv70nVHbvv70s77+977+9Be+/vXnvv71FZ1oH5oWJ77+9fO+/ve+/ve+/vR/vv70A77+9MwPvv71e77+9Ae+/vVBg77+9I++/vVTvv70n77+9NQTvv71r77+9WdqPUu+/ve+/vWbvv73vv71X77+9WC9MBgbvv71W77+9QmQl77+9UQkm77+9KO+/ve+/vSfvv70K77+977+9IO+/ve+/ve+/ve+/vSPvv73vv70+77+9TAcq77+977+977+977+9XxJo77+9Ge+/vSrvv73vv70U77+9ce+/vXTvv73vv73vv70877+9Z++/vVrvv73vv73vv73vv70B77+977+977+9za3vv73vv709du+/vXFJ77+9Pti2A3/vv71PLUtGKDEuEe+/vTjYkl9sLXMS77+977+977+9Ae+/vTJnbGnvv70o77+977+9RO+/vRfvv71977+9NydkTwoPfO+/ve+/ve+/ve+/ve+/vWA/PO+/vXBm77+977+9QGfvv73vv73vv70AAO+/vc+M77+9amZu77+9Ou+/vU3ChCFAJwzvv70y77+977+977+977+977+9ABnvv73diO+/vVDvv712JQVWBu+/vR0Z77+977+9dD0077+9GnLvv70cTGvvv73vv71XLe+/ve+/ve+/vUJs77+977+9dEVx77+9Vu+/vW4R77+977+9ciBV77+977+977+9FO+/vTPvv73vv70jfu+/ve+/vS5o77+977+9Z++/vTkm77+977+977+977+977+977+977+977+935fvv73vv73dn1nQnO+/vXE2JUzvv71M77+9Uu+/vSI8be+/ve+/vTvvv71S77+9XO+/vUXvv73vv708GO+/vVwH77+9e++/vSHvv73vv73vv71F77+9cm8VEO+/vULvv71l77+9bEnvv73vv70IT++/ve+/ve+/vW5iEnXvv713YO+/ve+/vR1w77+977+9B++/ve+/vRlPMCY2au+/ve+/vUtEHu+/vXQG77+9AzMr77+9R++/vSXvv73vv73vv71u77+9cBQVd++/vVF+Y+qnrCHvv70iDH/vv71P77+977+9au+/vSXvv73vv71o77+9be+/ve+/vWHvv73vv71A77+977+977+977+977+9HUk2TBsw77+977+977+9Je+/ve+/ve+/vWPvv71m77+977+9Qe+/ve+/vUHvv70u77+9FhosB1t2Hu+/ve+/vSDLrmfvv73vv73vv73vv70RS++/vSfvv71CeVfvv71l77+9Re+/ve+/vWRb77+9CFnvv73vv70F77+9fu+/ve+/ve+/vVot77+977+9ZO+/ve+/ve+/vWot77+9Li9KBe+/vUhfMU0677+9FwBrTBXvv70477+9D2Hvv70LHTkp2IJfGtyD77+977+977+9Oe+/vXF0Ru+/vX/vv71GM++/vQPvv71EGTRdXgTvv73vv702RO+/ve+/ve+/ve+/ve+/vWHvv71kFFkDFzQ1Iu+/vTUi77+9L/GBsKnvv73vv73vv70oZO+/ve+/vX4ZSR8077+9Tu+/ve+/vQIrPwzvv70gQ++/vcq777+9YmZ277+9Ke+/vQsNPu+/vVlp77+9A3s/77+977+9a8eWX++/ve+/ve+/vQDQre+/vQjvv700Mxnvv73vv73vv73vv71Q77+9TO+/vRfvv73vv71f77+9dzZW77+9EAM1zqDvv73vv73vv73vv70s77+9wovvv70r77+977+977+9S++/vTLvv73vv71q77+9EO+/ve+/vRRm77+9QVEuZnoW77+977+977+977+977+9NlTvv73vv73Pi++/vVhNEnjvv71mRuutru+/ve+/vUHvv73vv71nUO+/ve+/ve+/vQwfBEg0Cu+/ve+/vSfvv73vv71PNgkIfu+/ve+/vUvvv73vv73vv73vv70nA0BE77+9IjZqbBBGVu+/vSHvv73vv71aK+O5j++/ve+/ve+/ve+/vQnvv73vv73vv71y77+977+9Y++/vc2777+977+9EO+/ve+/ve+/ve+/ve+/vRTvv73vv71RYjvvv73vv70z77+977+977+977+9x6zvv71aDu+/vV7vv70A77+9Q++/ve+/vcWG77+977+977+9eHExWm90ZtGU77+96oWs77+9ce+/ve+/ve+/vQgv77+977+977+9DzNY77+9Au+/ve+/vREwOu+/ve+/ve+/ve+/ve+/ve+/ve+/vSLvv73vv70ud++/ve+/ve+/ve+/vQ8q77+977+977+977+9A3IMACQ2au+/vQDvv70HAnFQ77+9OO+/vRrvv71DJDwFO++/ve+/vR4EcO+/vRHvv70yYe+/vX3vv73vv71H77+977+977+977+9He+/vVcX77+9AxRZG++/vV1ZT3jvv70BUlFyOQ/vv73vv73vv71d77+977+9RDcOU++/vX7vv73vv71z77+977+9KB/vv70TXu+/vSHvv71g77+9fX1877+9BgMj77+9F3kE77+9JO+/ve+/ve+/ve+/vSg+4amG77+977+9G3Ma77+9CkFi77+977+9bktNK++/vTjvv71VESfvv71mdO+/vT1lw4ZfVWDvv71R77+977+9fEYpJD/vv73vv71aLe+/vS4tRnTvv73vv70aZe+/vQYNYu+/vSbvv71177+9QO+/vUgmRlHvv71n77+977+9Ei1CC++/vTHTre+/ve+/ve+/vQ3vv73vv70k77+9S++/vW3vv70ACmDvv70f77+9cW4GZ3sn77+977+9ae+/vSBN77+9Uzvvv71I77+9w5FLY++/ve+/vXpn77+9TBsr77+977+977+977+9KO+/vQbvv73vv70E77+977+9CGvvv70O77+977+9NzAyFhg8Ox1oFho9MO+/ve+/vVbNne+/vVHvv70x77+9Bu+/vV9a77+977+977+9PO+/ve+/ve+/ve+/vR3vv73vv71ZNe+/vWnvv73vv73vv73vv70Y77+977+9MhIXQknvv70bRGXvv73vv73vv7193orvv71Xeijvv71RFe+/vUFs77+9Wu+/vQo53Y7vv70TdRrvv71WBgAY77+977+9OWrvv70177+977+977+9VAnvv73vv70k77+9CBTvv73vv73vv70o77+977+977+977+977+9fFkg77+9CnQ/WjBk77+9yrB+77+9SO+/ve+/ve+/ve+/vWx3cCJm77+9Qu+/ve+/vSNpEFLvv73vv73vv71rCyNBz6Tvv73vv70ZQnFX77+9GkzVhnbvv73vv73vv73vv73vv73vv73vv70ONe+/vVoR77+977+977+977+9Xe+/vXwPKjx6R++/ve2am++/ve+/ve+/vRXRgTVpWhw177+977+9AVJABe+/ve+/ve+/ve+/vXHvv73vv73vv73vv73vv73vv73vv718QO+/ve+/ve+/vTQdJXTvv73vv73vv73vv73vv718eWzvv702Me+/ve+/vTE977+9PUbvv70C77+9D9qHZX1V77+9DO+/ve+/vXEg77+9PG/vv71F77+9MDs5Dkjvv73vv70TH2Dvv70e77+977+9RTTvv70Db1N077+977+977+9Cu+/vQ/vv70sUe+/vWYcYu+/ve+/ve+/vX00Qizvv73vv73vv71Edu+/ve+/ve+/vV7vv70p77+977+977+977+9eO+/vVvvv73dknQH77+977+9e++/vThyCS9LKBYbekEAHG1r77+9A++/vStx77+9azPvv71M77+977+9XRXvv73iqIbvv73vv73vv73vv70c77+9ee+/vQkA77+9VO+/vRx434jvv71n77+977+977+9Fhdi77+924fvv70k77+9Jhvvv71UTWHvv73vv73vv70i1Znvv73vv71k77+977+977+9Bu+/vT/vv71BFe+/ve+/vdud77+9Vu+/ve+/vWcG77+9JyZhPyoM77+977+9dDFMbO+/vRYddznvv71977+977+9CO+/vVUbGO+/vToBAO+/ve+/ve+/vXnvv73vv73vv73vv73vv73vv71DAwLvv73vv71/77+9fO+/vQ9CRe+/vWPvv70EdEs677+9WAllQhwXOe+/ve+/vVXvv73jm6gd77+9Ye+/ve+/vWw177+977+9Mz7vv70877+977+9Eh1R77+9yLjvv71m77+9XxZwU++/ve+/vTQv77+9FxEn77+9LF09bO+/vXDvv73vv73vv73kgY5Rdu+/ve+/vSAA6JSyU1sKJXjvv71b77+977+977+977+977+977+9Gkzvv70H77+977+9ee+/ve+/ve+/vRlXFQ3vv73esw1+77+90bQHZu+/ve+/ve+/ve+/vXg977+9NFHvv71L77+9cV/vv714bEgaDe+/vWoj77+9aGLvv70pRu+/ve+/vX4u77+977+9AXzvv70MRu+/ve+/ve+/ve+/vXDvv71o77+9f0BoVVrvv70TSu+/vQJv77+977+9Ge+/vQwp77+977+9CyJQMDoV77+977+9ZO+/ve+/ve+/vQTvv73vv73vv73vv73vv73vv73vv73Uou+/vdaJ77+977+977+9Ou+/vTQlcu+/vSbvv71nbO+/ve+/ve+/vTDvv70w77+9fO+/ve+/ve+/ve+/vUvvv70Qde+/ve+/vXMKfm3vv73vv73vv73vv71L77+9Y2Tvv70UDhc777+9QMOo77+977+9ewLvv702eu+/vVrvv713cEvvv71077+9dHvvv71sEe+/ve+/ve+/ve+/vQfStu+/ve+/vVE777+9HkQ+77+9de+/vQDvv701Lu+/ve+/ve+/ve+/vee+vTbvv73vv71SOgYB77+977+9zqlYGi3vv73vv71r77+915pv77+977+977+977+9dQkLDhsU75mj77+977+9Eu+/vWY8Ue+/vRwT77+9YXBE77+9DF0A77+9VC7vv703EQc277+977+9bu+/vTnTnEPvv73vv73vv70w77+9Hu+/vca4ZztQ77+977+9Tu+/ve+/ve+/vQvvv73vv703Z++/ve+/vQ7vv71E77+9GgA477+9Du+/vURQ77+9bWbvv70R77+9BDrvv71277+977+977+9Xu+/vQAdf++/ve+/vcKp77+977+977+9Ze+/vSoi77+977+9IQdYcFYF77+9Q++/ve+/vX0t77+9OVvvv73vv71ienXvv70zZmnvv73en1fvv71Ybx9hIQHvv71hFHwQKu+/ve+/vUvvv73vv73vv73vv70o77+9E++/ve+/vRwONu+/ve+/vSEXCAEeKFvvv73vv73vv73jho5p77+9Z++/ve+/ve+/ve+/vQ7vv73vv705de+/ve+/vWheW++/vR5177+9yIjvv73vv71tMe+/vVNDDO+/ve+/vTpNWe+/ve+/vSzvv71U77+977+977+977+9Jlzvv73asu+/vVV3B++/vXvvv73vv70Y77+977+977+9SGkXdVgR77+977+977+9JRBUeO+/ve+/vVnvv71W3ZJ0De+/vQnvv71WSO+/ve+/vScP1Z7vv70777+9Fu+/vQnvv73vv73vv73vv70ebO+/ve+/vTXvv71977+9dRYSZ1Xvv70X77+90JRL77+9T01CPy0B77+9ZGjvv73PtDAG77+977+9GO+/vUt1aO+/ve+/vQUMCu+/vSgRdu+/ve+/vV9G77+977+977+9RFPvv71g77+9Lu+/vRJJUO+/ve+/vVjvv70mezjvv704QXjvv70O77+9IG7vv71kW++/vRsw77+9GUPPkzbIvhTvv70Ne++/ve+/vQR077+9zIE8SD5UTwsM77+977+9TFhFSO+/ve+/ve+/vTg377+917ZObE4a77+9KWHvv71kXQgGRkbvv73vv73vv71r77+9SjxS77+9A++/vQrvv70L77+977+977+9bGcjTu+/ve+/ve+/vcaO77+9A++/vQRNGBvvv70D77+977+977+9yY1T77+9eO+/vTM8752GUe+/ve+/vVbmqrskLe+/vW3vv70+bHFQemPvv70ET++/ve+/ve+/ve+/vUgB77+9LVvvv73vv70YAu+/ve+/ve+/vRdaRu+/vdGp77+9NO+/ve+/vQUGDyZrQygX77+9SO+/vSw6ZB3vv70NFO+/vWnvv73vv70nIRc6RS06ZR1k77+9YD9Tz5BC77+977+9bO+/vWDvv71Q77+977+9Jumwl++/vT/vv73vv70QSmQUX++/ve+/vcyY77+9YFXvv71M77+9Zu+/vRfvv73vv71CQu+/vXnvv71m77+977+977+977+9RCQc77+9b15nE0ZtGe+/vQJV77+977+9eO+/ve+/ve+/vQIw77+977+9YzQU77+9d++/ve+/vRfLsB8Z77+977+977+977+9eX3vv73vv70Pf++/vUPvv705fu+/vU4FVX/sgaYyFe+/vRAcY++/ve+/vU9M77+977+9a++/vSZx77+977+977+9Pe+/ve+/vQHdl0FB77+9x57vv71oEQ3vv73vv71oEwwZRRLdkg7vv73vv73vv71B77+977+977+9Ve+/vTNwSe+/vRbvv71rcuOLiANvB0rvv71jFO+/vQrvv70xTQI977+977+9CTZQ77+977+977+9GQZG77+977+9Je+/vUECI2nCvQzvv73vv73vv73vv71X77+9G3Lvv73vv73OqWPvv71bE++/ve+/ve+/vQXvv73vv73vv70e77+977+977+9XgkA77+9BQZveu+/ve+/ve+/vRYt77+977+9QmcD77+977+977+977+977+977+9OWIz77+9OO+/vWFBXseq77+9RO+/vVTvv70yOO+/vSRUSu+/vUDvv714NO+/vSDvv73vv73vv70u77+9ZQHvv70K77+977+977+977+92r7vv73vv71MCV7vv71W77+9OWnvv73vv701a++/vS0oGe+/ve+/vWI377+977+9e0bvv70O77+9ZO+/vQrvv71p77+977+9We+/vR4277+9WSbvv70gIO+/ve+/vRHvv73vv71177+9Fe+/ve+/vVIX77+9Ie+/ve+/vWnvv701TkE1R++/vVoGMu+/vRElW3jvv71IQAfvv73vv70+77+9CiQH77+9TGzvv70R77+977+9CO+/vSHvv70UZU7vv73vv709YyI477+977+9Tu+/ve+/ve+/vUfvv73vv71O77+977+9RO+/vWjvv73vv70d77+977+977+9Se+/ve+/vVhs77+977+9SUzvv71IPu+/vdqBX1s7UO+/vScxS++/vUfvv73vv73vv71J77+977+9cUtJPe+/vTXvv73vv70u77+9cFPvv700LDJ677+977+977+977+977+977+9de+/ve+/vRZr77+9EMW+Jjfvv70TZu+/vWBP77+9ybck77+9WBMffu+/vTho77+9ZntCZXFP77+9BO+/ve+/ve+/vTrvv73YiO+/ve+/ve+/ve+/vVga77+977+977+9wo3QjiE2cO+/ve+/ve+/vUcD77+977+977+9Vu+/ve+/ve+/ve+/ve+/vdqYGWHvv73vv70MfQAF77+977+977+977+9EHREGe+/ve+/vTxTLnFt77+977+9S++/vUvvv71QXF7vv70J17ZPR0xl77+9Hwbvv70977+9Bl9HHHjvv71TBW/vv71FWO+/vSVR77+977+977+9Ju+/vTFeE++/vXNV77+977+977+9Su+/vQASGGnvv70KBD81IO+/ve+/vQHvv71F77+9bnISQu+/vQTWkjo377+9MxB7WSTvv70EJO+/vXkUHBfvv73vv71aRHTvv70W77+977+9W++/ve+/vcebBU8m77+9Ke+/ve+/vXPvv70QZ++/vUbvv73vv73vv70R77+9GTVJ77+9cu+/vQvvv71XexFZ77+9HdKDDgBdD9mhRBhYD++/ve+/vTUp77+977+9LHrvv73vv73vv70fcRdoFwhq77+977+977+977+9ZO+/vRjvv73di31eSkzvv71lFDxV77+9DAQUaxMWPO+/ve+/vcm2VlRp77+977+977+9Ghxva++/vUHvv70edEta77+977+9Mg9nOO+/vXHvv73vv70JbwVKUe+/vTDvv70hNO+/ve+/vTtYKGPvv70uBBYU77+92ZsBAO+/ve+/ve+/ve+/ve+/ve+/ve+/ve+/vcS0MWnvv73vv73vv70z77+9C++/ve+/vRvvv71yLO+/ve+/vXFf77+977+9Ee+/ve+/vTJlX1MvBO+/ve+/ve+/ve+/ve+/ve+/vQPvv71u77+9Y++/vU0P77+9eO+/vVPvv73erCs1Me+/vVvvv71lOu+/vRJXRgfvv73vv73vv71O77+977+977+977+977+9dE3vv73vv73vv73vv70BJzQuQDTvv71L77+9I2vvv73vv73vv70g77+977+9WnFa77+9fO+/vQpR77+9fiRJHO+/vUVA77+9CO+/ve+/vQcgB++/ve+/ve+/ve+/vde477+9Ye+/ve+/ve+/vR3vv71377+9f++/ve+/vTFM77+9ODgVKCBZ77+9Nzvvv73vv73vv73vv70Z77+977+9Pu+/vRtPFe+/ve+/ve+/vWxwP2sF77+977+977+9ZO+/vRhcENak77+977+9FO+/vQfvv71CcsmUMe+/vXNnTu+/ve+/vVfvv70UGxNm77+9Vu+/vRYzdAHvv73vv73vv70wJmjvv71w77+9SWxO77+9cGnvv71MXO+/vdyCI++/ve+/vWhJ77+9cVfvv71E77+9ae+/vWHvv70277+977+9VXnvv73vv70K77+9XBLvv70Mfjjvv70k77+977+9Bu+/ve+/vVcGFhRzDX7vv73vv70aNGdQNC00eu+/vW4aeSdQ77+977+9Oifvv73vv73ylaakASc377+9Kwo377+9FHvrqJkGE++/vWrvv73vv71iO++/vWA677+90Zjvv70SPO+/vR4377+9Ohvvv73vv73EoHs377+9Ei5ybUHvv70m77+9TlHvv73vv73vv73vv71R77+9MO+/ve+/vWARZg0a77+977+9ExVJ77+977+977+977+9GTPvv71+EAArYjZc77+9Ph1mVkJUZe+/vSMK77+977+9Q++/ve+/ve+/vWPvv70277+9Ej4BF++/ve+/ve+/vW/vv71I77+9DO+/vRNmLD/vv71m77+9YWEFfu+/vVgH77+9WhPvv71877+977+977+9x57Ole+/ve+/vRhWVVrvv70R77+977+977+9HO+/ve+/vWPvv71b77+9M1UI77+977+977+9A1d877+9FB8IKu+/vQ7vv73vv73vv73vv73vv73vv73vv70MDVzvv70nEFVZ77+91Lgn77+9Nu+/ve+/vTfvv71m77+977+9e++/vSzvv73vv70qHE5t77+9A1cUbu+/vRHvv71OeGQBF++/ve+/ve+/vW8cLTjvv73vv70RRzXvv70177+9Fhs977+9Hmjvv70yKBXvv70477+977+9AArgvp4JOO+/vdGM77+9Clrvv73vv701OO+/vXF+77+977+977+9U3MfUnjvv73vv705Ze+/vXPvv73vv73vv73vv73vv71JWBbvv73jtpJ177+9bjwHWe+/ve+/vSvvv70XGu+/ve+/vU3vv70tD10H77+9Pu+/vTzvv70yWe+/vUAt77+977+977+977+977+9UO+/ve+/ve+/vRl8OO+/vdqB77+9Pe+/vQjvv70o16UAft2Qau+/vQ7vv70cBFDvv73vv70a77+9GQkHWnrvv73vv73vv70X77+977+977+977+9cmsnYB/vv70X77+977+9ejFOE++/ve+/ve+/ve+/vcSVL3ZiR++/ve+/vWXvv71s77+9Tgo4Au+/ve+/vUTvv73vv71jdzjvv70SX++/ve+/ve+/ve+/ve+/vVE177+9GO+/vVQzby7vv73vv73vv71z77+9GkzShu+/vTxm77+977+9cu+/ve+/vTUJJ++/vVtx77+977+9He+/vUIc77+9Ohpx77+977+9FUHvv73HhS1zcHZBE++/ve+/vXXvv70Rd92oY++/vQ8y77+9QO+/vSjvv70oVO+/ve+/vcqABe+/vTzvv70PW++/vUZcXe+/vRFV77+9KO+/vSQtLmvvv73vv71l77+9ckTvv71+77+977+977+9P++/vWzvv73vv71977+9TzLvv73vv73vv71QIe+/ve+/vS3vv73vv71sNWbrh69V77+977+977+9C++/ve+/vXkVN++/vSPvv706Gu+/vTbvv73vv70eGcSK77+9K2vvv70A77+977+977+9O++/ve+/vciZ77+977+9e0nvv70BFQTvv73vv73vv73vv70K77+977+9CO+/ve+/vW7vv73vv73vv73vv73vv70qPe+/ve+/vQ/vv71Z77+977+9fu+/vQ3vv71Y77+977+977+977+9Y++/vR4WIe+/ve+/vVDvv73vv71B77+977+9HT4W77+9c0hsZjBk77+9QO+/vVho77+977+977+9FxdSee+/ve+/vTwX77+977+977+9Yu+/ve+/ve+/vXvvv71W77+977+977+977+9UO+/ve+/ve+/ve+/vUTvv70577+9JnTvv70677+977+9NB/vv707WnDvv73vv70DK2NWPO+/vVvvv73vv73vv73vv73vv70BM++/vVDvv73Xl3fvv73vv73arO+/vW/vv70m77+977+977+9Hmc177+9G0Lvv70x77+977+9H3YI77+9Sgle77+9Ve+/ve+/vT/vv70s77+977+9De+/ve+/ve+/vXkj77+9c09RXW3vv73vv709Qxfvv73vv73vv71jJXIB77+977+9Qu+/vQ7vv73vv70r77+92KgB77+9OGUhUgbvv73vv71d77+977+9fRrckgbvv71JPe+/ve+/vVrvv70nK++/ve+/vVBL14ggHynvv71RIe+/ve+/vWrdt++/ve+/ve+/vQXvv70RBe+/vUrvv73vv70tFlLvv70BNO+/vTRK77+9MO+/vR0NdO+/vTnvv70UA++/ve+/ve+/vSrvv73omI49OOiigO+/vWVvJe+/vQ7vv73vv71X77+9Thxna++/ve+/vUHvv71e77+977+977+9Ie+/vR4cbu+/vUTvv73vv73vv73vv71t77+9cO+/ve+/vQ3vv73vv70277+9Le+/vXLvv70tAO+/ve+/ve+/vUnvv70Q77+9NBF0DgLlh6Ei77+977+9ORnvv70c77+9d++/vSnvv73vv73vv70F77+977+9MR1LIgU777+9KDQ177+9fWnMju+/vUtX77+977+9Hzoq77+9QHMP77+977+9Du+/vXdaQUvvv70bQXNp77+91Lvvv71QblDvv71seHfvv71d77+977+977+977+9Pe+/ve+/vTRQQGDvv71E77+9YG/vv71JBe+/vSgYw4Tvv71bO++/ve+/vUxHM++/vQ8TCGYYCkrvv70dQSHvv73vv73vv71BGO+/vQUcKO+/ve+/ve+/vRZVwo7vv73vv73vv70B77+977+9X++/vTLvv71U77+977+977+977+9QO+/vSxz7rSCVu+/vWxrQe+/ve+/ve+/vdq4BXd2Te+/vUcWMO+/ve+/vce1RRvvv70RFVcXbu+/ve+/ve+/vV54ZAEX77+977+977+977+977+9LTjvv73ehu+/vXrvv71yUhMV77+9CXRL77+9dHzvv70gSw/vv71o77+977+95aq2Ge+/vTTvv73vv71i77+9B++/ve+/vX7vv73vv73vv702XSvvv70S77+977+9c++/vTR9Fnbvv73GjikIKMKOfj1YGu+/veOohu+/vXBrST0OMWfvv71uH2Tvv73vv70d77+977+9TRNlXHwSB++/ve+/vRso77+9HO+/ve+/ve+/ve+/ve+/ve+/ve+/ve+/ve+/vUAx77+9cjQO77+977+977+9B3nvv73vv73vv73vv70Xce+/ve+/vRHvv71ZO3Lvv73vv71HEu+/vSzvv73LiANfRO+/vTjvv73RgO+/ve+/vW1Z77+9VlHvv73vv70777+977+9F++/vQfvv70j77+977+9c2BY77+9Ee+/vWxyWe+/ve+/ve+/ve+/vWLWpu+/vQcCTu+/vSgafWxA77+9Zu+/vVdUSu+/ve+/ve+/vRLfhu+/vdC1Be+/vTkbce+/ve+/vRFR77+977+977+9YSc6JS0q77+9GO+/vTXvv73vv70ra3Bu77+9XO+/ve+/vWzEoe+/vS5sTu+/ve+/vVRvNWbvv70DQ+iJju+/vXbvv70+bu+/ve+/vVjvv73vv73vv71Yaxtcfe+/ve+/vdOQCwTvv70777+9QiHvv73vv73vv70tc++/vRpC77+977+977+9We+/vUXvv70M77+977+9Ge+/vVfvv71j77+977+977+977+977+977+946q2Ge+/ve+/ve+/ve+/vU0l77+9aX/vv73Gie+/vWPvv71vdQ09BXDvv73vv70V77+9IFXvv71L77+977+977+9Ph9977+977+9HlAE77+977+9PR7vv73vv73vv71Nbzfvv70TZlzvv70yGxQEVxdt77+9Ce+/ve+/vVEB77+9Ue+/vRAzdO+/ve+/ve+/ve+/vQ/vv71e77+9NjMd77+9e++/vT0O77+977+977+9MEEb77+977+977+977+9K2vvv73vv71qcO+/vX/vv70477+977+9HUPvv71F77+9Yljvv70oSDbvv73vv73vv70dDnROUO+/ve+/vRc877+977+9M9aS77+9XO+/ve+/vcakCe+/ve+/ve+/vULvv70m77+9Qy3vv70477+91INDLV1ZOu+/vXF3LU4taO+/ve+/ve+/vS7vv71lAe+/ve+/ve+/ve+/vdG2du+/ve+/ve+/ve+/vQDvv70F77+9VUUbcXzvv70C77+9VC7vv73vv73vv73vv73vv73Dge+/vR5EFA4X77+977+977+977+9QSXvv70HZmjIpu+/vQFX77+977+977+9Ku+/ve+/ve+/ve+/ve+/vUAJNu+/vU3vv73vv718Fe+/ve+/vQJgB++/ve+/ve+/vRrvv70a77+977+977+9HCPvv70SKgPvv73shqLvv73vv70177+9DGVQ77+9J++/vVDFiiFDNW/vnITvv70K77+9ce+/ve+/vS3vv70m77+977+9BO+/vXFj77+9VO+/vW9277+977+977+977+9R++/ve+/vVBR77+9Pz/vv73cje+/vQtaUe+/ve+/ve+/vSbvv73vv71W77+9BGvvv71W77+977+9B++/vRnvv71v77+977+977+9UXXvv70vA++/ve+/vQAq77+977+9G++/ve+/vULvv73vv70L77+9RilIKe+/ve+/vWM6dgE/77+9BRxk77+977+9Je+/ve+/vQgqAlrvv71677+977+9X++/vcakASzvv73vv73vv71wE++/vWnvv73vv70y2Zjvv73hsrbvv70477+90oELXQ3vv73vv719xpAF77+9VjTvv73vv71EMFfvv73vv71nYWdWSe+/ve+/ve+/ve+/vS1uYQjFge+/ve+/ve+/ve+/vWbvv70Md3ZN77+9Je+/vT83Jk0477+9cQHvv70v3oBfW++/vXHvv73Jje+/ve+/vSfvv71dTO+/vSnvv71a77+9cu+/ve+/vUnvv70Z77+9DO+/vQM277+9R++/ve+/vWRt77+9ybnvv73vv718NyZn77+9Kwvvv70q77+9QO+/vTbvv713AkPvv71a77+9dTbvv70y77+977+977+9Fifvv73vv71s77+9HxM1Ye+/veGTq++/ve+/vUdb77+977+9HWTvv70p77+9MUwEBu+/vTAo77+977+977+9ce+/vWvvv70pM++/vWIpGGM/dXHvv73vv73vv71H77+9Qlzvv702C15Z77+977+9bB3vv71d77+9ejxV77+9DO+/ve+/ve+/vU03NhgYGWHvv73vv715Le+/vTFdH8SF77+9BmxJGu+/vWHvv71w77+9Amrvv70rfDnvv73chW8yAlMyZe+/vRInNCzvv73vv73vv71DFu+/vSxdADpG77+977+977+977+9cVPHlF3vv73vv70777+977+977+977+9Ywrvv71o77+9CQ3vv71i77+9GO+/vRUz77+9FHtr77+977+9c1l277+9HBp977+9Ie+/vXlHWkVP77+91qA7I1jvv70FXO+/vTYTAHDvv73vv70LSyZ+77+9Ve+/vT/vv71DFStg77+9RO+/vWki77+977+977+9e++/ve+/ve+/vQXvv71gJ0nvv71c77+9A2Pvv71077+977+977+977+9AB0777+9dAdP77+9fO+/vTkA77+977+9SDlc77+977+9bhluThpwee+/vUzvv73Xhu+/vWtrBw4xd++/ve+/vSfvv71177+9QGoA77+977+9UXDvv71rE++/vcW1be+/vVHvv73vv71R77+977+9Zu+/vXzvv70nTFBAcO+/ve+/vQ3vv70vRe+/vVDvv70TGu+/ve+/ve+/ve+/ve+/vWLvv73vv73vv73vv71J77+9elDvv73vv73vv71P77+977+9xoXvv73vv70o77+977+977+977+977+9ee+/vWjvv71IMlkX77+977+9Ye+/ve+/vXIB77+977+977+9zpwuG1VY77+9OGUM77+9AO+/ve+/ve+/vUvvv71KVO+/vTFw77+9Yn3ShO+/ve+/ve+/ve+/vWVY77+977+9LO+/ve+/vcmN77+9Ju+/vUfvv70MJmhD77+9dgjvv73vv70U36nvv73vv71V77+977+9VBjvv73vv71nEe+/vRMBYO+/vQFz77+9MQ48cO+/vVPvv708ae+/ve+/ve+/vSjvv73vv73vv73vv70V77+9Qu6HryMK77+977+9dSBOGSzvv70477+9CmZG77+9DH0A77+9WO+/vTHfkO+/ve+/vTJ6QAHvv73vv70+FTpW77+977+977+977+9cFnvv73vv73vv70S77+977+9TW58FCrvv73vv70V77+977+9fhzvv71n77+977+9S1hwSkErfm1rR0JlcV7vv70c77+9BkXvv73vv70LFu+/ve+/vSAd77+9bu+/vTbvv73vv71IODILZu+/vRlt77+977+9UDLvv70p77+977+9OHDvv70177+977+977+9f++/vcuQVBlcW++/vREcVCzvv73vv71Q77+9JyBSJgvvv73vv71YBu+/ve+/vS4ZMDkU2anvv70J77+9FBdl77+9W8mrL++/ve+/ve+/vSjvv73vv73YmEXvv71P77+9QO+/vTwe77+9RO+/ve+/vWIfeVcwMO+/vRxx77+977+9Su+/ve+/vSdqVu+/vTvvv73vv73vv70+Zu+/vd2lazBe77+9au+/ve+/ve+/vQLvv73vv70U77+977+9A++/vQ0+CEQF77+9Ulzvv702HSJl77+9TO+/vTJ4ZQ1W77+977+977+9K++/ve+/ve+/ve+/vXp077+9OnwYKsSs77+9cloA77+977+9C2Dvv70u77+977+977+977+977+977+9WBHvv71h77+9ZO+/vduH77+977+977+9We+/ve+/ve+/vRc4G3Hvv71zCw7atM+A77+977+977+977+977+9OBHvv73vv73vv71aVO+/vQzvv73rmYDvv70gCO+/vVgR77+9Iu+/vQg477+93o4vIw4s77+977+9MU0fRO+/vRDvv73vv73vv73vv70j77+977+9PO+/vUR777+9Ae+/vRtG77+9VyUsUO+/vVAX77+9Ah3vv70677+9bO+/ve+/vTkUOE8KQu+/ve+/ve+/vX0177+977+977+977+9VVQ/77+9DTVO77+9du+/vQtSbx7vv70c77+9YO+/vRwKdO+/vUjvv70077+90LHvv71J77+9cMiPAVYiEu+/vSoFHe+/vUNMYO+/vW4ecERF77+9Ju+/ve+/ve+/vWtTwoRjGxfvv70Qczfvv710NGPvv70277+9X1rvv73vv71LDATvv73ckgY977+9Fk9VLe+/ve+/ve+/vXjvv71T77+9Ju+/vVQpQVjvv71wa++/vTrvv73vv70yF++/ve+/vTZn77+9ejYk77+977+977+9bQbvv71Ew4/vv73vv70r77+9Ie+/vW7vv71G77+9zpJ9VGQL77+9HjDvv71C77+9JA3vv73vv73vv71j77+9Ju+/ve+/vSpW77+977+97pGFIXnvv71WUe+/vVfvv70VeBXvv71R77+9Dlzvv73vv70E77+9VnLvv73vv73vv705BUVcAicX77+977+977+9Sgjvv70877+977+977+977+9SXc177+977+977+9Tu+/vcuj77+9Te+/vUJbK++/vcekUmvTqu+/ve+/ve+/vTBkP++/vQY6ESjvv73vv73vv73vv71vG0pu77+9EzHTniYM77+9aR4G77+977+9Y++/vXhJMe+/ve+/vTTvv71pbhxFF++/ve+/vXvbhO+/vRfvv73vv71c77+977+977+977+977+9AExqVhvvv73vv70f77+977+9A1UtPO+/vUgOA++/ve+/ve+/vVwyXhMe77+977+9U++/vQTvv70F77+977+9Xu+/vRjvv73vv70YDjTvv71g77+9Pu+/vSnvv70gXHwS77+9fAJPVy7vv73vv73vv71q2Jfvv70K77+9TO+/vVLvv73vv70yDwDvv70mZu+/vS/vv71d77+977+9CyFBWWjvv73vv71+77+977+9Cu+/vQRLcEpBCyZp77+9OO+/vWUudu+/ve+/ve+/vQ7vv73vv71z77+977+9UihB77+977+9QRHvv71A77+9T2vvv73vv73vv71Ryqbvv73vv73vv73vv73Fr++/vR3vv71/77+977+977+9H++/vSbvv71X77+9Dhxr77+9QDkfw5bvv70Rd3RN77+977+9UXBj77+9ehxl77+977+977+9a++/ve+/vQLvv73vv70vAwXvv71obx3vv73vv70IFQTvv70NPjwt77+9bO+/ve+/vSwV77+9K++/ve+/ve+/ve+/ve+/vd+e77+9TCYZeO+/ve+/ve+/ve+/vWfegB3vv73vv73vv73vv70RUDFr77+9Me+/ve+/ve+/vVQmSGwe77+977+9Ge+/vSfvv704Pu+/ve+/vTglwqRN77+977+977+9Rx4i77+977+977+9f2QNUe+/vQQz77+9Ru+/vTrvv71P77+9Be+/ve+/vWBuJhATK++/vXdFG3Dvv73vv70LQe+/vUfvv73vv71FQ8KI77+9Pe+/ve+/ve+/vTBF77+977+9ZRR8Fnbvv70VbwVe77+977+9FmtjZjDvv70QGBXvv73vv73vv73vv73vv73vv70L77+977+977+9Te+/ve+/vW/vv70GIyPvv73vv73vv71v77+9ZVTvv73vv71577+9Ljnvv73vv73HkipNdEjvv71MWO+/ve+/ve+/ve+/ve+/vTjvv73agV9Y77+977+9QB/QiQwFBAnvv71N77+977+9T++/ve+/vSvvv73vv73vv73vv73vv73vv70YN++/vdqiDRAYFe+/vXZPSO+/vTnvv73Thu+/vWLvv73vv73vv70G77+9Mu+/vT5u77+9J2EX77+9czQOWWdPe2rvv73vv70yBe+/ve+/vRM/Se+/ve+/ve+/vTXvv70m77+977+9PO+/ve+/ve+/vX4A77+977+9Ju+/vQzvv70jEz9yfkAnDBnvv71h77+977+9KEUyS++/vTNUHgvvv70pYSkc77+9Bu+/vTg+77+977+9eO+/ve+/vQQY77+9CjXvv73vv704Nw4wKVYaJe+/ve+/ve+/vVY8TAx977+977+977+977+9NCRo77+977+977+9UO+/vTd/Oe+/ve+/vXbvv73vv73vv73vv71hFyZqQyjvv70TWBnvv71h77+9Lgjvv73vv71QQO+/vSBl77+977+9MgwfEQ4rPO+/ve+Yhu+/vXwVOO+/ve+/vRnvv73vv716MVkbGhLvv71577+9W++/vQcrVu+/ve+/vUh4O1ACEyvvv73vv73vv73vv73vv73vv71zMu+/vSvekO+/vc2uUu+/vWNs77+9WGTvv71A77+9BNe0Tx9177+977+977+977+977+9Pu+/vTHvv71N77+977+977+977+9WWhIGnFSH++/vcW+77+9Ue+/vSjvv70o77+9LgPvv70J77+977+977+977+9MzDvv73vv73vv73vv71F77+905/vv73vv73WgWfvv70qGO+/ve+/ve+/ve+/ve+/vXR8Mu+/vRs777+9Y2Xcglnvv70A77+9b1zvv71z77+977+977+977+9DSJKOe+/ve+/vS8b77+977+977+977+9VCV62Lwq77+9MjU6I++/vSbvv70yJjpUc2fvv73vv70qNE/vv70Pce+/vUUOBO+/ve+/ve+/vQIk77+9WWzvv70X77+9KO+/vdaT77+977+9Ejs5MHzKp++/vX3vv70T77+977+9S1Pvv73vv73vv701ee+/vU/vv70+Lu+/vR9NHe+/vQdaAe+/vX8C77+977+977+977+9BH8qX++/vWI+77+977+9CTPvv73qnojvv70q77+977+977+9HU9577+9cX/vv71K77+9VB5P77+977+977+977+977+9eu+/vdeEYWDkrLTvv71gaUnvv73vv71n77+9eO+/ve+/vT0JE++/vSFM1oZQ77+977+977+9MEs3OO+/ve+/ve+/ve+/ve+/vTAqWu+/vXrvv70fKkLvv70Q77+977+977+9bu+/vStQ77+917zvv70477+977+977+9F++/vVhk77+9IE5Z77+977+9MQUf77+977+977+9egZaRu+/ve+/ve+/ve+/vVNF77+977+977+9EO+/ve+/vW/vv73vv73vv73vv70MXQBGVu+/vWpwIX1Wzq5mLe+/vW8aCFwt77+977+9cWXvv70M77+977+9GFgNfWXvv73vv70l77+9WlZF77+977+977+9Ve+/ve+/ve+/vdeYNGDvv73vv70377+9Ge+/vQXFue+/vca877+977+977+9Se+/ve+/vXAzLm3vv73vv73vv70L77+9IVPvv73vv73vv73vv71xY++/vTRI77+977+9XXsDSu+/vc2PbSJDU++/vTzvv73vv73vv70n77+9Se+/ve+/vX4L77+9Qu+/vWbvv73vv715anQIQ3zvv70EA++/ve+/vRwCZ1Dvv71Q77+9Uu+/vTzalQZ5eO+/vR5N77+9FO+/vca+Uu+/ve+/vSvvv71NTO+/ve+/vRRUIe+/vX0lfyJF2Z16cjIYbO+/ve+/vUvvv73vv73vv73vv71lFAQVDiZW77+9PO+/vR/vv70277+977+915EC77+9FA7vv73vv70+77+977+9Mg/vv70eHe+/vQ4X77+977+9QUxh77+9HH/vv70I77+9UO+/vXlE77+9L++/ve+/vWcTPu+/vR9K77+9YO+/vT7vv70fYlbvv73vv73vv73vv71Dw6jvv70A77+96p6E77+9Cjdj77+9Lu+/ve+/ve+/ve+/vTcFTXhn77+9W++/vSVpxKkN77+977+977+977+977+9XO+/vQstMmvvv73NrO+/vScr77+977+9STLvv73vv73vv73vv70v77+9y7EubgYBcO+/ve+/vQFPVi3Hmu+/vTkN77+977+9TW48WO+/vQJkF3Pvv70L77+9JCbvv73vv71L77+977+9X++/ve+/vVrvv73vv73vv70m77+9dGYlFe+/vWPvv71M77+9Ae+/ve+/ve+/ve+/vTZZF++/vW/vv70277+9be+/vUnvv70QLnZt77+977+9A++/ve+/ve+/ve+/ve+/vQPvv70r77+95IaH77+9NUMZ77+977+977+977+9L37vv73vv70N77+9V++/veyjsyZl77+977+9Ug9HQO+/ve+/ve+/vWTNiu+/ve+/ve+/ve+/ve+/vRLvv716FVQhcD9j77+977+9TRNAU++/vXdq77+977+977+977+9EO+/vQIF77+9Vh7vv73vv71M77+977+9Ge+/vVJo77+9eAPvv70GFkXvv70z77+9Je+/vWDvv71EaO+/ve+/ve+/vQta77+9Tu+/vSTvv70pNl0X77+9bSXvv71wZe+/vUws77+977+9cO+/ve+/vR0H77+9e3BJ77+9LDg4Ee+/vTob77+977+977+977+9Ne+/ve+/vXFF77+9RgQUAe+/ve+/ve+/vULvv70m77+977+977+9EQ/vv71I77+9ESPvv70h77+977+9RS1zcO+/ve+/vQcX77+977+9YD/vv70b77+977+977+9Uybvv73vv70qMFUXwpkF77+9OHzvv70iNO+/vUbvv70177+9ce+/ve+/vT3vv70z77+977+9ZwLvv73vv73vv71xR++/vVrvv71pIjjvv73vv73vv73vv70lFAQUMmUwWRfvv70F77+9Blzvv706CzpG77+977+9VUvvv71SAu+/vSzvv73vv73vv70u77+9UlLvv73vv70D77+977+9UA4377+977+977+9BFsb77+977+9Rkjvv73vv73kk7jvv710DRYY77+977+977+9dVbvv73Zou+/vU9gP1Mv77+9GSjdqTXvv71+77+9Hu+/vSDvv71T77+9BO+/ve+/vRzvv719P++/vRMqC++/ve+/ve+/ve+/ve+/ve+/ve+/ve+/vRLvv71H77+9LiMjITzvv71yMSQd77+9Ze+/vWcAXe+/ve+/vRcaVyMM77+9H++/ve+/ve+/vSLvv70DW++/ve+/vTcA77+9Iu+/vTtG77+977+977+977+977+9Su+/vS8JJV7vv73vv714Mu+/vWTvv71NUe+/ve+/vVEB77+977+9Re+/vd2OISNh77+977+977+977+9SO+/ve+/vSYeK++/ve+/vU/vv73vv73vv73vv73Ovm9LBe+/ve+/vThYQlEq77+977+977+977+9L++/vQpP77+977+977+9YRfvv70u3Ijnq5fvv73sprl4P1jvv71777+9VuOusjXvv73vv73vv73vv71oShpw77+977+9AV9EHO+/vXTvv73vv714PRrvv71GNCYNeO+/vXwlfu+/vTwPDCjvv70p2oDvv73vv70mDe+/ve+/vSFV77+977+9Ue+/vRDvv73vv70h77+9a3Jj77+9Lu+/vXkGP++/vTkGH2rvv70o77+977+9BjQnDe+/vSrvv73vv71UbRAnFe+/ve+/ve+/vTVxK++/ve+/ve+/ve+/ve+/ve+/ve+/ve+/ve+/vXFj77+9VDQk77+9IADvv70vXu+/ve+/ve+/vSbvv73vv71P77+977+977+977+9eDNQAgLvv73vv73Ste+/ve+/vQ3vv73vv73vv73JmG/vv73vv73StVBB77+9BjcW77+977+977+9Nu+/vSUN77+977+977+9YO+/vdGD77+977+977+977+9yop477+9U++/vU/vv70u77+9CHFc77+92oTvv70sPe+/ve+/vXXvv71Ob++/ve+/vVUW77+9N+6RrkUYLHomFXxTQGDvv70DfT9Z77+977+977+9Pxjrl4VhCUoec++/ve+/vSDvv70b77+977+977+9JxLvv73Fi++/vSocb28bAO+/vQjvv73vv73vv73vv70BBSrvv73vv73vv73QpMSYMRE+77+9RRjvv73vv73vv73vv71Q77+9eBgK77+9Ze+/ve+/vQ4l77+9SFzvv73vv71DQD7vv73vv70QSmQY77+9JmA7PO+/vT1ZNe+/ve+/ve+/vRBBQmRxT++/vSp8Nu+/vVNcXe+/vRFz77+9fjzvv70zDh5J77+977+9HO+/vXjvv73vv70bAO+/ve+/ve+/vSXvv73vv70i77+977+9Wu+/vWUxO1pFPRYY77+9OO+/vdGc77+977+9be+/vUbvv73vv70LfO+/vVzvv70OIe+/vQMFcGfvv70aKGBw77+977+9De+/ve+/vQV7Du+/vXQN77+9CWZiJVxW77+9Ce+/ve+/vUF8GXHvv73vv73vv70K77+9Ok0EDO+/ve+/ve+/ve+/vQIF77+977+977+977+977+9aVM6Ze+/vUdxT++/vWrvv70YGe+/vXZPxL8C77+9AO+/vUsKN2Pvv73vv70LBQR377+977+977+977+9ce+/vRAnFe+/ve+/vWBzd++/ve+/vTnvv73vv71d77+977+977+9ERUveCrvv73vv73vv73vv73vv71tbVAo77+977+9HTPvv73vv73Jg0cqVu+/vcqK77+9IFTvv71H77+9de+/vci5Be+/ve+/vX7vv71DLN2477+9cxI+77+977+96Je377+977+9K1A6LO+/vVNAV++/vVZKf++/vSxN2Z3vv71GD++/vTzvv71uWO+/vU8GPQ8K77+9JW8l77+9DO+/ve+/ve+/ve+/vTnvv73vv71jOu+/veyehO+/ve+/vSzvv714bQTvv73vv70r77+9Se+/vUcM77+9AlLvv71RVi3vv73vv71577+9Tnbvv70YRO+/vWNdKO+/ve+/vRfvv73vv73vv71hPyLvv70P77+9TO+/vU7vv71t77+9CO+/vWwMU1Avx4B4ae+/vUZ377+9P++/vVoJ77+9YE0K77+9Yhl877+9DO+/vVjvv71Q77+977+9L++/vSAU77+977+9HHJWGiHvv73vv70B77+9ZSNsDAAq77+9SzhPOxd/77+9WO+/vXkGP++/vRnvv73vv70oVO+/vQtbZ+ORih9Q77+977+977+977+977+977+9cHHvv70s77+977+977+9VO+/ve+/vXdF77+9cVvvv70UPFXvv70UZxY077+977+977+9aBXvv73vv70QYu+/ve+/vUbvv70hYe+/vURtGCfvv73bsDxm77+977+9Rg9O77+977+977+9Pm7vv71lRe+/ve+/vWnEiQnvv70wDu+/vXTjsrbvv70o77+9aQbvv73vv71I77+977+977+9U++/vXjvv71y77+9WO+/vT7vv717JA3vv70K77+977+9LEcTPg4V77+977+977+9HXsYfH3vv70dEWHvv73Hje+/vVNR77+9x7Hvv73vv71B77+977+9OO+/vdGI77+9fRU477+91oZ5Bh/vv73vv70GdEh6PO+/ve+/vQEA77+977+977+9DlcWbe+/ve+/vU/vv71V77+977+9cCHvv73vv71aCgB477+9PQ52Nu+/vXtKV++/vSUU77+9Bu+/ve+/vXsc77+977+9Wu+/ve+/vXo/JO+/ve+/ve+/ve+/vSnvv73vv73vv71aRu+/ve+/vUtC77+9KNiYMG13D17vv70o77+977+9C2Lvv70+77+9GiHvv71SIQHvv70oICQVTO2StNiSNO+/ve+/ve+/vQ3vv71JM1Lvv70mP13vv70QCh0j77+977+977+9Mu+/ve+/vW/vv73vv73vv70H77+9IhgZCXvvv70oW++/ve+/vWsiLO+/ve+/ve+/vXrvv73vv70v77+977+977+9Z++/ve+/ve+/vRRL77+9Be+/vTMKXu+/ve+/vRYaRu+/ve+/vWEn77+9Ku+/vWPvv73vv73vv73VkUDvv70wcu+/ve+/vTxlGe+/vVJqCHnvv73vv71oKiVoakRoau+/ve+/vday77+977+977+977+9Qe+/ve+/vSF2ce+/ve+/ve+/vSLvv71R77+9L9CzMMyAeDlKVU/vv70AB++/vSXvv71tcxccE0Lvv73vv71z77+977+9JTIY77+977+977+9C++/vUjvv73vv71o77+977+977+9de+/ve+/ve+/ve+/vdGj77+9Tg/vv71U77+9FF0IdVwS77+9DO+/ve+/vVxHI1hCce+/ve+/vQfvv71CAivvv70277+9CnE477+9JO+/vShd77+9P++/ve+/ve+/ve+/vdK1OO+/vdyDZ3rvv73vv70C77+977+9W3Hvv73vv70b77+9e++/vXBzST0ONnXvv70VbwVuL13vv70TbG3vv71T77+9eO+/vW3vv73gtIJm77+9Eyzvv700XRDvv70t3Ijvv73vv71dOO+/vdGEY++/ve+/vWgT77+977+977+9aAPvv71p77+977+977+9zbokLU5p77+977+977+9Czfvv71Ye1t677+977+9SQbvv73vv71o77+9RBlcX++/vQHvv71GbWnvv71/ECrvv70r3op077+977+9Ke+/ve+/ve+/ve+/ve+/vW9AAHwVKe+/vS1977+9Wu+/ve+/vVbjkr7vv73vv73vv73vv70WbErvv71wS0k9RO+/ve+/ve+/ve+/vXF977+9Bu+/vUTvv70/77+9Ze+/vTZq77+9C1VL77+9Eu+/ve+/vXvvv73vv71QMV7vv73vv70OTi4J77+9LOCqtu+/vVgV77+977+9SGsnDu+/vXRh77+977+9DwUEV++/ve+/ve+/vQZsP1Nx77+9Lu+/vVPvv73vv73vv73vv70LPSPvv71D77+9YWXMih9iFgQUDQgoDjF377+977+977+977+9ZGHvv71a77+977+9QCle77+977+977+977+977+977+9P++/ve+/vT/vv73vv70P77+977+977+9ZVlf77+9SQNu77+9Au+/vTI477+977+9Be+/ve+/ve+/vVHvv70n77+9JWnvv71j77+9OnwSdu+/vd+1X++/vUITw4rvv70V77+977+9ZlAANxbvv73vv73vv73vv70fOUzvv73vv71oE29K77+9ae+/vVXvv71UYu+/vUrvv70kW3hEV2vvv70p77+977+9FSjZrO+/vQzvv707Fe+/vU4F77+977+9Cnbvv70Sa++/vUDvv73vv70Y77+9UO+/vRwhTHvvv71G77+9VEh5MwZI77+977+9de+/vWrvv73vv73vv73vv73vv73vv73HhGQzD9ai77+9eUoQfO+/vQzvv70QGGYkYO+/vS/vv73vv70tJnw+14zvv70pAu+/vSxb77+9czful6nvv70g77+9agfvv73vv73vv700Ee+/ve+/vWx177+977+9D1Xvv73vv73vv70REmVw77+977+9B0/vv73vv73vv70jC++/ve+/vQvvv73vv70ZOO+/vdmECiEG77+977+977+9W++/ve+/ve+/vSfvv70177+9AO+/vTPvv73vv70CFO+/ve+/vUXvv71xRu+/vTwcYHLvv73vv71677+977+9MRUmVkJY77+9YVTvv709UO+/ve+/ve+/vcW477+91YAFBi/vv73vv71977+977+9Iu+/ve+/ve+/ve+/ve+/vUbvv73vv73vv70vfBVx77+9EXcd77+977+9XAoHL++/vcymeTjvv73egXIh77+977+977+9EdWaWHpa77+9N2EHbi7vv73vv71EGe+/ve+/vT0RF++/ve+/ve+/vSBV77+9ee+/ve+/vTHvv703Xy7vv73vv71477+9D++/vTIK3JIGAu+/ve+/ve+/ve+/ve+/vTDvv70S77+977+9OHBT77+9NCw277+94p6y77+9KO+/vRNQQO+/vUHvv70If+qekB4277+977+9Mlvvv73vv73vv73Ohu+/vXfvv71N77+9AO+/ve+/vRkS77+9PDbvv71JJe+/ve+/vXjvv71d77+9Y2wdOO+/ve+/vQXvv70I77+977+9cXcdPCPvv71/77+9A1tMR++/vQd877+9fu+/vTzvv73vv73vv73vv719RjoS77+977+977+9eu+/vTzvv705Oj/vv73vv73vv71077+9ck4G77+977+977+977+977+9SSN6JC3vv70K77+9BGVgYhRYORFFfALvv71CPGvvv73XkGskSu+/vUxUdu+/ve+/vQBKJO+/vUBmCXTvv73vv71jCO+/ve+/vVrvv73vv71rX++/vStP77+9F++/vRDvv71SVjFfJEMo77+977+977+9Yxnvv71WRe+/vTvvv73vv70zbe+/ve+/vWVVEMK2cgpFE++/ve+/vX4077+9cnHvv71477+977+9QQXvv71r77+977+9Le+/vRZCbO+/vSHvv71YTO+/vXsbWO+/vQrvv707RiTvv70EaO+/vSQ4Twnvv70zK2B477+977+977+9KO+/ve+/vQfvv73vv73vv73vv70N77+9AV/OsO+/vXfvv73vv71EUWEMcw1+77+977+9Me+/ve+/ve+/vWBLd++/vTtN77+9zIgl77+9Lj7vv73vv73vv73vv73vv73vv71La1UA77+9S++/vXLvv73vv71877+9Pe+/vR4877+977+977+9Re+/vVHvv73vv73vv71lxIHvv73vv70e77+9Ye+/veOHmA1z77+9fhxgcmNV3IpfWzvvv73vv73ktZoIHu+/ve+/vW0lazFBG++/vUvvv70qTO+/vQZhZFPvv73vv73vv70jBe+/vSnvv70mTe+/ve+/vSxe77+977+9HnZOxJ/vv73vv702bu+/vX1l77+9AADvv71777+9cO+/ve+/vQlA77+977+977+9fFcD77+977+977+977+9F++/ve+/vU8Xde+/vWki77+977+9DHRB77+9y7gnF++/ve+/ve+/vRk577+9ej9e77+977+9DhVCDO+/ve+/vQbvv73vv71r77+9Ru+/vRxuae+/vQBcRxTvv71Y77+9Hu+/ve+/vVHvv73vv73vv73vv73vv71EbRjvv73vv73vv71p77+9DUlj77+9AWhtzIzvv73vv73vv71477+9X++/ve+/vUYP77+9KtqIN2vvv73vv73vv73vv73vv71277+9I++/ve+/ve+/ve+/vdW077+93r8JfBp277+9Ou+/vSbvv73vv70m77+977+977+9Ku+/ve+/vRdhB++/ve+/vRZA77+9BO+/vRHvv70IcRgZGRrvv73vv71d77+9EFJ5dO+/vTrvv73vv73vv71077+9bS4pEhLvv70k77+9QlThsKLvv73vv71077+9YUTvv704HRjvv73vv71z77+9c2Pvv73vv73vv71377+9Ue+/vUBs77+9Ie+/ve+/vRw477+9CjV/fhXvv70vUu+/ve+/vQoZaO+/vTRI77+9ShDvv73vv70mPO+/vRDvv73vv71Q77+977+9OhVA77+9AO+/ve+/vWMh77+977+977+9Bu+/ve+/vRPvv70X77+9SDQJ77+9De+/ve+/ve+/ve+/vS/vv70R77+9woDYkREkNu+/vSAc77+977+9EBbvv70IY0vvv70W77+977+9Ye+/ve+/vQvvv71xXe+/ve+/vTTvv70/De+/ve+/ve+/ve+/vQZ377+977+9Qe+/vSbvv71WUe+/ve+/vXprcO+/vWszCu+/vSRE77+977+977+9adiJLXtmAO+/vcyC77+977+977+977+977+977+977+977+9Cu+/vW3vv71gT++/vRd/77+977+9Y++/vd6P77+9bG1477+9Zzxm77+977+977+977+9aAN+77+9PgPvv73vv73vv73vv71CCe+/vWrvv73vv70/77+977+977+9NUUbBl4EURFQBHwQLO+/vS/vv71mau+/ve+/vUDvv73vv70rx4vvv71K77+9b3bvv73vv70n0ZQ077+9SdSnZ++/ve+/vVrvv70u77+977+9Z0Hvv711XxVK77+9JWnvv71nFO+/vXNE77+9de+/ve+/ve+/vcWz77+9IO+/ve+/vUR7K0rvv70EBO+/ve+/vSMJ2JQ077+977+9K13vv73vv73vv70477+9VO+/vXPvv70m77+9VXErNu+/ve+/vThhEEFE77+9QO+/vR1TcO+/ve+/vQDvv71QPO+/vTvvv71l77+977+9OO+/ve+/ve+/vQJc77+92oJ7Su+/vWDvv70+77+977+977+9Jg4h77+9HMihZxfvv73vv71PTkkBd2RNZ2YkHGTvv73vv73vv73vv70J77+977+977+977+9ce+/vQhzDT4sNnpwee+/vWYU8aOXje+/vVQe77+9fe+/ve+/ve+/vQ4977+9DgHvv71D77+977+977+977+9YBEM77+977+977+977+9SO+/ve+/vQlu77+977+977+977+9du+/vQQt77+9BTnvv73vv73vv70WUXjvv70f77+9FgHvv70WHu+/vWYe77+9FiHvv704JhPvv73vv70xZE4Y77+9EO+/vWYWcu+/ve+/vUjvv73vv70jUyBK77+977+9SQnvv71D77+977+9QXXvv70wOyrQlTALJcqANe+/ve+/vRTvv73vv70R77+9Ynfvv70iTijvv73vv73vv73vv71n77+977+9U++/vUjvv71BQO+/vTXvv73vv70CAO+/vVLvv71r77+977+977+9cXfvv70a77+977+977+9De+/vRLvv73vv70vR33ChO+/vWrvv70HT1Tvv73vv71ZcUfvv70k77+977+9dB0K77+9JO+/ve+/ve+/ve+/vdqn77+91IJU77+977+9RxZwS++/vVTvv73vv71owpw+77+977+9Q9ShXO+/ve+/ve+/vVHvv70C77+9EGLvv73vv70P77+977+9dEHvv73vv70p77+9XxZQ77+9J++/vSQa77+9Ae+/ve+/ve+/vSDvv73vv73vv73vv71YGivvv73vv71t77+977+9Zu+/ve+/ve+/vQE677+9YFJf77+9bdug77+977+977+977+9cu+/ve+/vVtx77+977+9M23vv73vv70YBe+/vVUtG++/ve+/vQwq77+9eTNQ77+977+9eh9K77+9OO+/ve+/ve+/ve+/vQ1fOe+/vR/vv70FGELvv71p77+977+9Mh7vv73vv70BS++/vXYs77+9WXHvv73vv70RW++/vUYYGTnvv71o7ZyIC++/ve+/vQbvv73vv73vv71wZe+/vcy8Ke+/vV18Eu+/vVV+77+9Sk0Mbwbvv71we++/vSTvv705EQfvv73vv73vv73vv70VMVvOst6AIu+/ve+/ve+/ve+/ve+/vQ4IHnTvv73vv73vv70N77+977+977+9Qu+/vRDvv73lrbPvv73vv71K77+977+9cUfvv70377+9TwYZVe+/vQHQjxI777+977+977+9B++/ve+ajO+/ve+/vQLvv71U0IJ377+9Gu+/vRzvv73vv73vv71i77+9Vu+/ve+/vTwH77+9KCB4M1Dvv73Ttu+/vSbvv73UjRNh77+977+977+977+9Qu+/ve+/ve+/ve+/vSHvv70H77+977+9f3nvv71tfHbvv73vv71o77+9Mkbvv70X77+9Gu+/ve+/vU7vv71C77+9AO+/vRLvv70E77+9NO+/vQUbDSXym62X77+977+9dSnvv709WO+/vQsZ77+9JTYK77+9LCrvv70377+9NVfvv73Plgod77+977+9Ce+/vVkZf++/vcW/AiXvv70Q77+9eO+/vWI5QFIE77+977+9BEvvv71M77+9UhTvv70J77+977+9W3Fb77+9ZO+/ve+/vWwd77+9au+/vX3vv73vv70zce+/ve+/vRFz77+9fkQUDu+/vXZM77+9TF0A77+9OBvvv70b77+9Ae+/vSzvv70YIO+/vQl3c0l977+977+9J++/ve+/ve+/vQrvv70U77+977+9WFs7Xu+/ve+/ve+/ve+/vVwN77+977+9aD1Obe+/vQMbE2Yc77+9eREe77+9XO+/vTpNBO+/vVjvv73vv70DBxRK77+977+977+9WBM377+977+9ZXjvv71T77+977+977+977+9dCFL77+977+9Jgt477+9U++/ve+/vQo377+9IypK77+9OH7vv701BX/vv71c77+977+9Cu+/ve+/vX7vv70I77+977+977+9OO+/ve+/ve+/vSXvv70t77+977+9c2p6Y3rvv73vv73LrClp77+977+9De+/vRVtQO+/ve+/ve+/ve+/vVHvv70W77+9G++/vXLvv73vv73vv73hkbVYHu+/ve+/ve+/vVPvv73vv73rjIJHK35A77+9Ju+/ve+/vQMl77+977+9Ywoo77+9bknvv73vv70bFu+/vWBzDyIq77+977+977+977+9W03vv73vv71VxIHvv70077+977+9ExXvv73vv71I77+9D++/ve+/vWV2Gu+/vWnvv73vv73vv73vv71+Hu+/ve+/vcKlN++/ve+/vcqYWVDvv70XKsSJ77+9Nu+/vTLvv73LrAIb77+9LO+/ve+/ve+/vR3vv70SJu+/vUp677+9FB5KX++/vWsB77+977+9OE0E77+9Ye+/ve+/vUTGvDE87q6D77+9B2ByQ++/vVrvv73vv71mAe+/ve+/vVjvv73vv73vv73vv73vv73vv73vv70XSyBs77+977+977+9L++/vWFaMO+/ve+/ve+/vQpB77+9Sz3vv73vv73vv71v77+9Fe+/vVAG77+9EO+/vRlI77+9Ru+/vSYA77+977+9QC8X77+9RGt+77+9Qe+/veagrRPvv70XD1QHBe+/vWtEbO+/vQ7vv73vv70B77+977+977+977+977+977+9dNeM77+9I++/vUXvv73vv73fjkfvv71XVAPvv73vv73kpoJ677+9N185Xu+/vVXvv70nC3jvv73vv71bWFkJ77+977+977+977+9GU8VHu+/vVjvv73vv73vv70g77+9RArvv71Eby3vv70tW++/vWpN77+977+95YaO77+977+9yLUFexnvv70QKe+/ve+/vTrvv71i77+9PgAn77+977+9Qgnvv73omoQ5Bj9+77+9Yx7vv73vv73vv73vv70477+9Qu+/vUHvv71lMTsCShvvv71pw5jvv70P4KSCFu+/vRQeb++/ve+/ve+/ve+/ve+/vTjvv73SmTbvv70/ChXvv70lXwXvv71R77+9DVhQ77+977+977+977+9y7Lvv70d77+9Yxbvv73vv71o77+977+9E1Hvv73vv73vv73vv73vv73puKN0Le+/vRt877+9d++/vQTvv73vv71rce+/ve+/vQHvv73vv70244Ge77+977+977+93It5Bh8oJe+/vRR1KBHvv70Q77+977+977+977+977+9EO+/veijsmrvv73vv71YEe+/ve+/vULvv70WKO+/ve+/vSUUC++/vR4877+9W++/ve+/ve+/vX7vv73vv73vv73dpe+/vTFJG++/vXdRO27pnJLvv73vv73vv701eO+/vVcx77+977+9YO+/vS4IFx/vv73vv73vv73vv70F77+9SDrvv73vv73vv71H77+9XO+/ve+/vWbvv71f77+9bDxT77+9DH8sX++/vUvvv71mQu+/vQQGNu+/ve+/ve+/vV/vv73vv71VDhoy77+9XWbvv73vv71qI3jvv71sJSo177+9dD7vv73vv71QEe+/ve+/vS/vv73vv70NCiUw77+9Mirvv70YXHwSZUIcDi4BPe+/ve+/vVU077+977+977+9CxsTJmjvv73vv71J77+9ICZq77+9KBUSKO+/vWPvv73vv73vv73Lue+/vS3vv73vv73vv716Yznvv71977+9ee+/ve+/vUo0CUg0DQTvv73vv71A77+9Ke+/ve+/ve+/ve+/vSBUSDDNi0NbJ++/vWtRVDHvv71NVFMpZ9e5UzIAdAJs77+9fO+/ve+/ve+/vQlNaEleeX9477+9MiHvv71fIxJNfFbvv70g77+9MjTvv70g77+977+90I5PIu+/ve+/vSHnsY7vv73Yi++/vT/vv73vv71pdy3vv73vv70BNu+/ve+/ve+/vQnvv73vv70LIiAL77+977+977+977+977+9IRfvv73qq4RHFu+/vRtHM++/vWpD77+977+9azLvv73vv73vv73vv71877+977+977+977+9Mu+/ve+/vVTvv71pTzUe77+9WO+/vVl6Pxrvv71G77+977+9MQXvv705G++/ve+/ve+/vRcqJe+/ve+/vWsKSu+/vTgONXfvv70NPe+/ve+/vVoh77+977+9MyLFmWbvv709Ze+/ve+/ve+/vQVY2ZTvv73vv73vv73vv70OL1V/77+9aiEKCmBiZu+/vVXvv73vv73vv73vv73vv70I77+9Re+/ve+/vRggJ++/ve+/vQRNGO+/vXgrcG7vv708XF7vv70JR++/vTrvv70q77+9cWzvv70CXFvvv70B77+9Wu+/vXB+77+9XDg5EUfvv73vv71xS++/vVQ877+9W++/vXPvv71N77+977+977+94ZOIE++/ve+/vVvvv73vv73vv70a77+9OAnvv719bu+/ve+/vRXvv70l77+9eu+/vRvvv70mUEowRRfEgxUrcXrTvO+/ve+/vV3vv73cjAPvv73vv70oTO+/vUZwQ++/vXrvv73vv701Me+/ve+/vdaZej/vv711NGHvv73vv70LIUfvv71U77+9DH7vv71Z77+9d38ZPgwW77+977+977+9axMWXO+/vTYT77+9VSzvv73vv71N77+977+9ZwLvv71DNO+/ve+/ve+/vV7vv73vv73vv70bHmYDOe+/vdqJW0rvv73vv71a77+977+9YDEedtehU9KiVEhgT++/vQ8lQgzvv71zUBB077+9Wizvv73vv73vv70i77+9Yijvv73vv70IJmvvv71477+9W++/ve+/ve+/vQvvv73vv71dKBXvv70477+90oV5ej/vv70g77+977+9Q++/ve+/vXfvv73vv73vv71/77+9de+/vRwZA2/vv71IIHVx77+9T0nCtEd877+977+977+9T++/vQc+MO+/vX0s77+977+977+9Ljvvv70Nae+/vSvvv73vv73OpO+/vTVOTxTvv71777+977+9Wu+/vVVx77+9cSLvv71T77+977+9T05CNzkBzpZaBO+/ve+/vWkh77+9Qu+/vVPvv70r77+977+977+977+977+9au+/ve+/vW5J77+9d0MleDcjOu+/vRDvv702Ue+/vUPvv70sRlJlcFfvv70a77+9Eyzvv70r77+9SnRLWjxQ77+9Eu+/ve+/vQgedO+/ve+/ve+/ve+/vUrvv73vv71sFe+/vTHvv73vv70nC++/ve+/vWMa77+977+9du+/vVBLFxRKcF/vv71E77+9aFIg77+9AG7rmoxKPu+/ve+/vSTvv73vv73vv700Y++/vV5GD++/ve+/vR7vv73vv73vv73vv71wZe+/ve+/vSHvv71+XEbvv71kQmXvv70c77+9eu+/ve+/vVDvv73vv73vv71t77+9b++/ve+/vTLvv71m77+977+9ORknNu+/vSpNFO+/vQzevu+/ve+/vdmY77+9C++/ve+/vSPvv73vv70P77+9Ye+/vTjvv700BFHvv73vv73vv701XyVO77+977+9Ie+/ve+/vVgfN++/vUYT77+9xrgJCiVQQe+/vTxq77+9SgkcbBJnNO+/ve+/vWvvv73vv710XQDvv70Vbsaf77+9J++/ve+/vU4P77+9dO+/vWzvv70VYmVFHGtr77+9Vxbvv71w77+977+9Yh3vv73vv71FG3Hvv73vv71977+977+9T++/ve+/ve+/vW/vv71h77+977+977+9K1zvv73vv73vv70577+977+977+9RArvv70J77+977+9aANW77+9bDAwShbvv73vv73vv71gHAd977+977+9z6Xvv71PK2jvv73vv71977+9F++/vSLvv73vv73vv70y77+9VA43Fu+/vWPvv70hMGrvv73vv71IGWxK77+90JQw77+9R++/veC3hRtgYWXvv70277+977+9HRjvv70h77+9cFbvv708HO+/ve+/vR8zNu+/vT8mTCjvv71g77+9VRjvv70D77+9YHTqgLnvv71EB9+/TCNW77+977+977+9VEzvv73vv71kF++/ve+/vTVp77+9U++/vTVZ77+977+9FVIF77+9GAB977+9J2Dvv71qH++/ve+/ve+/vU9JQjs+77+9zqcP77+9de+/vSTvv73vv73vv73vv70L77+9K++/vVdoDEDvv73vv73vv73vv73vv73VuO+/ve+/ve+/ve+/ve+/vQzvv71v77+9BQrvv70jFO+/vXvvv70w77+9EMSeRi/vv73vv73vv73vv702au+/vW3vv73vv70Q77+9PO+/vSlIae+/vXcCJXjvv709Hu+/vRVt77+977+977+9bu+/vSUNbuiYhu+/ve+/vW0477+977+9A++/vTLvv71T77+9BO+/vWnvv73vv70QcyoYeGfvv71E77+977+9B3Hvv71l77+977+977+977+9J++/vd64L++/vS3vv70jSe+/ve+/ve+/vWLvv70H77+977+9dCLvv73vv73vv71X77+9FGfvv73vv73vv73vv705N++/ve+/vcqnXe+/ve+/vSVd77+977+977+9e1zvv73vv73vv73Slu+/ve+/ve+/vdyb5p6P77+9LO+/vQ7vv73vv71sRyNCKu+/ve+/vTEb77+9Kgvvv70iYHnMhlpNBG3vv70eV++/ve+/ve+/vWMVP++/vXR7C++/ve+/vS8ZcSDvv70ETRjvv73vv73vv73vv71p77+977+977+9Bwbvv70BK+Oxih/vv73vv73vv73vv7191I53AiVYHe+/vSDvv73vv70o77+945igDe+/vUhrF++/vVTvv70KR++/vTtxc++/vVR4ZSHvv71cL3krMe+/ve+/ve+/vW0l77+977+9Ue+/vTDvv710Z++/ve+/vWss77+9JO+/ve+/vX5yQWtWRu+/vcaI77+9Ge+/vXnvv73vv73vv71D77+9Nkbvv70VMRvvv71p77+977+9I18N77+977+977+977+977+9GwYwzYnvv73vv73vv70Q77+9cxLvv71M77+9HGAR77+977+9AN+/zaMq77+9YSQq77+9yqVZ77+977+9xKxOA107Me+/vSLvv70XAgDvv70pbQzvv73Vq++/vUrvv73vv73vv73vv73vv70r77+977+9YUZi77+977+9UO+/vUHvv71eA0ZLYe+/ve+/ve+/vX5yclTvv70D77+977+9ZSZS77+9JM2877+977+977+977+977+9Se+/vUgAHGzvv73vv70fS1ch77+977+9OHzvv70i77+9Cu+/ve+/ve+/vV3vv73vv711Jkzvv73vv71j77+9LMSm77+9Ce+/vTtaMFfvv71La++/ve+/vQs377+9IHMP77+977+9Bu+/ve+/vTkFF++/vTZj77+977+977+977+977+977+977+9yZjvv73vv73vv71wS2fvv73vv73vv70cRxNm77+977+977+9MUpWH++/vXBC77+9du+/ve+/ve+/vQ58E3Xvv71b77+977+9VHsr77+977+9AigV77+977+9Mwrvv73vv73vv73vv73vv73vv73vv70M77+977+977+9bUjvv73vv70B77+977+9eu+/vUNgVO+/ve+/ve+/vTFV77+9Wlzvv73vv70qMVvvv73vv73vv73vv73vv73Ele+/ve+/vQAAX0cKcHPvv70U77+9Xu+/vRbvv70t3oBz77+977+977+9PO+/ve+/vRXvv71QxYp077+9Wu+/vUxW77+9ae+/vVQZAu+/vd6yVTlBHlJ5XO+/vT4N77+9Ze+/vV8DQE8f77+977+977+977+9fQbvv70QAABw77+9SURBVHLvv73vv73vv71wW++/vTrvv71W77+9LS5q77+977+9LUljRhUXcEvvv71U77+9We+/vQTvv73vv711Ae+/vU9S77+9Z++/ve+/ve+/vQcRQwLvv73vv73vv71u77+977+9aH3vv70y77+9L++/ve+/vSjvv73vv70277+977+977+90JLvv70j77+9cu+/ve+/ve+/vcqJKO+/vRPvv73vv71EMEkbQu+/vSbvv713WDFBWTzvv70e77+977+9Q++/ve+/vSc877+977+977+9yKhBEO+/ve+/vQTvv70v77+9Qijvv73vv703HF3vv71B77+9PRNCX++/vUHvv73vv73vv71Z77+977+977+9JjDvv73vv73vv71fPyPvv70+77+9E1Dvv73vv73vv70F77+9AX9a77+9xrfvv73vv73PqREmC+SylwVX77+9QA4yaO+/ve+/vQjvv71WHu+/ve+/vXHvv704DO+/vRNpMu+/vXws77+977+9Fe+/vcWxWQZG77+9TH0QS++/vUPvv71TV++/ve+/ve+/ve+/vRUO77+977+977+9Gs6+wpl777+9J0Lvv70MGhNGXFTvv70F17ZPR33chO+/vTfvv70DKytj77+9Nu+/vVnvv70AbuqYiu+/vQMl77+9be+/vQbvv73vv70H77+9N385Xu+/vVThppJ6LDB477+9WdiFGzrvv73vv73vv71P77+9dO+/vSPvv70K77+9JBpFA3pkLS50bUlX77+9Ae+/vT7vv71777+9JyLvv73vv73vv73QuQXvv70M77+9Pn4zE++/ve+/vTos77+9Tu+/vSbvv73vv73vv73vv71yR++/vUQA77+9f0MuNO+/vVkG77+977+9Xe+/vc641rcDJe+/ve+/vSzvv70uXe+/vcm60JDvv70WDe+/ve+/ve+/vVVpckXvv70S77+977+9NA9XFW1COR/vv70n77+9Qjzvv71XcXbvv73vv70dexvvv70yACdVBu+/vTTvv71Ffe+/vTzvv70zWRbvv73vv70tC3FX77+9Gu+/vVjvv709fu+/vTQv77+977+977+9Iwvvv71977+9ZO+/ve+/vXxl77+977+93qfvv73vv73vv73vv73vv71+77+9f++/vWLvv70m77+977+9S++/vVoiXkXvv71z77+9au+/vRUo77+9OE0Y77+9DH7vv71r6oWVE++/ve+/ve+/vQJ3SFrvv70377+977+977+977+9OgRkHu+/ve+/vSFM0oVQ77+9R1Hvv73vv73vv71KE++/ve+/ve+/vdKuQu+/vWTvv71/Qy7vv70ZKO+/vSLvv70H77+9de+/vVwZ77+977+977+977+9Zz3vv70W77+9BkAOQO+/ve+/vSHvv70L77+977+9Cgwz77+9SO+/ve+/ve+/vTHvv73vv70KxJVJ77+9dl7vv70uHc20e++/vQBIA103NUHvv71b77+9G++/vRLvv73vv70A77+977+9Iu+/vU7vv71477+9FmoC77+977+977+9dnBmFUrvv71NewXvv73vv73vv71LAe+/vU8h77+9f++/vRgyL04t77+977+977+93pxA77+9FHXvv700Qzjvv73vv70z77+977+9V++/vQ7vv70kDe+/vTLvv71AASfvv73Oiu+/vSAoE++/ve+/vSscTm7vv70P77+9MmBA77+9ZdiF77+9eu+/vSHvv71y77+977+9dB1YUFzvv706G++/ve+/ve+/ve+/vUjvv708P++/vVvvv70b77+977+977+9fjsn77+9EHN3eu+/vWjvv73vv71YEnbvv71AKe+/ve+/ve+/ve+/vQo377+9Gyjvv70q77+977+9PTV477+9UwUjK2NP77+9D++/ve+/vQjvv73vv73vv70WUe+/ve+/vUPvv705be+/ve+/vWnvv73vv70u77+977+93qrvv71ffhQqRGNyAe+/ve+/ve+/vVlAZwnvv70977+977+9Me+/ve+/vUtr77+9CQtWxa0477+9ae+/vSHvv73vv73vv71NOe+/ve+/vWlvzagg77+9OGVxVe+/vQzvv71e77+9Fk9ULu+/vWlN77+977+977+9Pw4V77+977+9SAEWGu+/ve+/vUnvv71aRu+/vUIJEhnEkCnvv70u77+977+977+9Fe+/vXRc77+9Ze+/ve+/ve+/vQJP77+9a3Hvv73vv70Lb++/vS4Z77+9Uu+/vSUBRe+/ve+/ve+/vRkb77+9JnwRce+/vdOZQ++/ve+/ve+/vXjvv73vv71777+9CzHvv708WBjvv713PVDvv71ADjLvv70sau+/vUdP77+977+977+9W++/vVHvv73vv73vv71GQO+/vXMD77+977+977+9y5boporvv718ZXVtDu+/ve+/vT9k77+9Ze+/ve+/ve+/ve+/vVZ+TRDvv70v77+9CH3vv73vv73vv73vv703aO+/ve+/ve+/ve+/ve+/ve+/vSDvv71P77+9AAJYDu+/vWbvv71TeS3vv73vv73atM+x77+9yY3vv73vv70p77+9WBrvv71n77+9OAPvv715Bj/vv70K77+9C++/ve+/vUzvv73vv71C77+9YXnvv70O77+9Eu+/ve+/vURwY++/vXok77+9YGXMik9C77+977+9V++/ve+/vTzvv73vv73vv71b77+977+9fylRBnPvv71+77+9Ve+/vSY177+9XNm277+977+977+9CRPvv70kHe+/vc2oYW8XdXgrUO+/vS8iDiwyeO+/vXLvv73vv73vv73vv70L77+977+9ae+/vVd8FVgSHu+/vUcdPO+/vS3vv73vv73vv71/05A077+9Ie+/vVRmQXF377+9Ghxo77+977+9d1E777+9Ce+/ve+/vQ8la++/vSnvv73vv73vv73vv73vv70Hcg7vv73vv70AXu+/ve+/ve+/ve+/vVko77+94KaOVFfvv70jFT/vv73vv73vv70977+9Le+/ve+/ve+/vRjvv73vv73vv73vv73vv73vv73vv73vv71qNe+/ve+/vXTvv73MtTk977+9EUjVr++/ve+/vX0GZArvv71W77+977+9XO+/vRTvv73bgVJ8GXFg77+9Nu+/vRNsbe+/ve+/vSIWGz1YbO+/ve+/ve+/vUAJPhzvv73vv73vv73vv73vv73vv71z77+977+977+9VO+/vXvvv709T++/ve+/ve+/vQ3vv71F77+977+9Su+/ve+/vVLvv73vv71KSnXvv71V77+9YA0qWEMq3qXvv73vv71E77+977+977+9G++/vX3vv73vv73vv71u77+977+9IUDvv70KWe+/ve+/vVLvv73vv70g77+9A0EUFO+/ve+/vVnvv73vv71P77+977+9T0nvv71xbAjvv73vv70JMEYF77+977+9Lz3vv70m77+9SO+/vSrvv70677+977+9axXvv73vv70aTRR177+9SO+/vU/vv71ZeHHvv73vv70L77+977+9Hmc277+9BGXSgToWFDHvv73vv71fKu+/ve+/ve+/vShY77+977+977+977+9Glzvv706GzcW77+977+977+9BQYvLnJt77+977+977+977+9ce+/ve+/vQ0LDF7vv71VFhsTZjjvv70ECu+/vUTvv70EA++/vTZm77+9f++/vUN677+9VHjvv70fLELvv70Qxavvv70N77+9SO+/ve+/vWpt3ILvv70E77+977+9ZcSBcj7vv70T77+9be+/ve+/vUlYWQnvv70S77+977+9Au+/vQnvv71kae+/ve+/vWIEI++/vQHvv70eNCQNOO+/vWFP77+9TVjvv73vv73vv70kb++/ve+/ve+/vQA9d++/vVfvv73vv73vv71377+977+977+9ZkZCEmzvv73vv70V77+977+977+977+9be+/vQzvv73vv73vv71bXFXvv70Rd3QN77+977+977+9aXbvv70+77+977+9MmIZJO+/vTJlUO+/ve+/ve+/ve+/vXLvv70OUe+/ve+/vVvvv73vv70X77+9Hlzvv73vv70ybDbvv70C77+977+9czI2Jkw477+977+9Fe+/vUbvv70477+9YS/vv71X77+9Ku+/vX7vv71Ecxfvv71M77+977+977+977+9O0Dvv73vv73vv73jn5nvv73vv73vv71u77+977+9ek9PSCMJ77+9WkF+77+9CAjvv70UUUDvv70UUSnvv73vv70gKE3vv71IVUDvv70UUe+/vRQVEEJPSCHvv73vv71y77+977+977+977+9Pjsz77+9P3Zv77+9NndJ77+9Qu+/ve+/vTzvv73vv73vv73vv73vv73vv73vv73vv71977+977+977+9xozvv71877+9VW7vv71zQu+/vUTvv71BLiLvv71bIe+/vQLvv73Jr++/vQtpGu+/vVgaFO+/vWvvv70HckEgZu+/ve+/ve+/vVzvv70dUVw8Cu+/ve+/vd2ya++/vTXvv70B77+977+9AATvv70u77+9Hm3vv70WFu+/ve+/vciGDu+/ve+/ve+/ve+/vQTvv70Z77+977+977+977+93KBE77+9fe+/vQl977+977+9IyUt77+9Pwnvv70DHO+/ve+/ve+/ve+/ve+/vcmj77+9eT3vv73vv73vv714Ke+/ve+/ve+/vT3vv73vv71s77+9OO+/ve+/ve+/vT00He+/vSZLeCla77+977+9Ni/vv73XtR8w77+9Fu+/ve+/ve+/ve+/vXkxWu+/ve+/vS17cXnvv71q77+977+977+977+9K2UN77+9cGgzzbPvv70+Eu+/vXXvv73vv70G77+9KO+/ve+/vS0q77+977+9TtyS77+977+9R++/ve+/vX3vv73vv73vv70D77+977+9N++/ve+/vWZJ77+977+9JhpZ77+977+9Yu+/ve+/vX9E77+977+9C++/ve+/vciWGmvvv712IO+/ve+/ve+/ve+/ve+/vc6jB++/ve+/vWYVR3nvv71QEe+/vTDvv73vv719U1nvv73vv73vv73vv70R77+9eBkRVe+/vdKu3bjvv73vv709Xu+/ve+/vU12YDjvv702BG5hJAPvv71KUu+/ve+/ve+/vWrvv71X77+9LWPvv73vv73vv709OO+/vXxjUe+/ve+/vVhyfe+/vXQ2Zu+/ve+/vdO4JD8l77+977+9w6sxP++/ve+/ve+/ve+/vWXvv73vv73vv73vv73vv73vv71jY2dVDnEPW++/ve+/ve+/vXhb77+9Rnbvv73vv70CEu+/vU3ehjEDcT5t77+9IO+/vWAE77+9Byzvv70zG0YBXRDvv70z77+977+977+9Be+/vT8j77+9MisdGW/vv73MqA3vv70K77+9X1pO77+9MVEc77+977+977+9Fe+/ve+/vSoL77+977+9Xe+/ve+/vRIm77+977+977+977+977+977+9Zx3vv71u77+977+977+9SSHvv73vv73vv70ZE+iqnu+/vRN3Sjli77+977+977+9KnNK77+9Iu+/ve+/vVnJge+/vQAH77+9Au+/vWfvv73vv73vv71rN2ZaY++/ve+/vWIdX3Xvv73vv70FV++/vTbvv73vv70jZnw9OO+/ve+/ve+/vXE/GU3vv73epu+/ve+/vRXvv73vv71h77+9fO+/vRQbbknvv70Ibzfvv71377+977+9xroVyILvv71L77+9Su+/vXAE77+9WEQr77+9DFfvv70o77+9Xe+/vXbvv70q77+9YW3vv71FW8aBDtSZE3zvv73Tg0nQsRQaatakXe+/vRYrQ0fvv71IG23vv73vv70n77+977+9OO+/ve+/ve+/ve+/ve+/ve+/vTww77+977+9bWvvv73Fq++/vTR177+9JB3vv73vv73vv71lQO+/ve+/vTbvv73vv71f77+9Cg7vv73vv71yd++/vRLvv708WE8O77+977+977+9ZUbvv73vv71gPRdX77+977+9G++/ve+/vSHvv73vv73vv70LZkTVmHpqFe+/vXwN77+9Le+/ve+/ve+/vSsWTmtb77+9Be+/ve+/vVLvv71j77+9H++/vSfvv70kUcKfGu+/vSvvv709B++/vQI877+977+9Nhd277+977+9WO+/vVYt77+977+9eO+/ve+/vWNv77+9au+/ve+/vVwl77+977+977+9E2zvv73vv70iI1k1A++/ve+/vU0f5oyzaSgBU++/ve+/vW3vv71zTnHRuu+/ve+/vVgY77+9FFrvv71EIe+/ve+/ve+/vQ8D3bV/wrXvv71A77+977+9BO+/ve+/vQNeAg8U77+9zKPvv71Y77+9wr1/Eu+/ve+/vTQDD3vvv73vv70O77+9Z8Wp77+9Fnnvv73vv71ZaWrvv70k77+9Ze+/ve+/vWHvv70zKu+/vVMt77+977+9Ye+/vUYjJe+/vUHvv73vv70977+9ee+/ve+/vRhvF++/ve+/vSHvv73UuO+/ve+/vWpCLu+/vUrvv70EeVrvv71o77+92plm77+9P++/ve+/ve+/ve+/ve+/ve+/vRHvv713V++/vU7vv73vv71oJUd677+977+977+9e++/vXPvv73vv73vv73vv73vv73Lumbvv71I77+977+9I++/ve+/ve+/vUwZNmbvv73MsEZ5OlxNIGfvv73bvnbWpl0477+93Kjvv71i77+977+9Zl3vv73vv703Szrvv71aViHvv70877+9JFHvv70L77+9Su+/vUnvv70+Mu+/vQsbBO+/vRhyMO+/vTLKiu+/ve+/ve+/vQXvv73vv71t77+977+9X1Tvv73vv73vv70677+9YmPvv71l77+977+9PFnvv70d77+9Se+/vTXvv73Mr++/vVdxalkrfe+/ve+/vT/vv71PMu+/ve+/vX7vv70/77+977+9eno577+977+977+9A++/vQ1j1Lrvv70GMe+/ve+/vW7vv73vv70mc3rvv71CTixt77+9Lu+/vR8YbO+/ve+/vWgV77+9Ne+/ve+/vVPKjXlg77+977+977+9Hjfvv71O77+977+9aGrvv73vv73vv73vv71h1aRb77+9G++/ve+/vTB5XNmXY++/ve+/ve+/vRLvv73vv71adO+/vVg6bu+/ve+/ve+/ve+/vRfvv73Juu+/ve+/vRbvv73vv73vv73vv73vv73vv70sSgXsmZ5Gfjbvv70O77+9XO+/ve+/ve+/vRsxGm/vv71j77+9I++/ve+/ve+/vXgQ154pBEnHte+/ve+/ve+/ve+/ve+/ve+/ve+/vUbvv711de+/vTFk77+9Ne+/vRHvv71uTi5r77+977+9JV3vv73vv73vv73vv73Nlu+/vXguUkVGE++/vW3vv71wde+/vSrvv73vv712GiPvv71lQSvvv73vv70eL++/ve+/ve+/vS7vv709SRUq77+9Xu+/ve+/vWnvv73akQTvv71jW++/ve+/ve+/ve+/ve+/vR/vv71tYu+/vSPvv71Lyo0LUi0ZB++/ve+/ve+/ve+/ve+/vXIdAjrvv73vv71UYRM13onvv70377+9DFvvv71N77+9fO+/ve+/vVvvv73vv73vv70RY++/ve+/vQpqPO+/ve+/ve+/ve+/vW4mW++/vWjvv73vv73vv71CNVzvv70977+9E1rvv73igLUH77+977+977+977+92JB2Mu+/vRZh77+9LUrvv70lMWYZ77+9eGXvv73vv73vv73vv70ZZmvvv70RVT/vv70V77+977+977+977+9WGAPIQsaZe+/vSzvv71vYO+/vSPvv71fCu+/vVbvv70ucHXvv70MIu+/vUzvv73vv70p77+9RUhpEu+/ve+/vU/itLIW77+9Ys6w77+9MiPvv73vv70CW++/ve+/vU0HLu+/ve+/vT7vv73vv71R77+977+9W++/vVPvv71aHgw2cHfvv71777+9TFneiO+/ve+/ve+/ve+/vQDvv70saFxc77+977+977+977+9LR91GBw9Yu+/vVPvv70DHu+/vT/vv73vv73vv71Z77+9aGdD77+977+977+9ZXRa77+9fe+/ve+/vVweXRHvv73vv705biXvv71xLkwXR++/vUXvv70I77+9RRrvv71ky73vv73vv73vv70pCkMXRe+/ve+/vTTvv73vv73luag0b++/vRvvv70+K++/ve+/ve+/vciY77+9NO+/ve+/vUTvv71tG++/ve+/ve+/vRbvv71v26fvv70qahRZOu+/ve+/vcO/Y++/ve+/vV7vv71C44iIa1Tvv715Kljvv71xLXtzfu+/vXoOcgXvv71nH++/vSTvv73vv71jHkd677+9Oe+/ve+/vc+D77+9ek4qbWXvv70977+96rSbS++/vWZTJ++/vTjvv719Ae+/ve+/vUJM77+9xbDvv70a77+9LlAibe+/ve+/vRtqOmku77+977+9H++/vXFf77+977+977+9L3tmMu+/vRpl77+977+95pSyVmZY77+977+9Gu+/vSvvv73vv73vv70IaU0i77+977+9U0cn77+9Lu+/ve+/vTXvv711Me+/ve+/ve+/ve+/vRVAF++/vVvvv70OJlnvv73vv71c77+977+9yKvvv71ybu6fvEM+fF/vv73vv703XO+/vSxhcXw4KO+/ve+/vVg577+977+9Zu+/ve+/ve+/vTx777+977+977+977+9Re+/vQVEQWdl77+977+9d1vvv715cmFEDe+/vTvvv71S77+977+977+9Ze+/vSNUFCd4KlzLie+/ve+/vRzvv73vv70w77+977+9LmERNSRBH++/ve+/vQsa77+977+977+9JmLvv73vv70LK++/vW03FnFb77+9ZO+/vW1877+9Cjnvv717CR/vv71277+9zp7vv70gx5V077+977+977+9L++/ve+/ve+/vWbvv73vv71577+9Te+/ve+/ve+/vTnvv73vv73vv70dck1z77+95Y6ZL3zvv73Hi++/ve+/ve+/ve+/vWfvv73vv73TpDTvv73vv73vv70Td++/ve+/ve+/vVUx77+977+9IM6U77+9FErvv70Ja1MWNBh477+9Q++/ve+/veGAsu+/vUbvv73vv73vv71+UmstdO+/vSbvv73vv73vv73vv73vv73vv70YXx3vv71U77+9fShY77+977+9R++/ve+/vTDvv71yS++/vQp077+977+9dGY177+9CnDvv73vv71r77+977+9U++/vciL77+9CW3vv73vv73vv73vv71RdVbvv73vv71eyIVEYm8677+9Le+/ve+/vVhpGe+/ve+/ve+/ve+/ve+/vT3vv71V77+977+9Dw9K77+9Vl3vv71m77+9cXRJJ0nvv73vv71FXe+/ve+/vTBlOO+/vQDvv71Gc++/ve+/ve+/ve+/vTjvv71s77+977+9Hu+/vVnvv70n77+977+9Re+/vWvvv71V77+977+9dO+/ve+/vVzvv73vv70rFjZnHHzvv73vv73vv71VaQ9n77+9Lu+/vc6c77+977+977+977+9HO+/vQps77+9Ly1KA2oS77+977+9ZdSYU3Rm77+9VBdM77+977+9XAHenO+/vTLvv73vv73vv70VSS/vv71F77+9eC/vv71nde+/vU3vv70pzYbvv73vv73vv71i77+9L2fvv70wZe+/vVPvv700WhLvv73vv73vv73vv73vv73vv71p77+9GO+/vVk7NXLvv73vv73vv73MlO+/vQLvv71L77+9Yu+/vcOU77+9Hu+/vUrvv71iY3Pvv73vv73vv70Jb++/ve+/vWTvv71oIO+/vRULGU1kWe+/ve+/vVU977+977+977+92opM8YGcmSdD77+9xrzvv73vv70G77+9Cgfvv73Dg3Xvv71Ke0ZZQD3vv718NDnvv73vv71H77+977+977+9CDTvv73vv70177+9Ie+/vdGEEXXvv73vv73vv73vv70HKQ/vv70EG3jvv70577+9Je+/vTV5PV7GtT3TuO+/vXEJde+/vSTvv73vv70u77+9a++/ve+/vVlQ77+977+9dg7vv716c++/ve+/ve+/ve+/ve+/ve+/ve+/ve+/vRTvv73vv71X77+9YhVU77+9WDbvv73vv73vv73vv73vv73vv70HV++/vXF/77+977+9Re+/vT3vv711TS7vv70977+977+9ew/vv73vv73vv71a77+9awXvv71PDFLvv73vv70oZe+/vUcw77+9KHTvv71U77+977+9Eu+/vRbvv73Oke+/vR1F77+9Te+/vRXvv70+77+9Yu+/vVzvv71lYQrvv73vv70CPX/vv73vv70C77+9bgDvv73vv73vv71477+9RO+/vW7vv71V77+9PDPvv73vv73vv73vv70I77+977+977+9JO+/vW8777+977+977+9ce+/vWQaS++/ve+/vSDvv73vv71r77+977+9Iu+/vXpp77+9GgNIQ++/ve+/ve+/ve+/vSZWJO+/ve+/vVXvv73RmHs2ZCp2Kjbvv73vv71JXu+/ve+/veOVsu+/ve+/ve+/vV/vv701Ru+/ve+/ve+/vT0z77+977+9ci3vv70E67ihbzrvv73vv71RI++/vXgn77+9Y03vv71UPe+/ve+/vXPMiHMO77+9QUXvv73vv71Afgjvv707CR8J77+977+977+9Ce+/ve+/vWwj77+9UyXvv70p77+977+977+977+9Ee+/vWAD77+977+977+9z6pW77+9A++/vUcrOe+/ve+/ve+/vSnvv70Ya++/vS4aLQnvv73vv70x77+977+9SAVh77+9zI/vv71o77+977+9SDlWJO+/vSQ077+9Ujnvv71cW++/ve+/ve+/vSgJTe+/vSPvv70177+977+9VDMR77+977+9Iu+/vSR9LO+/ve+/ve+/ve+/vVrvv71kP++/vV3vv70U77+9cAwabe+/vVvvv70nNe+/ve+/ve+/vWPvv73vv71kIe+/vVQ4VM++77+977+9Pe+/vXzvv70x77+9KzPvv73vv71dHe+/ve+/vVfvv708fO+/ve+/vW9UQu+/vSVL77+9Pn7euRvvv700LGPvv70lTnch77+977+9Re+/ve+/ve+/ve+/ve+/ve+/vR5j77+9RdS477+9ejULHWHvv73dsic2Se+/vWvvv73vv73vv71P77+977+9KO+/vWtx77+977+9OwkfakHvv71N77+9xKgtS3LDpGkE77+9Jkx/KiHvv70pU3VuEO+/ve+/vUksDQod77+977+977+9PjDvv73vv71C77+977+9Hcui77+9HBbvv70rdu+/ve+/vee3vGYU77+9Ne+/ve+/vUXvv73vv71wPj0zFe+/vR5U77+977+9FxRIGmPvv71af1JBau+/vUcrMGnvv73vv73vv73vv70cF2/vv711E++/ve+/vRVeRe+/vTvvv71777+977+9Hu+/vQDvv70AU20x77+977+977+9WzXvv70eDu+/ve+/vRTvv73vv71J3o3vv73vv71p77+9WiLvv71MR++/vU7vv70l77+9I++/vTpS77+977+9FT3vv71QdO+/vVPvv702M++/vRLvv73MlO+/vdSUIe+/vUtI77+9QO+/vUHvv71D77+977+977+977+977+9Pe+/vWnvv71azq/vv71AUhNRdO+/ve+/ve+/vU3vv70j77+977+926Q3EAvvv71DzJFQTe+/ve+/vT7vv73vv70x77+977+977+9Ce+/ve+/ve+/vUbvv73vv73vv73UlGF577+977+9Ie+/vV7vv71MGXpz77+977+9U++/vcKG77+977+90Lg2Ue+/vSUp77+977+977+9dk3WjRknN++/vU7vv73vv7142bjvv73vv70DA++/vRzvv70477+9IkdoTCbvv73vv71q77+977+977+977+977+9XO+/vWzvv73vv71Jde+/ve+/ve+/vWlb77+9XNeYU++/vR4vY++/vRh8b++/vS5wb++/ve+/vRfvv71xQxp977+977+9Hu+/vSTvv73bmM23Ou+/ve+/ve+/ve+/vTnvv71Q77+977+92bYI77+977+977+977+977+9FxDvv73vv73biBdMMHXvv71/77+9bg5wBnBJ77+9KAtlWe+/ve+/ve+/ve+/vc+halUOc3ZY77+9We+/vTpnLO+/vSrvv73vv73vv73vv71LTu+/vT0man8+77+977+9Xu+/ve+/ve+/vR5E77+9TmLvv73vv71caAfvv71t77+9eu+/ve+/vTnvv71c77+9Ye+/ve+/vSJb77+9XxsFdO+/ve+/vcyG77+9au+/vUZg77+9UO+/ve+/vdakbExt77+977+9BXTvv73vv70377+977+9H++/ve+/vQly0aLvv71Y77+9wrNv77+977+9Sgvvv73vv71HL++/ve+/vQfvv73vv73vv73vv70VfO+/ve+/ve+/vXvvv71vF++/vS3vv73vv714I15KR++/vV7vv73vv70CBe+/vXQb77+9OQdY77+9dnFB77+9eu+/ve+/ve+/ve+/ve+/ve+/ve+/ve+/ve+/vWcG77+9OAd5LV7vv71N77+906jvv73vv71ZYAvvv73vv70R77+9K2Xvv70RO0lNKu+/vTNXEe+/ve+/vTll77+9Ik7vv73vv73vv70zCXzvv710My4x77+9QwPvv70c77+977+977+9D++/ve+/vVzvv73vv70Bd++/vU5hWe+/ve+/vQJ777+977+9Bxs577+977+9Re+/vTnvv70977+9S9icce+/ve+/vUU2Z++/ve+/vQXvv71/Rcu5LTAJ77+977+977+9Yu+/ve+/ve+/vVTvv71DQO+/ve+/ve+/vWPvv73vv73vv73vv70K2r0vZ++/vXA1F2/vv70v77+977+9TO+/ve+/vTcFDe+/vUhOJu+/ve+/vWgraPSJsr/vv70XCCpOKt28VcqpXu+/ve+/vXc277+977+977+9JV1877+977+9VmTvv73vv71O77+977+9Q++/ve+/vVHvv70tI3Pvv73vv73vv71hbu+/ve+/vXrvv7165YihDllN77+9Ze+/ve+/vQnvv73vv70h77+977+9UE1FZe+/vSNlU++/ve+/ve+/vR3vv73vv73Lqg9Z77+9CO+/ve+/vSTvv71q353vv73vv73vv71ralZx77+9K++/ve+/vTPvv73vv70ZVCxFPO+/ve+/vVRkYxwsS++/vSXvv73vv71+Pu+/ve+/ve+/ve+/ve+/veuLrRzvv73vv70MSkwiOwJq77+9D++/ve+/ve+/vV9J77+977+977+977+977+977+9JURDQU5EbFPvv70t77+9yI7vv7111qnvv73vv71q77+977+9ce+/ve+/ve+/vSDvv71j77+9BkdE77+977+977+977+977+9MTbvv70bZ++/ve+/vSFa77+9Iu+/vTvJru+/ve+/vTPvv71777+9JO+/ve+/vSnvv73vv73vv73vv71X77+9BO+/ve+/vSvvv73vv71YeX5hJUlV77+9EHcv77+9bDEqTWnvv71S77+9IGAcaXbvv70F77+9f++/vSpYVDjvv73vv70J77+9Ke+/vVPvv71aJlvvv70ZL++/vVAdPzbvv73vv73vv73umZRICk3vv704ZXLvv73vv71iIe+/ve+/vWgvbO+/ve+/ve+/vRjvv73vv71v77+9Pw0077+9chzvv73vv71iae+/vce/77+9CTrvv70O77+9dQY477+9fSHvv73vv73vv71YJWdW77+977+9JT3vv71t77+977+977+9J++/vUwZ77+977+9VO+/vQN/CzZRNe+/ve+/vTx777+977+977+977+977+977+9Oe+/ve+/ve+/vTMpMSkEchbvv73vv71U77+977+9RGPvv73vv71HUu+/vS3vv73vv70nZQkXMgN5Nu+/vXwG77+9Lu+/ve+/ve+/ve+/ve+/vUBh77+9RCBn77+9O2tjbe+/ve+/vXsJ34RM77+977+977+9BVUX77+977+9Oe+/vQJ7CO+/ve+/ve+/vUPvv71l77+9QVYXeSRY77+9I++/vTrvv71yBu+/vSlD77+9YjUGLmxN77+9dQzvv70U3ojvv73vv71677+9SABndO+/vWhRTWjvv71177+9ce+/vUTvv70077+9bO+/ve+/ve+/vTjvv719AT/vv71Yx4Hvv70AOV3vv73vv73vv715fO+/ve+/vXBx77+9DQjvv73DlO+/ve+/ve+/vXbvv71tUnhm77+977+9He+/vThX77+977+9XO+/vXEe77+977+977+9M++/vTpK77+977+977+977+9Ae+/ve+/vRzdvysl77+90rAL77+977+9Nu+/ve+/ve+/vUpqfhbvv705P++/vWvvv70k77+977+977+977+9de+/vX9CZO+/ve+/vSPvv71bRO+/vUfvv73vv71bBe+/vSDvv71P77+977+9Bu+/vT1fTO+/ve+/ve+/ve+/vS/vv70u77+9bO+/vRPvv73vv73vv70VAVPvv71K3ZXvv71EXnYgOXXvv73vv71SOO+/vRHvv73ioInvv70RUO+/vcWn77+977+9eitPH++/vWPvv708C++/vXoH77+977+977+9B++/ve+/vSHvv71J77+9GO+/ve+/vcuuPu+/vUrvv71R77+977+977+9Mn5Y77+977+977+977+977+9U0pbae+/vdqxCCpZXe+/vUATBzoH77+9ae+/vRzvv73vv73vv71NfVPvv71eWStn77+9NxXvv71ibk1uD0zvv73Squ+/vWQR77+977+9Kh5L77+9WwPvv73vv71z77+977+977+977+977+9ce+/ve+/vTjvv70n77+9Gu+/vSYy77+977+977+9Z++/ve+/ve+/vQpkFEPvv73vv70ucVrvv70C77+977+9EyDvv73vv70v77+9b3rvv71RIe+/vRnImVnvv71yI++/ve+/vWvvv710IT/vv73vv71AXd2a77+977+9eO+/ve+/ve+/vQtNNn5TZu+/ve+/vRvvv71ZO38P77+977+9VO+/vWbvv70U77+9MVXvv73vv71kCe+/vRxB77+9bFjvv73vv70A77+9y7tn77+9ZGjvv73vv73vv73vv71iGTfvv73vv71LW3k7Ue+/ve+/vSEOIyfvv71mNO+/vQB+QjMZ77+9elw177+9Sn8FCu+/vTNn77+9be+/vVBPDzrvv73vv73vv73vv71YHC/vv73vv73vv71N77+9CO+/ve+/vRI+77+977+9R1rvv70477+92bNDIF/vv73vv71wzqbvv73vv73vv73HvjHvv73vv71c77+9Je+/vVkG77+977+9R++/vVY1Lu+/vXFZOe+/vWfvv70o77+9WgzHnDTvv73vv73vv73vv73vv70877+977+9Cu+/ve+/ve+/ve+/ve+/vdSc77+9GO+/ve+/vQwo77+9A++/ve+/vS/vv71SKe+/ve+/vQvlhaoe77+977+9WXk377+9ybzvv73vv73vv708FxEZ77+977+9G++/vXciOO+/vXHvv71L77+9WUgTf++/vUp077+977+977+9e1bvv73vv73vv73vv70W77+9Cizvv73vv73vv713J++/vTDvv70b77+977+977+9N++/vXLvv71jFu+/vSAg77+977+9Tu+/ve+/ve+/vQXvv73vv71+xYpZ77+9eDZS77+9R++/vXHvv71AI++/vXLvv73vv71q77+9eF8nbyd8LE9677+9M++/vcyD77+977+9NO+/vRNU77+904jvv70C77+9X++/ve+/vVpz77+9aAjvv70LWkUW77+94LWJKmdX77+977+977+977+9Thrvv73vv70xU01mMe+/vTsv77+977+9AQdDM++/ve+/ve+/ve+/ve+/vSrvv71hE1Ru77+9W86m77+9E++/ve+/ve+/vSrvv70Echbas3bvv70T77+9Gynvv71/RSvvv73vv705SFrvv73vv73vv73vv71iNkzvv73vv73vv73vv71577+977+9SS9X77+9zKRaTuOVslhFFQ3vv71+xYo+77+9YO+/ve+/ve+/vXoWOe+/vQbvv73vv70WSO+/vSdDNe+/vREKcw5yBe+/ve+/vTnvv70P77+9Fmw7PTtC77+977+9Ncm477+977+9au+/vTbvv71d77+9Oe+/vVDvv73vv73vv73vv70WclxJ77+9UQJ7U++/vTTvv70JV++/ve+/vXbvv73vv71vNGokfu+/vTnvv73vv70m77+9FU8vFxbvv73vv70T77+9AiV877+9dO+/vTzOuO+/vXfvv73vv70y77+9R++/vVHvv70VB++/ve+/ve+/vStAdVXvv73vv71mH++/vU7vv73vv73vv70F77+9T2A477+9bUpm77+9IO+/ve+/ve+/ve+/ve+/vT7vv73vv73vv71/xq/vv70PJu+/vV0jPgXvv70Z77+9TR8T77+9de+/ve+/vT/vv70bCDzvv70FXe+/ve+/ve+/vSjvv70jD++/vVxYJO+/ve+/vUZ077+977+977+977+9VsOkN++/ve+/ve+/vR0f77+977+9OO+/vVfvv70zfwXvv71y77+9Ju+/ve+/ve+/vVIO77+977+9AO+/ve+/vWJU77+9Ke+/ve+/vTRn77+9NmHvv73vv70377+9G++/vVLvv71qfu+/vXBb77+977+9V9y977+9Ze+/vSRU77+977+9Zu+/ve+/ve+/vTpm77+9Yu+/vRjvv73vv73WhmXvv709VMOL77+9Cu+/vSrvv73vv73Ciu+/vdSZE++/vSQl77+9YTXvv73vv70q77+977+9JO+/vTrvv73vv70p77+977+977+9SFYX77+9dAHvv73vv73vv73vv70FFF3vv73Yte+/vRPMmjnvv712He+/ve+/vUfvv73vv73vv73Xiu+/vQsVeENjfV7vv73vv70577+9fCM2Ke+/ve+/ve+/vTMNN++/vTRZEu+/ve+/vQhyTEknb++/vUvvv71x77+9XB4J77+9UVHvv73vv73vv71m77+977+9el8777+977+9fO+/ve+/vSspRiVbUjPvv73vv71E77+9TFnvv73vv73vv73vv73dsO+/vQ9THu+/ve+/ve+/vTwf77+9Gu+/vQjvv73vv73vv71y77+977+9Pu+/vUJFIe+/ve+/ve+/vSTvv70IfjTvv71X77+9ae+/ve+/vV7Fq++/ve+/ve+/ve+/ve+/ve+/ve+/vXJGL3pSE++/ve+/ve+/vSDvv71w77+977+9D++/vXfvv73vv73vv70+77+9Auy+gUbvv70dbO+/vTBvDxdX77+9ewbvv70LV++/ve+/ve+/ve+/ve+/vSLvv73vv73vv73vv71oREFnde+/ve+/vduJUiZbY++/ve+/vRwWUe+/vSUqWEUV77+977+9Ge+/vUPvv71i77+977+9Bu+/vW7vv73vv70/dzPvv71l77+9Jifvv73vv70M77+977+977+9Z++/vVjvv73vv71/77+977+977+9Tu+/ve+/vdiAH++/ve+/ve+/vUzvv71MYu+/ve+/ve+/vde+77+9W++/vcOeEATvv71xAx1A0ITvv710YRjvv73vv70DUnPvv73vv71jUO+/ve+/vTFETu+/ve+/vUMeBu+/ve+/ve+/ve+/vVfRswLvv73vv73vv70faGnvv71SelQM77+977+9Ce+/ve+/ve+/vXd077+9xIpy77+9ce+/vVPvv73vv73Pt++/vQ1w77+977+9alk377+9xLnvv70b77+977+9KgM5M++/vUzvv70177+9M++/vWp+XO+/vTrvv73vv70s77+93JThnoEmFETvv70aaCbvv71J77+977+9a++/vSkq77+9SHpY77+9cu+/ve+/vU3vv71nbVTvv73vv73vv73vv71c77+977+9Nw/vv70KU++/ve+/ve+/ve+/ve+/ve+/vRdR0Invv71MBngVXSTvv73vv70477+977+977+9F++/vQFOau+/vQPvv71J77+90JJuNF3gvr5mZu+/vSMGG86Q77+9fzdR77+9bu+/vQjvv70uFtW2Q++/vU/vv73vv73vv71z77+977+9KmZBJ++/vUlF7Zq3Fe+/vVbvv73vv73vv70577+977+9TkoT2Zx1MO+/vRI377+9XmPvv73vv70NfdOMMVVl77+977+9Ue+/vX9u77+9Bl7vv73vv71zbe+/vQzvv71WfHvvv70i77+977+9SO+/vUHvv71kFVXvv73vv73vv71nVdqzzYHvv71b77+9MlPvv70/1L9PThfvv73vv71777+9du+/vXdI77+977+977+9Rk4V77+9O++/vWxRdUoqaU3vv73vv73vv73Mt0U477+9PO+/ve+/ve+/ve+/vVRzY++/vTQOcA7vv73vv70P77+977+977+977+96p2O77+9C++/ve+/vXwDZxTvv73vv73vv70iVe+/ve+/vXvvv71RI++/vS3vv73vv71bypHvv71EEu+/vQnvv73vv73vv71FRQ58N8q177+977+977+9b++/vVfvv73vv73vv71hSinvv71X77+9f3Lvv73vv73jooRfcjDvv71XN++/vRgj77+9Ju+/ve+/vSIIDO+/ve+/vUzvv70uNu+/ve+/ve+/vWzvv73vv73vv70C77+9NDPvv70fZe+/ve+/vQfvv73vv70t77+977+977+977+977+9Mu+/vVNrLe+/ve+/ve+/vWbvv73vv70w77+9AO+/ve+/vXvvv71H3bnvv71wxZzvv70K6IqAa1EKS++/ve+/vWfvv70B77+9Pu+/vRRtTu+/vQfvv73vv73vv70WFTZl77+977+9Hu+/ve+/ve+/ve+/vSvvv71077+977+977+977+9MlPvv71NGSc977+977+977+9Ju+/vUTvv70WEe+/ve+/vQYa77+977+9Iu+/vVMuGiwp77+9J++/vWzvv706MAs677+9Ul5W77+9Pe+/ve+/vUBP77+9SlTvv73vv73vv73vv73vv73vv70KI++/ve+/vWTvv73vv70d77+9STYV77+9O++/vde777+9finvv71vXe+/vV3vv73vv71ofyPvv70m77+977+9ZmxS77+9SSV577+9XgDvv70M1bAp77+9MEDvv70+77+9U++/ve+/vQVXP19277+9cW/vv71777+9Qn5zDe+/ve+/ve+/vRQrbydKee+/ve+/vUXvv71m77+977+977+9aTcpXe+/ve+/vXgZ77+9LXHvv70FS0Hvv73vv71R77+977+91Zjvv73vv70u77+977+9Y++/vS/vv73vv71Z77+9CHFa77+9Qlbvv71d77+977+9zK/vv73vv70M77+9Px3vv71P77+977+977+9Fu+/veqemUZGYDwy77+9Fu+/ve+/vRVYRO+/ve+/ve+/vRZu77+9Xu+/vS7vv70477+9Ne+/vXPvv73vv71w77+9K++/ve+/vdiw77+9ZjIi77+9cU3vv70i77+9XO+/vTHvv71aS++/ve+/vQrvv73vv70377+9XO+/vTXvv73vv73vv70QN++/vS/vv70k77+977+9Ne+/ve+/ve+/vX1T77+9BO+/vcurP++/ve+/vWl/R2ASDww277+9Au+/vQDvv73vv71h77+9UVXvv715Xe+/ve+/vT1vOQ/vv73vv70177+977+977+9P++/vUYF54K877+9D++/vc2Fa8+UMUPvv713RO+/ve+/ve+/ve+/ve+/vRbvv70ZeO+/vT3vv71a77+9UWbvv73vv730h4KkHzDvv71+77+9TTNzb0wY6IKA77+9Xe+/vRPvv73vv71D77+977+9fzfvv71r77+977+9Yu+/vTcl77+9dO+/vQclKO+/vUbvv73vv71lVUvvv73vv73vv713EO+/ve+/vRvvv71XEu+/ve+/vRPvv73vv70477+9fyph77+9dAdvHe+/veChhu+/ve+/vQoq77+9DjZya++/vRR+We+/vQoBeD1RagRlHg/vv73vv70F77+977+9Me+/vVbvv73vv73vv71kREHvv717Ze+/ve+/vWzvv73vv71R77+9dCjvv73vv71y77+9Cdeh77+977+977+977+977+9YBrvv70v77+977+977+9CwMW77+977+9b++/ve+/vUgV77+9Ul7vv73vv71v77+977+977+977+9OW3vv71C77+9dAzvv73vv73vv71l77+93LoHSgF877+977+9Ge+/vW7vv71n77+9Alvvv70O77+9PO+/vWDvv716b1nvv707dO+/vTEl77+977+9cmbvv73vv73HsmQJ77+9d++/vULvv70FXu+/ve+/ve+/ve+/ve+/vVYGchbvv73vv73vv73vv71gWA8pPT/vv73vv70j77+9OO+/vW02Ce+/ve+/vTRL77+9Sjnvv71byo3vv73vv71eV++/vXEHchYu77+977+977+9eBnvv71U77+95a+T77+977+9b++/vWrvv70I1rIq77+977+9ShZd77+92ZLvv73bpW0c77+977+9ZE3vv73Nj++/ve+/vdK9DRdi77+9Je+/vW/vv73vv71M77+977+977+9cAfvv71mSQcDBcKP77+9Zu+/vWvvv70y1IRq4pqe77+977+9JO+/vV9W77+9ae+/vVbvv709XO+/vTHvv71J77+9BO+/vTYs77+9Ku+/ve+/ve+/vTvvv717Bxvvv73vv70a77+9167vv70L77+9fnLvv73vv70VPe+/vdiQdu+/vVlTd++/ve+/ve+/vVgTJSQNRF/vv73vv73vv73vv73vv71H77+9XHp0FARIbzTvv71777+977+977+977+977+977+9T++/ve+/vWPvv73YpmYQJB3vv73vv70J77+9ByZI77+977+9MO+/ve+/ve+/ve+/vVLbhO+/vRkrTg3vv70XP3Hvv70xQe+/vXpJ77+9aTsm77+9Awjvv71Ad++/vU3vv70S77+977+9Oi0tThsX77+9Je+/vdKjY++/ve+/ve+/vWIqU++/ve+/ve+/ve+/vVMj77+977+9TO+/vV3vv70IY3JlfiFd77+977+9bO+/ve+/vcSd77+9V3J277+9Ru+/vQzvv73vv71I77+9Hu+/vSYN77+977+977+977+9Iu+/ve+/vWAj77+977+9EcqZeTZSyb3vv73vv70677+977+977+9eCnvv70R77+977+9Gu+/ve+/vSPvv70D77+977+977+9Eu+/vTPvv70eYu+/vSXvv71FVHHvv70q77+9dBHvv73vv70fbGRm77+977+9M1c477+9NxQaT++/vWbvv73vv71F77+9cwhrJu+/vTpDfH3DvlxQ77+977+977+9G++/vTDvv70SJe+/ve+/vTfvv73vv70N77+977+9Qe+/vV1k77+9A3QV77+9YNamXO+/ve+/vRXbs3bvv73vv71H77+977+977+9JSnIgu+/vWDvv73vv73vv73bsCrvv70q77+977+9T++/ve+/vTRZQkrvv73vv70w77+977+9Iu+/ve+/vQvvv73vv703Y++/vTjvv70cezrvv73vv73vv71+Ge+/vW9b77+977+9e++/vTRPRmDvv71w77+977+9Fu+/vXDvv70R77+977+9eBkn77+977+9clxJJ8eUdNKZ77+977+9Iu+/vWVTxola77+9GNSb77+9zLRG77+9ae+/vdKjWO+/ve+/vXc677+977+977+9ae+/vR/vv73vv73vv73vv71V77+977+9K++/ve+/ve+/vRHZg++/vQM077+9Ku+/ve+/ve+/ve+/ve+/ve+/vQVM77+977+9de+/vSsRBe+/ve+/ve+/ve+/ve+/ve+/vXY3Zu+/vTnvv70cVxQyBW5R77+977+977+9LO+/vQfvv71pMhfvv73vv73vv70pKSzvv73vv71GdUJaRe+/ve+/vSrvv71WbjouK1/vv73vv71+77+9fW7vv73vv71IDu+/ve+/vX/vv70F77+9Fe+/ve+/vduJ77+9bu+/vTEn77+977+9CO+/vUUpEO+/vWNuP20XVe+/ve+/vUlGMGvvv71t77+9MtO377+9c++/ve+/vXvvv70JdO+/vSzvv73vv71q77+9cmDvv73vv71r77+9QxN977+977+9XO+/vQLvv73vv70JeA5OGCBPbzYz77+977+977+977+977+9dibvv73ejWNu77+977+977+9xYjvv71j77+9e2jvv70UUXLvv73vv73vv70b77+977+977+9Os+9XEdtX++/ve+/ve+/vXTvv71WZWHvv73vv73vv73vv73vv73vv71oBR92e++/vSxPcWdgEl/vv70D77+9Re+/vWDvv70iR++/ve+/vS7vv73vv71X35h2Me+/vRbvv71E77+9EMqZWe+/vVjvv73vv71Z77+9V++/vVxR77+977+977+977+977+977+977+977+9flJ077+977+9Ku+/ve+/ve+/ve+/vWRB77+9NWMn77+977+9eGLvv73vv71IBe+/ve+/vQcGG++/vXPTu++/vSdezrXvv73TmWHvv71xae+/vWrvv71Z77+977+9OTIqH1dNLO+/ve+/ve+/vXzvv73vv70LCu+/ve+/vSXvv73Zku+/vd6c77+9J++/ve+/vdiQdu+/ve+/vSXvv73vv71MRDXvv71yBFVzEVB077+977+977+977+9Be+/ve+/ve+/vUZL77+9YO+/ve+/vSbvv73vv73vv73vv71/Gu+/vTfvv73vv70iR++/ve+/vXxt3LsFf++/vXjvv71EUu+/ve+/vSJ/HBwmUO+/ve+/vTLvv73vv71P77+977+977+9JO+/vXYE77+977+9Me+/vVRr77+9I21dSO+/ve+/ve+/vUDvv73vv71G77+977+977+9SxNeMu+/vUDvv73vv70eM++/vV9h77+9cHnNqiLvv73JhC4VBe+/vUbvv71577+977+9AO+/ve+/vXIt77+9eHrvv70F77+9QO+/ve+/vWnvv70L77+9BO+/ve+/vRvvv73vv73vv71y77+977+9cw4vRivvv70y77+90bjvv71p77+9GO+/vVkbZ++/ve+/ve+/ve+/ve+/vQHvv73vv73vv70o77+9GBzvv70z77+977+977+977+977+977+9au+/ve+/ve+/vSbvv70+77+9RRwfIe+/ve+/vUrvv70v77+9DlwnVlpI77+9LO+/vdKgUHp0FO+/vRcSZDtkEisnEO+/vTsi77+9C0wd77+9Z++/vTDvv71Q77+977+9Ye+/ve+/vXfvv73vv70nBO+/vWbvv71v77+9Vnhe77+9Ke+/ve+/veG2ve+/ve+/ve+/vV9dVF84SHrvv73vv73vv70fDhfvv71FX3dQdnzvv73vv71z77+9VDEceU5tNnPvv70j77+9yLliO++/vT8L77+9zLEFCCgW77+977+977+977+977+977+977+977+977+977+977+977+977+977+9G2/vv73vv73vv719eBEQ77+9V++/ve+/ve+/vUM177+9KTbvv71MWe+/ve+/ve+/ve+/ve+/ve+/vWAs77+9Uu+/vR7vv73vv71IbxcLHUHvv70ITDICUu+/vTMu77+9Jn3vv73vv73vv73vv73vv708Z++/vduk77+9I++/ve+/vWAtLwbvv73vv73vv71uBRd077+9Y1Azc++/vSXvv73vv717OWHvv73vv73vv70q77+9Hy/vv706R++/vTt5Klzvv73vv70LXO+/vTnvv73vv70qG++/vWM/77+9y60N77+9UyXvv70NWu+/vS0l77+977+9eSXvv73vv73vv71Q77+9Me+/vXDvv70M77+9S++/ve+/ve+/vUs6We+/vXLvv73vv70i77+9OSttGTtXdu+/ve+/vTfvv70rOe+/vWwzD++/ve+/vUfvv73vv73bk++/ve+/vRgXdu+/vR1zLO+/ve+/ve+/vUkj77+977+9I8yC77+977+977+9Wjjvv73XgUnvv70wCe+/ve+/vR5k77+9CO+/vUfvv73vv71c77+9M++/ve+/ve+/ve+/ve+/vXVwYe+/ve+/vVHvv73vv71tVe+/vW1pBRgR77+91oUkVBPvv70377+977+9T++/ve+/ve+/ve+/ve+/vSzvv73vv71Rb05yd++/vRJq77+9KVbvv70977+977+9Pu+/ve+/ve+/vVo477+977+977+977+9He+/ve+/vW5BHz7vv70aGx7coe+/vXdc77+977+9Mu+/vRHvv70A77+977+9We+/vU8O77+9KRB777+9Ru+/vT5v77+96KWR77+9ae+/vem+qe+/ve+/ve+/ve+/vcil77+977+977+977+9ybITYnPvv70ocO+/vXN7b++/vS7vv73vv71s77+9Ae+/vWLvv70177+9YQ7vv71Lc1sacm9k77+9TO+/ve+/ve+/vSLvv704KD8l77+977+977+9MiNjcGBJzrzvv70beO+/vU3vv73vv71jCCbvv73vv71mM++/ve+/ve+/vUbvv70cQO+/ve+/ve+/vTvvv71tIUc8EWZqW++/vSktSe+/vU1g77+977+9Zgce77+9LO+/vX8777+977+9LGjvv71U77+977+9Ezfvv73vv71A77+977+977+977+9M++/vRZm77+9PULvv70577+9S++/vWEXc++/ve+/vSkj77+977+977+9Re+/ve+/ve+/vRHvv70u77+977+9CToKAArvv73vv73vv73vv70/Ce+/vSlNRu+/vTALKlndjO+/ve+/vQ82Lu+/vT9tI++/ve+/vTkd77+9OO+/vXIjJ1fvv73Qme+/vTPvv70W77+9yo3vv71Mee+/ve+/ve+/vRfvv73vv73vv70D77+9Ju+/ve+/vXbvv73vv73vv71fKwo6Ye+/vUzvv73vv73vv701YmPvv70rFu+/vRtsYu+/vTVC77+9NUnvv70p77+9We+/ve+/ve+/ve+/ve+/vcmWODfvv71OY++/vULvv71+77+977+9QwPvv73vv71077+977+9Ej5WFeivn++/vVRRIWfvv71RxboCbe+/vX9CQF8cLxvvv73vv73vv73vv71uOQfvv70CRDXvv70HBhtY77+977+9ElHvv73vv71A77+977+977+9y54+77+977+977+977+9SCXvv705M++/vdauKu+/ve+/vSvvv73JmsmY77+977+9XV3vv70L77+977+9cx7tip3vv70b77+9UC5nOO+/vXUB77+9J0vvv71p77+9cnvvv71SSu+/vSwv77+9yrnvv71rJlfvv73mrYc346WjWHUlQe+/ve+/ve+/vQ/vv71O77+977+977+977+977+91Zg077+977+9fWYGXRPvv71cC3vvv71wLUoRfe+/vUHvv70D77+977+977+9Pu+/vQ1K77+9MlLvv708TO+/ve+/ve+/vUsOde+/vRvvv73vv70z77+9Ix8Z77+977+9fO+/ve+/ve+/vTwJ77+9aO+/vUPvv70c77+977+9XTzvv73vv73vv73vv70TGHzvv71D77+977+9QWovDTDvv70Hc++/ve+/ve+/ve+/vXzvv70p77+9dzfvv73vv73vv73vv73vv70a77+9Lu+/vTjvv71s77+9Mggi77+9B++/vQbvv73vv71N5ri/Du+/vTfvv73vv70hb++/ve+/vW1jXisGHu+/ve+/ve+/vR4aD9yW77+977+977+9W++/ve+/ve+/vR3vv73vv71uD++/vTdl77+977+9d++/vX3vv71y77+9Pu+/vS3vv73vv73vv70NS++/vXLvv71T77+9MQZzZu+/vUxZEu+/ve+/ve+/vSoT77+9WTjvv73vv73vv70577+9CGfvv73vv71uFO+/ve+/vRMr77+9cG8377+9TXrvv73vv73vv71PZVnvv73vv70J5a2sTHg5f++/vULvv73vv71CVDNxcGHvv73vv71Q77+9be+/vT3vv73vv73vv73vv73vv73vv71IDVd1z6Bf77+977+9PO+/vWXvv709WO+/ve+/ve+/ve+/vQxOKcez77+9alIh77+977+9JtOR77+977+9Oe+/vcyw77+9Cu+/ve+/ve+/vX1T77+9GzFM77+9TwPvv73vv70r77+9Me+/vVvvv71h77+977+9S++/vXEOcu+/vStARO+/vTly77+9fmNE77+9fTwV77+977+977+9de+/ve+/ve+/ve+/ve+/vVvvv73vv71sFe+/ve+/vci6WFQZ77+9LVnvv73vv73vv71e77+977+9W++/vd+nzpLvv73vv73Ni1jvv71277+9VyE+77+9EHM8MO+/ve+/ve+/ve+/vUbvv71sfO+/vRnvv70oKV3iqp7Rs0lOLm1j77+9LR/vv71RBk1977+977+9ee+/vUzvv715Be+/vXYRX2rvv73vv707Ye+/vQcmQATvv73vv70JXO+/vSXvv73vv73vv71J77+9IQ9K77+977+977+9UO+/vVzvv70zQ++/vS0W77+9L++/vXzvv70fGe+/ve+/ve+/ve+/vXbvv70u77+9Bu+/vR3vv73SsyTvv73vv71S77+9Se+/vXoQ77+977+9O++/vXwp77+977+9OUvvv71Zw6Zc77+977+9bO+/ve+/vQ9xx5nvv70qFe+/ve+/ve+/vRzvv70g77+9Lu+/vXJWFe+/vSog77+9Ou+/vXxq77+9QxcPB++/ve+/vWwg0IXvv73vv73vv71W77+9de+/ve+/ve+/ve+/ve+/vRNe77+9TWnvv70t77+9xLfvv73Kui7vv71G77+977+9Yl/vv70gLRknZe+/vSAeUe+/ve+/vQrvv70VG++/ve+/ve+/vTEq77+9TO+/ve+/ve+/ve+/vRzvv73vv73vv73vv71V77+977+9XFDvv73vv73Lu2dx77+977+9PWlJO++/ve+/ve+/vQPvv73vv70j77+92rrvv71sIe+/vT1UPDPvv73Fj++/vXZiFlTvv70a77+9ZVXvv71j77+9eu+/vQrvv71y77+977+9B++/ve+/vXJW77+977+9ayTourM2Vu+/ve+/ve+/vRTvv73vv73vv714We+/vR3vv70477+977+9dgwz77+977+9SO+/vTZn77+9fVQZ77+9V++/vRAgG++/vVPvv73vv70U77+977+9Djbvv71A77+9O++/ve+/vTfvv73vv73vv70y77+9cXY077+977+9Z++/vUPvv73vv73vv73vv73vv71AFjRO2rwnbe+/vXzvv73vv73vv73vv70P77+90Lnvv713BksSJTzvv73vv73vv71R77+9c0fvv73vv71R77+9du+/vS1x77+977+9D++/ve+/vW0XVmxGZ++/ve+/vT7vv71s77+977+977+935Rhee+/vU3vv71JYVx777+9ECTvv71+Je+/ve+/vQsJQu+/vTsZfMSDEu+/ve+/ve+/vQbvv71LXe6Yne+/vVfvv71NUe+/vXs877+9He+/ve+/vSHvv73vv70tBu+/vRnvv71a77+9dyPvv73vv709HiLvv73vv73vv70/XBDvv71ab++/ve+/vVfDpu+/vWjvv73vv70UZu+/vT1deA5O77+9AO+/ve+/vRzvv73vv73vv73vv73vv73vv71h77+977+9enDvv70P77+9U++/vTvvv73vv73vv73vlLbvv71277+977+977+977+9He+/vWnvv70L77+977+9dxoPBu+/vXk8WFc0L++/vTtr77+977+9fExjN3vvv73lhY7vv73vv70u77+977+977+9Bg4cQRzvv73vv73vv73vv73vv71i77+9c2Pvv70KXu+/ve+/ve+/ve+/ve+/vT14MVLvv73vv71V77+9KREUflbvv73vv71977+977+9OO+/vRx31L9P77+977+9MjTvv73vv73vv73vv70h77+9Xlbvv73vv73vv73vv70777+977+977+9VO+/vcaI77+977+9D1Ie77+977+9UCDvv73vv71577+977+977+977+977+9Le+/ve+/vcmL77+977+9GRzvv70m77+9dGHvv73vv718P+eAke+/vXBJCuePiFbXm++/ve+/ve+/vXo1L01577+977+977+9VxFSzaxIeu+/vX5ZC++/vQTvv73OkO+/ve+/vQDvv71FQUfvv70FTizvv73vv70EXwfvv73XrkRC54KOee+/vWfvv70877+977+977+9Ae+/vQ9THu+/vRsoDkzvv70E77+9G++/vVZgKRTvv70E77+977+977+9NU0I77+9Mu+/vciXA2Q277+96biy77+977+977+9Vxrvv71eMO+/ve+/vQ7vv70x77+977+9Lsq/HzLvv70q77+9V++/vU8LK1sk77+935Nn77+977+9HO+/ve+/vU3vv73vv73vv73vv70A77+9L1ByaGzvv73vv73vv71LWnRFaB5X77+9Jizvv71+aQXvv71fBVEfNmFE77+9filO77+977+9YUzvv71qUe+/vVDvv71dPu+/vXhmW1VIAO+/vSfvv73vv73vv73vv71eVu+/vU1e77+9IBzvv70wbw8+Ke+/ve+/ve+/vWR077+977+9KQ9dWRs/77+9GNaUfjlDd2Hvv73vv71kS++/vTvvv70ncXpZCxFV77+977+977+9YQ0w77+9x5sFDe+/ve+/vXBx77+9HFrvv71277+9W++/ve+/vT/vv703G2bvv73vv73vv73vv73vv73vv71Y77+977+9cFHvv70677+9W++/ve+/vW4eY2bvv70k77+9XF7vv70h77+9DzZhEVTvv71Cce+/vR5177+977+9Nu+/vXguUsWz77+9fO+/vVwXDe+/vQ7vv71eKGvvv73vv70Y77+9XStn77+9byrvv73vv71obydPBGvvv73vv70Z77+977+977+977+9Re+/ve+/vUnvv70277+9H2zvv73vv70V77+977+9P++/vWXvv70S77+977+9Ez7vv715d3vvv70m77+977+9B++/vQsYVM2c77+977+9yJnvv73vv715LlxFQjNxV++/vRLvv73vv71OaRIXde+/vR7Vtu+/ve+/ve+/ve+/vcaI77+9XO+/ve+/ve+/vXvvv71n77+977+977+9RO+/ve+/ve+/vRDvv73vv71EEe+/vXw+N++/ve+/ve+/ve+/vRvvv73vv70x77+9dRbvv70v77+977+9MSfvv73vv73vv70w77+9WWlE77+977+977+977+9BO+/ve+/ve+/vRPvv71F77+977+977+9UnxBcVbvv73vv73vv719EO+/ve+/vULvv73vv73vv73vv71q77+9EGgu77+9QO+/vXdk77+9c++/vQlP77+9RDbvv73IuVLvv73vv71pKk8PFe+/vWLLtO+/ve+/ve+/ve+/vTDvv73vv70v77+977+977+9b++/ve+/ve+/vT3Cjyvvv70a77+977+9Ni0VQe+/vcacIu+/ve+/ve+/vU1ZVu+/ve+/vSzvv73vv70hCnoR77+9Ie+/vRMtCzrvv73vv70q77+9CAJP77+9eg1Z77+9eDVSQe+/vSnvv70r77+9cm7vv73vv71C77+9IO+/ve+/ve+/vRVJL++/vQvvv73vv70s77+9ZMeIeO+/ve+/vRzvv71QHSnvv73vv71L77+9Cnbvv71F77+9GV8A77+9Uu+/ve+/ve+/ve+/vXVv77+977+9eT9RwrPvv71q77+977+977+977+977+977+977+977+9cu+/vSsw77+915YE77+9S++/ve+/ve+/ve+/vTnvv71Z77+9EduI77+977+9I0rvv73vv70t77+9TO+/vWLvv73vv71l77+977+9O++/ve+/vR8a77+9bkXvv71vH++/vUvvv70l77+977+9Pu+/ve+/vWbvv70X1avvv73vv70I7rewJu+/vWxJFFFMXe+/vTttFO+/vd6h77+9Hu+/vRvvv73vv73vv73vv70277+977+9Ju+/vS7vv70b77+977+9PSVE77+9ce+/vSIb2bVf77+9Gu+/ve+/vWp/cVrvv73vv70KXO+/vVLvv71PCRN877+9Oe+/ve+/ve+/vWXfjHYiMu+/vQzvv71s77+977+9PjXvv73vv73vv70777+9Igjvv70Iwq1FP++/ve+/ve+/vUJEAhPvv70gclXvv73vv73vv70CNF7vv71n77+9PBfvv73vv73vv71Hy5lVEwY5QO+/vUDvv71f77+977+977+977+977+977+9cO+/ve+/ve+/ve+/ve+/vW8a77+977+9Vu+/vW7vv73vv71b77+9Uu+/ve+/ve+/vVLvv73vv71gTCXvv71vD++/ve+/vQhz77+90q7vv73ss4Xvv70XUm3vv70SWO+/vSZrHE3vv70bOmfvv71y77+977+977+9Yj4eDe+/ve+/ve+/vUlL77+977+9Kns7Bu+/ve+/ve+/ve+/vWhBNRfvv70377+9LO+/ve+/ve+/vVfvv73vv71bAlMM77+9Me+/vSRYCGjvv71VM++/vWrZk2t7Zu+/ve+/vWgFTjHvv73vv71L77+977+977+977+9Pe+/ve+/ve+/ve+/ve+/ve+/vSDvv71/Ae+/vQJw77+9f9OY77+977+9Zw/vv73vv70R77+9QO+/vSgT77+92JIuHgg2cO+/ve+/ve+/ve+/vWnXqO+/vUJOF++/vXXvv73vv70677+9Ru+/ve+/vR/vv70uIO+/vUnvv71Q77+977+9CO+/vQBfcO+/ve+/vWLvv73vv71oJu+/vXfvv73vv73Ht2B877+9bO+/ve+/ve+/ve+/vQ/vv73vv701N++/vV9T77+977+9B++/vX3vv700Ze+/vUbvv70dD3jvv73vv73vv73vv70gW3Hvv700MksjJe+/ve+/ve+/ve+/vXPvv70I77+977+9Gu+/ve+/vRbvv73vv73vv70TIu+/ve+/vUh777+9Vjjvv71xDzHvv71QCFAWM++/vSjvv73vv70XDO+/vVfvv73vv71276Gx77+977+9z7kOHO+/vWVQ77+9MO+/ve+/vR7vv70vXe+/vQkE77+977+9Yu+/vWHPuFkvR++/ve+/vcir77+9PDx7YAlUaTTvv71JOu+/vVY277+9a++/vWom77+977+9Nu+/ve+/vXYbQO+/vQTvv73vv71JL0d477+977+9au+/vXPvv71gI3vYg++/vUp577+977+977+9He+/vVHvv71v77+9Vx/vv73vv73auAcl77+977+9dEvvv71ya++/vT9Mfu+/ve+/vXEf77+9Rsq477+9dgUn77+9Lu+/vS5q77+91bBkTB99SO+/vVHvv73Fne+/vWZi77+977+9VGvvv718XO+/ve+/vWEWNe+/ve+/vXbilbJcUO+/ve+/vXVpFyvvv71e77+977+9VDPvv73vv70xCzoeKe+/vc+XCn7qrpZacyrvv70Mde+/ve+/vSYZEe+/ve+/vUcV77+9OGbTvnzvv73vv71VFO+/vT/vv73vv73vv71RG++/ve+/ve+/vU3vv71c77+977+9ewbvv73vv71hRO+/vUFC77+9cEjvv73fvz/rmoMk77+977+9Vu+/vT57OEbvv70q77+977+9NO+/vXjvv73vv73vv71AE8eUdHJZ77+9bkUH77+9Q++/vXFT77+9cu+/vUEm77+977+9fVnvv71l77+977+9U2nvv73vv71TfV7vv70Ade+/vSnvv70x77+9IO+/ve+/ve+/ve+/vUc6Qe+/vQ9q77+9QEknOTVE77+977+9yaXvv73vv73vv73vv71F77+9H++/vSvvv716fO+/vSAP17UDQe+/vcy9E++/vSFM77+9FO+/vU4s77+977+9Pwg677+9Gg/vv70H77+9Pe+/vS/vv73vv73vv70p77+977+9SXxg77+977+9exotI++/vXJ277+9VnPvv71F77+9be+/vXHvv71LE3vvv73vv71OJ2nvv73vv73vv70677+9DSLvv73vv70t77+9Ku+/ve+/vSgX77+9MO+/ve+/vd6c77+977+9eBlP77+977+977+9Yw/vv73StiLvv73vv70hcBtA77+9RB4baO+/vQBn77+977+977+9VxHvv71kTtm877+977+977+977+977+9bO+/ve+/ve+/vUXvv73vv73vv73vv73vv70+Wu+/ve+/vRV177+977+9AO+/vSHmuKrvv71DHg3vv71jLu+/vV/vv70WL++/vXTvv70wG++/vSJH77+9yZZ477+9KO+/ve+/ve+/ve+/vSfvv71177+977+9KO+/vUBXAO+/ve+/ve+/vTnvv70rZyPvv71+77+9FO+/vUtS77+9Nu+/vWnvv70m77+9Zu+/vTHvv70aY++/vTXvv71fzpDvv71F77+977+977+9AG5p77+9ZiEWflPvv73vv71u77+9N++/vdO577+9Pn9tVO+/vVnvv73vv70x77+9Eu+/vWANJ++/vTrvv73vv71gE++/vVnvv71BEzYe77+9XAfvv73vv73vv73vv73vv73vv73vv70Z1q0PMj3vv71sE0dt2pdXYu+/ve+/ve+/ve+/vRI6N9StMO+/vS3vv73vv73NvV3vv73vv73vv70Cxo3vv73vv73vv70iBg1U77+977+9XO+/ve+/vcKZ77+9dH0v77+9Uyvvv73vv73vv70uEy1nVe+/vX8xRO+/vSvvv70b2rPvv73vv73vv73vv70i77+9Iu+/vcuADmBOZ++/vVcs77+9M++/ve+/ve+/ve+/vWYtPTLvv71+77+977+977+9fkbvv71dPhzvv711I1p077+9MzPvv70F77+977+9KUvvv71FA++/ve+/vRTvv70u77+9IO+/vWrYtO+/ve+/vVXvv73vv73vv73vv73Hvu+/veecg3bvv71L77+9Xu+/ve+/vSJM77+977+9MQk6KgJVcu+/ve+/vUIzS04XMAk6blPvv70P77+9Hu+/ve+/vRTvv70u77+977+977+977+977+9fAN777+977+9HRRd77+9XO+/vQc5RHbvv73vv73vv71177+9B0RVGe+/ve+/veOuhiXvv73YuidZLT/vv73vv73vv73vv73vv73vv71p77+9bu+/vT0q77+9Nu+/vRzvv73vv73vv71v77+9GlLvv73vv73vv73vv73vv70377+9XF/vv73vv70g77+9AHgn77+9Y0nvv73vv70577+9CHs777+9DO+/vT/vv71Y77+9yb7vv71c77+9Oe+/vTnvv70o77+977+9We+/vSTvv71EVO+/vd6c77+9YO+/vUw4Zybvv73vv70G77+9VcqYXe+/vdeaTlHvv70tKXhNWUrvv70sFXIa77+9NggX77+9CO+/vTLvv70W77+9Ju+/ve+/ve+/vTvvv71t77+977+9MCXvv73RnO+/ve+/vUVGbXnvv71i77+977+9du+/vS9p556BJsOEThTvv70gb++/vW3vv71977+9MgLvv73vv73vv707Z++/vdOq77+977+9xrjvv70yU++/vWPvv73vv73vv70577+9XBTvv73vv71Z77+9Gu+/vVpS77+977+977+9H1d6dDBPAHzvv71nD++/vXDvv70vGMS1X++/ve+/vVt877+977+9PWzvv73Ile+/vVEnVO+/vc2lRe+/vWrvv70T77+977+977+9Te+/ve+/ve+/vTwZ77+977+9bO+/vXcTTu+/vU3vv70377+9T1hZ77+977+9de+/vSsMX++/ve+/ve+/ve+/ve+/ve+/vV0dek4Y77+977+977+9TO+/vUzvv71FFyVf77+977+9PyVM77+9dTtaShzvv70e77+9H2UzZO+/vR0T77+9fVXvv70Pb++/vWjvv73vv710H0fvv70677+9O++/ve+/ve+/ve+/ve+/ve+/vVvvv71t77+977+977+977+977+9Ge+/vT5p77+9e++/ve+/ve+/vRrvv70u77+90pTvv73vv71wNe+/vS/vv71r77+9clPvv73vv73vv73vv71NUUUT77+9EU7vv70tUU7vv73vv73vv71477+977+9P++/vU/vv70rK++/ve+/vWo1dzcs77+9W++/vWIVdO+/ve+/vTzvv70L77+977+9Gu+/vXZZ77+977+9DO+/vW3vv73Xvhkv77+9UC3vv71yBu+/ve+/ve+/vVDvv73vv717Hu+/ve+/ve+/vSvvv70+77+9G2zvv73vv70NB++/vTdlKO+/vTJk77+977+977+9B1Xvv71z77+9VRXvv73vv70U77+9aATvv73vv73vv71NE++/vWfvv73vv73vv73vv70I77+977+977+977+9BO+/ve+/vSvvv71wde+/vUwW2IPvv71Y77+977+9d0tb77+9bu+/vRcjFXzvv73vv73vv71hG++/ve+/vU/vv71I77+977+9GXdqbW3vv73vv70577+9C++/vTMn77+9be+/vTTvv73vv71ZNe+/vXXvv73vv71beTRUZ3Q1fu+/vWzvv70/77+977+977+9P++/vTbvv71rQu+/vURALkg6be+/vVRQcmjvv73vv71T77+9SA4N16IU77+977+9ehAtGu+/ve+/vUjvv715F3paREsJ77+9Ku+/vTEJLSHvv71YMe+/ve+/vXNJ77+9LDsx77+977+9Fu+/ve+/ve+/ve+/vVMg77+977+977+977+977+9BTRlfwvvv70F77+9STfvv73vv73vv73vv71O77+9MwLvv717URMi77+9T++/ve+/ve+/ve+/ve+/ve+/ve+/vVdp77+977+977+977+977+9Qnnvv71rMO+/ve+/ve+/vSjvv73vv73vv71477+9Oe+/ve+/vUbvv70v77+9FUYxC++/vVnvv73vv71/TH7vv73vv73vv73vv73vv71QKFEuZ3gqVO+/vXvvv73vv71a3Yrvv73vv73vv73Yk2NG77+977+9Te+/vQRn77+9Le+/ve+/ve+/ve+/vUdn77+977+9KRUt77+93rbvv73vv71mYi9H77+9ee+/vRDvv73vv71d77+9Ne+/vRlafO+/ve+/vW5bcu+/ve+/ve+/veejlXzvv73vv70w77+90KLvv71u77+977+9wqrvv73vv73Cte+/ve+/vc6pZe+/ve+/vVLvv70+YVXvv70H77+977+9Te+/vUrvv73vv73vv706F1fvv70t77+9GO+/vRXvv71477+977+977+977+977+9KO+/ve+/vW3vv70+77+9fO+/ve+/veK4n++/vQXaq++/ve+/vTZjFVXvv70PNu+/vRTvv73vv712cV/vv71xJu+/vUk477+977+9Me+/vVHvv73vv73vv719Ae+/vVXvv73mlrpl77+977+9KO+/ve+/vVQa77+977+977+9Qu+/ve+/vXBvd1FT77+977+9U2vvv73vv73vv70EQA7vv71PDO+/vXhDH++/vVLvv73vv71zLjbvv71ebUwcEu+/vRoI77+977+9JWJv77+9GXjvv71N77+9GRfvv71/77+977+977+9bi/vv70jGe+/vXd9O++/vSrvv73vv70R77+977+9EDBp77+9W3Zk77+9dgjvv73vv70U77+977+977+9DUVm0rcj77+977+9Nn3vv714PyMXERl8NB/vv70o77+9Rsyg77+9AnAf77+9KHTvv73vv73No1xA77+977+977+9Cu+/ve+/vS7vv71uSu+/ve+/ve+/vcKNZ1bvv73vv71Wcu+/vX8jNlHlnYTvv73vv73vv70dMXIo77+977+9XO+/vTvvv73vv73vv73vv73vv71p77+977+9HWXvv70X77+977+9cO+/ve+/ve+/vT1B77+977+9Fi5J77+977+977+9Zn5R77+977+9yZY4dwbvv73vv73vv71/Mu+/ve+/vSEu77+9Xu+/ve+/vTjvv71e77+9Thrvv71dKDY577+977+977+977+9AmBXJO+/vWjvv73vv71X77+977+977+977+9Ke+/vdG277+9dxM+77+9cO+/ve+/ve+/ve+/ve+/ve+/ve+/vSVG77+9d1dLWu+/ve+/vXfvv71x77+977+977+9be+/vU8r1pHvv73vv73Enmvvv70u77+9TFnvv71R77+977+96pSb77+9Ej5OKm3vv71INe+/ve+/ve+/ve+/vSpt77+9dH85Vs6XXe+/vRzvv73vv73vv70p77+9OO+/ve+/ve+/vVvvv71BHe+/ve+/ve+/vXbvv73vv700J++/ve+/vRl577+977+977+9KzZme03vv70T77+9He+/ve+/vWnvv73vv71H77+9Te+/ve+/vXxLD++/ve+/vWly77+9Eh1X77+9ae+/ve+/ve+/vVwhdWzvv71TaO+/ve+/vTdfFGPZse+/ve+/vWjvv73Xlh0f77+9Zwtt77+977+977+977+977+9Hwnvv70A77+977+9Re+/vRvvv71JYu+/vUXvv73vv71EPmPvv71vLiIvO2jvv73vv70CNSEiOTRqLhrvv73vv73vv70BI++/vRF977+9zqYz77+9SSzvv73vv71p77+977+977+9XO+/ve+/vRp+77+977+9Ie+/vTkH77+9yY86Pu+/vW3vv70x3qc1Yyfvv73KpFTvv71DPO+/vWRGaNqsLnJe77+977+977+977+977+977+977+977+9I++/ve+/vVPvv71RBO+/ve+/vc27c++/ve+/ve+/vXpz77+977+9N++/ve+/vcqM77+977+977+977+9HO+/vWlf77+977+9Yu+/vRHvv70LJe+/vduT77+9SjoQBR3vv73vv71jc++/vUHvv71iNRps77+977+9NO+/ve+/vXFu77+977+9Hmvvv73vv73vv73vv70177+9Te+/vXfvv73vv71pR++/vS/vv71a77+977+977+9TBkn77+9eWvvv71h77+977+9V++/vVPvv73vv71rRe+/vQV+35fvv71EPO+/vXQz77+977+9ce+/vWAjZe+/vSzvv73vv717eChY77+9b2jvv70O77+9w6rvv71S07Dvv71yQkkH77+9UiXvv73vv71tKTZB5Yyy77+977+91pc/DHp+W++/vSHvv73vv706ee+/ve+/vSDvv73vv70/77+977+9BCbvv71K77+9Ne+/ve+/vR3vv70n77+977+977+9bWPvv73vv73vv70E77+9du+/ve+/vTfvv73Lju+/vTLvv71ubO+/vTIT77+9Ku+/ve+/vWcDWe+/vWF8Cu+/vW1KZG7vv73vv73vv73vv71h77+9F07vv73vv70VRWvvv71KzZPvv71yb++/ve+/ve+/vSgCXdeV77+977+9ZO+/vT47Te+/ve+/ve+/ve+/ve+/vVdATe+/vXRdX0bvv701fu+/ve+/ve+/ve+/ve+/vSTvv73vv70G77+9dHsP77+977+977+977+9ZwHvv70HJO+/vX7vv71z77+977+9cEBTH19277+90Z7vv71zVu+/vQLvv71677+9AWdM77+9Wu+/vWgnNRPvv73vv71V0Knvv71pMifvv73vv712JXs677+977+977+9Uu+/ve+/vQ/vv73vv70Xbe+/vSzvv71AH++/vVvvv71y77+9Il5CS++/ve+/vWXdu3F277+9Ou+/ve+/ve+/ve+/vR3vv70M77+9LO+/ve+/vTHvv73vv70U77+977+9zIJGdu+/ve+/ve+/vSMpJHXvv73vv73vv71r77+977+9Gm8Wcu+/vWlN77+9WG8HDzbvv73vv73vv70z77+9yb/vv73vv73vv73vv70b77+977+9Gu+/ve+/vSsk77+9S++/vR9sGlUF77+91YBd77+977+977+977+9FnFW77+977+9Ru+/ve+/vXtl77+9VBfvv73vv70sSe+/ve+/ve+/ve+/vRzvv73vv73vv714GW0ZO++/ve+/ve+/ve+/vVjvv73vv73vv73vv73vv73vv70vbizZku+/ve+/vSrvv73vv71R77+977+9LX1T77+977+9Gu+/vXTQknHvv73vv73vv73vv73vv73Qi++/vSk777+9HO+/ve+/vUDvv71N77+977+977+977+9Q1fvv71dau+/ve+/vQ9R77+977+9AO+/vU1DS++/ve+/ve+/ve+/ve+/ve+/vR9V77+9Ke+/ve+/vUol2oRSaQDvv71677+9Le+/ve+/ve+/vTjvv71+77+977+977+977+977+9DnQAee+/vXIv77+9ZO+/vWvvv71X77+977+9ABNyFm3vv70zNO+/ve+/vQ/vv73vv70A77+9VVZazqwi77+9bwdyZe+/ve+/ve+/ve+/vVF7aR7Ylu+/vSzvv716Be+/ve+/vXnvv70377+977+9F1Pvv71K77+9Lu+/vUvvv71i77+9c0nvv71sTljvv70377+9D2tc77+977+9du+/vSkN77+9YDonVRN377+9Nu+/vWg577+977+9Bjjvv73vv73vv70dDUvvv73SiGEQUU3vv71777+9Fxrvv71C77+9VXHvv73ptZXvv70STl3vv70XezsGeSfvv73vv73vv715LFDvv71sW++/ve+/ve+/vVZwY++/vQrvv71C77+9YDo1bSPvv73vv73vv73vv71m77+977+9JO+/ve+/ve+/vXAV77+977+977+977+9BwYbae+/vTgI77+9ZO+/vSxJ77+9G++/vWjvv73vv73vv73vv73vv7123YTvv70mcnnvv70sHg7vv73vv71A77+9XFDvv73vv70rCu+/vVcWUe+/vQIT6429U9GB77+977+9WhB077+9c++/ve+/vWpz77+9fe+/vQM8FGzvv71uMC7vv73vv71wbe+/ve+/ve+/vWnvv73vv704XsqS77+9b++/ve+/ve+/vR8HW3A+Ju+/vX3vv73vv70R77+977+977+91p9UGO+/ve+/vWvvv70kTe+/ve+/vUXvv73vv73vv713au+/ve+/ve+/vXMq77+977+9S++/ve+/ve+/vWMSQW7vv71c77+977+977+977+9Ye+/vdeW77+9M++/ve+/vT7vv709fyTvv70LAu+/ve+/vXZhEe+/vWtydc6977+9b0/ol63vv70QW2pDVwXvv73vv73vv73vv712UQVK77+9Cdmn77+9dH3vv71B77+977+9JkTvv73vv73vv70J77+977+9ZVQg2aZlaO+/ve+/ve+/vXfvv71377+91J5h77+9Fe+/ve+/vSlw77+9Wjjvv70/QURNx5lUCe+/vWbiqolYQmbvv70XEu+/vRtNau+/ve+/ve+/vQdDUe+/vRvvv71WcO+/ve+/ve+/vQjvv71n77+977+977+977+9ZAnvv70uYBZ1Q++/vT82UO+/vQ3vv70r77+9Xe+/vXLvv73vv70HOWLvv73vv73vv73vv70sF1fvv71FLUxo77+977+9au+/vVEN77+977+977+977+9SO+/vWLvv73Tlu+/vXNxVe+/vT3vv71L77+9Pu+/ve+/vXlPzq7YuM2J77+977+9TO+/vWsSfx5o77+9B++/ve+/ve+/vXsVKydt3oPvv70m77+9UO+/vTvUm++/vRzvv73vv73vv70l77+9eO+/vVDvv73vv71Fdz/vv705B++/vSDvv73vv70/MT/vv73vv70m77+977+9QzXvv71yZk4ra2EgZzbonLfvv73vv71dIwLvv73vv70nSzjvv719ARoC77+977+9be+/vSc/77+9Uu+/vWsR77+9Mu+/vWfvv707Iu+/ve+/ve+/ve+/ve+/vUpq77+977+977+9H1YZUXRL77+9Qu+/vQ3vv73Ivu+/ve+/vXZVYO+/vVFP3pTvv73vv73vv73vv71P77+9Y++/vVJH77+9WO+/vQVB77+977+977+977+977+9I++/vUfvv70IV92m77+977+977+9HGkWYHDvv73vv73vv71OVQw+77+977+9RhPvv73vv73vv73vv73vv70yW++/vVfvv70177+977+9CF1777+9AO+/vWnvv73vv71377+9FQfvv71UEHnvv71J77+9HRvvv70X77+977+9N1lw77+977+977+91Zjvv71OMO+/ve+/vTnvv71L77+91JVBVu+/vRd0Te+/ve+/ve+/ve+/vUnSuu+/vQcf77+9cu+/vV96ae+/vc2I77+9Fu+/ve+/vV5ZZW1Q77+9RO+/vUvvv70+BOC9glbvv73vv70SL0Qr77+977+977+9DO+/vSx877+977+977+9c++/vWrOr1jPvu+/vUE0Qe+/vQvvv70A77+9cEHvv70G77+977+977+9cGbvv70COhUb77+977+9Oinvv73vv73Mtkfvv70w77+9Ie+/ve+/ve+/vXccTDci77+977+977+977+9C++/vc6e77+9IBkkTu+/ve+/vSHvv70X77+977+977+9PNiITVQ577+9QO+/vTXvv73vv70n77+977+977+977+977+9Oe+/ve+/ve+/vQsq77+9I++/vXAn77+977+977+977+977+9azZf77+977+977+977+9VGbZojwR77+9ZV3vv73vv703Szrvv71k77+977+9UO+/vQHvv73vv73vv73vv73vv71+1qVdTO+/vcaZZe+/vXrvv73vv73vv71QLXs5B++/vTXvv71YHC/vv73vv73dje+/vT3vv73vv73vv73vv70w77+9I++/ve+/vcaXWOm4ohwtPTHvv71m77+9LU3vv73Fg++/ve+/vUhh77+9yZFYakXvv70NL++/ve+/vRbvv73vv73vv71AcmrYpmcRHTrvv73vv71cfg/vv73ilLXvv73vv71777+9C1JR77+977+9Ie+/vUzvv73mj7rvv73vv73vv73vv70MOcSfAsaTFS3vv73vv73vv71n77+9IBMg77+9yYXvv70i77+9W0tTFu+/vV7vv73vv73vv73vv71r77+977+977+977+9USXvv73vv73vv71s77+935sP77+977+9D0gY77+905bvv71wVe+/vX9GK3gyVMuX77+9DmFSde+/vW1OATjvv73vv73vv73vv70K77+977+977+9TiHvv73vv71Z5ZuKWEbvv73vv73vv71b77+9Jxvvv73vv70XVO+/vWfvv70t77+9JV3vv73vv73SugN5IVrvv73vv73vv73vv73vv73vv71vCu+/vQQmcduU77+977+977+9Oe+/ve+/vXFf77+9F14eR3pt77+9DE0Z77+9fBvvv73vv73vv73vv73vv73vv73vv71877+977+9J++/vcSf77+9De+/ve+/vRVt77+977+9Au+/ve+/vU/vv73qnpnvv71S77+9fMyZ77+977+977+9JO+/ve+/ve+/ve+/vQLvv71D77+9Je+/vXd877+9bEjvv714LlJF77+9Je+/vRdd77+9PBpsIO+/ve+/vVxW77+977+9Qwos77+9WzXvv71VEy4x77+9U++/vRrvv71p77+9T++/vVAMJAk6x5R0cnPvv70UY++/ve+/vd+277+9cWXvv73vv73vv71UGBEBU++/ve+/ve+/vXkOTu+/ve+/ve+/vT7vv73vv73vv717TFcFem/vv70ReSUPbu+/vV5j17jvv70rAHdJMCrvv73vv73IqHMi77+9SO+/veyMte+/vSlA77+977+9SO+/vQoCVxXvv70ge++/ve+/vVsnZe+/ve+/ve+/ve+/vXQtGHbvv73vv73vv71Y77+977+977+977+977+9Fx3vv70oJzjvv70dGu+/vWNV77+977+9DW5Wbu+/ve+/ve+/ve+/ve+/vTBL77+9Le+/ve+/vTt/77+9XzMc77+977+977+977+977+9ZUbvv71HK++/ve+/vUPvv70R77+977+9TXtTZ++/ve+/ve+/ve+/vUt4OFTHqe+/ve+/ve+/ve+/vRXvv73vv73vv71J1Ljvv73vv71D77+977+9Lu+/vXTvv70m77+977+977+9BX1MQu+/vU/vv708FO+/vWc/77+9AA1j77+9BsKq77+9M++/vRfvv70i77+977+9L++/vW9t77+9Me+/vdSy77+9HO+/ve+/vWN577+977+9H++/vW/vv70vZ++/vSUw77+977+9Lu+/ve+/vU0M77+977+9PBfvv73vv73MlC3vv73vv70bE++/ve+/vVPvv71aft6177+9Ee+/vXBJCu+/vdW/77+977+9Cu+/ve+/ve+/veWEnnPvv71x77+9f0JdYlvvv73vv70tZ1cbbu+/ve+/vSlL77+9LT3vv70CGinvv71CNF7vv71A77+9TBzotqnvv73Frn0S77+9c++/vQTvv70X77+977+977+977+9Twzvv70BTO+/ve+/veuBogLvv73vv73vv73vv70RRO+/vW/vv70+z58HWS4q77+9ae+/vXrvv73vv73vv70S77+9Cgtp77+977+977+9CToc77+9Uu+/ve+/ve+/ve+/ve+/ve+/vUcd77+9OnLvv71N77+977+9dSfvv71277+977+9HykH77+9Ajzvv73vv70W77+9CwHvv73vv73vv71K77+9DjZybe+/vXLvv70u77+9YO+/vSPvv71f77+9L++/ve+/ve+/ve+/vXkpWe+/vQ4c77+9Zj9+77+9Ou+/vRnvv70IdkklW++/ve+/ve+/ve+/vUbvv70kSFw177+977+9YO+/vTHvv71h77+977+9Tu+/vTnvv71lL2ZZI++/vdGwdO+/vSw+77+977+977+977+977+977+977+9Zwbvv70C77+977+977+977+9053vv73vv71E77+977+9ae+/vRgH77+9Au+/vT/vv704LhMw77+977+9eCQ4HFNr77+9JHjvv73vv70d77+9cw4gAO+/ve+/vTfvv73vq6Dvv70W34Tvv70Q77+977+9NAckWn9cQei5vO+/vS05NO+/vS/vv73vv71/chhBGu+/vVvbrO+/ve+/vVHvv70iSu+/ve+/vTxiQe+/ve+/ve+/vQnvv73vv73vv73vv73vv73vv73vv73vv707az1377+977+9Ew4m77+977+977+9Du+/vQ3IklNzV++/vRnavCPvv73vv73vv70K77+9S++/vW40Ce+/ve+/vUxh77+9U++/vQ3vv73DqQjvv70oQe+/vW/vv73vv73vv70y77+9Psu377+9A1ZDUu+/ve+/vWPvv70H77+977+977+9GTI7H2p+77+977+977+9KGbvv73vv71rL++/ve+/ve+/ve+/ve+/vQPvv73vv73vv719SGoS77+9HCEO77+977+977+977+9Ag8377+9Q2/vv73vv73vv73vv73vv70U77+977+9fO+/vcynDO+/vQ8GGzjvv70ZGO+/ve+/vR8P77+9cVbvv73vv71cXO+/ve+/vXMrNu+/vTvvv73vv73vv70977+9Ae+/vQDvv73vv73vv70zW++/vVfvv70m77+9a2ASKU3vv710fwsbM++/vTEL77+9Rh1CI++/vVUd77+977+977+95reMBhUtIe+/vUtPcgx+77+9H++/vQnvv71myaEV77+9LUMR77+9BHpu77+977+9fWPvv71OXQDvv70JEe+/ve+/vdCD77+977+9CO+/vWcHKTs+Xz4d77+977+9e8KHS++/vTnvv71W77+9U++/vRZ5w5p+77+9RO+/vVA/Nu+/vQPvv71nKEvvv73vv73fjXzvv73vv73vv73vv71e77+977+97ISHTw8177+977+9Ng3vv70077+9We+/ve+/vRcBJu+/vd2NXO+/vRh3X3Hvv71wDjzvv73vv71A77+9I++/ve+/vU8OF1Xbie+/ve+/ve+/vVE6eQtB77+9O++/vU5P77+977+977+9Q++/ve+/veyokk7MhQ3vv73vv71F3ovvv73vv73vv73vv73vv73Mtu+/vThz77+9Hu+/vRsr77+9x5vvv71zU92K77+977+977+977+977+9Jl0MKBZy77+9YO+/vUN+WiTvv73vv708HO+/ve+/vRHvv715Ru+/ve+/ve+/vXs3Hg3vv73vv73vv73vv713Oe+/vTBEYSJyQe+/vQZe77+9Ve+/vSrvv71BAC7vv71aS0jvv73vv73vv71gA3NsYe+/vXYM77+9aO+/vX7vv73vv73vv73vv70xXe+/ve+/vVVr77+977+9du+/ve+/vWrvv73vv73vv73cs++/ve+/vVpN77+9YFkr77+977+9Vmnvv73vv73vv73vv71/77+977+977+977+9fnbvv73vv71OWn9USe+/vTsfK++/vTQo77+9Tw7vv707PO+/vSDvv71jRgPvv71NbEjvv71t77+977+9eu+/ve+/vWJbdu+/vV1v77+9TVnvv70z77+9dO+/ve+/vT4577+977+9Au+/vSJ777+94Ya+ee+/ve+/ve+/vQkF77+9Vljvv73vv73vv73vv71zB3Dvv71E77+9dO+/ve+/ve+/ve+/vSMjWnTvv73vv70KGu+/vVLvv719a++/vXZK77+977+9Hw/vv70/IULDr++/vTB577+9Ce+/vQZd77+977+9dlHvv73vv73vv73vv70NQ++/vWso77+977+9N2Xvv73vv73vv71dSk1ZLixfR++/ve+/ve+/vXvvv73vv73vv73vv73vv70P77+9aw/vv71E77+977+977+977+977+9Wxrvv70x77+9GitU1anjqozvv73vv73vv70D77+9Bg5277+9G1NlO++/vTbvv73vv715TzRd77+977+9Se+/vRgt77+9ExXvv73vv73vv711XO+/vTUTFe+/vRnvv70oR3s777+9b++/ve+/ve+/vSpzXu+/vVFV77+9Y++/vRQSOg8377+9M++/vQwD77+977+92bPvv73vv71HVRXvv719R++/ve+/vUfvv71YGhXvv70cFe+/ve+/ve+/ve+/vWAXVe+/vQbvv700FO+/vQvSm8y077+9WU3vv71B77+977+9R1fvv70iA++/ve+/ve+/ve+/vV8U77+9364VbO+/ve+/vRvvv73vv73vv71977+977+9N++/ve+/vdmrdu+/ve+/ve+/vQjvv73vv73WtO+/ve+/ve+/ve+/vWFE77+9Lu+/vcK677+977+977+977+9KUzvv71j77+92pTvv73vv73vv73vv70IZt2gRu+/vQ1KRF7vv73vv73vv707Ze+/vQHvv70x77+977+9cytRek1U77+9F++/vXtI77+9RM+0yrTvv73vv73vv71g77+977+9Ee+/vTYoLe+/vVxpaUrvv70m77+977+9Gwwq77+9OwLvv73vv73vv73vv73vv70F77+9ECsLU1fvv73snonHlGVj77+9ybdKOu+/vXpJFxZBY2Pvv73vv71d77+9Se+/ve+/ve+/vd6nQu+/vR/vv71xXx5teu+/vWpz77+9V2N+77+977+9xZnvv704bgsW77+9He+/vTPvv70W77+977+9Y++/vVPvv71aCe+/vWbvv71vw75877+977+977+9H1Xvv70j77+9M2/vv73vv73vv73vv73vv70577+9bQHvv71PeT1/KCvvv73vv73OrO+/ve+/vXZO77+9bn1L77+9HRHvv73vv70gek5A77+977+977+977+9BQXvv71DfO+/ve+/ve+/ve+/ve+/ve+/vd+177+9CgpLDg0tLe+/vU9c77+9KA3vv73vv73vv71z77+9Se+/vTbvv71kFwTvv70AeUburZ3vv73vv73vv71E77+977+977+9c2/vv71w77+977+977+977+9c++/vTM8ByQnZMKnN++/vWnvv715eT7vv71WOO+/vUzvv70q77+9R++/vQzvv70rAe+/ve+/vUvvv71RekxU77+9Ngzvv71s77+977+977+9S++/vQbvv73vv73vv71tcu+/vQtN77+9I++/vV/Jl2MVHO+/vXEfZO+/ve+/ve+/vT3vv73vv73vv71rNlVy77+9a++/ve+/vWPvv71U77+9aO+/ve+/ve+/ve+/vXzvv70NOQdn77+9LUAjP++/vU3vv73vv71E3ZcmS++/vWPvv71SIe+/ve+/ve+/vX8K77+977+977+977+977+9ZTsN77+9AO+/vVXvv73vv71PA03vv70oVu+/vVLvv70fVmzvv73vv71gHe+/ve+/vWXvv73vv73vv71YYXwxQGLvv73vv71v77+977+9G1IfFe+/vQDvv70X77+977+9IiLvv71J77+977+9Nh99d++/ve+/vTXvv73vv71vZu+/ve+/ve+/ve+/vSl/LO+/ve+/vSNcQjXvv71DIO+/ve+/ve+/ve+/vRtb77+9HEHvv71dAXLvv70JBTNb77+977+977+9UV/XnO+/vVfvv73aoe+/vVzvv70k77+9Qs+6Pu+/vTNi5biP77+9PhPvv70XXe+/vQ9KIDkKAO+/vSDvv70hE++/ve+/ve+/ve+/vRvvv71Qek3vv71PDBskfe+/ve+/vUTvv71PK1EGTFvvv71K77+9CRM2ZBwpVe+/vVzvv71j77+9UjhT77+9x4Mz77+94qiyDu+/ve+/vW9nIGfvv73Pgw3vv70kBQHvv73vv717J++/ve+/vXvvv73Tqzbvv71gHXNtUUzvv73vv70r77+977+977+977+9J1Us77+977+9H++/vSfvv71ILSlN4o6G77+9Y++/ve+/vT7vv73vv73vv70cOu+/vVFS77+9eXrvv71p77+977+9aAXtip3vv71c26YgbO+/ve+/ve+/vSBV77+977+977+977+9Ngbvv71UUu+/ve+/vTvvv73vv71eVwUEEzjmpbHvv70rdO+/vVhG77+977+9Lu+/ve+/vTLvv73vv70ZJO+/ve+/ve+/ve+/vceLfEfvv70x77+977+9ZO+/vWUjaDwh77+977+91ZY177+92LMnwojvv73vv70Lb++/vTPvv73vv71eee+/vSl/77+9Ze+/ve+/vXAwOe+/vSR+CzBC3YLvv7006Y6eaiR6Ju+/vVnvv73vv73vv73vv73vv73vv73vv73vv73vv71X77+977+9aw1sOu+/ve+/ve+/vSN577+977+977+9b++/vQxm77+9XFjvv73vv73vv70K77+977+9Yu+/ve+/vWfvv73vv73vv73vv71u77+977+977+9He+/vT3vv704dO+/ve+/vc+Z77+977+90pDvv71vfO+/ve+/vXJSRQs+OUtW77+977+9Q++/vVJ+V++/ve+/vR9t2p3vv70JL1ZRzZfPju+/vXvvv70gVXLvv73vv71Nfu+/vRtrV3BH77+977+9Iu+/ve+/ve+/vSkn77+977+90Zvvv73vv71i77+9Eu+/ve+/vRzvv71U77+977+977+9C++/vWrvv702JHPvv71v77+977+977+9W++/ve+/ve+/vQ7vv73vv73vv70yFe+/vTVaDe+/ve+/vXHvv70mRSzvv70K77+9AxPvv71ALiAh77+9NXRVIO+/ve+/vXDvv73vv71zUBLvv73vv70D77+9MRLdk27vv70f2ZkGRFRR77+977+9IO+/ve+/vXXdpe+/ve+/vTo13YLvv73vv71b77+977+9fe+/ve+/ve+/vVfvv73vv73vv73vv71777+977+977+9XO+/ve+/vV5jVE7duSBN77+977+977+977+977+977+9dmnvv70oairvv70sde+/vQwgV++/ve+/vXV/77+977+9NVHvv71jZe+/vQ3vv73vv70Teu+/vQ06XNO+G++/vWkHN++/vS5H77+977+977+91K7irI3vv73vv71X77+9BmlF77+9T08w77+9xJQddzvqjpvvv70677+916zvv73vv73vv71ENe+/ve+/ve+/vRDvv73vv717eT7vv70VY0/vv73Qsy5877+92Zjvv705be+/ve+/ve+/vXXvv71J77+9fGs3De+/ve+/vR8FdjUhEno2X++/ve+/ve+/vXYE77+977+9DO+/vVcEEGXvv73vv70bS2nvv73vv73vv73vv71z77+977+9Vu+/ve+/ve+/vWDvv70EJO+/vXBtX0guyZVuEe+/vT3vv702I926a++/ve+/vS7vv73vv71u05bvv73vv71sUx1gRBfNle+/vXI1Ke+/ve+/vVpjbRjXh++/ve+/vVpnJe+/ve+/vVLvv70X77+9AO+/ve+/ve+/vUfvv71o77+9QO+/vSXvv703FFty77+9Bu+/ve+/vWvvv70x77+9NNCh77+977+9Eu+/ve+/ve+/ve+/ve+/vSpacu+/ve+/ve+/vWdEIu+/vTvZvO+/ve+/vRs+Hzfvv71d77+9e0kfZ++/vS9gbe+/ve+/vRHvv71uGu+/vSnvv70I77+9ce+/ve+/vQfvv70pW++/vTjvv70ZF1/vv73vv73vv73vv71yZu+/vQrvv73vv71m77+977+9aE7vv71AV++/vX3vv73vv71M77+9xo1077+977+9SHfvv71VfkLvv71w77+977+977+977+9bWty77+9Su+/ve+/vUh2He+/vU/FuSBNdO+/vR09M++/vQ8y77+9FnxH77+9MO+/ve+/vXjvv73vv71ANO+/ve+/ve+/ve+/vSPvv71S77+9AO+/vQUlYm/vv73vv70NTu+/ve+/ve+/ve+/ve+/ve+/vSVf77+9L++/ve+/ve+/ve+/ve+/vTIr77+977+9Xe+/ve+/vT/vv70I77+977+9Ge+/ve+/vTLvv70OFO+/ve+/ve+/vWdVLU7vv73vv73vv73vv73vv73PtTZnae+/vUPvv70LCO+/ve+/vSXvv71I77+9QHtzde+/ve+/vREdRX1377+9MO+/ve+/vRtT77+9Su+/ve+/vSRX77+977+977+977+9b2zvv73vv70J77+9RO+/ve+/ve+/ve+/ve+/ve+/vUo5f++/vRreju+/ve+/ve+/vUAd77+9b++/vU/vv70w77+9GmPvv70977+977+9NuyPqgsc77+977+977+977+977+9Fhot77+977+9Se+/vXdlbTwQbO+/ve+/vWDvv70xNO+/vSxq77+977+9Ycmo77+9Le+/ve+/vUjvv71BL++/vRdcOxTvv70aSxpv77+977+9Pmvvv71UOu+/vWLvv73vv73vv73vv71t77+9OUFK77+9L2/vv73vv73vv73vv71kwrnvv73vv702WXnvv73vv73vv73vv70t77+935bvv73vv73vv70s77+977+977+977+9fyzvv73vv73vv71EOu+/vUrvv70A77+9KO+/ve+/ve+/ve+/ve+/vT0k77+977+977+977+977+977+977+9Jnh8aTEDTe+/vXPvv73vv70Aee+/vUEv77+9T++/vRFMOnXvv70FMHk177+9ByZo77+9bS/vv73aj++/vSY077+977+977+9DFdc77+977+977+9RzV+77+9W++/ve+/ve+/ve+/ve+/ve+/ve+/vSrvv73vv71WFXLvv73vv715Klld77+977+9cDVHbtqXCzvvv73vv73vv712csafJVnvv70s77+9Rx1z77+977+977+977+9eXDvv73vv73vv70u77+977+9Y++/vTsb77+98JualVzvv70977+9XCEz77+977+977+9ee+/vU3fqyHvv73vv73vv70DORR6Iwrvv73vv73vv70T77+9Nmdp77+9TX9R77+977+977+9E++/ve+/vW0TyYtvde+/ve+/ve+/veWNv++/vdmjGBR6f07vv73vv73vv704QO+/ve+/ve+/ve+/vUPvv73vv71z77+9X++/vSUvRdC/PXTvv70IEu+/ve+/vStJOe+/vXdn77+9RFpaDe+/ve+/vUR834gh77+977+977+977+977+9WS9X77+9aO+/ve+/ve+/ve+/vTQafO+/vU3vv71977+977+977+9Oivvv71r77+977+9KO+/vcmr77+977+9Yu+/vWzvv715XO+/ve+/vVvvv71acgLvv70PbATvv73vv71QZu+/ve+/ve+/vT0377+9JFVK77+977+9Yu+/vTXOg++/vXoean4XDe+/ve+/vRkHa++/vW7vv70K77+977+9Qu+/ve+/ve+/vS7vv71gSWHvv73vv73vv73vv71h77+977+9X++/ve+/ve+/ve+/vXs277+9DjTvv70p77+977+9Lu+/vRzvv73vv73vv701H3JqWSvvv73vv70U77+9LAneiO+/ve+/vRfvv73vv73vv71v77+977+977+977+977+93rbvv71wM8qQ77+977+9SWLvv73vv71ke++/ve+/ve+/vWzvv70J35FxRO+/vU7vv73vv70lOHZPIe+/vVQcC++/ve+/vRbvv73vv70yIlpc77+9Uu+/vWBtUu+/vU7vv73vv70P77+9HQTvv70g77+9LVPvv73vv70U77+9c++/ve+/vSII77+9Ee+/ve+/ve+/vSs/77+977+977+92JPvv73vv71q77+9T3Tvv73vv71G77+977+9bTfvv71tOu+/vcal77+977+9Ju+/vXnlp4Tvv71177+9OkRfc++/ve+/ve+/vXjvv73vv70wAm7vv71/77+977+977+9Bzp477+9HO+/ve+/ve+/ve+/ve+/vS/vv73vv71Mz60l77+977+9eDA/AVPvv73vv73vv714CDzvv73vv70pGu+/ve+/vVVZee+/vRXvv71haQ7vv73vv71m77+9X2DJjH8B77+977+977+977+9I++/vTwZ77+977+9VO+/vd2QBO+/vT3vv71B77+977+9dnNw77+977+977+977+9KCld77+977+977+977+977+977+91bwRLzVKf++/ve+/ve+/vRxd77+977+9Ie+/ve+/vVHvv71w77+9fjnvv73vv73vv71Q77+9S27vv73vv70+77+977+9FyTvv70U77+977+977+9dGvvv70ZF++/vX8sMVpV77+977+977+977+9WwTvv73vv73vv73vv703UXPvv70A77+977+9Me+/vW3vv73vv73vv73vv71IWBoVJu+/ve+/vQ0C77+9X1JBfO+/vXVH77+9NTDvv73vv73vv73vv73vv70u77+9Be+/ve+/ve+/vSPvv73vv73vv71vP++/vXXvv73vv71UcWRXW24D77+977+977+977+9JVZYP2zvv73vv73vv70J77+9T3RRa38WKO+/ve+/vUMy77+9Vxd9d++/vUFube+/ve+/ve+/ve+/vV5E77+977+977+9EO+/vXxuFe+/vS4T77+9U++/ve+/ve+/vQFc77+9Uu+/vTHvv71NZ1bvv70b77+9ee+/ve+/vWYD77+916Lvv704fR4v77+977+9yqjvv73vv70oVu+/vR0r77+9SO+/vWHvv71bRe+/ve+/vRxB77+9dwU477+9Ge+/ve+/vTTvv70777+977+9Xhzvv73vv71677+977+977+9Eu+/vWQK77+977+9yZY4X3Hvv71y77+977+9Z++/vVx8cu+/ve+/ve+/veuGsu+/vRLvv71qdu+/vV40eVUm77+977+977+9KO+/ve+/ve+/vem6ru+/ve+/vXoL77+977+9Ge+/vX7Xi++/vS/vv73vv71aSg7vv71R77+977+9IAgF77+9X1pOLixRf0U/77+9NVYG77+977+977+9EWDvv73vv73vv73vv73vv73cve+/vSgG77+977+977+9MzPvv71/77+9a++/vV8B77+977+9Cu+/vWzvv73vv70ZQeeQokjvv70z77+977+9e27vv70t77+977+9SyFC77+9ITHvv73vv70k77+977+9Cu+/ve+/vWbvv71/77+9GO+/vRol77+9Su+/vS3vv73vv70l77+9Ce+/ve+/vRvKiO+/vWUz77+977+977+9Vu+/ve+/vVoL77+9Je+/vV3vv701QUDvv73vv73vv73vv70r77+977+977+9MwYnC++/ve+/vd+4Oe+/ve+/vdW477+9N2J+77+9T3rvv73vv73vv71mS++/vUXvv71Bdu+/ve+/ve+/ve+/vR7vv73vv70D77+9O2vvv73vv71UCe+/vSfvv73vv73vv73vv73vv73vv73vv73vv71uFjQW77+9Q++/ve+/vRzvv71gV++/ve+/ve+/vdayYu+/ve+/ve+/ve+/ve+/ve+/ve+/ve+/ve+/vSh4du+/vT3vv70O77+9UXl277+9WG/Jkc+FDzzvv71m77+9EQ/Nt++/vWBpVGjvv73vv73vv73vv70SK++/vd+IUXF60LDvv73vv70uKUfvv73vv73vv73vv73vv73vv73vv73vv73vv706J1g0flnvv73vv73vv70z77+9R++/ve+/vXzvv70nCu+/vQDvv71aXO+/vWZ+De+/vTfvv73vv73vv71H77+9b++/ve+/ve+/ve+/vXvvv70FCu+/vXwKzrkw36Dvv70E77+9fO+/vW4n77+977+9E++/ve+/vStyXe+/ve+/vQvvv73vv70D77+977+9Y13vv73vv71M77+977+9KGFJ77+977+977+9D1Nu77+9DO+/ve+/vRnvv71bYkzvv73GmGbvv73vv71s77+9Uyvvv71277+977+9H1Nl77+9FCvvv73vv71O77+9Z1zvv73vv70ZFwEl77+9wpQF77+9We+/vSjvv71tYRY477+977+977+9CO+/vUrvv70NSe+/vVV7e++/vV5yfXLvv73vv70RBBYkaO+/vV0P77+9KVnvv71LbO+/vTHvv71gGE7vv73vv73vv71cbe+/ve+/vRsxYu+/ve+/ve+/vXhj77+9CRHvv70n77+977+9MgLvv73vv73vv73vv73vv73vv71j77+977+9c++/ve+/ve+/vV8E77+9ZO+/vV0m77+977+9A3bvv73vv73vv71TC3Tvv73vv71ae++/ve+/ve+/ve+/vQbvv71877+977+977+977+9xYEH77+977+977+977+977+9GDbvv73vv73vv71L77+9dO+/ve+/ve+/ve+/ve+/vULvv70Oz6dP77+9S++/vXRe77+977+9au+/vU3vv71q77+9HR9l77+9SRdqbO+/ve+/vS4F77+977+977+9Ru+/ve+/vX83bHbvv73vv73vv73vv73vv73vv70m77+9Ku+/vWFN77+977+977+9Au+/vTZl77+977+9R++/vWZ777+9LDXvv70U77+9DF4pS++/vUnvv73ElO+/vSnvv70I77+977+9e++/ve+/vSDvv73vv73vv71177+977+9LhHKmQnvv71kwqrvv73vv73vv73vv73vv73vv73vv70YOQx5bu+/vWZr77+9ae+/vRhTLHFm2qLvv71m77+9bHMOfe+/vS1bV++/ve+/vSVp77+9Q++/ve+/vXXvv70777+977+9SQ4N0aLvv70E77+9Xhfvv73UvO+/ve+/vQjvv73vv73vv70RZO+/vcqz77+9SO+/ve+/vXYXJB1dFe+/ve+/ve+/vVpy77+977+977+977+9HRsl77+977+977+977+977+9He+/vcmx77+9SW9o77+977+977+9d1rvv70uRe+/ve+/ve+/vVlsMe+/vTrvv70DZFbvv713E++/vVXvv73vv71a77+977+9Nu+/vcuDz7jvv73wkaKue++/ve+/ve+/ve+/vR8KUe+/ve+/vXMqDe+/vXrvv73vv73vv73vv73vv71B77+9H1hp77+9Re+/vVbvv73vv73vv70O77+9XFTvv73vv73vv71yMi3vv71d77+9Gjnvv71X77+9dx8QT++/vU/vv73vv71KTm3vv73vv71Qde+/vV7vv71K77+9Yu+/vUvvv73Rre+/ve+/ve+/vVrvv73vv71mwqpMSDUTyZnvv73vv71B77+9OGpj77+9OcyC77+9x5Tvv71E77+977+9LQxc77+977+977+9VO+/vSlq77+9JDVy77+9Cjnvv71d77+9CS0q77+977+9H3F3RF9zWHNB77+9HH1nT38b77+977+93q/vv73vv70cJu+/ve+/vULHle+/vVvvv73vv73vv71BSg7vv73vv70L77+9bO+/vX4N77+9U++/ve+/vSfvv71F77+977+977+977+9K++/vU7OtjTvv73vv73TvO+/ve+/vQZ0XT/vv70yK++/ve+/ve+/ve+/ve+/vU9ES1V277+977+9AF3vv703FO+/vWxd77+977+9X++/ve+/vWw/aEc/77+977+9KOWnhEDvv70D77+977+977+977+977+977+92pRFGe+/ve+/ve+/ve+/vU1+77+977+977+9dCFadDIdMi0/77+977+977+977+9RBMw77+9EU16TO+/ve+/vQ3vv73vv71J77+9fEfGquWynO+/vX7vv73vv71I77+9Ye+/vSsBKTHvv73vv73vv70777+977+93aTvv73vv71+XcW577+977+977+9c++/vTQV77+977+977+9NO+/ve+/ve+/vWpM77+95Yyq77+9anXJqTHvv71uTF7vv73vv70LLu+/vX/vv71D77+977+9Y++/vcu+HUHvv71077+977+9Mhvvv71f77+977+9QF7vv73vv70077+91b0F77+9Ce+/ve+/vV8w77+9Uu+/ve+/ve+/vTHvv73vv73vv71U1pjvv73vv71177+9JSjvv70sPe+/ve+/ve+/vRF6zrnPjt6rc++/vRQ1Pxnvv73vv70NZwR6b++/vRljde+/vSfvv70XDe+/vS7vv7117ZeVDwfvv708Ue+/ve+/ve+/vUjvv73vv73vv70Y77+96I6tVlbvv71qYe+/vTIXd++/vU3vv73vv71977+9btusTDnvv71dRz0n77+977+977+9QO+/vR1nJO+/ve+/vVlU77+9Ze+/ve+/vRXvv71o77+9H++/ve+/vTDvv70pVO+/vRbCtUc+1qLvv70C77+9Z++/vQQe77+9bNel77+9fiVO77+977+9BkHvv73Wn1bvv73vv70gbyjZpmfvv708K0Tvv71h77+9Du+/ve+/vXlN77+977+977+9fGvvv70t77+9LO+/ve+/vR0i77+9yr3vv71J77+977+9J++/vUla77+977+977+9RV3Qnu+/vWJ/77+977+977+977+977+9A++/vXvvv73vv73vv70g77+977+9Jkfvv71lAUML77+977+9c++/vXt777+9Nu+/vSDvv70zMjTvv73Um++/ve+/ve+/ve+/vUbvv71lee+/ve+/vVzvv71Qd++/vQDvv71CF++/ve+/vRYZfO+/ve+/ve+/ve+/ve+/ve+/vU3bj++/ve+/ve+/vWAiKVnVpO+/ve+/vdKSW82ZSjVd77+9zInvv71yRe+/vUo1We+/vTUJ77+9Ju+/ve+/ve+/vQ0g77+9Z0ppOXQtLmlaXO+/vWYHTe+/vWzvv73vv70yKO+/vWpUMu+/vVHvv73vv73vv71FGznvv73vv73vv73vv70BemxTXWfvv71d77+9JO+/ve+/vRIr77+9d++/ve+/vQvvv71E77+977+9fiFA77+9De+/ve+/ve+/vWzvv73Mpu+/ve+/vT5yMc6QOO+/vWTvv702XO+/vTt9SyNV0IUj77+9We+/vRc+Ke+/ve+/vcSxIe+/vVZbDu+/vdCf77+9Eu+/vQPvv717Xu+/ve+/ve+/vXdvdmDvv73vv71QUO+/ve+/ve+/vSDvv70T77+9IhItZ28jXy5A77+977+9eu+/vU3LoO+/vQItP++/vSLTke+/vVRNNw9P77+977+9RSTvv710MvKyg67vv73vv73vv71cdu+/vRha77+9fO+/vWPvv73vv70lRkDvv73vv71bEe+/vVMy77+9XVTvv71VThFLU++/ve+/vVt7ESTvv73vv73vv71eAg9+77+9zJ5z77+977+977+977+9K++/ve+/ve+/ve+/vWkR77+9aO+/ve+/ve+/vT8+Se+/ve+/vRPvv707ae+/ve+/vXlOQDwcKO+/ve+/vSg7Nu+/vX/vv70577+9JUxw77+977+9EShK77+9dO+/ve+/ve+/ve+/vTt8dO+/ve+/vWzvv71FMe+/vS8k77+9Ce+/ve+/vWfvv70G77+977+977+9OQPvv70H77+977+9P++/ve+/ve+/vcK8du+/vRcS77+977+9Pu+/ve+/ve+/ve+/ve+/vRzvv71fLVJD77+9fzlJb++/ve+/vVd8R++/vTDvv70p77+9Du+/vTHvv71P3ZQeHe+/vTEn77+9VV5/77+977+9xoN/c++/ve+/vQJq77+9bXsd77+9HhpfNwbvv73vv70uHu+/vUkD77+9J1Lvv70b77+977+9KnkR77+977+9AhTvv73vv70m3rN/77+977+9c++/ve+/vV11SO+/vVbvv73vv73vv710YypTUWMSG0/vv70yfEBrU++/ve+/ve+/ve+/ve+/vXLvv73vv73vv73vv73vv71mR++/vXVmPN2e77+9a3tjKe+/vX86ce+/ve+/ve+/vT8lRO+/vUUH77+9Vx0THgXvv73vv70Ibte+Ke+/vV/vv73hmJ8x77+977+9c2Hvv73vv73vv70OBh7vv73vv73vv71EHHPvv700XO+/vR8T77+9KwLvv71cYO+/ve+/vem7u++/vdi777+977+977+9Re+/ve+/ve+/vVEh77+977+9IyUD77+977+9N++/vR4X77+9cu+/vVtYEO+/ve+/ve+/vRnvv703P++/ve+/ve+/vRPvv70Dzbsp77+977+9ZRDvv70G77+9ORYlZ++/vV/vv73Xpwso77+977+9e0vvv70Z77+9VFbvv714e++/vTjQk2nvv73vv73vv71u77+9ae+/vTzvv73vv70G77+9f++/vQQlI++/ve+/vT0k77+9fUbvv73vv70f77+9EB3vv73vv71I77+9Bu+/vUrvv73vv70x77+977+9LmovDe+/vVzvv70uGu+/vWDvv70dF++/ve+/vT4k77+9SmLvv73vv73vv70b77+9CmlB77+9UWMS77+977+977+977+977+977+9HO+/vU1QHwXvv70gKHXvv70H77+977+9AHkQXe+/ve+/vScV77+9aO+/vW7vv71r77+9eTcRXtCh77+9KHDvv70j77+9Nn7vv71a3pkpHdmvMmko77+977+9Lu+/vXLvv73vv71077+977+977+9BDUX77+9z57vv70KC++/vVbvv73vv70C77+9RUfvv70KSHbvv70pD3ciWnRi77+977+96Li877+9c0Tvv70Fcu+/ve+/ve+/ve+/ve+/vQzvv71VXe+/ve+/ve+/vXYi77+9Ou+/ve+/ve+/ve+/ve+/vVRK77+977+9YO+/ve+/vXfvv71i77+977+977+977+9eTlyVe+/ve+/vXd3I++/ve+/vTzvv73vv70P77+977+977+9cCJ477+9GAcxT++/ve+/vXPvv70O77+977+977+9zplrFO+/vRbvv73vv73vv71V77+9au+/ve+/vV3vv71+3p/vv70NYe+/ve+/vV3vv71I0p4IFBHvv73vv71VSsm077+9O0zvv71T77+977+9We+/ve+/ve+/vU7YmO+/ve+/vXfvv73vv70477+977+9U++/ve+/ve+/ve+/ve+/vXPvv70VB++/vU/vv70rHHMzBiHvv73vv70RQO+/ve+/ve+/vUPvv73vv73vv70326fvv70u77+9SEsb77+9NSPvv70Y77+9Ie+/vXlFOR1X77+977+977+9bifvv73vv71M77+9LRttP++/vTDmnrkW77+9cO+/vUnvv73vv73vv70I77+9PT8e77+9Oinvv73vv71L77+977+9Uu+/ve+/vTTNv++/ve+/ve+/vUEqTg/vv70Q77+977+977+9RHLvv73vv73Tnu+/ve+/vQDvv70yRTTvv73vv71JB++/ve+/vQLvv70A77+9ae+/vS5Z77+977+9w7NFN2/XrNOe77+9cO+/vUlv77+9Ge+/ve+/vXfvv73vv73vv73vv70u77+9b9uM77+9GO+/vV/vv73vv73vv70I77+977+91I1zYT7vv73vv73vv73vv70SYwPvv70cHi1677+93q/vv70L77+977+9SO+/ve+/ve2bk++/vXrvv71Jd3dTdW7vv70zE8yT77+9Gu+/vX3vv71U77+9H2Tvv70D77+9eA8d77+977+9e++/ve+/vQRw77+9N++/vQFGVwXvv71vHg7RuO+/ve+/vV8z77+9XDjvv71C77+9TwlTd1nvv73vv73vv73vv73vv71OyaLvv70Q77+977+9A30neUzvv73vv73vv71N057vv70U77+977+9Ze+/ve+/ve+/vUvvv73vv70977+9MT3vv73vv71pWBPTp2Xvv70I77+977+977+9eu+/ve+/ve+/vRrvv73vv71b77+977+9Nlzvv703Je+/ve+/vXtz77+977+977+9emHvv73vv71Z77+9EzUm77+977+9B18RcO+/vULvv71HMO+/ve+/vTlaPu+/vWvvv73vv73vv71U77+9Ie+/vTpaJu+/vVZi77+977+9z6/vv73vv73vv73vv71uXl3vv71q77+9Xhbvv71S77+9YO+/vVNA77+977+9esunEu+/ve+/vVoF77+9Xikc77+977+977+9d++/vRZpce+/ve+/vRTvv71nXe+/vd6TbjETX2bvv705P++/ve+/vQsJ77+977+9LTFq77+9DdetxYzvv71nQu+/ve+/vWHvv73Ph2XUhO+/ve+/vUMeKu+/vQ4i77+9VO+/ve+/vSERX2bvv73vv73ukp1V77+977+9Vu+/vR5q77+9fSM6ae+/ve+/vXjvv73vv70vc++/vc6c77+977+9OdCL77+9Ie+/ve+/vTlb77+90LJZF++/vV8z77+977+977+977+977+977+9Te+/ve+/vVLvv73vv73PqTbvv705XB/vv71GFF5ke03vv70GJExlKiVHRkl+aEbvv71177+9Be+/ve+/vUtoWe+/vRDvv70b77+9XO+/vTHbq++/vQ0a77+9F0By77+977+977+977+9Te+/ve+/vT8x77+977+9KxN/fzgY77+977+9Fu+/ve+/vTHvv717Je+/vTXvv73vv73vv73vv71Tcljvv73So2JF77+977+977+9Mivvv70lVmLvv71t77+977+977+9T3pw77+9TyPvv706JUdGCe+/ve+/vR11TS4iIVfvv73vv71H77+9X0Pvv71wUnJ4DEvvv71C77+9U++/ve+/ve+/ve+/ve+/vVTvv71WbO+/vRBv77+977+9R++/vTZl77+9LGnNge+/vdOP77+9NO+/vX8O77+9fz4rc++/ve+/ve+/ve+/ve+/ve+/ve+/vRgIFUPvv71bG++/ve+/vWlP77+977+977+9z6luzbTJjTvvv73vv70077+977+977+977+92p/vv73vv70p77+9Pe+/vQk6Rmpn77+9CU/vv71v77+977+977+9C++/ve+/vX/vv71DK9mObRfvv73vv718Le+/ve+/vQsJ77+977+9QO+/vU0777+977+9E++/ve+/ve+/vX7vv71zYQrvv70077+977+9Oik7Cu+/vRXvv73vv73vv71LVe+/ve+/vSbvv73Puu+/ve+/vQDvv71XA++/ve+/vRYT77+9LO+/vRUtOu+/vTlp77+9HFo2T8yQWmMp77+9Qu+/vTU577+9X++/vWNt77+977+9e09JUSXvv73vv71R77+977+9IO+/ve+/ve+/ve+/vVDvv73vv73vv70G77+9B++/vXAT77+977+9O++/ve+/ve+/vVIr77+9VhlL77+9Qu+/vWFxBh/vv70U3a/Jq2Jt77+9W0nvv70RDO+/ve+/vSrvv71377+9D3Pvv71C77+9Ge+/vU7vv71KNFcr77+977+977+977+9E++/vS3vv73vv73CgO+/vXDvv708M++/ve+/vU8jZj7vv71AB++/vWfvv71eS++/ve+/vS0077+977+977+977+9XsOmNtKkO++/vRvvv71/c2/vv73vv71X77+977+977+9Ry7fjO+/vWnvv73vv71HFVTvv70fNO+/vWjvv73vv73vv73vv71fPATvv73vv70bD++/vX3vv73vv70E77+9IQLCre+/vXVy77+977+9H++/vRlPA3/vv73mnY3vv70TeUrvv70t77+9MXfvv71v77+9NCjvv70877+9PhTvv73vv73vv73vv71677+9cFbvv73vv73vv70M77+977+977+9EO+/vXdkDF0VWO+/vX/vv73vv717BEnvv73vv70o77+977+9Ru+/vVzvv70j77+91YBE370lRO+/ve+/ve+/vX9TSe+/vRgHbH3vv71RBO+/vSF+fdGiI1cpJD7vv73vv73vv71FRO+/ve+/ve+/vSJXBHJL77+9Au+/vT7vv71J77+977+977+9X++/vVTvv71oEO+/vVvvv73vv73vv70477+9Z++/ve+/vVTvv71177+9LhhEMO+/ve+/vTnvv73vv71cNg7vv71lVljvv73Tul9z77+9J++/ve+/ve+/vU7vv70077+977+9Ku+/vdG277+977+9TytePu+/vUAH77+977+9TXXvv70bOEjvv73vv73XoQtF77+9Inxf77+9NnkOTO+/vTfvv71eHe+/vWJi77+9R++/ve+/ve+/vToL77+9zqzvv73SoGDvv71k77+9ZCMgBFBSCO+/vWkp77+977+977+9WzdVJe+/vUbvv73vv70CCGbvv73vv70KC++/vU/vv73vv70V77+977+977+9LTTvv71o0bHvv73vv70YGkzvv71p77+977+9Oe+/ve+/ve+/ve+/ve+/vX4jWhTvv73vv71H77+9VnLvv73vv71Q77+977+9MO+/vU06dVfvv70bQUU0UAYkBFnvv71U77+9YSpT77+977+977+9AO+/vRrvv73vv73vv73vv71kWu+/vcWH77+9SO+/vXnvv73vv70K77+9c3Lvv70F77+9Xikc77+977+9ce+/vcKxMO+/vWtRfhhm77+9TTsd77+977+977+9TCLvv70477+9Ekbvv73vv73vv73vv73vv70S77+977+9C++/ve+/vRQV77+977+977+977+977+977+9fV7vv73vv71d77+9XSXvv73vv70/77+977+9bu+/ve+/vTQ3Gu+/vXDvv71M77+95o+J77+9dVfJp++/vWRL77+9Qu+/vTxDOQ/vv73vv73vv71o77+9Au+/ve+/ve+/ve+/vXdUe++/ve+/vVgvFO+/vWDvv70l77+9Ju+/vVhhLQLvv71c77+977+9MSdt77+977+977+96KiR77+9K++/vVnvv73vv73vv70AclUONSbvv71177+9HzTvv71SQu+/vUcvDhXvv71nZRBMOu+/ve+/vWhE77+977+9Be+/ve+/vTx577+9V2Qe77+9BHphCu+/vUgq77+977+977+9De+/ve+/vd6w77+977+977+9Wjbvv71U77+977+9E++/vWjvv73vv73CmO+/ve+/vT8h77+9c2EaNS4aFsKQ77+9PO+/vRzvv71BZ23vv73vv71T77+9Pe+/ve+/vWF2ZT/vv73vv73vv73vv70w77+977+977+9IG49DO+/vSsC77+977+9ZWTvv73vv70x77+977+9Tu+/ve+/vc+DXO+/ve+/ve+/ve+/vQw877+977+9Be+/vUfXvO+/ve+/ve+/vU3vv71re++/ve+/vTTvv73vv73vv70c77+9Be+/vW3vv73vv705Q++/ve+/ve+/ve+/vTMBdMO377+977+9PO+/vSLvv70BYVRbYM2PB++/vTbvv73vv70d77+977+9au+/vS5577+9Fu+/ve+/vRIb77+977+977+977+977+977+9RO+/vXJfF++/ve+/ve+/ve+/vT4oYe+/ve+/vd+I77+92rvvv71m77+977+9fTPvv71zYe+/ve+/vRNtWlF077+9Pjd/bXrvv73vv71c77+9ZO+/ve+/ve+/ve+/vWM877+977+977+977+9I++/ve+/vTBC77+977+9P0fvv70UfO+/vVIV77+9EXnvv70h77+977+9Ssev77+977+9fO+huELvv70v77+977+9B++/ve+/ve+/vSkASBdM77+9LTV6LiIZEXJL77+9EO+/ve+/vTccDFPvv70i77+9F1x0XO+/vWfdse+/ve+/vV/vv73vv73Coe+/vXNO77+9VFjvv70X77+977+93rfvv71ybhUD77+977+9fGo5ya7vv71N77+9U2/vv73vv73vv73vv71pY++/ve+/vR1NFRZYZmQf77+977+977+977+9M1Xvv71t77+977+9bu+/ve+/vTJfEO+/vWIoLu+/ve+/vVTvv73vv73vv70e77+9LD8p77+9aWfvv73vv71m77+9ZDp+Uc6G77+9awnvv71fMDHvv708SULvv71PBu+/ve+/vRrvv71Zae+/vU/Ng0fvv70zITl0fEfEqO+/ve+/ve+/ve+/ve+/vQ7vv73vv73vv73vv71Ad++/vcub77+977+9NVYyXUPvv73vv70iWnRKClRYRe+/ve+/vSM077+977+9Pe+/ve+/ve+/vUvvv73vv70zaRw477+977+9PGPvv710ZjtlIu+/ve+/ve+/vXPbtAwm77+9Zu+/ve+/ve+/vcqcUQjvv73vv73vv70+dAgo77+9Ju+/vXfvv70FTHTvv73vv71H77+9De+/ve+/vSbvv70x77+977+9Sw50VSDyioOW77+977+9aO+/ve+/ve+/vWhk77+9aToccmXvv71GN07vv71re++/vVzvv73vv71u77+977+977+9Gznvv73vv73vv706O++/vemzhA0T77+9Me+/ve+/vTbvv73vv71R1phe77+9de+/vX7vv73vv70u77+977+9W++/vUkl77+9R1Pvv73Pre+/ve+/ve+/vXzvv73vv73vv71bBu+/vQLvv714CDzvv73vv70xJ++/ve+/ve+/vTFE77+9Dirvv71eMu+/vSDvv70oAe+/ve+/ve+/ve+/ve+/vTVw77+9T++/vVzvv73GtTDvv73vv73vv73vv70k77+9Si7vv71nLR3vv73vv70nV++/ve+/ve+/vXtzXQ7vv70X45i8Ku+/vRUW77+977+9DigqEhHvv71877+977+9PnIaTWob77+93qnvv71m77+9X8m7Ge+/ve+/ve+/vVBLEe+/ve+/vQrvv70N77+9EUDvv71x77+9S2Pvv73vv70Z77+977+977+9bFgaFCxNWUxl77+9Ye+/ve+/vRU577+977+9UiRXWO+/ve+/vXnvv70MPO+/vWXvv71xT++/ve+/ve+/vTPvv71S77+9a2nvv73vv73vv71a77+9ae+/ve+/ve+/ve+/vQbvv71P77+977+9VO+/ve+/vTMoJj7vv70i77+9yL3vv713MCcbNV8kCFwC77+977+977+977+9Zu+/vXxvV2Nq77+977+977+977+955Wi77+9EnZuI++/ve+/ve+/ve+/vU/vv73vv70DyrkgTe+/vcuE77+977+96L6+DDXvv73vv71w77+9N++/ve+/vd60E2x0Me+/ve+/vRHvv73vv73vv71tdu+/ve+/vUvvv70K77+977+9WzDvv73vv71ANe+/vSjvv73vv73vv73vv73vv73vv73vv716ECwjVHMh77+9JO+/vXQke++/ve+/vVzvv73vv704fG9bdjgIZh1R1o3vv73vv73vv71aMXzvv71sEdCV77+9ae+/ve+/ve+/vSpqVCLxnp2yY++/vdKjI++/vQk4F++/ve+/vXrvv71t77+977+977+9Qu+/ve+/ve+/vRTvv71a77+977+977+977+977+9fV5677+977+9eg5B77+977+977+9VS4V77+977+9He+/ve+/ve+/vQU677+9UEcK77+9V2TWmu+/vSrvv73cg86iIlN/77+9bO+/ve+/ve+/ve+/vRnvv73vv73vv701cO+/ve+/vVLvv70F666477+977+9Uivvv73vv700UXZ077+9dygYN9K3du+/ve+/ve+/ve+/vcm1FnRFQO+/vUHvv73vv70AJO+/vUbvv71DJu+/ve+/vQ1rU3bvv71GN++/ve+/ve+/ve+/vRU0eu+/vWDvv70j77+9cO+/ve+/vWTvv71K77+9LwJoGWjvv73vv70C77+9FO+/vTgQ77+977+9ZtS477+977+9FRDvv70677+9Be+/vRTvv71YOEzvv70xJu+/vSZX77+9Ue+/vSLvv71dKyrvv73Jm++/vRJ677+9Y++/vRLvv71077+9d++/vV1l77+9Ck/vv73vv70V77+9Ee+/ve+/ve+/vdOVJXzvv73vv71/77+9Ue+/vTI977+9Uu+/ve+/ve+/vSMg77+9FBhVVF12XO+/vXHvv70z77+9ZteeybUg7LKyxrFAPgTvv70hbu+/ve+/vWjvv71jfu+/vT/vv70wbxVoGe+/vUjvv70BDD7vv70C77+9Yu+/ve+/vRDvv70TRwA9V++/ve+/vWPvv73vv70M77+977+977+91LDvv73vv73vv70c77+977+9LO+/ve+/vRlqLh4w77+9RO+/ve+/ve+/ve+/vUHvv73vv71gcO+/ve+/vUsYE3EsTVnvv73vv70977+9Yjbvv71cdFUgWe+/vW3vv71AYu+/ve+/ve+/vT/vv73vv73vv73vv70q1p9Y77+977+977+977+9Y++/vVXXou+/ve+/ve+/vc+077+977+9Au+/vSAIF++/vTPvv70L77+9F0Dvv73vv73vv73vv71977+977+977+977+9de+/ve+/vU3vv716Le+/vSIj1oRYd2Vg77+977+9HVQ677+977+9b++/ve+/vW9p77+9I++/ve+/vU9oV0Yl77+9H1/vv71jQe+/ve+/vWDvv73vv73vv73vv71Q77+9BhsxDz7vv70pI1fvv73IhSUi77+9dhpm77+9ce+/vWjvv701eu+/ve+/vdGVXhPvv73vv70477+9HO+/ve+/vT4jQ++/vU8G77+9M9+c77+9dVNp77+9CO+/vVDWi++/vS/vv70Z77+977+977+9Hu+/vXvvv73vv71577+9XwTvv73vv70nQ++/ve+/vT9QHO+/vRp877+9Re+/vRUH77+9Ze+/vTHdgV0n77+9au+/ve+/vW3vv73vv71Nf++/vWjTpu+/vXXvv70g77+9RE4Xf2Lvv73vv73vv70tfu+/vRzvv73vv70Q77+977+9VBdwUnbvv718C++/ve+/vXtg77+9Le+/vTnvv73vv73vv73vv71JalLMtl9W3p5abW4Y77+977+977+9Ou+/ve+/vSTvv73vv73vv70j3oLvv73vv73vv717NVjvv71y77+9RgdIfWjvv70+M++/vU9FFTRo77+9Rlfvv70Cce+/vQjvv71977+977+9e2/vv71hbe+/vWJpUO+/vRzvv73vv719QO+/vWzvv70J77+9W0zvv70Ueu+/vWXvv70X77+9NHbvv70v77+9VF0w77+977+9RDNA77+977+9Nu+/vXfvv71vVO+/vXly77+977+9Y15VXe+/vUzvv71uau+/vU1/77+977+90LZm77+9LxV077+9fHlmbjHvv73vv70i77+9Pyzvv73vv73vv73vv73vv71P77+9Be+/vVcCVO+/vWlWJ++/vXTvv70177+977+9Zl3vv73vv73vv71Z77+9Au+/vS0NCu+/vd+IIlcqdF1bTi4sFk3vv73vv73vv73vv73vv73vv71VyaVScXrvv70gaO+/vdK1EMyFIe+/ve+/vVfvv73vv73vv70XZB3blGx+Jllc77+9SGPvv71377+977+977+9MzLvv711V++/vVdJDs2y77+9R9Wt77+9XyLvv71QHhAE77+9Z++/vX7vv73vv73vv71ubO+/vSXvv70V77+977+9NO+/vS4Z77+9JFPvv71y77+9e++/ve+/vW3vv70877+9Ku+/vXJ877+9GSXvv718Ll3vv73vv71jNnzvv70+Fe+/ve+/vTQm77+977+977+9CGRaZe+/vRvvv70077+90Ifvv705S2rvv73vv73vv73vv71XfjJ/77+9RMK9f++/ve+/ve+/vcKBSe+/ve+/vdWB77+977+9AlxvSmZvERbvv73vv73vv718Du+/ve+/vWXvv73vv73Fle+/ve+/ve+/ve+/vXAh77+977+977+9de+/vRdcG++/ve+/ve+/vTjvv73vv71U77+977+977+977+977+9Ku+/vRcM77+9c3MpSu+/vSfHs++/vQHvv71d77+9L++/vV44UXpY77+9aRvvv70GBe+/vRtNZG/vv71vDTTvv70c77+9Fu+/vS/Dm++/ve+/vX8k77+9OUDvv73Wru+/vQRM0Z5r77+977+9U2ssde+/vS7vv70z77+9Py8iWUtz77+977+977+9ZwM+S++/ve+/vS3vv73vv73vv73vv71z77+92Zrvv73vv70w77+977+977+9D++/vXPvv73vv70f77+9H++/vVQE77+977+9QD8faO+/vda177+977+9LQPvv73vv73Iue+/vVjvv73vv71I77+9cu+/vW1KXRPvv71a77+977+977+9SO+/vXdU77+9YTvvv73vv70F77+977+9ybnMnz7vv70FL++/vQPvv73vv70A77+977+977+9WWs5TO+/ve+/vUvvv70iSu+/ve+/vWhZQR1877+977+9Ee+/ve+/vdOuRkXvv73vv73vv71477+977+9We+/ve+/vQHvv70X77+977+977+977+9Q03vv73vv71dHu+/ve+/vUHvv73vv73cpzzvv71p77+9dO+/vRzvv70SUdaa77+977+9Ne+/vTTvv71YYO+/ve+/vU7vv70iKAN/Lu+/vRR877+977+9VmNi77+9AO+/ve+/vU9w77+9Mu+/vVXvv71LC3gOSu+/vcu/H2kQLe+/ve+/ve+/ve+/vQQeFwTvv71PMu+/ve+/ve+/vUDvv70sbe+/vQ3vv73vv73vv718PO+/vXAG77+977+9OHY177+977+9bH3vv71PeVPvv71N77+9T1ME77+977+977+9E0nvv73vv73vv73SoyLvv73vv73vv73vv70m77+9ce+/ve+/ve+/vQLvv73ZpGfvv70WZhHvv71877+9fQ7vv73vv73vv71k77+977+9C++/vTlJ14Vj77+977+977+9H++/ve+/ve+/ve+/ve+/vX/vv73vv70df++/vWHvv73vv73vv71K77+9HA7vv70z77+9HgLvv71J77+9W++/vXID77+977+977+977+977+9b8SqRe+/ve+/vRjvv71777+9de+/ve+/vRF477+9PENZ77+977+9Lu+/vRzvv70oXz7vv73vv71677+977+977+977+9RNCORhfvv70977+9RVNQ77+977+9ONuDLzlzSu+/ve+/ve+/vRLvv71X77+9Be+/vWnvv70EUU/vv70OLWJp77+977+9PQcnzJ4v77+9a0Xvv70+AXdF77+9A++/vUkB77+9L++/vRnvv73Fgu+/ve+/ve+/ve+/ve+/vRzvv71077+9S++/vTXWg3VBPRxdOBTvv70y77+977+9UAQt77+977+977+9G33vv70eT++/vTXvv73KgO+/vdSy77+9AxXvv73vv71V77+9P++/vQvvv70KJlLvv71VS8ilau+/vTIl77+9eg5IOlwL77+9Fe+/vXYo27AO77+9FwRdeu+/vTQz77+977+9PO+/ve+/ve+/ve+/vTnvv70/77+977+977+977+9Mknvv704REc/VO+/ve+/ve+/vU4gb++/vRZASsymVlkC77+9Dy3vv71M77+977+93KAk77+9MVHvv71T77+977+977+9CGY9J++/vVF1GQRBAEHXjEDvv70o77+977+9I2jvv715SXrvv708e++/ve+/ve+/vWvvv71kJO+/ve+/vRXvv716Vu+/vWg577+9T++/vSx177+9YO+/ve+/vXU6Zu+/vUtF77+977+9UcuAE++/ve+/vcu677+9P++/ve+/ve+/vQvvv73vv73vv73Nn++/vc+B77+977+977+977+9G++/ve+/vVTvv71CNHFfBO+/ve+/vXVhH++/ve+/vT7vv71bLCDvv73vv70J77+9Gwjvv70bJi3vv73vv71A77+977+9Oe+/vT/vv70dMe+/ve+/vXrvv700Ie+/ve+/vSAuAGbvv70+77+9be+/ve+/vX5CJQzvv71K77+9PkBn77+9bhLeskzLru+/vXzvv70/B++/ve+/vQ1JfWjvv73vv70Fbe+/vS7vv71177+9aQhCE++/vUAt77+977+977+9fA7vv70CfTPvv73vv70q77+977+9E3RhZVYVP++/ve+/vU51fO+/vWrvv70D77+9c++/vVkW77+9Eu+/ve+/vVonKTTvv71aJyDvv71q6KWCLu+/vQXvv70yHe+/vRTvv71SdGzvv71V77+9Fu+/vVoG77+977+9Lk7vv70pV0cnXO+/vS4FwqDvv70+77+9Iwzvv70eEBEGde+/vUFB77+9OnIS77+977+977+9dO+/ve+/vXDvv73vv71+Lu+/vV7vv70f77+9X0vvv73vv70JSHkAAAAldEVYdGRhdGU6Y3JlYXRlADIwMjYtMDYtMDNUMjE6NDY6MzgrMDA6MDDvv70m77+977+9AAAAJXRFWHRkYXRlOm1vZGlmeQAyMDI2LTA2LTAzVDIxOjQ2OjM4KzAwOjAw77+9e2xGAAAAAElFTkTvv71CYO+/vQ=="
        return """<img src="data:image/jpeg;base64,$base64" style="width: 76px; height: 76px; display: block; margin: 0 auto; object-fit: contain;" alt="شعار موريتانيا" />"""
    }

    private fun getCommonStyles(
        isLandscape: Boolean,
        isList: Boolean = false,
        schoolName: String = "",
        fileTitle: String = ""
    ): String {
        val pageOrientation = if (isLandscape) "landscape" else "portrait"
        val pageMargin = if (isLandscape) "6mm 8mm" else "8mm 6mm"

        val pagedMediaStyles = if (isList) {
            """
            @bottom-right { content: "$schoolName"; font-family: 'Cairo', sans-serif; font-size: 8pt; }
            @bottom-center { content: "$fileTitle"; font-family: 'Cairo', sans-serif; font-size: 8pt; }
            @bottom-left { content: "صفحة " counter(page) " من " counter(pages); font-family: 'Cairo', sans-serif; font-size: 8pt; }
            """.trimIndent()
        } else ""

        return """
        <style>
            @page {
                size: $pageOrientation;
                margin: $pageMargin; /* Uniform per-page margin; eliminates margin creep */
                $pagedMediaStyles
            }
            @media print {
                @page {
                    size: $pageOrientation;
                    margin: $pageMargin;
                }
            }
            * {
                box-sizing: border-box;
            }
            body {
                font-family: 'Cairo', 'Noto Sans Arabic', 'Arial', sans-serif;
                direction: rtl;
                margin: 0 !important;
                padding: 0 !important;
                width: 100% !important;
                background-color: #ffffff;
                color: #000000;
                font-size: 11px;
                -webkit-print-color-adjust: exact;
                print-color-adjust: exact;
            }
            .a4-page {
                position: relative;
                width: 100%;
                margin: 0;
                padding: 0;
                box-sizing: border-box;
                page-break-before: always;
                break-before: page;
                page-break-after: always;
                break-after: page;
                page-break-inside: avoid;
                break-inside: avoid;
            }
            .a4-page:first-child {
                page-break-before: auto !important;
                break-before: auto !important;
            }
            .a4-page:last-child {
                page-break-after: avoid !important;
                break-after: avoid !important;
            }
            /* Side-by-side layout for Landscape Report Cards */
            .cards-page-layout {
                display: flex;
                flex-direction: row;
                justify-content: space-between;
                align-items: flex-start; /* Strict Top Alignment (MainAxisAlignment.start) */
                width: 100%;
                height: 195mm;
                max-height: 195mm;
                gap: 4mm;
                margin: 0;
                padding: 0;
                box-sizing: border-box;
                background-color: #ffffff;
                overflow: hidden;
                page-break-before: always;
                break-before: page;
                page-break-after: always;
                break-after: page;
                page-break-inside: avoid;
                break-inside: avoid;
            }
            .cards-page-layout:first-child {
                page-break-before: auto !important;
                break-before: auto !important;
            }
            .cards-page-layout:last-child {
                page-break-after: avoid !important;
                break-after: avoid !important;
            }
            .cut-line-vertical {
                height: 195mm;
                align-self: stretch;
                border-left: 1.5px dashed #000000;
                position: relative;
                display: flex;
                align-items: center;
                justify-content: center;
                margin: 0;
            }
            .cut-line-vertical-text {
                writing-mode: vertical-rl;
                transform: rotate(180deg);
                background: #ffffff;
                padding: 8px 0;
                font-size: 8.5px;
                font-weight: bold;
                color: #000000;
            }
            .report-card-container {
                width: calc(50% - 6mm);
                height: 195mm;
                max-height: 195mm;
                border: 2px solid #000000;
                padding: 6px;
                display: flex;
                flex-direction: column;
                justify-content: flex-start;
                align-items: stretch;
                gap: 4px;
                background-color: #ffffff;
                box-sizing: border-box;
                overflow: hidden;
            }
            .header-section {
                display: flex;
                justify-content: space-between;
                align-items: flex-start;
                width: 100%;
                margin-bottom: 4px;
            }
            .header-column {
                flex: 1;
                font-size: 8px;
                line-height: 1.4;
            }
            .header-column.right {
                text-align: right;
                font-weight: bold;
            }
            .header-column.center {
                text-align: center;
                flex: 0.8;
            }
            .header-column.left {
                text-align: left;
                font-weight: bold;
            }
            .header-title-main {
                font-size: 11.5px;
                font-weight: 800;
                margin-top: 2px;
                color: #000000;
                text-transform: uppercase;
            }
            .header-subtitle {
                font-size: 9.5px;
                font-weight: bold;
                margin-top: 1px;
                color: #000000;
            }
            .student-info-grid {
                width: 100%;
                border-collapse: collapse;
                margin: 2px 0;
                font-size: 10.5px;
                font-weight: bold;
                border-bottom: 1.5px solid #000000;
                border-top: 1.5px solid #000000;
            }
            .student-info-grid td {
                padding: 2px 6px;
                height: 22px;
                border: none;
            }
            .content-split {
                display: flex;
                width: 100%;
                margin-top: 2px;
                gap: 6px;
                align-items: stretch;
                overflow: hidden;
            }
            .grades-section {
                flex: 0.76;
                display: flex;
                flex-direction: column;
                overflow: hidden;
            }
            .remarks-section {
                flex: 0.24;
                border: 1.5px solid #000000;
                display: flex;
                flex-direction: column;
                text-align: center;
                overflow: hidden;
                box-sizing: border-box;
            }
            .remarks-header {
                font-weight: bold;
                background-color: #ffffff;
                border-bottom: 1.5px solid #000000;
                padding: 4px;
                font-size: 10px;
                height: 22px;
                box-sizing: border-box;
            }
            .remarks-body {
                display: flex;
                align-items: center;
                justify-content: center;
                flex-grow: 1;
                font-size: 12px;
                font-weight: 800;
                padding: 6px;
                color: #000000;
            }
            .grades-table {
                width: 100%;
                border-collapse: collapse;
                font-size: 10px;
                border: 1.5px solid #000000;
            }
            .grades-table th {
                background-color: #ffffff;
                border: 1px solid #000000;
                padding: 2px;
                height: 20px;
                font-weight: bold;
                text-align: center;
                font-size: 9.5px;
                color: #000000;
            }
            .grades-table td {
                border: 1px solid #000000;
                padding: 2px;
                height: 18px;
                text-align: center;
                color: #000000;
            }
            .row-bg-accent {
                background-color: #ffffff;
            }
            .row-bg-highlight {
                background-color: #ffffff;
                font-weight: bold;
            }
            .row-bg-total {
                background-color: #ffffff;
                font-weight: bold;
            }
            
            .footer-signature-block {
                display: flex;
                justify-content: space-between;
                align-items: flex-start;
                padding: 4px 6px 0 6px;
                font-size: 10.5px;
                font-weight: bold;
                margin-top: 8px;
                color: #000000;
            }
            .footer-signature-item {
                text-align: center;
                flex: 1;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: flex-start;
                min-width: 0;
            }
            .footer-signature-title {
                margin-bottom: 3px;
                font-weight: 800;
                font-size: 10.5px;
                color: #000000;
            }
            .footer-signature-line {
                width: 75%;
                margin: 0 auto 4px auto;
                border-bottom: 1px dotted #000000;
            }
            .footer-stamp-slot {
                height: 50px;
                display: flex;
                align-items: center;
                justify-content: center;
                width: 100%;
            }
            
            /* Standalone List/Table styled sheets */
            .sheet-card {
                width: 100%;
                padding: 12px;
                border: 2px solid #000000;
                background-color: #ffffff;
                margin-bottom: 10px;
                box-sizing: border-box;
            }
            .sheet-title {
                text-align: center;
                font-size: 15px;
                font-weight: 900;
                margin: 4px 0 8px 0;
                text-decoration: underline;
                color: #000000;
            }
            .sheet-table {
                width: 100%;
                border-collapse: collapse;
                font-size: 10.5px;
                border: 1.5px solid #000000;
            }
            .sheet-table th {
                background-color: #ffffff;
                border: 1.5px solid #000000;
                padding: 5px;
                font-weight: bold;
                text-align: center;
                color: #000000;
            }
            .sheet-table td {
                border: 1.1px solid #000000;
                padding: 5px;
                text-align: center;
                color: #000000;
            }
            .sheet-footer-signals {
                display: flex;
                justify-content: space-between;
                margin-top: 25px;
                padding: 0 40px;
                font-size: 11px;
                font-weight: bold;
                color: #000000;
            }
            
            .print-footer {
                display: none;
            }
            
            /* Print style settings for lists (Excluding Report Cards) */
            @media print {
                .a4-page {
                    position: relative;
                }
                .sheet-table thead {
                    display: table-header-group !important;
                }
                .sheet-table tr {
                    page-break-inside: avoid !important;
                    break-inside: avoid !important;
                }
                .sheet-card {
                    border: none !important;
                    padding: 0 !important;
                    margin: 0 !important;
                    background-color: transparent !important;
                    display: block !important;
                }
                .print-footer {
                    display: none !important;
                }
                .print-footer .page-number::after {
                    content: "";
                }
            }
            
            /* Hymns and Cleaning Groups */
            .groups-grid {
                display: flex;
                flex-wrap: wrap;
                gap: 10px;
                width: 100%;
                margin-top: 10px;
            }
            .group-box {
                flex: 1;
                min-width: 18%;
                border: 1.5px solid #000000;
                border-radius: 4px;
                background-color: #ffffff;
                display: flex;
                flex-direction: column;
            }
            .group-box-title {
                background-color: #ffffff;
                border-bottom: 1.5px solid #000000;
                font-weight: bold;
                text-align: center;
                padding: 6px;
                font-size: 11px;
                color: #000000;
            }
            .group-box-content {
                padding: 0;
                margin: 0;
                list-style: none;
                flex-grow: 1;
            }
            .group-box-content li {
                padding: 5px 8px;
                border-bottom: 1px solid #000000;
                font-size: 9.5px;
                display: flex;
                justify-content: space-between;
                color: #000000;
            }
            .group-box-content li:last-child {
                border-bottom: none;
            }
            .group-item-number {
                font-weight: bold;
                color: #000000;
            }
        </style>
        """.trimIndent()
    }

    fun getRemarkString(avg: Double, viewModel: TeacherViewModel, classId: Long, level: Int): String {
        val is10 = viewModel.isOutOfTen.value
        val actualAvg = if (is10) avg / 2.0 else avg

        val fail = viewModel.getFailBoundForClass(classId, level)
        val pass = viewModel.getPassBoundForClass(classId)
        val acc = viewModel.getAcceptableBoundForClass(classId)
        val good = viewModel.getGoodBoundForClass(classId)
        val veryGood = viewModel.getVeryGoodBoundForClass(classId)

        return when {
            actualAvg < fail -> viewModel.getRemarkTextForClass(classId, level, 0)
            actualAvg < pass -> viewModel.getRemarkTextForClass(classId, level, 1)
            actualAvg < acc -> viewModel.getRemarkTextForClass(classId, level, 2)
            actualAvg < good -> viewModel.getRemarkTextForClass(classId, level, 3)
            actualAvg < veryGood -> viewModel.getRemarkTextForClass(classId, level, 4)
            else -> viewModel.getRemarkTextForClass(classId, level, 5)
        }
    }

    // Overload for backward compatibility / fallback
    fun getRemarkString(avg: Double, viewModel: TeacherViewModel? = null, termId: Int = 3): String {
        if (viewModel == null) {
            return when {
                avg >= 16.0 -> "ممتاز"
                avg >= 14.0 -> "جيد جداً"
                avg >= 12.0 -> "جيد"
                avg >= 10.0 -> "حسن"
                avg >= 8.0 -> "مقبول"
                else -> "ضعيف"
            }
        }
        val classId = viewModel.selectedClassId.value ?: 0L
        return getRemarkString(avg, viewModel, classId, 3)
    }

    // --- Official Stamps SVG Generation (ختم المعلم وختم المدير) ---

    /** Shrinks a font size just enough for the text to fit the given width (no clipping, no overflow). */
    private fun fitFont(text: String, maxWidth: Float, baseSize: Float, minSize: Float = 6f): Float {
        val estimated = text.length * baseSize * 0.40f
        if (estimated <= maxWidth || estimated <= 0f) return baseSize
        return (baseSize * maxWidth / estimated).coerceAtLeast(minSize)
    }

    fun generateCircularStampSvg(
        roleTitle: String,
        personName: String,
        schoolName: String,
        sizePx: Int = 60,
        financialId: String = ""
    ): String {
        val cleanSchool = schoolName.trim().ifBlank { "الطلحايه 1" }
        val formattedSchool = if (cleanSchool.startsWith("مدرسة")) cleanSchool else "مدرسة $cleanSchool"
        val cleanRole = when (roleTitle.trim()) {
            "معلم", "المعلم" -> "المعلم"
            "مدير", "المدير" -> "المدير"
            else -> roleTitle.trim().ifBlank { "المدير" }
        }
        val cleanName = personName.trim()
        val cleanFinancialId = financialId.trim()
        val directorDisplayName = if (cleanName.isNotBlank()) cleanName else "المدير"
        val bottomRoleLabel = if (cleanRole in listOf("مدير", "المدير")) "- المدير -" else "- $cleanRole -"
        val blueColor = "#153e90"

        val uid = java.util.UUID.randomUUID().toString().take(6)
        val topPathId = "topArc_$uid"
        val botPathId = "botArc_$uid"

        return """
        <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 200 200" width="${sizePx}px" height="${sizePx}px" style="font-family: 'Cairo', 'Amiri', Tahoma, sans-serif; display: inline-block; vertical-align: middle;">
            <defs>
                <!-- Top Arc: left (27,80.5) to right (173,80.5) over the top, radius 75.5 -->
                <path id="$topPathId" d="M 27,80.5 A 75.5,75.5 0 0,1 173,80.5" fill="none" />
                <!-- Bottom Arc: counter-clockwise from left (27,119.5) to right (173,119.5) with radius 75.5 -->
                <path id="$botPathId" d="M 27,119.5 A 75.5,75.5 0 0,0 173,119.5" fill="none" />
            </defs>
            <!-- Outer Double Circle Border -->
            <circle cx="100" cy="100" r="95" stroke="$blueColor" stroke-width="3.2" fill="none" />
            <circle cx="100" cy="100" r="89" stroke="$blueColor" stroke-width="1.2" fill="none" />
            <!-- Inner Circle Border -->
            <circle cx="100" cy="100" r="62" stroke="$blueColor" stroke-width="1.4" fill="none" />

            <!-- Circular Ring Curved Text -->
            <text fill="$blueColor" font-size="11" font-weight="900" font-family="'Cairo', 'Amiri', Tahoma, sans-serif">
                <textPath href="#$topPathId" xlink:href="#$topPathId" startOffset="50%" text-anchor="middle">الجمهورية الإسلامية الموريتانية</textPath>
            </text>
            <text fill="$blueColor" font-size="11" font-weight="bold" font-family="'Cairo', 'Amiri', Tahoma, sans-serif">
                <textPath href="#$botPathId" xlink:href="#$botPathId" startOffset="50%" text-anchor="middle">$bottomRoleLabel</textPath>
            </text>

            <!-- Separator dashes on left & right -->
            <text x="21" y="104" fill="$blueColor" font-size="14" font-weight="900" text-anchor="middle">-</text>
            <text x="179" y="104" fill="$blueColor" font-size="14" font-weight="900" text-anchor="middle">-</text>

            <!-- Inside Circle: School Name & Director Name -->
            <text x="100" y="86" fill="$blueColor" font-size="${fitFont(formattedSchool, 112f, 12f)}" font-weight="900" text-anchor="middle" font-family="'Cairo', 'Amiri', Tahoma, sans-serif">$formattedSchool</text>
            <line x1="68" y1="98" x2="132" y2="98" stroke="$blueColor" stroke-width="1.2" />
            <text x="100" y="117" fill="$blueColor" font-size="${fitFont(directorDisplayName, 112f, 11.5f)}" font-weight="bold" text-anchor="middle" font-family="'Cairo', 'Amiri', Tahoma, sans-serif">$directorDisplayName</text>
            ${if (cleanFinancialId.isNotBlank()) """<text x="100" y="131" fill="$blueColor" font-size="${fitFont(cleanFinancialId, 100f, 8.5f)}" font-weight="normal" text-anchor="middle" font-family="'Cairo', 'Amiri', Tahoma, sans-serif">($cleanFinancialId)</text>""" else ""}
        </svg>
        """.trimIndent()
    }

    fun generateStampSvg(
        roleTitle: String,
        personName: String,
        schoolName: String,
        shape: String = "rectangle",
        sizePx: Int = 54,
        financialId: String = ""
    ): String {
        if (shape.equals("circle", ignoreCase = true) || roleTitle.trim() in listOf("مدير", "المدير")) {
            return generateCircularStampSvg(
                roleTitle = roleTitle,
                personName = personName,
                schoolName = schoolName,
                sizePx = (sizePx * 1.15).toInt(),
                financialId = financialId
            )
        }
        val cleanSchool = schoolName.trim().ifBlank { "الطلحايه 1" }
        val formattedSchool = if (cleanSchool.startsWith("مدرسة")) cleanSchool else "مدرسة $cleanSchool"
        val cleanRole = when (roleTitle.trim()) {
            "معلم", "المعلم" -> "المعلم"
            "مدير", "المدير" -> "المدير"
            else -> roleTitle.trim().ifBlank { "المعلم" }
        }
        val cleanName = personName.trim()
        val cleanFinancialId = financialId.trim()

        val roleLine = when {
            cleanName.isNotBlank() && cleanFinancialId.isNotBlank() -> "$cleanRole: $cleanName ($cleanFinancialId)"
            cleanName.isNotBlank() -> "$cleanRole: $cleanName"
            else -> cleanRole
        }
        val blueColor = "#153e90"

        val width = (sizePx * 1.55).toInt()
        val height = sizePx
        return """
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 240 120" width="${width}px" height="${height}px" style="font-family: 'Cairo', 'Amiri', Tahoma, sans-serif; display: inline-block; vertical-align: middle;">
            <rect x="3" y="3" width="234" height="114" rx="6" stroke="$blueColor" stroke-width="3.5" fill="none" />
            <rect x="8" y="8" width="224" height="104" rx="4" stroke="$blueColor" stroke-width="1.5" fill="none" />
            <text x="120" y="32" fill="$blueColor" font-size="14" font-weight="900" text-anchor="middle" font-family="'Cairo', sans-serif">الجمهورية الإسلامية الموريتانية</text>
            <text x="20" y="62" fill="$blueColor" font-size="15" text-anchor="middle">★</text>
            <text x="120" y="62" fill="$blueColor" font-size="${fitFont(formattedSchool, 170f, 13f)}" font-weight="800" text-anchor="middle" font-family="'Cairo', sans-serif">$formattedSchool</text>
            <text x="220" y="62" fill="$blueColor" font-size="15" text-anchor="middle">★</text>
            <text x="120" y="94" fill="$blueColor" font-size="${fitFont(roleLine, 210f, 14f)}" font-weight="bold" text-anchor="middle" font-family="'Cairo', sans-serif">$roleLine</text>
        </svg>
        """.trimIndent()
    }

    fun getTeacherStampHtml(viewModel: TeacherViewModel, schoolName: String, sizePx: Int = 54): String {
        if (!viewModel.showTeacherStampInReports.value) return ""
        val name = viewModel.teacherStampName.value.trim()
        val role = "المعلم"
        val shape = "rectangle"
        return """
            <div style="display: flex; justify-content: center; align-items: center; transform: rotate(-2deg); opacity: 0.95;">
                ${generateStampSvg(
                    roleTitle = role,
                    personName = name,
                    schoolName = schoolName,
                    shape = shape,
                    sizePx = sizePx
                )}
            </div>
        """.trimIndent()
    }

    fun getPrincipalStampHtml(viewModel: TeacherViewModel, schoolName: String, sizePx: Int = 54): String {
        if (!viewModel.showPrincipalStampInReports.value) return ""
        val name = viewModel.principalStampName.value.trim()
        val role = "المدير"
        val shape = "circle"
        val financialId = viewModel.principalStampFinancialId.value
        return """
            <div style="display: flex; justify-content: center; align-items: center; transform: rotate(1.5deg); opacity: 0.95;">
                ${generateStampSvg(
                    roleTitle = role,
                    personName = name,
                    schoolName = schoolName,
                    shape = shape,
                    sizePx = sizePx,
                    financialId = financialId
                )}
            </div>
        """.trimIndent()
    }

    fun generateReportCardsHtml(
        context: Context,
        className: String,
        performances: List<StudentPerformance>,
        subjects: List<Subject>,
        activeTermId: Int,
        activeClass: ClassSection,
        viewModel: TeacherViewModel,
        targetStudentId: Long? = null,
        onePerPortraitPage: Boolean = false
    ): String {
        val sortedSubjects = subjects.sortedByOfficialOrder()
        val alphabeticalPerformances = performances.sortedBy { it.student.id }
        val isFinalExamMode = (activeTermId == 3 || activeTermId == 4)
        var sortedPerformances = if (isFinalExamMode) {
            performances.sortedWith(
                compareBy<StudentPerformance> {
                    val r = viewModel.calculateStudentGeneralRank(it.student.id, activeClass.id)
                    if (r == 0) Int.MAX_VALUE else r
                }.thenByDescending { viewModel.calculateStudentGeneralAverage(it.student.id, activeClass.id) }
            )
        } else {
            performances.sortedWith(
                compareBy<StudentPerformance> { if (it.rank == 0) Int.MAX_VALUE else it.rank }
                    .thenByDescending { it.averageScore }
            )
        }
        if (targetStudentId != null) {
            sortedPerformances = sortedPerformances.filter { it.student.id == targetStudentId }
        }
        val rankedCount = performances.size
        val generalRankedCount = performances.size
        val seal = getMauritanianSealImg(context)
        val styles = getCommonStyles(isLandscape = !onePerPortraitPage)
        val portraitSingleStyles = if (onePerPortraitPage) """
            <style>
            @page { size: portrait; margin: 6mm 8mm; }
            @media print { @page { size: portrait; margin: 6mm 8mm; } }
            html, body {
                margin: 0 !important;
                padding: 0 !important;
                background-color: #ffffff !important;
                -webkit-print-color-adjust: exact !important;
                print-color-adjust: exact !important;
            }
            .a4-page, .cards-page-layout {
                display: block !important;
                width: 100% !important;
                height: 275mm !important;
                max-height: 275mm !important;
                margin: 0 auto !important;
                padding: 0 !important;
                box-sizing: border-box !important;
                overflow: hidden !important;
                page-break-before: auto !important;
                break-before: auto !important;
                page-break-after: always !important;
                break-after: page !important;
                page-break-inside: avoid !important;
                break-inside: avoid !important;
            }
            .a4-page:last-child, .cards-page-layout:last-child {
                page-break-after: avoid !important;
                break-after: avoid !important;
            }
            .cut-line-vertical {
                display: none !important;
            }
            .report-card-container {
                width: 100% !important;
                height: 275mm !important;
                max-height: 275mm !important;
                box-sizing: border-box !important;
                border: 2px solid #000000 !important;
                padding: 6px 12px 4px 12px !important;
                display: flex !important;
                flex-direction: column !important;
                justify-content: flex-start !important;
                align-items: stretch !important;
                gap: 0 !important;
                overflow: hidden !important;
            }
            .header-section {
                flex-shrink: 0 !important;
                margin-bottom: 5px !important;
                padding-bottom: 5px !important;
                border-bottom: 2px solid #000000 !important;
            }
            .header-section img, .header-column img, .center img {
                width: 76px !important;
                height: 76px !important;
                margin: 0 auto 3px auto !important;
                object-fit: contain !important;
            }
            .header-column {
                font-size: 13px !important;
                line-height: 1.45 !important;
            }
            .header-column.right, .header-column.left {
                font-weight: bold !important;
                color: #000000 !important;
            }
            .header-title-main {
                font-size: 18.5px !important;
                font-weight: 900 !important;
                margin-top: 3px !important;
                color: #000000 !important;
                letter-spacing: -0.2px !important;
            }
            .header-subtitle {
                font-size: 13px !important;
                font-weight: bold !important;
                margin-top: 2px !important;
                color: #000000 !important;
            }
            .student-info-grid {
                flex-shrink: 0 !important;
                margin: 5px 0 7px 0 !important;
                font-size: 15px !important;
                font-weight: bold !important;
                border-top: 1.5px solid #000000 !important;
                border-bottom: 1.5px solid #000000 !important;
                background-color: #ffffff !important;
            }
            .student-info-grid td {
                padding: 6px 10px !important;
                height: 30px !important;
            }
            .content-split {
                flex: 0 0 auto !important;
                display: flex !important;
                gap: 12px !important;
                margin: 6px 0 10px 0 !important;
                align-items: stretch !important;
                box-sizing: border-box !important;
            }
            .grades-section {
                flex: 0 0 74% !important;
                width: 74% !important;
                display: flex !important;
                flex-direction: column !important;
                margin: 0 !important;
                padding: 0 !important;
                box-sizing: border-box !important;
            }
            .remarks-section {
                flex: 0 0 calc(26% - 12px) !important;
                width: calc(26% - 12px) !important;
                display: flex !important;
                flex-direction: column !important;
                border: 1.5px solid #000000 !important;
                border-radius: 2px !important;
                background-color: #ffffff !important;
                margin: 0 !important;
                box-sizing: border-box !important;
                overflow: hidden !important;
            }
            .grades-table {
                width: 100% !important;
                border-collapse: collapse !important;
                border: 1.5px solid #000000 !important;
                margin: 0 !important;
                box-sizing: border-box !important;
            }
            .grades-table th {
                font-size: 15px !important;
                font-weight: 900 !important;
                padding: 6px 8px !important;
                height: 30px !important;
                box-sizing: border-box !important;
                background-color: #ffffff !important;
                border: 1.5px solid #000000 !important;
                color: #000000 !important;
            }
            .grades-table td {
                font-size: 13.5px !important;
                padding: 5px 8px !important;
                border: 1px solid #000000 !important;
                color: #000000 !important;
            }
            .grades-table td.td-subject {
                font-weight: bold !important;
            }
            .grades-table td.td-grade {
                font-weight: 800 !important;
            }
            .grades-table tr.row-bg-total td {
                font-size: 14px !important;
                font-weight: 900 !important;
                background-color: #ffffff !important;
                color: #000000 !important;
                padding: 5.5px 8px !important;
            }
            .grades-table tr.row-term-avg td {
                font-size: 13.5px !important;
                font-weight: bold !important;
                color: #000000 !important;
                padding: 5px 8px !important;
            }
            .grades-table tr.row-bg-highlight td {
                font-size: 15px !important;
                font-weight: 900 !important;
                background-color: #ffffff !important;
                border-top: 1.5px solid #000000 !important;
                border-bottom: 1.5px solid #000000 !important;
                color: #000000 !important;
                padding: 6px 8px !important;
            }
            .grades-table tr.row-summary-decision td {
                font-size: 14px !important;
                font-weight: 900 !important;
                background-color: #ffffff !important;
                color: #000000 !important;
                padding: 5.5px 8px !important;
            }
            .grades-table tr.row-summary-rank td {
                font-size: 14px !important;
                font-weight: 900 !important;
                background-color: #ffffff !important;
                color: #000000 !important;
                border-bottom: 1.5px solid #000000 !important;
                padding: 5.5px 8px !important;
            }
            .remarks-header {
                font-size: 15px !important;
                font-weight: 900 !important;
                padding: 6px !important;
                height: 30px !important;
                box-sizing: border-box !important;
                background-color: #ffffff !important;
                border-bottom: 1.5px solid #000000 !important;
                text-align: center !important;
                color: #000000 !important;
            }
            .remarks-body {
                padding: 10px !important;
                flex: 1 1 auto !important;
                display: flex !important;
                flex-direction: column !important;
                align-items: center !important;
                justify-content: center !important;
                text-align: center !important;
                font-size: 20px !important;
                font-weight: 900 !important;
                color: #000000 !important;
                box-sizing: border-box !important;
            }
            .footer-signature-block {
                flex-shrink: 0 !important;
                margin-top: 16px !important;
                padding: 0 10px !important;
                border-top: none !important;
                display: flex !important;
                justify-content: space-around !important;
                align-items: flex-start !important;
            }
            .footer-signature-item {
                flex: 1 !important;
                text-align: center !important;
            }
            .footer-signature-title {
                font-size: 13.5px !important;
                font-weight: 900 !important;
                margin-bottom: 4px !important;
                color: #000000 !important;
            }
            .footer-signature-line {
                width: 80% !important;
                margin: 2px auto 6px auto !important;
                border-bottom: 1.5px dashed #000000 !important;
            }
            .footer-stamp-slot {
                height: 64px !important;
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
            }
            .footer-stamp-slot svg {
                max-height: 60px !important;
                max-width: 95% !important;
                display: block !important;
                margin: 0 auto !important;
            }
            </style>
        """ else ""
        
        val isOutOfTen = viewModel.isOutOfTen.value
        val scaleFactor = if (isOutOfTen) 2.0 else 1.0
        val scaleMax = if (isOutOfTen) 10 else 20
        
        val html = StringBuilder()
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>كشوف الدرجات</title>$styles$portraitSingleStyles</head><body>")

        val teacherStampHtml = getTeacherStampHtml(viewModel, activeClass.schoolName, sizePx = if (onePerPortraitPage) 64 else 46)
        val principalStampHtml = getPrincipalStampHtml(viewModel, activeClass.schoolName, sizePx = if (onePerPortraitPage) 64 else 46)
 
        // Group into chunks of 2, each representing an A4 landscape page
        val pages = sortedPerformances.chunked(if (onePerPortraitPage) 1 else 2)
        
        pages.forEachIndexed { pageIndex, pagePerformances ->
            html.append("<div class=\"a4-page cards-page-layout\">")
            
            pagePerformances.forEachIndexed { cardIndex, perf ->
                if (!onePerPortraitPage && cardIndex > 0) {
                    html.append("""
                    <div class="cut-line-vertical">
                        <span class="cut-line-vertical-text">✂️ مكان القطع بالمقص</span>
                    </div>
                    """)
                }
                val callNumber = alphabeticalPerformances.indexOfFirst { it.student.id == perf.student.id } + 1
                val totalMax = sortedSubjects.sumOf { it.maxPoints }
                val totalScore = sortedSubjects.sumOf { perf.gradesMap[it.id] ?: 0.0 }
                
                val remarkAvg = if (isFinalExamMode) {
                    viewModel.calculateStudentGeneralAverage(perf.student.id, activeClass.id)
                } else {
                    perf.averageScore
                }
                val remarkText = getRemarkString(remarkAvg, viewModel, activeClass.id, activeClass.level)

                val resultText = if (isFinalExamMode) {
                    val generalAverage = viewModel.calculateStudentGeneralAverage(perf.student.id, activeClass.id)
                    val generalAverageScaled = generalAverage / scaleFactor
                    val failBoundValue = viewModel.getFailBoundForClass(activeClass.id, activeClass.level)
                    val isPass = generalAverageScaled >= failBoundValue
                    if (isPass) "ناجح" else if (activeClass.level == 1) "متجاوز" else "راسب"
                } else {
                    val avg = perf.averageScore
                    val avgScaled = avg / scaleFactor
                    val failBoundValue = viewModel.getFailBoundForClass(activeClass.id, activeClass.level)
                    val isPass = avgScaled >= failBoundValue
                    if (isPass) "ناجح" else if (activeClass.level == 1) "متجاوز" else "راسب"
                }

                val remarksContentHtml = remarkText

                val studentNameStyle = if (onePerPortraitPage) "font-size: 15px; font-weight: 900; color: #000000;" else ""

                html.append("""
                <div class="report-card-container">
                    <!-- Header -->
                    <div class="header-section">
                        <div class="header-column right">
                            الجمهورية الإسلامية الموريتانية<br>
                            وزارة التربية وإصلاح النظام التعليمي<br>
                            الإدارة الجهوية: ${activeClass.wilaya.ifBlank { "غوركل" }}<br>
                            مفتشية مقاطعة: ${activeClass.moughataa.ifBlank { "لكصيبه 1" }}<br>
                            مدرسة: ${activeClass.schoolName.ifBlank { "الطلحايه 1" }}
                        </div>
                        <div class="header-column center">
                            $seal
                            <div class="header-title-main">
                                ${when (activeTermId) {
                                    1 -> "كشف الامتحان الأول"
                                    2 -> "كشف الامتحان الثاني"
                                    4 -> "كشف درجات الفصل الأخير"
                                    else -> "كشف الامتحان الثالث"
                                }}
                            </div>
                            ${if (activeTermId == 4) """<div class="header-subtitle">(امتحان التجاوز)</div>""" else ""}
                        </div>
                        <div class="header-column left">
                            شرف - إخاء - عدالة<br>
                            السنة الدراسية: ${activeClass.academicYear.ifBlank { "2025 - 2026" }}<br>
                            القسم: ${getDisplayClassName(activeClass)}<br>
                            الفصل: ${when(activeTermId) { 1 -> "الأول" 2 -> "الثاني" 4 -> "الأخير" else -> "الثالث" }}
                        </div>
                    </div>
                    
                    <!-- Student details -->
                    <table class="student-info-grid">
                        <tr>
                            <td>الاسم الكامل: <span style="$studentNameStyle">${perf.student.name}</span></td>
                            <td style="text-align: center;">رقم النداء: $callNumber</td>
                            <td style="text-align: left; padding-left: 15px;">الرقم المدرسي: <span style="display: inline-block; min-width: 60px; text-align: center; margin-left: 6px; ${if (perf.student.schoolId.isBlank()) "border-bottom: 1px dotted #000000;" else ""}">${perf.student.schoolId.ifBlank { "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" }}</span></td>
                        </tr>
                    </table>
                    
                    <!-- Main Body Layout: Side-by-Side Grades and Remarks Sections -->
                    <div class="content-split">
                        <div class="grades-section">
                            <table class="grades-table" style="width: 100%;">
                            <thead>
                                <tr>
                                    <th style="width: ${if (onePerPortraitPage) "65%" else "60%"};">المادة</th>
                                    <th style="width: ${if (onePerPortraitPage) "35%" else "40%"};">النقاط</th>
                                </tr>
                            </thead>
                            <tbody>
                """)

                sortedSubjects.forEach { sub ->
                    val score = perf.gradesMap[sub.id]
                    val maxPointsToShow = if (isOutOfTen) 10.0 else sub.maxPoints.toDouble()
                    val scoreValue = if (score != null) {
                        if (isOutOfTen) (score / sub.maxPoints.toDouble()) * 10.0 else score
                    } else null
                    val scoreStr = if (scoreValue != null) formatCleanNumber(scoreValue) else "-"
                    html.append("""
                        <tr>
                            <td class="td-subject" style="text-align: right; padding-right: 12px;">${sub.name}</td>
                            <td class="td-grade" style="direction: ltr;">${if (isOutOfTen) "10" else sub.maxPoints} / $scoreStr</td>
                        </tr>
                    """)
                }

                // Add Row for Totals
                val totalMaxToShow = if (isOutOfTen) sortedSubjects.size * 10 else totalMax
                val totalScoreToShow = if (isOutOfTen) {
                    sortedSubjects.sumOf { sub ->
                        perf.gradesMap[sub.id]?.let { s -> (s / sub.maxPoints.toDouble()) * 10.0 } ?: 0.0
                    }
                } else totalScore

                html.append("""
                    <tr class="row-bg-total">
                        <td class="td-subject" style="text-align: right; padding-right: 12px; font-weight: bold;">مجموع النقاط</td>
                        <td class="td-grade" style="direction: ltr; font-weight: bold;">$totalMaxToShow / ${formatCleanNumber(totalScoreToShow)}</td>
                    </tr>
                """)

                // Add Averages block depending on term
                if (isFinalExamMode) {
                    val avg1 = viewModel.calculateStudentTermAverage(perf.student.id, activeClass.id, 1)
                    val avg2 = viewModel.calculateStudentTermAverage(perf.student.id, activeClass.id, 2)
                    val avg3 = perf.averageScore
                    val generalAverage = viewModel.calculateStudentGeneralAverage(perf.student.id, activeClass.id)
                    val generalRank = viewModel.calculateStudentGeneralRank(perf.student.id, activeClass.id)
                    
                    val tot1 = viewModel.calculateStudentTermTotalScore(perf.student.id, activeClass.id, 1)
                    val tot2 = viewModel.calculateStudentTermTotalScore(perf.student.id, activeClass.id, 2)
                    val grandTotalScore = tot1.first + tot2.first + totalScoreToShow
                    val grandTotalMax = tot1.second + tot2.second + totalMaxToShow
                    
                    val generalAverageScaled = generalAverage / scaleFactor

                    html.append("""
                        <tr class="row-term-avg">
                            <td class="td-subject" style="text-align: right; padding-right: 12px;">معدل الامتحان الأول</td>
                            <td class="td-grade" style="direction: ltr;">$scaleMax / ${formatCleanNumber(avg1 / scaleFactor)}</td>
                        </tr>
                        <tr class="row-term-avg">
                            <td class="td-subject" style="text-align: right; padding-right: 12px;">معدل الامتحان الثاني</td>
                            <td class="td-grade" style="direction: ltr;">$scaleMax / ${formatCleanNumber(avg2 / scaleFactor)}</td>
                        </tr>
                        <tr class="row-term-avg">
                            <td class="td-subject" style="text-align: right; padding-right: 12px;">معدل الامتحان الثالث</td>
                            <td class="td-grade" style="direction: ltr;">$scaleMax / ${formatCleanNumber(avg3 / scaleFactor)}</td>
                        </tr>
                        <tr class="row-bg-highlight">
                            <td class="td-subject" style="text-align: right; padding-right: 12px; color: #000000;">المعدل العام (السنوي)</td>
                            <td class="td-grade" style="direction: ltr; color: #000000;">$scaleMax / ${formatCleanNumber(generalAverageScaled)}</td>
                        </tr>
                        <tr class="row-summary-decision">
                            <td class="td-subject" style="text-align: right; padding-right: 12px;">القرار أو الملاحظة</td>
                            <td class="td-grade">$resultText</td>
                        </tr>
                        <tr class="row-summary-rank">
                            <td class="td-subject" style="text-align: right; padding-right: 12px;">الرتبة العامة في القسم</td>
                            <td class="td-grade" style="direction: ltr;">${if (generalRank > 0) "$generalRankedCount / $generalRank" else "-"}</td>
                        </tr>
                    """)
                } else {
                    val avg = perf.averageScore
                    val avgScaled = avg / scaleFactor
                    
                    html.append("""
                        <tr class="row-bg-highlight">
                            <td class="td-subject" style="text-align: right; padding-right: 12px; color: #000000;">معدل الفصل</td>
                            <td class="td-grade" style="direction: ltr; color: #000000;">$scaleMax / ${formatCleanNumber(avgScaled)}</td>
                        </tr>
                        <tr class="row-summary-decision">
                            <td class="td-subject" style="text-align: right; padding-right: 12px;">النتيجة</td>
                            <td class="td-grade">$resultText</td>
                        </tr>
                        <tr class="row-summary-rank">
                            <td class="td-subject" style="text-align: right; padding-right: 12px;">الرتبة في القسم</td>
                            <td class="td-grade" style="direction: ltr;">${if (perf.rank > 0) "$rankedCount / ${perf.rank}" else "-"}</td>
                        </tr>
                    """)
                }

                html.append("""
                                </tbody>
                            </table>
                        </div>
                        
                        <!-- Remarks section side-by-side -->
                        <div class="remarks-section">
                            <div class="remarks-header">الملاحظات والقرار</div>
                            <div class="remarks-body" style="color: #000000;">$remarksContentHtml</div>
                        </div>
                    </div>
                    
                    <!-- Signatures block directly below the split content -->
                    <div class="footer-signature-block">
                        <div class="footer-signature-item">
                            <div class="footer-signature-title">توقيع المعلم</div>
                            <div class="footer-signature-line"></div>
                            <div class="footer-stamp-slot">
                                $teacherStampHtml
                            </div>
                        </div>
                        <div class="footer-signature-item">
                            <div class="footer-signature-title">توقيع الوكيل</div>
                            <div class="footer-signature-line"></div>
                            <div class="footer-stamp-slot"></div>
                        </div>
                        <div class="footer-signature-item">
                            <div class="footer-signature-title">توقيع المدير</div>
                            <div class="footer-signature-line"></div>
                            <div class="footer-stamp-slot">
                                $principalStampHtml
                            </div>
                        </div>
                    </div>
                </div>
                """)
            }
            
            if (!onePerPortraitPage && pagePerformances.size == 1) {
                html.append("<div class=\"report-card-container\" style=\"visibility: hidden; border: none;\"></div>")
            }
            
            html.append("</div>") // Close a4-page
        }

        html.append("</body></html>")
        return html.toString()
    }

    // 2. Class Student List (Excluding parent phones)
    fun generateStudentDirectoryHtml(
        context: Context,
        className: String,
        students: List<Student>,
        activeClass: ClassSection,
        viewModel: TeacherViewModel? = null
    ): String {
        val teacherStampHtml = if (viewModel != null) getTeacherStampHtml(viewModel, activeClass.schoolName, sizePx = 46) else ""
        val principalStampHtml = if (viewModel != null) getPrincipalStampHtml(viewModel, activeClass.schoolName, sizePx = 46) else ""
        val sortedStudents = students.sortedBy { it.id }
        val seal = getMauritanianSealImg(context)
        val styles = getCommonStyles(
            isLandscape = false,
            isList = true,
            schoolName = activeClass.schoolName,
            fileTitle = "لائحة التلاميذ"
        )
        
        val html = StringBuilder()
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"format-detection\" content=\"telephone=no, date=no, address=no, email=no\"><title>لائحة التلاميذ</title>$styles</head><body>")
        
        html.append("<div class=\"sheet-card\">")
        
        // First page header (Full official header)
        html.append("""
        <div class="header-section">
            <div class="header-column right">
                الجمهورية الإسلامية الموريتانية<br>
                وزارة التربية وإصلاح النظام التعليمي<br>
                الإدارة الجهوية للتربية: ${activeClass.wilaya.ifBlank { "غوركل" }}<br>
                مفتشية مقاطعة: ${activeClass.moughataa.ifBlank { "لكصيبه 1" }}<br>
                مدرسة: ${activeClass.schoolName.ifBlank { "الطلحايه 1" }}
            </div>
            <div class="header-column center">
                $seal
                <div class="header-title-main" style="margin-top: 6px;">لائحة التلاميذ</div>
            </div>
            <div class="header-column left">
                شرف - إخاء - عدالة<br>
                السنة الدراسية: ${activeClass.academicYear.ifBlank { "2025 - 2026" }}<br>
                القسم: ${getDisplayClassName(activeClass)}
            </div>
        </div>
        <div class="sheet-title">اللائحة الرسمية لتلاميذ القسم</div>
        """)
        
        html.append("""
        <table class="sheet-table">
            <thead>
                <tr>
                    <th style="width: 8%;">الرقم</th>
                    <th style="width: 25%;">الاسم الكامل (عربي)</th>
                    <th style="width: 25%;">الاسم بالفرنسية</th>
                    <th style="width: 14%;">الرقم المدرسي</th>
                    <th style="width: 14%;">الرقم الوطني</th>
                    <th style="width: 14%;">الملاحظات الصحية</th>
                </tr>
            </thead>
            <tbody>
        """)
        
        sortedStudents.forEachIndexed { index, student ->
            html.append("""
                <tr class="${if (index % 2 == 1) "row-bg-accent" else ""}">
                    <td>${index + 1}</td>
                    <td style="text-align: right; font-weight: bold; padding-right: 6px;">${student.name}</td>
                    <td style="text-align: left; padding-left: 6px;">${student.frenchName}</td>
                    <td><span style="display: inline-block; min-width: 45px; text-align: center; ${if (student.schoolId.isBlank()) "border-bottom: 1px dotted #000000;" else ""}">${student.schoolId.ifBlank { "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" }}</span></td>
                    <td>${student.nationalId}</td>
                    <td>${student.healthNotes.ifBlank { "سليم" }}</td>
                </tr>
            """)
        }
        
        html.append("""
            </tbody>
        </table>
        """)
        
        // Show signatures block at the end
        html.append("""
        <div class="sheet-footer-signals" style="margin-top: 25px; margin-bottom: 5px; page-break-inside: avoid; break-inside: avoid;">
            <div style="display: flex; flex-direction: column; align-items: center; min-width: 140px;">
                <div style="font-weight: 800; font-size: 11px;">توقيع المعلم</div>
                <div style="width: 80%; border-bottom: 1px dotted #000000; margin: 4px auto 0 auto; height: 35px;"></div>
                <div style="height: 46px; display: flex; align-items: center; justify-content: center;">
                    $teacherStampHtml
                </div>
            </div>
            <div style="display: flex; flex-direction: column; align-items: center; min-width: 140px;">
                <div style="font-weight: 800; font-size: 11px;">توقيع المدير</div>
                <div style="width: 80%; border-bottom: 1px dotted #000000; margin: 4px auto 0 auto; height: 35px;"></div>
                <div style="height: 46px; display: flex; align-items: center; justify-content: center;">
                    $principalStampHtml
                </div>
            </div>
        </div>
        """)
        
        html.append("</div>") // Close sheet-card
        
        html.append("</body></html>")
        return html.toString()
    }

    // French Grades Entry List
    fun generateFrenchGradesEntryHtml(
        context: Context,
        className: String,
        students: List<Student>,
        activeClass: ClassSection,
        frenchMaxPoints: Int,
        viewModel: TeacherViewModel? = null
    ): String {
        val teacherStampHtml = if (viewModel != null) getTeacherStampHtml(viewModel, activeClass.schoolName, sizePx = 46) else ""
        val principalStampHtml = if (viewModel != null) getPrincipalStampHtml(viewModel, activeClass.schoolName, sizePx = 46) else ""
        val sortedStudents = students.sortedBy { it.id }
        val seal = getMauritanianSealImg(context)
        val styles = getCommonStyles(
            isLandscape = false,
            isList = true,
            schoolName = activeClass.schoolName,
            fileTitle = "لائحة إدراج نتائج الفرنسية"
        )
        
        val html = StringBuilder()
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"format-detection\" content=\"telephone=no, date=no, address=no, email=no\"><title>لائحة إدراج نتائج الفرنسية</title>${styles}</head><body>")
        
        html.append("<div class=\"sheet-card\">")
        
        // First page header (Full official header)
        html.append("""
        <div class="header-section">
            <div class="header-column right">
                الجمهورية الإسلامية الموريتانية<br>
                وزارة التربية وإصلاح النظام التعليمي<br>
                الإدارة الجهوية للتربية: ${activeClass.wilaya.ifBlank { "غوركل" }}<br>
                مفتشية مقاطعة: ${activeClass.moughataa.ifBlank { "لكصيبه 1" }}<br>
                مدرسة: ${activeClass.schoolName.ifBlank { "الطلحايه 1" }}
            </div>
            <div class="header-column center">
                ${seal}
                <div class="header-title-main" style="margin-top: 6px;">لائحة إدراج نتائج الفرنسية</div>
            </div>
            <div class="header-column left">
                شرف - إخاء - عدالة<br>
                السنة الدراسية: ${activeClass.academicYear.ifBlank { "2025 - 2026" }}<br>
                القسم: ${getDisplayClassName(activeClass)}
            </div>
        </div>
        <div class="sheet-title">جدول رصد درجات مادة اللغة الفرنسية يدوياً</div>
        <div style="text-align: center; font-size: 11px; color: #555; margin-bottom: 12px; font-weight: bold;">
            (يسحب هذا الجدول ويسلم لمعلم اللغة الفرنسية لتدوين النتائج يدوياً، ثم يعاد تسليمه للمعلم المسير لإدخالها في التطبيق)
        </div>
        """)
        
        html.append("""
        <table class="sheet-table">
            <thead>
                <tr>
                    <th style="width: 15%; font-size: 12px; padding: 8px 4px;">رقم النداء (N°)</th>
                    <th style="width: 32%; font-size: 12px; padding: 8px 4px;">الاسم الكامل بالعربية</th>
                    <th style="width: 33%; font-size: 12px; padding: 8px 4px;">Nom de l'élève (Français)</th>
                    <th style="width: 20%; font-size: 12px; padding: 8px 4px; background-color: #f1f1f1; border-bottom: 2px solid #333;">خانة النتيجة (Note /${frenchMaxPoints})</th>
                </tr>
            </thead>
            <tbody>
        """)
        
        sortedStudents.forEachIndexed { index, student ->
            val isAccent = if (index % 2 == 1) "row-bg-accent" else ""
            html.append("""
                <tr class="${isAccent}">
                    <td style="font-weight: bold; font-size: 12px; padding: 10px 4px;">${index + 1}</td>
                    <td style="text-align: right; font-weight: bold; padding: 10px 6px; font-size: 12px;">${student.name}</td>
                    <td style="text-align: left; padding: 10px 6px; font-family: monospace, sans-serif; font-size: 12px;">${student.frenchName}</td>
                    <td style="background-color: #fafafa; border: 1.5px solid #666; height: 35px;"></td>
                </tr>
            """)
        }
        
        html.append("""
            </tbody>
        </table>
        """)
        
        // Show signatures block at the end
        html.append("""
        <div class="sheet-footer-signals" style="margin-top: 25px; margin-bottom: 5px; page-break-inside: avoid; break-inside: avoid;">
            <div style="display: flex; flex-direction: column; align-items: center; min-width: 140px;">
                <div style="font-weight: 800; font-size: 11px;">توقيع معلم الفرنسية</div>
                <div style="width: 80%; border-bottom: 1px dotted #000000; margin: 4px auto 0 auto; height: 35px;"></div>
                <div style="height: 46px; display: flex; align-items: center; justify-content: center;">
                    $teacherStampHtml
                </div>
            </div>
            <div style="display: flex; flex-direction: column; align-items: center; min-width: 140px;">
                <div style="font-weight: 800; font-size: 11px;">توقيع المعلم المدير/المسير</div>
                <div style="width: 80%; border-bottom: 1px dotted #000000; margin: 4px auto 0 auto; height: 35px;"></div>
                <div style="height: 46px; display: flex; align-items: center; justify-content: center;">
                    $principalStampHtml
                </div>
            </div>
        </div>
        """)
        
        html.append("</div>") // Close sheet-card
        
        html.append("</body></html>")
        return html.toString()
    }

    // 3. Hymns Groups (5 groups)
    fun generateHymnsGroupsHtml(
        context: Context,
        className: String,
        students: List<Student>,
        activeClass: ClassSection,
        viewModel: TeacherViewModel? = null
    ): String {
        val teacherStampHtml = if (viewModel != null) getTeacherStampHtml(viewModel, activeClass.schoolName, sizePx = 46) else ""
        val principalStampHtml = if (viewModel != null) getPrincipalStampHtml(viewModel, activeClass.schoolName, sizePx = 46) else ""
        val sortedStudents = students.sortedBy { it.id }
        val seal = getMauritanianSealImg(context)
        val styles = getCommonStyles(isLandscape = false)
        
        // Group students into 5 lists
        val groups = List(5) { mutableListOf<Student>() }
        sortedStudents.forEachIndexed { index, student ->
            groups[index % 5].add(student)
        }

        val html = StringBuilder()
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>لائحة الأناشيد</title>$styles</head><body>")
        html.append("<div class=\"a4-page\"><div class=\"sheet-card\">")
        
        html.append("""
        <div>
            <!-- Header -->
            <div class="header-section">
                <div class="header-column right">
                    الجمهورية الإسلامية الموريتانية<br>
                    وزارة التربية وإصلاح النظام التعليمي<br>
                    الإدارة الجهوية للتربية: ${activeClass.wilaya.ifBlank { "غوركل" }}<br>
                    مفتشية مقاطعة: ${activeClass.moughataa.ifBlank { "لكصيبه 1" }}<br>
                    مدرسة: ${activeClass.schoolName.ifBlank { "الطلحايه 1" }}
                </div>
                <div class="header-column center">
                    $seal
                    <div class="header-title-main" style="margin-top: 6px;">مجموعات النشيد المدرسي</div>
                </div>
                <div class="header-column left">
                    شرف - إخاء - عدالة<br>
                    السنة الدراسية: ${activeClass.academicYear.ifBlank { "2025 - 2026" }}<br>
                    القسم: ${getDisplayClassName(activeClass)}
                </div>
            </div>
            
            <div class="sheet-title" style="margin-bottom: 20px;">توزيع طلاب القسم إلى خمس مجموعات للإنشاد</div>
            
            <div class="groups-grid">
        """)

        val groupTitles = listOf("المجموعة الأولى", "المجموعة الثانية", "المجموعة الثالثة", "المجموعة الرابعة", "المجموعة الخامسة")
        val bgColors = listOf("#ffffff", "#ffffff", "#ffffff", "#ffffff", "#ffffff")

        for (i in 0 until 5) {
            html.append("""
                <div class="group-box" style="border-color: #000000;">
                    <div class="group-box-title" style="background-color: ${bgColors[i]}; border-bottom-color: #000000; font-size: 11px;">
                        ${groupTitles[i]} (${groups[i].size} طالباً)
                    </div>
                    <ul class="group-box-content">
            """)
            
            groups[i].forEachIndexed { itemIndex, student ->
                val callNumber = sortedStudents.indexOfFirst { it.id == student.id } + 1
                html.append("""
                    <li>
                        <span class="group-item-number">$callNumber -</span>
                        <span style="font-weight: bold; text-align: right; flex-grow: 1; padding-right: 4px;">${student.name}</span>
                    </li>
                """)
            }
            
            html.append("</ul></div>")
        }

        html.append("""
            </div>
        </div>
        
        <div class="sheet-footer-signals" style="margin-top: 25px; margin-bottom: 5px; page-break-inside: avoid; break-inside: avoid;">
            <div style="display: flex; flex-direction: column; align-items: center; min-width: 140px;">
                <div style="font-weight: 800; font-size: 11px;">توقيع المعلم</div>
                <div style="width: 80%; border-bottom: 1px dotted #000000; margin: 4px auto 0 auto; height: 35px;"></div>
                <div style="height: 46px; display: flex; align-items: center; justify-content: center;">
                    $teacherStampHtml
                </div>
            </div>
            <div style="display: flex; flex-direction: column; align-items: center; min-width: 140px;">
                <div style="font-weight: 800; font-size: 11px;">توقيع المدير</div>
                <div style="width: 80%; border-bottom: 1px dotted #000000; margin: 4px auto 0 auto; height: 35px;"></div>
                <div style="height: 46px; display: flex; align-items: center; justify-content: center;">
                    $principalStampHtml
                </div>
            </div>
        </div>
        
        </div></div></body></html>
        """)

        return html.toString()
    }

    // 4. Cleaning Groups (5 groups Sunday - Thursday)
    fun generateCleaningGroupsHtml(
        context: Context,
        className: String,
        students: List<Student>,
        activeClass: ClassSection,
        viewModel: TeacherViewModel? = null
    ): String {
        val teacherStampHtml = if (viewModel != null) getTeacherStampHtml(viewModel, activeClass.schoolName, sizePx = 46) else ""
        val principalStampHtml = if (viewModel != null) getPrincipalStampHtml(viewModel, activeClass.schoolName, sizePx = 46) else ""
        val sortedStudents = students.sortedBy { it.id }
        val seal = getMauritanianSealImg(context)
        val styles = getCommonStyles(isLandscape = false)
        
        // Group students into 5 lists
        val groups = List(5) { mutableListOf<Student>() }
        sortedStudents.forEachIndexed { index, student ->
            groups[index % 5].add(student)
        }

        val html = StringBuilder()
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>لائحة الكناسة والنظافة</title>$styles</head><body>")
        html.append("<div class=\"a4-page\"><div class=\"sheet-card\">")
        
        html.append("""
        <div>
            <!-- Header -->
            <div class="header-section">
                <div class="header-column right">
                    الجمهورية الإسلامية الموريتانية<br>
                    وزارة التربية وإصلاح النظام التعليمي<br>
                    الإدارة الجهوية للتربية: ${activeClass.wilaya.ifBlank { "غوركل" }}<br>
                    مفتشية مقاطعة: ${activeClass.moughataa.ifBlank { "لكصيبه 1" }}<br>
                    مدرسة: ${activeClass.schoolName.ifBlank { "الطلحايه 1" }}
                </div>
                <div class="header-column center">
                    $seal
                    <div class="header-title-main" style="margin-top: 6px;">جدول النظافة والكناسة الدوري</div>
                </div>
                <div class="header-column left">
                    شرف - إخاء - عدالة<br>
                    السنة الدراسية: ${activeClass.academicYear.ifBlank { "2025 - 2026" }}<br>
                    القسم: ${getDisplayClassName(activeClass)}
                </div>
            </div>
            
            <div class="sheet-title" style="margin-bottom: 20px;">توزيع الطلاب إلى خمس مجموعات نظافة دورية للتطوير الصحي والتربوي</div>
            
            <div class="groups-grid">
        """)

        val groupTitles = listOf(
            "المجموعة الأولى (يوم الاثنين)",
            "المجموعة الثانية (يوم الثلاثاء)",
            "المجموعة الثالثة (يوم الأربعاء)",
            "المجموعة الرابعة (يوم الخميس)",
            "المجموعة الخامسة (يوم الجمعة)"
        )
        val bgColors = listOf("#ffffff", "#ffffff", "#ffffff", "#ffffff", "#ffffff")

        for (i in 0 until 5) {
            html.append("""
                <div class="group-box" style="border-color: #000000;">
                    <div class="group-box-title" style="background-color: ${bgColors[i]}; border-bottom-color: #000000; font-size: 11px;">
                        ${groupTitles[i]}<br>(${groups[i].size} طالباً)
                    </div>
                    <ul class="group-box-content">
            """)
            
            groups[i].forEachIndexed { itemIndex, student ->
                val callNumber = sortedStudents.indexOfFirst { it.id == student.id } + 1
                html.append("""
                    <li>
                        <span class="group-item-number">$callNumber -</span>
                        <span style="font-weight: bold; text-align: right; flex-grow: 1; padding-right: 4px;">${student.name}</span>
                    </li>
                """)
            }
            
            html.append("</ul></div>")
        }

        html.append("""
            </div>
        </div>
        
        <div class="sheet-footer-signals" style="margin-top: 25px; margin-bottom: 5px; page-break-inside: avoid; break-inside: avoid;">
            <div style="display: flex; flex-direction: column; align-items: center; min-width: 140px;">
                <div style="font-weight: 800; font-size: 11px;">توقيع وإمضاء المعلم</div>
                <div style="width: 80%; border-bottom: 1px dotted #000000; margin: 4px auto 0 auto; height: 35px;"></div>
                <div style="height: 46px; display: flex; align-items: center; justify-content: center;">
                    $teacherStampHtml
                </div>
            </div>
            <div style="display: flex; flex-direction: column; align-items: center; min-width: 140px;">
                <div style="font-weight: 800; font-size: 11px;">توقيع وإمضاء المدير</div>
                <div style="width: 80%; border-bottom: 1px dotted #000000; margin: 4px auto 0 auto; height: 35px;"></div>
                <div style="height: 46px; display: flex; align-items: center; justify-content: center;">
                    $principalStampHtml
                </div>
            </div>
        </div>
        
        </div></div></body></html>
        """)

        return html.toString()
    }

    // 5. Detailed Term Ledger (Landscape mode)
    fun generateDetailedTermLedgerHtml(
        context: Context,
        className: String,
        performances: List<StudentPerformance>,
        subjects: List<Subject>,
        activeTermId: Int,
        activeClass: ClassSection,
        viewModel: TeacherViewModel
    ): String {
        val sortedSubjects = subjects.sortedByOfficialOrder()
        val classStudents = viewModel.students.value.filter { it.classId == activeClass.id }.sortedBy { it.id }
        // Sort by rank and average score (first to last)
        val isFinalExamMode = (activeTermId == 3 || activeTermId == 4)
        val sortedPerformances = if (isFinalExamMode) {
            performances.sortedWith(
                compareBy<StudentPerformance> { viewModel.calculateStudentGeneralRank(it.student.id, activeClass.id) }
                    .thenByDescending { viewModel.calculateStudentGeneralAverage(it.student.id, activeClass.id) }
            )
        } else {
            performances.sortedWith(
                compareBy<StudentPerformance> { it.rank }
                    .thenByDescending { it.averageScore }
            )
        }
        
        val seal = getMauritanianSealImg(context)
        val styles = getCommonStyles(
            isLandscape = true,
            isList = true,
            schoolName = activeClass.schoolName,
            fileTitle = "دفتر كشف الدرجات التفصيلي"
        )
        
        val isOutOfTen = viewModel.isOutOfTen.value
        val scaleFactor = if (isOutOfTen) 2.0 else 1.0
        val scaleMax = if (isOutOfTen) 10 else 20
        
        val html = StringBuilder()
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"format-detection\" content=\"telephone=no, date=no, address=no, email=no\"><title>لائحة النتائج للقسم</title>$styles</head><body>")
        
        // Pre-calculate statistics for the entire section
        val failBoundValue = viewModel.getFailBoundForClass(activeClass.id, activeClass.level)
        var passCount = 0
        var failCount = 0
        sortedPerformances.forEach { perf ->
            val avgScaled = if (isFinalExamMode) {
                viewModel.calculateStudentGeneralAverage(perf.student.id, activeClass.id) / scaleFactor
            } else {
                perf.averageScore / scaleFactor
            }
            if (avgScaled >= failBoundValue) {
                passCount++
            } else {
                failCount++
            }
        }
        val totalCount = sortedPerformances.size
        val passPercentage = if (totalCount > 0) (passCount.toDouble() / totalCount * 100.0) else 0.0

        html.append("<div class=\"sheet-card\">")
        
        // First page header (Full official header)
        html.append("""
        <div class="header-section">
            <div class="header-column right" style="font-size: 9px;">
                الجمهورية الإسلامية الموريتانية<br>
                وزارة التربية وإصلاح النظام التعليمي<br>
                الإدارة الجهوية للتربية: ${activeClass.wilaya.ifBlank { "غوركل" }}<br>
                مفتشية مقاطعة: ${activeClass.moughataa.ifBlank { "لكصيبه 1" }}<br>
                مدرسة: ${activeClass.schoolName.ifBlank { "الطلحايه 1" }}
            </div>
            <div class="header-column center" style="flex: 1;">
                $seal
                <div class="header-title-main" style="font-size: 13px; margin-top: 4px;">
                    لائحة النتائج التفصيلية - ${when (activeTermId) {
                        1 -> "الفصل الأول"
                        2 -> "الفصل الثاني"
                        4 -> "الفصل الأخير والسنوي"
                        else -> "الفصل الثالث"
                    }}
                </div>
            </div>
            <div class="header-column left" style="font-size: 9px;">
                شرف - إخاء - عدالة<br>
                السنة الدراسية: ${activeClass.academicYear.ifBlank { "2025 - 2026" }}<br>
                القسم: ${getDisplayClassName(activeClass)}<br>
                العدد الكلي للطلاب: ${sortedPerformances.size} طالباً
            </div>
        </div>
        
        <div class="sheet-title" style="font-size: 13.5px; margin-top: 4px; margin-bottom: 8px;">جدول كشف رصد الدرجات التفصيلي والنتائج العامة</div>
        """)

        // Statistics block displayed above the table
        html.append("""
        <div style="margin-top: 5px; margin-bottom: 12px; display: flex; justify-content: space-around; border: 1.5px solid #000000; padding: 10px 15px; background-color: #ffffff; border-radius: 4px; font-weight: bold; font-size: 11px;">
            <div>👥 عدد الطلاب الكلي: <span style="font-weight: 800; font-size: 12.5px; color: #000000;">$totalCount</span></div>
            <div>✅ عدد الناجحين: <span style="font-weight: 800; font-size: 12.5px; color: #000000;">$passCount</span></div>
            <div>❌ عدد الراسبين: <span style="font-weight: 800; font-size: 12.5px; color: #000000;">$failCount</span></div>
            <div>📈 نسبة النجاح العامة: <span style="font-weight: 800; font-size: 12.5px; color: #000000;">${formatCleanNumber(passPercentage)}%</span></div>
        </div>
        """)
        
        html.append("""
        <table class="sheet-table">
            <thead>
                <tr>
                    <th style="width: 4%;">رقم</th>
                    <th style="width: 20%; text-align: right; padding-right: 8px;">الاسم الكامل للطلبة</th>
                    <th style="width: 8%;">الرقم المدرسي</th>
                    <th style="width: 10%;">الرقم الوطني</th>
        """)
        
        // Subject headers
        sortedSubjects.forEach { sub ->
            val maxToShow = if (isOutOfTen) "10" else sub.maxPoints.toString()
            html.append("<th style=\"font-size: 9.5px;\" title=\"${sub.name}\">${sub.name}<br><span style=\"font-size: 8px; font-weight: normal; color: #555;\">($maxToShow)</span></th>")
        }
        
        val totalMaxPoints = if (isOutOfTen) sortedSubjects.size * 10 else sortedSubjects.sumOf { it.maxPoints }

        html.append("""
                    <th style="width: 10%; background-color: #ffffff; color: #000000; font-size: 9.5px;">مجموع النقاط<br><span style="font-size: 8px; font-weight: normal; color: #555;">($totalMaxPoints)</span></th>
                    <th style="width: 8%; background-color: #ffffff; color: #000000;">المعدل / $scaleMax</th>
                    <th style="width: 6%;">الرتبة</th>
                    <th style="width: 10%;">الملاحظة</th>
                </tr>
            </thead>
            <tbody>
        """)
        
        sortedPerformances.forEachIndexed { index, perf ->
            val overallIndex = sortedPerformances.indexOfFirst { it.student.id == perf.student.id }
            val displayAverage = if (isFinalExamMode) {
                viewModel.calculateStudentGeneralAverage(perf.student.id, activeClass.id)
            } else {
                perf.averageScore
            }
            val displayRank = if (isFinalExamMode) {
                viewModel.calculateStudentGeneralRank(perf.student.id, activeClass.id)
            } else {
                perf.rank
            }
            val remarkText = getRemarkString(displayAverage, viewModel, activeClass.id, activeClass.level)
            
            val fallbackCallNumber = performances.sortedBy { it.student.id }.indexOfFirst { it.student.id == perf.student.id } + 1
            val callNumber = if (classStudents.isEmpty()) fallbackCallNumber else {
                val idx = classStudents.indexOfFirst { it.id == perf.student.id }
                if (idx != -1) idx + 1 else fallbackCallNumber
            }
            
            html.append("""
                <tr class="${if (overallIndex % 2 == 1) "row-bg-accent" else ""}">
                    <td>${overallIndex + 1}</td>
                    <td style="text-align: right; font-weight: bold; padding-right: 6px; font-size: 11px;">${callNumber} - ${perf.student.name}</td>
                    <td style="font-size: 10px;"><span style="display: inline-block; min-width: 45px; text-align: center; ${if (perf.student.schoolId.isBlank()) "border-bottom: 1px dotted #000000;" else ""}">${perf.student.schoolId.ifBlank { "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" }}</span></td>
                    <td style="font-size: 10px;">${perf.student.nationalId.ifBlank { "-" }}</td>
            """)
            
            // Score for each subject
            sortedSubjects.forEach { sub ->
                val score = perf.gradesMap[sub.id]
                val scoreValue = if (score != null) {
                    if (isOutOfTen) (score / sub.maxPoints.toDouble()) * 10.0 else score
                } else null
                val scoreStr = if (scoreValue != null) formatCleanNumber(scoreValue) else "-"
                html.append("<td>$scoreStr</td>")
            }
            
            val totalAchievedScore = if (isOutOfTen) {
                sortedSubjects.sumOf { sub ->
                    perf.gradesMap[sub.id]?.let { (it / sub.maxPoints.toDouble()) * 10.0 } ?: 0.0
                }
            } else {
                sortedSubjects.sumOf { perf.gradesMap[it.id] ?: 0.0 }
            }

            html.append("""
                    <td style="background-color: #ffffff; color: #000000; font-weight: bold; font-size: 11px; direction: ltr;">$totalMaxPoints / ${formatCleanNumber(totalAchievedScore)}</td>
                    <td style="background-color: #ffffff; color: #000000; font-weight: bold; font-size: 11.5px;">${formatCleanNumber(displayAverage / scaleFactor)}</td>
                    <td style="font-weight: bold; direction: ltr;">${sortedPerformances.size} / $displayRank</td>
                    <td style="font-weight: bold; font-size: 9.5px;">$remarkText</td>
                </tr>
            """)
        }
        
        html.append("""
            </tbody>
        </table>
        """)
        
        // Signoff block only on the last page
        val teacherStampHtml = getTeacherStampHtml(viewModel, activeClass.schoolName, sizePx = 60)
        val principalStampHtml = getPrincipalStampHtml(viewModel, activeClass.schoolName, sizePx = 60)

        html.append("""
        <div class="sheet-footer-signals" style="margin-top: 15px; margin-bottom: 5px; page-break-inside: avoid; break-inside: avoid;">
            <div style="display: flex; flex-direction: column; align-items: center; min-width: 140px;">
                <div style="font-weight: 800; font-size: 11px;">توقيع معلم القسم</div>
                <div style="width: 80%; border-bottom: 1px dotted #000000; margin: 3px auto 4px auto;"></div>
                <div style="height: 60px; display: flex; align-items: center; justify-content: center;">
                    $teacherStampHtml
                </div>
            </div>
            <div style="display: flex; flex-direction: column; align-items: center; min-width: 140px;">
                <div style="font-weight: 800; font-size: 11px;">توقيع مدير المدرسة</div>
                <div style="width: 80%; border-bottom: 1px dotted #000000; margin: 3px auto 4px auto;"></div>
                <div style="height: 60px; display: flex; align-items: center; justify-content: center;">
                    $principalStampHtml
                </div>
            </div>
        </div>
        """)
        
        html.append("</div>") // Close sheet-card
        
        html.append("</body></html>")
        return html.toString()
    }

    // 6. Annual Averages table after completing three terms (Landscape) - This is the "لائحة نتائج امتحان التجاوز"
    fun generateAnnualAveragesHtml(
        context: Context,
        className: String,
        students: List<Student>,
        activeClass: ClassSection,
        viewModel: TeacherViewModel
    ): String {
        val classStudents = students.filter { it.classId == activeClass.id }
        
        val isOutOfTen = viewModel.isOutOfTen.value
        val scaleFactor = if (isOutOfTen) 2.0 else 1.0
        val scaleMax = if (isOutOfTen) 10 else 20
        val isWeighted = viewModel.useFinalExamFormula.value
        
        // Compute averages and sort students based on Weighted Annual rank
        data class AnnualStudentRow(
            val student: Student,
            val avg1: Double,
            val avg2: Double,
            val avg3: Double,
            val annualAverage: Double,
            var annualRank: Int = 0,
            var callNumber: Int = 0
        )

        val rows = classStudents.sortedBy { it.id }.mapIndexed { idx, student ->
            val avg1 = viewModel.calculateStudentTermAverage(student.id, activeClass.id, 1)
            val avg2 = viewModel.calculateStudentTermAverage(student.id, activeClass.id, 2)
            val avg3 = viewModel.calculateStudentTermAverage(student.id, activeClass.id, 3)
            val annualAvg = if (isWeighted) {
                (avg1 * 1.0 + avg2 * 2.0 + avg3 * 3.0) / 6.0
            } else {
                (avg1 + avg2 + avg3) / 3.0
            }
            AnnualStudentRow(
                student = student,
                avg1 = avg1,
                avg2 = avg2,
                avg3 = avg3,
                annualAverage = annualAvg,
                callNumber = idx + 1
            )
        }

        // Calculate rankings
        val sortedByAnnualAsc = rows.sortedByDescending { it.annualAverage }
        var currentRank = 1
        var prevAvg = -1.0
        sortedByAnnualAsc.forEachIndexed { i, studentRow ->
            if (studentRow.annualAverage != prevAvg) {
                currentRank = i + 1
                prevAvg = studentRow.annualAverage
            }
            studentRow.annualRank = currentRank
        }

        // Sort by annual rank (first to last) and average score
        val sortedRowList = rows.sortedWith(
            compareBy<AnnualStudentRow> { it.annualRank }
                .thenByDescending { it.annualAverage }
        )

        // Pre-calculate statistics for the entire section
        val failBoundValue = viewModel.getFailBoundForClass(activeClass.id, activeClass.level)
        var passCount = 0
        var failCount = 0
        sortedRowList.forEach { row ->
            val annualAvgScaled = row.annualAverage / scaleFactor
            if (annualAvgScaled >= failBoundValue) {
                passCount++
            } else {
                failCount++
            }
        }
        val totalCount = sortedRowList.size
        val passPercentage = if (totalCount > 0) (passCount.toDouble() / totalCount * 100.0) else 0.0

        val seal = getMauritanianSealImg(context)
        val styles = getCommonStyles(
            isLandscape = true,
            isList = true,
            schoolName = activeClass.schoolName,
            fileTitle = "دفتر كشف المعدلات السنوي ورتب التجاوز"
        )
        
        val html = StringBuilder()
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"format-detection\" content=\"telephone=no, date=no, address=no, email=no\"><title>لائحة نتائج امتحان التجاوز</title>$styles</head><body>")
        
        html.append("<div class=\"sheet-card\">")
        
        // First page header (Full official header)
        html.append("""
        <div class="header-section">
            <div class="header-column right" style="font-size: 9px;">
                الجمهورية الإسلامية الموريتانية<br>
                وزارة التربية وإصلاح النظام التعليمي<br>
                الإدارة الجهوية للتربية: ${activeClass.wilaya.ifBlank { "غوركل" }}<br>
                مفتشية مقاطعة: ${activeClass.moughataa.ifBlank { "لكصيبه 1" }}<br>
                مدرسة: ${activeClass.schoolName.ifBlank { "الطلحايه 1" }}
            </div>
            <div class="header-column center" style="flex: 1;">
                $seal
                <div class="header-title-main" style="font-size: 13px; margin-top: 4px;">لائحة نتائج امتحان التجاوز</div>
            </div>
            <div class="header-column left" style="font-size: 9px;">
                شرف - إخاء - عدالة<br>
                السنة الدراسية: ${activeClass.academicYear.ifBlank { "2025 - 2026" }}<br>
                القسم: ${getDisplayClassName(activeClass)}<br>
                العدد الكلي للطلاب: ${sortedRowList.size} طالباً
            </div>
        </div>
        
        <div class="sheet-title" style="font-size: 13.5px; margin-top: 4px; margin-bottom: 8px;">محضر نتائج امتحانات التجاوز والمعدلات الإجمالية النهائية</div>
        """)

        // Statistics block displayed above the table
        html.append("""
        <div style="margin-top: 5px; margin-bottom: 12px; display: flex; justify-content: space-around; border: 1.5px solid #000000; padding: 10px 15px; background-color: #ffffff; border-radius: 4px; font-weight: bold; font-size: 11px;">
            <div>👥 عدد الطلاب الكلي: <span style="font-weight: 800; font-size: 12.5px; color: #000000;">$totalCount</span></div>
            <div>✅ عدد الناجحين: <span style="font-weight: 800; font-size: 12.5px; color: #000000;">$passCount</span></div>
            <div>❌ عدد الراسبين: <span style="font-weight: 800; font-size: 12.5px; color: #000000;">$failCount</span></div>
            <div>📈 نسبة النجاح العامة: <span style="font-weight: 800; font-size: 12.5px; color: #000000;">${formatCleanNumber(passPercentage)}%</span></div>
        </div>
        """)
        
        html.append("""
        <table class="sheet-table">
            <thead>
                <tr>
                    <th style="width: 6%;">رقم النداء</th>
                    <th style="width: 23%; text-align: right; padding-right: 12px;">الاسم الكامل للطلبة</th>
                    <th style="width: 9%;">الرقم المدرسي</th>
                    <th style="width: 11%;">الرقم الوطني</th>
                    <th style="width: 12%; background-color: #ffffff; color: #000000;">معدل الامتحان الأول ${if (isWeighted) "(ضارب 1)" else ""}</th>
                    <th style="width: 12%; background-color: #ffffff; color: #000000;">معدل الامتحان الثاني ${if (isWeighted) "(ضارب 2)" else ""}</th>
                    <th style="width: 12%; background-color: #ffffff; color: #000000;">معدل امتحان التجاوز ${if (isWeighted) "(ضارب 3)" else ""}</th>
                    <th style="width: 9%; background-color: #ffffff; color: #000000; font-size: 11px;">المعدل العام / $scaleMax</th>
                    <th style="width: 6%;">الرتبة</th>
                    <th style="width: 5%;">الملاحظة</th>
                </tr>
            </thead>
            <tbody>
        """)
        
        sortedRowList.forEachIndexed { index, row ->
            val overallIndex = sortedRowList.indexOfFirst { it.student.id == row.student.id }
            val annualAvgScaled = row.annualAverage / scaleFactor
            val failBoundValue = viewModel.getFailBoundForClass(activeClass.id, activeClass.level)
            val isPass = annualAvgScaled >= failBoundValue
            val resultText = if (isPass) "ناجح" else {
                if (activeClass.level == 1) "متجاوز" else "راسب"
            }
            val remarkText = getRemarkString(row.annualAverage, viewModel, activeClass.id, activeClass.level)
            val badgeColor = "#ffffff"
            
            html.append("""
                <tr class="${if (overallIndex % 2 == 1) "row-bg-accent" else ""}">
                    <td>${row.callNumber}</td>
                    <td style="text-align: right; font-weight: bold; padding-right: 12px; font-size: 11px;">${row.student.name}</td>
                    <td style="font-size: 10px;"><span style="display: inline-block; min-width: 45px; text-align: center; ${if (row.student.schoolId.isBlank()) "border-bottom: 1px dotted #000000;" else ""}">${row.student.schoolId.ifBlank { "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" }}</span></td>
                    <td style="font-size: 10px;">${row.student.nationalId.ifBlank { "-" }}</td>
                    <td>${formatCleanNumber(row.avg1 / scaleFactor)}</td>
                    <td>${formatCleanNumber(row.avg2 / scaleFactor)}</td>
                    <td>${formatCleanNumber(row.avg3 / scaleFactor)}</td>
                    <td style="background-color: #ffffff; color: #000000; font-weight: 800; font-size: 12px; border-width: 1.5px;">${formatCleanNumber(annualAvgScaled)}</td>
                    <td style="font-weight: bold; font-size: 11px; direction: ltr;">${sortedRowList.size} / ${row.annualRank}</td>
                    <td style="font-weight: bold; padding: 4px; background-color: $badgeColor; font-size: 10px;">
                        $resultText - $remarkText
                    </td>
                </tr>
            """)
        }
        
        html.append("""
            </tbody>
        </table>
        """)
        
        // Signatures block at the end
        val teacherStampHtml = getTeacherStampHtml(viewModel, activeClass.schoolName, sizePx = 60)
        val principalStampHtml = getPrincipalStampHtml(viewModel, activeClass.schoolName, sizePx = 60)

        html.append("""
        <div class="sheet-footer-signals" style="margin-top: 15px; margin-bottom: 5px; page-break-inside: avoid; break-inside: avoid;">
            <div style="display: flex; flex-direction: column; align-items: center; min-width: 140px;">
                <div style="font-weight: 800; font-size: 11px;">توقيع وإمضاء المعلم</div>
                <div style="width: 80%; border-bottom: 1px dotted #000000; margin: 3px auto 4px auto;"></div>
                <div style="height: 60px; display: flex; align-items: center; justify-content: center;">
                    $teacherStampHtml
                </div>
            </div>
            <div style="display: flex; flex-direction: column; align-items: center; min-width: 140px;">
                <div style="font-weight: 800; font-size: 11px;">توقيع وإمضاء المدير</div>
                <div style="width: 80%; border-bottom: 1px dotted #000000; margin: 3px auto 4px auto;"></div>
                <div style="height: 60px; display: flex; align-items: center; justify-content: center;">
                    $principalStampHtml
                </div>
            </div>
        </div>
        """)
        
        html.append("</div>") // Close sheet-card
        
        html.append("</body></html>")
        return html.toString()
    }

    fun generateAveragesOnlyReportCardsHtml(
        context: Context,
        className: String,
        students: List<Student>,
        activeClass: ClassSection,
        viewModel: TeacherViewModel,
        targetStudentId: Long? = null
    ): String {
        val classStudents = students.filter { it.classId == activeClass.id }
        val alphabeticalStudents = classStudents.sortedBy { it.id }
        
        val isOutOfTen = viewModel.isOutOfTen.value
        val scaleFactor = if (isOutOfTen) 2.0 else 1.0
        val scaleMax = if (isOutOfTen) 10 else 20
        val isWeighted = viewModel.useFinalExamFormula.value

        data class StudentAverageRow(
            val student: Student,
            val avg1: Double,
            val avg2: Double,
            val avg3: Double,
            val generalAverage: Double,
            var rank: Int = 0,
            var callNumber: Int = 0
        )

        val rows = classStudents.map { student ->
            val avg1 = viewModel.calculateStudentTermAverage(student.id, activeClass.id, 1)
            val avg2 = viewModel.calculateStudentTermAverage(student.id, activeClass.id, 2)
            val avg3 = viewModel.calculateStudentTermAverage(student.id, activeClass.id, 3)
            val annualAvg = if (isWeighted) {
                (avg1 * 1.0 + avg2 * 2.0 + avg3 * 3.0) / 6.0
            } else {
                (avg1 + avg2 + avg3) / 3.0
            }
            val callNumber = alphabeticalStudents.indexOfFirst { it.id == student.id } + 1
            StudentAverageRow(
                student = student,
                avg1 = avg1,
                avg2 = avg2,
                avg3 = avg3,
                generalAverage = annualAvg,
                callNumber = callNumber
            )
        }

        // Calculate rankings
        val sortedByAnnualAsc = rows.sortedByDescending { r -> r.generalAverage }
        var currentRank = 1
        var prevAvg = -1.0
        sortedByAnnualAsc.forEachIndexed { i, r ->
            if (r.generalAverage != prevAvg) {
                currentRank = i + 1
                prevAvg = r.generalAverage
            }
            r.rank = currentRank
        }

        // Sort students by rank (first to last) then by name for report cards
        var sortedRowList = rows.sortedWith(
            compareBy<StudentAverageRow> { it.rank }
                .thenBy { it.student.name }
        )
        if (targetStudentId != null) {
            sortedRowList = sortedRowList.filter { it.student.id == targetStudentId }
        }

        val seal = getMauritanianSealImg(context)
        val styles = """
        <style>
            @page {
                size: portrait;
                margin: 8mm 6mm; /* Uniform per-page margin */
            }
            @media print {
                @page {
                    size: portrait;
                    margin: 8mm 6mm;
                }
            }
            * {
                box-sizing: border-box;
            }
            body {
                font-family: 'Cairo', 'Noto Sans Arabic', 'Arial', sans-serif;
                direction: rtl;
                margin: 0 !important;
                padding: 0 !important;
                width: 100% !important;
                background-color: #ffffff;
                color: #000000;
                font-size: 10px;
                -webkit-print-color-adjust: exact;
                print-color-adjust: exact;
            }
            .a4-portrait-page {
                width: 100%;
                height: 278mm;
                max-height: 278mm;
                display: flex;
                flex-direction: column;
                justify-content: flex-start;
                align-items: stretch;
                gap: 2mm;
                margin: 0;
                padding: 0;
                box-sizing: border-box;
                overflow: hidden;
                page-break-before: always;
                break-before: page;
                page-break-after: always;
                break-after: page;
                page-break-inside: avoid;
                break-inside: avoid;
            }
            .a4-portrait-page:first-child {
                page-break-before: auto !important;
                break-before: auto !important;
            }
            .a4-portrait-page:last-child {
                page-break-after: avoid !important;
                break-after: avoid !important;
            }
            .averages-card {
                width: 100%;
                height: 86mm;
                max-height: 86mm;
                border: 2px solid #000000;
                border-radius: 10px;
                padding: 6px 12px;
                display: flex;
                flex-direction: column;
                justify-content: flex-start;
                gap: 5px;
                box-sizing: border-box;
                background-color: #ffffff;
                overflow: hidden;
            }
            .cut-line-horizontal {
                width: 100%;
                text-align: center;
                position: relative;
                margin: 1mm 0;
                line-height: 1;
            }
            .cut-line-horizontal::before {
                content: "";
                position: absolute;
                top: 50%;
                left: 0;
                right: 0;
                border-top: 1.5px dashed #000000;
                z-index: 1;
            }
            .cut-line-text {
                position: relative;
                z-index: 2;
                background: #ffffff;
                padding: 0 12px;
                font-size: 8.5px;
                font-weight: bold;
                color: #000000;
            }
            .card-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-bottom: 2px solid #000000;
                padding-bottom: 4px;
                margin-bottom: 4px;
            }
            .header-col {
                flex: 1;
                font-size: 8.5px;
                line-height: 1.35;
            }
            .header-col.right {
                text-align: right;
                font-weight: bold;
                color: #000000;
            }
            .header-col.center {
                text-align: center;
                flex: 1.2;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
            }
            .header-col.left {
                text-align: left;
                font-weight: bold;
                color: #000000;
            }
            .student-info-compact {
                display: flex;
                justify-content: space-between;
                align-items: center;
                font-weight: bold;
                font-size: 11px;
                border: 1.5px solid #000000;
                border-radius: 6px;
                background-color: #ffffff;
                padding: 5px 10px;
                margin-bottom: 6px;
                color: #000000;
            }
            .compact-table {
                width: 100%;
                border-collapse: collapse;
                text-align: center;
                font-size: 11px;
                border: 2px solid #000000;
                margin-bottom: 6px;
                border-radius: 4px;
                overflow: hidden;
            }
            .compact-table th {
                background-color: #ffffff;
                color: #000000;
                border: 1.5px solid #000000;
                padding: 6px 4px;
                font-weight: bold;
                font-size: 10.5px;
            }
            .compact-table td {
                border: 1px solid #000000;
                padding: 6px 4px;
                font-size: 11px;
                color: #000000;
            }
            .compact-table tr:nth-child(even) {
                background-color: #ffffff;
            }
            .compact-table .highlight-col {
                font-weight: 900;
                font-size: 12px;
                background-color: #ffffff !important;
                border: 2px solid #000000 !important;
                color: #000000;
            }
            .signatures-block {
                display: flex;
                justify-content: space-between;
                align-items: flex-start;
                font-size: 10.5px;
                font-weight: bold;
                padding: 4px 15px 0 15px;
                margin-top: 6px;
                color: #000000;
            }
            .signature-item {
                text-align: center;
                flex: 1;
                display: flex;
                flex-direction: column;
                align-items: center;
                min-width: 0;
            }
            .signature-title {
                margin-bottom: 3px;
                color: #000000;
                font-weight: 800;
                font-size: 10.5px;
            }
            .signature-line {
                width: 75%;
                margin: 0 auto 4px auto;
                border-bottom: 1px dotted #000000;
            }
            .signature-stamp-slot {
                height: 46px;
                display: flex;
                align-items: center;
                justify-content: center;
                width: 100%;
            }
        </style>
        """.trimIndent()

        val html = StringBuilder()
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>كشوف المعدلات الإجمالية</title>$styles</head><body>")

        val teacherStampHtml = getTeacherStampHtml(viewModel, activeClass.schoolName, sizePx = 42)
        val principalStampHtml = getPrincipalStampHtml(viewModel, activeClass.schoolName, sizePx = 42)

        val chunks = sortedRowList.chunked(3)
        chunks.forEachIndexed { pageIdx, pageRows ->
            html.append("<div class=\"a4-portrait-page\">")
            pageRows.forEachIndexed { cardIdx, row ->
                if (cardIdx > 0) {
                    html.append("""
                    <div class="cut-line-horizontal">
                        <span class="cut-line-text">✂️ ------------------- مكان قطع الورقة بالمقص ------------------- ✂️</span>
                    </div>
                    """)
                }
                val generalAverageScaled = row.generalAverage / scaleFactor
                val failBoundValue = viewModel.getFailBoundForClass(activeClass.id, activeClass.level)
                val isPass = generalAverageScaled >= failBoundValue
                val resultText = if (isPass) "ناجح" else {
                    if (activeClass.level == 1) "متجاوز" else "راسب"
                }
                val remarkText = getRemarkString(row.generalAverage, viewModel, activeClass.id, activeClass.level)

                val tot1 = viewModel.calculateStudentTermTotalScore(row.student.id, activeClass.id, 1)
                val tot2 = viewModel.calculateStudentTermTotalScore(row.student.id, activeClass.id, 2)
                val tot3 = viewModel.calculateStudentTermTotalScore(row.student.id, activeClass.id, 3)
                val grandTotalScore = tot1.first + tot2.first + tot3.first
                val grandTotalMax = tot1.second + tot2.second + tot3.second
                val grandTotalStr = if (grandTotalMax > 0) "$grandTotalMax / ${formatCleanNumber(grandTotalScore)}" else "-"

                html.append("""
                <div class="averages-card">
                    <!-- Header -->
                    <div class="card-header">
                        <div class="header-col right">
                            الجمهورية الإسلامية الموريتانية<br>
                            وزارة التربية وإصلاح النظام التعليمي<br>
                            الإدارة الجهوية للتربية: ${activeClass.wilaya.ifBlank { "غوركل" }}<br>
                            مفتشية مقاطعة: ${activeClass.moughataa.ifBlank { "لكصيبه 1" }}<br>
                            مدرسة: ${activeClass.schoolName.ifBlank { "الطلحايه 1" }}
                        </div>
                        <div class="header-col center">
                            <div style="display: flex; justify-content: center; align-items: center; height: 38px; margin-bottom: 2px;">
                                <div style="transform: scale(0.6); transform-origin: center; display: inline-block;">
                                    $seal
                                </div>
                            </div>
                            <span style="font-size: 11.5px; font-weight: 900; color: #000000; display: block; margin-top: 2px;">كشف المعدلات الإجمالية</span>
                            <span style="font-size: 9px; font-weight: bold; color: #000000; display: block; margin-top: 1px;">(امتحان التجاوز)</span>
                        </div>
                        <div class="header-col left">
                            شرف - إخاء - عدالة<br>
                            السنة الدراسية: ${activeClass.academicYear.ifBlank { "2025 - 2026" }}<br>
                            القسم: ${getDisplayClassName(activeClass)}
                        </div>
                    </div>

                    <!-- Student Info -->
                    <div class="student-info-compact">
                        <div>التلميذ: <span style="font-size: 12px; font-weight: 900; color: #000000;">${row.student.name}</span></div>
                        <div>رقم النداء: <span style="font-size: 11.5px; color: #000000;">${row.callNumber}</span></div>
                        <div style="padding-left: 12px; margin-left: 8px;">الرقم المدرسي: <span style="font-size: 11.5px; color: #000000; display: inline-block; min-width: 60px; text-align: center; margin-left: 6px; ${if (row.student.schoolId.isBlank()) "border-bottom: 1px dotted #000000;" else ""}">${row.student.schoolId.ifBlank { "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" }}</span></div>
                    </div>

                    <!-- Averages Table -->
                    <table class="compact-table">
                        <thead>
                            <tr>
                                <th style="width: 17%;">معدل الفصل الأول</th>
                                <th style="width: 17%;">معدل الفصل الثاني</th>
                                <th style="width: 17%;">معدل الفصل الثالث</th>
                                <th style="width: 23%; background-color: #ffffff; color: #000000; border-color: #000000;">المعدل العام / $scaleMax</th>
                                <th style="width: 11%;">الرتبة</th>
                                <th style="width: 15%;">الملاحظة والقرار</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td style="font-weight: bold;">${formatCleanNumber(row.avg1 / scaleFactor)}</td>
                                <td style="font-weight: bold;">${formatCleanNumber(row.avg2 / scaleFactor)}</td>
                                <td style="font-weight: bold;">${formatCleanNumber(row.avg3 / scaleFactor)}</td>
                                <td class="highlight-col">${formatCleanNumber(generalAverageScaled)}</td>
                                <td style="font-weight: bold; color: #000000; direction: ltr;">${classStudents.size} / ${row.rank}</td>
                                <td style="font-weight: 900; font-size: 10.5px; padding: 4px 2px;">
                                    <div style="font-weight: 900; line-height: 1.2;">
                                        <span style="color: #000000; display: block; font-size: 11px;">$resultText</span>
                                        <span style="color: #000000; font-size: 9px; font-weight: 600; display: block; margin-top: 1px;">$remarkText</span>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <!-- Signatures -->
                    <div class="signatures-block">
                        <div class="signature-item">
                            <div class="signature-title">توقيع المعلم</div>
                            <div class="signature-line"></div>
                            <div class="signature-stamp-slot">
                                $teacherStampHtml
                            </div>
                        </div>
                        <div class="signature-item">
                            <div class="signature-title">توقيع الوكيل</div>
                            <div class="signature-line"></div>
                            <div class="signature-stamp-slot"></div>
                        </div>
                        <div class="signature-item">
                            <div class="signature-title">توقيع المدير</div>
                            <div class="signature-line"></div>
                            <div class="signature-stamp-slot">
                                $principalStampHtml
                            </div>
                        </div>
                    </div>
                </div>
                """)
            }
            // Fill remaining space if page has fewer than 3 cards to keep consistent height of A4 portrait
            val emptyCards = 3 - pageRows.size
            for (i in 0 until emptyCards) {
                html.append("<div style=\"width: 100%; height: 88mm; visibility: hidden;\"></div>")
            }
            html.append("</div>") // Close a4-portrait-page
        }

        html.append("</body></html>")
        return html.toString()
    }

    fun generateThreeTermGradesReportCardsHtml(
        context: Context,
        className: String,
        students: List<Student>,
        subjects: List<Subject>,
        activeClass: ClassSection,
        viewModel: TeacherViewModel,
        targetStudentId: Long? = null,
        onePerPortraitPage: Boolean = false
    ): String {
        val sortedSubjects = subjects.sortedByOfficialOrder()
        val classStudents = students.filter { it.classId == activeClass.id }
        val alphabeticalStudents = classStudents.sortedBy { it.id }
        var classStudentsSorted = classStudents.sortedWith(
            compareBy<Student> { viewModel.calculateStudentGeneralRank(it.id, activeClass.id) }
                .thenBy { it.name }
        )
        if (targetStudentId != null) {
            classStudentsSorted = classStudentsSorted.filter { it.id == targetStudentId }
        }
        
        val isOutOfTen = viewModel.isOutOfTen.value
        val scaleFactor = if (isOutOfTen) 2.0 else 1.0
        val scaleMax = if (isOutOfTen) 10 else 20
        val isWeighted = viewModel.useFinalExamFormula.value
        val allGrades = viewModel.grades.value

        // Pre-compute O(1) grade lookups and student term averages to eliminate lag and UI freeze
        val gradesMap = allGrades.associateBy { Triple(it.studentId, it.subjectId, it.termId) }
        val studentIdSet = classStudentsSorted.map { it.id }.toSet()
        val avg1Map = studentIdSet.associateWith { viewModel.calculateStudentTermAverage(it, activeClass.id, 1) }
        val avg2Map = studentIdSet.associateWith { viewModel.calculateStudentTermAverage(it, activeClass.id, 2) }
        val avg3Map = studentIdSet.associateWith { viewModel.calculateStudentTermAverage(it, activeClass.id, 3) }
        val generalRankMap = studentIdSet.associateWith { viewModel.calculateStudentGeneralRank(it, activeClass.id) }

        val seal = getMauritanianSealImg(context)
        val styles = """
        <style>
            @page {
                size: landscape;
                margin: 6mm 8mm; /* Uniform per-page margin */
            }
            @media print {
                @page {
                    size: landscape;
                    margin: 6mm 8mm;
                }
            }
            * {
                box-sizing: border-box;
            }
            body {
                font-family: 'Cairo', 'Noto Sans Arabic', 'Arial', sans-serif;
                direction: rtl;
                margin: 0 !important;
                padding: 0 !important;
                width: 100% !important;
                background-color: #ffffff;
                color: #000000;
                font-size: 10px;
                -webkit-print-color-adjust: exact;
                print-color-adjust: exact;
            }
            .a4-landscape-page {
                width: 100%;
                height: 195mm;
                max-height: 195mm;
                display: flex;
                flex-direction: row;
                justify-content: space-between;
                align-items: flex-start; /* Strict Top Alignment */
                gap: 4mm;
                margin: 0;
                padding: 0;
                box-sizing: border-box;
                overflow: hidden;
                page-break-before: always;
                break-before: page;
                page-break-after: always;
                break-after: page;
                page-break-inside: avoid;
                break-inside: avoid;
            }
            .a4-landscape-page:first-child {
                page-break-before: auto !important;
                break-before: auto !important;
            }
            .a4-landscape-page:last-child {
                page-break-after: avoid !important;
                break-after: avoid !important;
            }
            .cut-line-vertical {
                height: 195mm;
                align-self: stretch;
                border-left: 1.5px dashed #000000;
                position: relative;
                display: flex;
                align-items: center;
                justify-content: center;
                margin: 0;
            }
            .cut-line-vertical-text {
                writing-mode: vertical-rl;
                transform: rotate(180deg);
                background: #ffffff;
                padding: 8px 0;
                font-size: 8.5px;
                font-weight: bold;
                color: #000000;
            }
            .three-term-card {
                width: calc(50% - 6mm);
                height: 195mm;
                max-height: 195mm;
                border: 2px solid #000000;
                border-radius: 10px;
                padding: 8px 12px;
                display: flex;
                flex-direction: column;
                justify-content: flex-start;
                gap: 4px;
                box-sizing: border-box;
                background-color: #ffffff;
                overflow: hidden;
            }
            .card-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-bottom: 2px solid #000000;
                padding-bottom: 4px;
                margin-bottom: 2px;
            }
            .header-col {
                flex: 1;
                font-size: 8px;
                line-height: 1.35;
            }
            .header-col.right {
                text-align: right;
                font-weight: bold;
                color: #000000;
            }
            .header-col.center {
                text-align: center;
                flex: 1.3;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
            }
            .header-col.left {
                text-align: left;
                font-weight: bold;
                color: #000000;
            }
            .student-info-compact {
                display: flex;
                justify-content: space-between;
                align-items: center;
                font-weight: bold;
                font-size: 11px;
                border: 1.5px solid #000000;
                border-radius: 6px;
                background-color: #ffffff;
                padding: 5px 10px;
                margin-bottom: 4px;
                color: #000000;
            }
            .compact-table {
                width: 100%;
                border-collapse: collapse;
                text-align: center;
                font-size: 10.5px;
                border: 2px solid #000000;
                margin-bottom: 4px;
                border-radius: 4px;
                overflow: hidden;
            }
            .compact-table th {
                background-color: #ffffff;
                color: #000000;
                border: 1.5px solid #000000;
                padding: 5px 4px;
                font-weight: bold;
                font-size: 10px;
            }
            .compact-table td {
                border: 1px solid #000000;
                padding: 5px 4px;
                font-size: 10.5px;
                color: #000000;
            }
            .compact-table tr:nth-child(even) {
                background-color: #ffffff;
            }
            .totals-summary {
                display: flex;
                justify-content: space-around;
                align-items: center;
                background-color: #ffffff;
                border: 2px solid #000000;
                border-radius: 6px;
                padding: 5px 12px;
                font-weight: bold;
                font-size: 11px;
                margin-bottom: 2px;
                color: #000000;
            }
            .signatures-block {
                display: flex;
                justify-content: space-between;
                align-items: flex-start;
                font-size: 10.5px;
                font-weight: bold;
                padding: 4px 15px 0 15px;
                margin-top: 6px;
                color: #000000;
            }
            .signature-item {
                text-align: center;
                flex: 1;
                display: flex;
                flex-direction: column;
                align-items: center;
                min-width: 0;
            }
            .signature-title {
                margin-bottom: 3px;
                color: #000000;
                font-weight: 800;
                font-size: 10.5px;
            }
            .signature-line {
                width: 75%;
                margin: 0 auto 4px auto;
                border-bottom: 1px dotted #000000;
            }
            .signature-stamp-slot {
                height: 50px;
                display: flex;
                align-items: center;
                justify-content: center;
                width: 100%;
            }
        </style>
        """.trimIndent()

        val html = StringBuilder()
        val portraitSingleStyles = if (onePerPortraitPage) """
            <style>
            @page { size: portrait; margin: 6mm 8mm; }
            @media print { @page { size: portrait; margin: 6mm 8mm; } }
            html, body {
                margin: 0 !important;
                padding: 0 !important;
                background-color: #ffffff !important;
                -webkit-print-color-adjust: exact !important;
                print-color-adjust: exact !important;
            }
            .a4-landscape-page {
                display: block !important;
                width: 100% !important;
                height: 275mm !important;
                max-height: 275mm !important;
                margin: 0 auto !important;
                padding: 0 !important;
                box-sizing: border-box !important;
                overflow: hidden !important;
                page-break-before: auto !important;
                break-before: auto !important;
                page-break-after: always !important;
                break-after: page !important;
                page-break-inside: avoid !important;
                break-inside: avoid !important;
            }
            .a4-landscape-page:last-child {
                page-break-after: avoid !important;
                break-after: avoid !important;
            }
            .cut-line-vertical {
                display: none !important;
            }
            .three-term-card {
                width: 100% !important;
                height: 275mm !important;
                max-height: 275mm !important;
                box-sizing: border-box !important;
                border: 2px solid #000000 !important;
                border-radius: 12px !important;
                padding: 10px 14px 10px 14px !important;
                display: flex !important;
                flex-direction: column !important;
                justify-content: flex-start !important;
                align-items: stretch !important;
                gap: 0 !important;
                overflow: hidden !important;
                background-color: #ffffff !important;
            }
            .card-header {
                flex-shrink: 0 !important;
                padding-bottom: 5px !important;
                margin-bottom: 6px !important;
                border-bottom: 2px solid #000000 !important;
            }
            .card-header img, .header-col img, .center img {
                width: 66px !important;
                height: 66px !important;
                margin: 0 auto 2px auto !important;
                object-fit: contain !important;
            }
            .header-col {
                font-size: 11px !important;
                line-height: 1.4 !important;
            }
            .header-col.right, .header-col.left {
                font-weight: bold !important;
                color: #000000 !important;
            }
            .student-info-compact {
                flex-shrink: 0 !important;
                font-size: 14px !important;
                padding: 6px 14px !important;
                margin-bottom: 8px !important;
                border: 2px solid #000000 !important;
                background-color: #ffffff !important;
                border-radius: 8px !important;
            }
            .compact-table {
                width: 100% !important;
                margin-bottom: 8px !important;
                border-collapse: collapse !important;
                border: 2px solid #000000 !important;
                border-radius: 0 !important;
            }
            .compact-table th {
                font-size: 13.5px !important;
                font-weight: 900 !important;
                padding: 6px 6px !important;
                height: auto !important;
                background-color: #ffffff !important;
                border: 1.5px solid #000000 !important;
                color: #000000 !important;
            }
            .compact-table td {
                font-size: 12.5px !important;
                padding: 5px 6px !important;
                line-height: 1.35 !important;
                border: 1.5px solid #000000 !important;
                color: #000000 !important;
            }
            .totals-summary {
                flex-shrink: 0 !important;
                font-size: 13.5px !important;
                padding: 7px 14px !important;
                margin: 0 0 8px 0 !important;
                border: 2px solid #000000 !important;
                background-color: #ffffff !important;
                border-radius: 8px !important;
                display: flex !important;
                justify-content: space-between !important;
                align-items: center !important;
            }
            .signatures-block {
                flex-shrink: 0 !important;
                margin-top: 4px !important;
                padding: 8px 16px 0 16px !important;
                border-top: 1.5px dashed #000000 !important;
                display: flex !important;
                justify-content: space-around !important;
                align-items: flex-start !important;
            }
            .signature-item {
                flex: 1 !important;
                text-align: center !important;
            }
            .signature-title {
                font-size: 13px !important;
                font-weight: 900 !important;
                margin-bottom: 3px !important;
                color: #000000 !important;
            }
            .signature-line {
                width: 75% !important;
                margin: 3px auto !important;
                border-bottom: 1.5px dashed #000000 !important;
            }
            .signature-stamp-slot {
                height: 56px !important;
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
            }
            .signature-stamp-slot svg {
                max-height: 52px !important;
                max-width: 95% !important;
                display: block !important;
                margin: 0 auto !important;
            }
            </style>
        """ else ""
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>كشف النقاط السنوي</title>$styles$portraitSingleStyles</head><body>")

        val teacherStampHtml = getTeacherStampHtml(viewModel, activeClass.schoolName, sizePx = if (onePerPortraitPage) 64 else 46)
        val principalStampHtml = getPrincipalStampHtml(viewModel, activeClass.schoolName, sizePx = if (onePerPortraitPage) 64 else 46)

        val chunks = classStudentsSorted.chunked(if (onePerPortraitPage) 1 else 2)
        chunks.forEachIndexed { pageIdx, pageStudents ->
            html.append("<div class=\"a4-landscape-page\">")
            pageStudents.forEachIndexed { cardIdx, student ->
                if (!onePerPortraitPage && cardIdx > 0) {
                    html.append("""
                    <div class="cut-line-vertical">
                        <span class="cut-line-vertical-text">✂️ مكان القطع بالمقص</span>
                    </div>
                    """)
                }
                val callNumber = alphabeticalStudents.indexOfFirst { it.id == student.id } + 1
                
                // Fast pre-computed lookups
                val avg1 = avg1Map[student.id] ?: 0.0
                val avg2 = avg2Map[student.id] ?: 0.0
                val avg3 = avg3Map[student.id] ?: 0.0
                
                val generalAverage = if (isWeighted) {
                    (avg1 * 1.0 + avg2 * 2.0 + avg3 * 3.0) / 6.0
                } else {
                    (avg1 + avg2 + avg3) / 3.0
                }
                val generalAverageScaled = generalAverage / scaleFactor
                val generalRank = generalRankMap[student.id] ?: 0
                
                val failBoundValue = viewModel.getFailBoundForClass(activeClass.id, activeClass.level)
                val isPass = generalAverageScaled >= failBoundValue
                val resultText = if (isPass) "ناجح" else {
                    if (activeClass.level == 1) "متجاوز" else "راسب"
                }
                val remarkText = getRemarkString(generalAverage, viewModel, activeClass.id, activeClass.level)

                val sealHeight = if (onePerPortraitPage) "66px" else "38px"
                val sealScale = if (onePerPortraitPage) "0.9" else "0.6"
                val titleFontSize = if (onePerPortraitPage) "17px" else "12.5px"
                val subtitleFontSize = if (onePerPortraitPage) "12px" else "9.5px"
                val studentNameSize = if (onePerPortraitPage) "15px" else "12px"
                val studentMetaSize = if (onePerPortraitPage) "13.5px" else "11.5px"
                val thFontSize = if (onePerPortraitPage) "13.5px" else "10.5px"
                val tdFontSize = if (onePerPortraitPage) "12.5px" else "10px"
                val totalsRowFontSize = if (onePerPortraitPage) "13px" else "11px"
                val summaryAvgSize = if (onePerPortraitPage) "14px" else "12.5px"
                val summaryRankSize = if (onePerPortraitPage) "14px" else "12px"
                val summaryDecisionSize = if (onePerPortraitPage) "14px" else "12.5px"
                val summaryRemarkSize = if (onePerPortraitPage) "13px" else "10px"

                html.append("""
                <div class="three-term-card">
                    <!-- Header -->
                    <div class="card-header">
                        <div class="header-col right">
                            الجمهورية الإسلامية الموريتانية<br>
                            وزارة التربية وإصلاح النظام التعليمي<br>
                            الإدارة الجهوية للتربية: ${activeClass.wilaya.ifBlank { "غوركل" }}<br>
                            مفتشية مقاطعة: ${activeClass.moughataa.ifBlank { "لكصيبه 1" }}<br>
                            مدرسة: ${activeClass.schoolName.ifBlank { "الطلحايه 1" }}
                        </div>
                        <div class="header-col center">
                            <div style="display: flex; justify-content: center; align-items: center; height: $sealHeight; margin-bottom: 2px;">
                                <div style="transform: scale($sealScale); transform-origin: center; display: inline-block;">
                                    $seal
                                </div>
                            </div>
                            <span style="font-size: $titleFontSize; font-weight: 900; color: #000000; display: block; margin-top: 2px;">كشف النقاط السنوي</span>
                            <span style="font-size: $subtitleFontSize; font-weight: bold; color: #000000; display: block; margin-top: 1px;">(امتحان التجاوز)</span>
                        </div>
                        <div class="header-col left">
                            شرف - إخاء - عدالة<br>
                            السنة الدراسية: ${activeClass.academicYear.ifBlank { "2025 - 2026" }}<br>
                            القسم: ${getDisplayClassName(activeClass)}
                        </div>
                    </div>

                    <!-- Student Info -->
                    <div class="student-info-compact">
                        <div>التلميذ: <span style="font-size: $studentNameSize; font-weight: 900; color: #000000;">${student.name}</span></div>
                        <div>رقم النداء: <span style="font-size: $studentMetaSize; font-weight: bold; color: #000000;">$callNumber</span></div>
                        <div>الرقم المدرسي: <span style="font-size: $studentMetaSize; font-weight: bold; color: #000000; display: inline-block; min-width: 60px; text-align: center; margin-left: 6px;">${student.schoolId.ifBlank { "-----------------" }}</span></div>
                    </div>

                    <!-- Subject Grades Table -->
                    <table class="compact-table">
                        <thead>
                            <tr>
                                <th style="width: 46%; text-align: right; padding-right: 8px; font-size: $thFontSize; font-weight: 900;">المواد الدراسية</th>
                                <th style="width: 18%; font-size: $thFontSize; font-weight: 900;">الفصل 1</th>
                                <th style="width: 18%; font-size: $thFontSize; font-weight: 900;">الفصل 2</th>
                                <th style="width: 18%; font-size: $thFontSize; font-weight: 900;">الفصل 3</th>
                            </tr>
                        </thead>
                        <tbody>
                """)

                sortedSubjects.forEach { sub ->
                    val grade1 = gradesMap[Triple(student.id, sub.id, 1)]?.score
                    val grade2 = gradesMap[Triple(student.id, sub.id, 2)]?.score
                    val grade3 = gradesMap[Triple(student.id, sub.id, 3)]?.score

                    val maxPointsToShow = if (isOutOfTen) 10.0 else sub.maxPoints.toDouble()
                    val score1 = if (grade1 != null) {
                        if (isOutOfTen) (grade1 / sub.maxPoints.toDouble()) * 10.0 else grade1
                    } else null
                    val score2 = if (grade2 != null) {
                        if (isOutOfTen) (grade2 / sub.maxPoints.toDouble()) * 10.0 else grade2
                    } else null
                    val score3 = if (grade3 != null) {
                        if (isOutOfTen) (grade3 / sub.maxPoints.toDouble()) * 10.0 else grade3
                    } else null

                    val maxPointsStr = if (isOutOfTen) "10" else "${sub.maxPoints}"

                    val str1 = if (score1 != null) "$maxPointsStr/${formatCleanNumber(score1)}" else "-"
                    val str2 = if (score2 != null) "$maxPointsStr/${formatCleanNumber(score2)}" else "-"
                    val str3 = if (score3 != null) "$maxPointsStr/${formatCleanNumber(score3)}" else "-"

                    html.append("""
                            <tr>
                                <td style="text-align: right; padding-right: 8px; font-weight: 900; color: #000000; font-size: $tdFontSize;">${sub.name}</td>
                                <td style="font-weight: bold; direction: ltr; font-size: $tdFontSize;">$str1</td>
                                <td style="font-weight: bold; direction: ltr; font-size: $tdFontSize;">$str2</td>
                                <td style="font-weight: bold; direction: ltr; font-size: $tdFontSize;">$str3</td>
                            </tr>
                    """)
                }

                val avg1Scaled = avg1 / scaleFactor
                val avg2Scaled = avg2 / scaleFactor
                val avg3Scaled = avg3 / scaleFactor

                val totalMaxToShow = if (isOutOfTen) sortedSubjects.size * 10 else sortedSubjects.sumOf { it.maxPoints }

                var totalScore1 = if (isOutOfTen) {
                    sortedSubjects.sumOf { sub ->
                        gradesMap[Triple(student.id, sub.id, 1)]?.score?.let { s -> (s / sub.maxPoints.toDouble()) * 10.0 } ?: 0.0
                    }
                } else {
                    sortedSubjects.sumOf { sub ->
                        gradesMap[Triple(student.id, sub.id, 1)]?.score ?: 0.0
                    }
                }
                if (totalScore1 == 0.0 && avg1 > 0.0) {
                    totalScore1 = (avg1 / 20.0) * totalMaxToShow
                }

                var totalScore2 = if (isOutOfTen) {
                    sortedSubjects.sumOf { sub ->
                        gradesMap[Triple(student.id, sub.id, 2)]?.score?.let { s -> (s / sub.maxPoints.toDouble()) * 10.0 } ?: 0.0
                    }
                } else {
                    sortedSubjects.sumOf { sub ->
                        gradesMap[Triple(student.id, sub.id, 2)]?.score ?: 0.0
                    }
                }
                if (totalScore2 == 0.0 && avg2 > 0.0) {
                    totalScore2 = (avg2 / 20.0) * totalMaxToShow
                }

                var totalScore3 = if (isOutOfTen) {
                    sortedSubjects.sumOf { sub ->
                        gradesMap[Triple(student.id, sub.id, 3)]?.score?.let { s -> (s / sub.maxPoints.toDouble()) * 10.0 } ?: 0.0
                    }
                } else {
                    sortedSubjects.sumOf { sub ->
                        gradesMap[Triple(student.id, sub.id, 3)]?.score ?: 0.0
                    }
                }
                if (totalScore3 == 0.0 && avg3 > 0.0) {
                    totalScore3 = (avg3 / 20.0) * totalMaxToShow
                }

                // Total Points Row & Averages Row (bottom of table)
                html.append("""
                            <tr style="background-color: #ffffff; font-weight: 900; border-top: 2px solid #000000;">
                                <td style="text-align: right; padding: 7px 8px; font-size: $totalsRowFontSize; color: #000000; font-weight: 900;">مجموع النقاط ($totalMaxToShow)</td>
                                <td style="padding: 7px 8px; font-size: $totalsRowFontSize; color: #000000; direction: ltr; font-weight: 900;">$totalMaxToShow / ${formatCleanNumber(totalScore1)}</td>
                                <td style="padding: 7px 8px; font-size: $totalsRowFontSize; color: #000000; direction: ltr; font-weight: 900;">$totalMaxToShow / ${formatCleanNumber(totalScore2)}</td>
                                <td style="padding: 7px 8px; font-size: $totalsRowFontSize; color: #000000; direction: ltr; font-weight: 900;">$totalMaxToShow / ${formatCleanNumber(totalScore3)}</td>
                            </tr>
                            <tr style="background-color: #ffffff; font-weight: 900; border-top: 1px solid #000000; border-bottom: 2px solid #000000;">
                                <td style="text-align: right; padding: 7px 8px; font-size: $totalsRowFontSize; color: #000000; font-weight: 900;">معدل الفصل</td>
                                <td style="padding: 7px 8px; font-size: $totalsRowFontSize; color: #000000; direction: ltr; font-weight: 900;">$scaleMax/${formatCleanNumber(avg1Scaled)}</td>
                                <td style="padding: 7px 8px; font-size: $totalsRowFontSize; color: #000000; direction: ltr; font-weight: 900;">$scaleMax/${formatCleanNumber(avg2Scaled)}</td>
                                <td style="padding: 7px 8px; font-size: $totalsRowFontSize; color: #000000; direction: ltr; font-weight: 900;">$scaleMax/${formatCleanNumber(avg3Scaled)}</td>
                            </tr>
                        </tbody>
                    </table>

                    <!-- Annual Summary Block -->
                    <div class="totals-summary">
                        <div>المعدل العام السنوي: <span style="font-size: $summaryAvgSize; font-weight: 900; color: #000000; direction: ltr; display: inline-block; margin-right: 4px;">$scaleMax / ${formatCleanNumber(generalAverageScaled)}</span></div>
                        <div>الرتبة العامة: <span style="font-size: $summaryRankSize; font-weight: 900; color: #000000; direction: ltr; display: inline-block; margin-right: 4px;">${classStudents.size} / $generalRank</span></div>
                        <div style="display: flex; align-items: center; gap: 4px;">
                            <span>القرار والملاحظة:</span>
                            <span style="font-weight: 900; color: #000000; font-size: $summaryDecisionSize; margin-right: 4px;">$resultText</span>
                            <span style="color: #000000; font-size: $summaryRemarkSize; font-weight: bold; margin-right: 2px;">($remarkText)</span>
                        </div>
                    </div>

                    <!-- Signatures -->
                    <div class="signatures-block">
                        <div class="signature-item">
                            <div class="signature-title">توقيع المعلم</div>
                            <div class="signature-line"></div>
                            <div class="signature-stamp-slot">
                                $teacherStampHtml
                            </div>
                        </div>
                        <div class="signature-item">
                            <div class="signature-title">توقيع الوكيل</div>
                            <div class="signature-line"></div>
                            <div class="signature-stamp-slot"></div>
                        </div>
                        <div class="signature-item">
                            <div class="signature-title">توقيع المدير</div>
                            <div class="signature-line"></div>
                            <div class="signature-stamp-slot">
                                $principalStampHtml
                            </div>
                        </div>
                    </div>
                </div>
                """)
            }
            
            if (!onePerPortraitPage) {
                // Fill remaining space if page has fewer than 2 cards to keep consistent height/width of A4 landscape
                val emptyCards = 2 - pageStudents.size
                for (i in 0 until emptyCards) {
                    html.append("<div style=\"width: 49%; height: 198mm; visibility: hidden;\"></div>")
                }
            }
            html.append("</div>") // Close a4-landscape-page
        }

        html.append("</body></html>")
        return html.toString()
    }

    // Print execution method (iOS: WKWebView + UIPrintInteractionController)
    fun printHtml(context: android.content.Context, htmlContent: String, jobName: String, isLandscape: Boolean = false) {
        com.example.compat.PrintProgress.show("جاري تحضير ملف الطباعة... 🖨️")
        com.example.compat.PlatformApi.printHtml(htmlContent, jobName, isLandscape) { ok, error ->
            com.example.compat.PrintProgress.hide()
            if (!ok && error != null) {
                Toast.makeText(context, "خطأ أثناء الطباعة: $error", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Share as PDF (iOS: WKWebView → PDF A4 → share sheet)
    fun shareHtmlAsPdf(context: android.content.Context, htmlContent: String, filename: String, isLandscape: Boolean = false) {
        com.example.compat.PrintProgress.show("جاري تحضير ملف PDF... 📄")
        com.example.compat.PlatformApi.shareHtmlAsPdf(htmlContent, filename, isLandscape) { ok, error ->
            com.example.compat.PrintProgress.hide()
            if (!ok) {
                Toast.makeText(context, "خطأ أثناء تحويل PDF: ${error ?: ""}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
