package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.captureMediaPhoto

import tech.zhifu.app.myhub.component.media.MediaPicker
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandResult
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.attachPickedMedia.MediaAttachmentSupport
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.attachPickedMedia.assetIdentity

class ToolCommandCaptureMediaPhotoExecutor(
    private val mediaPicker: MediaPicker,
    private val mediaAttachmentSupport: MediaAttachmentSupport,
) : ToolCommandExecutor {
    override suspend fun execute(
        command: ToolCommand
    ): ToolCommandResult {
        val command = command as ToolCommand.CaptureMediaPhoto
        val file = mediaPicker.capturePhoto() ?: return ToolCommandResult.MediaAttached(
            draft = command.draft,
            attachedAssets = emptyList(),
        )
        val imported = mediaAttachmentSupport.buildAttachedMedia(
            draftId = command.draft.id,
            files = listOf(file),
        )
        return ToolCommandResult.MediaAttached(
            draft = command.draft.copy(
                mediaAssets = (command.draft.mediaAssets + imported.assets).distinctBy(::assetIdentity)
            ),
            attachedAssets = imported.assets,
            analysisInputs = imported.analysisInputs,
        )
    }
}
