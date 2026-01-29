package tech.zhifu.app.myhub.datastore.datasource

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.ReviewProgress

interface LocalCardDataSource {

    suspend fun insertCard(card: Card)

    suspend fun getCard(cardId: String): Card?

    fun observeCard(cardId: String): Flow<Card>

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

    fun observeCards(userId: String): Flow<List<Card>>

    suspend fun deleteCard(cardId: String)

    suspend fun deleteCards(userId: String)

    suspend fun getUnreviewedCards(userId: String): List<Card>

    suspend fun getReviewProgress(userId: String): ReviewProgress
}
