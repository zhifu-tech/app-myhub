package tech.zhifu.app.myhub.datastore.repository.capture

import tech.zhifu.app.myhub.datastore.database.MyHubDatabase

class CaptureLocalRepositoryImpl(
    private val database: MyHubDatabase,
) : CaptureLocalRepository {
    override suspend fun upsertDraftSession(snapshot: DraftSessionSnapshot) {
        database.draft_sessionQueries.upsertDraftSession(
            id = snapshot.id,
            card_id = snapshot.cardId,
            state = snapshot.state,
            draft_json = snapshot.draftJson,
            missing_fields_json = snapshot.missingFieldsJson,
            updated_at = snapshot.updatedAt,
        )
    }

    override suspend fun getLatestDraftSession(): DraftSessionSnapshot? {
        val row = database.draft_sessionQueries
            .selectLatestDraftSessions(limit = 1, offset = 0)
            .executeAsOneOrNull() ?: return null
        return DraftSessionSnapshot(
            id = row.id,
            cardId = row.card_id,
            state = row.state,
            draftJson = row.draft_json,
            missingFieldsJson = row.missing_fields_json,
            updatedAt = row.updated_at,
        )
    }

    override suspend fun deleteDraftSession(sessionId: String) {
        database.draft_sessionQueries.deleteDraftSessionById(sessionId)
    }

    override suspend fun upsertAiJob(snapshot: AiJobSnapshot) {
        database.ai_jobQueries.upsertAiJob(
            id = snapshot.id,
            provider = snapshot.provider,
            request_json = snapshot.requestJson,
            response_json = snapshot.responseJson,
            status = snapshot.status,
            error_message = snapshot.errorMessage,
            created_at = snapshot.createdAt,
            updated_at = snapshot.updatedAt,
        )
    }

    override suspend fun getAiJob(jobId: String): AiJobSnapshot? {
        val row = database.ai_jobQueries.selectAiJobById(jobId).executeAsOneOrNull() ?: return null
        return AiJobSnapshot(
            id = row.id,
            provider = row.provider,
            requestJson = row.request_json,
            responseJson = row.response_json,
            status = row.status,
            errorMessage = row.error_message,
            createdAt = row.created_at,
            updatedAt = row.updated_at,
        )
    }

    override suspend fun listPendingAiJobs(limit: Int): List<AiJobSnapshot> {
        return database.ai_jobQueries
            .selectPendingAiJobs()
            .executeAsList()
            .take(limit)
            .map { row ->
                AiJobSnapshot(
                    id = row.id,
                    provider = row.provider,
                    requestJson = row.request_json,
                    responseJson = row.response_json,
                    status = row.status,
                    errorMessage = row.error_message,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
    }

    override suspend fun listAiJobsByStatus(status: String, limit: Int, offset: Int): List<AiJobSnapshot> {
        return database.ai_jobQueries
            .selectAiJobsByStatus(status = status, limit = limit.toLong(), offset = offset.toLong())
            .executeAsList()
            .map { row ->
                AiJobSnapshot(
                    id = row.id,
                    provider = row.provider,
                    requestJson = row.request_json,
                    responseJson = row.response_json,
                    status = row.status,
                    errorMessage = row.error_message,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at,
                )
            }
    }

    override suspend fun deleteAiJob(jobId: String) {
        database.ai_jobQueries.deleteAiJobById(jobId)
    }

    override suspend fun upsertMediaAsset(snapshot: MediaAssetSnapshot) {
        database.media_assetQueries.upsertMediaAsset(
            id = snapshot.id,
            card_id = snapshot.cardId,
            media_type = snapshot.mediaType,
            local_uri = snapshot.localUri,
            thumb_uri = snapshot.thumbUri,
            width = snapshot.width,
            height = snapshot.height,
            duration_ms = snapshot.durationMs,
            size_bytes = snapshot.sizeBytes,
            sha256 = snapshot.sha256,
            created_at = snapshot.createdAt,
        )
    }

    override suspend fun getMediaAsset(id: String): MediaAssetSnapshot? {
        val row = database.media_assetQueries.selectMediaAssetById(id).executeAsOneOrNull() ?: return null
        return MediaAssetSnapshot(
            id = row.id,
            cardId = row.card_id,
            mediaType = row.media_type,
            localUri = row.local_uri,
            thumbUri = row.thumb_uri,
            width = row.width,
            height = row.height,
            durationMs = row.duration_ms,
            sizeBytes = row.size_bytes,
            sha256 = row.sha256,
            createdAt = row.created_at,
        )
    }

    override suspend fun listAllMediaAssets(limit: Int, offset: Int): List<MediaAssetSnapshot> {
        return database.media_assetQueries
            .selectAllMediaAssets(limit = limit.toLong(), offset = offset.toLong())
            .executeAsList()
            .map { row ->
                MediaAssetSnapshot(
                    id = row.id,
                    cardId = row.card_id,
                    mediaType = row.media_type,
                    localUri = row.local_uri,
                    thumbUri = row.thumb_uri,
                    width = row.width,
                    height = row.height,
                    durationMs = row.duration_ms,
                    sizeBytes = row.size_bytes,
                    sha256 = row.sha256,
                    createdAt = row.created_at,
                )
            }
    }

    override suspend fun deleteMediaAsset(id: String) {
        database.media_assetQueries.deleteMediaAssetById(id)
    }

    override suspend fun hasCard(cardId: String): Boolean {
        return database.cardQueries.selectCardById(cardId).executeAsOneOrNull() != null
    }
}
