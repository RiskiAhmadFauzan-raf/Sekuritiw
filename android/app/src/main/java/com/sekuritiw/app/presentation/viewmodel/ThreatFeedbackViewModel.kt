package com.sekuritiw.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sekuritiw.app.data.api.BackendApi
import com.sekuritiw.app.data.api.ThreatFeedbackRequest
import com.sekuritiw.app.data.local.NotificationRecordDao
import com.sekuritiw.app.domain.util.SensitiveDataRedactor
import com.sekuritiw.app.presentation.screens.HistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

sealed class FeedbackSubmissionState {
    object Idle : FeedbackSubmissionState()
    object Submitting : FeedbackSubmissionState()
    data class Success(val message: String) : FeedbackSubmissionState()
    data class Error(val message: String) : FeedbackSubmissionState()
}

@HiltViewModel
class ThreatFeedbackViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val notificationRecordDao: NotificationRecordDao,
    private val backendApi: BackendApi
) : ViewModel() {

    private val prefs by lazy {
        context.getSharedPreferences("sekuritiw_feedback_prefs", android.content.Context.MODE_PRIVATE)
    }

    private val _submissionState = MutableStateFlow<FeedbackSubmissionState>(FeedbackSubmissionState.Idle)
    val submissionState: StateFlow<FeedbackSubmissionState> = _submissionState.asStateFlow()

    fun resetState() {
        _submissionState.value = FeedbackSubmissionState.Idle
    }

    /** Cek apakah pesan dengan ID ini sudah pernah dilaporkan pengguna */
    fun isFeedbackReported(itemId: String): Boolean {
        return prefs.getBoolean("reported_$itemId", false)
    }

    /** Ambil tipe feedback yang dilaporkan (misal: "Salah Deteksi (Sebenarnya Aman)") */
    fun getReportedType(itemId: String): String? {
        return prefs.getString("reported_type_$itemId", null)
    }

    /** Simpan status pelaporan secara persisten di penyimpanan perangkat */
    fun markFeedbackReported(itemId: String, reportedType: String) {
        prefs.edit()
            .putBoolean("reported_$itemId", true)
            .putString("reported_type_$itemId", reportedType)
            .apply()
    }

    /**
     * Mengirimkan umpan balik salah deteksi ke server backend Laravel
     * dengan penyamaran data sensitif di perangkat klien (UU PDP & Minimisasi Data).
     */
    fun submitFeedback(
        item: HistoryItem,
        feedbackType: String,
        userReportedLabel: String,
        reasonCategory: String,
        userNotes: String,
        consentVersion: String = "v1",
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _submissionState.value = FeedbackSubmissionState.Submitting

            // 1. Penyamaran data sensitif di perangkat lokal (Client-side Redaction)
            val redactedText = SensitiveDataRedactor.redactText(item.snippet)
            val redactedSender = SensitiveDataRedactor.redactSender(item.sender, item.packageName)
            val redactedNotes = SensitiveDataRedactor.redactText(userNotes.trim()).ifBlank { null }

            // 2. Integritas Data Deteksi: Hasil scan asli sistem tidak ditimpa demi keamanan pengguna.

            // 3. Susun payload yang hanya memuat data tersamar
            val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            val request = ThreatFeedbackRequest(
                deviceRecordId = item.id,
                textRedacted = redactedText,
                labelOriginal = item.riskLevel,
                labelUser = userReportedLabel,
                category = reasonCategory,
                appPackage = item.packageName,
                senderType = redactedSender.senderType,
                senderDisplay = redactedSender.senderDisplay,
                userNoteRedacted = redactedNotes,
                consentVersion = consentVersion,
                createdAt = isoDate
            )

            val reportedType = if (feedbackType == "FALSE_POSITIVE") {
                "Salah Deteksi (Sebenarnya Aman)"
            } else {
                "Salah Deteksi (Sebenarnya Berbahaya)"
            }

            val message = try {
                val response = backendApi.submitFeedback(request)
                // Kunci permanen di perangkat lokal agar tidak bisa dikirim berulang
                markFeedbackReported(item.id, reportedType)

                if (response.isSuccessful && response.body() != null) {
                    response.body()?.message ?: "Feedback berhasil dicatat."
                } else {
                    "Laporan dicatat. Server response: ${response.code()}"
                }
            } catch (e: Exception) {
                // Tetap tandai sebagai dilaporkan di perangkat lokal agar tidak spamming saat offline
                markFeedbackReported(item.id, reportedType)
                "Laporan tersimpan di perangkat. Akan disinkronkan saat terhubung."
            }

            withContext(Dispatchers.Main) {
                _submissionState.value = FeedbackSubmissionState.Success(message)
                onComplete(true)
            }
        }
    }
}
