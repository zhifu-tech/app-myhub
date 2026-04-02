package tech.zhifu.app.myhub.feature.ai.layer.agent

import kotlin.time.Clock

interface ProviderTelemetry {
    fun recordSuccess(mode: ProviderMode, latencyMs: Long)
    fun recordFailure(mode: ProviderMode, latencyMs: Long, category: ProviderErrorCategory)
    fun snapshot(): ProviderTelemetrySnapshot
}

class InMemoryProviderTelemetry : ProviderTelemetry {
    private var successCount: Long = 0
    private var failureCount: Long = 0
    private var totalLatencyMs: Long = 0
    private val errors: MutableMap<ProviderErrorCategory, Long> = mutableMapOf()
    private var updatedAtMs: Long = 0

    override fun recordSuccess(mode: ProviderMode, latencyMs: Long) {
        successCount += 1
        totalLatencyMs += latencyMs.coerceAtLeast(0)
        updatedAtMs = Clock.System.now().toEpochMilliseconds()
    }

    override fun recordFailure(mode: ProviderMode, latencyMs: Long, category: ProviderErrorCategory) {
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

data class ProviderTelemetrySnapshot(
    val totalRequests: Long,
    val successCount: Long,
    val failureCount: Long,
    val avgLatencyMs: Long,
    val errorBreakdown: Map<String, Long>,
    val updatedAtMs: Long,
)
