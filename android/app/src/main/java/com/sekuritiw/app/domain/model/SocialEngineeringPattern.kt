package com.sekuritiw.app.domain.model

/**
 * Kategori pola serangan manipulasi psikologis / rekayasa sosial (Social Engineering).
 * Requirements: 4.1, 4.2, 4.5, 4.6
 */
enum class SocialEngineeringPattern {
    /** Pemberitahuan menang hadiah atau undian palsu */
    PRIZE_LOTTERY,

    /** Permintaan pembayaran atau transfer uang secara mendesak/terburu-buru */
    URGENT_PAYMENT,

    /** Peringatan ancaman pemblokiran rekening atau penonaktifan akun */
    ACCOUNT_SUSPENSION,

    /** Permintaan informasi sensitif (OTP, PIN, Password, Nomor Kartu Kredit/CVV) */
    SENSITIVE_CREDENTIAL_REQUEST,

    /** Taktik tekanan waktu (penawaran sangat terbatas / harus diproses hari ini) */
    TIME_PRESSURE,

    /** Peniruan identitas instansi resmi (Bank, Pemerintah, Kepolisian, Kurir) */
    IMPERSONATION,

    /** Indikator penipuan finansial (investasi untung pasti, pinjol tanpa syarat, kripto) */
    FINANCIAL_SCAM
}

/**
 * Hasil analisis pencocokan pola Social Engineering.
 * Requirements: 4.1 - 4.7
 */
data class SocialEngineeringDetectionResult(
    val detected: Boolean,
    val matchedPatterns: List<SocialEngineeringPattern>,
    val detectedPhrases: List<String>,
    val containsCredentialRequest: Boolean,
    val containsFinancialScam: Boolean,
    val riskContributionScore: Float
)
