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
    object Loading : AIUiState(state = State.LOADING)

    data class Content(
        val messages: List<Message> = emptyList(),
        val conversationState: ConversationState = ConversationState.IDLE,
        val draft: CaptureDraft? = null,
        val inputField: Field? = null,
        val missingFields: List<Field> = emptyList(),
        val actionComponents: List<ActionComponentSchema> = emptyList(),
        val providerMode: ProviderMode = ProviderMode.DISABLED,
        val input: String = "",
        val isPublishing: Boolean = false,
        val reasoningText: String = "",
        val reasoningStatus: Boolean = false,
        val previewState: PreviewState = PreviewState(),
    ) : AIUiState(state = State.CONTENT)

    enum class State {
        LOADING, CONTENT
    }
}
