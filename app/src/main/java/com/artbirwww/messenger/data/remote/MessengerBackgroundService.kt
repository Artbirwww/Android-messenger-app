package com.artbirwww.messenger.data.remote

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.artbirwww.messenger.data.model.Chat
import com.artbirwww.messenger.data.repository.AuthRepository
import com.artbirwww.messenger.data.repository.ChatRepository
import com.artbirwww.messenger.ui.components.NotificationHelper
import kotlinx.coroutines.*

class MessengerBackgroundService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val lastChatCounts = mutableMapOf<String, Long>()
    private var serviceStartTime = System.currentTimeMillis()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("MessengerService", "Service Created")
        startForegroundService()
        observeMessages()
    }

    private fun startForegroundService() {
        val channelId = "messenger_service"
        val channelName = "Messenger Background Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Мессенджер онлайн")
            .setContentText("Служба уведомлений активна")
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .setPriority(NotificationCompat.PRIORITY_MIN)
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
                Log.d("MessengerService", "Received ${chats.size} chats")
                chats.forEach { chat ->
                    val lastCount = lastChatCounts[chat.id]
                    
                    val isIncreased = lastCount != null && chat.messageCount > lastCount
                    val isNewAndFresh = lastCount == null && chat.lastMessageTime > serviceStartTime - 10000

                    if ((isIncreased || isNewAndFresh) && chat.lastSenderId != currentUser.uid) {
                        Log.d("MessengerService", "Showing notification for chat ${chat.id}")
                        NotificationHelper.showNotification(
                            applicationContext,
                            chat.otherUserName.ifEmpty { "Новое сообщение" },
                            chat.lastMessage
                        )
                    }
                    
                    lastChatCounts[chat.id] = chat.messageCount
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MessengerService", "Service Destroyed")
        serviceJob.cancel()
    }
}
