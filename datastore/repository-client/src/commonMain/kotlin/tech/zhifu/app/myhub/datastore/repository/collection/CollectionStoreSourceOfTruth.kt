package tech.zhifu.app.myhub.datastore.repository.collection

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.LocalCollectionDataSource

@OptIn(ExperimentalStoreApi::class)
fun createCollectionStoreSourceOfTruth(
    localCollectionDataSource: LocalCollectionDataSource
): CollectionStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        when (key) {
            is CollectionStoreKey.ById -> flow {
                try {
                    localCollectionDataSource.observeCollection(key.id).collect { collection ->
                        emit(CollectionStoreData.Single(collection))
                    }
                } catch (_: Exception) {
                    emit(null)
                }
            }

            is CollectionStoreKey.ByUser -> {
                localCollectionDataSource.observeCollections(key.userId).map { collections ->
                    if (collections.isEmpty()) null
                    else CollectionStoreData.Items.fromCollections(collections, key.userId)
                }
            }
        }
    },
    writer = { key, data ->
        when {
            key is CollectionStoreKey.ById && data is CollectionStoreData.Single -> {
                localCollectionDataSource.insertCollection(data.collection)
            }

            key is CollectionStoreKey.ByUser && data is CollectionStoreData.Items -> {
                data.collections.forEach { collection ->
                    localCollectionDataSource.insertCollection(collection)
                }
            }

            else -> {
                // Store5 框架应该保证类型匹配，这里主要是防御性编程
            }
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
