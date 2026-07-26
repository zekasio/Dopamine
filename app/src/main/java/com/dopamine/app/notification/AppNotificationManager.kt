package com.dopamine.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dopamine.app.MainActivity

class AppNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "dopamine_reports_channel"
        const val CHANNEL_NAME = "Dopamine Bildirimleri"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Dopamine haftalık rapor, moderatör dürtme ve onay/red bildirimleri"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(title: String, message: String, notificationId: Int = System.currentTimeMillis().toInt()) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, builder.build())
    }

    fun sendRejectionNotification(username: String, reason: String) {
        sendNotification(
            title = "🔴 Raporunuz Reddedildi!",
            message = "Sayın $username, haftalık saha raporunuz reddedildi. Red nedeni: $reason"
        )
    }

    fun sendNudgeNotification(username: String) {
        sendNotification(
            title = "🚨 Moderatör Hatırlatması!",
            message = "Sayın $username, moderatör bu haftaki saha raporunuzu doldurmanızı hatırlattı."
        )
    }

    fun sendApprovalNotification(username: String) {
        sendNotification(
            title = "✅ Raporunuz Onaylandı!",
            message = "Tebrikler $username, bu haftaki saha raporunuz onaylandı."
        )
    }

    fun sendSundayReminder(username: String, hour: Int) {
        val title = if (hour == 18) "⏰ Bu haftalık Raporu Doldurun ($username)" else "⏰ Lütfen Bugunki raporu Doldurun"
        val body = "Pazar saat $hour:00 (Son Gün)! Lütfen haftalık saha çalışması verilerinizi girmeyi unutmayın."
        sendNotification(title = title, message = body)
    }
}
