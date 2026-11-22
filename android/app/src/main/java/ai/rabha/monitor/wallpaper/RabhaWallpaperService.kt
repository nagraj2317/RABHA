package ai.rabha.monitor.wallpaper

import android.content.Context
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class RabhaWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = RabhaEngine()

    inner class RabhaEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var startTime = 0L

        private val runnable = object : Runnable {
            override fun run() {
                draw()
                handler.postDelayed(this, 40L)
            }
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            visible = isVisible
            if (isVisible) {
                startTime = System.currentTimeMillis()
                handler.post(runnable)
            } else {
                handler.removeCallbacks(runnable)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(runnable)
        }

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    val prefs = getSharedPreferences("rabha", Context.MODE_PRIVATE)
                    val mode = prefs.getInt("wallpaper_mode", 0)
                    val accent = prefs.getInt("wallpaper_accent", 0xFFFFB020.toInt())
                    val t = (System.currentTimeMillis() - startTime) / 1000f
                    WallpaperArt.draw(canvas, canvas.width.toFloat(), canvas.height.toFloat(), mode, t, accent)
                }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}
