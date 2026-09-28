package com.example.platform

/** الوقت الحالي بالميلي ثانية (بديل System.currentTimeMillis) */
expect fun currentTimeMillis(): Long

/** السنة والشهر (1..12) حسب توقيت الجهاز (بديل java.util.Calendar) */
expect fun currentYearMonth(): Pair<Int, Int>

/** فكّ ضغط ملف gzip (بديل GZIPInputStream) */
expect fun gunzip(data: ByteArray): ByteArray
