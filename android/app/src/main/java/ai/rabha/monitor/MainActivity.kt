package ai.rabha.monitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ai.rabha.monitor.ui.RabhaApp
import ai.rabha.monitor.ui.theme.RabhaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ai.rabha.monitor.service.MonitoringService.createChannels(this)
        setContent {
            RabhaTheme {
                RabhaApp()
            }
        }
    }
}
