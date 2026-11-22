package com.rabha.privacymonitor.network

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.rabha.privacymonitor.core.Constants
import com.rabha.privacymonitor.core.Formatters
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.local.PrefsStore
import com.rabha.privacymonitor.notifications.NotificationEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The monitoring engine. It is deliberately impossible to run hidden:
 * this service always has a non-dismissible notification with Pause and Stop.
 */
@AndroidEntryPoint
class MonitorForegroundService : Service() {

    @Inject lateinit var notificationEngine: NotificationEngine
    @Inject lateinit var networkRepository: NetworkStatsRepository
    @Inject lateinit var prefsStore: PrefsStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var loop: Job? = null
    private var paused = false
    private var limitAlertSentForDay: String? = null

    override fun onCreate() {
        super.onCreate()
        notificationEngine.createChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.FOREGROUND_SERVICE_ACTION_PAUSE -> {
                paused = true
                scope.launch { prefsStore.setMonitoringPaused(true) }
                pushNotification("Monitoring paused by you. Tap Resume in the app.", paused = true)
                return START_STICKY
            }

            Constants.FOREGROUND_SERVICE_ACTION_STOP -> {
                scope.launch { prefsStore.setMonitoringPaused(false) }
                stopSelfSafely()
                return START_NOT_STICKY
            }
        }

        scope.launch {
            paused = prefsStore.flow.first().monitoringPaused
            pushNotification("Starting…", paused)
        }
        startLoop()
        return START_STICKY
    }

    private fun startLoop() {
        if (loop?.isActive == true) return
        loop = scope.launch {
            while (isActive) {
                refresh()
                delay(30_000)
            }
        }
    }

    private suspend fun refresh() {
        if (paused) {
            pushNotification("Paused. No reads are happening.", paused = true)
            return
        }
        val prefs = prefsStore.flow.first()
        val result = networkRepository.totals(NetworkStatsRepository.startOfToday())
        val text = when (result) {
            is RabhaResult.Available -> {
                val totals = result.value
                checkDataLimit(prefs.dataLimitBytes, totals.total)
                "Today: ${Formatters.bytes(totals.total)} · down ${Formatters.bytes(totals.rxBytes)} · up ${Formatters.bytes(totals.txBytes)}"
            }

            is RabhaResult.Unavailable -> "Usage Access is missing, so traffic cannot be read. Open the app to fix it."
            is RabhaResult.Failed -> "Read failed: ${result.message}"
        }
        pushNotification(text, paused = false)
    }

    private suspend fun checkDataLimit(limit: Long, used: Long) {
        if (limit <= 0 || used < limit) return
        val day = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        if (limitAlertSentForDay == day) return
        limitAlertSentForDay = day
        notificationEngine.postAlert(
            "Data limit reached",
            "You have used ${Formatters.bytes(used)} of your ${Formatters.bytes(limit)} daily limit."
        )
    }

    private fun pushNotification(text: String, paused: Boolean) {
        val notification = notificationEngine.monitorNotification(text, paused)
        try {
            ServiceCompat.startForeground(
                this,
                Constants.MONITOR_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } catch (e: Exception) {
            // If the system refuses the foreground promotion there is nothing to hide:
            // the service stops instead of running invisibly.
            stopSelfSafely()
        }
    }

    private fun stopSelfSafely() {
        loop?.cancel()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        loop?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MonitorForegroundService::class.java)
                .setAction(Constants.FOREGROUND_SERVICE_ACTION_START)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun pause(context: Context) {
            val intent = Intent(context, MonitorForegroundService::class.java)
                .setAction(Constants.FOREGROUND_SERVICE_ACTION_PAUSE)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MonitorForegroundService::class.java)
                .setAction(Constants.FOREGROUND_SERVICE_ACTION_STOP)
            context.startService(intent)
        }
    }
}
