package tech.zhifu.app.myhub.datastore.model

import kotlinx.serialization.Serializable

/**
 * 统计信息实体
 */
@Serializable
data class Statistics(
    val totalCards: Int = 0,
    val favoriteCards: Int = 0,
    val recentEdits: Int = 0, // 最近编辑的卡片数
    val cardsByType: Map<CardType, Int> = emptyMap(),
    val cardsByTag: Map<String, Int> = emptyMap(),
    val lastSyncTime: Long? = null // Unix timestamp
)

/**
 * 卡片统计详情
 */
@Serializable
data class CardStatistics(
    val cardId: String,
    val viewCount: Int = 0,
    val editCount: Int = 0,
    val shareCount: Int = 0,
    val lastViewedAt: Long? = null
)

