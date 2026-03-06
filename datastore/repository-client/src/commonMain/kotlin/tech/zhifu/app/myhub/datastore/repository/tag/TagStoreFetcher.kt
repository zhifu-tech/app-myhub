package tech.zhifu.app.myhub.datastore.repository.tag

import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.FetcherResult
import tech.zhifu.app.myhub.datastore.datasource.RemoteTagDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug

fun createTagStoreFetcher(
    remoteTagDataSource: RemoteTagDataSource,
    logger: Logger
): TagStoreFetcher = Fetcher.ofResult { key ->
    when (key) {
        is TagStoreKey.ById -> {
            logger.debug { "TagStoreKey.ById requires userId, but it's not available. Use TagStoreKey.ByUser instead" }
            FetcherResult.Error.Message(
                "TagStoreKey.ById requires userId, but it's not available. Use TagStoreKey.ByUser instead."
            )
        }

        is TagStoreKey.ByUser -> {
            val tags = remoteTagDataSource.getTags(key.userId)
            FetcherResult.Data(TagStoreData.Collection.fromTags(tags, key.userId))
        }
    }
}
