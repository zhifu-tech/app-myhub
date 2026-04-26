package tech.zhifu.app.myhub.feature.ai.layer.conversation.context

import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationProgress
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Field
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.feature.ai.model.newCaptureDraft

data class ConversationContext(
    val sessionId: String? = null,
    val state: ConversationState = ConversationState.IDLE,

    val draft: CaptureDraft = newCaptureDraft(),
    val messages: List<Message> = emptyList(),
    val actionOwnerMessageId: String? = null,

    val focusField: Field? = null,
    val missingFields: List<Field> = emptyList(),
    val actionComponents: List<ActionComponentSchema> = emptyList(),

    val reasoningText: String = "",
    val reasoningStatus: Boolean = false,
    val analysisRunning: Boolean = false,
    val analysisIncludesMedia: Boolean = false,
    val mediaGenerationProgress: ProviderImageGenerationProgress? = null,

    val clarifyPendingCount: Int = 0,
    val clarifyPendingQuestions: List<String> = emptyList(),
)
