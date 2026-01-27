package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.datastore.model.domain.Card

interface CardRepository {
    suspend fun getCards(
        userId: String,
        page: Int = 1,
        limit: Int = 20,
        type: String? = null,
        isFavorite: Boolean? = null
    ): List<Card>

    suspend fun countCards(
        userId: String,
        type: String? = null,
        isFavorite: Boolean? = null
    ): Long

    suspend fun getCard(cardId: String): Card?

    suspend fun upsertCard(card: Card): Card

    suspend fun deleteCard(cardId: String)
}
