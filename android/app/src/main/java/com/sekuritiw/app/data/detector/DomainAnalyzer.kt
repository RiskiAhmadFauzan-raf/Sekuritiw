package com.sekuritiw.app.data.analyzer

import com.sekuritiw.app.domain.model.SuspiciousPattern
import java.net.URI
import java.util.Locale
import java.util.regex.Pattern

/**
 * Penganalisis domain yang mendeteksi peniruan bank Indonesia (typosquatting),
 * serangan homograf, penggunaan IP mentah, TLD berisiko tinggi, dan pemendek URL.
 *
 * Requirements: 3.2, 3.3, 3.6, 3.7, 4.7
 */
class DomainAnalyzer {

    companion object {
        /** Whitelist domain bank & lembaga keuangan resmi Indonesia */
        private val INDONESIAN_BANK_DOMAINS = listOf(
            "bca.co.id", "klikbca.com",
            "bankmandiri.co.id", "mandiri.co.id",
            "bri.co.id", "brimo.bri.co.id",
            "bni.co.id", "bniu.bni.co.id",
            "cimbniaga.co.id", "octoclicks.co.id",
            "danamon.co.id", "permatabank.com",
            "btn.co.id", "banksyariahindonesia.co.id",
            "ojk.go.id", "bi.go.id"
        )

        /** Whitelist domain pemerintah Indonesia */
        private val GOV_DOMAINS = listOf("go.id", "polri.go.id", "pajak.go.id", "kemkes.go.id")

        /** Daftar domain pemendek URL populer */
        private val SHORTENER_DOMAINS = setOf(
            "bit.ly", "tinyurl.com", "s.id", "cutt.ly", "t.co",
            "tr.ee", "linktr.ee", "is.gd", "buff.ly", "shorturl.at"
        )

        /** TLD tidak biasa atau berisiko tinggi */
        private val UNUSUAL_TLDS = setOf(
            "xyz", "top", "site", "club", "live", "online",
            "tech", "work", "fun", "buzz", "vip", "icu", "cam", "fit"
        )

        /** Daftar domain dan platform terpercaya — tidak akan pernah ditandai phishing */
        private val TRUSTED_DOMAINS = setOf(
            // Platform teknologi global
            "google.com", "googleapis.com", "youtube.com", "youtu.be", "gstatic.com",
            "github.com", "github.io", "gitlab.com", "stackoverflow.com",
            "microsoft.com", "office.com", "live.com", "outlook.com", "azure.com",
            "apple.com", "icloud.com",
            "mozilla.org", "android.com",
            // Sosial media
            "discord.com", "discord.gg", "discordapp.com",
            "telegram.org", "t.me", "whatsapp.com", "wa.me",
            "twitter.com", "x.com", "instagram.com", "facebook.com",
            "linkedin.com", "reddit.com", "medium.com",
            "twitch.tv", "spotify.com", "netflix.com",
            // E-commerce Indonesia
            "tokopedia.com", "shopee.co.id", "bukalapak.com", "lazada.co.id",
            "gojek.com", "grab.com", "traveloka.com", "tiket.com",
            // Berita Indonesia
            "kompas.com", "detik.com", "tribunnews.com", "cnbcindonesia.com",
            // Download & Software
            "sourceforge.net", "fosshub.com", "divxland.org", "filehippo.com",
            "ninite.com", "majorgeeks.com", "softpedia.com",
            // CDN & Infrastruktur
            "cloudflare.com", "akamai.com", "fastly.com", "jsdelivr.net", "unpkg.com"
        )

        /** Regex alamat IP IPv4 */
        private val IP_PATTERN = Pattern.compile(
            "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        )

        /** Regex karakter non-ASCII (Homograph check) */
        private val NON_ASCII_PATTERN = Pattern.compile("[^\\x00-\\x7F]")
    }

    /**
     * Mengekstrak nama domain murni dari URL.
     */
    fun extractDomain(urlStr: String): String {
        return try {
            val formattedUrl = if (!urlStr.startsWith("http://") && !urlStr.startsWith("https://")) {
                "http://$urlStr"
            } else {
                urlStr
            }
            val uri = URI(formattedUrl)
            val host = uri.host ?: urlStr.split("/")[0]
            host.lowercase(Locale("id", "ID")).removePrefix("www.")
        } catch (e: Exception) {
            urlStr.split("/")[0].lowercase(Locale("id", "ID")).removePrefix("www.")
        }
    }

    /**
     * Mengecek apakah domain adalah pemendek URL.
     */
    fun isShortener(domain: String): Boolean {
        return SHORTENER_DOMAINS.any { domain.endsWith(it) || domain == it }
    }

    /**
     * Mengecek apakah domain menggunakan IP mentah.
     */
    fun isIpAddress(domain: String): Boolean {
        return IP_PATTERN.matcher(domain).matches()
    }

    /**
     * Mengecek apakah TLD tergolong unusual/berisiko.
     */
    fun isSuspiciousTld(domain: String): Boolean {
        val parts = domain.split(".")
        if (parts.size > 1) {
            val tld = parts.last()
            return UNUSUAL_TLDS.contains(tld)
        }
        return false
    }

    /**
     * Mengecek apakah domain mengandung karakter homograf (non-ASCII).
     * Requirement: 3.6 - Flag homograph lookalike characters
     */
    fun hasHomograph(domain: String): Boolean {
        return NON_ASCII_PATTERN.matcher(domain).find() || domain.startsWith("xn--")
    }

    /**
     * Mengecek apakah domain melakukan typosquatting/peniruan bank Indonesia.
     * Requirement: 3.7, 4.7 - Recognize Indonesian bank domain imitations
     */
    fun detectFakeBankingDomain(domain: String, fullUrl: String = ""): Boolean {
        // Ekstrak hanya registrable domain (misal: divxland.org dari discord.en.divxland.org)
        val registrableDomain = extractRegistrableDomain(domain)

        // Jika domain persis sama dengan domain resmi bank atau domain terpercaya, maka aman
        if (INDONESIAN_BANK_DOMAINS.contains(registrableDomain) ||
            INDONESIAN_BANK_DOMAINS.contains(domain)) return false
        if (TRUSTED_DOMAINS.contains(registrableDomain) ||
            TRUSTED_DOMAINS.contains(domain)) return false

        val bankKeywords = listOf("bca", "mandiri", "bri", "bni", "cimb", "danamon", "permata", "bsi")
        val targetText = if (fullUrl.isNotBlank()) fullUrl.lowercase() else domain.lowercase()

        // Cek jika domain (termasuk subdomain) atau URL path mengandung kata kunci bank TETAPI bukan domain bank resmi
        for (keyword in bankKeywords) {
            if (targetText.contains(keyword)) {
                val isOfficial = INDONESIAN_BANK_DOMAINS.any { domain.endsWith(it) }
                if (!isOfficial) return true
            }
        }

        // Cek Levenshtein distance — hanya untuk domain yang cukup mirip secara panjang
        for (officialDomain in INDONESIAN_BANK_DOMAINS) {
            if (registrableDomain.length < 6 || officialDomain.length < 6) continue
            if (kotlin.math.abs(registrableDomain.length - officialDomain.length) > 3) continue
            val distance = levenshteinDistance(registrableDomain, officialDomain)
            if (distance in 1..2) {
                return true
            }
        }

        return false
    }

    /**
     * Menghitung Levenshtein Distance untuk pencocokan kemiripan string.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[m][n]
    }

    /**
     * Mengekstrak registrable domain (domain utama tanpa subdomain).
     * Contoh: "discord.en.divxland.org" → "divxland.org"
     *         "www.bca.co.id" → "bca.co.id"
     */
    fun extractRegistrableDomain(host: String): String {
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
