package com.artbirwww.messenger.ui.screens.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artbirwww.messenger.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var loginSuccess = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)

    fun login(onComplete: (FirebaseUser?) -> Unit) {
        if (email.value.isEmpty() || password.value.isEmpty()) {
            errorMessage.value = "Заполните все поля"
            return
        }
        isLoading.value = true
        viewModelScope.launch {
            val user = AuthRepository.signIn(email.value, password.value)
            isLoading.value = false
            if (user != null) {
                loginSuccess.value = true
                onComplete(user)
            } else {
                errorMessage.value = "Ошибка авторизации"
            }
        }
    }
}
