package tech.zhifu.app.myhub.feature.ai.layer.agent

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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import tech.zhifu.app.myhub.network.ApiConfig

interface ProviderAnalysisExecutor {
    suspend fun analyze(route: ProviderRouteDecision, request: ProviderAnalysisRequest): ProviderAnalysisResult
}

class RoutedProviderAnalysisExecutor(
    private val configSource: ProviderConfigSource,
    private val serverGatewayClient: ServerGatewayProviderClient,
    private val directApiClient: DirectApiProviderClient,
) : ProviderAnalysisExecutor {
    override suspend fun analyze(
        route: ProviderRouteDecision,
        request: ProviderAnalysisRequest
    ): ProviderAnalysisResult {
        if (!route.available) {
            return ProviderAnalysisResult.Failed(
                reason = route.reason ?: "AI_UNAVAILABLE",
                category = ProviderErrorCategory.UNAVAILABLE,
            )
        }
        val config = configSource.current()
        return when (route.mode) {
            ProviderMode.SERVER_GATEWAY -> serverGatewayClient.analyze(request, config)
            ProviderMode.DIRECT_API -> directApiClient.analyze(request, config)
            ProviderMode.DISABLED -> ProviderAnalysisResult.Failed(
                reason = "AI_UNAVAILABLE:disabled",
                category = ProviderErrorCategory.UNAVAILABLE,
            )
        }
    }
}

class ServerGatewayProviderClient(
    private val httpClient: HttpClient,
) {
    suspend fun analyze(
        request: ProviderAnalysisRequest,
        config: ProviderRoutingConfig,
    ): ProviderAnalysisResult {
        val url = "${ApiConfig.BASE_URL}/api/ai/capture-analysis"
        val responseText = withTimeoutOrNull(config.timeoutMs) {
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
            category = ProviderErrorCategory.TIMEOUT,
        )
        if (responseText.startsWith("__HTTP_ERROR__")) {
            return ProviderAnalysisResult.Failed(
                reason = "server_gateway_http_error",
                category = ProviderErrorCategory.HTTP,
            )
        }

        val parsed = parseProviderOutput(responseText)
            ?: return ProviderAnalysisResult.Failed(
                reason = "server_gateway_invalid_response",
                category = ProviderErrorCategory.PARSE,
            )
        return ProviderAnalysisResult.Success(parsed, rawResponseJson = responseText)
    }
}

class DirectApiProviderClient(
    private val httpClient: HttpClient,
) {
    suspend fun analyze(
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
            val response = httpClient.post("$endpoint/v1/chat/completions") {
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
        } ?: return ProviderAnalysisResult.Failed(
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
        return ProviderAnalysisResult.Success(parsed, rawResponseJson = responseText)
    }
}

@Serializable
data class ProviderAnalysisRequest(
    val task: String = "capture_analysis",
    val input: ProviderAnalysisInput,
    val context: ProviderAnalysisContext,
)

@Serializable
data class ProviderAnalysisInput(
    val text: String,
    val media: List<String> = emptyList(),
)

@Serializable
data class ProviderAnalysisContext(
    val state: String,
    val missing_fields: List<String> = emptyList(),
)

data class ProviderAnalysisOutput(
    val intent: String,
    val title: String,
    val summary: String,
    val tags: List<String>,
)

sealed interface ProviderAnalysisResult {
    data class Success(
        val output: ProviderAnalysisOutput,
        val rawResponseJson: String,
    ) : ProviderAnalysisResult

    data class Failed(
        val reason: String,
        val category: ProviderErrorCategory,
    ) : ProviderAnalysisResult
}

enum class ProviderErrorCategory {
    CONFIG,
    AUTH,
    TIMEOUT,
    HTTP,
    PARSE,
    UNAVAILABLE,
}

private fun extractDirectChoiceContent(jsonText: String): String? {
    val root = runCatching { Json.parseToJsonElement(jsonText).jsonObject }.getOrNull() ?: return null
    val choices = root["choices"] as? JsonArray ?: return null
    val first = choices.firstOrNull()?.jsonObject ?: return null
    val message = first["message"]?.jsonObject ?: return null
    return message["content"]?.jsonPrimitive?.contentOrNull
}

private fun parseProviderOutput(jsonText: String): ProviderAnalysisOutput? {
    val root = runCatching { Json.parseToJsonElement(jsonText).jsonObject }.getOrNull() ?: return null
    val data = (root["output"] as? JsonObject) ?: root
    val title = data.string("title").orEmpty().trim()
    val summary = data.string("summary").orEmpty().trim()
    val tags = data.stringList("tags")
    val intent = data.string("intent").orEmpty().ifBlank { "create_card" }
    if (title.isBlank() && summary.isBlank()) return null
    return ProviderAnalysisOutput(
        intent = intent,
        title = title,
        summary = summary,
        tags = tags,
    )
}

private fun JsonObject.string(key: String): String? {
    return (this[key] as? JsonPrimitive)?.contentOrNull
}

private fun JsonObject.stringList(key: String): List<String> {
    val arr = this[key] as? JsonArray ?: return emptyList()
    return arr.mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.trim() }.filter { it.isNotBlank() }
}
