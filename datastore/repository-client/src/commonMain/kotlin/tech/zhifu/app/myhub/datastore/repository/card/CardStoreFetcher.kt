package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.store5.Fetcher
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource

internal fun createCardStoreFetcher(
    remoteCardDataSource: RemoteCardDataSource
): CardStoreFetcher = Fetcher.of { key ->
    when (key) {
        is CardStoreKey.ById -> {
            val card = remoteCardDataSource.getCardById(key.id)
                ?: throw NoSuchElementException("Card not found: ${key.id}")
            CardStoreData.Single(card)
        }

        is CardStoreKey.ByIds -> {
            val cards = key.ids.mapNotNull { id ->
                remoteCardDataSource.getCardById(id)
            }
            CardStoreData.CollectionIds(
                items = cards.map { CardStoreData.Single(it) },
                ids = key.ids
            )
        }

        is CardStoreKey.ByUser -> {
            val cards = remoteCardDataSource.getCards(
                userId = key.userId,
                page = key.page,
                limit = key.pageSize
            )
            CardStoreData.Collection.fromCards(cards, key.userId)
        }
    }
}
