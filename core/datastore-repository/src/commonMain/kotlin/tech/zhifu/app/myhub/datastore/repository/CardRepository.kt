package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.SearchFilter

/**
 * 卡片仓库接口（基础接口，同步风格）
 * 服务端和客户端都支持
 */
interface CardRepository {
    /**
     * 获取所有卡片
     */
    suspend fun getAllCards(): List<Card>

    /**
     * 根据ID获取卡片
     */
    suspend fun getCardById(id: String): Card?

    /**
     * 搜索和筛选卡片
     */
    suspend fun searchCards(filter: SearchFilter): List<Card>

    /**
     * 创建新卡片
     */
    suspend fun createCard(card: Card): Card

    /**
     * 更新卡片
     */
    suspend fun updateCard(card: Card): Card

    /**
     * 删除卡片
     */
    suspend fun deleteCard(id: String): Boolean

    /**
     * 切换收藏状态
     */
    suspend fun toggleFavorite(cardId: String): Card
}

/**
 * 响应式卡片仓库接口（扩展接口，客户端使用）
 */
interface ReactiveCardRepository : CardRepository {
    /**
     * 观察所有卡片（响应式）
     */
    fun observeAllCards(): Flow<List<Card>>

    /**
     * 观察搜索结果（响应式）
     */
    fun observeSearchCards(filter: SearchFilter): Flow<List<Card>>

    /**
     * 观察收藏的卡片（响应式）
     */
    fun observeFavoriteCards(): Flow<List<Card>>

    /**
     * 根据类型观察卡片（响应式）
     */
    fun observeCardsByType(type: CardType): Flow<List<Card>>

    /**
     * 根据标签观察卡片（响应式）
     */
    fun observeCardsByTag(tag: String): Flow<List<Card>>
}

