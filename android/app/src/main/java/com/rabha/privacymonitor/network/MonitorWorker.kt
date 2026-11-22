package com.rabha.privacymonitor.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.local.MonitoringDao
import com.rabha.privacymonitor.local.MonitoringSnapshotEntity
import com.rabha.privacymonitor.local.PrefsStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Persists one aggregated snapshot so the dashboard has history even when the app is closed. */
@HiltWorker
class MonitorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: NetworkStatsRepository,
    private val monitoringDao: MonitoringDao,
    private val prefsStore: PrefsStore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = prefsStore.flow.first()
        if (!prefs.consentMonitoring || prefs.monitoringPaused) return Result.success()

        val result = repository.totals(NetworkStatsRepository.startOfToday())
        if (result !is RabhaResult.Available) return Result.success()

        val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        monitoringDao.insert(
            MonitoringSnapshotEntity(
                day = day,
                rxBytes = result.value.rxBytes,
                txBytes = result.value.txBytes,
                screenMillis = 0L,
                networkType = "all",
                recordedAt = System.currentTimeMillis()
            )
        )
        monitoringDao.deleteOlderThan(
            System.currentTimeMillis() - prefs.retentionDays * 24L * 60 * 60 * 1000
        )
        return Result.success()
    }

    companion object {
        const val NAME = "rabha-monitor-refresh"
    }
}
