package tech.zhifu.app.myhub.feature.dashboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.mobilenativefoundation.store.store5.StoreReadResponse
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.isFavorite
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.model.domain.ReviewProgress
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.card.CardStoreData
import tech.zhifu.app.myhub.datastore.repository.card.cards
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionRepository
import tech.zhifu.app.myhub.datastore.repository.collection.CollectionStoreData
import tech.zhifu.app.myhub.datastore.repository.collection.collections
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import kotlin.time.Clock

class DashboardViewModel(
    private val cardRepository: CardRepository,
    private val collectionRepository: CollectionRepository,
    private val userRepository: UserRepository,
    private val coroutineScope: CoroutineScope
) {
    private val logger = logger("Dashboard")

    private val _uiState = MutableStateFlow<DashboardUiState>(
        DashboardUiState.InitialLoading()
    )

    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /** 401 未授权时发出一次，用于触发跳转登录 */
    private val _navigateToLogin = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    val navigateToLogin: SharedFlow<Unit> = _navigateToLogin.asSharedFlow()

    private fun isUnauthorizedError(message: String?): Boolean =
        message != null && (message.contains("401") || message.contains("Unauthorized", ignoreCase = true))

    private fun requestNavigateToLogin() {
        _navigateToLogin.tryEmit(Unit)
    }

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
                // 第二步：用户存在后，获取 userId 并加载数据
                loadCardsForUser(user.id)
                loadCollectionsForUser(user.id)
                loadReviewProgress(user.id)
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
                if (isUnauthorizedError(e.message)) requestNavigateToLogin()
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
                if (isUnauthorizedError(errorMessage)) requestNavigateToLogin()
                // 网络错误时，尝试从本地加载
                handleNetworkError(errorMessage, userId)
            }

            else -> {
                // 其他状态（NoNewData, Initial）不需要特殊处理
            }
        }
    }

    private fun updateUiStateWithCards(cards: List<Card>) {
        // 分页加载：只取第一页的数据
        val pageSize = 20
        val recentCards = cards
            .sortedByDescending { it.updatedAt }
            .take(pageSize)

        val favoriteCards = cards.filter { it.isFavorite }

        val currentState = _uiState.value
        val hasMoreCards = cards.size > pageSize

        // 获取当前的 reviewProgress（如果存在）
        val currentReviewProgress = when (currentState) {
            is DashboardUiState.Content -> currentState.reviewProgress
            is DashboardUiState.InitialLoading -> ReviewProgress()
        }

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
                reviewProgress = currentReviewProgress,
                showFocusReview = true,
                hasMoreCards = hasMoreCards,
                cardsPage = 1,
                cardsPageSize = pageSize
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
                hasMoreCards = hasMoreCards,
                cardsPage = 1,
                cardsPageSize = pageSize
            )
        }
    }

    private fun loadCollectionsForUser(userId: String) {
        logger.info { "Loading collections for user: $userId" }

        collectionRepository.streamCollections(userId, refresh = false)
            .onEach { response ->
                when (response) {
                    is StoreReadResponse.Data -> {
                        val storeData = response.value as? CollectionStoreData
                        val collections = storeData?.collections ?: emptyList()
                        logger.info { "Received ${collections.size} collections from ${response.origin}" }
                        updateUiStateWithCollections(collections, userId)
                    }
                    is StoreReadResponse.Loading -> {
                        logger.info { "Loading collections from ${response.origin}" }
                    }
                    is StoreReadResponse.Error -> {
                        val errorMessage = response.errorMessageOrNull()
                        logger.error { "Error loading collections: $errorMessage" }
                        if (isUnauthorizedError(errorMessage)) requestNavigateToLogin()
                    }
                    else -> {
                        // 其他状态不需要特殊处理
                    }
                }
            }
            .catch { e ->
                logger.error(e) { "Failed to load collections for user: $userId" }
                if (isUnauthorizedError(e.message)) requestNavigateToLogin()
            }
            .launchIn(coroutineScope)
    }

    private fun updateUiStateWithCollections(collections: List<Collection>, userId: String) {
        // collections 已经是从 store 获取的分页数据（page=1, pageSize=10）
        val pageSize = 10
        val hasMoreCollections = collections.size >= pageSize

        val currentState = _uiState.value
        _uiState.value = when (currentState) {
            is DashboardUiState.InitialLoading -> {
                // 如果还没有 Content 状态，等待 Cards 加载完成
                currentState
            }
            is DashboardUiState.Content -> currentState.copy(
                collections = collections,
                hasMoreCollections = hasMoreCollections,
                collectionsPage = 1,
                collectionsPageSize = pageSize
            )
        }
    }

    private fun loadReviewProgress(userId: String) {
        coroutineScope.launch {
            // FIXME:
//            try {
//                val progress = cardRepository.getReviewProgress(userId)
//
//                val currentState = _uiState.value
//                _uiState.value = when (currentState) {
//                    is DashboardUiState.InitialLoading -> {
//                        // 如果还没有 Content 状态，等待 Cards 加载完成
//                        currentState
//                    }
//                    is DashboardUiState.Content -> currentState.copy(
//                        reviewProgress = progress,
//                        reviewCardsCount = progress.total
//                    )
//                }
//            } catch (e: Exception) {
//                logger.error(e) { "Failed to load review progress for user: $userId" }
//            }
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
     * 使用 Repository 的 stream 方法强制从网络刷新
     */
    fun refresh() {
        coroutineScope.launch {
            logger.info { "Refreshing dashboard data" }

            val currentState = _uiState.value
            val userId = getCurrentUserId() ?: return@launch

            // 设置刷新状态
            if (currentState is DashboardUiState.Content) {
                _uiState.value = currentState.copy(
                    isRefreshing = true,
                    error = null
                )
            }

            // 刷新所有数据（异步执行，不等待完成）
            cardRepository.streamCards(userId, refresh = true)
                .onEach { response ->
                    if (response is StoreReadResponse.Data) {
                        val cards = (response.value as? CardStoreData)?.cards ?: emptyList()
                        updateUiStateWithCards(cards)
                        checkAndEndRefreshing()
                    }
                    if (response is StoreReadResponse.Error && isUnauthorizedError(response.errorMessageOrNull())) {
                        requestNavigateToLogin()
                    }
                }
                .catch { e ->
                    logger.error(e) { "Error refreshing cards" }
                    if (isUnauthorizedError(e.message)) requestNavigateToLogin()
                    checkAndEndRefreshing()
                }
                .launchIn(coroutineScope)

            collectionRepository.streamCollections(userId, refresh = true)
                .onEach { response ->
                    if (response is StoreReadResponse.Data) {
                        val collections = (response.value as? CollectionStoreData)?.collections ?: emptyList()
                        updateUiStateWithCollections(collections, userId)
                        checkAndEndRefreshing()
                    }
                    if (response is StoreReadResponse.Error && isUnauthorizedError(response.errorMessageOrNull())) {
                        requestNavigateToLogin()
                    }
                }
                .catch { e ->
                    logger.error(e) { "Error refreshing collections" }
                    if (isUnauthorizedError(e.message)) requestNavigateToLogin()
                    checkAndEndRefreshing()
                }
                .launchIn(coroutineScope)

            // 刷新 ReviewProgress
            loadReviewProgress(userId)
        }
    }

    /**
     * 检查并结束刷新状态
     * 当所有数据都加载完成后，设置 isRefreshing = false
     */
    private fun checkAndEndRefreshing() {
        val currentState = _uiState.value
        if (currentState is DashboardUiState.Content && currentState.isRefreshing) {
            // 简单处理：延迟一点时间后结束刷新，确保所有数据都已更新
            coroutineScope.launch {
                kotlinx.coroutines.delay(300) // 给一点缓冲时间
                val updatedState = _uiState.value
                if (updatedState is DashboardUiState.Content) {
                    _uiState.value = updatedState.copy(isRefreshing = false)
                }
            }
        }
    }

    /**
     * 加载更多 Cards（分页）
     */
    fun loadMoreCards() {
        val currentState = _uiState.value as? DashboardUiState.Content ?: return
        if (currentState.isLoadingMoreCards || !currentState.hasMoreCards) return

        coroutineScope.launch {
            val userId = getCurrentUserId() ?: return@launch
            _uiState.value = currentState.copy(isLoadingMoreCards = true)

            try {
                val nextPage = currentState.cardsPage + 1
                val storeData = cardRepository.getCards(
                    userId = userId,
                    page = nextPage,
                    pageSize = currentState.cardsPageSize
                )
                val newCards = storeData?.cards ?: emptyList()

                val updatedCards = currentState.recentCards + newCards
                val hasMore = newCards.size >= currentState.cardsPageSize

                _uiState.value = currentState.copy(
                    recentCards = updatedCards,
                    cardsPage = nextPage,
                    hasMoreCards = hasMore,
                    isLoadingMoreCards = false
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to load more cards" }
                _uiState.value = currentState.copy(
                    isLoadingMoreCards = false,
                    error = "加载更多卡片失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 加载更多 Collections（分页）
     */
    fun loadMoreCollections() {
        val currentState = _uiState.value as? DashboardUiState.Content ?: return
        if (currentState.isLoadingMoreCollections || !currentState.hasMoreCollections) return

        coroutineScope.launch {
            val userId = getCurrentUserId() ?: return@launch
            _uiState.value = currentState.copy(isLoadingMoreCollections = true)

            try {
                val nextPage = currentState.collectionsPage + 1
                val storeData = collectionRepository.getCollections(
                    userId = userId,
                    page = nextPage,
                    pageSize = currentState.collectionsPageSize
                )
                val newCollections = storeData?.collections ?: emptyList()

                val updatedCollections = currentState.collections + newCollections
                val hasMore = newCollections.size >= currentState.collectionsPageSize

                _uiState.value = currentState.copy(
                    collections = updatedCollections,
                    collectionsPage = nextPage,
                    hasMoreCollections = hasMore,
                    isLoadingMoreCollections = false
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to load more collections" }
                _uiState.value = currentState.copy(
                    isLoadingMoreCollections = false,
                    error = "加载更多集合失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 获取当前用户 ID
     */
    private suspend fun getCurrentUserId(): String? {
        return try {
            userRepository.getUser().id
        } catch (e: Exception) {
            logger.error(e) { "Failed to get current user" }
            null
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
