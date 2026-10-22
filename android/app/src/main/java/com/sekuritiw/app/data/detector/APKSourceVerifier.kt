package com.sekuritiw.app.data.detector

import com.sekuritiw.app.domain.model.SourceTrustLevel
import java.util.Locale

/**
 * Verifikator tingkat kepercayaan sumber pengiriman file APK.
 * Memeriksa domain atau asal sumber terhadap whitelist dan blacklist resmi.
 *
 * Requirements: 2.5, 11.3
 */
class APKSourceVerifier {

    companion object {
        /** Whitelist domain resmi dan sangat terpercaya (Google Play) */
        private val TRUSTED_SOURCES = setOf(
            "play.google.com",
            "market.android.com"
        )

        /** Whitelist toko aplikasi alternatif resmi produsen */
        private val KNOWN_SOURCES = setOf(
            "galaxy.store.samsung.com",
            "appgallery.huawei.com",
            "getapps.mi.com",
            "store.oppo.com",
            "apkpure.com",
            "apkmirror.com"
        )

        /** Blacklist pola domain yang sering disalahgunakan untuk distribusi APK palsu */
        private val SUSPICIOUS_DOMAINS = setOf(
            "bit.ly", "tinyurl.com", "s.id", "cutt.ly",
            "mediafire.com", "drive.google.com", "dropbox.com",
            "top", "xyz", "online", "site", "live", "club", "buzz"
        )
    }

    /**
     * Memverifikasi tingkat kepercayaan dari domain atau sumber pengiriman APK.
     *
     * @param source Domain atau string sumber asal APK
     * @return SourceTrustLevel (TRUSTED, KNOWN, UNKNOWN, atau SUSPICIOUS)
     */
    fun verifySource(source: String?): SourceTrustLevel {
        if (source.isNullOrBlank()) return SourceTrustLevel.UNKNOWN

        val cleanSource = source.lowercase(Locale("id", "ID")).trim()

        // 1. Cek trusted sources (Google Play Store)
        if (TRUSTED_SOURCES.any { cleanSource.contains(it) }) {
            return SourceTrustLevel.TRUSTED
        }

        // 2. Cek known app stores
        if (KNOWN_SOURCES.any { cleanSource.contains(it) }) {
            return SourceTrustLevel.KNOWN
        }

        // 3. Cek suspicious domains / shorteners / file sharing
        if (SUSPICIOUS_DOMAINS.any { cleanSource.contains(it) }) {
            return SourceTrustLevel.SUSPICIOUS
        }

        // Default untuk sumber tidak dikenal lainnya
        return SourceTrustLevel.UNKNOWN
    }
}
