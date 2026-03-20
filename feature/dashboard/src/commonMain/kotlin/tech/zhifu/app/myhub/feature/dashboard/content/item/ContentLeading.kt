package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage

@Composable
fun ContentItemLeading(
    item: ContentItem,
    size: Dp?,
    iconSize: Dp,
    shape: CornerBasedShape,
    modifier: Modifier = Modifier,
) {
    val containerModifier = modifier.then(
        if (size != null) {
            Modifier.size(size).clip(shape)
        } else {
            Modifier.fillMaxSize().clip(shape)
        }
    )
    when (val cover = item.cover) {
        is ContentItemCover.Icon -> {
            val background = if (size == null) {
                cover.background.copy(alpha = 0.5f)
            } else {
                cover.background
            }
            Box(
                modifier = containerModifier
                    .background(color = background),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = cover.icon,
                    contentDescription = null,
                    tint = cover.tint,
                    modifier = Modifier.size(iconSize),
                )
            }
        }

        is ContentItemCover.Image -> {
            Box(
                modifier = containerModifier
                    .background(color = cover.placeholder),
            ) {
                SubcomposeAsyncImage(
                    model = cover.url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                if (cover.isVideo) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        val playIconSize = if (size != null) 20.dp else 30.dp
                        Icon(
                            imageVector = Icons.Outlined.PlayCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(playIconSize),
                        )
                    }
                }
            }
        }
    }
}
