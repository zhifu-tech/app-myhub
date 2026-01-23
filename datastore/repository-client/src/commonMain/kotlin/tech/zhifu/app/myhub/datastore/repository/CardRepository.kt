package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.Card

interface CardRepository {
    val syncChangeApplier: SyncChangeApplier

    suspend fun getCards(userId: String): List<Card>

    suspend fun getCard(cardId: String): Card?

    suspend fun deleteCard(cardId: String)

    suspend fun insertCard(card: Card, needSync: Boolean = true)

    suspend fun insertCardWithTags(card: Card, needSync: Boolean = true)

    fun observeCard(cardId: String): Flow<Card>

    fun observeCards(userId: String): Flow<List<Card>>

    suspend fun fetchCards(userId: String): List<Card>

    suspend fun fetchCard(cardId: String): Card?
}
