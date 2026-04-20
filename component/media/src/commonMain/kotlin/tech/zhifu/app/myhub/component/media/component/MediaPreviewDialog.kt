package tech.zhifu.app.myhub.component.media.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import coil3.compose.SubcomposeAsyncImage
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.MediaPreviewer
import tech.zhifu.app.myhub.component.media.util.toPlayableUrl

@Composable
fun MediaPreviewDialog(
    item: MediaItem,
    mediaPreviewer: MediaPreviewer,
    onDismiss: () -> Unit
) {
    val mediaUrl = remember(item.file) { item.file.toPlayableUrl() }
    val shouldFallbackToSystemPlayer = item.isVideo &&
        (mediaPreviewer.isSystemPlayerPreferred() || mediaUrl.isBlank())

    if (shouldFallbackToSystemPlayer) {
        LaunchedEffect(item) {
            mediaPreviewer.openInSystemPlayer(item)
            onDismiss()
        }
        MediaPreviewFallbackOverlay()
        return
    }

    when {
        item.isVideo -> MediaPreviewDialogShell(onDismiss = onDismiss) {
            VideoContent(mediaUrl = mediaUrl)
        }

        else -> MediaPreviewDialogShell(onDismiss = onDismiss) {
            ImageContent(item = item)
        }
    }
}

@Composable
private fun MediaPreviewDialogShell(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismiss() })
                }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.85f)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {})
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    content()
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoContent(
    mediaUrl: String
) {
    val playerHost = remember(mediaUrl) {
        MediaPlayerHost(
            mediaUrl = mediaUrl,
            autoPlay = true,
            isLooping = true
        )
    }
    VideoPlayerComposable(
        modifier = Modifier.fillMaxSize(),
        playerHost = playerHost,
        playerConfig = VideoPlayerConfig(
            isFullScreenEnabled = false,
            enableBackButton = false
        )
    )
}

@Composable
private fun ImageContent(
    item: MediaItem
) {
    val imageModel = remember(item.file) {
        item.file.toPlayableUrl().ifBlank { item.file.toString() }
    }
    SubcomposeAsyncImage(
        model = imageModel,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        },
        error = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.name.ifBlank { "Media preview unavailable" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
    )
}

@Composable
private fun MediaPreviewFallbackOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Opening system player…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
