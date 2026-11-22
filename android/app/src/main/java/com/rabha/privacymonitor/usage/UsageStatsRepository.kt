package com.rabha.privacymonitor.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.UnavailableReason
import com.rabha.privacymonitor.core.runCatchingResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

data class AppUsageInfo(
    val packageName: String,
    val label: String,
    val foregroundMillis: Long,
    val lastUsed: Long
)

data class UsageEventInfo(
    val packageName: String,
    val label: String,
    val type: String,
    val timestamp: Long
)

@Singleton
class UsageStatsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val usageManager: UsageStatsManager? =
        context.getSystemService(UsageStatsManager::class.java)

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun unavailable() = RabhaResult.Unavailable(
        UnavailableReason.MISSING_USAGE_ACCESS,
        "Android hides app usage without Usage Access. Grant it in Settings → Special app access → Usage access."
    )

    suspend fun usage(start: Long, end: Long = System.currentTimeMillis()): RabhaResult<List<AppUsageInfo>> =
        withContext(Dispatchers.IO) {
            if (!hasUsageAccess()) return@withContext unavailable()
            val manager = usageManager ?: return@withContext RabhaResult.Unavailable(
                UnavailableReason.NOT_SUPPORTED_ON_THIS_ANDROID,
                "UsageStatsManager is not present on this device."
            )
            runCatchingResult {
                val stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end).orEmpty()
                val pm = context.packageManager
                stats.filter { it.totalTimeInForeground > 0 }
                    .map { stat ->
                        val label = try {
                            pm.getApplicationLabel(pm.getApplicationInfo(stat.packageName, 0)).toString()
                        } catch (e: Exception) {
                            stat.packageName
                        }
                        AppUsageInfo(
                            packageName = stat.packageName,
                            label = label,
                            foregroundMillis = stat.totalTimeInForeground,
                            lastUsed = stat.lastTimeUsed
                        )
                    }
                    .sortedByDescending { it.foregroundMillis }
            }
        }

    suspend fun recentEvents(limit: Int = 60): RabhaResult<List<UsageEventInfo>> =
        withContext(Dispatchers.IO) {
            if (!hasUsageAccess()) return@withContext unavailable()
            val manager = usageManager ?: return@withContext RabhaResult.Unavailable(
                UnavailableReason.NOT_SUPPORTED_ON_THIS_ANDROID,
                "UsageStatsManager is not present on this device."
            )
            runCatchingResult {
                val begin = System.currentTimeMillis() - 24L * 60 * 60 * 1000
                val events = manager.queryEvents(begin, System.currentTimeMillis())
                val pm = context.packageManager
                val out = ArrayList<UsageEventInfo>()
                val event = UsageEvents.Event()
                while (events.hasNextEvent() && out.size < limit) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                        event.eventType == UsageEvents.Event.ACTIVITY_PAUSED
                    ) {
                        val label = try {
                            pm.getApplicationLabel(pm.getApplicationInfo(event.packageName, 0)).toString()
                        } catch (e: Exception) {
                            event.packageName
                        }
                        out.add(
                            UsageEventInfo(
                                packageName = event.packageName,
                                label = label,
                                type = if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) "Opened" else "Left",
                                timestamp = event.timeStamp
                            )
                        )
                    }
                }
                out.sortedByDescending { it.timestamp }
            }
        }

    companion object {
        fun startOfToday(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }
    }
}
