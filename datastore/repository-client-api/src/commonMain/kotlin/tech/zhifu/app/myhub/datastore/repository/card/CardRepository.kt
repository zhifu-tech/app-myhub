package tech.zhifu.app.myhub.datastore.repository.card

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.core5.StoreKey
import org.mobilenativefoundation.store.store5.StoreReadResponse
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.ReviewProgress

interface CardRepository {

    suspend fun insertCard(
        card: Card,
        needSync: Boolean = true
    )

    suspend fun getCard(
        cardId: String
    ): CardStoreData?

    suspend fun getCards(
        userId: String,
        page: Int,
        size: Int,
        sort: StoreKey.Sort? = null
    ): CardStoreData

    suspend fun getCards(
        cardIds: List<String>
    ): CardStoreData?

    fun streamCards(
        userId: String,
        page: Int = 1,
        size: Int = 20,
        sort: StoreKey.Sort? = null,
        refresh: Boolean = false
    ): Flow<StoreReadResponse<CardStoreData>>

    fun streamCard(
        cardId: String,
        refresh: Boolean = false
    ): Flow<StoreReadResponse<CardStoreData>>

    suspend fun fetchCards(
        userId: String,
        page: Int = 1,
        size: Int = 20,
        sort: StoreKey.Sort? = null,
        filters: List<StoreKey.Filter<*>>? = null
    ): Flow<StoreReadResponse<CardStoreData>>

    suspend fun fetchCard(
        cardId: String
    ): Flow<StoreReadResponse<CardStoreData>>

    suspend fun clearCard(
        cardId: String
    )

    suspend fun clearCards(
        userId: String
    )

    suspend fun getReviewProgress(
        userId: String
    ): ReviewProgress
}
