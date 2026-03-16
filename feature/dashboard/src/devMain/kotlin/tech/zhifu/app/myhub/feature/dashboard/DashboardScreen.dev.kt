package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.runtime.Composable
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.BottomBarContent
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.FabContent
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.TopBarAvatarContent
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.TopBarContent
import tech.zhifu.app.myhub.feature.dashboard.content.menu.MenuContent
import tech.zhifu.app.myhub.feature.dashboard.content.menu.MenuItemLayoutContent
import tech.zhifu.app.myhub.feature.dashboard.content.menu.MenuItemLogoutContent
import tech.zhifu.app.myhub.feature.dashboard.content.menu.MenuItemSortContent
import tech.zhifu.app.myhub.feature.dashboard.content.search.SearchBarContent
import tech.zhifu.app.myhub.theme.AppTheme
import tech.zhifu.app.myhub.ui.PreviewPhoneLightDark

@PreviewPhoneLightDark
@Composable
fun DashboardScreen_Loading() {
    AppTheme {
        val uiState = DashboardUiState.Loading
        DashboardScreen(
            state = uiState.state,
            topBar = {
                TopBarContent(
                    actions = {
                        TopBarAvatarContent(
                            onClick = {},
                            menu = {
                                MenuContent(
                                    expanded = true,
                                    onDismissRequest = {},
                                    menuItemLayout = {
                                        MenuItemLayoutContent(
                                            layoutAsList = true,
                                            onActionLayout = {}
                                        )
                                    },
                                    menuItemSort = {
                                        MenuItemSortContent(
                                            sortAsDate = true,
                                            onActionSort = {},
                                        )
                                    },
                                    menuItemLogout = {
                                        MenuItemLogoutContent(
                                            onActionLogout = {},
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            },
            bottomBar = {
                BottomBarContent(
                    searchBar = {
                        SearchBarContent(
                            query = "MyHub",
                            onQueryChange = {},
                        )
                    },
                    fab = {
                        FabContent(
                            onClickAdd = {},
                            onClickClose = {},
                        )
                    }
                )
            },
            error = {},
            content = {},
        )
    }
}
//
//@PreviewPhoneLightDark
//@PreviewTabletLightDark
//@PreviewDesktopLightDark
//@Composable
//fun DashboardScreen_Error() {
//    AppTheme {
//        val uiState = DashboardUiState.Error(
//            message = "未知错误，请稍后再试！",
//        )
//        DashboardScreen(
//            state = uiState.state,
//            topBar = {},
//            error = { modifier ->
//                Error(
//                    modifier = modifier,
//                    message = uiState.message,
//                    onRetry = {}
//                )
//            },
//            content = {},
//        )
//    }
//}
//
//@PreviewPhoneLightDark
//@Composable
//fun DashboardScreen_Content() {
//    AppTheme {
//        val uiState = DashboardUiState.Content(
//            isRefreshing = false,
//            collectionSectionState = CollectionSectionState(
//                collections = collectionList,
//            ),
//            cardSectionState = CardSectionState(
//                cards = cardList,
//            )
//        )
//        DashboardScreen(
//            state = uiState.state,
//            topBar = {
//                TopBar(
//                    scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
//                    topBarState = TopBarState(
//                        isRefreshing = uiState.isRefreshing,
//                        reviewCardsCount = 0,
//                        showReviewEntrance = false,
//                    ),
//                    onStartReview = {},
//                    onRefresh = {},
//                )
//            },
//            error = {},
//            content = { modifier ->
//                Content(
//                    modifier = modifier,
//                    isRefreshing = uiState.isRefreshing,
//                    onRefresh = {},
//                    gridContent = {
//                        ContentGridContent(
//                            gridState = rememberLazyStaggeredGridState(),
//                            columns = 1,
//                            collectionSection = {
//                                CollectionSection(
//                                    listState = rememberLazyListState(),
//                                    collections = uiState.collectionSectionState.collections,
//                                    onCollectionClick = {},
//                                    onViewAllClick = {},
//                                    isLoadingMore = uiState.collectionSectionState.isLoading,
//                                    hasMore = uiState.collectionSectionState.hasMore,
//                                )
//                            },
//                            cards = uiState.cardSectionState.cards,
//                            hasMoreCards = uiState.cardSectionState.hasMore,
//                            isLoadingMoreCards = uiState.cardSectionState.isLoading,
//                            onNavigateToCardEdit = {},
//                            onNavigateToCardDetail = {},
//                            onNavigateToCapture = {},
//                            onNavigateToCardList = {},
//                        )
//                    },
//                    reviewSection = {},
//                )
//            },
//        )
//    }
//}
//
