package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.generateImage

import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.CaptureType

internal object CaptureImagePromptBuilder {
    fun hasSemanticSeed(
        draft: CaptureDraft,
    ): Boolean {
        return draft.title.isNotBlank() ||
            draft.summary.isNotBlank() ||
            draft.sourceText.isNotBlank() ||
            draft.tags.isNotEmpty() ||
            draft.captureType != null ||
            draft.location?.name.isNullOrBlank().not()
    }

    fun build(
        draft: CaptureDraft,
        language: String,
    ): String {
        val blueprint = blueprint(draft = draft)
        if (blueprint.isBlank()) return ""
        return buildString {
            append("Create a standalone image with one clear focal subject or scene. ")
            append("Make it visually strong both as a thumbnail and as a full-size preview. ")
            append("Use detailed composition, realistic materials, intentional lighting, layered depth, and clean negative space. ")
            append("Prefer a square 1024x1024-friendly composition. ")
            append("Avoid UI, poster layout, collage, borders, watermark, logo, stock-photo staging, and unrelated objects. ")
            append("Do not render any text unless exact wording is explicitly requested. ")
            append(blueprint)
            languageHint(language)?.let {
                append(" ")
                append(it)
            }
        }
    }

    fun buildRefinerBrief(
        draft: CaptureDraft,
        language: String,
    ): String {
        return buildString {
            appendLine("Goal: write one high-quality English prompt for an image model.")
            appendLine("Output target: a standalone image, not a UI mockup, not a poster, not a card cover unless explicitly requested.")
            appendLine("Source language: ${languageName(language)}.")
            appendLine("Square framing target: 1024x1024.")
            draft.title.trim().takeIf { it.isNotBlank() }?.let {
                appendLine("Title cue: $it")
            }
            draft.summary.trim().takeIf { it.isNotBlank() }?.let {
                appendLine("Summary cue: $it")
            }
            draft.sourceText.trim().takeIf { it.isNotBlank() }?.let {
                appendLine("Context cue: ${it.take(420)}")
            }
            draft.location?.name?.trim()?.takeIf { it.isNotBlank() }?.let {
                appendLine("Location cue: $it")
            }
            draft.tags
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .take(8)
                .takeIf { it.isNotEmpty() }
                ?.let {
                    appendLine("Tag cues: ${it.joinToString(", ")}")
                }
            draft.captureType?.let {
                appendLine("Capture type: ${it.name.lowercase()}")
                appendLine("Visual direction: ${captureTypeVisualDirection(it)}")
                appendLine("Color palette hint: ${captureTypePalette(it)}")
            }
            appendLine("Deterministic base prompt: ${build(draft = draft, language = language)}")
        }.trim()
    }

    private fun blueprint(
        draft: CaptureDraft,
    ): String {
        val clauses = buildList {
            primarySubjectClause(draft)?.let(::add)
            narrativeClause(draft)?.let(::add)
            locationClause(draft)?.let(::add)
            tagsClause(draft)?.let(::add)
            draft.captureType?.let { type ->
                add(captureTypeVisualDirection(type))
                add("Use a restrained palette centered on ${captureTypePalette(type)}.")
            }
        }
        return clauses.joinToString(" ")
    }

    private fun primarySubjectClause(
        draft: CaptureDraft,
    ): String? {
        val title = draft.title.trim()
        val summary = draft.summary.trim()
        val source = draft.sourceText.trim()
        return when {
            title.isNotBlank() && summary.isNotBlank() ->
                "Center the image around \"$title\", expressed through $summary."

            title.isNotBlank() ->
                "Center the image around the idea of \"$title\"."

            summary.isNotBlank() ->
                "Translate this idea into a concrete visual scene: $summary."

            source.isNotBlank() ->
                "Turn this context into a specific visual scene: ${source.take(260)}."

            else -> null
        }
    }

    private fun narrativeClause(
        draft: CaptureDraft,
    ): String? {
        val cues = buildList {
            draft.summary.trim().takeIf { it.isNotBlank() }?.let { add(it) }
            draft.sourceText.trim().takeIf { it.isNotBlank() }?.let { add(it.take(220)) }
        }
        return cues
            .distinct()
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = " ")
            ?.let { "Keep the narrative grounded in: $it." }
    }

    private fun locationClause(
        draft: CaptureDraft,
    ): String? {
        val location = draft.location?.name?.trim().orEmpty()
        if (location.isBlank()) return null
        return "Anchor the environment in $location with believable local details."
    }

    private fun tagsClause(
        draft: CaptureDraft,
    ): String? {
        val tags = draft.tags
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(8)
        if (tags.isEmpty()) return null
        return "Subtly incorporate cues of ${tags.joinToString(", ")}."
    }

    private fun captureTypeVisualDirection(
        type: CaptureType,
    ): String = when (type) {
        CaptureType.PLACE ->
            "Use grounded environmental photography, tactile surfaces, natural atmosphere, and a clear sense of place."

        CaptureType.PERSON ->
            "Use an editorial portrait or documentary-style human scene with authentic expression, subtle gesture, and believable styling."

        CaptureType.ARTICLE ->
            "Use a conceptual editorial still life or scene that communicates the topic without relying on UI or screenshots."

        CaptureType.IDEA ->
            "Use a bold conceptual image with one strong metaphor, simplified forms, and memorable visual contrast."
    }

    private fun captureTypePalette(
        type: CaptureType,
    ): String = when (type) {
        CaptureType.PLACE -> "#64748B, #A3B18A, #E7DCC7"
        CaptureType.PERSON -> "#7C5C4D, #D8B89B, #F5EDE5"
        CaptureType.ARTICLE -> "#0F172A, #CBD5E1, #F8FAFC"
        CaptureType.IDEA -> "#1D4ED8, #F59E0B, #FFF7ED"
    }

    private fun languageHint(
        language: String,
    ): String? = when {
        language.startsWith("zh", ignoreCase = true) ->
            "Preserve East Asian context cues where relevant, but keep the prompt itself visually universal."

        language.startsWith("en", ignoreCase = true) ->
            "Preserve culturally specific cues from the source where relevant."

        else -> null
    }

    private fun languageName(
        language: String,
    ): String = when {
        language.startsWith("zh-CN", ignoreCase = true) -> "Simplified Chinese"
        language.startsWith("zh-TW", ignoreCase = true) -> "Traditional Chinese"
        language.startsWith("zh", ignoreCase = true) -> "Chinese"
        language.startsWith("en", ignoreCase = true) -> "English"
        else -> language
    }
}
