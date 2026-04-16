package tech.zhifu.app.myhub.feature.ai.layer.tool.command

import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.CaptureType

sealed interface ToolCommand {
    data class UpdateTitle(
        val draft: CaptureDraft,
        val title: String,
    ) : ToolCommand

    data class AddTag(
        val draft: CaptureDraft,
        val tag: String,
    ) : ToolCommand

    data class RemoveTag(
        val draft: CaptureDraft,
        val tag: String,
    ) : ToolCommand

    data class UpdateSummary(
        val draft: CaptureDraft,
        val summary: String,
    ) : ToolCommand

    data class UpdateType(
        val draft: CaptureDraft,
        val type: CaptureType,
    ) : ToolCommand

    data class UpdateLocation(
        val draft: CaptureDraft,
        val location: String,
    ) : ToolCommand

    data class ClearLocation(
        val draft: CaptureDraft,
    ) : ToolCommand

    data class PublishCard(
        val draft: CaptureDraft,
    ) : ToolCommand

    data class AttachPickedMedia(
        val draft: CaptureDraft,
        val maxItems: Int = 3,
    ) : ToolCommand
}
