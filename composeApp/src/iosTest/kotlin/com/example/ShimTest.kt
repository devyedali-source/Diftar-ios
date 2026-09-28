package com.example

import com.example.compat.format
import com.example.compat.formatPrintf
import com.example.data.backup.BackupData
import com.example.data.models.ClassSection
import com.example.data.models.Grade
import com.squareup.moshi.Moshi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** اختبارات طبقة التوافق: يجب أن تعطي نفس نتائج جافا في أندرويد */
class ShimTest {
    @Test
    fun printfMatchesJava() {
        assertEquals("12.35", formatPrintf("%.2f", arrayOf(12.345)))
        assertEquals("05", formatPrintf("%02d", arrayOf(5)))
        assertEquals("1,234,567", formatPrintf("%,d", arrayOf(1234567)))
        assertEquals("10.0", formatPrintf("%.1f", arrayOf(9.95)))
        assertEquals("a-b", formatPrintf("%s-%s", arrayOf("a", "b")))
        assertEquals("15.00", String.format(java.util.Locale.US, "%.2f", 15.0))
        assertEquals("7", "%d".format(7))
        assertEquals("-3.50", formatPrintf("%.2f", arrayOf(-3.5)))
        assertEquals("0A", formatPrintf("%02X", arrayOf(10)))
    }

    @Test
    fun decimalFormatMatchesJava() {
        val df = java.text.DecimalFormat("0.##", java.text.DecimalFormatSymbols(java.util.Locale.US))
        assertEquals("15", df.format(15.0))
        assertEquals("15.5", df.format(15.5))
        assertEquals("15.26", df.format(15.256))
        assertEquals("0.5", df.format(0.5))
        assertEquals("7.10", java.text.DecimalFormat("0.00").format(7.1))
    }

    @Test
    fun calendarWorks() {
        val c = java.util.Calendar.getInstance()
        c.set(2026, java.util.Calendar.SEPTEMBER, 28)
        assertEquals(2026, c.get(java.util.Calendar.YEAR))
        assertEquals(8, c.get(java.util.Calendar.MONTH))
        c.add(java.util.Calendar.DAY_OF_YEAR, 7)
        assertEquals(9, c.get(java.util.Calendar.MONTH))
        assertEquals(5, c.get(java.util.Calendar.DAY_OF_MONTH))
        c.add(java.util.Calendar.MONTH, 4)
        assertEquals(2027, c.get(java.util.Calendar.YEAR))
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        assertEquals("2027-02-05", sdf.format(c.time))
        val parsed = sdf.parse("2026-01-15")!!
        assertEquals("2026/01/15", java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.US).format(parsed))
    }

    @Test
    fun localDateAndHijri() {
        val d = java.time.LocalDate.of(2026, 9, 28)
        assertEquals(java.time.DayOfWeek.MONDAY, d.dayOfWeek)
        assertEquals(java.time.LocalDate.of(2026, 10, 3), d.plusDays(5))
        val h = java.time.chrono.HijrahDate.from(d)
        val y = h.get(java.time.temporal.ChronoField.YEAR)
        val m = h.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
        val day = h.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
        assertEquals(1448, y)
        val back = java.time.LocalDate.from(java.time.chrono.HijrahDate.of(y, m, day))
        assertEquals(d, back)
        assertEquals(5L, java.time.temporal.ChronoUnit.DAYS.between(d, d.plusDays(5)))
        println("Hijri of 2026-09-28 = $day/$m/$y")
    }

    @Test
    fun jsonRoundTrip() {
        val o = org.json.JSONObject()
        o.put("name", "قسم \"أ\"")
        o.put("count", 5)
        o.put("avg", 12.5)
        o.put("ok", true)
        o.put("list", org.json.JSONArray().put(1).put("x"))
        val back = org.json.JSONObject(o.toString())
        assertEquals("قسم \"أ\"", back.getString("name"))
        assertEquals(5, back.getInt("count"))
        assertEquals(12.5, back.getDouble("avg"))
        assertTrue(back.getBoolean("ok"))
        assertEquals(2, back.getJSONArray("list").length())
        assertEquals("", back.optString("missing"))
    }

    @Test
    fun backupJsonCompatibleWithAndroid() {
        // نسخة احتياطية بصيغة Moshi في أندرويد (حقول إضافية مجهولة يجب تجاهلها)
        val androidJson = """{"version":1,"timestamp":1790000000000,"userId":"u1","userEmail":"t@x.com",
            "classSections":[{"id":3,"name":"س5 - أ","level":5,"academicYear":"2026-2027","isTerm1Activated":true,"isTerm2Activated":true,"extra":1}],
            "grades":[{"studentId":7,"subjectId":2,"termId":1,"score":15.5}],"teacherSettings":{"k":"v"}}"""
        val adapter = Moshi.Builder().build().adapter(BackupData::class)
        val b = adapter.fromJson(androidJson)!!
        assertEquals(1, b.classSections.size)
        assertEquals(ClassSection(id = 3, name = "س5 - أ", level = 5, academicYear = "2026-2027", isTerm2Activated = true), b.classSections[0])
        assertEquals(Grade(7, 2, 1, 15.5), b.grades[0])
        assertEquals("v", b.teacherSettings["k"])
        val again = adapter.fromJson(adapter.toJson(b))!!
        assertEquals(b, again)
    }

    @Test
    fun uriParsing() {
        val u = android.net.Uri.parse("daftarmeallim://activate?uid=abc&classId=5&name=%D8%A3")
        assertEquals("daftarmeallim", u.scheme)
        assertEquals("activate", u.host)
        assertEquals("5", u.getQueryParameter("classId"))
        assertEquals("أ", u.getQueryParameter("name"))
        val w = android.net.Uri.parse("https://diftar-almoaalim.web.app/activate/abc?x=1")
        assertEquals("diftar-almoaalim.web.app", w.host)
        assertEquals("/activate/abc", w.path)
        assertEquals("abc", w.lastPathSegment)
        assertEquals("%D8%A3%20b", android.net.Uri.encode("أ b"))
    }

    @Test
    fun timestampRfc3339() {
        val t = com.google.firebase.Timestamp(1790000000L, 123000000)
        val s = t.toRfc3339()
        assertEquals(t, com.google.firebase.Timestamp.parseRfc3339(s))
        assertEquals(com.google.firebase.Timestamp(0, 0), com.google.firebase.Timestamp.parseRfc3339("1970-01-01T00:00:00Z"))
    }
}
