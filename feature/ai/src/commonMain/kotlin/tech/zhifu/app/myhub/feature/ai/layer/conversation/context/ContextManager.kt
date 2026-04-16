package tech.zhifu.app.myhub.feature.ai.layer.conversation.context

import tech.zhifu.app.myhub.datastore.model.util.generateUUId
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState

class ContextManager {
    var context: ConversationContext = newContext()
    private val contextChangeCallbacks = mutableSetOf<ContextChangeCallback>()

    fun newContext(): ConversationContext = ConversationContext(
        sessionId = nextSessionId(),
        state = ConversationState.IDLE,
        actionComponents = emptyList(),
    )

    fun nextSessionId(): String = generateUUId()

    fun nextMessageId(): String = generateUUId()

    suspend fun notifyContextChange(
        updated: ConversationContext,
    ) {
        val pre = this.context
        this.context = updated
        contextChangeCallbacks.forEach { callback ->
            callback.onChange(context = updated, pre = pre)
        }
    }

    fun addContextChangeCallback(callback: ContextChangeCallback) {
        contextChangeCallbacks.add(callback)
    }

    fun removeContextChangeCallback(callback: ContextChangeCallback) {
        contextChangeCallbacks.remove(callback)
    }
}

fun interface ContextChangeCallback {
    suspend fun onChange(
        context: ConversationContext,
        pre: ConversationContext?,
    )
}
