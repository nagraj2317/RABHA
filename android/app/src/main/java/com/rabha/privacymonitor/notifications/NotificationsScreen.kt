package com.rabha.privacymonitor.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rabha.privacymonitor.core.AccentDot
import com.rabha.privacymonitor.core.EmptyState
import com.rabha.privacymonitor.core.Formatters
import com.rabha.privacymonitor.core.GlassCard
import com.rabha.privacymonitor.core.RabhaColors
import com.rabha.privacymonitor.core.SectionHeader

@Composable
internal fun NotificationsScreen(viewModel: NotificationsViewModel = hiltViewModel()) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val unread by viewModel.unread.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            GlassCard(accent = RabhaColors.Blue) {
                Text("$unread unread", style = MaterialTheme.typography.headlineMedium, color = RabhaColors.TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Four channels: monitoring, alerts, reports, announcements. Ads never appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
                Row {
                    TextButton(onClick = viewModel::markAllRead) { Text("Mark all read", color = RabhaColors.Amber) }
                    TextButton(onClick = viewModel::clear) { Text("Clear history", color = RabhaColors.Danger) }
                }
            }
        }

        item { SectionHeader("History", "Stored on this device only") }

        if (history.isEmpty()) {
            item { EmptyState("No notifications yet", "Alerts and reports you receive are listed here.") }
        } else {
            items(history) { item ->
                GlassCard(accent = if (item.read) RabhaColors.GlassStroke else RabhaColors.Amber) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        if (!item.read) AccentDot(RabhaColors.Amber, size = 8)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium, color = RabhaColors.TextPrimary)
                            Text(item.body, style = MaterialTheme.typography.bodyMedium, color = RabhaColors.TextSecondary)
                            Text(
                                text = "${item.category} · ${Formatters.time(item.timestamp)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = RabhaColors.TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
