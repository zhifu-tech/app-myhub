package tech.zhifu.app.myhub.feature.ai.layer.tool.command

import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft

sealed interface ToolCommand {
    data class UpdateTitle(
        val draft: CaptureDraft,
        val title: String,
    ) : ToolCommand

    data class AddTag(
        val draft: CaptureDraft,
        val tag: String,
    ) : ToolCommand

    data class PublishCard(
        val draft: CaptureDraft,
    ) : ToolCommand

    data class AttachPickedMedia(
        val draft: CaptureDraft,
        val maxItems: Int = 3,
    ) : ToolCommand
}
