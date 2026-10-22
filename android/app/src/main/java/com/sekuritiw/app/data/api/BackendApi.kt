package com.sekuritiw.app.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Request payload untuk feedback salah deteksi sesuai Spesifikasi Privasi SEKURITIW (UU PDP).
 * Memuat data yang telah disamarkan di perangkat klien sebelum dikirim.
 */
data class ThreatFeedbackRequest(
    @SerializedName("device_record_id")
    val deviceRecordId: String? = null,

    @SerializedName("text_redacted")
    val textRedacted: String,

    @SerializedName("label_original")
    val labelOriginal: String? = null,

    @SerializedName("label_user")
    val labelUser: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("app_package")
    val appPackage: String? = null,

    @SerializedName("sender_type")
    val senderType: String, // "PHONE", "ALPHANUMERIC", "OTHER"

    @SerializedName("sender_display")
    val senderDisplay: String,

    @SerializedName("user_note_redacted")
    val userNoteRedacted: String? = null,

    @SerializedName("consent_version")
    val consentVersion: String = "v1",

    @SerializedName("created_at")
    val createdAt: String? = null,

    // Aliases untuk kompatibilitas skema controller lama
    @SerializedName("feedback_type")
    val feedbackType: String = if (labelUser == "SAFE") "FALSE_POSITIVE" else "FALSE_NEGATIVE",

    @SerializedName("user_reported_label")
    val userReportedLabel: String = labelUser,

    @SerializedName("reason_category")
    val reasonCategory: String = category,

    @SerializedName("user_notes")
    val userNotes: String? = userNoteRedacted,

    @SerializedName("snippet")
    val snippet: String = textRedacted,

    @SerializedName("sender")
    val sender: String = senderDisplay,

    @SerializedName("package_name")
    val packageName: String? = appPackage,

    @SerializedName("original_risk_level")
    val originalRiskLevel: String? = labelOriginal,

    @SerializedName("original_category")
    val originalCategory: String? = null
)

/** Response model dari backend Laravel */
data class ThreatFeedbackResponse(
    val status: String,
    val message: String
)

/** API Service untuk berkomunikasi dengan Backend Laravel SEKURITIW */
interface BackendApi {
    @POST("threats/feedback")
    suspend fun submitFeedback(@Body request: ThreatFeedbackRequest): Response<ThreatFeedbackResponse>
}
