package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.cache5.CacheBuilder
import org.mobilenativefoundation.store.cache5.StoreMultiCache
import org.mobilenativefoundation.store.core5.KeyProvider
import org.mobilenativefoundation.store.core5.StoreKey
import tech.zhifu.app.myhub.datastore.repository.store.StoreCacheConfig
import tech.zhifu.app.myhub.datastore.repository.store.StoreCacheConfigs

fun createCardStoreCache(
    config: StoreCacheConfig = StoreCacheConfigs.CARD
): CardStoreCache = StoreMultiCache(
    keyProvider = object : KeyProvider<String, CardStoreData.Single> {
        override fun fromCollection(
            key: StoreKey.Collection<String>,
            value: CardStoreData.Single
        ): StoreKey.Single<String> = CardStoreKey.ById(value.userId, value.id)

        override fun fromSingle(
            key: StoreKey.Single<String>,
            value: CardStoreData.Single
        ): StoreKey.Collection<String> = CardStoreKey.ByUserCursor(
            userId = value.userId,
        )
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
