package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.network.NetworkException

internal fun createCardStoreUpdater(
    remoteCardDataSource: RemoteCardDataSource,
    logger: Logger = logger("CardStoreUpdater")
): CardStoreUpdater = Updater.by(
    post = { key, data ->
        try {
            when {
                key is CardStoreKey.ById && data is CardStoreData.Single -> {
                    val updatedCard = remoteCardDataSource.upsertCard(data.card)
                    UpdaterResult.Success.Typed(
                        StoreWriteResponse.Success.Typed(
                            CardStoreData.Single(updatedCard)
                        )
                    )
                }

                key is CardStoreKey.ByUser && data is CardStoreData.Collection -> {
                    // ⚠️ 批量更新：当前实现逐个更新，未来可以优化为批量 API
                    // 注意：Store5 的 Updater 通常处理单个操作，批量操作可能需要特殊处理
                    val updatedCards = data.cards.map { card ->
                        remoteCardDataSource.upsertCard(card)
                    }
                    UpdaterResult.Success.Typed(
                        StoreWriteResponse.Success.Typed(
                            CardStoreData.Collection.fromCards(updatedCards, data.userId)
                        )
                    )
                }

                else -> {
                    logger.error { "Unsupported key/data combination: key=${key::class}, data=${data::class}" }
                    UpdaterResult.Error.Message("Unsupported operation: key and data type mismatch")
                }
            }
        } catch (e: NetworkException) {
            logger.error(e) { "Network error during card update: key=$key" }
            UpdaterResult.Error.Exception(e)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during card update: key=$key" }
            UpdaterResult.Error.Exception(e)
        }
    }
)
