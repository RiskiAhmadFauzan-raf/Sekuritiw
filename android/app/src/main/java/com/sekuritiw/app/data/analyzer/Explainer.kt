package com.sekuritiw.app.data.analyzer

import com.sekuritiw.app.domain.model.APKDetectionResult
import com.sekuritiw.app.domain.model.Explanation
import com.sekuritiw.app.domain.model.RiskLevel
import com.sekuritiw.app.domain.model.RiskScore
import com.sekuritiw.app.domain.model.SocialEngineeringDetectionResult
import com.sekuritiw.app.domain.model.SocialEngineeringPattern
import com.sekuritiw.app.domain.model.SourceTrustLevel
import com.sekuritiw.app.domain.model.SuspiciousPattern
import com.sekuritiw.app.domain.model.URLAnalysisResult

/**
 * Modul Explainer yang menghasilkan penalaran keputusan AI dalam Bahasa Indonesia
 * yang mudah dipahami oleh pengguna umum dari semua tingkat literasi digital.
 *
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 24.6
 */
class Explainer {

    /**
     * Menghasilkan objek Explanation berdasarkan hasil penilaian skor risiko dan rincian deteksi.
     *
     * @param riskScore Objek RiskScore hasil dari RiskScoringEngine
     * @param apkResult Hasil deteksi APK (opsional)
     * @param urlResults Hasil analisis URL (opsional)
     * @param seResult Hasil pencocokan pola Social Engineering (opsional)
     * @return Objek Explanation komprehensif Bahasa Indonesia
     */
    fun generateExplanation(
        riskScore: RiskScore,
        apkResult: APKDetectionResult? = null,
        urlResults: List<URLAnalysisResult> = emptyList(),
        seResult: SocialEngineeringDetectionResult? = null
    ): Explanation {
        val summary = generateSummary(riskScore.level)
        val riskFactorList = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        var educationalNote = ""

        // 1. Ekstraksi Faktor Risiko & Rekomendasi Spesifik APK (Req 6.5)
        if (apkResult != null && apkResult.detected) {
            when (apkResult.trustLevel) {
                SourceTrustLevel.SUSPICIOUS, SourceTrustLevel.UNKNOWN -> {
                    val fileName = apkResult.fileName ?: "aplikasi tidak dikenal"
                    riskFactorList.add("Terdeteksi permintaan mengunduh/memasang file APK luar ($fileName).")
                    recommendations.add("🚫 Jangan menginstal file APK tersebut.")
                    recommendations.add("Hapus pesan ini dan gunakan hanya Google Play Store resmi.")
                    educationalNote = "Tahukah kamu: Penipu sering menyamarkan file aplikasi berbahaya (.apk) sebagai dokumen kurir paket, undangan nikah, atau surat tilang untuk mencuri OTP SMS bank kamu."
                }
                SourceTrustLevel.KNOWN -> {
                    riskFactorList.add("Tautan merujuk ke toko aplikasi pihak ketiga (${apkResult.source}).")
                    recommendations.add("Pastikan sumber aplikasi benar-benar terpercaya sebelum memasang.")
                }
                SourceTrustLevel.TRUSTED -> {
                    riskFactorList.add("Tautan merujuk ke toko resmi Google Play Store.")
                }
            }
        }

        // 2. Ekstraksi Faktor Risiko & Rekomendasi Spesifik URL (Req 6.5)
        for (urlRes in urlResults) {
            if (urlRes.riskScore > 35.0f) {
                var hasSpecificPattern = false
                for (pattern in urlRes.detectedPatterns) {
                    hasSpecificPattern = true
                    when (pattern) {
                        SuspiciousPattern.FAKE_BANKING_DOMAIN, SuspiciousPattern.TYPOSQUATTING -> {
                            riskFactorList.add("Tautan meniru alamat situs bank resmi (Domain: ${urlRes.domain}).")
                            recommendations.add("🚫 JANGAN mengklik tautan tersebut.")
                            recommendations.add("Buka aplikasi bank resmi kamu secara langsung dari menu HP.")
                            educationalNote = "Tips Keamanan: Bank resmi selalu menggunakan domain sah (seperti .co.id) dan tidak pernah mengirimkan link login melalui pesan singkat/WhatsApp."
                        }
                        SuspiciousPattern.HOMOGRAPH_ATTACK -> {
                            riskFactorList.add("Tautan menggunakan penyamaran huruf mirip/homograf (${urlRes.domain}).")
                            recommendations.add("Waspada penyamaran karakter visual pada tautan.")
                        }
                        SuspiciousPattern.URL_SHORTENER -> {
                            riskFactorList.add("Tautan menggunakan pemendek URL tersembunyi (${urlRes.domain}).")
                            recommendations.add("Berhati-hatilah dengan tautan yang disembunyikan.")
                        }
                        SuspiciousPattern.IP_ADDRESS -> {
                            riskFactorList.add("Tautan menggunakan IP mentah (${urlRes.domain}).")
                            recommendations.add("Hindari mengklik tautan tanpa nama domain resmi.")
                        }
                        SuspiciousPattern.UNUSUAL_TLD -> {
                            riskFactorList.add("Tautan menggunakan akhiran domain tidak biasa (${urlRes.domain}).")
                        }
                        else -> {}
                    }
                }
                // Jika model AI mendeteksi risiko tapi tidak ada pola heuristik spesifik
                // Tampilkan penjelasan berbasis skor AI (jujur dan informatif)
                if (!hasSpecificPattern) {
                    val scoreInt = urlRes.riskScore.toInt()
                    riskFactorList.add("Model AI mendeteksi pola URL yang tidak biasa pada domain ${urlRes.domain} (Skor: $scoreInt/100).")
                    recommendations.add("Periksa kembali keabsahan sumber tautan ini sebelum mengaksesnya.")
                }
            }
        }

        // 3. Ekstraksi Faktor Risiko & Rekomendasi Spesifik Social Engineering (Req 6.6)
        if (seResult != null && seResult.detected) {
            for (pattern in seResult.matchedPatterns) {
                when (pattern) {
                    SocialEngineeringPattern.SENSITIVE_CREDENTIAL_REQUEST -> {
                        riskFactorList.add("KRITIS: Pesan meminta kode OTP, PIN, atau kata sandi pribadi.")
                        recommendations.add("🛑 JANGAN PERNAH memberikan kode OTP / PIN kepada siapapun!")
                        recommendations.add("Petugas resmi tidak pernah meminta kode OTP atau PIN Anda.")
                        educationalNote = "Aturan Emas: Kode OTP adalah kunci rahasia transaksi Anda. Siapapun yang meminta OTP Anda dipastikan adalah penipu!"
                    }
                    SocialEngineeringPattern.PRIZE_LOTTERY -> {
                        riskFactorList.add("Terdeteksi modus pemberitahuan menang undian/hadiah palsu.")
                        recommendations.add("Jangan percaya klaim kemenangan undian yang tidak pernah Anda ikuti.")
                        recommendations.add("Verifikasi informasi ke saluran resmi instansi terkait.")
                        educationalNote = "Tahukah kamu: Penipuan undian palsu biasanya meminta Anda membayar 'biaya pemrosesan' atau 'pajak pemenang' terlebih dahulu."
                    }
                    SocialEngineeringPattern.FINANCIAL_SCAM -> {
                        riskFactorList.add("Terdeteksi penawaran investasi tidak wajar atau pinjol ilegal.")
                        recommendations.add("Pastikan lembaga keuangan sudah terdaftar resmi di OJK (ojk.go.id).")
                        educationalNote = "Tips: Jangan tergiur jaminan keuntungan pasti tinggi atau pinjaman instan tanpa syarat."
                    }
                    SocialEngineeringPattern.URGENT_PAYMENT, SocialEngineeringPattern.TIME_PRESSURE -> {
                        riskFactorList.add("Pesan menggunakan taktik tekanan waktu untuk membuat Anda panik.")
                        recommendations.add("Tetap tenang dan jangan terburu-buru melakukan transfer uang.")
                    }
                    SocialEngineeringPattern.ACCOUNT_SUSPENSION -> {
                        riskFactorList.add("Pesan mengancam pemblokiran akun/rekening secara sepihak.")
                        recommendations.add("Cek status akun Anda melalui aplikasi resmi, bukan via link pesan ini.")
                    }
                    SocialEngineeringPattern.IMPERSONATION -> {
                        riskFactorList.add("Pesan meniru identitas instansi resmi atau pihak berwenang.")
                        recommendations.add("Periksa nomor pengirim dan verifikasi ke customer service resmi.")
                    }
                }
            }
        }

        // Fallback rekomendasi jika belum ada
        if (recommendations.isEmpty()) {
            when (riskScore.level) {
                RiskLevel.SAFE -> {
                    recommendations.add("Konten ini relatif aman. Tetap jaga kerahasiaan data pribadi Anda.")
                }
                RiskLevel.CAUTIOUS -> {
                    recommendations.add("Periksa kembali keabsahan pengirim sebelum membalas atau mengklik tautan.")
                }
                RiskLevel.DANGEROUS -> {
                    recommendations.add("Hapus pesan ini dan laporkan sebagai indikasi penipuan.")
                }
            }
        }

        if (educationalNote.isBlank()) {
            educationalNote = "Tips Keamanan Digital: Selalu periksa pengirim pesan dan jangan membagikan informasi sensitif seperti OTP atau PIN."
        }

        val detailedReasoning = generateDetailedReasoning(riskScore, riskFactorList)

        return Explanation(
            summary = summary,
            riskFactors = riskFactorList.distinct(),
            detailedReasoning = detailedReasoning,
            recommendations = recommendations.distinct(),
            educationalNote = educationalNote
        )
    }

    private fun generateSummary(level: RiskLevel): String {
        return when (level) {
            RiskLevel.SAFE -> "Konten ini tampak aman. Tidak ditemukan indikator ancaman bahaya."
            RiskLevel.CAUTIOUS -> "Waspada! Ditemukan indikator mencurigakan pada konten ini."
            RiskLevel.DANGEROUS -> "BAHAYA! Konten ini kemungkinan besar merupakan upaya penipuan siber."
        }
    }

    private fun generateDetailedReasoning(riskScore: RiskScore, riskFactors: List<String>): String {
        if (riskFactors.isEmpty()) {
            return "Hasil analisis menunjukkan bahwa pesan tidak mengandung kata kunci penipuan, tautan berbahaya, atau instruksi pemasangan aplikasi palsu. Skor risiko saat ini adalah ${riskScore.score.toInt()}/100 (Aman)."
        }

        val factorsFormatted = riskFactors.joinToString("; ")
        return "Sistem mendeteksi skor risiko sebesar ${riskScore.score.toInt()}/100 (${riskScore.level.name}) karena ditemukannya faktor berikut: $factorsFormatted. Harap berhati-hati dan ikuti langkah rekomendasi keamanan."
    }
}
