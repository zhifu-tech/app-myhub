package tech.zhifu.app.myhub.settings.domain

/**
 * 设置仓库接口
 * 负责注册和管理所有设置项
 */
interface SettingsRepository {
    /**
     * 注册设置项
     */
    fun <T> register(setting: Setting<T>)

    /**
     * 获取设置项
     *
     * @param key 设置项的键
     * @return 设置项，如果不存在则返回 null
     */
    fun <T> get(key: String): Setting<T>?

    /**
     * 获取所有设置项
     */
    fun getAll(): List<Setting<*>>
}

