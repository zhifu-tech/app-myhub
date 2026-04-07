package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun ActionSectionItem(
    viewModel: AIViewModel
) {
    val components by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.actionComponents.orEmpty()
    }
    ActionSectionItemContent(
        components = components,
        onAction = viewModel::performQuickAction,
    )
}

@Composable
fun ActionSectionItemContent(
    components: List<ActionComponentSchema>,
    onAction: (String) -> Unit,
) {
    if (components.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        components.forEach { component ->
            when (component.type) {
                "quick_reply", "card_actions", "tag_selector", "upload" -> {
                    component.options.forEach { option ->
                        AssistChip(
                            onClick = { onAction(option.value) },
                            label = { Text(option.label) }
                        )
                    }
                }

                "input" -> {
                    Text(
                        text = component.options.firstOrNull()?.label ?: "请在输入框输入内容",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569),
                    )
                }
            }
        }
    }
}
