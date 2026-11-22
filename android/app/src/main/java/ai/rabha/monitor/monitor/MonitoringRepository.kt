package ai.rabha.monitor.monitor

import android.app.AppOpsManager
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Process

sealed interface MonitorState<out T> {
    data class Available<T>(val value: T) : MonitorState<T>
    data class Unavailable(val reason: String) : MonitorState<Nothing>
}

data class Traffic(val uploadBytes: Long, val downloadBytes: Long)
data class AppUsage(val packageName: String, val label: String, val usageMs: Long)

class MonitoringRepository(private val context: Context) {

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun usageToday(): MonitorState<List<AppUsage>> {
        if (!hasUsageAccess()) {
            return MonitorState.Unavailable(
                "Usage access is not granted. Android returns nothing without it, so no numbers are shown."
            )
        }
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 24L * 60L * 60L * 1000L
        val stats = try {
            usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
        } catch (e: Exception) {
            return MonitorState.Unavailable(
                "The system did not return usage data: " + (e.message ?: e.javaClass.simpleName)
            )
        } ?: return MonitorState.Unavailable("The system returned no usage data.")

        val pm = context.packageManager
        val rows = stats
            .filter { it.totalTimeInForeground > 0L }
            .sortedByDescending { it.totalTimeInForeground }
            .take(15)
            .map { s ->
                val label = runCatching {
                    pm.getApplicationLabel(pm.getApplicationInfo(s.packageName, 0)).toString()
                }.getOrDefault(s.packageName)
                AppUsage(s.packageName, label, s.totalTimeInForeground)
            }
        return MonitorState.Available(rows)
    }

    fun traffic(): MonitorState<Traffic> {
        val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val end = System.currentTimeMillis()
        val start = end - 24L * 60L * 60L * 1000L
        var up = 0L
        var down = 0L
        var any = false
        val types = listOf(ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE)
        for (type in types) {
            try {
                val bucket = nsm.querySummaryForDevice(type, null, start, end)
                if (bucket != null) {
                    up += bucket.txBytes
                    down += bucket.rxBytes
                    any = true
                }
            } catch (e: Exception) {
                // mobile totals need a subscriber id (READ_PHONE_STATE); skipped when absent
            }
        }
        return if (any) {
            MonitorState.Available(Traffic(up, down))
        } else {
            MonitorState.Unavailable(
                "Network statistics are not available on this device. Mobile totals need a subscriber id, which needs phone-state access."
            )
        }
    }
}
