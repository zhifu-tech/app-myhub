package tech.zhifu.app.myhub.feature.dashboard.viewmodel

import androidx.compose.runtime.Composable
import tech.zhifu.app.myhub.feature.dashboard.DashboardSideEffect
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

@Composable
fun DashboardViewModel.collectContentAsEmpty() =
    collectFieldAsState {
        (it as? DashboardUiState.Content)
            ?.items?.isEmpty() ?: true
    }

@Composable
fun DashboardViewModel.collectContentFieldItems() =
    collectFieldAsState {
        (it as? DashboardUiState.Content)
            ?.items ?: emptyList()
    }

@Composable
fun DashboardViewModel.collectContentAsSearching() =
    collectFieldAsState {
        (it as? DashboardUiState.Content)?.searchQuery?.isNotEmpty()
            ?: false
    }

@Composable
fun DashboardViewModel.collectContentSearchQuery() =
    collectFieldAsState {
        (it as? DashboardUiState.Content)?.searchQuery.orEmpty()
    }

@Composable
fun DashboardViewModel.CollectSideEffectShowSnack(
    block: (DashboardSideEffect.ShowSnack) -> Unit
) = collectSharedSideEffect {
    (it as? DashboardSideEffect.ShowSnack)?.let { effect ->
        block(effect)
    }
}

@Composable
fun DashboardViewModel.CollectSideEffectResetSearch(
    block: (DashboardSideEffect.ResetSearch) -> Unit
) = collectSharedSideEffect {
    (it as? DashboardSideEffect.ResetSearch)?.let { effect ->
        block(effect)
    }
}
