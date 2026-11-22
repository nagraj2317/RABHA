package com.rabha.privacymonitor.security

import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.runCatchingResult
import com.rabha.privacymonitor.local.PrefsStore
import com.rabha.privacymonitor.local.RabhaPrefs
import com.rabha.privacymonitor.supabase.SupabaseClientProvider
import com.rabha.privacymonitor.supabase.dto.UserConsentDto
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

data class ConsentSnapshot(
    val monitoring: Boolean,
    val cloudUpload: Boolean,
    val ads: Boolean,
    val retentionDays: Int
)

/**
 * Single gate for every consent flag. Nothing is uploaded unless cloudUpload is on,
 * and the server row is only written when the user is signed in.
 */
@Singleton
class ConsentManager @Inject constructor(
    private val prefsStore: PrefsStore,
    private val provider: SupabaseClientProvider
) {

    suspend fun snapshot(): ConsentSnapshot {
        val prefs: RabhaPrefs = prefsStore.flow.first()
        return ConsentSnapshot(
            monitoring = prefs.consentMonitoring,
            cloudUpload = prefs.consentCloudUpload,
            ads = prefs.consentAds,
            retentionDays = prefs.retentionDays
        )
    }

    suspend fun setMonitoring(value: Boolean) {
        prefsStore.setConsentMonitoring(value)
        if (!value) prefsStore.setMonitoringPaused(false)
        record("monitoring", value)
    }

    suspend fun setCloudUpload(value: Boolean) {
        prefsStore.setConsentCloudUpload(value)
        record("cloud_upload", value)
    }

    suspend fun setAds(value: Boolean) {
        prefsStore.setConsentAds(value)
        record("ads", value)
    }

    private suspend fun record(scope: String, granted: Boolean) {
        val client = provider.client ?: return
        val user = runCatching { client.auth.currentUserOrNull() }.getOrNull() ?: return
        val prefs = prefsStore.flow.first()
        if (!prefs.consentCloudUpload) return
        runCatchingResult {
            client.from("user_consents").insert(
                UserConsentDto(userId = user.id, scope = scope, granted = granted)
            )
        }
    }

    suspend fun wipeLocalData() {
        prefsStore.setConsentMonitoring(false)
        prefsStore.setConsentCloudUpload(false)
    }

    suspend fun deleteAccount(): RabhaResult<Unit> {
        val client = provider.client
            ?: return RabhaResult.Failed("Supabase is not configured for this build.")
        // SECURITY DEFINER function on the server: it only ever acts on auth.uid().
        return when (
            val result = runCatchingResult {
                client.postgrest.rpc(function = "delete_own_account")
            }
        ) {
            is RabhaResult.Available -> RabhaResult.Available(Unit)
            is RabhaResult.Unavailable -> result
            is RabhaResult.Failed -> result
        }
    }
}
