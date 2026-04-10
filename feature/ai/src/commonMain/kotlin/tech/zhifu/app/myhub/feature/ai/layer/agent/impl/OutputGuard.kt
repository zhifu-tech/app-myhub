package tech.zhifu.app.myhub.feature.ai.layer.agent.impl

class OutputGuard {
    fun ensureValid(
        parsed: ParsedAgentOutput,
        rawInput: String
    ): ParsedAgentOutput {
        val fixedTitle = parsed.title.ifBlank { "Untitled capture" }
        val fixedSummary = parsed.summary.ifBlank { rawInput.trim() }
        return parsed.copy(
            title = fixedTitle,
            summary = fixedSummary,
        )
    }
}

