package com.sekuritiw.app.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sekuritiw.app.data.local.NotificationRecordDao
import com.sekuritiw.app.data.local.NotificationRecordEntity
import com.sekuritiw.app.presentation.MainActivity
import com.sekuritiw.app.domain.model.NotificationContent
import com.sekuritiw.app.domain.model.RiskLevel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Layanan inti SEKURITIW yang mendengarkan semua notifikasi masuk di perangkat Android.
 * Mengimplementasikan [NotificationListenerService] dan berjalan sebagai Foreground Service
 * untuk keandalan maksimal di latar belakang.
 *
 * Requirements: 1.1, 1.3, 1.4, 1.5, 1.6, 10.4, 20.2
 */
@AndroidEntryPoint
class SekuritiwNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "SekuritiwListener"

        // Foreground service notification
        private const val FOREGROUND_CHANNEL_ID = "sekuritiw_foreground_channel"
        private const val FOREGROUND_NOTIFICATION_ID = 1001

        // High priority alert notification channel
        private const val ALERT_CHANNEL_ID = "sekuritiw_alert_channel"

        private const val MAX_CONSECUTIVE_FAILURES = 3

        const val ACTION_START_FOREGROUND = "com.sekuritiw.app.START_FOREGROUND"
        const val ACTION_STOP_FOREGROUND = "com.sekuritiw.app.STOP_FOREGROUND"
    }

    @Inject
    lateinit var notificationDao: NotificationRecordDao

    @Inject
    lateinit var indoBertApi: com.sekuritiw.app.data.api.IndoBertApi

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val extractor = NotificationExtractor()
    private val contentAnalyzer by lazy { com.sekuritiw.app.data.analyzer.ContentAnalyzer(this, indoBertApi) }
    private val apkClassifier by lazy { com.sekuritiw.app.data.detector.APKMalwareClassifier(this) }
    private var consecutiveFailures = 0

    // Cache untuk mencegah penganalisisan ulang pesan yang sama akibat update notifikasi
    private val processedSignaturesWithTime = LinkedHashMap<String, Long>(100, 0.75f, true)

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "SekuritiwNotificationListener dibuat (onCreate). Service berjalan di latar belakang.")
        createNotificationChannels()
        startForeground(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification())
        // Folder observer for APK has been removed because APKs are now scanned via Intent directly.
    }

    private fun startApkFolderObserver() {
        try {
            val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir.exists()) {
                observePath(downloadDir.absolutePath)
            }
            val whatsappDocDir = java.io.File(android.os.Environment.getExternalStorageDirectory(), "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents")
            if (whatsappDocDir.exists()) {
                observePath(whatsappDocDir.absolutePath)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memulai observer folder APK: ${e.message}")
        }
    }

    private fun observePath(path: String) {
        val mask = android.os.FileObserver.CLOSE_WRITE or android.os.FileObserver.CREATE
        val observer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : android.os.FileObserver(java.io.File(path), mask) {
                override fun onEvent(event: Int, fileName: String?) {
                    checkAndScanApk(path, fileName)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            object : android.os.FileObserver(path, mask) {
                override fun onEvent(event: Int, fileName: String?) {
                    checkAndScanApk(path, fileName)
                }
            }
        }
        observer.startWatching()
    }

    private fun checkAndScanApk(path: String, fileName: String?) {
        if (fileName != null && fileName.lowercase().endsWith(".apk")) {
            val file = java.io.File(path, fileName)
            if (file.exists() && file.length() > 0) {
                Log.i(TAG, "🔍 File APK baru terdeteksi secara otomatis: ${file.name}")
                try {
                    val result = apkClassifier.classifyAPK(file.absolutePath)
                    if (result.score >= 30.0f || result.detectedDangerousPermissions.isNotEmpty()) {
                        showThreatAlertNotification(
                            "⚠️ BERKAS APK BERBAHAYA TERDETEKSI!",
                            "File '${file.name}' baru saja terunduh dan terdeteksi mengandung ancaman (${result.score.toInt()}%).",
                            (System.currentTimeMillis() % 10000).toInt()
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal memindai APK terunduh: ${e.message}")
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_FOREGROUND -> {
                Log.i(TAG, "Menerima perintah STOP, menghentikan foreground service.")
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                Log.i(TAG, "SekuritiwNotificationListener dimulai (onStartCommand).")
                startForeground(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification())
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "SekuritiwNotificationListener dihentikan (onDestroy). Service akan dimulai ulang oleh sistem.")
        serviceScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (extractor.isSystemNotification(sbn)) return
        if (sbn.packageName == packageName) return

        // 1. Abaikan notifikasi ongoing (seperti progress bar download, musik, atau pencadangan WA yang tidak bisa di-swipe)
        if (sbn.isOngoing) {
            Log.d(TAG, "Mengabaikan notifikasi ongoing (seperti backup/progress) dari ${sbn.packageName}")
            return
        }

        // 2. Abaikan notifikasi ringkasan grup (seperti "15 pesan baru") agar tidak menumpuk di histori
        if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
            Log.d(TAG, "Mengabaikan notifikasi ringkasan grup (FLAG_GROUP_SUMMARY) dari ${sbn.packageName}")
            return
        }

        // 3. Tambahan filter kata kunci untuk pencadangan, transfer media, mengirim berkas, dll.
        val extras = sbn.notification.extras
        val titleStr = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.lowercase() ?: ""
        val textStr = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.lowercase() ?: ""
        if (titleStr.contains("pencadangan") || titleStr.contains("backup") || titleStr.contains("mengunggah") || titleStr.contains("upload") || titleStr.contains("mengirim") || titleStr.contains("sending")) {
            return
        }
        if (textStr.contains("pencadangan") || textStr.contains("backup") || textStr.contains("mengunggah") || textStr.contains("upload") || textStr.contains("mengirim") || textStr.contains("sending")) {
            return
        }

        // Hanya analisis notifikasi yang berasal dari aplikasi perpesanan atau email
        if (!extractor.isMessagingApp(sbn.packageName)) {
            Log.v(TAG, "Mengabaikan notifikasi dari ${sbn.packageName} karena bukan aplikasi pesan/email.")
            return
        }

        val content = extractor.extract(sbn) ?: run {
            Log.w(TAG, "Gagal mengekstrak konten dari notifikasi: ${sbn.packageName}")
            handleAnalysisFailure()
            return
        }

        if (!content.hasAnalyzableContent()) return

        // Mencegah analisis berulang: Abaikan jika pengirim & isi pesan ini sudah pernah diproses dalam 10 detik terakhir
        if (isDuplicate(sbn.packageName, content.sender ?: "", content.text ?: "")) {
            Log.d(TAG, "Mengabaikan notifikasi duplikat ter-update dari ${sbn.packageName}")
            return
        }

        consecutiveFailures = 0

        Log.i(TAG, "📬 Notifikasi latar belakang diterima dari ${content.packageName}")
        analyzeNotificationAsync(content)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.v(TAG, "Notifikasi dihapus dari: ${sbn.packageName}")
    }

    /**
     * Mengecek apakah notifikasi ini duplikat (sudah diproses baru-baru ini).
     * Mencegah double analysis saat aplikasi chat mengupdate notifikasi yang sama.
     */
    private fun isDuplicate(packageName: String, sender: String, text: String): Boolean {
        val signature = "$packageName:$sender:$text"
        val now = System.currentTimeMillis()

        val lastTime = processedSignaturesWithTime[signature]
        if (lastTime != null && (now - lastTime) < 10000) {
            return true
        }

        processedSignaturesWithTime[signature] = now

        // Batasi ukuran cache
        if (processedSignaturesWithTime.size > 150) {
            val it = processedSignaturesWithTime.keys.iterator()
            if (it.hasNext()) {
                it.next()
                it.remove()
            }
        }

        return false
    }

    private fun analyzeNotificationAsync(content: NotificationContent) {
        serviceScope.launch {
            try {
                // 1. Eksekusi analisis lengkap
                val result = contentAnalyzer.analyze(content)

                Log.i(TAG, "✅ Analisis Selesai (${result.processingTimeMs} ms): Category=${result.threatCategory}, Score=${result.riskScore}")

                // 2. Simpan ke database terenkripsi SQLCipher (Task 19)
                if (::notificationDao.isInitialized) {
                    val record = NotificationRecordEntity(
                        id = result.contentId,
                        packageName = result.packageName,
                        sender = content.sender,
                        groupName = content.groupName,
                        rawText = content.getFullText(),
                        normalizedText = result.preprocessedText.normalized,
                        threatCategory = result.threatCategory,
                        riskScore = result.riskScore,
                        riskLevel = result.riskLevel,
                        severityRating = result.classificationResult?.severityRating?.name ?: "LOW",
                        reasoning = result.reasoning,
                        timestamp = content.timestamp,
                        isSynced = false
                    )
                    notificationDao.insertRecord(record)
                    Log.d(TAG, "💾 Berhasil menyimpan riwayat ancaman ke database terenkripsi.")
                }

                // 3. Jika terdeteksi ancaman (CAUTIOUS / DANGEROUS), tampilkan Peringatan Sistem (Task 26)
                if (result.riskLevel == RiskLevel.DANGEROUS.name || result.riskLevel == RiskLevel.CAUTIOUS.name) {
                    val categoryName = when (result.threatCategory) {
                        "FINANCIAL_SCAM" -> "Penipuan Finansial"
                        "PHISHING" -> "Link Phishing"
                        "APK_SCAM" -> "APK Berbahaya"
                        "SOCIAL_ENGINEERING" -> "Rekayasa Sosial"
                        "SPAM" -> "Pesan Spam"
                        else -> "Ancaman Mencurigakan"
                    }
                    showThreatAlertNotification(
                        title = if (result.riskLevel == RiskLevel.DANGEROUS.name) "Sekuritiw: Ancaman Berbahaya!" else "Sekuritiw: Peringatan Waspada",
                        message = "[$categoryName] ${result.reasoning}",
                        notificationId = (System.currentTimeMillis() % 10000).toInt(),
                        contentId = result.contentId
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saat menganalisis notifikasi dari ${content.packageName}: ${e.message}", e)
                handleAnalysisFailure()
            }
        }
    }

    private fun handleAnalysisFailure() {
        consecutiveFailures++
        Log.w(TAG, "Jumlah kegagalan berturut-turut: $consecutiveFailures / $MAX_CONSECUTIVE_FAILURES")
        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
            Log.e(TAG, "Kegagalan kritis terdeteksi setelah $MAX_CONSECUTIVE_FAILURES kegagalan berturut-turut!")
            consecutiveFailures = 0
        }
    }

    /**
     * Membuat NotificationChannel untuk Foreground Service dan High-Priority Threat Alert.
     */
    private fun createNotificationChannels() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // 1. Channel Foreground Service (Low Priority, Tanpa Suara)
        val fgChannel = NotificationChannel(
            FOREGROUND_CHANNEL_ID,
            "Sekuritiw - Perlindungan Aktif",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Menandakan Sekuritiw sedang aktif memantau notifikasi di latar belakang."
            setShowBadge(false)
        }
        manager.createNotificationChannel(fgChannel)

        // 2. Channel Alert Ancaman Berbahaya (High Priority, Suara & Getar)
        val alertChannel = NotificationChannel(
            ALERT_CHANNEL_ID,
            "Sekuritiw - Peringatan Ancaman",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi peringatan jika terdeteksi ancaman phishing, APK scam, atau penipuan."
            enableVibration(true)
            setShowBadge(true)
        }
        manager.createNotificationChannel(alertChannel)

        Log.d(TAG, "Notification Channels berhasil dibuat.")
    }

    /**
     * Memunculkan notifikasi peringatan berprioritas tinggi jika terdeteksi ancaman di latar belakang.
     * Mengarahkan langsung ke detail pesan ancaman saat notifikasi dipencet.
     */
    private fun showThreatAlertNotification(
        title: String,
        message: String,
        notificationId: Int,
        contentId: String? = null
    ) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_NAVIGATE_TO", "detail")
            if (!contentId.isNullOrBlank()) {
                putExtra("EXTRA_CONTENT_ID", contentId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId, notification)
    }

    /**
     * Membangun notifikasi persisten yang ditampilkan saat foreground service aktif.
     */
    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setContentTitle("Sekuritiw Aktif")
            .setContentText("Memantau pesan & unduhan berkas secara otomatis di latar belakang.")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}
