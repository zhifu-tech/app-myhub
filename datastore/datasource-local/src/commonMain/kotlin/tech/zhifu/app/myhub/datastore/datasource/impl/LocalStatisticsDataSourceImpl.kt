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

    override suspend fun getStatistics(userId: String): Statistics? {
        val statsRow = database.statisticsQueries.selectStatistics(userId).awaitAsOneOrNull() ?: return null
        val cardTypeStats = database.statisticsQueries.selectCardTypeStatistics(userId)
            .awaitAsList()
            .associate { CardType.valueOf(it.card_type) to it.count.toInt() }
        val tagStats = database.statisticsQueries.selectTagStatistics(userId)
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

    override suspend fun saveStatistics(statistics: Statistics, userId: String) {
        database.transaction {
            val updatedAt = Clock.System.now().toString()
            // 保存主统计信息
            // 使用 INSERT OR REPLACE，基于 user_id 查找现有记录的 id
            // 参数顺序：user_id (查找), total_cards, favorite_cards, recent_edits, last_sync_time, updated_at, user_id (插入)
            database.statisticsQueries.updateStatistics(
                userId,  // 用于查找现有记录
                statistics.totalCards.toLong(),
                statistics.favoriteCards.toLong(),
                statistics.recentEdits.toLong(),
                statistics.lastSyncTime,
                updatedAt,
                userId  // 用于插入新记录
            )

            // 保存卡片类型统计
            statistics.cardsByType.forEach { (type, count) ->
                database.statisticsQueries.updateCardTypeStatistics(
                    card_type = type.name,
                    count = count.toLong(),
                    user_id = userId
                )
            }

            // 保存标签统计
            statistics.cardsByTag.forEach { (tagName, count) ->
                database.statisticsQueries.updateTagStatistics(
                    tag_name = tagName,
                    count = count.toLong(),
                    user_id = userId
                )
            }
        }
    }

    override suspend fun clearStatistics(userId: String) {
        database.statisticsQueries.resetStatistics(Clock.System.now().toString(), userId)
    }
}

