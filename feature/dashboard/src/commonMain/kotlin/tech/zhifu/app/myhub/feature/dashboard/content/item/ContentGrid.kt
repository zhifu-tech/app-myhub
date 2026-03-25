package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import tech.zhifu.app.myhub.feature.preview.PreviewState

@Composable
fun ContentGridContent(
    modifier: Modifier,
    paddingValues: PaddingValues,
    items: List<ContentItem>,
    previewState: PreviewState,
    onLoadMore: () -> Unit,
    onClickItem: (ContentItem) -> Unit,
) {
    val gridState = rememberLazyGridState()
    AutoLoadMoreGrid(
        gridState = gridState,
        totalCount = items.size,
        onLoadMore = onLoadMore
    )
    LazyVerticalGrid(
        modifier = modifier.padding(top = 24.dp),
        contentPadding = paddingValues,
        columns = GridCells.Adaptive(minSize = 150.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        state = gridState,
    ) {
        items(
            items = items,
            key = { it.id },
            contentType = { "gridItem" }
        ) { item ->
            ContentItemHost(
                item = item,
                previewState = previewState,
                modifier = Modifier.animateItem(),
                onClickItem = onClickItem,
            ) { animatedVisibilityScope ->
                ContentGridItem(
                    item = item,
                )
            }
        }
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
