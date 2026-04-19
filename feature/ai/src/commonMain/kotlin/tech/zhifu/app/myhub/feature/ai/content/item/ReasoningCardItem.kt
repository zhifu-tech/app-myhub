package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_thinking_title
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun ReasoningCardItem(
    viewModel: AIViewModel,
) {
    val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.let { state ->
            ReasoningCardItemState(
                reasoningText = state.context.reasoningText,
                reasoningStatus = state.context.reasoningStatus,
            )
        }
    }
    val safeState = state ?: return
    ReasoningCardItemContent(
        reasoningStatus = safeState.reasoningStatus,
        reasoningText = state?.reasoningText.orEmpty(),
    )
}

@Immutable
private data class ReasoningCardItemState(
    val reasoningText: String,
    val reasoningStatus: Boolean,
)

@Composable
fun ReasoningCardItemContent(
    reasoningStatus: Boolean,
    reasoningText: String,
) {
    if (!reasoningStatus || reasoningText.isBlank()) return
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(Res.string.feature_ai_thinking_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = reasoningText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
