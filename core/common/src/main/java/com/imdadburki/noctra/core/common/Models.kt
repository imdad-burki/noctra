package com.imdadburki.noctra.core.common

import kotlinx.serialization.Serializable

/**
 * NOCTRA clock styles — the full catalogue from the spec.
 *
 * Every digital style has a REAL, distinct Canvas implementation in
 * :core:clock (DigitalClockRenderer). Styles are not buttons that all
 * draw the same clock: each one changes font weight, tracking, glow,
 * segment rendering, casing or layout in a measurable way.
 */
enum class DigitalStyle(val label: String) {
    STANDARD("Standard Digital"),
    SEVEN_SEGMENT("Seven Segment LED"),
    MINIMAL_THIN("Minimal Thin"),
    BOLD("Bold Digital"),
    ULTRA_THIN("Ultra Thin"),
    OVERSIZED("Oversized Digits"),
    VERTICAL("Vertical Digits"),
    SPLIT("Split Clock"),
    OUTLINE("Outline Clock"),
    FRAME("Frame Clock"),
    RETRO("Retro Digital"),
    AMBER_LED("Amber LED"),
    RED_LED("Red LED"),
    GREEN_LED("Green LED"),
    MATRIX("Matrix Inspired"),
    DOT_MATRIX("Dot Matrix"),
    LCD("LCD Clock"),
    FLIP("Flip Clock"),
    WORD("Word Clock"),
    TYPOGRAPHY("Typography Clock"),
    MONOSPACE("Monospace Clock"),
    EDITORIAL("Editorial Clock"),
    FLOATING("Floating Digits"),
    PERSPECTIVE("Perspective Clock"),
    GRADIENT("Gradient Clock"),
    GLASS("Glass-inspired Clock"),
    MINIMAL_MONO("Minimal Mono"),
    TERMINAL("Terminal Clock"),
    CYBER("Cyber Clock"),
    EXPERIMENTAL("Experimental Clock");

    companion object {
        fun fromName(name: String): DigitalStyle =
            entries.firstOrNull { it.name == name } ?: STANDARD
    }
}

/**
 * Analog clock styles — each one changes the dial, markers, numerals,
 * hand shapes or accent in AnalogClockRenderer.
 */
enum class AnalogStyle(val label: String) {
    CLASSIC("Classic Analog"),
    MINIMAL("Minimal Analog"),
    ROMAN("Roman Numerals"),
    MODERN_LUXURY("Modern Luxury"),
    VINTAGE("Vintage"),
    SKELETON("Skeleton-inspired"),
    COMPASS("Compass Clock"),
    MINIMAL_DOT("Minimal Dot"),
    GEOMETRIC("Geometric"),
    MONOCHROME("Monochrome"),
    GOLD_ACCENT("Gold Accent"),
    BLACK_EDITION("Black Edition"),
    CONTEMPORARY("Contemporary"),
    SCIENTIFIC("Scientific Dial"),
    ABSTRACT("Abstract Dial"),
    SPORT("Sport Chrono");

    companion object {
        fun fromName(name: String): AnalogStyle =
            entries.firstOrNull { it.name == name } ?: CLASSIC
    }
}

/**
 * Digital clock LAYOUT — the geometry of the time on screen.
 * This is what makes clocks look genuinely different from each other,
 * not just a colour change.
 */
enum class DigitalLayout(val label: String) {
    STANDARD("Standard"),   // one horizontal line HH:MM(:SS)
    TALL("Tall"),           // digits stacked vertically, one per line
    STACKED("Stacked"),     // three rows: HH / MM / SS
    SPLIT("Split"),         // HH big left, MM big right, colon between
    COMPACT("Compact"),     // HH:MM only, small and tight
    WIDE("Wide");           // time line + full date line beneath

    companion object {
        fun fromName(name: String): DigitalLayout =
            entries.firstOrNull { it.name == name } ?: STANDARD
    }
}

/**
 * Digital clock TREATMENT — how each digit is drawn.
 * Layout x Treatment x Colour is what gives 150 visibly different clocks.
 */
enum class DigitalTreatment(val label: String) {
    SOLID("Solid"),
    SEVEN_SEGMENT("Seven Segment"),
    DOT_MATRIX("Dot Matrix"),
    FLIP("Flip Cards"),
    WORD("Word Clock"),
    NEON("Neon Glow"),
    GRADIENT("Gradient"),
    OUTLINE("Outline"),
    GLASS("Glass Panel"),
    TERMINAL("Terminal");

    companion object {
        fun fromName(name: String): DigitalTreatment =
            entries.firstOrNull { it.name == name } ?: SOLID
    }
}

/** Analog hand shape. */
enum class AnalogHand(val label: String) {
    TAPERED("Tapered"),
    NEEDLE("Needle"),
    BOLD("Bold");

