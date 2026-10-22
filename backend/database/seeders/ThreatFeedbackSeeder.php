<?php

namespace Database\Seeders;

use App\Models\ThreatFeedback;
use Illuminate\Database\Seeder;

class ThreatFeedbackSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $feedbacks = [
            [
                'feedback_type'       => 'FALSE_POSITIVE',
                'user_reported_label' => 'SAFE',
                'reason_category'     => 'OFFICIAL_BANK_NOTIFICATION',
                'user_notes'          => 'Ini SMS resmi notifikasi mutasi dari Bank BCA, bukan modus penipuan transfer.',
                'device_record_id'    => 'dev_rec_001',
                'package_name'        => 'com.google.android.apps.messaging',
                'sender'              => 'BANK-BCA',
                'snippet'             => 'Rekening 1234567890 berhasil didebit Rp 75.000 untuk transaksi QRIS di Indomaret. Info: 1500888',
                'original_risk_level' => 'DANGEROUS',
                'original_category'   => 'FINANCIAL_SCAM',
                'status'              => 'REVIEWED',
            ],
            [
                'feedback_type'       => 'FALSE_POSITIVE',
                'user_reported_label' => 'SAFE',
                'reason_category'     => 'OFFICIAL_OTP_SERVICE',
                'user_notes'          => 'Kode OTP login Tokopedia resmi, tapi terdeteksi cautious phishing.',
                'device_record_id'    => 'dev_rec_002',
                'package_name'        => 'com.google.android.apps.messaging',
                'sender'              => 'TOKOPEDIA',
                'snippet'             => 'KODE VERIFIKASI: 492011. JANGAN berikan kode ini kepada siapapun termasuk pihak Tokopedia.',
                'original_risk_level' => 'CAUTIOUS',
                'original_category'   => 'PHISHING',
                'status'              => 'DATASET_EXPORTED',
            ],
            [
                'feedback_type'       => 'FALSE_NEGATIVE',
                'user_reported_label' => 'DANGEROUS',
                'reason_category'     => 'APK_SCAM_PACKAGE',
                'user_notes'          => 'Modus kirim file APK kurir ekspedisi resi palsu via WhatsApp, deteksi awal bilang aman.',
                'device_record_id'    => 'dev_rec_003',
                'package_name'        => 'com.whatsapp',
                'sender'              => '+62 821-9988-1234',
                'snippet'             => 'Selamat siang, ada paket J&T yang belum terantar. Silakan cek resi dan foto paket di link berikut: bit.ly/Resi-JNT-Update.apk',
                'original_risk_level' => 'SAFE',
                'original_category'   => 'BENIGN',
                'status'              => 'PENDING',
            ],
            [
                'feedback_type'       => 'FALSE_NEGATIVE',
                'user_reported_label' => 'DANGEROUS',
                'reason_category'     => 'TRAFFIC_TICKET_APK_SCAM',
                'user_notes'          => 'Surat tilang ETLE palsu menyuruh unduh aplikasi file APK berbahaya.',
                'device_record_id'    => 'dev_rec_004',
                'package_name'        => 'com.whatsapp',
                'sender'              => '+62 857-1122-3344',
                'snippet'             => 'Pemberitahuan Pelanggaran Lalu Lintas ETLE No. 9812/2026. Unduh surat bukti tilang resmi Anda: https://etle-polri-korlantas.apk-drive.xyz',
                'original_risk_level' => 'SAFE',
                'original_category'   => 'BENIGN',
                'status'              => 'PENDING',
            ],
            [
                'feedback_type'       => 'FALSE_POSITIVE',
                'user_reported_label' => 'SAFE',
                'reason_category'     => 'UTILITY_BILL_NOTIFICATION',
                'user_notes'          => 'Pemberitahuan tagihan listrik pascabayar PLN resmi.',
                'device_record_id'    => 'dev_rec_005',
                'package_name'        => 'com.google.android.apps.messaging',
                'sender'              => 'PLN-INFO',
                'snippet'             => 'Yth. Pelanggan ID 512345678, tagihan listrik periode berjalan Rp 324.500 telah terbit. Bayar tepat waktu sebelum tanggal 20.',
                'original_risk_level' => 'CAUTIOUS',
                'original_category'   => 'SOCIAL_ENGINEERING',
                'status'              => 'REVIEWED',
            ],
        ];

        foreach ($feedbacks as $item) {
            ThreatFeedback::create($item);
        }
    }
}
