package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.impl

import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisClient
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisError
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisMediaInput
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisRequest
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisResult
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig

class DirectApiProviderClient(
    val httpClient: HttpClient,
) : ProviderAnalysisClient {

    override suspend fun analyze(
        request: ProviderAnalysisRequest,
        config: ProviderRoutingConfig,
        onReasoning: suspend (String) -> Unit,
    ): ProviderAnalysisResult {
        val endpoint = config.directEndpoint.trimEnd('/')
        if (endpoint.isBlank()) {
            return ProviderAnalysisResult.Failed(
                reason = "direct_api_endpoint_missing",
                category = ProviderAnalysisError.CONFIG,
            )
        }
        if (config.directModel.isBlank()) {
            return ProviderAnalysisResult.Failed(
                reason = "direct_api_model_missing",
                category = ProviderAnalysisError.CONFIG,
            )
        }
        if (request.mediaInputs.isNotEmpty() && config.directVisionModel.isBlank()) {
            return ProviderAnalysisResult.Failed(
                reason = "direct_api_vision_model_missing",
                category = ProviderAnalysisError.CONFIG,
            )
        }
        if (requiresApiKey(endpoint = endpoint) && config.directApiKey.isBlank()) {
            return ProviderAnalysisResult.Failed(
                reason = "direct_api_api_key_missing",
                category = ProviderAnalysisError.AUTH,
            )
        }
        val model = if (request.mediaInputs.isNotEmpty()) {
            config.directVisionModel
        } else {
            config.directModel
        }

        val payload = executeChatCompletion(
            endpoint = endpoint,
            apiKey = config.directApiKey,
            model = model,
            timeoutMs = config.timeoutMs,
            systemPrompt = systemPrompt(),
            request = request,
            onReasoning = onReasoning,
        )
            ?: return ProviderAnalysisResult.Failed(
                reason = "direct_api_timeout",
                category = ProviderAnalysisError.TIMEOUT,
            )

        logger.debug { "DirectApiProviderClient payload: $payload" }
        if (payload.responseText.startsWith("__HTTP_ERROR__")) {
            return ProviderAnalysisResult.Failed(
                reason = "direct_api_http_error:${payload.httpStatus ?: "unknown"}",
                category = ProviderAnalysisError.HTTP,
            )
        }
        if (payload.responseText.isBlank()) {
            val reason = when {
                payload.sawDone && payload.finishReason != null -> "direct_api_stream_finished_without_content:${payload.finishReason}"
                payload.sawDone -> "direct_api_stream_done_without_content"
                else -> "direct_api_stream_interrupted_without_content"
            }
            logger.error {
                "DirectApiProviderClient stream ended without content: reason=$reason, " +
                    "chunks=${payload.chunkCount}, reasoningChunks=${payload.reasoningChunkCount}, " +
                    "contentChunks=${payload.contentChunkCount}, sawDone=${payload.sawDone}, " +
                    "finishReason=${payload.finishReason}, raw=${payload.rawResponseText.orEmpty()}"
            }
            return ProviderAnalysisResult.Failed(
                reason = reason,
                category = if (payload.sawDone) {
                    ProviderAnalysisError.PARSE
                } else {
                    ProviderAnalysisError.TIMEOUT
                },
            )
        }

        val parsed = parseProviderOutput(
            jsonText = payload.responseText,
            reasoning = payload.reasoning,
        )
            ?: return ProviderAnalysisResult.Failed(
                reason = "direct_api_invalid_choice_content",
                category = ProviderAnalysisError.PARSE,
            )
        return ProviderAnalysisResult.Success(
            data = parsed,
        )
    }

    private suspend fun executeChatCompletion(
        endpoint: String,
        apiKey: String,
        model: String,
        timeoutMs: Long,
        systemPrompt: String,
        request: ProviderAnalysisRequest,
        onReasoning: (suspend (String) -> Unit) = {},
    ): StreamResponsePayload? = withTimeoutOrNull(timeMillis = timeoutMs) {
        val userPrompt = userPrompt(request)
        val allowReasoningTrace = request.mediaInputs.isEmpty()
        logger.debug {
            "DirectApiProviderClient executeChatCompletion " +
                "endpoint=$endpoint model=$model " +
                "timeoutMs=$timeoutMs systemPromptLength=${systemPrompt.length} " +
                "userPromptLength=${userPrompt.length} mediaCount=${request.mediaInputs.size} " +
                "allowReasoningTrace=$allowReasoningTrace"
        }
        httpClient
            .preparePost(urlString = "$endpoint/v1/chat/completions") {
                contentType(type = ContentType.Application.Json)
                timeout {
                    requestTimeoutMillis = timeoutMs
                    connectTimeoutMillis = timeoutMs
                    socketTimeoutMillis = timeoutMs
                }
                if (shouldSendBearerHeader(endpoint = endpoint, apiKey = apiKey)) {
                    header(
                        key = HttpHeaders.Authorization,
                        value = "Bearer $apiKey"
                    )
                }
                setBody(
                    buildChatCompletionPayload(
                        model = model,
                        systemPrompt = systemPrompt,
                        userPrompt = userPrompt,
                        mediaInputs = request.mediaInputs,
                        disableReasoning = allowReasoningTrace.not(),
                    )
                )
            }
            .execute { response ->
                if (!response.status.isSuccess()) {
                    return@execute StreamResponsePayload(
                        responseText = "__HTTP_ERROR__:${response.status.value}",
                        reasoning = null,
                        httpStatus = response.status.value,
                        errorBody = response.bodyAsText(),
                    )
                }

                val reasoningBuilder = StringBuilder()
                val contentBuilder = StringBuilder()
                var chunkCount = 0
                var reasoningChunkCount = 0
                var contentChunkCount = 0
                var sawDone = false
                var finishReason: String? = null

                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    if (line.isBlank() || !line.startsWith("data:")) continue
                    val data = line.removePrefix("data:").trim()
                    if (data == "[DONE]") {
                        sawDone = true
                        break
                    }

                    chunkCount += 1
                    val delta = parseStreamChunk(jsonText = data) ?: continue
                    finishReason = delta.finishReason ?: finishReason
                    delta.reasoning?.let {
                        reasoningChunkCount += 1
                        reasoningBuilder.append(it)
                        onReasoning(reasoningBuilder.toString())
                    }
                    delta.content?.let {
                        contentChunkCount += 1
                        contentBuilder.append(it)
                    }
                }

                StreamResponsePayload(
                    responseText = contentBuilder.toString().trim(),
                    reasoning = reasoningBuilder.toString().ifBlank { null },
                    chunkCount = chunkCount,
                    reasoningChunkCount = reasoningChunkCount,
                    contentChunkCount = contentChunkCount,
                    sawDone = sawDone,
                    finishReason = finishReason,
                )
            }
    }
}

