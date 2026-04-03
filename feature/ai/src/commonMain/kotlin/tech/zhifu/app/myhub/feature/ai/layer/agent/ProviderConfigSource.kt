package tech.zhifu.app.myhub.feature.ai.layer.agent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.ui.state.ai.AIProvider
import tech.zhifu.app.myhub.ui.state.ai.AIProviderMode

interface ProviderConfigSource {
    fun current(): ProviderRoutingConfig
}

interface MutableProviderConfigSource : ProviderConfigSource {
    suspend fun update(config: ProviderRoutingConfig)
}

class SettingsProviderConfigSource(
    private val userRepository: UserRepository,
) : MutableProviderConfigSource {

    private var cachedConfig: ProviderRoutingConfig = ProviderRoutingConfig()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            userRepository.userFlow()
                .filterNotNull()
                .distinctUntilChangedBy { it.id }
                .flatMapLatest { user ->
                    userRepository
                        .userPreferencesFlow(user.id)
                        .map { it?.aiProvider }
                }
                .collect { aiProviderJson ->
                    cachedConfig = aiProviderJson
                        .toProviderRoutingConfig()
                }
        }
    }

    override fun current(): ProviderRoutingConfig = cachedConfig

    override suspend fun update(config: ProviderRoutingConfig) {
        val user = userRepository.requireUser()
        userRepository.updateUserPreferencesAiProvider(
            userId = user.id,
            aiProvider = config.toAIProvider().toJsonText(),
        )
    }
}

private fun String?.toProviderRoutingConfig(): ProviderRoutingConfig {
    val provider = AIProvider.fromJsonText(this)
    return ProviderRoutingConfig(
        mode = when (provider.mode) {
            AIProviderMode.Disabled -> ProviderMode.DISABLED
            AIProviderMode.ServerGateway -> ProviderMode.SERVER_GATEWAY
            AIProviderMode.DirectApi -> ProviderMode.DIRECT_API
        },
        directEndpoint = provider.directEndpoint,
        directModel = provider.directModel,
        directApiKey = provider.directApiKey,
        timeoutMs = provider.timeoutMs,
        maxRetries = provider.maxRetries,
        healthFailThreshold = 3,
        circuitOpenMs = 60_000L,
    )
}

private fun ProviderRoutingConfig.toAIProvider(): AIProvider {
    return AIProvider(
        mode = when (mode) {
            ProviderMode.DISABLED -> AIProviderMode.Disabled
            ProviderMode.SERVER_GATEWAY -> AIProviderMode.ServerGateway
            ProviderMode.DIRECT_API -> AIProviderMode.DirectApi
        },
        directEndpoint = directEndpoint,
        directModel = directModel,
        directApiKey = directApiKey,
        timeoutMs = timeoutMs,
        maxRetries = maxRetries,
    )
}
