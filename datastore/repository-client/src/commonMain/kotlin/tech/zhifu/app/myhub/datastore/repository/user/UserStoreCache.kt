package tech.zhifu.app.myhub.datastore.repository.user

import org.mobilenativefoundation.store.cache5.CacheBuilder
import tech.zhifu.app.myhub.datastore.repository.store.StoreCacheConfig
import tech.zhifu.app.myhub.datastore.repository.store.StoreCacheConfigs

fun createUserStoreCache(
    config: StoreCacheConfig = StoreCacheConfigs.USER
): UserStoreCache = CacheBuilder<UserStoreKey, UserStoreData>()
    .maximumSize(config.singleCacheSize.toLong())
    .expireAfterWrite(config.expireAfterWrite)
    .apply {
        config.expireAfterAccess?.let { expireAfterAccess(it) }
    }
    .build()
