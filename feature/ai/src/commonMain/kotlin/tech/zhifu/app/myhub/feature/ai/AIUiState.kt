package tech.zhifu.app.myhub.feature.ai

import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Field
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.feature.preview.PreviewState
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode

sealed class AIUiState(
    val state: State,
) {
    object Idle : AIUiState(state = State.IDLE)

    object Loading : AIUiState(state = State.LOADING)

    data class Content(
        val conversationState: ConversationState = ConversationState.IDLE,
        val sessionId: String? = null,
        val messages: List<Message> = emptyList(),
        val draft: CaptureDraft? = null,
        val missingFields: List<Field> = emptyList(),
        val actionComponents: List<ActionComponentSchema> = emptyList(),
        val providerMode: ProviderMode = ProviderMode.DISABLED,
        val input: String = "",
        val isPublishing: Boolean = false,
        val thinkingText: String = "",
        val isThinking: Boolean = false,
        val previewState: PreviewState = PreviewState(),
    ) : AIUiState(state = State.CONTENT)

    data class Error(
        val message: String = "",
    ) : AIUiState(state = State.ERROR)

    enum class State {
        IDLE, LOADING, CONTENT, ERROR
    }
}
