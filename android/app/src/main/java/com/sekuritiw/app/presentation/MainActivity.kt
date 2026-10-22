package com.sekuritiw.app.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sekuritiw.app.presentation.navigation.SekuritiwNavGraph
import com.sekuritiw.app.presentation.theme.SekuritiwTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Entry point utama aplikasi AmbatuGuard.
 * Menyiapkan Jetpack Compose, Theme, NavGraph utama, dan mendengarkan "Share to Scan" Intent.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var sharedTextState by mutableStateOf<String?>(null)
    private var sharedApkUriState by mutableStateOf<android.net.Uri?>(null)
    private var targetRecordIdState by mutableStateOf<String?>(null)
    private var navigateToHistoryState by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            SekuritiwTheme {
                SekuritiwNavGraph(
                    sharedText = sharedTextState,
                    sharedApkUri = sharedApkUriState,
                    targetRecordId = targetRecordIdState,
                    navigateToHistory = navigateToHistoryState,
                    onClearSharedApkUri = { sharedApkUriState = null },
                    onClearSharedText = { sharedTextState = null },
                    onClearTargetRecordId = { targetRecordIdState = null },
                    onClearNavigateToHistory = { navigateToHistoryState = false }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        // Cek jika intent berasal dari notifikasi peringatan ancaman
        if (intent.hasExtra("EXTRA_CONTENT_ID")) {
            val contentId = intent.getStringExtra("EXTRA_CONTENT_ID")
            if (!contentId.isNullOrBlank()) {
                targetRecordIdState = contentId
            } else {
                navigateToHistoryState = true
            }
        } else if (intent.getStringExtra("EXTRA_NAVIGATE_TO") == "history") {
            navigateToHistoryState = true
        }
        
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                sharedTextState = text
            }
        } else if (intent.action == Intent.ACTION_VIEW || intent.action == Intent.ACTION_SEND) {
            val uri = intent.data ?: intent.getParcelableExtra(Intent.EXTRA_STREAM) as? android.net.Uri
            if (uri != null) {
                sharedApkUriState = uri
            }
        }
    }
}
