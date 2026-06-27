package com.artbirwww.messenger.ui.components

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.Executor

@Composable
fun VideoMessageRecorder(
    onVideoRecorded: (File) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = ContextCompat.getMainExecutor(context)
    
    val previewView = remember { PreviewView(context) }
    val videoCapture = remember {
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.SD))
            .build()
        VideoCapture.withOutput(recorder)
    }
    
    var recording: Recording? by remember { mutableStateOf(null) }
    var isRecording by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.previewSurfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    videoCapture
                )
            } catch (e: Exception) {
                Log.e("CameraX", "Binding failed", e)
            }
        }, executor)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel, modifier = Modifier.background(Color.White, CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Black)
                }
                
                IconButton(
                    onClick = {
                        if (isRecording) {
                            recording?.stop()
                            isRecording = false
                        } else {
                            val file = File(context.cacheDir, "video_msg_${System.currentTimeMillis()}.mp4")
                            val outputOptions = FileOutputOptions.Builder(file).build()
                            
                            recording = videoCapture.output
                                .prepareRecording(context, outputOptions)
                                .withAudioEnabled()
                                .start(executor) { event ->
                                    if (event is VideoRecordEvent.Finalize) {
                                        if (!event.hasError()) {
                                            onVideoRecorded(file)
                                        }
                                    }
                                }
                            isRecording = true
                        }
                    },
                    modifier = Modifier.size(72.dp).background(if (isRecording) Color.Red else Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.RadioButtonChecked,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (isRecording) Color.White else Color.Red
                    )
                }
            }
        }
    }
}
