package com.sekuritiw.app.domain.model

/**
 * Klasifikasi tingkat risiko konten yang dianalisis oleh SEKURITIW.
 *
 * Requirements: 5.2
 * - SAFE: 0 - 30
 * - CAUTIOUS: 31 - 70
 * - DANGEROUS: 71 - 100
 */
enum class RiskLevel {
    /** Aman (Skor 0-30) - Tidak ada indikator ancaman signifikan */
    SAFE,

    /** Waspada (Skor 31-70) - Ditemukan indikator mencurigakan yang memerlukan perhatian */
    CAUTIOUS,

    /** Bahaya (Skor 71-100) - Terindikasi kuat sebagai ancaman penipuan/phishing/malware */
    DANGEROUS;

    companion object {
        /**
         * Mengonversi skor numerik (0-100) menjadi RiskLevel.
         */
        fun fromScore(score: Float): RiskLevel {
            return when {
                score <= 30.0f -> SAFE
                score <= 70.0f -> CAUTIOUS
                else -> DANGEROUS
            }
        }
    }
}
