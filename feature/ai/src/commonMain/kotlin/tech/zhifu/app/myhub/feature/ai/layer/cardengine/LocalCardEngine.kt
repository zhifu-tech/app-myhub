package tech.zhifu.app.myhub.feature.ai.layer.cardengine

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.feature.ai.CaptureDraft
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardContent
import tech.zhifu.app.myhub.datastore.model.domain.CardContentType
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.datastore.model.domain.CardSourceKind
import tech.zhifu.app.myhub.datastore.model.domain.CardStatus
import tech.zhifu.app.myhub.datastore.model.domain.CardType
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

    override fun updateDraftTitle(draft: CaptureDraft, title: String): CaptureDraft {
        return validator.validateDraft(draft.copy(title = fieldFormatter.normalizeTitle(title)))
    }

    override fun appendTag(draft: CaptureDraft, tag: String): CaptureDraft {
        val normalized = fieldFormatter.normalizeTag(tag)
        if (normalized.isBlank()) return draft
        if (normalized in draft.tags) return draft
        return validator.validateDraft(draft.copy(tags = draft.tags + normalized))
    }

    override fun prePublishCheck(draft: CaptureDraft): PrePublishCheckResult {
        return prePublishChecker.check(draft)
    }

    override fun toPublishedCard(draft: CaptureDraft): Card {
        val check = prePublishCheck(draft)
        if (!check.ok) {
            throw IllegalStateException("pre_publish_check_failed:${check.issues.joinToString(",")}")
        }
        val safe = validator.validateDraft(draft)
        val now = Clock.System.now()
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
            tagsRaw = json.encodeToString(ListSerializer(String.serializer()), safe.tags),
            uiRaw = null,
            contentRaw = json.encodeToString(
                CardContent.serializer(),
                CardContent(type = CardContentType.TEXT, value = safe.summary),
            ),
            sourceRaw = json.encodeToString(
                CardSource.serializer(),
                CardSource(kind = CardSourceKind.MANUAL)
            ),
        )
    }
}

class CardValidator(
    private val fieldFormatter: CardFieldFormatter,
) {
    fun validateDraft(draft: CaptureDraft): CaptureDraft {
        val title = fieldFormatter.normalizeTitle(draft.title)
        val summary = fieldFormatter.normalizeSummary(draft.summary, draft.sourceText)
        val tags = fieldFormatter.normalizeTags(draft.tags)
        return draft.copy(
            title = title.trim(),
            summary = summary.trim(),
            tags = tags,
        )
    }
}

class CardFieldFormatter {
    fun normalizeTitle(title: String): String {
        return title.trim().ifBlank { "未命名捕获" }.take(64)
    }

    fun normalizeSummary(summary: String, sourceText: String): String {
        return summary.trim().ifBlank { sourceText.trim() }.take(5000)
    }

    fun normalizeTags(tags: List<String>): List<String> {
        return tags
            .map(::normalizeTag)
            .filter { it.isNotBlank() }
            .distinct()
            .take(20)
    }

    fun normalizeTag(tag: String): String {
        return tag
            .trim()
            .replace("#", "")
            .replace(Regex("\\s+"), " ")
            .take(24)
    }
}

class CardPrePublishChecker {
    fun check(draft: CaptureDraft): PrePublishCheckResult {
        val issues = buildList {
            if (draft.title.trim().isBlank()) add("title_missing")
            if (draft.summary.trim().isBlank() && draft.sourceText.trim().isBlank()) add("content_missing")
            if (draft.tags.isEmpty()) add("tags_missing")
            if (draft.tags.size > 20) add("tags_too_many")
            if (draft.summary.length > 5000) add("summary_too_long")
            if (draft.title.length > 64) add("title_too_long")
        }
        return PrePublishCheckResult(
            ok = issues.isEmpty(),
            issues = issues,
        )
    }
}
