package tech.zhifu.app.myhub.feature.ai.layer.cardengine

import tech.zhifu.app.myhub.feature.ai.CaptureDraft
import tech.zhifu.app.myhub.datastore.model.domain.Card

interface CardEngine {
    fun updateDraftTitle(draft: CaptureDraft, title: String): CaptureDraft
    fun appendTag(draft: CaptureDraft, tag: String): CaptureDraft
    fun prePublishCheck(draft: CaptureDraft): PrePublishCheckResult
    fun toPublishedCard(draft: CaptureDraft): Card
}

data class PrePublishCheckResult(
    val ok: Boolean,
    val issues: List<String> = emptyList(),
)
