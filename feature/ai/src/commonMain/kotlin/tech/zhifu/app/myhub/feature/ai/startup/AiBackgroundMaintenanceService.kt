package tech.zhifu.app.myhub.feature.ai.startup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.analytics.AnalyticsEvent
import tech.zhifu.app.myhub.analytics.AnalyticsService
import tech.zhifu.app.myhub.analytics.AnalyticsValue
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.telemetry.ProviderTelemetry
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaGarbageCollector
import tech.zhifu.app.myhub.feature.ai.layer.storage.media.MediaPostProcessExecutor

class AiBackgroundMaintenanceService(
    private val mediaPostProcessExecutor: MediaPostProcessExecutor,
    private val mediaGarbageCollector: MediaGarbageCollector,
    private val providerTelemetry: ProviderTelemetry,
    private val analyticsService: AnalyticsService,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    fun start(
        intervalMs: Long = 60_000L,
        mediaBatchSize: Int = 50,
        gcBatchSize: Int = 500,
    ) {
        if (job?.isActive == true) return
        job = scope.launch {
            while (isActive) {
                runCatching {
                    mediaPostProcessExecutor.processQueuedJobs(limit = mediaBatchSize)
                    mediaGarbageCollector.collect(limit = gcBatchSize)
                    reportTelemetry()
                }
                delay(intervalMs.coerceAtLeast(10_000L))
            }
        }
    }

    private fun reportTelemetry() {
        val snapshot = providerTelemetry.snapshot()
        analyticsService.logEvent(
            AnalyticsEvent(
                name = "ai_provider_metrics",
                parameters = buildMap {
                    put("total_requests", AnalyticsValue.Int(snapshot.totalRequests))
                    put("success_count", AnalyticsValue.Int(snapshot.successCount))
                    put("failure_count", AnalyticsValue.Int(snapshot.failureCount))
                    put("avg_latency_ms", AnalyticsValue.Int(snapshot.avgLatencyMs))
                    put("error_breakdown", AnalyticsValue.Str(snapshot.errorBreakdown.toString()))
                }
            )
        )
    }
}
