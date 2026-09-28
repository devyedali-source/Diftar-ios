package com.example.compat

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

/** قيمة JSON بسيطة من أي نوع (نص/رقم/منطقي) — بديل List<Any> في Moshi */
object AnyValueSerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

    override fun deserialize(decoder: Decoder): Any {
        val e = (decoder as JsonDecoder).decodeJsonElement()
        if (e is JsonPrimitive) {
            if (e.isString) return e.content
            e.booleanOrNull?.let { return it }
            e.longOrNull?.let { return it.toDouble() }
            e.doubleOrNull?.let { return it }
        }
        return e.toString()
    }

    override fun serialize(encoder: Encoder, value: Any) {
        val el: JsonElement = when (value) {
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is String -> JsonPrimitive(value)
            else -> JsonPrimitive(value.toString())
        }
        (encoder as JsonEncoder).encodeJsonElement(el)
    }
}