    companion object {
        fun fromName(name: String): AnalogHand =
            entries.firstOrNull { it.name == name } ?: TAPERED
    }
}

/** Which kind of clock face a design uses. */
enum class ClockKind { DIGITAL, ANALOG, HYBRID }

/** A clock face configuration: style + format + visual options. */
@Serializable
data class ClockFace(
    val kind: ClockKind = ClockKind.DIGITAL,
    val displayName: String = "",
    val digitalStyle: String = DigitalStyle.STANDARD.name,
    val analogStyle: String = AnalogStyle.CLASSIC.name,
    /** v2: layout + treatment are what make each of the 150 clocks distinct. */
    val digitalLayout: String = DigitalLayout.STANDARD.name,
    val digitalTreatment: String = DigitalTreatment.SOLID.name,
    val analogHand: String = AnalogHand.TAPERED.name,
    val is24Hour: Boolean = false,
    val showSeconds: Boolean = true,
    val showDate: Boolean = true,
    val showAmPm: Boolean = true,
    val leadingZero: Boolean = true,
    val colorHex: Long = 0xFFF5F5F7,
    val accentHex: Long = 0xFFD4AF37,
    val backgroundHex: Long = 0xFF07070A,
    val glow: Boolean = false,
    val outline: Boolean = false
)

/** A wallpaper design (built-in collection item or user project). */
@Serializable
data class WallpaperDesign(
    val id: String,
    val title: String,
    val collection: String,
    val creator: String = "NOCTRA Studio",
    val backgroundHex: Long = 0xFF07070A,
    val backgroundHex2: Long? = null,
    val gradientAngle: Float = 160f,
    val clockFace: ClockFace = ClockFace(),
    val clockScale: Float = 1f,
    val clockOffsetX: Float = 0f,
    val clockOffsetY: Float = 0f,
    val dateVisible: Boolean = true,
    val batteryVisible: Boolean = false,
    val customText: String = "",
    val isFavorite: Boolean = false,
    val createdAtMs: Long = 0L
)

/** Named collections from the spec (§5). */
object Collections {
    val AMOLED = listOf(
        "Pure Black", "Absolute Black", "Obsidian", "Shadowless", "Matte Black",
        "Zero Reflection", "Black Monolith", "Dark Geometry", "Invisible Clock", "Midnight"
    )
    val MAFIA = listOf(
        "Noir", "Midnight Executive", "Black Edition", "Crimson Hour",
        "Obsidian Luxury", "Dark Empire", "Silent Billionaire", "Vintage Noir",
        "Shadow Society", "Monochrome Mafia"
    )
    val MINIMAL = listOf(
        "Swiss", "Ultra Thin", "Clean Time", "White Space", "Minimal Grid",
        "Thin Line", "Architectural", "Modern Typography", "Monochrome", "Soft Contrast"
    )
    val RETRO = listOf(
        "Red LED", "Green LED", "Amber LED", "Seven Segment", "CRT Inspired",
        "Retro Alarm", "Old Radio", "80s Digital", "Vintage Flip", "Retro Futurism"
    )
    val CINEMATIC = listOf(
        "Desert Horizon", "Ocean Time", "Midnight Road", "Mountain Horizon",
        "Cosmic Clock", "Cloud Clock", "Rainy Window", "City at Night",
        "Space Typography", "Golden Hour"
    )
    val FUTURISTIC = listOf(
        "Neon", "Cyberpunk", "Matrix", "Holographic", "Digital Grid",
        "Abstract Geometry", "Electric Blue", "Experimental", "Sci-Fi", "Minimal Future"
    )
    val ADDITIONAL = listOf(
        "Vintage", "Nature", "Architecture", "Abstract", "Monochrome",
        "Typography", "Anime Original", "Landscape", "Dark Academia", "Luxury"
    )

    /** Collection name -> its wallpapers, in display order. */
    val ALL: List<Pair<String, List<String>>> = listOf(
        "AMOLED" to AMOLED,
        "Mafia / Dark Luxury" to MAFIA,
        "Minimal" to MINIMAL,
        "Retro" to RETRO,
        "Cinematic" to CINEMATIC,
        "Futuristic" to FUTURISTIC,
        "More" to ADDITIONAL
    )
}

/** Export formats + resolutions (spec §8). */
enum class ExportFormat(val extension: String) { PNG("png"), JPEG("jpg"), WEBP("webp") }

enum class ExportResolution(val label: String, val width: Int, val height: Int) {
    P720("720p", 720, 1280),
    P1080("1080p", 1080, 1920),
    P1440("1440p", 1440, 2560);

    companion object {
        fun native(w: Int, h: Int) = Native(w, h)
        data class Native(val width: Int, val height: Int)
    }
}
