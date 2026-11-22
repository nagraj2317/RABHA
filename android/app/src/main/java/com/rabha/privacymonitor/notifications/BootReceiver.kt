package com.rabha.privacymonitor.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rabha.privacymonitor.local.PrefsStore
import com.rabha.privacymonitor.network.MonitorForegroundService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var prefsStore: PrefsStore

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val prefs = prefsStore.flow.first()
                if (prefs.consentMonitoring && !prefs.monitoringPaused) {
                    MonitorForegroundService.start(context)
                }
            } catch (e: Exception) {
                // A failed restart is visible to the user next time the app opens.
            } finally {
                pendingResult.finish()
            }
        }
    }
}
