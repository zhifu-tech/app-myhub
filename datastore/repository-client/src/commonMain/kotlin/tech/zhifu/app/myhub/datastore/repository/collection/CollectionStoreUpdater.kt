package tech.zhifu.app.myhub.datastore.repository.collection

import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import tech.zhifu.app.myhub.datastore.datasource.RemoteCollectionDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.network.NetworkException

internal fun createCollectionStoreUpdater(
    remoteCollectionDataSource: RemoteCollectionDataSource,
    logger: Logger = logger("CollectionStoreUpdater")
): CollectionStoreUpdater = Updater.by(
    post = { key, data ->
        try {
            when {
                key is CollectionStoreKey.ById && data is CollectionStoreData.Single -> {
                    val updatedCollection = remoteCollectionDataSource.updateCollection(
                        id = key.id,
                        collection = data.collection,
                        userId = data.collection.userId
                    )
                    UpdaterResult.Success.Typed(
                        StoreWriteResponse.Success.Typed(
                            CollectionStoreData.Single(updatedCollection)
                        )
                    )
                }

                key is CollectionStoreKey.ByUser && data is CollectionStoreData.Items -> {
                    // ⚠️ 批量更新：当前实现逐个更新，未来可以优化为批量 API
                    val updatedCollections = data.collections.map { collection ->
                        remoteCollectionDataSource.updateCollection(
                            id = collection.id,
                            collection = collection,
                            userId = collection.userId
                        )
                    }
                    UpdaterResult.Success.Typed(
                        StoreWriteResponse.Success.Typed(
                            CollectionStoreData.Items.fromCollections(updatedCollections, data.userId)
                        )
                    )
                }

                else -> {
                    logger.error { "Unsupported key/data combination: key=${key::class}, data=${data::class}" }
                    UpdaterResult.Error.Message("Unsupported operation: key and data type mismatch")
                }
            }
        } catch (e: NetworkException) {
            logger.error(e) { "Network error during collection update: key=$key" }
            UpdaterResult.Error.Exception(e)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during collection update: key=$key" }
            UpdaterResult.Error.Exception(e)
        }
    }
)
