@file:Suppress("unused", "PackageDirectoryMismatch", "FunctionName")

package org.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray as KArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject as KObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

class JSONException(message: String?) : RuntimeException(message)

internal object JsonConv {
    fun fromElement(e: JsonElement): Any? = when (e) {
        is JsonNull -> JSONObject.NULL
        is KObject -> JSONObject(e)
        is KArray -> JSONArray(e)
        is JsonPrimitive -> when {
            e.isString -> e.content
            e.booleanOrNull != null -> e.booleanOrNull
            e.longOrNull != null -> {
                val l = e.longOrNull!!
                if (l in Int.MIN_VALUE..Int.MAX_VALUE) l.toInt() else l
            }
            e.doubleOrNull != null -> e.doubleOrNull
            else -> e.content
        }
    }

    fun quote(s: String): String {
        val sb = StringBuilder("\"")
        for (c in s) {
            when (c) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                '\b' -> sb.append("\\b")
                '\u000C' -> sb.append("\\f")
                '/' -> sb.append("\\/")
                else -> if (c < ' ') sb.append("\\u" + c.code.toString(16).padStart(4, '0')) else sb.append(c)
            }
        }
        return sb.append('"').toString()
    }

    fun valueToString(v: Any?): String = when (v) {
        null, JSONObject.NULL -> "null"
        is String -> quote(v)
        is JSONObject -> v.toString()
        is JSONArray -> v.toString()
        is Double -> if (v % 1.0 == 0.0 && kotlin.math.abs(v) < 1e15) v.toLong().toString() else v.toString()
        is Float -> valueToString(v.toDouble())
        is Number, is Boolean -> v.toString()
        is Map<*, *> -> JSONObject(v).toString()
        is Collection<*> -> JSONArray(v).toString()
        else -> quote(v.toString())
    }

    fun wrap(v: Any?): Any? = when (v) {
        null -> JSONObject.NULL
        is Map<*, *> -> JSONObject(v)
        is Collection<*> -> JSONArray(v)
        is Array<*> -> JSONArray(v.toList())
        else -> v
    }

    fun toLong(v: Any?): Long? = when (v) {
        is Number -> v.toLong()
        is String -> v.trim().toLongOrNull() ?: v.trim().toDoubleOrNull()?.toLong()
        else -> null
    }

    fun toDouble(v: Any?): Double? = when (v) {
        is Number -> v.toDouble()
        is String -> v.trim().toDoubleOrNull()
        else -> null
    }

    fun toBool(v: Any?): Boolean? = when (v) {
        is Boolean -> v
        is String -> if (v.equals("true", true)) true else if (v.equals("false", true)) false else null
        else -> null
    }
}

class JSONObject() {
    private val map = LinkedHashMap<String, Any?>()

    constructor(text: String) : this() {
        val e = try { Json.parseToJsonElement(text) } catch (ex: Exception) { throw JSONException(ex.message) }
        if (e !is KObject) throw JSONException("Value is not a JSONObject")
        for ((k, v) in e) map[k] = JsonConv.fromElement(v)
    }

    internal constructor(obj: KObject) : this() {
        for ((k, v) in obj) map[k] = JsonConv.fromElement(v)
    }

    constructor(source: Map<*, *>) : this() {
        for ((k, v) in source) if (k != null) map[k.toString()] = JsonConv.wrap(v)
    }

    fun length(): Int = map.size
    fun has(key: String): Boolean = map.containsKey(key)
    fun isNull(key: String): Boolean = map[key] == null || map[key] == NULL
    fun keys(): Iterator<String> = map.keys.toList().iterator()
    fun names(): JSONArray? = if (map.isEmpty()) null else JSONArray(map.keys.toList())
    fun remove(key: String): Any? = map.remove(key)

    fun put(key: String, value: Any?): JSONObject {
        if (value == null) map.remove(key) else map[key] = JsonConv.wrap(value)
        return this
    }
    fun put(key: String, value: Int): JSONObject { map[key] = value; return this }
    fun put(key: String, value: Long): JSONObject { map[key] = value; return this }
    fun put(key: String, value: Double): JSONObject { map[key] = value; return this }
    fun put(key: String, value: Boolean): JSONObject { map[key] = value; return this }
    fun putOpt(key: String?, value: Any?): JSONObject { if (key != null && value != null) put(key, value); return this }

    fun get(key: String): Any = map[key] ?: throw JSONException("JSONObject[\"$key\"] not found.")
    fun opt(key: String): Any? = map[key]

    fun getString(key: String): String {
        val v = get(key)
        if (v == NULL) throw JSONException("JSONObject[\"$key\"] is null")
        return v.toString()
    }
    fun optString(key: String): String = optString(key, "")
    fun optString(key: String, fallback: String?): String {
        val v = map[key]
        return if (v == null || v == NULL) (fallback ?: "") else v.toString()
    }

    fun getInt(key: String): Int = JsonConv.toLong(get(key))?.toInt() ?: throw JSONException("JSONObject[\"$key\"] is not a number")
    fun optInt(key: String): Int = optInt(key, 0)
    fun optInt(key: String, fallback: Int): Int = JsonConv.toLong(map[key])?.toInt() ?: fallback

    fun getLong(key: String): Long = JsonConv.toLong(get(key)) ?: throw JSONException("JSONObject[\"$key\"] is not a number")
    fun optLong(key: String): Long = optLong(key, 0L)
    fun optLong(key: String, fallback: Long): Long = JsonConv.toLong(map[key]) ?: fallback

