package tech.zhifu.app.myhub.feature.ai.layer.storage.gateway.impl

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
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.layer.common.util.inferMimeType
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.layer.storage.StorageGateway
import tech.zhifu.app.myhub.feature.ai.layer.storage.StoredAiJob
import tech.zhifu.app.myhub.feature.ai.layer.storage.StoredDraftSession
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaGarbageCollector
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaPostProcessExecutor
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaPostProcessRequest
import kotlin.enums.enumEntries
import kotlin.time.Clock

class RepositoryStorageGateway(
    private val userRepository: UserRepository,
    private val cardRepository: CardRepository,
    private val captureLocalRepository: CaptureLocalRepository,
    private val mediaPostProcessExecutor: MediaPostProcessExecutor,
    private val mediaGarbageCollector: MediaGarbageCollector,
) : StorageGateway {
    private val json by lazy {
        Json {
            explicitNulls = false
            encodeDefaults = true
        }
    }

    override suspend fun loadLatestDraftSession(): StoredDraftSession? {
        val snapshot = captureLocalRepository.getLatestDraftSession()
            ?: return null
        val state = enumEntries<ConversationState>()
            .firstOrNull {
                it.name.equals(
                    other = snapshot.state,
                    ignoreCase = true
                )
            }
            ?: ConversationState.IDLE
        val draft = runCatching {
            json.decodeFromString(
                deserializer = CaptureDraft.serializer(),
                string = snapshot.draftJson
            )
        }.getOrNull()
        val missingFields = snapshot.missingFieldsJson
            ?.let {
                runCatching {
                    json.decodeFromString(
                        deserializer = ListSerializer(
                            elementSerializer = String.serializer()
                        ),
                        string = it
                    )
                }.getOrNull()
            }
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
        state: ConversationState,
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
            draftJson = json.encodeToString(
                serializer = CaptureDraft.serializer(),
                value = safeDraft
            ),
            missingFieldsJson = json.encodeToString(
                serializer = ListSerializer(
                    elementSerializer = String.serializer()
                ),
                value = missingFields
            ),
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )
        captureLocalRepository.upsertDraftSession(snapshot)
    }

    override suspend fun clearDraftSession(
        sessionId: String
    ) = captureLocalRepository.deleteDraftSession(sessionId)

    override suspend fun saveAiJob(
        snapshot: StoredAiJob
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        captureLocalRepository.upsertAiJob(
            snapshot = AiJobSnapshot(
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

    override suspend fun savePublishedCard(
        card: Card,
        draft: CaptureDraft
    ) {
        val user = userRepository.requireUser()
        runCatching {
            cardRepository.insertCard(card = card, userId = user.id)
            draft.mediaAssets.forEachIndexed { index, media ->
                val now = Clock.System.now().toEpochMilliseconds()
                captureLocalRepository.upsertMediaAsset(
                    snapshot = MediaAssetSnapshot(
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
                    snapshot = AiJobSnapshot(
                        id = "media_postprocess_${card.id}_$index",
                        provider = "local_media_pipeline",
                        requestJson = json.encodeToString(
                            value = MediaPostProcessRequest(
                                cardId = card.id,
                                mediaId = "media_${card.id}_$index",
                                mediaUri = media.localUri,
                            )
                        ),
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
                cardRepository.deleteCard(
                    userId = user.id,
                    cardId = card.id,
                    needSync = false
                )
            }
            throw it
        }
    }
}
