package tech.zhifu.app.myhub.feature.ai.layer.agent.impl

class PromptAssembler {
    fun assemble(
        input: String
    ): String = "task=capture_analysis;input=$input"
}
