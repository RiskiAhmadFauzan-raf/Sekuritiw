<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class FeedbackAccessLog extends Model
{
    use HasFactory;

    public $timestamps = false; // hanya menggunakan created_at

    protected $fillable = [
        'analyst_id',
        'feedback_id',
        'action',
        'ip_address',
        'created_at',
    ];

    public function analyst(): BelongsTo
    {
        return $this->belongsTo(User::class, 'analyst_id');
    }

    public function feedback(): BelongsTo
    {
        return $this->belongsTo(ThreatFeedback::class, 'feedback_id');
    }
}
