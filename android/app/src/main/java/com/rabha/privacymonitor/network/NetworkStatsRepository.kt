package com.rabha.privacymonitor.network

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
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

data class NetworkTotals(
    val rxBytes: Long,
    val txBytes: Long,
    val wifiRx: Long,
    val wifiTx: Long,
    val mobileRx: Long,
    val mobileTx: Long
) {
    val total: Long get() = rxBytes + txBytes
}

data class AppDataUsage(
    val packageName: String,
    val label: String,
    val rxBytes: Long,
    val txBytes: Long
) {
    val total: Long get() = rxBytes + txBytes
}

data class DayTotal(val dayLabel: String, val dayStart: Long, val bytes: Long)

@Singleton
class NetworkStatsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val statsManager: NetworkStatsManager? =
        context.getSystemService(NetworkStatsManager::class.java)

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun usageUnavailable() = RabhaResult.Unavailable(
        UnavailableReason.MISSING_USAGE_ACCESS,
        "Android returns no traffic numbers without Usage Access. Grant it in Settings → Special app access → Usage access."
    )

    /**
     * Device totals, summed from querySummary() buckets.
     *
     * querySummary() returns a NetworkStats iterator on every API level 29-35, while
     * querySummaryForDevice() has a different return type in the android-35 stub than in the
     * published documentation. One stable path avoids that difference entirely.
     */
    private fun sumForDevice(type: Int, start: Long, end: Long): Pair<Long, Long> {
        val manager = statsManager ?: return 0L to 0L
        val stats: NetworkStats = try {
            manager.querySummary(type, null, start, end)
        } catch (se: SecurityException) {
            return 0L to 0L
        }
        var rx = 0L
        var tx = 0L
        val bucket = NetworkStats.Bucket()
        stats.use {
            while (it.hasNextBucket()) {
                it.getNextBucket(bucket)
                rx += bucket.rxBytes
                tx += bucket.txBytes
            }
        }
        return rx to tx
    }

    suspend fun totals(rangeStart: Long, rangeEnd: Long = System.currentTimeMillis()): RabhaResult<NetworkTotals> =
        withContext(Dispatchers.IO) {
            if (!hasUsageAccess()) return@withContext usageUnavailable()
            if (statsManager == null) {
                return@withContext RabhaResult.Unavailable(
                    UnavailableReason.NOT_SUPPORTED_ON_THIS_ANDROID,
                    "NetworkStatsManager is not present on this device."
                )
            }
            runCatchingResult {
                val (wifiRx, wifiTx) = sumForDevice(ConnectivityManager.TYPE_WIFI, rangeStart, rangeEnd)
                val (mobileRx, mobileTx) = try {
                    sumForDevice(ConnectivityManager.TYPE_MOBILE, rangeStart, rangeEnd)
                } catch (se: SecurityException) {
                    0L to 0L
                }
                NetworkTotals(
                    rxBytes = wifiRx + mobileRx,
                    txBytes = wifiTx + mobileTx,
                    wifiRx = wifiRx,
                    wifiTx = wifiTx,
                    mobileRx = mobileRx,
                    mobileTx = mobileTx
                )
            }
        }

    /** Per-uid traffic for the range. Requires Usage Access; returns an explicit state otherwise. */
    suspend fun perApp(rangeStart: Long, rangeEnd: Long = System.currentTimeMillis()): RabhaResult<List<AppDataUsage>> =
        withContext(Dispatchers.IO) {
            if (!hasUsageAccess()) return@withContext usageUnavailable()
            val manager = statsManager ?: return@withContext RabhaResult.Unavailable(
                UnavailableReason.NOT_SUPPORTED_ON_THIS_ANDROID,
                "NetworkStatsManager is not present on this device."
            )
            runCatchingResult {
                val merged = HashMap<Int, LongArray>()
                fun collect(type: Int) {
                    val stats = try {
                        manager.querySummary(type, null, rangeStart, rangeEnd)
                    } catch (se: SecurityException) {
                        null
                    } ?: return
                    val bucket = NetworkStats.Bucket()
                    stats.use {
                        while (it.hasNextBucket()) {
                            it.getNextBucket(bucket)
                            val entry = merged.getOrPut(bucket.uid) { LongArray(2) }
                            entry[0] += bucket.rxBytes
                            entry[1] += bucket.txBytes
                        }
                    }
                }
                collect(ConnectivityManager.TYPE_WIFI)
                collect(ConnectivityManager.TYPE_MOBILE)

                val pm = context.packageManager
                merged.entries
                    .filter { it.key > 0 && (it.value[0] + it.value[1]) > 0 }
                    .map { (uid, bytes) ->
                        val packages = pm.getPackagesForUid(uid)
                        val pkg = packages?.firstOrNull() ?: "uid:$uid"
                        val label = try {
                            pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                        } catch (e: Exception) {
                            pkg
                        }
                        AppDataUsage(
                            packageName = pkg,
                            label = label,
                            rxBytes = bytes[0],
                            txBytes = bytes[1]
                        )
                    }
                    .sortedByDescending { it.total }
            }
        }

    suspend fun dailySeries(days: Int): RabhaResult<List<DayTotal>> = withContext(Dispatchers.IO) {
        if (!hasUsageAccess()) return@withContext usageUnavailable()
        runCatchingResult {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val todayStart = calendar.timeInMillis
            val dayMillis = 24L * 60 * 60 * 1000
            (days - 1 downTo 0).map { offset ->
                val start = todayStart - offset * dayMillis
                val end = start + dayMillis
                val (wifiRx, wifiTx) = sumForDevice(ConnectivityManager.TYPE_WIFI, start, end)
                val (mobileRx, mobileTx) = try {
                    sumForDevice(ConnectivityManager.TYPE_MOBILE, start, end)
                } catch (se: SecurityException) {
                    0L to 0L
                }
                val label = if (offset == 0) "Today" else "-${offset}d"
                DayTotal(label, start, wifiRx + wifiTx + mobileRx + mobileTx)
            }
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

        fun startOfWeek(): Long = startOfToday() - 6 * 24L * 60 * 60 * 1000
        fun startOfMonth(): Long = startOfToday() - 29 * 24L * 60 * 60 * 1000
    }
}
