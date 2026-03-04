package tech.zhifu.app.myhub.feature.dashboard.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.collection.CollectionSectionRoute
import tech.zhifu.app.myhub.feature.dashboard.content.review.ReviewSectionRoute
import tech.zhifu.app.myhub.feature.dashboard.resultCompletedPayload
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

@Composable
fun ResultOutputCompletedRoute(
    viewModel: DashboardViewModel,
    innerPadding: PaddingValues,
) {
    val isRefreshing = viewModel.collectFieldAsState { uiState ->
        uiState.resultCompletedPayload?.isRefreshing
    }.value ?: return

    ResultOutputCompleted(
        innerPadding = innerPadding,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        gridContent = {
            DashboardGridContentRoute(
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
private fun ResultOutputCompleted(
    innerPadding: PaddingValues,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    gridContent: @Composable () -> Unit,
    reviewSection: @Composable (Modifier) -> Unit,
) {
    logger.debug("ResultOutputCompleted") {
        "ResultOutputCompleted --ResultOutputCompleted  2"
    }
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
