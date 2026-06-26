package com.artbirwww.messenger.ui.screens.profile

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

@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user = viewModel.userState.value
    val mediaGalleryState = viewModel.mediaUrls.collectAsState()
    val scrollState = rememberScrollState()

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
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserAvatar(
                photoURL = user.photoURL,
                modifier = Modifier.size(120.dp)
            )
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

            // Since it's inside a scrollable column, we might want to avoid nested scrolling or use a fixed height.
            // But LazyVerticalGrid doesn't work well directly inside scrollable Column unless its height is constrained.
            // For now, let's use a simple Column of Rows or just fixed height.
            
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
