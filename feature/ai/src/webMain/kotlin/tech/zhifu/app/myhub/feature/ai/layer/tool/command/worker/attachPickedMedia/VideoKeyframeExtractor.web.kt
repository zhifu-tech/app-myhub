package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.attachPickedMedia

import io.github.vinceglb.filekit.PlatformFile

actual object VideoKeyframeExtractor {
    actual suspend fun extract(
        file: PlatformFile,
        frameCount: Int,
    ): VideoFrameExtraction = VideoFrameExtraction()
}
