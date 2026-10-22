package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.FileUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerDialog(
    file: File,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var isBuffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("video_player_dialog"),
            color = Color.Black
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            text = file.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose, modifier = Modifier.testTag("video_close_button")) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close Video",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { FileUtils.shareFile(context, file) },
                            modifier = Modifier.testTag("video_share_button")
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share Video",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    )
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasError) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Unable to play this video codec or format.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray
                            )
                        }
                    } else {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    val mediaController = MediaController(ctx)
                                    mediaController.setAnchorView(this)
                                    setMediaController(mediaController)
                                    setVideoURI(Uri.fromFile(file))
                                    setOnPreparedListener { mp: MediaPlayer ->
                                        isBuffering = false
                                        mp.isLooping = false
                                        start()
                                    }
                                    setOnErrorListener { _, _, _ ->
                                        isBuffering = false
                                        hasError = true
                                        true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
