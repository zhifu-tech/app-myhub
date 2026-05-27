package tech.zhifu.app.myhub.feature.ai.layer.card.util

import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft

class CardValidator(
    private val fieldFormatter: CardFieldFormatter,
) {
    fun validateDraft(
        draft: CaptureDraft
    ): CaptureDraft {
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
