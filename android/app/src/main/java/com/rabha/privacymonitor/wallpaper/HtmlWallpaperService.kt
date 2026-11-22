package com.rabha.privacymonitor.wallpaper

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.service.wallpaper.WallpaperService
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import com.rabha.privacymonitor.core.Constants
import java.io.File

/**
 * HTML live wallpaper.
 *
 * Security posture, enforced here:
 *  - the page is loaded from app-private storage only,
 *  - JavaScript runs, but `addJavascriptInterface` is never called anywhere in this file,
 *  - `allowFileAccessFromFileURLs` is false and `blockNetworkLoads` is true, so an uploaded
 *    page cannot read other files or reach the network.
 */
class HtmlWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = HtmlEngine()

    inner class HtmlEngine : Engine() {

        private var webView: WebView? = null
        private var attached = false

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                attach()
            } else {
                detach()
            }
        }

        private fun attach() {
            if (attached) return
            val prefs = getSharedPreferences(Constants.WALLPAPER_SELECTION, Context.MODE_PRIVATE)
            val entry = prefs.getString(Constants.WALLPAPER_HTML_ENTRY, null)
            if (entry.isNullOrBlank()) {
                drawMessage("No HTML wallpaper selected.\nOpen RABHA → Wallpapers and import one.")
                return
            }
            val root = File(filesDir, Constants.WALLPAPER_DIR + "/" + WallpaperKind.HTML.folder)
            val file = File(root, entry)
            if (!file.exists()) {
                drawMessage("HTML file is missing: $entry")
                return
            }

            val view = WebView(this@HtmlWallpaperService)
            view.setBackgroundColor(Color.BLACK)
            view.settings.apply {
                javaScriptEnabled = true
                allowFileAccess = true
                allowContentAccess = false
                allowFileAccessFromFileURLs = false
                allowUniversalAccessFromFileURLs = false
                blockNetworkLoads = true
                blockNetworkImage = false
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
            view.loadUrl(file.toURI().toString())

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.TOP or Gravity.START }

            val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            try {
                manager.addView(view, params)
                webView = view
                attached = true
            } catch (e: Exception) {
                drawMessage("This device did not allow the HTML layer: ${e.message}")
            }
        }

        private fun detach() {
            val view = webView ?: return
            try {
                (getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(view)
            } catch (e: Exception) {
                // The window may already be gone; nothing to do.
            }
            view.destroy()
            webView = null
            attached = false
        }

        private fun drawMessage(text: String) {
            val holder: SurfaceHolder = surfaceHolder
            if (!holder.surface.isValid) return
            val canvas = try {
                holder.lockCanvas()
            } catch (e: Exception) {
                null
            } ?: return
            try {
                canvas.drawColor(Color.BLACK)
                val paint = android.graphics.Paint().apply {
                    color = Color.WHITE
                    textSize = 36f
                    isAntiAlias = true
                }
                text.split("\n").forEachIndexed { index, line ->
                    canvas.drawText(line, 40f, 200f + index * 50f, paint)
                }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            detach()
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            detach()
            super.onDestroy()
        }
    }
}
