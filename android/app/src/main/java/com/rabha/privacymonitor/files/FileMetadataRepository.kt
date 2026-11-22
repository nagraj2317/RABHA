package com.rabha.privacymonitor.files

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.runCatchingResult
import com.rabha.privacymonitor.local.FileMetadataDao
import com.rabha.privacymonitor.local.FileMetadataEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Records metadata for files the user explicitly picked through the Storage Access Framework.
 * No directory is scanned and no other app's private files are ever touched.
 */
@Singleton
class FileMetadataRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: FileMetadataDao
) {

    fun observe(): Flow<List<FileMetadataEntity>> = dao.observeAll()

    suspend fun record(uri: Uri, direction: String = "imported"): RabhaResult<FileMetadataEntity> =
        withContext(Dispatchers.IO) {
            runCatchingResult {
                val resolver = context.contentResolver
                var name = uri.lastPathSegment ?: "unknown"
                var size = 0L
                resolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: name
                        if (sizeIndex >= 0) size = cursor.getLong(sizeIndex)
                    }
                }
                val mime = resolver.getType(uri) ?: "application/octet-stream"
                val entity = FileMetadataEntity(
                    displayName = name,
                    mimeType = mime,
                    sizeBytes = size,
                    direction = direction,
                    status = "picked",
                    recordedAt = System.currentTimeMillis()
                )
                dao.insert(entity)
                entity
            }
        }

    suspend fun clear() = dao.clear()
}
