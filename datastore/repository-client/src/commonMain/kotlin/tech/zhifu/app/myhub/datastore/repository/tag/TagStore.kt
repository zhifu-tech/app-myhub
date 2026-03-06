
package tech.zhifu.app.myhub.datastore.repository.tag

import org.mobilenativefoundation.store.cache5.CacheBuilder
import org.mobilenativefoundation.store.cache5.StoreMultiCache
import org.mobilenativefoundation.store.core5.KeyProvider
import org.mobilenativefoundation.store.core5.StoreKey
import org.mobilenativefoundation.store.store5.Bookkeeper
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater
import tech.zhifu.app.myhub.datastore.repository.store.StoreCacheConfig
import tech.zhifu.app.myhub.datastore.repository.store.StoreCacheConfigs

typealias TagStore = MutableStore<TagStoreKey, TagStoreData>
typealias TagStoreCache = StoreMultiCache<String, TagStoreKey, TagStoreData.Single, TagStoreData.Collection, TagStoreData>
typealias TagStoreSourceOfTruth = SourceOfTruth<TagStoreKey, TagStoreData, TagStoreData>
typealias TagStoreBookkeeper = Bookkeeper<TagStoreKey>
typealias TagStoreUpdater = Updater<TagStoreKey, TagStoreData, StoreWriteResponse>
typealias TagStoreFetcher = Fetcher<TagStoreKey, TagStoreData>

fun createTagStoreCache(
    config: StoreCacheConfig = StoreCacheConfigs.TAG
): TagStoreCache = StoreMultiCache(
    keyProvider = object : KeyProvider<String, TagStoreData.Single> {
        override fun fromCollection(
            key: StoreKey.Collection<String>,
            value: TagStoreData.Single
        ): StoreKey.Single<String> = TagStoreKey.ById(value.id)

        override fun fromSingle(
            key: StoreKey.Single<String>,
            value: TagStoreData.Single
        ): StoreKey.Collection<String> = TagStoreKey.ByUser(value.tag.userId)
    },
    singlesCache = CacheBuilder<StoreKey.Single<String>, TagStoreData.Single>()
        .maximumSize(config.singleCacheSize.toLong())
        .expireAfterWrite(config.expireAfterWrite)
        .apply {
            config.expireAfterAccess?.let { expireAfterAccess(it) }
        }
        .build(),
    collectionsCache = CacheBuilder<StoreKey.Collection<String>, TagStoreData.Collection>()
        .maximumSize(config.collectionCacheSize.toLong())
        .expireAfterWrite(config.expireAfterWrite)
        .apply {
            config.expireAfterAccess?.let { expireAfterAccess(it) }
        }
        .build()
)
