package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.impl

import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisExecutor
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisRequest
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisResult
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderErrorCategory
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.config.ProviderConfigSource
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router.ProviderRouteDecision
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode

class RoutedProviderAnalysisExecutor(
    private val configSource: ProviderConfigSource,
    private val serverGatewayClient: ServerGatewayProviderClient,
    private val directApiClient: DirectApiProviderClient,
) : ProviderAnalysisExecutor {

    override suspend fun analyze(
        route: ProviderRouteDecision,
        request: ProviderAnalysisRequest
    ): ProviderAnalysisResult {
        if (!route.available) {
            return ProviderAnalysisResult.Failed(
                reason = route.reason ?: "AI_UNAVAILABLE",
                category = ProviderErrorCategory.UNAVAILABLE,
            )
        }
        val config = configSource.current()
        return when (route.mode) {
            ProviderMode.SERVER_GATEWAY -> {
                serverGatewayClient.analyze(request, config)
            }

            ProviderMode.DIRECT_API -> {
                directApiClient.analyze(request, config)
            }

            ProviderMode.DISABLED -> {
                ProviderAnalysisResult.Failed(
                    reason = "AI_UNAVAILABLE:disabled",
                    category = ProviderErrorCategory.UNAVAILABLE,
                )
            }
        }
    }
}
