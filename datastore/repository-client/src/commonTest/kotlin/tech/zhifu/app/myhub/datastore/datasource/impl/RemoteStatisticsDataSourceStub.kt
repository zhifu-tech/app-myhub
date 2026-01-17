package tech.zhifu.app.myhub.datastore.datasource.impl

import tech.zhifu.app.myhub.datastore.datasource.RemoteStatisticsDataSource
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Statistics
import kotlin.time.Clock

/**
 * 远程统计信息数据源占位实现（仅用于测试）
 */
class RemoteStatisticsDataSourceStub(
    private val statistics: Statistics? = null
) : RemoteStatisticsDataSource {
    override suspend fun getStatistics(): Statistics {
        return statistics ?: Statistics(
            totalCards = 10,
            favoriteCards = 5,
            recentEdits = 3,
            cardsByType = mapOf(
                CardType.QUOTE to 4,
                CardType.CODE to 3,
                CardType.IDEA to 3
            ),
            cardsByTag = mapOf(
                "tag1" to 5,
                "tag2" to 3
            ),
            lastSyncTime = Clock.System.now().toEpochMilliseconds()
        )
    }
}

