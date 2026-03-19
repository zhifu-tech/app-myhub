package tech.zhifu.app.myhub.feature.dashboard.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.item.ContentGridItem
import tech.zhifu.app.myhub.feature.dashboard.content.item.ContentItem
import tech.zhifu.app.myhub.feature.dashboard.content.item.ContentListItem
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.collectUserPreferencesFieldState
import tech.zhifu.app.myhub.util.tapToClearFocus

@Composable
fun Content(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel,
    paddingValues: PaddingValues,
) {
    val items = viewModel.collectFieldAsState {
        (it as? DashboardUiState.Content)?.contentItems
    }.value ?: emptyList()

    if (items.isEmpty()) {
        Empty(viewModel = viewModel, modifier = modifier)
        return
    }

    val layoutAsList = viewModel.collectUserPreferencesFieldState {
        it.layoutAsList
    }.value ?: true


    ContentContent(
        modifier = modifier,
        paddingValues = paddingValues,
        items = items,
        layoutAsList = layoutAsList,
        onLoadMore = viewModel::loadMoreData,
        onClickItem = {
            // viewModel.navigateToAppDetail(it)
        }
    )
}

@Composable
private fun ContentContent(
    modifier: Modifier,
    paddingValues: PaddingValues,
    items: List<ContentItem>,
    layoutAsList: Boolean,
    onLoadMore: () -> Unit,
    onClickItem: (ContentItem) -> Unit,
) {
    val modifier = modifier
        .fillMaxSize()
        .tapToClearFocus()
        .testTag("content-list")
        .padding(horizontal = 16.dp)
        .background(color = MaterialTheme.colorScheme.background)
    if (layoutAsList) {
        val listState = rememberLazyListState()
        AutoLoadMoreList(
            listState = listState,
            totalCount = items.size,
            onLoadMore = onLoadMore
        )
        LazyColumn(
            modifier = modifier,
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            state = listState,
        ) {
            items(items, key = { it.id }) { item ->
                ContentListItem(item = item, onClick = { onClickItem(item) })
            }
        }
    } else {
        val gridState = rememberLazyGridState()
        AutoLoadMoreGrid(
            gridState = gridState,
            totalCount = items.size,
            onLoadMore = onLoadMore
        )
        LazyVerticalGrid(
            modifier = modifier,
            contentPadding = paddingValues,
            columns = GridCells.Adaptive(minSize = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            state = gridState,
        ) {
            items(items, key = { it.id }) { item ->
                ContentGridItem(item = item, onClick = { onClickItem(item) })
            }
        }
    }
}

@Composable
private fun AutoLoadMoreList(
    listState: LazyListState,
    totalCount: Int,
    onLoadMore: () -> Unit,
    triggerOffset: Int = 3,
) {
    LaunchedEffect(listState, totalCount) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= (totalCount - triggerOffset)
        }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if (shouldLoad && totalCount > 0) onLoadMore()
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
