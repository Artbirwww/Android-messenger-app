package com.artbirwww.messenger.ui.screens.chat

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artbirwww.messenger.data.model.Chat
import com.artbirwww.messenger.data.model.User
import com.artbirwww.messenger.data.repository.AuthRepository
import com.artbirwww.messenger.data.repository.ChatRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatListViewModel : ViewModel() {
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    var searchQuery = mutableStateOf("")
    var searchResults = mutableStateOf<List<User>>(emptyList())
    var isSearching = mutableStateOf(false)

    val currentUserId: String
        get() = AuthRepository.getCurrentUser()?.uid ?: ""

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            ChatRepository.getChats(currentUserId).collect { baseChats ->
                // Асинхронно обогащаем чаты информацией о пользователях
                val enrichedChats = baseChats.map { chat ->
                    async {
                        if (chat.otherUserId.isNotEmpty()) {
                            val user = AuthRepository.getUserProfile(chat.otherUserId)
                            if (user != null) {
                                chat.copy(
                                    otherUserName = user.name.ifEmpty { user.email },
                                    otherUserPhotoURL = user.photoURL,
                                    otherUserEmail = user.email
                                )
                            } else chat
                        } else chat
                    }
                }.awaitAll()
                _chats.value = enrichedChats
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            searchResults.value = emptyList()
            return
        }
        isSearching.value = true
        viewModelScope.launch {
            val users = AuthRepository.searchUsers(query)
            searchResults.value = users.filter { it.uid != currentUserId }
            isSearching.value = false
        }
    }
}
