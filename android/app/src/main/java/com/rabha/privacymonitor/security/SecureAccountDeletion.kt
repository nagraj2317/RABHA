package com.rabha.privacymonitor.security

import android.content.Context
import com.rabha.privacymonitor.auth.AuthRepository
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.local.RabhaDatabase
import com.rabha.privacymonitor.local.PrefsStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deletes everything this app holds locally, and asks the server to delete the account
 * through a SECURITY DEFINER function that can only ever act on the caller's own auth.uid().
 */
@Singleton
class SecureAccountDeletion @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: RabhaDatabase,
    private val prefsStore: PrefsStore,
    private val consentManager: ConsentManager,
    private val authRepository: AuthRepository
) {

    suspend fun deleteLocalEverything(): RabhaResult<Unit> = withContext(Dispatchers.IO) {
        try {
            database.notificationDao().clear()
            database.fileMetadataDao().clear()
            val wallpaperDir = File(context.filesDir, "wallpapers")
            wallpaperDir.deleteRecursively()
            prefsStore.clearAll()
            RabhaResult.Available(Unit)
        } catch (e: Exception) {
            RabhaResult.Failed("Local delete failed: ${e.message}")
        }
    }

    suspend fun deleteAccountEverywhere(): RabhaResult<Unit> = withContext(Dispatchers.IO) {
        val remote = consentManager.deleteAccount()
        val local = deleteLocalEverything()
        authRepository.signOut()
        when {
            remote is RabhaResult.Available && local is RabhaResult.Available -> RabhaResult.Available(Unit)
            remote !is RabhaResult.Available -> remote
            else -> local
        }
    }
}
