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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.ui.viewmodel.collectAsState

@Composable
fun ThinkingCardItem(
    viewModel: AIViewModel,
) {
    val state by viewModel.collectAsState {
        (it as? AIUiState.Content)?.let { state ->
            ThinkingCardItemState(
                text = state.thinkingText,
                isThinking = state.isThinking,
            )
        }
    }

    ThinkingCardItemContent(
        isThinking = state?.isThinking ?: false,
        text = state?.text.orEmpty(),
    )
}

@Composable
fun ThinkingCardItemContent(
    isThinking: Boolean,
    text: String,
) {
    if (!isThinking || text.isBlank()) return
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
                text = "AI 思考中…",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF334155),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF0F172A),
            )
        }
    }
}

@Immutable
private data class ThinkingCardItemState(
    val text: String,
    val isThinking: Boolean,
)
