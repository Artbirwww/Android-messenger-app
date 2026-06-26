package com.artbirwww.messenger.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

@Composable
fun LoginScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.login_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        PrimaryInputField(
            value = viewModel.email.value,
            onValueChange = { viewModel.email.value = it },
            hint = stringResource(id = R.string.email_hint)
        )
        Spacer(modifier = Modifier.height(16.dp))

        PasswordInputField(
            value = viewModel.password.value,
            onValueChange = { viewModel.password.value = it },
            hint = stringResource(id = R.string.password_hint)
        )
        Spacer(modifier = Modifier.height(24.dp))

        viewModel.errorMessage.value?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        ActionButton(text = stringResource(id = R.string.btn_login)) {
            viewModel.login { user ->
                if (user != null) onNavigateToChat()
            }
        }

        TextButton(onClick = onNavigateToRegister) {
            Text(text = stringResource(id = R.string.goto_reg))
        }
    }
}