private fun requiresApiKey(
    endpoint: String,
): Boolean {
    val normalized = endpoint.lowercase()
    return !normalized.contains("localhost") &&
        !normalized.contains("127.0.0.1") &&
        !normalized.contains(":11434") &&
        !normalized.contains("ollama")
}

private fun shouldSendBearerHeader(
    endpoint: String,
    apiKey: String,
): Boolean = apiKey.isNotBlank() && requiresApiKey(endpoint)

private fun buildChatCompletionPayload(
    model: String,
    systemPrompt: String,
    userPrompt: String,
    mediaInputs: List<ProviderAnalysisMediaInput>,
    disableReasoning: Boolean,
): JsonObject = buildJsonObject {
    put(key = "model", element = JsonPrimitive(value = model))
    put(
        key = "temperature",
        element = JsonPrimitive(value = if (mediaInputs.isEmpty()) 0.2 else 0.1)
    )
    put(key = "max_tokens", element = JsonPrimitive(600))
    put(key = "stream", element = JsonPrimitive(true))
    if (disableReasoning) {
        put(key = "reasoning_effort", element = JsonPrimitive("none"))
        put(
            key = "reasoning",
            element = buildJsonObject {
                put("effort", JsonPrimitive("none"))
            }
        )
    }
    put(
        key = "messages",
        element = buildJsonArray {
            buildJsonObject {
                put(key = "role", element = JsonPrimitive("system"))
                put(key = "content", element = JsonPrimitive(value = systemPrompt))
            }.also { add(it) }

            buildJsonObject {
                put(key = "role", element = JsonPrimitive("user"))
                if (mediaInputs.isEmpty()) {
                    put(key = "content", element = JsonPrimitive(value = userPrompt))
                } else {
                    put(
                        key = "content",
                        element = buildJsonArray {
                            add(
                                buildJsonObject {
                                    put("type", JsonPrimitive("text"))
                                    put("text", JsonPrimitive(userPrompt))
                                }
                            )
                            mediaInputs.forEach { media ->
                                add(
                                    buildJsonObject {
                                        put("type", JsonPrimitive("image_url"))
                                        put(
                                            "image_url",
                                            buildJsonObject {
                                                put("url", JsonPrimitive(media.toDataUrl()))
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    )
                }
            }.also { add(it) }
        }
    )
    put(
        key = "response_format",
        element = buildJsonObject {
            put("type", JsonPrimitive("json_object"))
        }
    )
}

private fun parseStreamChunk(
    jsonText: String
): StreamChunkDelta? {
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
        finishReason = first.string("finish_reason"),
    )
}

private data class StreamResponsePayload(
    val responseText: String,
    val reasoning: String?,
    // 用于调试
    val rawResponseText: String? = null,
    val chunkCount: Int = 0,
    val reasoningChunkCount: Int = 0,
    val contentChunkCount: Int = 0,
    val sawDone: Boolean = false,
    val finishReason: String? = null,
    val httpStatus: Int? = null,
    val errorBody: String? = null,
)

private data class StreamChunkDelta(
    val content: String?,
    val reasoning: String?,
    val finishReason: String?,
)
