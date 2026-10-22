package com.sekuritiw.app.domain.model

/**
 * Data class merepresentasikan konten notifikasi yang telah diekstrak dari
 * StatusBarNotification Android. Digunakan sebagai input utama pipeline analisis.
 *
 * Requirement: 1.6 - THE Notification_Analyzer SHALL extract sender information,
 * message body, and timestamp
 */
data class NotificationContent(
    /** ID unik notifikasi, gabungan dari packageName + notification ID */
    val id: String,

    /** Nama paket aplikasi sumber notifikasi (contoh: "com.whatsapp") */
    val packageName: String,

    /** Judul notifikasi (umumnya nama pengirim) */
    val title: String?,

    /** Teks ringkas notifikasi */
    val text: String?,

    /** Teks panjang notifikasi jika menggunakan BigTextStyle */
    val bigText: String?,

    /** Teks info tambahan pada notifikasi */
    val subText: String?,

    /** Timestamp Unix (ms) saat notifikasi diposting */
    val timestamp: Long,

    /** Nama pengirim yang diekstrak (untuk messaging notifications) */
    val sender: String?,

    /** Apakah notifikasi berasal dari aplikasi pesan (WhatsApp, SMS, dll.) */
    val isMessagingNotification: Boolean = false,

    /** Nama grup jika notifikasi berasal dari percakapan grup (WhatsApp, Telegram, dll.) */
    val groupName: String? = null,

    /** Apakah notifikasi terdeteksi sebagai obrolan grup */
    val isGroup: Boolean = false
) {
    /**
     * Menggabungkan semua konten teks yang tersedia menjadi satu string
     * untuk dianalisis oleh AI Engine. Prioritas: bigText > text > subText.
     *
     * Requirement: 1.6 - Ekstraksi seluruh field teks dari notifikasi
     */
    fun getFullText(): String {
        val bodyText = when {
            !bigText.isNullOrBlank() -> bigText
            !text.isNullOrBlank() -> text
            else -> ""
        }
        return if (bodyText.isNotBlank()) {
            bodyText.trim()
        } else {
            listOfNotNull(title, subText).joinToString(" ").trim()
        }
    }

    /**
     * Mengembalikan true jika notifikasi memiliki cukup konten teks untuk dianalisis.
     * Requirement: 1.7 - Jika konten tidak dapat diakses, catat error dan lanjutkan
     */
    fun hasAnalyzableContent(): Boolean {
        return getFullText().isNotBlank()
    }
}
