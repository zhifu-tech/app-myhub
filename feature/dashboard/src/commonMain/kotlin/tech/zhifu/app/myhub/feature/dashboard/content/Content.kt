package tech.zhifu.app.myhub.feature.dashboard.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.feature.dashboard.DashboardSideEffect
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.item.ContentGrid
import tech.zhifu.app.myhub.feature.dashboard.content.item.ContentList
import tech.zhifu.app.myhub.feature.dashboard.content.search.collectAsSearchingStateWithLifecycle
import tech.zhifu.app.myhub.feature.dashboard.content.statics.Empty
import tech.zhifu.app.myhub.ui.design.util.LocalSnackbarState
import tech.zhifu.app.myhub.ui.design.util.tapToClearFocus
import tech.zhifu.app.myhub.ui.state.layout.collectAsLayoutAsListStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.CollectPredicatedSharedSideEffect
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun Content(
    viewModel: DashboardViewModel,
    paddingValues: PaddingValues,
) {
    val snackbarState = LocalSnackbarState.current
    viewModel.sideEffect.CollectPredicatedSharedSideEffect(
        predicate = { it is DashboardSideEffect.ShowSnack },
    ) { sideEffect ->
        sideEffect as DashboardSideEffect.ShowSnack
        viewModel.viewModelScope.launch {
            snackbarState.showSnackbar(sideEffect.message)
        }
    }
    val isContentEmpty by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? DashboardUiState.Content)?.items?.isEmpty() ?: true
    }
    val isSearching by viewModel.searchState.collectAsSearchingStateWithLifecycle()
    val layoutAsList by viewModel.layout.collectAsLayoutAsListStateWithLifecycle()
    ContentContent(
        layoutAsList = layoutAsList,
        isContentEmpty = isContentEmpty,
        isSearching = isSearching,
        contentEmpty = {
            Empty(contentPadding = paddingValues, viewModel = viewModel)
        },
        contentList = { modifier ->
            ContentList(
                viewModel = viewModel,
                modifier = modifier,
                paddingValues = paddingValues,
            )
        },
        contentGrid = { modifier ->
            ContentGrid(
                viewModel = viewModel,
                modifier = modifier,
                paddingValues = paddingValues,
            )
        }
    )
}

@Composable
private fun ContentContent(
    layoutAsList: Boolean,
    isContentEmpty: Boolean,
    isSearching: Boolean,
    contentEmpty: @Composable () -> Unit,
    contentList: @Composable (Modifier) -> Unit,
    contentGrid: @Composable (Modifier) -> Unit,
) {
    if (isContentEmpty) {
        if (isSearching.not()) {
            contentEmpty()
            return
        }
        return
    }
    val modifier = Modifier
        .fillMaxSize()
        .tapToClearFocus()
        .padding(horizontal = 16.dp)
        .background(color = MaterialTheme.colorScheme.surfaceVariant)

    if (layoutAsList) {
        contentList(modifier)
    } else {
        contentGrid(modifier)
    }
}
