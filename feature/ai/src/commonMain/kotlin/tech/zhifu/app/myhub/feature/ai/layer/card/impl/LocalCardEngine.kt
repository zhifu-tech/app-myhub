package tech.zhifu.app.myhub.feature.ai.layer.card.impl

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardContent
import tech.zhifu.app.myhub.datastore.model.domain.CardContentType
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.datastore.model.domain.CardSourceKind
import tech.zhifu.app.myhub.datastore.model.domain.CardStatus
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.layer.card.CardEngine
import tech.zhifu.app.myhub.feature.ai.layer.card.PrePublishCheckResult
import tech.zhifu.app.myhub.feature.ai.layer.card.util.CardFieldFormatter
import tech.zhifu.app.myhub.feature.ai.layer.card.util.CardPrePublishChecker
import tech.zhifu.app.myhub.feature.ai.layer.card.util.CardValidator
import kotlin.time.Clock

class LocalCardEngine(
    private val fieldFormatter: CardFieldFormatter,
    private val prePublishChecker: CardPrePublishChecker,
    private val validator: CardValidator,
) : CardEngine {
    private val json = Json {
        explicitNulls = false
        encodeDefaults = true
    }

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
            throw IllegalStateException(
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
            tagsRaw = json.encodeToString(
                serializer = ListSerializer(elementSerializer = String.serializer()),
                value = safe.tags
            ),
            uiRaw = null,
            contentRaw = json.encodeToString(
                // fixme ? 为什么写死, 这里还有UI的元素的
                serializer = CardContent.serializer(),
                value = CardContent(
                    type = CardContentType.TEXT, // fixme ? 为什么写死
                    value = safe.summary
                ),
            ),
            sourceRaw = json.encodeToString(
                serializer = CardSource.serializer(),
                value = CardSource(
                    kind = CardSourceKind.MANUAL, // fixme ? 为什么写死
                )
            ),
        )
    }
}

