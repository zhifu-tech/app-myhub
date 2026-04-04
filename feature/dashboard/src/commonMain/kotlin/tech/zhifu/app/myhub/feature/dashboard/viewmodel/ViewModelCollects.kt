package tech.zhifu.app.myhub.feature.dashboard.viewmodel

import androidx.compose.runtime.Composable
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.ui.viewmodel.collectAsState

@Composable
fun DashboardViewModel.collectContentAsEmpty() =
    collectAsState {
        (it as? DashboardUiState.Content)
            ?.items?.isEmpty() ?: true
    }

@Composable
fun DashboardViewModel.collectContentFieldItems() =
    collectAsState {
        (it as? DashboardUiState.Content)
            ?.items ?: emptyList()
    }
