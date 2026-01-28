package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.datastore.model.domain.Card

interface CardRepository {
    /**
     * 获取用户的卡片列表
     * @param userId 用户 ID
     * @param page 页码（从 1 开始），如果为 null 则返回所有卡片
     * @param limit 每页数量，仅在 page 不为 null 时有效
     * @param type 卡片类型筛选（可选）
     * @param isFavorite 是否收藏筛选（可选）
     * @return 卡片列表
     */
    suspend fun getCards(
        userId: String,
        page: Int? = null,
        limit: Int? = null,
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
