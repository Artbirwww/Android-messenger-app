package com.artbirwww.messenger.ui.screens.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artbirwww.messenger.R
import com.artbirwww.messenger.ui.components.MessageBubble
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    otherUserId: String,
    onNavigateToProfile: () -> Unit,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    // Initialize viewModel with parameters
    LaunchedEffect(chatId, otherUserId) {
        viewModel.currentChatId = chatId
        viewModel.otherUserId = otherUserId
        viewModel.loadMessages()
    }

    val messagesState = viewModel.messages.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val chatBackground by viewModel.chatBackground.collectAsState()
    var isSearchVisible by remember { mutableStateOf(false) }
    var showBgMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            val fileName = it.path?.substringAfterLast('/') ?: "file"
            val fileType = context.contentResolver.getType(it) ?: "application/octet-stream"
            if (bytes != null) {
                viewModel.uploadAndSendFile(bytes, fileName, fileType)
            }
        }
    }

    Scaffold(
        topBar = {
            if (isSearchVisible) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            placeholder = { Text("Поиск в чате...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            isSearchVisible = false
                            viewModel.searchQuery.value = ""
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Search")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchVisible = true }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                        }
                        Box {
                            IconButton(onClick = { showBgMenu = true }) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = "Background") // Proxy for Bg
                            }
                            DropdownMenu(
                                expanded = showBgMenu,
                                onDismissRequest = { showBgMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Светлый") },
                                    onClick = { viewModel.updateBackground("color", "#FFFFFF"); showBgMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Тёмный") },
                                    onClick = { viewModel.updateBackground("color", "#1A1A1A"); showBgMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Мятный") },
                                    onClick = { viewModel.updateBackground("color", "#CCFBF1"); showBgMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Персиковый") },
                                    onClick = { viewModel.updateBackground("color", "#FFEDD5"); showBgMenu = false }
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    when (chatBackground?.type) {
                        "color" -> {
                            try {
                                androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(chatBackground?.value))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.background
                            }
                        }
                        else -> MaterialTheme.colorScheme.background
                    }
                )
        ) {
            // Исправлено: weight(1f) заставляет список заполнить всё пространство
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messagesState.value) { message ->
                    var showMenu by remember { mutableStateOf(false) }

                    Box {
                        MessageBubble(
                            message = message,
                            currentUserId = viewModel.currentUserId,
                            onLongClick = { showMenu = true }
                        )

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ответить") },
                                onClick = {
                                    viewModel.replyingTo.value = message
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) } // Share used as proxy for reply
                            )
                            if (message.fromId == viewModel.currentUserId) {
                                DropdownMenuItem(
                                    text = { Text("Редактировать") },
                                    onClick = {
                                        viewModel.startEditing(message)
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Удалить") },
                                onClick = {
                                    viewModel.deleteMessage(message.id)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
            }

            // Reply/Edit Preview
            if (viewModel.replyingTo.value != null || viewModel.editingMessage.value != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (viewModel.editingMessage.value != null) "Редактирование" else "Ответ на",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = viewModel.editingMessage.value?.text ?: viewModel.replyingTo.value?.text ?: "",
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = { viewModel.cancelEditOrReply() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            }

            // Поле ввода теперь всегда прижато к низу списка
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Attach")
                }
                
                OutlinedTextField(
                    value = viewModel.typedMessage.value,
                    onValueChange = { viewModel.typedMessage.value = it },
                    placeholder = { Text(stringResource(id = R.string.send_hint)) },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp)
                )
                
                IconButton(
                    onClick = { viewModel.sendMessage() },
                    enabled = viewModel.typedMessage.value.isNotBlank() || viewModel.isUploading.value
                ) {
                    if (viewModel.isUploading.value) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}