package com.artbirwww.messenger.ui.components

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun AudioPlayer(url: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableLongStateOf(0L) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    duration = exoPlayer.duration
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            progress = exoPlayer.currentPosition.toFloat() / (if (duration > 0) duration else 1).toFloat()
            kotlinx.coroutines.delay(500)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
        }) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null
            )
        }
        Slider(
            value = progress,
            onValueChange = {
                progress = it
                exoPlayer.seekTo((it * duration).toLong())
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(UnstableApi::class, ExperimentalFoundationApi::class)
@Composable
fun VideoCirclePlayer(url: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
        }
    }

    Box(
        modifier = Modifier
            .size(240.dp)
            .clip(CircleShape)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = {
                // it.player = exoPlayer
            }
        )
        
        var isPlaying by remember { mutableStateOf(false) }
        DisposableEffect(exoPlayer) {
            val listener = object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
            }
            exoPlayer.addListener(listener)
            onDispose { 
                exoPlayer.removeListener(listener)
                exoPlayer.release() 
            }
        }

        if (!isPlaying) {
            IconButton(
                onClick = { exoPlayer.play() },
                modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
            }
        } else {
            // Invisible overlay to pause
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        onClick = { exoPlayer.pause() },
                        onLongClick = { /* Maybe full screen? */ }
                    )
            )
        }
    }
}
