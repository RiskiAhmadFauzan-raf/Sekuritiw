package com.sekuritiw.app.presentation.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Halaman Pengaturan (Settings Screen).
 * Dibuat presisi mengikuti UI Mockup terbaru.
 */
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var isNotificationAccessGranted by remember {
        mutableStateOf(isNotificationServiceEnabled(context))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isNotificationAccessGranted = isNotificationServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var periksaOtomatisEnabled by remember { mutableStateOf(true) }
    var showGuideDialog by remember { mutableStateOf(false) }

    if (showGuideDialog) {
        com.sekuritiw.app.presentation.components.RestrictedSettingsGuideDialog(
            isListenerEnabled = isNotificationAccessGranted,
            onDismiss = { showGuideDialog = false }
        )
    }

    val primaryTextColor = Color(0xFF1D2A44)
    val secondaryTextColor = Color(0xFF475569)
    val navyButtonColor = Color(0xFF1E3A8A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Soft light slate background
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ── Judul Utama (Centered Title) ──────────────────────────────────────────
        Text(
            text = "Pengaturan",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = primaryTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Sub-header: Perlindungan ─────────────────────────────────────────────
        Text(
            text = "Perlindungan",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = primaryTextColor
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ── Kartu Status Akses Notifikasi (Red/Green Bordered Card) ───────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isNotificationAccessGranted) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isNotificationAccessGranted) Color(0xFFECFDF5) else Color(0xFFFEE2E2)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header status row (Status Icon + Title + Status Dot Indicator)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isNotificationAccessGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (isNotificationAccessGranted) Color(0xFF047857) else Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isNotificationAccessGranted)
                                "Izin diberikan - Proteksi aktif"
                            else
                                "Izin belum diberikan - Proteksi tidak aktif",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNotificationAccessGranted) Color(0xFF047857) else Color(0xFFDC2626)
                        )
                    }

                    // Dot Indicator
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (isNotificationAccessGranted) Color(0xFF10B981)
                                else Color(0xFFEF4444)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Deskripsi Izin
                Text(
                    text = if (isNotificationAccessGranted)
                        "Akses Notifikasi aktif. Sekuritiw siap memindai setiap pesan masuk secara otomatis di latar belakang."
                    else
                        "Sekuritiw memerlukan izin Akses Notifikasi untuk memindai pesan masuk secara otomatis. Tidak ada isi notifikasi yang dikirimkan ke server tanpa persetujuan Anda.",
                    fontSize = 11.sp,
                    color = secondaryTextColor,
                    lineHeight = 16.sp
                )

                if (!isNotificationAccessGranted) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                showGuideDialog = true
                            } else {
                                com.sekuritiw.app.presentation.components.openNotificationListenerSettings(context)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = navyButtonColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Buka Pengaturan Sistem",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Row Item: Periksa Otomatis ───────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Periksa Otomatis",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Analisis setiap notifikasi pesan masuk secara otomatis tanpa perlu interaksi manual",
                    fontSize = 11.sp,
                    color = secondaryTextColor,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Switch(
                checked = periksaOtomatisEnabled,
                onCheckedChange = { periksaOtomatisEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = navyButtonColor,
                    uncheckedThumbColor = Color(0xFF94A3B8),
                    uncheckedTrackColor = Color(0xFFE2E8F0)
                )
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

private fun isNotificationServiceEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )
    return flat != null && flat.contains(pkgName)
}
