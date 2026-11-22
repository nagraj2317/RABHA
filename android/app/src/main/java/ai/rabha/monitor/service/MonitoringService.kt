package ai.rabha.monitor.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import ai.rabha.monitor.MainActivity

class MonitoringService : Service() {

    companion object {
        const val CHANNEL_MONITORING = "monitoring"
        const val CHANNEL_ALERTS = "alerts"
        const val CHANNEL_REPORTS = "reports"
        const val CHANNEL_ANNOUNCEMENTS = "announcements"
        private const val NOTIF_ID = 1001
        private const val ACTION_PAUSE = "ai.rabha.monitor.PAUSE"
        private const val ACTION_STOP = "ai.rabha.monitor.STOP"

        var paused = false

        fun start(context: Context) {
            val i = Intent(context, MonitoringService::class.java)
            ContextCompat.startForegroundService(context, i)
        }

        fun createChannels(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val mgr = context.getSystemService(NotificationManager::class.java)
            val defs = listOf(
                Triple(CHANNEL_MONITORING, "Monitoring", NotificationManager.IMPORTANCE_LOW),
                Triple(CHANNEL_ALERTS, "Alerts", NotificationManager.IMPORTANCE_DEFAULT),
                Triple(CHANNEL_REPORTS, "Reports", NotificationManager.IMPORTANCE_LOW),
                Triple(CHANNEL_ANNOUNCEMENTS, "Announcements", NotificationManager.IMPORTANCE_DEFAULT)
            )
            for ((id, name, importance) in defs) {
                val ch = NotificationChannel(id, name, importance)
                mgr.createNotificationChannel(ch)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> paused = true
            ACTION_STOP -> {
                paused = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> paused = false
        }
        startForeground(NOTIF_ID, buildNotification())
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        createChannels(this)

        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val pauseIntent = PendingIntent.getService(
            this, 1, Intent(this, MonitoringService::class.java).setAction(ACTION_PAUSE),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 2, Intent(this, MonitoringService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_MONITORING)
            .setContentTitle("RABHA monitoring")
            .setContentText(if (paused) "Paused — nothing is being read" else "Reading your own device data only")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(open)
            .addAction(0, if (paused) "Resume" else "Pause", pauseIntent)
            .addAction(0, "Stop", stopIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
