package tech.zhifu.app.myhub.datastore.repository.collection

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import tech.zhifu.app.myhub.datastore.datasource.RemoteCollectionDataSource

@OptIn(ExperimentalStoreApi::class)
fun createCollectionStoreFetcher(
    remoteCollectionDataSource: RemoteCollectionDataSource
): CollectionStoreFetcher = Fetcher.of { key ->
    when (key) {
        is CollectionStoreKey.ById -> {
            // 注意：新的 RESTful API 需要 userId，但 CollectionStoreKey.ById 不包含 userId
            // 由于 API 使用 JWT 认证，userId 应该从 token 中获取
            // 这里我们无法直接获取 userId，所以先尝试获取所有卡集，然后过滤
            // 这是一个临时解决方案，理想情况下应该从认证上下文获取 userId
            // TODO: 需要从认证上下文获取 userId，或者修改 CollectionStoreKey.ById 以包含 userId
            throw UnsupportedOperationException("CollectionStoreKey.ById requires userId, but it's not available. Use CollectionStoreKey.ByUser instead.")
        }

        is CollectionStoreKey.ByUser -> {
            val collections = remoteCollectionDataSource.getCollections(key.userId)
            CollectionStoreData.Items.fromCollections(collections, key.userId)
        }
    }
}
