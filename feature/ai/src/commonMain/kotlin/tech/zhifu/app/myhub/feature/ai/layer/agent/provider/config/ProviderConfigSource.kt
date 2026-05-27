package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.config

import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig


interface ProviderConfigSource {
    fun current(): ProviderRoutingConfig
}

interface MutableProviderConfigSource : ProviderConfigSource {
    suspend fun update(config: ProviderRoutingConfig)
}
