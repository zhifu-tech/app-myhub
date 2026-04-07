package tech.zhifu.app.myhub.feature.dashboard.content.search

import kotlinx.coroutines.flow.MutableStateFlow
import tech.zhifu.app.myhub.feature.dashboard.DashboardSideEffect
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

fun createSearchStateFlow() = MutableStateFlow("")

fun DashboardViewModel.search(query: String) {
    searchState.value = query
}

fun DashboardViewModel.resetSearch() = intent {
    searchState.value = ""
    postSideEffect(DashboardSideEffect.ResetSearch)
}
