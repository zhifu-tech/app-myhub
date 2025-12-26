package tech.zhifu.app.myhub.datastore.datasource.impl

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.LocalStatisticsDataSource
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Statistics
import kotlin.time.Clock

/**
 * 本地统计信息数据源实现（使用SQLDelight）
 */
class LocalStatisticsDataSourceImpl(
    private val database: MyHubDatabase
) : LocalStatisticsDataSource {

    override suspend fun getStatistics(): Statistics? {
        val statsRow = database.statisticsQueries.selectStatistics().awaitAsOneOrNull() ?: return null
        val cardTypeStats = database.statisticsQueries.selectCardTypeStatistics()
            .awaitAsList()
            .associate { it.card_type to CardType.valueOf(it.card_type) to it.count.toInt() }
            .mapKeys { it.key.second }
        val tagStats = database.statisticsQueries.selectTagStatistics()
            .awaitAsList()
            .associate { it.tag_name to it.count.toInt() }

        return Statistics(
            totalCards = statsRow.total_cards.toInt(),
            favoriteCards = statsRow.favorite_cards.toInt(),
            recentEdits = statsRow.recent_edits.toInt(),
            cardsByType = cardTypeStats,
            cardsByTag = tagStats,
            lastSyncTime = statsRow.last_sync_time
        )
    }

    override suspend fun saveStatistics(statistics: Statistics) {
        database.transaction {
            // 保存主统计信息
            database.statisticsQueries.updateStatistics(
                total_cards = statistics.totalCards.toLong(),
                favorite_cards = statistics.favoriteCards.toLong(),
                recent_edits = statistics.recentEdits.toLong(),
                last_sync_time = statistics.lastSyncTime,
                updated_at = Clock.System.now().toString()
            )

            // 保存卡片类型统计
            statistics.cardsByType.forEach { (type, count) ->
                database.statisticsQueries.updateCardTypeStatistics(
                    card_type = type.name,
                    count = count.toLong()
                )
            }

            // 保存标签统计
            statistics.cardsByTag.forEach { (tagName, count) ->
                database.statisticsQueries.updateTagStatistics(
                    tag_name = tagName,
                    count = count.toLong()
                )
            }
        }
    }

    override suspend fun clearStatistics() {
        database.statisticsQueries.resetStatistics(Clock.System.now().toString())
    }
}

