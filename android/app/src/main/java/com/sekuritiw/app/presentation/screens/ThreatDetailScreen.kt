package com.sekuritiw.app.presentation.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sekuritiw.app.presentation.components.ThreatFeedbackDialog
import com.sekuritiw.app.presentation.viewmodel.FeedbackSubmissionState
import com.sekuritiw.app.presentation.viewmodel.ThreatFeedbackViewModel

/** Tema status visual hasil scan pesan */
data class MessageStatusTheme(
    val themeColor: Color,
    val cardBg: Color,
    val statusTitle: String,
    val statusSubtitle: String,
    val statusIcon: ImageVector
)

/**
 * Layar Detail Hasil Pemindaian Pesan (Detail Hasil Scan Pesan Screen).
 * Dilengkapi fitur koreksi/feedback salah deteksi (False Positive & False Negative).
 */
@Composable
fun ThreatDetailScreen(
    item: HistoryItem,
    onBack: () -> Unit,
    feedbackViewModel: ThreatFeedbackViewModel = hiltViewModel()
) {
    var currentRiskLevel by remember(item.id, item.riskLevel) { mutableStateOf(item.riskLevel) }
    var currentReasoning by remember(item.id, item.reasoning) { mutableStateOf(item.reasoning) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackSuccessMsg by remember { mutableStateOf<String?>(null) }
    var isFeedbackReported by remember(item.id) {
        mutableStateOf(feedbackViewModel.isFeedbackReported(item.id))
    }
    var reportedTypeInfo by remember(item.id) {
        mutableStateOf(feedbackViewModel.getReportedType(item.id))
    }

    val submissionState by feedbackViewModel.submissionState.collectAsState()
    val sourceApp = getAppName(item.packageName)

    // Tentukan parameter visual berdasarkan tingkat risiko terkini
    val theme = when (currentRiskLevel) {
        "DANGEROUS" -> MessageStatusTheme(
            themeColor = Color(0xFFDC2626), // DangerRed
            cardBg = Color(0xFFFEE2E2), // Soft red background
            statusTitle = "Berbahaya",
            statusSubtitle = "Sekuritiw telah memindai pesan secara otomatis dan menemukan ancaman berbahaya",
            statusIcon = Icons.Default.Cancel // Red Cross icon
        )
        
        "CAUTIOUS" -> MessageStatusTheme(
            themeColor = Color(0xFFD97706), // WarningOrange
            cardBg = Color(0xFFFEF3C7), // Soft yellow background
            statusTitle = "Waspada",
            statusSubtitle = "Sekuritiw telah memindai pesan secara otomatis dan menemukan konten mencurigakan",
            statusIcon = Icons.Default.Warning // Yellow warning icon
        )

        else -> MessageStatusTheme(
            themeColor = Color(0xFF059669), // SuccessGreen
            cardBg = Color(0xFFECFDF5), // Soft green background
            statusTitle = "Aman",
            statusSubtitle = "Tidak ditemukan indikasi bahaya pada pesan ini. Catatan: status aman bukan jaminan mutlak bebas risiko, tetap berhati-hati.",
            statusIcon = Icons.Default.CheckCircle // Green Checkmark icon
        )
    }

    // Detail Analisis Bullets (Dinamis & Selalu 3 Poin Informasi Lengkap)
    val analysisDetails = remember(currentReasoning, currentRiskLevel) {
        val rawReasoning = currentReasoning
        val baseList = if (rawReasoning.isNotBlank()) {
            rawReasoning.split("|||")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } else emptyList()

        if (baseList.isNotEmpty()) {
            val prefix = when (currentRiskLevel) {
                "DANGEROUS" -> "❌ "
                "CAUTIOUS" -> "⚠️ "
                else -> "✅ "
            }
            val formatted = baseList.map { if (it.startsWith("❌") || it.startsWith("⚠️") || it.startsWith("✅")) it else "$prefix$it" }.toMutableList()
            if (formatted.size < 3) {
                if (currentRiskLevel == "DANGEROUS") {
                    if (formatted.size < 2) formatted.add("❌ Teridentifikasi indikator penipuan / ancaman berisiko tinggi")
                    if (formatted.size < 3) formatted.add("❌ Disarankan tidak memberikan informasi sensitif")
                } else if (currentRiskLevel == "CAUTIOUS") {
                    if (formatted.size < 2) formatted.add("⚠️ Indikasi penipuan / pola pesan mencurigakan")
                    if (formatted.size < 3) formatted.add("⚠️ Meminta data atau tindakan pengguna")
                } else {
                    if (formatted.size < 2) formatted.add("✅ Tidak mengandung link atau konten berbahaya")
                    if (formatted.size < 3) formatted.add("✅ Tidak meminta data pribadi")
                }
            }
            formatted.take(3)
        } else {
            when (currentRiskLevel) {
                "DANGEROUS" -> listOf(
                    "❌ Mengandung kata/pola pesan mencurigakan",
                    "❌ Teridentifikasi berpotensi penipuan",
                    "❌ Berpotensi merugikan pengguna"
                )
                "CAUTIOUS" -> listOf(
                    "⚠️ Pesan terdeteksi mencurigakan",
                    "⚠️ Indikasi penipuan / pola mencurigakan",
                    "⚠️ Meminta tindakan pengguna"
                )
                else -> listOf(
                    "✅ Pengirim terverifikasi",
                    "✅ Tidak mengandung link atau konten berbahaya",
                    "✅ Tidak meminta data pribadi"
                )
            }
        }
    }

    // Cek apakah pesan benar-benar mengandung tautan/link
    val hasUrl = remember(item.snippet) {
        val text = item.snippet.lowercase()
        text.contains("http://") || text.contains("https://") || 
        text.contains("www.") || text.contains("bit.ly") || 
        text.contains(".com") || text.contains(".id") || text.contains(".top")
    }

    // Tips Aman Bullets (Dinamis & Sesuai Konten Pesan)
    val safetyTips = remember(currentRiskLevel, hasUrl) {
        when {
            hasUrl -> when (currentRiskLevel) {
                "DANGEROUS" -> listOf(
                    "Jangan klik link pada pesan ini",
                    "Jangan berikan data pribadi ke situs tersebut",
                    "Hapus pesan dan blokir pengirim"
                )
                "CAUTIOUS" -> listOf(
                    "Periksa kebenaran pesan melalui sumber resmi",
                    "Jangan masukkan data pribadi di link tersebut",
                    "Pastikan alamat link sesuai sebelum membuka"
                )
                else -> listOf(
                    "Pastikan tautan dari sumber resmi sebelum dibuka",
                    "Jangan bagikan OTP, PIN, atau data pribadi"
                )
            }
            else -> when (currentRiskLevel) {
                "DANGEROUS" -> listOf(
                    "Jangan melakukan transfer dana yang diminta",
                    "Jangan pernah memberikan kode OTP, PIN, atau kata sandi",
                    "Abaikan pesan dan blokir nomor pengirim"
                )
                "CAUTIOUS" -> listOf(
                    "Periksa kebenaran informasi melalui kontak resmi",
                    "Jangan mudah tergiur tawaran hadiah atau investasi fantastis",
                    "Jangan bagikan informasi perbankan atau data pribadi"
                )
                else -> listOf(
                    "Tetap waspada terhadap pesan dari pengirim tak dikenal",
                    "Jangan bagikan OTP, PIN, atau data pribadi"
                )
            }
        }
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
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color(0xFF0F172A))
            }
            Text(
                text = "Detail Hasil Scan",
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = theme.statusTitle,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.themeColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = theme.statusSubtitle,
                            fontSize = 10.sp,
                            color = theme.themeColor.copy(alpha = 0.8f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 2. Informasi Pesan Section ──────────────────────────────────
            Text(
                text = "Informasi Pesan",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            
            var isMessageExpanded by remember(item.id) { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ikon Chat Bubble sesuai warna status
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.cardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = theme.themeColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Detail Metadata (Sumber, Grup, Pengirim)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InfoRow("Sumber", sourceApp)
                    if (!item.groupName.isNullOrBlank()) {
                        InfoRow("Grup", item.groupName)
                    }
                    InfoRow("Pengirim", item.sender.ifBlank { "Tidak Dikenal" })
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Kotak Kontainer Khusus Isi Pesan (Collapsible & Full Width)
            Text(
                text = "Isi Pesan",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(6.dp))

            val isLongMessage = item.snippet.length > 100 || item.snippet.count { it == '\n' } >= 3

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = item.snippet,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B),
                        lineHeight = 19.sp,
                        maxLines = if (isMessageExpanded) Int.MAX_VALUE else 4,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isLongMessage) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { isMessageExpanded = !isMessageExpanded }
                                .padding(vertical = 4.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isMessageExpanded) "Sembunyikan" else "Lihat Selengkapnya",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isMessageExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 3. Detail Analisis Section ──────────────────────────────────
            Text(
                text = "Detail Analisis",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                analysisDetails.forEach { detail ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = detail,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 4. Tips Aman Card (Soft blue) ──────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(16.dp))

            // ── 5. Feedback Salah Deteksi (User Correction Card) ────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Feedback,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hasil Deteksi Kurang Sesuai?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Koreksi hasil scan untuk menyempurnakan Sekuritiw",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isFeedbackReported) {
                        // Tampilan Status Laporan Terkirim (Opsi A: Hasil scan asli tetap)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9))
                                .border(BorderStroke(1.dp, Color(0xFFCBD5E1)), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Laporan Berhasil Terkirim",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Terima kasih! Kontribusi Anda telah dicatat untuk menyempurnakan akurasi model Sekuritiw. Hasil analisis awal tetap dipertahankan demi menjaga keamanan perangkat Anda.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF475569),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showFeedbackDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF334155)
                            ),
                            border = BorderStroke(
                                1.dp,
                                Color(0xFFCBD5E1)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Feedback,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF2563EB)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Laporkan Salah Deteksi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (feedbackSuccessMsg != null && !isFeedbackReported) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ $feedbackSuccessMsg",
                            fontSize = 11.sp,
                            color = Color(0xFF059669),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Modal Dialog Feedback
    if (showFeedbackDialog) {
        ThreatFeedbackDialog(
            rawSnippet = item.snippet,
            rawSender = item.sender,
            packageName = item.packageName,
            initialIsFalsePositive = (currentRiskLevel != "SAFE"),
            originalRiskLevel = currentRiskLevel,
            isSubmitting = (submissionState is FeedbackSubmissionState.Submitting),
            onDismiss = { showFeedbackDialog = false },
            onSubmitFeedback = { feedbackType, userReportedLabel, reasonCategory, userNotes, consentVersion ->
                feedbackViewModel.submitFeedback(
                    item = item,
                    feedbackType = feedbackType,
                    userReportedLabel = userReportedLabel,
                    reasonCategory = reasonCategory,
                    userNotes = userNotes,
                    consentVersion = consentVersion
                ) {
                    // Opsi A: Hasil scan sistem tidak di-overwrite, hanya status pelaporan yang aktif
                    isFeedbackReported = true
                    reportedTypeInfo = if (feedbackType == "FALSE_POSITIVE") {
                        "Salah Deteksi (Sebenarnya Aman)"
                    } else {
                        "Salah Deteksi (Sebenarnya Berbahaya)"
                    }
                    feedbackSuccessMsg = "Laporan berhasil dikirim ke tim analis Sekuritiw."
                    showFeedbackDialog = false
                }
            }
        )
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
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
