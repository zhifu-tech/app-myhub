package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.CardStatistics
import tech.zhifu.app.myhub.datastore.model.Statistics

/**
 * 统计信息仓库接口
 */
interface StatisticsRepository {
    /**
     * 获取总体统计信息
     */
    fun getStatistics(): Flow<Statistics>

    /**
     * 获取卡片统计详情
     */
    suspend fun getCardStatistics(cardId: String): CardStatistics?

    /**
     * 更新卡片查看次数
     */
    suspend fun incrementCardViewCount(cardId: String): Result<Unit>

    /**
     * 更新卡片编辑次数
     */
    suspend fun incrementCardEditCount(cardId: String): Result<Unit>

    /**
     * 刷新统计信息
     */
    suspend fun refreshStatistics(): Result<Statistics>
}

