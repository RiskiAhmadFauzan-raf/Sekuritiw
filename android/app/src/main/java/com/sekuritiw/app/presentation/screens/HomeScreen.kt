package com.sekuritiw.app.presentation.screens

import android.content.Context
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import android.content.Intent
import android.provider.Settings
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.sekuritiw.app.presentation.theme.SekuritiwTheme
import com.sekuritiw.app.data.analyzer.AnalysisResult
import com.sekuritiw.app.data.analyzer.ContentAnalyzer
import com.sekuritiw.app.domain.model.NotificationContent
import com.sekuritiw.app.presentation.viewmodel.ThreatHistoryViewModel
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.sekuritiw.app.data.detector.APKMalwareClassifier



@Composable
fun HomeScreen(
    onViewAllClick: () -> Unit,
    onItemClick: (HistoryItem) -> Unit,
    onScanApkClick: () -> Unit,
    onCheckLinkClick: () -> Unit,
    viewModel: ThreatHistoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val realRecords by viewModel.historyItems.collectAsState()
    
    var isListenerEnabled by remember {
        mutableStateOf(isNotificationListenerEnabled(context))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isListenerEnabled = isNotificationListenerEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    HomeScreenContent(
        onViewAllClick = onViewAllClick,
        onItemClick = onItemClick,
        onScanApkClick = onScanApkClick,
        onCheckLinkClick = onCheckLinkClick,
        isListenerEnabled = isListenerEnabled,
        historyItems = realRecords
    )
}

@Composable
fun HomeScreenContent(
    onViewAllClick: () -> Unit,
    onItemClick: (HistoryItem) -> Unit,
    onScanApkClick: () -> Unit,
    onCheckLinkClick: () -> Unit,
    isListenerEnabled: Boolean,
    historyItems: List<HistoryItem>
) {
    val context = LocalContext.current

    val tinggiCount = historyItems.count { it.riskLevel == "DANGEROUS" }
    val sedangCount = historyItems.count { it.riskLevel == "CAUTIOUS" }
    val rendahCount = historyItems.count { it.riskLevel == "SAFE" }
    val totalCount = historyItems.size

    // Tentukan daftar aktivitas yang akan ditampilkan
    val activitiesToDisplay = remember(historyItems) {
        historyItems.take(3)
    }


    var showGuideDialog by remember { mutableStateOf(false) }

    if (showGuideDialog) {
        com.sekuritiw.app.presentation.components.RestrictedSettingsGuideDialog(
            isListenerEnabled = isListenerEnabled,
            onDismiss = { showGuideDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // NeutralBg Light Grey-Blue
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // ── 1. Perlindungan Status Card (Dynamic Green / Red state) ────────────────
        val cardBg = if (isListenerEnabled) Color(0xFFECFDF5) else Color(0xFFFEE2E2)
        val cardBorderColor = if (isListenerEnabled) Color(0xFF6EE7B7) else Color(0xFFFCA5A5)
        val iconTint = if (isListenerEnabled) Color(0xFF059669) else Color(0xFFDC2626)
        val titleColor = if (isListenerEnabled) Color(0xFF064E3B) else Color(0xFF991B1B)
        val subColor = if (isListenerEnabled) Color(0xFF047857) else Color(0xFFB91C1C)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp))
                .clickable {
                    if (!isListenerEnabled) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            showGuideDialog = true
                        } else {
                            com.sekuritiw.app.presentation.components.openNotificationListenerSettings(context)
                        }
                    }
                },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isListenerEnabled) Icons.Outlined.Shield else Icons.Default.Warning,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isListenerEnabled) "Perlindungan Aktif" else "Proteksi Belum Aktif",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isListenerEnabled) "Sistem memantau ancaman secara real-time" else "Izin Akses Notifikasi Dibutuhkan. Klik di sini.",
                            fontSize = 11.sp,
                            color = subColor
                        )
                    }
                }
                
                Icon(
                    imageVector = if (isListenerEnabled) Icons.Filled.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── 2. Tingkat Risiko Terhadang Card (White Card from Image 2) ──────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)), // NeutralBorder
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White) // NeutralSurface White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tingkat Risiko Terhadang",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B) // TextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DonutChart(
                        tinggiCount = tinggiCount,
                        sedangCount = sedangCount,
                        rendahCount = rendahCount,
                        totalDisplayOverride = totalCount,
                        modifier = Modifier.size(110.dp)
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        LegendRow(
                            color = Color(0xFFEF4444), // DangerRed
                            label = "Tinggi",
                            count = tinggiCount,
                            countColor = Color(0xFFDC2626) // DangerDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LegendRow(
                            color = Color(0xFFF59E0B), // WarningYellow
                            label = "Sedang",
                            count = sedangCount,
                            countColor = Color(0xFFD97706) // WarningDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LegendRow(
                            color = Color(0xFF10B981), // SuccessGreen
                            label = "Rendah",
                            count = rendahCount,
                            countColor = Color(0xFF059669) // SuccessDark
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 3. Pemindaian Cepat Section ────────────────────────────
        Text(
            text = "Pemindaian Cepat",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cek Link Button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                    .clickable { onCheckLinkClick() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECFDF5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_link_green),
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cek Link",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Phising",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Scan APK Button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                    .clickable { onScanApkClick() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE6EEF6)), // Light blue-grey tint
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = Color(0xFF2563EB), // PrimaryBlue
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scan APK",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "File Jahat",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── 4. Aktivitas Terbaru Section ───────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aktivitas Terbaru",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Lihat Semua",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB), // PrimaryBlue
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            if (activitiesToDisplay.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Belum Ada Aktivitas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hasil pemindaian pesan, link, atau APK akan muncul di sini.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    activitiesToDisplay.forEachIndexed { index, item ->
                    if (index > 0) {
                        Divider(
                            color = Color(0xFFE2E8F0),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

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
                            HomeVisualInfo(title = "APK", subtitle = sub, drawableRes = apkRes, iconColor = Color.Unspecified)
                        }
                        isManualLink -> {
                            val words = item.snippet.split("\\s+".toRegex())
                            val url = words.firstOrNull { it.contains("http") || it.contains("bit.ly") || it.contains(".xyz") } ?: item.snippet
                            val linkRes = when (item.riskLevel) {
                                "DANGEROUS" -> R.drawable.ic_link_red
                                "CAUTIOUS" -> R.drawable.ic_link_yellow
                                else -> R.drawable.ic_link_green
                            }
                            HomeVisualInfo(title = "Link", subtitle = url, drawableRes = linkRes, iconColor = Color.Unspecified)
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
                            HomeVisualInfo(title = "Pesan", subtitle = sub, drawableRes = msgRes, iconColor = Color.Unspecified)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(item) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
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
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp)) // Padding bawah nav bar
    }
}
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SekuritiwTheme {
        HomeScreenContent(
            onViewAllClick = {},
            onItemClick = {},
            onScanApkClick = {},
            onCheckLinkClick = {},
            isListenerEnabled = true,
            historyItems = emptyList()
        )
    }
}

