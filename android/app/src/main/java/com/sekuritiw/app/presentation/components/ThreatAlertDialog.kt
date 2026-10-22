package com.sekuritiw.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sekuritiw.app.data.analyzer.AnalysisResult
import com.sekuritiw.app.domain.model.RiskLevel

/**
 * Dialog Peringatan Ancaman (Threat Alert Dialog / Overlay) yang muncul saat
 * terdeteksi konten mencurigakan atau berbahaya.
 *
 * Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 11.8
 */
@Composable
fun ThreatAlertDialog(
    analysisResult: AnalysisResult,
    onDismiss: () -> Unit,
    onReport: () -> Unit
) {
    val riskLevel = analysisResult.riskLevel
    val (headerColor, levelTitle, statusLabel) = when (riskLevel) {
        "DANGEROUS" -> Triple(Color(0xFFDC2626), "⚠️ PERINGATAN BAHAYA", "BAHAYA")
        "CAUTIOUS" -> Triple(Color(0xFFD97706), "⚡ WASPADA ANCAMAN", "WASPADA")
        else -> Triple(Color(0xFF059669), "✅ APLIKASI AMAN", "AMAN") // SuccessDark
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, headerColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White) // NeutralSurface (White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Header Banner Risiko
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = levelTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerColor
                    )

                    // Badge Status Risiko
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(headerColor.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = headerColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Kategori Ancaman Utama
                Text(
                    text = "Kategori: ${analysisResult.threatCategory}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B) // TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Ringkasan Alasan Peringatan
                Text(
                    text = analysisResult.reasoning,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF0F172A), // TextPrimary
                    lineHeight = 20.sp
                )

                // 4. Rincian Rekomendasi Tindakan
                if (analysisResult.explanation != null && analysisResult.explanation.recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Rekomendasi Tindakan Keamanan:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A) // TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    analysisResult.explanation.recommendations.take(3).forEach { rec ->
                        Text(
                            text = "• $rec",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B), // TextSecondary
                            modifier = Modifier.padding(vertical = 2.dp),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Tombol Aksi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onReport,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)) // NeutralBorder
                    ) {
                        Text("Laporkan", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = headerColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Pahami Risiko", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
