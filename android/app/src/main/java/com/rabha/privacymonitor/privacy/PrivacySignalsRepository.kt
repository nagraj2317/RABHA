package com.rabha.privacymonitor.privacy

import android.app.AppOpsManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Process
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.UnavailableReason
import com.rabha.privacymonitor.core.runCatchingResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class SignalState(
    val title: String,
    val state: String,
    val detail: String,
    val available: Boolean
)

@Singleton
class PrivacySignalsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun opState(op: String): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = appOps.unsafeCheckOpNoThrow(op, Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun ownSignals(): List<SignalState> = listOf(
        SignalState(
            title = "Microphone (this app)",
            state = if (opState(AppOpsManager.OPSTR_RECORD_AUDIO)) "Active" else "Idle",
            detail = "Read from AppOpsManager for this app only. No audio is recorded or stored.",
            available = true
        ),
        SignalState(
            title = "Camera (this app)",
            state = if (opState(AppOpsManager.OPSTR_CAMERA)) "Active" else "Idle",
            detail = "Read from AppOpsManager for this app only.",
            available = true
        ),
        SignalState(
            title = "Microphone (other apps)",
            state = "Unavailable",
            detail = "No public API reports another app's microphone use. AppOpsManager reads for other apps need the " +
                "signature-level GET_APP_OPS_STATS permission, which a Play app cannot hold.",
            available = false
        ),
        SignalState(
            title = "Camera (other apps)",
            state = "Unavailable",
            detail = "Same limit as above: another app's camera state is not readable without a signature permission.",
            available = false
        ),
        SignalState(
            title = "Active recordings",
            state = "Unavailable",
            detail = "AudioManager.getActiveRecordingConfigurations() requires MODIFY_AUDIO_ROUTING, and " +
                "AudioRecordingConfiguration never exposes the client package name publicly.",
            available = false
        ),
        SignalState(
            title = "System privacy indicator",
            state = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "Not readable" else "Not available",
            detail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                "Android 12+ shows the green dot in the status bar. That indicator is the only real source, " +
                    "and there is no API to read it from an app. The dot itself is the truth — trust it over any app."
            else
                "This device runs Android ${Build.VERSION.RELEASE}. Android 10 and 11 have no system privacy indicator at all.",
            available = false
        )
    )

    /** Attempts the platform call so the UI can show the real refusal instead of a guess. */
    suspend fun recordingConfigurations(): RabhaResult<List<String>> = withContext(Dispatchers.IO) {
        runCatchingResult {
            val audioManager = context.getSystemService(AudioManager::class.java)
                ?: throw IllegalStateException("AudioManager is not available")
            val configs = audioManager.activeRecordingConfigurations
            configs.map { it.toString() }
        }.let { result ->
            if (result is RabhaResult.Failed) {
                RabhaResult.Unavailable(
                    UnavailableReason.SIGNATURE_PERMISSION_REQUIRED,
                    "MODIFY_AUDIO_ROUTING is required to read active recording configurations: ${result.message}"
                )
            } else {
                result
            }
        }
    }
}
