package tech.zhifu.app.myhub.service

import tech.zhifu.app.myhub.datastore.model.Statistics
import tech.zhifu.app.myhub.datastore.repository.StatisticsRepository

/**
 * 统计信息服务
 */
class StatisticsService(
    private val statisticsRepository: StatisticsRepository
) {
    suspend fun getStatistics(): Statistics {
        return statisticsRepository.getStatistics()
    }

    suspend fun refreshStatistics(): Statistics {
        return statisticsRepository.refreshStatistics()
    }
}

