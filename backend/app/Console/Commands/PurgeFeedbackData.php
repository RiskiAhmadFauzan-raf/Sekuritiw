<?php

namespace App\Console\Commands;

use App\Models\ThreatFeedback;
use Illuminate\Console\Command;

/**
 * Command untuk membersihkan dan meminimalkan data laporan feedback
 * sesuai aturan retensi data privasi (Perubahan 5 - UU PDP).
 */
class PurgeFeedbackData extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'feedback:purge';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Menjalankan retensi data feedback: meminimalkan laporan reviewed >30 hari dan menghapus laporan pending >90 hari';

    /**
     * Execute the console command.
     */
    public function handle(): int
    {
        $daysReviewed = (int) env('FEEDBACK_RETENTION_REVIEWED_DAYS', 30);
        $daysPending = (int) env('FEEDBACK_RETENTION_PENDING_DAYS', 90);

        $cutoffReviewed = now()->subDays($daysReviewed);
        $cutoffPending = now()->subDays($daysPending);

        // 1. Hapus teks sensitif pada feedback yang sudah direview >30 hari (simpan metadata agregat)
        $reviewedCount = ThreatFeedback::whereIn('status', ['REVIEWED', 'DATASET_EXPORTED'])
            ->where('created_at', '<', $cutoffReviewed)
            ->where(function ($query) {
                $query->whereNotNull('text_redacted')
                      ->orWhereNotNull('snippet')
                      ->orWhereNotNull('user_notes')
                      ->orWhereNotNull('user_note_redacted')
                      ->orWhereNotNull('sender_display');
            })
            ->update([
                'text_redacted'      => null,
                'snippet'            => null,
                'user_notes'         => null,
                'user_note_redacted' => null,
                'sender_display'     => '[DIHAPUS_RETENSI]',
                'sender'             => '[DIHAPUS_RETENSI]',
            ]);

        // 2. Hapus penuh laporan berstatus PENDING yang tidak ditinjau >90 hari
        $pendingCount = ThreatFeedback::where('status', 'PENDING')
            ->where('created_at', '<', $cutoffPending)
            ->delete();

        $this->info("Retensi feedback berhasil diproses.");
        $this->info("- Laporan reviewed dibersihkan teksnya: {$reviewedCount} baris (>{$daysReviewed} hari).");
        $this->info("- Laporan pending dihapus penuh: {$pendingCount} baris (>{$daysPending} hari).");

        return Command::SUCCESS;
    }
}
