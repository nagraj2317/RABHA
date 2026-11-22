package com.rabha.privacymonitor.wallpaper

import android.content.Context
import android.net.Uri
import com.rabha.privacymonitor.core.Constants
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.runCatchingResult
import com.rabha.privacymonitor.supabase.RemoteDataSource
import com.rabha.privacymonitor.supabase.dto.WallpaperDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

enum class WallpaperKind(val folder: String, val label: String) {
    HTML("html", "HTML"),
    VIDEO("video", "Video"),
    IMAGE("image", "Image")
}

data class LocalWallpaper(
    val file: File,
    val kind: WallpaperKind,
    val sizeBytes: Long
)

@Singleton
class WallpaperRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remote: RemoteDataSource
) {

    private val root: File
        get() = File(context.filesDir, Constants.WALLPAPER_DIR)

    fun prefs() = context.getSharedPreferences(Constants.WALLPAPER_SELECTION, Context.MODE_PRIVATE)

    fun folderFor(kind: WallpaperKind): File {
        val dir = File(root, kind.folder)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun listLocal(): List<LocalWallpaper> =
        WallpaperKind.entries.flatMap { kind ->
            folderFor(kind).listFiles()?.filter { it.isFile }?.map {
                LocalWallpaper(it, kind, it.length())
            }.orEmpty()
        }

    /** Copies a user-picked file into app-private storage. Nothing is uploaded. */
    suspend fun import(uri: Uri, kind: WallpaperKind): RabhaResult<LocalWallpaper> = withContext(Dispatchers.IO) {
        runCatchingResult {
            val name = (uri.lastPathSegment ?: "wallpaper_${System.currentTimeMillis()}").substringAfterLast('/')
            val target = File(folderFor(kind), name)
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: throw IllegalStateException("The picker returned no readable stream")
            LocalWallpaper(target, kind, target.length())
        }
    }

    /** HTML packages are folders: pick index.html and its siblings are read from the same folder. */
    suspend fun importHtmlPackage(uri: Uri): RabhaResult<LocalWallpaper> = import(uri, WallpaperKind.HTML)

    fun selectHtml(entryName: String) {
        prefs().edit().putString(Constants.WALLPAPER_HTML_ENTRY, entryName).apply()
    }

    fun selectVideo(path: String) {
        prefs().edit().putString(Constants.WALLPAPER_VIDEO_PATH, path).apply()
    }

    fun selectAnimated(style: String) {
        prefs().edit().putString(Constants.WALLPAPER_ANIMATED_STYLE, style).apply()
    }

    fun htmlEntry(): String? = prefs().getString(Constants.WALLPAPER_HTML_ENTRY, null)
    fun videoPath(): String? = prefs().getString(Constants.WALLPAPER_VIDEO_PATH, null)
    fun animatedStyle(): String = prefs().getString(Constants.WALLPAPER_ANIMATED_STYLE, "particles") ?: "particles"

    fun delete(item: LocalWallpaper): Boolean = item.file.delete()

    suspend fun remoteGallery(): RabhaResult<List<WallpaperDto>> = remote.wallpapers()
}
