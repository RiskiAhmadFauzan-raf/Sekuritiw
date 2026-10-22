package com.sekuritiw.app.data.detector

import com.sekuritiw.app.domain.model.PreprocessedText
import com.sekuritiw.app.domain.model.SocialEngineeringDetectionResult
import com.sekuritiw.app.domain.model.SocialEngineeringPattern

/**
 * Pendeteksi pola rekayasa sosial (Social Engineering Matcher) yang menganalisis
 * teks untuk taktik manipulasi psikologis, penipuan finansial, dan permintaan kredensial.
 *
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 24.2
 */
class SocialEngineeringMatcher {

    companion object {
        /** Pola 1: Hadiah / Undian Palsu */
        private val PRIZE_PATTERNS = listOf(
            "menang hadiah", "selamat anda", "undian resmi", "pemenang ke",
            "dapat hadiah", "bonus saldo", "dapat mobil", "dapat uang"
        )

        /** Pola 2: Pembayaran Mendesak */
        private val URGENT_PATTERNS = listOf(
            "segera transfer", "bayar sekarang", "jatuh tempo hari ini",
            "transfer sebelum", "sebelum jam", "tunggakan pajak", "denda administrasi"
        )

        /** Pola 3: Pemblokiran Akun / Rekening */
        private val SUSPENSION_PATTERNS = listOf(
            "akun diblokir", "rekening anda dibekukan", "verifikasi akun",
            "akun dinonaktifkan", "akses anda ditutup", "perbarui data akun"
        )

        /** Pola 4: Permintaan Kredensial Sensitif (Req 4.5) */
        private val CREDENTIAL_PATTERNS = listOf(
            "kode otp", "kirim otp", "bagikan otp", "berikan otp",
            "masukkan password", "kirim pin", "pin atm", "nomor kartu kredit",
            "kode cvv", "3 digit dibelakang"
        )

        /** Pola 5: Tekanan Waktu */
        private val TIME_PRESSURE_PATTERNS = listOf(
            "hari ini saja", "terbatas untuk", "sisa 5 menit",
            "berlaku 1 jam", "kesempatan terakhir"
        )

        /** Pola 6: Peniruan Identitas (Impersonation) */
        private val IMPERSONATION_PATTERNS = listOf(
            "dari bank", "pihak kepolisian", "customer service bca",
            "petugas mandiri", "kurir j&t", "call center", "pihak perpajakan"
        )

        /** Pola 7: Penipuan Finansial & Pinjol Ilegal (Req 4.6) */
        private val FINANCIAL_SCAM_PATTERNS = listOf(
            "pasti untung", "dijamin profit", "pinjaman tanpa survey",
            "pinjol langsung cair", "investasi crypto", "bitcoin gratis",
            "modal 100rb jadi 10juta", "bunga 0% tanpa jaminan"
        )
    }

    /**
     * Menganalisis PreprocessedText dan menghasilkan SocialEngineeringDetectionResult.
     *
     * Requirement 4.3: Multiple pattern indicators increase Risk_Level proportionally
     * Requirement 4.5: Requesting OTP, password, PIN, or card number -> Risk >= 71
     */
    fun match(preprocessed: PreprocessedText): SocialEngineeringDetectionResult {
        val textLower = preprocessed.normalized

        val matchedPatterns = mutableSetOf<SocialEngineeringPattern>()
        val detectedPhrases = mutableListOf<String>()

        // 1. Cek Hadiah / Undian
        checkPatterns(textLower, PRIZE_PATTERNS, SocialEngineeringPattern.PRIZE_LOTTERY, matchedPatterns, detectedPhrases)

        // 2. Cek Pembayaran Mendesak
        checkPatterns(textLower, URGENT_PATTERNS, SocialEngineeringPattern.URGENT_PAYMENT, matchedPatterns, detectedPhrases)

        // 3. Cek Pemblokiran Akun
        checkPatterns(textLower, SUSPENSION_PATTERNS, SocialEngineeringPattern.ACCOUNT_SUSPENSION, matchedPatterns, detectedPhrases)

        // 4. Cek Permintaan Kredensial (OTP, Password, PIN)
        val hasCredentialReq = checkPatterns(textLower, CREDENTIAL_PATTERNS, SocialEngineeringPattern.SENSITIVE_CREDENTIAL_REQUEST, matchedPatterns, detectedPhrases)

        // 5. Cek Tekanan Waktu
        checkPatterns(textLower, TIME_PRESSURE_PATTERNS, SocialEngineeringPattern.TIME_PRESSURE, matchedPatterns, detectedPhrases)

        // 6. Cek Peniruan Identitas
        checkPatterns(textLower, IMPERSONATION_PATTERNS, SocialEngineeringPattern.IMPERSONATION, matchedPatterns, detectedPhrases)

        // 7. Cek Penipuan Finansial (Req 4.6)
        val hasFinancialScam = checkPatterns(textLower, FINANCIAL_SCAM_PATTERNS, SocialEngineeringPattern.FINANCIAL_SCAM, matchedPatterns, detectedPhrases)

        val isDetected = matchedPatterns.isNotEmpty()

        if (!isDetected) {
            return SocialEngineeringDetectionResult(
                detected = false,
                matchedPatterns = emptyList(),
                detectedPhrases = emptyList(),
                containsCredentialRequest = false,
                containsFinancialScam = false,
                riskContributionScore = 0.0f
            )
        }

        // Kalkulasi skor risikoproporsional (Req 4.3)
        var score = matchedPatterns.size * 20.0f

        // Requirement 4.5: Jika meminta OTP/PIN/Password -> Skor minimal 75.0f (DANGEROUS)
        if (hasCredentialReq) {
            score = maxOf(score, 85.0f)
        }

        // Requirement 4.6: Jika indikator penipuan finansial -> Tambahan poin
        if (hasFinancialScam) {
            score = maxOf(score, 75.0f)
        }

        val finalScore = minOf(100.0f, score)

        return SocialEngineeringDetectionResult(
            detected = true,
            matchedPatterns = matchedPatterns.toList(),
            detectedPhrases = detectedPhrases,
            containsCredentialRequest = hasCredentialReq,
            containsFinancialScam = hasFinancialScam,
            riskContributionScore = finalScore
        )
    }

    private fun checkPatterns(
        text: String,
        patterns: List<String>,
        patternType: SocialEngineeringPattern,
        matchedPatterns: MutableSet<SocialEngineeringPattern>,
        detectedPhrases: MutableList<String>
    ): Boolean {
        var found = false
        for (pattern in patterns) {
            if (text.contains(pattern)) {
                matchedPatterns.add(patternType)
                if (!detectedPhrases.contains(pattern)) {
                    detectedPhrases.add(pattern)
                }
                found = true
            }
        }
        return found
    }
}
