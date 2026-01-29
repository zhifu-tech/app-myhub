package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.FetcherResult
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource

internal fun createCardStoreFetcher(
    remoteCardDataSource: RemoteCardDataSource
): CardStoreFetcher = Fetcher.ofResult { key ->
    when (key) {
        is CardStoreKey.ById -> {
            val card = remoteCardDataSource.getCardById(key.id)
            if (card == null) FetcherResult.Error.Message("Card not found: ${key.id}")
            else FetcherResult.Data(CardStoreData.Single(card))
        }

        is CardStoreKey.ByIds -> {
            val cards = key.ids.mapNotNull { id ->
                remoteCardDataSource.getCardById(id)
            }
            FetcherResult.Data(
                CardStoreData.CollectionIds(
                    items = cards.map { CardStoreData.Single(it) },
                    ids = key.ids
                )
            )
        }

        is CardStoreKey.ByUser -> {
            val cards = remoteCardDataSource.getCards(
                userId = key.userId,
                page = key.page,
                limit = key.pageSize
            )
            FetcherResult.Data(CardStoreData.Collection.fromCards(cards, key.userId))
        }
    }
}
