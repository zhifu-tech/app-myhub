package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.store5.Fetcher
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource

internal fun createCardStoreFetcher(
    remoteCardDataSource: RemoteCardDataSource
): Fetcher<CardStoreKey, CardStoreData> = Fetcher.of { key ->
    when (key) {
        is CardStoreKey.ById -> {
            val card = remoteCardDataSource.getCardById(key.id)
                ?: throw NoSuchElementException("Card not found: ${key.id}")
            CardStoreData.Single(card)
        }

        is CardStoreKey.ByUser -> {
            val cards = remoteCardDataSource.getCards(
                userId = key.userId,
                page = 1,
                limit = 100 // 获取前100条，如果需要更多可以分页获取
            )
            CardStoreData.Collection.fromCards(cards, key.userId)
        }
    }
}
