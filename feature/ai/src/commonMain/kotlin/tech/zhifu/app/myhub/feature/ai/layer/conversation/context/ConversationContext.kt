package tech.zhifu.app.myhub.feature.ai.layer.conversation.context

import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState

data class ConversationContext(
    val sessionId: String? = null,
    val state: ConversationState = ConversationState.IDLE,
    val messages: List<Message> = emptyList(),
    val draft: CaptureDraft? = null,
    val missingFields: List<String> = emptyList(),
    val actionComponents: List<ActionComponentSchema> = emptyList(),
)
