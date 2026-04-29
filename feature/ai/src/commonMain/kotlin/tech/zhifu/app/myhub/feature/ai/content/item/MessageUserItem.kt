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
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.component.MediaGalleryDialog
import tech.zhifu.app.myhub.component.media.component.MediaGridNine
import tech.zhifu.app.myhub.feature.ai.content.text
import tech.zhifu.app.myhub.feature.ai.model.CaptureMediaAsset
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.feature.ai.model.displayName
import tech.zhifu.app.myhub.feature.ai.model.isVideo
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_user_badge

@Composable
fun MessageUserItem(
    message: Message
) {
    var mediaInitialIndex by remember(message.id, message.mediaAssets) {
        mutableStateOf<Int?>(null)
    }
    val mediaItems = remember(message.id, message.mediaAssets) {
        message.mediaAssets.mapNotNull { asset ->
            if (asset.isMissing) null
            else asset.toMediaItem()
        }
    }

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
                Text(
                    text = message.text(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                )
                MediaGridNine(
                    items = mediaItems,
                    onItemClick = { index ->
                        mediaInitialIndex = index
                    },
                )
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

    mediaInitialIndex?.let {
        MediaGalleryDialog(
            items = mediaItems,
            initialIndex = it,
            onDismiss = { mediaInitialIndex = null },
        )
    }
}

private fun CaptureMediaAsset.toMediaItem(): MediaItem =
    MediaItem(
        id = sha256.ifBlank { storageHandle },
        name = displayName(),
        previewUrl = accessUrl.takeUnless { isMissing }.orEmpty(),
        isVideo = isVideo()
    )
