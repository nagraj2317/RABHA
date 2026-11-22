package com.rabha.privacymonitor.security

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/** A stable, non-resettable-per-app device id, hashed so the raw ANDROID_ID never leaves the device. */
@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val deviceId: String by lazy {
        val raw = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        digest.joinToString("") { "%02x".format(it) }.take(32)
    }
}
