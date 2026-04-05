package tech.zhifu.app.myhub.feature.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.navigation.AppNavigator
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode
import tech.zhifu.app.myhub.ui.viewmodel.collectAsState

@Composable
fun AiScreen(
    navigator: AppNavigator,
    viewModel: AIViewModel = koinViewModel<AIViewModel>(),
) {

    val state by viewModel.collectAsState { it }
    when (val uiState = state) {
        AIUiState.Idle,
        AIUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F7F8)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is AIUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F7F8)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = uiState.message.ifBlank { "加载失败" })
            }
        }

        is AIUiState.Content -> {
            CaptureContent(
                state = uiState,
                onBack = navigator::goBack,
                onInputChange = viewModel::updateInput,
                onSubmit = viewModel::submitInput,
                onAction = viewModel::performQuickAction,
                onProviderModeChange = viewModel::updateProviderMode,
            )
        }
    }
}

@Composable
private fun CaptureContent(
    state: AIUiState.Content,
    onBack: () -> Unit,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onAction: (String) -> Unit,
    onProviderModeChange: (ProviderMode) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            }
            Text(
                text = "AI 捕获（客户端主导）",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
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
                        val active = if (mode == state.providerMode) "✓" else ""
                        Text("$active${mode.name.lowercase()}")
                    },
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                if (state.draft != null) {
                    DraftPreviewCard(
                        title = state.draft.title,
                        summary = state.draft.summary,
                        tags = state.draft.tags,
                        mediaCount = state.draft.mediaAssets.size,
                        conversationState = state.conversationState,
                    )
                }
            }
            items(state.messages, key = { it.id }) { msg ->
                MessageBubble(message = msg)
            }
            item {
                ActionSection(
                    components = state.actionComponents,
                    onAction = onAction,
                )
            }
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            value = state.input,
            onValueChange = onInputChange,
            placeholder = {
                Text(
                    when (state.conversationState) {
                        ConversationState.INFO_COLLECT -> "输入标签后发送，例如：美食"
                        ConversationState.CARD_REVIEW, ConversationState.MANUAL_EDIT -> "输入新标题后发送"
                        else -> "输入要捕获的内容"
                    }
                )
            },
            enabled = !state.isPublishing,
            shape = RoundedCornerShape(14.dp),
            trailingIcon = {
                TextButton(
                    onClick = onSubmit,
                    enabled = !state.isPublishing
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Spacer(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
        )
    }
}

@Composable
private fun DraftPreviewCard(
    title: String,
    summary: String,
    tags: List<String>,
    mediaCount: Int,
    conversationState: ConversationState,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "草稿预览 · ${conversationState.name}",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF64748B),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (tags.isNotEmpty()) {
                Text(
                    text = tags.joinToString(prefix = "#", separator = " #"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF475569),
                )
            }
            if (mediaCount > 0) {
                Text(
                    text = "已附加媒体：$mediaCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF334155),
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val isUser = message.role == Message.Role.USER
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .background(
                    color = when (message.role) {
                        Message.Role.AI -> Color(0xFFEFF6FF)
                        Message.Role.USER -> Color(0xFF1D4ED8)
                        Message.Role.SYSTEM -> Color(0xFFE2E8F0)
                    },
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUser) Color.White else Color(0xFF0F172A),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ActionSection(
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
