package com.sekuritiw.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) untuk manipulasi data riwayat notifikasi pada Room Database.
 *
 * Requirements: 10.4, 10.5, 10.6, 10.7
 */
@Dao
interface NotificationRecordDao {

    /** Menyimpan atau memperbarui riwayat analisis notifikasi */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: NotificationRecordEntity)

    /** Menyimpan daftar riwayat notifikasi secara batch */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<NotificationRecordEntity>)

    /** Memperbarui data riwayat */
    @Update
    suspend fun updateRecord(record: NotificationRecordEntity)

    /** Memperbarui hasil deteksi berdasarkan feedback koreksi pengguna */
    @Query("UPDATE notification_records SET riskLevel = :riskLevel, riskScore = :riskScore, reasoning = :reasoning WHERE id = :id")
    suspend fun updateDetectionResult(id: String, riskLevel: String, riskScore: Float, reasoning: String)

    /** Mengambil seluruh riwayat notifikasi diurutkan dari yang terbaru (Flow untuk Compose reactive UI) */
    @Query("SELECT * FROM notification_records ORDER BY timestamp DESC")
    fun getAllRecordsFlow(): Flow<List<NotificationRecordEntity>>

    /** Mengambil riwayat notifikasi berdasarkan ID */
    @Query("SELECT * FROM notification_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: String): NotificationRecordEntity?

    /** Mengambil riwayat yang belum tersinkronisasi ke server backend */
    @Query("SELECT * FROM notification_records WHERE isSynced = 0 ORDER BY timestamp ASC LIMIT 50")
    suspend fun getUnsyncedRecords(): List<NotificationRecordEntity>

    /** Memperbarui status sinkronisasi ke server */
    @Query("UPDATE notification_records SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    /**
     * Menghapus riwayat yang lebih tua dari timestamp tertentu (Aturan retensi data 30 hari).
     * Requirement: 10.7 - Delete records older than 30 days
     */
    @Query("DELETE FROM notification_records WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteRecordsOlderThan(cutoffTimestamp: Long): Int

    /** Menghitung total jumlah ancaman berbahaya yang pernah diblokir */
    @Query("SELECT COUNT(*) FROM notification_records WHERE riskLevel = 'DANGEROUS'")
    fun getBlockedThreatsCountFlow(): Flow<Int>

    /** Menghapus seluruh riwayat pemindaian */
    @Query("DELETE FROM notification_records")
    suspend fun deleteAllRecords(): Int
}
