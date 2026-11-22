package com.rabha.privacymonitor.settings

import com.rabha.privacymonitor.auth.AuthRepository
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.local.PrefsStore
import com.rabha.privacymonitor.local.RabhaPrefs
import com.rabha.privacymonitor.security.ConsentManager
import com.rabha.privacymonitor.security.SecureAccountDeletion
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val prefsStore: PrefsStore,
    private val consentManager: ConsentManager,
    private val deletion: SecureAccountDeletion,
    private val authRepository: AuthRepository
) {

    val prefs: Flow<RabhaPrefs> = prefsStore.flow

    suspend fun setAlerts(value: Boolean) = prefsStore.setAlerts(value)
    suspend fun setReports(value: Boolean) = prefsStore.setReports(value)
    suspend fun setAnnouncements(value: Boolean) = prefsStore.setAnnouncements(value)
    suspend fun setRetention(days: Int) = prefsStore.setRetentionDays(days)
    suspend fun setDataLimit(bytes: Long) = prefsStore.setDataLimit(bytes)
    suspend fun setAdsConsent(value: Boolean) = consentManager.setAds(value)
    suspend fun setMonitoringConsent(value: Boolean) = consentManager.setMonitoring(value)
    suspend fun setCloudUpload(value: Boolean) = consentManager.setCloudUpload(value)

    suspend fun deleteLocalData(): RabhaResult<Unit> = deletion.deleteLocalEverything()
    suspend fun deleteAccount(): RabhaResult<Unit> = deletion.deleteAccountEverywhere()
    suspend fun signOut(): RabhaResult<Unit> = authRepository.signOut()
}
