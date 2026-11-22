package ai.rabha.monitor.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ai.rabha.monitor.monitor.AppUsage
import ai.rabha.monitor.monitor.MonitorState
import ai.rabha.monitor.monitor.MonitoringRepository
import ai.rabha.monitor.monitor.Traffic
import ai.rabha.monitor.permissions.PermissionState
import ai.rabha.monitor.service.MonitoringService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun gb(v: Long): String = String.format("%.2f GB", v / 1024.0 / 1024.0 / 1024.0)

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var traffic by remember { mutableStateOf<MonitorState<Traffic>?>(null) }
    var usage by remember { mutableStateOf<MonitorState<List<AppUsage>>?>(null) }
    var running by remember { mutableStateOf(false) }

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
        Text("Today on this device", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                val t = traffic
                when (t) {
                    null -> Text("Reading…", style = MaterialTheme.typography.bodyMedium)
                    is MonitorState.Available -> {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Upload", style = MaterialTheme.typography.bodyMedium)
                            Text(gb(t.value.uploadBytes), style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Download", style = MaterialTheme.typography.bodyMedium)
                            Text(gb(t.value.downloadBytes), style = MaterialTheme.typography.bodyMedium)
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

        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                val u = usage
                when (u) {
                    null -> Text("Reading…", style = MaterialTheme.typography.bodyMedium)
                    is MonitorState.Available -> Text(
                        "${u.value.size} apps used today",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    is MonitorState.Unavailable -> Text(
                        u.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                MonitoringService.start(context)
                running = true
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (running) "Monitoring started" else "Start monitoring") }

        Spacer(Modifier.height(10.dp))
        val perms = PermissionState.items(context)
        val missing = perms.filter { !it.granted }
        if (missing.isNotEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Permissions still missing", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    missing.forEach { p ->
                        Text("• " + p.title, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val i = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(i)
                    }) { Text("Open settings") }
                }
            }
        }
    }
}
