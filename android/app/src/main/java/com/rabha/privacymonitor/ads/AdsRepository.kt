package com.rabha.privacymonitor.ads

import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.local.AdEventDao
import com.rabha.privacymonitor.local.AdEventEntity
import com.rabha.privacymonitor.local.PrefsStore
import com.rabha.privacymonitor.supabase.RemoteDataSource
import com.rabha.privacymonitor.supabase.dto.AdCampaignDto
import com.rabha.privacymonitor.supabase.dto.AdCreativeDto
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

data class AdUnit(
    val campaign: AdCampaignDto,
    val creative: AdCreativeDto
)

/**
 * Rules enforced here, in code:
 *  - no ad while the app is in the background (checked by the caller's lifecycle),
 *  - never in the notification bar (this class has no notification path),
 *  - always labelled Sponsored (rendered by [AdSlot]),
 *  - never targeted using monitoring data (this class only reads campaign metadata),
 *  - a failed load never blocks the app (every failure returns null).
 */
@Singleton
class AdsRepository @Inject constructor(
    private val remote: RemoteDataSource,
    private val prefsStore: PrefsStore,
    private val adEventDao: AdEventDao
) {

    private var campaigns: List<AdCampaignDto> = emptyList()
    private var creatives: List<AdCreativeDto> = emptyList()
    private var lastLoad: Long = 0L

    private fun parseDate(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(value.take(19))?.time
        } catch (e: Exception) {
            null
        }
    }

    suspend fun load(): Boolean {
        val campaignResult = remote.campaigns()
        val creativeResult = remote.creatives()
        if (campaignResult is RabhaResult.Available) campaigns = campaignResult.value
        if (creativeResult is RabhaResult.Available) creatives = creativeResult.value
        lastLoad = System.currentTimeMillis()
        return campaigns.isNotEmpty() && creatives.isNotEmpty()
    }

    suspend fun adFor(placement: String): AdUnit? {
        val prefs = prefsStore.flow.first()
        if (!prefs.consentAds) return null

        val intervalMillis = prefs.adIntervalMinutes.coerceAtLeast(1) * 60_000L
        if (System.currentTimeMillis() - prefs.lastAdShownAt < intervalMillis) return null

        if (campaigns.isEmpty() || System.currentTimeMillis() - lastLoad > 10 * 60_000L) {
            load()
        }

        val now = System.currentTimeMillis()
        val candidates = campaigns
            .filter { it.enabled && it.placement == placement }
            .filter { campaign ->
                val start = parseDate(campaign.startAt)
                val end = parseDate(campaign.endAt)
                (start == null || now >= start) && (end == null || now <= end)
            }
            .sortedByDescending { it.priority }

        for (campaign in candidates) {
            val creative = creatives.firstOrNull { it.campaignId == campaign.id } ?: continue
            val impressions = adEventDao.impressionsSince(campaign.id, now - 24 * 60 * 60 * 1000L)
            if (impressions >= campaign.frequencyCap.coerceAtLeast(1)) continue
            return AdUnit(campaign, creative)
        }
        return null
    }

    suspend fun recordImpression(unit: AdUnit) {
        adEventDao.insert(
            AdEventEntity(
                campaignId = unit.campaign.id,
                creativeId = unit.creative.id,
                type = "impression",
                occurredAt = System.currentTimeMillis()
            )
        )
        prefsStore.setLastAdShownAt(System.currentTimeMillis())
    }

    suspend fun recordClick(unit: AdUnit) {
        adEventDao.insert(
            AdEventEntity(
                campaignId = unit.campaign.id,
                creativeId = unit.creative.id,
                type = "click",
                occurredAt = System.currentTimeMillis()
            )
        )
    }
}
