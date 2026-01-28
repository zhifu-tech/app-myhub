package tech.zhifu.app.myhub.datastore.repository.collection

import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import tech.zhifu.app.myhub.datastore.datasource.RemoteCollectionDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error

internal fun createCollectionStoreUpdater(
    remoteCollectionDataSource: RemoteCollectionDataSource,
    logger: Logger,
): CollectionStoreUpdater = Updater.by(
    post = { key, data ->
        logger.debug("updater") {
            "post is called with key: $key (type=${key::class.qualifiedName}, " +
                "data=$data, instance=${System.identityHashCode(key)}"
        }
        when (key) {
            is CollectionStoreKey.ById if data is CollectionStoreData.Single -> {
                val updatedCollection = remoteCollectionDataSource.updateCollection(
                    id = key.id,
                    collection = data.collection,
                    userId = data.collection.userId
                )
                UpdaterResult.Success.Typed(
                    value = StoreWriteResponse.Success.Typed(
                        value = CollectionStoreData.Single(updatedCollection)
                    )
                )
            }

            is CollectionStoreKey.ByUser if data is CollectionStoreData.Items -> {
                val updatedCollections = data.collections.map { collection ->
                    remoteCollectionDataSource.updateCollection(
                        id = collection.id,
                        collection = collection,
                        userId = collection.userId
                    )
                }
                UpdaterResult.Success.Typed(
                    value = StoreWriteResponse.Success.Typed(
                        value = CollectionStoreData.Items.fromCollections(updatedCollections, data.userId)
                    )
                )
            }

            else -> {
                logger.error { "Unsupported key/data combination: key=${key::class}, data=${data::class}" }
                UpdaterResult.Error.Message("Unsupported operation: key and data type mismatch")
            }
        }
    }
)
