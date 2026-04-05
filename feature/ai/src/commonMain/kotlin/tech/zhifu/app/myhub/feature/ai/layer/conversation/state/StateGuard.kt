package tech.zhifu.app.myhub.feature.ai.layer.conversation.state

class StateGuard {
    fun canInput(
        state: ConversationState
    ): Boolean {
        val allowed = setOf(
            ConversationState.IDLE,
            ConversationState.COMPLETE,
            ConversationState.INFO_COLLECT,
            ConversationState.CARD_REVIEW,
            ConversationState.MANUAL_EDIT,
        )
        return state in allowed
    }

    fun canAction(
        state: ConversationState,
        action: String
    ): Boolean {
        if (action.startsWith("tag:") &&
            state == ConversationState.INFO_COLLECT
        ) {
            return true
        }
        val allowed = when (state) {
            ConversationState.INFO_COLLECT -> {
                setOf("skip_tags", "review", "upload_media")
            }

            ConversationState.CARD_REVIEW,
            ConversationState.MANUAL_EDIT -> {
                setOf("edit_title", "publish")
            }

            ConversationState.COMPLETE -> {
                setOf("new_capture")
            }

            else -> emptySet()
        }
        return action in allowed
    }
}
