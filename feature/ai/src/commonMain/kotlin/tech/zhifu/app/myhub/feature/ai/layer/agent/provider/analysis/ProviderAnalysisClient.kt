package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis

import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig

interface ProviderAnalysisClient {
    suspend fun analyze(
        request: ProviderAnalysisRequest,
        config: ProviderRoutingConfig,
    ): ProviderAnalysisResult
}
