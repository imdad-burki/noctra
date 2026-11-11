package com.imdadburki.noctra.core.clock

import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import com.imdadburki.noctra.core.common.ClockFace
import com.imdadburki.noctra.core.common.DigitalLayout
import com.imdadburki.noctra.core.common.DigitalStyle
import com.imdadburki.noctra.core.common.DigitalTreatment
import kotlin.math.min

/**
 * Shared Canvas clock renderer (android.graphics.Canvas).
 *
 * This is THE clock engine: the Compose screens, the wallpaper previews,
 * the export engine and the live wallpaper all call these functions with
 * the same [ClockFace] + [ClockTime], so the clock never renders
 * differently in two places.
 *
 * Every digital style has a genuinely different draw path — weight,
 * tracking, glow, outline, gradient, segment polygons, dot grids, flip
 * cards, word layout or transform. No two styles draw identically.
 */

// ---------------------------------------------------------------------------
// Styled text helper
// ---------------------------------------------------------------------------

private data class TextStyleParams(
    val sizePx: Float,
    val color: Int,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val mono: Boolean = false,
    val letterSpacingPx: Float = 0f,
    val glowColor: Int? = null,
    val glowRadius: Float = 0f,
    val outline: Boolean = false,
    val outlineWidth: Float = 0f,
    val gradientColors: IntArray? = null,
    val skewX: Float = 0f,
    val alpha: Int = 255
)

private fun makeTextPaint(p: TextStyleParams): Paint {
    val base = if (p.mono) Typeface.MONOSPACE else Typeface.SANS_SERIF
    val style = when {
        p.bold && p.italic -> Typeface.BOLD_ITALIC
        p.bold -> Typeface.BOLD
        p.italic -> Typeface.ITALIC
        else -> Typeface.NORMAL
    }
    return Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = p.sizePx
        typeface = Typeface.create(base, style)
        letterSpacing = if (p.sizePx > 0) p.letterSpacingPx / p.sizePx else 0f
        color = p.color
        alpha = p.alpha
        if (p.glowColor != null && p.glowRadius > 0) {
            setShadowLayer(p.glowRadius, 0f, 0f, p.glowColor)
        }
        if (p.outline) {
            this.style = Paint.Style.STROKE
            strokeWidth = p.outlineWidth
            strokeJoin = Paint.Join.ROUND
        }
        if (p.gradientColors != null && p.gradientColors.size >= 2) {
            shader = LinearGradient(
                0f, 0f, 0f, p.sizePx,
                p.gradientColors, null, Shader.TileMode.CLAMP
            )
        }
    }
}

/** Draws [text] centred in [rect] with the given style params. */
private fun Canvas.drawCenteredText(text: String, rect: RectF, p: TextStyleParams) {
    val paint = makeTextPaint(p)
    val width = paint.measureText(text)
    val fm = paint.fontMetrics
    val x = rect.centerX() - width / 2f
    val y = rect.centerY() - (fm.ascent + fm.descent) / 2f
    if (p.skewX != 0f) {
        save()
        skew(p.skewX, 0f)
        drawText(text, x, y, paint)
        restore()
    } else {
        drawText(text, x, y, paint)
    }
}

// ---------------------------------------------------------------------------
// Seven-segment renderer
// ---------------------------------------------------------------------------

private fun Canvas.drawSevenSegmentDigit(
    digit: Char, left: Float, top: Float, w: Float, h: Float,
    color: Int, thickness: Float
) {
    val seg = SEVEN_SEGMENT_MAP[digit] ?: return
    val t = thickness
    val x0 = left; val x1 = left + w
    val y0 = top; val y1 = top + h / 2f; val y2 = top + h
    val mid = (x0 + x1) / 2f
    // Segment polygons: a,b,c,d,e,f,g — each a hexagon-ish bar.
    fun hBar(y: Float): Path = Path().apply {
        moveTo(x0 + t, y); lineTo(x1 - t, y)
        lineTo(x1 - t / 2f, y + t / 2f); lineTo(x1 - t, y + t)
        lineTo(x0 + t, y + t); lineTo(x0 + t / 2f, y + t / 2f); close()
    }
    fun vBar(x: Float, yTop: Float, yBot: Float): Path = Path().apply {
        moveTo(x, yTop + t); lineTo(x + t / 2f, yTop + t / 2f); lineTo(x + t, yTop + t)
        lineTo(x + t, yBot - t); lineTo(x + t / 2f, yBot - t / 2f); lineTo(x, yBot - t); close()
    }
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    val paths = listOf(
        hBar(y0) to seg[0],            // a
        vBar(x1 - t, y0, y1) to seg[1], // b
        vBar(x1 - t, y1, y2) to seg[2], // c
        hBar(y2 - t) to seg[3],         // d
        vBar(x0, y1, y2) to seg[4],     // e
        vBar(x0, y0, y1) to seg[5],     // f
        hBar(y1 - t / 2f) to seg[6]     // g
    )
    // Unlit segments, faint — the "off" look of a real LED display.
    val offPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color; alpha = 24
    }
    for ((path, on) in paths) drawPath(path, if (on) paint else offPaint)
    @Suppress("UNUSED_VARIABLE") val unused = mid
}

