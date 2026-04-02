package tech.zhifu.app.myhub.feature.ai.layer.tool

import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import tech.zhifu.app.myhub.component.media.MediaPicker
import tech.zhifu.app.myhub.feature.ai.CaptureMediaAsset
import tech.zhifu.app.myhub.feature.ai.layer.cardengine.CardEngine
import tech.zhifu.app.myhub.feature.ai.layer.storage.CaptureStorageGateway

class ToolRegistry(
    private val cardEngine: CardEngine,
    private val storageGateway: CaptureStorageGateway,
    private val mediaPicker: MediaPicker,
) {
    suspend fun execute(command: ToolCommand): ToolResult {
        return when (command) {
            is ToolCommand.UpdateTitle -> {
                ToolResult.DraftUpdated(cardEngine.updateDraftTitle(command.draft, command.title))
            }

            is ToolCommand.AddTag -> {
                ToolResult.DraftUpdated(cardEngine.appendTag(command.draft, command.tag))
            }

            is ToolCommand.PublishCard -> {
                val check = cardEngine.prePublishCheck(command.draft)
                if (!check.ok) {
                    return ToolResult.Failed(
                        code = ToolErrorCode.PRECONDITION_FAILED,
                        message = "pre_publish_check_failed:${check.issues.joinToString(",")}",
                    )
                }
                val card = cardEngine.toPublishedCard(command.draft)
                storageGateway.savePublishedCard(card, command.draft)
                ToolResult.Published(card.id, card.title)
            }

            is ToolCommand.AttachPickedMedia -> {
                val files = mediaPicker.pickImagesAndVideos(maxItems = command.maxItems)
                val assets = files
                    .mapNotNull { file ->
                        val localUri = file.toString().takeIf { it.isNotBlank() } ?: return@mapNotNull null
                        val bytes = runCatching { file.readBytes() }.getOrDefault(ByteArray(0))
                        CaptureMediaAsset(
                            localUri = localUri,
                            mediaType = file.mimeType()?.toString().orEmpty().ifBlank { inferMimeType(localUri) },
                            sizeBytes = runCatching { file.size() }.getOrDefault(0L),
                            sha256 = Sha256.digestHex(bytes),
                        )
                    }
                ToolResult.MediaAttached(
                    command.draft.copy(
                        mediaAssets = (command.draft.mediaAssets + assets).distinctBy { it.localUri }
                    )
                )
            }
        }
    }
}

private fun inferMimeType(uri: String): String {
    val normalized = uri.lowercase()
    return when {
        normalized.endsWith(".jpg") || normalized.endsWith(".jpeg") -> "image/jpeg"
        normalized.endsWith(".png") -> "image/png"
        normalized.endsWith(".webp") -> "image/webp"
        normalized.endsWith(".gif") -> "image/gif"
        normalized.endsWith(".mp4") -> "video/mp4"
        normalized.endsWith(".mov") -> "video/quicktime"
        normalized.endsWith(".m4v") -> "video/x-m4v"
        else -> "application/octet-stream"
    }
}
