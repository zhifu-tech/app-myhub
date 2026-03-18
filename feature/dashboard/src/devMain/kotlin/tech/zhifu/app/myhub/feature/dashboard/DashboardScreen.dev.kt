package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.runtime.Composable
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.BottomBarContent
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.FabContent
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.TopBarAvatarContent
import tech.zhifu.app.myhub.feature.dashboard.content.appbar.TopBarContent
import tech.zhifu.app.myhub.feature.dashboard.content.search.SearchBarContent
import tech.zhifu.app.myhub.theme.AppTheme
import tech.zhifu.app.myhub.ui.PreviewPhoneLightDark

@PreviewPhoneLightDark
@Composable
fun DashboardScreen_Loading() {
    AppTheme {
        val uiState = DashboardUiState.Loading()
        DashboardScreenContent(
            state = uiState.state,
            topBar = {
                TopBarContent(
                    actions = {
                        TopBarAvatarContent(
                            onClick = {},
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
            loading = {},
            error = {},
            content = {},
        )
    }
}
