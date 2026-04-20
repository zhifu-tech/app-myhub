package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import io.github.vinceglb.filekit.PlatformFile
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.MediaPreviewer
import tech.zhifu.app.myhub.component.media.component.MediaPreviewDialog
import tech.zhifu.app.myhub.component.media.util.toPlayableUrl
import tech.zhifu.app.myhub.feature.ai.model.CaptureMediaAsset
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_user_badge

@Composable
fun MessageUserItem(
    message: Message
) {
    val mediaPreviewer: MediaPreviewer = koinInject()
    var previewingAsset by remember(message.id) { mutableStateOf<CaptureMediaAsset?>(null) }
    val text = message.textRes
        ?.let { res ->
            stringResource(
                resource = res,
                formatArgs = message.textArgs.toTypedArray(),
            )
        }
        ?: message.text
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            modifier = Modifier
                .weight(1f, fill = false)
                .widthIn(max = 520.dp),
            shape = RoundedCornerShape(
                topStart = 22.dp,
                topEnd = 6.dp,
                bottomStart = 22.dp,
                bottomEnd = 22.dp,
            ),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                text
                    .trim()
                    .takeIf { it.isNotBlank() }
                    ?.let { content ->
                        Text(
                            text = content,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                if (message.mediaAssets.isNotEmpty()) {
                    UserMediaSection(
                        mediaAssets = message.mediaAssets,
                        onPreview = { previewingAsset = it },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .size(width = 38.dp, height = 38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.feature_ai_user_badge),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }

    previewingAsset?.let { asset ->
        MediaPreviewDialog(
            item = asset.toMediaItem(),
            mediaPreviewer = mediaPreviewer,
            onDismiss = { previewingAsset = null },
        )
    }
}

@Composable
private fun UserMediaSection(
    mediaAssets: List<CaptureMediaAsset>,
    onPreview: (CaptureMediaAsset) -> Unit,
) {
    val single = mediaAssets.size == 1
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        mediaAssets.forEachIndexed { index, asset ->
            val shape = RoundedCornerShape(16.dp)
            val thumbWidth = if (single) 220.dp else 128.dp
            Box(
                modifier = Modifier
                    .width(thumbWidth)
                    .height(if (single) 168.dp else 128.dp)
                    .clip(shape)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f),
                        shape = shape,
                    )
                    .clickable { onPreview(asset) }
            ) {
                if (asset.isVideo()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Outlined.PlayCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(30.dp),
                            )
                            Text(
                                text = asset.displayName(index),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                } else {
                    SubcomposeAsyncImage(
                        model = asset.localUri.toPlayableUrl(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            MediaPlaceholder(
                                label = asset.displayName(index),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        },
                        error = {
                            MediaPlaceholder(
                                label = asset.displayName(index),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaPlaceholder(
    label: String,
    contentColor: Color,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}

private fun CaptureMediaAsset.toMediaItem(): MediaItem =
    MediaItem(
        id = sha256.ifBlank { localUri },
        file = PlatformFile(localUri.removePrefix("file://")),
        name = displayName(),
        isVideo = isVideo(),
    )

private fun CaptureMediaAsset.displayName(index: Int = 0): String =
    localUri.substringAfterLast('/').ifBlank {
        if (isVideo()) {
            "video-${index + 1}"
        } else {
            "image-${index + 1}"
        }
    }

private fun CaptureMediaAsset.isVideo(): Boolean =
    mediaType.startsWith("video/", ignoreCase = true)
