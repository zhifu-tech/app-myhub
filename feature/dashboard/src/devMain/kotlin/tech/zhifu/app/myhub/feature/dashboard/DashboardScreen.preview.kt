package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.feature.dashboard.content.Loading
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.TopBarContent
import tech.zhifu.app.myhub.ui.design.PreviewPhoneLightDark
import tech.zhifu.app.myhub.ui.design.theme.AppTheme
import tech.zhifu.app.myhub.ui.design.util.LocalSharedTransitionScope

@Composable
@PreviewPhoneLightDark
fun DashboardScreenPreview() {
    AppTheme {
        SharedTransitionLayout {
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this
            ) {
                DashboardScreenContent(
                    scaffold = {
                        DashboardScaffoldContent(
                            topBar = { hazeState ->
                                TopBarContent(
                                    hazeState = hazeState,
                                    actions = {}
                                )
                            },
                            bottomBar = {},
                            content = { contentPadding, hazeState ->
                                DashboardContentContent(
                                    state = DashboardUiState.State.LOADING,
                                    loading = {
                                        Loading(modifier = Modifier.padding(paddingValues = contentPadding))
                                    },
                                    error = {},
                                    content = {}
                                )
                            },
                        )
                    },
                    preview = {

                    }
                )
            }
//                ContentListContent(
//                    items = mockContentCards(),
//                    modifier = Modifier,
//                    paddingValues = PaddingValues(all = 20.dp),
//                    onLoadMore = {},
//                    contentItem = { item ->
//                        ContentItemHostContent(
//                            item = item,
//                            isSelected = false,
//                            onClick = {},
//                            content = { animatedVisibilityScope ->
//                                ContentListItem(
//                                    item = item,
//                                    animatedVisibilityScope = animatedVisibilityScope
//                                )
//                            },
//                        )
//                    }
//                )
//            }
        }
    }
}
