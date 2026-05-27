package tech.zhifu.app.myhub.feature.ai.layer.card.util

import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.layer.card.PrePublishCheckResult

class CardPrePublishChecker {
    fun check(
        draft: CaptureDraft
    ): PrePublishCheckResult {
        val issues = buildList {
            if (draft.title.trim().isBlank()) {
                add("title_missing")
            }
            if (draft.summary.trim().isBlank() &&
                draft.sourceText.trim().isBlank()
            ) {
                add("content_missing")
            }
            if (draft.tags.isEmpty()) {
                add("tags_missing")
            }
            if (draft.tags.size > 20) {
                add("tags_too_many")
            }
            if (draft.summary.length > 5000) {
                add("summary_too_long")
            }
            if (draft.title.length > 64) {
                add("title_too_long")
            }
        }
        return PrePublishCheckResult(
            ok = issues.isEmpty(),
            issues = issues,
        )
    }
}
