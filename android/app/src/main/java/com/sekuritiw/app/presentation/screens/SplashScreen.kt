package com.sekuritiw.app.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekuritiw.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Layar Splash Screen awal aplikasi Sekuritiw.
 * Menampilkan Logo Sekuritiw + Teks "Sekuritiw" dan 3 titik animasi pulsing dots di bawah.
 */
@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000) // Tampil 2 detik
        onTimeout()
    }

    val primaryTextColor = Color(0xFF1D2A44)
    val dotColor = Color(0xFF1E3A8A)

    // Animasi pulsing untuk 3 titik loading di bawah
    val dot1Alpha = remember { Animatable(0.3f) }
    val dot2Alpha = remember { Animatable(0.3f) }
    val dot3Alpha = remember { Animatable(0.3f) }

    LaunchedEffect(Unit) {
        val tweenSpec = infiniteRepeatable<Float>(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
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
            .background(Color(0xFFF8FAFC)), // Light Slate background
        contentAlignment = Alignment.Center
    ) {
        // ── Logo & Teks Utama Rata Tengah ─────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Logo Sekuritiw",
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Sekuritiw",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
            )
        }

        // ── 3 Loading Dots di Bagian Bawah ────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp),
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
                    .size(12.dp)
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
    }
}
