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
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.preview.sharedBounds
import tech.zhifu.app.myhub.feature.preview.util.PreviewAnimatedVisibility
import tech.zhifu.app.myhub.ui.model.ContentCard
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun ContentItemHost(
    item: ContentCard,
    modifier: Modifier,
    viewModel: DashboardViewModel,
    content: @Composable (AnimatedVisibilityScope) -> Unit,
) {
    val previewState by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? DashboardUiState.Content)?.previewState
    }
    val onClick: () -> Unit = remember(item, previewState) {
        { previewState?.show(item) }
    }
    ContentItemHostContent(
        isSelected = previewState?.card?.value === item,
        modifier = modifier,
        onClick = onClick,
        item = item,
        content = content
    )
}

@Composable
internal fun ContentItemHostContent(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    item: ContentCard,
    content: @Composable (AnimatedVisibilityScope) -> Unit,
) {
    PreviewAnimatedVisibility(
        visible = isSelected.not(),
        modifier = modifier,
    ) {
        ElevatedCard(
            onClick = onClick,
            modifier = Modifier
                .sharedBounds(
                    key = "content-preview-${item.id}",
                    animatedVisibilityScope = this,
                    overlayClipShape = MaterialTheme.shapes.large,
                )
                .clip(MaterialTheme.shapes.large),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp,
            ),
        ) {
            content(this@PreviewAnimatedVisibility)
        }
    }
}
