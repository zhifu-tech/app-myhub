package tech.zhifu.app.myhub.feature.ai.layer.agent.impl

import tech.zhifu.app.myhub.datastore.model.util.generateUUId
import tech.zhifu.app.myhub.feature.ai.layer.agent.AgentDraftSuggestion
import tech.zhifu.app.myhub.feature.ai.layer.agent.CaptureAgent
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft

class DefaultCaptureAgent(
    private val promptAssembler: PromptAssembler,
    private val toolPlanner: ToolPlanner,
    private val responseParser: ResponseParser,
    private val outputGuard: OutputGuard,
) : CaptureAgent {

    override fun analyzeToDraft(
        input: String
    ): AgentDraftSuggestion {
        val prompt = promptAssembler.assemble(input)
        val plan = toolPlanner.plan(prompt)
        val parsed = responseParser.parse(input = input, plan = plan)
        val safe = outputGuard.ensureValid(parsed, input)
        return AgentDraftSuggestion(
            intent = safe.intent,
            draft = CaptureDraft(
                id = "draft_${generateUUId()}",
                title = safe.title,
                summary = safe.summary,
                tags = safe.tags,
                sourceText = input,
                mediaAssets = emptyList(),
            ),
        )
    }
}
