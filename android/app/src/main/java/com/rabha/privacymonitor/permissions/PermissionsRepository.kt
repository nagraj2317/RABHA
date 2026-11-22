package com.rabha.privacymonitor.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.UnavailableReason
import com.rabha.privacymonitor.core.runCatchingResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class PermissionItem(
    val title: String,
    val permission: String?,
    val granted: Boolean,
    val why: String,
    val specialAccess: String? = null
)

data class DeclaredPermissionInfo(
    val packageName: String,
    val label: String,
    val declared: List<String>
)

@Singleton
class PermissionsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun ownPermissions(): List<PermissionItem> {
        fun check(permission: String): Boolean =
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        val items = mutableListOf(
            PermissionItem(
                title = "Camera",
                permission = Manifest.permission.CAMERA,
                granted = check(Manifest.permission.CAMERA),
                why = "Only used if you attach a photo to a support ticket. Never read in the background."
            ),
            PermissionItem(
                title = "Microphone",
                permission = Manifest.permission.RECORD_AUDIO,
                granted = check(Manifest.permission.RECORD_AUDIO),
                why = "Not required by any feature. The app shows its own microphone state on the Privacy screen."
            ),
            PermissionItem(
                title = "Location",
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                granted = check(Manifest.permission.ACCESS_FINE_LOCATION),
                why = "Not used. Kept off unless you enable it yourself."
            ),
            PermissionItem(
                title = "Contacts",
                permission = Manifest.permission.READ_CONTACTS,
                granted = check(Manifest.permission.READ_CONTACTS),
                why = "Not used. The app never reads your contacts."
            ),
            PermissionItem(
                title = "Photos and videos",
                permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE,
                granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    check(Manifest.permission.READ_MEDIA_IMAGES) else check(Manifest.permission.READ_EXTERNAL_STORAGE),
                why = "Needed to pick an image or video for a wallpaper. Only the file you pick is read."
            ),
            PermissionItem(
                title = "Notifications",
                permission = Manifest.permission.POST_NOTIFICATIONS,
                granted = check(Manifest.permission.POST_NOTIFICATIONS),
                why = "Required for the always-visible monitoring notification and alerts."
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            items.add(
                PermissionItem(
                    title = "Nearby devices",
                    permission = Manifest.permission.BLUETOOTH_CONNECT,
                    granted = check(Manifest.permission.BLUETOOTH_CONNECT),
                    why = "Shown only so you can see the state. The app does not scan for devices."
                )
            )
        }

        items.add(
            PermissionItem(
                title = "Usage access",
                permission = null,
                granted = UsageAccessChecker.hasAccess(context),
                why = "Android requires this for per-app data and app usage. Granted in system settings, not by a dialog.",
                specialAccess = "usage"
            )
        )

        items.add(
            PermissionItem(
                title = "Phone state",
                permission = Manifest.permission.READ_PHONE_STATE,
                granted = check(Manifest.permission.READ_PHONE_STATE),
                why = "Needed on many devices to read mobile-data totals. Without it only Wi-Fi totals are available."
            )
        )

        return items
    }

    /**
     * For other apps Android exposes only what a package *declares* in its manifest.
     * What the user actually granted is not readable — this never pretends otherwise.
     */
    suspend fun declaredPermissions(): RabhaResult<List<DeclaredPermissionInfo>> = withContext(Dispatchers.IO) {
        runCatchingResult {
            val pm = context.packageManager
            val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            packages.mapNotNull { info ->
                val declared = info.requestedPermissions?.toList().orEmpty()
                if (declared.isEmpty()) return@mapNotNull null
                DeclaredPermissionInfo(
                    packageName = info.packageName,
                    label = try {
                        val appInfo = info.applicationInfo
                        if (appInfo != null) pm.getApplicationLabel(appInfo).toString() else info.packageName
                    } catch (e: Exception) {
                        info.packageName
                    },
                    declared = declared
                )
            }.sortedBy { it.label.lowercase() }
        }
    }

    fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

object UsageAccessChecker {
    fun hasAccess(context: Context): Boolean {
        val appOps = context.getSystemService(android.app.AppOpsManager::class.java) ?: return false
        val mode = appOps.unsafeCheckOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }
}

@Suppress("unused")
private fun unavailable(detail: String) =
    RabhaResult.Unavailable(UnavailableReason.MISSING_PERMISSION, detail)
