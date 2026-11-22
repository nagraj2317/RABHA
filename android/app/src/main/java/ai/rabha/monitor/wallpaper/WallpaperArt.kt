package ai.rabha.monitor.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ten animated scenes drawn on a plain Canvas, so the same code runs inside the
 * Compose preview and inside the live wallpaper service.
 */
object WallpaperArt {

    val names = listOf(
        "Aurora",
        "Neon Grid",
        "Liquid",
        "Starfield",
        "Ocean",
        "Particles",
        "Rings",
        "Rain",
        "Sunset",
        "Clock"
    )

    fun draw(canvas: Canvas, w: Float, h: Float, mode: Int, t: Float, accent: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        when (mode % 10) {
            0 -> aurora(canvas, w, h, t, accent, p)
            1 -> neonGrid(canvas, w, h, t, accent, p)
            2 -> liquid(canvas, w, h, t, accent, p)
            3 -> starfield(canvas, w, h, t, accent, p)
            4 -> ocean(canvas, w, h, t, accent, p)
            5 -> particles(canvas, w, h, t, accent, p)
            6 -> rings(canvas, w, h, t, accent, p)
            7 -> rain(canvas, w, h, t, accent, p)
            8 -> sunset(canvas, w, h, t, accent, p)
            else -> clock(canvas, w, h, t, accent, p)
        }
    }

    private fun vGradient(c: Canvas, w: Float, h: Float, top: Int, bottom: Int, p: Paint) {
        p.shader = LinearGradient(0f, 0f, 0f, h, top, bottom, Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, p)
        p.shader = null
    }

    private fun glow(c: Canvas, x: Float, y: Float, r: Float, color: Int, p: Paint) {
        p.shader = RadialGradient(
            x, y, r,
            color,
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        c.drawCircle(x, y, r, p)
        p.shader = null
    }

    private fun band(c: Canvas, w: Float, h: Float, t: Float, yBase: Float, amp: Float, color: Int, p: Paint) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = amp * 0.9f
        p.color = color
        p.alpha = 90
        val path = Path()
        path.moveTo(0f, yBase)
        var x = 0f
        while (x <= w) {
            val y = yBase + sin((x / w) * 2.4f * PI.toFloat() + t).toFloat() * amp
            path.lineTo(x, y)
            x += 10f
        }
        c.drawPath(path, p)
        p.alpha = 255
        p.style = Paint.Style.FILL
    }

    private fun aurora(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        vGradient(c, w, h, Color.rgb(4, 8, 20), Color.rgb(12, 4, 28), p)
        glow(c, w * 0.3f, h * 0.25f, w * 0.8f, Color.argb(90, 0, 200, 170), p)
        band(c, w, h, t * 0.6f, h * 0.35f, h * 0.07f, Color.rgb(0, 220, 190), p)
        band(c, w, h, t * 0.6f + 1.2f, h * 0.45f, h * 0.06f, accent, p)
        band(c, w, h, t * 0.6f + 2.4f, h * 0.55f, h * 0.05f, Color.rgb(120, 80, 255), p)
    }

    private fun neonGrid(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        vGradient(c, w, h, Color.rgb(6, 2, 18), Color.rgb(20, 0, 40), p)
        glow(c, w / 2, h * 0.62f, w * 0.9f, Color.argb(80, 255, 60, 180), p)

        p.style = Paint.Style.STROKE
        p.strokeWidth = 1.6f
        val horizon = h * 0.62f
        val vpx = w / 2

        p.color = Color.argb(120, 120, 220, 255)
        for (i in -8..8) {
            c.drawLine(vpx, horizon, vpx + i * w * 0.18f, h, p)
        }

        val scroll = (t * 0.6f) % 1f
        for (i in 0..14) {
            val k = (i + scroll) / 14f
            val y = horizon + (h - horizon) * k * k
            p.color = Color.argb((40 + 160 * (1 - k)).toInt().coerceIn(30, 220), 120, 220, 255)
            c.drawLine(0f, y, w, y, p)
        }
        p.style = Paint.Style.FILL
    }

    private fun liquid(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        vGradient(c, w, h, Color.rgb(8, 2, 16), Color.rgb(2, 6, 18), p)
        val a = Color.argb(120, Color.red(accent), Color.green(accent), Color.blue(accent))
        glow(c, w * (0.5f + 0.3f * sin(t * 0.7f).toFloat()), h * 0.3f, w * 0.7f, a, p)
        glow(c, w * (0.5f + 0.3f * cos(t * 0.5f).toFloat()), h * 0.7f, w * 0.8f, Color.argb(110, 47, 140, 255), p)
        glow(c, w * 0.5f, h * (0.5f + 0.25f * sin(t * 0.9f).toFloat()), w * 0.5f, Color.argb(90, 155, 92, 255), p)
    }

    private fun starfield(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        vGradient(c, w, h, Color.rgb(2, 3, 10), Color.rgb(8, 6, 22), p)
        for (i in 0 until 140) {
            val x = ((i * 61.7) % w).toFloat()
            val speed = 6f + (i % 5) * 4f
            val y = ((i * 97.3 + t * speed).toFloat() % h)
            val big = i % 11 == 0
            if (big) glow(c, x, y, 14f, Color.argb(120, Color.red(accent), Color.green(accent), Color.blue(accent)), p)
            p.color = if (big) accent else Color.argb(150 + (i % 90), 255, 255, 255)
            c.drawCircle(x, y, if (big) 2.6f else 1.2f, p)
        }
    }

