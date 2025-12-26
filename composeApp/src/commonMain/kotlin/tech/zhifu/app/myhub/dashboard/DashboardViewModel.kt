package tech.zhifu.app.myhub.dashboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.model.Statistics
import tech.zhifu.app.myhub.datastore.repository.ReactiveCardRepository
import tech.zhifu.app.myhub.datastore.repository.ReactiveStatisticsRepository
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import kotlin.time.Clock

/**
 * Dashboard ViewModel
 *
 * 管理 Dashboard 页面的状态和业务逻辑
 */
class DashboardViewModel(
    private val cardRepository: ReactiveCardRepository,
    private val statisticsRepository: ReactiveStatisticsRepository,
    private val coroutineScope: CoroutineScope
) {
    private val logger = logger("Dashboard")

    private val _uiState = MutableStateFlow<DashboardUiState>(
        DashboardUiState(
            statistics = Statistics(),
            recentCards = emptyList(),
            favoriteCards = emptyList(),
            isLoading = false,
            error = null,
            lastSyncTime = null
        )
    )

    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    /**
     * 加载 Dashboard 数据
     * 同时监听统计信息和卡片数据的变化
     */
    private fun loadDashboardData() {
        logger.info { "Loading dashboard data from server" }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        // 先从服务器获取数据
        coroutineScope.launch {
            try {
                // 从服务器获取所有卡片
                val cards = cardRepository.getAllCards()
                logger.info { "Fetched ${cards.size} cards from card repository" }

                // 从服务器刷新统计信息
                val statistics = statisticsRepository.refreshStatistics()
                logger.info { "Statistics refreshed: totalCards=${statistics.totalCards}, favoriteCards=${statistics.favoriteCards}" }
            } catch (e: Exception) {
                logger.error(e) {
                    "Failed to load data from server: ${e.message}"
                }
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load data from server: ${e.message}"
                )
            }
        }

        // 监听统计信息
        statisticsRepository.observeStatistics()
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load statistics"
                )
            }
            .onEach { statistics ->
                _uiState.value = _uiState.value.copy(
                    statistics = statistics,
                    lastSyncTime = statistics.lastSyncTime
                )
            }
            .launchIn(coroutineScope)

        // 监听所有卡片，用于获取最近编辑的卡片
        cardRepository.observeAllCards()
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load cards"
                )
            }
            .onEach { cards ->
                // 获取最近编辑的卡片（按 updated_at 排序，取前 10 个）
                val recentCards = cards
                    .sortedByDescending { it.updatedAt }
                    .take(10)

                _uiState.value = _uiState.value.copy(
                    recentCards = recentCards,
                    isLoading = false
                )
            }
            .launchIn(coroutineScope)

        // 监听收藏的卡片
        cardRepository.observeFavoriteCards()
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load favorite cards"
                )
            }
            .onEach { favoriteCards ->
                _uiState.value = _uiState.value.copy(
                    favoriteCards = favoriteCards
                )
            }
            .launchIn(coroutineScope)
    }

    /**
     * 刷新 Dashboard 数据
     * 触发统计信息和卡片数据的刷新
     */
    fun refresh() {
        coroutineScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 刷新统计信息
                val statistics = statisticsRepository.refreshStatistics()
                _uiState.value = _uiState.value.copy(
                    statistics = statistics,
                    lastSyncTime = statistics.lastSyncTime
                )

                // 刷新卡片数据（通过重新获取所有卡片触发更新）
                cardRepository.getAllCards()

                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to refresh"
                )
            }
        }
    }

    /**
     * 同步数据（从服务器拉取最新数据）
     */
    fun sync() {
        coroutineScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 同步卡片数据（通过刷新所有卡片）
                cardRepository.getAllCards()

                // 同步成功后刷新统计信息
                val statistics = statisticsRepository.refreshStatistics()
                _uiState.value = _uiState.value.copy(
                    statistics = statistics,
                    lastSyncTime = Clock.System.now().toEpochMilliseconds(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to sync"
                )
            }
        }
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * 导出数据为 JSON
     * 获取所有卡片和统计信息，序列化为 JSON 格式
     */
    fun exportData() {
        coroutineScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 获取所有卡片
                val allCards = cardRepository.getAllCards()

                // 获取统计信息
                val statistics = statisticsRepository.getStatistics()

                // 创建导出数据
                val exportData = DashboardExportData(
                    exportTime = Clock.System.now().toEpochMilliseconds(),
                    cards = allCards,
                    statistics = statistics
                )

                // 序列化为 JSON（使用 pretty print 格式，便于编辑）
                val json = Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
                val jsonString = json.encodeToString(DashboardExportData.serializer(), exportData)

                _uiState.value = _uiState.value.copy(
                    exportedJson = jsonString,
                    showExportDialog = true,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "导出失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 关闭导出对话框
     */
    fun closeExportDialog() {
        _uiState.value = _uiState.value.copy(
            showExportDialog = false,
            exportedJson = null
        )
    }

    /**
     * 显示导入对话框
     */
    fun showImportDialog() {
        _uiState.value = _uiState.value.copy(
            showImportDialog = true,
            importJson = "",
            importError = null,
            importPreview = null
        )
    }

    /**
     * 关闭导入对话框
     */
    fun closeImportDialog() {
        _uiState.value = _uiState.value.copy(
            showImportDialog = false,
            importJson = "",
            importError = null,
            importPreview = null
        )
    }

    /**
     * 更新导入 JSON 文本
     */
    fun updateImportJson(json: String) {
        _uiState.value = _uiState.value.copy(
            importJson = json,
            importError = null,
            importPreview = null
        )
    }

    /**
     * 预览导入数据（解析 JSON，但不写入数据库）
     */
    fun previewImportData() {
        val jsonString = _uiState.value.importJson.trim()
        if (jsonString.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                importError = "请输入 JSON 数据"
            )
            return
        }

        try {
            val json = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
            val exportData = json.decodeFromString<DashboardExportData>(jsonString)

            _uiState.value = _uiState.value.copy(
                importPreview = exportData,
                importError = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                importError = "JSON 解析失败: ${e.message}",
                importPreview = null
            )
        }
    }

    /**
     * 执行导入数据（将数据写入数据库）
     * 注意：目前暂时不写入，只验证数据格式
     */
    fun importData() {
        val preview = _uiState.value.importPreview
        if (preview == null) {
            _uiState.value = _uiState.value.copy(
                importError = "请先预览数据"
            )
            return
        }

        coroutineScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // TODO: 暂时不写入数据库，只验证数据格式
                // 未来可以在这里实现：
                // 1. 批量插入卡片
                // 2. 更新统计信息
                // 3. 处理冲突（如 ID 已存在）

                // 验证数据
                val cardCount = preview.cards.size
                val statistics = preview.statistics

                // 暂时只显示成功消息，不实际写入
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )

                // 关闭对话框
                closeImportDialog()

                // 显示成功消息（通过 error 字段，实际应该是 success 消息）
                // TODO: 添加 success 消息字段
                _uiState.value = _uiState.value.copy(
                    error = "导入预览成功！共 ${cardCount} 张卡片。\n注意：当前版本仅支持预览，实际导入功能待实现。"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    importError = "导入失败: ${e.message}"
                )
            }
        }
    }
}
