package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.MediaPreviewer
import tech.zhifu.app.myhub.component.media.component.MediaGalleryDialog
import tech.zhifu.app.myhub.component.media.component.MediaGridNine
import tech.zhifu.app.myhub.feature.ai.model.CaptureMediaAsset
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_user_badge

@Composable
fun MessageUserItem(
    message: Message
) {
    val mediaPreviewer: MediaPreviewer = koinInject()
    var previewingIndex by remember(message.id, message.mediaAssets) { mutableStateOf<Int?>(null) }
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
                        onPreview = { previewingIndex = it },
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

    previewingIndex?.let { index ->
        MediaGalleryDialog(
            items = message.mediaAssets.mapIndexed { mediaIndex, asset ->
                asset.toMediaItem(index = mediaIndex)
            },
            initialIndex = index,
            mediaPreviewer = mediaPreviewer,
            onDismiss = { previewingIndex = null },
        )
    }
}

@Composable
private fun UserMediaSection(
    mediaAssets: List<CaptureMediaAsset>,
    onPreview: (Int) -> Unit,
) {
    MediaGridNine(
        items = mediaAssets.mapIndexed { index, asset -> asset.toMediaItem(index) },
        onItemClick = { index ->
            if (!mediaAssets[index].isMissing) {
                onPreview(index)
            }
        },
    )
}

private fun CaptureMediaAsset.toMediaItem(index: Int = 0): MediaItem =
    MediaItem(
        id = sha256.ifBlank { storageHandle },
        name = displayName(),
        previewUrl = accessUrl.takeUnless { isMissing }.orEmpty(),
        isVideo = isVideo(),
    )

private fun CaptureMediaAsset.displayName(index: Int = 0): String =
    accessUrl.substringAfterLast('/').ifBlank {
        if (isVideo()) {
            "video-${index + 1}"
        } else {
            "image-${index + 1}"
        }
    }

private fun CaptureMediaAsset.isVideo(): Boolean =
    mediaType.startsWith("video/", ignoreCase = true)
