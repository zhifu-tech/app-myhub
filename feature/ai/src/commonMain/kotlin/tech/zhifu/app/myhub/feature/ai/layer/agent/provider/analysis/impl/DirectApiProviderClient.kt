package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.impl

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisClient
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisRequest
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisResult
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderErrorCategory
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig

class DirectApiProviderClient(
    private val httpClient: HttpClient,
) : ProviderAnalysisClient {
    override suspend fun analyze(
        request: ProviderAnalysisRequest,
        config: ProviderRoutingConfig,
        onReasoning: (suspend (String) -> Unit)?,
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

        val payload = withTimeoutOrNull(
            timeMillis = config.timeoutMs
        ) {
            httpClient
                .preparePost(
                    urlString = "$endpoint/v1/chat/completions"
                ) {
                    contentType(type = ContentType.Application.Json)
                    header(
                        key = HttpHeaders.Authorization,
                        value = "Bearer ${config.directApiKey}"
                    )
                    setBody(
                        buildJsonObject {
                            put(
                                key = "model",
                                element = JsonPrimitive(value = config.directModel)
                            )
                            put(
                                key = "temperature",
                                element = JsonPrimitive(value = 0.2)
                            )
                            put(
                                key = "stream",
                                element = JsonPrimitive(true)
                            )
                            put(
                                key = "messages",
                                element = buildJsonArray {
                                    buildJsonObject {
                                        put(
                                            key = "role",
                                            element = JsonPrimitive("system")
                                        )
                                        put(
                                            key = "content",
                                            element = JsonPrimitive(
                                                value = "你是 AI 捕获助手。仅返回 JSON，不要 markdown 代码块。必须包含 intent,title,summary,tags 四个字段；tags 为字符串数组。若信息不足也要给可用草稿，intent 默认 create_card。"
                                            )
                                        )
                                    }.also { add(it) }

                                    buildJsonObject {
                                        put(
                                            key = "role",
                                            element = JsonPrimitive("user")
                                        )
                                        put(
                                            key = "content",
                                            element = JsonPrimitive(request.input.text)
                                        )
                                    }.also { add(it) }
                                }
                            )
                        }
                    )
                }
                .execute { response ->
                    if (!response.status.isSuccess()) {
                        return@execute StreamResponsePayload(
                            responseText = "__HTTP_ERROR__:${response.status.value}",
                            reasoning = null,
                        )
                    }

                    val reasoningBuilder = StringBuilder()
                    val contentBuilder = StringBuilder()
                    val rawBuilder = StringBuilder()
                    val channel = response.bodyAsChannel()
                    while (!channel.isClosedForRead) {
                        val line = channel.readUTF8Line() ?: break
                        if (line.isBlank() || !line.startsWith("data:")) continue
                        val data = line.removePrefix("data:").trim()
                        if (data == "[DONE]") break

                        rawBuilder.append(data).append('\n')
                        val delta = parseStreamChunk(jsonText = data) ?: continue
                        delta.reasoning?.let {
                            reasoningBuilder.append(it)
                            onReasoning?.invoke(reasoningBuilder.toString())
                        }
                        delta.content?.let {
                            contentBuilder.append(it)
                        }
                    }

                    val responseText = contentBuilder.toString()
                    StreamResponsePayload(
                        responseText = responseText.ifBlank {
                            rawBuilder.toString().trim()
                        },
                        reasoning = reasoningBuilder.toString().ifBlank { null },
                    )
                }
        } ?: return ProviderAnalysisResult.Failed(
            reason = "direct_api_timeout",
            category = ProviderErrorCategory.TIMEOUT,
        )

        logger.debug { "DirectApiProviderClient responseText: ${payload.responseText}" }
        if (payload.responseText.startsWith("__HTTP_ERROR__")) {
            return ProviderAnalysisResult.Failed(
                reason = "direct_api_http_error",
                category = ProviderErrorCategory.HTTP,
            )
        }

        val parsed = parseProviderOutput(jsonText = payload.responseText)
            ?: return ProviderAnalysisResult.Failed(
                reason = "direct_api_invalid_choice_content",
                category = ProviderErrorCategory.PARSE,
            )
        return ProviderAnalysisResult.Success(
            output = parsed,
            rawResponseJson = payload.responseText,
            reasoning = payload.reasoning?.trim()?.ifBlank { null },
        )
    }
}

private data class StreamResponsePayload(
    val responseText: String,
    val reasoning: String?,
)

private data class StreamChunkDelta(
    val content: String?,
    val reasoning: String?,
)

private fun parseStreamChunk(jsonText: String): StreamChunkDelta? {
    val root = runCatching {
        Json.parseToJsonElement(string = jsonText).jsonObject
    }.getOrNull() ?: return null
    val choices = root["choices"] as? JsonArray ?: return null
    val first = choices.firstOrNull()?.jsonObject ?: return null
    val delta = first["delta"]?.jsonObject ?: return null
    return StreamChunkDelta(
        content = delta.string("content"),
        reasoning = delta.string("reasoning")
            ?: delta.string("reasoning_content"),
    )
}

private fun JsonObject.string(
    key: String
): String? {
    return (this[key] as? JsonPrimitive)
        ?.contentOrNull
        ?.takeIf { it.isNotEmpty() }
}

private val logger = logger("DirectApiProviderClient")
