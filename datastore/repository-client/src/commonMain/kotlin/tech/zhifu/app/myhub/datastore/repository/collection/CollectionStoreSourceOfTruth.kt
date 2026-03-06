package tech.zhifu.app.myhub.datastore.repository.collection

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.core5.StoreKey
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.CollectionSort
import tech.zhifu.app.myhub.datastore.datasource.LocalCollectionDataSource
import tech.zhifu.app.myhub.logger.Logger

@OptIn(ExperimentalStoreApi::class)
fun createCollectionStoreSourceOfTruth(
    localCollectionDataSource: LocalCollectionDataSource,
    logger: Logger,
): CollectionStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        when (key) {
            is CollectionStoreKey.ById -> flow {
                localCollectionDataSource.observeCollection(key.id).collect { collection ->
                    emit(CollectionStoreData.Single(collection))
                }
            }

            is CollectionStoreKey.ByUser -> localCollectionDataSource.observeCollectionsPage(
                userId = key.userId,
                page = key.page,
                size = key.size,
                sort = key.sort.toCollectionSort(),
            ).map { pageItems ->
                if (pageItems.isEmpty()) null
                else CollectionStoreData.Items.fromCollections(pageItems, key.userId)
            }
        }
    },
    writer = { key, data ->
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

@OptIn(ExperimentalStoreApi::class)
private fun StoreKey.Sort?.toCollectionSort(): CollectionSort? = when (this) {
    StoreKey.Sort.NEWEST -> CollectionSort.NEWEST
    StoreKey.Sort.OLDEST -> CollectionSort.OLDEST
    else -> null
}
