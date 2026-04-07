package tech.zhifu.app.myhub.feature.dashboard.viewmodel

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle

@Composable
fun StateFlow<DashboardUiState>.collectAsContentEmptyStateWithLifecycle() =
    collectAsSelectedStateWithLifecycle {
        (it as? DashboardUiState.Content)
            ?.items?.isEmpty() ?: true
    }

@Composable
fun StateFlow<DashboardUiState>.collectAsItemsStateWithLifecycle() =
    collectAsSelectedStateWithLifecycle {
        (it as? DashboardUiState.Content)
            ?.items ?: emptyList()
    }
