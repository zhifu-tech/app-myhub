package tech.zhifu.app.myhub.ui.state.ai

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository

interface ProviderState {
    val userRepository: UserRepository
    val providerRoutingConfig: StateFlow<ProviderRoutingConfig>
}

@Serializable
data class ProviderRoutingConfig(
    val mode: ProviderMode = ProviderMode.DISABLED,
    val directEndpoint: String = "http://192.168.0.154:11434",
    val directModel: String = "qwen3:0.6b",
    val directVisionModel: String = "qwen3-vl:2b",
//    val directImageModel: String = "gpt-image-1.5",
//    val directImageModel: String = "x/z-image-turbo",
    val directImageModel: String = "x/flux2-klein",
    val directApiKey: String = "ollama",
    val timeoutMs: Long = 15_000L,
    val maxRetries: Int = 1,
    val healthFailThreshold: Int = 3,
    val circuitOpenMs: Long = 60_000,
    val shortcutVisible: Boolean = false
)

@Serializable
enum class ProviderMode {
    @SerialName("disabled")
    DISABLED,

    @SerialName("server_gateway")
    SERVER_GATEWAY,

    @SerialName("direct_api")
    DIRECT_API,
}
