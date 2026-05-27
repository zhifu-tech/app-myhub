package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.impl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderGeneratedImage
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationClient
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationError
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationProgress
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationRequest
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image.ProviderImageGenerationResult
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.max

class DirectApiImageGenerationClient(
    private val httpClient: HttpClient,
) : ProviderImageGenerationClient {

    override suspend fun generate(
        request: ProviderImageGenerationRequest,
        config: ProviderRoutingConfig,
        onProgress: suspend (ProviderImageGenerationProgress) -> Unit,
    ): ProviderImageGenerationResult {
        val endpoint = config.directEndpoint.trimEnd('/')
        val imageModel = config.directImageModel.trim()
        val useOllamaApi = shouldUseOllamaApi(
            endpoint = endpoint,
            model = imageModel,
        )
        val timeoutMs = effectiveTimeoutMs(
            config = config,
            useOllamaApi = useOllamaApi,
        )
        if (endpoint.isBlank()) {
            return ProviderImageGenerationResult.Failed(
                reason = "direct_api_endpoint_missing",
                category = ProviderImageGenerationError.CONFIG,
            )
        }
        if (imageModel.isBlank()) {
            return ProviderImageGenerationResult.Failed(
                reason = "direct_api_image_model_missing",
                category = ProviderImageGenerationError.CONFIG,
            )
        }
        if (requiresApiKey(endpoint = endpoint, model = imageModel) && config.directApiKey.isBlank()) {
            return ProviderImageGenerationResult.Failed(
                reason = "direct_api_api_key_missing",
                category = ProviderImageGenerationError.AUTH,
            )
        }

        logger.debug {
            "DirectApiImageGenerationClient.generate start " +
                "endpoint=$endpoint model=$imageModel timeoutMs=$timeoutMs " +
                "useOllamaApi=$useOllamaApi promptLength=${request.prompt.length} size=${request.size}"
        }

        return runCatching {
            if (useOllamaApi) {
                generateWithOllama(
                    endpoint = endpoint,
                    apiKey = config.directApiKey,
                    model = imageModel,
                    request = request,
                    timeoutMs = timeoutMs,
                    onProgress = onProgress,
                )
            } else {
                generateWithOpenAiCompatible(
                    endpoint = endpoint,
                    apiKey = config.directApiKey,
                    model = imageModel,
                    request = request,
                    timeoutMs = timeoutMs,
                    onProgress = onProgress,
                )
            }
        }.getOrElse { throwable ->
            logger.error(throwable) {
                "DirectApiImageGenerationClient.generate failed " +
                    "endpoint=$endpoint model=$imageModel useOllamaApi=$useOllamaApi"
            }
            return ProviderImageGenerationResult.Failed(
                reason = "direct_api_exception:${throwable.message.orEmpty()}",
                category = ProviderImageGenerationError.HTTP,
            )
        }
    }

    private suspend fun downloadBytes(
        url: String,
    ): ByteArray? {
        val response = httpClient.get(url)
        if (!response.status.isSuccess()) return null
        return response.body()
    }

    private suspend fun generateWithOpenAiCompatible(
        endpoint: String,
        apiKey: String,
        model: String,
        request: ProviderImageGenerationRequest,
        timeoutMs: Long,
        onProgress: suspend (ProviderImageGenerationProgress) -> Unit,
    ): ProviderImageGenerationResult {
        onProgress(
            ProviderImageGenerationProgress(
                stage = ProviderImageGenerationProgress.Stage.PREPARING,
            )
        )
        val responseText = withTimeoutOrNull(timeMillis = timeoutMs) {
            val response = httpClient.post("$endpoint/v1/images/generations") {
                contentType(ContentType.Application.Json)
                timeout {
                    requestTimeoutMillis = timeoutMs
                    connectTimeoutMillis = timeoutMs
                    socketTimeoutMillis = timeoutMs
                }
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                setBody(
                    buildOpenAiImageGenerationPayload(
                        model = model,
                        request = request,
                    )
                )
            }
            logger.debug {
                "DirectApiImageGenerationClient.openai response " +
                    "status=${response.status.value} endpoint=$endpoint model=$model"
            }
            if (!response.status.isSuccess()) {
                return@withTimeoutOrNull "__HTTP_ERROR__:${response.status.value}:${response.bodyAsText()}"
            }
            response.bodyAsText()
        } ?: return ProviderImageGenerationResult.Failed(
            reason = "direct_api_timeout",
            category = ProviderImageGenerationError.TIMEOUT,
        )

        if (responseText.startsWith("__HTTP_ERROR__")) {
            return ProviderImageGenerationResult.Failed(
                reason = responseText,
                category = ProviderImageGenerationError.HTTP,
            )
        }

        onProgress(
            ProviderImageGenerationProgress(
                stage = ProviderImageGenerationProgress.Stage.FINALIZING,
            )
        )
        return parseGenerationResponse(
            jsonText = responseText,
            downloadBytes = ::downloadBytes,
        ) ?: ProviderImageGenerationResult.Failed(
            reason = "direct_api_invalid_image_response",
            category = ProviderImageGenerationError.PARSE,
        )
    }

    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun generateWithOllama(
        endpoint: String,
        apiKey: String,
        model: String,
        request: ProviderImageGenerationRequest,
        timeoutMs: Long,
        onProgress: suspend (ProviderImageGenerationProgress) -> Unit,
    ): ProviderImageGenerationResult {
        onProgress(
            ProviderImageGenerationProgress(
                stage = ProviderImageGenerationProgress.Stage.PREPARING,
            )
        )
        val streamResult = withTimeoutOrNull(timeMillis = timeoutMs) {
            httpClient.preparePost(urlString = "$endpoint/api/generate") {
                contentType(type = ContentType.Application.Json)
                timeout {
                    requestTimeoutMillis = timeoutMs
                    connectTimeoutMillis = timeoutMs
                    socketTimeoutMillis = timeoutMs
                }
                if (shouldSendBearerHeader(endpoint = endpoint, apiKey = apiKey)) {
                    header(HttpHeaders.Authorization, "Bearer $apiKey")
                }
                setBody(
                    buildOllamaImageGenerationPayload(
                        model = model,
                        request = request,
                    )
                )
            }.execute { response ->
                logger.debug {
                    "DirectApiImageGenerationClient.ollama response " +
                        "status=${response.status.value} endpoint=$endpoint model=$model"
                }
                if (!response.status.isSuccess()) {
                    return@execute OllamaStreamResult.HttpError(
                        reason = "__HTTP_ERROR__:${response.status.value}:${response.bodyAsText()}"
                    )
                }

                var finalChunk: OllamaImageStreamChunk? = null
                var chunkCount = 0
                var lastProgressKey: String? = null

                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    if (line.isBlank()) continue

                    chunkCount += 1
                    val chunk = parseOllamaImageStreamChunk(line)
                    if (chunk == null) {
                        logger.warn {
                            "DirectApiImageGenerationClient.ollama parse skipped " +
                                "chunk=$chunkCount line=${line.take(160)}"
                        }
                        continue
                    }

                    if (chunk.done) {
                        finalChunk = chunk
                        logger.debug {
                            "DirectApiImageGenerationClient.ollama done " +
                                "chunk=$chunkCount completed=${chunk.completed} total=${chunk.total} " +
                                "hasImage=${chunk.imageBase64.isNullOrBlank().not()} reason=${chunk.doneReason}"
                        }
                        onProgress(
                            ProviderImageGenerationProgress(
                                stage = ProviderImageGenerationProgress.Stage.FINALIZING,
                                completed = chunk.completed,
                                total = chunk.total,
                                status = chunk.doneReason ?: chunk.status,
                            )
                        )
                        continue
                    }

                    val progress = ProviderImageGenerationProgress(
                        stage = ProviderImageGenerationProgress.Stage.GENERATING,
                        completed = chunk.completed,
                        total = chunk.total,
                        status = chunk.status,
                    )
                    val progressKey = "${progress.stage}:${progress.completed}:${progress.total}:${progress.status}"
                    if (progressKey != lastProgressKey) {
                        lastProgressKey = progressKey
                        onProgress(progress)
                    }
                    logger.debug {
                        "DirectApiImageGenerationClient.ollama progress " +
                            "chunk=$chunkCount completed=${chunk.completed} total=${chunk.total} " +
                            "status=${chunk.status.orEmpty()}"
                    }
                }

                if (finalChunk == null) {
                    OllamaStreamResult.ParseError(reason = "ollama_image_stream_missing_final_chunk")
                } else {
                    OllamaStreamResult.Success(finalChunk)
                }
            }
        } ?: return ProviderImageGenerationResult.Failed(
            reason = "direct_api_timeout",
            category = ProviderImageGenerationError.TIMEOUT,
        )

        return when (streamResult) {
            is OllamaStreamResult.HttpError -> ProviderImageGenerationResult.Failed(
                reason = streamResult.reason,
                category = ProviderImageGenerationError.HTTP,
            )

            is OllamaStreamResult.ParseError -> ProviderImageGenerationResult.Failed(
                reason = streamResult.reason,
                category = ProviderImageGenerationError.PARSE,
            )

            is OllamaStreamResult.Success -> {
                val base64 = streamResult.chunk.imageBase64
                val bytes = base64
                    ?.takeIf { it.isNotBlank() }
                    ?.let { runCatching { Base64.decode(it) }.getOrNull() }
                    ?: return ProviderImageGenerationResult.Failed(
                        reason = "ollama_image_stream_missing_image",
                        category = ProviderImageGenerationError.PARSE,
                    )
                ProviderImageGenerationResult.Success(
                    image = ProviderGeneratedImage(
                        bytes = bytes,
                        mimeType = streamResult.chunk.mimeType ?: "image/png",
                    )
                )
            }
        }
    }
}

