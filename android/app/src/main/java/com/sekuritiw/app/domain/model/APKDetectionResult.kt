package com.sekuritiw.app.domain.model

/**
 * Metode pengiriman/referensi file APK yang terdeteksi dalam pesan/notifikasi.
 * Requirement: 2.2, 2.3
 */
enum class APKDeliveryMethod {
    /** Lampiran file secara langsung (contoh: file .apk di WhatsApp) */
    DIRECT_ATTACHMENT,

    /** Tautan unduhan langsung yang berakhiran .apk */
    DOWNLOAD_LINK,

    /** Referensi terenkode atau pola tersembunyi */
    ENCODED_REFERENCE,

    /** Frasa instruksi/permintaan instalasi Bahasa Indonesia */
    INSTALLATION_REQUEST,

    /** Tidak ada APK terdeteksi */
    NONE
}

/**
 * Tingkat kepercayaan sumber file APK atau domain asal pengiriman.
 * Requirement: 2.5
 */
enum class SourceTrustLevel {
    /** Toko aplikasi resmi terpercaya (Google Play Store) */
    TRUSTED,

    /** Toko aplikasi pihak ketiga yang dikenal (Samsung Galaxy Store, Huawei AppGallery) */
    KNOWN,

    /** Sumber tidak dikenal atau tidak terverifikasi */
    UNKNOWN,

    /** Sumber yang memiliki indikator pola penipuan atau domain mencurigakan */
    SUSPICIOUS
}

/**
 * Metadata detail dari referensi APK yang berhasil diekstrak.
 * Requirement: 2.2
 */
data class APKMetadata(
    val fileName: String?,
    val source: String?,
    val deliveryMethod: APKDeliveryMethod
)

/**
 * Hasil deteksi komprehensif dari APK Detector.
 * Requirement: 2.1 - 2.7
 */
data class APKDetectionResult(
    val detected: Boolean,
    val fileName: String?,
    val source: String?,
    val deliveryMethod: APKDeliveryMethod,
    val trustLevel: SourceTrustLevel,
    val matchedPhrases: List<String> = emptyList()
)
