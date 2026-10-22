package com.sekuritiw.app.data.detector

import com.sekuritiw.app.domain.model.APKDeliveryMethod
import com.sekuritiw.app.domain.model.APKDetectionResult
import com.sekuritiw.app.domain.model.APKMetadata
import com.sekuritiw.app.domain.model.PreprocessedText
import java.util.Locale
import java.util.regex.Pattern

/**
 * Pendeteksi pola APK yang mengekstrak metadata, mengidentifikasi metode pengiriman,
 * dan mengenali frasa instruksi instalasi dalam Bahasa Indonesia.
 *
 * Requirements: 2.1, 2.2, 2.3, 2.6
 */
class APKPatternMatcher {

    companion object {
        /** Regex nama file ber-ekstensi .apk */
        private val APK_FILE_NAME_PATTERN = Pattern.compile(
            "([\\w\\-\\._]+\\.apk)",
            Pattern.CASE_INSENSITIVE
        )

        /** Frasa instruksi instalasi APK populer dalam Bahasa Indonesia */
        private val INDONESIAN_INSTALL_PHRASES = listOf(
            "install aplikasi",
            "pasang apk",
            "download aplikasi",
            "unduh aplikasi",
            "unduh file",
            "klik untuk install",
            "instal file",
            "undangan nikah",
            "undangan pernikahan",
            "cek resi paket",
            "foto paket",
            "tagihan listrik",
            "surat tilang"
        )
    }

    private val sourceVerifier = APKSourceVerifier()

    /**
     * Menganalisis PreprocessedText dan menghasilkan APKDetectionResult.
     *
     * @param preprocessed Objek PreprocessedText dari ContentPreprocessor
     * @return APKDetectionResult yang berisi status deteksi dan metadata
     */
    fun detect(preprocessed: PreprocessedText): APKDetectionResult {
        val originalText = preprocessed.original
        val textLower = preprocessed.normalized

        val matchedPhrases = mutableListOf<String>()

        // 1. Cek frasa instruksi instalasi Bahasa Indonesia (Req 2.6)
        for (phrase in INDONESIAN_INSTALL_PHRASES) {
            if (textLower.contains(phrase)) {
                matchedPhrases.add(phrase)
            }
        }

        // 2. Ekstrak nama file APK (Req 2.2)
        val metadata = extractAPKMetadata(originalText, preprocessed.extractedUrls)

        val isDetected = preprocessed.containsAPK ||
                metadata != null ||
                matchedPhrases.isNotEmpty()

        if (!isDetected) {
            return APKDetectionResult(
                detected = false,
                fileName = null,
                source = null,
                deliveryMethod = APKDeliveryMethod.NONE,
                trustLevel = sourceVerifier.verifySource(null),
                matchedPhrases = emptyList()
            )
        }

        // 3. Tentukan delivery method (Req 2.2, 2.3)
        val deliveryMethod = when {
            metadata?.deliveryMethod != null -> metadata.deliveryMethod
            preprocessed.containsURL -> APKDeliveryMethod.DOWNLOAD_LINK
            matchedPhrases.isNotEmpty() -> APKDeliveryMethod.INSTALLATION_REQUEST
            else -> APKDeliveryMethod.DIRECT_ATTACHMENT
        }

        // 4. Verifikasi tingkat kepercayaan sumber (Req 2.5)
        val source = metadata?.source ?: preprocessed.extractedUrls.firstOrNull()
        val trustLevel = sourceVerifier.verifySource(source)

        return APKDetectionResult(
            detected = true,
            fileName = metadata?.fileName,
            source = source,
            deliveryMethod = deliveryMethod,
            trustLevel = trustLevel,
            matchedPhrases = matchedPhrases
        )
    }

    /**
     * Mengekstrak nama file dan sumber pengiriman dari teks atau daftar URL.
     * Requirement: 2.2 - Extract file name, source, and delivery method
     */
    fun extractAPKMetadata(text: String, urls: List<String>): APKMetadata? {
        val matcher = APK_FILE_NAME_PATTERN.matcher(text)
        var fileName: String? = null

        if (matcher.find()) {
            fileName = matcher.group(1)
        }

        val source = urls.firstOrNull()

        val deliveryMethod = when {
            source != null && fileName != null -> APKDeliveryMethod.DOWNLOAD_LINK
            fileName != null -> APKDeliveryMethod.DIRECT_ATTACHMENT
            else -> null
        }

        return if (fileName != null) {
            APKMetadata(
                fileName = fileName,
                source = source,
                deliveryMethod = deliveryMethod ?: APKDeliveryMethod.DOWNLOAD_LINK
            )
        } else {
            null
        }
    }
}
