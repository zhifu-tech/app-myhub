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
fun DashboardViewModel.CollectSideEffectShowSnack(
    block: (DashboardSideEffect.ShowSnack) -> Unit
) = collectSharedSideEffect {
    (it as? DashboardSideEffect.ShowSnack)?.let { effect ->
        block(effect)
    }
}
