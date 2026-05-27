package tech.zhifu.app.myhub.cache

/**
 * MyHub 缓存接口。
 *
 * 目标：对底层缓存库提供稳定、统一的访问接口，避免上层代码直接依赖第三方 API。
 *
 * 说明：
 * - K 和 V 都要求非空类型（与底层 Cache4k 保持一致）。
 * - value 不存在时返回 null。
 */
interface Cache<K : Any, V : Any> {
    fun get(key: K): V?

    suspend fun getOrPut(
        key: K,
        loader: () -> V
    ): V

    fun put(
        key: K,
        value: V
    )

    fun invalidate(key: K)

    fun invalidateAll()
}

