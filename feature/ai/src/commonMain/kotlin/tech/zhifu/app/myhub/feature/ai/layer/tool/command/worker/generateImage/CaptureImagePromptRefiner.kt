package tech.zhifu.app.myhub.feature.ai.layer.tool.command.worker.generateImage

import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
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
import kotlinx.serialization.json.jsonObject
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.config.ProviderConfigSource
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

class CaptureImagePromptRefiner(
    private val httpClient: HttpClient,
    private val providerConfigSource: ProviderConfigSource,
) {
    suspend fun refine(
        draft: CaptureDraft,
        language: String,
        basePrompt: String,
    ): String {
        if (basePrompt.isBlank()) return basePrompt
        val config = providerConfigSource.current()
        val endpoint = config.directEndpoint.trimEnd('/')
        val model = config.directModel.trim()
        if (endpoint.isBlank() || model.isBlank()) {
            return basePrompt
        }
        val timeoutMs = config.timeoutMs.coerceAtLeast(20_000L)
        val responseText = withTimeoutOrNull(timeoutMs) {
            val response = httpClient.post("$endpoint/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                timeout {
                    requestTimeoutMillis = timeoutMs
                    connectTimeoutMillis = timeoutMs
                    socketTimeoutMillis = timeoutMs
                }
                if (shouldSendBearerHeader(endpoint, config.directApiKey)) {
                    header(HttpHeaders.Authorization, "Bearer ${config.directApiKey}")
                }
                setBody(
                    buildJsonObject {
                        put("model", JsonPrimitive(model))
                        put("temperature", JsonPrimitive(0.4))
                        put("stream", JsonPrimitive(false))
                        put(
                            "messages",
                            buildJsonArray {
                                add(
                                    buildJsonObject {
                                        put("role", JsonPrimitive("system"))
                                        put(
                                            "content",
                                            JsonPrimitive(
                                                """
                                                You are an expert prompt writer for FLUX-style text-to-image models.
                                                Rewrite the user's capture intent into one precise English image prompt.
                                                Detailed and specific prompts work best.
                                                Favor concrete subject, environment, composition, lighting, material, camera distance, mood, and color palette.
                                                The image must work as a standalone visual, not as UI, poster, collage, or card cover unless explicitly requested.
                                                Only mention visible text when exact wording must appear in the image.
                                                1024x1024-friendly composition is preferred.
                                                Output exactly one prompt and nothing else.
                                                """.trimIndent()
                                            )
                                        )
                                    }
                                )
                                add(
                                    buildJsonObject {
                                        put("role", JsonPrimitive("user"))
                                        put(
                                            "content",
                                            JsonPrimitive(
                                                CaptureImagePromptBuilder.buildRefinerBrief(
                                                    draft = draft,
                                                    language = language,
                                                )
                                            )
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            }
            if (!response.status.isSuccess()) {
                logger.debug {
                    "CaptureImagePromptRefiner HTTP failed " +
                        "status=${response.status.value} endpoint=$endpoint model=$model"
                }
                return@withTimeoutOrNull null
            }
            response.bodyAsText()
        } ?: return basePrompt

        val refined = parseContent(responseText)?.sanitizePrompt().orEmpty()
        logger.debug {
            "CaptureImagePromptRefiner finished " +
                "baseLength=${basePrompt.length} refinedLength=${refined.length} changed=${refined.isNotBlank() && refined != basePrompt}"
        }
        return refined.ifBlank { basePrompt }
    }

    private fun parseContent(
        jsonText: String,
    ): String? {
        val root = runCatching {
            Json.parseToJsonElement(jsonText)
        }.getOrNull() ?: return null
        val choices = root
            .jsonObject["choices"] as? JsonArray
            ?: return null
        val message = choices.firstOrNull()
            ?.jsonObject
            ?.get("message")
            ?.jsonObject
            ?: return null
        val content = message["content"] ?: return null
        return when (content) {
            is JsonPrimitive -> content.content
            is JsonArray -> content
                .joinToString(separator = "\n") { item ->
                    item.jsonObject["text"]
                        ?.let { textElement -> (textElement as? JsonPrimitive)?.content }
                        .orEmpty()
                }
                .ifBlank { null }

            else -> null
        }
    }

    private fun String.sanitizePrompt(): String {
        return lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith("prompt:", ignoreCase = true) }
            .joinToString(" ")
            .trim()
            .removePrefix("\"")
            .removeSuffix("\"")
    }
}

private fun shouldSendBearerHeader(
    endpoint: String,
    apiKey: String,
): Boolean = apiKey.isNotBlank() &&
    !endpoint.lowercase().contains("localhost") &&
    !endpoint.lowercase().contains("127.0.0.1") &&
    !endpoint.lowercase().contains(":11434")