    private fun ocean(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        vGradient(c, w, h, Color.rgb(2, 10, 24), Color.rgb(1, 24, 40), p)
        glow(c, w * 0.7f, h * 0.22f, w * 0.5f, Color.argb(90, 120, 220, 255), p)
        val layers = listOf(
            Triple(h * 0.55f, h * 0.06f, Color.rgb(0, 90, 140)),
            Triple(h * 0.68f, h * 0.05f, Color.rgb(0, 120, 180)),
            Triple(h * 0.80f, h * 0.04f, accent)
        )
        for ((i, l) in layers.withIndex()) {
            val (base, amp, color) = l
            val path = Path()
            path.moveTo(0f, h)
            path.lineTo(0f, base)
            var x = 0f
            while (x <= w) {
                val y = base + sin((x / w) * 3f * PI.toFloat() + t * 1.2f + i).toFloat() * amp
                path.lineTo(x, y)
                x += 10f
            }
            path.lineTo(w, h)
            path.close()
            p.color = color
            p.alpha = 200
            c.drawPath(path, p)
            p.alpha = 255
        }
    }

    private fun particles(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        vGradient(c, w, h, Color.rgb(3, 3, 8), Color.rgb(10, 6, 18), p)
        for (i in 0 until 46) {
            val x = ((i * 47.3 + sin(t * 0.6f + i).toFloat() * 60).toFloat() % w + w) % w
            val y = ((i * 83.1 - t * (10 + i % 7)) % h + h) % h
            val r = 18f + (i % 5) * 10f
            glow(c, x, y, r, Color.argb(70, Color.red(accent), Color.green(accent), Color.blue(accent)), p)
            p.color = accent
            c.drawCircle(x, y, 2f, p)
        }
    }

    private fun rings(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        vGradient(c, w, h, Color.rgb(6, 4, 14), Color.rgb(2, 8, 16), p)
        glow(c, w / 2, h / 2, w * 0.7f, Color.argb(60, Color.red(accent), Color.green(accent), Color.blue(accent)), p)
        p.style = Paint.Style.STROKE
        for (k in 0 until 7) {
            val phase = ((t * 0.35f + k / 7f) % 1f)
            val r = phase * (w * 0.55f)
            p.strokeWidth = 2f + 6f * (1 - phase)
            p.color = accent
            p.alpha = (200 * (1 - phase)).toInt().coerceIn(10, 220)
            c.drawCircle(w / 2, h / 2, r, p)
        }
        p.alpha = 255
        p.style = Paint.Style.FILL
    }

    private fun rain(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        vGradient(c, w, h, Color.rgb(4, 8, 14), Color.rgb(2, 4, 8), p)
        glow(c, w * 0.5f, h * 0.15f, w * 0.9f, Color.argb(45, 90, 160, 255), p)
        p.strokeWidth = 2.2f
        for (i in 0 until 80) {
            val x = ((i * 43.7) % w).toFloat()
            val y = ((i * 71.3 + t * 320).toFloat() % (h + 120)) - 60
            p.color = Color.argb(70 + (i % 60), 180, 220, 255)
            c.drawLine(x, y, x - 5f, y + 30f, p)
        }
        p.color = accent
        p.alpha = 40
        c.drawCircle(w * 0.3f, h * 0.85f, w * 0.25f, p)
        p.alpha = 255
    }

    private fun sunset(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        p.shader = LinearGradient(
            0f, 0f, 0f, h,
            intArrayOf(Color.rgb(12, 4, 30), Color.rgb(120, 30, 80), Color.rgb(255, 110, 60)),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, w, h, p)
        p.shader = null
        val sunY = h * (0.62f + 0.03f * sin(t * 0.5f).toFloat())
        glow(c, w * 0.5f, sunY, w * 0.45f, Color.argb(160, 255, 210, 120), p)
        p.color = Color.rgb(255, 236, 190)
        c.drawCircle(w * 0.5f, sunY, w * 0.11f, p)
        p.color = Color.argb(60, 0, 0, 0)
        for (i in 0 until 6) {
            val y = h * 0.72f + i * h * 0.045f
            c.drawRect(0f, y, w, y + h * 0.012f, p)
        }
    }

    private fun clock(c: Canvas, w: Float, h: Float, t: Float, accent: Int, p: Paint) {
        vGradient(c, w, h, Color.rgb(2, 2, 4), Color.rgb(8, 8, 12), p)
        val cx = w / 2
        val cy = h / 2
        val r = w * 0.34f

        p.style = Paint.Style.STROKE
        p.strokeWidth = 1.5f
        p.color = Color.argb(45, 255, 255, 255)
        for (i in 0 until 60) {
            val a = i / 60f * 2f * PI.toFloat()
            val inner = if (i % 5 == 0) r * 0.88f else r * 0.94f
            c.drawLine(
                cx + cos(a) * inner, cy + sin(a) * inner,
                cx + cos(a) * r, cy + sin(a) * r, p
            )
        }

        p.color = accent
        p.strokeWidth = 5f
        val hour = (t * 0.05f) % (2 * PI.toFloat())
        c.drawLine(cx, cy, cx + cos(hour) * r * 0.5f, cy + sin(hour) * r * 0.5f, p)

        p.strokeWidth = 3f
        val min = (t * 0.3f) % (2 * PI.toFloat())
        c.drawLine(cx, cy, cx + cos(min) * r * 0.75f, cy + sin(min) * r * 0.75f, p)

        p.style = Paint.Style.FILL
        p.color = accent
        c.drawCircle(cx, cy, 5f, p)
    }
}
