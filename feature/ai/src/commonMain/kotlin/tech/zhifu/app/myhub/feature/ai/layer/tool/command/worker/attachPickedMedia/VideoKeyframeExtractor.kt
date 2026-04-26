package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.attachPickedMedia

import io.github.vinceglb.filekit.PlatformFile

data class VideoFrameExtraction(
    val durationMs: Long? = null,
    val frames: List<ExtractedVideoFrame> = emptyList(),
)

data class ExtractedVideoFrame(
    val bytes: ByteArray,
    val mimeType: String = "image/jpeg",
    val timestampMs: Long? = null,
)

expect object VideoKeyframeExtractor {
    suspend fun extract(
        file: PlatformFile,
        frameCount: Int,
    ): VideoFrameExtraction
}
