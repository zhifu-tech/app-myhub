package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

sealed interface ActionCommand {
    data object CaptureMedia : ActionCommand
    data object UploadMedia : ActionCommand
    data object ReplaceMedia : ActionCommand
    data object GenerateMedia : ActionCommand
    data object RemoveMedia : ActionCommand
    data object SkipMedia : ActionCommand
    data object SkipTags : ActionCommand
    data object Review : ActionCommand
    data object EditMedia : ActionCommand
    data object EditTitle : ActionCommand
    data object EditTags : ActionCommand
    data object EditSummary : ActionCommand
    data object EditLocation : ActionCommand
    data object Publish : ActionCommand
    data object SaveDraft : ActionCommand
    data object DeleteCard : ActionCommand
    data object NewCapture : ActionCommand
    data object ClearLocation : ActionCommand
    data class AddTag(val tag: String) : ActionCommand
    data class RemoveTag(val tag: String) : ActionCommand
    data class SetLocation(val location: String) : ActionCommand
    data class RemoveMediaAt(val index: Int) : ActionCommand
}
