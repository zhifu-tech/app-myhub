package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_cancel_analysis
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_reasoning_live_desc_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_reasoning_live_desc_text
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
                analysisRunning = state.context.analysisRunning,
                analysisIncludesMedia = state.context.analysisIncludesMedia,
            )
        }
    }
    val safeState = state ?: return
    ReasoningCardItemContent(
        reasoningStatus = safeState.reasoningStatus,
        analysisRunning = safeState.analysisRunning,
        analysisIncludesMedia = safeState.analysisIncludesMedia,
        reasoningText = safeState.reasoningText,
        onCancel = viewModel::cancelAnalysis,
    )
}

@Composable
fun ReasoningCardItemContent(
    reasoningStatus: Boolean,
    analysisRunning: Boolean,
    analysisIncludesMedia: Boolean,
    reasoningText: String,
    onCancel: () -> Unit,
) {
    if (!analysisRunning && (!reasoningStatus || reasoningText.isBlank())) return
    ReasoningTraceCard(
        text = reasoningText.ifBlank {
            if (analysisIncludesMedia) {
                stringResource(Res.string.feature_ai_reasoning_live_desc_media)
            } else {
                stringResource(Res.string.feature_ai_reasoning_live_desc_text)
            }
        },
        live = analysisRunning,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        persistentKey = "reasoning-live",
        secondaryActionLabel = if (analysisRunning) {
            stringResource(Res.string.feature_ai_action_cancel_analysis)
        } else {
            null
        },
        onSecondaryAction = if (analysisRunning) onCancel else null,
    )
}

@Immutable
private data class ReasoningCardItemState(
    val reasoningText: String,
    val reasoningStatus: Boolean,
    val analysisRunning: Boolean,
    val analysisIncludesMedia: Boolean,
)
