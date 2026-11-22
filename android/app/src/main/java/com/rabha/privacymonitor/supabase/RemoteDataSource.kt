package com.rabha.privacymonitor.supabase

import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.UnavailableReason
import com.rabha.privacymonitor.core.runCatchingResult
import com.rabha.privacymonitor.supabase.dto.AdCampaignDto
import com.rabha.privacymonitor.supabase.dto.AdCreativeDto
import com.rabha.privacymonitor.supabase.dto.AnnouncementDto
import com.rabha.privacymonitor.supabase.dto.AppConfigDto
import com.rabha.privacymonitor.supabase.dto.FeatureFlagDto
import com.rabha.privacymonitor.supabase.dto.WallpaperDto
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin PostgREST access layer. Every table is read under RLS with the user's JWT,
 * so a missing session or a blocked policy surfaces as an explicit failure, never as empty data.
 */
@Singleton
class RemoteDataSource @Inject constructor(
    private val provider: SupabaseClientProvider
) {

    private fun notConfigured(): RabhaResult<Nothing> = RabhaResult.Unavailable(
        UnavailableReason.FEATURE_DISABLED,
        "Supabase is not configured for this build (SUPABASE_URL / SUPABASE_ANON_KEY missing)."
    )

    suspend fun wallpapers(): RabhaResult<List<WallpaperDto>> {
        val client = provider.client ?: return notConfigured()
        return runCatchingResult {
            client.from("wallpapers").select().decodeList<WallpaperDto>()
        }
    }

    suspend fun announcements(): RabhaResult<List<AnnouncementDto>> {
        val client = provider.client ?: return notConfigured()
        return runCatchingResult {
            client.from("announcements").select().decodeList<AnnouncementDto>()
        }
    }

    suspend fun campaigns(): RabhaResult<List<AdCampaignDto>> {
        val client = provider.client ?: return notConfigured()
        return runCatchingResult {
            client.from("ad_campaigns").select().decodeList<AdCampaignDto>()
        }
    }

    suspend fun creatives(): RabhaResult<List<AdCreativeDto>> {
        val client = provider.client ?: return notConfigured()
        return runCatchingResult {
            client.from("ad_creatives").select().decodeList<AdCreativeDto>()
        }
    }

    suspend fun appConfig(): RabhaResult<List<AppConfigDto>> {
        val client = provider.client ?: return notConfigured()
        return runCatchingResult {
            client.from("app_config").select().decodeList<AppConfigDto>()
        }
    }

    suspend fun featureFlags(): RabhaResult<List<FeatureFlagDto>> {
        val client = provider.client ?: return notConfigured()
        return runCatchingResult {
            client.from("feature_flags").select().decodeList<FeatureFlagDto>()
        }
    }
}
