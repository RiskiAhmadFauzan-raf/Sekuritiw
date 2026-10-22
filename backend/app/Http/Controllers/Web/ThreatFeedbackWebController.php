<?php

namespace App\Http\Controllers\Web;

use App\Http\Controllers\Controller;
use App\Models\FeedbackAccessLog;
use App\Models\ThreatFeedback;
use App\Services\SensitiveDataRedactor;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\StreamedResponse;

class ThreatFeedbackWebController extends Controller
{
    /**
     * Menampilkan dashboard web penerimaan dan evaluasi feedback ancaman.
     * Menerapkan pembatasan tampilan teks default (Perubahan 3).
     */
    public function index(Request $request)
    {
        $query = ThreatFeedback::orderByDesc('created_at');

        // Filter tipe feedback
        if ($request->filled('feedback_type')) {
            $query->where('feedback_type', $request->feedback_type);
        }

        // Filter status review
        if ($request->filled('status')) {
            $query->where('status', $request->status);
        }

        // Filter kategori alasan
        if ($request->filled('reason_category')) {
            $query->where('reason_category', $request->reason_category);
        }

        // Pencarian teks (sender_display, package_name, atau snippet)
        if ($request->filled('q')) {
            $search = $request->q;
            $query->where(function ($q) use ($search) {
                $q->where('sender_display', 'like', "%{$search}%")
                  ->orWhere('sender', 'like', "%{$search}%")
                  ->orWhere('package_name', 'like', "%{$search}%")
                  ->orWhere('text_redacted', 'like', "%{$search}%")
                  ->orWhere('snippet', 'like', "%{$search}%");
            });
        }

        $feedbacks = $query->paginate(15)->withQueryString();

        // Metrik statistik feedback
        $stats = [
            'total'             => ThreatFeedback::count(),
            'false_positives'   => ThreatFeedback::where('feedback_type', 'FALSE_POSITIVE')->count(),
            'false_negatives'   => ThreatFeedback::where('feedback_type', 'FALSE_NEGATIVE')->count(),
            'pending'           => ThreatFeedback::where('status', 'PENDING')->count(),
            'reviewed'          => ThreatFeedback::where('status', 'REVIEWED')->count(),
            'dataset_exported'  => ThreatFeedback::where('status', 'DATASET_EXPORTED')->count(),
        ];

        // Daftar kategori alasan unik untuk dropdown filter
        $availableReasons = ThreatFeedback::select('reason_category')
            ->distinct()
            ->whereNotNull('reason_category')
            ->pluck('reason_category');

        // Riwayat audit log terbaru (Perubahan 3)
        $auditLogs = FeedbackAccessLog::with(['analyst', 'feedback'])
            ->orderByDesc('created_at')
            ->limit(20)
            ->get();

        return view('feedback.index', compact('feedbacks', 'stats', 'availableReasons', 'auditLogs'));
    }

    /**
     * Membuka teks tersamar penuh per baris dengan pencatatan audit log (Perubahan 3).
     */
    public function reveal(Request $request, ThreatFeedback $feedback): JsonResponse
    {
        FeedbackAccessLog::create([
            'analyst_id'  => auth()->id(),
            'feedback_id' => $feedback->id,
            'action'      => 'REVEAL',
            'ip_address'  => $request->ip(),
            'created_at'  => now(),
        ]);

        return response()->json([
            'status'        => 'success',
            'feedback_id'   => $feedback->id,
            'text_redacted' => $feedback->text_redacted ?: $feedback->snippet ?: '(Teks tidak tersedia atau telah terhapus kebijakan retensi)',
            'user_notes'    => $feedback->user_note_redacted ?: $feedback->user_notes ?: null,
        ]);
    }

    /**
     * Memperbarui status evaluasi feedback dengan pencatatan audit log (STATUS_CHANGE).
     */
    public function updateStatus(Request $request, ThreatFeedback $feedback): RedirectResponse
    {
        $validated = $request->validate([
            'status' => 'required|in:PENDING,REVIEWED,DATASET_EXPORTED,REJECTED',
        ]);

        $feedback->update(['status' => $validated['status']]);

        // Catat di audit log
        FeedbackAccessLog::create([
            'analyst_id'  => auth()->id(),
            'feedback_id' => $feedback->id,
            'action'      => 'STATUS_CHANGE',
            'ip_address'  => $request->ip(),
            'created_at'  => now(),
        ]);

        return redirect()->back()->with('success', "Status feedback #{$feedback->id} berhasil diperbarui menjadi {$validated['status']}.");
    }

