package tech.zhifu.app.myhub.feature.ai.layer.storage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.repository.capture.AiJobSnapshot
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureLocalRepository
import kotlin.time.Clock

class MediaPostProcessExecutor(
    private val captureLocalRepository: CaptureLocalRepository,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun processQueuedJobs(limit: Int = 20) {
        captureLocalRepository.listPendingAiJobs(limit)
            .filter { it.provider == "local_media_pipeline" && it.status == "queued" }
            .forEach { job ->
                processSingle(job)
            }
    }

    private suspend fun processSingle(job: AiJobSnapshot) {
        val now = Clock.System.now().toEpochMilliseconds()
        val request = runCatching {
            json.decodeFromString(MediaPostProcessRequest.serializer(), job.requestJson)
        }.getOrNull()
        if (request == null) {
            captureLocalRepository.upsertAiJob(
                job.copy(
                    status = "failed",
                    errorMessage = "invalid_request_json",
                    updatedAt = now,
                )
            )
            return
        }

        val media = captureLocalRepository.getMediaAsset(request.mediaId)
        if (media == null) {
            captureLocalRepository.upsertAiJob(
                job.copy(
                    status = "failed",
                    errorMessage = "media_not_found",
                    updatedAt = now,
                )
            )
            return
        }

        val thumbUri = deriveThumbUri(media.localUri, media.mediaType)
        val duration = deriveDurationMs(media.mediaType)
        captureLocalRepository.upsertMediaAsset(
            media.copy(
                thumbUri = thumbUri,
                durationMs = duration,
            )
        )
        captureLocalRepository.upsertAiJob(
            job.copy(
                status = "succeeded",
                responseJson = """{"thumbUri":"$thumbUri","durationMs":${duration ?: "null"}}""",
                updatedAt = now,
            )
        )
    }

    private fun deriveThumbUri(localUri: String, mediaType: String): String? {
        return when {
            mediaType.startsWith("image/") -> "$localUri.thumb.jpg"
            mediaType.startsWith("video/") -> "$localUri.frame.jpg"
            else -> null
        }
    }

    private fun deriveDurationMs(mediaType: String): Long? {
        return if (mediaType.startsWith("video/")) 0L else null
    }
}

@Serializable
private data class MediaPostProcessRequest(
    @SerialName("cardId") val cardId: String,
    @SerialName("mediaId") val mediaId: String,
    @SerialName("mediaUri") val mediaUri: String,
)
