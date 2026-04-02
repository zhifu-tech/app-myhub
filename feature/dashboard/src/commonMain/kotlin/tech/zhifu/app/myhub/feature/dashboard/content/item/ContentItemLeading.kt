package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import tech.zhifu.app.myhub.ui.model.ContentCard
import tech.zhifu.app.myhub.ui.model.toImageVector

@Composable
fun ContentItemLeading(
    item: ContentCard,
    modifier: Modifier = Modifier,
) {
    val cover = item.cover
    val coverUrl = cover.url
    val coverIcon = cover.iconKey?.toImageVector()
    val coverIconTint = cover.tint
    Box(
        modifier = modifier
            .background(color = cover.background),
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
