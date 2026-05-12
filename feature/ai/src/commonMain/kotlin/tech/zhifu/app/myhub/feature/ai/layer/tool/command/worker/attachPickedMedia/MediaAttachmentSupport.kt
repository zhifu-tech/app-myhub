package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.attachPickedMedia

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import tech.zhifu.app.myhub.component.media.isImage
import tech.zhifu.app.myhub.component.media.isVideo
import tech.zhifu.app.myhub.datastore.model.util.generateUUId
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisMediaInput
import tech.zhifu.app.myhub.feature.ai.layer.common.util.inferMimeType
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaFileStore
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaImportSource
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.util.Sha256
import tech.zhifu.app.myhub.feature.ai.model.CaptureMediaAsset
import kotlin.io.encoding.Base64

class MediaAttachmentSupport(
    private val mediaFileStore: MediaFileStore,
) {
    suspend fun buildAttachedMedia(
        draftId: String,
        files: List<PlatformFile>,
    ): ImportedMedia {
        val safeFiles = files.take(MAX_MEDIA_ITEMS)
        val assets = mutableListOf<CaptureMediaAsset>()
        val analysisInputs = mutableListOf<ProviderAnalysisMediaInput>()
        val imageFiles = safeFiles.filterNot(::isLikelyVideoFile)
        val videoFiles = safeFiles.filter(::isLikelyVideoFile)
        val remainingFrameBudget = (MAX_MEDIA_ITEMS - imageFiles.size).coerceAtLeast(0)
        val frameBudgets = distributeFrameBudget(
            videoCount = videoFiles.size,
            totalBudget = remainingFrameBudget,
        )

        safeFiles.forEachIndexed { _, file ->
            val sourceUri = file.toString()
                .takeIf { it.isNotBlank() }
                ?: return@forEachIndexed
            val mimeType = file.mimeType()?.toString().orEmpty()
                .ifBlank { inferMimeType(sourceUri) }
            val bytes = if (mimeType.isImage()) {
                runCatching { file.readBytes() }.getOrDefault(defaultValue = ByteArray(0))
            } else {
                ByteArray(0)
            }
            val signatureBytes = bytes.takeIf { it.isNotEmpty() }
                ?: "$sourceUri|$mimeType|${runCatching { file.size() }.getOrDefault(0L)}"
                    .encodeToByteArray()
            val imported = mediaFileStore.persistDraftMedia(
                draftId = draftId,
                mediaId = "media-${generateUUId()}",
                source = MediaImportSource(
                    accessUrl = sourceUri,
                    platformFile = file,
                    sizeBytes = runCatching { file.size() }.getOrNull(),
                ),
            )
            assets += CaptureMediaAsset(
                storageHandle = imported.storageHandle,
                accessUrl = imported.accessUrl,
                mediaType = mimeType,
                sizeBytes = imported.sizeBytes,
                sha256 = Sha256.digestHex(signatureBytes),
            )
            if (bytes.isNotEmpty() && mimeType.isImage()) {
                analysisInputs += ProviderAnalysisMediaInput(
                    mimeType = mimeType,
                    dataBase64 = Base64.encode(bytes),
                    sourceUrl = imported.accessUrl,
                )
            }
            if (mimeType.isVideo()) {
                val videoIndex = videoFiles.indexOf(file).takeIf { it >= 0 } ?: 0
                val extraction = VideoKeyframeExtractor.extract(
                    file = file,
                    frameCount = frameBudgets.getOrElse(videoIndex) { 0 },
                )
                analysisInputs += extraction.frames.mapIndexed { frameIndex, frame ->
                    ProviderAnalysisMediaInput(
                        mimeType = frame.mimeType,
                        dataBase64 = Base64.encode(frame.bytes),
                        sourceUrl = "${imported.accessUrl}#frame-$frameIndex",
                    )
                }
            }
        }
        return ImportedMedia(
            assets = assets,
            analysisInputs = analysisInputs,
        )
    }

    private fun isLikelyVideoFile(
        file: PlatformFile,
    ): Boolean {
        val mimeType = file.mimeType()?.toString().orEmpty()
        if (mimeType.isVideo()) return true
        val path = file.toString().lowercase()
        return path.endsWith(".mp4") ||
            path.endsWith(".mov") ||
            path.endsWith(".m4v") ||
            path.endsWith(".webm") ||
            path.endsWith(".avi") ||
            path.endsWith(".mkv")
    }

    private fun distributeFrameBudget(
        videoCount: Int,
        totalBudget: Int,
    ): List<Int> {
        if (videoCount <= 0 || totalBudget <= 0) return List(videoCount.coerceAtLeast(0)) { 0 }
        val base = totalBudget / videoCount
        var remainder = totalBudget % videoCount
        return List(videoCount) {
            base + if (remainder-- > 0) 1 else 0
        }
    }

    private companion object {
        const val MAX_MEDIA_ITEMS = 9
    }
}

fun assetIdentity(asset: CaptureMediaAsset): String =
    asset.sha256.ifBlank { asset.storageHandle }

data class ImportedMedia(
    val assets: List<CaptureMediaAsset>,
    val analysisInputs: List<ProviderAnalysisMediaInput>,
)
