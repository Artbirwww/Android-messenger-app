package com.artbirwww.messenger.ui.screens.chat

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artbirwww.messenger.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onContactSelected: (String, String, String) -> Unit, // chatId, otherUserId, otherUserName
    onNavigateToRoute: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ContactsViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUserId = com.artbirwww.messenger.data.repository.AuthRepository.getCurrentUser()?.uid ?: ""

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadContacts(context)
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Контакты") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            com.artbirwww.messenger.ui.components.MessengerBottomBar(
                currentRoute = "contacts",
                onNavigate = onNavigateToRoute
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (viewModel.isLoading.value) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.registeredContacts.value.isEmpty()) {
                Text(
                    text = "Нет зарегистрированных контактов",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn {
                    items(viewModel.registeredContacts.value) { user ->
                        ListItem(
                            headlineContent = { Text(user.name) },
                            supportingContent = { Text(user.phone) },
                            leadingContent = { UserAvatar(photoURL = user.photoURL) },
                            modifier = Modifier.clickable {
                                val chatId = listOf(currentUserId, user.uid).sorted().joinToString("_")
                                onContactSelected(chatId, user.uid, user.name)
                            }
                        )
                    }
                }
            }
        }
    }
}
