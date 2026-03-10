package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import tech.zhifu.app.myhub.feature.dashboard.content.Content
import tech.zhifu.app.myhub.feature.dashboard.content.ContentGridContent
import tech.zhifu.app.myhub.feature.dashboard.content.Error
import tech.zhifu.app.myhub.feature.dashboard.content.card.CardSectionState
import tech.zhifu.app.myhub.feature.dashboard.content.collection.CollectionSection
import tech.zhifu.app.myhub.feature.dashboard.content.collection.CollectionSectionState
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
fun DashboardScreen_Loading() {
    AppTheme {
        val uiState = DashboardUiState.Loading
        DashboardScreen(
            state = uiState.state,
            topBar = {},
            error = {},
            content = {},
        )
    }
}

@PreviewPhoneLightDark
@PreviewTabletLightDark
@PreviewDesktopLightDark
@Composable
fun DashboardScreen_Error() {
    AppTheme {
        val uiState = DashboardUiState.Error(
            message = "未知错误，请稍后再试！",
        )
        DashboardScreen(
            state = uiState.state,
            topBar = {},
            error = { modifier ->
                Error(
                    modifier = modifier,
                    message = uiState.message,
                    onRetry = {}
                )
            },
            content = {},
        )
    }
}

@PreviewPhoneLightDark
@Composable
fun DashboardScreen_Content() {
    AppTheme {
        val uiState = DashboardUiState.Content(
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
            error = {},
            content = { modifier ->
                Content(
                    modifier = modifier,
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = {},
                    gridContent = {
                        ContentGridContent(
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

