package com.rabha.privacymonitor.wallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.runCatchingResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperInstaller @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Live wallpapers cannot be switched silently. Android requires the user to confirm
     * in the system picker, so this opens that picker with the right engine preselected.
     */
    fun requestLiveWallpaper(serviceClass: Class<*>): RabhaResult<Unit> = runCatching {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, serviceClass)
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }.fold(
        onSuccess = { RabhaResult.Available(Unit) },
        onFailure = { RabhaResult.Failed("Could not open the wallpaper picker: ${it.message}") }
    )

    /** Static images can be set directly with SET_WALLPAPER. */
    suspend fun applyStaticImage(file: File): RabhaResult<Unit> = runCatchingResult {
        val bitmap: Bitmap = BitmapFactory.decodeFile(file.absolutePath)
            ?: throw IllegalStateException("The file is not a readable image")
        val manager = WallpaperManager.getInstance(context)
        manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
    }

    fun openLiveWallpaperPreview() {
        val intent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(context, intent, null)
    }
}
