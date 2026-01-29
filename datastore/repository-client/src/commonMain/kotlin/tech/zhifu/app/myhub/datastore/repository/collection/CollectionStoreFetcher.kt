package tech.zhifu.app.myhub.datastore.repository.collection

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.FetcherResult
import tech.zhifu.app.myhub.datastore.datasource.RemoteCollectionDataSource
import tech.zhifu.app.myhub.logger.Logger

@OptIn(ExperimentalStoreApi::class)
fun createCollectionStoreFetcher(
    remoteCollectionDataSource: RemoteCollectionDataSource,
    logger: Logger
): CollectionStoreFetcher = Fetcher.ofResult { key ->
    when (key) {
        is CollectionStoreKey.ById -> FetcherResult.Error.Message(
            "CollectionStoreKey.ById requires userId, but it's not available. Use CollectionStoreKey.ByUser instead."
        )

        is CollectionStoreKey.ByUser -> {
            val collections = remoteCollectionDataSource.getCollections(
                userId = key.userId,
                page = key.page,
                pageSize = key.pageSize
            )
            FetcherResult.Data(CollectionStoreData.Items.fromCollections(collections, key.userId))
        }
    }
}
