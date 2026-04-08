package tech.zhifu.app.myhub.feature.dashboard.content.appbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.search.SearchBar
import tech.zhifu.app.myhub.ui.design.util.isWidthCompact
import tech.zhifu.app.myhub.ui.design.util.rememberWindowSizeClass
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun BottomBar(
    modifier: Modifier,
    viewModel: DashboardViewModel,
) {
    val showBottomBar by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        it is DashboardUiState.Content
    }

    BottomBarContent(
        showBottomBar = showBottomBar,
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
    showBottomBar: Boolean,
    modifier: Modifier = Modifier,
    searchBar: @Composable (Modifier) -> Unit,
    fab: @Composable (Modifier) -> Unit,
) {
    if (!showBottomBar) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(32.dp), // 保持与键盘间距
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = 12.dp,
            alignment = Alignment.CenterHorizontally
        )
    ) {
        val windowSizeClass = rememberWindowSizeClass()
        val modifier = if (windowSizeClass.isWidthCompact()) {
            Modifier.weight(1f)
        } else {
            Modifier.widthIn(max = 500.dp)
        }
        searchBar(modifier)
        fab(Modifier)
    }
}
