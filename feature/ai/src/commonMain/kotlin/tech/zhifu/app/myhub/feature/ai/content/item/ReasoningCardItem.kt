package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
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

@Composable
fun ReasoningCardItemContent(
    reasoningStatus: Boolean,
    reasoningText: String,
) {
    if (!reasoningStatus || reasoningText.isBlank()) return
    ReasoningTraceCard(
        text = reasoningText,
        live = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        persistentKey = "reasoning-live",
    )
}

@Immutable
private data class ReasoningCardItemState(
    val reasoningText: String,
    val reasoningStatus: Boolean,
)
