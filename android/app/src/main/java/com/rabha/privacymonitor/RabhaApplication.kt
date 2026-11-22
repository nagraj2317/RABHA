package com.rabha.privacymonitor

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rabha.privacymonitor.network.MonitorWorker
import com.rabha.privacymonitor.notifications.NotificationEngine
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class RabhaApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationEngine: NotificationEngine

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationEngine.createChannels()
        scheduleMonitoring()
    }

    private fun scheduleMonitoring() {
        val request = PeriodicWorkRequestBuilder<MonitorWorker>(15, TimeUnit.MINUTES)
            .build()
        try {
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                MonitorWorker.NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        } catch (e: IllegalStateException) {
            // WorkManager is not initialised in some test contexts; the foreground service still works.
        }
    }
}
