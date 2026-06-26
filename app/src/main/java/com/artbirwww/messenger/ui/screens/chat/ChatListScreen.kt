package com.artbirwww.messenger.ui.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artbirwww.messenger.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatSelected: (String, String, String) -> Unit, // chatId, otherUserId, otherUserName
    onNavigateToProfile: () -> Unit,
    viewModel: ChatListViewModel = viewModel()
) {
    val chatsState = viewModel.chats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чаты") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = viewModel.searchQuery.value,
                onValueChange = { 
                    viewModel.searchQuery.value = it
                    viewModel.searchUsers(it)
                },
                placeholder = { Text("Найти контакты…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            if (viewModel.searchResults.value.isNotEmpty()) {
                Text(
                    text = "Результаты поиска",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium
                )
                LazyColumn(modifier = Modifier.weight(0.4f)) {
                    items(viewModel.searchResults.value) { user ->
                        ListItem(
                            headlineContent = { Text(user.name) },
                            supportingContent = { Text(user.email) },
                            leadingContent = { UserAvatar(photoURL = user.photoURL) },
                            modifier = Modifier.clickable {
                                val chatId = listOf(viewModel.currentUserId, user.uid).sorted().joinToString("_")
                                onChatSelected(chatId, user.uid, user.name)
                            }
                        )
                    }
                }
                Divider()
            }

            // Chat List
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(chatsState.value) { chat ->
                    ListItem(
                        headlineContent = { Text(chat.otherUserName) },
                        supportingContent = { 
                            Text(
                                text = chat.lastMessage,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            ) 
                        },
                        leadingContent = { UserAvatar(photoURL = chat.otherUserPhotoURL) },
                        trailingContent = {
                            Column(horizontalAlignment = Alignment.End) {
                                val time = if (chat.lastMessageTime > 0) {
                                    java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(chat.lastMessageTime))
                                } else ""
                                Text(text = time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (chat.unreadCount > 0) {
                                    Badge { Text(chat.unreadCount.toString()) }
                                }
                            }
                        },
                        modifier = Modifier.clickable {
                            onChatSelected(chat.id, chat.otherUserId, chat.otherUserName)
                        }
                    )
                }
            }
        }
    }
}
