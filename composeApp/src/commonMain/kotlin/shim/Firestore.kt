@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "MemberVisibilityCanBePrivate")

package com.google.firebase.firestore

import com.example.compat.FirebaseConfig
import com.example.compat.PlatformApi
import com.example.compat.Void
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.backgroundTask
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

// ---------------------------------------------------------------------------
// أنواع مساعدة
// ---------------------------------------------------------------------------

class FirebaseFirestoreException(message: String?, val code: Code) : FirebaseException(message) {
    enum class Code {
        OK, CANCELLED, UNKNOWN, INVALID_ARGUMENT, DEADLINE_EXCEEDED, NOT_FOUND, ALREADY_EXISTS,
        PERMISSION_DENIED, RESOURCE_EXHAUSTED, FAILED_PRECONDITION, ABORTED, OUT_OF_RANGE,
        UNIMPLEMENTED, INTERNAL, UNAVAILABLE, DATA_LOSS, UNAUTHENTICATED
    }
}

enum class Source { DEFAULT, SERVER, CACHE }
enum class AggregateSource { SERVER }
enum class MetadataChanges { INCLUDE, EXCLUDE }

class SetOptions private constructor(val merge: Boolean, val fields: List<String>?) {
    companion object {
        fun merge(): SetOptions = SetOptions(true, null)
        fun mergeFields(vararg fields: String): SetOptions = SetOptions(true, fields.toList())
        fun mergeFields(fields: List<String>): SetOptions = SetOptions(true, fields)
    }
}

sealed class FieldValue {
    internal object ServerTimestamp : FieldValue()
    internal object Delete : FieldValue()
    internal class Increment(val value: Number) : FieldValue()
    internal class ArrayUnion(val values: List<Any?>) : FieldValue()
    internal class ArrayRemove(val values: List<Any?>) : FieldValue()

    companion object {
        fun serverTimestamp(): FieldValue = ServerTimestamp
        fun delete(): FieldValue = Delete
        fun increment(v: Long): FieldValue = Increment(v)
        fun increment(v: Double): FieldValue = Increment(v)
        fun arrayUnion(vararg v: Any?): FieldValue = ArrayUnion(v.toList())
        fun arrayRemove(vararg v: Any?): FieldValue = ArrayRemove(v.toList())
    }
}

class FieldPath private constructor(val segments: List<String>) {
    companion object {
        fun of(vararg names: String) = FieldPath(names.toList())
        fun documentId() = FieldPath(listOf("__name__"))
    }
}

fun interface EventListener<T> {
    fun onEvent(value: T?, error: FirebaseFirestoreException?)
}

interface ListenerRegistration {
    fun remove()
}

class SnapshotMetadata(val isFromCache: Boolean = false, private val pending: Boolean = false) {
    fun hasPendingWrites(): Boolean = pending
}

// ---------------------------------------------------------------------------
// المستندات
// ---------------------------------------------------------------------------

class DocumentSnapshot internal constructor(
    val reference: DocumentReference,
    private val fields: Map<String, Any?>?
) {
    val id: String get() = reference.id
    val metadata: SnapshotMetadata = SnapshotMetadata()
    fun exists(): Boolean = fields != null
    @Suppress("UNCHECKED_CAST")
    val data: Map<String, Any>? get() = fields as Map<String, Any>?

    fun get(field: String): Any? {
        var cur: Any? = fields ?: return null
        for (seg in field.split('.')) {
            cur = (cur as? Map<*, *>)?.get(seg) ?: return null
        }
        return cur
    }
    fun get(field: FieldPath): Any? = get(field.segments.joinToString("."))
    fun contains(field: String): Boolean = get(field) != null || fields?.containsKey(field) == true
    fun getString(field: String): String? = get(field) as? String
    fun getBoolean(field: String): Boolean? = get(field) as? Boolean
    fun getLong(field: String): Long? = (get(field) as? Number)?.toLong()
    fun getDouble(field: String): Double? = (get(field) as? Number)?.toDouble()
    fun getTimestamp(field: String): Timestamp? = get(field) as? Timestamp
    fun getDate(field: String): java.util.Date? = getTimestamp(field)?.toDate()
    @Suppress("UNCHECKED_CAST")
    fun <T> get(field: String, type: kotlin.reflect.KClass<*>): T? = get(field) as? T
}

