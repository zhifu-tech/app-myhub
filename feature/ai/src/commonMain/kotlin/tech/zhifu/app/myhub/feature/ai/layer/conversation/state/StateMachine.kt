package tech.zhifu.app.myhub.feature.ai.layer.conversation.state

class StateMachine {
    private val transitionTable: Map<TransitionKey, ConversationState> = mapOf(
        transition(ConversationState.IDLE, Signal.START_CAPTURE, ConversationState.INTENT_DETECT),

        transition(ConversationState.INTENT_DETECT, Signal.DRAFT_INCOMPLETE, ConversationState.INFO_COLLECT),
        transition(ConversationState.INTENT_DETECT, Signal.DRAFT_COMPLETE, ConversationState.CARD_REVIEW),

        transition(ConversationState.INFO_COLLECT, Signal.DRAFT_INCOMPLETE, ConversationState.INFO_COLLECT),
        transition(ConversationState.INFO_COLLECT, Signal.DRAFT_COMPLETE, ConversationState.CARD_REVIEW),

        transition(ConversationState.CARD_REVIEW, Signal.REQUEST_MANUAL_EDIT, ConversationState.MANUAL_EDIT),
        transition(ConversationState.CARD_REVIEW, Signal.REQUEST_PUBLISH, ConversationState.PUBLISH),
        transition(ConversationState.CARD_REVIEW, Signal.DRAFT_COMPLETE, ConversationState.CARD_REVIEW),

        transition(ConversationState.MANUAL_EDIT, Signal.REQUEST_REVIEW, ConversationState.CARD_REVIEW),

        transition(ConversationState.PUBLISH, Signal.PUBLISH_SUCCEEDED, ConversationState.COMPLETE),

        transition(ConversationState.COMPLETE, Signal.START_CAPTURE, ConversationState.INTENT_DETECT),
    )

    fun transition(
        current: ConversationState,
        signal: Signal,
    ): ConversationState = transitionTable[TransitionKey(current, signal)] ?: current

    private fun transition(
        from: ConversationState,
        signal: Signal,
        to: ConversationState,
    ): Pair<TransitionKey, ConversationState> = TransitionKey(from, signal) to to

    private data class TransitionKey(
        val from: ConversationState,
        val signal: Signal,
    )
}

enum class Signal {
    START_CAPTURE,
    DRAFT_COMPLETE,
    DRAFT_INCOMPLETE,
    REQUEST_REVIEW,
    REQUEST_MANUAL_EDIT,
    REQUEST_PUBLISH,
    PUBLISH_SUCCEEDED,
}
