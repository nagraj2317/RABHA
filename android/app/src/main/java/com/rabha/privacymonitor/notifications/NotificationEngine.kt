package com.rabha.privacymonitor.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rabha.privacymonitor.MainActivity
import com.rabha.privacymonitor.R
import com.rabha.privacymonitor.core.Constants
import com.rabha.privacymonitor.local.NotificationDao
import com.rabha.privacymonitor.local.NotificationEntity
import com.rabha.privacymonitor.local.PrefsStore
import com.rabha.privacymonitor.network.MonitorForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the four channels and every notification the app posts.
 * Ads are never allowed here: there is no ad path into this class by design.
 */
@Singleton
class NotificationEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDao: NotificationDao,
    private val prefsStore: PrefsStore
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channels = listOf(
            Triple(Constants.MONITOR_CHANNEL_ID, R.string.notif_channel_monitoring, NotificationManager.IMPORTANCE_LOW),
            Triple(Constants.ALERTS_CHANNEL_ID, R.string.notif_channel_alerts, NotificationManager.IMPORTANCE_HIGH),
            Triple(Constants.REPORTS_CHANNEL_ID, R.string.notif_channel_reports, NotificationManager.IMPORTANCE_DEFAULT),
            Triple(Constants.ANNOUNCEMENTS_CHANNEL_ID, R.string.notif_channel_announcements, NotificationManager.IMPORTANCE_DEFAULT)
        )
        channels.forEach { (id, nameRes, importance) ->
            val channel = NotificationChannel(id, context.getString(nameRes), importance).apply {
                description = when (id) {
                    Constants.MONITOR_CHANNEL_ID -> "Always-on monitoring status. Cannot be hidden while monitoring runs."
                    Constants.ALERTS_CHANNEL_ID -> "Data limit and privacy alerts."
                    Constants.REPORTS_CHANNEL_ID -> "Daily and weekly summaries."
                    else -> "Product announcements."
                }
                setShowBadge(id == Constants.ALERTS_CHANNEL_ID)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun serviceAction(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MonitorForegroundService::class.java).setAction(action)
        return PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun monitorNotification(summary: String, paused: Boolean): Notification {
        val builder = NotificationCompat.Builder(context, Constants.MONITOR_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(if (paused) "Monitoring paused" else "RABHA is monitoring your phone")
            .setContentText(summary)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(contentIntent())
            .addAction(
                0,
                context.getString(R.string.monitor_pause),
                serviceAction(Constants.FOREGROUND_SERVICE_ACTION_PAUSE, 11)
            )
            .addAction(
                0,
                context.getString(R.string.monitor_stop),
                serviceAction(Constants.FOREGROUND_SERVICE_ACTION_STOP, 12)
            )
        return builder.build()
    }

    fun postAlert(title: String, body: String) {
        post(Constants.ALERTS_CHANNEL_ID, title, body, "alert", checkPref = true)
    }

    fun postReport(title: String, body: String) {
        post(Constants.REPORTS_CHANNEL_ID, title, body, "report", checkPref = true)
    }

    fun postAnnouncement(title: String, body: String) {
        post(Constants.ANNOUNCEMENTS_CHANNEL_ID, title, body, "announcement", checkPref = true)
    }

    private fun post(channelId: String, title: String, body: String, category: String, checkPref: Boolean) {
        scope.launch {
            val prefs = prefsStore.flow.first()
            val allowed = when (category) {
                "alert" -> prefs.alertsEnabled
                "report" -> prefs.reportsEnabled
                "announcement" -> prefs.announcementsEnabled
                else -> true
            }
            if (checkPref && !allowed) return@launch

            notificationDao.insert(
                NotificationEntity(
                    title = title,
                    body = body,
                    category = category,
                    timestamp = System.currentTimeMillis()
                )
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(contentIntent())
                .build()

            try {
                NotificationManagerCompat.from(context).notify(
                    (System.currentTimeMillis() % 100000).toInt(),
                    notification
                )
            } catch (se: SecurityException) {
                // POST_NOTIFICATIONS not granted: history is still recorded locally.
            }
        }
    }
}
