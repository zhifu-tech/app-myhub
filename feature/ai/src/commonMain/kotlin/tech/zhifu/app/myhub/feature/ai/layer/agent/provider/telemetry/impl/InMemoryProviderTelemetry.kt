package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.telemetry.impl

import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderErrorCategory
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.telemetry.ProviderTelemetry
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.telemetry.ProviderTelemetrySnapshot
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode
import kotlin.time.Clock

class InMemoryProviderTelemetry : ProviderTelemetry {
    private var successCount: Long = 0
    private var failureCount: Long = 0
    private var totalLatencyMs: Long = 0
    private val errors: MutableMap<ProviderErrorCategory, Long> = mutableMapOf()
    private var updatedAtMs: Long = 0

    override fun recordSuccess(
        mode: ProviderMode,
        latencyMs: Long
    ) {
        successCount += 1
        totalLatencyMs += latencyMs.coerceAtLeast(0)
        updatedAtMs = Clock.System.now().toEpochMilliseconds()
    }

    override fun recordFailure(
        mode: ProviderMode,
        latencyMs: Long,
        category: ProviderErrorCategory
    ) {
        failureCount += 1
        totalLatencyMs += latencyMs.coerceAtLeast(0)
        errors[category] = (errors[category] ?: 0) + 1
        updatedAtMs = Clock.System.now().toEpochMilliseconds()
    }

    override fun snapshot(): ProviderTelemetrySnapshot {
        val total = successCount + failureCount
        return ProviderTelemetrySnapshot(
            totalRequests = total,
            successCount = successCount,
            failureCount = failureCount,
            avgLatencyMs = if (total == 0L) 0L else totalLatencyMs / total,
            errorBreakdown = errors.toMap().mapKeys { it.key.name.lowercase() },
            updatedAtMs = updatedAtMs,
        )
    }
}
