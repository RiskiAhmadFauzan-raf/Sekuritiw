package com.sekuritiw.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Test untuk SensitiveDataRedactor sesuai Spesifikasi Privasi Feedback SEKURITIW (Bagian 6).
 */
class SensitiveDataRedactorTest {

    @Test
    fun testRedactOtpCode() {
        val input = "Kode verifikasi: 492011. Jangan bagikan."
        val expected = "Kode verifikasi: [KODE]. Jangan bagikan."
        val actual = SensitiveDataRedactor.redactText(input)
        assertEquals(expected, actual)

        val inputInterveningWords = "kode otp login anda 0404"
        val expectedIntervening = "kode otp login anda [KODE]"
        assertEquals(expectedIntervening, SensitiveDataRedactor.redactText(inputInterveningWords))

        val inputPrecedingDigits = "0404 adalah kode verifikasi akun anda"
        val expectedPreceding = "[KODE] adalah kode verifikasi akun anda"
        assertEquals(expectedPreceding, SensitiveDataRedactor.redactText(inputPrecedingDigits))
    }

    @Test
    fun testRedactBankAccountAndCurrency() {
        val input = "Rekening 1234567890 berhasil didebit Rp 1.500.000"
        val expected = "Rekening [NOMOR] berhasil didebit Rp[NOMINAL]"
        val actual = SensitiveDataRedactor.redactText(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testRedactPhoneFormats() {
        val variations = listOf(
            "+62 821-9988-1234",
            "0821 9988 1234",
            "6282199881234"
        )
        for (phone in variations) {
            val result = SensitiveDataRedactor.redactText("Hubungi nomor $phone sekarang")
            assertEquals("Hubungi nomor [TELEPON] sekarang", result)
        }
    }

    @Test
    fun testRedactEmail() {
        val input = "hubungi cs@contoh.com"
        val expected = "hubungi [EMAIL]"
        val actual = SensitiveDataRedactor.redactText(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testRedactUrl() {
        val input = "klik https://bca-klaim.example.com/login?id=123"
        val expected = "klik [URL: bca-klaim.example.com]"
        val actual = SensitiveDataRedactor.redactText(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testTextWithoutSensitiveDataRemainsUnchanged() {
        val input = "Halo selamat siang, apakah paket saya sudah sampai di kantor cabang?"
        val actual = SensitiveDataRedactor.redactText(input)
        assertEquals(input, actual)
    }

    @Test
    fun testSenderRedaction() {
        // Pengirim nomor telepon
        val phoneSender = SensitiveDataRedactor.redactSender("+62 821-9988-1234")
        assertEquals("+62 821 **** **34", phoneSender.senderDisplay)
        assertEquals("PHONE", phoneSender.senderType)

        val localPhoneSender = SensitiveDataRedactor.redactSender("081234567890")
        assertEquals("+62 812 **** **90", localPhoneSender.senderDisplay)
        assertEquals("PHONE", localPhoneSender.senderType)

        // Pengirim ID alfanumerik instansi resmi
        val bankSender = SensitiveDataRedactor.redactSender("BANK-BCA")
        assertEquals("BANK-BCA", bankSender.senderDisplay)
        assertEquals("ALPHANUMERIC", bankSender.senderType)

        val tokpedSender = SensitiveDataRedactor.redactSender("TOKOPEDIA")
        assertEquals("TOKOPEDIA", tokpedSender.senderDisplay)
        assertEquals("ALPHANUMERIC", tokpedSender.senderType)

        // Pengirim kontak WhatsApp personal
        val waContact = SensitiveDataRedactor.redactSender("Riski IF IF", "com.whatsapp")
        assertEquals("R***i I* I*", waContact.senderDisplay)
        assertEquals("CONTACT", waContact.senderType)

        val jonaContact = SensitiveDataRedactor.redactSender("Jonatan  IF", "com.whatsapp")
        assertEquals("J***n I*", jonaContact.senderDisplay)
        assertEquals("CONTACT", jonaContact.senderType)

        // Pengirim nama kontak ber-spasi tanpa package
        val spaceContact = SensitiveDataRedactor.redactSender("Budi Santoso")
        assertEquals("B***i S***o", spaceContact.senderDisplay)
        assertEquals("CONTACT", spaceContact.senderType)
    }

    @Test
    fun testEmptyAndVeryLongInputsDoNotCrash() {
        assertEquals("", SensitiveDataRedactor.redactText(""))
        assertEquals("", SensitiveDataRedactor.redactText(null))

        val longInput = "Pesan uji ".repeat(5000)
        val result = SensitiveDataRedactor.redactText(longInput)
        assertTrue(result.isNotEmpty())
    }
}
