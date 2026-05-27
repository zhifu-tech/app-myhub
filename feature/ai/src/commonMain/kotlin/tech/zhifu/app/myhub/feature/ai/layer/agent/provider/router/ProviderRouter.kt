package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router

import tech.zhifu.app.myhub.ui.state.ai.ProviderMode

interface ProviderRouter {
    val mode: ProviderMode
    suspend fun resolveRoute(): ProviderRouteDecision
}
