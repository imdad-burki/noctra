package com.imdadburki.noctra.core.common

/** Deterministic background palette for built-in designs. */
private val BG_PALETTE = listOf(
    0xFF07070A to 0xFF1A1A22, 0xFF0A0A18 to 0xFF1B2A4A, 0xFF120A0A to 0xFF3A1414,
    0xFF0A120C to 0xFF14301C, 0xFF101014 to 0xFF2A2A33, 0xFF0D0D10 to 0xFF23232B,
    0xFF140F08 to 0xFF3A2A12, 0xFF0A0F14 to 0xFF12303A, 0xFF120A14 to 0xFF2E1430,
    0xFF0A0A0A to 0xFF1C1C1C
)

/** Builds the 70 built-in designs from the spec's collections — each with a
 *  distinct background + clock style, rendered by the real engine.
 *  Lives in :core:common so Discover and Editor share one source of truth. */
fun builtInDesigns(): List<WallpaperDesign> {
    val designs = mutableListOf<WallpaperDesign>()
    var i = 0
    for ((collection, names) in Collections.ALL) {
        for (name in names) {
            val (bg, bg2) = BG_PALETTE[i % BG_PALETTE.size]
            val digital = DigitalStyle.entries[i % DigitalStyle.entries.size]
            val kind = if (i % 5 == 0) ClockKind.ANALOG else ClockKind.DIGITAL
            designs += WallpaperDesign(
                id = "builtin-${collection.lowercase().replace(" ", "-")}-$i",
                title = name,
                collection = collection,
                backgroundHex = bg,
                backgroundHex2 = bg2,
                gradientAngle = (140 + (i * 17) % 80).toFloat(),
                clockFace = ClockFace(
                    kind = kind,
                    digitalStyle = digital.name,
                    analogStyle = "CLASSIC",
                    colorHex = 0xFFF5F5F7,
                    accentHex = if (i % 3 == 0) 0xFFD4AF37 else 0xFFF5F5F7
                )
            )
            i++
        }
    }
    return designs
}
