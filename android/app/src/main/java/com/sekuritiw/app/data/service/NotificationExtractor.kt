package com.sekuritiw.app.data.service

import android.app.Notification
import android.service.notification.StatusBarNotification
import android.util.Log
import com.sekuritiw.app.domain.model.NotificationContent
import java.util.UUID

/**
 * Helper class untuk mengekstrak konten dari StatusBarNotification Android.
 * Menangani berbagai format notifikasi termasuk MessagingStyle, BigTextStyle,
 * dan format notifikasi standar.
 *
 * Requirement: 1.1 - Capture notification content within 500 milliseconds
 * Requirement: 1.6 - Extract sender information, message body, and timestamp
 */
class NotificationExtractor {

    companion object {
        private const val TAG = "NotificationExtractor"

        /**
         * Daftar package aplikasi sistem yang perlu difilter agar tidak
         * menghabiskan sumber daya untuk analisis yang tidak perlu.
         *
         * Requirement: Task 9.1 - Filter out system notifications to reduce processing load
         */
        private val SYSTEM_PACKAGES = setOf(
            "android",
            "com.android.systemui",
            "com.android.settings",
            "com.android.phone",
            "com.google.android.gsf",
            "com.google.android.gms",
            "com.android.providers.downloads",
            "com.android.packageinstaller"
        )

        /**
         * Daftar package aplikasi pesan dan email yang diprioritaskan untuk dianalisis.
         * Requirement: 1.3 - Monitor secara berkelanjutan
         */
        private val MESSAGING_PACKAGES = setOf(
            "com.whatsapp",
            "com.whatsapp.w4b",
            "org.telegram.messenger",
            "com.instagram.android",
            "com.facebook.orca",
            "com.google.android.apps.messaging",
            "com.android.mms",
            "com.samsung.android.messaging",
            "jp.naver.line.android",               // LINE
            "com.google.android.gm",               // Gmail
            "com.yahoo.mobile.client.android.mail" // Yahoo Mail
        )
    }

    /**
     * Mengecek apakah notifikasi berasal dari paket sistem yang harus dilewati.
     * Requirement: Task 9.1 - Filter system notifications
     */
    fun isSystemNotification(sbn: StatusBarNotification): Boolean {
        return sbn.packageName in SYSTEM_PACKAGES
    }

    /**
     * Mengecek apakah notifikasi berasal dari aplikasi perpesanan.
     */
    fun isMessagingApp(packageName: String): Boolean {
        return packageName in MESSAGING_PACKAGES
    }

