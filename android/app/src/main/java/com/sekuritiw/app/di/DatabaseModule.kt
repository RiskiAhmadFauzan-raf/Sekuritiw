package com.sekuritiw.app.di

import android.content.Context
import androidx.room.Room
import com.sekuritiw.app.data.local.NotificationRecordDao
import com.sekuritiw.app.data.local.SekuritiwDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Dependency Injection Module untuk menyediakan instance SekuritiwDatabase dan DAO.
 *
 * Requirements: 10.1, 10.8
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Menyediakan instance SekuritiwDatabase (Room Database).
     */
    @Provides
    @Singleton
    fun provideSekuritiwDatabase(
        @ApplicationContext context: Context
    ): SekuritiwDatabase {
        return Room.databaseBuilder(
            context,
            SekuritiwDatabase::class.java,
            SekuritiwDatabase.DATABASE_NAME
        )
            .addMigrations(SekuritiwDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Menyediakan instance NotificationRecordDao.
     */
    @Provides
    @Singleton
    fun provideNotificationRecordDao(
        database: SekuritiwDatabase
    ): NotificationRecordDao {
        return database.notificationRecordDao()
    }
}
