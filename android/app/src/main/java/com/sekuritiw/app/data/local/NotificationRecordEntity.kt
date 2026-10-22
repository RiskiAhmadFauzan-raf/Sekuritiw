package com.sekuritiw.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room Database untuk menyimpan riwayat analisis notifikasi pengguna secara terenkripsi.
 *
 * Requirements: 10.1, 10.2, 10.3, 10.4, 10.7, 13.3
 */
@Entity(tableName = "notification_records")
data class NotificationRecordEntity(
    /** ID unik notifikasi (Primary Key) */
    @PrimaryKey
    val id: String,

    /** Nama paket aplikasi sumber notifikasi (contoh: "com.whatsapp") */
    val packageName: String,

    /** Nama pengirim yang diekstrak (contoh: nama kontak / nomor HP) */
    val sender: String?,

    /** Nama grup jika pesan berasal dari percakapan grup */
    val groupName: String? = null,

    /** Teks mentah asli notifikasi */
    val rawText: String,

    /** Teks yang sudah dibersihkan dan ter-normalisasi */
    val normalizedText: String,

    /** Kategori ancaman utama (PHISHING, APK_SCAM, SOCIAL_ENGINEERING, SPAM, FINANCIAL_SCAM, BENIGN) */
    val threatCategory: String,

    /** Skor risiko numerik (0.0 - 100.0) */
    val riskScore: Float,

    /** Tingkat risiko (SAFE, CAUTIOUS, DANGEROUS) */
    val riskLevel: String,

    /** Tingkat keparahan dampak (LOW, MEDIUM, HIGH, CRITICAL) */
    val severityRating: String,

    /** Ringkasan penjelasan AI dalam Bahasa Indonesia */
    val reasoning: String,

    /** Timestamp Unix (ms) saat notifikasi diproses */
    val timestamp: Long,

    /** Status apakah riwayat ini sudah tersinkronisasi ke server Laravel backend */
    val isSynced: Boolean = false
)