@Composable
fun AktivitasAvatar(category: String, snippet: String, riskColor: Color) {
    val isApk = category == "APK_SCAM" || snippet.contains(".apk", ignoreCase = true)
    val isLink = category == "PHISHING" || snippet.contains("http", ignoreCase = true) || snippet.contains("bit.ly", ignoreCase = true)

    val (bg, icon) = when {
        isApk -> Color(0xFFEFF6FF) to Icons.Default.Android
        isLink -> Color(0xFFECFDF5) to Icons.Default.Link
        else -> Color(0xFFF5F3FF) to Icons.Default.Email
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = riskColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** Ikon Kustom Delta (Lingkaran abu-abu dengan segitiga putih) */
@Preview
@Composable
fun CustomDeltaIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFF5B7086)), // Grey-blue circle
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width / 2f, 1.dp.toPx())
                lineTo(size.width, size.height - 1.dp.toPx())
                lineTo(0f, size.height - 1.dp.toPx())
                close()
            }
            drawPath(path = path, color = Color.White)
            
            // Titik di dalam delta untuk sentuhan estetis
            drawCircle(
                color = Color(0xFF5B7086),
                radius = 2.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height * 0.65f)
            )
        }
    }
}

/** Legend Row untuk Donut Chart */
@Composable
fun LegendRow(color: Color, label: String, count: Int, countColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = count.toString(),
            fontSize = 12.sp,
            color = countColor, // Berwarna sesuai kategori
            fontWeight = FontWeight.Bold
        )
    }
}

