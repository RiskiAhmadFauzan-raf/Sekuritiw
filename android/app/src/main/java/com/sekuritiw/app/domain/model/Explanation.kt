package com.sekuritiw.app.domain.model

/**
 * Objek penjelasan hasil keputusan AI yang dapat dipahami oleh manusia dalam Bahasa Indonesia.
 * Requirements: 6.1 - 6.8
 */
data class Explanation(
    /** Ringkasan hasil analisis singkat (1 kalimat) */
    val summary: String,

    /** Daftar spesifik faktor risiko yang ditemukan */
    val riskFactors: List<String>,

    /** Penjelasan mendalam dengan bahasa non-teknis yang mudah dipahami */
    val detailedReasoning: String,

    /** Daftar rekomendasi tindakan konkret bagi pengguna */
    val recommendations: List<String>,

    /** Edukasi / wawasan singkat mengenai taktik penipuan yang terdeteksi */
    val educationalNote: String
)
