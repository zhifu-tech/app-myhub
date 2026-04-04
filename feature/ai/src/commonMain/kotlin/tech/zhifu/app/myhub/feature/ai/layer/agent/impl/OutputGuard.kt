package tech.zhifu.app.myhub.feature.ai.layer.agent.impl

class OutputGuard {
    fun ensureValid(
        parsed: ParsedAgentOutput,
        rawInput: String
    ): ParsedAgentOutput {
        val fixedTitle = parsed.title.ifBlank { "未命名捕获" }
        val fixedSummary = parsed.summary.ifBlank { rawInput.trim() }
        return parsed.copy(
            title = fixedTitle,
            summary = fixedSummary,
        )
    }
}


