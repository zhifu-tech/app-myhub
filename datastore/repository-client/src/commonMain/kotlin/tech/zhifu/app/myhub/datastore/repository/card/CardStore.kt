@file:OptIn(ExperimentalStoreApi::class)

package tech.zhifu.app.myhub.datastore.repository.card

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

typealias CardStore = MutableStore<CardStoreKey, CardStoreData>
typealias CardStoreCache = StoreMultiCache<String, CardStoreKey, CardStoreData.Single, CardStoreData.Collection, CardStoreData>
typealias CardStoreSourceOfTruth = SourceOfTruth<CardStoreKey, CardStoreData, CardStoreData>
typealias CardStoreBookkeeper = Bookkeeper<CardStoreKey>
typealias CardStoreUpdater = Updater<CardStoreKey, CardStoreData, StoreWriteResponse>
typealias CardStoreFetcher = Fetcher<CardStoreKey, CardStoreData>


@OptIn(ExperimentalStoreApi::class)
fun createCardStoreCache(
    config: StoreCacheConfig = StoreCacheConfigs.CARD
): CardStoreCache = CardStoreCache(
    keyProvider = object : KeyProvider<String, CardStoreData.Single> {
        override fun fromCollection(
            key: StoreKey.Collection<String>,
            value: CardStoreData.Single
        ): StoreKey.Single<String> = CardStoreKey.ById(value.id)

        override fun fromSingle(
            key: StoreKey.Single<String>,
            value: CardStoreData.Single
        ): StoreKey.Collection<String> = CardStoreKey.ByUser(value.card.userId)
    },
    singlesCache = CacheBuilder<StoreKey.Single<String>, CardStoreData.Single>()
        .maximumSize(config.singleCacheSize.toLong())
        .expireAfterWrite(config.expireAfterWrite)
        .apply {
            config.expireAfterAccess?.let { expireAfterAccess(it) }
        }
        .build(),
    collectionsCache = CacheBuilder<StoreKey.Collection<String>, CardStoreData.Collection>()
        .maximumSize(config.collectionCacheSize.toLong())
        .expireAfterWrite(config.expireAfterWrite)
        .apply {
            config.expireAfterAccess?.let { expireAfterAccess(it) }
        }
        .build()
)
