package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.telemetry

import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisError
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode

interface ProviderTelemetry {
    fun recordSuccess(
        mode: ProviderMode,
        latencyMs: Long
    )

    fun recordFailure(
        mode: ProviderMode,
        latencyMs: Long,
        category: ProviderAnalysisError
    )

    fun snapshot(): ProviderTelemetrySnapshot
}

data class ProviderTelemetrySnapshot(
    val totalRequests: Long,
    val successCount: Long,
    val failureCount: Long,
    val avgLatencyMs: Long,
    val errorBreakdown: Map<String, Long>,
    val updatedAtMs: Long,
)
