package tech.zhifu.app.myhub.feature.dashboard.viewmodel

import androidx.compose.runtime.Composable
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

@Composable
fun DashboardViewModel.collectContentState() = collectFieldAsState {
    it as? DashboardUiState.Content
}

@Composable
fun DashboardViewModel.collectContentEmptyState() = collectFieldAsState {
    (it as? DashboardUiState.Content)
        ?.contentItems?.isEmpty() ?: true
}

@Composable
fun DashboardViewModel.collectContentCountThreshold(
    threshold: Int
) = collectFieldAsState { state ->
    val size = (state as? DashboardUiState.Content)
        ?.contentItems?.size ?: 0
    size >= threshold
}
