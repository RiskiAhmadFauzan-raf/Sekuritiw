package com.sekuritiw.app.domain.model

/**
 * Kategori formal ancaman siber yang teridentifikasi.
 * Requirements: 8.1, 8.2, 8.4, 8.5
 */
enum class ThreatCategory {
    /** Penipuan pencurian identitas / tautan palsu perbankan */
    PHISHING,

    /** Penipuan pemasangan file APK berbahaya / pengunduhan aplikasi luar */
    APK_SCAM,

    /** Taktik rekayasa sosial / manipulasi psikologis / hadiah palsu */
    SOCIAL_ENGINEERING,

    /** Pesan promosi berlebihan / spam massal */
    SPAM,

    /** Penipuan finansial, pinjaman online ilegal, investasi bodong, kripto palsu */
    FINANCIAL_SCAM,

    /** Konten wajar / tidak berbahaya */
    BENIGN,

    /** Kategori tidak dikenal / belum dapat diklasifikasikan */
    UNKNOWN
}

/**
 * Rating keparahan dampak ancaman (Severity Rating).
 * Requirement: 8.3
 */
enum class SeverityRating {
    /** Keparahan Rendah (Spam, Promosi) */
    LOW,

    /** Keparahan Sedang (Tautan mencurigakan tanpa ancaman kredensial langsung) */
    MEDIUM,

    /** Keparahan Tinggi (Tautan phishing bank, penipuan finansial) */
    HIGH,

    /** Keparahan Kritis (Permintaan OTP, PIN, APK malware pencuri SMS) */
    CRITICAL
}

/**
 * Hasil klasifikasi pengelompokan ancaman formal (mendukung multi-label).
 * Requirements: 8.1 - 8.6
 */
data class ThreatClassificationResult(
    val primaryCategory: ThreatCategory,
    val secondaryCategories: List<ThreatCategory>,
    val severityRating: SeverityRating,
    val confidence: Float
)