private fun buildOpenAiImageGenerationPayload(
    model: String,
    request: ProviderImageGenerationRequest,
): JsonObject = buildJsonObject {
    put("model", JsonPrimitive(model))
    put("prompt", JsonPrimitive(request.prompt))
    put("size", JsonPrimitive(request.size))
    put("quality", JsonPrimitive(request.quality))
    put("n", JsonPrimitive(1))
    put("output_format", JsonPrimitive("png"))
}

private fun buildOllamaImageGenerationPayload(
    model: String,
    request: ProviderImageGenerationRequest,
): JsonObject = buildJsonObject {
    val size = parseImageSize(request.size)
    put("model", JsonPrimitive(model))
    put("prompt", JsonPrimitive(request.prompt))
    put("stream", JsonPrimitive(true))
    put("width", JsonPrimitive(size.width))
    put("height", JsonPrimitive(size.height))
    put("steps", JsonPrimitive(qualityToSteps(request.quality)))
}

private fun shouldUseOllamaApi(
    endpoint: String,
    model: String,
): Boolean {
    val normalized = endpoint.lowercase()
    return model.startsWith("x/") ||
        normalized.contains(":11434") ||
        normalized.contains("localhost") ||
        normalized.contains("127.0.0.1") ||
        normalized.contains("ollama")
}