/** Donut Chart Custom Canvas */
@Preview(showBackground = true)
@Composable
fun DonutChartPreview() {
    SekuritiwTheme {
        DonutChart(
            tinggiCount = 2,
            sedangCount = 3,
            rendahCount = 7,
            totalDisplayOverride = 12,
            modifier = Modifier.size(200.dp)
        )
    }
}

/** Donut Chart Custom Canvas */
@Composable
fun DonutChart(
    tinggiCount: Int,
    sedangCount: Int,
    rendahCount: Int,
    totalDisplayOverride: Int,
    modifier: Modifier = Modifier
) {
    val totalReal = tinggiCount + sedangCount + rendahCount
    val totalText = totalDisplayOverride.toString()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            
            val tinggiAngle = if (totalReal > 0) (tinggiCount.toFloat() / totalReal) * 360f else 120f
            val sedangAngle = if (totalReal > 0) (sedangCount.toFloat() / totalReal) * 360f else 120f
            val rendahAngle = if (totalReal > 0) (rendahCount.toFloat() / totalReal) * 360f else 120f

            if (totalReal == 0) {
                // Neutral gray circle if no records at all
                drawArc(
                    color = Color(0xFFCBD5E1),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                )
            } else {
                // Draw arc for Tinggi (Red)
                drawArc(
                    color = Color(0xFFEF4444),
                    startAngle = -90f,
                    sweepAngle = tinggiAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                )

                // Draw arc for Sedang (Yellow/Amber)
                drawArc(
                    color = Color(0xFFF59E0B),
                    startAngle = -90f + tinggiAngle,
                    sweepAngle = sedangAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                )

                // Draw arc for Rendah (Green)
                drawArc(
                    color = Color(0xFF10B981),
                    startAngle = -90f + tinggiAngle + sedangAngle,
                    sweepAngle = rendahAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                )
            }
        }

        // Center Text: e.g. "12 Total"
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = totalText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Total",
                fontSize = 10.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

/** Cek apakah NotificationListenerService SEKURITIW sudah diizinkan di sistem. */
private fun isNotificationListenerEnabled(context: Context): Boolean {
    val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
    return enabledPackages.contains(context.packageName)
}

/** Helper untuk memetakan deskripsi aktivitas */
fun getDisplayTitleAndSubtext(item: HistoryItem): Pair<String, String> {
    if (item.id.startsWith("mock_")) {
        return when (item.id) {
            "mock_1" -> "APK Penipuan Diblokir" to "File Berbahaya: Ninja_Expres.apk"
            "mock_2" -> "Link Mencurigakan" to "bit.ly/promo-hadiah"
            "mock_3" -> "WhatsApp Aman" to "Pesan dari 0812-xxxx-xxxx"
            else -> "Aktivitas Terdeteksi" to item.snippet
        }
    }
    
    val title = when (item.category) {
        "APK_SCAM" -> "APK Penipuan Diblokir"
        "PHISHING" -> "Link Mencurigakan"
        "BENIGN" -> "WhatsApp Aman"
        "SOCIAL_ENGINEERING" -> "Percakapan Mencurigakan"
        "SPAM" -> "SMS Spam Terdeteksi"
        "FINANCIAL_SCAM" -> "Investasi Mencurigakan"
        else -> "Aktivitas Terdeteksi"
    }
    
    val subtext = when (item.category) {
        "APK_SCAM" -> {
            if (item.snippet.contains(".apk", ignoreCase = true)) {
                val apkName = item.snippet.substringAfterLast("/").substringAfterLast("\\").trim()
                "File Berbahaya: $apkName"
            } else {
                "File Berbahaya: ${item.snippet.take(30)}"
            }
        }
        "PHISHING" -> {
            val words = item.snippet.split("\\s+".toRegex())
            val url = words.firstOrNull { it.contains("http") || it.contains("bit.ly") || it.contains(".xyz") } ?: item.snippet.take(30)
            url
        }
        "BENIGN" -> {
            "Pesan dari ${item.sender}"
        }
        else -> item.snippet.take(35)
    }
    return title to subtext
}

/** Helper untuk memetakan package name ke nama aplikasi user-friendly */
private fun getAppNameLocal(packageName: String): String {
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

private data class HomeVisualInfo(
    val title: String,
    val subtitle: String,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val drawableRes: Int? = null,
    val iconColor: Color
)


