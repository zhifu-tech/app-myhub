package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.model.domain.Card

interface RemoteCardDataSource {
    suspend fun getCards(userId: String): List<Card>
    suspend fun getCardById(id: String): Card?
    
    suspend fun upsertCard(card: Card): Card
    
    suspend fun deleteCard(cardId: String)
}
