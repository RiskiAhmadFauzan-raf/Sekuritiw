package com.sekuritiw.app.data.analyzer

import android.util.Log
import com.sekuritiw.app.data.detector.APKPatternMatcher
import com.sekuritiw.app.data.detector.LinkChecker
import com.sekuritiw.app.data.detector.RiskScoringEngine
import com.sekuritiw.app.data.detector.SocialEngineeringMatcher
import com.sekuritiw.app.data.detector.ThreatClassifier
import com.sekuritiw.app.domain.model.APKDetectionResult
import com.sekuritiw.app.domain.model.Explanation
import com.sekuritiw.app.domain.model.NotificationContent
import com.sekuritiw.app.domain.model.PreprocessedText
import com.sekuritiw.app.domain.model.RiskScore
import com.sekuritiw.app.domain.model.SocialEngineeringDetectionResult
import com.sekuritiw.app.domain.model.ThreatClassificationResult
import com.sekuritiw.app.domain.model.URLAnalysisResult

/**
 * Hasil agregat dari alur analisis konten.
 */
data class AnalysisResult(
    val contentId: String,
    val packageName: String,
    val preprocessedText: PreprocessedText,
    val apkResult: APKDetectionResult? = null,
    val urlResults: List<URLAnalysisResult> = emptyList(),
    val socialEngineeringResult: SocialEngineeringDetectionResult? = null,
    val calculatedRiskScore: RiskScore? = null,
    val classificationResult: ThreatClassificationResult? = null,
    val explanation: Explanation? = null,
    val threatCategory: String,
    val riskScore: Float,
    val riskLevel: String,
    val reasoning: String,
    val processingTimeMs: Long
)

/**
 * Koordinator utama pipeline analisis yang menerima NotificationContent,
 * memanggil ContentPreprocessor, dan menyalurkan teks ke detektor & AI Engine.
 *
 * Requirements: 1.2, 2.1-2.7, 3.1-3.8, 4.1-4.7, 5.1-5.8, 6.1-6.8, 8.1-8.6, 20.1
 */