typealias QueryDocumentSnapshot = DocumentSnapshot

class QuerySnapshot internal constructor(val documents: List<DocumentSnapshot>) : Iterable<DocumentSnapshot> {
    val isEmpty: Boolean get() = documents.isEmpty()
    fun size(): Int = documents.size
    val metadata: SnapshotMetadata = SnapshotMetadata()
    val documentChanges: List<DocumentChange> get() = documents.map { DocumentChange(it, DocumentChange.Type.ADDED) }
    override fun iterator(): Iterator<DocumentSnapshot> = documents.iterator()
}

class DocumentChange(val document: DocumentSnapshot, val type: Type) {
    enum class Type { ADDED, MODIFIED, REMOVED }
}

class AggregateQuerySnapshot(val count: Long) {
}

// ---------------------------------------------------------------------------
// المراجع والاستعلامات
// ---------------------------------------------------------------------------

class DocumentReference internal constructor(internal val db: FirebaseFirestore, val path: String) {
    val id: String get() = path.substringAfterLast('/')
    val parent: CollectionReference get() = CollectionReference(db, path.substringBeforeLast('/'))

    fun collection(name: String): CollectionReference = CollectionReference(db, "$path/$name")

    fun get(): Task<DocumentSnapshot> = backgroundTask { db.getDocument(this) }
    fun get(source: Source): Task<DocumentSnapshot> = get()

    fun set(data: Any): Task<Void?> = backgroundTask { db.commit(listOf(Write.Set(this, toMap(data), null))); null }
    fun set(data: Any, options: SetOptions): Task<Void?> = backgroundTask {
        db.commit(listOf(Write.Set(this, toMap(data), options))); null
    }
    fun update(data: Map<String, Any?>): Task<Void?> = backgroundTask { db.commit(listOf(Write.Update(this, data))); null }
    fun update(field: String, value: Any?, vararg more: Any?): Task<Void?> {
        val m = linkedMapOf<String, Any?>(field to value)
        var i = 0
        while (i + 1 < more.size) { m[more[i].toString()] = more[i + 1]; i += 2 }
        return update(m)
    }
    fun delete(): Task<Void?> = backgroundTask { db.commit(listOf(Write.Delete(this))); null }

    fun addSnapshotListener(listener: EventListener<DocumentSnapshot>): ListenerRegistration =
        db.poll({ db.getDocument(this) }, { a, b -> a.data == b.data && a.exists() == b.exists() }, listener)
    fun addSnapshotListener(changes: MetadataChanges, listener: EventListener<DocumentSnapshot>): ListenerRegistration = addSnapshotListener(listener)
    fun addSnapshotListener(activity: Any?, listener: EventListener<DocumentSnapshot>): ListenerRegistration = addSnapshotListener(listener)

    override fun equals(other: Any?) = other is DocumentReference && other.path == path
    override fun hashCode() = path.hashCode()
}

