package com.artbirwww.messenger.ui.screens.profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artbirwww.messenger.data.model.User
import com.artbirwww.messenger.data.repository.AuthRepository
import com.artbirwww.messenger.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    var userState = mutableStateOf<User?>(null)
    var isSaving = mutableStateOf(false)

    // Media gallery from messages
    private val _mediaUrls = MutableStateFlow<List<String>>(emptyList())
    val mediaUrls: StateFlow<List<String>> = _mediaUrls

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val currentUid = AuthRepository.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            val user = AuthRepository.getUserProfile(currentUid)
            userState.value = user
            if (user != null) {
                loadMediaGallery()
            }
        }
    }

    private fun loadMediaGallery() {
        viewModelScope.launch {
            // In a real app, we'd aggregate from all user's chats or a specific one.
            // Matching web's logic which listens to current chat.
            ChatRepository.getMessages("general_chat").collect { messages ->
                val urls = mutableListOf<String>()
                messages.forEach { msg ->
                    msg.imageUrl?.let { urls.add(it) }
                    msg.imageUrls?.let { urls.addAll(it) }
                }
                _mediaUrls.value = urls.distinct().reversed()
            }
        }
    }

    fun saveProfile(
        firstName: String,
        lastName: String,
        bio: String,
        phone: String,
        birthday: String,
        gender: String
    ) {
        val currentUser = userState.value ?: return
        val updated = currentUser.copy(
            firstName = firstName,
            lastName = lastName,
            name = "$firstName $lastName".trim(),
            bio = bio,
            phone = phone,
            birthday = birthday,
            gender = gender
        )
        
        isSaving.value = true
        viewModelScope.launch {
            val success = AuthRepository.updateUserProfile(updated)
            if (success) {
                userState.value = updated
            }
            isSaving.value = false
        }
    }

    fun updateAvatar(newPhotoURL: String) {
        val currentUser = userState.value ?: return
        val updated = currentUser.copy(photoURL = newPhotoURL)
        viewModelScope.launch {
            val success = AuthRepository.updateUserProfile(updated)
            if (success) userState.value = updated
        }
    }
    
    fun signOut() {
        AuthRepository.signOut()
    }
}
