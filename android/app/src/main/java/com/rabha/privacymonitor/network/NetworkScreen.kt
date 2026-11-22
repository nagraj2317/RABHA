package com.rabha.privacymonitor.network

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rabha.privacymonitor.core.AccentDot
import com.rabha.privacymonitor.core.EmptyState
import com.rabha.privacymonitor.core.Formatters
import com.rabha.privacymonitor.core.GlassCard
import com.rabha.privacymonitor.core.LoadingState
import com.rabha.privacymonitor.core.MetricRow
import com.rabha.privacymonitor.core.RabhaColors
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.SectionHeader
import com.rabha.privacymonitor.core.StatTile
import com.rabha.privacymonitor.core.UnavailableState

@Composable
internal fun NetworkScreen(viewModel: NetworkViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.loading) {
        LoadingState("Reading NetworkStatsManager…")
        return
    }

    val daily = state.daily
    if (daily is RabhaResult.Unavailable) {
        UnavailableState(
            reason = "Per-app and device traffic need Usage Access",
            detail = daily.detail,
            onAction = null
        )
        return
    }
    if (daily is RabhaResult.Failed) {
        UnavailableState(reason = "Traffic read failed", detail = daily.message)
        return
    }

    val totals = (daily as? RabhaResult.Available)?.value

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            GlassCard(accent = RabhaColors.Blue) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AccentDot(if (state.status.online) RabhaColors.Success else RabhaColors.Danger)
                    Text(
                        text = state.status.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = RabhaColors.TextSecondary
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = Formatters.bytes(totals?.total ?: 0L),
                    style = MaterialTheme.typography.displaySmall,
                    color = RabhaColors.TextPrimary
                )
                Text(
                    text = "used today, upload + download",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatTile(
                    label = "Download",
                    value = Formatters.bytes(totals?.rxBytes ?: 0L),
                    accent = RabhaColors.Blue,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Upload",
                    value = Formatters.bytes(totals?.txBytes ?: 0L),
                    accent = RabhaColors.Purple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            GlassCard {
                SectionHeader("Wi-Fi vs mobile", "Today")
                MetricRow("Wi-Fi down", Formatters.bytes(totals?.wifiRx ?: 0L))
                MetricRow("Wi-Fi up", Formatters.bytes(totals?.wifiTx ?: 0L))
                MetricRow("Mobile down", Formatters.bytes(totals?.mobileRx ?: 0L))
                MetricRow("Mobile up", Formatters.bytes(totals?.mobileTx ?: 0L))
            }
        }

        item {
            GlassCard {
                SectionHeader("Last 7 days", "Sum of Wi-Fi and mobile")
                val series = (state.series as? RabhaResult.Available)?.value.orEmpty()
                if (series.isEmpty()) {
                    EmptyState("No history yet", "Android keeps traffic buckets for a few days on most devices.")
                } else {
                    UsageBarChart(series)
                }
            }
        }

        item { SectionHeader("This month", "Weekly and monthly totals") }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatTile(
                    label = "7 days",
                    value = Formatters.bytes((state.weekly as? RabhaResult.Available)?.value?.total ?: 0L),
                    accent = RabhaColors.Amber,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "30 days",
                    value = Formatters.bytes((state.monthly as? RabhaResult.Available)?.value?.total ?: 0L),
                    accent = RabhaColors.Purple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { SectionHeader("Per app", "Last 30 days") }

        when (val perApp = state.perApp) {
            is RabhaResult.Available -> {
                if (perApp.value.isEmpty()) {
                    item { EmptyState("No per-app traffic", "No app recorded traffic in this window.") }
                } else {
                    items(perApp.value.take(40)) { app ->
                        GlassCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = RabhaColors.TextPrimary
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = RabhaColors.TextSecondary
                                    )
                                }
                                Text(
                                    text = Formatters.bytes(app.total),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = RabhaColors.Amber
                                )
                            }
                        }
                    }
                }
            }

            is RabhaResult.Unavailable -> item {
                UnavailableState("Per-app data unavailable", perApp.detail)
            }

            is RabhaResult.Failed -> item {
                UnavailableState("Per-app read failed", perApp.message)
            }

            null -> Unit
        }
    }
}

@Composable
private fun UsageBarChart(series: List<DayTotal>) {
    val max = series.maxOfOrNull { it.bytes }?.coerceAtLeast(1L) ?: 1L
    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            val barWidth = size.width / (series.size * 1.8f)
            val gap = (size.width - barWidth * series.size) / (series.size + 1)
            series.forEachIndexed { index, day ->
                val ratio = day.bytes.toFloat() / max.toFloat()
                val barHeight = (size.height - 12f) * ratio.coerceIn(0.02f, 1f)
                val left = gap + index * (barWidth + gap)
                drawRoundRect(
                    color = if (index == series.lastIndex) RabhaColors.Amber else RabhaColors.Blue.copy(alpha = 0.65f),
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            series.forEach { day ->
                Text(
                    text = day.dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = RabhaColors.TextSecondary
                )
            }
        }
    }
}
