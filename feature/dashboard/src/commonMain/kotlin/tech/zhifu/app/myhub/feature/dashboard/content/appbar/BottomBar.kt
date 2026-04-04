package tech.zhifu.app.myhub.feature.dashboard.content.appbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.search.SearchBar
import tech.zhifu.app.myhub.ui.viewmodel.collectAsState

@Composable
fun BottomBar(
    modifier: Modifier,
    viewModel: DashboardViewModel,
) {
    val bottomBarEnabled = viewModel.collectAsState {
        it is DashboardUiState.Content
    }.value

    if (bottomBarEnabled.not()) {
        return
    }

    BottomBarContent(
        modifier = modifier,
        searchBar = { modifier ->
            SearchBar(
                modifier = modifier,
                viewModel = viewModel,
            )
        },
        fab = { modifier ->
            Fab(
                modifier = modifier,
                viewModel = viewModel,
            )
        }
    )
}

@Composable
fun BottomBarContent(
    modifier: Modifier = Modifier,
    searchBar: @Composable (Modifier) -> Unit,
    fab: @Composable (Modifier) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(32.dp), // 保持 SearchBar 与键盘间距
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        searchBar(Modifier.weight(1f))
        fab(Modifier)
    }
}
