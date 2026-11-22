package com.rabha.privacymonitor.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rabha.privacymonitor.core.Constants
import com.rabha.privacymonitor.core.Formatters
import com.rabha.privacymonitor.core.GlassCard
import com.rabha.privacymonitor.core.RabhaColors
import com.rabha.privacymonitor.core.SectionHeader

@Composable
internal fun SettingsScreen(
    onOpenAccount: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SectionHeader("Privacy", "Consent, in your hands") }

        item {
            GlassCard(accent = RabhaColors.Amber) {
                ToggleRow(
                    title = "Monitoring consent",
                    subtitle = "Reads traffic and usage on this device. Always shows a notification.",
                    checked = prefs.consentMonitoring,
                    onChange = viewModel::setMonitoring
                )
                ToggleRow(
                    title = "Cloud upload",
                    subtitle = "Off by default. When off, nothing leaves this phone.",
                    checked = prefs.consentCloudUpload,
                    onChange = viewModel::setCloudUpload
                )
                ToggleRow(
                    title = "Ads",
                    subtitle = "Funds the app. Never targeted with monitoring data.",
                    checked = prefs.consentAds,
                    onChange = viewModel::setAds
                )
            }
        }

        item { SectionHeader("Notifications", "Per category") }

        item {
            GlassCard {
                ToggleRow("Alerts", "Data limit and privacy alerts.", prefs.alertsEnabled, viewModel::setAlerts)
                ToggleRow("Reports", "Daily and weekly summaries.", prefs.reportsEnabled, viewModel::setReports)
                ToggleRow("Announcements", "Product news.", prefs.announcementsEnabled, viewModel::setAnnouncements)
            }
        }

        item { SectionHeader("Data", "Limits and retention") }

        item {
            GlassCard(accent = RabhaColors.Blue) {
                Text(
                    text = "Daily alert limit: ${Formatters.bytes(prefs.dataLimitBytes)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = RabhaColors.TextPrimary
                )
                Row {
                    listOf(500L, 1024L, 2048L, 5120L).forEach { mb ->
                        TextButton(onClick = { viewModel.setDataLimit(mb) }) {
                            Text("${mb} MB", color = RabhaColors.Blue)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Keep history for ${prefs.retentionDays} days",
                    style = MaterialTheme.typography.bodyLarge,
                    color = RabhaColors.TextPrimary
                )
                Row {
                    listOf(7, 14, 30, 90).forEach { days ->
                        TextButton(onClick = { viewModel.setRetention(days) }) {
                            Text("$days d", color = RabhaColors.Purple)
                        }
                    }
                }
                Text(
                    text = "Stored as daily totals and app usage durations. No URLs, no packet contents, no app contents.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
            }
        }

        item { SectionHeader("Ad engine", "Interval is server-controlled") }

        item {
            GlassCard {
                Text(
                    text = "Default interval: ${Constants.AD_DEFAULT_INTERVAL_MINUTES} minutes. The admin panel can override it per campaign. Frequency caps and campaign dates are enforced on the device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
            }
        }

        item { SectionHeader("Account", "Sign-in and deletion") }

        item {
            GlassCard(accent = RabhaColors.Danger) {
                Text(
                    text = viewModel.signedInEmail ?: "Not signed in",
                    style = MaterialTheme.typography.bodyLarge,
                    color = RabhaColors.TextPrimary
                )
                Row {
                    TextButton(onClick = onOpenAccount) { Text("Sign in", color = RabhaColors.Amber) }
                    TextButton(onClick = viewModel::signOut) { Text("Sign out", color = RabhaColors.TextSecondary) }
                }
                TextButton(onClick = viewModel::deleteLocalData) {
                    Text("Delete local data", color = RabhaColors.Warning)
                }
                TextButton(onClick = viewModel::deleteAccount) {
                    Text("Delete my account", color = RabhaColors.Danger)
                }
                Text(
                    text = "Account deletion calls a SECURITY DEFINER function that can only act on your own auth.uid().",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
            }
        }

        item { SectionHeader("About", null) }

        item {
            GlassCard {
                Text(
                    text = "RABHA Privacy Monitor 1.0.0\nPrivacy-first by design: nothing hidden, nothing intercepted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RabhaColors.TextSecondary
                )
                TextButton(onClick = {
                    try {
                        context.startActivity(
                            android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(Constants.PRIVACY_POLICY_URL)
                            )
                        )
                    } catch (e: Exception) {
                        // No browser installed.
                    }
                }) { Text("Privacy policy", color = RabhaColors.Blue) }
            }
        }

        if (message != null) {
            item {
                GlassCard(accent = RabhaColors.Success) {
                    Text(message!!, style = MaterialTheme.typography.bodyMedium, color = RabhaColors.TextPrimary)
                    TextButton(onClick = viewModel::clearMessage) {
                        Text("Dismiss", color = RabhaColors.TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = RabhaColors.TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = RabhaColors.TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = RabhaColors.Black,
                checkedTrackColor = RabhaColors.Amber,
                uncheckedTrackColor = RabhaColors.SurfaceHigh
            )
        )
    }
}
