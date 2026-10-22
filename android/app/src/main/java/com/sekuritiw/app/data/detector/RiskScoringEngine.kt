package com.sekuritiw.app.data.detector

import com.sekuritiw.app.domain.model.APKDetectionResult
import com.sekuritiw.app.domain.model.FactorType
import com.sekuritiw.app.domain.model.RiskFactor
import com.sekuritiw.app.domain.model.RiskLevel
import com.sekuritiw.app.domain.model.RiskScore
import com.sekuritiw.app.domain.model.SocialEngineeringDetectionResult
import com.sekuritiw.app.domain.model.SourceTrustLevel
import com.sekuritiw.app.domain.model.URLAnalysisResult

/**
 * Mesin Penilaian Risiko (Risk Scoring Engine) yang mengkalkulasi skor risiko numerik (0-100)
 * secara deterministik berdasarkan pembobotan faktor terakumulasi.
 *
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.6, 5.7, 5.8
 */
class RiskScoringEngine {

    /**
     * Menghitung total skor risiko dari hasil seluruh komponen detektor.
     *
     * @param apkResult Hasil deteksi APK
     * @param urlResults Hasil analisis daftar URL
     * @param seResult Hasil pencocokan pola Social Engineering
     * @return Objek RiskScore komprehensif
     */
    fun calculateScore(
        apkResult: APKDetectionResult?,
        urlResults: List<URLAnalysisResult>,
        seResult: SocialEngineeringDetectionResult?
    ): RiskScore {
        val contributors = mutableListOf<RiskFactor>()
        var accumulatedScore = 0.0f

        // 1. Evaluasi Kontribusi Deteksi APK (Bobot 40%, Req 5.3, 2.4, 2.5)
        if (apkResult != null && apkResult.detected) {
            val apkContribution = when (apkResult.trustLevel) {
                SourceTrustLevel.TRUSTED -> 10.0f
                SourceTrustLevel.KNOWN -> 30.0f
                SourceTrustLevel.UNKNOWN -> 45.0f // Req 5.3: minimal +40 poin
                SourceTrustLevel.SUSPICIOUS -> 55.0f // Req 5.3: minimal +40 poin
            }

            val desc = when (apkResult.trustLevel) {
                SourceTrustLevel.TRUSTED -> "Aplikasi dari toko resmi Google Play Store (Aman)."
                SourceTrustLevel.KNOWN -> "Aplikasi dari toko pihak ketiga yang dikenal."
                SourceTrustLevel.UNKNOWN -> "Peringatan: File APK dari sumber tidak dikenal (+45 poin)."
                SourceTrustLevel.SUSPICIOUS -> "BAHAYA: File APK dari sumber mencurigakan (+55 poin)."
            }

            contributors.add(
                RiskFactor(
                    type = FactorType.APK_DETECTION,
                    weight = 0.40f,
                    contribution = apkContribution,
                    description = desc
                )
            )
            accumulatedScore += apkContribution
        }

        // 2. Evaluasi Kontribusi Pemeriksa Tautan (Bobot 30%, Req 3.3, 3.6, 3.7)
        val maxUrlRisk = urlResults.maxByOrNull { it.riskScore }
        if (maxUrlRisk != null && maxUrlRisk.riskScore > 0.0f) {
            val hasOtherThreats = (apkResult != null && apkResult.detected) || (seResult != null && seResult.detected)
            val urlContribution = if (!hasOtherThreats) {
                maxUrlRisk.riskScore
            } else {
                maxOf(maxUrlRisk.riskScore * 0.50f, maxUrlRisk.riskScore - 15.0f)
            }

            val desc = when {
                maxUrlRisk.hasTyposquatting -> "BAHAYA: Tautan meniru domain perbankan resmi (${maxUrlRisk.domain})."
                maxUrlRisk.hasHomograph -> "BAHAYA: Tautan menggunakan karakter homograf penyamaran."
                maxUrlRisk.isShortener -> "Waspada: Tautan menggunakan pemendek URL tersembunyi."
                else -> "Tautan mencurigakan ditemukan (${maxUrlRisk.domain})."
            }

            contributors.add(
                RiskFactor(
                    type = FactorType.URL_ANALYSIS,
                    weight = 0.30f,
                    contribution = urlContribution,
                    description = desc
                )
            )
            accumulatedScore += urlContribution
        }

        // 3. Evaluasi Kontribusi Social Engineering (Bobot 30%, Req 4.3, 4.5, 4.6)
        if (seResult != null && seResult.detected) {
            var seContribution = seResult.riskContributionScore * 0.40f

            // Req 5.3: OTP / Kredensial sensitif menyumbang minimal +40 poin
            if (seResult.containsCredentialRequest) {
                seContribution = maxOf(seContribution, 45.0f)
                contributors.add(
                    RiskFactor(
                        type = FactorType.PATTERN_MATCHING,
                        weight = 0.40f,
                        contribution = 45.0f,
                        description = "KRITIS: Permintaan kode OTP / PIN / Password sensitif (+45 poin)."
                    )
                )
            } else if (seResult.containsFinancialScam) {
                seContribution = maxOf(seContribution, 35.0f)
                contributors.add(
                    RiskFactor(
                        type = FactorType.PATTERN_MATCHING,
                        weight = 0.30f,
                        contribution = 35.0f,
                        description = "BAHAYA: Indikator penipuan finansial / pinjol ilegal (+35 poin)."
                    )
                )
            } else {
                contributors.add(
                    RiskFactor(
                        type = FactorType.PATTERN_MATCHING,
                        weight = 0.20f,
                        contribution = seContribution,
                        description = "Taktik manipulasi psikologis / hadiah palsu terdeteksi."
                    )
                )
            }
            accumulatedScore += seContribution
        }

        // Clamp total skor numerik ke rentang [0.0, 100.0] (Req 5.1)
        val finalScore = minOf(100.0f, maxOf(0.0f, accumulatedScore))
        val riskLevel = RiskLevel.fromScore(finalScore)

        // Estimasi confidence
        val confidence = if (contributors.isNotEmpty()) 0.92f else 0.98f

        return RiskScore(
            score = finalScore,
            level = riskLevel,
            contributors = contributors,
            confidence = confidence
        )
    }
}
