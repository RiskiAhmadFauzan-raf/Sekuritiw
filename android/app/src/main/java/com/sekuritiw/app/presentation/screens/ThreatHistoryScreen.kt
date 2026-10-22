package com.sekuritiw.app.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.res.painterResource
import com.sekuritiw.app.R
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sekuritiw.app.presentation.viewmodel.ThreatHistoryViewModel

/** Data item riwayat ancaman */
data class HistoryItem(
    val id: String,
    val packageName: String,
    val sender: String,
    val groupName: String? = null,
    val category: String,
    val riskScore: Int,
    val riskLevel: String,
    val snippet: String,
    val timeAgo: String,
    val reasoning: String = ""
)

/**
 * Halaman Riwayat Ancaman (Threat History Screen).
 * Terhubung langsung secara real-time ke NotificationListener & Room Database.
 */
@Composable
fun ThreatHistoryScreen(
    viewModel: ThreatHistoryViewModel = hiltViewModel(),
    onItemClick: (HistoryItem) -> Unit
) {
    val realRecords by viewModel.historyItems.collectAsState()

    val activeRecords = realRecords

    // Hitung counts untuk masing-masing kategori
    val totalCount = activeRecords.size
    val pesanCount = remember(activeRecords) {
        activeRecords.count { it.sender != "Sistem Cek Link" && it.sender != "Pindai File APK" && it.category != "APK_SCAM" }
    }
    val linkCount = remember(activeRecords) {
        activeRecords.count { it.sender == "Sistem Cek Link" }
    }
    val apkCount = remember(activeRecords) {
        activeRecords.count { it.sender == "Pindai File APK" || it.category == "APK_SCAM" }
    }

    var selectedFilterIndex by remember { mutableStateOf(0) }
    val filters = listOf(
        "Semua ($totalCount)",
        "Pesan ($pesanCount)",
        "Link ($linkCount)",
        "APK ($apkCount)"
    )

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val filteredItems = remember(selectedFilterIndex, activeRecords) {
        when (selectedFilterIndex) {
            0 -> activeRecords
            1 -> activeRecords.filter { it.sender != "Sistem Cek Link" && it.sender != "Pindai File APK" && it.category != "APK_SCAM" }
            2 -> activeRecords.filter { it.sender == "Sistem Cek Link" }
            else -> activeRecords.filter { it.sender == "Pindai File APK" || it.category == "APK_SCAM" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Background putih bersih sesuai mockup
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Centered dengan Tombol Hapus Riwayat di Kanan
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Riwayat",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.align(Alignment.Center)
            )

            if (totalCount > 0) {
                IconButton(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Riwayat",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }

        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmDialog = false },
                title = {
                    Text(
                        text = "Hapus Semua Riwayat?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                text = {
                    Text(
                        text = "Tindakan ini akan mengosongkan seluruh catatan riwayat pemindaian ancaman.",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearConfirmDialog = false
                            viewModel.clearAllHistory()
                        }
                    ) {
                        Text("Hapus", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmDialog = false }) {
                        Text("Batal", color = Color(0xFF64748B))
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Bar Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters.size) { index ->
                val isSelected = index == selectedFilterIndex
                val filterText = filters[index]
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF1E3A8A) else Color(0xFFEBF2F7)) // Navy Blue vs Light Blue-Grey
                        .clickable { selectedFilterIndex = index }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = filterText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF4E6178)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Divider(color = Color(0xFFF1F5F9)) // Garis pembatas atas list

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📬", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (realRecords.isEmpty()) "Belum Ada Riwayat Pemindaian" else "Tidak ada riwayat untuk kategori ini",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (realRecords.isEmpty()) "Hasil pemindaian pesan, link, atau APK akan tersimpan di sini secara otomatis." else "Coba pilih kategori lain.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredItems.size) { index ->
                    val item = filteredItems[index]
                    val (badgeBg, badgeText, badgeLabel) = when (item.riskLevel) {
                        "DANGEROUS" -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "Berbahaya")
                        "CAUTIOUS" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "Waspada")
                        else -> Triple(Color(0xFFECFDF5), Color(0xFF059669), "Aman")
                    }

                    val isManualApk = item.sender == "Pindai File APK" || item.category == "APK_SCAM"
                    val isManualLink = item.sender == "Sistem Cek Link"

                    val visual = when {
                        isManualApk -> {
                            val rawName = when {
                                !item.groupName.isNullOrBlank() -> item.groupName
                                item.snippet.contains("Berkas '") && item.snippet.contains("' telah") -> {
                                    item.snippet.substringAfter("Berkas '").substringBefore("' telah").trim()
                                }
                                item.sender != "Pindai File APK" && item.sender.lowercase().endsWith(".apk") -> {
                                    item.sender.trim()
                                }
                                item.snippet.contains(".apk", ignoreCase = true) -> {
                                    item.snippet.substringAfterLast("/").substringAfterLast("\\").trim()
                                }
                                else -> ""
                            }

                            val cleanName = rawName.removePrefix("File Berbahaya:")
                                .removePrefix("File Mencurigakan:")
                                .removePrefix("File Aman:")
                                .removePrefix("Berkas APK:").trim()

                            val sub = if (cleanName.isNotBlank()) {
                                when (item.riskLevel) {
                                    "DANGEROUS" -> "File Berbahaya: $cleanName"
                                    "CAUTIOUS" -> "File Mencurigakan: $cleanName"
                                    else -> "File Aman: $cleanName"
                                }
                            } else {
                                when (item.riskLevel) {
                                    "DANGEROUS" -> "File APK Berbahaya Diblokir"
                                    "CAUTIOUS" -> "File APK Perlu Kewaspadaan"
                                    else -> "File APK Aman Dipasang"
                                }
                            }
                            val apkRes = when (item.riskLevel) {
                                "DANGEROUS" -> R.drawable.ic_apk_red
                                "CAUTIOUS" -> R.drawable.ic_apk_yellow
                                else -> R.drawable.ic_apk_green
                            }
                            CategoryVisual(title = "APK", subtitle = sub, drawableRes = apkRes, iconColor = Color.Unspecified)
                        }
                        isManualLink -> {
                            val words = item.snippet.split("\\s+".toRegex())
                            val url = words.firstOrNull { it.contains("http") || it.contains("bit.ly") || it.contains(".xyz") } ?: item.snippet
                            val linkRes = when (item.riskLevel) {
                                "DANGEROUS" -> R.drawable.ic_link_red
                                "CAUTIOUS" -> R.drawable.ic_link_yellow
                                else -> R.drawable.ic_link_green
                            }
                            CategoryVisual(title = "Link", subtitle = url, drawableRes = linkRes, iconColor = Color.Unspecified)
                        }
                        else -> {
                            val sub = if (item.snippet.startsWith("Pesan dari") || item.snippet.startsWith("Pesan aman:")) {
                                item.snippet
                            } else if (!item.groupName.isNullOrBlank()) {
                                "Pesan dari ${item.sender} di ${item.groupName}"
                            } else if (item.sender.isNotBlank()) {
                                "Pesan dari ${item.sender}"
                            } else {
                                item.snippet
                            }
                            val msgRes = when (item.riskLevel) {
                                "DANGEROUS" -> R.drawable.ic_message_red
                                "CAUTIOUS" -> R.drawable.ic_message_yellow
                                else -> R.drawable.ic_message_green
                            }
                            CategoryVisual(title = "Pesan", subtitle = sub, drawableRes = msgRes, iconColor = Color.Unspecified)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(item) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Icon Kiri (Ikon APK kustom atau Ikon Vector)
                            if (visual.drawableRes != null) {
                                Image(
                                    painter = painterResource(id = visual.drawableRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(34.dp)
                                )
                            } else if (visual.iconVector != null) {
                                Icon(
                                    imageVector = visual.iconVector,
                                    contentDescription = null,
                                    tint = visual.iconColor,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // 2. Info Kategori (APK / Link / Pesan) & Subtitle
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = visual.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = visual.subtitle,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // 3. Badge Capsule Kanan
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = badgeLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeText
                                )
                            }
                        }

                        Divider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}

/** Helper untuk memetakan package name ke nama aplikasi user-friendly */
fun getAppName(packageName: String): String {
    return when (packageName) {
        "com.whatsapp" -> "WhatsApp"
        "com.whatsapp.w4b" -> "WhatsApp Business"
        "org.telegram.messenger" -> "Telegram"
        "com.instagram.android" -> "Instagram"
        "com.facebook.orca" -> "Messenger"
        "com.google.android.apps.messaging" -> "Messages"
        "com.android.mms" -> "SMS"
        "com.samsung.android.messaging" -> "Samsung SMS"
        "jp.naver.line.android" -> "LINE"
        "com.google.android.gm" -> "Gmail"
        "com.yahoo.mobile.client.android.mail" -> "Yahoo Mail"
        else -> packageName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
    }
}

/** Helper untuk melabeli kategori ancaman dalam Bahasa Indonesia */
fun getCategoryLabel(category: String): String {
    return when (category) {
        "APK_SCAM" -> "APK Palsu"
        "PHISHING" -> "Phishing"
        "SOCIAL_ENGINEERING" -> "Manipulasi"
        "SPAM" -> "Spam"
        "FINANCIAL_SCAM" -> "Investasi Bodong"
        "BENIGN" -> "Aman"
        else -> "Lainnya"
    }
}

/** Helper untuk menghapus duplikasi nama pengirim dari isi notifikasi */
fun getCleanSnippet(snippet: String, sender: String): String {
    var cleaned = snippet.trim()
    val trimmedSender = sender.trim()
    if (trimmedSender.isBlank()) return cleaned

    // Bersihkan prefix sender (misal "Riski Simp: halo" atau "Riski Simp halo")
    val prefixWithColon = "$trimmedSender:"
    if (cleaned.startsWith(prefixWithColon, ignoreCase = true)) {
        cleaned = cleaned.substring(prefixWithColon.length).trim()
    } else if (cleaned.startsWith(trimmedSender, ignoreCase = true)) {
        cleaned = cleaned.substring(trimmedSender.length).trim()
    }

    // Bersihkan suffix sender (misal "halo Riski Simp" atau "3 Riski Simp")
    if (cleaned.endsWith(trimmedSender, ignoreCase = true)) {
        cleaned = cleaned.substring(0, cleaned.length - trimmedSender.length).trim()
    }
    
    // Jika setelah dibersihkan menjadi kosong (karena isi pesan hanya berisi nama pengirim), kembalikan teks asli
    return if (cleaned.isEmpty()) snippet else cleaned
}

private data class CategoryVisual(
    val title: String,
    val subtitle: String,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val drawableRes: Int? = null,
    val iconColor: Color
)

