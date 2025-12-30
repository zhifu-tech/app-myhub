package tech.zhifu.app.myhub.dashboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
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

    // 标记是否正在进行手动刷新（用于防止监听器干扰刷新状态）
    private var isRefreshing = false

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
                // 始终保留更更新的 lastSyncTime（避免被旧的统计信息覆盖）
                val currentLastSyncTime = _uiState.value.lastSyncTime
                val newLastSyncTime = when {
                    // 如果正在进行手动刷新，保留刷新时设置的 lastSyncTime
                    isRefreshing -> currentLastSyncTime
                    // 如果当前 lastSyncTime 不为 null，且比统计信息中的更新，保留它
                    currentLastSyncTime != null &&
                        statistics.lastSyncTime != null &&
                        currentLastSyncTime > statistics.lastSyncTime!! -> currentLastSyncTime
                    // 如果当前 lastSyncTime 不为 null，但统计信息中的为 null，保留当前的
                    currentLastSyncTime != null &&
                        statistics.lastSyncTime == null -> currentLastSyncTime
                    // 否则使用统计信息中的 lastSyncTime（可能为 null）
                    else -> statistics.lastSyncTime
                }

                _uiState.value = _uiState.value.copy(
                    statistics = statistics,
                    lastSyncTime = newLastSyncTime
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
                    isLoading = if (isRefreshing) _uiState.value.isLoading else false
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
     * 确保刷新动画至少持续 2 秒，但数据返回后立即展示
     */
    fun refresh() {
        coroutineScope.launch {
            val startTime = Clock.System.now().toEpochMilliseconds()
            isRefreshing = true // 标记开始刷新
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 刷新统计信息
                val statistics = statisticsRepository.refreshStatistics()
                // 数据返回后立即更新 UI（不等待动画）
                // 手动设置同步时间为当前时间，确保时间戳正确更新
                val currentSyncTime = Clock.System.now().toEpochMilliseconds()
                _uiState.value = _uiState.value.copy(
                    statistics = statistics,
                    lastSyncTime = currentSyncTime
                )

                // 刷新卡片数据（通过重新获取所有卡片触发更新）
                cardRepository.getAllCards()

                // 计算已用时间，确保动画至少持续 2 秒
                val elapsedTime = Clock.System.now().toEpochMilliseconds() - startTime
                val minAnimationDuration = 2000L // 2 秒
                val remainingTime = minAnimationDuration - elapsedTime

                if (remainingTime > 0) {
                    delay(remainingTime)
                }

                isRefreshing = false // 标记刷新结束
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                logger.error(e) { "Failed to refresh: ${e.message}" }
                // 即使出错，也确保动画至少持续 2 秒
                val elapsedTime = Clock.System.now().toEpochMilliseconds() - startTime
                val minAnimationDuration = 2000L // 2 秒
                val remainingTime = minAnimationDuration - elapsedTime

                if (remainingTime > 0) {
                    delay(remainingTime)
                }

                isRefreshing = false // 标记刷新结束
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to refresh"
                )
            }
        }
    }

    /**
     * 同步数据（从服务器拉取最新数据）
     *
     * 使用场景：
     * - 应用从后台恢复时
     * - 网络恢复时自动同步
     * - 定期后台同步（不显示加载动画）
     * - 页面重新可见时
     *
     * 注意：与 refresh() 的区别：
     * - sync() 用于后台自动同步，不显示加载动画
     * - refresh() 用于用户手动刷新，有 2 秒最小动画时长
     */
    fun sync() {
        coroutineScope.launch {
            try {
                // 同步卡片数据（通过刷新所有卡片）
                cardRepository.getAllCards()

                // 同步成功后刷新统计信息
                val statistics = statisticsRepository.refreshStatistics()
                val currentSyncTime = Clock.System.now().toEpochMilliseconds()

                // 静默更新 UI（不设置 isLoading，避免显示加载动画）
                _uiState.value = _uiState.value.copy(
                    statistics = statistics,
                    lastSyncTime = currentSyncTime
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to sync: ${e.message}" }
                // 后台同步失败不显示错误，避免打扰用户
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
     * 编辑卡片
     */
    fun editCard(cardId: String) {
        logger.info { "Edit card: $cardId" }
        // TODO: 导航到编辑页面
    }

    /**
     * 切换收藏状态
     */
    fun toggleFavorite(cardId: String) {
        coroutineScope.launch {
            try {
                cardRepository.toggleFavorite(cardId)
                logger.info { "Toggled favorite for card: $cardId" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to toggle favorite: ${e.message}" }
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle favorite: ${e.message}"
                )
            }
        }
    }

    /**
     * 查看卡片详情
     */
    fun viewCard(cardId: String) {
        logger.info { "View card: $cardId" }
        // TODO: 导航到详情页面
    }
}

