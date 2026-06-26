package com.artbirwww.messenger.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artbirwww.messenger.data.model.Message
import coil.compose.AsyncImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    currentUserId: String,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val isOutgoing = message.fromId == currentUserId
    val alignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isOutgoing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isOutgoing) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOutgoing) 16.dp else 4.dp,
                        bottomEnd = if (isOutgoing) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(12.dp)
        ) {
            // Reply Preview
            message.replyTo?.let { reply ->
                Column(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .background(contentColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = reply.fromName ?: "Reply",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isOutgoing) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = reply.text,
                        fontSize = 12.sp,
                        maxLines = 2,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            // Single Image
            if (!message.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = message.imageUrl,
                    contentDescription = "Attachment",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .padding(bottom = 6.dp)
                )
            }

            // Multiple Images
            message.imageUrls?.forEach { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Attachment",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .padding(bottom = 6.dp)
                )
            }

            // Multiple Files
            message.files?.forEach { file ->
                Row(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .background(contentColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📎", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = file.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = contentColor)
                        Text(text = "${file.size / 1024} KB", fontSize = 11.sp, color = contentColor.copy(alpha = 0.7f))
                    }
                }
            }

            if (message.text.isNotEmpty()) {
                Text(text = message.text, fontSize = 16.sp, color = contentColor)
            }

            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (message.editedAt != null) {
                    Text(
                        text = "(edited)",
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Italic,
                        color = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                
                // Time
                val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
                Text(
                    text = time,
                    fontSize = 10.sp,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }

            if (message.reactions.isNotEmpty()) {
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    message.reactions.forEach { (emoji, count) ->
                        Text(text = "$emoji $count", fontSize = 12.sp, color = contentColor, modifier = Modifier.padding(end = 6.dp))
                    }
                }
            }
        }
    }
}
