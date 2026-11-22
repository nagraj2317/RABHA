package com.rabha.privacymonitor.wallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.rabha.privacymonitor.core.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Canvas-drawn animated wallpaper: particles, a neon clock and a battery-aware frame rate.
 * No WebView and no video decode, so it stays cheap on battery.
 */
class AnimatedWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = AnimatedEngine()

    inner class AnimatedEngine : Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private val paint = Paint().apply { isAntiAlias = true }
        private var visible = false
        private var particles = emptyList<Particle>()
        private var lastFrame = 0L
        private var fps = 30

        private val frame = object : Runnable {
            override fun run() {
                draw()
                handler.postDelayed(this, frameDelay())
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            val style = getSharedPreferences(Constants.WALLPAPER_SELECTION, Context.MODE_PRIVATE)
                .getString(Constants.WALLPAPER_ANIMATED_STYLE, "particles") ?: "particles"
            fps = if (style == "neon") 24 else 30
        }

        private fun frameDelay(): Long {
            val manager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val level = manager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
            val effective = if (level < 20) 12 else fps
            return (1000L / effective).coerceAtLeast(16L)
        }

        private fun draw() {
            val holder = surfaceHolder
            if (!visible || !holder.surface.isValid) return
            val now = System.currentTimeMillis()
            if (now - lastFrame < 16) return
            lastFrame = now
            val canvas: Canvas = try {
                holder.lockCanvas()
            } catch (e: Exception) {
                return
            }
            try {
                render(canvas, now)
            } finally {
                try {
                    holder.unlockCanvasAndPost(canvas)
                } catch (e: Exception) {
                    // Surface went away mid-frame.
                }
            }
        }

        private fun render(canvas: Canvas, now: Long) {
            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()
            canvas.drawColor(Color.BLACK)

            if (particles.isEmpty() || particles.size > 90) {
                particles = List(70) {
                    Particle(
                        x = Random.nextFloat() * width,
                        y = Random.nextFloat() * height,
                        radius = 1.5f + Random.nextFloat() * 3.5f,
                        speed = 0.3f + Random.nextFloat() * 1.2f,
                        hue = Random.nextInt(0, 3)
                    )
                }
            }

            val t = now / 1000.0
            particles.forEach { p ->
                p.y -= p.speed
                if (p.y < -10) {
                    p.y = height + 10
                    p.x = Random.nextFloat() * width
                }
                val drift = sin((t + p.x / 200).toFloat()) * 0.6f
                val x = p.x + drift * 20
                paint.color = when (p.hue) {
                    0 -> Color.parseColor("#FFFFB020")
                    1 -> Color.parseColor("#FF00D1FF")
                    else -> Color.parseColor("#FF9B5CFF")
                }
                paint.alpha = 140
                canvas.drawCircle(x, p.y, p.radius, paint)
            }

            // Neon ring + clock
            paint.alpha = 255
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.parseColor("#FFFFB020")
            val radius = (width.coerceAtMost(height) * 0.18f) + (sin(t.toFloat()) * 6f)
            canvas.drawCircle(width / 2f, height / 2f, radius, paint)
            paint.style = Paint.Style.FILL

            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            paint.color = Color.WHITE
            paint.textSize = radius * 0.42f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(time, width / 2f, height / 2f + radius * 0.15f, paint)

            paint.textSize = radius * 0.16f
            paint.color = Color.parseColor("#FF00D1FF")
            canvas.drawText("RABHA", width / 2f, height / 2f + radius * 0.6f, paint)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            handler.removeCallbacks(frame)
            if (visible) {
                particles = emptyList()
                handler.post(frame)
            }
        }

        override fun onDestroy() {
            handler.removeCallbacks(frame)
            super.onDestroy()
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            draw()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            draw()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            handler.removeCallbacks(frame)
        }
    }

    private data class Particle(
        var x: Float,
        var y: Float,
        val radius: Float,
        val speed: Float,
        val hue: Int
    )
}
