package tech.zhifu.app.myhub.datastore.repository.card

import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.card.LocalCardDataSource
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

internal fun createCardStoreSourceOfTruth(
    localCardDataSource: LocalCardDataSource,
): CardStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        when (key) {
            is CardStoreKey.ById -> {
                localCardDataSource
                    .flowCard(
                        cardId = key.id,
                    )
                    .map { card ->
                        if (card == null) null
                        else CardStoreData.Single(
                            card = card,
                            userId = key.userId
                        )
                    }
            }

            is CardStoreKey.ByUserCursor -> {
                localCardDataSource
                    .flowCards(
                        userId = key.userId,
                        cursorCardId = key.cursor,
                        cursorTitle = key.cursorTitle,
                        cursorUpdatedAt = key.cursorUpdatedAt,
                        orderByTitle = key.orderByTitle,
                        orderByUpdated = key.orderByUpdated,
                        limit = key.size,
                    )
                    .map { items ->
                        logger.debug { "flowCards: $items" }
                        if (items.isEmpty()) null
                        else CardStoreData.Collection.fromCards(items, key.userId)
                    }
            }
        }
    },
    writer = { key, data ->
        when (key) {
            is CardStoreKey.ById if data is CardStoreData.Single -> {
                localCardDataSource
                    .insertCard(
                        userId = data.userId,
                        card = data.card
                    )
            }

            else -> {}
        }
    },
    delete = { key ->
        when (key) {
            is CardStoreKey.ById -> {
                localCardDataSource
                    .deleteCard(
                        cardId = key.id
                    )
            }

            else -> {}
        }
    },
    deleteAll = {}
)
