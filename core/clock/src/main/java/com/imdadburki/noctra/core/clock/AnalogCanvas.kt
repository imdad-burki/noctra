package com.imdadburki.noctra.core.clock

import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.imdadburki.noctra.core.common.AnalogHand
import com.imdadburki.noctra.core.common.AnalogStyle
import com.imdadburki.noctra.core.common.ClockFace
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Analog clock renderer (android.graphics.Canvas).
 * One shared draw path for screens, previews, exports and live wallpaper.
 * Each of the 15 styles changes the dial, markers, numerals, hand shapes
 * or accent — no two styles render identically.
 */

private val ROMAN = arrayOf(
    "XII", "I", "II", "III", "IIII", "V", "VI", "VII", "VIII", "IX", "X", "XI"
)

private fun paint(color: Int, width: Float = 0f, style: Paint.Style = Paint.Style.FILL): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.style = style
        strokeWidth = width
        strokeCap = Paint.Cap.ROUND
    }

/** A tapered hand: wide at the base, pointed at the tip. */
private fun handPath(cx: Float, cy: Float, angleDeg: Float, len: Float, tailLen: Float, halfW: Float): Path {
    val a = Math.toRadians((angleDeg - 90).toDouble())
    val dx = cos(a).toFloat(); val dy = sin(a).toFloat()
    val px = -dy; val py = dx // perpendicular
    val tipX = cx + dx * len; val tipY = cy + dy * len
    val tailX = cx - dx * tailLen; val tailY = cy - dy * tailLen
    return Path().apply {
        moveTo(cx + px * halfW, cy + py * halfW)
        lineTo(tipX, tipY)
        lineTo(cx - px * halfW, cy - py * halfW)
        lineTo(tailX, tailY)
        close()
    }
}

