package com.sekuritiw.app.domain.model

/**
 * Jenis-jenis faktor kontributor risiko dalam penilaian ancaman.
 * Requirements: 5.3, 5.8
 */
enum class FactorType {
    /** Hasil klasifikasi model pembelajaran mesin / AI */
    AI_CLASSIFICATION,

    /** Hasil analisis keamanan tautan (Link Checker) */
    URL_ANALYSIS,

    /** Hasil deteksi file atau instruksi APK (APK Detector) */
    APK_DETECTION,

    /** Hasil pencocokan pola rekayasa sosial (Social Engineering Matcher) */
    PATTERN_MATCHING,

    /** Reputasi pengirim notifikasi */
    SENDER_REPUTATION
}

/**
 * Rincian kontribusi satu faktor terhadap total skor risiko.
 * Requirements: 5.3, 6.2
 */
data class RiskFactor(
    val type: FactorType,
    val weight: Float,
    val contribution: Float,
    val description: String
)

/**
 * Objek kalkulasi skor risiko akhir beserta tingkat bahaya dan kontributornya.
 * Requirements: 5.1 - 5.8
 */
data class RiskScore(
    /** Skor numerik antara 0.0 sampai 100.0 */
    val score: Float,

    /** Kategori tingkat risiko (SAFE, CAUTIOUS, DANGEROUS) */
    val level: RiskLevel,

    /** Daftar faktor kontributor risiko yang ditemukan */
    val contributors: List<RiskFactor>,

    /** Tingkat keyakinan analisis (0.0 - 1.0) */
    val confidence: Float
)