private fun requiresApiKey(
    endpoint: String,
    model: String,
): Boolean = !shouldUseOllamaApi(endpoint, model)

private fun shouldSendBearerHeader(
    endpoint: String,
    apiKey: String,
): Boolean = apiKey.isNotBlank() && !endpoint.lowercase().contains("localhost") &&
    !endpoint.lowercase().contains("127.0.0.1") &&
    !endpoint.lowercase().contains(":11434")

private fun effectiveTimeoutMs(
    config: ProviderRoutingConfig,
    useOllamaApi: Boolean,
): Long = if (useOllamaApi) {
    max(config.timeoutMs, 120_000L)
} else {
    config.timeoutMs
}

private data class ImageSize(
    val width: Int,
    val height: Int,
)

private fun parseImageSize(size: String): ImageSize {
    val parts = size
        .lowercase()
        .split('x')
        .mapNotNull { it.trim().toIntOrNull() }
    if (parts.size == 2) {
        return ImageSize(width = parts[0], height = parts[1])
    }
    return ImageSize(width = 1536, height = 1024)
}

private fun qualityToSteps(quality: String): Int = when (quality.lowercase()) {
    "low" -> 12
    "high" -> 28
    else -> 20
}

private sealed interface OllamaStreamResult {
    data class Success(
        val chunk: OllamaImageStreamChunk,
    ) : OllamaStreamResult

