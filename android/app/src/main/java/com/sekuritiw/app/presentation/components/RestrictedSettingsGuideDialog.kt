package com.sekuritiw.app.presentation.components

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Cek apakah "Setelan Terbatas" (Restricted Settings) sudah diizinkan di level OS.
 */
fun isRestrictedSettingsAllowed(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return true
    }
    return try {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
        val mode = appOps?.unsafeCheckOpNoThrow(
            "android:access_restricted_settings",
            Process.myUid(),
            context.packageName
        )
        mode == AppOpsManager.MODE_ALLOWED
    } catch (_: Exception) {
        false
    }
}

/**
 * Instruksi singkat 1 kalimat sesuai merek HP.
 */
fun getShortDeviceInstruction(): Pair<String, String> {
    val manufacturer = Build.MANUFACTURER.lowercase()
    val brand = Build.BRAND.lowercase()

    return when {
        manufacturer.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco") -> {
            "Xiaomi / Redmi / POCO" to "Gulir ke paling bawah → ketuk \"Izinkan setelan terbatas\"."
        }
        manufacturer.contains("samsung") -> {
            "Samsung Galaxy" to "Ketuk titik 3 (⋮) di pojok kanan atas → \"Izinkan setelan terbatas\"."
        }
        manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus") -> {
            "OPPO / Realme / OnePlus" to "Ketuk titik 3 (⋮) di kanan atas → \"Izinkan setelan terbatas\"."
        }
        manufacturer.contains("vivo") || brand.contains("iqoo") -> {
            "Vivo / iQOO" to "Ketuk titik 3 (⋮) di kanan atas → \"Izinkan setelan terbatas\"."
        }
        manufacturer.contains("infinix") || manufacturer.contains("tecno") || manufacturer.contains("itel") -> {
            "Infinix / Tecno / itel" to "Ketuk titik 3 (⋮) atau gulir ke bawah → \"Izinkan setelan terbatas\"."
        }
        else -> {
            val name = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
            name to "Ketuk titik 3 (⋮) atau gulir ke bawah → \"Izinkan setelan terbatas\"."
        }
    }
}

/**
 * Dialog panduan aktivasi 2 langkah ringkas dengan verifikasi status riil.
 */
@Composable
fun RestrictedSettingsGuideDialog(
    isListenerEnabled: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val (brandName, shortStep1Guide) = remember { getShortDeviceInstruction() }

    // Status riil dari sistem apakah setelan terbatas sudah diizinkan
    var isStep1Allowed by remember {
        mutableStateOf(isRestrictedSettingsAllowed(context))
    }

    // Perbarui status otomatis saat pengguna kembali dari pengaturan (ON_RESUME)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isStep1Allowed = isRestrictedSettingsAllowed(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Langkah 1 dianggap selesai HANYA jika terverifikasi oleh OS atau listener sudah aktif
    val isStep1Done = isListenerEnabled || isStep1Allowed
    val isStep2Done = isListenerEnabled

    val primaryNavy = Color(0xFF1E3A8A)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF475569)
    val doneGreen = Color(0xFF15803D)
    val doneGreenBg = Color(0xFFDCFCE7)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Header: Judul Ringkas + Badge Merek HP ────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isStep2Done) doneGreenBg else Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isStep2Done) Icons.Default.CheckCircle else Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = if (isStep2Done) doneGreen else primaryNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isStep2Done) "Akses Notifikasi Aktif!" else "Aktivasi Akses Notifikasi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "Terdeteksi: $brandName",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── LANGKAH 1 CARD ────────────────────────────────────────────────
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (isStep1Done) Color(0xFF86EFAC) else Color(0xFFCBD5E1),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isStep1Done) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Badge Indikator Langkah 1
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isStep1Done) doneGreen else Color(0xFF1D4ED8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isStep1Done) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "1",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Buka Setelan Terbatas",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                            }

                            if (isStep1Done) {
                                Text(
                                    text = "Selesai",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = doneGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Instruksi super singkat
                        Text(
                            text = shortStep1Guide,
                            fontSize = 12.sp,
                            color = textSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                openAppDetailsSettings(context)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isStep1Done) Color(0xFF334155) else primaryNavy
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isStep1Done) "Buka Ulang Info Aplikasi" else "1. Buka Info Aplikasi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ── LANGKAH 2 CARD ────────────────────────────────────────────────
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (isStep2Done) Color(0xFF86EFAC) else if (isStep1Done) Color(0xFF93C5FD) else Color(0xFFCBD5E1),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isStep2Done) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Badge Indikator Langkah 2
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isStep2Done) doneGreen else Color(0xFF047857)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isStep2Done) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "2",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Nyalakan Akses Notifikasi",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                            }

                            if (isStep2Done) {
                                Text(
                                    text = "Selesai",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = doneGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Instruksi super singkat
                        Text(
                            text = "Aktifkan saklar untuk Sekuritiw.",
                            fontSize = 12.sp,
                            color = textSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                openNotificationListenerSettings(context)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isStep2Done) doneGreen else Color(0xFF047857)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Icon(
                                imageVector = if (isStep2Done) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isStep2Done) "✓ Sudah Aktif" else "2. Buka Akses Notifikasi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── Tombol Bawah (Selesai / Tutup) ─────────────────────────────────
                if (isStep2Done) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = doneGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "Selesai & Mulai Perlindungan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text(
                            text = "Tutup",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = textSecondary
                        )
                    }
                }
            }
        }
    }
}

/** Membuka halaman Info Aplikasi spesifik untuk package Sekuritiw */
fun openAppDetailsSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val fallbackIntent = Intent(Settings.ACTION_APPLICATION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallbackIntent)
    }
}

/** Membuka halaman izin Akses Notifikasi sistem */
fun openNotificationListenerSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
