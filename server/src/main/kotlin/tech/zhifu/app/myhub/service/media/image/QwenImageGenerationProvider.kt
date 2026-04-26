package tech.zhifu.app.myhub.service.media.image

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import tech.zhifu.app.myhub.service.media.analysis.AnalysisProviderConfig
import tech.zhifu.app.myhub.service.media.analysis.ProviderHttpSupport

class QwenImageGenerationProvider(
    private val config: AnalysisProviderConfig,
) : ImageGenerationProvider {
    override fun generate(
        request: CaptureImageGenerationRequest,
    ): GeneratedImagePayload {
        val apiKey = config.qwenApiKey
            ?: throw IllegalStateException("QWEN_API_KEY is required when CAPTURE_ANALYSIS_PROVIDER=qwen")
        val response = ProviderHttpSupport.withRetry(
            maxAttempts = config.retryMax,
            initialBackoffMs = config.retryBackoffMs,
        ) {
            ProviderHttpSupport.postJson(
                url = "${config.qwenImageBaseUrl.trimEnd('/')}/services/aigc/multimodal-generation/generation",
                body = buildQwenImageRequestBody(
                    model = config.qwenImageModel,
                    prompt = request.prompt,
                    size = request.size.replace('x', '*'),
                ),
                timeoutMs = config.timeoutMs,
                headers = mapOf(
                    "Authorization" to "Bearer $apiKey",
                ),
            )
        }
        if (response.statusCode() !in 200..299) {
            throw IllegalStateException("qwen_image http ${response.statusCode()}: ${response.body()}")
        }

        val imageUrl = parseQwenImageUrl(response.body())
            ?: throw IllegalStateException("qwen_image_missing_url")
        val imageResponse = ProviderHttpSupport.getBytes(
            url = imageUrl,
            timeoutMs = config.timeoutMs,
        )
        if (imageResponse.statusCode() !in 200..299) {
            throw IllegalStateException("qwen_image_download http ${imageResponse.statusCode()}")
        }
        return GeneratedImagePayload(
            bytes = imageResponse.body(),
            mimeType = "image/png",
        )
    }
}

private fun buildQwenImageRequestBody(
    model: String,
    prompt: String,
    size: String,
): String {
    return buildJsonObject {
        put("model", JsonPrimitive(model))
        put(
            "input",
            buildJsonObject {
                put(
                    "messages",
                    buildJsonArray {
                        add(
                            buildJsonObject {
                                put("role", JsonPrimitive("user"))
                                put(
                                    "content",
                                    buildJsonArray {
                                        add(
                                            buildJsonObject {
                                                put("text", JsonPrimitive(prompt))
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            }
        )
        put(
            "parameters",
            buildJsonObject {
                put(
                    "negative_prompt",
                    JsonPrimitive("low resolution, blurry, distorted text, watermark, logo, ui overlay, malformed hands, extra limbs, cluttered composition"),
                )
                put("prompt_extend", JsonPrimitive(true))
                put("watermark", JsonPrimitive(false))
                put("size", JsonPrimitive(size))
            }
        )
    }.toString()
}

private fun parseQwenImageUrl(
    body: String,
): String? {
    val root = runCatching {
        Json.parseToJsonElement(body).jsonObject
    }.getOrNull() ?: return null
    val output = root["output"]?.jsonObject ?: return null
    val choices = output["choices"] as? JsonArray
    val contentUrl = choices
        ?.firstOrNull()
        ?.jsonObject
        ?.get("message")
        ?.jsonObject
        ?.get("content")
        ?.let { it as? JsonArray }
        ?.firstOrNull()
        ?.jsonObject
        ?.string("image")
    if (!contentUrl.isNullOrBlank()) return contentUrl
    return output.string("image")
}

private fun JsonObject.string(
    key: String,
): String? = (this[key] as? JsonPrimitive)?.contentOrNull
