@file:Suppress("unused", "ClassName", "PackageDirectoryMismatch")

package com.example

import com.example.compat.PlatformApi
import com.example.resources.Res
import com.example.resources.ic_facebook
import com.example.resources.ic_gmail
import com.example.resources.ic_google
import com.example.resources.ic_telegram
import com.example.resources.ic_whatsapp
import com.example.resources.ic_youtube
import com.example.resources.img_mauritania_seal
import com.example.resources.teacher_logo
import org.jetbrains.compose.resources.DrawableResource

/** بديل صنف R في أندرويد: أرقام الموارد المستعملة في التطبيق */
object R {
    object drawable {
        const val ic_facebook = 1001
        const val ic_gmail = 1002
        const val ic_google = 1003
        const val ic_telegram = 1004
        const val ic_whatsapp = 1005
        const val ic_youtube = 1006
        const val teacher_logo = 1007
        const val img_mauritania_seal = 1008
    }

    object raw {
        const val img_mauritania_seal = 2001
    }

    object string {
        const val default_web_client_id = 3001
        const val app_name = 3002
    }

    object mipmap {
        const val ic_launcher = 1007
    }

    fun drawableResource(id: Int): DrawableResource = when (id) {
        drawable.ic_facebook -> Res.drawable.ic_facebook
        drawable.ic_gmail -> Res.drawable.ic_gmail
        drawable.ic_google -> Res.drawable.ic_google
        drawable.ic_telegram -> Res.drawable.ic_telegram
        drawable.ic_whatsapp -> Res.drawable.ic_whatsapp
        drawable.ic_youtube -> Res.drawable.ic_youtube
        drawable.img_mauritania_seal -> Res.drawable.img_mauritania_seal
        else -> Res.drawable.teacher_logo
    }

    fun rawBytes(id: Int): ByteArray? = when (id) {
        raw.img_mauritania_seal, drawable.img_mauritania_seal -> PlatformApi.readBundledFile("raw/img_mauritania_seal.jpg")
        else -> null
    }

    fun stringValue(id: Int): String = when (id) {
        string.default_web_client_id -> "16610884498-n6670r0i5scer207l4jkuod0kamruapn.apps.googleusercontent.com"
        string.app_name -> "دفتر المعلم - DM"
        else -> ""
    }
}

object BuildConfig {
    const val DEBUG = false
    const val APPLICATION_ID = "com.elyedali.schoolmanager"
    const val BUILD_TYPE = "release"
    val VERSION_NAME: String get() = PlatformApi.appVersionName()
    val VERSION_CODE: Int get() = PlatformApi.appVersionCode().toInt()
}
