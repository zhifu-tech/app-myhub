package tech.zhifu.app.myhub.datastore.repository.collection

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import tech.zhifu.app.myhub.datastore.datasource.LocalCollectionDataSource

@OptIn(ExperimentalStoreApi::class)
fun createCollectionStoreFetcher(
    localCollectionDataSource: LocalCollectionDataSource
): CollectionStoreFetcher = Fetcher.of { key ->
    // 当前没有 RemoteCollectionDataSource，从本地数据源获取
    when (key) {
        is CollectionStoreKey.ById -> {
            val collection = localCollectionDataSource.getCollection(key.id)
                ?: throw NoSuchElementException("Collection not found: ${key.id}")
            CollectionStoreData.Single(collection)
        }

        is CollectionStoreKey.ByUser -> {
            val collections = localCollectionDataSource.getCollections(key.userId)
            CollectionStoreData.Items.fromCollections(collections, key.userId)
        }
    }
}
