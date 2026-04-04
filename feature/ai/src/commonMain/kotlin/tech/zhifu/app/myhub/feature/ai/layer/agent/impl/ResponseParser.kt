package tech.zhifu.app.myhub.feature.ai.layer.agent.impl

class ResponseParser {
    fun parse(
        input: String,
        plan: String
    ): ParsedAgentOutput {
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

data class ParsedAgentOutput(
    val intent: String,
    val title: String,
    val summary: String,
    val tags: List<String>,
)