private fun Canvas.drawSevenSegmentText(
    text: String, rect: RectF, color: Int, glow: Boolean
) {
    val chars = text.filter { it.isDigit() || it == ':' }
    if (chars.isEmpty()) return
    val digitW = rect.width() / (chars.length * 1.1f)
    val digitH = min(rect.height() * 0.92f, digitW * 1.9f)
    val thickness = digitW * 0.16f
    var x = rect.centerX() - (chars.length * digitW * 1.1f) / 2f
    val top = rect.centerY() - digitH / 2f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        if (glow) setShadowLayer(thickness, 0f, 0f, color)
    }
    for (ch in chars) {
        if (ch == ':') {
            drawCircle(x + digitW * 0.3f, rect.centerY() - digitH * 0.22f, thickness * 0.45f, paint)
            drawCircle(x + digitW * 0.3f, rect.centerY() + digitH * 0.22f, thickness * 0.45f, paint)
            x += digitW * 0.55f
        } else {
            drawSevenSegmentDigit(ch, x, top, digitW, digitH, color, thickness)
            x += digitW * 1.1f
        }
    }
}

// ---------------------------------------------------------------------------
// Dot-matrix renderer
// ---------------------------------------------------------------------------

private fun Canvas.drawDotMatrixText(text: String, rect: RectF, color: Int) {
    val chars = text.filter { DOT_MATRIX_FONT.containsKey(it) }
    if (chars.isEmpty()) return
    val cols = chars.sumOf { (DOT_MATRIX_FONT[it]?.firstOrNull()?.length ?: 5) + 1 }
    val cell = min(rect.width() / cols, rect.height() / 7.5f)
    val radius = cell * 0.32f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    val offPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; alpha = 18 }
    val totalW = cols * cell
    var x = rect.centerX() - totalW / 2f
    val top = rect.centerY() - 3.5f * cell
    for (ch in chars) {
        val grid = DOT_MATRIX_FONT[ch] ?: continue
        for (r in grid.indices) {
            for (c in grid[r].indices) {
                val cx = x + c * cell + cell / 2f
                val cy = top + r * cell + cell / 2f
                drawCircle(cx, cy, radius, if (grid[r][c] == '1') paint else offPaint)
            }
        }
        x += (grid[0].length + 1) * cell
    }
}

// ---------------------------------------------------------------------------
// Flip clock renderer
// ---------------------------------------------------------------------------

private fun Canvas.drawFlipText(text: String, rect: RectF, color: Int, bgColor: Int) {
    val parts = text.split(":")
    val cardW = rect.width() / (parts.size * 2f)
    val cardH = min(rect.height() * 0.9f, cardW * 1.25f)
    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = bgColor }
    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.argb(90, 0, 0, 0); strokeWidth = 3f
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textSize = cardH * 0.62f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    var x = rect.centerX() - (parts.size * cardW * 1.35f) / 2f
    val top = rect.centerY() - cardH / 2f
    for ((i, part) in parts.withIndex()) {
        val cardRect = RectF(x, top, x + cardW * 1.2f, top + cardH)
        drawRoundRect(cardRect, 14f, 14f, cardPaint)
        // Flip hinge line across the middle.
        drawLine(cardRect.left, rect.centerY(), cardRect.right, rect.centerY(), linePaint)
        val fm = textPaint.fontMetrics
        drawText(
            part, cardRect.centerX(),
            rect.centerY() - (fm.ascent + fm.descent) / 2f, textPaint
        )
        x += cardW * 1.35f
        if (i < parts.size - 1) {
            // Colon dots between cards.
            drawCircle(x - cardW * 0.14f, rect.centerY() - cardH * 0.18f, 5f, textPaint)
            drawCircle(x - cardW * 0.14f, rect.centerY() + cardH * 0.18f, 5f, textPaint)
        }
    }
}

