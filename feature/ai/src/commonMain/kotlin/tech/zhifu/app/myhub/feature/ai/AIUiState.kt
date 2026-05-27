package tech.zhifu.app.myhub.feature.ai

import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ConversationContext
import tech.zhifu.app.myhub.feature.preview.PreviewState
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode

sealed class AIUiState(
    val state: State,
) {
    object Loading : AIUiState(state = State.LOADING)

    data class Content(
        val providerMode: ProviderMode = ProviderMode.DISABLED,
        val shortcutVisible: Boolean = false,
        val context: ConversationContext = ConversationContext(),
        val input: String = "",
        val previewState: PreviewState = PreviewState(),
    ) : AIUiState(state = State.CONTENT)

    enum class State {
        LOADING, CONTENT
    }
}
