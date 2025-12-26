package tech.zhifu.app.myhub.datastore.repository.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.model.Statistics
import tech.zhifu.app.myhub.datastore.repository.StatisticsRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/**
 * 统计信息仓库实现（服务端）
 * 使用 LocalCardDataSource 获取数据并计算统计信息
 */
class StatisticsRepositoryImpl(
    private val cardDataSource: LocalCardDataSource
) : StatisticsRepository {

    override suspend fun getStatistics(): Statistics {
        // 从 LocalCardDataSource 获取卡片数据并计算统计信息
        val cards = cardDataSource.getAllCards()

        val totalCards = cards.size
        val favoriteCards = cards.count { it.isFavorite }

        // 计算最近编辑的卡片数（最近7天）
        val sevenDaysAgo = Clock.System.now().minus(7.days)
        val recentEdits = cards.count { card ->
            card.updatedAt > sevenDaysAgo
        }

        // 按类型统计
        val cardsByType = cards.groupBy { it.type }
            .mapValues { it.value.size }

        // 按标签统计
        val cardsByTag = mutableMapOf<String, Int>()
        cards.forEach { card ->
            card.tags.forEach { tagName ->
                cardsByTag[tagName] = cardsByTag.getOrDefault(tagName, 0) + 1
            }
        }

        return Statistics(
            totalCards = totalCards,
            favoriteCards = favoriteCards,
            recentEdits = recentEdits,
            cardsByType = cardsByType,
            cardsByTag = cardsByTag,
            lastSyncTime = Clock.System.now().toEpochMilliseconds()
        )
    }

    override suspend fun refreshStatistics(): Statistics {
        return getStatistics()
    }
}

