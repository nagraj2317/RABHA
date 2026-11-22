package com.rabha.privacymonitor.dashboard

import com.rabha.privacymonitor.core.ConnectivityObserver
import com.rabha.privacymonitor.core.NetworkStatus
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.local.PrefsStore
import com.rabha.privacymonitor.local.RabhaPrefs
import com.rabha.privacymonitor.network.NetworkStatsRepository
import com.rabha.privacymonitor.network.NetworkTotals
import com.rabha.privacymonitor.permissions.PermissionsRepository
import com.rabha.privacymonitor.usage.UsageStatsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

data class DashboardData(
    val totals: RabhaResult<NetworkTotals>,
    val screenTimeMillis: RabhaResult<Long>,
    val topAppLabel: String?,
    val grantedPermissions: Int,
    val totalPermissions: Int,
    val prefs: RabhaPrefs,
    val status: NetworkStatus
)

@Singleton
class DashboardRepository @Inject constructor(
    private val networkStats: NetworkStatsRepository,
    private val usageStats: UsageStatsRepository,
    private val permissions: PermissionsRepository,
    private val prefsStore: PrefsStore,
    private val connectivity: ConnectivityObserver
) {

    suspend fun load(): DashboardData {
        val prefs = prefsStore.flow.first()
        val totals = networkStats.totals(NetworkStatsRepository.startOfToday())
        val usage = usageStats.usage(UsageStatsRepository.startOfToday())
        val screenTime: RabhaResult<Long> = when (usage) {
            is RabhaResult.Available -> RabhaResult.Available(usage.value.sumOf { it.foregroundMillis })
            is RabhaResult.Unavailable -> usage
            is RabhaResult.Failed -> usage
        }
        val topApp = (usage as? RabhaResult.Available)?.value?.firstOrNull()?.label
        val permissionItems = permissions.ownPermissions()
        return DashboardData(
            totals = totals,
            screenTimeMillis = screenTime,
            topAppLabel = topApp,
            grantedPermissions = permissionItems.count { it.granted },
            totalPermissions = permissionItems.size,
            prefs = prefs,
            status = connectivity.currentStatus()
        )
    }
}
