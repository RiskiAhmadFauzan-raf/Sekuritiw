package com.sekuritiw.app.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sekuritiw.app.data.analyzer.AnalysisResult
import com.sekuritiw.app.presentation.components.ThreatAlertDialog
import com.sekuritiw.app.presentation.screens.EdukasiScreen
import com.sekuritiw.app.presentation.screens.EdukasiDetailScreen
import com.sekuritiw.app.presentation.screens.HistoryItem
import com.sekuritiw.app.presentation.screens.HomeScreen
import com.sekuritiw.app.presentation.screens.ApkScanResultScreen
import com.sekuritiw.app.presentation.screens.LinkScanResultScreen
import com.sekuritiw.app.presentation.screens.CheckLinkScreen
import com.sekuritiw.app.presentation.screens.ScanApkScreen
import com.sekuritiw.app.presentation.screens.SettingsScreen
import com.sekuritiw.app.presentation.screens.ThreatDetailScreen
import com.sekuritiw.app.presentation.screens.ThreatHistoryScreen
import com.sekuritiw.app.presentation.screens.SplashScreen
import com.sekuritiw.app.presentation.screens.OnboardingScreen
import com.sekuritiw.app.presentation.theme.CyanPrimary
import com.sekuritiw.app.presentation.theme.DarkBackground
import com.sekuritiw.app.presentation.theme.DarkSurface
import com.sekuritiw.app.presentation.theme.TextMuted
import com.sekuritiw.app.presentation.theme.TextPrimary

sealed class NavRoute(val route: String, val label: String, val icon: ImageVector, val iconSelected: ImageVector) {
    object Home    : NavRoute("home",    "Beranda",  Icons.Outlined.Home,    Icons.Filled.Home)
    object History : NavRoute("history", "Riwayat",  Icons.Outlined.History, Icons.Filled.History)
    object Edukasi : NavRoute("edukasi", "Edukasi",  Icons.Outlined.School,   Icons.Filled.School)
    object Settings: NavRoute("settings","Pengaturan",Icons.Outlined.Settings,Icons.Filled.Settings)
}

val bottomNavItems = listOf(NavRoute.Home, NavRoute.History, NavRoute.Edukasi, NavRoute.Settings)

/**
 * Graf Navigasi utama SEKURITIW App yang mengelola Bottom Navigation Bar
 * dan perpindahan layar antara Home, History, Detail, dan Settings.
 */
