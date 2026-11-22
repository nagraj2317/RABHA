package com.rabha.privacymonitor.wallpaper

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.unavailableText
import com.rabha.privacymonitor.supabase.dto.WallpaperDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WallpaperUiState(
    val local: List<LocalWallpaper> = emptyList(),
    val remote: RabhaResult<List<WallpaperDto>>? = null,
    val busy: Boolean = false,
    val message: String? = null,
    val selectedKind: String = "none",
    val htmlEntry: String? = null,
    val videoPath: String? = null,
    val animatedStyle: String = "particles"
)

@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val repository: WallpaperRepository,
    private val installer: WallpaperInstaller
) : ViewModel() {

    private val _state = MutableStateFlow(WallpaperUiState())
    val state: StateFlow<WallpaperUiState> = _state.asStateFlow()

    init {
        reload()
        viewModelScope.launch {
            _state.value = _state.value.copy(remote = repository.remoteGallery())
        }
    }

    fun reload() {
        _state.value = _state.value.copy(
            local = repository.listLocal(),
            htmlEntry = repository.htmlEntry(),
            videoPath = repository.videoPath(),
            animatedStyle = repository.animatedStyle()
        )
    }

    fun importFile(uri: Uri, kind: WallpaperKind) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true)
            when (val result = repository.import(uri, kind)) {
                is RabhaResult.Available -> {
                    when (kind) {
                        WallpaperKind.HTML -> repository.selectHtml(result.value.file.name)
                        WallpaperKind.VIDEO -> repository.selectVideo(result.value.file.absolutePath)
                        WallpaperKind.IMAGE -> Unit
                    }
                    _state.value = _state.value.copy(busy = false, message = "Imported ${result.value.file.name}")
                }

                else -> _state.value = _state.value.copy(busy = false, message = result.unavailableText())
            }
            reload()
        }
    }

    fun selectHtml(entryName: String) {
        repository.selectHtml(entryName)
        reload()
    }

    fun selectVideo(path: String) {
        repository.selectVideo(path)
        reload()
    }

    fun selectAnimated(style: String) {
        repository.selectAnimated(style)
        reload()
    }

    fun applyHtml() {
        val result = installer.requestLiveWallpaper(HtmlWallpaperService::class.java)
        _state.value = _state.value.copy(message = messageFor(result, "HTML wallpaper"))
    }

    fun applyVideo() {
        val result = installer.requestLiveWallpaper(VideoWallpaperService::class.java)
        _state.value = _state.value.copy(message = messageFor(result, "Video wallpaper"))
    }

    fun applyAnimated() {
        val result = installer.requestLiveWallpaper(AnimatedWallpaperService::class.java)
        _state.value = _state.value.copy(message = messageFor(result, "Animated wallpaper"))
    }

    fun applyStatic(item: LocalWallpaper) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true)
            val result = installer.applyStaticImage(item.file)
            _state.value = _state.value.copy(
                busy = false,
                message = when (result) {
                    is RabhaResult.Available -> "Home screen wallpaper set."
                    else -> result.unavailableText()
                }
            )
        }
    }

    fun delete(item: LocalWallpaper) {
        val deleted = repository.delete(item)
        _state.value = _state.value.copy(message = if (deleted) "Deleted ${item.file.name}" else "Could not delete the file")
        reload()
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun messageFor(result: RabhaResult<Unit>, label: String): String = when (result) {
        is RabhaResult.Available ->
            "$label engine selected. Android will ask you to confirm in the system picker — that confirmation cannot be skipped."

        else -> result.unavailableText() ?: "Could not open the picker"
    }
}
