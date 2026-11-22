package ai.rabha.monitor.permissions

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import androidx.core.content.ContextCompat

data class PermissionItem(
    val key: String,
    val title: String,
    val why: String,
    val granted: Boolean
)

object PermissionState {

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasPhoneState(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

    fun items(context: Context): List<PermissionItem> = listOf(
        PermissionItem(
            "usage", "Usage access",
            "Reads how long each app was in the foreground. Without it Android returns nothing.",
            hasUsageAccess(context)
        ),
        PermissionItem(
            "notifications", "Notifications",
            "Shows the permanent monitoring notification with Pause and Stop.",
            hasNotifications(context)
        ),
        PermissionItem(
            "phone", "Phone state",
            "Needed only for mobile-data totals. Wi-Fi totals work without it.",
            hasPhoneState(context)
        )
    )
}
