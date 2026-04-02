//package tech.zhifu.app.myhub.feature.settings.content.ai
//
//import kotlinx.serialization.serializer
//import tech.zhifu.app.myhub.datastore.repository.settings.IntSettingSerializer
//import tech.zhifu.app.myhub.datastore.repository.settings.LongSettingSerializer
//import tech.zhifu.app.myhub.datastore.repository.settings.Setting
//import tech.zhifu.app.myhub.datastore.repository.settings.SettingImpl
//import tech.zhifu.app.myhub.datastore.repository.settings.SettingScope
//import tech.zhifu.app.myhub.datastore.repository.settings.SettingsRepository
//import tech.zhifu.app.myhub.datastore.repository.settings.StringSettingSerializer
//import tech.zhifu.app.myhub.settings.LocalSettingStore
//
//private const val AI_MODE_KEY = "ai.mode"
//private const val AI_DIRECT_ENDPOINT_KEY = "ai.direct.endpoint"
//private const val AI_DIRECT_MODEL_KEY = "ai.direct.model"
//private const val AI_DIRECT_API_KEY_KEY = "ai.direct.apiKey"
//private const val AI_TIMEOUT_MS_KEY = "ai.request.timeoutMs"
//private const val AI_MAX_RETRIES_KEY = "ai.request.maxRetries"
//
//val SettingsRepository.aiModeSetting: Setting<String>
//    get() = get(AI_MODE_KEY) ?: error("ai.mode setting not found")
//val SettingsRepository.aiDirectEndpointSetting: Setting<String>
//    get() = get(AI_DIRECT_ENDPOINT_KEY) ?: error("ai.direct.endpoint setting not found")
//val SettingsRepository.aiDirectModelSetting: Setting<String>
//    get() = get(AI_DIRECT_MODEL_KEY) ?: error("ai.direct.model setting not found")
//val SettingsRepository.aiDirectApiKeySetting: Setting<String>
//    get() = get(AI_DIRECT_API_KEY_KEY) ?: error("ai.direct.apiKey setting not found")
//val SettingsRepository.aiTimeoutMsSetting: Setting<Long>
//    get() = get(AI_TIMEOUT_MS_KEY) ?: error("ai.request.timeoutMs setting not found")
//val SettingsRepository.aiMaxRetriesSetting: Setting<Int>
//    get() = get(AI_MAX_RETRIES_KEY) ?: error("ai.request.maxRetries setting not found")
//
//internal class AiModeSetting(localStore: LocalSettingStore) : Setting<String> {
//    override val key: String = AI_MODE_KEY
//    override val scope: SettingScope = SettingScope.APP
//    override val defaultValue: String = "DISABLED"
//    private val delegate = SettingImpl(
//        key = key,
//        scope = scope,
//        defaultValue = defaultValue,
//        localStore = localStore,
//        userRepository = null,
//        serializer = serializer()
//    )
//
//    override fun flow() = delegate.flow()
//    override suspend fun get() = delegate.get()
//    override suspend fun set(value: String) = delegate.set(value)
//    override suspend fun reset() = delegate.reset()
//}
//
//internal class AiDirectEndpointSetting(localStore: LocalSettingStore) : Setting<String> {
//    override val key = AI_DIRECT_ENDPOINT_KEY
//    override val scope = SettingScope.APP
//    override val defaultValue: String = ""
//    private val delegate = SettingImpl(
//        key = key,
//        scope = scope,
//        defaultValue = defaultValue,
//        localStore = localStore,
//        userRepository = null,
//        serializer = StringSettingSerializer(),
//    )
//
//    override fun flow() = delegate.flow()
//    override suspend fun get() = delegate.get()
//    override suspend fun set(value: String) = delegate.set(value)
//    override suspend fun reset() = delegate.reset()
//}
//
//internal class AiDirectModelSetting(localStore: LocalSettingStore) : Setting<String> {
//    override val key = AI_DIRECT_MODEL_KEY
//    override val scope = SettingScope.APP
//    override val defaultValue: String = ""
//    private val delegate = SettingImpl(
//        key = key,
//        scope = scope,
//        defaultValue = defaultValue,
//        localStore = localStore,
//        userRepository = null,
//        serializer = StringSettingSerializer(),
//    )
//
//    override fun flow() = delegate.flow()
//    override suspend fun get() = delegate.get()
//    override suspend fun set(value: String) = delegate.set(value)
//    override suspend fun reset() = delegate.reset()
//}
//
//internal class AiDirectApiKeySetting(localStore: LocalSettingStore) : Setting<String> {
//    override val key = AI_DIRECT_API_KEY_KEY
//    override val scope = SettingScope.APP
//    override val defaultValue: String = ""
//    private val delegate = SettingImpl(
//        key = key,
//        scope = scope,
//        defaultValue = defaultValue,
//        localStore = localStore,
//        userRepository = null,
//        serializer = StringSettingSerializer(),
//    )
//
//    override fun flow() = delegate.flow()
//    override suspend fun get() = delegate.get()
//    override suspend fun set(value: String) = delegate.set(value)
//    override suspend fun reset() = delegate.reset()
//}
//
//internal class AiTimeoutMsSetting(localStore: LocalSettingStore) : Setting<Long> {
//    override val key = AI_TIMEOUT_MS_KEY
//    override val scope = SettingScope.APP
//    override val defaultValue: Long = 15_000L
//    private val delegate = SettingImpl(
//        key = key,
//        scope = scope,
//        defaultValue = defaultValue,
//        localStore = localStore,
//        userRepository = null,
//        serializer = LongSettingSerializer(),
//    )
//
//    override fun flow() = delegate.flow()
//    override suspend fun get() = delegate.get()
//    override suspend fun set(value: Long) = delegate.set(value)
//    override suspend fun reset() = delegate.reset()
//}
//
//internal class AiMaxRetriesSetting(localStore: LocalSettingStore) : Setting<Int> {
//    override val key = AI_MAX_RETRIES_KEY
//    override val scope = SettingScope.APP
//    override val defaultValue: Int = 1
//    private val delegate = SettingImpl(
//        key = key,
//        scope = scope,
//        defaultValue = defaultValue,
//        localStore = localStore,
//        userRepository = null,
//        serializer = IntSettingSerializer(),
//    )
//
//    override fun flow() = delegate.flow()
//    override suspend fun get() = delegate.get()
//    override suspend fun set(value: Int) = delegate.set(value)
//    override suspend fun reset() = delegate.reset()
//}
