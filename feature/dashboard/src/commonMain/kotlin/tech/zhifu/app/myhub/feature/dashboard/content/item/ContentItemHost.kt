package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.preview.PreviewState
import tech.zhifu.app.myhub.feature.preview.sharedBounds

@Composable
fun ContentItemHost(
    item: ContentItem,
    previewState: PreviewState,
    modifier: Modifier,
    onClickItem: (ContentItem) -> Unit,
    content: @Composable (AnimatedVisibilityScope) -> Unit,
) {
    val onClick: () -> Unit = remember {
        { onClickItem(item) }
    }
    AnimatedVisibility(
        visible = previewState.payload?.id != item.id,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier,
    ) {
        ElevatedCard(
            onClick = onClick,
            modifier = Modifier
                .sharedBounds(
                    key = "content-preview-${item.id}",
                    animatedVisibilityScope = this,
                )
                .clip(MaterialTheme.shapes.large),
            enabled = true,
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp,
            ),
        ) {
            content(
                this@AnimatedVisibility,
            )
        }
    }
}
