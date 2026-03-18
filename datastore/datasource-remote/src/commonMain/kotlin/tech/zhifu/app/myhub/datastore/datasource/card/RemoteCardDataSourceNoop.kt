package tech.zhifu.app.myhub.datastore.datasource.card

import tech.zhifu.app.myhub.datastore.model.domain.Card

class RemoteCardDataSourceNoop : RemoteCardDataSource {
    override suspend fun getCards(
        userId: String,
        page: Int,
        limit: Int,
        type: String?,
        isFavorite: Boolean?
    ): List<Card> = emptyList()

    override suspend fun getCardById(id: String): Card? = null

    override suspend fun createCard(card: Card): Card = card

    override suspend fun updateCard(card: Card): Card = card

    override suspend fun partialUpdateCard(card: Card): Card = card

    override suspend fun deleteCard(cardId: String) = Unit
}
