package tech.zhifu.app.myhub.feature.dashboard.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import tech.zhifu.app.myhub.feature.dashboard.DashboardSideEffect
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

// 搜索入口内容数量阈值
const val SHOW_SEARCH_ENTRANCE_CONTENT_COUNT_THRESHOLD = 5

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
