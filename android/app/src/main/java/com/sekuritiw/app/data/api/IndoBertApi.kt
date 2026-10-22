package com.sekuritiw.app.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class AnalysisContext(
    @SerializedName("has_url") val hasUrl: Boolean = false,
    @SerializedName("has_apk") val hasApk: Boolean = false,
    @SerializedName("device_risk_score") val deviceRiskScore: Float? = null,
    @SerializedName("device_confidence") val deviceConfidence: Float? = null,
    @SerializedName("package_name") val packageName: String? = null
)

data class PredictRequest(
    @SerializedName("text") val text: String,
    @SerializedName("context") val context: AnalysisContext? = null
)

data class PredictResponse(
    @SerializedName("category")
    val category: String? = null,

    @SerializedName("confidence")
    val confidence: Double = 0.0,

    @SerializedName("probabilities")
    val probabilities: Map<String, Double>? = null,

    @SerializedName("risk_score")
    val riskScore: Double? = null,

    @SerializedName("model_version")
    val modelVersion: String? = null,

    @SerializedName("inference_source")
    val inferenceSource: String? = null,

    @SerializedName("processing_time_ms")
    val processingTimeMs: Long? = null,

    // Fallback fields jika berkomunikasi dengan backend legacy
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("prediction")
    val prediction: String? = null,

    @SerializedName("class_id")
    val classId: Int? = null
) {
    /**
     * Kategori ancaman terpadu, mendukung skema FastAPI baru ("category") maupun legacy ("prediction").
     */
    val effectiveCategory: String
        get() {
            if (classId == 0) return "BENIGN"
            val raw = category ?: prediction ?: "BENIGN"
            val lower = raw.lowercase()
            return when {
                lower == "aman" || lower == "benign" || lower == "normal" || lower.contains("promo") -> "BENIGN"
                lower.contains("phishing") -> "PHISHING"
                lower.contains("penipuan") || lower.contains("scam") || lower.contains("transfer") -> "FINANCIAL_SCAM"
                else -> raw
            }
        }
}

interface IndoBertApi {
    @POST("api/v1/predict")
    suspend fun analyzeText(@Body request: PredictRequest): Response<PredictResponse>
}

