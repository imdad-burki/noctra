package com.imdadburki.noctra.core.clock

import com.imdadburki.noctra.core.common.ClockFace
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pure clock-time model. Every function here is unit-testable (no Android
 * framework calls, no Composables) — the rendering layer consumes it.
 */
data class ClockTime(
    val hour24: Int,
    val minute: Int,
    val second: Int,
    val dayOfMonth: Int,
    val month: Int,
    val year: Int,
    val dayOfWeek: Int // java.time.DayOfWeek value 1 (Mon) .. 7 (Sun)
) {
    val hour12: Int get() = when (hour24 % 12) { 0 -> 12; else -> hour24 % 12 }
    val amPm: String get() = if (hour24 < 12) "AM" else "PM"
    val isNight: Boolean get() = hour24 < 6 || hour24 >= 18

    companion object {
        fun now(): ClockTime {
            val t = LocalDateTime.now()
            return ClockTime(
                hour24 = t.hour,
                minute = t.minute,
                second = t.second,
                dayOfMonth = t.dayOfMonth,
                month = t.monthValue,
                year = t.year,
                dayOfWeek = t.dayOfWeek.value
            )
        }

        fun of(hour24: Int, minute: Int, second: Int): ClockTime =
            ClockTime(hour24, minute, second, 1, 1, 2026, 1)
    }
}

/** "07:42" / "7:42" / "07:42:09" — honours 12/24h, leading zero, seconds. */
fun ClockTime.formatTime(face: ClockFace): String {
    val h = if (face.is24Hour) {
        if (face.leadingZero) hour24.toString().padStart(2, '0') else hour24.toString()
    } else {
        if (face.leadingZero) hour12.toString().padStart(2, '0') else hour12.toString()
    }
    val m = minute.toString().padStart(2, '0')
    val base = "$h:$m"
    val withSeconds = if (face.showSeconds) "$base:${second.toString().padStart(2, '0')}" else base
    return if (!face.is24Hour && face.showAmPm) "$withSeconds $amPm" else withSeconds
}

/** "Wed, 11 Nov 2026" — day-of-week + date, locale-safe. */
fun ClockTime.formatDate(): String {
    val t = LocalDateTime.of(year, month, dayOfMonth, hour24, minute, second)
    return t.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH))
}

/** Short date for widgets: "11 Nov". */
fun ClockTime.formatDateShort(): String {
    val t = LocalDateTime.of(year, month, dayOfMonth, hour24, minute, second)
    return t.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH))
}

// ---------------------------------------------------------------------------
// Word clock ("TWENTY PAST THREE", "QUARTER TO TWELVE", ...)
// ---------------------------------------------------------------------------

private val HOUR_WORDS = arrayOf(
    "TWELVE", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX",
    "SEVEN", "EIGHT", "NINE", "TEN", "ELEVEN"
)
private val MINUTE_WORDS = mapOf(
    5 to "FIVE", 10 to "TEN", 15 to "QUARTER", 20 to "TWENTY", 25 to "TWENTY-FIVE",
    30 to "HALF"
)

/** Word-clock phrase for this time, e.g. "TWENTY PAST THREE". */
fun ClockTime.wordPhrase(): String {
    val m = minute
    val hourWord = HOUR_WORDS[hour12 % 12]
    val nextHourWord = HOUR_WORDS[(hour12 % 12 + 1) % 12]
    return when {
        m == 0 -> "$hourWord O'CLOCK"
        m <= 30 -> {
            val label = MINUTE_WORDS[m] ?: run {
                // e.g. 7 -> "SEVEN PAST", 23 -> "TWENTY-THREE PAST"
                val tens = (m / 10) * 10
                val ones = m % 10
                val tensWord = when (tens) { 10 -> "TEN"; 20 -> "TWENTY"; else -> "" }
                val onesWord = if (ones > 0) HOUR_WORDS[ones] else ""
                listOf(tensWord, onesWord).filter { it.isNotEmpty() }.joinToString("-")
            }
            "$label PAST $hourWord"
        }
        else -> {
            val to = 60 - m
            val label = MINUTE_WORDS[to] ?: run {
                val tens = (to / 10) * 10
                val ones = to % 10
                val tensWord = when (tens) { 10 -> "TEN"; 20 -> "TWENTY"; else -> "" }
                val onesWord = if (ones > 0) HOUR_WORDS[ones] else ""
                listOf(tensWord, onesWord).filter { it.isNotEmpty() }.joinToString("-")
            }
            "$label TO $nextHourWord"
        }
    }
}

// ---------------------------------------------------------------------------
// Seven-segment display
// Segments: a (top), b (top-right), c (bottom-right), d (bottom),
//           e (bottom-left), f (top-left), g (middle).
// ---------------------------------------------------------------------------

/** Segments lit for each digit 0-9. Bit order: a b c d e f g. */
val SEVEN_SEGMENT_MAP: Map<Char, BooleanArray> = mapOf(
    '0' to booleanArrayOf(true, true, true, true, true, true, false),
    '1' to booleanArrayOf(false, true, true, false, false, false, false),
    '2' to booleanArrayOf(true, true, false, true, true, false, true),
    '3' to booleanArrayOf(true, true, true, true, false, false, true),
    '4' to booleanArrayOf(false, true, true, false, false, true, true),
    '5' to booleanArrayOf(true, false, true, true, false, true, true),
    '6' to booleanArrayOf(true, false, true, true, true, true, true),
    '7' to booleanArrayOf(true, true, true, false, false, false, false),
    '8' to booleanArrayOf(true, true, true, true, true, true, true),
    '9' to booleanArrayOf(true, true, true, true, false, true, true)
)

// ---------------------------------------------------------------------------
// Dot-matrix 5x7 font (digits + colon). Rows top-to-bottom, 5 columns.
// ---------------------------------------------------------------------------

val DOT_MATRIX_FONT: Map<Char, Array<String>> = mapOf(
    '0' to arrayOf("01110", "10001", "10011", "10101", "11001", "10001", "01110"),
    '1' to arrayOf("00100", "01100", "00100", "00100", "00100", "00100", "01110"),
    '2' to arrayOf("01110", "10001", "00001", "00110", "01000", "10000", "11111"),
    '3' to arrayOf("11111", "00010", "00100", "00010", "00001", "10001", "01110"),
    '4' to arrayOf("00010", "00110", "01010", "10010", "11111", "00010", "00010"),
    '5' to arrayOf("11111", "10000", "11110", "00001", "00001", "10001", "01110"),
    '6' to arrayOf("00110", "01000", "10000", "11110", "10001", "10001", "01110"),
    '7' to arrayOf("11111", "00001", "00010", "00100", "01000", "01000", "01000"),
    '8' to arrayOf("01110", "10001", "10001", "01110", "10001", "10001", "01110"),
    '9' to arrayOf("01110", "10001", "10001", "01111", "00001", "00010", "01100"),
    ':' to arrayOf("00000", "00100", "00100", "00000", "00100", "00100", "00000")
)

/** Next alarm in plain words ("in 7h 20m"), for the alarm screen. */
fun minutesUntil(hour: Int, minute: Int, now: ClockTime): Long {
    val nowMin = now.hour24 * 60 + now.minute
    val target = hour * 60 + minute
    var diff = target - nowMin
    if (diff <= 0) diff += 24 * 60
    return diff.toLong()
}
