package com.imdadburki.noctra.core.clock

import com.imdadburki.noctra.core.common.ClockFace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockTimeTest {

    private val face12 = ClockFace(is24Hour = false, showSeconds = true, showAmPm = true)
    private val face24 = ClockFace(is24Hour = true, showSeconds = false, showAmPm = false)

    @Test
    fun `12h format with seconds and AM-PM`() {
        val t = ClockTime.of(7, 42, 9)
        assertEquals("07:42:09 AM", t.formatTime(face12))
    }

    @Test
    fun `12h midnight is 12 AM`() {
        val t = ClockTime.of(0, 5, 0)
        assertEquals("12:05:00 AM", t.formatTime(face12))
    }

    @Test
    fun `12h noon is 12 PM`() {
        val t = ClockTime.of(12, 0, 0)
        assertEquals("12:00:00 PM", t.formatTime(face12))
    }

    @Test
    fun `24h format without seconds`() {
        val t = ClockTime.of(19, 30, 45)
        assertEquals("19:30", t.formatTime(face24))
    }

    @Test
    fun `24h leading zero kept`() {
        val t = ClockTime.of(7, 5, 0)
        assertEquals("07:05", t.formatTime(face24))
    }

    @Test
    fun `word phrase on the hour`() {
        assertEquals("THREE O'CLOCK", ClockTime.of(3, 0, 0).wordPhrase())
    }

    @Test
    fun `word phrase twenty past`() {
        assertEquals("TWENTY PAST THREE", ClockTime.of(3, 20, 0).wordPhrase())
    }

    @Test
    fun `word phrase half past`() {
        assertEquals("HALF PAST THREE", ClockTime.of(3, 30, 0).wordPhrase())
    }

    @Test
    fun `word phrase quarter to`() {
        assertEquals("QUARTER TO FOUR", ClockTime.of(3, 45, 0).wordPhrase())
    }

    @Test
    fun `word phrase five to`() {
        assertEquals("FIVE TO FOUR", ClockTime.of(3, 55, 0).wordPhrase())
    }

    @Test
    fun `seven segment 8 lights all segments`() {
        val seg = SEVEN_SEGMENT_MAP['8']!!
        assertTrue(seg.all { it })
    }

    @Test
    fun `seven segment 1 lights only b and c`() {
        val seg = SEVEN_SEGMENT_MAP['1']!!
        assertEquals(booleanArrayOf(false, true, true, false, false, false, false).toList(), seg.toList())
    }

    @Test
    fun `dot matrix digits are 7 rows of 5`() {
        for ((_, grid) in DOT_MATRIX_FONT) {
            assertEquals(7, grid.size)
            for (row in grid) assertEquals(5, row.length)
        }
    }

    @Test
    fun `minutes until later today`() {
        val now = ClockTime.of(10, 0, 0)
        assertEquals(120L, minutesUntil(12, 0, now))
    }

    @Test
    fun `minutes until wraps to tomorrow`() {
        val now = ClockTime.of(23, 0, 0)
        assertEquals(60L, minutesUntil(0, 0, now))
    }

    @Test
    fun `night detection`() {
        assertTrue(ClockTime.of(23, 0, 0).isNight)
        assertTrue(ClockTime.of(4, 0, 0).isNight)
        assertFalse(ClockTime.of(12, 0, 0).isNight)
    }
}