// ---------------------------------------------------------------------------
// Main entry: draw a digital clock into [rect].
// ---------------------------------------------------------------------------
//
// v2 — layout x treatment engine.
//
// A clock face is now defined by TWO independent axes:
//   * layout    — STANDARD / TALL / STACKED / SPLIT / COMPACT / WIDE
//   * treatment — SOLID / SEVEN_SEGMENT / DOT_MATRIX / FLIP / WORD /
//                 NEON / GRADIENT / OUTLINE / GLASS / TERMINAL
// Layout decides WHERE the digits go; treatment decides HOW each digit is
// drawn. 6 x 10 x 2 colourways = 120 visibly different digital clocks.
// Legacy v1 faces (built-in wallpaper designs) carry only a DigitalStyle:
// they are mapped onto the same axes below so nothing renders flat.

/**
 * Plain-float rectangle for the clock's PURE geometry (strip math and
 * line layout). android.graphics.RectF methods are NOT mocked in plain
 * JVM unit tests, so every pure function here works on FloatRect and the
 * Canvas code converts at the boundary ([toRectF]). That is what lets
 * ClockStripTest prove the strips never overlap.
 */
data class FloatRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun width(): Float = right - left
    fun height(): Float = bottom - top
    fun centerX(): Float = (left + right) / 2f
    fun centerY(): Float = (top + bottom) / 2f
    /** True only when the two share AREA (edge-touching is not overlap). */
    fun intersects(other: FloatRect): Boolean =
        left < other.right && other.left < right && top < other.bottom && other.top < bottom
    fun toRectF(): RectF = RectF(left, top, right, bottom)
    companion object {
        fun fromRectF(r: RectF): FloatRect = FloatRect(r.left, r.top, r.right, r.bottom)
    }
}

internal data class ClockLine(val text: String, val rect: FloatRect, val sizePx: Float)

/** Legacy v1 DigitalStyle -> (layout, treatment, colour override). */
private fun legacyMapping(face: ClockFace): Triple<DigitalLayout, DigitalTreatment, Int?> {
    val style = DigitalStyle.fromName(face.digitalStyle)
    return when (style) {
        DigitalStyle.SEVEN_SEGMENT -> Triple(DigitalLayout.STANDARD, DigitalTreatment.SEVEN_SEGMENT, null)
        DigitalStyle.AMBER_LED -> Triple(DigitalLayout.STANDARD, DigitalTreatment.SEVEN_SEGMENT, 0xFFFFB340.toInt())
        DigitalStyle.RED_LED -> Triple(DigitalLayout.STANDARD, DigitalTreatment.SEVEN_SEGMENT, 0xFFFF3B30.toInt())
        DigitalStyle.GREEN_LED -> Triple(DigitalLayout.STANDARD, DigitalTreatment.SEVEN_SEGMENT, 0xFF30D158.toInt())
        DigitalStyle.DOT_MATRIX -> Triple(DigitalLayout.STANDARD, DigitalTreatment.DOT_MATRIX, null)
        DigitalStyle.MATRIX -> Triple(DigitalLayout.STANDARD, DigitalTreatment.DOT_MATRIX, 0xFF00FF66.toInt())
        DigitalStyle.FLIP -> Triple(DigitalLayout.STANDARD, DigitalTreatment.FLIP, null)
        DigitalStyle.WORD -> Triple(DigitalLayout.STANDARD, DigitalTreatment.WORD, null)
        DigitalStyle.VERTICAL -> Triple(DigitalLayout.TALL, DigitalTreatment.SOLID, null)
        DigitalStyle.SPLIT -> Triple(DigitalLayout.SPLIT, DigitalTreatment.SOLID, null)
        DigitalStyle.RETRO -> Triple(DigitalLayout.STANDARD, DigitalTreatment.TERMINAL, 0xFFFFB340.toInt())
        DigitalStyle.TERMINAL -> Triple(DigitalLayout.STANDARD, DigitalTreatment.TERMINAL, 0xFF00FF66.toInt())
        DigitalStyle.CYBER -> Triple(DigitalLayout.STANDARD, DigitalTreatment.NEON, 0xFF00E5FF.toInt())
        DigitalStyle.GRADIENT -> Triple(DigitalLayout.STANDARD, DigitalTreatment.GRADIENT, null)
        DigitalStyle.GLASS -> Triple(DigitalLayout.STANDARD, DigitalTreatment.GLASS, null)
        DigitalStyle.OUTLINE -> Triple(DigitalLayout.STANDARD, DigitalTreatment.OUTLINE, null)
        DigitalStyle.OVERSIZED -> Triple(DigitalLayout.STANDARD, DigitalTreatment.SOLID, null)
        else -> Triple(DigitalLayout.STANDARD, DigitalTreatment.SOLID, null)
    }
}

