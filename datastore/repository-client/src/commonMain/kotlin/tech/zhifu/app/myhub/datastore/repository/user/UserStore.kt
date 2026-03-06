
package tech.zhifu.app.myhub.datastore.repository.user

import org.mobilenativefoundation.store.cache5.Cache
import org.mobilenativefoundation.store.cache5.CacheBuilder
import org.mobilenativefoundation.store.store5.Bookkeeper
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater
import tech.zhifu.app.myhub.datastore.repository.store.StoreCacheConfig
import tech.zhifu.app.myhub.datastore.repository.store.StoreCacheConfigs

typealias UserStore = MutableStore<UserStoreKey, UserStoreData>
typealias UserStoreCache = Cache<UserStoreKey, UserStoreData>
typealias UserStoreSourceOfTruth = SourceOfTruth<UserStoreKey, UserStoreData, UserStoreData>
typealias UserStoreBookkeeper = Bookkeeper<UserStoreKey>
typealias UserStoreUpdater = Updater<UserStoreKey, UserStoreData, StoreWriteResponse>
typealias UserStoreFetcher = Fetcher<UserStoreKey, UserStoreData>

fun createUserStoreCache(
    config: StoreCacheConfig = StoreCacheConfigs.USER
): UserStoreCache = CacheBuilder<UserStoreKey, UserStoreData>()
    .maximumSize(config.singleCacheSize.toLong())
    .expireAfterWrite(config.expireAfterWrite)
    .apply {
        config.expireAfterAccess?.let { expireAfterAccess(it) }
    }
    .build()
