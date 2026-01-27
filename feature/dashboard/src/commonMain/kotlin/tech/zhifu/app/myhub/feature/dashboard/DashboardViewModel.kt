package tech.zhifu.app.myhub.feature.dashboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.mobilenativefoundation.store.store5.StoreReadResponse
import tech.zhifu.app.myhub.datastore.model.domain.isFavorite
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.card.cards
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import kotlin.time.Clock

/**
 * Dashboard ViewModel
 *
 * 管理 Dashboard 页面的状态和业务逻辑
 * 使用 CardRepository 作为数据层接口
 */
class DashboardViewModel(
    private val cardRepository: CardRepository,
    private val coroutineScope: CoroutineScope,
    private val userId: String = "guest-1" // TODO: 从用户会话获取
) {
    private val logger = logger("Dashboard")

    private val _uiState = MutableStateFlow<DashboardUiState>(
        DashboardUiState.InitialLoading()
    )

    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    /**
     * 加载 Dashboard 数据
     * 使用 Repository 的响应式流监听数据变化
     */
    private fun loadDashboardData() {
        logger.info { "Loading dashboard data" }
        _uiState.value = DashboardUiState.InitialLoading()

        // 使用 observeCards 监听数据变化，refresh = true 触发网络刷新
        cardRepository.streamCards(userId, refresh = true)
            .onEach { response ->
                when (response) {
                    is StoreReadResponse.Initial -> {
                        // 初始状态，不做处理
                    }

                    is StoreReadResponse.Loading -> {
                        logger.info { "Loading cards from ${response.origin}" }
                    }

                    is StoreReadResponse.Data -> {
                        val cards = response.value.cards
                        logger.info { "Received ${cards.size} cards from ${response.origin}" }

                        // 获取最近编辑的卡片（按 updated_at 排序，取前 10 个）
                        val recentCards = cards
                            .sortedByDescending { it.updatedAt }
                            .take(10)

                        // 获取收藏的卡片
                        val favoriteCards = cards.filter { it.isFavorite }

                        // 更新状态
                        val currentState = _uiState.value
                        _uiState.value = when (currentState) {
                            is DashboardUiState.InitialLoading -> DashboardUiState.Content(
                                statistics = Statistics(
                                    totalCards = cards.size,
                                    favoriteCards = favoriteCards.size,
                                    recentEdits = recentCards.size,
                                    lastSyncTime = Clock.System.now().toEpochMilliseconds()
                                ),
                                recentCards = recentCards,
                                favoriteCards = favoriteCards,
                                lastSyncTime = Clock.System.now().toEpochMilliseconds()
                            )

                            is DashboardUiState.Content -> currentState.copy(
                                statistics = Statistics(
                                    totalCards = cards.size,
                                    favoriteCards = favoriteCards.size,
                                    recentEdits = recentCards.size,
                                    lastSyncTime = Clock.System.now().toEpochMilliseconds()
                                ),
                                recentCards = recentCards,
                                favoriteCards = favoriteCards,
                                lastSyncTime = Clock.System.now().toEpochMilliseconds(),
                                isRefreshing = false
                            )
                        }
                    }

                    is StoreReadResponse.NoNewData -> {
                        logger.info { "No new data from ${response.origin}" }
                    }

                    is StoreReadResponse.Error -> {
                        val errorMessage = response.errorMessageOrNull() ?: "Unknown error"
                        logger.error { "Error loading cards: $errorMessage" }

                        val currentState = _uiState.value
                        _uiState.value = when (currentState) {
                            is DashboardUiState.InitialLoading -> DashboardUiState.Content(
                                statistics = Statistics(),
                                recentCards = emptyList(),
                                favoriteCards = emptyList(),
                                lastSyncTime = currentState.lastSyncTime,
                                error = errorMessage
                            )

                            is DashboardUiState.Content -> currentState.copy(
                                error = errorMessage,
                                isRefreshing = false
                            )
                        }
                    }

                }
            }
            .catch { e ->
                logger.error(e) { "Failed to load dashboard data: ${e.message}" }
                val currentState = _uiState.value
                _uiState.value = when (currentState) {
                    is DashboardUiState.InitialLoading -> DashboardUiState.Content(
                        statistics = Statistics(),
                        recentCards = emptyList(),
                        favoriteCards = emptyList(),
                        lastSyncTime = currentState.lastSyncTime,
                        error = e.message ?: "Failed to load data"
                    )

                    is DashboardUiState.Content -> currentState.copy(
                        error = e.message ?: "Failed to load data",
                        isRefreshing = false
                    )
                }
            }
            .launchIn(coroutineScope)
    }

    /**
     * 刷新 Dashboard 数据
     * 使用 Repository 的 fetchCards 强制从网络刷新
     */
    fun refresh() {
        coroutineScope.launch {
            logger.info { "Refreshing dashboard data" }

            // 设置刷新状态
            val currentState = _uiState.value
            if (currentState is DashboardUiState.Content) {
                _uiState.value = currentState.copy(
                    isRefreshing = true,
                    error = null
                )
            }

//            try {
//                // 使用 Repository 强制从网络刷新
//                val cards = cardRepository.fetchCards(userId)
//                logger.info { "Refreshed ${cards.size} cards from network" }
//
//                // 数据更新会通过 observeCards 自动推送到 UI
//                val finalState = _uiState.value
//                if (finalState is DashboardUiState.Content) {
//                    _uiState.value = finalState.copy(isRefreshing = false)
//                }
//            } catch (e: Exception) {
//                logger.error(e) { "Failed to refresh: ${e.message}" }
//                val finalState = _uiState.value
//                if (finalState is DashboardUiState.Content) {
//                    _uiState.value = finalState.copy(
//                        isRefreshing = false,
//                        error = e.message ?: "Failed to refresh"
//                    )
//                }
//            }
        }
    }

    /**
     * 同步数据（从服务器拉取最新数据）
     */
    fun sync() {
        coroutineScope.launch {
            try {
                cardRepository.fetchCards(userId)
                logger.info { "Background sync completed" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to sync: ${e.message}" }
            }
        }
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        val currentState = _uiState.value
        if (currentState is DashboardUiState.Content && currentState.error != null) {
            _uiState.value = currentState.copy(error = null)
        }
    }

    fun editCard(cardId: String) {
        logger.info { "Edit card: $cardId" }
    }

    fun toggleFavorite(cardId: String) {
        // TODO: 实现收藏切换
    }

    fun viewCard(cardId: String) {
        logger.info { "View card: $cardId" }
    }

    fun toggleViewType() {
        val currentState = _uiState.value
        if (currentState is DashboardUiState.Content) {
            val newViewType = when (currentState.viewType) {
                ViewType.GRID -> ViewType.LIST
                ViewType.LIST -> ViewType.GRID
            }
            _uiState.value = currentState.copy(viewType = newViewType)
        }
    }

    fun setViewType(viewType: ViewType) {
        val currentState = _uiState.value
        if (currentState is DashboardUiState.Content) {
            _uiState.value = currentState.copy(viewType = viewType)
        }
    }
}
