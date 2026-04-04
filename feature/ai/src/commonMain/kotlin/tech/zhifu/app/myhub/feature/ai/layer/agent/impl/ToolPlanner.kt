package tech.zhifu.app.myhub.feature.ai.layer.agent.impl

class ToolPlanner {
    fun plan(
        prompt: String
    ): String =
        if (prompt.contains("capture_analysis")) {
            "create_draft"
        } else {
            "manual"
        }
}
