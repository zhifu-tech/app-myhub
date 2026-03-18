package tech.zhifu.app.myhub.datastore.repository.tag

import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import tech.zhifu.app.myhub.datastore.datasource.tag.RemoteTagDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error

internal fun createTagStoreUpdater(
    remoteTagDataSource: RemoteTagDataSource,
    logger: Logger,
): TagStoreUpdater = Updater.by(
    post = { key, data ->
        logger.debug("updater") {
            "post is called with key: $key (type=${key::class.qualifiedName}, " +
                "data=$data, instance=${key.hashCode()}"
        }
        when (key) {
            is TagStoreKey.ById if data is TagStoreData.Single -> {
                val updatedTag = remoteTagDataSource.updateTag(
                    id = key.id,
                    tag = data.tag,
                    userId = data.tag.userId,
                )
                UpdaterResult.Success.Typed(
                    value = StoreWriteResponse.Success.Typed(
                        value = TagStoreData.Single(
                            tag = updatedTag
                        )
                    )
                )
            }

            is TagStoreKey.ByUser if data is TagStoreData.Collection -> {
                val updatedTags = data.tags.map { tag ->
                    remoteTagDataSource.updateTag(
                        id = tag.id,
                        tag = tag,
                        userId = tag.userId,
                    )
                }
                UpdaterResult.Success.Typed(
                    value = StoreWriteResponse.Success.Typed(
                        value = TagStoreData.Collection.fromTags(
                            tags = updatedTags,
                            userId = data.userId,
                        )
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
