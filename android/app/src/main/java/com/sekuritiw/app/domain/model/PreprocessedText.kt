package com.sekuritiw.app.domain.model

/**
 * Data class hasil pemrosesan awal (preprocessing) teks pesan notifikasi.
 * Menyimpan teks asli, teks yang sudah dibersihkan dan dinormalisasi,
 * token-token, serta indikator awal keberadaan URL atau referensi APK.
 *
 * Requirements: 4.4, 7.4, 24.1, 24.5
 */
data class PreprocessedText(
    /** Teks asli sebelum diproses */
    val original: String,

    /** Teks setelah pembersihan karakter kontrol & spasi berlebih */
    val cleaned: String,

    /** Teks setelah normalisasi Unicode (NFKC) dan lowercase */
    val normalized: String,

    /** Daftar kata/token hasil pembagian teks */
    val tokens: List<String>,

    /** Daftar URL yang berhasil diekstrak */
    val extractedUrls: List<String>,

    /** Daftar referensi/nama file APK yang berhasil diekstrak */
    val extractedApks: List<String>,

    /** Apakah teks mengandung minimal 1 URL */
    val containsURL: Boolean = extractedUrls.isNotEmpty(),

    /** Apakah teks mengandung indikator file APK */
    val containsAPK: Boolean = extractedApks.isNotEmpty()
)
