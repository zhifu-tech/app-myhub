package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.health

import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig

interface ProviderHealthChecker {
    suspend fun check(config: ProviderRoutingConfig): ProviderHealth
}

