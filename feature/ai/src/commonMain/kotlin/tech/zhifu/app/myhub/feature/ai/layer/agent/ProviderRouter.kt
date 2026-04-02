package tech.zhifu.app.myhub.feature.ai.layer.agent

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.withTimeoutOrNull
import tech.zhifu.app.myhub.network.ApiConfig
import kotlin.time.Clock

interface ProviderRouter {
    val mode: ProviderMode
    suspend fun resolveRoute(): ProviderRouteDecision
}

enum class ProviderMode {
    DISABLED,
    SERVER_GATEWAY,
    DIRECT_API,
}

class DisabledProviderRouter : ProviderRouter {
    override val mode: ProviderMode = ProviderMode.DISABLED

    override suspend fun resolveRoute(): ProviderRouteDecision {
        return ProviderRouteDecision(
            mode = ProviderMode.DISABLED,
            available = false,
            reason = "AI_UNAVAILABLE:disabled",
        )
    }
}

data class ProviderRouteDecision(
    val mode: ProviderMode,
    val available: Boolean,
    val reason: String? = null,
)

data class ProviderRoutingConfig(
    val mode: ProviderMode = ProviderMode.DISABLED,
    val directEndpoint: String = "",
    val directModel: String = "",
    val directApiKey: String = "",
    val timeoutMs: Long = 15_000,
    val maxRetries: Int = 1,
    val healthFailThreshold: Int = 3,
    val circuitOpenMs: Long = 60_000,
)

class InMemoryProviderConfigSource(
    private var config: ProviderRoutingConfig = ProviderRoutingConfig(),
) : MutableProviderConfigSource {
    override fun current(): ProviderRoutingConfig = config

    override suspend fun update(config: ProviderRoutingConfig) {
        this.config = config
    }
}

interface ProviderHealthChecker {
    suspend fun check(config: ProviderRoutingConfig): ProviderHealth
}

data class ProviderHealth(
    val healthy: Boolean,
    val reason: String? = null,
)

class ServerGatewayHealthChecker : ProviderHealthChecker {
    override suspend fun check(config: ProviderRoutingConfig): ProviderHealth {
        return ProviderHealth(healthy = true)
    }
}

class RealServerGatewayHealthChecker(
    private val httpClient: HttpClient,
) : ProviderHealthChecker {
    override suspend fun check(config: ProviderRoutingConfig): ProviderHealth {
        if (config.timeoutMs <= 0 || config.maxRetries < 0) {
            return ProviderHealth(healthy = false, reason = "server_gateway_invalid_timeout_or_retry")
        }
        val healthy = withTimeoutOrNull(config.timeoutMs) {
            runCatching {
                httpClient.get("${ApiConfig.BASE_URL}/api/ai/providers/health").status.isSuccess()
            }.getOrDefault(false)
        } ?: false
        return if (healthy) {
            ProviderHealth(healthy = true)
        } else {
            ProviderHealth(healthy = false, reason = "server_gateway_healthcheck_failed")
        }
    }
}

class DirectApiHealthChecker : ProviderHealthChecker {
    override suspend fun check(config: ProviderRoutingConfig): ProviderHealth {
        return ProviderHealth(healthy = true)
    }
}

class RealDirectApiHealthChecker(
    private val httpClient: HttpClient,
) : ProviderHealthChecker {
    override suspend fun check(config: ProviderRoutingConfig): ProviderHealth {
        if (config.directEndpoint.isBlank()) {
            return ProviderHealth(healthy = false, reason = "direct_api_endpoint_missing")
        }
        if (config.directModel.isBlank()) {
            return ProviderHealth(healthy = false, reason = "direct_api_model_missing")
        }
        if (config.directApiKey.isBlank()) {
            return ProviderHealth(healthy = false, reason = "direct_api_api_key_missing")
        }
        if (config.timeoutMs <= 0 || config.maxRetries < 0) {
            return ProviderHealth(healthy = false, reason = "direct_api_invalid_timeout_or_retry")
        }
        val endpoint = config.directEndpoint.trimEnd('/')
        val healthy = withTimeoutOrNull(config.timeoutMs) {
            runCatching {
                httpClient.get("$endpoint/v1/models") {
                    header(HttpHeaders.Authorization, "Bearer ${config.directApiKey}")
                }.status.isSuccess()
            }.getOrDefault(false)
        } ?: false
        return if (healthy) {
            ProviderHealth(healthy = true)
        } else {
            ProviderHealth(healthy = false, reason = "direct_api_healthcheck_failed")
        }
    }
}

class ConfigurableProviderRouter(
    private val configSource: ProviderConfigSource,
    private val serverHealthChecker: ProviderHealthChecker,
    private val directHealthChecker: ProviderHealthChecker,
) : ProviderRouter {
    private val circuitStates: MutableMap<ProviderMode, CircuitState> = mutableMapOf(
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
                        onHealthSuccess(candidate)
                        return ProviderRouteDecision(mode = candidate, available = true)
                    } else {
                        onHealthFailure(candidate, config)
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