@Composable
fun SekuritiwNavGraph(
    sharedText: String? = null,
    sharedApkUri: android.net.Uri? = null,
    targetRecordId: String? = null,
    navigateToHistory: Boolean = false,
    onClearSharedText: () -> Unit = {},
    onClearSharedApkUri: () -> Unit = {},
    onClearTargetRecordId: () -> Unit = {},
    onClearNavigateToHistory: () -> Unit = {},
    historyViewModel: com.sekuritiw.app.presentation.viewmodel.ThreatHistoryViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController: NavHostController = rememberNavController()
    var pendingAlertResult by remember { mutableStateOf<AnalysisResult?>(null) }
    var selectedHistoryItem by remember { mutableStateOf<HistoryItem?>(null) }
    var lastApkScanResult by remember { mutableStateOf<AnalysisResult?>(null) }
    var lastLinkScanResult by remember { mutableStateOf<AnalysisResult?>(null) }

    // Navigasi langsung ke detail pesan saat notifikasi ancaman diklik
    androidx.compose.runtime.LaunchedEffect(targetRecordId) {
        if (!targetRecordId.isNullOrBlank()) {
            historyViewModel.getHistoryItemById(targetRecordId) { item ->
                if (item != null) {
                    selectedHistoryItem = item
                    navController.navigate("detail")
                } else {
                    navController.navigate(NavRoute.History.route)
                }
                onClearTargetRecordId()
            }
        }
    }

    // Navigasi ke riwayat jika diarahkan dari notifikasi umum
    androidx.compose.runtime.LaunchedEffect(navigateToHistory) {
        if (navigateToHistory) {
            navController.navigate(NavRoute.History.route)
            onClearNavigateToHistory()
        }
    }

    // Fitur "Share to Scan": Otomatis menganalisis link yang dibagikan dari luar aplikasi (WA, SMS, Chrome)
    androidx.compose.runtime.LaunchedEffect(sharedText) {
        if (!sharedText.isNullOrBlank()) {
            val analyzer = com.sekuritiw.app.data.analyzer.ContentAnalyzer(context)
            val dummyContent = com.sekuritiw.app.domain.model.NotificationContent(
                id = "shared_link_scan_${System.currentTimeMillis()}",
                packageName = "com.android.chrome",
                title = "Share to Scan",
                text = "Link dibagikan: $sharedText",
                bigText = null,
                subText = null,
                timestamp = System.currentTimeMillis(),
                sender = "Share to Scan"
            )
            val result = analyzer.analyze(dummyContent)
            val modifiedResult = result.copy(
                preprocessedText = result.preprocessedText.copy(
                    extractedUrls = if (result.preprocessedText.extractedUrls.isNotEmpty()) result.preprocessedText.extractedUrls else listOf(sharedText)
                )
            )
            onClearSharedText()
            lastLinkScanResult = modifiedResult
            navController.navigate("link_result")
        }
    }

    androidx.compose.runtime.LaunchedEffect(sharedApkUri) {
        if (sharedApkUri != null) {
            navController.navigate("scan_apk")
        }
    }

    // Tampilkan dialog peringatan jika ada ancaman terdeteksi dari manual scan
    pendingAlertResult?.let { result ->
        ThreatAlertDialog(
            analysisResult = result,
            onDismiss = { pendingAlertResult = null },
            onReport = { pendingAlertResult = null /* TODO: Report flow */ }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC), // Set background color to light
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Sembunyikan bottom nav di luar main tabs
            val showBottomBar = currentRoute in listOf(
                NavRoute.Home.route,
                NavRoute.History.route,
                NavRoute.Edukasi.route,
                NavRoute.Settings.route
            )

            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        popUpTo(NavRoute.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.iconSelected else item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF4E6178), // Slate gray-blue
                                selectedTextColor = Color(0xFF4E6178),
                                unselectedIconColor = Color(0xFF94A3B8), // Cool grey
                                unselectedTextColor = Color(0xFF94A3B8),
                                indicatorColor = Color(0xFFE6EEF6) // Light blue-grey background pill
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)) // Set background color to light
                .padding(innerPadding)
        ) {
            val prefs = remember { context.getSharedPreferences("sekuritiw_prefs", android.content.Context.MODE_PRIVATE) }

            NavHost(
                navController = navController,
                startDestination = "splash"
            ) {
                composable("splash") {
                    SplashScreen(
                        onTimeout = {
                            val hasCompleted = prefs.getBoolean("onboarding_completed", false)
                            val target = if (hasCompleted) NavRoute.Home.route else "onboarding"
                            navController.navigate(target) {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    )
                }

                composable("onboarding") {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            prefs.edit().putBoolean("onboarding_completed", true).apply()
                            navController.navigate(NavRoute.Home.route) {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    )
                }

                composable(NavRoute.Home.route) {
                    HomeScreen(
                        onViewAllClick = {
                            navController.navigate(NavRoute.History.route) {
                                popUpTo(NavRoute.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onItemClick = { item ->
                            selectedHistoryItem = item
                            navController.navigate("detail")
                        },
                        onScanApkClick = {
                            navController.navigate("scan_apk")
                        },
                        onCheckLinkClick = {
                            navController.navigate("check_link")
                        }
                    )
                }

                composable("scan_apk") {
                    ScanApkScreen(
                        initialApkUri = sharedApkUri,
                        onClearInitialApkUri = onClearSharedApkUri,
                        onAnalyzeResult = { result ->
                            if (result.apkResult != null) {
                                lastApkScanResult = result
                                navController.navigate("apk_result")
                            } else if (result.preprocessedText.extractedUrls.isNotEmpty() || result.threatCategory == "PHISHING") {
                                lastLinkScanResult = result
                                navController.navigate("link_result")
                            } else {
                                pendingAlertResult = result
                            }
                        },
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onItemClick = { item ->
                            selectedHistoryItem = item
                            navController.navigate("detail")
                        },
                        onViewAllClick = {
                            navController.navigate(NavRoute.History.route) {
                                popUpTo(NavRoute.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable("check_link") {
                    CheckLinkScreen(
                        onAnalyzeResult = { result ->
                            lastLinkScanResult = result
                            navController.navigate("link_result")
                        },
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onItemClick = { item ->
                            selectedHistoryItem = item
                            navController.navigate("detail")
                        },
                        onViewAllClick = {
                            navController.navigate(NavRoute.History.route) {
                                popUpTo(NavRoute.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable("apk_result") {
                    ApkScanResultScreen(
                        result = lastApkScanResult,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable("link_result") {
                    LinkScanResultScreen(
                        result = lastLinkScanResult,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(NavRoute.History.route) {
                    ThreatHistoryScreen(
                        onItemClick = { item ->
                            selectedHistoryItem = item
                            navController.navigate("detail")
                        }
                    )
                }

                composable(NavRoute.Edukasi.route) {
                    EdukasiScreen(
                        onMaterialClick = { materialId ->
                            navController.navigate("edukasi_detail/$materialId")
                        }
                    )
                }

                composable("edukasi_detail/{materialId}") { backStackEntry ->
                    val materialId = backStackEntry.arguments?.getString("materialId") ?: "1"
                    EdukasiDetailScreen(
                        materialId = materialId,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("detail") {
                    selectedHistoryItem?.let { item ->
                        val rawFactorsString = when {
                            item.reasoning.contains("|||") -> item.reasoning
                            item.snippet.contains("|||") -> item.snippet
                            else -> ""
                        }
                        val parsedFactors = if (rawFactorsString.isNotEmpty()) {
                            rawFactorsString.split("|||")
                        } else {
                            emptyList()
                        }

                        val apkFileName = when {
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
                            else -> item.sender
                        }

                        if (item.sender == "Pindai File APK" || item.category == "APK_SCAM") {
                            // Construct dummy AnalysisResult from HistoryItem and show ApkScanResultScreen!
                            val dummyResult = AnalysisResult(
                                contentId = item.id,
                                packageName = item.packageName,
                                preprocessedText = com.sekuritiw.app.domain.model.PreprocessedText(
                                    original = item.snippet,
                                    cleaned = item.snippet,
                                    normalized = item.snippet.lowercase(),
                                    tokens = item.snippet.split(" "),
                                    extractedUrls = emptyList(),
                                    extractedApks = listOf(apkFileName)
                                ),
                                apkResult = com.sekuritiw.app.domain.model.APKDetectionResult(
                                    detected = true,
                                    fileName = apkFileName,
                                    source = "Penyimpanan Lokal",
                                    deliveryMethod = com.sekuritiw.app.domain.model.APKDeliveryMethod.DIRECT_ATTACHMENT,
                                    trustLevel = if (item.riskLevel == "DANGEROUS") com.sekuritiw.app.domain.model.SourceTrustLevel.SUSPICIOUS else com.sekuritiw.app.domain.model.SourceTrustLevel.TRUSTED,
                                    matchedPhrases = emptyList()
                                ),
                                urlResults = emptyList(),
                                socialEngineeringResult = null,
                                calculatedRiskScore = com.sekuritiw.app.domain.model.RiskScore(
                                    score = item.riskScore.toFloat(),
                                    level = if (item.riskLevel == "DANGEROUS") com.sekuritiw.app.domain.model.RiskLevel.DANGEROUS else if (item.riskLevel == "CAUTIOUS") com.sekuritiw.app.domain.model.RiskLevel.CAUTIOUS else com.sekuritiw.app.domain.model.RiskLevel.SAFE,
                                    contributors = emptyList(),
                                    confidence = 0.99f
                                ),
                                classificationResult = com.sekuritiw.app.domain.model.ThreatClassificationResult(
                                    primaryCategory = com.sekuritiw.app.domain.model.ThreatCategory.APK_SCAM,
                                    secondaryCategories = emptyList(),
                                    severityRating = if (item.riskLevel == "DANGEROUS") com.sekuritiw.app.domain.model.SeverityRating.HIGH else if (item.riskLevel == "CAUTIOUS") com.sekuritiw.app.domain.model.SeverityRating.MEDIUM else com.sekuritiw.app.domain.model.SeverityRating.LOW,
                                    confidence = 0.99f
                                ),
                                explanation = com.sekuritiw.app.domain.model.Explanation(
                                    summary = item.snippet.substringBefore("|||"),
                                    riskFactors = parsedFactors,
                                    detailedReasoning = "",
                                    recommendations = emptyList(),
                                    educationalNote = ""
                                ),
                                threatCategory = "APK_SCAM",
                                riskScore = item.riskScore.toFloat(),
                                riskLevel = item.riskLevel,
                                reasoning = item.snippet,
                                processingTimeMs = 120
                            )
                            ApkScanResultScreen(
                                result = dummyResult,
                                onBackClick = { navController.popBackStack() }
                            )
                        } else if (item.sender == "Sistem Cek Link") {
                            // Construct dummy AnalysisResult from HistoryItem and show LinkScanResultScreen!
                            val dummyResult = AnalysisResult(
                                contentId = item.id,
                                packageName = item.packageName,
                                preprocessedText = com.sekuritiw.app.domain.model.PreprocessedText(
                                    original = item.snippet,
                                    cleaned = item.snippet,
                                    normalized = item.snippet.lowercase(),
                                    tokens = item.snippet.split(" "),
                                    extractedUrls = listOf(item.snippet.substringBefore("|||")),
                                    extractedApks = emptyList()
                                ),
                                apkResult = null,
                                urlResults = emptyList(),
                                socialEngineeringResult = null,
                                calculatedRiskScore = com.sekuritiw.app.domain.model.RiskScore(
                                    score = item.riskScore.toFloat(),
                                    level = if (item.riskLevel == "DANGEROUS") com.sekuritiw.app.domain.model.RiskLevel.DANGEROUS else if (item.riskLevel == "CAUTIOUS") com.sekuritiw.app.domain.model.RiskLevel.CAUTIOUS else com.sekuritiw.app.domain.model.RiskLevel.SAFE,
                                    contributors = emptyList(),
                                    confidence = 0.99f
                                ),
                                classificationResult = com.sekuritiw.app.domain.model.ThreatClassificationResult(
                                    primaryCategory = com.sekuritiw.app.domain.model.ThreatCategory.PHISHING,
                                    secondaryCategories = emptyList(),
                                    severityRating = if (item.riskLevel == "DANGEROUS") com.sekuritiw.app.domain.model.SeverityRating.HIGH else if (item.riskLevel == "CAUTIOUS") com.sekuritiw.app.domain.model.SeverityRating.MEDIUM else com.sekuritiw.app.domain.model.SeverityRating.LOW,
                                    confidence = 0.99f
                                ),
                                explanation = com.sekuritiw.app.domain.model.Explanation(
                                    summary = item.snippet.substringBefore("|||"),
                                    riskFactors = parsedFactors,
                                    detailedReasoning = "",
                                    recommendations = emptyList(),
                                    educationalNote = ""
                                ),
                                threatCategory = "PHISHING",
                                riskScore = item.riskScore.toFloat(),
                                riskLevel = item.riskLevel,
                                reasoning = item.snippet,
                                processingTimeMs = 120
                            )
                            LinkScanResultScreen(
                                result = dummyResult,
                                onBackClick = { navController.popBackStack() }
                            )
                        } else {
                            ThreatDetailScreen(
                                item = item,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }

                composable(NavRoute.Settings.route) {
                    SettingsScreen()
                }
            }
        }
    }
}
