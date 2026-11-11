package com.imdadburki.noctra.service.livewallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.imdadburki.noctra.core.clock.ClockTime
import com.imdadburki.noctra.core.clock.drawAnalogClock
import com.imdadburki.noctra.core.clock.drawDigitalClock
import com.imdadburki.noctra.core.clock.formatDate
import com.imdadburki.noctra.core.clock.formatTime
import com.imdadburki.noctra.core.common.clockFaceByName
import com.imdadburki.noctra.core.common.ClockFace
import com.imdadburki.noctra.core.common.ClockKind
import com.imdadburki.noctra.core.datastore.SettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * NOCTRA live wallpaper (spec §11) — v2.
 *
 * The v1 bug Imdad reported ("wallpaper pe lagate hi clock ruk jata hai")
 * had two causes:
 *  1. He was applying the STATIC wallpaper (a picture) — that can never
 *     tick. v2 labels static vs live everywhere and adds a one-tap
 *     "Set Live Wallpaper" button (ACTION_CHANGE_LIVE_WALLPAPER).
 *  2. The engine hardcoded one GRADIENT face and its loop could die
 *     silently. v2 reads the user's SELECTED catalogue clock from
 *     SettingsStore, restarts the loop on surface changes too, and wraps
 *     every frame in try/catch so one bad frame never kills the loop.
 *
 * Battery-conscious: draws once per second ONLY while visible, stops
 * entirely when hidden. No background service beyond the engine itself.
 */
class NoctraWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = NoctraEngine()

    /** Inner class: WallpaperService.Engine is an inner class, so the engine
     *  must be inner too (its constructor needs the service receiver). */
    private inner class NoctraEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var face: ClockFace = clockFaceByName("Solid Standard Gold")

        /** Re-read the user's selected clock (cheap; DataStore, first value). */
        private fun refreshFace() {
            face = runCatching {
                runBlocking {
                    val store = SettingsStore(applicationContext)
                    val name = store.defaultClockName.first()
                    val is24 = store.is24Hour.first()
                    val secs = store.showSeconds.first()
                    val date = store.showDate.first()
                    clockFaceByName(name).copy(
                        is24Hour = is24, showSeconds = secs, showDate = date
                    )
                }
            }.getOrDefault(face)
        }

        private val drawRunnable = object : Runnable {
            override fun run() {
                try {
                    drawFrame()
                } catch (_: Exception) {
                    // One bad frame must never kill the loop.
                }
                if (visible) handler.postDelayed(this, 1000L)
            }
        }

        private fun startLoop() {
            refreshFace()
            handler.removeCallbacks(drawRunnable)
            handler.post(drawRunnable)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            handler.removeCallbacks(drawRunnable)
            if (visible) startLoop()
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder, format: Int, width: Int, height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            // Surface recreation (rotation, wallpaper re-apply) restarts the loop.
            if (visible) startLoop() else drawFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunnable)
            visible = false
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas == null) return
                val time = ClockTime.now()
                canvas.drawColor(Color.BLACK)
                val w = canvas.width.toFloat()
                val h = canvas.height.toFloat()
                when (face.kind) {
                    ClockKind.DIGITAL -> {
                        val rect = RectF(w * 0.05f, h * 0.42f, w * 0.95f, h * 0.58f)
                        val probe = time.formatTime(face.copy(showAmPm = false))
                        val sizePx = (rect.width() / (probe.length * 0.62f))
                            .coerceAtMost(rect.height() * 0.85f)
                        drawDigitalClock(canvas, face, time, rect, sizePx)
                    }
                    else -> {
                        val r = minOf(w, h) * 0.3f
                        drawAnalogClock(
                            canvas, face, time,
                            RectF(w / 2f - r, h / 2f - r, w / 2f + r, h / 2f + r)
                        )
                    }
                }
                // Date line (digital faces draw their own date inside the
                // engine for STANDARD/WIDE; this one line covers analog).
                if (face.kind != ClockKind.DIGITAL) {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = 0xFFD4AF37.toInt()
                        textSize = h * 0.02f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(time.formatDate(), w / 2f, h * 0.62f, paint)
                }
            } finally {
                canvas?.let { holder.unlockCanvasAndPost(it) }
            }
        }
    }
}
