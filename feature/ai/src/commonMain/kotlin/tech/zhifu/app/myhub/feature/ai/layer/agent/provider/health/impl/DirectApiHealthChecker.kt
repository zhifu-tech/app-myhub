package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.health.impl

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.withTimeoutOrNull
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.health.ProviderHealth
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.health.ProviderHealthChecker
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig

class DirectApiHealthChecker : ProviderHealthChecker {
    override suspend fun check(
        config: ProviderRoutingConfig
    ): ProviderHealth {
        return ProviderHealth(healthy = true)
    }
}

class RealDirectApiHealthChecker(
    private val httpClient: HttpClient,
) : ProviderHealthChecker {
    override suspend fun check(
        config: ProviderRoutingConfig
    ): ProviderHealth {
        if (config.directEndpoint.isBlank()) {
            return ProviderHealth(
                healthy = false,
                reason = "direct_api_endpoint_missing"
            )
        }
        if (config.directModel.isBlank()) {
            return ProviderHealth(
                healthy = false,
                reason = "direct_api_model_missing"
            )
        }
        if (config.directApiKey.isBlank()) {
            return ProviderHealth(
                healthy = false,
                reason = "direct_api_api_key_missing"
            )
        }
        if (config.timeoutMs <= 0 || config.maxRetries < 0) {
            return ProviderHealth(
                healthy = false,
                reason = "direct_api_invalid_timeout_or_retry"
            )
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
            ProviderHealth(
                healthy = true
            )
        } else {
            ProviderHealth(
                healthy = false,
                reason = "direct_api_healthcheck_failed"
            )
        }
    }
}
