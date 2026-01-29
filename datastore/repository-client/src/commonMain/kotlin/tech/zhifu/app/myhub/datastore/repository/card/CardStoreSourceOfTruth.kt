package tech.zhifu.app.myhub.datastore.repository.card

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug

@OptIn(ExperimentalStoreApi::class)
internal fun createCardStoreSourceOfTruth(
    localCardDataSource: LocalCardDataSource,
    logger: Logger,
): CardStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        logger.debug {
            "reader called with key: $key (type=${key::class.qualifiedName}, " +
                "instance=${System.identityHashCode(key)}"
        }
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

            is CardStoreKey.ByUser -> localCardDataSource.observeCards(key.userId)
                .map { fullList ->
                    val pagedCards = fullList.drop((key.page - 1) * key.pageSize).take(key.pageSize)
                    if (pagedCards.isEmpty()) null
                    else CardStoreData.Collection.fromCards(pagedCards, key.userId)
                }
        }
    },
    writer = { key, data ->
        logger.debug {
            "writer called with key: $key (type=${key::class.qualifiedName}, " +
                "instance=${System.identityHashCode(key)}"
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
