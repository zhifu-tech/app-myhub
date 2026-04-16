package tech.zhifu.app.myhub.feature.ai.layer.conversation.context

import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Field
import tech.zhifu.app.myhub.feature.ai.model.Message

data class ConversationContext(
    val sessionId: String? = null,
    val state: ConversationState = ConversationState.IDLE,
    val messages: List<Message> = emptyList(),
    val draft: CaptureDraft? = null,
    val focusField: Field? = null,
    val missingFields: List<Field> = emptyList(),
    val actionComponents: List<ActionComponentSchema> = emptyList(),
    val reasoningText: String = "",
    val reasoningStatus: Boolean = false,
)
