package tech.zhifu.app.myhub.feature.ai.layer.storage.media

import tech.zhifu.app.myhub.datastore.model.serializer.deserialize
import tech.zhifu.app.myhub.datastore.model.serializer.serialize
import tech.zhifu.app.myhub.datastore.repository.capture.AiJobSnapshot
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureLocalRepository
import kotlin.time.Clock

class MediaPostProcessExecutor(
    private val captureLocalRepository: CaptureLocalRepository,
) {
    suspend fun processQueuedJobs(
        limit: Int = 20
    ) = captureLocalRepository
        .listPendingAiJobs(limit)
        .filter { it.provider == "local_media_pipeline" && it.status == "queued" }
        .forEach { job ->
            processQueuedJob(job)
        }

    private suspend fun processQueuedJob(
        job: AiJobSnapshot
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        val request = job.requestJson
            .deserialize<MediaPostProcessRequest>()
            .getOrNull()

        if (request == null) {
            captureLocalRepository.upsertAiJob(
                snapshot = job.copy(
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
                snapshot = job.copy(
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
            snapshot = media.copy(
                thumbUri = thumbUri,
                durationMs = duration,
            )
        )
        captureLocalRepository.upsertAiJob(
            snapshot = job.copy(
                status = "succeeded",
                responseJson = MediaPostProcessResponse(
                    thumbUri = thumbUri ?: "",
                    durationMs = duration,
                ).serialize().orEmpty(),
                updatedAt = now,
            )
        )
    }

    private fun deriveThumbUri(
        localUri: String,
        mediaType: String
    ): String? = when {
        mediaType.startsWith("image/") -> "$localUri.thumb.jpg"
        mediaType.startsWith("video/") -> "$localUri.frame.jpg"
        else -> null
    }

    private fun deriveDurationMs(
        mediaType: String
    ): Long? = if (mediaType.startsWith("video/")) 0L else null
}
