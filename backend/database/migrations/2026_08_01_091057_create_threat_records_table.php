<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * Tabel threat_records: menyimpan riwayat ancaman yang disinkronisasi
 * dari aplikasi Android pengguna ke server backend.
 *
 * Requirements: 10.5, 10.6, 15.1, 15.2, 15.3
 */
return new class extends Migration
{
    public function up(): void
    {
        Schema::create('threat_records', function (Blueprint $table) {
            $table->id();

            // Referensi ke user pemilik (nullable untuk anonim)
            $table->foreignId('user_id')->nullable()->constrained()->nullOnDelete();

            // ID unik dari perangkat Android (UUID)
            $table->string('device_record_id')->unique();

            // Metadata notifikasi
            $table->string('package_name', 100);
            $table->string('sender')->nullable();
            $table->text('raw_text');
            $table->text('normalized_text');

            // Hasil klasifikasi ancaman
            $table->string('threat_category', 50); // PHISHING, APK_SCAM, etc.
            $table->string('risk_level', 20);       // SAFE, CAUTIOUS, DANGEROUS
            $table->string('severity_rating', 20);  // LOW, MEDIUM, HIGH, CRITICAL
            $table->float('risk_score');            // 0.0 - 100.0

            // Penjelasan AI
            $table->text('reasoning')->nullable();

            // Status sinkronisasi & verifikasi komunitas
            $table->boolean('is_verified')->default(false);
            $table->unsignedSmallInteger('community_reports')->default(0);

            // Timestamp notifikasi di perangkat
            $table->timestamp('notified_at');
            $table->timestamps();

            // Index untuk query performa
            $table->index(['user_id', 'created_at']);
            $table->index(['threat_category', 'risk_level']);
            $table->index('risk_score');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('threat_records');
    }
};
