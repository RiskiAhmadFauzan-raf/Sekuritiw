<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\ThreatRecordController;
use App\Http\Controllers\Api\ThreatFeedbackController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| API Routes - SEKURITIW Backend Laravel 13
|--------------------------------------------------------------------------
*/

// ─── Health Check ────────────────────────────────────────────────────────────
Route::get('/ping', fn () => response()->json([
    'status'  => 'ok',
    'version' => 'SEKURITIW API v1.0',
    'laravel' => app()->version(),
]));

// ─── Autentikasi (Publik) ─────────────────────────────────────────────────────
Route::prefix('auth')->group(function () {
    Route::post('/register', [AuthController::class, 'register']);
    Route::post('/login',    [AuthController::class, 'login']);
});

// ─── Feedback Salah Deteksi (False Positive / False Negative) ─────────────────
Route::prefix('threats/feedback')->group(function () {
    Route::post('/',                   [ThreatFeedbackController::class, 'store']);
    Route::get('/stats',               [ThreatFeedbackController::class, 'stats']);
    Route::get('/',                    [ThreatFeedbackController::class, 'index']);
    Route::patch('/{feedback}/status', [ThreatFeedbackController::class, 'updateStatus']);
});

// ─── Endpoint Terproteksi Sanctum ────────────────────────────────────────────
Route::middleware('auth:sanctum')->group(function () {

    // Auth
    Route::prefix('auth')->group(function () {
        Route::post('/logout', [AuthController::class, 'logout']);
        Route::get('/me',     [AuthController::class, 'me']);
    });

    // Riwayat Ancaman
    Route::prefix('threats')->group(function () {
        Route::get('/stats',           [ThreatRecordController::class, 'stats']);
        Route::get('/',                [ThreatRecordController::class, 'index']);
        Route::get('/{threatRecord}',  [ThreatRecordController::class, 'show']);
        Route::post('/sync',           [ThreatRecordController::class, 'sync']);
        Route::post('/{threatRecord}/report', [ThreatRecordController::class, 'report']);
        Route::post('/{threatRecord}/feedback', [ThreatFeedbackController::class, 'store']);
    });
});

