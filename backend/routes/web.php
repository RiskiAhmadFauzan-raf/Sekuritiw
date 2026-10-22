<?php

use App\Http\Controllers\Web\AuthController;
use App\Http\Controllers\Web\ThreatFeedbackWebController;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| Web Routes - SEKURITIW Feedback Center & Dashboard (UU PDP Compliant)
|--------------------------------------------------------------------------
*/

// Rute Tamu (Autentikasi Analis)
Route::middleware('guest')->group(function () {
    Route::get('/login', [AuthController::class, 'showLoginForm'])->name('login');
    Route::post('/login', [AuthController::class, 'login']);
});

// Logout (Hanya untuk pengguna terotentikasi)
Route::post('/logout', [AuthController::class, 'logout'])->name('logout')->middleware('auth');

// Rute Terproteksi Khusus Analis Keamanan
Route::middleware('auth')->group(function () {
    Route::get('/', fn () => redirect()->route('feedback.index'))->name('home');

    Route::prefix('feedback')->name('feedback.')->group(function () {
        Route::get('/', [ThreatFeedbackWebController::class, 'index'])->name('index');
        Route::post('/', [ThreatFeedbackWebController::class, 'store'])->name('store');
        Route::get('/export', [ThreatFeedbackWebController::class, 'export'])->name('export');
        Route::post('/{feedback}/reveal', [ThreatFeedbackWebController::class, 'reveal'])->name('reveal');
        Route::patch('/{feedback}/status', [ThreatFeedbackWebController::class, 'updateStatus'])->name('status.update');
    });
});
