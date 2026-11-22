package com.rabha.privacymonitor.permissions

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
import com.rabha.privacymonitor.core.GlassCard
import com.rabha.privacymonitor.core.RabhaColors
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.SectionHeader
import com.rabha.privacymonitor.core.UnavailableState

@Composable
internal fun PermissionsScreen(viewModel: PermissionsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            GlassCard(accent = RabhaColors.Warning) {
                Text("Granted one at a time", style = MaterialTheme.typography.titleMedium, color = RabhaColors.TextPrimary)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Every permission below explains itself before it is asked for. Nothing is requested on first launch.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
            }
        }

        item { SectionHeader("This app", "Current state") }

        items(state.items) { item ->
            GlassCard(accent = if (item.granted) RabhaColors.Success else RabhaColors.Warning) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    AccentDot(if (item.granted) RabhaColors.Success else RabhaColors.Warning, size = 10)
                    Text(item.title, style = MaterialTheme.typography.titleMedium, color = RabhaColors.TextPrimary)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = if (item.granted) "Granted" else "Not granted",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.granted) RabhaColors.Success else RabhaColors.TextSecondary
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(item.why, style = MaterialTheme.typography.bodyMedium, color = RabhaColors.TextSecondary)
                if (!item.granted) {
                    TextButton(onClick = {
                        when (item.specialAccess) {
                            "usage" -> viewModel.openUsageSettings()
                            else -> if (item.permission == "android.permission.POST_NOTIFICATIONS") {
                                viewModel.openNotificationSettings()
                            } else {
                                viewModel.openAppSettings()
                            }
                        }
                    }) {
                        Text(
                            text = if (item.specialAccess == "usage") "Open usage access settings" else "Open settings",
                            color = RabhaColors.Amber
                        )
                    }
                }
            }
        }

        item { SectionHeader("Other apps", "Declared only") }

        item {
            GlassCard {
                Text(
                    text = "Android lets an app read which permissions another app declares in its manifest. What you granted to that app is not readable without a signature-level permission. This list therefore says “declares”, never “granted”.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
            }
        }

        when (val declared = state.declared) {
            is RabhaResult.Available -> items(declared.value.take(30)) { info ->
                GlassCard {
                    Text(info.label, style = MaterialTheme.typography.titleMedium, color = RabhaColors.TextPrimary)
                    Text(info.packageName, style = MaterialTheme.typography.bodyMedium, color = RabhaColors.TextSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Declares: " + info.declared.take(4).joinToString { it.substringAfterLast('.') },
                        style = MaterialTheme.typography.bodyMedium,
                        color = RabhaColors.Blue
                    )
                }
            }

            is RabhaResult.Unavailable -> item { UnavailableState("Cannot list apps", declared.detail) }
            is RabhaResult.Failed -> item { UnavailableState("Read failed", declared.message) }
            null -> item { EmptyState("Loading", "Reading installed packages…") }
        }
    }
}
