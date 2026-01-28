@file:OptIn(ExperimentalStoreApi::class)

package tech.zhifu.app.myhub.datastore.repository.collection

import org.mobilenativefoundation.store.cache5.CacheBuilder
import org.mobilenativefoundation.store.cache5.StoreMultiCache
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
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

typealias CollectionStore = MutableStore<CollectionStoreKey, CollectionStoreData>
typealias CollectionStoreCache = StoreMultiCache<String, CollectionStoreKey, CollectionStoreData.Single, CollectionStoreData.Items, CollectionStoreData>
typealias CollectionStoreSourceOfTruth = SourceOfTruth<CollectionStoreKey, CollectionStoreData, CollectionStoreData>
typealias CollectionStoreBookkeeper = Bookkeeper<CollectionStoreKey>
typealias CollectionStoreUpdater = Updater<CollectionStoreKey, CollectionStoreData, StoreWriteResponse>
typealias CollectionStoreFetcher = Fetcher<CollectionStoreKey, CollectionStoreData>

@OptIn(ExperimentalStoreApi::class)
fun createCollectionStoreCache(
    config: StoreCacheConfig = StoreCacheConfigs.COLLECTION
): CollectionStoreCache = StoreMultiCache(
    keyProvider = object : KeyProvider<String, CollectionStoreData.Single> {
        override fun fromCollection(
            key: StoreKey.Collection<String>,
            value: CollectionStoreData.Single
        ): StoreKey.Single<String> = CollectionStoreKey.ById(value.id)

        override fun fromSingle(
            key: StoreKey.Single<String>,
            value: CollectionStoreData.Single
        ): StoreKey.Collection<String> = CollectionStoreKey.ByUser(
            userId = value.collection.userId,
            page = 1,
            pageSize = 10
        )
    },
    singlesCache = CacheBuilder<StoreKey.Single<String>, CollectionStoreData.Single>()
        .maximumSize(config.singleCacheSize.toLong())
        .expireAfterWrite(config.expireAfterWrite)
        .apply {
            config.expireAfterAccess?.let { expireAfterAccess(it) }
        }
        .build(),
    collectionsCache = CacheBuilder<StoreKey.Collection<String>, CollectionStoreData.Items>()
        .maximumSize(config.collectionCacheSize.toLong())
        .expireAfterWrite(config.expireAfterWrite)
        .apply {
            config.expireAfterAccess?.let { expireAfterAccess(it) }
        }
        .build()
)
