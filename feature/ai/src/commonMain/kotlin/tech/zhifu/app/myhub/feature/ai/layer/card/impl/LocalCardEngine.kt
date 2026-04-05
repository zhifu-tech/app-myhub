package tech.zhifu.app.myhub.feature.ai.layer.card.impl

import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardContent
import tech.zhifu.app.myhub.datastore.model.domain.CardContentType
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.datastore.model.domain.CardSourceKind
import tech.zhifu.app.myhub.datastore.model.domain.CardStatus
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.datastore.model.serializer.serialize
import tech.zhifu.app.myhub.feature.ai.layer.card.CardEngine
import tech.zhifu.app.myhub.feature.ai.layer.card.PrePublishCheckResult
import tech.zhifu.app.myhub.feature.ai.layer.card.util.CardFieldFormatter
import tech.zhifu.app.myhub.feature.ai.layer.card.util.CardPrePublishChecker
import tech.zhifu.app.myhub.feature.ai.layer.card.util.CardValidator
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import kotlin.time.Clock

class LocalCardEngine(
    private val fieldFormatter: CardFieldFormatter,
    private val prePublishChecker: CardPrePublishChecker,
    private val validator: CardValidator,
) : CardEngine {
    override fun updateDraftTitle(
        draft: CaptureDraft,
        title: String
    ): CaptureDraft {
        return validator.validateDraft(
            draft = draft.copy(
                title = fieldFormatter.normalizeTitle(title = title)
            )
        )
    }

    override fun appendTag(
        draft: CaptureDraft,
        tag: String
    ): CaptureDraft {
        val normalized = fieldFormatter.normalizeTag(tag = tag)
        if (normalized.isBlank()) return draft
        if (normalized in draft.tags) return draft
        return validator.validateDraft(
            draft = draft.copy(
                tags = draft.tags + normalized
            )
        )
    }

    override fun prePublishCheck(
        draft: CaptureDraft
    ): PrePublishCheckResult {
        return prePublishChecker.check(draft)
    }

    override fun toPublishedCard(
        draft: CaptureDraft
    ): Card {
        val check = prePublishCheck(draft)
        if (!check.ok) {
            error(
                "pre_publish_check_failed:${check.issues.joinToString(",")}"
            )
        }
        val safe = validator.validateDraft(draft)
        val now = Clock.System.now()
        return Card(
            id = "card_${now.toEpochMilliseconds()}_${safe.id}",
            type = CardType.NOTE,       // fixme ? 为什么写死
            status = CardStatus.PUBLISHED,
            title = safe.title,
            summary = safe.summary,
            version = 1,
            deletedAt = null,
            createdAt = now,
            updatedAt = now,
            locationRaw = null,
            tagsRaw = safe.tags.serialize().orEmpty(),
            uiRaw = null,
            contentRaw = CardContent(
                // fixme ? 为什么写死, 这里还有UI的元素的
                type = CardContentType.TEXT, // fixme ? 为什么写死
                value = safe.summary
            ).serialize().orEmpty(),
            sourceRaw = CardSource(
                kind = CardSourceKind.MANUAL, // fixme ? 为什么写死
            ).serialize().orEmpty(),
        )
    }
}
