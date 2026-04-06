package tech.zhifu.app.myhub.feature.ai.content.input

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.content.appbar.BottomBarState
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState

@Composable
fun InputBox(
    modifier: Modifier,
    viewModel: AIViewModel,
    state: BottomBarState,
) {
    var input by remember { mutableStateOf(state.input) }
    val placeHolder = when (state.conversationState) {
        ConversationState.INFO_COLLECT -> "输入标签后发送，例如：美食"
        ConversationState.CARD_REVIEW, ConversationState.MANUAL_EDIT -> "输入新标题后发送"
        else -> "输入要捕获的内容"
    }
    ChatInputBar(
        modifier = modifier,
        onInputChange = {
            input = it
            viewModel.updateInput(it)
        },
        input = input,
        placeHolder = placeHolder,
        onSend = {
            viewModel.submitInput()
        }
    )
}

@Composable
fun ChatInputBar(
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

