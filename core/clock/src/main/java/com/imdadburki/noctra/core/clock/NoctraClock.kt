package com.imdadburki.noctra.core.clock

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imdadburki.noctra.core.common.ClockFace
import com.imdadburki.noctra.core.common.ClockKind
import kotlinx.coroutines.delay

/**
 * Compose wrappers around the shared Canvas engine.
 * [NoctraClock] ticks once a second; [StaticClock] renders a fixed time
 * (previews, exports, widgets).
 */

@Composable
fun rememberTickingTime(): ClockTime {
    var time by remember { mutableStateOf(ClockTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            time = ClockTime.now()
            delay(1000L)
        }
    }
    return time
}

@Composable
fun DigitalClockView(face: ClockFace, time: ClockTime, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val rect = RectF(0f, 0f, size.width, size.height)
        // Base text size: fit the longest time string in the width.
        val probe = time.formatTime(face.copy(showAmPm = false))
        val sizePx = (size.width / (probe.length * 0.62f)).coerceAtMost(size.height * 0.85f)
        drawIntoCanvas { canvas ->
            drawDigitalClock(canvas.nativeCanvas, face, time, rect, sizePx)
        }
    }
}

@Composable
fun AnalogClockView(face: ClockFace, time: ClockTime, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val rect = RectF(0f, 0f, size.width, size.height)
        drawIntoCanvas { canvas ->
            drawAnalogClock(canvas.nativeCanvas, face, time, rect)
        }
    }
}

/** Live clock: digital, analog or hybrid, ticking every second. */
@Composable
fun NoctraClock(face: ClockFace, modifier: Modifier = Modifier, size: Dp = 280.dp) {
    val time = rememberTickingTime()
    StaticClock(face, time, modifier, size)
}

/** Fixed-time clock (previews, editor, widgets, exports). */
@Composable
fun StaticClock(face: ClockFace, time: ClockTime, modifier: Modifier = Modifier, size: Dp = 280.dp) {
    // NOTE: no extra date Text here — the Canvas engine already draws the
    // date line for digital STANDARD/WIDE faces (v1 drew it twice).
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        when (face.kind) {
            ClockKind.DIGITAL -> DigitalClockView(face, time, Modifier.size(size))
            ClockKind.ANALOG -> AnalogClockView(face, time, Modifier.size(size))
            ClockKind.HYBRID -> {
                AnalogClockView(face, time, Modifier.size(size * 0.62f))
                DigitalClockView(face, time, Modifier.size(size * 0.9f, size * 0.28f))
            }
        }
    }
}

/** ARGB colour of a face field, for Compose call sites. */
fun ClockFace.composeColor(): Color = Color(colorHex.toInt())
fun ClockFace.composeAccent(): Color = Color(accentHex.toInt())
fun ClockFace.composeBackground(): Color = Color(backgroundHex.toInt())
fun Long.toComposeColor(): Color = Color(this.toInt())
fun Color.toHexLong(): Long = toArgb().toLong() and 0xFFFFFFFFL
