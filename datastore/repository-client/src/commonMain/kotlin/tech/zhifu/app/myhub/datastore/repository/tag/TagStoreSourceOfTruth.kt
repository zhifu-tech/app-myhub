package tech.zhifu.app.myhub.datastore.repository.tag

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource

@OptIn(ExperimentalStoreApi::class)
fun createTagStoreSourceOfTruth(
    localTagDataSource: LocalTagDataSource
): TagStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        when (key) {
            is TagStoreKey.ById -> flow {
                try {
                    localTagDataSource.observeTag(key.id).collect { tag ->
                        emit(TagStoreData.Single(tag))
                    }
                } catch (_: Exception) {
                    emit(null)
                }
            }

            is TagStoreKey.ByUser -> {
                localTagDataSource.observeTags(key.userId).map { tags ->
                    if (tags.isEmpty()) null
                    else TagStoreData.Collection.fromTags(tags, key.userId)
                }
            }
        }
    },
    writer = { key, data ->
        when {
            key is TagStoreKey.ById && data is TagStoreData.Single -> {
                localTagDataSource.insertTag(data.tag)
            }

            key is TagStoreKey.ByUser && data is TagStoreData.Collection -> {
                data.tags.forEach { tag ->
                    localTagDataSource.insertTag(tag)
                }
            }

            else -> {
                // Store5 框架应该保证类型匹配，这里主要是防御性编程
            }
        }
    },
    delete = { key ->
        when (key) {
            is TagStoreKey.ById -> localTagDataSource.deleteTag(key.id)
            is TagStoreKey.ByUser -> {
                // 获取该用户的所有 tags 然后逐一删除
                val tags = localTagDataSource.getTags(key.userId)
                tags.forEach { tag ->
                    localTagDataSource.deleteTag(tag.id)
                }
            }
        }
    },
    deleteAll = { }
)
