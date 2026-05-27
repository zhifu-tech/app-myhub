package tech.zhifu.app.myhub.feature.dashboard.content.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.preview.Preview
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun DashboardPreview(
    viewModel: DashboardViewModel
) {
    val previewState by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? DashboardUiState.Content)?.previewState
    }
    val safePreviewState = previewState ?: return
    Preview(state = safePreviewState)
}
