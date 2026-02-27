package tech.zhifu.app.myhub.feature.dashboard

import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.ReviewProgress

/**
 * 统计信息
 */
data class Statistics(
    val totalCards: Int = 0,
    val favoriteCards: Int = 0,
    val recentEdits: Int = 0,
    val lastSyncTime: Long? = null
)

sealed class DashboardUiState {
    /**
     * 初始加载状态
     * 应用正在初始化或首次加载数据，无数据可显示
     */
    data class InitialLoading(
        val lastSyncTime: Long? = null
    ) : DashboardUiState()

    /**
     * 内容状态（有数据）
     * 应用已加载完成，可以正常显示数据
     * 支持同时显示数据和加载状态（如刷新时）
     */
    data class Content(
        val statistics: Statistics = Statistics(),
        val recentCards: List<Card>,
        val favoriteCards: List<Card>,
        val lastSyncTime: Long?,
        val isRefreshing: Boolean = false,  // 刷新时仍显示数据
        val error: String? = null,         // 错误时仍显示数据
        val reviewProgress: ReviewProgress = ReviewProgress(), // 复习进度
        val showFocusReview: Boolean = true, // 是否显示 Focus & Review 模块
        // 新增字段
        val collections: List<tech.zhifu.app.myhub.datastore.model.domain.Collection> = emptyList(),
        val collectionCardCounts: Map<String, Int> = emptyMap(), // collectionId -> cardCount
        val reviewCardsCount: Int = 0, // 待复习卡片数量
        // 分页相关字段
        val hasMoreCards: Boolean = false,
        val isLoadingMoreCards: Boolean = false,
        val cardsPage: Int = 1,
        val cardsPageSize: Int = 20,
        val hasMoreCollections: Boolean = false,
        val isLoadingMoreCollections: Boolean = false,
        val collectionsPage: Int = 1,
        val collectionsPageSize: Int = 10
    ) : DashboardUiState()
}

