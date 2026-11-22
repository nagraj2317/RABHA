package com.rabha.privacymonitor.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
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
import com.rabha.privacymonitor.ads.AdSlot
import com.rabha.privacymonitor.core.AccentDot
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
internal fun DashboardScreen(
    onOpenNetwork: () -> Unit,
    onOpenUsage: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.loading) {
        LoadingState()
        return
    }
    val data = state.data ?: return

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AccentDot(if (data.status.online) RabhaColors.Success else RabhaColors.Danger, size = 10)
                Text(
                    text = data.status.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = RabhaColors.TextSecondary
                )
            }
        }

        item {
            GlassCard(accent = RabhaColors.Amber) {
                Text(
                    text = "RABHA",
                    style = MaterialTheme.typography.labelSmall,
                    color = RabhaColors.TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (data.prefs.consentMonitoring && !data.prefs.monitoringPaused)
                        "Monitoring is on" else "Monitoring is off",
                    style = MaterialTheme.typography.headlineMedium,
                    color = RabhaColors.TextPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (data.prefs.consentMonitoring)
                        "A visible notification is running. Pause or stop it any time."
                    else
                        "Nothing is collected until you switch it on. The switch is in Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
                Row {
                    if (!data.prefs.consentMonitoring) {
                        TextButton(onClick = viewModel::enableMonitoring) {
                            Text("Start monitoring", color = RabhaColors.Amber)
                        }
                    } else if (data.prefs.monitoringPaused) {
                        TextButton(onClick = viewModel::enableMonitoring) {
                            Text("Resume", color = RabhaColors.Success)
                        }
                        TextButton(onClick = viewModel::stopMonitoring) {
                            Text("Stop", color = RabhaColors.Danger)
                        }
                    } else {
                        TextButton(onClick = viewModel::pauseMonitoring) {
                            Text("Pause", color = RabhaColors.Warning)
                        }
                        TextButton(onClick = viewModel::stopMonitoring) {
                            Text("Stop", color = RabhaColors.Danger)
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                val totals = data.totals
                StatTile(
                    label = "Data today",
                    value = when (totals) {
                        is RabhaResult.Available -> Formatters.bytes(totals.value.total)
                        else -> "—"
                    },
                    hint = when (totals) {
                        is RabhaResult.Available -> "down ${Formatters.bytes(totals.value.rxBytes)}"
                        is RabhaResult.Unavailable -> "needs Usage Access"
                        else -> "read failed"
                    },
                    accent = RabhaColors.Blue,
                    modifier = Modifier.weight(1f)
                )
                val screen = data.screenTimeMillis
                StatTile(
                    label = "Screen time",
                    value = when (screen) {
                        is RabhaResult.Available -> Formatters.duration(screen.value)
                        else -> "—"
                    },
                    hint = when (screen) {
                        is RabhaResult.Available -> data.topAppLabel ?: "no apps yet"
                        is RabhaResult.Unavailable -> "needs Usage Access"
                        else -> "read failed"
                    },
                    accent = RabhaColors.Purple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            GlassCard {
                SectionHeader("Privacy posture", "${data.grantedPermissions} of ${data.totalPermissions} permissions granted")
                MetricRow("Monitoring consent", if (data.prefs.consentMonitoring) "Given" else "Not given")
                MetricRow("Cloud upload", if (data.prefs.consentCloudUpload) "On" else "Off")
                MetricRow("Ads", if (data.prefs.consentAds) "On" else "Off")
                MetricRow("Retention", "${data.prefs.retentionDays} days")
            }
        }

        when (val totals = data.totals) {
            is RabhaResult.Unavailable -> item {
                UnavailableState("Traffic data unavailable", totals.detail, onOpenNetwork, "Open Monitor")
            }

            is RabhaResult.Failed -> item {
                UnavailableState("Traffic read failed", totals.message)
            }

            is RabhaResult.Available -> item {
                TextButton(onClick = onOpenNetwork) {
                    Text("See full traffic breakdown", color = RabhaColors.Blue)
                }
            }
        }

        item { TextButton(onClick = onOpenUsage) { Text("See app usage", color = RabhaColors.Purple) } }

        item {
            GlassCard(accent = RabhaColors.TextSecondary) {
                SectionHeader("Packet counter", "Local, sizes only")
                Text(
                    text = "Reads the tun descriptor, adds up byte counts and discards every payload. No TLS interception, no certificates, no payload parsing. While it runs, traffic is not forwarded — it is a short measurement, not a proxy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
                Row {
                    TextButton(onClick = viewModel::startPacketCounter) {
                        Text("Start counting", color = RabhaColors.Amber)
                    }
                    TextButton(onClick = viewModel::stopPacketCounter) {
                        Text("Stop", color = RabhaColors.TextSecondary)
                    }
                }
            }
        }

        item { AdSlot(placement = "home_dashboard") }

        if (state.message != null) {
            item {
                GlassCard(accent = RabhaColors.Success) {
                    Text(state.message!!, style = MaterialTheme.typography.bodyMedium, color = RabhaColors.TextPrimary)
                    TextButton(onClick = viewModel::dismissMessage) { Text("Dismiss", color = RabhaColors.TextSecondary) }
                }
            }
        }
    }
}
