package com.artbirwww.messenger.data.repository

import android.util.Log
import com.artbirwww.messenger.data.model.Chat
import com.artbirwww.messenger.data.model.ChatBackground
import com.artbirwww.messenger.data.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object ChatRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "Listen failed", error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    fun getChats(userId: String): Flow<List<Chat>> = callbackFlow {
        // In this implementation, we assume a "chats" collection where 
        // documents have a "participantIds" array.
        val listener = db.collection("chats")
            .whereArrayContains("participantIds", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "Listen failed", error)
                    return@addSnapshotListener
                }
                val chats = snapshot?.toObjects(Chat::class.java) ?: emptyList()
                trySend(chats)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(chatId: String, message: Message) {
        try {
            val docRef = db.collection("chats").document(chatId).collection("messages").document()
            val finalMessage = message.copy(id = docRef.id)
            docRef.set(finalMessage).await()

            // Update chat metadata in Firestore (unlike web which uses localStorage primarily,
            // we'll keep it in Firestore for better sync, while keeping the web's structure)
            db.collection("chats").document(chatId).set(
                mapOf(
                    "lastMessage" to (message.text.ifEmpty { "Attachment" }),
                    "lastMessageTime" to message.timestamp,
                    "lastSenderId" to message.fromId,
                    "participantIds" to listOf(message.fromId, message.toId),
                    "updatedAt" to System.currentTimeMillis()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Send failed", e)
        }
    }

    suspend fun deleteMessage(chatId: String, messageId: String) {
        try {
            db.collection("chats").document(chatId).collection("messages").document(messageId).delete().await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Delete failed", e)
        }
    }

    suspend fun editMessage(chatId: String, messageId: String, newText: String) {
        try {
            db.collection("chats").document(chatId).collection("messages").document(messageId)
                .update(mapOf(
                    "text" to newText,
                    "editedAt" to System.currentTimeMillis()
                )).await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Edit failed", e)
        }
    }

    fun addReaction(chatId: String, messageId: String, reaction: String) {
        val docRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentReactions = snapshot.get("reactions") as? Map<String, Long> ?: emptyMap()
            val newCount = (currentReactions[reaction] ?: 0L) + 1
            val updatedReactions = currentReactions.toMutableMap().apply { this[reaction] = newCount }
            transaction.update(docRef, "reactions", updatedReactions)
        }.addOnFailureListener { Log.e("ChatRepository", "Reaction failed", it) }
    }

    fun listenChatBackground(chatId: String): Flow<ChatBackground?> = callbackFlow {
        val listener = db.collection("chats").document(chatId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val background = snapshot?.get("background") as? Map<String, String>
                if (background != null) {
                    trySend(ChatBackground(background["type"] ?: "", background["value"] ?: ""))
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveChatBackground(chatId: String, background: ChatBackground?) {
        db.collection("chats").document(chatId).update("background", background).await()
    }
}
