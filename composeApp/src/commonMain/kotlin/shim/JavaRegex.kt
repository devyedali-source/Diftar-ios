@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch", "ClassName", "FunctionName")

package java.util.regex

class Pattern private constructor(val regex: Regex) {
    fun matcher(input: CharSequence): Matcher = Matcher(regex, input)
    fun pattern(): String = regex.pattern
    companion object {
        const val CASE_INSENSITIVE = 2
        fun compile(p: String): Pattern = Pattern(Regex(p))
        fun compile(p: String, flags: Int): Pattern =
            Pattern(if (flags and CASE_INSENSITIVE != 0) Regex(p, RegexOption.IGNORE_CASE) else Regex(p))
        fun matches(p: String, input: CharSequence): Boolean = Regex(p).matches(input)
        fun quote(s: String): String = Regex.escape(s)
    }
}

class Matcher(private val regex: Regex, private val input: CharSequence) {
    private var last: MatchResult? = null
    private var searchFrom = 0
    fun matches(): Boolean = regex.matches(input)
    fun find(): Boolean {
        val m = regex.find(input, searchFrom)
        last = m
        if (m != null) searchFrom = if (m.range.isEmpty()) m.range.first + 1 else m.range.last + 1
        return m != null
    }
    fun group(): String = last?.value ?: ""
    fun group(i: Int): String? = last?.groupValues?.getOrNull(i)
    fun start(): Int = last?.range?.first ?: -1
    fun end(): Int = (last?.range?.last ?: -2) + 1
}
