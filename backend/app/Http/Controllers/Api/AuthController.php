<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\Rules\Password;
use Illuminate\Validation\ValidationException;

/**
 * Controller autentikasi API menggunakan Laravel Sanctum.
 * Menyediakan endpoint register, login, logout, dan profil pengguna.
 *
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5
 */
class AuthController extends Controller
{
    /**
     * Registrasi pengguna baru.
     * POST /api/auth/register
     */
    public function register(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'name'     => ['required', 'string', 'max:255'],
            'email'    => ['required', 'string', 'email', 'max:255', 'unique:users'],
            'password' => ['required', 'confirmed', Password::min(8)->mixedCase()->numbers()],
            'device_id' => ['nullable', 'string', 'max:64'],
        ]);

        $user = User::create([
            'name'     => $validated['name'],
            'email'    => $validated['email'],
            'password' => Hash::make($validated['password']),
        ]);

        $token = $user->createToken('sekuritiw-android', ['threat:sync', 'report:submit'])->plainTextToken;

        return response()->json([
            'message' => 'Registrasi berhasil.',
            'user'    => [
                'id'    => $user->id,
                'name'  => $user->name,
                'email' => $user->email,
            ],
            'token'   => $token,
        ], 201);
    }

    /**
     * Login pengguna dan menghasilkan API token Sanctum.
     * POST /api/auth/login
     */
    public function login(Request $request): JsonResponse
    {
        $request->validate([
            'email'    => ['required', 'email'],
            'password' => ['required', 'string'],
        ]);

        $user = User::where('email', $request->email)->first();

        if (! $user || ! Hash::check($request->password, $user->password)) {
            throw ValidationException::withMessages([
                'email' => ['Email atau password salah.'],
            ]);
        }

        // Hapus token lama agar tidak menumpuk
        $user->tokens()->where('name', 'sekuritiw-android')->delete();

        $token = $user->createToken('sekuritiw-android', ['threat:sync', 'report:submit'])->plainTextToken;

        return response()->json([
            'message' => 'Login berhasil.',
            'user'    => [
                'id'    => $user->id,
                'name'  => $user->name,
                'email' => $user->email,
            ],
            'token'   => $token,
        ]);
    }

    /**
     * Logout — hapus token aktif saat ini.
     * POST /api/auth/logout
     */
    public function logout(Request $request): JsonResponse
    {
        $request->user()->currentAccessToken()->delete();

        return response()->json(['message' => 'Logout berhasil.']);
    }

    /**
     * Profil pengguna yang sedang login.
     * GET /api/auth/me
     */
    public function me(Request $request): JsonResponse
    {
        return response()->json([
            'id'         => $request->user()->id,
            'name'       => $request->user()->name,
            'email'      => $request->user()->email,
            'created_at' => $request->user()->created_at,
        ]);
    }
}
