<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

/**
 * Model Eloquent untuk umpan balik salah deteksi (Misdetection Feedback).
 * Mengimplementasikan pemisahan identitas dan minimisasi data sesuai UU PDP.
 */
class ThreatFeedback extends Model
{
    use HasFactory;

    protected $table = 'threat_feedbacks';

    protected $fillable = [
        'threat_record_id',
        'device_record_id',
        'package_name',
        'sender',
        'sender_display',
        'sender_type',
        'snippet',
        'text_redacted',
        'original_risk_level',
        'original_category',
        'feedback_type',
        'user_reported_label',
        'reason_category',
        'user_notes',
        'user_note_redacted',
        'status',
        'consent_version',
        'consent_at',
        'reporter_hash',
    ];

    protected $casts = [
        'consent_at' => 'datetime',
    ];

    /** Relasi ke ThreatRecord (opsional jika ada di server) */
    public function threatRecord(): BelongsTo
    {
        return $this->belongsTo(ThreatRecord::class);
    }

    /** Relasi ke Log Akses Audit (Perubahan 3) */
    public function accessLogs(): HasMany
    {
        return $this->hasMany(FeedbackAccessLog::class, 'feedback_id');
    }

    /** Scope: feedback False Positive */
    public function scopeFalsePositive($query)
    {
        return $query->where('feedback_type', 'FALSE_POSITIVE');
    }

    /** Scope: feedback False Negative */
    public function scopeFalseNegative($query)
    {
        return $query->where('feedback_type', 'FALSE_NEGATIVE');
    }

    /** Scope: status pending review */
    public function scopePending($query)
    {
        return $query->where('status', 'PENDING');
    }
}
