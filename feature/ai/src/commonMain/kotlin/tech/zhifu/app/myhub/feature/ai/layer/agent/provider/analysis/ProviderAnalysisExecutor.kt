package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis

import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router.ProviderRouteDecision

interface ProviderAnalysisExecutor {
    suspend fun analyze(
        route: ProviderRouteDecision,
        request: ProviderAnalysisRequest
    ): ProviderAnalysisResult

}