    fun getDouble(key: String): Double = JsonConv.toDouble(get(key)) ?: throw JSONException("JSONObject[\"$key\"] is not a number")
    fun optDouble(key: String): Double = optDouble(key, Double.NaN)
    fun optDouble(key: String, fallback: Double): Double = JsonConv.toDouble(map[key]) ?: fallback

    fun getBoolean(key: String): Boolean = JsonConv.toBool(get(key)) ?: throw JSONException("JSONObject[\"$key\"] is not a Boolean")
    fun optBoolean(key: String): Boolean = optBoolean(key, false)
    fun optBoolean(key: String, fallback: Boolean): Boolean = JsonConv.toBool(map[key]) ?: fallback

    fun getJSONObject(key: String): JSONObject = get(key) as? JSONObject ?: throw JSONException("JSONObject[\"$key\"] is not a JSONObject")
    fun optJSONObject(key: String): JSONObject? = map[key] as? JSONObject
    fun getJSONArray(key: String): JSONArray = get(key) as? JSONArray ?: throw JSONException("JSONObject[\"$key\"] is not a JSONArray")
    fun optJSONArray(key: String): JSONArray? = map[key] as? JSONArray

    fun toMap(): Map<String, Any?> = map.mapValues { (_, v) -> unwrap(v) }

    override fun toString(): String =
        map.entries.joinToString(",", "{", "}") { (k, v) -> JsonConv.quote(k) + ":" + JsonConv.valueToString(v) }

    fun toString(indent: Int): String = toString()

    companion object {
        val NULL: Any = object {
            override fun toString() = "null"
            override fun equals(other: Any?) = other == null || other === this
            override fun hashCode() = 0
        }
        fun quote(s: String?): String = JsonConv.quote(s ?: "")
        internal fun unwrap(v: Any?): Any? = when (v) {
            NULL -> null
            is JSONObject -> v.toMap()
            is JSONArray -> v.toList()
            else -> v
        }
    }
}

class JSONArray() : Iterable<Any?> {
    private val list = ArrayList<Any?>()

    constructor(text: String) : this() {
        val e = try { Json.parseToJsonElement(text) } catch (ex: Exception) { throw JSONException(ex.message) }
        if (e !is KArray) throw JSONException("Value is not a JSONArray")
        for (v in e) list.add(JsonConv.fromElement(v))
    }

    internal constructor(arr: KArray) : this() {
        for (v in arr) list.add(JsonConv.fromElement(v))
    }

    constructor(source: Collection<*>) : this() {
        for (v in source) list.add(JsonConv.wrap(v))
    }

    fun length(): Int = list.size
    fun isNull(i: Int): Boolean = list.getOrNull(i) == null || list[i] == JSONObject.NULL
    fun put(value: Any?): JSONArray { list.add(JsonConv.wrap(value)); return this }
    fun put(value: Int): JSONArray { list.add(value); return this }
    fun put(value: Long): JSONArray { list.add(value); return this }
    fun put(value: Double): JSONArray { list.add(value); return this }
    fun put(value: Boolean): JSONArray { list.add(value); return this }
    fun put(index: Int, value: Any?): JSONArray {
        while (list.size <= index) list.add(JSONObject.NULL)
        list[index] = JsonConv.wrap(value); return this
    }
    fun remove(index: Int): Any? = if (index in list.indices) list.removeAt(index) else null

    fun get(i: Int): Any = list.getOrNull(i) ?: throw JSONException("JSONArray[$i] not found.")
    fun opt(i: Int): Any? = list.getOrNull(i)
    fun getString(i: Int): String = get(i).toString()
    fun optString(i: Int): String = optString(i, "")
    fun optString(i: Int, fallback: String): String { val v = list.getOrNull(i); return if (v == null || v == JSONObject.NULL) fallback else v.toString() }
    fun getInt(i: Int): Int = JsonConv.toLong(get(i))?.toInt() ?: throw JSONException("JSONArray[$i] is not a number")
    fun optInt(i: Int, fallback: Int = 0): Int = JsonConv.toLong(list.getOrNull(i))?.toInt() ?: fallback
    fun getLong(i: Int): Long = JsonConv.toLong(get(i)) ?: throw JSONException("JSONArray[$i] is not a number")
    fun optLong(i: Int, fallback: Long = 0L): Long = JsonConv.toLong(list.getOrNull(i)) ?: fallback
    fun getDouble(i: Int): Double = JsonConv.toDouble(get(i)) ?: throw JSONException("JSONArray[$i] is not a number")
    fun optDouble(i: Int, fallback: Double = Double.NaN): Double = JsonConv.toDouble(list.getOrNull(i)) ?: fallback
    fun getBoolean(i: Int): Boolean = JsonConv.toBool(get(i)) ?: throw JSONException("JSONArray[$i] is not a Boolean")
    fun optBoolean(i: Int, fallback: Boolean = false): Boolean = JsonConv.toBool(list.getOrNull(i)) ?: fallback
    fun getJSONObject(i: Int): JSONObject = get(i) as? JSONObject ?: throw JSONException("JSONArray[$i] is not a JSONObject")
    fun optJSONObject(i: Int): JSONObject? = list.getOrNull(i) as? JSONObject
    fun getJSONArray(i: Int): JSONArray = get(i) as? JSONArray ?: throw JSONException("JSONArray[$i] is not a JSONArray")
    fun optJSONArray(i: Int): JSONArray? = list.getOrNull(i) as? JSONArray

    fun toList(): List<Any?> = list.map { JSONObject.unwrap(it) }
    override fun iterator(): Iterator<Any?> = list.iterator()

    override fun toString(): String = list.joinToString(",", "[", "]") { JsonConv.valueToString(it) }
    fun toString(indent: Int): String = toString()
}
