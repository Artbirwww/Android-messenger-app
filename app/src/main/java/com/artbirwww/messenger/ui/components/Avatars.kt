package com.artbirwww.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun UserAvatar(photoURL: String?, modifier: Modifier = Modifier.size(48.dp)) {
    AsyncImage(
        model = if (photoURL.isNullOrEmpty()) "https://ui-avatars.com/api/?name=User&background=random" else photoURL,
        contentDescription = "Avatar",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
    )
}