    /**
     * Menerima input feedback baru via web (dengan penyamaran otomatis).
     */
    public function store(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'feedback_type'       => 'required|in:FALSE_POSITIVE,FALSE_NEGATIVE',
            'user_reported_label' => 'required|in:SAFE,CAUTIOUS,DANGEROUS',
            'reason_category'     => 'required|string|max:100',
            'user_notes'          => 'nullable|string|max:200',
            'package_name'        => 'nullable|string|max:100',
            'sender'              => 'nullable|string|max:255',
            'snippet'             => 'nullable|string|max:2000',
            'original_risk_level' => 'nullable|string|max:20',
            'original_category'   => 'nullable|string|max:50',
        ]);

        // Redaksi server-side
        $redactedText = SensitiveDataRedactor::redactText($validated['snippet'] ?? '');
        $senderInfo = SensitiveDataRedactor::redactSender($validated['sender'] ?? '', $validated['package_name'] ?? null);
        $redactedNotes = !empty($validated['user_notes']) ? SensitiveDataRedactor::redactText($validated['user_notes']) : null;

        $reporterSecret = config('app.key') ?: 'sekuritiw-secret-key';
        $reporterHash = hash_hmac('sha256', (string) ($request->ip()), $reporterSecret);

        $feedback = ThreatFeedback::create([
            'feedback_type'       => $validated['feedback_type'],
            'user_reported_label' => $validated['user_reported_label'],
            'reason_category'     => $validated['reason_category'],
            'package_name'        => $validated['package_name'],
            'sender'              => $senderInfo['sender_display'],
            'sender_display'      => $senderInfo['sender_display'],
            'sender_type'         => $senderInfo['sender_type'],
            'snippet'             => $redactedText,
            'text_redacted'       => $redactedText,
            'user_notes'          => $redactedNotes,
            'user_note_redacted'  => $redactedNotes,
            'original_risk_level' => $validated['original_risk_level'] ?? null,
            'original_category'   => $validated['original_category'] ?? null,
            'status'              => 'PENDING',
            'consent_version'     => 'v1',
            'consent_at'          => now(),
            'reporter_hash'       => $reporterHash,
        ]);

        return redirect()->route('feedback.index')->with('success', "Feedback baru #{$feedback->id} berhasil dicatat dengan data tersamar.");
    }

    /**
     * Ekspor dataset JSON MLOps sesuai batasan privasi ketat (Perubahan 4).
     * HANYA berisi: text_redacted, label_final, category, app_package, sender_type.
     * Dilarang memuat sender_display, reporter_hash, catatan pengguna, atau timestamp presisi.
     */
    public function export(Request $request): StreamedResponse
    {
        $query = ThreatFeedback::orderByDesc('created_at');

        if ($request->filled('status')) {
            $query->where('status', $request->status);
        }
        if ($request->filled('feedback_type')) {
            $query->where('feedback_type', $request->feedback_type);
        }

        $allFeedbacks = $query->get();

        // Catat aksi audit EXPORT untuk setiap laporan yang diekspor
        $analystId = auth()->id();
        $ip = $request->ip();
        $now = now();
        $logEntries = [];
        foreach ($allFeedbacks as $fb) {
            $logEntries[] = [
                'analyst_id'  => $analystId,
                'feedback_id' => $fb->id,
                'action'      => 'EXPORT',
                'ip_address'  => $ip,
                'created_at'  => $now,
            ];
        }
        if (!empty($logEntries)) {
            FeedbackAccessLog::insert($logEntries);
        }

        // Susun dataset yang dimitigasi privasinya
        $items = $allFeedbacks->map(function ($item) {
            $labelFinal = $item->user_reported_label ?: ($item->feedback_type === 'FALSE_POSITIVE' ? 'SAFE' : 'DANGEROUS');
            return [
                'text_redacted' => $item->text_redacted ?: $item->snippet,
                'label_final'   => $labelFinal,
                'category'      => $item->reason_category,
                'app_package'   => $item->package_name,
                'sender_type'   => $item->sender_type ?: 'OTHER',
            ];
        });

        $filename = 'sekuritiw_mlops_dataset_' . date('Y-m-d') . '.json';

        return response()->streamDownload(function () use ($items) {
            echo json_encode($items, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        }, $filename, [
            'Content-Type' => 'application/json',
        ]);
    }
}