    /**
     * Mengekstrak seluruh konten relevan dari StatusBarNotification menjadi
     * objek NotificationContent yang terstruktur.
     *
     * Requirement: 1.6 - Extract sender information, message body, and timestamp
     * Requirement: 1.7 - Log error dan lanjutkan jika gagal mengakses konten
     *
     * @param sbn StatusBarNotification dari sistem Android
     * @return NotificationContent atau null jika ekstraksi gagal
     */
    fun extract(sbn: StatusBarNotification): NotificationContent? {
        return try {
            val notification = sbn.notification
            val extras = notification.extras

            // Ekstrak semua field teks dari Notification.extras bundle
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

            val isMessaging = isMessagingApp(sbn.packageName)

            // Ekstrak informasi grup (WhatsApp, Telegram, dll.)
            var isGroup = false
            var groupName: String? = null

            val messagingStyle = androidx.core.app.NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
            if (messagingStyle != null) {
                isGroup = messagingStyle.isGroupConversation
                val convTitle = messagingStyle.conversationTitle?.toString()
                if (!convTitle.isNullOrBlank()) {
                    groupName = convTitle
                }
            }

            // Fallback ekstraksi grup dari extras standar
            if (groupName.isNullOrBlank()) {
                val convTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
                if (!convTitle.isNullOrBlank()) {
                    groupName = convTitle
                    isGroup = true
                }
            }

            if (!isGroup) {
                isGroup = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false)
            }

            // Coba ekstrak sender dari MessagingStyle (WhatsApp, Telegram, dll.)
            val sender = extractSender(sbn, extras)

            // Jika terindikasi grup tapi groupName belum terisi, gunakan title jika title berbeda dari sender
            if (isGroup && groupName.isNullOrBlank()) {
                if (!title.isNullOrBlank() && title != sender) {
                    groupName = title
                }
            }

            // Fallback heuristik untuk aplikasi perpesanan:
            // Jika title ada, sender ada, dan title != sender, hampir pasti title adalah nama grup
            if (!isGroup && isMessaging && !title.isNullOrBlank() && !sender.isNullOrBlank() && title != sender) {
                groupName = title
                isGroup = true
            }

            // Coba ambil pesan asli terakhir dari MessagingStyle (WhatsApp/Telegram) 
            // guna menghindari teks ringkasan grup seperti "3 pesan baru"
            var actualText = text
            try {
                val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
                if (messages != null && messages.isNotEmpty()) {
                    val lastMessage = messages[messages.size - 1]
                    if (lastMessage is android.os.Bundle) {
                        val msgText = lastMessage.getCharSequence("text")?.toString()
                        if (!msgText.isNullOrBlank()) {
                            actualText = msgText
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gagal mengekstrak pesan asli terakhir dari MessagingStyle: ${e.message}")
            }

            // Bersihkan pesan dari pengulangan sender/title/group prefix
            var cleanedMessage = actualText ?: text ?: bigText ?: ""
            if (!sender.isNullOrBlank() && cleanedMessage.startsWith("$sender:")) {
                cleanedMessage = cleanedMessage.substringAfter("$sender:").trim()
            }
            if (!groupName.isNullOrBlank() && cleanedMessage.startsWith("$groupName:")) {
                cleanedMessage = cleanedMessage.substringAfter("$groupName:").trim()
            }
            if (!title.isNullOrBlank() && cleanedMessage.startsWith("$title:")) {
                cleanedMessage = cleanedMessage.substringAfter("$title:").trim()
            }

            val id = "${sbn.packageName}_${sbn.id}_${sbn.postTime}"

            NotificationContent(
                id = id,
                packageName = sbn.packageName,
                title = title,
                text = cleanedMessage, // Menggunakan isi pesan yang bersih dari duplikasi sender
                bigText = bigText,
                subText = subText,
                timestamp = sbn.postTime,
                sender = sender,
                isMessagingNotification = isMessaging,
                groupName = groupName,
                isGroup = isGroup
            ).also { content ->
                if (!content.hasAnalyzableContent()) {
                    Log.d(TAG, "Notifikasi dari ${sbn.packageName} tidak memiliki konten teks yang bisa dianalisis.")
                }
            }
        } catch (e: Exception) {
            // Requirement: 1.7 - Log error dan lanjutkan ke notifikasi berikutnya
            Log.e(TAG, "Gagal mengekstrak konten notifikasi dari ${sbn.packageName}: ${e.message}", e)
            null
        }
    }

    /**
     * Mengekstrak informasi pengirim dari extras notifikasi.
     * Mendukung MessagingStyle Android dan format teks standar.
     */
    private fun extractSender(
        sbn: StatusBarNotification,
        extras: android.os.Bundle
    ): String? {
        return try {
            // 1. Coba ambil sender dari pesan TERAKHIR (pesan paling baru masuk di WhatsApp/MessagingStyle)
            val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            if (messages != null && messages.isNotEmpty()) {
                val lastMessage = messages[messages.size - 1]
                if (lastMessage is android.os.Bundle) {
                    val lastSender = lastMessage.getCharSequence("sender")?.toString()
                    if (!lastSender.isNullOrBlank()) {
                        return lastSender
                    }
                }
            }
            // 2. Fallback: gunakan Notification.EXTRA_TITLE
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val convTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
            
            when {
                !title.isNullOrBlank() -> title
                !convTitle.isNullOrBlank() -> convTitle
                else -> null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gagal mengekstrak sender dari ${sbn.packageName}: ${e.message}")
            null
        }
    }
}
