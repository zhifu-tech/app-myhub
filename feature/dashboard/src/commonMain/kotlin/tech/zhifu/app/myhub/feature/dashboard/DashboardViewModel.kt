package tech.zhifu.app.myhub.feature.dashboard

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.datastore.bootstrap.Bootstrap
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.design.util.ViewModelContainerHost
import tech.zhifu.app.myhub.ui.model.toDashboardContentCard

class DashboardViewModel(
    val logger: Logger = logger("Dashboard"),
    internal val bootstrap: Bootstrap,
    internal val userRepository: UserRepository,
    internal val cardRepository: CardRepository,
) : ViewModelContainerHost<DashboardUiState, DashboardSideEffect>() {

    override val container = container<DashboardUiState, DashboardSideEffect>(
        initialState = DashboardUiState.Idle,
    ) {
        observeUiStateFlow()
    }

    private val searchQueryFlow = MutableStateFlow("")

    private val userFlow: StateFlow<User?> =
        userRepository
            .streamUser()
            .onEach { user ->
                if (user == null) {
                    logger.debug { "用户不存在，静默登陆" }
                    bootstrap.initialize("default")
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = null
            )

    private val prefsFlow: StateFlow<UserPreferences?> =
        userFlow
            .filterNotNull()
            .distinctUntilChangedBy { it.id }
            .flatMapLatest { user ->
                logger.debug { "获取用户偏好" }
                userRepository
                    .streamUserPreferences(user.id)
                    .map { it ?: UserPreferences(user.id) }
                    .catch { e ->
                        logger.error(e) { "获取用户偏好失败，采用默认值" }
                        emit(UserPreferences(user.id))
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = null
            )

    fun refresh() = intent {
        run {
            val currentState = state as? DashboardUiState.Loading
            if (currentState != null) {
                logger.debug { "正在刷新，忽略本次刷新请求" }
                return@intent
            }
        }
        reduce {
            when (state) {
                is DashboardUiState.Content -> {
                    logger.debug { "刷新发生在内容态，不清空UI" }
                    state
                }

                else -> {
                    logger.debug { "刷新发生在非内容态，清空UI" }
                    DashboardUiState.Loading
                }
            }
        }
        runCatching {
            val user = userFlow.value ?: return@intent
            val prefs = prefsFlow.value ?: return@intent
            val query = searchQueryFlow.value
            cardRepository
                .flowCards(
                    userId = user.id,
                    cursorCardId = null,
                    cursorTitle = null,
                    cursorUpdatedAt = null,
                    orderByUpdated = prefs.sortAsDate,
                    orderByTitle = prefs.sortAsName,
                    query = query,
                    limit = DashboardUiState.Content.PAGE_SIZE,
                )
                .first()
                .map { it.toDashboardContentCard() }
        }.onSuccess { cards ->
            logger.debug { "刷新成功，刷新UI ${cards.size}" }
            reduce {
                when (val currentState = state) {
                    is DashboardUiState.Content -> {
                        currentState.copy(
                            items = cards,
                            hasMore = cards.size == DashboardUiState.Content.PAGE_SIZE,
                            isLoadingMore = false,
                        )
                    }

                    else -> {
                        val user = userFlow.value ?: return@reduce state
                        val prefs = prefsFlow.value ?: return@reduce state
                        val searchQuery = searchQueryFlow.value
                        DashboardUiState.Content(
                            user = user,
                            userPreferences = prefs,
                            searchQuery = searchQuery,
                            items = cards,
                            hasMore = cards.size == DashboardUiState.Content.PAGE_SIZE,
                            isLoadingMore = false
                        )

                    }
                }
            }
        }.onFailure { e ->
            handleError(e, "刷新失败，请稍后重试")
        }
    }

    fun loadMore() = intent {
        run {
            val currentState = state as? DashboardUiState.Content
            if (currentState == null) {
                logger.debug { "非内容状态，忽略本次加载更多请求" }
                return@intent
            }
            if (currentState.isLoadingMore) {
                logger.debug { "正在加载更多，忽略本次加载更多请求" }
                return@intent
            }
            if (!currentState.hasMore) {
                logger.debug { "没有更多了，忽略本次加载更多请求" }
                return@intent
            }
        }
        reduce {
            when (val currentState = state) {
                is DashboardUiState.Content -> {
                    currentState.copy(isLoadingMore = true)
                }

                else -> state
            }
        }
        runCatching {
            val user = userFlow.value ?: return@intent
            val prefs = prefsFlow.value ?: return@intent
            val currentState = state as? DashboardUiState.Content
            val cursor = currentState?.items?.lastOrNull()
            val query = searchQueryFlow.value
            cardRepository
                .flowCards(
                    userId = user.id,
                    cursorCardId = cursor?.id,
                    cursorTitle = cursor?.title,
                    cursorUpdatedAt = cursor?.updatedAt,
                    orderByUpdated = prefs.sortAsDate,
                    orderByTitle = prefs.sortAsName,
                    query = query,
                    limit = DashboardUiState.Content.PAGE_SIZE
                )
                .first()
                .map { it.toDashboardContentCard() }

        }.onSuccess { cards ->
            reduce {
                when (val currentState = state) {
                    is DashboardUiState.Content -> {
                        currentState.copy(
                            items = currentState.items + cards,
                            hasMore = cards.size == DashboardUiState.Content.PAGE_SIZE,
                            isLoadingMore = false,
                        )
                    }

                    else -> run {
                        logger.debug { "非内容状态，忽略本次加载更多请求" }
                        state
                    }
                }
            }
        }.onFailure { e ->
            handleError(e, "加载异常，请稍后重试")
        }
    }

    fun search(query: String = "", reset: Boolean = false) {
        searchQueryFlow.value = query
        if (reset) {
            intent {
                postSideEffect(DashboardSideEffect.ResetSearch)
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeUiStateFlow() = intent {
        userFlow
            .filterNotNull()
            .onEach { user ->
                reduce {
                    when (val currentState = state) {
                        is DashboardUiState.Content -> {
                            currentState.copy(user = user)
                        }

                        else -> run {
                            logger.debug { "非内容状态，忽略本次用户更新请求" }
                            return@reduce state
                        }
                    }
                }
            }
            .launchIn(viewModelScope)

        prefsFlow
            .filterNotNull()
            .distinctUntilChangedBy { prefs ->
                listOf(prefs.layoutAsList, prefs.userId, prefs.sortAsName, prefs.sortAsDate)
            }
            // give the time to show the loading state
            .debounce(timeoutMillis = 300)
            .onEach { userPreferences ->
                reduce {
                    when (val currentState = state) {
                        is DashboardUiState.Content -> {
                            currentState.copy(userPreferences = userPreferences)
                        }

                        else -> run {
                            logger.debug { "非内容状态，忽略本次用户偏好更新请求" }
                            state
                        }
                    }
                }
            }
            .onEach {
                logger.debug { "更新用户偏好发生变化，刷新UI" }
                refresh()
            }
            .launchIn(viewModelScope)

        searchQueryFlow
            .onEach { searchQuery ->
                reduce {
                    when (val currentState = state) {
                        is DashboardUiState.Content -> {
                            currentState.copy(searchQuery = searchQuery)
                        }

                        else -> run {
                            logger.debug { "非内容状态，忽略本次搜索关键词更新请求" }
                            state
                        }
                    }
                }
            }
            .debounce(timeoutMillis = 300)
            .distinctUntilChanged()
            .onEach {
                logger.debug { "搜索关键词发生变化，刷新UI" }
                refresh()
            }
            .launchIn(viewModelScope)
    }

    private fun handleError(
        e: Throwable,
        message: String
    ) = intent {
        logger.error(e) { message }
        reduce {
            when (val currentState = state) {
                is DashboardUiState.Content -> {
                    currentState.copy(
                        errorMessage = message,
                        isLoadingMore = false,
                    )
                }

                else -> run {
                    DashboardUiState.Error(message = message)
                    state
                }
            }
        }
        postSideEffect(DashboardSideEffect.ShowSnack(message))
    }
}
