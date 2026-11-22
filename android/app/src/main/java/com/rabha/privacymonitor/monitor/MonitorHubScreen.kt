package com.rabha.privacymonitor.monitor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rabha.privacymonitor.core.RabhaColors
import com.rabha.privacymonitor.files.FilesScreen
import com.rabha.privacymonitor.network.NetworkScreen
import com.rabha.privacymonitor.permissions.PermissionsScreen
import com.rabha.privacymonitor.privacy.PrivacyScreen
import com.rabha.privacymonitor.usage.UsageScreen

private enum class MonitorTab(val label: String) {
    NETWORK("Network"),
    USAGE("App usage"),
    PERMISSIONS("Permissions"),
    PRIVACY("Privacy"),
    FILES("Files")
}

/** The Monitor destination: one hub, five real data surfaces. */
@Composable
internal fun MonitorHubScreen() {
    var tab by remember { mutableStateOf(MonitorTab.NETWORK) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(MonitorTab.entries.size) { index ->
                val entry = MonitorTab.entries[index]
                val selected = entry == tab
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) RabhaColors.Amber.copy(alpha = 0.18f) else RabhaColors.SurfaceHigh)
                        .clickable { tab = entry }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) RabhaColors.Amber else RabhaColors.TextSecondary
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(top = 6.dp)) {
            when (tab) {
                MonitorTab.NETWORK -> NetworkScreen()
                MonitorTab.USAGE -> UsageScreen()
                MonitorTab.PERMISSIONS -> PermissionsScreen()
                MonitorTab.PRIVACY -> PrivacyScreen()
                MonitorTab.FILES -> FilesScreen()
            }
        }
    }
}
