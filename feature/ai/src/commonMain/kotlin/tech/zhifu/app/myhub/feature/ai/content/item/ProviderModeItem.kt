package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode
import tech.zhifu.app.myhub.ui.viewmodel.collectAsState

@Composable
fun ProviderModeItem(viewModel: AIViewModel) {
    val providerMode by viewModel.collectAsState {
        (it as? AIUiState.Content)?.providerMode
    }
    ProviderModeItemContent(
        onProviderModeChange = viewModel::updateProviderMode,
        providerMode = providerMode
    )
}

@Composable
fun ProviderModeItemContent(
    onProviderModeChange: (ProviderMode) -> Unit,
    providerMode: ProviderMode?,
) {
    if (providerMode == null) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProviderMode.entries.forEach { mode ->
            AssistChip(
                onClick = { onProviderModeChange(mode) },
                label = {
                    val active = if (mode == providerMode) "✓" else ""
                    Text("$active${mode.name.lowercase()}")
                },
            )
        }
    }
}
