package com.sekuritiw.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sekuritiw.app.domain.util.SensitiveDataRedactor

/**
 * Dialog feedback salah deteksi dengan Opt-in Preview dan Persetujuan Privasi (Consent).
 * Mematuhi UU PDP No. 27/2022 dan Spesifikasi Privasi Feedback SEKURITIW (Perubahan 1 & 6).
 */
@Composable
fun ThreatFeedbackDialog(
    rawSnippet: String,
    rawSender: String?,
    initialIsFalsePositive: Boolean,
    originalRiskLevel: String,
    onDismiss: () -> Unit,
    onSubmitFeedback: (
        feedbackType: String,
        userReportedLabel: String,
        reasonCategory: String,
        userNotes: String,
        consentVersion: String
    ) -> Unit,
    isSubmitting: Boolean = false,
    packageName: String? = null
) {
    var isFalsePositive by remember { mutableStateOf(initialIsFalsePositive) }

    val fpReasons = listOf(
        "Layanan resmi terpercaya (Bank, Ekspedisi, Telco)",
        "Pengirim adalah kontak / keluarga pribadi",
        "Pesan transaksi normal tanpa link palsu",
        "Lainnya"
    )

    val fnReasons = listOf(
        "Modus penipuan / phishing link baru",
        "Meminta kode OTP / transfer uang secara mencurigakan",
        "Pengirim mencurigakan / nomor asing berbahaya",
        "Lainnya"
    )

    var selectedReason by remember(isFalsePositive) {
        mutableStateOf(if (isFalsePositive) fpReasons.first() else fnReasons.first())
    }

    var userNotes by remember { mutableStateOf("") }
    var isConsentChecked by remember { mutableStateOf(false) }

    // Jalankan penyamaran di perangkat untuk preview
    val redactedSender = remember(rawSender, packageName) { SensitiveDataRedactor.redactSender(rawSender, packageName) }
    val redactedSnippet = remember(rawSnippet) { SensitiveDataRedactor.redactText(rawSnippet) }
    val redactedNotes = remember(userNotes) { SensitiveDataRedactor.redactText(userNotes.trim()) }

    val activeColor = if (isFalsePositive) Color(0xFF059669) else Color(0xFFDC2626)
    val activeBg = if (isFalsePositive) Color(0xFFECFDF5) else Color(0xFFFEF2F2)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── 1. Header ──────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Feedback,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Laporkan Salah Deteksi",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Privasi terlindungi dengan penyamaran otomatis",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── 2. Pilihan Jenis Koreksi ────────────────────────────
                Text(
                    text = "Pesan ini menurut Anda:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val opt1Selected = isFalsePositive
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (opt1Selected) Color(0xFFECFDF5) else Color(0xFFF8FAFC))
                            .border(
                                width = if (opt1Selected) 1.5.dp else 1.dp,
                                color = if (opt1Selected) Color(0xFF059669) else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                isFalsePositive = true
                                selectedReason = fpReasons.first()
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (opt1Selected) Color(0xFF059669) else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sebenarnya Aman",
                                fontSize = 11.sp,
                                fontWeight = if (opt1Selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (opt1Selected) Color(0xFF065F46) else Color(0xFF475569)
                            )
                        }
                    }

                    val opt2Selected = !isFalsePositive
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (opt2Selected) Color(0xFFFEF2F2) else Color(0xFFF8FAFC))
                            .border(
                                width = if (opt2Selected) 1.5.dp else 1.dp,
                                color = if (opt2Selected) Color(0xFFDC2626) else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                isFalsePositive = false
                                selectedReason = fnReasons.first()
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReportProblem,
                                contentDescription = null,
                                tint = if (opt2Selected) Color(0xFFDC2626) else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sebenarnya Bahaya",
                                fontSize = 11.sp,
                                fontWeight = if (opt2Selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (opt2Selected) Color(0xFF991B1B) else Color(0xFF475569)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── 3. Pilihan Alasan ───────────────────────────────────
                Text(
                    text = "Pilih Alasan:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(6.dp))

                val currentReasons = if (isFalsePositive) fpReasons else fnReasons
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    currentReasons.forEach { reason ->
                        val isChosen = (selectedReason == reason)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isChosen) activeBg else Color(0xFFF8FAFC))
                                .border(
                                    width = 1.dp,
                                    color = if (isChosen) activeColor else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedReason = reason }
                                .padding(vertical = 8.dp, horizontal = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = reason,
                                    fontSize = 11.sp,
                                    color = if (isChosen) Color(0xFF0F172A) else Color(0xFF475569),
                                    fontWeight = if (isChosen) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isChosen) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = activeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── 4. Pratinjau Data yang Akan Dikirim (Opt-in Preview) ─
                Text(
                    text = "Pratinjau Data yang Dikirim (Sudah Disamarkan):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            Text(
                                text = "Pengirim:",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.width(80.dp)
                            )
                            Text(
                                text = "${redactedSender.senderDisplay} (${redactedSender.senderType})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Row {
                            Text(
                                text = "Tipe Laporan:",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.width(80.dp)
                            )
                            Text(
                                text = if (isFalsePositive) "False Positive (Aman)" else "False Negative (Bahaya)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = activeColor
                            )
                        }

                        Text(
                            text = "Cuplikan Pesan Tersamar:",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = redactedSnippet.ifBlank { "(Pesan kosong)" },
                                fontSize = 11.sp,
                                color = Color(0xFF1E293B),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── 5. Catatan Pengguna (Maks 200 Karakter) ─────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Catatan Tambahan (Opsional):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Text(
                        text = "${userNotes.length}/200",
                        fontSize = 10.sp,
                        color = if (userNotes.length >= 200) Color.Red else Color(0xFF94A3B8)
                    )
                }

                Text(
                    text = "Peringatan: Jangan menulis data pribadi, kata sandi, atau nomor rahasia pada catatan.",
                    fontSize = 10.sp,
                    color = Color(0xFFD97706),
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )

                OutlinedTextField(
                    value = userNotes,
                    onValueChange = { if (it.length <= 200) userNotes = it },
                    placeholder = {
                        Text(
                            text = "Contoh: Ini pesan verifikasi akun resmi...",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    ),
                    maxLines = 3,
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ── 6. Persetujuan Privasi (Consent Checkbox) ───────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "Laporan ini membantu kami memperbaiki akurasi deteksi. Sebelum dikirim, data sensitif (OTP, rekening, telepon, email) otomatis disamarkan di HP Anda. Laporan tidak terhubung ke akun pribadi dan isinya dihapus setelah ditinjau.",
                            fontSize = 10.sp,
                            color = Color(0xFF475569),
                            lineHeight = 14.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isConsentChecked = !isConsentChecked }
                        ) {
                            Checkbox(
                                checked = isConsentChecked,
                                onCheckedChange = { isConsentChecked = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Saya mengizinkan data yang sudah disamarkan ini dikirim untuk meningkatkan deteksi.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A),
                                lineHeight = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ── 7. Tombol Aksi ─────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B))
                    ) {
                        Text(text = "Batal", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val fType = if (isFalsePositive) "FALSE_POSITIVE" else "FALSE_NEGATIVE"
                            val label = if (isFalsePositive) "SAFE" else "DANGEROUS"
                            onSubmitFeedback(
                                fType,
                                label,
                                selectedReason,
                                redactedNotes,
                                "v1"
                            )
                        },
                        enabled = isConsentChecked && !isSubmitting,
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            disabledContainerColor = Color(0xFF94A3B8)
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Kirim",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
