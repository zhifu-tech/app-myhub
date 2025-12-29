package tech.zhifu.app.myhub.settings.data.store

/**
 * 本地设置存储接口
 * 使用 multiplatform-settings 实现跨平台存储
 */
interface LocalSettingStore {
    /**
     * 获取设置值
     */
    suspend fun get(key: String): String?

    /**
     * 设置值
     */
    suspend fun set(key: String, value: String)

    /**
     * 删除设置值
     */
    suspend fun remove(key: String)

    /**
     * 清空所有设置
     */
    suspend fun clear()
}

