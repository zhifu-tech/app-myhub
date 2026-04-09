package tech.zhifu.app.myhub.feature.dashboard.content.appbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.search.SearchBar
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
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val modifier = when {
            windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) -> {
                Modifier.widthIn(min = 120.dp, max = 480.dp)
            }

            else -> {
                Modifier.weight(1f)
            }
        }
        searchBar(modifier)
        fab(Modifier)
    }
}
