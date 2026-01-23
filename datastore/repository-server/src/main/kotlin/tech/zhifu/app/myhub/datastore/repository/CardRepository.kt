package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.datastore.model.domain.Card

interface CardRepository {
    suspend fun getCards(userId: String): List<Card>

    suspend fun getCard(cardId: String): Card?

    suspend fun deleteCard(cardId: String)
}
