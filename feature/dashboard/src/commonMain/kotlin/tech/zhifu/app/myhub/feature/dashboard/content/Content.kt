package tech.zhifu.app.myhub.feature.dashboard.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.feature.dashboard.DashboardSideEffect
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.item.ContentGridContent
import tech.zhifu.app.myhub.feature.dashboard.content.item.ContentListContent
import tech.zhifu.app.myhub.feature.dashboard.content.search.collectSearchingState
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.collectContentAsEmpty
import tech.zhifu.app.myhub.ui.design.util.LocalSnackbarState
import tech.zhifu.app.myhub.ui.design.util.tapToClearFocus
import tech.zhifu.app.myhub.ui.state.layout.collectLayoutAsList
import tech.zhifu.app.myhub.ui.viewmodel.collectSharedSideEffect

@Composable
fun Content(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel,
    paddingValues: PaddingValues,
) {
    val snackbarState = LocalSnackbarState.current
    viewModel.collectSharedSideEffect(
        predicate = { it is DashboardSideEffect.ShowSnack },
    ) { sideEffect ->
        sideEffect as DashboardSideEffect.ShowSnack
        viewModel.viewModelScope.launch {
            snackbarState.showSnackbar(sideEffect.message)
        }
    }

    val isContentEmpty by viewModel.collectContentAsEmpty()
    if (isContentEmpty) {
        val isSearching by viewModel.collectSearchingState()
        if (isSearching.not()) {
            Empty(viewModel = viewModel, modifier = modifier)
            return
        }
        return
    }
    ContentContent(
        viewModel = viewModel,
        modifier = modifier,
        paddingValues = paddingValues,
    )
}

@Composable
private fun ContentContent(
    viewModel: DashboardViewModel,
    modifier: Modifier,
    paddingValues: PaddingValues,
) {
    val layoutAsList by viewModel.collectLayoutAsList()

    val modifier = modifier
        .fillMaxSize()
        .tapToClearFocus()
        .testTag("content-list")
        .padding(horizontal = 16.dp)
        .background(color = MaterialTheme.colorScheme.surfaceVariant)

    if (layoutAsList) {
        ContentListContent(
            viewModel = viewModel,
            modifier = modifier,
            paddingValues = paddingValues,
        )
    } else {
        ContentGridContent(
            viewModel = viewModel,
            modifier = modifier,
            paddingValues = paddingValues,
        )
    }
}
