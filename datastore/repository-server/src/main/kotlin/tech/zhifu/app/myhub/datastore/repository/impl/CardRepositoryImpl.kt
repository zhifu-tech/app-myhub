package tech.zhifu.app.myhub.datastore.repository.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.isFavorite
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import tech.zhifu.app.myhub.datastore.repository.TagRepository

/**
 * 卡片仓库实现（服务端）
 * 复用 LocalCardDataSource 的实现，避免代码重复
 */
class CardRepositoryImpl(
    private val localDataSource: LocalCardDataSource,
    private val tagRepository: TagRepository
) : CardRepository {

    override suspend fun getCards(
        userId: String,
        page: Int,
        limit: Int,
        type: String?,
        isFavorite: Boolean?
    ): List<Card> {
        // 使用数据库层面的分页和筛选
        return localDataSource.getCards(
            userId = userId,
            page = page,
            limit = limit,
            type = type,
            isFavorite = isFavorite
        )
    }

    override suspend fun countCards(
        userId: String,
        type: String?,
        isFavorite: Boolean?
    ): Long {
        // 使用数据库层面的计数查询
        return localDataSource.countCards(
            userId = userId,
            type = type,
            isFavorite = isFavorite
        )
    }

    override suspend fun getCard(cardId: String): Card? {
        return localDataSource.getCard(cardId)
    }

    override suspend fun upsertCard(card: Card): Card {
        // 1. 业务逻辑：确保 Tags 存在
        val resolvedTags = tagRepository.ensureTags(card.userId, card.tags)
        val cardWithResolvedTags = card.copy(tags = resolvedTags)
        
        // 2. 检查是否存在
        val existing = localDataSource.getCard(cardWithResolvedTags.id)
        if (existing != null) {
            // 如果存在，先删除再插入（简单的更新策略）
            // TODO: 未来可以扩展 LocalCardDataSource 添加 updateCard 方法
            localDataSource.deleteCard(cardWithResolvedTags.id)
        }
        
        // 3. 插入卡片（LocalCardDataSource.insertCard 会自动处理 card_tag 关联）
        localDataSource.insertCard(cardWithResolvedTags)
        
        // 4. 返回插入后的卡片（从数据库重新获取以确保数据一致性，包括标签关联）
        return localDataSource.getCard(cardWithResolvedTags.id) ?: cardWithResolvedTags
    }

    override suspend fun deleteCard(cardId: String) {
        localDataSource.deleteCard(cardId)
    }
}
