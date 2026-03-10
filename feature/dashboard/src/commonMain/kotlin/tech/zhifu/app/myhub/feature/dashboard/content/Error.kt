package tech.zhifu.app.myhub.feature.dashboard.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel

@Composable
fun ErrorRoute(
    modifier: Modifier,
    viewModel: DashboardViewModel
) {
    val state = viewModel.collectFieldAsState { uiState ->
        uiState as? DashboardUiState.Error
    }.value ?: return

    Error(
        modifier = modifier,
        message = state.message,
        onRetry = viewModel::retry,
    )
}

@Composable
internal fun Error(
    modifier: Modifier,
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(16.dp))
        TextButton(
            onClick = onRetry,
        ) {
            Text("Retry")
        }
    }
}
