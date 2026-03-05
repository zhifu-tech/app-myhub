package tech.zhifu.app.myhub.feature.dashboard.content

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.State

@Composable
fun ContentRoute(
    viewModel: DashboardViewModel,
    innerPadding: PaddingValues,
) {
    logger.debug("Dashboard Screen") { "DashboardContent" }
    val state by viewModel.collectFieldAsState {
        it.state
    }
    Content(
        state = state,
        initGlobalPendingContent = {
            InitGlobalPending(innerPadding = innerPadding)
        },
        errorDisabledContent = {
            ResultErrorDisabledRoute(
                innerPadding = innerPadding,
                viewModel = viewModel,
            )
        },
        outputCompletedContent = {
            ResultOutputCompletedRoute(
                innerPadding = innerPadding,
                viewModel = viewModel,
            )
        },
    )
}

@Composable
fun Content(
    state: State,
    initGlobalPendingContent: @Composable () -> Unit,
    errorDisabledContent: @Composable () -> Unit,
    outputCompletedContent: @Composable () -> Unit,
) {
    when (state) {
        DashboardUiState.DASHBOARD_INIT_GLOBAL_PENDING -> initGlobalPendingContent()
        DashboardUiState.DASHBOARD_RESULT_ERROR_DISABLED -> errorDisabledContent()
        DashboardUiState.DASHBOARD_RESULT_OUTPUT_COMPLETED -> outputCompletedContent()
        else -> Unit
    }
}
