package tech.zhifu.app.myhub.feature.ai.layer.storage

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.repository.capture.AiJobSnapshot
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureLocalRepository
import tech.zhifu.app.myhub.datastore.repository.capture.DraftSessionSnapshot
import tech.zhifu.app.myhub.datastore.repository.capture.MediaAssetSnapshot
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.ai.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.CaptureState
import kotlin.time.Clock

class RepositoryCaptureStorageGateway(
    private val userRepository: UserRepository,
    private val cardRepository: CardRepository,
    private val captureLocalRepository: CaptureLocalRepository,
    private val mediaPostProcessExecutor: MediaPostProcessExecutor,
    private val mediaGarbageCollector: MediaGarbageCollector,
) : CaptureStorageGateway {
    private val json = Json {
        explicitNulls = false
        encodeDefaults = true
    }

    override suspend fun loadLatestDraftSession(): StoredDraftSession? {
        val snapshot = captureLocalRepository.getLatestDraftSession() ?: return null
        val state = runCatching { CaptureState.valueOf(snapshot.state) }.getOrDefault(CaptureState.IDLE)
        val draft = runCatching {
            json.decodeFromString(CaptureDraft.serializer(), snapshot.draftJson)
        }.getOrNull()
        val missingFields = snapshot.missingFieldsJson
            ?.let { runCatching { json.decodeFromString(ListSerializer(String.serializer()), it) }.getOrNull() }
            ?: emptyList()
        return StoredDraftSession(
            sessionId = snapshot.id,
            state = state,
            draft = draft,
            missingFields = missingFields,
        )
    }

    override suspend fun saveDraftSession(
        sessionId: String,
        state: CaptureState,
        draft: CaptureDraft?,
        missingFields: List<String>,
    ) {
        val safeDraft = draft ?: CaptureDraft(
            id = sessionId,
            title = "",
            summary = "",
            tags = emptyList(),
            sourceText = "",
            mediaAssets = emptyList(),
        )
        val snapshot = DraftSessionSnapshot(
            id = sessionId,
            state = state.name,
            draftJson = json.encodeToString(CaptureDraft.serializer(), safeDraft),
            missingFieldsJson = json.encodeToString(ListSerializer(String.serializer()), missingFields),
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
        captureLocalRepository.upsertDraftSession(snapshot)
    }

    override suspend fun clearDraftSession(sessionId: String) {
        captureLocalRepository.deleteDraftSession(sessionId)
    }

    override suspend fun saveAiJob(snapshot: StoredAiJob) {
        val now = Clock.System.now().toEpochMilliseconds()
        captureLocalRepository.upsertAiJob(
            AiJobSnapshot(
                id = snapshot.id,
                provider = snapshot.provider,
                requestJson = snapshot.requestJson,
                responseJson = snapshot.responseJson,
                status = snapshot.status,
                errorMessage = snapshot.errorMessage,
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    override suspend fun savePublishedCard(card: Card, draft: CaptureDraft) {
        val user = userRepository.requireUser()
        runCatching {
            cardRepository.insertCard(card = card, userId = user.id)
            draft.mediaAssets.forEachIndexed { index, media ->
                val now = Clock.System.now().toEpochMilliseconds()
                captureLocalRepository.upsertMediaAsset(
                    MediaAssetSnapshot(
                        id = "media_${card.id}_$index",
                        cardId = card.id,
                        mediaType = media.mediaType.ifBlank { inferMimeType(media.localUri) },
                        localUri = media.localUri,
                        sizeBytes = media.sizeBytes,
                        sha256 = media.sha256,
                        createdAt = now,
                    )
                )
                captureLocalRepository.upsertAiJob(
                    AiJobSnapshot(
                        id = "media_postprocess_${card.id}_$index",
                        provider = "local_media_pipeline",
                        requestJson = """{"cardId":"${card.id}","mediaId":"media_${card.id}_$index","mediaUri":"${media.localUri}"}""",
                        responseJson = null,
                        status = "queued",
                        errorMessage = null,
                        createdAt = now,
                        updatedAt = now,
                    )
                )
            }
            mediaPostProcessExecutor.processQueuedJobs(limit = 50)
            mediaGarbageCollector.collect(limit = 500)
        }.onFailure {
            runCatching {
                cardRepository.deleteCard(userId = user.id, cardId = card.id, needSync = false)
            }
            throw it
        }
    }

    private fun inferMimeType(uri: String): String {
        val normalized = uri.lowercase()
        return when {
            normalized.endsWith(".jpg") || normalized.endsWith(".jpeg") -> "image/jpeg"
            normalized.endsWith(".png") -> "image/png"
            normalized.endsWith(".webp") -> "image/webp"
            normalized.endsWith(".mp4") -> "video/mp4"
            normalized.endsWith(".mov") -> "video/quicktime"
            else -> "application/octet-stream"
        }
    }
}
