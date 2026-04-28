package tech.zhifu.app.myhub.component.media.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import coil3.compose.SubcomposeAsyncImage
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.MediaPreviewer
import tech.zhifu.app.myhub.component.media.displayImageModel
import tech.zhifu.app.myhub.component.media.displayMediaUrl

@Composable
internal fun MediaPreviewPage(
    item: MediaItem,
) {
    val mediaPreviewer: MediaPreviewer = koinInject()
    val mediaUrl = remember(item.previewUrl, item.file) {
        item.displayMediaUrl()
    }
    val shouldFallbackToSystemPlayer = item.isVideo &&
        (mediaPreviewer.isSystemPlayerPreferred() || mediaUrl.isBlank())

    when {
        shouldFallbackToSystemPlayer -> {
            LaunchedEffect(item) {
                mediaPreviewer.openInSystemPlayer(item)
            }
            MediaPreviewFallbackOverlay()
        }

        item.isVideo -> VideoContent(mediaUrl = mediaUrl)
        else -> ImageContent(item = item)
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
    val imageModel = remember(item.previewUrl, item.thumbnailUrl, item.file) {
        item.displayImageModel()
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
