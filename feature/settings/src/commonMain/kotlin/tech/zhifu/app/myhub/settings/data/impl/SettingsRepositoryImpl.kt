package tech.zhifu.app.myhub.settings.data.impl

import tech.zhifu.app.myhub.settings.domain.Setting
import tech.zhifu.app.myhub.settings.domain.SettingsRepository

/**
 * 设置仓库实现
 */
internal class SettingsRepositoryImpl : SettingsRepository {
    private val settings = mutableMapOf<String, Setting<*>>()

    override fun <T> register(setting: Setting<T>) {
        settings[setting.key] = setting
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): Setting<T>? {
        return settings[key] as? Setting<T>
    }

    override fun getAll(): List<Setting<*>> {
        return settings.values.toList()
    }
}


