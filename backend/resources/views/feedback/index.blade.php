<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="csrf-token" content="{{ csrf_token() }}">
    <title>SEKURITIW - Feedback Center & Dataset MLOps (UU PDP)</title>
    
    <!-- Google Fonts: Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    fontFamily: {
                        sans: ['Inter', 'sans-serif'],
                    },
                    colors: {
                        brand: {
                            50: '#eff6ff',
                            100: '#dbeafe',
                            500: '#3b82f6',
                            600: '#2563eb',
                            700: '#1d4ed8',
                            800: '#1e40af',
                            900: '#1e3a8a',
                        }
                    }
                }
            }
        }
    </script>
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f8fafc;
            color: #0f172a;
        }
    </style>
</head>
<body class="min-h-screen bg-slate-50 text-slate-900 antialiased flex flex-col">

    <!-- Top Navigation Bar -->
    <header class="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-sm">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex items-center justify-between h-16">
                <!-- Brand & Logo -->
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-lg bg-blue-600 flex items-center justify-center text-white font-bold text-lg shadow-sm">
                        S
                    </div>
                    <div>
                        <div class="flex items-center gap-2">
                            <span class="text-lg font-bold tracking-tight text-slate-900">SEKURITIW</span>
                            <span class="px-2 py-0.5 text-xs font-semibold rounded bg-emerald-100 text-emerald-800">Privasi Terjaga</span>
                            <span class="px-2 py-0.5 text-xs font-semibold rounded bg-blue-100 text-blue-800">Analyst Portal</span>
                        </div>
                        <p class="text-xs text-slate-500">Pusat Evaluasi Umpan Balik & Retraining Dataset AI (UU PDP Compliant)</p>
                    </div>
                </div>

                <!-- Action Buttons in Nav -->
                <div class="flex items-center gap-2">
                    <button type="button" 
                            onclick="document.getElementById('modal-audit-log').classList.remove('hidden')"
                            class="inline-flex items-center gap-1.5 px-3 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-md hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors shadow-sm"
                            title="Lihat riwayat audit log pembukaan data">
                        <svg class="w-4 h-4 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                        </svg>
                        <span>Audit Log Akses</span>
                    </button>

                    <a href="{{ route('feedback.export', request()->query()) }}" 
                       class="inline-flex items-center gap-1.5 px-3 py-2 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-md hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors shadow-sm"
                       title="Unduh dataset JSON tersamar untuk retraining AI">
                        <svg class="w-4 h-4 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/>
                        </svg>
                        <span>Ekspor JSON Dataset</span>
                    </a>

                    <button type="button" 
                            onclick="document.getElementById('modal-create').classList.remove('hidden')"
                            class="inline-flex items-center gap-1.5 px-3 py-2 text-sm font-semibold text-white bg-blue-600 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-1 transition-colors shadow-sm">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
                        </svg>
                        <span>Simulasi Feedback</span>
                    </button>

                    <!-- Analyst Profile & Logout -->
                    @auth
                    <div class="h-6 w-px bg-slate-300 mx-1 hidden sm:block"></div>
                    <div class="flex items-center gap-2">
                        <div class="hidden md:flex flex-col text-right">
                            <span class="text-xs font-semibold text-slate-800">{{ auth()->user()->name }}</span>
                            <span class="text-[10px] text-slate-500 uppercase tracking-wider">{{ auth()->user()->role ?? 'analyst' }}</span>
                        </div>
                        <form action="{{ route('logout') }}" method="POST" class="inline">
                            @csrf
                            <button type="submit" 
                                    class="inline-flex items-center gap-1.5 px-3 py-2 text-sm font-medium text-red-700 bg-red-50 border border-red-200 rounded-md hover:bg-red-100 focus:outline-none focus:ring-2 focus:ring-red-500 transition-colors shadow-sm"
                                    title="Keluar dari sesi portal analis">
                                <svg class="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
                                </svg>
                                <span>Keluar</span>
                            </button>
                        </form>
                    </div>
                    @endauth
                </div>
            </div>
        </div>
    </header>

    <!-- Main Content Area -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex-1 w-full">

        <!-- Flash Message Notification -->
        @if(session('success'))
            <div class="mb-6 p-4 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-900 flex items-start gap-3 shadow-sm">
                <svg class="w-5 h-5 text-emerald-600 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
                <div class="flex-1">
                    <p class="text-sm font-medium">{{ session('success') }}</p>
                </div>
                <button type="button" onclick="this.parentElement.remove()" class="text-emerald-700 hover:text-emerald-900 p-1" aria-label="Tutup pemberitahuan">
                    &times;
                </button>
            </div>
        @endif

        @if($errors->any())
            <div class="mb-6 p-4 rounded-lg bg-red-50 border border-red-200 text-red-900 flex items-start gap-3 shadow-sm">
                <svg class="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
                <div class="flex-1">
                    <p class="text-sm font-semibold">Terdapat kesalahan data:</p>
                    <ul class="list-disc list-inside text-xs mt-1 space-y-0.5 text-red-800">
                        @foreach($errors->all() as $err)
                            <li>{{ $err }}</li>
                        @endforeach
                    </ul>
                </div>
            </div>
        @endif

        <!-- Banner Prinsip Privasi UU PDP -->
        <div class="mb-6 p-4 rounded-lg bg-blue-50 border border-blue-200 text-blue-900 flex items-start gap-3 shadow-sm">
            <svg class="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            <div class="text-xs leading-relaxed">
                <span class="font-bold">Kepatuhan UU PDP No. 27/2022:</span> Seluruh data laporan disamarkan secara otomatis di HP pengguna dan diverifikasi ulang di server. Nomor telepon disamarkan, identitas akun tidak disimpan, dan setiap aksi pembukaan teks lengkap dicatat dalam audit trail.
            </div>
        </div>

        <!-- Header Information & Statistics Grid -->
        <div class="mb-8">
            <div class="mb-6">
                <h1 class="text-2xl font-bold text-slate-900 tracking-tight">Daftar Feedback Pendeteksian Ancaman</h1>
                <p class="text-sm text-slate-600 mt-1">
                    Memantau laporan False Positive dan False Negative yang dikirimkan oleh pengguna aplikasi mobile Sekuritiw untuk evaluasi performa model AI.
                </p>
            </div>

            <!-- Metric Cards Row -->
            <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
                <div class="bg-white p-4 rounded-lg border border-slate-200 shadow-sm">
                    <p class="text-xs font-medium text-slate-500 uppercase tracking-wide">Total Masuk</p>
                    <p class="text-2xl font-bold text-slate-900 mt-1">{{ number_format($stats['total']) }}</p>
                    <p class="text-xs text-slate-500 mt-1">Laporan tersimpan</p>
                </div>

                <div class="bg-white p-4 rounded-lg border border-slate-200 shadow-sm border-l-4 border-l-amber-500">
                    <p class="text-xs font-medium text-slate-500 uppercase tracking-wide">False Positive</p>
                    <p class="text-2xl font-bold text-amber-600 mt-1">{{ number_format($stats['false_positives']) }}</p>
                    <p class="text-xs text-slate-500 mt-1">Salah tuduh aman</p>
                </div>

                <div class="bg-white p-4 rounded-lg border border-slate-200 shadow-sm border-l-4 border-l-rose-500">
                    <p class="text-xs font-medium text-slate-500 uppercase tracking-wide">False Negative</p>
                    <p class="text-2xl font-bold text-rose-600 mt-1">{{ number_format($stats['false_negatives']) }}</p>
                    <p class="text-xs text-slate-500 mt-1">Lolos deteksi bahaya</p>
                </div>

                <div class="bg-white p-4 rounded-lg border border-slate-200 shadow-sm border-l-4 border-l-blue-500">
                    <p class="text-xs font-medium text-slate-500 uppercase tracking-wide">Menunggu Review</p>
                    <p class="text-2xl font-bold text-blue-600 mt-1">{{ number_format($stats['pending']) }}</p>
                    <p class="text-xs text-slate-500 mt-1">Butuh verifikasi analis</p>
                </div>

                <div class="bg-white p-4 rounded-lg border border-slate-200 shadow-sm border-l-4 border-l-emerald-500">
                    <p class="text-xs font-medium text-slate-500 uppercase tracking-wide">Telah Direview</p>
                    <p class="text-2xl font-bold text-emerald-600 mt-1">{{ number_format($stats['reviewed']) }}</p>
                    <p class="text-xs text-slate-500 mt-1">Selesai diverifikasi</p>
                </div>

                <div class="bg-white p-4 rounded-lg border border-slate-200 shadow-sm border-l-4 border-l-purple-500">
                    <p class="text-xs font-medium text-slate-500 uppercase tracking-wide">Dataset MLOps</p>
                    <p class="text-2xl font-bold text-purple-600 mt-1">{{ number_format($stats['dataset_exported']) }}</p>
                    <p class="text-xs text-slate-500 mt-1">Siap retraining model</p>
                </div>
            </div>
        </div>

        <!-- Filter & Search Toolbar -->
        <div class="bg-white p-4 rounded-lg border border-slate-200 shadow-sm mb-6">
            <form method="GET" action="{{ route('feedback.index') }}" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3">
                <div class="lg:col-span-2">
                    <label for="filter-q" class="block text-xs font-medium text-slate-700 mb-1">Cari Pengirim atau Aplikasi</label>
                    <div class="relative">
                        <input type="text" 
                               id="filter-q"
                               name="q" 
                               value="{{ request('q') }}" 
                               placeholder="Contoh: WhatsApp, BANK-BCA, com.google..."
                               class="w-full pl-9 pr-3 py-2 text-sm border border-slate-300 rounded-md bg-white text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500">
                        <svg class="w-4 h-4 text-slate-400 absolute left-3 top-2.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
                        </svg>
                    </div>
                </div>

                <div>
                    <label for="filter-type" class="block text-xs font-medium text-slate-700 mb-1">Tipe Kesalahan</label>
                    <select id="filter-type" name="feedback_type" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-md bg-white text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500">
                        <option value="">Semua Tipe</option>
                        <option value="FALSE_POSITIVE" {{ request('feedback_type') == 'FALSE_POSITIVE' ? 'selected' : '' }}>False Positive (Salah Tuduh)</option>
                        <option value="FALSE_NEGATIVE" {{ request('feedback_type') == 'FALSE_NEGATIVE' ? 'selected' : '' }}>False Negative (Lolos Deteksi)</option>
                    </select>
                </div>

                <div>
                    <label for="filter-status" class="block text-xs font-medium text-slate-700 mb-1">Status Evaluasi</label>
                    <select id="filter-status" name="status" class="w-full px-3 py-2 text-sm border border-slate-300 rounded-md bg-white text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500">
                        <option value="">Semua Status</option>
                        <option value="PENDING" {{ request('status') == 'PENDING' ? 'selected' : '' }}>PENDING (Menunggu)</option>
                        <option value="REVIEWED" {{ request('status') == 'REVIEWED' ? 'selected' : '' }}>REVIEWED (Diverifikasi)</option>
                        <option value="DATASET_EXPORTED" {{ request('status') == 'DATASET_EXPORTED' ? 'selected' : '' }}>DATASET_EXPORTED (Ke Dataset)</option>
                        <option value="REJECTED" {{ request('status') == 'REJECTED' ? 'selected' : '' }}>REJECTED (Ditolak)</option>
                    </select>
                </div>

                <div class="flex items-end gap-2">
                    <button type="submit" class="flex-1 px-4 py-2 text-sm font-semibold text-white bg-blue-600 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 shadow-sm transition-colors">
                        Terapkan
                    </button>
                    @if(request()->hasAny(['q', 'feedback_type', 'status', 'reason_category']))
                        <a href="{{ route('feedback.index') }}" class="px-3 py-2 text-sm font-medium text-slate-600 bg-slate-100 rounded-md hover:bg-slate-200 transition-colors" title="Reset filter">
                            Reset
                        </a>
                    @endif
                </div>
            </form>
        </div>

        <!-- Table Feedback List -->
        <div class="bg-white rounded-lg border border-slate-200 shadow-sm overflow-hidden">
            @if($feedbacks->isEmpty())
                <div class="p-12 text-center">
                    <div class="w-16 h-16 mx-auto rounded-full bg-slate-100 flex items-center justify-center text-slate-400 mb-4">
                        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"/>
                        </svg>
                    </div>
                    <h3 class="text-base font-semibold text-slate-800">Belum Ada Feedback Sesuai Kriteria</h3>
                    <p class="text-sm text-slate-500 max-w-md mx-auto mt-1">
                        Belum ada laporan umpan balik yang cocok dengan filter aktif, atau belum ada laporan yang masuk.
                    </p>
                    <div class="mt-6 flex justify-center gap-3">
                        @if(request()->hasAny(['q', 'feedback_type', 'status', 'reason_category']))
                            <a href="{{ route('feedback.index') }}" class="inline-flex items-center px-4 py-2 text-sm font-medium text-slate-700 bg-slate-100 rounded-md hover:bg-slate-200 transition-colors">
                                Hapus Filter Pencarian
                            </a>
                        @endif
                        <button type="button" 
                                onclick="document.getElementById('modal-create').classList.remove('hidden')"
                                class="inline-flex items-center px-4 py-2 text-sm font-semibold text-white bg-blue-600 rounded-md hover:bg-blue-700 transition-colors">
                            Buat Contoh Data Uji
                        </button>
                    </div>
                </div>
            @else
                <div class="overflow-x-auto">
                    <table class="w-full text-left text-sm">
                        <thead class="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-600 uppercase tracking-wider">
                            <tr>
                                <th scope="col" class="py-3.5 px-4">ID & Waktu</th>
                                <th scope="col" class="py-3.5 px-4">Tipe & Label</th>
                                <th scope="col" class="py-3.5 px-4">Pengirim & Tipe</th>
                                <th scope="col" class="py-3.5 px-4">Cuplikan Konten (Tersamar)</th>
                                <th scope="col" class="py-3.5 px-4">Alasan & Catatan</th>
                                <th scope="col" class="py-3.5 px-4">Status</th>
                                <th scope="col" class="py-3.5 px-4 text-right">Aksi Analis</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-slate-200 text-slate-700">
                            @foreach($feedbacks as $fb)
                                @php
                                    $content = $fb->text_redacted ?: $fb->snippet ?: '';
                                    $isTruncated = strlen($content) > 45;
                                    $displaySnippet = $isTruncated ? substr($content, 0, 45) . '...' : $content;
                                @endphp
                                <tr class="hover:bg-slate-50/80 transition-colors" id="row-{{ $fb->id }}">
                                    <!-- ID & Waktu -->
                                    <td class="py-4 px-4 align-top whitespace-nowrap">
                                        <div class="font-bold text-slate-900">#{{ $fb->id }}</div>
                                        <div class="text-xs text-slate-500 mt-0.5">
                                            {{ $fb->created_at ? $fb->created_at->format('d M Y, H:i') : 'Tanggal tidak tercatat' }}
                                        </div>
                                        <div class="text-[10px] text-slate-400 font-mono mt-1">
                                            Consent: {{ $fb->consent_version ?? 'v1' }}
                                        </div>
                                    </td>

                                    <!-- Tipe & Label Koreksi -->
                                    <td class="py-4 px-4 align-top whitespace-nowrap">
                                        @if($fb->feedback_type === 'FALSE_POSITIVE')
                                            <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-amber-100 text-amber-800">
                                                False Positive
                                            </span>
                                        @else
                                            <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-rose-100 text-rose-800">
                                                False Negative
                                            </span>
                                        @endif

                                        <div class="text-xs text-slate-500 mt-1.5">
                                            Deteksi Awal: 
                                            <span class="font-medium text-slate-700">{{ $fb->original_risk_level ?? 'Belum ada data' }}</span>
                                        </div>
                                        <div class="text-xs text-slate-500">
                                            Laporan User: 
                                            <span class="font-bold {{ $fb->user_reported_label === 'SAFE' ? 'text-emerald-700' : ($fb->user_reported_label === 'DANGEROUS' ? 'text-rose-700' : 'text-amber-700') }}">
                                                {{ $fb->user_reported_label }}
                                            </span>
                                        </div>
                                    </td>

                                    <!-- Pengirim & Tipe Pengirim -->
                                    <td class="py-4 px-4 align-top">
                                        @php
                                            $rawSender = $fb->sender_display ?: $fb->sender ?: 'Anonim';
                                            $isMessaging = !empty($fb->package_name) && preg_match('/(whatsapp|telegram|signal|facebook\.orca|line|wechat|discord)/i', $fb->package_name);
                                            $hasSpaces = (bool) preg_match('/\s+/', trim($rawSender));
                                            $isAlreadyMasked = str_contains($rawSender, '***') || str_contains($rawSender, '****') || $rawSender === '[DIHAPUS_RETENSI]';
                                            
                                            $displaySender = $rawSender;
                                            $displayType = $fb->sender_type ?? 'OTHER';
                                            
                                            if (($isMessaging || $hasSpaces) && !$isAlreadyMasked && $displayType !== 'PHONE') {
                                                $displaySender = \App\Services\SensitiveDataRedactor::maskPersonalName($rawSender);
                                                $displayType = 'CONTACT';
                                            }
                                        @endphp
                                        <div class="font-medium text-slate-900 font-mono text-xs">
                                            {{ $displaySender }}
                                        </div>
                                        <div class="text-[11px] text-slate-500 mt-0.5">
                                            Tipe: <span class="font-semibold text-slate-700">{{ $displayType }}</span>
                                        </div>
                                        <div class="text-xs text-slate-500 font-mono mt-0.5 truncate max-w-[160px]" title="{{ $fb->package_name }}">
                                            {{ $fb->package_name ?: 'Aplikasi Sistem' }}
                                        </div>
                                    </td>

                                    <!-- Cuplikan Konten Tersamar (Default Terpotong + Tombol Tampilkan) -->
                                    <td class="py-4 px-4 align-top max-w-xs">
                                        <div id="snippet-box-{{ $fb->id }}" class="text-xs font-mono bg-slate-50 p-2 rounded border border-slate-200 text-slate-800">
                                            <span id="snippet-text-{{ $fb->id }}">{{ $displaySnippet ?: '(Pesan kosong)' }}</span>
                                        </div>

                                        @if($isTruncated)
                                            <button type="button" 
                                                    id="btn-reveal-{{ $fb->id }}"
                                                    onclick="revealContent({{ $fb->id }})" 
                                                    class="mt-1 text-xs text-blue-600 hover:text-blue-800 font-semibold inline-flex items-center gap-1">
                                                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                                                </svg>
                                                <span>Tampilkan (Dicatat Log)</span>
                                            </button>
                                        @endif
                                    </td>

                                    <!-- Alasan & Catatan User -->
                                    <td class="py-4 px-4 align-top max-w-xs">
                                        <div class="text-xs font-medium text-slate-800">
                                            Kategori: <span class="text-blue-700">{{ $fb->reason_category }}</span>
                                        </div>
                                        @if($fb->user_note_redacted || $fb->user_notes)
                                            <div class="text-xs text-slate-600 italic mt-1 bg-amber-50/50 p-1.5 rounded border border-amber-100">
                                                "{{ $fb->user_note_redacted ?: $fb->user_notes }}"
                                            </div>
                                        @else
                                            <div class="text-xs text-slate-400 mt-1">Tidak ada catatan</div>
                                        @endif
                                    </td>

                                    <!-- Status Evaluasi -->
                                    <td class="py-4 px-4 align-top whitespace-nowrap">
                                        @if($fb->status === 'PENDING')
                                            <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-semibold bg-blue-100 text-blue-800">
                                                Menunggu Review
                                            </span>
                                        @elseif($fb->status === 'REVIEWED')
                                            <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800">
                                                Telah Direview
                                            </span>
                                        @elseif($fb->status === 'DATASET_EXPORTED')
                                            <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-semibold bg-purple-100 text-purple-800">
                                                Masuk Dataset
                                            </span>
                                        @elseif($fb->status === 'REJECTED')
                                            <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-semibold bg-slate-200 text-slate-700">
                                                Ditolak
                                            </span>
                                        @endif
                                    </td>

                                    <!-- Form Aksi Cepat Analis -->
                                    <td class="py-4 px-4 align-top text-right whitespace-nowrap">
                                        <form method="POST" action="{{ route('feedback.status.update', $fb->id) }}" class="inline-flex flex-col gap-1 items-end">
                                            @csrf
                                            @method('PATCH')
                                            
                                            <select name="status" 
                                                    onchange="this.form.submit()" 
                                                    class="text-xs px-2 py-1 border border-slate-300 rounded bg-white text-slate-800 focus:outline-none focus:ring-1 focus:ring-blue-500 font-medium">
                                                <option value="PENDING" {{ $fb->status === 'PENDING' ? 'selected' : '' }}>Pending</option>
                                                <option value="REVIEWED" {{ $fb->status === 'REVIEWED' ? 'selected' : '' }}>Tandai Direview</option>
                                                <option value="DATASET_EXPORTED" {{ $fb->status === 'DATASET_EXPORTED' ? 'selected' : '' }}>Ke Dataset AI</option>
                                                <option value="REJECTED" {{ $fb->status === 'REJECTED' ? 'selected' : '' }}>Tolak Laporan</option>
                                            </select>
                                        </form>
                                    </td>
                                </tr>
                            @endforeach
                        </tbody>
                    </table>
                </div>

                @if($feedbacks->hasPages())
                    <div class="px-4 py-3 bg-slate-50 border-t border-slate-200 flex items-center justify-between">
                        {{ $feedbacks->links() }}
                    </div>
                @endif
            @endif
        </div>
    </main>

    <!-- Footer -->
    <footer class="bg-white border-t border-slate-200 py-6 mt-12 text-center text-xs text-slate-500">
        <div class="max-w-7xl mx-auto px-4">
            <p>SEKURITIW Threat Detection & Intelligent Shield - Tim Gemastik</p>
            <p class="mt-1">Modul Umpan Balik Kepatuhan Privasi UU PDP No. 27/2022 (Human-in-the-Loop MLOps)</p>
        </div>
    </footer>

    <!-- Modal Audit Log Akses Analis (Perubahan 3) -->
    <div id="modal-audit-log" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 hidden" role="dialog" aria-modal="true">
        <div class="bg-white rounded-lg max-w-3xl w-full p-6 shadow-xl border border-slate-200 relative max-h-[90vh] overflow-y-auto">
            <div class="flex items-center justify-between pb-3 border-b border-slate-200">
                <div>
                    <h3 class="text-lg font-bold text-slate-900">Audit Log Akses Analis (UU PDP Compliance)</h3>
                    <p class="text-xs text-slate-500">Mencatat riwayat pembukaan konten (REVEAL), perubahan status, dan ekspor dataset.</p>
                </div>
                <button type="button" onclick="document.getElementById('modal-audit-log').classList.add('hidden')" class="text-slate-400 hover:text-slate-700 text-xl font-bold p-1">
                    &times;
                </button>
            </div>

            <div class="mt-4 overflow-x-auto">
                <table class="w-full text-left text-xs">
                    <thead class="bg-slate-100 text-slate-700 font-semibold uppercase">
                        <tr>
                            <th class="py-2.5 px-3">Waktu</th>
                            <th class="py-2.5 px-3">Aksi</th>
                            <th class="py-2.5 px-3">Target Feedback</th>
                            <th class="py-2.5 px-3">Alamat IP</th>
                            <th class="py-2.5 px-3">Analis / User</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-slate-200 text-slate-700">
                        @forelse($auditLogs as $log)
                            <tr class="hover:bg-slate-50">
                                <td class="py-2.5 px-3 whitespace-nowrap">{{ $log->created_at }}</td>
                                <td class="py-2.5 px-3 whitespace-nowrap">
                                    @if($log->action === 'REVEAL')
                                        <span class="px-1.5 py-0.5 rounded bg-amber-100 text-amber-800 font-bold">REVEAL</span>
                                    @elseif($log->action === 'EXPORT')
                                        <span class="px-1.5 py-0.5 rounded bg-blue-100 text-blue-800 font-bold">EXPORT</span>
                                    @else
                                        <span class="px-1.5 py-0.5 rounded bg-emerald-100 text-emerald-800 font-bold">STATUS_CHANGE</span>
                                    @endif
                                </td>
                                <td class="py-2.5 px-3">Feedback #{{ $log->feedback_id }}</td>
                                <td class="py-2.5 px-3 font-mono">{{ $log->ip_address ?: '127.0.0.1' }}</td>
                                <td class="py-2.5 px-3">{{ $log->analyst?->name ?? 'Analis (Sesi Aktif)' }}</td>
                            </tr>
                        @empty
                            <tr>
                                <td colspan="5" class="py-6 text-center text-slate-400">Belum ada riwayat audit log tercatat.</td>
                            </tr>
                        @endforelse
                    </tbody>
                </table>
            </div>

            <div class="mt-6 flex justify-end">
                <button type="button" onclick="document.getElementById('modal-audit-log').classList.add('hidden')" class="px-4 py-2 text-sm font-medium text-slate-700 bg-slate-100 rounded-md hover:bg-slate-200">
                    Tutup
                </button>
            </div>
        </div>
    </div>

    <!-- Modal Simulasi Feedback Baru -->
    <div id="modal-create" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 hidden" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <div class="bg-white rounded-lg max-w-lg w-full p-6 shadow-xl border border-slate-200 relative max-h-[90vh] overflow-y-auto">
            <div class="flex items-center justify-between pb-3 border-b border-slate-200">
                <h3 id="modal-title" class="text-lg font-bold text-slate-900">Simulasi / Tambah Feedback Baru</h3>
                <button type="button" onclick="document.getElementById('modal-create').classList.add('hidden')" class="text-slate-400 hover:text-slate-700 text-xl font-bold p-1">
                    &times;
                </button>
            </div>

            <form method="POST" action="{{ route('feedback.store') }}" class="mt-4 space-y-4">
                @csrf
                
                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label for="modal-feedback_type" class="block text-xs font-medium text-slate-700 mb-1">Tipe Kesalahan</label>
                        <select id="modal-feedback_type" name="feedback_type" required class="w-full text-sm px-3 py-2 border border-slate-300 rounded-md focus:ring-2 focus:ring-blue-500">
                            <option value="FALSE_POSITIVE">FALSE POSITIVE (Salah Tuduh)</option>
                            <option value="FALSE_NEGATIVE">FALSE NEGATIVE (Lolos Deteksi)</option>
                        </select>
                    </div>

                    <div>
                        <label for="modal-user_reported_label" class="block text-xs font-medium text-slate-700 mb-1">Label Seharusnya</label>
                        <select id="modal-user_reported_label" name="user_reported_label" required class="w-full text-sm px-3 py-2 border border-slate-300 rounded-md focus:ring-2 focus:ring-blue-500">
                            <option value="SAFE">SAFE (Aman)</option>
                            <option value="CAUTIOUS">CAUTIOUS (Waspada)</option>
                            <option value="DANGEROUS">DANGEROUS (Berbahaya)</option>
                        </select>
                    </div>
                </div>

                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label for="modal-package_name" class="block text-xs font-medium text-slate-700 mb-1">Aplikasi Sumber</label>
                        <input type="text" id="modal-package_name" name="package_name" placeholder="com.whatsapp" value="com.whatsapp" class="w-full text-sm px-3 py-2 border border-slate-300 rounded-md focus:ring-2 focus:ring-blue-500">
                    </div>

                    <div>
                        <label for="modal-sender" class="block text-xs font-medium text-slate-700 mb-1">Nama / Nomor Pengirim</label>
                        <input type="text" id="modal-sender" name="sender" placeholder="+62 812-3456-7890" value="+62 812-3456-7890" class="w-full text-sm px-3 py-2 border border-slate-300 rounded-md focus:ring-2 focus:ring-blue-500">
                    </div>
                </div>

                <div>
                    <label for="modal-reason_category" class="block text-xs font-medium text-slate-700 mb-1">Alasan Pengguna</label>
                    <input type="text" id="modal-reason_category" name="reason_category" required placeholder="OFFICIAL_BANK_NOTIFICATION" value="OFFICIAL_BANK_NOTIFICATION" class="w-full text-sm px-3 py-2 border border-slate-300 rounded-md focus:ring-2 focus:ring-blue-500">
                </div>

                <div>
                    <label for="modal-snippet" class="block text-xs font-medium text-slate-700 mb-1">Cuplikan Teks Pesan (Akan Disamarkan Otomatis)</label>
                    <textarea id="modal-snippet" name="snippet" rows="3" class="w-full text-sm px-3 py-2 border border-slate-300 rounded-md focus:ring-2 focus:ring-blue-500">Yth. Nasabah, transaksi Rp 150.000 di merchant berhasil. Cek mutasi Anda di aplikasi perbankan resmi.</textarea>
                </div>

                <div>
                    <label for="modal-user_notes" class="block text-xs font-medium text-slate-700 mb-1">Catatan Tambahan (Maks 200 Karakter)</label>
                    <textarea id="modal-user_notes" name="user_notes" maxlength="200" rows="2" placeholder="Catatan opsional..." class="w-full text-sm px-3 py-2 border border-slate-300 rounded-md focus:ring-2 focus:ring-blue-500">Ini SMS resmi verifikasi perbankan bukan penipuan phishing.</textarea>
                </div>

                <div class="flex items-center justify-end gap-2 pt-3 border-t border-slate-200">
                    <button type="button" onclick="document.getElementById('modal-create').classList.add('hidden')" class="px-4 py-2 text-sm font-medium text-slate-700 bg-slate-100 rounded-md hover:bg-slate-200 transition-colors">
                        Batal
                    </button>
                    <button type="submit" class="px-4 py-2 text-sm font-semibold text-white bg-blue-600 rounded-md hover:bg-blue-700 shadow-sm transition-colors">
                        Simpan Feedback Tersamar
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- Script AJAX untuk Tombol Tampilkan (REVEAL) dengan Audit Log -->
    <script>
        function revealContent(feedbackId) {
            const token = document.querySelector('meta[name="csrf-token"]').getAttribute('content');
            const btn = document.getElementById('btn-reveal-' + feedbackId);
            const textBox = document.getElementById('snippet-text-' + feedbackId);

            if (btn) {
                btn.innerText = 'Memuat...';
                btn.disabled = true;
            }

            fetch('/feedback/' + feedbackId + '/reveal', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': token,
                    'Accept': 'application/json'
                }
            })
            .then(res => res.json())
            .then(data => {
                if (data.status === 'success') {
                    textBox.innerText = data.text_redacted;
                    if (btn) {
                        btn.outerHTML = '<span class="text-[10px] text-emerald-600 font-semibold">✓ Konten dibuka (Tercatat Log)</span>';
                    }
                } else {
                    alert('Gagal membuka konten: ' + (data.message || 'Error'));
                    if (btn) {
                        btn.innerText = 'Tampilkan (Dicatat Log)';
                        btn.disabled = false;
                    }
                }
            })
            .catch(err => {
                alert('Gagal menghubungi server: ' + err);
                if (btn) {
                    btn.innerText = 'Tampilkan (Dicatat Log)';
                    btn.disabled = false;
                }
            });
        }
    </script>

</body>
</html>
