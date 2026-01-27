package tech.zhifu.app.myhub.datastore.repository.store

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

data class StoreCacheConfig(
    val singleCacheSize: Int,
    val collectionCacheSize: Int,
    val expireAfterWrite: Duration,
    val expireAfterAccess: Duration? = null
) {
    init {
        require(singleCacheSize > 0) { "singleCacheSize must be positive" }
        require(collectionCacheSize > 0) { "collectionCacheSize must be positive" }
    }
}

/**
 * 预定义的 Store 缓存配置
 *
 * 配置说明：
 * - CardStore: 500/50 - 卡片数据量大，需要较大的缓存
 * - TagStore: 200/20 - 标签数据量中等
 * - CollectionStore: 100/20 - 集合数据量中等
 * - TemplateStore: 50/5 - 模板数据量小
 * - UserStore: 10 - 用户数据是单例，不需要 Collection 缓存
 */
object StoreCacheConfigs {
    val CARD = StoreCacheConfig(
        singleCacheSize = 500,
        collectionCacheSize = 50,
        expireAfterWrite = 1.hours
    )

    val TAG = StoreCacheConfig(
        singleCacheSize = 200,
        collectionCacheSize = 20,
        expireAfterWrite = 1.hours
    )

    val COLLECTION = StoreCacheConfig(
        singleCacheSize = 100,
        collectionCacheSize = 20,
        expireAfterWrite = 1.hours
    )

    val TEMPLATE = StoreCacheConfig(
        singleCacheSize = 50,
        collectionCacheSize = 5,
        expireAfterWrite = 1.hours,
    )

    val USER = StoreCacheConfig(
        singleCacheSize = 10,
        collectionCacheSize = 1, // 不使用，但需要提供
        expireAfterWrite = 1.hours
    )
}
