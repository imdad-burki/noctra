package com.imdadburki.noctra.core.clock

import com.imdadburki.noctra.core.common.DigitalLayout
import com.imdadburki.noctra.core.common.DigitalTreatment
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v2.0.1 regression test: the digital clock's time / AM/PM / date strips
 * must NEVER overlap, for every layout x treatment x date x AM/PM
 * combination. The v2 bug: AM/PM drawn inside the last digit row
 * (TALL/STACKED), seconds crossing the SPLIT halves, and a second date
 * line from the exporter over the engine's.
 *
 * NOTE: the geometry under test is pure Kotlin ([FloatRect]) — plain JVM
 * unit tests cannot call android.graphics.RectF methods (not mocked).
 */
class ClockStripTest {

    private val rect = FloatRect(0f, 0f, 1000f, 625f) // 1.6 grid card
    private val hero = FloatRect(0f, 0f, 320f, 320f) // hero clock

    @Test
    fun stripsNeverOverlap_gridAndHero() {
        for (r in listOf(rect, hero)) {
            for (layout in DigitalLayout.entries) {
                for (treatment in DigitalTreatment.entries) {
                    for (showDate in listOf(true, false)) {
                        for (amPm in listOf("", "PM")) {
                            val inline = isInlineAmPm(treatment, layout, amPm)
                            val (content, ampm, date) =
                                digitalStripRects(r, 64f, layout, showDate, amPm, inline)
                            // No two strips may share area (edge-touching is fine).
                            assertFalse(
                                "content x ampm overlap $layout/$treatment date=$showDate amPm='$amPm'",
                                content.intersects(ampm)
                            )
                            assertFalse(
                                "content x date overlap $layout/$treatment date=$showDate amPm='$amPm'",
                                content.intersects(date)
                            )
                            assertFalse(
                                "ampm x date overlap $layout/$treatment date=$showDate amPm='$amPm'",
                                ampm.intersects(date)
                            )
                            // Every strip stays inside the rect.
                            for (s in listOf(content, ampm, date)) {
                                assertTrue("strip left of rect", s.left >= r.left - 0.01f)
                                assertTrue("strip right of rect", s.right <= r.right + 0.01f)
                                assertTrue("strip above rect", s.top >= r.top - 0.01f)
                                assertTrue("strip below rect", s.bottom <= r.bottom + 0.01f)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun dateStripOnlyForStandardAndWide() {
        for (layout in DigitalLayout.entries) {
            val (_, _, date) = digitalStripRects(rect, 64f, layout, true, "", false)
            val expected = layout == DigitalLayout.STANDARD || layout == DigitalLayout.WIDE
            assertTrue(
                "date strip for $layout should be ${if (expected) "present" else "empty"}",
                (date.height() > 0f) == expected
            )
        }
    }

    @Test
    fun ampmStripOnlyWhenNotInline() {
        // Text treatment + STANDARD => inline => no strip.
        val (_, ampmInline, _) = digitalStripRects(
            rect, 64f, DigitalLayout.STANDARD, false, "PM",
            isInlineAmPm(DigitalTreatment.SOLID, DigitalLayout.STANDARD, "PM")
        )
        assertTrue("inline AM/PM must leave an empty strip", ampmInline.height() == 0f)
        // Segment treatment + STANDARD => strip.
        val (_, ampmStrip, _) = digitalStripRects(
            rect, 64f, DigitalLayout.STANDARD, false, "PM",
            isInlineAmPm(DigitalTreatment.SEVEN_SEGMENT, DigitalLayout.STANDARD, "PM")
        )
        assertTrue("segment AM/PM needs a strip", ampmStrip.height() > 0f)
        // No AM/PM at all => empty strip.
        val (_, ampmNone, _) = digitalStripRects(rect, 64f, DigitalLayout.TALL, false, "", false)
        assertTrue("no AM/PM => empty strip", ampmNone.height() == 0f)
    }

    @Test
    fun layoutLinesNeverOverlapEachOther() {
        for (layout in DigitalLayout.entries) {
            for (withSeconds in listOf(true, false)) {
                val text = if (withSeconds) "10:24:33" else "10:24"
                val lines = layoutLines(text, rect, 64f, layout)
                for (i in lines.indices) {
                    for (j in i + 1 until lines.size) {
                        assertFalse(
                            "lines $i x $j overlap in $layout '$text'",
                            lines[i].rect.intersects(lines[j].rect)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun splitSecondsRowStaysBelowHalves() {
        val lines = layoutLines("10:24:33", rect, 64f, DigitalLayout.SPLIT)
        assertTrue("split needs 3 lines with seconds", lines.size == 3)
        // Seconds line must start at or below the halves' bottom edge.
        val halvesBottom = maxOf(lines[0].rect.bottom, lines[1].rect.bottom)
        assertTrue("seconds row overlaps halves", lines[2].rect.top >= halvesBottom - 0.01f)
    }
}
