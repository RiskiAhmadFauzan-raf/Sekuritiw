package com.sekuritiw.app.data.detector

import com.sekuritiw.app.domain.model.APKDetectionResult
import com.sekuritiw.app.domain.model.RiskLevel
import com.sekuritiw.app.domain.model.RiskScore
import com.sekuritiw.app.domain.model.SeverityRating
import com.sekuritiw.app.domain.model.SocialEngineeringDetectionResult
import com.sekuritiw.app.domain.model.SocialEngineeringPattern
import com.sekuritiw.app.domain.model.SourceTrustLevel
import com.sekuritiw.app.domain.model.ThreatCategory
import com.sekuritiw.app.domain.model.ThreatClassificationResult
import com.sekuritiw.app.domain.model.URLAnalysisResult

/**
 * Komponen Pengelompok Ancaman (Threat Classifier) yang mengklasifikasikan ancaman siber
 * ke dalam kategori formal, mendukung multi-label, dan menentukan rating keparahan.
 *
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6
 */
class ThreatClassifier {

    /**
     * Mengklasifikasikan ancaman berdasarkan hasil dari seluruh komponen detektor.
     *
     * @param riskScore Objek RiskScore dari RiskScoringEngine
     * @param apkResult Hasil deteksi APK (opsional)
     * @param urlResults Hasil analisis URL (opsional)
     * @param seResult Hasil pencocokan pola Social Engineering (opsional)
     * @return Objek ThreatClassificationResult terstruktur
     */
    fun classify(
        riskScore: RiskScore,
        apkResult: APKDetectionResult? = null,
        urlResults: List<URLAnalysisResult> = emptyList(),
        seResult: SocialEngineeringDetectionResult? = null
    ): ThreatClassificationResult {
        // 1. Jika skor risiko aman (SAFE)
        if (riskScore.level == RiskLevel.SAFE && riskScore.score < 30.0f) {
            return ThreatClassificationResult(
                primaryCategory = ThreatCategory.BENIGN,
                secondaryCategories = emptyList(),
                severityRating = SeverityRating.LOW,
                confidence = riskScore.confidence
            )
        }

        val detectedCategories = mutableSetOf<ThreatCategory>()

        // 2. Evaluasi APK Scam
        if (apkResult != null && apkResult.detected && apkResult.trustLevel != SourceTrustLevel.TRUSTED) {
            detectedCategories.add(ThreatCategory.APK_SCAM)
        }

        // 3. Evaluasi Phishing via URL
        val hasPhishingUrl = urlResults.any { it.hasTyposquatting || it.hasHomograph || it.riskScore >= 70.0f }
        val hasSpamUrl = urlResults.any { it.isShortener || (it.riskScore in 30.0f..69.0f) }

        if (hasPhishingUrl) {
            detectedCategories.add(ThreatCategory.PHISHING)
        }
        if (hasSpamUrl) {
            detectedCategories.add(ThreatCategory.SPAM)
        }

        // 4. Evaluasi Social Engineering & Financial Scam
        if (seResult != null && seResult.detected) {
            if (seResult.containsFinancialScam) {
                detectedCategories.add(ThreatCategory.FINANCIAL_SCAM)
            }
            if (seResult.matchedPatterns.contains(SocialEngineeringPattern.PRIZE_LOTTERY)) {
                detectedCategories.add(ThreatCategory.SPAM)
            }
            if (seResult.containsCredentialRequest || seResult.matchedPatterns.contains(SocialEngineeringPattern.ACCOUNT_SUSPENSION)) {
                detectedCategories.add(ThreatCategory.SOCIAL_ENGINEERING)
                detectedCategories.add(ThreatCategory.PHISHING)
            }
        }

        // Handle ancaman ambigu / tidak terdeteksi spesifik (Req 8.6)
        if (detectedCategories.isEmpty()) {
            val fallbackCategory = if (riskScore.score > 50.0f) ThreatCategory.SOCIAL_ENGINEERING else ThreatCategory.UNKNOWN
            return ThreatClassificationResult(
                primaryCategory = fallbackCategory,
                secondaryCategories = emptyList(),
                severityRating = calculateSeverity(riskScore.score, fallbackCategory, seResult, apkResult),
                confidence = 0.85f
            )
        }

        // Tentukan Kategori Utama (Primary Category) berdasarkan hierarki tingkat risiko
        val primary = when {
            detectedCategories.contains(ThreatCategory.APK_SCAM) -> ThreatCategory.APK_SCAM
            detectedCategories.contains(ThreatCategory.PHISHING) -> ThreatCategory.PHISHING
            detectedCategories.contains(ThreatCategory.FINANCIAL_SCAM) -> ThreatCategory.FINANCIAL_SCAM
            detectedCategories.contains(ThreatCategory.SOCIAL_ENGINEERING) -> ThreatCategory.SOCIAL_ENGINEERING
            detectedCategories.contains(ThreatCategory.SPAM) -> ThreatCategory.SPAM
            else -> ThreatCategory.UNKNOWN
        }

        val secondary = detectedCategories.filter { it != primary }
        val severity = calculateSeverity(riskScore.score, primary, seResult, apkResult)

        return ThreatClassificationResult(
            primaryCategory = primary,
            secondaryCategories = secondary,
            severityRating = severity,
            confidence = riskScore.confidence
        )
    }

    /**
     * Menghitung rating keparahan dampak (Severity Rating) berdasarkan skor dan kategori.
     * Requirement: 8.3
     */
    private fun calculateSeverity(
        score: Float,
        primary: ThreatCategory,
        seResult: SocialEngineeringDetectionResult?,
        apkResult: APKDetectionResult?
    ): SeverityRating {
        // Rating Kritis (CRITICAL): OTP Request atau APK Malware luar
        if ((seResult != null && seResult.containsCredentialRequest) ||
            (apkResult != null && apkResult.detected && apkResult.trustLevel == SourceTrustLevel.SUSPICIOUS) ||
            score >= 85.0f
        ) {
            return SeverityRating.CRITICAL
        }

        // Rating Tinggi (HIGH): Phishing bank / Financial scam
        if (primary == ThreatCategory.PHISHING || primary == ThreatCategory.FINANCIAL_SCAM || score >= 70.0f) {
            return SeverityRating.HIGH
        }

        // Rating Sedang (MEDIUM): Tautan spam mencurigakan / Hadiah palsu
        if (score >= 35.0f) {
            return SeverityRating.MEDIUM
        }

        return SeverityRating.LOW
    }
}
