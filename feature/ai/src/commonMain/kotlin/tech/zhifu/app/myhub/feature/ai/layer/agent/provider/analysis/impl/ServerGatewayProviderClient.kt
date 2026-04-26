package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.impl

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.withTimeoutOrNull
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisClient
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisError
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisRequest
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisResult
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig

class ServerGatewayProviderClient(
    private val httpClient: HttpClient,
) : ProviderAnalysisClient {
    override suspend fun analyze(
        request: ProviderAnalysisRequest,
        config: ProviderRoutingConfig,
        onReasoning: suspend (String) -> Unit,
    ): ProviderAnalysisResult {
        // fixme：需要同步 direct 的操作，我们先开发 direct，最后调试Api的
        val url = "${ApiConfig.BASE_URL}/api/capture/analysis"
        val responseText =
            withTimeoutOrNull(config.timeoutMs) {
                val response = httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
                if (!response.status.isSuccess()) {
                    return@withTimeoutOrNull "__HTTP_ERROR__:${response.status.value}"
                }
                response.bodyAsText()
            } ?: return ProviderAnalysisResult.Failed(
                reason = "server_gateway_timeout",
                category = ProviderAnalysisError.TIMEOUT,
            )
        if (responseText.startsWith("__HTTP_ERROR__")) {
            return ProviderAnalysisResult.Failed(
                reason = "server_gateway_http_error",
                category = ProviderAnalysisError.HTTP,
            )
        }

        val parsed = parseProviderOutput(
            responseText,
            reasoning = ""
        )
            ?: return ProviderAnalysisResult.Failed(
                reason = "server_gateway_invalid_response",
                category = ProviderAnalysisError.PARSE,
            )
        return ProviderAnalysisResult.Success(
            data = parsed,
        )
    }
}
