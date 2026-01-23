package tech.zhifu.app.myhub.datastore.repository.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.repository.CardRepository

/**
 * 卡片仓库实现（服务端）
 * 复用 LocalCardDataSource 的实现，避免代码重复
 */
class CardRepositoryImpl(
    private val localDataSource: LocalCardDataSource,
) : CardRepository {

    override suspend fun getCards(userId: String): List<Card> {
        return localDataSource.getCards(userId)
    }

    override suspend fun getCard(cardId: String): Card? {
        return localDataSource.getCard(cardId)
    }

    override suspend fun deleteCard(cardId: String) {
        localDataSource.deleteCard(cardId)
    }
}
