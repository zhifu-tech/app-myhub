package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.health.impl

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.coroutines.withTimeoutOrNull
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.health.ProviderHealth
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.health.ProviderHealthChecker
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig

class ServerGatewayHealthChecker : ProviderHealthChecker {
    override suspend fun check(
        config: ProviderRoutingConfig
    ): ProviderHealth {
        return ProviderHealth(
            healthy = true
        )
    }
}

class RealServerGatewayHealthChecker(
    private val httpClient: HttpClient,
) : ProviderHealthChecker {
    override suspend fun check(config: ProviderRoutingConfig): ProviderHealth {
        if (config.timeoutMs <= 0 || config.maxRetries < 0) {
            return ProviderHealth(
                healthy = false,
                reason = "server_gateway_invalid_timeout_or_retry"
            )
        }
        val healthy = withTimeoutOrNull(config.timeoutMs) {
            runCatching {
                httpClient.get("${ApiConfig.BASE_URL}/api/ai/providers/health").status.isSuccess()
            }.getOrDefault(false)
        } ?: false
        return if (healthy) {
            ProviderHealth(
                healthy = true
            )
        } else {
            ProviderHealth(
                healthy = false,
                reason = "server_gateway_healthcheck_failed"
            )
        }
    }
}
