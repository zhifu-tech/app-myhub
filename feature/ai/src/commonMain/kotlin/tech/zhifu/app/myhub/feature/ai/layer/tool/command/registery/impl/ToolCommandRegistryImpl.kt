package tech.zhifu.app.myhub.feature.ai.layer.tool.command.registery.impl

import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import tech.zhifu.app.myhub.component.media.MediaPicker
import tech.zhifu.app.myhub.feature.ai.model.CaptureMediaAsset
import tech.zhifu.app.myhub.feature.ai.layer.card.CardEngine
import tech.zhifu.app.myhub.feature.ai.layer.common.util.inferMimeType
import tech.zhifu.app.myhub.feature.ai.layer.storage.StorageGateway
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandRegistry
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandResult
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolErrorCode

class ToolCommandRegistryImpl(
    private val cardEngine: CardEngine,
    private val storageGateway: StorageGateway,
    private val mediaPicker: MediaPicker,
) : ToolCommandRegistry {
    override suspend fun execute(
        command: ToolCommand
    ): ToolCommandResult {
        return when (command) {
            is ToolCommand.UpdateTitle -> {
                ToolCommandResult.DraftUpdated(
                    draft = cardEngine.updateDraftTitle(
                        draft = command.draft,
                        title = command.title
                    )
                )
            }

            is ToolCommand.AddTag -> {
                ToolCommandResult.DraftUpdated(
                    draft = cardEngine.appendTag(
                        command.draft,
                        command.tag
                    )
                )
            }

            is ToolCommand.PublishCard -> {
                val check = cardEngine.prePublishCheck(
                    draft = command.draft
                )
                if (!check.ok) {
                    return ToolCommandResult.Failed(
                        code = ToolErrorCode.PRECONDITION_FAILED,
                        message = "pre_publish_check_failed:${check.issues.joinToString(",")}",
                    )
                }
                val card = cardEngine.toPublishedCard(
                    draft = command.draft
                )
                storageGateway.savePublishedCard(
                    card = card,
                    draft = command.draft
                )
                ToolCommandResult.Published(
                    cardId = card.id,
                    title = card.title
                )
            }

            is ToolCommand.AttachPickedMedia -> {
                val files = mediaPicker.pickImagesAndVideos(
                    maxItems = command.maxItems
                )
                val assets = files
                    .mapNotNull { file ->
                        val localUri = file.toString()
                            .takeIf { it.isNotBlank() }
                            ?: return@mapNotNull null
                        val bytes = runCatching { file.readBytes() }
                            .getOrDefault(defaultValue = ByteArray(0))
                        CaptureMediaAsset(
                            localUri = localUri,
                            mediaType = file.mimeType()?.toString().orEmpty()
                                .ifBlank { inferMimeType(localUri) },
                            sizeBytes = runCatching { file.size() }
                                .getOrDefault(0L),
                            sha256 = Sha256.digestHex(bytes),
                        )
                    }
                ToolCommandResult.MediaAttached(
                    command.draft.copy(
                        mediaAssets = (command.draft.mediaAssets + assets).distinctBy { it.localUri }
                    )
                )
            }
        }
    }
}
