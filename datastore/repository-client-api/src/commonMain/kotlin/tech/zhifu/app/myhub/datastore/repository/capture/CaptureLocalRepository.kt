package tech.zhifu.app.myhub.datastore.repository.capture

import tech.zhifu.app.myhub.datastore.model.domain.MediaAsset

interface CaptureLocalRepository {
    suspend fun upsertDraftSession(snapshot: DraftSessionSnapshot)
    suspend fun getLatestDraftSession(): DraftSessionSnapshot?
    suspend fun deleteDraftSession(sessionId: String): Long

    suspend fun upsertAiJob(snapshot: AiJobSnapshot)
    suspend fun getAiJob(jobId: String): AiJobSnapshot?
    suspend fun listPendingAiJobs(limit: Int): List<AiJobSnapshot>
    suspend fun listAiJobsByStatus(status: String, limit: Int, offset: Int = 0): List<AiJobSnapshot>
    suspend fun deleteAiJob(jobId: String)

    suspend fun upsertMediaAsset(snapshot: MediaAsset)
    suspend fun getMediaAsset(id: String): MediaAsset?
    suspend fun listMediaAssetsByCardId(cardId: String): List<MediaAsset>
    suspend fun listAllMediaAssets(limit: Int, offset: Int = 0): List<MediaAsset>
    suspend fun deleteMediaAsset(id: String)
    suspend fun hasCard(cardId: String): Boolean
}

data class DraftSessionSnapshot(
    val id: String,
    val cardId: String? = null,
    val state: String,
    val draftJson: String,
    val missingFieldsJson: String? = null,
    val updatedAt: Long,
)

data class AiJobSnapshot(
    val id: String,
    val provider: String,
    val requestJson: String,
    val responseJson: String? = null,
    val status: String,
    val errorMessage: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
