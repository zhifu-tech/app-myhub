package tech.zhifu.app.myhub.service.media.analysis

import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.service.media.CaptureAnalysisRequest
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Base64

object AnalysisMediaInputBuilder {
    fun buildOllamaImages(request: CaptureAnalysisRequest, config: AnalysisProviderConfig): List<String> {
        val binaries = mutableListOf<ByteArray>()
        request.media.forEach { media ->
            val type = media.type.lowercase()
            when {
                type == "image" -> loadImageBytes(media.remoteUri)?.let { binaries.add(it) }
                type == "video" -> {
                    binaries.addAll(
                        VideoFrameExtractor.extractFrames(
                            remoteUri = media.remoteUri,
                            frameCount = config.videoFrameCount,
                            intervalSeconds = config.videoFrameIntervalSeconds
                        )
                    )
                }
            }
        }
        return binaries
            .take(config.ollamaVisionImageLimit.coerceAtLeast(1))
            .map { Base64.getEncoder().encodeToString(it) }
    }

    private fun loadImageBytes(remoteUri: String): ByteArray? {
        val path = toPath(remoteUri) ?: return null
        if (!Files.exists(path)) return null
        return runCatching { Files.readAllBytes(path) }
            .onFailure { logger.debug { "loadImageBytes failed: uri=$remoteUri, err=${it.message}" } }
            .getOrNull()
    }

    internal fun toPath(remoteUri: String): Path? {
        return runCatching {
            if (remoteUri.startsWith("file:/")) {
                Paths.get(URI.create(remoteUri))
            } else {
                Paths.get(remoteUri)
            }
        }.onFailure {
            logger.debug { "toPath failed: uri=$remoteUri, err=${it.message}" }
        }.getOrNull()
    }
}

