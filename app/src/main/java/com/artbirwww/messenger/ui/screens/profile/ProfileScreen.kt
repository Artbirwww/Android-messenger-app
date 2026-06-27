package com.artbirwww.messenger.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.artbirwww.messenger.R
import com.artbirwww.messenger.ui.components.ActionButton
import com.artbirwww.messenger.ui.components.PrimaryInputField
import com.artbirwww.messenger.ui.components.UserAvatar
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user = viewModel.userState.value
    val mediaGalleryState = viewModel.mediaUrls.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val inputStream: java.io.InputStream? = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            val fileName = "avatar_${System.currentTimeMillis()}.jpg"
            if (bytes != null) {
                // We'll use GitHubService to upload avatar
                kotlinx.coroutines.MainScope().launch {
                    val url = com.artbirwww.messenger.data.remote.GitHubService.uploadFile(
                        bytes, fileName, "avatar", user?.uid ?: ""
                    )
                    if (url != null) {
                        viewModel.updateAvatar(url)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Настройки") })
        },
        bottomBar = {
            com.artbirwww.messenger.ui.components.MessengerBottomBar(
                currentRoute = "profile",
                onNavigate = onNavigateToRoute
            )
        }
    ) { paddingValues ->
        if (user != null) {
            var firstName by remember { mutableStateOf(user.firstName) }
            var lastName by remember { mutableStateOf(user.lastName) }
            var bio by remember { mutableStateOf(user.bio) }
            var phone by remember { mutableStateOf(user.phone) }
            var birthday by remember { mutableStateOf(user.birthday) }
            var gender by remember { mutableStateOf(user.gender) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserAvatar(
                    photoURL = user.photoURL,
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { avatarPickerLauncher.launch("image/*") }) {
                        Text("Сменить фото")
                    }
                    if (!user.photoURL.isNullOrEmpty()) {
                        TextButton(onClick = { viewModel.updateAvatar("") }) {
                            Text("Удалить", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Редактировать профиль",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                PrimaryInputField(value = firstName, onValueChange = { firstName = it }, hint = "Имя")
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryInputField(value = lastName, onValueChange = { lastName = it }, hint = "Фамилия")
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryInputField(value = bio, onValueChange = { bio = it }, hint = "О себе")
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryInputField(value = phone, onValueChange = { phone = it }, hint = "Телефон")
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryInputField(value = birthday, onValueChange = { birthday = it }, hint = "Дата рождения (ГГГГ-ММ-ДД)")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ActionButton(
                    text = if (viewModel.isSaving.value) "Сохранение..." else "Сохранить профиль",
                    enabled = !viewModel.isSaving.value
                ) {
                    viewModel.saveProfile(firstName, lastName, bio, phone, birthday, gender)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        viewModel.signOut()
                        onNavigateToLogin()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Выйти из аккаунта")
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Раздел Медиагалереи
                Text(
                    text = "Медиа из чатов",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.height(300.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mediaGalleryState.value) { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "Media item",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
