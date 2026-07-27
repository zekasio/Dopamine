package com.dopamine.app.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // TODO: Send token to Supabase for the current user
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        val title = remoteMessage.notification?.title ?: "Yeni Bildirim"
        val body = remoteMessage.notification?.body ?: ""
        
        val notificationManager = AppNotificationManager(applicationContext)
        notificationManager.sendNotification(title, body)
    }
}
