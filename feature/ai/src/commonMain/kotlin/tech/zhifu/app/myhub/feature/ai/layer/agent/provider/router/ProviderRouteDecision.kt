package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router

import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode

@Serializable
data class ProviderRouteDecision(
    val mode: ProviderMode,
    val available: Boolean,
    val reason: String? = null,
)