private fun effectiveAxes(face: ClockFace): Triple<DigitalLayout, DigitalTreatment, Int?> {
    val layout = DigitalLayout.fromName(face.digitalLayout)
    val treatment = DigitalTreatment.fromName(face.digitalTreatment)
    // v2 faces set layout/treatment explicitly; v1 faces fall back to the map.
    return if (layout != DigitalLayout.STANDARD || treatment != DigitalTreatment.SOLID) {
        Triple(layout, treatment, null)
    } else {
        legacyMapping(face)
    }
}

/** Split the time into lines for the given layout. */
internal fun layoutLines(
    timeText: String, rect: FloatRect, sizePx: Float, layout: DigitalLayout
): List<ClockLine> {
    val parts = timeText.split(":")
    return when (layout) {
        DigitalLayout.STANDARD -> listOf(ClockLine(timeText, rect, sizePx))
        DigitalLayout.COMPACT -> {
            val short = parts.take(2).joinToString(":")
            listOf(ClockLine(short, rect, sizePx * 0.9f))
        }
        DigitalLayout.WIDE -> {
            // Time on top, date line beneath (date drawn by the caller).
            val timeRect = FloatRect(rect.left, rect.top, rect.right, rect.top + rect.height() * 0.62f)
            listOf(ClockLine(timeText, timeRect, sizePx * 0.92f))
        }
        DigitalLayout.TALL -> {
            val digits = timeText.filter { it.isDigit() }
            if (digits.isEmpty()) return listOf(ClockLine(timeText, rect, sizePx))
            val rowH = rect.height() / digits.length
            digits.mapIndexed { i, d ->
                ClockLine(
                    d.toString(),
                    FloatRect(rect.left, rect.top + i * rowH, rect.right, rect.top + (i + 1) * rowH),
                    min(rowH * 0.78f, rect.width() * 0.85f)
                )
            }
        }
        DigitalLayout.STACKED -> {
            val rowH = rect.height() / parts.size
            parts.mapIndexed { i, part ->
                ClockLine(
                    part,
                    FloatRect(rect.left, rect.top + i * rowH, rect.right, rect.top + (i + 1) * rowH),
                    min(rowH * 0.72f, rect.width() / (part.length * 0.62f + 0.4f))
                )
            }
        }
        DigitalLayout.SPLIT -> {
            // Halves on top (72%), seconds row underneath (28%) — the old
            // version drew the seconds line across the halves' bottom edge.
            val topH = rect.height() * 0.72f
            val left = FloatRect(rect.left, rect.top, rect.centerX() - 6f, rect.top + topH)
            val right = FloatRect(rect.centerX() + 6f, rect.top, rect.right, rect.top + topH)
            val lines = mutableListOf(
                ClockLine(parts.getOrElse(0) { "" }, left, sizePx * 0.95f),
                ClockLine(parts.getOrElse(1) { "" }, right, sizePx * 0.95f)
            )
            if (parts.size > 2) {
                lines += ClockLine(
                    parts[2],
                    FloatRect(rect.left, rect.top + topH, rect.right, rect.bottom),
                    sizePx * 0.34f
                )
            }
            lines
        }
    }
}

