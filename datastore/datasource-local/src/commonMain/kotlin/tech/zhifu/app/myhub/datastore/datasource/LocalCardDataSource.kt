package tech.zhifu.app.myhub.datastore.datasource

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.Card


interface LocalCardDataSource {

    suspend fun insertCard(card: Card)

    suspend fun getCard(cardId: String): Card?

    fun observeCard(cardId: String): Flow<Card>

    suspend fun getCards(userId: String): List<Card>

    /**
     * 获取卡片列表（支持筛选和分页）
     * @param userId 用户 ID
     * @param page 页码（从 1 开始）
     * @param limit 每页数量
     * @param type 卡片类型筛选（可选）
     * @param isFavorite 是否收藏筛选（可选）
     * @return 卡片列表
     */
    suspend fun getCards(
        userId: String,
        page: Int,
        limit: Int,
        type: String? = null,
        isFavorite: Boolean? = null
    ): List<Card>

    /**
     * 统计卡片数量（支持筛选）
     * @param userId 用户 ID
     * @param type 卡片类型筛选（可选）
     * @param isFavorite 是否收藏筛选（可选）
     * @return 卡片数量
     */
    suspend fun countCards(
        userId: String,
        type: String? = null,
        isFavorite: Boolean? = null
    ): Long

    fun observeCards(userId: String): Flow<List<Card>>

    suspend fun deleteCard(cardId: String)

    suspend fun deleteCards(userId: String)
}
