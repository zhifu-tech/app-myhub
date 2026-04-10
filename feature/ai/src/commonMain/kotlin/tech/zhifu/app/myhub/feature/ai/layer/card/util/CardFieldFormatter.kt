package tech.zhifu.app.myhub.feature.ai.layer.card.util

class CardFieldFormatter {
    fun normalizeTitle(
        title: String
    ): String = title
        .trim()
        .ifBlank { "Untitled capture" }
        .take(64)

    fun normalizeSummary(
        summary: String,
        sourceText: String
    ): String = summary
        .trim()
        .ifBlank { sourceText.trim() }
        .take(5000)

    fun normalizeTags(
        tags: List<String>
    ): List<String> = tags
        .map(::normalizeTag)
        .filter { it.isNotBlank() }
        .distinct()
        .take(20)

    fun normalizeTag(
        tag: String
    ): String = tag
        .trim()
        .replace("#", "")
        .replace(Regex("\\s+"), " ")
        .take(24)
}
