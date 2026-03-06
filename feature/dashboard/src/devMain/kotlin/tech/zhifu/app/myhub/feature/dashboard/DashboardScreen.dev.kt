package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import tech.zhifu.app.myhub.feature.dashboard.content.DashboardGridContent
import tech.zhifu.app.myhub.feature.dashboard.content.InitGlobalPending
import tech.zhifu.app.myhub.feature.dashboard.content.ResultErrorDisabled
import tech.zhifu.app.myhub.feature.dashboard.content.ResultOutputCompleted
import tech.zhifu.app.myhub.feature.dashboard.content.collection.CollectionSection
import tech.zhifu.app.myhub.feature.dashboard.topbar.TopBar
import tech.zhifu.app.myhub.feature.dashboard.topbar.TopBarState
import tech.zhifu.app.myhub.theme.AppTheme
import tech.zhifu.app.myhub.ui.PreviewDesktopLightDark
import tech.zhifu.app.myhub.ui.PreviewPhoneLightDark
import tech.zhifu.app.myhub.ui.PreviewTabletLightDark

@PreviewPhoneLightDark
@PreviewTabletLightDark
@PreviewDesktopLightDark
@Composable
fun DashboardScreenInitGlobalPending() {
    AppTheme {
        DashboardScreen(
            state = DashboardUiState.DASHBOARD_INIT_GLOBAL_PENDING,
            topBar = {},
            initGlobalPendingContent = {
                InitGlobalPending(innerPadding = it)
            },
            errorDisabledContent = {},
            resultOutputCompletedContent = {},
        )
    }
}

@PreviewPhoneLightDark
@PreviewTabletLightDark
@PreviewDesktopLightDark
@Composable
fun DashboardScreenResultErrorDisabled() {
    AppTheme {
        DashboardScreen(
            state = DashboardUiState.DASHBOARD_RESULT_ERROR_DISABLED,
            topBar = {},
            initGlobalPendingContent = {},
            errorDisabledContent = {
                ResultErrorDisabled(
                    innerPadding = it,
                    message = "未知错误，请稍后再试！",
                    canRetry = true,
                    onRetry = {}
                )
            },
            resultOutputCompletedContent = {},
        )
    }
}

@PreviewPhoneLightDark
@Composable
fun DashboardScreenResultOutputCompleted() {
    AppTheme {
        DashboardScreen(
            state = DashboardUiState.DASHBOARD_RESULT_OUTPUT_COMPLETED,
            topBar = {
                TopBar(
                    scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
                    topBarState = TopBarState(
                        isRefreshing = false,
                        reviewCardsCount = 0,
                        showReviewEntrance = false,
                    ),
                    onStartReview = {},
                    onRefresh = {},
                )
            },
            initGlobalPendingContent = {},
            errorDisabledContent = {},
            resultOutputCompletedContent = {
                ResultOutputCompleted(
                    innerPadding = it,
                    isRefreshing = false,
                    onRefresh = {},
                    gridContent = {
                        DashboardGridContent(
                            gridState = rememberLazyStaggeredGridState(),
                            columns = 1,
                            collectionSection = {
                                CollectionSection(
                                    listState = rememberLazyListState(),
                                    collections = collectionList,
                                    onCollectionClick = {},
                                    onViewAllClick = {},
                                    isLoadingMore = false,
                                    hasMore = false,
                                )
                            },
                            cards = cardList,
                            hasMoreCards = false,
                            isLoadingMoreCards = false,
                            onNavigateToCardEdit = {},
                            onNavigateToCardDetail = {},
                            onNavigateToCapture = {},
                            onNavigateToCardList = {},
                        )
                    },
                    reviewSection = {},
                )
            },
        )
    }
}

