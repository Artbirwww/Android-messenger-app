package com.artbirwww.messenger.ui.screens.chat

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.artbirwww.messenger.R
import com.artbirwww.messenger.data.model.Message
import com.artbirwww.messenger.data.model.User
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
    LaunchedEffect(chatId, otherUserId) {
        viewModel.currentChatId = chatId
        viewModel.otherUserId = otherUserId
        viewModel.loadMessages()
    }

    val messagesState by viewModel.messages.collectAsState()
    val searchQueryState by viewModel.searchQuery.collectAsState()
    val chatBackground by viewModel.chatBackground.collectAsState()
    var isSearchVisible by remember { mutableStateOf(false) }
    var showBgMenu by remember { mutableStateOf(false) }
    var showRecipientProfile by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        ))
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            val fileName = it.lastPathSegment ?: "file"
            val fileType = context.contentResolver.getType(it) ?: "application/octet-stream"
            if (bytes != null) {
                viewModel.uploadAndSendFile(bytes, fileName, fileType)
            }
        }
    }

    Scaffold(
        topBar = {
            if (viewModel.selectionMode.value) {
                TopAppBar(
                    title = { Text(text = "${viewModel.selectedMessages.value.size} выбрано") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.cancelEditOrReply() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.deleteSelectedMessages() }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            } else if (isSearchVisible) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQueryState,
                            onValueChange = { newValue: String -> viewModel.searchQuery.value = newValue },
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
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showRecipientProfile = true }
                        ) {
                            com.artbirwww.messenger.ui.components.UserAvatar(
                                photoURL = viewModel.otherUser.value?.photoURL,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = viewModel.otherUser.value?.name ?: "Чат",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = viewModel.otherUser.value?.email ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
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
                                Icon(imageVector = Icons.Default.Settings, contentDescription = "Background") 
                            }
                            DropdownMenu(
                                expanded = showBgMenu,
                                onDismissRequest = { showBgMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(text = "Светлый") },
                                    onClick = { viewModel.updateBackground("color", "#FFFFFF"); showBgMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = "Тёмный") },
                                    onClick = { viewModel.updateBackground("color", "#1A1A1A"); showBgMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = "Мятный") },
                                    onClick = { viewModel.updateBackground("color", "#CCFBF1"); showBgMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = "Персиковый") },
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
        if (showRecipientProfile && viewModel.otherUser.value != null) {
            RecipientProfileDialog(
                user = viewModel.otherUser.value!!,
                messages = messagesState,
                onDismiss = { showRecipientProfile = false }
            )
        }

        val bgColor = when (chatBackground?.type) {
            "color" -> {
                try {
                    Color(android.graphics.Color.parseColor(chatBackground?.value))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.background
                }
            }
            else -> MaterialTheme.colorScheme.background
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(bgColor)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messagesState.size, key = { index: Int -> messagesState[index].id }) { index: Int ->
                    val message = messagesState[index]
                    var showMenu by remember { mutableStateOf(false) }
                    val isSelected = viewModel.selectedMessages.value.contains(message.id)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (viewModel.selectionMode.value) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { viewModel.toggleSelection(message.id) },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            MessageBubble(
                                message = message,
                                currentUserId = viewModel.currentUserId,
                                highlightQuery = searchQueryState,
                                onLongClick = { 
                                    if (!viewModel.selectionMode.value) {
                                        showMenu = true 
                                    }
                                },
                                onClick = {
                                    if (viewModel.selectionMode.value) {
                                        viewModel.toggleSelection(message.id)
                                    }
                                }
                            )

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(text = "Ответить") },
                                    onClick = {
                                        viewModel.replyingTo.value = message
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Share, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = "Выбрать") },
                                    onClick = {
                                        viewModel.selectionMode.value = true
                                        viewModel.toggleSelection(message.id)
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Check, contentDescription = null) }
                                )
                                if (message.fromId == viewModel.currentUserId) {
                                    DropdownMenuItem(
                                        text = { Text(text = "Редактировать") },
                                        onClick = {
                                            viewModel.startEditing(message)
                                            showMenu = false
                                        },
                                        leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(text = "Удалить") },
                                    onClick = {
                                        viewModel.deleteMessage(message.id)
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                )
                            }
                        }
                    }
                }
            }

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
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            }

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
                
                IconButton(onClick = { viewModel.isRecordingVideo.value = true }) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Video Message", tint = Color.Red)
                }

                OutlinedTextField(
                    value = viewModel.typedMessage.value,
                    onValueChange = { newValue: String -> viewModel.typedMessage.value = newValue },
                    placeholder = { 
                        Text(text = if (viewModel.isRecordingAudio.value) "Запись аудио..." else stringResource(id = R.string.send_hint)) 
                    },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    enabled = !viewModel.isRecordingAudio.value
                )
                
                if (viewModel.typedMessage.value.isEmpty() && !viewModel.isUploading.value) {
                    IconButton(onClick = {
                        if (viewModel.isRecordingAudio.value) {
                            viewModel.stopAudioRecording()
                        } else {
                            viewModel.startAudioRecording()
                        }
                    }) {
                        Icon(
                            imageVector = if (viewModel.isRecordingAudio.value) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = "Audio Message",
                            tint = if (viewModel.isRecordingAudio.value) Color.Red else MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
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

        if (viewModel.isRecordingVideo.value) {
            com.artbirwww.messenger.ui.components.VideoMessageRecorder(
                onVideoRecorded = { file ->
                    viewModel.sendVideoMessage(file)
                    viewModel.isRecordingVideo.value = false
                },
                onCancel = { viewModel.isRecordingVideo.value = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipientProfileDialog(
    user: User,
    messages: List<Message>,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Инфо", "Изображения", "Файлы")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text(text = "Профиль пользователя") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    com.artbirwww.messenger.ui.components.UserAvatar(
                        photoURL = user.photoURL,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = user.name, style = MaterialTheme.typography.headlineSmall)
                    Text(text = user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                TabRow(selectedTabIndex = activeTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            text = { Text(text = title) }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                    when (activeTab) {
                        0 -> { // Info
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                ProfileInfoRow("Телефон", user.phone.ifEmpty { "Не указан" })
                                ProfileInfoRow("Био", user.bio.ifEmpty { "—" })
                                ProfileInfoRow("Пол", when(user.gender) {
                                    "male" -> "Мужской"
                                    "female" -> "Женский"
                                    "other" -> "Другой"
                                    else -> "Не указан"
                                })
                            }
                        }
                        1 -> { // Images
                            val images = (messages.flatMap { it.imageUrls ?: emptyList() } + 
                                         messages.mapNotNull { it.imageUrl }).distinct()
                            
                            if (images.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(text = "Нет изображений")
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(images.size) { index: Int ->
                                        val url = images[index]
                                        AsyncImage(
                                            model = url,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                }
                            }
                        }
                        2 -> { // Files
                            val files = (messages.flatMap { it.files ?: emptyList() } +
                                        messages.mapNotNull { msg -> 
                                            if (msg.fileUrl != null && msg.fileName != null) {
                                                com.artbirwww.messenger.data.model.AttachmentFile(msg.fileUrl, msg.fileName, msg.fileType ?: "", msg.fileSize ?: 0)
                                            } else null
                                        }).distinctBy { it.url }
                            
                            if (files.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(text = "Нет файлов")
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(files.size) { index: Int ->
                                        val file = files[index]
                                        ListItem(
                                            headlineContent = { Text(text = file.name) },
                                            supportingContent = { Text(text = "${file.size / 1024} KB") },
                                            leadingContent = { Text(text = "📎", fontSize = 24.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column {
        Text(text = label)
        Text(text = value)
    }
}
