package tech.zhifu.app.myhub.feature.dashboard.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import tech.zhifu.app.myhub.component.card.CardPreview
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.card.LatestCapturesTitleRow
import tech.zhifu.app.myhub.feature.dashboard.content.card.NewCaptureCard
import tech.zhifu.app.myhub.feature.dashboard.resultCompletedPayload
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.LocalWindowSizeClass
import tech.zhifu.app.myhub.ui.isWidthCompact
import tech.zhifu.app.myhub.ui.isWidthExpanded
import tech.zhifu.app.myhub.ui.isWidthLarge
import tech.zhifu.app.myhub.ui.isWidthMedium

@Composable
fun DashboardGridContentRoute(
    viewModel: DashboardViewModel,
    collectionSection: @Composable () -> Unit,
) {
    logger.debug("ResultOutputCompleted") {
        "ResultOutputCompleted -- DashboardGridContentRoute"
    }
    val sizeClass = LocalWindowSizeClass.current
    val columns = when {
        sizeClass.isWidthCompact() -> 1
        sizeClass.isWidthMedium() -> 2
        sizeClass.isWidthExpanded() -> 3
        sizeClass.isWidthLarge() -> 4
        else -> 3
    }

    val state = viewModel.collectFieldAsState { uiState ->
        uiState.resultCompletedPayload?.cardSectionState
    }.value ?: return

    val gridState = rememberLazyStaggeredGridState()
    CardPagingLoadMoreEffect(
        gridState = gridState,
        hasMore = state.hasMore,
        isLoadingMore = state.isLoading,
        onLoadMore = viewModel::loadMoreCards,
    )
    DashboardGridContent(
        gridState = gridState,
        columns = columns,
        collectionSection = collectionSection,
        cards = state.cards,
        hasMoreCards = state.hasMore,
        isLoadingMoreCards = state.isLoading,
        onEditCard = viewModel::editCard,
        onNavigateToCardDetail = viewModel::navigateToCardDetail,
        onNavigateToCapture = viewModel::navigateToCapture,
    )
}


@Composable
private fun DashboardGridContent(
    gridState: LazyStaggeredGridState,
    columns: Int,
    collectionSection: @Composable () -> Unit,
    cards: List<Card>,
    hasMoreCards: Boolean,
    isLoadingMoreCards: Boolean,
    onEditCard: (String) -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    onNavigateToCapture: () -> Unit,
) {
    logger.debug("ResultOutputCompleted") {
        "ResultOutputCompleted -- DashboardGridContent"
    }
    LazyVerticalStaggeredGrid(
        state = gridState,
        columns = StaggeredGridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 32.dp,
            end = 32.dp,
            top = 16.dp,
            bottom = 16.dp
        ),
        verticalItemSpacing = 20.dp,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(
            key = "collection_section",
            contentType = "header_collection",
            span = StaggeredGridItemSpan.FullLine
        ) {
            collectionSection()
        }
        item(
            key = "latest_captures_title",
            contentType = "header_title",
            span = StaggeredGridItemSpan.FullLine
        ) {
            LatestCapturesTitleRow(
                onViewAllClick = { /* TODO */ },
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        items(
            items = cards,
            key = { it.id },
            contentType = { "card_preview" }
        ) { card ->
            CardPreview(
                card = card,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigateToCardDetail(card.id) },
                onEdit = { onEditCard(card.id) }
            )
        }
        if (hasMoreCards && isLoadingMoreCards) {
            item(
                key = "cards_loading_more",
                contentType = "loading",
                span = StaggeredGridItemSpan.FullLine
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
        item(
            key = "new_capture_card",
            contentType = "entry_new_capture",
            span = StaggeredGridItemSpan.SingleLane
        ) {
            NewCaptureCard(
                onClick = onNavigateToCapture,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CardPagingLoadMoreEffect(
    gridState: LazyStaggeredGridState,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
) {
    val currentHasMore by rememberUpdatedState(hasMore)
    val currentIsLoadingMore by rememberUpdatedState(isLoadingMore)
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)

    LaunchedEffect(key1 = gridState) {
        snapshotFlow {
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val totalCount = gridState.layoutInfo.totalItemsCount
            totalCount > 0 && lastVisibleIndex >= totalCount - 3
        }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if (shouldLoad && currentHasMore && !currentIsLoadingMore) {
                    currentOnLoadMore()
                }
            }
    }
}
