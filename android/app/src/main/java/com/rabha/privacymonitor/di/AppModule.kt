package com.rabha.privacymonitor.di

import android.content.Context
import androidx.room.Room
import com.rabha.privacymonitor.local.AdEventDao
import com.rabha.privacymonitor.local.AppUsageDao
import com.rabha.privacymonitor.local.FileMetadataDao
import com.rabha.privacymonitor.local.MonitoringDao
import com.rabha.privacymonitor.local.NotificationDao
import com.rabha.privacymonitor.local.RabhaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** Unqualified Context so repositories can inject plain Context without repeating the qualifier. */
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RabhaDatabase =
        Room.databaseBuilder(context, RabhaDatabase::class.java, "rabha.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideNotificationDao(database: RabhaDatabase): NotificationDao = database.notificationDao()

    @Provides
    fun provideMonitoringDao(database: RabhaDatabase): MonitoringDao = database.monitoringDao()

    @Provides
    fun provideAppUsageDao(database: RabhaDatabase): AppUsageDao = database.appUsageDao()

    @Provides
    fun provideAdEventDao(database: RabhaDatabase): AdEventDao = database.adEventDao()

    @Provides
    fun provideFileMetadataDao(database: RabhaDatabase): FileMetadataDao = database.fileMetadataDao()
}
