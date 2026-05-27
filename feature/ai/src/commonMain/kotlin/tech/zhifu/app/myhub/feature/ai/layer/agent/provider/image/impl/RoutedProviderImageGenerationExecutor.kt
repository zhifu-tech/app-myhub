package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.impl

import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.config.ProviderConfigSource
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationExecutor
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationProgress
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationRequest
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationResult
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode

class RoutedProviderImageGenerationExecutor(
    private val configSource: ProviderConfigSource,
    private val serverGatewayClient: ServerGatewayImageGenerationClient,
    private val directApiClient: DirectApiImageGenerationClient,
) : ProviderImageGenerationExecutor {

    override suspend fun generate(
        request: ProviderImageGenerationRequest,
        onProgress: suspend (ProviderImageGenerationProgress) -> Unit,
    ): ProviderImageGenerationResult {
        val config = configSource.current()
        val modes = when (config.mode) {
            ProviderMode.SERVER_GATEWAY -> listOf(
                ProviderMode.SERVER_GATEWAY,
                ProviderMode.DIRECT_API,
                ProviderMode.DISABLED,
            )

            ProviderMode.DIRECT_API -> listOf(
                ProviderMode.DIRECT_API,
                ProviderMode.SERVER_GATEWAY,
                ProviderMode.DISABLED,
            )

            ProviderMode.DISABLED -> listOf(
                ProviderMode.DIRECT_API,
                ProviderMode.SERVER_GATEWAY,
                ProviderMode.DISABLED,
            )
        }

        var lastFailure: ProviderImageGenerationResult.Failed? = null
        modes.forEach { mode ->
            when (mode) {
                ProviderMode.SERVER_GATEWAY -> {
                    when (val result = serverGatewayClient.generate(request, config, onProgress)) {
                        is ProviderImageGenerationResult.Success -> return result
                        is ProviderImageGenerationResult.Failed -> lastFailure = result
                    }
                }

                ProviderMode.DIRECT_API -> {
                    when (val result = directApiClient.generate(request, config, onProgress)) {

                        is ProviderImageGenerationResult.Success -> return result
                        is ProviderImageGenerationResult.Failed -> lastFailure = result
                    }.also {
                        logger.info { "directApiClient.generate result: ${it}" }
                    }
                }

                ProviderMode.DISABLED -> {
                    lastFailure = ProviderImageGenerationResult.Failed(
                        reason = "AI_UNAVAILABLE:disabled",
                        category = tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationError.UNAVAILABLE,
                    )
                }
            }
        }
        return lastFailure ?: ProviderImageGenerationResult.Failed(
            reason = "AI_UNAVAILABLE:no_provider",
            category = tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationError.UNAVAILABLE,
        )
    }
}
