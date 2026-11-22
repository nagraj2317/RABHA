package com.rabha.privacymonitor.network

import android.app.Notification
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.rabha.privacymonitor.MainActivity
import com.rabha.privacymonitor.core.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream

/**
 * Local packet-size counter.
 *
 * It reads the tun descriptor, adds up byte counts and discards every payload.
 * There is no TLS interception, no certificate install and no payload parsing anywhere in this file.
 *
 * Honest limitation (shown in the UI before it is switched on): with no forwarder in userspace
 * the counted packets are not re-injected, so internet traffic does not pass while counting runs.
 * It is a short measurement tool, not a proxy, and it never claims to be one.
 */
class RabhaVpnService : VpnService() {

    private var tunnel: ParcelFileDescriptor? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    var rxBytes: Long = 0
        private set

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopCounting()
                return START_NOT_STICKY
            }

            else -> startCounting()
        }
        return START_STICKY
    }

    private val notificationManager by lazy {
        getSystemService(android.app.NotificationManager::class.java)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, Constants.MONITOR_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("RABHA packet counter is running")
            .setContentText("Counting packet sizes only. Payloads are discarded.")
            .setOngoing(true)
            .setContentIntent(
                android.app.PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

    private fun startCounting() {
        if (tunnel != null) return
        val builder = Builder()
            .setSession("RABHA packet counter")
            .addAddress("10.111.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .setBlocking(true)
        val descriptor = try {
            builder.establish()
        } catch (e: Exception) {
            null
        }
        tunnel = descriptor ?: run {
            stopSelf()
            return
        }
        notificationManager.notify(Constants.MONITOR_NOTIFICATION_ID + 1, buildNotification())
        val input = FileInputStream(tunnel!!.fileDescriptor)
        scope.launch {
            val buffer = ByteArray(32767)
            while (isActive) {
                val read = try {
                    input.read(buffer)
                } catch (e: Exception) {
                    -1
                }
                if (read < 0) break
                rxBytes += read.toLong()
                // Payload intentionally discarded: sizes only, no content is stored or parsed.
            }
            delay(1)
        }
    }

    private fun stopCounting() {
        try {
            tunnel?.close()
        } catch (e: Exception) {
            // Closing an already-closed descriptor is not an error worth surfacing.
        }
        tunnel = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        stopCounting()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "com.rabha.privacymonitor.vpn.STOP"
    }
}
