package com.rabha.privacymonitor.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rabha_prefs")

data class RabhaPrefs(
    val consentMonitoring: Boolean = false,
    val consentCloudUpload: Boolean = false,
    val consentAds: Boolean = true,
    val monitoringPaused: Boolean = false,
    val dataLimitBytes: Long = 2L * 1024 * 1024 * 1024,
    val alertsEnabled: Boolean = true,
    val reportsEnabled: Boolean = true,
    val announcementsEnabled: Boolean = true,
    val adIntervalMinutes: Int = 15,
    val wallpaperKind: String = "none",
    val retentionDays: Int = 30,
    val lastAdShownAt: Long = 0L
)

@Singleton
class PrefsStore @Inject constructor(private val context: Context) {

    private object Keys {
        val consentMonitoring = booleanPreferencesKey("consent_monitoring")
        val consentCloudUpload = booleanPreferencesKey("consent_cloud_upload")
        val consentAds = booleanPreferencesKey("consent_ads")
        val monitoringPaused = booleanPreferencesKey("monitoring_paused")
        val dataLimit = longPreferencesKey("data_limit_bytes")
        val alertsEnabled = booleanPreferencesKey("alerts_enabled")
        val reportsEnabled = booleanPreferencesKey("reports_enabled")
        val announcementsEnabled = booleanPreferencesKey("announcements_enabled")
        val adInterval = intPreferencesKey("ad_interval_minutes")
        val wallpaperKind = stringPreferencesKey("wallpaper_kind")
        val retentionDays = intPreferencesKey("retention_days")
        val lastAdShownAt = longPreferencesKey("last_ad_shown_at")
    }

    val flow: Flow<RabhaPrefs> = context.dataStore.data.map { p ->
        RabhaPrefs(
            consentMonitoring = p[Keys.consentMonitoring] ?: false,
            consentCloudUpload = p[Keys.consentCloudUpload] ?: false,
            consentAds = p[Keys.consentAds] ?: true,
            monitoringPaused = p[Keys.monitoringPaused] ?: false,
            dataLimitBytes = p[Keys.dataLimit] ?: (2L * 1024 * 1024 * 1024),
            alertsEnabled = p[Keys.alertsEnabled] ?: true,
            reportsEnabled = p[Keys.reportsEnabled] ?: true,
            announcementsEnabled = p[Keys.announcementsEnabled] ?: true,
            adIntervalMinutes = p[Keys.adInterval] ?: 15,
            wallpaperKind = p[Keys.wallpaperKind] ?: "none",
            retentionDays = p[Keys.retentionDays] ?: 30,
            lastAdShownAt = p[Keys.lastAdShownAt] ?: 0L
        )
    }

    suspend fun setConsentMonitoring(value: Boolean) = edit { it[Keys.consentMonitoring] = value }
    suspend fun setConsentCloudUpload(value: Boolean) = edit { it[Keys.consentCloudUpload] = value }
    suspend fun setConsentAds(value: Boolean) = edit { it[Keys.consentAds] = value }
    suspend fun setMonitoringPaused(value: Boolean) = edit { it[Keys.monitoringPaused] = value }
    suspend fun setDataLimit(value: Long) = edit { it[Keys.dataLimit] = value }
    suspend fun setAlerts(value: Boolean) = edit { it[Keys.alertsEnabled] = value }
    suspend fun setReports(value: Boolean) = edit { it[Keys.reportsEnabled] = value }
    suspend fun setAnnouncements(value: Boolean) = edit { it[Keys.announcementsEnabled] = value }
    suspend fun setAdInterval(value: Int) = edit { it[Keys.adInterval] = value }
    suspend fun setWallpaperKind(value: String) = edit { it[Keys.wallpaperKind] = value }
    suspend fun setRetentionDays(value: Int) = edit { it[Keys.retentionDays] = value }
    suspend fun setLastAdShownAt(value: Long) = edit { it[Keys.lastAdShownAt] = value }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit { prefs -> block(prefs) }
    }
}
