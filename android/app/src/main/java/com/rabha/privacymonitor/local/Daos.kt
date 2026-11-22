package com.rabha.privacymonitor.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificationEntity)

    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC LIMIT 200")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("UPDATE notification_history SET read = 1")
    suspend fun markAllRead()

    @Query("DELETE FROM notification_history")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM notification_history WHERE read = 0")
    fun observeUnread(): Flow<Int>
}

@Dao
interface MonitoringDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MonitoringSnapshotEntity)

    @Query("SELECT * FROM monitoring_snapshots WHERE day = :day ORDER BY recordedAt DESC")
    fun observeDay(day: String): Flow<List<MonitoringSnapshotEntity>>

    @Query("SELECT * FROM monitoring_snapshots ORDER BY recordedAt DESC LIMIT 1")
    suspend fun latest(): MonitoringSnapshotEntity?

    @Query("DELETE FROM monitoring_snapshots WHERE recordedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}

@Dao
interface AppUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AppUsageEntity>)

    @Query("SELECT * FROM app_usage_summaries WHERE day = :day ORDER BY foregroundMillis DESC")
    fun observeDay(day: String): Flow<List<AppUsageEntity>>

    @Query("DELETE FROM app_usage_summaries WHERE day < :day")
    suspend fun deleteBefore(day: String)
}

@Dao
interface AdEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AdEventEntity)

    @Query("SELECT COUNT(*) FROM ad_events WHERE campaignId = :campaignId AND type = 'impression' AND occurredAt > :since")
    suspend fun impressionsSince(campaignId: String, since: Long): Int

    @Query("SELECT * FROM ad_events WHERE synced = 0 LIMIT 200")
    suspend fun pendingSync(): List<AdEventEntity>

    @Query("UPDATE ad_events SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)
}

@Dao
interface FileMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FileMetadataEntity)

    @Query("SELECT * FROM file_metadata ORDER BY recordedAt DESC LIMIT 100")
    fun observeAll(): Flow<List<FileMetadataEntity>>

    @Query("DELETE FROM file_metadata")
    suspend fun clear()
}
