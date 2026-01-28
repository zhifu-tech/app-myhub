package tech.zhifu.app.myhub.datastore.repository.card

import kotlinx.coroutines.flow.flow
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

            is CardStoreKey.ByUser -> flow {
                localCardDataSource.observeCards(key.userId).collect { cards ->
                    emit(
                        if (cards.isEmpty()) null
                        else CardStoreData.Collection.fromCards(cards, key.userId)
                    )
                }
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
            is CardStoreKey.ByUser -> localCardDataSource.deleteCards(key.userId)
        }
    },
    deleteAll = { }
)
