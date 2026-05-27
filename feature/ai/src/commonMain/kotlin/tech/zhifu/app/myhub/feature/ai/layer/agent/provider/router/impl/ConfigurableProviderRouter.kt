package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router.impl

import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.config.ProviderConfigSource
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.health.ProviderHealth
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.health.ProviderHealthChecker
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router.ProviderRouteDecision
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router.ProviderRouter
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig
import kotlin.time.Clock

class ConfigurableProviderRouter(
    private val configSource: ProviderConfigSource,
    private val serverHealthChecker: ProviderHealthChecker,
    private val directHealthChecker: ProviderHealthChecker,
) : ProviderRouter {
    private val circuitStates = mapOf(
        ProviderMode.SERVER_GATEWAY to CircuitState(),
        ProviderMode.DIRECT_API to CircuitState(),
    )

    override val mode: ProviderMode
        get() = configSource.current().mode

    override suspend fun resolveRoute(): ProviderRouteDecision {
        val config = configSource.current()
        val candidates = fallbackChain(config.mode)
        for (candidate in candidates) {
            when (candidate) {
                ProviderMode.DISABLED -> continue
                ProviderMode.SERVER_GATEWAY -> {
                    if (isCircuitOpen(candidate)) continue
                    val health = checkWithRetry(serverHealthChecker, config)
                    if (health.healthy) {
                        onHealthSuccess(mode = candidate)
                        return ProviderRouteDecision(
                            mode = candidate,
                            available = true
                        )
                    } else {
                        onHealthFailure(mode = candidate, config = config)
                    }
                }

                ProviderMode.DIRECT_API -> {
                    if (isCircuitOpen(candidate)) continue
                    val health = checkWithRetry(directHealthChecker, config)
                    if (health.healthy) {
                        onHealthSuccess(candidate)
                        return ProviderRouteDecision(mode = candidate, available = true)
                    } else {
                        onHealthFailure(candidate, config)
                    }
                }
            }
        }
        return ProviderRouteDecision(
            mode = ProviderMode.DISABLED,
            available = false,
            reason = "AI_UNAVAILABLE:all_provider_unhealthy_or_disabled",
        )
    }

    private suspend fun checkWithRetry(
        checker: ProviderHealthChecker,
        config: ProviderRoutingConfig,
    ): ProviderHealth {
        val attempts = (config.maxRetries + 1).coerceAtLeast(1)
        repeat(attempts - 1) {
            val health = checker.check(config)
            if (health.healthy) return health
        }
        return checker.check(config)
    }

    private fun isCircuitOpen(mode: ProviderMode): Boolean {
        val state = circuitStates[mode] ?: return false
        val now = Clock.System.now().toEpochMilliseconds()
        return state.openUntilMs > now
    }

    private fun onHealthSuccess(mode: ProviderMode) {
        val state = circuitStates[mode] ?: return
        state.failureCount = 0
        state.openUntilMs = 0L
    }

    private fun onHealthFailure(mode: ProviderMode, config: ProviderRoutingConfig) {
        val state = circuitStates[mode] ?: return
        state.failureCount += 1
        if (state.failureCount >= config.healthFailThreshold.coerceAtLeast(1)) {
            state.openUntilMs = Clock.System.now().toEpochMilliseconds() + config.circuitOpenMs.coerceAtLeast(1_000L)
            state.failureCount = 0
        }
    }

    private fun fallbackChain(mode: ProviderMode): List<ProviderMode> {
        return when (mode) {
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

            ProviderMode.DISABLED -> listOf(ProviderMode.DISABLED)
        }
    }
}

private data class CircuitState(
    var failureCount: Int = 0,
    var openUntilMs: Long = 0L,
)
