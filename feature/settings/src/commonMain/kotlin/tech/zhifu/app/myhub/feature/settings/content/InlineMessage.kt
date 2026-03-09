package tech.zhifu.app.myhub.feature.settings.content

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.settings.SettingsUiState
import tech.zhifu.app.myhub.feature.settings.SettingsViewModel
import tech.zhifu.app.myhub.feature.settings.resources.Res
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_close


@Composable
fun InlineMessageRoute(
    viewModel: SettingsViewModel
) {
    val state = viewModel.collectFieldAsState { uiState ->
        (uiState as? SettingsUiState.ResultOutputCompleted)?.inlineMessage
    }.value ?: return

    InlineMessage(
        message = state,
        onClick = viewModel::clearError
    )
}

@Composable
internal fun InlineMessage(
    message: String,
    onClick: () -> Unit
) {
    if (message.isNotEmpty()) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
        TextButton(onClick = onClick) {
            Text(stringResource(Res.string.feature_settings_close))
        }
    }
}
