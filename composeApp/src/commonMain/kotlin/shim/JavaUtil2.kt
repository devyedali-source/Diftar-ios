@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName", "FunctionName")

package java.util.concurrent

/** على الآيفون تكفي خريطة عادية لأن الوصول يتمّ من خيط واحد في الغالب */
class ConcurrentHashMap<K, V> : MutableMap<K, V> by LinkedHashMap() {
    fun putIfAbsent(key: K, value: V): V? {
        val existing = get(key)
        if (existing == null) put(key, value)
        return existing
    }
}

class CopyOnWriteArrayList<E> : MutableList<E> by ArrayList()

enum class TimeUnit(private val ms: Long) {
    NANOSECONDS(0), MICROSECONDS(0), MILLISECONDS(1), SECONDS(1000), MINUTES(60_000), HOURS(3_600_000), DAYS(86_400_000);
    fun toMillis(d: Long): Long = if (ms == 0L) d / 1_000_000 else d * ms
    fun toSeconds(d: Long): Long = toMillis(d) / 1000
    fun toMinutes(d: Long): Long = toMillis(d) / 60_000
    fun toHours(d: Long): Long = toMillis(d) / 3_600_000
    fun toDays(d: Long): Long = toMillis(d) / 86_400_000
}

class TimeoutException(message: String? = null) : Exception(message)
class ExecutionException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
