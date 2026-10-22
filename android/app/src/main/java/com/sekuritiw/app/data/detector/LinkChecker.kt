package com.sekuritiw.app.data.detector

import android.content.Context
import com.sekuritiw.app.data.analyzer.DomainAnalyzer
import com.sekuritiw.app.domain.model.PreprocessedText
import com.sekuritiw.app.domain.model.RiskLevel
import com.sekuritiw.app.domain.model.SuspiciousPattern
import com.sekuritiw.app.domain.model.URLAnalysisResult

/**
 * Pemeriksa Keamanan Tautan (Link Checker) Hybrid Multi-Layer:
 * Layer 1: Analysis Heuristik Domain & Pattern (Homograf, Typosquatting, TLD, IP)
 * Layer 2: On-Device TensorFlow Lite AI Model Inference (phishing_model.tflite)
 *
 * Requirements: 3.1, 3.2, 3.3, 3.5, 3.6, 3.7, 3.8
 */
class LinkChecker(context: Context? = null) {

    companion object {
        /** Domain dan platform terpercaya yang tidak akan pernah dianggap phishing */
        private val TRUSTED_DOMAINS = setOf(
            "google.com", "googleapis.com", "youtube.com", "youtu.be", "gstatic.com",
            "github.com", "github.io", "gitlab.com", "stackoverflow.com",
            "microsoft.com", "office.com", "live.com", "outlook.com", "azure.com",
            "apple.com", "icloud.com", "mozilla.org", "android.com",
            "discord.com", "discord.gg", "discordapp.com",
            "telegram.org", "t.me", "whatsapp.com", "wa.me",
            "twitter.com", "x.com", "instagram.com", "facebook.com",
            "linkedin.com", "reddit.com", "medium.com",
            "twitch.tv", "spotify.com", "netflix.com",
            "tokopedia.com", "shopee.co.id", "bukalapak.com", "lazada.co.id",
            "gojek.com", "grab.com", "traveloka.com", "tiket.com",
            "kompas.com", "detik.com", "tribunnews.com", "cnbcindonesia.com",
            "sourceforge.net", "fosshub.com", "divxland.org", "filehippo.com",
            "ninite.com", "majorgeeks.com", "softpedia.com",
            "cloudflare.com", "akamai.com", "fastly.com", "jsdelivr.net", "unpkg.com"
        )
    }

    private val domainAnalyzer = DomainAnalyzer()
    private val tfliteClassifier = PhishingURLClassifier(context)

    /**
     * Menganalisis seluruh URL yang terdapat dalam PreprocessedText.
     */
    fun analyzeUrls(preprocessed: PreprocessedText): List<URLAnalysisResult> {
        return preprocessed.extractedUrls.map { url ->
            analyzeSingleUrl(url)
        }
    }

    /**
     * Menganalisis 1 URL spesifik dengan mengkombinasikan Layer 1 (Heuristik) & Layer 2 (TFLite AI Model).
     */
    fun analyzeSingleUrl(url: String): URLAnalysisResult {
        val domain = domainAnalyzer.extractDomain(url)
        val registrableDomain = domainAnalyzer.extractRegistrableDomain(domain)
        val patterns = mutableListOf<SuspiciousPattern>()

        // ── Guard: Domain terpercaya → langsung kembalikan SAFE ──
        val isTrustedDomain = TRUSTED_DOMAINS.any {
            domain == it || domain.endsWith(".$it") || registrableDomain == it
        }
        if (isTrustedDomain) {
            return URLAnalysisResult(
                url = url,
                domain = domain,
                isShortener = false,
                isSuspiciousTLD = false,
                hasTyposquatting = false,
                hasHomograph = false,
                reputationScore = 0.0f,
                detectedPatterns = emptyList(),
                riskLevel = com.sekuritiw.app.domain.model.RiskLevel.SAFE,
                riskScore = 0.0f
            )
        }

        val isShortener = domainAnalyzer.isShortener(domain)
        val isIp = domainAnalyzer.isIpAddress(domain)
        val isSuspiciousTld = domainAnalyzer.isSuspiciousTld(domain)
        val hasHomograph = domainAnalyzer.hasHomograph(domain)
        val isFakeBank = domainAnalyzer.detectFakeBankingDomain(domain, url)

        // ── Layer 1: Heuristic Domain Score ──
        var heuristicScore = 0.0f

        if (isShortener) {
            patterns.add(SuspiciousPattern.URL_SHORTENER)
            heuristicScore += 35.0f
        }

        if (isIp) {
            patterns.add(SuspiciousPattern.IP_ADDRESS)
            heuristicScore += 45.0f
        }

        if (isSuspiciousTld) {
            patterns.add(SuspiciousPattern.UNUSUAL_TLD)
            heuristicScore += 35.0f
        }

        if (hasHomograph) {
            patterns.add(SuspiciousPattern.HOMOGRAPH_ATTACK)
            heuristicScore = maxOf(heuristicScore, 75.0f)
        }

        if (isFakeBank) {
            patterns.add(SuspiciousPattern.FAKE_BANKING_DOMAIN)
            patterns.add(SuspiciousPattern.TYPOSQUATTING)
            heuristicScore = maxOf(heuristicScore, 85.0f)
        }

        // ── Layer 2: On-Device TFLite AI Model Score (phishing_model.tflite) ──
        val tfliteScore = tfliteClassifier.classifyUrl(url)

        // ── Hybrid Score Blending (Penggabungan Multi-Layer) ──
        val finalScore = when {
            // Jika Heuristik menemukan bahaya fatal (Fake Bank / Homograph) -> pertahankan skor tinggi
            isFakeBank || hasHomograph -> maxOf(heuristicScore, tfliteScore)
            // Jika tidak ada pola heuristik mencurigakan dan skor TFLite aman -> 0.0f (Aman)
            patterns.isEmpty() && tfliteScore < 30.0f -> 0.0f
            // Kombinasi gabungan: 60% TFLite AI + 40% Heuristic
            else -> maxOf(tfliteScore, (tfliteScore * 0.60f) + (heuristicScore * 0.40f)).coerceIn(0.0f, 100.0f)
        }

        val riskLevel = RiskLevel.fromScore(finalScore)

        return URLAnalysisResult(
            url = url,
            domain = domain,
            isShortener = isShortener,
            isSuspiciousTLD = isSuspiciousTld,
            hasTyposquatting = isFakeBank,
            hasHomograph = hasHomograph,
            reputationScore = tfliteScore, // TFLite ML Score
            detectedPatterns = patterns,
            riskLevel = riskLevel,
            riskScore = finalScore
        )
    }
}
