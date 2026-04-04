package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router

import tech.zhifu.app.myhub.ui.state.ai.ProviderMode

data class ProviderRouteDecision(
    val mode: ProviderMode,
    val available: Boolean,
    val reason: String? = null,
)
