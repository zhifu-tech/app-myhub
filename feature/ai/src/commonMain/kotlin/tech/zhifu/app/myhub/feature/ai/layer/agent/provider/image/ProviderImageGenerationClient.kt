package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image

import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig

interface ProviderImageGenerationClient {
    suspend fun generate(
        request: ProviderImageGenerationRequest,
        config: ProviderRoutingConfig,
        onProgress: suspend (ProviderImageGenerationProgress) -> Unit = {},
    ): ProviderImageGenerationResult
}
