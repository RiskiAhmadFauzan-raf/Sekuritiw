<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        // 1. Tambah role pada tabel users jika belum ada
        if (!Schema::hasColumn('users', 'role')) {
            Schema::table('users', function (Blueprint $table) {
                $table->string('role', 20)->default('analyst')->after('password');
            });
        }

        // 2. Perbarui skema tabel threat_feedbacks sesuai spesifikasi privasi
        Schema::table('threat_feedbacks', function (Blueprint $table) {
            // Kolom data tersamar (privacy-minimized)
            $table->text('text_redacted')->nullable()->after('snippet');
            $table->string('sender_display', 255)->nullable()->after('sender');
            $table->string('sender_type', 30)->default('OTHER')->after('sender_display');
            $table->text('user_note_redacted')->nullable()->after('user_notes');
            
            // Persetujuan (consent) dan pemisahan identitas (anonymity)
            $table->string('consent_version', 20)->default('v1')->after('status');
            $table->timestamp('consent_at')->nullable()->after('consent_version');
            $table->string('reporter_hash', 64)->nullable()->index()->after('consent_at');

            // Hapus user_id untuk memutus tautan identitas pelapor (Perubahan 4)
            if (Schema::hasColumn('threat_feedbacks', 'user_id')) {
                // Drop foreign key jika ada, lalu drop column
                try {
                    $table->dropForeign(['user_id']);
                } catch (\Throwable $e) {}
                $table->dropColumn('user_id');
            }
        });

        // 3. Buat tabel audit log akses feedback (Perubahan 3)
        Schema::create('feedback_access_logs', function (Blueprint $table) {
            $table->id();
            $table->foreignId('analyst_id')->nullable()->constrained('users')->nullOnDelete();
            $table->foreignId('feedback_id')->constrained('threat_feedbacks')->cascadeOnDelete();
            $table->string('action', 50); // REVEAL, STATUS_CHANGE, EXPORT
            $table->string('ip_address', 45)->nullable();
            $table->timestamp('created_at')->useCurrent();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('feedback_access_logs');

        Schema::table('threat_feedbacks', function (Blueprint $table) {
            $table->dropColumn([
                'text_redacted',
                'sender_display',
                'sender_type',
                'user_note_redacted',
                'consent_version',
                'consent_at',
                'reporter_hash'
            ]);
        });

        if (Schema::hasColumn('users', 'role')) {
            Schema::table('users', function (Blueprint $table) {
                $table->dropColumn('role');
            });
        }
    }
};
