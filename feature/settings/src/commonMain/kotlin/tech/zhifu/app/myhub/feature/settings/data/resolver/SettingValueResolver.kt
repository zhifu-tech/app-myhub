package tech.zhifu.app.myhub.feature.settings.data.resolver

import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.settings.data.store.LocalSettingStore
import tech.zhifu.app.myhub.feature.settings.data.store.SettingSerializer
import tech.zhifu.app.myhub.feature.settings.domain.SettingScope

/**
 * 设置值解析器
 * 按照优先级从多个数据源获取设置值
 *
 * 优先级（从高到低）：
 * 1. 用户偏好（USER_PREFERENCE）- 从 UserRepository 获取
 * 2. 应用默认（APP_DEFAULT）- 从本地存储获取
 * 3. 系统默认（SYSTEM_DEFAULT）- 硬编码的默认值
 */
class SettingValueResolver<T>(
    private val key: String,
    private val scope: SettingScope,
    private val defaultValue: T,
    private val localStore: LocalSettingStore,
    private val userRepository: UserRepository?,
    private val serializer: SettingSerializer<T>,
    private val userPreferenceExtractor: ((UserPreferences) -> T?)? = null
) {
    /**
     * 解析设置值
     */
    suspend fun resolve(): T = when (scope) {
        SettingScope.USER -> {
            // 1. 尝试从用户偏好获取
            userRepository?.getUserPreferences()?.let { prefs ->
                userPreferenceExtractor?.invoke(prefs)
            }
            // 2. 尝试从本地存储获取
                ?: localStore.get(key)?.let {
                    serializer.deserialize(it)
                }
                // 3. 使用默认值
                ?: defaultValue
        }

        SettingScope.APP -> {
            // 1. 从本地存储获取
            localStore.get(key)?.let {
                serializer.deserialize(it)
            }
            // 2. 使用默认值
                ?: defaultValue
        }

        SettingScope.SESSION -> {
            // 仅从内存获取（不持久化）
            defaultValue
        }
    }

    /**
     * 保存设置值到本地存储
     * 注意：用户偏好的更新由 SettingImpl 处理
     */
    suspend fun saveToLocal(value: T) {
        when (scope) {
            SettingScope.USER, SettingScope.APP -> {
                // 保存到本地存储
                localStore.set(key, serializer.serialize(value))
            }

            SettingScope.SESSION -> {
                // 会话级设置不持久化
            }
        }
    }
}

