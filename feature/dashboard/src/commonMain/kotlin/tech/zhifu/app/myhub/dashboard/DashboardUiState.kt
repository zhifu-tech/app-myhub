package tech.zhifu.app.myhub.dashboard

import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.Statistics

/**
 * 视图类型枚举
 */
enum class ViewType {
    GRID,   // 瀑布流视图
    LIST    // 列表视图
}

/**
 * Dashboard UI状态
 */
data class DashboardUiState(
    val statistics: Statistics = Statistics(),
    val recentCards: List<Card> = emptyList(),
    val favoriteCards: List<Card> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastSyncTime: Long? = null,
    val viewType: ViewType = ViewType.GRID
)

