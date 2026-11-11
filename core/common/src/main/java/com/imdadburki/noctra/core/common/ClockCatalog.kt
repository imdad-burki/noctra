package com.imdadburki.noctra.core.common

/**
 * NOCTRA v2 clock catalogue — 150 genuinely different clocks.
 *
 * Built from the Pinterest research (flip clocks, neon glow, word clocks,
 * seven-segment LED, dot-matrix, terminal, minimal, roman, skeleton,
 * compass, sport chrono) and Imdad's order: "sab ek jaisy rakhy hain" —
 * so every entry differs on at least TWO axes:
 *
 * Digital: 6 layouts x 10 treatments x 2 colourways = 120
 *   layouts:    STANDARD, TALL (vertical digits), STACKED (HH/MM/SS rows),
 *               SPLIT (HH left / MM right), COMPACT (HH:MM), WIDE (+ date)
 *   treatments: SOLID, SEVEN_SEGMENT, DOT_MATRIX, FLIP, WORD,
 *               NEON, GRADIENT, OUTLINE, GLASS, TERMINAL
 * Analog: 10 dials x 3 hand shapes = 30
 *   dials: CLASSIC, GOLD_ACCENT, VINTAGE, SKELETON, COMPASS, SCIENTIFIC,
 *          ABSTRACT, MINIMAL, ROMAN, SPORT
 *   hands: TAPERED, NEEDLE, BOLD
 *
 * Total: 150. One clock, one time — each entry is a full ClockFace the
 * engine renders directly (DigitalCanvas / AnalogCanvas).
 */

private data class Colourway(val name: String, val color: Long, val accent: Long)

private fun colourways(treatment: DigitalTreatment): List<Colourway> = when (treatment) {
    DigitalTreatment.SEVEN_SEGMENT -> listOf(
        Colourway("Amber", 0xFFFFB340, 0xFFD4AF37),
        Colourway("Green", 0xFF30D158, 0xFF00FF66)
    )
    DigitalTreatment.DOT_MATRIX -> listOf(
        Colourway("Matrix Green", 0xFF00FF66, 0xFF30D158),
        Colourway("Cyan", 0xFF00E5FF, 0xFF0090FF)
    )
    DigitalTreatment.NEON -> listOf(
        Colourway("Pink", 0xFFFF2D78, 0xFF7B61FF),
        Colourway("Cyan", 0xFF00E5FF, 0xFF0090FF)
    )
    DigitalTreatment.TERMINAL -> listOf(
        Colourway("Green", 0xFF00FF66, 0xFF30D158),
        Colourway("Amber", 0xFFFFB340, 0xFFD4AF37)
    )
    DigitalTreatment.FLIP -> listOf(
        Colourway("Gold", 0xFFF5F5F7, 0xFFD4AF37),
        Colourway("White", 0xFFFFFFFF, 0xFF8E8E93)
    )
    DigitalTreatment.WORD -> listOf(
        Colourway("Gold", 0xFFE8C468, 0xFFD4AF37),
        Colourway("White", 0xFFF5F5F7, 0xFF8E8E93)
    )
    DigitalTreatment.GRADIENT -> listOf(
        Colourway("Gold Fade", 0xFFE8C468, 0xFFFF3B30),
        Colourway("Ocean Fade", 0xFF00E5FF, 0xFF7B61FF)
    )
    DigitalTreatment.OUTLINE -> listOf(
        Colourway("Gold", 0xFFE8C468, 0xFFD4AF37),
        Colourway("White", 0xFFF5F5F7, 0xFF8E8E93)
    )
    DigitalTreatment.GLASS -> listOf(
        Colourway("Frost", 0xFFF5F5F7, 0xFFD4AF37),
        Colourway("Ice", 0xFFD6F4FF, 0xFF00E5FF)
    )
    DigitalTreatment.SOLID -> listOf(
        Colourway("Gold", 0xFFE8C468, 0xFFD4AF37),
        Colourway("White", 0xFFF5F5F7, 0xFF8E8E93)
    )
}

/** The full 150-clock catalogue, in display order. */
fun allClockStyles(): List<ClockFace> {
    val out = ArrayList<ClockFace>(150)
    // --- Digital: 120 ------------------------------------------------------
    for (layout in DigitalLayout.entries) {
        for (treatment in DigitalTreatment.entries) {
            for (cw in colourways(treatment)) {
                out += ClockFace(
                    kind = ClockKind.DIGITAL,
                    displayName = "${treatment.label} ${layout.label} ${cw.name}",
                    digitalStyle = when (treatment) {
                        DigitalTreatment.SEVEN_SEGMENT -> DigitalStyle.SEVEN_SEGMENT.name
                        DigitalTreatment.DOT_MATRIX -> DigitalStyle.DOT_MATRIX.name
                        DigitalTreatment.FLIP -> DigitalStyle.FLIP.name
                        DigitalTreatment.WORD -> DigitalStyle.WORD.name
                        else -> DigitalStyle.STANDARD.name
                    },
                    digitalLayout = layout.name,
                    digitalTreatment = treatment.name,
                    is24Hour = false,
                    showSeconds = layout != DigitalLayout.COMPACT,
                    showDate = layout == DigitalLayout.WIDE || layout == DigitalLayout.STANDARD,
                    showAmPm = true,
                    colorHex = cw.color,
                    accentHex = cw.accent,
                    backgroundHex = 0xFF07070A
                )
            }
        }
    }
    // --- Analog: 30 ---------------------------------------------------------
    val dials = listOf(
        AnalogStyle.CLASSIC, AnalogStyle.GOLD_ACCENT, AnalogStyle.VINTAGE,
        AnalogStyle.SKELETON, AnalogStyle.COMPASS, AnalogStyle.SCIENTIFIC,
        AnalogStyle.ABSTRACT, AnalogStyle.MINIMAL, AnalogStyle.ROMAN,
        AnalogStyle.SPORT
    )
    for (dial in dials) {
        for (hand in AnalogHand.entries) {
            out += ClockFace(
                kind = ClockKind.ANALOG,
                displayName = "${dial.label} ${hand.label}",
                analogStyle = dial.name,
                analogHand = hand.name,
                is24Hour = false,
                showSeconds = true,
                showDate = true,
                showAmPm = false,
                colorHex = 0xFFF5F5F7,
                accentHex = if (dial == AnalogStyle.SPORT) 0xFFFF3B30 else 0xFFD4AF37,
                backgroundHex = 0xFF07070A
            )
        }
    }
    return out
}

/** Look a catalogue face up by its display name (the SettingsStore key). */
fun clockFaceByName(name: String): ClockFace =
    allClockStyles().firstOrNull { it.displayName == name } ?: allClockStyles().first()

/** The default clock (first catalogue entry: Solid Standard Gold). */
fun defaultClockFace(): ClockFace = allClockStyles().first()
