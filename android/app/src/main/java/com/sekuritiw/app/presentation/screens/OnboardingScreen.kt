package com.sekuritiw.app.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekuritiw.app.R
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val imageRes: Int,
    val title: String,
    val description: String
)

/**
 * Layar Onboarding Intro Sekuritiw (3 Halaman Slide).
 * Menggunakan aset gambar asli resolusi 100% (onboarding_1, onboarding_2, onboarding_3).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val pages = listOf(
        OnboardingPageData(
            imageRes = R.drawable.onboarding_1,
            title = "Lindungi dirimu\ndari penipuan digital",
            description = "Sekuritiw membutuhkan izin Akses Notifikasi untuk mendeteksi pesan dan link berbahaya secara otomatis di perangkat Anda, tanpa mengirim chat pribadi ke server."
        ),
        OnboardingPageData(
            imageRes = R.drawable.onboarding_2,
            title = "Peringatan instan,\nkeputusan aman",
            description = "Dapatkan notifikasi peringatan saat ada indikasi bahaya, lengkap dengan rekomendasi tindakan pencegahan."
        ),
        OnboardingPageData(
            imageRes = R.drawable.onboarding_3,
            title = "Edukasi & literasi\nkeamanan digital",
            description = "Pelajari modus penipuan terkini melalui panduan praktis agar tetap terlindungi dari berbagai ancaman siber."
        )
    )

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    val primaryTextColor = Color(0xFF1D2A44)
    val secondaryTextColor = Color(0xFF64748B)
    val navyButtonColor = Color(0xFF1E3A8A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Light Slate background
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // ── Pager Slide Ilustrasi & Teks ─────────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Ilustrasi Aset Asli (Fit scale)
                Image(
                    painter = painterResource(id = page.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(260.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Judul Slide
                Text(
                    text = page.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Deskripsi Slide
                Text(
                    text = page.description,
                    fontSize = 13.sp,
                    color = secondaryTextColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Indikator Titik (Page Dots Indicator) ────────────────────────────
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(pages.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(if (isSelected) 28.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) navyButtonColor else Color(0xFFCBD5E1)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Tombol Utama (Next / Get Started) ────────────────────────────────
        Button(
            onClick = {
                if (pagerState.currentPage < pages.size - 1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onFinishOnboarding()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = navyButtonColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Tombol Sekunder (Skip Introduction) ──────────────────────────────
        if (pagerState.currentPage < pages.size - 1) {
            Text(
                text = "Skip Introduction",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor,
                modifier = Modifier
                    .clickable { onFinishOnboarding() }
                    .padding(vertical = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(34.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
