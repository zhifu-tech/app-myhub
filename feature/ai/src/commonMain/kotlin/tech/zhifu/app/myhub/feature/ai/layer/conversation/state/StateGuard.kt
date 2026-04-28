package tech.zhifu.app.myhub.feature.ai.layer.conversation.state

import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionCommand

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
        command: ActionCommand,
    ): Boolean = when (command) {
        ActionCommand.CaptureMedia,
        ActionCommand.UploadMedia -> state in setOf(
            ConversationState.IDLE,
            ConversationState.INFO_COLLECT,
            ConversationState.MANUAL_EDIT,
            ConversationState.COMPLETE,
        )

        ActionCommand.ReplaceMedia,
        ActionCommand.GenerateMedia,
        ActionCommand.RemoveMedia,
        is ActionCommand.RemoveMediaAt,
        ActionCommand.SkipMedia -> state in setOf(
            ConversationState.INFO_COLLECT,
            ConversationState.MANUAL_EDIT,
        )

        ActionCommand.EditMedia,
        ActionCommand.EditTitle,
        ActionCommand.EditTags,
        ActionCommand.EditSummary,
        ActionCommand.EditLocation,
        ActionCommand.Publish,
        ActionCommand.SaveDraft,
        ActionCommand.DeleteCard -> state == ConversationState.CARD_REVIEW

        ActionCommand.Review,
        ActionCommand.ClearLocation,
        ActionCommand.SkipTags,
        is ActionCommand.AddTag,
        is ActionCommand.RemoveTag,
        is ActionCommand.SetLocation -> state in setOf(
            ConversationState.INFO_COLLECT,
            ConversationState.MANUAL_EDIT,
        )

        ActionCommand.NewCapture -> state == ConversationState.COMPLETE
    }
}
