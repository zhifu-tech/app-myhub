package tech.zhifu.app.myhub.datastore.repository.tag

import kotlinx.coroutines.flow.flow
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug

@OptIn(ExperimentalStoreApi::class)
fun createTagStoreSourceOfTruth(
    localTagDataSource: LocalTagDataSource,
    logger: Logger,
): TagStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        logger.debug {
            "reader called with key: $key (type=${key::class.qualifiedName}, " +
                "instance=${System.identityHashCode(key)}"
        }
        when (key) {
            is TagStoreKey.ById -> flow {
                localTagDataSource.observeTag(key.id).collect { tag ->
                    emit(TagStoreData.Single(tag))
                }
            }

            is TagStoreKey.ByUser -> flow {
                localTagDataSource.observeTags(key.userId).collect { tags ->
                    emit(
                        if (tags.isEmpty()) null as? TagStoreData.Collection?
                        else TagStoreData.Collection.fromTags(tags, key.userId)
                    )
                }
            }
        }
    },
    writer = { key, data ->
        logger.debug {
            "writer called with key: $key (type=${key::class.qualifiedName}, " +
                "instance=${System.identityHashCode(key)}"
        }
        when (key) {
            is TagStoreKey.ById if data is TagStoreData.Single -> {
                localTagDataSource.insertTag(data.tag)
            }

            is TagStoreKey.ByUser if data is TagStoreData.Collection -> {
                data.tags.forEach { tag ->
                    localTagDataSource.insertTag(tag)
                }
            }

            else -> {}
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
