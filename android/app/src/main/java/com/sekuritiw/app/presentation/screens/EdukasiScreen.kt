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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekuritiw.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class EdukasiMaterialItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val bannerRes: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EdukasiScreen(
    onMaterialClick: (String) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("Semua") }
    val coroutineScope = rememberCoroutineScope()

    val categories = listOf("Semua", "Pesan", "Link", "APK")

    val materiList = listOf(
        EdukasiMaterialItem(
            id = "1",
            title = "Pesan Penipuan: Kenali Sebelum Terjebak)",
            description = "Pesan penipuan dirancang untuk membuat kita panik atau penasaran agar mau memberikan data pribadi, mengklik tautan, atau menginstal aplikasi berbahaya.",
            category = "Pesan",
            bannerRes = R.drawable.banner_edukasi_1
        ),
        EdukasiMaterialItem(
            id = "2",
            title = "Link Berbahaya: Jangan Asal Klik!",
            description = "Link berbahaya adalah tautan yang mengarahkan Anda ke situs phishing untuk mencuri kredensial dan data keuangan.",
            category = "Link",
            bannerRes = R.drawable.banner_edukasi_2
        ),
        EdukasiMaterialItem(
            id = "3",
            title = "Cara Mengenali APK Penipuan di Android",
            description = "Dalam video ini, Anda akan belajar langkah-langkah mudah untuk mengenali file APK berbahaya (seperti resi/undangan palsu).",
            category = "APK",
            bannerRes = R.drawable.banner_edukasi_3
        )
    )

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { materiList.size })



    val filteredMateri = if (selectedCategory == "Semua") {
        materiList
    } else {
        materiList.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val primaryTextColor = Color(0xFF1D2A44)
    val secondaryTextColor = Color(0xFF64748B)
    val activeNavColor = Color(0xFF1E3A8A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Soft light background
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ── Title Centered ───────────────────────────────────────────────────────
        Text(
            text = "Edukasi",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = primaryTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Interactive Horizontal Pager Carousel dengan Tombol Panah Manual ────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
            ) { page ->
                val currentBanner = materiList[page]
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onMaterialClick(currentBanner.id) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF07132B))
                ) {
                    Image(
                        painter = painterResource(id = currentBanner.bannerRes),
                        contentDescription = currentBanner.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Tombol Panah Kiri (<)
            if (pagerState.currentPage > 0) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Banner Sebelumnya",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Tombol Panah Kanan (>)
            if (pagerState.currentPage < materiList.size - 1) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Banner Selanjutnya",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Interactive Carousel Indicator Dots ─────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(materiList.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 10.dp else 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) activeNavColor
                            else Color(0xFFCBD5E1)
                        )
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Section 1: Kategori ──────────────────────────────────────────────────
        Text(
            text = "Kategori",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = primaryTextColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) activeNavColor else Color(0xFFE2E8F0)
                        )
                        .clickable { selectedCategory = category },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF475569)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Section 2: Materi Edukasi ────────────────────────────────────────────
        Text(
            text = "Materi Edukasi",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = primaryTextColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Materi List with Exact Image Assets & Dividers
        filteredMateri.forEachIndexed { index, materi ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMaterialClick(materi.id) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exact Image Thumbnail
                Card(
                    modifier = Modifier
                        .size(68.dp, 56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36))
                ) {
                    Image(
                        painter = painterResource(id = materi.bannerRes),
                        contentDescription = materi.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Text Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = materi.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = materi.description,
                        fontSize = 13.sp,
                        color = secondaryTextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
            }

            if (index < filteredMateri.size - 1) {
                Divider(
                    color = Color(0xFFE2E8F0),
                    thickness = 1.dp
                )
            }
        }
    }
}
