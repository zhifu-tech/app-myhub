package tech.zhifu.app.myhub.feature.dashboard.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.collection.CollectionSectionRoute
import tech.zhifu.app.myhub.feature.dashboard.content.review.ReviewSectionRoute

@Composable
fun ContentRoute(
    viewModel: DashboardViewModel,
    modifier: Modifier,
) {
    val isRefreshing = viewModel.collectFieldAsState { uiState ->
        (uiState as? DashboardUiState.Content)?.isRefreshing
    }.value ?: return

    Content(
        modifier = modifier,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        gridContent = {
            ContentGridContentRoute(
                viewModel = viewModel,
                collectionSection = {
                    CollectionSectionRoute(viewModel = viewModel)
                },
            )
        },
        reviewSection = { modifier ->
            ReviewSectionRoute(
                viewModel = viewModel,
                modifier = modifier,
            )
        }
    )
}

@Composable
internal fun Content(
    modifier: Modifier,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    gridContent: @Composable () -> Unit,
    reviewSection: @Composable (Modifier) -> Unit,
) {
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            gridContent()
            reviewSection(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        start = 32.dp,
                        end = 32.dp,
                        top = 16.dp
                    )
                    .zIndex(1f)
            )
        }
    }
}
