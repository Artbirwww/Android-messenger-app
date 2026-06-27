package com.artbirwww.messenger.data.remote

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.artbirwww.messenger.data.repository.AuthRepository
import com.artbirwww.messenger.data.repository.ChatRepository
import com.artbirwww.messenger.ui.components.NotificationHelper
import kotlinx.coroutines.*

class MessengerBackgroundService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var lastProcessedTime = System.currentTimeMillis()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        observeMessages()
    }

    private fun startForegroundService() {
        val channelId = "messenger_service"
        val channelName = "Messenger Background Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Мессенджер активен")
            .setContentText("Ожидание новых сообщений...")
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
    }

    private fun observeMessages() {
        val currentUser = AuthRepository.getCurrentUser() ?: return
        serviceScope.launch {
            ChatRepository.getChats(currentUser.uid).collect { chats ->
                val latestChat = chats.firstOrNull() ?: return@collect
                if (latestChat.lastSenderId != currentUser.uid && 
                    latestChat.lastMessageTime > lastProcessedTime) {
                    
                    NotificationHelper.showNotification(
                        applicationContext,
                        latestChat.otherUserName.ifEmpty { "Новое сообщение" },
                        latestChat.lastMessage
                    )
                    lastProcessedTime = latestChat.lastMessageTime
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