    data class HttpError(
        val reason: String,
    ) : OllamaStreamResult

    data class ParseError(
        val reason: String,
    ) : OllamaStreamResult
}

private data class OllamaImageStreamChunk(
    val completed: Int? = null,
    val total: Int? = null,
    val done: Boolean = false,
    val doneReason: String? = null,
    val status: String? = null,
    val imageBase64: String? = null,
    val mimeType: String? = null,
)

private fun parseOllamaImageStreamChunk(
    jsonText: String,
): OllamaImageStreamChunk? {
    val root = runCatching {
        Json.parseToJsonElement(jsonText).jsonObject
    }.getOrNull() ?: return null

    return OllamaImageStreamChunk(
        completed = root.int("completed"),
        total = root.int("total"),
        done = root.boolean("done") ?: false,
        doneReason = root.string("done_reason"),
        status = root.string("status") ?: root.string("response"),
        imageBase64 = root.string("image")
            ?: (root["images"] as? JsonArray)?.firstOrNull()?.let { element ->
                (element as? JsonPrimitive)?.contentOrNull
            },
        mimeType = root.string("mime_type"),
    )
}

@OptIn(ExperimentalEncodingApi::class)
internal suspend fun parseGenerationResponse(
    jsonText: String,
    downloadBytes: suspend (String) -> ByteArray?,
): ProviderImageGenerationResult.Success? {
    val root = runCatching {
        Json.parseToJsonElement(jsonText).jsonObject
    }.getOrNull() ?: return null

    val firstData = (root["data"] as? JsonArray)
        ?.firstOrNull()
        ?.jsonObject
    val mimeType = firstData?.string("mime_type")
        ?: root.string("mime_type")
        ?: "image/png"
    val revisedPrompt = firstData?.string("revised_prompt")
        ?: root.string("revised_prompt")
    val base64 = firstData?.string("b64_json")
        ?: firstData?.string("b64")
        ?: firstData?.string("base64")
        ?: firstData?.string("image_base64")
        ?: root.string("imageBase64")
        ?: root.string("b64_json")
    val bytes = when {
        base64.isNullOrBlank().not() -> runCatching { Base64.decode(base64) }.getOrNull()
        else -> {
            val url = firstData?.string("url")
                ?: root.string("url")
            if (url.isNullOrBlank()) {
                null
            } else {
                downloadBytes(url)
            }
        }
    } ?: return null

    return ProviderImageGenerationResult.Success(
        image = ProviderGeneratedImage(
            bytes = bytes,
            mimeType = mimeType,
            revisedPrompt = revisedPrompt,
        )
    )
}

internal fun JsonObject.string(key: String): String? {
    return (this[key] as? JsonPrimitive)?.contentOrNull
}

internal fun JsonObject.int(key: String): Int? {
    return (this[key] as? JsonPrimitive)?.contentOrNull?.toIntOrNull()
}

internal fun JsonObject.boolean(key: String): Boolean? {
    return (this[key] as? JsonPrimitive)?.contentOrNull?.toBooleanStrictOrNull()
}
