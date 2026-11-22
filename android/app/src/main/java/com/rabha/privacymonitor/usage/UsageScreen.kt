package com.rabha.privacymonitor.usage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rabha.privacymonitor.core.EmptyState
import com.rabha.privacymonitor.core.Formatters
import com.rabha.privacymonitor.core.GlassCard
import com.rabha.privacymonitor.core.LoadingState
import com.rabha.privacymonitor.core.RabhaColors
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.SectionHeader
import com.rabha.privacymonitor.core.UnavailableState

@Composable
internal fun UsageScreen(viewModel: UsageViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.loading) {
        LoadingState("Reading UsageStatsManager…")
        return
    }

    val today = state.today
    if (today is RabhaResult.Unavailable) {
        UnavailableState("App usage needs Usage Access", today.detail)
        return
    }

    val rows = (today as? RabhaResult.Available)?.value.orEmpty()
    val weekRows = (state.week as? RabhaResult.Available)?.value.orEmpty()
    val events = (state.events as? RabhaResult.Available)?.value.orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            GlassCard(accent = RabhaColors.Purple) {
                Text("Screen time today", style = MaterialTheme.typography.labelSmall, color = RabhaColors.TextSecondary)
                Text(
                    text = Formatters.duration(rows.sumOf { it.foregroundMillis }),
                    style = MaterialTheme.typography.displaySmall,
                    color = RabhaColors.TextPrimary
                )
                Text(
                    text = "${rows.size} apps opened",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
            }
        }

        item { SectionHeader("Most used today", "Foreground time") }

        if (rows.isEmpty()) {
            item { EmptyState("Nothing recorded today", "Android records usage only while apps are in the foreground.") }
        } else {
            items(rows.take(15)) { app ->
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(app.label, style = MaterialTheme.typography.titleMedium, color = RabhaColors.TextPrimary)
                            Text(
                                text = "Last used ${Formatters.shortTime(app.lastUsed)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RabhaColors.TextSecondary
                            )
                        }
                        Text(
                            text = Formatters.duration(app.foregroundMillis),
                            style = MaterialTheme.typography.titleMedium,
                            color = RabhaColors.Purple
                        )
                    }
                }
            }
        }

        item { SectionHeader("This week", "Top five") }
        items(weekRows.take(5)) { app ->
            GlassCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(app.label, color = RabhaColors.TextPrimary, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        Formatters.duration(app.foregroundMillis),
                        color = RabhaColors.Amber,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        item { SectionHeader("History", "Last 24 hours") }
        if (events.isEmpty()) {
            item { EmptyState("No events", "Usage events appear after apps are opened.") }
        } else {
            items(events.take(30)) { event ->
                GlassCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(event.label, color = RabhaColors.TextPrimary, style = MaterialTheme.typography.bodyLarge)
                            Text(event.type, color = RabhaColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            Formatters.shortTime(event.timestamp),
                            color = RabhaColors.TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
