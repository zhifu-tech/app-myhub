package tech.zhifu.app.myhub.datastore.datasource.card

import tech.zhifu.app.myhub.datastore.model.domain.Card

interface RemoteCardDataSource {
    suspend fun getCards(
        userId: String,
        page: Int = 1,
        limit: Int = 20,
        type: String? = null,
        isFavorite: Boolean? = null
    ): List<Card>

    suspend fun getCardById(id: String): Card?

    suspend fun createCard(card: Card): Card

    suspend fun updateCard(card: Card): Card

    suspend fun partialUpdateCard(card: Card): Card

    suspend fun deleteCard(cardId: String)
}
