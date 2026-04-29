package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

sealed interface ActionEvent {
    data object CaptureMedia : ActionEvent
    data object UploadMedia : ActionEvent
    data object ReplaceMedia : ActionEvent
    data object GenerateMedia : ActionEvent
    data object RemoveMedia : ActionEvent
    data object SkipMedia : ActionEvent
    data object SkipTags : ActionEvent
    data object Review : ActionEvent
    data object EditMedia : ActionEvent
    data object EditTitle : ActionEvent
    data object EditTags : ActionEvent
    data object EditSummary : ActionEvent
    data object EditLocation : ActionEvent
    data object Publish : ActionEvent
    data object SaveDraft : ActionEvent
    data object DeleteCard : ActionEvent
    data object NewCapture : ActionEvent
    data object ClearLocation : ActionEvent
    data class AddTag(val tag: String) : ActionEvent
    data class RemoveTag(val tag: String) : ActionEvent
    data class SetLocation(val location: String) : ActionEvent
    data class RemoveMediaAt(val index: Int) : ActionEvent
}
