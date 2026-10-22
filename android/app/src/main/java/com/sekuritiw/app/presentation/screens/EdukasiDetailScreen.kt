package com.sekuritiw.app.presentation.screens

import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun EdukasiDetailScreen(
    materialId: String = "1",
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val primaryTextColor = Color(0xFF1D2A44)
    val secondaryTextColor = Color(0xFF475569)
    val navyButtonColor = Color(0xFF1E3A8A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ── Top Navigation Header ───────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Kembali",
                    tint = primaryTextColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Detail Edukasi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (materialId) {
            "3" -> {
                // ── VIDEO YOUTUBE (APK PENIPUAN) ───────────────────────────────────
                Text(
                    text = "Cara Mengenali APK Penipuan di Android",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Graphic Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF07132B))
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.sekuritiw.app.R.drawable.banner_edukasi_3),
                        contentDescription = "Banner APK Penipuan",
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Deskripsi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Dalam video ini, Anda akan belajar langkah-langkah mudah untuk mengenali APK penipuan di Android, termasuk contoh nyata dan cara melindungi diri.",
                    fontSize = 15.sp,
                    color = secondaryTextColor,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Checklist Items
                val checkItems = listOf(
                    "Ciri-ciri APK penipuan yang paling umum",
                    "Contoh kasus nyata penipuan APK",
                    "Cara memeriksa keamanan file APK",
                    "Tips aman sebelum instal aplikasi"
                )

                checkItems.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Checked",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = item,
                            fontSize = 14.sp,
                            color = primaryTextColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Button Tonton di YouTube
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/W98rn22grBs?si=RF1KHINu7xSLB01b"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = navyButtonColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Tonton di YouTube", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
            }

            "2" -> {
                // ── LINK BERBAHAYA ────────────────────────────────────────────────
                Text(
                    text = "Link Berbahaya: Jangan Asal Klik!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Graphic Banner
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF07132B))
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.sekuritiw.app.R.drawable.banner_edukasi_2),
                        contentDescription = "Banner Link Berbahaya",
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Link berbahaya adalah tautan yang mengarahkan Anda ke situs phishing atau malware yang dibuat untuk mencuri data atau merusak perangkat. Penipu menyebarkan link berbahaya untuk mencuri informasi pribadi, akun, OTP, hingga menginstal malware di perangkat Anda.",
                    fontSize = 15.sp,
                    color = secondaryTextColor,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Ciri-ciri link berbahaya", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Alamat situs terlihat aneh, menggunakan URL pendek (bit.ly, tinyurl), atau memiliki karakter yang tidak biasa seperti goOgle, paylpal, atau rnicrosoft.", fontSize = 14.sp, color = secondaryTextColor, lineHeight = 21.sp)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Cara memeriksa link", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Perhatikan domain, pastikan menggunakan HTTPS, cek ejaan kata, dan pastikan tujuan website sesuai sebelum mengaksesnya.", fontSize = 14.sp, color = secondaryTextColor, lineHeight = 21.sp)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Jangan langsung klik!!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Jangan klik link dari pesan mencurigakan. Buka browser, cari website resmi sendiri, dan pastikan Anda mengaksesnya melalui sumber terpercaya.", fontSize = 14.sp, color = secondaryTextColor, lineHeight = 21.sp)
            }

            else -> {
                // ── PESAN PENIPUAN (DEFAULT ID "1") ────────────────────────────────
                Text(
                    text = "Pesan Penipuan: Kenali Sebelum Terjebak",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Graphic Banner
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF07132B))
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.sekuritiw.app.R.drawable.banner_edukasi_1),
                        contentDescription = "Banner Pesan Penipuan",
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pesan penipuan dirancang untuk membuat kita panik atau penasaran agar mau memberikan data pribadi, mengklik tautan, atau menginstal aplikasi berbahaya. Modus ini masih menjadi salah satu cara paling umum digunakan pelaku kejahatan siber.",
                    fontSize = 15.sp,
                    color = secondaryTextColor,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Mengapa Berbahaya?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Pelaku memanfaatkan emosi seperti panik, takut, atau penasaran untuk membuat korban bertindak tanpa berpikir. Akibatnya, korban bisa membuka link berbahaya, menginstal APK, memberikan OTP, hingga mentransfer uang.", fontSize = 14.sp, color = secondaryTextColor, lineHeight = 21.sp)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Contoh Pesan Penipuan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                Spacer(modifier = Modifier.height(6.dp))
                Text("\"Klik di sini agar rekening tidak diblokir.\"", fontSize = 14.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Cara Menghindari", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Tetap tenang, selalu cek pengirim, pastikan informasi lewat kanal resmi, jangan pernah berikan OTP, dan jangan klik link yang tidak jelas.", fontSize = 14.sp, color = secondaryTextColor, lineHeight = 21.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
