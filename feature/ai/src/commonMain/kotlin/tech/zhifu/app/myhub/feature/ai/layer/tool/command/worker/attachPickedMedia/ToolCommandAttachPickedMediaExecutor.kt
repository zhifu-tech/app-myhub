package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.attachPickedMedia

import tech.zhifu.app.myhub.component.media.MediaPicker
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandResult

class ToolCommandAttachPickedMediaExecutor(
    private val mediaPicker: MediaPicker,
    private val mediaAttachmentSupport: MediaAttachmentSupport,
) : ToolCommandExecutor {

    override suspend fun execute(
        command: ToolCommand
    ): ToolCommandResult {
        command as ToolCommand.AttachPickedMedia
        val files = if (command.imagesOnly) {
            mediaPicker.pickImages(maxItems = command.maxItems)
        } else {
            mediaPicker.pickImagesAndVideos(maxItems = command.maxItems)
        }
        val imported = mediaAttachmentSupport.buildAttachedMedia(
            draftId = command.draft.id,
            files = files,
        )
        return ToolCommandResult.MediaAttached(
            draft = command.draft.copy(
                mediaAssets = (command.draft.mediaAssets + imported.assets)
                    .distinctBy(::assetIdentity)
            ),
            attachedAssets = imported.assets,
            analysisInputs = imported.analysisInputs,
        )
    }
}
