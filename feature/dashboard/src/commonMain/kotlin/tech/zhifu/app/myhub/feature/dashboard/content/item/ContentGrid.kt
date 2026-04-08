package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.ui.model.ContentCard
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun ContentGrid(
    viewModel: DashboardViewModel,
    modifier: Modifier,
    paddingValues: PaddingValues,
) {
    val items by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? DashboardUiState.Content)?.items ?: emptyList()
    }
    val gridState = rememberLazyGridState()

    AutoLoadMoreGrid(
        gridState = gridState,
        totalCount = items.size,
        onLoadMore = viewModel::loadMore
    )

    ContentGridContent(
        items = items,
        modifier = modifier,
        paddingValues = paddingValues,
        gridState = gridState,
    ) { item ->
        ContentItemHost(
            item = item,
            modifier = Modifier.animateItem(),
            viewModel = viewModel
        ) { animatedVisibilityScope ->
            ContentGridItem(
                item = item,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    }
}

@Composable
internal fun ContentGridContent(
    items: List<ContentCard>,
    modifier: Modifier,
    paddingValues: PaddingValues,
    gridState: LazyGridState,
    contentItem: @Composable LazyGridItemScope.(ContentCard) -> Unit,
) {
    LazyVerticalGrid(
        modifier = modifier,
        contentPadding = paddingValues,
        columns = GridCells.Adaptive(minSize = 150.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        state = gridState,
    ) {
        items(
            items = items,
            key = { it.id },
            contentType = { "gridItem" },
            itemContent = contentItem,
        )
    }
}

@Composable
private fun AutoLoadMoreGrid(
    gridState: LazyGridState,
    totalCount: Int,
    onLoadMore: () -> Unit,
    triggerOffset: Int = 3,
) {
    LaunchedEffect(gridState, totalCount) {
        snapshotFlow {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= (totalCount - triggerOffset)
        }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if (shouldLoad && totalCount > 0) onLoadMore()
            }
    }
}
