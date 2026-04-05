package tech.zhifu.app.myhub.ui.state.ai

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository

interface ProviderState {
    val userRepository: UserRepository
    val providerRoutingConfigStateFlow: StateFlow<ProviderRoutingConfig>
}

@Serializable
data class ProviderRoutingConfig(
    val mode: ProviderMode = ProviderMode.DISABLED,
    val directEndpoint: String = "http://localhost:11434",
    val directModel: String = "qwen3:0.6b",
    val directApiKey: String = "ollama",
    val timeoutMs: Long = 15_000L,
    val maxRetries: Int = 1,
    val healthFailThreshold: Int = 3,
    val circuitOpenMs: Long = 60_000,
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

