package com.sekuritiw.app.data.detector

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Locale

/**
 * Classifiers Phishing URL menggunakan Model TensorFlow Lite (phishing_model.tflite)
 * Menggunakan 16 Fitur Leksikal & Struktural URL On-Device (100% Offline).
 */
class PhishingURLClassifier(private val context: Context? = null) {

    companion object {
        private const val TAG = "PhishingURLClassifier"
        private const val MODEL_FILE = "phishing_model.tflite"

        private val SHORTENERS = setOf(
            "bit.ly", "tinyurl.com", "t.co", "goo.gl", "is.gd", "buff.ly",
            "ow.ly", "cutt.ly", "s.id", "shorturl.at", "rebrand.ly", "v.gd"
        )

        private val SUSPICIOUS_TLDS = setOf(
            ".xyz", ".top", ".cc", ".cf", ".gq", ".ml", ".tk", ".ga",
            ".work", ".site", ".vip", ".icu", ".buzz", ".fit", ".rest",
            ".cam", ".monster", ".click", ".link", ".one", ".beauty"
        )

        private val BANK_KEYWORDS = listOf(
            "bca", "mandiri", "bri", "bni", "cimb", "permata", "danamon",
            "dana", "ovo", "gopay", "shopee", "tokopedia", "whatsapp",
            "telegram", "telkomsel", "indihome", "pln", "pajak", "bpjs"
        )

        private val SENSITIVE_KEYWORDS = listOf(
            "login", "verify", "verification", "secure", "account", "update",
            "banking", "konfirmasi", "hadiah", "promo", "klaim", "menang", "lapor"
        )

        // Domain dan platform terpercaya yang tidak boleh dianggap phishing
        private val TRUSTED_DOMAINS = setOf(
            // Sosial & Chat
            "discord.com", "discord.gg", "discord.gg", "discordapp.com",
            "telegram.org", "t.me", "whatsapp.com", "wa.me",
            // Teknologi
            "google.com", "googleapis.com", "youtube.com", "youtu.be",
            "github.com", "github.io", "gitlab.com", "stackoverflow.com",
            "microsoft.com", "office.com", "live.com", "outlook.com",
            "apple.com", "icloud.com",
            "mozilla.org", "firefox.com", "chrome.com",
            "android.com", "play.google.com", "developer.android.com",
            // Media & Konten
            "twitter.com", "x.com", "instagram.com", "facebook.com",
            "linkedin.com", "reddit.com", "medium.com", "substack.com",
            "twitch.tv", "spotify.com", "netflix.com",
            // Indonesia
            "tokopedia.com", "shopee.co.id", "bukalapak.com", "lazada.co.id",
            "gojek.com", "grab.com", "traveloka.com", "tiket.com",
            "kompas.com", "detik.com", "tribunnews.com", "cnbcindonesia.com",
            // CDN & Infrastruktur
            "cloudflare.com", "akamai.com", "fastly.com", "jsdelivr.net", "unpkg.com",
            // Download & Repo
            "sourceforge.net", "fosshub.com", "divxland.org", "filehippo.com",
            "ninite.com", "majorgeeks.com", "softpedia.com"
        )
    }

    private var interpreter: Interpreter? = null

    init {
        context?.let { ctx ->
            try {
                val modelBuffer = loadModelFile(ctx, MODEL_FILE)
                interpreter = Interpreter(modelBuffer)
                Log.i(TAG, "✅ Model TFLite phishing_model.tflite berhasil dimuat.")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Tidak dapat memuat model TFLite ($MODEL_FILE): ${e.message}. Menggunakan ekstraksi fitur heuristik.")
            }
        }
    }

    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Menganalisis URL dan mengembalikan skor TFLite (0.0f - 100.0f)
     */
    fun classifyUrl(url: String): Float {
        val features = extractFeatures(url)
        val host = extractHost(url.lowercase(Locale.ROOT).trim())
        val registrableDomain = extractRegistrableDomain(host)

        // 1. Jika domain berada dalam whitelist domain terpercaya → kembalikan skor 0
        if (TRUSTED_DOMAINS.any { host == it || host.endsWith(".${it}") || registrableDomain == it }) {
            Log.i(TAG, "Domain terpercaya ditemukan ($host) → Skor 0%")
            return 0.0f
        }

        // 2. Jika tidak ada satu pun indikator/fitur mencurigakan (activeFeatureCount == 0) → URL 100% Aman (0.0f)
        val activeFeatureCount = features.count { it > 0.0f }
        if (activeFeatureCount == 0) {
            Log.i(TAG, "Tidak ada indikator risiko pada URL ($url) → Skor 0%")
            return 0.0f
        }

        if (interpreter != null) {
            try {
                val input = Array(1) { features }
                val output = Array(1) { FloatArray(1) }
                interpreter?.run(input, output)
                
                val rawScore = output[0][0] // Probabilitas 0.0 - 1.0
                var percentScore = (rawScore * 100.0f).coerceIn(0.0f, 100.0f)

                // Jika hanya 1 fitur minor (misal hanya URL > 54 char atau hanya ada 1 tanda hubung), batasi skor agar tidak false positive
                val hasCriticalSignal = features[1] > 0 || features[2] > 0 || features[7] > 0 || features[9] > 0 || features[10] > 0 || features[12] > 0 || features[15] > 0
                if (!hasCriticalSignal && activeFeatureCount < 2) {
                    percentScore = minOf(percentScore, 25.0f)
                }

                Log.i(TAG, "TFLite Model Phishing Score: $percentScore% (Raw: $rawScore) for URL: $url")
                return percentScore
            } catch (e: Exception) {
                Log.e(TAG, "Error saat inference TFLite: ${e.message}", e)
            }
        }

        // Fallback perhitungan skor berbasis bobot fitur jika TFLite belum terinisialisasi
        var fallbackScore = 0.0f
        features.forEach { f -> fallbackScore += f * 8.0f }
        return fallbackScore.coerceIn(0.0f, 100.0f)
    }

