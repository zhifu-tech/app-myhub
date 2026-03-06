package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.core5.StoreKey
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.FetcherResult
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Card

@OptIn(ExperimentalStoreApi::class)
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
                limit = key.size
            )
            val sortedCards = applyCardSort(cards, key.sort)
            val filteredCards = applyCardFilters(sortedCards, key.filters)
            FetcherResult.Data(CardStoreData.Collection.fromCards(filteredCards, key.userId))
        }
    }
}

@OptIn(ExperimentalStoreApi::class)
@Suppress("UNCHECKED_CAST")
private fun applyCardFilters(
    items: List<Card>,
    filters: List<StoreKey.Filter<*>>?
): List<Card> {
    if (filters.isNullOrEmpty()) return items
    var result = items
    filters.forEach { filter ->
        val typed = filter as? StoreKey.Filter<Card> ?: return@forEach
        result = typed(result)
    }
    return result
}

@OptIn(ExperimentalStoreApi::class)
private fun applyCardSort(
    items: List<Card>,
    sort: StoreKey.Sort?
): List<Card> = when (sort ?: StoreKey.Sort.NEWEST) {
    StoreKey.Sort.NEWEST -> items.sortedByDescending { it.updatedAt }
    StoreKey.Sort.OLDEST -> items.sortedBy { it.updatedAt }
    // Card does not support alphabetical sort; fallback to NEWEST.
    StoreKey.Sort.ALPHABETICAL -> items.sortedByDescending { it.updatedAt }
    StoreKey.Sort.REVERSE_ALPHABETICAL -> items.sortedByDescending { it.updatedAt }
}
