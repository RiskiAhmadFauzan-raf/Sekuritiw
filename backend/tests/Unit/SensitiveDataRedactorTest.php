<?php

namespace Tests\Unit;

use App\Services\SensitiveDataRedactor;
use PHPUnit\Framework\TestCase;

class SensitiveDataRedactorTest extends TestCase
{
    public function test_redact_otp_code()
    {
        $input = "Kode verifikasi: 492011. Jangan bagikan.";
        $expected = "Kode verifikasi: [KODE]. Jangan bagikan.";
        $this->assertEquals($expected, SensitiveDataRedactor::redactText($input));

        $inputIntervening = "kode otp login anda 0404";
        $expectedIntervening = "kode otp login anda [KODE]";
        $this->assertEquals($expectedIntervening, SensitiveDataRedactor::redactText($inputIntervening));

        $inputPreceding = "0404 adalah kode verifikasi akun anda";
        $expectedPreceding = "[KODE] adalah kode verifikasi akun anda";
        $this->assertEquals($expectedPreceding, SensitiveDataRedactor::redactText($inputPreceding));
    }

    public function test_redact_bank_account_and_nominal()
    {
        $input = "Rekening 1234567890 berhasil didebit Rp 1.500.000";
        $expected = "Rekening [NOMOR] berhasil didebit Rp[NOMINAL]";
        $this->assertEquals($expected, SensitiveDataRedactor::redactText($input));
    }

    public function test_redact_phone_number_variations()
    {
        $phones = [
            '+62 821-9988-1234',
            '0821 9988 1234',
            '6282199881234',
        ];

        foreach ($phones as $phone) {
            $result = SensitiveDataRedactor::redactText("Hubungi nomor {$phone} sekarang");
            $this->assertEquals("Hubungi nomor [TELEPON] sekarang", $result);
        }
    }

    public function test_redact_email()
    {
        $input = "hubungi cs@contoh.com";
        $expected = "hubungi [EMAIL]";
        $this->assertEquals($expected, SensitiveDataRedactor::redactText($input));
    }

    public function test_redact_url()
    {
        $input = "klik https://bca-klaim.example.com/login?id=123";
        $expected = "klik [URL: bca-klaim.example.com]";
        $this->assertEquals($expected, SensitiveDataRedactor::redactText($input));
    }

    public function test_text_without_sensitive_data_remains_unchanged()
    {
        $input = "Halo selamat siang, apakah paket saya sudah sampai?";
        $this->assertEquals($input, SensitiveDataRedactor::redactText($input));
    }

    public function test_redact_sender()
    {
        $phoneSender = SensitiveDataRedactor::redactSender("+62 821-9988-1234");
        $this->assertEquals("+62 821 **** **34", $phoneSender['sender_display']);
        $this->assertEquals("PHONE", $phoneSender['sender_type']);

        $localPhoneSender = SensitiveDataRedactor::redactSender("081234567890");
        $this->assertEquals("+62 812 **** **90", $localPhoneSender['sender_display']);
        $this->assertEquals("PHONE", $localPhoneSender['sender_type']);

        $bankSender = SensitiveDataRedactor::redactSender("BANK-BCA");
        $this->assertEquals("BANK-BCA", $bankSender['sender_display']);
        $this->assertEquals("ALPHANUMERIC", $bankSender['sender_type']);

        // WhatsApp personal contact name redaction
        $whatsappContact = SensitiveDataRedactor::redactSender("Jonatan  IF", "com.whatsapp");
        $this->assertEquals("J***n I*", $whatsappContact['sender_display']);
        $this->assertEquals("CONTACT", $whatsappContact['sender_type']);

        // Personal contact name with multiple words without package
        $contactWithSpaces = SensitiveDataRedactor::redactSender("Budi Santoso");
        $this->assertEquals("B***i S***o", $contactWithSpaces['sender_display']);
        $this->assertEquals("CONTACT", $contactWithSpaces['sender_type']);

        // Single-word contact on messaging app
        $singleWordContact = SensitiveDataRedactor::redactSender("Budi", "org.telegram.messenger");
        $this->assertEquals("B***i", $singleWordContact['sender_display']);
        $this->assertEquals("CONTACT", $singleWordContact['sender_type']);
    }

    public function test_empty_and_very_long_input_do_not_crash()
    {
        $this->assertEquals("", SensitiveDataRedactor::redactText(""));
        $this->assertEquals("", SensitiveDataRedactor::redactText(null));

        $longInput = str_repeat("Pesan pengujian ", 2000);
        $result = SensitiveDataRedactor::redactText($longInput);
        $this->assertNotEmpty($result);
    }
}
