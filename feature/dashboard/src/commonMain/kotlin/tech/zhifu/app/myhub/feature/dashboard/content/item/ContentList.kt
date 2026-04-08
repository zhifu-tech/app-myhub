package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
fun ContentList(
    viewModel: DashboardViewModel,
    modifier: Modifier,
    paddingValues: PaddingValues,
) {
    val items by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? DashboardUiState.Content)?.items ?: emptyList()
    }
    val listState = rememberLazyListState()

    AutoLoadMoreList(
        listState = listState,
        totalCount = items.size,
        onLoadMore = viewModel::loadMore,
    )

    ContentListContent(
        items = items,
        modifier = modifier,
        paddingValues = paddingValues,
        listState = listState,
    ) { item ->
        ContentItemHost(
            item = item,
            modifier = Modifier.animateItem(),
            viewModel = viewModel
        ) { animatedVisibilityScope ->
            ContentListItem(
                item = item,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    }
}

@Composable
internal fun ContentListContent(
    items: List<ContentCard>,
    modifier: Modifier,
    paddingValues: PaddingValues,
    listState: LazyListState,
    contentItem: @Composable LazyItemScope.(ContentCard) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        state = listState,
    ) {
        items(
            items = items,
            key = { it.id },
            contentType = { "listItem" },
            itemContent = contentItem,
        )
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
