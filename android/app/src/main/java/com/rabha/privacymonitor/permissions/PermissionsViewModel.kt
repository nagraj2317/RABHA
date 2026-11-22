package com.rabha.privacymonitor.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabha.privacymonitor.core.RabhaResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PermissionsUiState(
    val items: List<PermissionItem> = emptyList(),
    val declared: RabhaResult<List<DeclaredPermissionInfo>>? = null
)

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val repository: PermissionsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PermissionsUiState())
    val state: StateFlow<PermissionsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(items = repository.ownPermissions())
        viewModelScope.launch {
            _state.value = _state.value.copy(declared = repository.declaredPermissions())
        }
    }

    fun openAppSettings() = repository.openAppSettings()
    fun openUsageSettings() = repository.openUsageAccessSettings()
    fun openNotificationSettings() = repository.openNotificationSettings()
}
