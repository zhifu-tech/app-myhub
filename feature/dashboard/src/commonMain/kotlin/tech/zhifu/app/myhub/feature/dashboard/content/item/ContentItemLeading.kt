package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.component.MediaGridNine
import tech.zhifu.app.myhub.datastore.model.domain.ContentCard

@Composable
fun ContentItemLeading(
    item: ContentCard,
    onOpenMediaPreview: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (item.medias.isNotEmpty()) {
            MediaGridNine(
                items = item.medias.map { media ->
                    MediaItem(
                        id = media.id,
                        name = media.accessUrl.substringAfterLast('/'),
                        previewUrl = media.accessUrl,
                        isVideo = media.mediaType.startsWith(
                            prefix = "video/",
                            ignoreCase = true
                        ),
                        thumbnailUrl = media.thumbAccessUrl,
                    )
                },
                modifier = Modifier.fillMaxSize(),
                onItemClick = onOpenMediaPreview,
            )
        }
    }
}
