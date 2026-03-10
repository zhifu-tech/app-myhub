package tech.zhifu.app.myhub.feature.settings.content


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.settings.SettingsUiState
import tech.zhifu.app.myhub.feature.settings.SettingsViewModel

@Composable
fun ErrorRoute(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state = viewModel.collectFieldAsState { uiState ->
        uiState as? SettingsUiState.Error
    }.value ?: return

    Error(
        message = state.message,
        canRetry = state.canRetry,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
fun Error(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(all = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            if (canRetry) {
                Button(onClick = onRetry) {
                    Text("重试")
                }
            }
        }
    }
}
