package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import tech.zhifu.app.myhub.datastore.datasource.card.RemoteCardDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error

internal fun createCardStoreUpdater(
    remoteCardDataSource: RemoteCardDataSource,
    logger: Logger,
): CardStoreUpdater = Updater.by(
    post = { key, data ->
        logger.debug("updater") {
            "post is called with key: $key (type=${key::class.qualifiedName}, " +
                "data=$data, instance=${key.hashCode()}"
        }
        when (key) {
            is CardStoreKey.ById if data is CardStoreData.Single -> {
                val updatedCard = remoteCardDataSource.updateCard(data.card)
                UpdaterResult.Success.Typed(
                    value = StoreWriteResponse.Success.Typed(
                        value = CardStoreData.Single(updatedCard)
                    )
                )
            }

            is CardStoreKey.ByUser if data is CardStoreData.Collection -> {
                val updatedCards = data.cards.map { card ->
                    remoteCardDataSource.updateCard(card)
                }
                UpdaterResult.Success.Typed(
                    value = StoreWriteResponse.Success.Typed(
                        value = CardStoreData.Collection.fromCards(updatedCards, data.userId)
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