class ContentAnalyzer(
    context: android.content.Context? = null,
    private val indoBertApi: com.sekuritiw.app.data.api.IndoBertApi? = null
) {

    companion object {
        private const val TAG = "ContentAnalyzer"
    }

    private val preprocessor = ContentPreprocessor()
    private val apkDetector = APKPatternMatcher()
    private val linkChecker = LinkChecker(context)
    private val seMatcher = SocialEngineeringMatcher()
    private val scoringEngine = RiskScoringEngine()
    private val threatClassifier = ThreatClassifier()
    private val explainer = Explainer()

    /**
     * Memproses dan menganalisis konten notifikasi secara terkoordinasi.
     *
     * @param content Objek NotificationContent yang diekstrak dari notifikasi
     * @return Objek AnalysisResult hasil agregasi analisis
     */
    suspend fun analyze(content: NotificationContent): AnalysisResult {
        val startTime = System.currentTimeMillis()
        val fullText = content.getFullText()

        // 1. Pemrosesan awal teks (Preprocessing)
        val preprocessed = preprocessor.preprocess(fullText)

        // 2. Deteksi APK dari teks notifikasi dinonaktifkan (karena APK dipindai secara langsung)
        val apkResult = com.sekuritiw.app.domain.model.APKDetectionResult(
            detected = false,
            fileName = null,
            source = null,
            deliveryMethod = com.sekuritiw.app.domain.model.APKDeliveryMethod.NONE,
            trustLevel = com.sekuritiw.app.domain.model.SourceTrustLevel.UNKNOWN,
            matchedPhrases = emptyList()
        )

        // 3. Pemeriksa Tautan (Task 12)
        val urlResults = linkChecker.analyzeUrls(preprocessed)

        // 4. Pencocok Pola Social Engineering (Task 13)
        var seResult = seMatcher.match(preprocessed)

        // 4.5. Tambahan: Panggil API IndoBERT jika tersedia (Cloud AI)
        if (indoBertApi != null && preprocessed.normalized.isNotEmpty()) {
            try {
                val context = com.sekuritiw.app.data.api.AnalysisContext(
                    hasUrl = urlResults.isNotEmpty(),
                    hasApk = apkResult.detected,
                    packageName = content.packageName
                )
                val request = com.sekuritiw.app.data.api.PredictRequest(
                    text = preprocessed.normalized,
                    context = context
                )
                val response = indoBertApi.analyzeText(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    val threatCategory = body?.effectiveCategory.orEmpty()
                    val confidence = body?.confidence ?: 0.0
                    val isThreat = threatCategory != "BENIGN" &&
                            threatCategory.isNotBlank() &&
                            confidence >= 0.50

                    if (body != null && isThreat) {
                        // Perkaya hasil deteksi lokal dengan inferensi AI Cloud (IndoBERT)
                        val scoreContrib = (body.riskScore?.toFloat() ?: (confidence * 100).toFloat()).coerceIn(0.0f, 100.0f)
                        seResult = com.sekuritiw.app.domain.model.SocialEngineeringDetectionResult(
                            detected = true,
                            matchedPatterns = seResult.matchedPatterns,
                            detectedPhrases = seResult.detectedPhrases + listOf("Peringatan AI Cloud ($threatCategory): ${String.format(java.util.Locale.US, "%.1f%%", confidence * 100)}"),
                            containsCredentialRequest = seResult.containsCredentialRequest ||
                                    threatCategory.contains("PHISHING", ignoreCase = true) ||
                                    threatCategory.contains("SOCIAL", ignoreCase = true),
                            containsFinancialScam = seResult.containsFinancialScam ||
                                    threatCategory.contains("FINANCIAL", ignoreCase = true) ||
                                    threatCategory.contains("SCAM", ignoreCase = true),
                            riskContributionScore = maxOf(seResult.riskContributionScore, scoreContrib)
                        )
                        Log.d(TAG, "IndoBERT Cloud API mendeteksi ancaman: $threatCategory (${confidence * 100}%)")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gagal memanggil IndoBERT API: ${e.message}")
            }
        }

        // 5. Kalkulasi Skor Risiko Terbobot (Task 16)
        val calculatedScore = scoringEngine.calculateScore(apkResult, urlResults, seResult)

        // 6. Pengelompokan Ancaman Formal & Multi-label (Task 18)
        val classification = threatClassifier.classify(calculatedScore, apkResult, urlResults, seResult)

        // 7. Generasi Penjelasan Bahasa Indonesia (Task 17)
        val explanation = explainer.generateExplanation(calculatedScore, apkResult, urlResults, seResult)

        Log.d(TAG, "Teks ter-normalisasi: ${preprocessed.normalized}")
        Log.d(TAG, "URL Terdeteksi: ${urlResults.map { "${it.domain} (${it.riskLevel})" }}")
        Log.d(TAG, "APK Result: detected=${apkResult.detected}, file=${apkResult.fileName}, trust=${apkResult.trustLevel}")
        Log.d(TAG, "SE Result: detected=${seResult.detected}, patterns=${seResult.matchedPatterns}")
        Log.d(TAG, "Calculated RiskScore: ${calculatedScore.score} (${calculatedScore.level})")
        Log.d(TAG, "Classification: primary=${classification.primaryCategory}, severity=${classification.severityRating}")

        val elapsedTime = System.currentTimeMillis() - startTime

        return AnalysisResult(
            contentId = content.id,
            packageName = content.packageName,
            preprocessedText = preprocessed,
            apkResult = apkResult,
            urlResults = urlResults,
            socialEngineeringResult = seResult,
            calculatedRiskScore = calculatedScore,
            classificationResult = classification,
            explanation = explanation,
            threatCategory = classification.primaryCategory.name,
            riskScore = calculatedScore.score,
            riskLevel = calculatedScore.level.name,
            reasoning = explanation.summary,
            processingTimeMs = elapsedTime
        )
    }
}





