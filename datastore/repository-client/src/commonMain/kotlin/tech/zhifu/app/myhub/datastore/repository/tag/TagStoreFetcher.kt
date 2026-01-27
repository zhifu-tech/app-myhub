package tech.zhifu.app.myhub.datastore.repository.tag

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import tech.zhifu.app.myhub.datastore.datasource.RemoteTagDataSource

@OptIn(ExperimentalStoreApi::class)
fun createTagStoreFetcher(
    remoteTagDataSource: RemoteTagDataSource
): TagStoreFetcher = Fetcher.of { key ->
    when (key) {
        is TagStoreKey.ById -> {
            val tag = remoteTagDataSource.getTagById(key.id)
                ?: throw NoSuchElementException("Tag not found: ${key.id}")
            TagStoreData.Single(tag)
        }

        is TagStoreKey.ByUser -> {
            // RemoteTagDataSource.getAllTags() 返回所有 tags，需要过滤
            val allTags = remoteTagDataSource.getAllTags()
            val userTags = allTags.filter { it.userId == key.userId }
            TagStoreData.Collection.fromTags(userTags, key.userId)
        }
    }
}
