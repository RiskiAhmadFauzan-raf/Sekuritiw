package com.sekuritiw.app.domain.util

import java.net.URI

/**
 * Komponen terpusat untuk penyamaran data sensitif (Data Minimization & Redaction).
 * Sesuai prinsip UU PDP No. 27/2022 dan Spesifikasi Privasi Feedback SEKURITIW.
 *
 * Komponen ini dibuat sebagai pure function (tanpa dependensi framework Android)
 * agar dapat diuji secara terisolasi dan mudah diverifikasi.
 */
object SensitiveDataRedactor {

    data class RedactedSender(
        val senderDisplay: String,
        val senderType: String // "PHONE", "ALPHANUMERIC", "OTHER"
    )

    /**
     * Menyamarkan data sensitif pada teks (pesan atau catatan pengguna)
     * dengan urutan eksekusi yang ketat agar pola tidak saling merusak.
     *
     * Urutan:
     * 1. URL -> [URL: domain.com]
     * 2. Email -> [EMAIL]
     * 3. Nomor Telepon -> [TELEPON]
     * 4. Kode OTP / Verifikasi -> [KODE]
     * 5. Deretan 8 digit atau lebih -> [NOMOR]
     * 6. Nominal uang -> Rp[NOMINAL]
     */
    fun redactText(input: String?): String {
        if (input.isNullOrBlank()) return ""

        var text = input

        // 1. URL: simpan domain, buang path, port, dan query parameter
        val urlRegex = Regex("""(?i)\bhttps?://([a-zA-Z0-9.-]+)(?::\d+)?(?:/[^\s]*)?""")
        text = urlRegex.replace(text) { matchResult ->
            val host = matchResult.groupValues[1].trim().lowercase()
            "[URL: $host]"
        }

        // 2. Email
        val emailRegex = Regex("""(?i)\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b""")
        text = emailRegex.replace(text, "[EMAIL]")

        // 3. Nomor Telepon Indonesia (+62..., 08..., 62...)
        // Menangkap variasi spasi atau tanda hubung antar digit
        val phoneRegex = Regex("""(?:\+?62\s*|0)8[0-9\s-]{7,15}[0-9]""")
        text = phoneRegex.replace(text) { matchResult ->
            val digitsOnly = matchResult.value.filter { it.isDigit() }
            if (digitsOnly.length in 10..15) {
                "[TELEPON]"
            } else {
                matchResult.value
            }
        }

        // 4. Kode OTP / Verifikasi (4 sampai 8 digit di dekat kata kunci)
        // Pola A: Kata kunci mendahului digit (contoh: "Kode verifikasi: 492011", "kode otp login anda 0404")
        val otpAfterKeywordRegex = Regex(
            """(?i)\b(kode(?:\s*(?:otp|verifikasi|rahasia|keamanan|masuk|login))?|otp|pin|token|password|passcode|verifikasi)\b([^0-9\r\n.;?!]{0,35}?)(\b\d{4,8}\b)"""
        )
        text = otpAfterKeywordRegex.replace(text) { match ->
            val keyword = match.groupValues[1]
            val sep = match.groupValues[2]
            "$keyword$sep[KODE]"
        }

        // Pola B: Digit mendahului kata kunci (contoh: "492011 adalah kode verifikasi Anda", "0404 adalah kode otp login anda")
        val otpBeforeKeywordRegex = Regex(
            """(?i)(\b\d{4,8}\b)([^0-9\r\n.;?!]{0,35}?\b(?:kode|otp|pin|token|verifikasi|password|passcode)\b)"""
        )
        text = otpBeforeKeywordRegex.replace(text) { match ->
            val trailing = match.groupValues[2]
            "[KODE]$trailing"
        }

        // 5. Deretan 8 digit atau lebih (nomor rekening, kartu debit/kredit, NIK, resi sensitif)
        val longDigitsRegex = Regex("""\b\d{8,}\b""")
        text = longDigitsRegex.replace(text, "[NOMOR]")

        // 6. Nominal uang (Rp 1.500.000, IDR 50.000, dsb.)
        val currencyRegex = Regex("""(?i)\b(?:Rp\.?|IDR)\s*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})?|[0-9]+)\b""")
        text = currencyRegex.replace(text, "Rp[NOMINAL]")

        return text
    }

    /**
     * Menyamarkan identitas pengirim (Sender Redaction).
     * Jika berupa nomor telepon: disamarkan menjadi format '+62 821 **** **34'.
     * Jika dari aplikasi pesan personal (WhatsApp, dll) atau berupa nama kontak personal: disamarkan (misal: 'R***i I* I*').
     * Jika berupa ID alfanumerik resmi (misal 'BANK-BCA'): dipertahankan apa adanya.
     */
    fun redactSender(sender: String?, packageName: String? = null): RedactedSender {
        if (sender.isNullOrBlank()) {
            return RedactedSender(senderDisplay = "Anonim", senderType = "OTHER")
        }

        val trimmed = sender.trim()
        val digitsOnly = trimmed.filter { it.isDigit() }

        // Periksa apakah sender berupa nomor telepon
        val isPhone = (trimmed.startsWith("+62") || trimmed.startsWith("62") || trimmed.startsWith("08")) &&
                digitsOnly.length in 9..16

        if (isPhone) {
            // Standarisasi awalan
            val normalizedDigits = when {
                trimmed.startsWith("+62") -> digitsOnly
                trimmed.startsWith("62") -> digitsOnly
                trimmed.startsWith("0") -> "62" + digitsOnly.substring(1)
                else -> digitsOnly
            }

            val prefix = if (normalizedDigits.length >= 5) {
                "+62 " + normalizedDigits.substring(2, 5)
            } else {
                "+62"
            }

            val last2 = if (normalizedDigits.length >= 2) {
                normalizedDigits.takeLast(2)
            } else {
                "**"
            }

            val masked = "$prefix **** **$last2"
            return RedactedSender(senderDisplay = masked, senderType = "PHONE")
        }

        // Periksa apakah berasal dari aplikasi pesan personal atau berupa nama kontak personal
        val isMessagingApp = !packageName.isNullOrBlank() && Regex(
            """(?i)(whatsapp|telegram|signal|facebook\.orca|line|wechat|discord)"""
        ).containsMatchIn(packageName)
        val hasLetters = trimmed.any { it.isLetter() }
        val hasSpaces = trimmed.contains(" ")

        if (hasLetters && (isMessagingApp || hasSpaces)) {
            return RedactedSender(
                senderDisplay = maskPersonalName(trimmed),
                senderType = "CONTACT"
            )
        }

        // Periksa apakah alfanumerik (nama instansi / ID pengirim resmi)
        if (hasLetters) {
            return RedactedSender(senderDisplay = trimmed, senderType = "ALPHANUMERIC")
        }

        return RedactedSender(senderDisplay = trimmed, senderType = "OTHER")
    }

    /**
     * Menyamarkan nama personal kontak per kata untuk melindungi privasi PII.
     * Contoh: "Riski IF IF" -> "R***i I* I*", "Jonatan IF" -> "J***n I*"
     */
    fun maskPersonalName(name: String): String {
        val words = name.trim().split(Regex("""\s+"""))
        return words.joinToString(" ") { word ->
            when {
                word.length <= 1 -> "*"
                word.length == 2 -> "${word.first()}*"
                else -> "${word.first()}***${word.last()}"
            }
        }
    }
}
