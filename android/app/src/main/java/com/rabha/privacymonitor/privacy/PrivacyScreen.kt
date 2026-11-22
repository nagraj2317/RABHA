package com.rabha.privacymonitor.privacy

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
import com.rabha.privacymonitor.core.GlassCard
import com.rabha.privacymonitor.core.RabhaColors
import com.rabha.privacymonitor.core.SectionHeader

@Composable
internal fun PrivacyScreen(viewModel: PrivacyViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            GlassCard(accent = RabhaColors.Danger) {
                Text("What is real, and what is not", style = MaterialTheme.typography.titleMedium, color = RabhaColors.TextPrimary)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "This screen never shows a status it cannot prove. Where Android blocks a read, it says so and names the permission that blocks it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
                TextButton(onClick = viewModel::refresh) { Text("Refresh", color = RabhaColors.Amber) }
            }
        }

        items(state.signals) { signal ->
            GlassCard(accent = if (signal.available) RabhaColors.Success else RabhaColors.Warning) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    AccentDot(if (signal.available) RabhaColors.Success else RabhaColors.TextSecondary, size = 10)
                    Text(signal.title, style = MaterialTheme.typography.titleMedium, color = RabhaColors.TextPrimary)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = signal.state,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (signal.state == "Active") RabhaColors.Danger else RabhaColors.TextSecondary
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(signal.detail, style = MaterialTheme.typography.bodyMedium, color = RabhaColors.TextSecondary)
            }
        }

        item { SectionHeader("Platform probe", "The real call, and its real refusal") }

        item {
            GlassCard {
                val recordings = state.recordings
                Text(
                    text = when {
                        recordings == null -> "Calling AudioManager.getActiveRecordingConfigurations()…"
                        recordings is com.rabha.privacymonitor.core.RabhaResult.Available ->
                            if (recordings.value.isEmpty()) "No active recordings reported."
                            else recordings.value.joinToString("\n")

                        recordings is com.rabha.privacymonitor.core.RabhaResult.Unavailable -> recordings.detail
                        else -> "Call failed."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
            }
        }
    }
}
