package com.rabha.privacymonitor.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabha.privacymonitor.auth.AuthRepository
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.unavailableText
import com.rabha.privacymonitor.local.RabhaPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val prefs: StateFlow<RabhaPrefs> = repository.prefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RabhaPrefs())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val signedInEmail: String? get() = authRepository.session.value.email

    fun setAlerts(value: Boolean) = viewModelScope.launch { repository.setAlerts(value) }
    fun setReports(value: Boolean) = viewModelScope.launch { repository.setReports(value) }
    fun setAnnouncements(value: Boolean) = viewModelScope.launch { repository.setAnnouncements(value) }
    fun setAds(value: Boolean) = viewModelScope.launch { repository.setAdsConsent(value) }
    fun setCloudUpload(value: Boolean) = viewModelScope.launch { repository.setCloudUpload(value) }
    fun setMonitoring(value: Boolean) = viewModelScope.launch { repository.setMonitoringConsent(value) }

    fun setRetention(days: Int) = viewModelScope.launch {
        repository.setRetention(days)
        _message.value = "History older than $days days will be removed at the next refresh."
    }

    fun setDataLimit(megabytes: Long) = viewModelScope.launch {
        repository.setDataLimit(megabytes * 1024 * 1024)
        _message.value = "Daily alert limit set to $megabytes MB."
    }

    fun deleteLocalData() = viewModelScope.launch {
        _message.value = when (val result = repository.deleteLocalData()) {
            is RabhaResult.Available -> "Local data deleted."
            else -> result.unavailableText()
        }
    }

    fun deleteAccount() = viewModelScope.launch {
        _message.value = when (val result = repository.deleteAccount()) {
            is RabhaResult.Available -> "Account and local data deleted."
            else -> result.unavailableText()
        }
    }

    fun signOut() = viewModelScope.launch {
        repository.signOut()
        _message.value = "Signed out."
    }

    fun clearMessage() {
        _message.value = null
    }
}
