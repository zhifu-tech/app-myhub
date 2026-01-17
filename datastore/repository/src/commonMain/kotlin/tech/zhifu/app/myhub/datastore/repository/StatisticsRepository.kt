package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.Statistics

/**
 * 统计信息仓库接口（基础接口，同步风格）
 */
interface StatisticsRepository {
    suspend fun getStatistics(): Statistics
    suspend fun refreshStatistics(): Statistics
}

/**
 * 响应式统计信息仓库接口（扩展接口，客户端使用）
 */
interface ReactiveStatisticsRepository : StatisticsRepository {
    fun observeStatistics(): Flow<Statistics>
}

