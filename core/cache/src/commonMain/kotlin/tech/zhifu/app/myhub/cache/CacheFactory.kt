package tech.zhifu.app.myhub.cache

/**
 * 缓存工厂接口。
 *
 * 用于隔离底层缓存库的创建方式，保证上层只依赖稳定的工厂接口。
 */
interface CacheFactory {
    fun <K : Any, V : Any> create(config: CacheConfig = CacheConfig()): Cache<K, V>
}

/**
 * 创建一个缓存实例（使用默认实现）。
 */
fun <K : Any, V : Any> cache(
    config: CacheConfig = CacheConfig()
): Cache<K, V> = DefaultCacheFactory.create(config)

