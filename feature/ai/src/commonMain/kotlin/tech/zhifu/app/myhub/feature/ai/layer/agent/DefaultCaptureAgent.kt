package tech.zhifu.app.myhub.feature.ai.layer.agent

import tech.zhifu.app.myhub.feature.ai.CaptureDraft
import kotlin.time.Clock

class DefaultCaptureAgent(
    private val promptAssembler: PromptAssembler,
    private val toolPlanner: ToolPlanner,
    private val responseParser: ResponseParser,
    private val outputGuard: OutputGuard,
) : CaptureAgent {
    override fun analyzeToDraft(input: String): AgentDraftSuggestion {
        val prompt = promptAssembler.assemble(input)
        val plan = toolPlanner.plan(prompt)
        val parsed = responseParser.parse(input = input, plan = plan)
        val safe = outputGuard.ensureValid(parsed, input)
        return AgentDraftSuggestion(
            intent = safe.intent,
            draft = CaptureDraft(
                id = "draft_${Clock.System.now().toEpochMilliseconds()}",
                title = safe.title,
                summary = safe.summary,
                tags = safe.tags,
                sourceText = input,
                mediaAssets = emptyList(),
            ),
        )
    }
}

class PromptAssembler {
    fun assemble(input: String): String = "task=capture_analysis;input=$input"
}

class ToolPlanner {
    fun plan(prompt: String): String {
        return if (prompt.contains("capture_analysis")) "create_draft" else "manual"
    }
}

class ResponseParser {
    fun parse(input: String, plan: String): ParsedAgentOutput {
        val title = input
            .lineSequence()
            .firstOrNull()
            .orEmpty()
            .trim()
            .take(24)
            .ifBlank { "未命名捕获" }
        val tags = input
            .replace("，", " ")
            .replace("。", " ")
            .replace(",", " ")
            .replace(".", " ")
            .split(" ")
            .map { it.trim() }
            .filter { it.length in 2..8 }
            .distinct()
            .take(3)
        return ParsedAgentOutput(
            intent = if (plan == "create_draft") "create_card" else "manual_edit",
            title = title,
            summary = input.trim(),
            tags = tags,
        )
    }
}

class OutputGuard {
    fun ensureValid(parsed: ParsedAgentOutput, rawInput: String): ParsedAgentOutput {
        val fixedTitle = parsed.title.ifBlank { "未命名捕获" }
        val fixedSummary = parsed.summary.ifBlank { rawInput.trim() }
        return parsed.copy(
            title = fixedTitle,
            summary = fixedSummary,
        )
    }
}

data class ParsedAgentOutput(
    val intent: String,
    val title: String,
    val summary: String,
    val tags: List<String>,
)
