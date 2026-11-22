package com.rabha.privacymonitor.wallpaper

import android.content.Context
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.rabha.privacymonitor.core.Constants
import java.io.File

/** Video live wallpaper on Media3 ExoPlayer: muted, looping, and paused when the screen is off. */
class VideoWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = VideoEngine()

    inner class VideoEngine : Engine() {

        private var player: ExoPlayer? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            val prefs = getSharedPreferences(Constants.WALLPAPER_SELECTION, Context.MODE_PRIVATE)
            val path = prefs.getString(Constants.WALLPAPER_VIDEO_PATH, null)
            if (path.isNullOrBlank() || !File(path).exists()) return

            val exo = ExoPlayer.Builder(this@VideoWallpaperService).build()
            exo.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(path))))
            exo.repeatMode = Player.REPEAT_MODE_ALL
            exo.volume = 0f
            exo.setVideoSurface(holder.surface)
            exo.prepare()
            exo.playWhenReady = true
            player = exo
        }

        override fun onVisibilityChanged(visible: Boolean) {
            val exo = player ?: return
            if (visible) exo.play() else exo.pause()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            player?.release()
            player = null
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            player?.release()
            player = null
            super.onDestroy()
        }
    }
}
