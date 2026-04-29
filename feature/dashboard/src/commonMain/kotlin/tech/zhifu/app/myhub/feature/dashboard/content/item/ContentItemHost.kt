package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.datastore.model.domain.CardStatus
import tech.zhifu.app.myhub.datastore.model.domain.ContentCard
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.preview.PreviewState
import tech.zhifu.app.myhub.feature.preview.sharedBounds
import tech.zhifu.app.myhub.feature.preview.util.PreviewAnimatedVisibility
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.navigateToAiCapture

@Composable
fun ContentItemHost(
    item: ContentCard,
    modifier: Modifier,
    viewModel: DashboardViewModel,
    content: @Composable (
        AnimatedVisibilityScope,
        onOpenMediaPreview: (Int) -> Unit,
    ) -> Unit,
) {
    val hostState by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? DashboardUiState.Content)?.let { content ->
            HostState(
                previewState = content.previewState,
                items = content.items,
            )
        }
    }
    val previewState = hostState?.previewState
    val items = hostState?.items?.toList().orEmpty()
    val onCardClick: () -> Unit = remember(item, previewState, items) {
        {
            if (item.card.status == CardStatus.DRAFT) {
                viewModel.navigateToAiCapture()
            } else {
                previewState?.show(item, items)
            }
        }
    }
    val onMediaClick: (Int) -> Unit = remember(item, previewState, items) {
        { mediaIndex ->
            if (item.medias.isNotEmpty()) {
                previewState?.showMedia(
                    card = item,
                    mediaIndex = mediaIndex,
                    deck = items,
                    allowCrossCardNavigation = true,
                )
            } else {
                previewState?.show(item, items)
            }
        }
    }
    ContentItemHostContent(
        isSelected = previewState?.card?.value === item,
        modifier = modifier,
        item = item,
        onOpenCardPreview = onCardClick,
        onOpenMediaPreview = onMediaClick,
        content = content,
    )
}

@Composable
internal fun ContentItemHostContent(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    item: ContentCard,
    onOpenCardPreview: () -> Unit,
    onOpenMediaPreview: (Int) -> Unit,
    content: @Composable (
        AnimatedVisibilityScope,
        onOpenMediaPreview: (Int) -> Unit,
    ) -> Unit,
) {
    PreviewAnimatedVisibility(
        visible = isSelected.not(),
        modifier = modifier,
    ) {
        ElevatedCard(
            onClick = onOpenCardPreview,
            modifier = Modifier
                .sharedBounds(
                    key = "content-preview-${item.card.id}",
                    animatedVisibilityScope = this,
                    overlayClipShape = MaterialTheme.shapes.extraLarge,
                )
                .clip(MaterialTheme.shapes.extraLarge),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 5.dp,
                pressedElevation = 8.dp,
                focusedElevation = 6.dp,
                hoveredElevation = 7.dp,
            ),
        ) {
            content(
                this@PreviewAnimatedVisibility,
                onOpenMediaPreview,
            )
        }
    }
}

private data class HostState(
    val previewState: PreviewState,
    val items: List<ContentCard>,
)
