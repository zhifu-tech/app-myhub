package tech.zhifu.app.myhub.feature.dashboard.content.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import tech.zhifu.app.myhub.feature.dashboard.DashboardSideEffect
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.logger.debug

data class SearchState(
    val isViable: Boolean = false,
    val query: String = ""
)

@Composable
fun DashboardViewModel.collectSearchStateQuery(): State<String?> =
    collectFieldAsState { state ->
        (state as? DashboardUiState.Content)?.searchState?.query
    }

fun DashboardViewModel.updateSearchStateQuery(
    query: String
) = reduce<DashboardUiState.Content> {
    copy(
        searchState = searchState.copy(query = query)
    )
}

fun DashboardViewModel.resetSearchState() {
    logger.debug {
        "resetSearch is called"
    }
    updateSearchStateQuery("")
    postSideEffect(DashboardSideEffect.ResetSearch)
}

@Composable
fun DashboardViewModel.collectSideEffectResetSearch(
    block: (DashboardSideEffect.ResetSearch) -> Unit
) = collectSharedSideEffect { effect ->
    (effect as? DashboardSideEffect.ResetSearch)?.let {
        block(it)
    }
}
