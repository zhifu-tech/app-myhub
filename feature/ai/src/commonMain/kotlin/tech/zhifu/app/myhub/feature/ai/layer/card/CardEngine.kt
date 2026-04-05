package tech.zhifu.app.myhub.feature.ai.layer.card

import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft

interface CardEngine {
    fun updateDraftTitle(
        draft: CaptureDraft,
        title: String
    ): CaptureDraft

    fun appendTag(
        draft: CaptureDraft,
        tag: String
    ): CaptureDraft

    fun prePublishCheck(
        draft: CaptureDraft
    ): PrePublishCheckResult

    fun toPublishedCard(
        draft: CaptureDraft
    ): Card
}

data class PrePublishCheckResult(
    val ok: Boolean,
    val issues: List<String> = emptyList(),
)
