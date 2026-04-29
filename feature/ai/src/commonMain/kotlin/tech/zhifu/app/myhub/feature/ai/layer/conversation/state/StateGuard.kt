package tech.zhifu.app.myhub.feature.ai.layer.conversation.state

import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionEvent

class StateGuard {
    private val inputAllowedStates = setOf(
        ConversationState.IDLE,
        ConversationState.COMPLETE,
        ConversationState.INFO_COLLECT,
        ConversationState.CARD_REVIEW,
        ConversationState.MANUAL_EDIT,
    )

    fun canInput(
        state: ConversationState
    ): Boolean = state in inputAllowedStates

    fun canAction(
        state: ConversationState,
        event: ActionEvent,
    ): Boolean = when (event) {
        ActionEvent.CaptureMedia,
        ActionEvent.UploadMedia -> state in setOf(
            ConversationState.IDLE,
            ConversationState.INFO_COLLECT,
            ConversationState.MANUAL_EDIT,
            ConversationState.COMPLETE,
        )

        ActionEvent.ReplaceMedia,
        ActionEvent.GenerateMedia,
        ActionEvent.RemoveMedia,
        is ActionEvent.RemoveMediaAt,
        ActionEvent.SkipMedia -> state in setOf(
            ConversationState.INFO_COLLECT,
            ConversationState.MANUAL_EDIT,
        )

        ActionEvent.EditMedia,
        ActionEvent.EditTitle,
        ActionEvent.EditTags,
        ActionEvent.EditSummary,
        ActionEvent.EditLocation,
        ActionEvent.Publish,
        ActionEvent.SaveDraft,
        ActionEvent.DeleteCard -> state == ConversationState.CARD_REVIEW

        ActionEvent.Review,
        ActionEvent.ClearLocation,
        ActionEvent.SkipTags,
        is ActionEvent.AddTag,
        is ActionEvent.RemoveTag,
        is ActionEvent.SetLocation -> state in setOf(
            ConversationState.INFO_COLLECT,
            ConversationState.MANUAL_EDIT,
        )

        ActionEvent.NewCapture -> state == ConversationState.COMPLETE
    }
}
