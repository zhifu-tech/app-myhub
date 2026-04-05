package tech.zhifu.app.myhub.feature.ai.layer.agent

import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft

interface CaptureAgent {
    fun analyzeToDraft(
        input: String
    ): AgentDraftSuggestion
}

data class AgentDraftSuggestion(
    val intent: String,
    val draft: CaptureDraft,
)

