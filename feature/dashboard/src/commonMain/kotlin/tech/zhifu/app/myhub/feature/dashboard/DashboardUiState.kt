package tech.zhifu.app.myhub.feature.dashboard

import tech.zhifu.app.myhub.datastore.model.domain.Card

/**
 * 统计信息
 */
data class Statistics(
    val totalCards: Int = 0,
    val favoriteCards: Int = 0,
    val recentEdits: Int = 0,
    val lastSyncTime: Long? = null
)

/**
 * 复习进度信息
 */
data class ReviewProgress(
    val completed: Int = 0,
    val total: Int = 0
) {
    val progress: Float
        get() = if (total > 0) completed.toFloat() / total.toFloat() else 0f
}

/**
 * 视图类型枚举
 */
enum class ViewType {
    GRID,   // 瀑布流视图
    LIST    // 列表视图
}

/**
 * Dashboard UI状态
 *
 * 使用 sealed class 表示不同的状态，确保状态互斥和类型安全
 *
 * 设计说明：
 * - InitialLoading: 首次加载，无数据可显示
 * - Content: 有数据的状态，可以同时显示数据和加载状态（刷新时）
 */
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
        val viewType: ViewType = ViewType.GRID,
        val isRefreshing: Boolean = false,  // 刷新时仍显示数据
        val error: String? = null,         // 错误时仍显示数据
        val reviewProgress: ReviewProgress = ReviewProgress(), // 复习进度
        val showFocusReview: Boolean = true // 是否显示 Focus & Review 模块
    ) : DashboardUiState()
}