open class Query internal constructor(
    internal val db: FirebaseFirestore,
    internal val parentPath: String,
    internal val collectionId: String,
    internal val allDescendants: Boolean,
    internal val filters: List<Triple<String, String, Any?>> = emptyList(),
    internal val orders: List<Pair<String, Direction>> = emptyList(),
    internal val limitCount: Int? = null,
    internal val startAfterValues: List<Any?>? = null
) {
    enum class Direction { ASCENDING, DESCENDING }

    private fun copy(
        filters: List<Triple<String, String, Any?>> = this.filters,
        orders: List<Pair<String, Direction>> = this.orders,
        limitCount: Int? = this.limitCount,
        startAfterValues: List<Any?>? = this.startAfterValues
    ) = Query(db, parentPath, collectionId, allDescendants, filters, orders, limitCount, startAfterValues)

    fun whereEqualTo(field: String, value: Any?): Query = copy(filters = filters + Triple(field, "EQUAL", value))
    fun whereNotEqualTo(field: String, value: Any?): Query = copy(filters = filters + Triple(field, "NOT_EQUAL", value))
    fun whereGreaterThan(field: String, value: Any): Query = copy(filters = filters + Triple(field, "GREATER_THAN", value))
    fun whereGreaterThanOrEqualTo(field: String, value: Any): Query = copy(filters = filters + Triple(field, "GREATER_THAN_OR_EQUAL", value))
    fun whereLessThan(field: String, value: Any): Query = copy(filters = filters + Triple(field, "LESS_THAN", value))
    fun whereLessThanOrEqualTo(field: String, value: Any): Query = copy(filters = filters + Triple(field, "LESS_THAN_OR_EQUAL", value))
    fun whereIn(field: String, values: List<Any?>): Query = copy(filters = filters + Triple(field, "IN", values))
    fun whereArrayContains(field: String, value: Any): Query = copy(filters = filters + Triple(field, "ARRAY_CONTAINS", value))
    fun orderBy(field: String): Query = copy(orders = orders + (field to Direction.ASCENDING))
    fun orderBy(field: String, direction: Direction): Query = copy(orders = orders + (field to direction))
    fun limit(n: Long): Query = copy(limitCount = n.toInt())
    fun limit(n: Int): Query = copy(limitCount = n)
    fun startAfter(vararg values: Any?): Query {
        val v = values.map { if (it is DocumentSnapshot) it.reference else it }
        return copy(startAfterValues = v)
    }

    fun get(): Task<QuerySnapshot> = backgroundTask { db.runQuery(this) }
    fun get(source: Source): Task<QuerySnapshot> = get()

    fun count(): AggregateQuery = AggregateQuery(this)

    fun addSnapshotListener(listener: EventListener<QuerySnapshot>): ListenerRegistration =
        db.poll({ db.runQuery(this) }, { a, b -> a.documents.map { it.id to it.data } == b.documents.map { it.id to it.data } }, listener)
    fun addSnapshotListener(changes: MetadataChanges, listener: EventListener<QuerySnapshot>): ListenerRegistration = addSnapshotListener(listener)
    fun addSnapshotListener(activity: Any?, listener: EventListener<QuerySnapshot>): ListenerRegistration = addSnapshotListener(listener)
}

class CollectionReference internal constructor(db: FirebaseFirestore, val path: String) :
    Query(db, if (path.contains('/')) path.substringBeforeLast('/') else "", path.substringAfterLast('/'), false) {
    val id: String get() = path.substringAfterLast('/')
    val parent: DocumentReference? get() = if (path.contains('/')) DocumentReference(db, path.substringBeforeLast('/')) else null
    fun document(): DocumentReference = DocumentReference(db, "$path/${FirebaseFirestore.autoId()}")
    fun document(id: String): DocumentReference = DocumentReference(db, "$path/$id")
    fun add(data: Any): Task<DocumentReference> = backgroundTask {
        val ref = document()
        db.commit(listOf(Write.Set(ref, toMap(data), null)))
        ref
    }
}

class AggregateQuery internal constructor(private val query: Query) {
    fun get(source: AggregateSource): Task<AggregateQuerySnapshot> = backgroundTask { query.db.runCount(query) }
}

internal sealed class Write {
    class Set(val ref: DocumentReference, val data: Map<String, Any?>, val options: SetOptions?) : Write()
    class Update(val ref: DocumentReference, val data: Map<String, Any?>) : Write()
    class Delete(val ref: DocumentReference) : Write()
}

class WriteBatch internal constructor(private val db: FirebaseFirestore) {
    private val writes = mutableListOf<Write>()
    fun set(ref: DocumentReference, data: Any): WriteBatch { writes.add(Write.Set(ref, toMap(data), null)); return this }
    fun set(ref: DocumentReference, data: Any, options: SetOptions): WriteBatch { writes.add(Write.Set(ref, toMap(data), options)); return this }
    fun update(ref: DocumentReference, data: Map<String, Any?>): WriteBatch { writes.add(Write.Update(ref, data)); return this }
    fun update(ref: DocumentReference, field: String, value: Any?): WriteBatch { writes.add(Write.Update(ref, mapOf(field to value))); return this }
    fun delete(ref: DocumentReference): WriteBatch { writes.add(Write.Delete(ref)); return this }
    fun commit(): Task<Void?> {
        val copy = writes.toList()
        return backgroundTask {
            // حدّ Firestore: 500 عملية في كل طلب
            copy.chunked(450).forEach { db.commit(it) }
            null
        }
    }
}

@Suppress("UNCHECKED_CAST")
internal fun toMap(data: Any): Map<String, Any?> = when (data) {
    is Map<*, *> -> data as Map<String, Any?>
    else -> throw IllegalArgumentException("Only Map data is supported on iOS: ${data::class.simpleName}")
}

