package ai.rabha.monitor.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ai.rabha.monitor.ui.theme.fontOptions
import ai.rabha.monitor.ui.theme.palettes
import ai.rabha.monitor.ui.theme.schemeFor

enum class RabhaTab(val label: String) {
    Home("Home"),
    Monitor("Monitor"),
    Wallpapers("Wallpapers"),
    Notifications("Notifications"),
    Settings("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RabhaApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("rabha", Context.MODE_PRIVATE) }

    var onboarded by remember { mutableStateOf(prefs.getBoolean("onboarded", false)) }
    var themeIndex by remember { mutableIntStateOf(prefs.getInt("theme", 0)) }
    var fontIndex by remember { mutableIntStateOf(prefs.getInt("font", 0)) }
    var selected by remember { mutableStateOf(RabhaTab.Home) }

    if (!onboarded) {
        OnboardingScreen {
            prefs.edit().putBoolean("onboarded", true).apply()
            onboarded = true
        }
        return
    }

    val palette = palettes[themeIndex.coerceIn(0, palettes.size - 1)]
    val font = fontOptions[fontIndex.coerceIn(0, fontOptions.size - 1)].family

    MaterialTheme(
        colorScheme = schemeFor(palette),
        typography = Typography(defaultFontFamily = font)
    ) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("RABHA Privacy Monitor") }) },
            bottomBar = {
                NavigationBar {
                    RabhaTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selected == tab,
                            onClick = { selected = tab },
                            icon = { Icon(Icons.Filled.Circle, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.TopStart
            ) {
                when (selected) {
                    RabhaTab.Home -> HomeScreen()
                    RabhaTab.Monitor -> MonitorScreen()
                    RabhaTab.Wallpapers -> WallpapersScreen()
                    RabhaTab.Settings -> SettingsScreen(
                        themeIndex = themeIndex,
                        fontIndex = fontIndex,
                        onTheme = {
                            themeIndex = it
                            prefs.edit().putInt("theme", it).apply()
                        },
                        onFont = {
                            fontIndex = it
                            prefs.edit().putInt("font", it).apply()
                        }
                    )
                    RabhaTab.Notifications -> UnavailableState(
                        title = "Notifications",
                        reason = "Notification history is not connected yet. The monitoring notification is running in the status bar."
                    )
                }
            }
        }
    }
}

@Composable
internal fun UnavailableState(title: String, reason: String) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        androidx.compose.foundation.layout.Spacer(Modifier.padding(6.dp))
        Text(
            text = reason,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
