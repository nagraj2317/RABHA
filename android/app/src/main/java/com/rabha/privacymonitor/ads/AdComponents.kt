package com.rabha.privacymonitor.ads

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.rabha.privacymonitor.core.GlassCard
import com.rabha.privacymonitor.core.RabhaColors
import com.rabha.privacymonitor.core.SponsoredBadge

/**
 * One ad slot. It renders nothing at all when no campaign is eligible,
 * so a failed or empty load can never block or shrink the surrounding screen.
 */
@Composable
internal fun AdSlot(
    placement: String,
    viewModel: AdsViewModel = hiltViewModel()
) {
    val units by viewModel.units.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(placement) {
        viewModel.load(placement)
    }

    val unit = units[placement] ?: return
    val creative = unit.creative

    GlassCard(accent = RabhaColors.Amber) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SponsoredBadge()
        }
        Spacer(Modifier.height(8.dp))
        if (!creative.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = creative.imageUrl,
                contentDescription = creative.headline,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.onClick(placement)
                    val url = creative.clickUrl
                    if (!url.isNullOrBlank()) {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (e: Exception) {
                            // No browser installed: the ad stays informational, nothing crashes.
                        }
                    }
                }
        ) {
            Text(
                text = creative.headline ?: unit.campaign.name,
                style = MaterialTheme.typography.titleMedium,
                color = RabhaColors.TextPrimary
            )
            if (!creative.body.isNullOrBlank()) {
                Text(
                    text = creative.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
            }
        }
    }
}
