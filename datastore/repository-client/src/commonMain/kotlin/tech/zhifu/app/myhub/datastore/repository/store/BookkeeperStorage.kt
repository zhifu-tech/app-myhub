package tech.zhifu.app.myhub.datastore.repository.store

/**
 * Bookkeeper 数据存储接口
 *
 * 用于追踪失败的同步操作时间戳。
 * Store5 的 Bookkeeper 组件使用此接口来：
 * - 记录写入失败的时间戳
 * - 查询是否有待重试的操作
 * - 清除成功同步后的记录
 */
interface BookkeeperStorage {

    /**
     * 获取上次失败的同步时间戳
     *
     * @param key 同步操作的唯一标识（如 "card:123" 或 "cards:user-1"）
     * @return 失败时间戳（毫秒），如果没有失败记录则返回 null
     */
    suspend fun getLastFailedSync(key: String): Long?

    /**
     * 设置失败的同步时间戳
     *
     * @param key 同步操作的唯一标识
     * @param timestamp 失败时间戳（毫秒）
     * @return 是否设置成功
     */
    suspend fun setLastFailedSync(key: String, timestamp: Long): Boolean

    /**
     * 清除指定 key 的失败同步记录
     *
     * 当同步成功后调用此方法清除记录
     *
     * @param key 同步操作的唯一标识
     * @return 是否清除成功
     */
    suspend fun clearFailedSync(key: String): Boolean

    /**
     * 清除所有失败同步记录
     *
     * @return 是否清除成功
     */
    suspend fun clearAllFailedSyncs(): Boolean
}
