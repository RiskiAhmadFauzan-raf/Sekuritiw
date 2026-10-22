package com.sekuritiw.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room Database utama SEKURITIW yang dienkripsi menggunakan SQLCipher (AES-256).
 *
 * Requirements: 10.1, 10.8, 13.3
 */
@Database(
    entities = [NotificationRecordEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SekuritiwDatabase : RoomDatabase() {

    /** Mengakses Data Access Object (DAO) riwayat notifikasi */
    abstract fun notificationRecordDao(): NotificationRecordDao

    companion object {
        const val DATABASE_NAME = "sekuritiw_encrypted.db"

        /** Migrasi dari versi 1 ke 2: Penambahan kolom groupName */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notification_records ADD COLUMN groupName TEXT DEFAULT NULL")
            }
        }
    }
}
