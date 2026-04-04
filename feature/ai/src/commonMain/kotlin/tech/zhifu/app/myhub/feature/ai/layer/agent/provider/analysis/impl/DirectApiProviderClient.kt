package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.impl

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisClient
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisRequest
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisResult
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderErrorCategory
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig

class DirectApiProviderClient(
    private val httpClient: HttpClient,
) : ProviderAnalysisClient {
    override suspend fun analyze(
        request: ProviderAnalysisRequest,
        config: ProviderRoutingConfig,
    ): ProviderAnalysisResult {
        val endpoint = config.directEndpoint.trimEnd('/')
        if (endpoint.isBlank()) {
            return ProviderAnalysisResult.Failed(
                reason = "direct_api_endpoint_missing",
                category = ProviderErrorCategory.CONFIG,
            )
        }
        if (config.directModel.isBlank()) {
            return ProviderAnalysisResult.Failed(
                reason = "direct_api_model_missing",
                category = ProviderErrorCategory.CONFIG,
            )
        }
        if (config.directApiKey.isBlank()) {
            return ProviderAnalysisResult.Failed(
                reason = "direct_api_api_key_missing",
                category = ProviderErrorCategory.AUTH,
            )
        }

        val responseText = withTimeoutOrNull(config.timeoutMs) {
            val response = httpClient
                .post("$endpoint/v1/chat/completions") {
                    contentType(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer ${config.directApiKey}")
                    setBody(
                        buildJsonObject {
                            put("model", JsonPrimitive(config.directModel))
                            put("temperature", JsonPrimitive(0.2))
                            put(
                                "messages",
                                buildJsonArray {
                                    add(
                                        buildJsonObject {
                                            put("role", JsonPrimitive("system"))
                                            put(
                                                "content",
                                                JsonPrimitive(
                                                    "Return JSON with keys intent,title,summary,tags for capture analysis."
                                                )
                                            )
                                        }
                                    )
                                    add(
                                        buildJsonObject {
                                            put("role", JsonPrimitive("user"))
                                            put("content", JsonPrimitive(request.input.text))
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            if (!response.status.isSuccess()) {
                return@withTimeoutOrNull "__HTTP_ERROR__:${response.status.value}"
            }
            response.bodyAsText()
        }
            ?: return ProviderAnalysisResult.Failed(
                reason = "direct_api_timeout",
                category = ProviderErrorCategory.TIMEOUT,
            )
        if (responseText.startsWith("__HTTP_ERROR__")) {
            return ProviderAnalysisResult.Failed(
                reason = "direct_api_http_error",
                category = ProviderErrorCategory.HTTP,
            )
        }

        val choiceContent = extractDirectChoiceContent(responseText)
            ?: return ProviderAnalysisResult.Failed(
                reason = "direct_api_invalid_response",
                category = ProviderErrorCategory.PARSE,
            )
        val parsed = parseProviderOutput(choiceContent)
            ?: return ProviderAnalysisResult.Failed(
                reason = "direct_api_invalid_choice_content",
                category = ProviderErrorCategory.PARSE,
            )
        return ProviderAnalysisResult.Success(
            output = parsed,
            rawResponseJson = responseText
        )
    }
}

private fun extractDirectChoiceContent(jsonText: String): String? {
    val root = runCatching { Json.parseToJsonElement(jsonText).jsonObject }.getOrNull() ?: return null
    val choices = root["choices"] as? JsonArray ?: return null
    val first = choices.firstOrNull()?.jsonObject ?: return null
    val message = first["message"]?.jsonObject ?: return null
    return message["content"]?.jsonPrimitive?.contentOrNull
}
