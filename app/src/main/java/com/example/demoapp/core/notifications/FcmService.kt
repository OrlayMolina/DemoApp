package com.example.demoapp.core.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.demoapp.MainActivity
import com.example.demoapp.R
import com.example.demoapp.domain.model.Notification
import com.example.demoapp.domain.model.NotificationType
import com.example.demoapp.domain.repository.NotificationRepository
import com.example.demoapp.domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var notificationRepository: NotificationRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        userRepository.currentUser.value?.let { current ->
            userRepository.updateFcmToken(current.id, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val rawTitle = data["title"] ?: message.notification?.title
        val rawBody  = data["body"]  ?: message.notification?.body
        val title = rawTitle?.takeIf { it.isNotBlank() } ?: getString(R.string.fcm_default_title)
        val body  = rawBody?.takeIf  { it.isNotBlank() } ?: getString(R.string.fcm_default_body)

        showSystemNotification(title, body)
        saveToInAppList(data)
    }

    private fun saveToInAppList(data: Map<String, String>) {
        val typeStr = data["type"]?.takeIf { it.isNotBlank() } ?: return
        val type = runCatching { NotificationType.valueOf(typeStr) }.getOrNull() ?: return

        val notification = Notification(
            id = "",
            type = type,
            userName = data["userName"].orEmpty(),
            publicationTitle = data["publicationTitle"]?.takeIf { it.isNotBlank() },
            date = SimpleDateFormat("dd MMM, HH:mm", Locale("es")).format(Date()),
            createdAt = System.currentTimeMillis(),
            isRead = false,
            relatedEntityId = data["relatedEntityId"]?.takeIf { it.isNotBlank() }
        )

        notificationRepository.add(notification)
    }

    private fun showSystemNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, NotificationChannelIds.DEFAULT)
            .setSmallIcon(R.mipmap.logo_red_explora)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        getSystemService(NotificationManager::class.java)
            ?.notify(System.currentTimeMillis().toInt(), notification)
    }
}
