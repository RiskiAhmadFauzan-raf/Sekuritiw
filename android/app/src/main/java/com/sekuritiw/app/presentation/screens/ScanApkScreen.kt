package com.sekuritiw.app.presentation.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sekuritiw.app.data.analyzer.AnalysisResult
import com.sekuritiw.app.data.detector.APKMalwareClassifier
import com.sekuritiw.app.domain.model.NotificationContent
import com.sekuritiw.app.presentation.viewmodel.ThreatHistoryViewModel

/**
 * Layar Scan APK Khusus (Scan APK Screen).
 * Memiliki pemilih file APK dengan tampilan Drag & Drop area sesuai desain visual.
 */
@Composable
fun ScanApkScreen(
    initialApkUri: android.net.Uri? = null,
    onClearInitialApkUri: () -> Unit = {},
    onAnalyzeResult: (AnalysisResult) -> Unit,
    onBackClick: () -> Unit,
    onItemClick: (HistoryItem) -> Unit,
    onViewAllClick: () -> Unit,
    viewModel: ThreatHistoryViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var processedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val context = LocalContext.current
    val apkClassifier = remember { APKMalwareClassifier(context) }
    val apkRecords by viewModel.apkHistoryItems.collectAsState()
    var isScanningApk by remember { mutableStateOf(false) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            isScanningApk = false
        }
    }

    // Ambil maksimal 3 riwayat APK terbaru
    val recentActivities = remember(apkRecords) {
        apkRecords.take(3)
    }

    // Launcher untuk memilih berkas sistem
    val processApkUri: suspend (android.net.Uri) -> Unit = processApkUri@{ fileUri ->
        var isApk = false
        var fileName = "Aplikasi.apk"
            try {
                context.contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                        if (fileName.lowercase().endsWith(".apk")) {
                            isApk = true
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("ScanApkScreen", "Gagal mendapatkan nama file dari query cursor: ${e.message}")
            }
            
            if (!isApk && fileUri.path?.lowercase()?.endsWith(".apk") == true) {
                isApk = true
                fileName = fileUri.path?.substringAfterLast('/') ?: "Aplikasi.apk"
            }
            
            if (!isApk && !fileName.lowercase().endsWith(".apk")) {
                android.widget.Toast.makeText(context, "File yang dipilih bukan berkas APK (.apk)", android.widget.Toast.LENGTH_LONG).show()
                return@processApkUri
            }

            val tempFile = java.io.File(context.cacheDir, "temp_scan.apk")
            try {
                context.contentResolver.openInputStream(fileUri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Menganalisis dengan Model TFLite
                val scanResult = apkClassifier.classifyAPK(tempFile.absolutePath)
                val malwareScore = scanResult.score
                val isMalware = malwareScore > 50.0f
                val dangerousPerms = scanResult.detectedDangerousPermissions
                
                val threatDetails = if (scanResult.suspectedThreatType != null) {
                    "\n\nSuspek Jenis Ancaman:\n• ${scanResult.suspectedThreatType}"
                } else {
                    ""
                }

                val permissionDetails = if (dangerousPerms.isNotEmpty()) {
                    "\n\nIzin sensitif terdeteksi:\n" + dangerousPerms.joinToString("\n") { "• $it" }
                } else {
                    "\n\nTidak ada izin sensitif yang mencurigakan."
                }
                
                val dummyContent = NotificationContent(
                    id = "apk_file_scan_${System.currentTimeMillis()}",
                    packageName = "com.android.providers.downloads",
                    title = "File APK Dipindai",
                    text = "Memindai file: $fileName. Hasil analisis AI lokal (TFLite) menunjukkan tingkat bahaya.",
                    bigText = null,
                    subText = null,
                    timestamp = System.currentTimeMillis(),
                    sender = "Pindai File APK"
                )
                
                val apkResult = com.sekuritiw.app.domain.model.APKDetectionResult(
                    detected = true,
                    fileName = fileName,
                    source = "Penyimpanan Lokal",
                    deliveryMethod = com.sekuritiw.app.domain.model.APKDeliveryMethod.DIRECT_ATTACHMENT,
                    trustLevel = if (isMalware) com.sekuritiw.app.domain.model.SourceTrustLevel.SUSPICIOUS else com.sekuritiw.app.domain.model.SourceTrustLevel.TRUSTED,
                    matchedPhrases = listOf("Local File Scan")
                )
                
                val calculatedScore = com.sekuritiw.app.domain.model.RiskScore(
                    score = malwareScore,
                    level = if (malwareScore > 70f) com.sekuritiw.app.domain.model.RiskLevel.DANGEROUS else if (malwareScore > 30f) com.sekuritiw.app.domain.model.RiskLevel.CAUTIOUS else com.sekuritiw.app.domain.model.RiskLevel.SAFE,
                    contributors = listOf(
                        com.sekuritiw.app.domain.model.RiskFactor(
                            type = com.sekuritiw.app.domain.model.FactorType.APK_DETECTION,
                            weight = 1.0f,
                            contribution = malwareScore,
                            description = "Analisis AI Lokal (TFLite) mendeteksi permissions mencurigakan."
                        )
                    ),
                    confidence = 0.99f
                )
                
                val classification = com.sekuritiw.app.domain.model.ThreatClassificationResult(
                    primaryCategory = if (isMalware) com.sekuritiw.app.domain.model.ThreatCategory.APK_SCAM else com.sekuritiw.app.domain.model.ThreatCategory.BENIGN,
                    secondaryCategories = emptyList(),
                    severityRating = if (malwareScore > 70f) com.sekuritiw.app.domain.model.SeverityRating.HIGH else if (malwareScore > 30f) com.sekuritiw.app.domain.model.SeverityRating.MEDIUM else com.sekuritiw.app.domain.model.SeverityRating.LOW,
                    confidence = 0.99f
                )
                
                val riskLevel = calculatedScore.level.name
                val factorsList = mutableListOf<String>()

                if (riskLevel == "SAFE") {
                    // Aman: tidak perlu tampilkan daftar izin
                    factorsList.add("SAFE:Tidak ditemukan kombinasi izin yang mencurigakan.")
                    factorsList.add("SAFE:Izin aplikasi normal dan wajar.")
                    factorsList.add("SAFE:Tidak terdeteksi perilaku berbahaya.")
                } else {
                    // Tambahkan tipe ancaman sebagai item berbahaya
                    if (scanResult.suspectedThreatType != null) {
                        factorsList.add("DANGER:${scanResult.suspectedThreatType}")
                    }
                    // Izin yang terdeteksi dicurigai → tandai sebagai berbahaya (DANGER) atau info (INFO)
                    val highRiskKeywords = listOf("sms", "otp", "akses", "install", "overlay", "accessibility", "kunci", "kartu sim", "panggilan telepon")
                    dangerousPerms.forEach { perm ->
                        val isHighRisk = highRiskKeywords.any { keyword -> perm.lowercase().contains(keyword) }
                        if (isHighRisk) {
                            factorsList.add("DANGER:Meminta izin: $perm")
                        } else {
                            factorsList.add("INFO:Meminta izin: $perm")
                        }
                    }
                    if (factorsList.isEmpty()) {
                        factorsList.add("DANGER:Meminta kombinasi izin yang tidak biasa")
                    }
                }

                val explanation = com.sekuritiw.app.domain.model.Explanation(
                    summary = "Berkas '$fileName' telah dianalisis 100% offline menggunakan kecerdasan buatan (TensorFlow Lite) pada perangkat.$threatDetails$permissionDetails",
                    riskFactors = factorsList,
                    detailedReasoning = "Analisis AI Lokal (TFLite) mendeteksi persentase ancaman sebesar ${malwareScore.toInt()}%. " +
                            if (isMalware) "Ditemukan kombinasi izin/permission berbahaya yang kerap disalahgunakan oleh malware/trojan untuk mencuri data SMS, kontak, atau lokasi." else "Model AI tidak mendeteksi indikasi aneh pada permission APK ini.",
                    recommendations = if (isMalware) listOf(
                        "Jangan pasang (install) file APK ini.",
                        "Hapus file APK ini dari penyimpanan HP Anda segera."
                    ) else listOf("File APK terlihat aman untuk dipasang."),
                    educationalNote = "Selalu waspada terhadap file APK yang dikirim melalui aplikasi pesan, karena ini adalah metode umum penyebaran malware."
                )
                
                val result = AnalysisResult(
                    contentId = dummyContent.id,
                    packageName = dummyContent.packageName,
                    preprocessedText = com.sekuritiw.app.domain.model.PreprocessedText(
                        original = dummyContent.text ?: "",
                        cleaned = dummyContent.text ?: "",
                        normalized = dummyContent.text?.lowercase() ?: "",
                        tokens = (dummyContent.text ?: "").split(" "),
                        extractedUrls = emptyList(),
                        extractedApks = listOf(fileName)
                    ),
                    apkResult = apkResult,
                    urlResults = emptyList(),
                    socialEngineeringResult = null,
                    calculatedRiskScore = calculatedScore,
                    classificationResult = classification,
                    explanation = explanation,
                    threatCategory = classification.primaryCategory.name,
                    riskScore = calculatedScore.score,
                    riskLevel = calculatedScore.level.name,
                    reasoning = explanation.summary,
                    processingTimeMs = 120
                )
                
                val savedReasoning = if (explanation.riskFactors.isNotEmpty()) {
                    explanation.riskFactors.joinToString("|||")
                } else {
                    explanation.detailedReasoning
                }
                
                viewModel.insertRecord(
                     id = result.contentId,
                     packageName = result.packageName,
                     sender = "Pindai File APK",
                     rawText = explanation.summary,
                     threatCategory = "APK_SCAM",
                     riskScore = malwareScore,
                     riskLevel = result.riskLevel,
                     reasoning = savedReasoning,
                     groupName = fileName
                 )
                   delay(1200)
                   withContext(Dispatchers.Main) {
                       onAnalyzeResult(result)
                   }
                
            } catch (e: Exception) {
                isScanningApk = false
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("APKScan", "Gagal menyalin atau memindai file APK: ${e.message}", e)
                android.widget.Toast.makeText(context, "Gagal memindai file: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            } finally {
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }
        }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isScanningApk = true
            scope.launch(Dispatchers.IO) {
                processApkUri(uri)
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(initialApkUri) {
        if (initialApkUri != null && initialApkUri != processedUri) {
            processedUri = initialApkUri
            onClearInitialApkUri()
            isScanningApk = true
            scope.launch(Dispatchers.IO) {
                processApkUri(initialApkUri)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Light Slate background
    ) {
        if (isScanningApk) {
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
                        text = "Scan Apk",
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
                text = "Scan APK",
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
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = Color(0xFF1E3A8A),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Scan APK untuk keamanan perangkat",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pindai file APK untuk mendeteksi malware, spyware, dan potensi ancaman lainnya.",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 2. Dropzone / Dashed Picker Card ───────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        // Drawing custom dashed border matching the mockup design
                        val stroke = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawRoundRect(
                            color = Color(0xFF94A3B8),
                            style = stroke,
                            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                        )
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudUpload,
                        contentDescription = null,
                        tint = Color(0xFF1E3A8A),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tarik & lepas file APK ke sini",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Maksimal ukuran 100 MB",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { filePickerLauncher.launch("application/vnd.android.package-archive") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(
                            text = "Pilih File APK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Format yang didukung: .apk",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
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
                        text = "✓ Unduh aplikasi hanya dari toko resmi seperti Google Play Store",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF1E3A8A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✓ Selalu pindai berkas APK sebelum melakukan instalasi manual",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF1E3A8A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✓ Jangan izinkan instalasi aplikasi dari sumber yang tidak dikenal",
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
                                text = "Belum Ada Pemindaian APK",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "File APK yang Anda pindai akan muncul di sini.",
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
                        val rawName = when {
                            rawSnippet.contains("Berkas '") && rawSnippet.contains("' telah") -> {
                                rawSnippet.substringAfter("Berkas '").substringBefore("' telah").trim()
                            }
                            item.sender.isNotBlank() && item.sender != "Pindai File APK" && !item.sender.startsWith("Pesan") -> {
                                item.sender.trim()
                            }
                            rawSnippet.contains(".apk", ignoreCase = true) -> {
                                rawSnippet.substringAfterLast("/").substringAfterLast("\\").trim()
                            }
                            else -> rawSnippet
                        }

                        val cleanName = rawName.removePrefix("File Berbahaya:")
                            .removePrefix("File Mencurigakan:")
                            .removePrefix("File Aman:")
                            .removePrefix("Berkas APK:").trim()

                        val sub = if (cleanName.isNotBlank() && !cleanName.startsWith("Berkas")) {
                            cleanName
                        } else {
                            item.sender
                        }

                        val apkRes = when (item.riskLevel) {
                            "DANGEROUS" -> R.drawable.ic_apk_red
                            "CAUTIOUS" -> R.drawable.ic_apk_yellow
                            else -> R.drawable.ic_apk_green
                        }
                        val visual = ScanApkVisualInfo(title = "APK", subtitle = sub, drawableRes = apkRes, iconColor = Color.Unspecified)

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

private data class ScanApkVisualInfo(
    val title: String,
    val subtitle: String,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val drawableRes: Int? = null,
    val iconColor: Color
)