// ---------------------------------------------------------------------------
// العميل (Firestore REST API)
// ---------------------------------------------------------------------------

class FirebaseFirestoreSettings private constructor() {
    class Builder {
        fun setPersistenceEnabled(enabled: Boolean) = this
        fun setCacheSizeBytes(bytes: Long) = this
        fun setLocalCacheSettings(settings: Any?) = this
        fun build() = FirebaseFirestoreSettings()
    }
    companion object {
        const val CACHE_SIZE_UNLIMITED = -1L
    }
}

class FirebaseFirestore private constructor() {

    var firestoreSettings: FirebaseFirestoreSettings? = null

    private val projectId get() = FirebaseConfig.projectId
    private val dbRoot get() = "projects/$projectId/databases/(default)/documents"
    private val base get() = "https://firestore.googleapis.com/v1/$dbRoot"

    fun collection(path: String): CollectionReference = CollectionReference(this, path.trim('/'))
    fun document(path: String): DocumentReference = DocumentReference(this, path.trim('/'))
    fun collectionGroup(id: String): Query = Query(this, "", id, true)
    fun batch(): WriteBatch = WriteBatch(this)

    fun <T> runTransaction(block: (Transaction) -> T): Task<T> = backgroundTask {
        val tx = Transaction(this)
        val r = block(tx)
        commit(tx.writes)
        r
    }

    class Transaction internal constructor(private val db: FirebaseFirestore) {
        internal val writes = mutableListOf<Write>()
        fun get(ref: DocumentReference): DocumentSnapshot = db.getDocument(ref)
        fun set(ref: DocumentReference, data: Any): Transaction { writes.add(Write.Set(ref, toMap(data), null)); return this }
        fun set(ref: DocumentReference, data: Any, options: SetOptions): Transaction { writes.add(Write.Set(ref, toMap(data), options)); return this }
        fun update(ref: DocumentReference, data: Map<String, Any?>): Transaction { writes.add(Write.Update(ref, data)); return this }
        fun delete(ref: DocumentReference): Transaction { writes.add(Write.Delete(ref)); return this }
    }

    // ------------------------------------------------------------ HTTP

    private fun request(method: String, url: String, body: JsonElement?): JsonElement {
        val headers = HashMap<String, String>()
        headers["Content-Type"] = "application/json"
        FirebaseAuth.getInstance().idTokenOrNull()?.let { headers["Authorization"] = "Bearer $it" }
        headers.putAll(FirebaseConfig.apiHeaders())
        val fullUrl = if (FirebaseConfig.hasIosConfig) {
            url + (if (url.contains('?')) "&" else "?") + "key=" + FirebaseConfig.apiKey
        } else url
        val res = try {
            PlatformApi.httpExecute(method, fullUrl, headers, body?.toString()?.encodeToByteArray(), 30.0)
        } catch (e: Exception) {
            throw FirebaseFirestoreException("Failed to get document because the client is offline.", FirebaseFirestoreException.Code.UNAVAILABLE)
        }
        if (res.code == 0) {
            throw FirebaseFirestoreException("Failed to get document because the client is offline.", FirebaseFirestoreException.Code.UNAVAILABLE)
        }
        val text = res.text()
        if (!res.isSuccessful) {
            val status = try {
                Json.parseToJsonElement(text).jsonObject["error"]?.jsonObject?.get("status")?.jsonPrimitive?.contentOrNull
            } catch (e: Exception) { null }
            val code = status?.let { s -> FirebaseFirestoreException.Code.entries.firstOrNull { it.name == s } }
                ?: when (res.code) {
                    403 -> FirebaseFirestoreException.Code.PERMISSION_DENIED
                    404 -> FirebaseFirestoreException.Code.NOT_FOUND
                    401 -> FirebaseFirestoreException.Code.UNAUTHENTICATED
                    429 -> FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED
                    else -> FirebaseFirestoreException.Code.UNKNOWN
                }
            throw FirebaseFirestoreException("${code.name}: $text", code)
        }
        return if (text.isBlank()) JsonObject(emptyMap()) else Json.parseToJsonElement(text)
    }

