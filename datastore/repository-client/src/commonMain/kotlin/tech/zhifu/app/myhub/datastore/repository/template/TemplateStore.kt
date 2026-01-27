@file:OptIn(ExperimentalStoreApi::class)

package tech.zhifu.app.myhub.datastore.repository.template

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

typealias TemplateStore = MutableStore<TemplateStoreKey, TemplateStoreData>
typealias TemplateStoreCache = StoreMultiCache<String, TemplateStoreKey, TemplateStoreData.Single, TemplateStoreData.Collection, TemplateStoreData>
typealias TemplateStoreSourceOfTruth = SourceOfTruth<TemplateStoreKey, TemplateStoreData, TemplateStoreData>
typealias TemplateStoreBookkeeper = Bookkeeper<TemplateStoreKey>
typealias TemplateStoreUpdater = Updater<TemplateStoreKey, TemplateStoreData, StoreWriteResponse>
typealias TemplateStoreFetcher = Fetcher<TemplateStoreKey, TemplateStoreData>

@OptIn(ExperimentalStoreApi::class)
fun createTemplateStoreCache(
    config: StoreCacheConfig = StoreCacheConfigs.TEMPLATE
): TemplateStoreCache = StoreMultiCache(
    keyProvider = object : KeyProvider<String, TemplateStoreData.Single> {
        override fun fromCollection(
            key: StoreKey.Collection<String>,
            value: TemplateStoreData.Single
        ): StoreKey.Single<String> = TemplateStoreKey.ById(value.id)

        override fun fromSingle(
            key: StoreKey.Single<String>,
            value: TemplateStoreData.Single
        ): StoreKey.Collection<String> = TemplateStoreKey.All()
    },
    singlesCache = CacheBuilder<StoreKey.Single<String>, TemplateStoreData.Single>()
        .maximumSize(config.singleCacheSize.toLong())
        .expireAfterWrite(config.expireAfterWrite)
        .apply {
            config.expireAfterAccess?.let { expireAfterAccess(it) }
        }
        .build(),
    collectionsCache = CacheBuilder<StoreKey.Collection<String>, TemplateStoreData.Collection>()
        .maximumSize(config.collectionCacheSize.toLong())
        .expireAfterWrite(config.expireAfterWrite)
        .apply {
            config.expireAfterAccess?.let { expireAfterAccess(it) }
        }
        .build()
)
