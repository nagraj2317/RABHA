package ai.rabha.monitor.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import ai.rabha.monitor.monitor.AppUsage
import ai.rabha.monitor.monitor.MonitorState
import ai.rabha.monitor.monitor.MonitoringRepository
import ai.rabha.monitor.monitor.Traffic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun bytes(v: Long): String {
    val kb = v / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        else -> String.format("%.0f KB", kb)
    }
}

private fun duration(ms: Long): String {
    val mins = ms / 60000
    val h = mins / 60
    val m = mins % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

@Composable
fun MonitorScreen() {
    val context = LocalContext.current
    var usage by remember { mutableStateOf<MonitorState<List<AppUsage>>?>(null) }
    var traffic by remember { mutableStateOf<MonitorState<Traffic>?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val repo = MonitoringRepository(context)
            traffic = repo.traffic()
            usage = repo.usageToday()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Network · last 24h", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                when (val t = traffic) {
                    null -> Text("Reading…", style = MaterialTheme.typography.bodyMedium)
                    is MonitorState.Available -> {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Upload", style = MaterialTheme.typography.bodyMedium)
                            Text(bytes(t.value.uploadBytes), style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Download", style = MaterialTheme.typography.bodyMedium)
                            Text(bytes(t.value.downloadBytes), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    is MonitorState.Unavailable -> Text(
                        t.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Apps used · last 24h", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        when (val u = usage) {
            null -> Text("Reading…", style = MaterialTheme.typography.bodyMedium)
            is MonitorState.Unavailable -> Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        u.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = {
                        val i = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(i)
                    }) { Text("Open usage access settings") }
                }
            }
            is MonitorState.Available -> {
                if (u.value.isEmpty()) {
                    Text("No app usage recorded yet.", style = MaterialTheme.typography.bodySmall)
                } else {
                    u.value.forEach { row ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = row.icon
                                if (icon != null) {
                                    Image(
                                        bitmap = icon.toBitmap(96, 96).asImageBitmap(),
                                        contentDescription = row.label,
                                        modifier = Modifier.size(38.dp)
                                    )
                                    Spacer(Modifier.size(12.dp))
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(row.label, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        row.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(duration(row.usageMs), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
