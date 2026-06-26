package com.artbirwww.messenger.data.model

data class Chat(
    val id: String = "",
    val otherUserId: String = "",
    val otherUserEmail: String = "",
    val otherUserName: String = "",
    val otherUserFirstName: String = "",
    val otherUserLastName: String = "",
    val otherUserPhotoURL: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val lastSenderId: String = "",
    val unreadCount: Int = 0,
    val background: ChatBackground? = null
)

data class ChatBackground(
    val type: String = "", // "color", "gradient", "image"
    val value: String = ""
)
