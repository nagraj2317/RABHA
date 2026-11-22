package com.rabha.privacymonitor.ads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdsViewModel @Inject constructor(
    private val repository: AdsRepository
) : ViewModel() {

    private val cache = mutableMapOf<String, AdUnit?>()
    private val _units = MutableStateFlow<Map<String, AdUnit?>>(emptyMap())
    val units: StateFlow<Map<String, AdUnit?>> = _units.asStateFlow()

    fun load(placement: String) {
        if (cache.containsKey(placement)) return
        viewModelScope.launch {
            val unit = try {
                repository.adFor(placement)
            } catch (e: Exception) {
                null
            }
            cache[placement] = unit
            _units.value = _units.value + (placement to unit)
            if (unit != null) repository.recordImpression(unit)
        }
    }

    fun onClick(placement: String) {
        val unit = cache[placement] ?: return
        viewModelScope.launch { repository.recordClick(unit) }
    }
}
