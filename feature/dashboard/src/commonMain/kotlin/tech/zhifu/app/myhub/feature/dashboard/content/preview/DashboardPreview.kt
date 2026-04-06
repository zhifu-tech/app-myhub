package tech.zhifu.app.myhub.feature.dashboard.content.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.selectedCard
import tech.zhifu.app.myhub.feature.preview.Preview
import tech.zhifu.app.myhub.ui.viewmodel.collectAsState

@Composable
fun DashboardPreview(
    viewModel: DashboardViewModel
) {
    val selectedCard by viewModel.collectAsState {
        (it as? DashboardUiState.Content)?.selectedCard
    }

    Preview(
        card = selectedCard,
        actionHide = {
            viewModel.selectedCard(null)
        }
    )
}
