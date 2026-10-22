<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

/**
 * Model Eloquent untuk riwayat ancaman yang disinkronisasi dari Android.
 *
 * Requirements: 10.5, 15.1, 15.2, 15.3
 */
class ThreatRecord extends Model
{
    use HasFactory;

    protected $fillable = [
        'user_id',
        'device_record_id',
        'package_name',
        'sender',
        'raw_text',
        'normalized_text',
        'threat_category',
        'risk_level',
        'severity_rating',
        'risk_score',
        'reasoning',
        'is_verified',
        'community_reports',
        'notified_at',
    ];

    protected $casts = [
        'risk_score'      => 'float',
        'is_verified'     => 'boolean',
        'community_reports' => 'integer',
        'notified_at'     => 'datetime',
    ];

    /** Relasi ke user pemilik */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    /** Relasi ke feedback salah deteksi */
    public function feedbacks(): HasMany
    {
        return $this->hasMany(ThreatFeedback::class);
    }

    /** Scope: hanya ancaman berbahaya */
    public function scopeDangerous($query)
    {
        return $query->where('risk_level', 'DANGEROUS');
    }

    /** Scope: filter per kategori ancaman */
    public function scopeOfCategory($query, string $category)
    {
        return $query->where('threat_category', $category);
    }
}
