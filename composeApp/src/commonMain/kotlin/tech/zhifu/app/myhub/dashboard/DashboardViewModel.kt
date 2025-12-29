package tech.zhifu.app.myhub.dashboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
}
