package com.rabha.privacymonitor.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    val email: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class DeviceDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val model: String,
    @SerialName("android_version") val androidVersion: String,
    @SerialName("app_version") val appVersion: String,
    @SerialName("last_seen") val lastSeen: String? = null
)

@Serializable
data class UserConsentDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val scope: String,
    val granted: Boolean,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class MonitoringSummaryDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("device_id") val deviceId: String? = null,
    val day: String,
    @SerialName("rx_bytes") val rxBytes: Long,
    @SerialName("tx_bytes") val txBytes: Long,
    @SerialName("screen_millis") val screenMillis: Long,
    @SerialName("top_package") val topPackage: String? = null
)

@Serializable
data class AppUsageSummaryDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val day: String,
    val packageName: String,
    @SerialName("foreground_millis") val foregroundMillis: Long
)

@Serializable
data class WallpaperDto(
    val id: String,
    val title: String,
    val kind: String,
    @SerialName("preview_url") val previewUrl: String? = null,
    @SerialName("asset_path") val assetPath: String? = null,
    @SerialName("is_premium") val isPremium: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class WallpaperCategoryDto(
    val id: String,
    val name: String,
    val slug: String? = null
)

@Serializable
data class AdCampaignDto(
    val id: String,
    val name: String,
    val placement: String,
    val priority: Int = 0,
    val enabled: Boolean = true,
    @SerialName("start_at") val startAt: String? = null,
    @SerialName("end_at") val endAt: String? = null,
    @SerialName("frequency_cap") val frequencyCap: Int = 3,
    @SerialName("interval_minutes") val intervalMinutes: Int = 15
)

@Serializable
data class AdCreativeDto(
    val id: String,
    @SerialName("campaign_id") val campaignId: String,
    val type: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("video_url") val videoUrl: String? = null,
    @SerialName("html_url") val htmlUrl: String? = null,
    val headline: String? = null,
    val body: String? = null,
    @SerialName("click_url") val clickUrl: String? = null
)

@Serializable
data class AdEventDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("campaign_id") val campaignId: String,
    @SerialName("creative_id") val creativeId: String? = null,
    val type: String,
    @SerialName("occurred_at") val occurredAt: String? = null
)

@Serializable
data class AnnouncementDto(
    val id: String,
    val title: String,
    val body: String,
    val active: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AppConfigDto(
    val key: String,
    val value: String,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class FeatureFlagDto(
    val key: String,
    val enabled: Boolean,
    val description: String? = null
)

@Serializable
data class NotificationTemplateDto(
    val id: String,
    val title: String,
    val body: String,
    val category: String
)

@Serializable
data class SupportTicketDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val subject: String,
    val message: String,
    val status: String = "open"
)
