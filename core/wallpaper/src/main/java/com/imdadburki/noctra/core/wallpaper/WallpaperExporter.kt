package com.imdadburki.noctra.core.wallpaper

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.provider.MediaStore
import com.imdadburki.noctra.core.clock.ClockTime
import com.imdadburki.noctra.core.clock.drawAnalogClock
import com.imdadburki.noctra.core.clock.drawDigitalClock
import com.imdadburki.noctra.core.clock.formatDate
import com.imdadburki.noctra.core.clock.formatTime
import com.imdadburki.noctra.core.common.ClockKind
import com.imdadburki.noctra.core.common.ExportFormat
import com.imdadburki.noctra.core.common.WallpaperDesign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/** Where an exported wallpaper goes. */
sealed class ExportResult {
    data class Success(val path: String, val width: Int, val height: Int) : ExportResult()
    data class Failure(val reason: String) : ExportResult()
}

/** Where a wallpaper can be applied. */
enum class ApplyTarget { HOME, LOCK_SCREEN, BOTH }

/**
 * Wallpaper export + application engine.
 *
 * Rendering reuses the SAME Canvas functions as the on-screen clock
 * ([drawDigitalClock]/[drawAnalogClock]) — an export is never a fake
 * preview; it is the real clock drawn at export resolution.
 */
object WallpaperExporter {

