package tech.zhifu.app.myhub.feature.ai.layer.agent

import tech.zhifu.app.myhub.settings.LocalSettingStore

interface ProviderConfigSource {
    fun current(): ProviderRoutingConfig
}

interface MutableProviderConfigSource : ProviderConfigSource {
    suspend fun update(config: ProviderRoutingConfig)
}

class SettingsProviderConfigSource(
    private val localSettingStore: LocalSettingStore,
) : MutableProviderConfigSource {
    override fun current(): ProviderRoutingConfig {
        val mode = localSettingStore.getSync(KEY_MODE)
            ?.let { runCatching { ProviderMode.valueOf(it) }.getOrNull() }
            ?: ProviderMode.DISABLED
        return ProviderRoutingConfig(
            mode = mode,
            directEndpoint = localSettingStore.getSync(KEY_DIRECT_ENDPOINT).orEmpty(),
            directModel = localSettingStore.getSync(KEY_DIRECT_MODEL).orEmpty(),
            directApiKey = localSettingStore.getSync(KEY_DIRECT_API_KEY).orEmpty(),
            timeoutMs = localSettingStore.getSync(KEY_TIMEOUT_MS)?.toLongOrNull() ?: 15_000L,
            maxRetries = localSettingStore.getSync(KEY_MAX_RETRIES)?.toIntOrNull() ?: 1,
            healthFailThreshold = localSettingStore.getSync(KEY_HEALTH_FAIL_THRESHOLD)?.toIntOrNull() ?: 3,
            circuitOpenMs = localSettingStore.getSync(KEY_CIRCUIT_OPEN_MS)?.toLongOrNull() ?: 60_000L,
        )
    }

    override suspend fun update(config: ProviderRoutingConfig) {
        localSettingStore.set(KEY_MODE, config.mode.name)
        localSettingStore.set(KEY_DIRECT_ENDPOINT, config.directEndpoint)
        localSettingStore.set(KEY_DIRECT_MODEL, config.directModel)
        localSettingStore.set(KEY_DIRECT_API_KEY, config.directApiKey)
        localSettingStore.set(KEY_TIMEOUT_MS, config.timeoutMs.toString())
        localSettingStore.set(KEY_MAX_RETRIES, config.maxRetries.toString())
        localSettingStore.set(KEY_HEALTH_FAIL_THRESHOLD, config.healthFailThreshold.toString())
        localSettingStore.set(KEY_CIRCUIT_OPEN_MS, config.circuitOpenMs.toString())
    }

    private companion object {
        private const val KEY_MODE = "ai.mode"
        private const val KEY_DIRECT_ENDPOINT = "ai.direct.endpoint"
        private const val KEY_DIRECT_MODEL = "ai.direct.model"
        private const val KEY_DIRECT_API_KEY = "ai.direct.apiKey"
        private const val KEY_TIMEOUT_MS = "ai.request.timeoutMs"
        private const val KEY_MAX_RETRIES = "ai.request.maxRetries"
        private const val KEY_HEALTH_FAIL_THRESHOLD = "ai.health.failThreshold"
        private const val KEY_CIRCUIT_OPEN_MS = "ai.health.circuitOpenMs"
    }
}
