package tech.zhifu.app.myhub.datastore.repository.collection

import kotlinx.coroutines.flow.flow
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.LocalCollectionDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug

@OptIn(ExperimentalStoreApi::class)
fun createCollectionStoreSourceOfTruth(
    localCollectionDataSource: LocalCollectionDataSource,
    logger: Logger,
): CollectionStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        logger.debug {
            "reader called with key: $key (type=${key::class.qualifiedName}, " +
                "instance=${System.identityHashCode(key)}"
        }
        when (key) {
            is CollectionStoreKey.ById -> flow {
                localCollectionDataSource.observeCollection(key.id).collect { collection ->
                    emit(CollectionStoreData.Single(collection))
                }
            }

            is CollectionStoreKey.ByUser -> flow {
                val pagedCollections = localCollectionDataSource.getCollections(
                    userId = key.userId,
                    page = key.page,
                    pageSize = key.pageSize
                )
                emit(
                    if (pagedCollections.isEmpty()) null
                    else CollectionStoreData.Items.fromCollections(pagedCollections, key.userId)
                )
            }
        }
    },
    writer = { key, data ->
        logger.debug {
            "writer called with key: $key (type=${key::class.qualifiedName}, " +
                "instance=${System.identityHashCode(key)}"
        }
        when (key) {
            is CollectionStoreKey.ById if data is CollectionStoreData.Single -> {
                localCollectionDataSource.insertCollection(data.collection)
            }

            is CollectionStoreKey.ByUser if data is CollectionStoreData.Items -> {
                data.collections.forEach { collection ->
                    localCollectionDataSource.insertCollection(collection)
                }
            }

            else -> {}
        }
    },
    delete = { key ->
        when (key) {
            is CollectionStoreKey.ById -> localCollectionDataSource.deleteCollection(key.id)
            is CollectionStoreKey.ByUser -> localCollectionDataSource.deleteCollections(key.userId)
        }
    },
    deleteAll = { }
)
