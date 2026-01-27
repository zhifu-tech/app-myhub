package tech.zhifu.app.myhub.datastore.repository.tag

import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import tech.zhifu.app.myhub.datastore.datasource.RemoteTagDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.network.NetworkException

internal fun createTagStoreUpdater(
    remoteTagDataSource: RemoteTagDataSource,
    logger: Logger = logger("TagStoreUpdater")
): TagStoreUpdater = Updater.by(
    post = { key, data ->
        try {
            when {
                key is TagStoreKey.ById && data is TagStoreData.Single -> {
                    val updatedTag = remoteTagDataSource.updateTag(data.tag)
                    UpdaterResult.Success.Typed(
                        StoreWriteResponse.Success.Typed(
                            TagStoreData.Single(updatedTag)
                        )
                    )
                }

                key is TagStoreKey.ByUser && data is TagStoreData.Collection -> {
                    val updatedTags = data.tags.map { tag ->
                        remoteTagDataSource.updateTag(tag)
                    }
                    UpdaterResult.Success.Typed(
                        StoreWriteResponse.Success.Typed(
                            TagStoreData.Collection.fromTags(updatedTags, data.userId)
                        )
                    )
                }

                else -> {
                    logger.error { "Unsupported key/data combination: key=${key::class}, data=${data::class}" }
                    UpdaterResult.Error.Message("Unsupported operation: key and data type mismatch")
                }
            }
        } catch (e: NetworkException) {
            logger.error(e) { "Network error during tag update: key=$key" }
            UpdaterResult.Error.Exception(e)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during tag update: key=$key" }
            UpdaterResult.Error.Exception(e)
        }
    }
)
