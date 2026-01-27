package tech.zhifu.app.myhub.settings

/**
 * 本地设置存储接口
 * 跨平台统一的键值存储抽象
 */
interface LocalSettingStore {
    /**
     * 获取设置值（异步）
     */
    suspend fun get(key: String): String?

    /**
     * 获取设置值（同步）
     * 注意：某些平台实现可能需要在后台线程执行
     */
    fun getSync(key: String): String?

    /**
     * 设置值（异步）
     */
    suspend fun set(key: String, value: String)

    /**
     * 设置值（同步）
     */
    fun setSync(key: String, value: String)

    /**
     * 删除设置值（异步）
     */
    suspend fun remove(key: String)

    /**
     * 删除设置值（同步）
     */
    fun removeSync(key: String)

    /**
     * 清空所有设置
     */
    suspend fun clear()
}
