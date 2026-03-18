package tech.zhifu.app.myhub.datastore.repository.collection

import org.mobilenativefoundation.store.core5.StoreKey
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.FetcherResult
import tech.zhifu.app.myhub.datastore.datasource.collection.RemoteCollectionDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.logger.Logger

fun createCollectionStoreFetcher(
    remoteCollectionDataSource: RemoteCollectionDataSource,
    logger: Logger
): CollectionStoreFetcher = Fetcher.ofResult { key ->
    when (key) {
        is CollectionStoreKey.ById -> FetcherResult.Error.Message(
            "CollectionStoreKey.ById requires userId, but it's not available. Use CollectionStoreKey.ByUserPage instead."
        )

        is CollectionStoreKey.ByUser -> {
            val remoteCollections = remoteCollectionDataSource.getCollections(
                userId = key.userId,
                page = key.page,
                pageSize = key.size
            )
            val sortedCollections = applyCollectionSort(remoteCollections, key.sort)
            val collections = applyCollectionFilters(sortedCollections, key.filters)
            FetcherResult.Data(CollectionStoreData.Items.fromCollections(collections, key.userId))
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun applyCollectionFilters(
    items: List<Collection>,
    filters: List<StoreKey.Filter<*>>?
): List<Collection> {
    if (filters.isNullOrEmpty()) return items
    var result = items
    filters.forEach { filter ->
        val typed = filter as? StoreKey.Filter<Collection> ?: return@forEach
        result = typed(result)
    }
    return result
}

private fun applyCollectionSort(
    items: List<Collection>,
    sort: StoreKey.Sort?
): List<Collection> = when (sort ?: StoreKey.Sort.NEWEST) {
    StoreKey.Sort.NEWEST -> items.sortedByDescending { it.updatedAt }
    StoreKey.Sort.OLDEST -> items.sortedBy { it.updatedAt }
    // Collection does not support alphabetical sort; fallback to NEWEST.
    StoreKey.Sort.ALPHABETICAL -> items.sortedByDescending { it.updatedAt }
    StoreKey.Sort.REVERSE_ALPHABETICAL -> items.sortedByDescending { it.updatedAt }
}
