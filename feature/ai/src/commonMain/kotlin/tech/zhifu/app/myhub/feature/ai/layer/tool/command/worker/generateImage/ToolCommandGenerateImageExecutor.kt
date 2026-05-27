package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.generateImage

import tech.zhifu.app.myhub.datastore.model.util.generateUUId
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationExecutor
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationProgress
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationRequest
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationResult
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaFileStore
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandExecutor
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandResult
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolErrorCode
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.attachPickedMedia.assetIdentity
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.util.Sha256
import tech.zhifu.app.myhub.feature.ai.model.CaptureMediaAsset

class ToolCommandGenerateImageExecutor(
    private val providerImageGenerationExecutor: ProviderImageGenerationExecutor,
    private val mediaFileStore: MediaFileStore,
    private val captureImagePromptRefiner: CaptureImagePromptRefiner,
) : ToolCommandExecutor {
    override suspend fun execute(
        command: ToolCommand
    ): ToolCommandResult {
        val command = command as ToolCommand.GenerateImage
        val basePrompt = CaptureImagePromptBuilder.build(
            draft = command.draft,
            language = command.language,
        )
        if (basePrompt.isBlank()) {
            return ToolCommandResult.Failed(
                code = ToolErrorCode.INVALID_ARGUMENT,
                message = "image_generation_prompt_blank",
            )
        }
        command.onProgress(
            ProviderImageGenerationProgress(
                stage = ProviderImageGenerationProgress.Stage.PREPARING,
                status = if (command.language.startsWith("zh", ignoreCase = true)) {
                    "正在优化提示词"
                } else {
                    "Refining prompt"
                },
            )
        )
        val prompt = captureImagePromptRefiner.refine(
            draft = command.draft,
            language = command.language,
            basePrompt = basePrompt,
        )
        return when (
            val generation = providerImageGenerationExecutor.generate(
                request = ProviderImageGenerationRequest(
                    prompt = prompt,
                    language = command.language,
                    draftId = command.draft.id,
                ),
                onProgress = command.onProgress,
            )
        ) {
            is ProviderImageGenerationResult.Failed -> ToolCommandResult.Failed(
                code = ToolErrorCode.EXECUTION_FAILED,
                message = "image_generation_failed:${generation.category}:${generation.reason}",
            )

            is ProviderImageGenerationResult.Success -> {
                val mediaId = "media-${generateUUId()}"
                val imported = mediaFileStore.saveGeneratedImage(
                    draftId = command.draft.id,
                    mediaId = mediaId,
                    bytes = generation.image.bytes,
                    mimeType = generation.image.mimeType,
                )
                val asset = CaptureMediaAsset(
                    storageHandle = imported.storageHandle,
                    accessUrl = imported.accessUrl,
                    mediaType = generation.image.mimeType,
                    sizeBytes = imported.sizeBytes,
                    sha256 = Sha256.digestHex(generation.image.bytes),
                )
                val updatedDraft = if (command.replaceExisting) {
                    command.draft.copy(mediaAssets = listOf(asset))
                } else {
                    command.draft.copy(
                        mediaAssets = (command.draft.mediaAssets + asset).distinctBy(::assetIdentity)
                    )
                }
                ToolCommandResult.MediaAttached(
                    draft = updatedDraft,
                    attachedAssets = listOf(asset),
                )
            }
        }
    }
}
