package com.rabha.privacymonitor.usage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabha.privacymonitor.core.RabhaResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UsageUiState(
    val loading: Boolean = true,
    val today: RabhaResult<List<AppUsageInfo>>? = null,
    val week: RabhaResult<List<AppUsageInfo>>? = null,
    val events: RabhaResult<List<UsageEventInfo>>? = null
)

@HiltViewModel
class UsageViewModel @Inject constructor(
    private val repository: UsageStatsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UsageUiState())
    val state: StateFlow<UsageUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val now = System.currentTimeMillis()
            val today = repository.usage(UsageStatsRepository.startOfToday(), now)
            val week = repository.usage(now - 7L * 24 * 60 * 60 * 1000, now)
            val events = repository.recentEvents()
            _state.value = UsageUiState(
                loading = false,
                today = today,
                week = week,
                events = events
            )
        }
    }
}
