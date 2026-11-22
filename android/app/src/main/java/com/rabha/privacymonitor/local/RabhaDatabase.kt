package com.rabha.privacymonitor.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        NotificationEntity::class,
        MonitoringSnapshotEntity::class,
        AppUsageEntity::class,
        AdEventEntity::class,
        FileMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RabhaDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun monitoringDao(): MonitoringDao
    abstract fun appUsageDao(): AppUsageDao
    abstract fun adEventDao(): AdEventDao
    abstract fun fileMetadataDao(): FileMetadataDao
}