    internal fun getDocument(ref: DocumentReference): DocumentSnapshot {
        return try {
            val o = request("GET", "$base/${encodePath(ref.path)}", null).jsonObject
            DocumentSnapshot(ref, decodeFields(o["fields"]))
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) DocumentSnapshot(ref, null) else throw e
        }
    }

    private fun structuredQuery(q: Query): JsonObject = buildJsonObject {
        putJsonArray("from") {
            add(buildJsonObject {
                put("collectionId", q.collectionId)
                if (q.allDescendants) put("allDescendants", true)
            })
        }
        val fieldFilters = q.filters.map { (field, op, value) ->
            buildJsonObject {
                putJsonObject("fieldFilter") {
                    putJsonObject("field") { put("fieldPath", quotePath(field)) }
                    put("op", op)
                    put("value", encodeValue(value))
                }
            }
        }
        if (fieldFilters.size == 1) put("where", fieldFilters[0])
        else if (fieldFilters.size > 1) putJsonObject("where") {
            putJsonObject("compositeFilter") {
                put("op", "AND")
                put("filters", JsonArray(fieldFilters))
            }
        }
        if (q.orders.isNotEmpty()) putJsonArray("orderBy") {
            q.orders.forEach { (f, d) ->
                add(buildJsonObject {
                    putJsonObject("field") { put("fieldPath", quotePath(f)) }
                    put("direction", if (d == Query.Direction.DESCENDING) "DESCENDING" else "ASCENDING")
                })
            }
        }
        q.startAfterValues?.let { vals ->
            putJsonObject("startAt") {
                put("values", JsonArray(vals.map { encodeValue(it) }))
                put("before", false)
            }
        }
        q.limitCount?.let { put("limit", it) }
    }

    private fun parentUrl(q: Query): String =
        if (q.parentPath.isEmpty()) base else "$base/${encodePath(q.parentPath)}"

    internal fun runQuery(q: Query): QuerySnapshot {
        val res = request("POST", "${parentUrl(q)}:runQuery", buildJsonObject { put("structuredQuery", structuredQuery(q)) })
        val docs = mutableListOf<DocumentSnapshot>()
        for (item in res.jsonArray) {
            val doc = item.jsonObject["document"]?.jsonObject ?: continue
            val name = doc["name"]?.jsonPrimitive?.contentOrNull ?: continue
            val path = name.substringAfter("/documents/")
            docs.add(DocumentSnapshot(DocumentReference(this, decodePath(path)), decodeFields(doc["fields"])))
        }
        return QuerySnapshot(docs)
    }

    internal fun runCount(q: Query): AggregateQuerySnapshot {
        val body = buildJsonObject {
            putJsonObject("structuredAggregationQuery") {
                put("structuredQuery", structuredQuery(q))
                putJsonArray("aggregations") {
                    add(buildJsonObject { put("alias", "count"); putJsonObject("count") {} })
                }
            }
        }
        val res = request("POST", "${parentUrl(q)}:runAggregationQuery", body)
        for (item in res.jsonArray) {
            val v = item.jsonObject["result"]?.jsonObject?.get("aggregateFields")?.jsonObject?.get("count")?.jsonObject
                ?.get("integerValue")?.jsonPrimitive?.contentOrNull
            if (v != null) return AggregateQuerySnapshot(v.toLong())
        }
        return AggregateQuerySnapshot(0)
    }

    internal fun commit(writes: List<Write>) {
        if (writes.isEmpty()) return
        val arr = buildJsonArray {
            for (w in writes) {
                when (w) {
                    is Write.Delete -> add(buildJsonObject { put("delete", docName(w.ref)) })
                    is Write.Set -> {
                        val transforms = mutableListOf<JsonObject>()
                        val fields = encodeFieldsCollectingTransforms(w.data, "", transforms)
                        add(buildJsonObject {
                            putJsonObject("update") {
                                put("name", docName(w.ref))
                                put("fields", fields)
                            }
                            if (w.options?.merge == true) {
                                putJsonObject("updateMask") {
                                    val paths = w.options.fields ?: leafPaths(w.data, "")
                                    put("fieldPaths", JsonArray(paths.map { JsonPrimitive(it) }))
                                }
                            }
                            if (transforms.isNotEmpty()) put("updateTransforms", JsonArray(transforms))
                        })
                    }
                    is Write.Update -> {
                        val transforms = mutableListOf<JsonObject>()
                        val nested = LinkedHashMap<String, Any?>()
                        val mask = mutableListOf<String>()
                        for ((k, v) in w.data) {
                            val segs = k.split('.')
                            val quoted = segs.joinToString(".") { quoteSegment(it) }
                            when (v) {
                                is FieldValue.ServerTimestamp, is FieldValue.Increment, is FieldValue.ArrayUnion, is FieldValue.ArrayRemove ->
                                    transforms.add(transformJson(quoted, v as FieldValue))
                                is FieldValue.Delete -> mask.add(quoted)
                                else -> {
                                    mask.add(quoted)
                                    putNested(nested, segs, v)
                                }
                            }
                        }
                        add(buildJsonObject {
                            putJsonObject("update") {
                                put("name", docName(w.ref))
                                put("fields", encodeFieldsCollectingTransforms(nested, "", mutableListOf()))
                            }
                            putJsonObject("updateMask") { put("fieldPaths", JsonArray(mask.map { JsonPrimitive(it) })) }
                            putJsonObject("currentDocument") { put("exists", true) }
                            if (transforms.isNotEmpty()) put("updateTransforms", JsonArray(transforms))
                        })
                    }
                }
            }
        }
        request("POST", "$base:commit", buildJsonObject { put("writes", arr) })
    }

    // ------------------------------------------------------------ الاستماع (استطلاع دوري)

    private val listenScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    internal fun <T> poll(fetch: () -> T, same: (T, T) -> Boolean, listener: EventListener<T>): ListenerRegistration {
        var job: Job? = null
        job = listenScope.launch {
            var last: T? = null
            var first = true
            while (isActive) {
                try {
                    val v = fetch()
                    val prev = last
                    if (first || prev == null || !same(prev, v)) {
                        last = v
                        first = false
                        PlatformApi.runOnMain { if (job?.isActive == true) listener.onEvent(v, null) }
                    }
                } catch (e: FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        PlatformApi.runOnMain { listener.onEvent(null, e) }
                        break
                    }
                } catch (e: Exception) {
                    // انقطاع مؤقت — نعيد المحاولة
                }
                delay(POLL_INTERVAL_MS)
            }
        }
        return object : ListenerRegistration {
            override fun remove() { job?.cancel() }
        }
    }

    // ------------------------------------------------------------ الترميز

    private fun docName(ref: DocumentReference) = "$dbRoot/${ref.path}"

    private fun encodePath(path: String) = path.split('/').joinToString("/") { android.net.Uri.encode(it) }
    private fun decodePath(path: String) = path.split('/').joinToString("/") { android.net.Uri.decode(it) }

    private fun quoteSegment(s: String): String =
        if (Regex("^[a-zA-Z_][a-zA-Z_0-9]*$").matches(s)) s else "`" + s.replace("\\", "\\\\").replace("`", "\\`") + "`"

    private fun quotePath(p: String): String = if (p == "__name__") p else p.split('.').joinToString(".") { quoteSegment(it) }

    private fun leafPaths(data: Map<String, Any?>, prefix: String): List<String> {
        val out = mutableListOf<String>()
        for ((k, v) in data) {
            val p = if (prefix.isEmpty()) quoteSegment(k) else "$prefix.${quoteSegment(k)}"
            when {
                v is FieldValue.ServerTimestamp || v is FieldValue.Increment || v is FieldValue.ArrayUnion || v is FieldValue.ArrayRemove -> {}
                v is Map<*, *> && v.isNotEmpty() -> {
                    @Suppress("UNCHECKED_CAST")
                    out.addAll(leafPaths(v as Map<String, Any?>, p))
                }
                else -> out.add(p)
            }
        }
        return out
    }

    private fun putNested(target: MutableMap<String, Any?>, segs: List<String>, value: Any?) {
        if (segs.size == 1) { target[segs[0]] = value; return }
        @Suppress("UNCHECKED_CAST")
        val child = (target[segs[0]] as? MutableMap<String, Any?>) ?: LinkedHashMap<String, Any?>().also { target[segs[0]] = it }
        putNested(child, segs.drop(1), value)
    }

    private fun transformJson(path: String, v: FieldValue): JsonObject = buildJsonObject {
        put("fieldPath", path)
        when (v) {
            is FieldValue.ServerTimestamp -> put("setToServerValue", "REQUEST_TIME")
            is FieldValue.Increment -> put("increment", encodeValue(v.value))
            is FieldValue.ArrayUnion -> putJsonObject("appendMissingElements") { put("values", JsonArray(v.values.map { encodeValue(it) })) }
            is FieldValue.ArrayRemove -> putJsonObject("removeAllFromArray") { put("values", JsonArray(v.values.map { encodeValue(it) })) }
            else -> {}
        }
    }

    private fun encodeFieldsCollectingTransforms(data: Map<String, Any?>, prefix: String, transforms: MutableList<JsonObject>): JsonObject =
        buildJsonObject {
            for ((k, v) in data) {
                val p = if (prefix.isEmpty()) quoteSegment(k) else "$prefix.${quoteSegment(k)}"
                when (v) {
                    is FieldValue.Delete -> {}
                    is FieldValue -> transforms.add(transformJson(p, v))
                    is Map<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        val inner = encodeFieldsCollectingTransforms(v as Map<String, Any?>, p, transforms)
                        put(k, buildJsonObject { putJsonObject("mapValue") { put("fields", inner) } })
                    }
                    else -> put(k, encodeValue(v))
                }
            }
        }

    internal fun encodeValue(v: Any?): JsonObject = when (v) {
        null -> buildJsonObject { put("nullValue", JsonNull) }
        is Boolean -> buildJsonObject { put("booleanValue", v) }
        is Int, is Long, is Short, is Byte -> buildJsonObject { put("integerValue", v.toString()) }
        is Double -> buildJsonObject { put("doubleValue", v) }
        is Float -> buildJsonObject { put("doubleValue", v.toDouble()) }
        is String -> buildJsonObject { put("stringValue", v) }
        is Timestamp -> buildJsonObject { put("timestampValue", v.toRfc3339()) }
        is java.util.Date -> buildJsonObject { put("timestampValue", Timestamp(v).toRfc3339()) }
        is DocumentReference -> buildJsonObject { put("referenceValue", docName(v)) }
        is Map<*, *> -> buildJsonObject {
            putJsonObject("mapValue") {
                putJsonObject("fields") { for ((k, x) in v) put(k.toString(), encodeValue(x)) }
            }
        }
        is Collection<*> -> buildJsonObject { putJsonObject("arrayValue") { put("values", JsonArray(v.map { encodeValue(it) })) } }
        is Array<*> -> encodeValue(v.toList())
        is Enum<*> -> buildJsonObject { put("stringValue", v.name) }
        else -> buildJsonObject { put("stringValue", v.toString()) }
    }

    private fun decodeFields(e: JsonElement?): Map<String, Any?> {
        val o = e as? JsonObject ?: return emptyMap()
        val out = LinkedHashMap<String, Any?>()
        for ((k, v) in o) out[k] = decodeValue(v.jsonObject)
        return out
    }

    private fun decodeValue(o: JsonObject): Any? {
        val (type, value) = o.entries.firstOrNull() ?: return null
        return when (type) {
            "nullValue" -> null
            "booleanValue" -> value.jsonPrimitive.contentOrNull?.toBoolean()
            "integerValue" -> value.jsonPrimitive.contentOrNull?.toLongOrNull()
            "doubleValue" -> value.jsonPrimitive.contentOrNull?.toDoubleOrNull()
            "stringValue" -> value.jsonPrimitive.contentOrNull
            "timestampValue" -> value.jsonPrimitive.contentOrNull?.let { Timestamp.parseRfc3339(it) }
            "referenceValue" -> value.jsonPrimitive.contentOrNull?.let { DocumentReference(this, it.substringAfter("/documents/")) }
            "mapValue" -> decodeFields(value.jsonObject["fields"])
            "arrayValue" -> (value.jsonObject["values"] as? JsonArray)?.map { decodeValue(it.jsonObject) } ?: emptyList<Any?>()
            "bytesValue" -> value.jsonPrimitive.contentOrNull
            "geoPointValue" -> value.toString()
            else -> null
        }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 4000L
        private val instance by lazy { FirebaseFirestore() }
        fun getInstance(): FirebaseFirestore = instance

        private const val AUTO_ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        fun autoId(): String = (1..20).map { AUTO_ID_CHARS[kotlin.random.Random.nextInt(AUTO_ID_CHARS.length)] }.joinToString("")
    }
}

/** للاستعمال بصيغة Firebase.firestore */
val com.google.firebase.FirebaseApp.firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
