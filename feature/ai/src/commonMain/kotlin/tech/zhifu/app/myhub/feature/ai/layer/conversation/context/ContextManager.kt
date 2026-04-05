package tech.zhifu.app.myhub.feature.ai.layer.conversation.context

import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState

class ContextManager {
    private var sessionSeq: Long = 0L
    private var messageSeq: Long = 0L
    fun newContext(): ConversationContext = ConversationContext(
        sessionId = nextSessionId(),
        state = ConversationState.IDLE,
        actionComponents = emptyList(),
    )

    fun nextSessionId(): String {
        sessionSeq += 1
        return "capture_session_$sessionSeq"
    }

    fun nextMessageId(): String {
        messageSeq += 1
        return "m$messageSeq"
    }
}
