package tech.zhifu.app.myhub.feature.dashboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.mobilenativefoundation.store.store5.StoreReadResponse
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.isFavorite
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.card.CardStoreData
import tech.zhifu.app.myhub.datastore.repository.card.cards
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import kotlin.time.Clock

class DashboardViewModel(
    private val cardRepository: CardRepository,
    private val userRepository: UserRepository,
    private val coroutineScope: CoroutineScope
) {
    private val logger = logger("Dashboard")

    private val _uiState = MutableStateFlow<DashboardUiState>(
        DashboardUiState.InitialLoading()
    )

    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        logger.info { "Loading dashboard data" }
        _uiState.value = DashboardUiState.InitialLoading()
        userRepository.streamUser()
            .catch { e ->
                logger.error(e) { "User not found, waiting for bootstrap..." }
            }
            .distinctUntilChangedBy { user -> user.id }
            .onEach { user ->
                logger.info { "User observed: ${user.id}" }
                // 第二步：用户存在后，获取 userId 并 streamCards
                loadCardsForUser(user.id)
            }
            .launchIn(coroutineScope)
    }

    private fun loadCardsForUser(userId: String) {
        logger.info { "Loading cards for user: $userId" }

        cardRepository.streamCards(userId, refresh = false)
            .onEach { response ->
                handleStoreResponse(response, userId)
            }
            .catch { e ->
                logger.error(e) { "Failed to load cards for user: $userId" }
                handleError(e as Exception)
            }
            .launchIn(coroutineScope)
    }

    private fun handleStoreResponse(response: StoreReadResponse<*>, userId: String) {
        when (response) {
            is StoreReadResponse.Loading -> {
                logger.info { "Loading cards from ${response.origin}" }
                // 保持 InitialLoading 状态，不更新 UI
            }

            is StoreReadResponse.Data -> {
                val storeData = response.value as? CardStoreData
                val cards = storeData?.cards ?: emptyList()
                logger.info { "Received ${cards.size} cards from ${response.origin}" }

                if (cards.isNotEmpty()) {
                    updateUiStateWithCards(cards)
                }
                // 如果 cards 为空，保持 InitialLoading 状态
                // Store5 会在 Bootstrap 写入数据后自动推送更新
            }

            is StoreReadResponse.Error -> {
                val errorMessage = response.errorMessageOrNull() ?: "Unknown error"
                logger.error { "Error loading cards: $errorMessage" }

                // 网络错误时，尝试从本地加载
                handleNetworkError(errorMessage, userId)
            }

            else -> {
                // 其他状态（NoNewData, Initial）不需要特殊处理
            }
        }
    }

    private fun updateUiStateWithCards(cards: List<Card>) {
        val recentCards = cards
            .sortedByDescending { it.updatedAt }
            .take(10)

        val favoriteCards = cards.filter { it.isFavorite }

        val reviewProgress = ReviewProgress(
            completed = 10,
            total = 15
        )

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
                showFocusReview = true
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

    /**
     * 处理网络错误
     */
    private fun handleNetworkError(errorMessage: String, userId: String) {
        val currentState = _uiState.value

        if (currentState is DashboardUiState.Content && currentState.recentCards.isNotEmpty()) {
            // 有本地数据，只更新错误信息
            _uiState.value = currentState.copy(
                error = "网络连接失败，显示本地数据",
                isRefreshing = false
            )
        }
        // 如果没有数据，保持 InitialLoading 状态，等待 Store5 从本地加载
    }


    private fun handleError(e: Exception) {
        _uiState.value = DashboardUiState.Content(
            statistics = Statistics(),
            recentCards = emptyList(),
            favoriteCards = emptyList(),
            lastSyncTime = null,
            error = e.message ?: "无法加载数据",
            showFocusReview = false
        )
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
