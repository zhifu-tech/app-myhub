package tech.zhifu.app.myhub.feature.ai.content.appbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.content.input.InputBox
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.ui.design.util.isWidthCompact
import tech.zhifu.app.myhub.ui.design.util.rememberWindowSizeClass
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun BottomBar(
    viewModel: AIViewModel,
) {
    val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.let { uiState ->
            BottomBarState(
                draft = uiState.draft,
                input = uiState.input,
                isPublishing = uiState.isPublishing,
                conversationState = uiState.conversationState,
            )
        } ?: BottomBarState()
    }
    BottomBarContent(
        state = state,
        inputBox = { modifier ->
            InputBox(
                modifier = modifier,
                viewModel = viewModel,
                state = state,
            )
        }
    )
}

@Composable
fun BottomBarContent(
    state: BottomBarState?,
    inputBox: @Composable (Modifier) -> Unit = {},
    sendButton: @Composable (Modifier) -> Unit = {},
) {
    if (state == null || state.isPublishing) {
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(32.dp), // 保持与键盘间距
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = 12.dp,
            alignment = Alignment.CenterHorizontally
        )
    ) {
        val windowSizeClass = rememberWindowSizeClass()
        val modifier = if (windowSizeClass.isWidthCompact()) {
            Modifier.weight(1f)
        } else {
            Modifier.widthIn(min = 120.dp, max = 500.dp)
        }

        inputBox(modifier)
        sendButton(Modifier)
    }
}


@Immutable
data class BottomBarState(
    val draft: CaptureDraft? = null,
    val input: String = "",
    val isPublishing: Boolean = false,
    val conversationState: ConversationState = ConversationState.IDLE,
)
