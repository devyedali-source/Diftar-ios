@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch")
@file:OptIn(kotlinx.serialization.InternalSerializationApi::class, kotlinx.serialization.ExperimentalSerializationApi::class)

package com.squareup.moshi

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/** بديل Moshi مبني على kotlinx.serialization — نفس أسماء الحقول ونفس شكل JSON */
class Moshi private constructor() {
    class Builder {
        fun add(factory: Any?): Builder = this
        fun addLast(factory: Any?): Builder = this
        fun build(): Moshi = Moshi()
    }

    fun <T : Any> adapter(type: KClass<T>): JsonAdapter<T> = JsonAdapter(type.serializer())

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
            encodeDefaults = true
            isLenient = true
            allowSpecialFloatingPointValues = true
        }
    }
}

class JsonAdapter<T>(private val serializer: KSerializer<T>) {
    fun fromJson(text: String): T? = Moshi.json.decodeFromString(serializer, text)
    fun toJson(value: T?): String = if (value == null) "null" else Moshi.json.encodeToString(serializer, value)
    fun lenient(): JsonAdapter<T> = this
    fun nullSafe(): JsonAdapter<T> = this
    fun indent(s: String): JsonAdapter<T> = this
}

annotation class JsonClass(val generateAdapter: Boolean, val generator: String = "")
annotation class Json(val name: String = "", val ignore: Boolean = false)