/** Text params for one line under a text-based treatment. */
private fun treatmentParams(
    treatment: DigitalTreatment, sizePx: Float, color: Int, accent: Int
): TextStyleParams = when (treatment) {
    DigitalTreatment.SOLID -> TextStyleParams(sizePx, color)
    DigitalTreatment.NEON -> TextStyleParams(
        sizePx, color, bold = true,
        glowColor = color, glowRadius = sizePx * 0.28f
    )
    DigitalTreatment.GRADIENT -> TextStyleParams(
        sizePx, color, bold = true,
        gradientColors = intArrayOf(color, accent)
    )
    DigitalTreatment.OUTLINE -> TextStyleParams(
        sizePx, color, bold = true, outline = true, outlineWidth = sizePx * 0.05f
    )
    DigitalTreatment.GLASS -> TextStyleParams(sizePx, color, bold = true, alpha = 235)
    DigitalTreatment.TERMINAL -> TextStyleParams(
        sizePx, color, mono = true,
        glowColor = color, glowRadius = sizePx * 0.08f
    )
    else -> TextStyleParams(sizePx, color)
}

/**
 * Pure strip math for the digital clock — no Canvas, so unit tests can
 * prove the strips never overlap (see ClockStripTest). The rect is split
 * bottom-up: date strip, AM/PM strip, then the time's content strip.
 */
fun digitalStripRects(
    rect: FloatRect,
    sizePx: Float,
    layout: DigitalLayout,
    showDate: Boolean,
    amPm: String,
    inlineAmPm: Boolean
): Triple<FloatRect, FloatRect, FloatRect> {
    val dateH = if (showDate &&
        (layout == DigitalLayout.STANDARD || layout == DigitalLayout.WIDE)
    ) sizePx * 0.46f else 0f
    val ampmH = if (amPm.isNotEmpty() && !inlineAmPm) sizePx * 0.44f else 0f
    val content = FloatRect(rect.left, rect.top, rect.right, rect.bottom - dateH - ampmH)
    val ampm = FloatRect(rect.left, rect.bottom - dateH - ampmH, rect.right, rect.bottom - dateH)
    val date = FloatRect(rect.left, rect.bottom - dateH, rect.right, rect.bottom)
    return Triple(content, ampm, date)
}

/**
 * Inline AM/PM (right after the time, same baseline) is only safe for
 * text treatments on single-line layouts; everything else gets its own
 * strip. Pure, so tests can enumerate every combination.
 */
fun isInlineAmPm(treatment: DigitalTreatment, layout: DigitalLayout, amPm: String): Boolean {
    if (amPm.isEmpty()) return false
    val textTreatment = treatment == DigitalTreatment.SOLID ||
        treatment == DigitalTreatment.NEON ||
        treatment == DigitalTreatment.GRADIENT ||
        treatment == DigitalTreatment.OUTLINE ||
        treatment == DigitalTreatment.GLASS ||
        treatment == DigitalTreatment.TERMINAL
    return textTreatment &&
        (layout == DigitalLayout.STANDARD || layout == DigitalLayout.COMPACT ||
            layout == DigitalLayout.WIDE)
}

/**
 * Draws the digital clock for [face] + [time] centred in [rect].
 * [sizePx] is the base text size; callers pass a value fitted to the rect.
 *
 * v2.0.1 — OVERLAP FIX: the rect is split into reserved strips UP FRONT
 * via [digitalStripRects]: a content strip (the time), an AM/PM strip and
 * a date strip, each with its own rectangle. Time, AM/PM and date can
 * never draw on top of each other, in ANY layout — the v2 bug was AM/PM
 * drawn inside the last digit row (TALL/STACKED) and the seconds line
 * crossing the SPLIT halves, plus the exporter drawing a second date
 * line over the engine's.
 */
