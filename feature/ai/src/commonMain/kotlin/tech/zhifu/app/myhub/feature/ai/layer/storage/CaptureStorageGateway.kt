package tech.zhifu.app.myhub.feature.ai.layer.storage

import tech.zhifu.app.myhub.feature.ai.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.CaptureState
import tech.zhifu.app.myhub.datastore.model.domain.Card

interface CaptureStorageGateway {
    suspend fun loadLatestDraftSession(): StoredDraftSession?
    suspend fun saveDraftSession(
        sessionId: String,
        state: CaptureState,
        draft: CaptureDraft?,
        missingFields: List<String>,
    )
    suspend fun clearDraftSession(sessionId: String)
    suspend fun saveAiJob(snapshot: StoredAiJob)
    suspend fun savePublishedCard(card: Card, draft: CaptureDraft)
}

data class StoredDraftSession(
    val sessionId: String,
    val state: CaptureState,
    val draft: CaptureDraft?,
    val missingFields: List<String>,
)

data class StoredAiJob(
    val id: String,
    val provider: String,
    val requestJson: String,
    val responseJson: String? = null,
    val status: String,
    val errorMessage: String? = null,
)
