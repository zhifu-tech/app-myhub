package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import tech.zhifu.app.myhub.ui.model.ContentCard
import tech.zhifu.app.myhub.ui.model.toImageVector

@Composable
internal fun PreviewContentCover(
    modifier: Modifier,
    card: ContentCard,
) {
    val background = card.cover.background
    val coverUrl = card.cover.url
    val coverIcon = card.cover.iconKey?.toImageVector()
    val coverIconTint = card.cover.tint

    Box(
        modifier = modifier
            .aspectRatio(4f / 3f)
            .clip(shape = MaterialTheme.shapes.large)
            .background(color = background),
        contentAlignment = Alignment.Center,
    ) {
        if (coverUrl != null) {
            SubcomposeAsyncImage(
                model = coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        if (coverIcon != null && coverIconTint != null) {
            Icon(
                imageVector = coverIcon,
                contentDescription = null,
                tint = coverIconTint,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}
