package com.rabha.privacymonitor.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.ConnectivityObserver
import com.rabha.privacymonitor.core.NetworkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NetworkUiState(
    val loading: Boolean = true,
    val daily: RabhaResult<NetworkTotals>? = null,
    val weekly: RabhaResult<NetworkTotals>? = null,
    val monthly: RabhaResult<NetworkTotals>? = null,
    val perApp: RabhaResult<List<AppDataUsage>>? = null,
    val series: RabhaResult<List<DayTotal>>? = null,
    val status: NetworkStatus = NetworkStatus(false, "Unknown", false, false)
)

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val repository: NetworkStatsRepository,
    private val connectivity: ConnectivityObserver
) : ViewModel() {

    private val _state = MutableStateFlow(NetworkUiState())
    val state: StateFlow<NetworkUiState> = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            connectivity.observe().collect { status ->
                _state.value = _state.value.copy(status = status)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, status = connectivity.currentStatus())
            val now = System.currentTimeMillis()
            val daily = repository.totals(NetworkStatsRepository.startOfToday(), now)
            val weekly = repository.totals(NetworkStatsRepository.startOfWeek(), now)
            val monthly = repository.totals(NetworkStatsRepository.startOfMonth(), now)
            val perApp = repository.perApp(NetworkStatsRepository.startOfMonth(), now)
            val series = repository.dailySeries(7)
            _state.value = NetworkUiState(
                loading = false,
                daily = daily,
                weekly = weekly,
                monthly = monthly,
                perApp = perApp,
                series = series,
                status = connectivity.currentStatus()
            )
        }
    }
}
