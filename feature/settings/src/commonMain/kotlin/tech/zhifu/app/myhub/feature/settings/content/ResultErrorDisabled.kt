package tech.zhifu.app.myhub.feature.settings.content


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tech.zhifu.app.myhub.feature.settings.SettingsUiState
import tech.zhifu.app.myhub.feature.settings.SettingsViewModel
import tech.zhifu.app.myhub.ui.content.ResultErrorDisabled

@Composable
fun ResultErrorDisabledRoute(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state = viewModel.collectFieldAsState { uiState ->
        uiState as? SettingsUiState.ResultErrorDisabled
    }.value ?: return

    ResultErrorDisabled(
        message = state.message,
        canRetry = state.canRetry,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}
