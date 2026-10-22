<?php

use App\Models\ThreatFeedback;
use App\Services\SensitiveDataRedactor;
use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Jalankan penyamaran retroaktif pada baris database feedback lama
     * yang masih memuat nama kontak pribadi dari aplikasi pesan (WhatsApp, dll).
     */
    public function up(): void
    {
        if (!Schema::hasTable('threat_feedbacks')) {
            return;
        }

        $feedbacks = ThreatFeedback::all();

        foreach ($feedbacks as $fb) {
            $sender = $fb->sender_display ?: $fb->sender;
            if (empty($sender)) {
                continue;
            }

            $info = SensitiveDataRedactor::redactSender($sender, $fb->package_name);

            // Jika terdeteksi disamarkan atau berubah tipe
            if ($info['sender_display'] !== $sender || $info['sender_type'] !== $fb->sender_type) {
                $fb->update([
                    'sender'         => $info['sender_display'],
                    'sender_display' => $info['sender_display'],
                    'sender_type'    => $info['sender_type'],
                ]);
            }
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        // Data tersamar tidak dapat dikembalikan secara otomatis (one-way privacy minimization)
    }
};
