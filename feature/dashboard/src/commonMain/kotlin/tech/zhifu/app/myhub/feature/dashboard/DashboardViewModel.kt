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
import tech.zhifu.app.myhub.logger.warn
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
    private val userId: String = "user-001" // fixme: 需要移除
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

        // 使用 streamCards 监听数据变化
        // refresh = false 表示优先使用缓存（本地数据），避免网络错误时无法使用
        // 如果需要刷新，可以通过 refresh() 方法单独触发
        cardRepository.streamCards(userId, refresh = false)
            .onEach { response ->
                when (response) {
                    is StoreReadResponse.Initial -> {}
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

                        // 计算复习进度（TODO: 实际应该从 user_card.last_reviewed_at 计算）
                        // 暂时使用模拟数据：假设有 5 张卡片需要复习，总共 15 张卡片
                        val reviewProgress = ReviewProgress(
                            completed = 10, // 已完成复习的卡片数
                            total = 15     // 需要复习的卡片总数
                        )

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
                                lastSyncTime = Clock.System.now().toEpochMilliseconds(),
                                reviewProgress = reviewProgress,
                                showFocusReview = true // 初始加载时默认显示
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
                                isRefreshing = false,
                                reviewProgress = reviewProgress
                            )
                        }
                    }

                    is StoreReadResponse.NoNewData -> {
                        logger.info { "No new data from ${response.origin}" }
                    }

                    is StoreReadResponse.Error -> {
                        val errorMessage = response.errorMessageOrNull() ?: "Unknown error"
                        logger.error { "Error loading cards: $errorMessage" }

                        // 网络错误时，不立即设置空数据，而是等待本地数据加载
                        // Store5 会自动从 SourceOfTruth（本地数据库）加载数据
                        // 只有在没有本地数据且是初始加载时才设置错误状态
                        val currentState = _uiState.value

                        // 如果已经有数据（从本地加载的），只记录错误但不影响显示
                        if (currentState is DashboardUiState.Content && currentState.recentCards.isNotEmpty()) {
                            // 有本地数据，只更新错误信息（用于提示用户网络不可用）
                            _uiState.value = currentState.copy(
                                error = "网络连接失败，显示本地数据",
                                isRefreshing = false
                            )
                        } else if (currentState is DashboardUiState.InitialLoading) {
                            // 初始加载且网络错误，主动从本地数据库加载数据
                            logger.info { "Network error, attempting to load from local database" }
                            coroutineScope.launch {
                                try {
                                    val localCards = cardRepository.getCards(userId)?.cards ?: emptyList()
                                    if (localCards.isNotEmpty()) {
                                        logger.info { "Loaded ${localCards.size} cards from local database" }
                                        val recentCards = localCards
                                            .sortedByDescending { it.updatedAt }
                                            .take(10)
                                        val favoriteCards = localCards.filter { it.isFavorite }
                                        val reviewProgress = ReviewProgress(completed = 10, total = 15)

                                        _uiState.value = DashboardUiState.Content(
                                            statistics = Statistics(
                                                totalCards = localCards.size,
                                                favoriteCards = favoriteCards.size,
                                                recentEdits = recentCards.size,
                                                lastSyncTime = Clock.System.now().toEpochMilliseconds()
                                            ),
                                            recentCards = recentCards,
                                            favoriteCards = favoriteCards,
                                            lastSyncTime = Clock.System.now().toEpochMilliseconds(),
                                            reviewProgress = reviewProgress,
                                            showFocusReview = true,
                                            error = "网络连接失败，显示本地数据"
                                        )
                                    } else {
                                        // 本地也没有数据，显示错误
                                        logger.warn { "No local data available" }
                                        _uiState.value = DashboardUiState.Content(
                                            statistics = Statistics(),
                                            recentCards = emptyList(),
                                            favoriteCards = emptyList(),
                                            lastSyncTime = currentState.lastSyncTime,
                                            error = errorMessage,
                                            showFocusReview = false
                                        )
                                    }
                                } catch (localError: Exception) {
                                    logger.error(localError) { "Failed to load from local database" }
                                    _uiState.value = DashboardUiState.Content(
                                        statistics = Statistics(),
                                        recentCards = emptyList(),
                                        favoriteCards = emptyList(),
                                        lastSyncTime = currentState.lastSyncTime,
                                        error = errorMessage,
                                        showFocusReview = false
                                    )
                                }
                            }
                        } else if (currentState is DashboardUiState.Content) {
                            // Content 状态但没有数据，可能是网络错误
                            _uiState.value = currentState.copy(
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

                // 网络错误时，尝试从本地数据库加载数据
                if (currentState is DashboardUiState.InitialLoading) {
                    logger.info { "Network error, attempting to load from local database" }
                    // 尝试从本地加载数据（不触发网络请求）
                    coroutineScope.launch {
                        try {
                            val localCards = cardRepository.getCards(userId)?.cards ?: emptyList()
                            if (localCards.isNotEmpty()) {
                                logger.info { "Loaded ${localCards.size} cards from local database" }
                                val recentCards = localCards
                                    .sortedByDescending { it.updatedAt }
                                    .take(10)
                                val favoriteCards = localCards.filter { it.isFavorite }
                                val reviewProgress = ReviewProgress(completed = 10, total = 15)

                                _uiState.value = DashboardUiState.Content(
                                    statistics = Statistics(
                                        totalCards = localCards.size,
                                        favoriteCards = favoriteCards.size,
                                        recentEdits = recentCards.size,
                                        lastSyncTime = Clock.System.now().toEpochMilliseconds()
                                    ),
                                    recentCards = recentCards,
                                    favoriteCards = favoriteCards,
                                    lastSyncTime = Clock.System.now().toEpochMilliseconds(),
                                    reviewProgress = reviewProgress,
                                    showFocusReview = true,
                                    error = "网络连接失败，显示本地数据"
                                )
                            } else {
                                // 本地也没有数据，显示错误
                                _uiState.value = DashboardUiState.Content(
                                    statistics = Statistics(),
                                    recentCards = emptyList(),
                                    favoriteCards = emptyList(),
                                    lastSyncTime = currentState.lastSyncTime,
                                    error = e.message ?: "无法加载数据，请检查网络连接",
                                    showFocusReview = false
                                )
                            }
                        } catch (localError: Exception) {
                            logger.error(localError) { "Failed to load from local database" }
                            _uiState.value = DashboardUiState.Content(
                                statistics = Statistics(),
                                recentCards = emptyList(),
                                favoriteCards = emptyList(),
                                lastSyncTime = currentState.lastSyncTime,
                                error = e.message ?: "无法加载数据",
                                showFocusReview = false
                            )
                        }
                    }
                } else if (currentState is DashboardUiState.Content) {
                    // Content 状态，只更新错误信息
                    _uiState.value = currentState.copy(
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

    fun dismissFocusReview() {
        val currentState = _uiState.value
        if (currentState is DashboardUiState.Content) {
            _uiState.value = currentState.copy(showFocusReview = false)
        }
    }

    fun startReview() {
        logger.info { "Start review flow" }
        // TODO: 导航到复习页面
    }
}
