package tech.zhifu.app.myhub.feature.dashboard.content.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.resources.Res
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_asset_collections
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_curated_library
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_view_all
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger


@Composable
fun CollectionSectionRoute(
    viewModel: DashboardViewModel,
) {
    val payload = viewModel.collectFieldAsState { uiState ->
        (uiState as? DashboardUiState.ResultOutputCompleted)?.collectionSectionState
    }.value ?: return

    logger.debug { "AssetCollectionsModule + ${payload.hashCode()}" }

    val listState = rememberLazyListState()
    CollectionPagingLoadMoreEffect(
        listState = listState,
        hasMore = payload.hasMore,
        isLoadingMore = payload.isLoading,
        onLoadMore = viewModel::loadMoreCollections,
    )
    CollectionSection(
        listState = listState,
        collections = payload.collections,
        onCollectionClick = {/*TODO*/ },
        onViewAllClick = { /*TODO*/ },
        isLoadingMore = payload.isLoading,
        hasMore = payload.hasMore,
        modifier = Modifier.fillMaxWidth().padding(bottom = 36.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionSection(
    listState: LazyListState,
    collections: List<Collection>,
    onCollectionClick: (Collection) -> Unit,
    onViewAllClick: () -> Unit,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    modifier: Modifier = Modifier.Companion
) {
    logger.debug { "Loading AssetCollectionsModule -- 2" }
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.feature_dashboard_asset_collections),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                    tooltip = { PlainTooltip { Text(stringResource(Res.string.feature_dashboard_curated_library)) } },
                    state = rememberTooltipState(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            TextButton(
                onClick = onViewAllClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = stringResource(Res.string.feature_dashboard_view_all),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        val cardWidth = 280.dp
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(end = if (hasMore && isLoadingMore) 56.dp else 0.dp)
        ) {
            items(
                items = collections,
                key = { it.id },
                contentType = { "collection_card" }
            ) { collection ->
                CollectionCard(
                    collection = collection,
                    onClick = { onCollectionClick(collection) },
                    modifier = Modifier.width(cardWidth)
                )
            }
            if (hasMore && isLoadingMore) {
                item(
                    key = "collections_loading_more",
                    contentType = "loading"
                ) {
                    Box(
                        modifier = Modifier
                            .width(cardWidth)
                            .height(250.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionPagingLoadMoreEffect(
    listState: LazyListState,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
) {
    val currentHasMore by rememberUpdatedState(hasMore)
    val currentIsLoadingMore by rememberUpdatedState(isLoadingMore)
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)

    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val totalCount = listState.layoutInfo.totalItemsCount
            totalCount > 0 && lastVisibleIndex >= totalCount - 2
        }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if (shouldLoad && currentHasMore && !currentIsLoadingMore) {
                    currentOnLoadMore()
                }
            }
    }
}
