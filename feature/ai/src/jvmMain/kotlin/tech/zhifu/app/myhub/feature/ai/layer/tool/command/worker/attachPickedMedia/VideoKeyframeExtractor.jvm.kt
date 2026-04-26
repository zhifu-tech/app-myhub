package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.attachPickedMedia

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.deleteIfExists

actual object VideoKeyframeExtractor {
    actual suspend fun extract(
        file: PlatformFile,
        frameCount: Int,
    ): VideoFrameExtraction {
        val path = runCatching {
            Paths.get(file.toString().removePrefix("file://"))
        }.getOrNull()
            ?: return VideoFrameExtraction()
        if (!Files.exists(path)) return VideoFrameExtraction()

        val safeCount = frameCount.coerceAtLeast(1)
        val durationMs = probeDurationMs(path)
        val timestamps = buildTimestamps(durationMs = durationMs, frameCount = safeCount)
        val tempDir = withContext(Dispatchers.IO) {
            Files.createTempDirectory("myhub_video_frames_")
        }
        return try {
            val frames = timestamps.mapIndexedNotNull { index, timestampMs ->
                val output = tempDir.resolve("frame_${index.toString().padStart(3, '0')}.jpg")
                val process = ProcessBuilder(
                    "ffmpeg",
                    "-hide_banner",
                    "-loglevel",
                    "error",
                    "-y",
                    "-ss",
                    "%.3f".format(timestampMs / 1000.0),
                    "-i",
                    path.toString(),
                    "-frames:v",
                    "1",
                    output.toString(),
                ).redirectErrorStream(true).start()
                val console = process.inputStream.bufferedReader().readText()
                val exitCode = process.waitFor()
                if (exitCode != 0 || !Files.exists(output)) {
                    logger.debug {
                        "VideoKeyframeExtractor ffmpeg failed path=$path timestamp=$timestampMs exit=$exitCode output=$console"
                    }
                    return@mapIndexedNotNull null
                }
                ExtractedVideoFrame(
                    bytes = Files.readAllBytes(output),
                    timestampMs = timestampMs,
                )
            }
            VideoFrameExtraction(
                durationMs = durationMs,
                frames = frames,
            )
        } catch (e: Exception) {
            logger.debug { "VideoKeyframeExtractor exception path=$path err=${e.message}" }
            VideoFrameExtraction(durationMs = durationMs)
        } finally {
            cleanupDir(tempDir)
        }
    }

    private fun probeDurationMs(path: Path): Long? {
        return runCatching {
            val process = ProcessBuilder(
                "ffprobe",
                "-v",
                "error",
                "-show_entries",
                "format=duration",
                "-of",
                "default=noprint_wrappers=1:nokey=1",
                path.toString(),
            ).redirectErrorStream(true).start()
            val value = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            if (exitCode != 0) return null
            (value.toDoubleOrNull()?.times(1000))?.toLong()
        }.getOrNull()
    }

    private fun buildTimestamps(
        durationMs: Long?,
        frameCount: Int,
    ): List<Long> {
        val safeDuration = durationMs?.takeIf { it > 0 }
            ?: return List(frameCount) { index -> index * 2_000L }
        if (frameCount == 1) return listOf((safeDuration * 0.4).toLong())
        return List(frameCount) { index ->
            (((index + 1).toDouble() / (frameCount + 1).toDouble()) * safeDuration)
                .toLong()
                .coerceAtLeast(0L)
        }
    }

    private fun cleanupDir(dir: Path) {
        runCatching {
            Files.walk(dir).use { stream ->
                stream.sorted(Comparator.reverseOrder()).forEach { it.deleteIfExists() }
            }
        }
    }
}
