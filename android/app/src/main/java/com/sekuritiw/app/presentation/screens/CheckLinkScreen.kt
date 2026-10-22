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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sekuritiw.app.data.analyzer.AnalysisResult
import com.sekuritiw.app.data.analyzer.ContentAnalyzer
import com.sekuritiw.app.domain.model.NotificationContent
import com.sekuritiw.app.presentation.viewmodel.ThreatHistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Layar Cek Link Khusus (Check Link Screen).
 * Memiliki input field link phishing/scam sesuai desain visual mockup.
 */
@Composable
fun CheckLinkScreen(
    onAnalyzeResult: (AnalysisResult) -> Unit,
    onBackClick: () -> Unit,
    onItemClick: (HistoryItem) -> Unit,
    onViewAllClick: () -> Unit,
    viewModel: ThreatHistoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var linkInput by remember { mutableStateOf("") }
    val analyzer = remember(context) { ContentAnalyzer(context) }
    val linkRecords by viewModel.linkHistoryItems.collectAsState()
    val scope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            isScanning = false
        }
    }

    // Ambil maksimal 3 riwayat Link terbaru
    val recentActivities = remember(linkRecords) {
        linkRecords.take(3)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Light Slate background
    ) {
        if (isScanning) {
            val dotColor = Color(0xFF1E3A8A)
            val dot1Alpha = remember { androidx.compose.animation.core.Animatable(0.3f) }
            val dot2Alpha = remember { androidx.compose.animation.core.Animatable(0.3f) }
            val dot3Alpha = remember { androidx.compose.animation.core.Animatable(0.3f) }

            androidx.compose.runtime.LaunchedEffect(Unit) {
                val tweenSpec = androidx.compose.animation.core.infiniteRepeatable<Float>(
                    animation = androidx.compose.animation.core.tween(durationMillis = 600, easing = androidx.compose.animation.core.LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                )
                launch { dot1Alpha.animateTo(1f, tweenSpec) }
                delay(200)
                launch { dot2Alpha.animateTo(1f, tweenSpec) }
                delay(200)
                launch { dot3Alpha.animateTo(1f, tweenSpec) }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(dotColor.copy(alpha = dot1Alpha.value))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(dotColor.copy(alpha = dot2Alpha.value))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(dotColor.copy(alpha = dot3Alpha.value))
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Mengecek Link",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D2A44)
                    )
                }
            }
            return
        }
        Spacer(modifier = Modifier.height(10.dp))

        // ── Header (Back & Title) ───────────────────────────────────────────
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
                text = "Cek Link",
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
            // ── 1. Top Card Information (Soft blue) ────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF2F7))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_link_green),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cek Keamanan Link",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kami akan memeriksa link untuk mendeteksi phishing, malware, dan ancaman lainnya.",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 2. Input Field Area ──────────────────────────────────────────
            Text(
                text = "Masukkan Link",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = linkInput,
                onValueChange = { linkInput = it },
                placeholder = { Text("https://contoh-link.com", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_link_green),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (linkInput.isNotEmpty()) {
                        IconButton(onClick = { linkInput = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Bersihkan",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E3A8A),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF1E293B),
                    unfocusedTextColor = Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    if (linkInput.isNotBlank()) {
                        val preprocessor = com.sekuritiw.app.data.analyzer.ContentPreprocessor()
                        val preprocessedInput = preprocessor.preprocess(linkInput)
                        val extractedLink = preprocessedInput.extractedUrls.firstOrNull() ?: linkInput.trim()
                        val normalizedUrl = when {
                            extractedLink.startsWith("https://") -> extractedLink
                            extractedLink.startsWith("http://") -> extractedLink
                            else -> "https://$extractedLink"
                        }
                        val dummyContent = NotificationContent(
                            id = "manual_link_check_${System.currentTimeMillis()}",
                            packageName = "com.android.chrome",
                            title = "Pesan Cek Link",
                            text = linkInput.trim(),
                            bigText = null,
                            subText = null,
                            timestamp = System.currentTimeMillis(),
                            sender = "Sistem Cek Link"
                        )
                        isScanning = true
                        scope.launch(Dispatchers.IO) {
                            val result = analyzer.analyze(dummyContent)
                            delay(1000)
                            
                            // Menimpa agar Tautan hanya berisi URL murni yang diekstrak
                            val finalUrls = if (preprocessedInput.extractedUrls.isNotEmpty()) {
                                preprocessedInput.extractedUrls
                            } else if (result.preprocessedText.extractedUrls.isNotEmpty()) {
                                result.preprocessedText.extractedUrls
                            } else {
                                listOf(normalizedUrl)
                            }
                            
                            val modifiedResult = result.copy(
                                preprocessedText = result.preprocessedText.copy(
                                    original = linkInput.trim(),
                                    extractedUrls = finalUrls
                                )
                            )
                            val savedReasoning = if (modifiedResult.explanation?.riskFactors?.isNotEmpty() == true) {
                                modifiedResult.explanation.riskFactors.joinToString("|||")
                            } else {
                                modifiedResult.reasoning
                            }
                            
                            withContext(Dispatchers.Main) {
                                viewModel.insertRecord(
                                     id = modifiedResult.contentId,
                                     packageName = modifiedResult.packageName,
                                     sender = "Sistem Cek Link",
                                     rawText = normalizedUrl,
                                     threatCategory = "PHISHING",
                                     riskScore = modifiedResult.riskScore,
                                     riskLevel = modifiedResult.riskLevel,
                                     reasoning = savedReasoning
                                )
                                onAnalyzeResult(modifiedResult)
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Periksa Link",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 3. Tips Aman Card (Soft blue) ──────────────────────────────
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ Jangan sembarangan mengklik tautan dari pesan mencurigakan",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF1E3A8A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✓ Periksa keaslian domain dan protokol keamanan (HTTPS)",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF1E3A8A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✓ Jangan pernah membagikan kode OTP atau PIN perbankan",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF1E3A8A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 4. Aktivitas Terbaru Section ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivitas Terbaru",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Lihat Semua",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Aktivitas Terbaru list items (Render matching the visual design as a single unified card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                if (recentActivities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Belum Ada Pemindaian Link",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tautan yang Anda periksa akan muncul di sini.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        recentActivities.forEachIndexed { index, item ->
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

                            val rawSnippet = item.snippet.substringBefore("|||").trim()
                            val urlText = if (rawSnippet.contains("http") || rawSnippet.contains(".")) {
                                rawSnippet
                            } else if (item.sender.isNotBlank() && item.sender != "Sistem Cek Link") {
                                item.sender
                            } else {
                                rawSnippet
                            }

                            val linkRes = when (item.riskLevel) {
                                "DANGEROUS" -> R.drawable.ic_link_red
                                "CAUTIOUS" -> R.drawable.ic_link_yellow
                                else -> R.drawable.ic_link_green
                            }
                            val visual = CheckLinkVisualInfo(title = "Link", subtitle = urlText, drawableRes = linkRes, iconColor = Color.Unspecified)

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
            }

            Spacer(modifier = Modifier.height(20.dp))

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private data class CheckLinkVisualInfo(
    val title: String,
    val subtitle: String,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val drawableRes: Int? = null,
    val iconColor: Color
)


