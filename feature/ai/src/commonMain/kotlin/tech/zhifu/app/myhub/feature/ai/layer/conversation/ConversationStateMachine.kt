package tech.zhifu.app.myhub.feature.ai.layer.conversation

import tech.zhifu.app.myhub.feature.ai.CaptureState

class ConversationStateMachine {
    private val transitions: Map<Pair<CaptureState, CaptureSignal>, CaptureState> = mapOf(
        (CaptureState.IDLE to CaptureSignal.NeedMoreInfo) to CaptureState.INFO_COLLECT,
        (CaptureState.IDLE to CaptureSignal.DraftReady) to CaptureState.CARD_REVIEW,

        (CaptureState.INTENT_DETECT to CaptureSignal.NeedMoreInfo) to CaptureState.INFO_COLLECT,
        (CaptureState.INTENT_DETECT to CaptureSignal.DraftReady) to CaptureState.CARD_REVIEW,
        (CaptureState.DRAFT_CREATE to CaptureSignal.NeedMoreInfo) to CaptureState.INFO_COLLECT,
        (CaptureState.DRAFT_CREATE to CaptureSignal.DraftReady) to CaptureState.CARD_REVIEW,

        (CaptureState.INFO_COLLECT to CaptureSignal.NeedMoreInfo) to CaptureState.INFO_COLLECT,
        (CaptureState.INFO_COLLECT to CaptureSignal.DraftReady) to CaptureState.CARD_REVIEW,
        (CaptureState.INFO_COLLECT to CaptureSignal.MoveToReview) to CaptureState.CARD_REVIEW,

        (CaptureState.CARD_REVIEW to CaptureSignal.PublishRequested) to CaptureState.PUBLISH_CONFIRM,
        (CaptureState.MANUAL_EDIT to CaptureSignal.PublishRequested) to CaptureState.PUBLISH_CONFIRM,

        (CaptureState.PUBLISH_CONFIRM to CaptureSignal.PublishSucceeded) to CaptureState.COMPLETE,

        (CaptureState.COMPLETE to CaptureSignal.NeedMoreInfo) to CaptureState.INFO_COLLECT,
        (CaptureState.COMPLETE to CaptureSignal.DraftReady) to CaptureState.CARD_REVIEW,
    )

    fun transition(current: CaptureState, signal: CaptureSignal): CaptureState {
        return transitions[current to signal] ?: current
    }
}

enum class CaptureSignal {
    DraftReady,
    NeedMoreInfo,
    MoveToReview,
    PublishRequested,
    PublishSucceeded,
}
