package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.config.impl

import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.config.MutableProviderConfigSource
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig

class InMemoryProviderConfigSource(
    private var config: ProviderRoutingConfig = ProviderRoutingConfig(),
) : MutableProviderConfigSource {

    override fun current(): ProviderRoutingConfig = config

    override suspend fun update(config: ProviderRoutingConfig) {
        this.config = config
    }
}
