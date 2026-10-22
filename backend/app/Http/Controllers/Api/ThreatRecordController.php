<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\ThreatRecord;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

/**
 * Controller sinkronisasi riwayat ancaman dari Android ke backend Laravel.
 *
 * Requirements: 10.5, 10.6, 15.1, 15.2, 15.3, 15.4
 */
class ThreatRecordController extends Controller
{
    /**
     * Sinkronisasi batch riwayat ancaman dari perangkat Android.
     * POST /api/threats/sync
     *
     * Menerima array records yang belum disinkronisasi (isSynced = false)
     * dari database lokal Android dan menyimpannya di server.
     */
    public function sync(Request $request): JsonResponse
    {
        $request->validate([
            'records'                        => ['required', 'array', 'min:1', 'max:50'],
            'records.*.device_record_id'     => ['required', 'string', 'max:64'],
            'records.*.package_name'         => ['required', 'string', 'max:100'],
            'records.*.sender'               => ['nullable', 'string', 'max:255'],
            'records.*.raw_text'             => ['required', 'string'],
            'records.*.normalized_text'      => ['required', 'string'],
            'records.*.threat_category'      => ['required', 'string', 'in:PHISHING,APK_SCAM,SOCIAL_ENGINEERING,SPAM,FINANCIAL_SCAM,BENIGN,UNKNOWN'],
            'records.*.risk_level'           => ['required', 'string', 'in:SAFE,CAUTIOUS,DANGEROUS'],
            'records.*.severity_rating'      => ['required', 'string', 'in:LOW,MEDIUM,HIGH,CRITICAL'],
            'records.*.risk_score'           => ['required', 'numeric', 'min:0', 'max:100'],
            'records.*.reasoning'            => ['nullable', 'string'],
            'records.*.notified_at'          => ['required', 'integer'], // Unix timestamp ms
        ]);

        $userId = $request->user()->id;
        $synced = [];
        $skipped = [];

        DB::transaction(function () use ($request, $userId, &$synced, &$skipped) {
            foreach ($request->records as $rec) {
                // Skip jika sudah ada di server (idempotent sync)
                $exists = ThreatRecord::where('device_record_id', $rec['device_record_id'])->exists();

                if ($exists) {
                    $skipped[] = $rec['device_record_id'];
                    continue;
                }

                ThreatRecord::create([
                    'user_id'          => $userId,
                    'device_record_id' => $rec['device_record_id'],
                    'package_name'     => $rec['package_name'],
                    'sender'           => $rec['sender'] ?? null,
                    'raw_text'         => $rec['raw_text'],
                    'normalized_text'  => $rec['normalized_text'],
                    'threat_category'  => $rec['threat_category'],
                    'risk_level'       => $rec['risk_level'],
                    'severity_rating'  => $rec['severity_rating'],
                    'risk_score'       => $rec['risk_score'],
                    'reasoning'        => $rec['reasoning'] ?? null,
                    'notified_at'      => now()->setTimestampMs($rec['notified_at']),
                ]);

                $synced[] = $rec['device_record_id'];
            }
        });

        return response()->json([
            'message'      => 'Sinkronisasi selesai.',
            'synced_count' => count($synced),
            'skipped_count'=> count($skipped),
            'synced_ids'   => $synced,
        ]);
    }

    /**
     * Mengambil riwayat ancaman milik pengguna yang login.
     * GET /api/threats
     */
    public function index(Request $request): JsonResponse
    {
        $query = ThreatRecord::where('user_id', $request->user()->id)
            ->orderByDesc('notified_at');

        // Filter opsional
        if ($request->filled('category')) {
            $query->ofCategory($request->category);
        }
        if ($request->filled('risk_level')) {
            $query->where('risk_level', $request->risk_level);
        }

        $records = $query->paginate(20);

        return response()->json($records);
    }

    /**
     * Detail satu riwayat ancaman.
     * GET /api/threats/{id}
     */
    public function show(Request $request, ThreatRecord $threatRecord): JsonResponse
    {
        // Pastikan user hanya bisa lihat miliknya sendiri
        if ($threatRecord->user_id !== $request->user()->id) {
            return response()->json(['message' => 'Tidak ditemukan.'], 404);
        }

        return response()->json($threatRecord);
    }

    /**
     * Statistik ringkasan ancaman milik user.
     * GET /api/threats/stats
     */
    public function stats(Request $request): JsonResponse
    {
        $userId = $request->user()->id;

        $stats = ThreatRecord::where('user_id', $userId)
            ->selectRaw('
                COUNT(*) as total,
                SUM(CASE WHEN risk_level = "DANGEROUS" THEN 1 ELSE 0 END) as dangerous_count,
                SUM(CASE WHEN risk_level = "CAUTIOUS" THEN 1 ELSE 0 END) as cautious_count,
                SUM(CASE WHEN risk_level = "SAFE" THEN 1 ELSE 0 END) as safe_count,
                AVG(risk_score) as avg_risk_score
            ')
            ->first();

        $byCategory = ThreatRecord::where('user_id', $userId)
            ->selectRaw('threat_category, COUNT(*) as count')
            ->groupBy('threat_category')
            ->orderByDesc('count')
            ->get();

        return response()->json([
            'summary'     => $stats,
            'by_category' => $byCategory,
        ]);
    }

    /**
     * Laporan ancaman dari komunitas (crowdsourced reporting).
     * POST /api/threats/{id}/report
     * Requirement: 15.4
     */
    public function report(ThreatRecord $threatRecord): JsonResponse
    {
        $threatRecord->increment('community_reports');

        // Auto-verify jika sudah dilaporkan >= 5 pengguna
        if ($threatRecord->community_reports >= 5 && ! $threatRecord->is_verified) {
            $threatRecord->update(['is_verified' => true]);
        }

        return response()->json([
            'message'           => 'Laporan berhasil dikirim. Terima kasih telah membantu komunitas.',
            'community_reports' => $threatRecord->fresh()->community_reports,
        ]);
    }
}
