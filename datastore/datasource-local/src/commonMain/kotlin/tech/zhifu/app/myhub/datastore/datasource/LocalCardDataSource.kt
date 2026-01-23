package tech.zhifu.app.myhub.datastore.datasource

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.Card


interface LocalCardDataSource {

    suspend fun insertCard(card: Card)

    suspend fun getCard(cardId: String): Card?

    fun observeCard(cardId: String): Flow<Card>

    suspend fun getCards(userId: String): List<Card>

    fun observeCards(userId: String): Flow<List<Card>>

    suspend fun deleteCard(cardId: String)

    suspend fun deleteCards(userId: String)
}
