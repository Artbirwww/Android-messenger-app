package com.artbirwww.messenger.ui.screens.chat

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artbirwww.messenger.data.model.Chat
import com.artbirwww.messenger.data.model.User
import com.artbirwww.messenger.data.repository.AuthRepository
import com.artbirwww.messenger.data.repository.ChatRepository
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
            ChatRepository.getChats(currentUserId).collect {
                _chats.value = it
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
