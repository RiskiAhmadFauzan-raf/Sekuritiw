package com.sekuritiw.app.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekuritiw.app.data.analyzer.AnalysisResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Tema status visual hasil scan APK */
data class ApkStatusTheme(
    val themeColor: Color,
    val cardBg: Color,
    val statusTitle: String,
    val statusSubtitle: String,
    val statusIcon: ImageVector
)

/**
 * Layar Detail Hasil Pemindaian APK (Detail Hasil Scan APK Screen)
 * Menampilkan hasil deteksi detail berdasarkan Kategori Risiko: Berbahaya, Waspada, atau Aman.
 */
@Composable
fun ApkScanResultScreen(
    result: AnalysisResult?,
    onBackClick: () -> Unit
) {
    if (result == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada data hasil pemindaian.")
        }
        return
    }

    val riskLevel = result.riskLevel
    val fileName = result.apkResult?.fileName ?: "Berkas_Aplikasi.apk"
    val isManualScan = result.contentId.startsWith("apk_file_scan_")
    val packageName = if (isManualScan) {
        if (result.packageName == "com.android.providers.downloads") {
            "com.app.${fileName.replace(".apk", "").lowercase().replace(" ", "")}"
        } else {
            result.packageName
        }
    } else {
        "Tidak diketahui (Belum diinstal)"
    }
    val sourceApp = if (!isManualScan) result.packageName else "Penyimpanan Lokal"

    // Tentukan parameter visual berdasarkan tingkat risiko menggunakan ApkStatusTheme
    val theme = when (riskLevel) {
        "DANGEROUS" -> ApkStatusTheme(
            themeColor = Color(0xFFDC2626), // DangerRed
            cardBg = Color(0xFFFEE2E2), // Soft red background
            statusTitle = "Berbahaya",
            statusSubtitle = "File ini tidak aman untuk diinstal",
            statusIcon = Icons.Default.Cancel // Red Cross icon
        )
        
        "CAUTIOUS" -> ApkStatusTheme(
            themeColor = Color(0xFFD97706), // WarningOrange
            cardBg = Color(0xFFFEF3C7), // Soft yellow background
            statusTitle = "Waspada",
            statusSubtitle = "File ini berpotensi membahayakan",
            statusIcon = Icons.Default.Warning // Yellow warning icon
        )

        else -> ApkStatusTheme(
            themeColor = Color(0xFF059669), // SuccessGreen
            cardBg = Color(0xFFECFDF5), // Soft green background
            statusTitle = "Aman",
            statusSubtitle = "File ini aman untuk diinstal",
            statusIcon = Icons.Default.CheckCircle // Green Checkmark icon
        )
    }

    // Detail Analisis Bullets
    val analysisDetails = remember(result) {
        val factors = result.explanation?.riskFactors ?: emptyList()
        if (factors.isNotEmpty()) {
            factors.map { factor ->
                when {
                    // Sudah ada prefix baru → langsung pakai
                    factor.startsWith("SAFE:") || factor.startsWith("DANGER:") || factor.startsWith("INFO:") -> factor
                    // Legacy prefix → konversi
                    factor.startsWith("❌ ") || factor.startsWith("⚠️ ") || factor.startsWith("✅ ") -> factor
                    // Teks biasa → beri prefix sesuai riskLevel
                    else -> {
                        val clean = factor.removePrefix("Suspek: ")
                        when (riskLevel) {
                            "DANGEROUS" -> "DANGER:$clean"
                            "CAUTIOUS" -> if (
                                listOf("sms", "otp", "install", "overlay", "accessibility", "kunci", "kartu sim", "panggilan").any { clean.lowercase().contains(it) }
                            ) "DANGER:$clean" else "INFO:$clean"
                            else -> "SAFE:$clean"
                        }
                    }
                }
            }
        } else {
            when (riskLevel) {
                "DANGEROUS" -> listOf(
                    "DANGER:Meminta izin berbahaya (SMS, Call, Install)",
                    "DANGER:Mencoba mengakses Accessibility Service",
                    "DANGER:Berpotensi melakukan overlay attack",
                    "DANGER:Terdeteksi pola malware perbankan"
                )
                "CAUTIOUS" -> listOf(
                    "DANGER:Meminta izin yang tidak biasa",
                    "INFO:Terhubung ke server yang mencurigakan",
                    "INFO:Tidak ditemukan di Google Play Store"
                )
                else -> listOf(
                    "SAFE:Tidak ditemukan kombinasi izin yang mencurigakan.",
                    "SAFE:Izin aplikasi normal dan wajar.",
                    "SAFE:Tidak terhubung ke server mencurigakan."
                )
            }
        }
    }

    // Tips Aman Bullets
    val safetyTips = when (riskLevel) {
        "DANGEROUS" -> listOf(
            "Jangan instal aplikasi ini",
            "Hapus file APK dari perangkat",
            "Aktifkan Play Protect untuk perlindungan lebih baik"
        )
        "CAUTIOUS" -> listOf(
            "Periksa detail izin sebelum instal",
            "Pastikan sumber file dapat dipercaya",
            "Selalu perbarui sistem keamanan perangkat"
        )
        else -> listOf(
            "Selalu unduh aplikasi dari sumber resmi",
            "Perbarui aplikasi secara berkala",
            "Aktifkan Play Protect untuk perlindungan maksimal",
            "Tetap waspada terhadap aplikasi baru"
        )
    }

    // Mengambil waktu scan (format Indonesia)
    val scanTime = remember {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id")).format(Date(result.contentId.substringAfterLast("_").toLongOrNull() ?: System.currentTimeMillis()))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color(0xFF0F172A))
            }
            Text(
                text = "Detail Hasil Scan APK",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ── 1. Top Status Banner Card ───────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = theme.statusIcon,
                        contentDescription = null,
                        tint = theme.themeColor,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = theme.statusTitle,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.themeColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = theme.statusSubtitle,
                            fontSize = 11.sp,
                            color = theme.themeColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 2. Informasi Apk Section ────────────────────────────────────
            Text(
                text = "Informasi Apk",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dokumen Icon Android sesuai warna status
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.cardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = theme.themeColor,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Detail Data
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    InfoRow("Nama File", fileName)
                    InfoRow("Package Name", packageName)
                    InfoRow("Sumber Deteksi", sourceApp)
                    InfoRow("Ukuran File", "17.34 MB")
                    InfoRow("Scan Time", scanTime)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 3. Jenis Ancaman Terdeteksi (hanya jika bukan SAFE) ─────────
            val threatType = remember(result) {
                result.explanation?.riskFactors
                    ?.firstOrNull { it.startsWith("DANGER:") && !it.contains("Meminta izin:") }
                    ?.removePrefix("DANGER:")?.trim()
            }

            if (riskLevel != "SAFE" && !threatType.isNullOrBlank()) {
                Text(
                    text = "Jenis Ancaman Terdeteksi",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = threatType,
                    fontSize = 13.sp,
                    color = if (riskLevel == "DANGEROUS") Color(0xFFDC2626) else Color(0xFFD97706)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── 4. Detail Analisis Section ──────────────────────────────────
            Text(
                text = "Detail Analisis",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter: hapus item threat type dari detail list agar tidak dobel
            val filteredDetails = remember(analysisDetails, threatType) {
                analysisDetails.filter { detail ->
                    val text = detail.removePrefix("DANGER:").removePrefix("INFO:").removePrefix("SAFE:").trim()
                    text != (threatType ?: "") && !text.equals(threatType?.trim(), ignoreCase = true)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredDetails.forEach { detail ->
                    val (icon, color, text) = when {
                        detail.startsWith("SAFE:") -> Triple("✅", Color(0xFF059669), detail.removePrefix("SAFE:"))
                        detail.startsWith("DANGER:") -> Triple("❌", Color(0xFFDC2626), detail.removePrefix("DANGER:"))
                        detail.startsWith("INFO:") -> Triple("ℹ️", Color(0xFF2563EB), detail.removePrefix("INFO:"))
                        // fallback lama jika prefix tidak ada
                        detail.startsWith("❌") -> Triple("❌", Color(0xFFDC2626), detail.removePrefix("❌ "))
                        detail.startsWith("⚠️") -> Triple("⚠️", Color(0xFFD97706), detail.removePrefix("⚠️ "))
                        else -> Triple("✅", Color(0xFF059669), detail.removePrefix("✅ "))
                    }
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = icon,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 1.dp, end = 8.dp)
                        )
                        Text(
                            text = text,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = color,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 4. Tips Aman Card (Soft blue) ──────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF5FF))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tips Aman",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    safetyTips.forEach { tip ->
                        Text(
                            text = "✓ $tip",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF1E3A8A),
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            maxLines = 1
        )
    }
}
