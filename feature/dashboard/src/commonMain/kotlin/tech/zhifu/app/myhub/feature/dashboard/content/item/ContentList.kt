package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import tech.zhifu.app.myhub.feature.preview.PreviewState

@Composable
fun ContentListContent(
    modifier: Modifier,
    paddingValues: PaddingValues,
    items: List<ContentItem>,
    onLoadMore: () -> Unit,
    previewState: PreviewState,
    onClickItem: (ContentItem) -> Unit,
) {
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
            ContentItemHost(
                item = item,
                previewState = previewState,
            ) { modifier ->
                ContentListItem(
                    item = item,
                    previewState = previewState,
                    modifier = modifier,
                    onClick = { onClickItem(item) }
                )
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