fun drawAnalogClock(canvas: Canvas, face: ClockFace, time: ClockTime, rect: RectF) {
    val style = AnalogStyle.fromName(face.analogStyle)
    val cx = rect.centerX(); val cy = rect.centerY()
    val r = min(rect.width(), rect.height()) / 2f
    val fg = face.colorHex.toInt()
    val accent = face.accentHex.toInt()
    val bg = face.backgroundHex.toInt()

    // --- Dial ---------------------------------------------------------------
    when (style) {
        AnalogStyle.BLACK_EDITION, AnalogStyle.MONOCHROME -> {
            canvas.drawCircle(cx, cy, r, paint(0xFF000000.toInt()))
            canvas.drawCircle(cx, cy, r, paint(fg, r * 0.02f, Paint.Style.STROKE))
        }
        AnalogStyle.GOLD_ACCENT, AnalogStyle.MODERN_LUXURY -> {
            canvas.drawCircle(cx, cy, r, paint(0xFF0D0D10.toInt()))
            canvas.drawCircle(cx, cy, r, paint(accent, r * 0.025f, Paint.Style.STROKE))
            canvas.drawCircle(cx, cy, r * 0.94f, paint(accent, r * 0.006f, Paint.Style.STROKE))
        }
        AnalogStyle.VINTAGE -> {
            canvas.drawCircle(cx, cy, r, paint(0xFF1A140D.toInt()))
            canvas.drawCircle(cx, cy, r, paint(0xFF8A6D3B.toInt(), r * 0.03f, Paint.Style.STROKE))
        }
        AnalogStyle.SKELETON -> {
            canvas.drawCircle(cx, cy, r, paint(0xFF0A0A0C.toInt()))
            canvas.drawCircle(cx, cy, r * 0.62f, paint(0xFF23232B.toInt()))
            canvas.drawCircle(cx, cy, r, paint(fg, r * 0.015f, Paint.Style.STROKE))
        }
        AnalogStyle.COMPASS -> {
            canvas.drawCircle(cx, cy, r, paint(0xFF0B1116.toInt()))
            canvas.drawCircle(cx, cy, r, paint(0xFF3A5A6E.toInt(), r * 0.02f, Paint.Style.STROKE))
        }
        AnalogStyle.SCIENTIFIC -> {
            canvas.drawCircle(cx, cy, r, paint(0xFF0E0E10.toInt()))
            canvas.drawCircle(cx, cy, r * 0.82f, paint(0xFF2A2A33.toInt(), r * 0.008f, Paint.Style.STROKE))
            canvas.drawCircle(cx, cy, r, paint(fg, r * 0.015f, Paint.Style.STROKE))
        }
        AnalogStyle.SPORT -> {
            // Bold sport chrono: dark dial, thick accent ring, red second zone.
            canvas.drawCircle(cx, cy, r, paint(0xFF0A0A0E.toInt()))
            canvas.drawCircle(cx, cy, r, paint(accent, r * 0.035f, Paint.Style.STROKE))
            canvas.drawArc(
                RectF(cx - r * 0.8f, cy - r * 0.8f, cx + r * 0.8f, cy + r * 0.8f),
                -90f, 90f, false,
                paint(0xFFFF3B30.toInt(), r * 0.02f, Paint.Style.STROKE)
            )
        }
        AnalogStyle.ABSTRACT -> {
            // Off-centre dial + accent arc — the abstract look.
            canvas.drawCircle(cx - r * 0.08f, cy + r * 0.06f, r * 0.9f, paint(0xFF14141A.toInt()))
            canvas.drawArc(
                RectF(cx - r, cy - r, cx + r, cy + r), -90f, 200f, false,
                paint(accent, r * 0.03f, Paint.Style.STROKE)
            )
        }
        else -> {
            canvas.drawCircle(cx, cy, r, paint(bg))
            canvas.drawCircle(cx, cy, r, paint(fg, r * 0.015f, Paint.Style.STROKE))
        }
    }

    // --- Markers / numerals ---------------------------------------------------
    val numeralPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fg
        textSize = r * 0.16f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    val romanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fg
        textSize = r * 0.13f
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    for (i in 0 until 60) {
        val angle = Math.toRadians((i * 6 - 90).toDouble())
        val isHour = i % 5 == 0
        val inner = r * if (isHour) 0.82f else 0.88f
        val outer = r * 0.94f
        val x0 = cx + cos(angle).toFloat() * inner
        val y0 = cy + sin(angle).toFloat() * inner
        val x1 = cx + cos(angle).toFloat() * outer
        val y1 = cy + sin(angle).toFloat() * outer
        when (style) {
            AnalogStyle.MINIMAL, AnalogStyle.MINIMAL_DOT -> {
                if (isHour) canvas.drawCircle(x1, y1, r * 0.022f, paint(fg))
            }
            AnalogStyle.SPORT -> {
                if (isHour) {
                    val s = r * 0.045f
                    canvas.drawRect(x1 - s, y1 - s, x1 + s, y1 + s, paint(fg))
                } else {
                    canvas.drawCircle(x1, y1, r * 0.01f, paint(fg))
                }
            }
            AnalogStyle.GEOMETRIC -> {
                if (isHour) {
                    // Small squares at hours, dots at minutes.
                    val s = r * 0.03f
                    canvas.drawRect(x1 - s, y1 - s, x1 + s, y1 + s, paint(accent))
                } else if (i % 15 == 0) {
                    canvas.drawCircle(x1, y1, r * 0.012f, paint(fg))
                }
            }
            else -> {
                canvas.drawLine(
                    x0, y0, x1, y1,
                    paint(if (isHour) fg else AndroidColor.argb(120, 245, 245, 247), r * if (isHour) 0.028f else 0.01f)
                )
            }
        }
    }
    // Numerals.
    when (style) {
        AnalogStyle.ROMAN, AnalogStyle.VINTAGE -> {
            for (h in 0 until 12) {
                val angle = Math.toRadians((h * 30 - 90).toDouble())
                val x = cx + cos(angle).toFloat() * r * 0.68f
                val y = cy + sin(angle).toFloat() * r * 0.68f
                val fm = romanPaint.fontMetrics
                canvas.drawText(ROMAN[h], x, y - (fm.ascent + fm.descent) / 2f, romanPaint)
            }
        }
        AnalogStyle.CLASSIC, AnalogStyle.MODERN_LUXURY, AnalogStyle.GOLD_ACCENT,
        AnalogStyle.CONTEMPORARY, AnalogStyle.SCIENTIFIC -> {
            for (h in 1..12) {
                val angle = Math.toRadians((h * 30 - 90).toDouble())
                val x = cx + cos(angle).toFloat() * r * 0.68f
                val y = cy + sin(angle).toFloat() * r * 0.68f
                val fm = numeralPaint.fontMetrics
                canvas.drawText(h.toString(), x, y - (fm.ascent + fm.descent) / 2f, numeralPaint)
            }
        }
        AnalogStyle.COMPASS -> {
            val dirs = arrayOf("N", "E", "S", "W")
            for (i in dirs.indices) {
                val angle = Math.toRadians((i * 90 - 90).toDouble())
                val x = cx + cos(angle).toFloat() * r * 0.68f
                val y = cy + sin(angle).toFloat() * r * 0.68f
                val fm = numeralPaint.fontMetrics
                canvas.drawText(dirs[i], x, y - (fm.ascent + fm.descent) / 2f, numeralPaint)
            }
        }
        else -> { /* Minimal / dot / geometric / monochrome / black / abstract: no numerals */ }
    }

    // --- Date window (Classic / Luxury / Contemporary) -----------------------
    if (style == AnalogStyle.CLASSIC || style == AnalogStyle.MODERN_LUXURY ||
        style == AnalogStyle.CONTEMPORARY
    ) {
        val w = r * 0.22f; val h = r * 0.16f
        val dateRect = RectF(cx + r * 0.42f - w / 2f, cy - h / 2f, cx + r * 0.42f + w / 2f, cy + h / 2f)
        canvas.drawRoundRect(dateRect, 6f, 6f, paint(0xFFFFFFFF.toInt()))
        val dp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF000000.toInt()
            textSize = r * 0.11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val fm = dp.fontMetrics
        canvas.drawText(
            time.dayOfMonth.toString(), dateRect.centerX(),
            dateRect.centerY() - (fm.ascent + fm.descent) / 2f, dp
        )
    }

    // --- Hands ---------------------------------------------------------------
    // v2: hand shape comes from the face (TAPERED / NEEDLE / BOLD) — the
    // third axis of the analog catalogue (dial x hand x colour).
    val handStyle = AnalogHand.fromName(face.analogHand)
    val hourAngle = ((time.hour24 % 12) + time.minute / 60f) * 30f
    val minuteAngle = (time.minute + time.second / 60f) * 6f
    val secondAngle = time.second * 6f

    val handColor = when (style) {
        AnalogStyle.MONOCHROME, AnalogStyle.BLACK_EDITION -> 0xFFF5F5F7.toInt()
        AnalogStyle.GOLD_ACCENT, AnalogStyle.MODERN_LUXURY -> 0xFFE8C468.toInt()
        else -> fg
    }
    val handWidth = when (style) {
        AnalogStyle.MINIMAL, AnalogStyle.MINIMAL_DOT -> r * 0.02f
        AnalogStyle.SKELETON -> r * 0.035f
        else -> r * 0.045f
    }
    fun drawHand(angleDeg: Float, len: Float, halfW: Float, color: Int) {
        when (handStyle) {
            AnalogHand.NEEDLE -> {
                // Thin needle: a single line with a round cap.
                val a = Math.toRadians((angleDeg - 90).toDouble())
                val dx = cos(a).toFloat(); val dy = sin(a).toFloat()
                canvas.drawLine(
                    cx - dx * r * 0.06f, cy - dy * r * 0.06f,
                    cx + dx * len, cy + dy * len,
                    paint(color, halfW)
                )
            }
            AnalogHand.BOLD -> {
                // Thick rectangular hand.
                canvas.drawPath(
                    handPath(cx, cy, angleDeg, len, r * 0.05f, halfW * 1.9f), paint(color)
                )
            }
            else -> {
                canvas.drawPath(
                    handPath(cx, cy, angleDeg, len, r * 0.08f, halfW), paint(color)
                )
            }
        }
    }
    // Hour hand.
    drawHand(hourAngle, r * 0.5f, handWidth, handColor)
    // Minute hand.
    drawHand(minuteAngle, r * 0.78f, handWidth * 0.8f, handColor)
    // Second hand — thin, accent coloured, with a counterweight circle on
    // Classic / Luxury; absent on Minimal Dot.
    if (style != AnalogStyle.MINIMAL_DOT) {
        val secPaint = paint(accent, r * 0.012f)
        val a = Math.toRadians((secondAngle - 90).toDouble())
        val dx = cos(a).toFloat(); val dy = sin(a).toFloat()
        canvas.drawLine(
            cx - dx * r * 0.12f, cy - dy * r * 0.12f,
            cx + dx * r * 0.84f, cy + dy * r * 0.84f, secPaint
        )
        if (style == AnalogStyle.CLASSIC || style == AnalogStyle.MODERN_LUXURY) {
            canvas.drawCircle(cx - dx * r * 0.16f, cy - dy * r * 0.16f, r * 0.035f, paint(accent))
        }
    }
    // Center pin.
    canvas.drawCircle(cx, cy, r * 0.045f, paint(accent))
    canvas.drawCircle(cx, cy, r * 0.02f, paint(0xFF07070A.toInt()))

    @Suppress("UNUSED_VARIABLE")
    val unusedPi = PI
}
