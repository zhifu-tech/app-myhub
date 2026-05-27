package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image

interface ProviderImageGenerationExecutor {
    suspend fun generate(
        request: ProviderImageGenerationRequest,
        onProgress: suspend (ProviderImageGenerationProgress) -> Unit = {},
    ): ProviderImageGenerationResult
}
