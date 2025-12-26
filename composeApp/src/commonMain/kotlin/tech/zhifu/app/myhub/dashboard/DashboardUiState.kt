package tech.zhifu.app.myhub.dashboard

import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.Statistics

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
    // 导出/导入相关状态
    val exportedJson: String? = null, // 导出的 JSON 数据
    val showExportDialog: Boolean = false, // 是否显示导出对话框
    val showImportDialog: Boolean = false, // 是否显示导入对话框
    val importJson: String = "", // 导入的 JSON 数据（用户输入）
    val importError: String? = null, // 导入错误信息
    val importPreview: DashboardExportData? = null // 导入数据预览（解析后的数据）
)
