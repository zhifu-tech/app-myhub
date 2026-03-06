package tech.zhifu.app.myhub.datastore.repository.card

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.core5.StoreKey
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.CardSort
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug

@OptIn(ExperimentalStoreApi::class)
internal fun createCardStoreSourceOfTruth(
    localCardDataSource: LocalCardDataSource,
    logger: Logger,
): CardStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        when (key) {
            is CardStoreKey.ById -> flow {
                localCardDataSource.observeCard(key.id).collect { card ->
                    emit(CardStoreData.Single(card))
                }
            }

            is CardStoreKey.ByIds -> flow {
                val cards = key.ids.mapNotNull { id ->
                    localCardDataSource.getCard(id)
                }
                emit(
                    if (cards.isEmpty()) null
                    else CardStoreData.CollectionIds(
                        items = cards.map { CardStoreData.Single(it) },
                        ids = key.ids
                    )
                )
            }

            is CardStoreKey.ByUser -> localCardDataSource.observeCardsPage(
                userId = key.userId,
                page = key.page,
                size = key.size,
                sort = key.sort.toCardSort(),
            ).map { pageItems ->
                if (pageItems.isEmpty()) null
                else CardStoreData.Collection.fromCards(pageItems, key.userId)
            }
        }
    },
    writer = { key, data ->
        logger.debug {
            "writer called with key: $key (type=${key::class.qualifiedName}, " +
                "instance=${key.hashCode()}"
        }
        when (key) {
            is CardStoreKey.ById if data is CardStoreData.Single -> {
                localCardDataSource.insertCard(data.card)
            }

            is CardStoreKey.ByIds if data is CardStoreData.CollectionIds -> {
                data.cards.forEach { card ->
                    localCardDataSource.insertCard(card)
                }
            }

            is CardStoreKey.ByUser if data is CardStoreData.Collection -> {
                data.cards.forEach { card ->
                    localCardDataSource.insertCard(card)
                }
            }

            else -> {}
        }
    },
    delete = { key ->
        when (key) {
            is CardStoreKey.ById -> localCardDataSource.deleteCard(key.id)
            is CardStoreKey.ByIds -> key.ids.forEach { localCardDataSource.deleteCard(it) }
            is CardStoreKey.ByUser -> localCardDataSource.deleteCards(key.userId)
        }
    },
    deleteAll = { }
)

@OptIn(ExperimentalStoreApi::class)
private fun StoreKey.Sort?.toCardSort(): CardSort? = when (this) {
    StoreKey.Sort.NEWEST -> CardSort.NEWEST
    StoreKey.Sort.OLDEST -> CardSort.OLDEST
    else -> null
}