    /**
     * Renders [design] at [width]x[height] into a Bitmap.
     * Background: solid colour or a two-colour linear gradient at
     * [WallpaperDesign.gradientAngle]. Clock drawn centred, scaled by
     * [WallpaperDesign.clockScale] and offset by its offsets.
     */
    fun renderBitmap(design: WallpaperDesign, width: Int, height: Int, time: ClockTime): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bg = design.backgroundHex.toInt()
        val bg2 = design.backgroundHex2
        if (bg2 != null) {
            val angleRad = Math.toRadians(design.gradientAngle.toDouble())
            val dx = kotlin.math.cos(angleRad).toFloat()
            val dy = kotlin.math.sin(angleRad).toFloat()
            val paint = Paint().apply {
                shader = LinearGradient(
                    width * (0.5f - dx * 0.5f), height * (0.5f - dy * 0.5f),
                    width * (0.5f + dx * 0.5f), height * (0.5f + dy * 0.5f),
                    bg, bg2.toInt(), Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        } else {
            canvas.drawColor(bg)
        }

        val face = design.clockFace
        val cx = width / 2f + design.clockOffsetX * width
        val cy = height / 2f + design.clockOffsetY * height
        val scale = design.clockScale
        when (face.kind) {
            ClockKind.DIGITAL -> {
                val textW = width * 0.86f * scale
                val textH = height * 0.22f * scale
                val rect = RectF(cx - textW / 2f, cy - textH / 2f, cx + textW / 2f, cy + textH / 2f)
                val probe = time.formatTime(face.copy(showAmPm = false))
                val sizePx = (textW / (probe.length * 0.62f)).coerceAtMost(textH * 0.85f)
                drawDigitalClock(canvas, face, time, rect, sizePx)
            }
            ClockKind.ANALOG -> {
                val r = (minOf(width, height) * 0.36f) * scale
                drawAnalogClock(canvas, face, time, RectF(cx - r, cy - r, cx + r, cy + r))
            }
            ClockKind.HYBRID -> {
                val r = (minOf(width, height) * 0.24f) * scale
                drawAnalogClock(canvas, face, time, RectF(cx - r, cy - r * 1.7f, cx + r, cy - r * 0.7f))
                val textW = width * 0.8f * scale
                val textH = height * 0.12f * scale
                val rect = RectF(cx - textW / 2f, cy + r * 0.1f, cx + textW / 2f, cy + r * 0.1f + textH)
                val probe = time.formatTime(face.copy(showAmPm = false))
                val sizePx = (textW / (probe.length * 0.62f)).coerceAtMost(textH * 0.85f)
                drawDigitalClock(canvas, face, time, rect, sizePx)
            }
        }

        // Date line under the clock — DIGITAL faces draw their own date
        // inside the engine (strip-based, never overlapping), so the
        // exporter only adds one for ANALOG faces (dial has no full date).
        if (design.dateVisible && face.showDate && face.kind != ClockKind.DIGITAL) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = design.clockFace.accentHex.toInt()
                textSize = height * 0.022f
                typeface = android.graphics.Typeface.create(
                    android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD
                )
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(time.formatDate(), cx, cy + height * 0.22f * scale, paint)
        }
        // Custom text (editor widget).
        if (design.customText.isNotBlank()) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = design.clockFace.colorHex.toInt()
                textSize = height * 0.02f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(design.customText, cx, height * 0.94f, paint)
        }
        return bitmap
    }

    /**
     * Exports [design] to the Pictures/NOCTRA folder (MediaStore on API 29+,
     * plain file below) and returns the saved path. Runs on IO.
     * Returns [ExportResult.Failure] with the real reason on any error —
     * never a silent success.
     */
    suspend fun export(
        context: Context,
        design: WallpaperDesign,
        width: Int,
        height: Int,
        format: ExportFormat,
        time: ClockTime = ClockTime.now()
    ): ExportResult = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = renderBitmap(design, width, height, time)
            val fileName = "NOCTRA_${design.id}_${width}x$height.${format.extension}"
            val mime = when (format) {
                ExportFormat.PNG -> "image/png"
                ExportFormat.JPEG -> "image/jpeg"
                ExportFormat.WEBP -> "image/webp"
            }
            val compressFormat = when (format) {
                ExportFormat.PNG -> Bitmap.CompressFormat.PNG
                ExportFormat.JPEG -> Bitmap.CompressFormat.JPEG
                ExportFormat.WEBP -> if (Build.VERSION.SDK_INT >= 30) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP
                }
            }
            if (Build.VERSION.SDK_INT >= 29) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, mime)
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/NOCTRA")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                ) ?: return@withContext ExportResult.Failure("MediaStore insert failed")
                resolver.openOutputStream(uri)?.use { out ->
                    if (!bitmap.compress(compressFormat, 92, out)) {
                        return@withContext ExportResult.Failure("Bitmap compression failed")
                    }
                } ?: return@withContext ExportResult.Failure("Could not open output stream")
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                ExportResult.Success("Pictures/NOCTRA/$fileName", width, height)
            } else {
                val dir = File(
                    android.os.Environment.getExternalStoragePublicDirectory(
                        android.os.Environment.DIRECTORY_PICTURES
                    ), "NOCTRA"
                )
                if (!dir.exists() && !dir.mkdirs()) {
                    return@withContext ExportResult.Failure("Could not create Pictures/NOCTRA")
                }
                val file = File(dir, fileName)
                FileOutputStream(file).use { out ->
                    if (!bitmap.compress(compressFormat, 92, out)) {
                        return@withContext ExportResult.Failure("Bitmap compression failed")
                    }
                }
                ExportResult.Success(file.absolutePath, width, height)
            }
        }.getOrElse { e ->
            ExportResult.Failure(e.message ?: "Unknown export error")
        }
    }

    /** Applies a rendered [bitmap] as home / lock / both wallpaper. */
    suspend fun apply(
        context: Context,
        bitmap: Bitmap,
        target: ApplyTarget
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val wm = WallpaperManager.getInstance(context)
            when (target) {
                ApplyTarget.HOME -> wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                ApplyTarget.LOCK_SCREEN -> {
                    if (Build.VERSION.SDK_INT >= 24) {
                        wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    } else {
                        throw UnsupportedOperationException(
                            "Lock-screen wallpaper needs Android 7.0+"
                        )
                    }
                }
                ApplyTarget.BOTH -> {
                    wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    if (Build.VERSION.SDK_INT >= 24) {
                        wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    }
                }
            }
            Unit
        }
    }
}
