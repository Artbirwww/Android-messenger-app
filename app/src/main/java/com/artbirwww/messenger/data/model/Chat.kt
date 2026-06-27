package com.artbirwww.messenger.data.model

data class Chat(
    val id: String = "",
    val participantIds: List<String> = emptyList(), // Список тех, у кого чат в списке
    val members: List<String> = emptyList(), // Все участники (всегда двое)
    val otherUserId: String = "",
    val otherUserEmail: String = "",
    val otherUserName: String = "",
    val otherUserFirstName: String = "",
    val otherUserLastName: String = "",
    val otherUserPhotoURL: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val lastSenderId: String = "",
    val messageCount: Long = 0L,
    val unreadCount: Int = 0,
    val background: ChatBackground? = null
)

data class ChatBackground(
    val type: String = "", // "color", "gradient", "image"
    val value: String = ""
)
