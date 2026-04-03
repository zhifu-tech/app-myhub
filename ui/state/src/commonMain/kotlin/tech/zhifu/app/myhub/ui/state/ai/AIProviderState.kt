package tech.zhifu.app.myhub.ui.state.ai

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository

interface AIProviderState {
    val userRepository: UserRepository
    val aiProviderStateFlow: StateFlow<AIProvider>
}

enum class AIProviderMode(val wireValue: String) {
    Disabled("DISABLED"),
    ServerGateway("SERVER_GATEWAY"),
    DirectApi("DIRECT_API");

    companion object {
        fun fromWire(value: String?): AIProviderMode? {
            return entries.firstOrNull { it.wireValue == value?.trim()?.uppercase() }
        }
    }
}

data class AIProvider(
    val mode: AIProviderMode = AIProviderMode.Disabled,
    val directEndpoint: String = "",
    val directModel: String = "",
    val directApiKey: String = "",
    val timeoutMs: Long = 15_000L,
    val maxRetries: Int = 1,
) {
    fun toJsonText(): String {
        return buildJsonObject {
            put(KEY_MODE, JsonPrimitive(mode.wireValue))
            put(KEY_DIRECT_ENDPOINT, JsonPrimitive(directEndpoint))
            put(KEY_DIRECT_MODEL, JsonPrimitive(directModel))
            put(KEY_DIRECT_API_KEY, JsonPrimitive(directApiKey))
            put(KEY_TIMEOUT_MS, JsonPrimitive(timeoutMs))
            put(KEY_MAX_RETRIES, JsonPrimitive(maxRetries))
        }.toString()
    }

    companion object {
        fun fromJsonText(text: String?): AIProvider {
            val obj = runCatching {
                Json.parseToJsonElement(text.orEmpty()).jsonObject
            }.getOrDefault(JsonObject(emptyMap()))

            return AIProvider(
                mode = AIProviderMode.fromWire(
                    obj[KEY_MODE]?.jsonPrimitive?.content
                ) ?: AIProviderMode.Disabled,
                directEndpoint = obj[KEY_DIRECT_ENDPOINT]?.jsonPrimitive?.content.orEmpty(),
                directModel = obj[KEY_DIRECT_MODEL]?.jsonPrimitive?.content.orEmpty(),
                directApiKey = obj[KEY_DIRECT_API_KEY]?.jsonPrimitive?.content.orEmpty(),
                timeoutMs = obj[KEY_TIMEOUT_MS]?.jsonPrimitive?.content?.toLongOrNull() ?: 15_000L,
                maxRetries = obj[KEY_MAX_RETRIES]?.jsonPrimitive?.content?.toIntOrNull() ?: 1,
            )
        }
    }
}

private const val KEY_MODE = "mode"
private const val KEY_DIRECT_ENDPOINT = "directEndpoint"
private const val KEY_DIRECT_MODEL = "directModel"
private const val KEY_DIRECT_API_KEY = "directApiKey"
private const val KEY_TIMEOUT_MS = "timeoutMs"
private const val KEY_MAX_RETRIES = "maxRetries"
