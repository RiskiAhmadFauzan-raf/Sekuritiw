package com.sekuritiw.app.domain.model

/**
 * Pola-pola mencurigakan pada URL yang dapat diidentifikasi.
 * Requirement: 3.3, 3.6, 3.7
 */
enum class SuspiciousPattern {
    /** Pemendek URL (bit.ly, tinyurl, dll.) */
    URL_SHORTENER,

    /** Alamat IP mentah yang digunakan sebagai domain (http://192.168.1.1) */
    IP_ADDRESS,

    /** TLD tidak biasa atau berisiko tinggi (.xyz, .top, .site, .club, dll.) */
    UNUSUAL_TLD,

    /** Peniruan nama domain populer/bank dengan kemiripan ejaan (Levenshtein distance) */
    TYPOSQUATTING,

    /** Serangan karakter mirip/homograf (pencampuran abjad Latin & Sirilik) */
    HOMOGRAPH_ATTACK,

    /** Jumlah sub-domain berlebihan (contoh: login.bca.pembayaran.verifikasi.com) */
    EXCESSIVE_SUBDOMAINS,

    /** Karakter ter-encode berlebihan (%20, %2F, %00) */
    ENCODED_CHARS,

    /** Peniruan domain resmi perbankan Indonesia */
    FAKE_BANKING_DOMAIN,

    /** Peniruan domain resmi pemerintah Indonesia (.go.id) */
    FAKE_GOV_DOMAIN
}

/**
 * Hasil analisis detail untuk 1 URL yang diperiksa.
 * Requirement: 3.1 - 3.8
 */
data class URLAnalysisResult(
    val url: String,
    val domain: String,
    val isShortener: Boolean,
    val isSuspiciousTLD: Boolean,
    val hasTyposquatting: Boolean,
    val hasHomograph: Boolean,
    val reputationScore: Float? = null, // null jika sedang luring
    val detectedPatterns: List<SuspiciousPattern>,
    val riskLevel: RiskLevel,
    val riskScore: Float
)
