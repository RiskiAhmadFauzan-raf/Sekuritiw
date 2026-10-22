<?php

namespace App\Http\Controllers\Web;

use App\Http\Controllers\Controller;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\RateLimiter;
use Illuminate\Support\Str;
use Illuminate\View\View;

class AuthController extends Controller
{
    /**
     * Tampilkan halaman formulir login analis portal.
     */
    public function showLoginForm(): View|RedirectResponse
    {
        if (Auth::check()) {
            return redirect()->route('feedback.index');
        }

        return view('auth.login');
    }

    /**
     * Proses autentikasi analis dengan perlindungan rate limiting (brute-force).
     */
    public function login(Request $request): RedirectResponse
    {
        try {
            $credentials = $request->validate([
                'email'    => ['required', 'string', 'email'],
                'password' => ['required', 'string'],
            ], [
                'email.required'    => 'Alamat email wajib diisi.',
                'email.email'       => 'Format alamat email tidak valid.',
                'password.required' => 'Kata sandi wajib diisi.',
            ]);

            $throttleKey = Str::lower($request->input('email')) . '|' . $request->ip();

            if (RateLimiter::tooManyAttempts($throttleKey, 5)) {
                $seconds = RateLimiter::availableIn($throttleKey);
                return back()->withErrors([
                    'email' => "Terlalu banyak percobaan masuk yang gagal. Silakan coba lagi dalam {$seconds} detik.",
                ])->onlyInput('email');
            }

            $remember = $request->boolean('remember');

            if (Auth::attempt($credentials, $remember)) {
                RateLimiter::clear($throttleKey);
                $request->session()->regenerate();

                return redirect()->intended(route('feedback.index'));
            }

            RateLimiter::hit($throttleKey, 60);

            return back()->withErrors([
                'email' => 'Kredensial yang Anda masukkan tidak sesuai dengan catatan analis kami.',
            ])->onlyInput('email');
        } catch (\Throwable $e) {
            \Illuminate\Support\Facades\Log::error('Kesalahan otentikasi: ' . $e->getMessage(), [
                'file' => $e->getFile(),
                'line' => $e->getLine(),
            ]);

            return back()->withErrors([
                'email' => 'Terjadi kendala pada server saat proses masuk. Silakan coba beberapa saat lagi.',
            ])->onlyInput('email');
        }
    }

    /**
     * Keluar dari sesi analis dengan aman.
     */
    public function logout(Request $request): RedirectResponse
    {
        Auth::logout();

        $request->session()->invalidate();
        $request->session()->regenerateToken();

        return redirect()->route('login')->with('success', 'Sesi Anda telah berakhir. Anda berhasil keluar dengan aman.');
    }
}
