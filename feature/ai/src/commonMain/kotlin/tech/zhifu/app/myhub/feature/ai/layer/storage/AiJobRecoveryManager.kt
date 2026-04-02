package tech.zhifu.app.myhub.feature.ai.layer.storage

import tech.zhifu.app.myhub.datastore.repository.capture.AiJobSnapshot
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureLocalRepository
import kotlin.time.Clock

class AiJobRecoveryManager(
    private val captureLocalRepository: CaptureLocalRepository,
    private val mediaPostProcessExecutor: MediaPostProcessExecutor,
) {
    suspend fun listFailedJobs(limit: Int = 50, offset: Int = 0): List<AiJobSnapshot> {
        return captureLocalRepository.listAiJobsByStatus(
            status = "failed",
            limit = limit,
            offset = offset,
        )
    }

    suspend fun retry(jobId: String): Boolean {
        val job = captureLocalRepository.getAiJob(jobId) ?: return false
        val now = Clock.System.now().toEpochMilliseconds()
        val retriedStatus = if (job.provider == "local_media_pipeline") "queued" else "running"
        captureLocalRepository.upsertAiJob(
            job.copy(
                status = retriedStatus,
                errorMessage = null,
                responseJson = null,
                updatedAt = now,
            )
        )
        if (job.provider == "local_media_pipeline") {
            mediaPostProcessExecutor.processQueuedJobs(limit = 1)
        }
        return true
    }

    suspend fun replay(jobId: String): AiJobSnapshot? {
        return captureLocalRepository.getAiJob(jobId)
    }
}
