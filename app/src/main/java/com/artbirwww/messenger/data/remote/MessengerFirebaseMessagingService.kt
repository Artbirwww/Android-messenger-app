package com.artbirwww.messenger.data.remote

import com.artbirwww.messenger.ui.components.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessengerFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"] ?: "Новое сообщение"
        val body = message.notification?.body ?: message.data["body"] ?: ""

        NotificationHelper.showNotification(applicationContext, title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        updateTokenInFirestore(token)
    }

    private fun updateTokenInFirestore(token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
            .update("fcmToken", token)
    }
}
