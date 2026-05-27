package tech.zhifu.app.myhub.service.media.analysis

import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

object VideoFrameExtractor {
    fun extractFrames(remoteUri: String, frameCount: Int, intervalSeconds: Int): List<ByteArray> {
        val videoPath = AnalysisMediaInputBuilder.toPath(remoteUri) ?: return emptyList()
        if (!Files.exists(videoPath)) return emptyList()
        val safeFrameCount = frameCount.coerceAtLeast(1)
        val safeInterval = intervalSeconds.coerceAtLeast(1)
        val tempDir = Files.createTempDirectory("myhub_capture_frames_")
        return try {
            val outputPattern = tempDir.resolve("frame_%03d.jpg")
            val process = ProcessBuilder(
                "ffmpeg",
                "-hide_banner",
                "-loglevel",
                "error",
                "-y",
                "-i",
                videoPath.toString(),
                "-vf",
                "fps=1/$safeInterval",
                "-frames:v",
                safeFrameCount.toString(),
                outputPattern.toString()
            ).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            val exit = process.waitFor()
            if (exit != 0) {
                logger.debug { "ffmpeg extract failed: exit=$exit, output=$output" }
                return emptyList()
            }
            Files.list(tempDir).use { stream ->
                stream.sorted()
                    .limit(safeFrameCount.toLong())
                    .map { Files.readAllBytes(it) }
                    .toList()
            }
        } catch (e: Exception) {
            logger.debug { "extractFrames exception: ${e.message}" }
            emptyList()
        } finally {
            cleanupDir(tempDir)
        }
    }

    private fun cleanupDir(dir: Path) {
        runCatching {
            Files.walk(dir).use { walk ->
                walk.sorted(Comparator.reverseOrder()).forEach { it.deleteIfExists() }
            }
        }
    }
}

