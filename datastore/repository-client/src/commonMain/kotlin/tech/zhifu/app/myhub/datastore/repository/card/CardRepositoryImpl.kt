package tech.zhifu.app.myhub.datastore.repository.card

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import tech.zhifu.app.myhub.datastore.model.domain.Card

class CardRepositoryImpl(
    private val store: CardStore
) : CardRepository {

    override suspend fun insertCard(
        card: Card,
        userId: String,
    ) {
        store
            .write(
                request = StoreWriteRequest.of(
                    key = CardStoreKey.ById(
                        userId = userId,
                        id = card.id
                    ),
                    value = CardStoreData.Single(
                        card = card,
                        userId = userId
                    )
                )
            )
    }

    override fun flowCard(
        userId: String,
        cardId: String
    ): Flow<Card?> =
        store
            .storeStreamCard(
                userId = userId,
                cardId = cardId
            )
            .filterNot { it is StoreReadResponse.Loading || it is StoreReadResponse.NoNewData }
            .map { it.dataOrNull()?.card }

    override suspend fun getCard(
        userId: String,
        cardId: String
    ): Card? =
        store
            .storeStreamCard(
                userId = userId,
                cardId = cardId
            )
            .filterNot { it is StoreReadResponse.Loading || it is StoreReadResponse.NoNewData }
            .first()
            .dataOrNull()
            ?.card

    override fun flowCards(
        userId: String,
        cursorCardId: String?,
        cursorTitle: String?,
        cursorUpdatedAt: Long?,
        orderByUpdated: Boolean,
        orderByTitle: Boolean,
        query: String?,
        limit: Int
    ): Flow<List<Card>> =
        store
            .storeStreamCards(
                userId = userId,
                cursorCardId = cursorCardId,
                cursorTitle = cursorTitle,
                cursorUpdatedAt = cursorUpdatedAt,
                orderByUpdated = orderByUpdated,
                orderByTitle = orderByTitle,
                query = query,
                limit = limit,
            )
            .map { it.dataOrNull()?.cards ?: emptyList() }

    override suspend fun deleteCard(
        userId: String,
        cardId: String,
        needSync: Boolean,
    ) {
        store
            .clear(
                key = CardStoreKey.ById(
                    userId = userId,
                    id = cardId
                )
            )
    }
}
