package tech.zhifu.app.myhub.settings.data.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.datastore.model.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.settings.data.resolver.SettingValueResolver
import tech.zhifu.app.myhub.settings.data.store.LocalSettingStore
import tech.zhifu.app.myhub.settings.data.store.SettingSerializer
import tech.zhifu.app.myhub.settings.domain.Setting
import tech.zhifu.app.myhub.settings.domain.SettingScope

/**
 * 设置项实现
 */
class SettingImpl<T>(
    override val key: String,
    override val scope: SettingScope,
    override val defaultValue: T,
    private val localStore: LocalSettingStore,
    private val userRepository: UserRepository?,
    private val serializer: SettingSerializer<T>,
    private val userPreferenceExtractor: ((UserPreferences) -> T?)? = null,
    private val userPreferenceUpdater: ((UserPreferences, T) -> UserPreferences)? = null
) : Setting<T> {

    private val resolver = SettingValueResolver(
        key = key,
        scope = scope,
        defaultValue = defaultValue,
        localStore = localStore,
        userRepository = userRepository,
        serializer = serializer,
        userPreferenceExtractor = userPreferenceExtractor
    )

    private val _value = MutableStateFlow<T>(defaultValue)

    // 使用独立的 CoroutineScope 来加载初始值
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var isInitialized = false

    override fun observe(): StateFlow<T> {
        // 在第一次观察时加载值
        if (!isInitialized) {
            coroutineScope.launch {
                try {
                    _value.value = resolver.resolve()
                    isInitialized = true
                } catch (e: Exception) {
                    // 如果加载失败，使用默认值
                    _value.value = defaultValue
                    isInitialized = true
                }
            }
        }
        return _value.asStateFlow()
    }

    override suspend fun get(): T {
        val currentValue = resolver.resolve()
        _value.value = currentValue
        isInitialized = true
        return currentValue
    }

    override suspend fun set(value: T) {
        // 保存到本地存储
        resolver.saveToLocal(value)

        // 如果作用域是 USER，同步到用户偏好
        if (scope == SettingScope.USER && userPreferenceUpdater != null) {
            userRepository?.getCurrentUser()?.let { user ->
                val updatedPrefs = userPreferenceUpdater.invoke(
                    user.preferences ?: UserPreferences(),
                    value
                )
                val updatedUser = user.copy(preferences = updatedPrefs)
                userRepository.updateUser(updatedUser)
            }
        }

        // 更新状态流
        _value.update { value }
    }

    override suspend fun reset() {
        set(defaultValue)
    }
}

