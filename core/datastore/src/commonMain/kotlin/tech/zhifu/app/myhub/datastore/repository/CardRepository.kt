package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.SearchFilter

/**
 * 卡片仓库接口
 * 定义卡片相关的数据操作
 */
interface CardRepository {
    /**
     * 获取所有卡片
     */
    fun getAllCards(): Flow<List<Card>>

    /**
     * 根据ID获取卡片
     */
    suspend fun getCardById(id: String): Card?

    /**
     * 搜索和筛选卡片
     */
    fun searchCards(filter: SearchFilter): Flow<List<Card>>

    /**
     * 创建新卡片
     */
    suspend fun createCard(card: Card): Result<Card>

    /**
     * 更新卡片
     */
    suspend fun updateCard(card: Card): Result<Card>

    /**
     * 删除卡片
     */
    suspend fun deleteCard(id: String): Result<Unit>

    /**
     * 切换收藏状态
     */
    suspend fun toggleFavorite(cardId: String): Result<Card>

    /**
     * 获取收藏的卡片
     */
    fun getFavoriteCards(): Flow<List<Card>>

    /**
     * 根据类型获取卡片
     */
    fun getCardsByType(type: CardType): Flow<List<Card>>

    /**
     * 根据标签获取卡片
     */
    fun getCardsByTag(tag: String): Flow<List<Card>>

    /**
     * 同步数据（从服务器拉取最新数据）
     */
    suspend fun sync(): Result<Unit>

    /**
     * 刷新本地缓存
     */
    suspend fun refresh(): Result<Unit>
}

