package ai.rabha.monitor.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ten animated scenes, drawn on a plain Canvas so the same code runs inside a
 * Compose preview and inside the live wallpaper service.
 */
object WallpaperArt {

    val names = listOf(
        "Particles",
        "Waves",
        "Gradient Pulse",
        "Neon Grid",
        "Starfield",
        "Rain",
        "Rings",
        "Bubbles",
        "Aurora",
        "Clock"
    )

    fun draw(canvas: Canvas, w: Float, h: Float, mode: Int, t: Float, accent: Int) {
        canvas.drawColor(Color.BLACK)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (mode % 10) {
            0 -> particles(canvas, w, h, t, accent, paint)
            1 -> waves(canvas, w, h, t, accent, paint)
            2 -> pulse(canvas, w, h, t, accent, paint)
            3 -> grid(canvas, w, h, t, accent, paint)
            4 -> stars(canvas, w, h, t, accent, paint)
            5 -> rain(canvas, w, h, t, accent, paint)
            6 -> rings(canvas, w, h, t, accent, paint)
            7 -> bubbles(canvas, w, h, t, accent, paint)
            8 -> aurora(canvas, w, h, t, accent, paint)
            else -> clock(canvas, w, h, t, accent, paint)
        }
    }

    private fun particles(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        for (i in 0 until 60) {
            val seed = i * 37.0
            val x = ((seed * 13 + t * 20 * (1 + i % 3)).toFloat() % w + w) % w
            val y = ((seed * 29 + sin(t + i).toDouble() * 40).toFloat() % h + h) % h
            p.color = if (i % 5 == 0) accent else Color.argb(120, 255, 255, 255)
            c.drawCircle(x, y, 2f + (i % 4), p)
        }
    }

    private fun waves(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 3f
        for (k in 0 until 5) {
            p.color = if (k % 2 == 0) accent else Color.argb(90, 255, 255, 255)
            val path = android.graphics.Path()
            var x = 0f
            path.moveTo(0f, h / 2)
            while (x <= w) {
                val y = h / 2 + sin((x / w * 4 * PI) + t + k).toFloat() * (h / 8)
                path.lineTo(x, y)
                x += 12f
            }
            c.drawPath(path, p)
        }
        p.style = Paint.Style.FILL
    }

    private fun pulse(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        val r = (w / 2) * (0.5f + 0.4f * sin(t).toFloat())
        p.color = accent
        p.alpha = 60
        c.drawCircle(w / 2, h / 2, r, p)
        p.alpha = 120
        c.drawCircle(w / 2, h / 2, r * 0.6f, p)
        p.alpha = 255
    }

    private fun grid(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 2f
        val step = w / 10
        val off = (t * 30) % step
        for (i in 0..12) {
            val x = i * step + off
            p.color = if (i % 3 == 0) accent else Color.argb(60, 255, 255, 255)
            c.drawLine(x, 0f, x, h, p)
            c.drawLine(0f, x, w, x, p)
        }
        p.style = Paint.Style.FILL
    }

    private fun stars(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        for (i in 0 until 90) {
            val x = ((i * 53.7) % w).toFloat()
            val y = ((i * 91.3 + t * 12).toFloat() % h)
            val r = if (i % 7 == 0) 2.5f else 1.2f
            p.color = if (i % 7 == 0) accent else Color.argb(200, 255, 255, 255)
            c.drawCircle(x, y, r, p)
        }
    }

    private fun rain(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        p.strokeWidth = 3f
        p.color = accent
        for (i in 0 until 70) {
            val x = ((i * 41.3) % w).toFloat()
            val y = ((i * 67.1 + t * 220).toFloat() % h)
            c.drawLine(x, y, x - 6f, y + 26f, p)
        }
    }

    private fun rings(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 4f
        for (k in 0 until 6) {
            val phase = (t + k * 0.5f) % 3f
            val r = (phase / 3f) * (w / 1.6f)
            p.color = if (k % 2 == 0) accent else Color.argb(80, 255, 255, 255)
            c.drawCircle(w / 2, h / 2, r, p)
        }
        p.style = Paint.Style.FILL
    }

    private fun bubbles(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        for (i in 0 until 24) {
            val x = ((i * 71.0) % w).toFloat() + sin(t + i).toFloat() * 20f
            val y = h - (((i * 47.0 + t * 60).toFloat()) % h)
            p.color = if (i % 4 == 0) accent else Color.argb(70, 255, 255, 255)
            c.drawCircle(x, y, 8f + (i % 6) * 4f, p)
        }
    }

    private fun aurora(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = 60f
        p.alpha = 70
        for (k in 0 until 3) {
            p.color = if (k == 0) accent else if (k == 1) Color.rgb(47, 140, 255) else Color.rgb(155, 92, 255)
            val path = android.graphics.Path()
            path.moveTo(0f, h / 3)
            var x = 0f
            while (x <= w) {
                val y = h / 3 + k * 90f + sin((x / w * 3 * PI) + t * 1.4f + k).toFloat() * 70f
                path.lineTo(x, y)
                x += 16f
            }
            c.drawPath(path, p)
        }
        p.alpha = 255
        p.style = Paint.Style.FILL
    }

    private fun clock(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        val cx = w / 2
        val cy = h / 2
        val r = w / 3
        p.style = Paint.Style.STROKE
        p.strokeWidth = 6f
        p.color = Color.argb(90, 255, 255, 255)
        c.drawCircle(cx, cy, r, p)
        p.color = accent
        p.strokeWidth = 8f
        val a = (t * 0.6f) % (2 * PI.toFloat())
        c.drawLine(cx, cy, cx + cos(a) * r * 0.8f, cy + sin(a) * r * 0.8f, p)
        val b = (t * 0.1f) % (2 * PI.toFloat())
        c.drawLine(cx, cy, cx + cos(b) * r * 0.55f, cy + sin(b) * r * 0.55f, p)
        p.style = Paint.Style.FILL
    }
}
