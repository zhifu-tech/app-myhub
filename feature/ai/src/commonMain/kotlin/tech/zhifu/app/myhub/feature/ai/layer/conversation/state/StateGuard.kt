package tech.zhifu.app.myhub.feature.ai.layer.conversation.state

import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionOptionType

class StateGuard {
    private val inputAllowedStates = setOf(
        ConversationState.IDLE,
        ConversationState.COMPLETE,
        ConversationState.INFO_COLLECT,
        ConversationState.CARD_REVIEW,
        ConversationState.MANUAL_EDIT,
    )

    private val tagAddAllowedStates = setOf(
        ConversationState.INFO_COLLECT,
        ConversationState.CARD_REVIEW,
        ConversationState.MANUAL_EDIT,
    )

    private val removeMediaAtAllowedStates = setOf(
        ConversationState.INFO_COLLECT,
        ConversationState.CARD_REVIEW,
        ConversationState.MANUAL_EDIT,
    )

    private val reviewEditAllowedStates = setOf(
        ConversationState.CARD_REVIEW,
        ConversationState.MANUAL_EDIT,
    )

    private val staticActionsByState: Map<ConversationState, Set<String>> = mapOf(
        ConversationState.INFO_COLLECT to setOf(
            ActionOptionType.SKIP_TAGS.value,
            ActionOptionType.SKIP_MEDIA.value,
            ActionOptionType.UPLOAD_MEDIA.value,
            ActionOptionType.REPLACE_MEDIA.value,
            ActionOptionType.REMOVE_MEDIA.value,
        ),
        ConversationState.CARD_REVIEW to setOf(
            ActionOptionType.EDIT_MEDIA.value,
            ActionOptionType.EDIT_TITLE.value,
            ActionOptionType.EDIT_TAGS.value,
            ActionOptionType.EDIT_SUMMARY.value,
            ActionOptionType.EDIT_LOCATION.value,
            ActionOptionType.PUBLISH.value,
            ActionOptionType.SAVE_DRAFT.value,
            ActionOptionType.DELETE_CARD.value,
            ActionOptionType.CLEAR_LOCATION.value,
        ),
        ConversationState.MANUAL_EDIT to setOf(
            ActionOptionType.EDIT_MEDIA.value,
            ActionOptionType.EDIT_TITLE.value,
            ActionOptionType.EDIT_TAGS.value,
            ActionOptionType.EDIT_SUMMARY.value,
            ActionOptionType.EDIT_LOCATION.value,
            ActionOptionType.REVIEW.value,
            ActionOptionType.UPLOAD_MEDIA.value,
            ActionOptionType.REPLACE_MEDIA.value,
            ActionOptionType.REMOVE_MEDIA.value,
            ActionOptionType.SKIP_MEDIA.value,
            ActionOptionType.SKIP_TAGS.value,
            ActionOptionType.CLEAR_LOCATION.value,
            ActionOptionType.DELETE_CARD.value,
        ),
        ConversationState.COMPLETE to setOf(
            ActionOptionType.NEW_CAPTURE.value
        ),
    )

    fun canInput(
        state: ConversationState
    ): Boolean = state in inputAllowedStates

    fun canAction(
        state: ConversationState,
        action: String
    ): Boolean {
        if (ActionOptionType.isTagAddAction(action) && state in tagAddAllowedStates) return true
        if (ActionOptionType.decodeRemoveMediaAt(action) != null && state in removeMediaAtAllowedStates) return true
        if (ActionOptionType.decodeTagRemove(action) != null && state in reviewEditAllowedStates) return true
        if (ActionOptionType.decodeSetCaptureType(action) != null && state in reviewEditAllowedStates) return true
        if (ActionOptionType.decodeSetLocation(action) != null && state in reviewEditAllowedStates) return true

        return action in staticActionsByState[state].orEmpty()
    }
}
