package com.artbirwww.messenger.ui.screens.register

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artbirwww.messenger.data.model.User
import com.artbirwww.messenger.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    var firstName = mutableStateOf("")
    var lastName = mutableStateOf("")
    var email = mutableStateOf("")
    var phone = mutableStateOf("")
    var gender = mutableStateOf("unspecified")
    var password = mutableStateOf("")
    var repeatPassword = mutableStateOf("")

    var errorMessage = mutableStateOf<String?>(null)
    var isSuccess = mutableStateOf(false)
    var isLoading = mutableStateOf(false)

    fun register(onSuccessNavigate: () -> Unit) {
        if (firstName.value.isEmpty() || lastName.value.isEmpty() || email.value.isEmpty() || password.value.isEmpty()) {
            errorMessage.value = "Пожалуйста, заполните обязательные поля"
            return
        }
        if (password.value != repeatPassword.value) {
            errorMessage.value = "Пароли не совпадают"
            return
        }

        isLoading.value = true
        viewModelScope.launch {
            // Normalize phone: keep digits and +, handle leading 8 -> +7
            var normalizedPhone = phone.value.replace(Regex("[^\\d+]"), "")
            if (normalizedPhone.startsWith("8") && normalizedPhone.length == 11) {
                normalizedPhone = "+7" + normalizedPhone.substring(1)
            } else if (normalizedPhone.startsWith("7") && normalizedPhone.length == 11) {
                normalizedPhone = "+$normalizedPhone"
            }

            val newUser = User(
                firstName = firstName.value,
                lastName = lastName.value,
                name = "${firstName.value} ${lastName.value}".trim(),
                email = email.value,
                phone = normalizedPhone,
                gender = gender.value
            )

            val success = AuthRepository.signUp(newUser, password.value)
            isLoading.value = false
            if (success) {
                isSuccess.value = true
                onSuccessNavigate()
            } else {
                errorMessage.value = "Ошибка регистрации. Проверьте данные или сеть."
            }
        }
    }
}
