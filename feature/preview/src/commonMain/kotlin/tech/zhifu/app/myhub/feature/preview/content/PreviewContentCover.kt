package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.component.MediaGridNine
import tech.zhifu.app.myhub.datastore.model.domain.ContentCard
import tech.zhifu.app.myhub.feature.preview.PreviewState
import tech.zhifu.app.myhub.feature.preview.PreviewTokens

@Composable
internal fun PreviewContentCover(
    modifier: Modifier,
    card: ContentCard,
    previewState: PreviewState,
) {
    Box(
        modifier = modifier
            .aspectRatio(4f / 3f)
            .clip(shape = PreviewTokens.CoverShape)
            .background(color = MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (card.medias.isNotEmpty()) {
            MediaGridNine(
                items = card.medias.map { media ->
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
                onItemClick = { index ->
                    previewState.showMedia(
                        card = card,
                        mediaIndex = index,
                        deck = listOf(card),
                        allowCrossCardNavigation = false,
                    )
                },
            )
        }
    }
}
