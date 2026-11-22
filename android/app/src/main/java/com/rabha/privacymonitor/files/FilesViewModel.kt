package com.rabha.privacymonitor.files

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.unavailableText
import com.rabha.privacymonitor.local.FileMetadataEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val repository: FileMetadataRepository
) : ViewModel() {

    val files: StateFlow<List<FileMetadataEntity>> = repository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onFilePicked(uri: Uri) {
        viewModelScope.launch {
            when (val result = repository.record(uri)) {
                is RabhaResult.Available -> _message.value = "Recorded metadata for ${result.value.displayName}"
                else -> _message.value = result.unavailableText()
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            repository.clear()
            _message.value = "Local file metadata cleared."
        }
    }
}
