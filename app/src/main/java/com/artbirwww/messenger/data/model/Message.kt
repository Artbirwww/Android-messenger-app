package com.artbirwww.messenger.data.model

data class Message(
    val id: String = "",
    val text: String = "",
    val fromId: String = "",
    val toId: String = "",
    val timestamp: Long = 0L,
    val imageUrl: String? = null,
    val imageUrls: List<String>? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileType: String? = null,
    val fileSize: Long? = null,
    val files: List<AttachmentFile>? = null,
    val audioUrl: String? = null,
    val audioDuration: Long? = null,
    val videoUrl: String? = null,
    val isVideoMessage: Boolean = false,
    val editedAt: Long? = null,
    val replyTo: ReplyTo? = null,
    val reactions: Map<String, Long> = emptyMap()
)

data class AttachmentFile(
    val url: String = "",
    val name: String = "",
    val type: String = "",
    val size: Long = 0L
)

data class ReplyTo(
    val messageId: String = "",
    val text: String = "",
    val fromId: String = "",
    val fromName: String? = null
)
