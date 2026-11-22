package com.rabha.privacymonitor.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabha.privacymonitor.core.RabhaResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrivacyUiState(
    val signals: List<SignalState> = emptyList(),
    val recordings: RabhaResult<List<String>>? = null
)

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val repository: PrivacySignalsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PrivacyUiState())
    val state: StateFlow<PrivacyUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(signals = repository.ownSignals())
        viewModelScope.launch {
            _state.value = _state.value.copy(recordings = repository.recordingConfigurations())
        }
    }
}
