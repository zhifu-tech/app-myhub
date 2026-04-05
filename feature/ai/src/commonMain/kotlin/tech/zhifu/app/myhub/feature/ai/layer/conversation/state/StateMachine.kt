package tech.zhifu.app.myhub.feature.ai.layer.conversation.state

class StateMachine {
    private val transitions: Map<Pair<ConversationState, Signal>, ConversationState> = mapOf(
        ConversationState.IDLE to Signal.NeedMoreInfo
            to ConversationState.INFO_COLLECT,
        ConversationState.IDLE to Signal.DraftReady
            to ConversationState.CARD_REVIEW,

        ConversationState.INTENT_DETECT to Signal.NeedMoreInfo
            to ConversationState.INFO_COLLECT,
        ConversationState.INTENT_DETECT to Signal.DraftReady
            to ConversationState.CARD_REVIEW,

        ConversationState.DRAFT_CREATE to Signal.NeedMoreInfo
            to ConversationState.INFO_COLLECT,
        ConversationState.DRAFT_CREATE to Signal.DraftReady
            to ConversationState.CARD_REVIEW,

        ConversationState.INFO_COLLECT to Signal.NeedMoreInfo
            to ConversationState.INFO_COLLECT,
        ConversationState.INFO_COLLECT to Signal.DraftReady
            to ConversationState.CARD_REVIEW,
        ConversationState.INFO_COLLECT to Signal.MoveToReview
            to ConversationState.CARD_REVIEW,

        ConversationState.CARD_REVIEW to Signal.PublishRequested
            to ConversationState.PUBLISH_CONFIRM,

        ConversationState.MANUAL_EDIT to Signal.PublishRequested
            to ConversationState.PUBLISH_CONFIRM,

        ConversationState.PUBLISH_CONFIRM to Signal.PublishSucceeded
            to ConversationState.COMPLETE,

        ConversationState.COMPLETE to Signal.NeedMoreInfo
            to ConversationState.INFO_COLLECT,
        ConversationState.COMPLETE to Signal.DraftReady
            to ConversationState.CARD_REVIEW,
    )

    fun transition(current: ConversationState, signal: Signal): ConversationState {
        return transitions[current to signal] ?: current
    }
}

enum class Signal {
    DraftReady,
    NeedMoreInfo,
    MoveToReview,
    PublishRequested,
    PublishSucceeded,
}
