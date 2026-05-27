package tech.zhifu.app.myhub.cache

import io.github.reactivecircus.cache4k.Cache as Cache4k

internal object DefaultCacheFactory : CacheFactory {
    override fun <K : Any, V : Any> create(config: CacheConfig): Cache<K, V> {
        val builder = Cache4k.Builder<K, V>()
            .maximumCacheSize(config.maximumSize)

        config.expireAfterWrite?.let { builder.expireAfterWrite(it) }
        config.expireAfterAccess?.let { builder.expireAfterAccess(it) }

        return Cache4kAdapter(builder.build())
    }
}

private class Cache4kAdapter<K : Any, V : Any>(
    private val delegate: Cache4k<K, V>
) : Cache<K, V> {
    override fun get(key: K): V? = delegate.get(key)
    override suspend fun getOrPut(key: K, loader: () -> V): V = delegate.get(key, loader)
    override fun put(key: K, value: V) = delegate.put(key, value)
    override fun invalidate(key: K) = delegate.invalidate(key)
    override fun invalidateAll() = delegate.invalidateAll()
}

