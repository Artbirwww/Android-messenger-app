package com.artbirwww.messenger.ui.screens.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artbirwww.messenger.R
import com.artbirwww.messenger.ui.components.ActionButton
import com.artbirwww.messenger.ui.components.PasswordInputField
import com.artbirwww.messenger.ui.components.PrimaryInputField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToChat: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.register_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        PrimaryInputField(
            value = viewModel.firstName.value,
            onValueChange = { viewModel.firstName.value = it },
            hint = "Имя *"
        )
        Spacer(modifier = Modifier.height(12.dp))

        PrimaryInputField(
            value = viewModel.lastName.value,
            onValueChange = { viewModel.lastName.value = it },
            hint = "Фамилия *"
        )
        Spacer(modifier = Modifier.height(12.dp))

        PrimaryInputField(
            value = viewModel.email.value,
            onValueChange = { viewModel.email.value = it },
            hint = stringResource(id = R.string.email_hint)
        )
        Spacer(modifier = Modifier.height(12.dp))

        PrimaryInputField(
            value = viewModel.phone.value,
            onValueChange = { viewModel.phone.value = it },
            hint = stringResource(id = R.string.phone_hint)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Gender Selection (Simplified as Row of chips or similar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("male" to "Мужской", "female" to "Женский", "other" to "Другой").forEach { (value, label) ->
                FilterChip(
                    selected = viewModel.gender.value == value,
                    onClick = { viewModel.gender.value = value },
                    label = { Text(label) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        PasswordInputField(
            value = viewModel.password.value,
            onValueChange = { viewModel.password.value = it },
            hint = stringResource(id = R.string.password_hint)
        )
        Spacer(modifier = Modifier.height(12.dp))

        PasswordInputField(
            value = viewModel.repeatPassword.value,
            onValueChange = { viewModel.repeatPassword.value = it },
            hint = stringResource(id = R.string.repeat_password_hint)
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (viewModel.isLoading.value) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
        }

        viewModel.errorMessage.value?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        ActionButton(
            text = if (viewModel.isLoading.value) "Создание..." else stringResource(id = R.string.btn_register),
            enabled = !viewModel.isLoading.value
        ) {
            viewModel.register {
                onNavigateToChat()
            }
        }

        TextButton(onClick = onNavigateToLogin, enabled = !viewModel.isLoading.value) {
            Text(text = stringResource(id = R.string.goto_login))
        }
    }
}
