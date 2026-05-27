package tech.zhifu.app.myhub.datastore.repository.card

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.Card

interface CardRepository {
    fun observeContentRevision(): Flow<Long>

    suspend fun insertCard(
        card: Card,
        userId: String,
    )

    suspend fun getCard(
        userId: String,
        cardId: String
    ): Card?

    fun flowCard(
        userId: String,
        cardId: String
    ): Flow<Card?>

    fun flowCards(
        userId: String,
        cursorCardId: String?,
        cursorTitle: String? = null,
        cursorUpdatedAt: Long? = null,
        orderByUpdated: Boolean = true,
        orderByTitle: Boolean = false,
        query: String? = null,
        limit: Int
    ): Flow<List<Card>>

    suspend fun deleteCard(
        userId: String,
        cardId: String,
        needSync: Boolean = true,
    )
}
