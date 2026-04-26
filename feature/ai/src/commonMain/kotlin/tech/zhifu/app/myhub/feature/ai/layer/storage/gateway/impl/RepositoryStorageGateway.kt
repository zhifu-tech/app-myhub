package tech.zhifu.app.myhub.feature.ai.layer.storage.gateway.impl

import tech.zhifu.app.myhub.datastore.file.storage.isExternalStorageHandle
import tech.zhifu.app.myhub.datastore.file.storage.resolveStorageHandleToAccessUrl
import tech.zhifu.app.myhub.datastore.file.storage.storageHandleFromAccessUrl
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.MediaAsset
import tech.zhifu.app.myhub.datastore.model.serializer.deserialize
import tech.zhifu.app.myhub.datastore.model.serializer.serialize
import tech.zhifu.app.myhub.datastore.repository.capture.AiJobSnapshot
import tech.zhifu.app.myhub.datastore.repository.capture.CaptureLocalRepository
import tech.zhifu.app.myhub.datastore.repository.capture.DraftSessionSnapshot
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.ai.layer.common.util.inferMimeType
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.layer.storage.StorageGateway
import tech.zhifu.app.myhub.feature.ai.layer.storage.StoredAiJob
import tech.zhifu.app.myhub.feature.ai.layer.storage.StoredDraftSession
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaFileStore
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaGarbageCollector
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaImportSource
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaPostProcessExecutor
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaPostProcessRequest
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.CaptureMediaAsset
import tech.zhifu.app.myhub.feature.ai.model.Field
import kotlin.enums.enumEntries
import kotlin.time.Clock

class RepositoryStorageGateway(
    private val userRepository: UserRepository,
    private val cardRepository: CardRepository,
    private val captureLocalRepository: CaptureLocalRepository,
    private val mediaPostProcessExecutor: MediaPostProcessExecutor,
    private val mediaGarbageCollector: MediaGarbageCollector,
    private val mediaFileStore: MediaFileStore,
) : StorageGateway {
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
        val draft = snapshot.draftJson
            .deserialize<CaptureDraft>()
            .getOrNull()
        val missingFields = snapshot.missingFieldsJson
            ?.let { json ->
                json.deserialize<List<Field>>().getOrNull()
                    ?: json.deserialize<List<String>>().getOrNull()
                        ?.map(Field::valueOf)
            }
            ?: emptyList()
        val restoredDraft = draft
            ?.hydrateResolvedUrls()
            ?.let { markMissingMediaAssets(it, mediaFileStore) }
        return StoredDraftSession(
            sessionId = snapshot.id,
            state = state,
            draft = restoredDraft,
            missingFields = missingFields,
            invalidMediaCount = restoredDraft?.mediaAssets?.count { it.isMissing } ?: 0,
        )
    }

    override suspend fun saveDraftSession(
        sessionId: String,
        state: ConversationState,
        draft: CaptureDraft?,
        missingFields: List<Field>,
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
            draftJson = safeDraft.normalizeStoredRefs().serialize().orEmpty(),
            missingFieldsJson = missingFields.serialize().orEmpty(),
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
        val importedUris = mutableListOf<String>()
        runCatching {
            val mediaIdPrefix = "media_${card.id}_"
            val importedDraft = draft.copy(
                mediaAssets = draft.mediaAssets.mapIndexed { index, media ->
                    val mediaId = "$mediaIdPrefix$index"
                    val imported = mediaFileStore.importToManagedStorage(
                        cardId = card.id,
                        mediaId = mediaId,
                        source = MediaImportSource(
                            storageHandle = media.storageHandle,
                            accessUrl = media.accessUrl,
                            sizeBytes = media.sizeBytes,
                        ),
                    )
                    importedUris += imported.storageHandle
                    media.copy(
                        storageHandle = imported.storageHandle,
                        accessUrl = imported.accessUrl,
                        sizeBytes = imported.sizeBytes,
                    )
                }
            )
            cardRepository.insertCard(card = card, userId = user.id)
            importedDraft.mediaAssets.forEachIndexed { index, media ->
                val now = Clock.System.now().toEpochMilliseconds()
                captureLocalRepository.upsertMediaAsset(
                    snapshot = MediaAsset(
                        id = "$mediaIdPrefix$index",
                        cardId = card.id,
                        mediaType = media.mediaType.ifBlank { inferMimeType(media.accessUrl) },
                        storageHandle = media.storageHandle,
                        accessUrl = media.accessUrl,
                        sizeBytes = media.sizeBytes,
                        sha256 = media.sha256,
                        createdAt = now,
                    )
                )
                captureLocalRepository.upsertAiJob(
                    snapshot = AiJobSnapshot(
                        id = "media_postprocess_${card.id}_$index",
                        provider = "local_media_pipeline",
                        requestJson = MediaPostProcessRequest(
                            cardId = card.id,
                            mediaId = "$mediaIdPrefix$index",
                            mediaStorageHandle = media.storageHandle,
                        ).serialize().orEmpty(),
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
            importedUris.forEach { uri ->
                runCatching { mediaFileStore.deleteIfExists(storageHandle = uri) }
            }
            throw it
        }
    }
}

private suspend fun markMissingMediaAssets(
    draft: CaptureDraft,
    mediaFileStore: MediaFileStore,
): CaptureDraft {
    val updatedAssets = draft.mediaAssets.map { asset ->
        if (!asset.shouldValidateExistence()) {
            asset.copy(isMissing = false)
        } else {
            asset.copy(
                isMissing = !mediaFileStore.fileExists(
                    storageHandle = asset.storageHandle,
                    accessUrl = asset.accessUrl,
                )
            )
        }
    }
    return draft.copy(mediaAssets = updatedAssets)
}

private fun CaptureDraft.normalizeStoredRefs(): CaptureDraft = copy(
    mediaAssets = mediaAssets.map { asset ->
        val storageHandle = asset.storageHandle.ifBlank {
            storageHandleFromAccessUrl(asset.accessUrl)
        }
        asset.copy(
            storageHandle = storageHandle,
            accessUrl = if (isExternalStorageHandle(storageHandle)) {
                storageHandle
            } else {
                ""
            }
        )
    }
)

private suspend fun CaptureDraft.hydrateResolvedUrls(): CaptureDraft = copy(
    mediaAssets = mediaAssets.map { asset ->
        asset.copy(accessUrl = resolveStorageHandleToAccessUrl(asset.storageHandle).orEmpty())
    }
)

private fun CaptureMediaAsset.shouldValidateExistence(): Boolean {
    val uri = accessUrl.trim()
    if (uri.isBlank()) return false
    return !(uri.startsWith("http://") ||
        uri.startsWith("https://") ||
        uri.startsWith("data:") ||
        uri.startsWith("blob:"))
}
