package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import tech.zhifu.app.myhub.feature.dashboard.content.DashboardGridContent
import tech.zhifu.app.myhub.feature.dashboard.content.ResultErrorDisabled
import tech.zhifu.app.myhub.feature.dashboard.content.ResultOutputCompleted
import tech.zhifu.app.myhub.feature.dashboard.content.card.CardSectionState
import tech.zhifu.app.myhub.feature.dashboard.content.collection.CollectionSection
import tech.zhifu.app.myhub.feature.dashboard.content.collection.CollectionSectionState
import tech.zhifu.app.myhub.feature.dashboard.topbar.TopBar
import tech.zhifu.app.myhub.feature.dashboard.topbar.TopBarState
import tech.zhifu.app.myhub.theme.AppTheme
import tech.zhifu.app.myhub.ui.PreviewDesktopLightDark
import tech.zhifu.app.myhub.ui.PreviewPhoneLightDark
import tech.zhifu.app.myhub.ui.PreviewTabletLightDark
import tech.zhifu.app.myhub.ui.content.InitGlobalPending

@PreviewPhoneLightDark
@PreviewTabletLightDark
@PreviewDesktopLightDark
@Composable
fun DashboardScreen_InitGlobalPending() {
    AppTheme {
        val uiState = DashboardUiState.InitGlobalPending
        DashboardScreen(
            state = uiState.state,
            topBar = {},
            initGlobalPending = { modifier ->
                InitGlobalPending(modifier = modifier)
            },
            resultErrorDisabled = {},
            resultOutputCompleted = {},
        )
    }
}

@PreviewPhoneLightDark
@PreviewTabletLightDark
@PreviewDesktopLightDark
@Composable
fun DashboardScreenResultErrorDisabled() {
    AppTheme {
        val uiState = DashboardUiState.ResultErrorDisabled(
            message = "未知错误，请稍后再试！",
            canRetry = true,
        )
        DashboardScreen(
            state = uiState.state,
            topBar = {},
            initGlobalPending = {},
            resultErrorDisabled = { modifier ->
                ResultErrorDisabled(
                    modifier = modifier,
                    message = uiState.message,
                    canRetry = uiState.canRetry,
                    onRetry = {}
                )
            },
            resultOutputCompleted = {},
        )
    }
}

@PreviewPhoneLightDark
@Composable
fun DashboardScreenResultOutputCompleted() {
    AppTheme {
        val uiState = DashboardUiState.ResultOutputCompleted(
            isRefreshing = false,
            collectionSectionState = CollectionSectionState(
                collections = collectionList,
            ),
            cardSectionState = CardSectionState(
                cards = cardList,
            )
        )
        DashboardScreen(
            state = uiState.state,
            topBar = {
                TopBar(
                    scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
                    topBarState = TopBarState(
                        isRefreshing = uiState.isRefreshing,
                        reviewCardsCount = 0,
                        showReviewEntrance = false,
                    ),
                    onStartReview = {},
                    onRefresh = {},
                )
            },
            initGlobalPending = {},
            resultErrorDisabled = {},
            resultOutputCompleted = { modifier ->
                ResultOutputCompleted(
                    modifier = modifier,
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = {},
                    gridContent = {
                        DashboardGridContent(
                            gridState = rememberLazyStaggeredGridState(),
                            columns = 1,
                            collectionSection = {
                                CollectionSection(
                                    listState = rememberLazyListState(),
                                    collections = uiState.collectionSectionState.collections,
                                    onCollectionClick = {},
                                    onViewAllClick = {},
                                    isLoadingMore = uiState.collectionSectionState.isLoading,
                                    hasMore = uiState.collectionSectionState.hasMore,
                                )
                            },
                            cards = uiState.cardSectionState.cards,
                            hasMoreCards = uiState.cardSectionState.hasMore,
                            isLoadingMoreCards = uiState.cardSectionState.isLoading,
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

