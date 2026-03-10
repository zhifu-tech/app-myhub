package tech.zhifu.app.myhub.feature.dashboard.topbar

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.navigateToReview
import tech.zhifu.app.myhub.feature.dashboard.resources.Res
import tech.zhifu.app.myhub.feature.dashboard.resources.feature_dashboard_good_evening

@Composable
internal fun TopBarRoute(
    viewModel: DashboardViewModel,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val topBarState = viewModel.collectFieldAsState { uiState ->
        (uiState as? DashboardUiState.Content)?.let {
            TopBarState(
                isRefreshing = it.isRefreshing,
                reviewCardsCount = it.reviewState?.reviewCardsCount ?: 0,
                showReviewEntrance = it.reviewState?.showFocusReview ?: false
            )
        }
    }.value ?: return

    TopBar(
        topBarState = topBarState,
        scrollBehavior = scrollBehavior,
        onStartReview = viewModel::navigateToReview,
        onRefresh = viewModel::refresh,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    topBarState: TopBarState,
    onStartReview: () -> Unit,
    onRefresh: () -> Unit,
) {

    LargeFlexibleTopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.feature_dashboard_good_evening),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        subtitle = {
            TopBarReviewEntrance(
                showReviewEntrance = topBarState.showReviewEntrance,
                reviewCardsCount = topBarState.reviewCardsCount,
                onStartReview = onStartReview
            )
        },
        navigationIcon = {},
        actions = {
            TopBarActions(
                isRefreshing = topBarState.isRefreshing,
                onRefresh = onRefresh
            )
        },
        scrollBehavior = scrollBehavior,
    )
}
