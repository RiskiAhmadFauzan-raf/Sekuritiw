<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * Tabel threat_feedbacks: menyimpan umpan balik (feedback) dari pengguna terkait
 * salah pendeteksian (False Positive & False Negative) untuk evaluasi dan dataset retraining AI.
 */
return new class extends Migration
{
    public function up(): void
    {
        Schema::create('threat_feedbacks', function (Blueprint $table) {
            $table->id();

            // Referensi user (opsional jika user login / anonim)
            $table->foreignId('user_id')->nullable()->constrained()->nullOnDelete();

            // Referensi record ancaman lokal atau backend
            $table->foreignId('threat_record_id')->nullable()->constrained('threat_records')->nullOnDelete();
            $table->string('device_record_id')->nullable()->index();

            // Metadata pesan saat dideteksi
            $table->string('package_name', 100)->nullable();
            $table->string('sender')->nullable();
            $table->text('snippet')->nullable(); // Cuplikan teks pesan (tersanitasi)

            // Label awal dari sistem deteksi
            $table->string('original_risk_level', 20)->nullable(); // SAFE, CAUTIOUS, DANGEROUS
            $table->string('original_category', 50)->nullable();   // PHISHING, APK_SCAM, SOCIAL_ENGINEERING, dll.

            // Umpan balik dari pengguna
            $table->enum('feedback_type', ['FALSE_POSITIVE', 'FALSE_NEGATIVE']); // Salah tuduh vs Lolos deteksi
            $table->string('user_reported_label', 20)->default('SAFE');         // SAFE, CAUTIOUS, DANGEROUS
            $table->string('reason_category', 100);                              // OFFICIAL_SERVICE, PERSONAL_CONTACT, NEW_SCAM, dll.
            $table->text('user_notes')->nullable();                              // Catatan/penjelasan pengguna

            // Status evaluasi untuk tim analis / dataset MLOps
            $table->enum('status', ['PENDING', 'REVIEWED', 'DATASET_EXPORTED', 'REJECTED'])->default('PENDING');

            $table->timestamps();

            // Index pencarian & statistik
            $table->index(['feedback_type', 'status']);
            $table->index('reason_category');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('threat_feedbacks');
    }
};
