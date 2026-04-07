package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.selectedCard
import tech.zhifu.app.myhub.feature.preview.PreviewTransitionTokens
import tech.zhifu.app.myhub.feature.preview.sharedBounds
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
    val onClick: () -> Unit = remember(item) {
        { viewModel.selectedCard(item) }
    }
    val isSelected by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? DashboardUiState.Content)?.selectedCard === item
    }

    ContentItemHostContent(
        isSelected = isSelected,
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
    AnimatedVisibility(
        visible = isSelected.not(),
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = PreviewTransitionTokens.HOST_ENTER_DURATION_MS,
                easing = FastOutSlowInEasing,
            )
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = PreviewTransitionTokens.HOST_ENTER_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
            initialScale = PreviewTransitionTokens.HOST_SCALE_FROM,
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = PreviewTransitionTokens.HOST_EXIT_DURATION_MS,
                easing = FastOutSlowInEasing,
            )
        ) + scaleOut(
            animationSpec = tween(
                durationMillis = PreviewTransitionTokens.HOST_EXIT_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
            targetScale = PreviewTransitionTokens.HOST_SCALE_TO,
        ),
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
            content(
                this@AnimatedVisibility,
            )
        }
    }
}
