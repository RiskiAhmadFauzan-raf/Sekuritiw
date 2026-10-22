package com.sekuritiw.app.presentation.theme

import androidx.compose.ui.graphics.Color

// ── SEKURITIW NEW COLOR STYLE (LIGHT THEME & WCAG AAA ACCESSIBILITY) ──

// Neutral / Background Elements
val NeutralBg = Color(0xFFF8FAFC)          // Neutral 50 (Screen Background)
val NeutralSurface = Color(0xFFFFFFFF)     // Card / Box Background
val NeutralBorder = Color(0xFFE2E8F0)      // Neutral 200 (Dividers / Borders)

// Text Colors
val TextPrimary = Color(0xFF0F172A)        // Neutral 900 (Crisp Dark Text)
val TextSecondary = Color(0xFF64748B)      // Neutral 500 (Cool Slate Gray)
val TextMuted = Color(0xFF94A3B8)          // Neutral 400 (Subtle Hints)

// Primary & Secondary Accents
val PrimaryBlue = Color(0xFF2563EB)        // Primary 600 (Electric Blue)
val PrimaryDark = Color(0xFF1E3A8A)        // Primary 800 (Dark Slate Blue)
val SecondaryBlue = Color(0xFF0EA5E9)      // Secondary 500 (Sky Blue)

// Success (Rendah / Safe)
val SuccessGreen = Color(0xFF10B981)       // Success 500 (Base green)
val SuccessDark = Color(0xFF059669)        // Success 600 (Accessible text green)
val SuccessBg = Color(0xFFECFDF5)          // Success 100 (Badge Background)
val SuccessStatusBg = Color(0xFFA7F3D0)    // Success 200 (Active Protection Card Background)

// Warning (Sedang / Cautious)
val WarningYellow = Color(0xFFF59E0B)      // Warning 500 (Base yellow)
val WarningDark = Color(0xFFD97706)        // Warning 600 (Accessible text amber)
val WarningBg = Color(0xFFFEF3C7)          // Warning 100 (Badge Background)

// Danger (Tinggi / Dangerous)
val DangerRed = Color(0xFFEF4444)          // Danger 500 (Base red)
val DangerDark = Color(0xFFDC2626)         // Danger 600 (Accessible text red)
val DangerBg = Color(0xFFFEE2E2)           // Danger 100 (Badge Background)

// Keep old color definitions as mapping to prevent compile breaks elsewhere, but redirect them to new light palette colors
val DarkBackground = NeutralBg
val DarkSurface = NeutralSurface
val DarkSurfaceVariant = Color(0xFFEBF2F7) // light blue-gray
val DarkBorder = NeutralBorder
val CyanPrimary = PrimaryBlue
val BlueAccent = PrimaryBlue
val RiskSafeGreen = SuccessGreen
val RiskCautiousAmber = WarningYellow
val RiskDangerousRed = DangerRed
val RiskCriticalPurple = Color(0xFFA855F7)
