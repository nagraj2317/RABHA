package com.rabha.privacymonitor.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabha.privacymonitor.core.Constants
import com.rabha.privacymonitor.local.PrefsStore
import com.rabha.privacymonitor.network.MonitorForegroundService
import com.rabha.privacymonitor.network.RabhaVpnService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val loading: Boolean = true,
    val data: DashboardData? = null,
    val message: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val prefsStore: PrefsStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            _state.value = DashboardUiState(loading = false, data = repository.load())
        }
    }

    /** Monitoring only ever starts after an explicit consent toggle. */
    fun enableMonitoring() {
        viewModelScope.launch {
            prefsStore.setConsentMonitoring(true)
            prefsStore.setMonitoringPaused(false)
            MonitorForegroundService.start(context)
            _state.value = _state.value.copy(message = "Monitoring started. A visible notification is now running.")
            refresh()
        }
    }

    fun pauseMonitoring() {
        MonitorForegroundService.pause(context)
        viewModelScope.launch {
            prefsStore.setMonitoringPaused(true)
            refresh()
        }
    }

    fun stopMonitoring() {
        MonitorForegroundService.stop(context)
        viewModelScope.launch {
            prefsStore.setConsentMonitoring(false)
            prefsStore.setMonitoringPaused(false)
            _state.value = _state.value.copy(message = "Monitoring stopped and consent withdrawn.")
            refresh()
        }
    }

    fun startPacketCounter() {
        val intent = android.content.Intent(context, RabhaVpnService::class.java)
        try {
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
            _state.value = _state.value.copy(
                message = "Packet counter started. It counts sizes only; internet traffic will not pass while it runs."
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(message = "Could not start the counter: ${e.message}")
        }
    }

    fun stopPacketCounter() {
        val intent = android.content.Intent(context, RabhaVpnService::class.java)
            .setAction(RabhaVpnService.ACTION_STOP)
        context.startService(intent)
        _state.value = _state.value.copy(message = "Packet counter stopped.")
    }

    fun dismissMessage() {
        _state.value = _state.value.copy(message = null)
    }

    companion object {
        const val DEFAULT_INTERVAL = Constants.AD_DEFAULT_INTERVAL_MINUTES
    }
}
