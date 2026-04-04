package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router.impl

import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router.ProviderRouteDecision
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router.ProviderRouter
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode

class DisabledProviderRouter(
    override val mode: ProviderMode = ProviderMode.DISABLED
) : ProviderRouter {

    override suspend fun resolveRoute(): ProviderRouteDecision =
        ProviderRouteDecision(
            mode = ProviderMode.DISABLED,
            available = false,
            reason = "AI_UNAVAILABLE:disabled",
        )
}
