package tech.zhifu.app.myhub.datastore.repository.card

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadResponse
import tech.zhifu.app.myhub.datastore.model.domain.Card

interface CardRepository {

    suspend fun insertCard(card: Card, needSync: Boolean = true)

    suspend fun getCards(userId: String, page: Int, pageSize: Int): CardStoreData?

    suspend fun getCards(cardIds: List<String>): CardStoreData?

    suspend fun getCard(cardId: String): CardStoreData?

    fun streamCards(userId: String, refresh: Boolean = false): Flow<StoreReadResponse<CardStoreData>>

    fun streamCard(cardId: String, refresh: Boolean = false): Flow<StoreReadResponse<CardStoreData>>

    suspend fun fetchCards(userId: String): Flow<StoreReadResponse<CardStoreData>>

    suspend fun fetchCard(cardId: String): Flow<StoreReadResponse<CardStoreData>>

    suspend fun clearCard(cardId: String)
    suspend fun clearCards(userId: String)

    suspend fun getReviewProgress(userId: String): tech.zhifu.app.myhub.datastore.model.domain.ReviewProgress
}
