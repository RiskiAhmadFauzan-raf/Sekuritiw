package com.sekuritiw.app.data.analyzer

import com.sekuritiw.app.domain.model.PreprocessedText
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

/**
 * Modul pemrosesan awal teks (preprocessing) untuk menormalisasi teks notifikasi Bahasa Indonesia,
 * mendeteksi/mengisolasi URL dan file APK, serta melakukan tokenisasi dasar.
 *
 * Requirements: 4.4, 7.4, 24.1, 24.5
 */
class ContentPreprocessor {

    companion object {
        /** Regex untuk mendeteksi URL (http, https, www, atau domain.tld) */
        private val URL_PATTERN = Pattern.compile(
            "(https?://[\\w\\-\\.]+\\.[a-zA-Z]{2,}(?:/[^\\s]*)?)|(www\\.[\\w\\-\\.]+\\.[a-zA-Z]{2,}(?:/[^\\s]*)?)|([\\w\\-\\.]+\\.(?:com|co\\.id|id|xyz|net|org|info|biz|site|online|top|live|app|apk)(?:/[^\\s]*)?)",
            Pattern.CASE_INSENSITIVE
        )

        /** Regex untuk mendeteksi nama file APK atau ekstensi .apk */
        private val APK_PATTERN = Pattern.compile(
            "([\\w\\-]+\\.apk(?:\\?[^\\s]*)?)|(unduh\\s+apk|install\\s+apk|pasang\\s+apk|download\\s+aplikasi|unduh\\s+aplikasi)",
            Pattern.CASE_INSENSITIVE
        )

        /** Token pengganti untuk URL dan APK */
        const val TOKEN_URL = "[URL]"
        const val TOKEN_APK = "[APK]"
    }

    /**
     * Memproses teks mentah menjadi objek PreprocessedText yang siap digunakan oleh komponen detektor dan AI Engine.
     *
     * @param rawText Teks gabungan judul, isi, dan subteks dari notifikasi
     * @return Objek PreprocessedText yang sudah terstruktur
     */
    fun preprocess(rawText: String): PreprocessedText {
        if (rawText.isBlank()) {
            return PreprocessedText(
                original = rawText,
                cleaned = "",
                normalized = "",
                tokens = emptyList(),
                extractedUrls = emptyList(),
                extractedApks = emptyList()
            )
        }

        // 1. Ekstraksi URL sebelum teks diubah
        val extractedUrls = extractUrls(rawText)

        // 2. Ekstraksi referensi APK
        val extractedApks = extractApks(rawText)

        // 3. Normalisasi Unicode NFKC & Karakter kontrol
        val nfkcNormalized = Normalizer.normalize(rawText, Normalizer.Form.NFKC)

        // 4. Pembersihan spasi berlebih dan karakter tidak tercetak
        val cleaned = nfkcNormalized
            .replace(Regex("[\\r\\n\\t]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        // 5. Lowercase untuk Locale Indonesia
        var normalized = cleaned.lowercase(Locale("id", "ID"))

        // 6. Ganti URL dengan token khusus [URL]
        for (url in extractedUrls) {
            normalized = normalized.replace(url.lowercase(Locale("id", "ID")), TOKEN_URL)
        }

        // 7. Tokenisasi dasar (pembagian kata)
        val tokens = tokenize(normalized)

        return PreprocessedText(
            original = rawText,
            cleaned = cleaned,
            normalized = normalized,
            tokens = tokens,
            extractedUrls = extractedUrls,
            extractedApks = extractedApks
        )
    }

    /**
     * Mengekstrak seluruh URL dari teks mentah.
     */
    private fun extractUrls(text: String): List<String> {
        val matcher = URL_PATTERN.matcher(text)
        val urls = mutableListOf<String>()
        while (matcher.find()) {
            val match = matcher.group()
            if (match.isNotBlank() && !urls.contains(match)) {
                urls.add(match)
            }
        }
        return urls
    }

    /**
     * Mengekstrak referensi APK atau instruksi instalasi dari teks.
     */
    private fun extractApks(text: String): List<String> {
        val matcher = APK_PATTERN.matcher(text)
        val apks = mutableListOf<String>()
        while (matcher.find()) {
            val match = matcher.group()
            if (match.isNotBlank() && !apks.contains(match)) {
                apks.add(match)
            }
        }
        return apks
    }

    /**
     * Membagi teks ter-normalisasi menjadi token kata dasar.
     */
    private fun tokenize(text: String): List<String> {
        return text.split(Regex("[^a-zA-Z0-9\\[\\]]+"))
            .filter { it.isNotBlank() }
    }
}
