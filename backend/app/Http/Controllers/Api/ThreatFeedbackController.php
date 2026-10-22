<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\ThreatFeedback;
use App\Models\ThreatRecord;
use App\Services\SensitiveDataRedactor;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

/**
 * Controller untuk mengelola feedback salah deteksi dari pengguna.
 * Menerapkan prinsip privasi UU PDP (minimisasi data, penyamaran berlapis, consent).
 */
class ThreatFeedbackController extends Controller
{
    /**
     * Menyimpan feedback salah pendeteksian baru dari aplikasi Android.
     * POST /api/threats/feedback
     */
    public function store(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            // Field standar & alias privasi
            'feedback_type'       => 'nullable|in:FALSE_POSITIVE,FALSE_NEGATIVE',
            'user_reported_label' => 'nullable|in:SAFE,CAUTIOUS,DANGEROUS',
            'reason_category'     => 'nullable|string|max:100',
            'category'            => 'nullable|string|max:100',
            'label_user'          => 'nullable|in:SAFE,CAUTIOUS,DANGEROUS',
            'label_original'      => 'nullable|string|max:20',
            'user_notes'          => 'nullable|string|max:1000',
            'user_note_redacted'  => 'nullable|string|max:1000',
            'device_record_id'    => 'nullable|string|max:255',
            'threat_record_id'    => 'nullable|exists:threat_records,id',
            'package_name'        => 'nullable|string|max:100',
            'app_package'         => 'nullable|string|max:100',
            'sender'              => 'nullable|string|max:255',
            'sender_display'      => 'nullable|string|max:255',
            'sender_type'         => 'nullable|string|max:30',
            'snippet'             => 'nullable|string|max:2000',
            'text_redacted'       => 'nullable|string|max:2000',
            'original_risk_level' => 'nullable|string|max:20',
            'original_category'   => 'nullable|string|max:50',
            'consent_version'     => 'required|string|max:20', // Wajib consent (Perubahan 6)
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status'  => 'error',
                'message' => 'Data feedback tidak valid atau persetujuan privasi belum diberikan.',
                'errors'  => $validator->errors(),
            ], 422);
        }

        $input = $validator->validated();

        // Normalisasi field
        $feedbackType = $input['feedback_type'] ?? (
            ($input['label_user'] ?? $input['user_reported_label'] ?? '') === 'SAFE' ? 'FALSE_POSITIVE' : 'FALSE_NEGATIVE'
        );
        $userReportedLabel = $input['user_reported_label'] ?? $input['label_user'] ?? ($feedbackType === 'FALSE_POSITIVE' ? 'SAFE' : 'DANGEROUS');
        $reasonCategory = $input['reason_category'] ?? $input['category'] ?? 'Lainnya';
        $packageName = $input['package_name'] ?? $input['app_package'] ?? null;
        $originalRisk = $input['original_risk_level'] ?? $input['label_original'] ?? null;

        // 1. Penyamaran Pertahanan Berlapis di Server (Defense in Depth)
        $rawText = $input['text_redacted'] ?? $input['snippet'] ?? '';
        $redactedText = SensitiveDataRedactor::redactText($rawText);

        $rawNotes = $input['user_note_redacted'] ?? $input['user_notes'] ?? null;
        $redactedNotes = $rawNotes ? SensitiveDataRedactor::redactText($rawNotes) : null;

        $rawSender = $input['sender_display'] ?? $input['sender'] ?? null;
        $senderInfo = SensitiveDataRedactor::redactSender($rawSender, $packageName);
        $finalSenderType = in_array($senderInfo['sender_type'], ['PHONE', 'CONTACT'])
            ? $senderInfo['sender_type']
            : ($input['sender_type'] ?? $senderInfo['sender_type']);

        // 2. Pemisahan Identitas (Anonymity & Zero User ID Linkage - Perubahan 4)
        $reporterSecret = config('app.key') ?: 'sekuritiw-secret-hash-key';
        $reporterIdentifier = (string) ($request->user()?->id ?: $request->ip() ?: 'anonymous');
        $reporterHash = hash_hmac('sha256', $reporterIdentifier, $reporterSecret);

        // 3. Simpan data yang sudah dimitigasi privasinya
        $data = [
            'threat_record_id'    => $input['threat_record_id'] ?? null,
            'device_record_id'    => $input['device_record_id'] ?? null,
            'package_name'        => $packageName,
            'sender'              => $senderInfo['sender_display'], // tidak pernah simpan nomor HP atau nama kontak mentah
            'sender_display'      => $senderInfo['sender_display'],
            'sender_type'         => $finalSenderType,
            'snippet'             => $redactedText,                 // tidak pernah simpan teks mentah
            'text_redacted'       => $redactedText,
            'original_risk_level' => $originalRisk,
            'original_category'   => $input['original_category'] ?? null,
            'feedback_type'       => $feedbackType,
            'user_reported_label' => $userReportedLabel,
            'reason_category'     => $reasonCategory,
            'user_notes'          => $redactedNotes,
            'user_note_redacted'  => $redactedNotes,
            'status'              => 'PENDING',
            'consent_version'     => $input['consent_version'],
            'consent_at'          => now(),
            'reporter_hash'       => $reporterHash,
        ];

        // Auto-match ke ThreatRecord jika ada
        if (empty($data['threat_record_id']) && !empty($data['device_record_id'])) {
            $matchingRecord = ThreatRecord::where('device_record_id', $data['device_record_id'])->first();
            if ($matchingRecord) {
                $data['threat_record_id'] = $matchingRecord->id;
                if (empty($data['package_name'])) $data['package_name'] = $matchingRecord->package_name;
                if (empty($data['original_risk_level'])) $data['original_risk_level'] = $matchingRecord->risk_level;
                if (empty($data['original_category'])) $data['original_category'] = $matchingRecord->threat_category;
            }
        }

        // 4. Pencegahan Duplikasi: Pastikan pengguna hanya dapat mengirim feedback 1 kali per pesan
        $existingFeedback = null;
        if (!empty($data['device_record_id'])) {
            $existingFeedback = ThreatFeedback::where('device_record_id', $data['device_record_id'])->first();
        }
        if (!$existingFeedback && !empty($data['reporter_hash']) && !empty($data['snippet'])) {
            $existingFeedback = ThreatFeedback::where('reporter_hash', $data['reporter_hash'])
                ->where('snippet', $data['snippet'])
                ->first();
        }

        if ($existingFeedback) {
            return response()->json([
                'status'  => 'success',
                'message' => 'Laporan untuk pesan ini sudah pernah dicatat sebelumnya.',
                'data'    => [
                    'id'            => $existingFeedback->id,
                    'feedback_type' => $existingFeedback->feedback_type,
                    'status'        => $existingFeedback->status,
                    'created_at'    => $existingFeedback->created_at,
                ],
            ], 200);
        }

        $feedback = ThreatFeedback::create($data);

        return response()->json([
            'status'  => 'success',
            'message' => 'Terima kasih! Feedback Anda telah disamarkan dan berhasil dicatat dengan aman.',
            'data'    => [
                'id'            => $feedback->id,
                'feedback_type' => $feedback->feedback_type,
                'status'        => $feedback->status,
                'created_at'    => $feedback->created_at,
            ],
        ], 201);
    }

    /**
     * Mengambil daftar feedback untuk tim analis.
     * GET /api/threats/feedback
     */
    public function index(Request $request): JsonResponse
    {
        $query = ThreatFeedback::orderByDesc('created_at');

        if ($request->filled('feedback_type')) {
            $query->where('feedback_type', $request->feedback_type);
        }
        if ($request->filled('status')) {
            $query->where('status', $request->status);
        }
        if ($request->filled('reason_category')) {
            $query->where('reason_category', $request->reason_category);
        }

        $feedbacks = $query->paginate(25);

        return response()->json([
            'status' => 'success',
            'data'   => $feedbacks,
        ]);
    }

    /**
     * Ringkasan statistik feedback salah deteksi.
     * GET /api/threats/feedback/stats
     */
    public function stats(): JsonResponse
    {
        $stats = ThreatFeedback::selectRaw('
            COUNT(*) as total_feedbacks,
            SUM(CASE WHEN feedback_type = "FALSE_POSITIVE" THEN 1 ELSE 0 END) as false_positives,
            SUM(CASE WHEN feedback_type = "FALSE_NEGATIVE" THEN 1 ELSE 0 END) as false_negatives,
            SUM(CASE WHEN status = "PENDING" THEN 1 ELSE 0 END) as pending_review,
            SUM(CASE WHEN status = "REVIEWED" THEN 1 ELSE 0 END) as reviewed,
            SUM(CASE WHEN status = "DATASET_EXPORTED" THEN 1 ELSE 0 END) as added_to_dataset
        ')->first();

        $byReason = ThreatFeedback::selectRaw('reason_category, COUNT(*) as count')
            ->groupBy('reason_category')
            ->orderByDesc('count')
            ->get();

        return response()->json([
            'status'     => 'success',
            'summary'    => $stats,
            'by_reasons' => $byReason,
        ]);
    }

    /**
     * Mengubah status feedback.
     * PATCH /api/threats/feedback/{id}/status
     */
    public function updateStatus(Request $request, ThreatFeedback $feedback): JsonResponse
    {
        $validated = $request->validate([
            'status' => 'required|in:PENDING,REVIEWED,DATASET_EXPORTED,REJECTED',
        ]);

        $feedback->update(['status' => $validated['status']]);

        return response()->json([
            'status'  => 'success',
            'message' => 'Status feedback berhasil diperbarui.',
            'data'    => $feedback,
        ]);
    }
}
