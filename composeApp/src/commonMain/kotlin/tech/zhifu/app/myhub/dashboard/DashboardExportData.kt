package tech.zhifu.app.myhub.dashboard

import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.Statistics

/**
 * Dashboard 导出数据
 * 包含所有卡片和统计信息，用于导出为 JSON 格式的测试数据
 */
@Serializable
data class DashboardExportData(
    val version: String = "1.0",
    val exportTime: Long, // Unix timestamp
    val cards: List<Card>,
    val statistics: Statistics
)


