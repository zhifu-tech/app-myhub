package tech.zhifu.app.myhub.feature.dashboard.content.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.feature.dashboard.DashboardSideEffect
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

@Composable
fun SearchState.collectSearchingState(): State<Boolean> {
    return searchStateFlow
        .map { it.isNotEmpty() }
        .distinctUntilChanged()
        .collectAsState(initial = false)
}

@Composable
fun DashboardViewModel.collectResetSearch(): State<Boolean> {
    val resetSearch = remember {
        mutableStateOf(false)
    }
    collectSharedSideEffect {
        if (it is DashboardSideEffect.ResetSearch) {
            resetSearch.value = true
        }
    }
    return resetSearch
}
