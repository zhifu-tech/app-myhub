package tech.zhifu.app.myhub.datastore.datasource.card

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.Card

interface LocalCardDataSource {

    suspend fun insertCard(
        userId: String,
        card: Card
    )

    fun flowCard(
        cardId: String,
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

    suspend fun deleteCard(cardId: String)
}
