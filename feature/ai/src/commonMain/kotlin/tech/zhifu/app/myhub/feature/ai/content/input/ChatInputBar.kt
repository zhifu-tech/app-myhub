package tech.zhifu.app.myhub.feature.ai.content.input

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.model.Field
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_placeholder_default
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_placeholder_location
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_placeholder_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_placeholder_review
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_placeholder_summary
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_placeholder_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_placeholder_title
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun ChatInputBar(
    modifier: Modifier,
    viewModel: AIViewModel,
) {
    val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.let { uiState ->
            ChatInputBarState(
                input = uiState.input,
                isPublishing = uiState.isPublishing,
                conversationState = uiState.conversationState,
                inputField = uiState.inputField,
                missingFields = uiState.missingFields,
            )
        }
    }
    val safeState = state ?: return

    val placeHolder = when (safeState.conversationState) {
        ConversationState.INFO_COLLECT if safeState.missingFields.firstOrNull() == Field.MEDIA -> {
            stringResource(Res.string.feature_ai_input_placeholder_media)
        }

        ConversationState.INFO_COLLECT if safeState.missingFields.firstOrNull() == Field.TAGS -> {
            stringResource(Res.string.feature_ai_input_placeholder_tags)
        }

        ConversationState.INFO_COLLECT if safeState.missingFields.firstOrNull() == Field.TITLE -> {
            stringResource(Res.string.feature_ai_input_placeholder_title)
        }

        ConversationState.CARD_REVIEW, ConversationState.MANUAL_EDIT -> {
            when (safeState.inputField) {
                Field.TAGS -> stringResource(Res.string.feature_ai_input_placeholder_tags)
                Field.SUMMARY -> stringResource(Res.string.feature_ai_input_placeholder_summary)
                Field.TITLE -> stringResource(Res.string.feature_ai_input_placeholder_title)
                Field.LOCATION -> stringResource(Res.string.feature_ai_input_placeholder_location)
                else -> stringResource(Res.string.feature_ai_input_placeholder_review)
            }
        }

        else -> stringResource(Res.string.feature_ai_input_placeholder_default)
    }
    // 输入需要及时响应，updateInput 会有延迟，导致输入跳变
    var input by remember { mutableStateOf("") }
    input = safeState.input
    ChatInputBarContent(
        modifier = modifier,
        onInputChange = { text ->
            input = text
            viewModel.updateInput(text)
        },
        input = input,
        placeHolder = placeHolder,
        onSend = {
            viewModel.submitInput()
        }
    )
}

@Immutable
data class ChatInputBarState(
    val input: String,
    val isPublishing: Boolean,
    val conversationState: ConversationState,
    val inputField: Field?,
    val missingFields: List<Field>,
)

@Composable
fun ChatInputBarContent(
    modifier: Modifier = Modifier,
    input: String,
    placeHolder: String,
    onInputChange: (String) -> Unit,
    onSend: (String) -> Unit,

    // 🔌 可扩展 slot
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable ((Boolean) -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    val canSend = input.isNotBlank()
    val heightState = rememberStickyInputHeight()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {

            // 🧩 左侧扩展（附件 / 相册）
            leadingContent?.invoke()

            // ✏️ 输入核心
            StickyInputCore(
                input = input,
                placeHolder = placeHolder,
                onInputChange = onInputChange,
                heightState = heightState,
                onSend = {
                    if (canSend) {
                        onSend(input)
                        onInputChange("")
                        heightState.reset() // ✅ 发送后收缩
                        focusManager.clearFocus()
                    }
                }
            )

            // 🚀 右侧扩展（发送按钮）
            if (trailingContent != null) {
                trailingContent(canSend)
            } else {
                SendButton(
                    enabled = canSend,
                    onClick = {
                        if (canSend) {
                            onSend(input)
                            onInputChange("")
                            heightState.reset()
                            focusManager.clearFocus()
                        }
                    }
                )
            }
        }
    }
}
