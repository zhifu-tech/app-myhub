package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.impl

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.withTimeoutOrNull
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationClient
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationError
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationProgress
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationRequest
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationResult
import tech.zhifu.app.myhub.network.ApiConfig
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig

class ServerGatewayImageGenerationClient(
    private val httpClient: HttpClient,
) : ProviderImageGenerationClient {
    override suspend fun generate(
        request: ProviderImageGenerationRequest,
        config: ProviderRoutingConfig,
        onProgress: suspend (ProviderImageGenerationProgress) -> Unit,
    ): ProviderImageGenerationResult {
        onProgress(
            ProviderImageGenerationProgress(
                stage = ProviderImageGenerationProgress.Stage.PREPARING,
            )
        )
        val responseText = withTimeoutOrNull(config.timeoutMs) {
            val response = httpClient.post("${ApiConfig.BASE_URL}/api/capture/image-generation") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (!response.status.isSuccess()) {
                return@withTimeoutOrNull "__HTTP_ERROR__:${response.status.value}:${response.bodyAsText()}"
            }
            response.bodyAsText()
        } ?: return ProviderImageGenerationResult.Failed(
            reason = "server_gateway_timeout",
            category = ProviderImageGenerationError.TIMEOUT,
        )

        if (responseText.startsWith("__HTTP_ERROR__")) {
            return ProviderImageGenerationResult.Failed(
                reason = responseText,
                category = ProviderImageGenerationError.HTTP,
            )
        }

        return parseGenerationResponse(
            jsonText = responseText,
            downloadBytes = { null },
        ) ?: ProviderImageGenerationResult.Failed(
            reason = "server_gateway_invalid_image_response",
            category = ProviderImageGenerationError.PARSE,
        )
    }
}