fun drawDigitalClock(
    canvas: Canvas,
    face: ClockFace,
    time: ClockTime,
    rect: RectF,
    sizePx: Float
) {
    val color = face.colorHex.toInt()
    val accent = face.accentHex.toInt()
    val (layout, treatment, colorOverride) = effectiveAxes(face)
    val drawColor = colorOverride ?: color
    val text = time.formatTime(face.copy(showAmPm = false))
    val amPm = if (!face.is24Hour && face.showAmPm) time.amPm else ""

    // --- Reserved strips (bottom-up): date, then AM/PM, then the time. ---
    val fr = FloatRect.fromRectF(rect)
    val inlineAmPm = isInlineAmPm(treatment, layout, amPm)
    val (contentRect, ampmRect, dateRect) =
        digitalStripRects(fr, sizePx, layout, face.showDate, amPm, inlineAmPm)
    val dateH = dateRect.height()
    // Inline AM/PM sits right of the time — shrink the time a little so the
    // marker never runs past the view's right edge.
    val effSizePx = if (inlineAmPm) sizePx * 0.88f else sizePx

    // Word treatment: the phrase IS the layout; it fills the content strip.
    if (treatment == DigitalTreatment.WORD) {
        val phrase = time.wordPhrase()
        val words = phrase.split(" ")
        val lines = if (words.size > 2) {
            listOf(words.take(2).joinToString(" "), words.drop(2).joinToString(" "))
        } else listOf(phrase)
        val lineH = contentRect.height() / lines.size.coerceAtLeast(1)
        var y = contentRect.top + lineH / 2f
        for (line in lines) {
            val r = RectF(contentRect.left, y - lineH / 2f, contentRect.right, y + lineH / 2f)
            canvas.drawCenteredText(
                line, r,
                TextStyleParams(min(lineH * 0.62f, sizePx * 0.62f), drawColor, bold = true)
            )
            y += lineH
        }
    } else {
        val lines = layoutLines(text, contentRect, effSizePx, layout)
        for ((index, line) in lines.withIndex()) {
            val lr = line.rect.toRectF()
            when (treatment) {
                DigitalTreatment.SEVEN_SEGMENT ->
                    canvas.drawSevenSegmentText(line.text, lr, drawColor, glow = true)
                DigitalTreatment.DOT_MATRIX ->
                    canvas.drawDotMatrixText(line.text, lr, drawColor)
                DigitalTreatment.FLIP ->
                    canvas.drawFlipText(line.text, lr, drawColor, 0xFF1C1C24.toInt())
                DigitalTreatment.GLASS -> {
                    // Translucent panel behind the line, then solid text.
                    val params = treatmentParams(treatment, line.sizePx, drawColor, accent)
                    val measure = makeTextPaint(params).measureText(line.text)
                    val pad = line.sizePx * 0.3f
                    canvas.drawRoundRect(
                        RectF(
                            lr.centerX() - measure / 2f - pad,
                            lr.centerY() - line.sizePx * 0.7f,
                            lr.centerX() + measure / 2f + pad,
                            lr.centerY() + line.sizePx * 0.7f
                        ),
                        24f, 24f,
                        Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            this.color = AndroidColor.argb(60, 255, 255, 255)
                        }
                    )
                    canvas.drawCenteredText(line.text, lr, params)
                }
                else -> {
                    // SPLIT layout: second half (minutes) in the accent colour.
                    val lineColor =
                        if (layout == DigitalLayout.SPLIT && index == 1) accent else drawColor
                    val params = treatmentParams(treatment, line.sizePx, lineColor, accent)
                    canvas.drawCenteredText(line.text, lr, params)
                    // Inline AM/PM: right after the first line's measured
                    // text, on the SAME baseline — never on top of the time.
                    if (inlineAmPm && index == 0) {
                        val measure = makeTextPaint(params).measureText(line.text)
                        val amX = lr.centerX() + measure / 2f + sizePx * 0.14f
                        val amPaint = makeTextPaint(
                            TextStyleParams(sizePx * 0.32f, accent, bold = true)
                        )
                        val fm = amPaint.fontMetrics
                        val amY = lr.centerY() - (fm.ascent + fm.descent) / 2f
                        canvas.drawText(amPm, amX, amY, amPaint)
                    }
                }
            }
        }
    }

    // AM/PM strip (only when not drawn inline).
    if (amPm.isNotEmpty() && !inlineAmPm) {
        canvas.drawCenteredText(
            amPm, ampmRect.toRectF(),
            TextStyleParams(sizePx * 0.3f, accent, bold = true)
        )
    }

    // Date strip (standard + wide layouts only).
    if (dateH > 0f) {
        canvas.drawCenteredText(
            time.formatDate(), dateRect.toRectF(),
            TextStyleParams(sizePx * 0.3f, accent)
        )
    }
}
