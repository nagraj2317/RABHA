package com.rabha.privacymonitor.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "notification_history")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val category: String,
    val timestamp: Long,
    val read: Boolean = false
)

@Entity(tableName = "monitoring_snapshots", indices = [Index("day")])
data class MonitoringSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val day: String,
    val rxBytes: Long,
    val txBytes: Long,
    val screenMillis: Long,
    val networkType: String,
    val recordedAt: Long
)

@Entity(tableName = "app_usage_summaries", indices = [Index("day")])
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val day: String,
    val packageName: String,
    val appLabel: String,
    val foregroundMillis: Long
)

@Entity(tableName = "ad_events")
data class AdEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val campaignId: String,
    val creativeId: String?,
    val type: String,
    val occurredAt: Long,
    val synced: Boolean = false
)

@Entity(tableName = "file_metadata")
data class FileMetadataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val direction: String,
    val status: String,
    val recordedAt: Long
)
