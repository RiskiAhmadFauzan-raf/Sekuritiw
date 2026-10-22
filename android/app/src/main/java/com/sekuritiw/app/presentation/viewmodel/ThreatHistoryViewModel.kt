package com.sekuritiw.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sekuritiw.app.data.local.NotificationRecordDao
import com.sekuritiw.app.data.local.NotificationRecordEntity
import com.sekuritiw.app.presentation.screens.HistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ThreatHistoryViewModel @Inject constructor(
    private val notificationRecordDao: NotificationRecordDao
) : ViewModel() {

    fun insertRecord(
        id: String,
        packageName: String,
        sender: String,
        rawText: String,
        threatCategory: String,
        riskScore: Float,
        riskLevel: String,
        reasoning: String,
        groupName: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val record = NotificationRecordEntity(
                id = id,
                packageName = packageName,
                sender = sender,
                groupName = groupName,
                rawText = rawText,
                normalizedText = rawText.lowercase(),
                threatCategory = threatCategory,
                riskScore = riskScore,
                riskLevel = riskLevel,
                severityRating = if (riskLevel == "DANGEROUS") "HIGH" else if (riskLevel == "CAUTIOUS") "MEDIUM" else "LOW",
                reasoning = reasoning,
                timestamp = System.currentTimeMillis(),
                isSynced = false
            )
            notificationRecordDao.insertRecord(record)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            notificationRecordDao.deleteAllRecords()
        }
    }

    val historyItems: StateFlow<List<HistoryItem>> = notificationRecordDao.getAllRecordsFlow()
        .map { entities ->
            entities.map { entity ->
                entity.toHistoryItem()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Riwayat khusus APK: untuk ScanApkScreen */
    val apkHistoryItems: StateFlow<List<HistoryItem>> = notificationRecordDao.getAllRecordsFlow()
        .map { entities ->
            entities
                .filter { entity ->
                    entity.sender?.contains("Pindai File APK", ignoreCase = true) == true ||
                    entity.threatCategory == "APK_SCAM"
                }
                .map { it.toHistoryItem() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Riwayat khusus Link: untuk CheckLinkScreen (hanya scan manual Cek Link) */
    val linkHistoryItems: StateFlow<List<HistoryItem>> = notificationRecordDao.getAllRecordsFlow()
        .map { entities ->
            entities
                .filter { entity ->
                    entity.sender == "Sistem Cek Link"
                }
                .map { it.toHistoryItem() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun NotificationRecordEntity.toHistoryItem(): HistoryItem {
        val timeFormatted = try {
            val sdf = SimpleDateFormat("HH:mm, dd MMM", Locale("id", "ID"))
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Baru saja"
        }

        return HistoryItem(
            id = id,
            packageName = packageName,
            sender = sender ?: packageName,
            groupName = groupName,
            category = threatCategory,
            riskScore = riskScore.toInt(),
            riskLevel = riskLevel,
            snippet = rawText,
            timeAgo = timeFormatted,
            reasoning = reasoning
        )
    }

    /** Mengambil satu item riwayat berdasarkan ID (digunakan saat notifikasi ancaman diklik) */
    fun getHistoryItemById(id: String, onResult: (HistoryItem?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = notificationRecordDao.getRecordById(id)
            val historyItem = entity?.toHistoryItem()
            withContext(Dispatchers.Main) {
                onResult(historyItem)
            }
        }
    }
}
