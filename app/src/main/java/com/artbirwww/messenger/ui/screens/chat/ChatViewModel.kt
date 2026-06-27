package com.artbirwww.messenger.ui.screens.chat

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.artbirwww.messenger.data.model.Message
import com.artbirwww.messenger.data.model.ReplyTo
import com.artbirwww.messenger.data.model.User
import com.artbirwww.messenger.data.repository.AuthRepository
import com.artbirwww.messenger.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _allMessages = MutableStateFlow<List<Message>>(emptyList())
    val searchQuery = MutableStateFlow("")
    val chatBackground = MutableStateFlow<com.artbirwww.messenger.data.model.ChatBackground?>(null)
    
    val messages: StateFlow<List<Message>> = combine(_allMessages, searchQuery) { msgs, query ->
        if (query.isBlank()) msgs
        else msgs.filter { it.text.contains(query, ignoreCase = true) }
    }.let { flow ->
        val state = MutableStateFlow<List<Message>>(emptyList())
        viewModelScope.launch {
            flow.collect { state.value = it }
        }
        state
    }

    var currentChatId = "general_chat"
    var otherUserId = "" 
    var otherUser = mutableStateOf<User?>(null)
    
    var typedMessage = mutableStateOf("")
    var replyingTo = mutableStateOf<Message?>(null)
    var editingMessage = mutableStateOf<Message?>(null)
    var isUploading = mutableStateOf(false)
    
    // Recording States
    var isRecordingAudio = mutableStateOf(false)
    var isRecordingVideo = mutableStateOf(false)
    private var audioFile: java.io.File? = null
    
    private val audioRecorder by lazy { com.artbirwww.messenger.data.remote.AudioRecorder(application) }

    // Selection Mode
    var selectionMode = mutableStateOf(false)
    var selectedMessages = mutableStateOf(setOf<String>())

    val currentUserId: String
        get() = AuthRepository.getCurrentUser()?.uid ?: ""

    fun loadMessages() {
        viewModelScope.launch {
            ChatRepository.joinChat(currentChatId, currentUserId)
            ChatRepository.getMessages(currentChatId).collect {
                _allMessages.value = it
            }
        }
        viewModelScope.launch {
            ChatRepository.listenChatBackground(currentChatId).collect {
                chatBackground.value = it
            }
        }
        viewModelScope.launch {
            if (otherUserId.isNotEmpty()) {
                otherUser.value = AuthRepository.getUserProfile(otherUserId)
            }
        }
    }

    fun startAudioRecording() {
        val file = java.io.File(getApplication<Application>().cacheDir, "audio_msg_${System.currentTimeMillis()}.m4a")
        audioFile = file
        audioRecorder.start(file)
        isRecordingAudio.value = true
    }

    fun stopAudioRecording() {
        audioRecorder.stop()
        isRecordingAudio.value = false
        audioFile?.let { file ->
            uploadAndSendAudio(file)
        }
    }

    private fun uploadAndSendAudio(file: java.io.File) {
        viewModelScope.launch {
            isUploading.value = true
            val bytes = file.readBytes()
            val url = com.artbirwww.messenger.data.remote.GitHubService.uploadFile(
                bytes, file.name, "chat", currentUserId, currentChatId
            )
            isUploading.value = false
            if (url != null) {
                sendAudioMessage(url)
            }
        }
    }

    fun sendAudioMessage(url: String) {
        viewModelScope.launch {
            val msg = Message(
                fromId = currentUserId,
                toId = otherUserId,
                text = "",
                timestamp = System.currentTimeMillis(),
                audioUrl = url
            )
            ChatRepository.sendMessage(currentChatId, msg)
        }
    }

    fun sendVideoMessage(file: java.io.File) {
        viewModelScope.launch {
            isUploading.value = true
            val bytes = file.readBytes()
            val url = com.artbirwww.messenger.data.remote.GitHubService.uploadFile(
                bytes, file.name, "chat", currentUserId, currentChatId
            )
            isUploading.value = false
            if (url != null) {
                val msg = Message(
                    fromId = currentUserId,
                    toId = otherUserId,
                    text = "",
                    timestamp = System.currentTimeMillis(),
                    videoUrl = url,
                    isVideoMessage = true
                )
                ChatRepository.sendMessage(currentChatId, msg)
            }
        }
    }

    fun updateBackground(type: String, value: String) {
        viewModelScope.launch {
            ChatRepository.saveChatBackground(currentChatId, com.artbirwww.messenger.data.model.ChatBackground(type, value))
        }
    }

    fun sendMessage(imageUrl: String? = null, fileUrl: String? = null, fileName: String? = null, fileType: String? = null, fileSize: Long? = null) {
        val text = typedMessage.value.trim()
        if (text.isEmpty() && imageUrl == null && fileUrl == null) return

        viewModelScope.launch {
            if (editingMessage.value != null) {
                ChatRepository.editMessage(currentChatId, editingMessage.value!!.id, text)
                editingMessage.value = null
                typedMessage.value = ""
                return@launch
            }

            val reply = replyingTo.value?.let {
                ReplyTo(
                    messageId = it.id,
                    text = it.text,
                    fromId = it.fromId,
                    fromName = otherUser.value?.name ?: "User"
                )
            }
            
            val msg = Message(
                fromId = currentUserId,
                toId = otherUserId,
                text = text,
                timestamp = System.currentTimeMillis(),
                imageUrl = imageUrl,
                fileUrl = fileUrl,
                fileName = fileName,
                fileType = fileType,
                fileSize = fileSize,
                replyTo = reply
            )
            ChatRepository.sendMessage(currentChatId, msg)

            typedMessage.value = ""
            replyingTo.value = null
        }
    }

    fun uploadAndSendFile(fileBytes: ByteArray, fileName: String, fileType: String) {
        viewModelScope.launch {
            isUploading.value = true
            val url = com.artbirwww.messenger.data.remote.GitHubService.uploadFile(
                fileBytes, fileName, "chat", currentUserId, currentChatId
            )
            isUploading.value = false
            if (url != null) {
                if (fileType.startsWith("image/")) {
                    sendMessage(imageUrl = url)
                } else {
                    sendMessage(fileUrl = url, fileName = fileName, fileType = fileType, fileSize = fileBytes.size.toLong())
                }
            }
        }
    }

    fun toggleSelection(messageId: String) {
        val current = selectedMessages.value
        if (current.contains(messageId)) {
            selectedMessages.value = current - messageId
        } else {
            selectedMessages.value = current + messageId
        }
        if (selectedMessages.value.isEmpty()) {
            selectionMode.value = false
        }
    }

    fun deleteSelectedMessages() {
        viewModelScope.launch {
            selectedMessages.value.forEach { id ->
                ChatRepository.deleteMessage(currentChatId, id)
            }
            selectedMessages.value = emptySet()
            selectionMode.value = false
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            ChatRepository.deleteMessage(currentChatId, messageId)
        }
    }

    fun startEditing(message: Message) {
        editingMessage.value = message
        typedMessage.value = message.text
    }

    fun cancelEditOrReply() {
        editingMessage.value = null
        replyingTo.value = null
        typedMessage.value = ""
        selectionMode.value = false
        selectedMessages.value = emptySet()
    }

    fun addReaction(messageId: String, emoji: String) {
        ChatRepository.addReaction(currentChatId, messageId, emoji)
    }
}
