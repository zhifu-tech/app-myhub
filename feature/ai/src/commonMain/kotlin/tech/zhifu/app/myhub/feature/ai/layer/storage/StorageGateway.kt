package tech.zhifu.app.myhub.feature.ai.layer.storage

import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Field

interface StorageGateway {
    suspend fun loadLatestDraftSession(): StoredDraftSession?
    suspend fun saveDraftSession(
        sessionId: String,
        state: ConversationState,
        draft: CaptureDraft?,
        missingFields: List<Field>,
    )

    suspend fun clearDraftSession(sessionId: String): Long
    suspend fun saveAiJob(snapshot: StoredAiJob)
    suspend fun savePublishedCard(card: Card, draft: CaptureDraft)
}

data class StoredDraftSession(
    val sessionId: String,
    val state: ConversationState,
    val draft: CaptureDraft?,
    val missingFields: List<Field>,
)

data class StoredAiJob(
    val id: String,
    val provider: String,
    val requestJson: String,
    val responseJson: String? = null,
    val status: String,
    val errorMessage: String? = null,
)
