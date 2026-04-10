package tech.zhifu.app.myhub.feature.ai.layer.card.impl

import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardContent
import tech.zhifu.app.myhub.datastore.model.domain.CardContentType
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.datastore.model.domain.CardSourceKind
import tech.zhifu.app.myhub.datastore.model.domain.CardStatus
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.datastore.model.domain.CardUi
import tech.zhifu.app.myhub.datastore.model.domain.Cover
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
        val coverUrl = safe.mediaAssets.firstOrNull { it.mediaType.startsWith("image/") }?.localUri
            ?: safe.mediaAssets.firstOrNull()?.localUri
        val contentType = when {
            coverUrl != null && safe.summary.isBlank() && safe.sourceText.isBlank() -> CardContentType.IMAGE
            else -> CardContentType.TEXT
        }
        val contentValue = safe.summary
            .ifBlank { safe.sourceText }
            .ifBlank { coverUrl.orEmpty() }
        val sourceKind = when {
            safe.sourceText.isHttpUrl() -> CardSourceKind.LINK
            safe.mediaAssets.isNotEmpty() -> CardSourceKind.IMPORT
            else -> CardSourceKind.MANUAL
        }
        val sourceRef = when {
            safe.sourceText.isHttpUrl() -> safe.sourceText
            coverUrl != null -> coverUrl
            else -> null
        }
        return Card(
            id = "card_${now.toEpochMilliseconds()}_${safe.id}",
            type = CardType.NOTE,
            status = CardStatus.PUBLISHED,
            title = safe.title,
            summary = safe.summary,
            version = 1,
            deletedAt = null,
            createdAt = now,
            updatedAt = now,
            locationRaw = null,
            tagsRaw = safe.tags.serialize().orEmpty(),
            uiRaw = coverUrl?.let { url ->
                CardUi(
                    cover = Cover(
                        iconKey = "",
                        bgColor = "#EFF6FF",
                        tintColor = "",
                        imageUrl = url,
                    )
                ).serialize().orEmpty()
            },
            contentRaw = CardContent(
                type = contentType,
                value = contentValue,
            ).serialize().orEmpty(),
            sourceRaw = CardSource(
                kind = sourceKind,
                ref = sourceRef,
            ).serialize().orEmpty(),
        )
    }
}

private fun String.isHttpUrl(): Boolean {
    val value = trim()
    return value.startsWith("http://", ignoreCase = true) ||
        value.startsWith("https://", ignoreCase = true)
}
