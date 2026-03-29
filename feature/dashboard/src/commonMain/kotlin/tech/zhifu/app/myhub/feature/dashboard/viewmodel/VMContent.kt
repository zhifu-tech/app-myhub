package tech.zhifu.app.myhub.feature.dashboard.viewmodel

import androidx.compose.runtime.Composable
import tech.zhifu.app.myhub.feature.dashboard.DashboardSideEffect
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

@Composable
fun DashboardViewModel.collectContentEmptyState() = collectFieldAsState {
    (it as? DashboardUiState.Content)
        ?.items?.isEmpty() ?: true
}

@Composable
fun DashboardViewModel.collectContentCountThreshold(
    threshold: Int
) = collectFieldAsState { state ->
    val size = (state as? DashboardUiState.Content)
        ?.items?.size ?: 0
    size >= threshold
}

@Composable
fun DashboardViewModel.collectSideEffectShowSnack(
    block: (DashboardSideEffect.ShowSnack) -> Unit
) = collectSharedSideEffect {
    (it as? DashboardSideEffect.ShowSnack)?.let { effect ->
        block(effect)
    }
}