    /**
     * Ekstraksi 16 Fitur Vektor Numerik FloatArray(16) dari String URL
     */
    fun extractFeatures(url: String): FloatArray {
        val lowerUrl = url.lowercase(Locale.ROOT).trim()
        val features = FloatArray(16)

        val host = extractHost(lowerUrl)

        // Fitur 0: Panjang URL > 54 Karakter
        features[0] = if (lowerUrl.length > 54) 1.0f else 0.0f

        // Fitur 1: Memiliki IP Address sebagai Host
        features[1] = if (host.matches(Regex("""^(\d{1,3}\.){3}\d{1,3}$"""))) 1.0f else 0.0f

        // Fitur 2: Menggunakan URL Shortener
        features[2] = if (SHORTENERS.any { host == it || host.endsWith(".$it") }) 1.0f else 0.0f

        // Fitur 3: Menggunakan Simbol '@'
        features[3] = if (lowerUrl.contains("@")) 1.0f else 0.0f

        // Fitur 4: Redireksi Double Slash '//' dalam Path
        val afterProto = lowerUrl.substringAfter("://", lowerUrl)
        features[4] = if (afterProto.contains("//")) 1.0f else 0.0f

        // Fitur 5: Tanda Hubung '-' pada Nama Domain Host
        features[5] = if (host.contains("-")) 1.0f else 0.0f

        // Fitur 6: Subdomain Berlebihan (Jumlah titik >= 4, bukan >= 3 agar subdomain 2-level tidak salah terdeteksi)
        val dotCount = host.count { it == '.' }
        features[6] = if (dotCount >= 4) 1.0f else 0.0f

        // Fitur 7: Bukan HTTPS (Misal HTTP polos atau Tanpa Protokol)
        features[7] = if (!lowerUrl.startsWith("https://")) 1.0f else 0.0f

        // Fitur 8: Port Khusus Non-Standar (e.g. :8080, :8443, :8000)
        features[8] = if (host.contains(Regex(""":\d+"""))) 1.0f else 0.0f

        // Fitur 9: Nama Domain Mengandung Kata Kunci Bank/Layanan Populer (Phishing Target)
        features[9] = if (BANK_KEYWORDS.any { host.contains(it) }) 1.0f else 0.0f

        // Fitur 10: TLD Berisiko Tinggi (.xyz, .top, .cc, dll.)
        features[10] = if (SUSPICIOUS_TLDS.any { host.endsWith(it) }) 1.0f else 0.0f

        // Fitur 11: Kata 'https' Tersembunyi di Nama Domain (Misal https-bca.com)
        features[11] = if (host.contains("https") || host.contains("http")) 1.0f else 0.0f

        // Fitur 12: Memiliki Kata Kunci Sensitif (login, verify, promo, klaim)
        features[12] = if (SENSITIVE_KEYWORDS.any { lowerUrl.contains(it) }) 1.0f else 0.0f

        // Fitur 13: Rasio Angka Tinggi pada Hostname (>20% Digit)
        val digitCount = host.count { it.isDigit() }
        features[13] = if (host.isNotEmpty() && (digitCount.toFloat() / host.length) > 0.20f) 1.0f else 0.0f

        // Fitur 14: Nama Host Sangat Panjang (>30 Karakter)
        features[14] = if (host.length > 30) 1.0f else 0.0f

        // Fitur 15: Parameter Query Mengandung URL Redirection (url=, redirect=, link=)
        features[15] = if (lowerUrl.contains("url=") || lowerUrl.contains("redirect=") || lowerUrl.contains("link=") || lowerUrl.contains("target=")) 1.0f else 0.0f

        return features
    }

    private fun extractHost(url: String): String {
        var clean = url.trim()
        if (clean.contains("://")) {
            clean = clean.substringAfter("://")
        }
        clean = clean.substringBefore("/").substringBefore("?").substringBefore("#")
        return clean
    }

    /**
     * Mengekstrak domain registrasi utama (registrable domain) dari hostname.
     * Contoh: "discord.en.divxland.org" → "divxland.org"
     *         "play.google.com" → "google.com"
     */
    private fun extractRegistrableDomain(host: String): String {
        val parts = host.split(".")
        // Handle country-code second-level domains Indonesia (e.g. co.id, go.id, my.id, web.id)
        val ccSLDs = setOf("co", "go", "ac", "or", "net", "web", "sch", "mil", "my", "biz", "desa", "ponpes")
        return if (parts.size >= 3 && ccSLDs.contains(parts[parts.size - 2])) {
            parts.takeLast(3).joinToString(".")
        } else if (parts.size >= 2) {
            parts.takeLast(2).joinToString(".")
        } else {
            host
        }
    }
}
