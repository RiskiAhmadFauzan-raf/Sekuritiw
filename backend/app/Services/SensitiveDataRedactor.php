<?php

namespace App\Services;

/**
 * Layanan penyamaran data sensitif (Defense in Depth) di sisi server Laravel.
 * Sesuai prinsip UU PDP No. 27/2022 dan Spesifikasi Privasi Feedback SEKURITIW (Perubahan 2).
 */
class SensitiveDataRedactor
{
    /**
     * Menyamarkan data sensitif pada teks dengan 6 urutan ketat:
     * 1. URL -> [URL: domain.com]
     * 2. Email -> [EMAIL]
     * 3. Telepon -> [TELEPON]
     * 4. OTP / Verifikasi -> [KODE]
     * 5. Deretan 8 digit atau lebih -> [NOMOR]
     * 6. Nominal uang -> Rp[NOMINAL]
     */
    public static function redactText(?string $input): string
    {
        if (empty($input) || trim($input) === '') {
            return '';
        }

        $text = $input;

        // 1. URL: simpan domain, buang path, query parameter, dan fragment
        $text = preg_replace_callback(
            '/\bhttps?:\/\/([a-zA-Z0-9.-]+)(?::\d+)?(?:\/[^\s]*)?/i',
            function ($matches) {
                $host = strtolower(trim($matches[1]));
                return "[URL: {$host}]";
            },
            $text
        );

        // 2. Email
        $text = preg_replace('/\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b/i', '[EMAIL]', $text);

        // 3. Nomor Telepon (+62..., 08..., 62...) dengan spasi/tanda hubung
        $text = preg_replace_callback(
            '/(?:\+?62\s*|0)8[0-9\s-]{7,15}[0-9]/',
            function ($matches) {
                $digits = preg_replace('/\D/', '', $matches[0]);
                if (strlen($digits) >= 10 && strlen($digits) <= 15) {
                    return '[TELEPON]';
                }
                return $matches[0];
            },
            $text
        );

        // 4. Kode OTP / Verifikasi (4 sampai 8 digit di dekat kata kunci)
        // Pola A: Kata kunci mendahului digit (contoh: "kode otp login anda 0404")
        $text = preg_replace_callback(
            '/\b(kode(?:\s*(?:otp|verifikasi|rahasia|keamanan|masuk|login))?|otp|pin|token|password|passcode|verifikasi)\b([^0-9\r\n.;?!]{0,35}?)(\b\d{4,8}\b)/i',
            function ($matches) {
                return $matches[1] . $matches[2] . '[KODE]';
            },
            $text
        );

        // Pola B: Digit mendahului kata kunci (contoh: "0404 adalah kode verifikasi anda")
        $text = preg_replace_callback(
            '/(\b\d{4,8}\b)([^0-9\r\n.;?!]{0,35}?\b(?:kode|otp|pin|token|verifikasi|password|passcode)\b)/i',
            function ($matches) {
                return '[KODE]' . $matches[2];
            },
            $text
        );

        // 5. Deretan 8 digit atau lebih (nomor rekening, kartu debit/kredit, NIK)
        $text = preg_replace('/\b\d{8,}\b/', '[NOMOR]', $text);

        // 6. Nominal uang (Rp 1.500.000, IDR 50.000, dsb.)
        $text = preg_replace(
            '/\b(?:Rp\.?|IDR)\s*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})?|[0-9]+)\b/i',
            'Rp[NOMINAL]',
            $text
        );

        return $text;
    }

    /**
     * Menyamarkan identitas pengirim (Sender Redaction).
     * 1. Jika nomor HP: disamarkan menjadi format '+62 821 **** **34' (PHONE).
     * 2. Jika berasal dari aplikasi perpesanan personal (WhatsApp, Telegram, dll) atau
     *    berupa nama kontak personal (mengandung spasi/pola nama): disamarkan menjadi 'J***n I*' (CONTACT).
     * 3. Jika ID resmi alfanumerik (misal 'BANK-BCA', 'TOKOPEDIA', 'PLN-INFO'): dipertahankan apa adanya (ALPHANUMERIC).
     */
    public static function redactSender(?string $sender, ?string $packageName = null): array
    {
        if (empty($sender) || trim($sender) === '') {
            return [
                'sender_display' => 'Anonim',
                'sender_type'    => 'OTHER',
            ];
        }

        $trimmed = trim($sender);
        $digitsOnly = preg_replace('/\D/', '', $trimmed);
        $len = strlen($digitsOnly);

        // 1. Periksa apakah nomor telepon (+62..., 08..., 62...)
        $isPhone = (str_starts_with($trimmed, '+62') || str_starts_with($trimmed, '62') || str_starts_with($trimmed, '08')) &&
                   ($len >= 9 && $len <= 16);

        if ($isPhone) {
            $normalized = $digitsOnly;
            if (str_starts_with($trimmed, '0')) {
                $normalized = '62' . substr($digitsOnly, 1);
            }

            $prefix = strlen($normalized) >= 5 ? '+62 ' . substr($normalized, 2, 3) : '+62';
            $last2 = strlen($normalized) >= 2 ? substr($normalized, -2) : '**';

            return [
                'sender_display' => "{$prefix} **** **{$last2}",
                'sender_type'    => 'PHONE',
            ];
        }

        // 2. Periksa apakah berasal dari aplikasi pesan personal atau berupa nama kontak personal
        $isMessagingApp = !empty($packageName) && (bool) preg_match('/(whatsapp|telegram|signal|facebook\.orca|line|wechat|discord)/i', $packageName);
        $hasLetters = (bool) preg_match('/[a-zA-Z]/', $trimmed);
        $hasSpaces = (bool) preg_match('/\s+/', $trimmed);

        // Jika pengirim berasal dari aplikasi perpesanan personal (WhatsApp dll)
        // ATAU pengirim berupa nama orang (mengandung spasi seperti "Jonatan IF")
        if ($hasLetters && ($isMessagingApp || $hasSpaces)) {
            return [
                'sender_display' => self::maskPersonalName($trimmed),
                'sender_type'    => 'CONTACT',
            ];
        }

        // 3. ID Alfanumerik resmi (misal: BANK-BCA, TOKOPEDIA, TELKOMSEL)
        if ($hasLetters) {
            return [
                'sender_display' => $trimmed,
                'sender_type'    => 'ALPHANUMERIC',
            ];
        }

        return [
            'sender_display' => $trimmed,
            'sender_type'    => 'OTHER',
        ];
    }

    /**
     * Menyamarkan nama personal kontak per kata untuk melindungi privasi PII.
     * Contoh: "Jonatan IF" -> "J***n I*", "Budi Santoso" -> "B***i S***o"
     */
    public static function maskPersonalName(string $name): string
    {
        $words = preg_split('/\s+/', trim($name));
        $maskedWords = [];

        foreach ($words as $word) {
            $len = mb_strlen($word);
            if ($len <= 1) {
                $maskedWords[] = '*';
            } elseif ($len === 2) {
                $maskedWords[] = mb_substr($word, 0, 1) . '*';
            } else {
                $first = mb_substr($word, 0, 1);
                $last = mb_substr($word, -1);
                $maskedWords[] = "{$first}***{$last}";
            }
        }

        return implode(' ', $maskedWords);
    }
}
