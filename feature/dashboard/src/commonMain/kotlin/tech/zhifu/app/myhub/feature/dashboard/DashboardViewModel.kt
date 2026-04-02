package tech.zhifu.app.myhub.feature.dashboard

import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.datastore.bootstrap.Bootstrap
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.dashboard.content.search.SearchState
import tech.zhifu.app.myhub.feature.dashboard.content.search.initSearchStateFlow
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.design.util.ViewModelContainerHost
import tech.zhifu.app.myhub.ui.model.toDashboardContentCard
import tech.zhifu.app.myhub.ui.state.layout.LayoutState
import tech.zhifu.app.myhub.ui.state.layout.initLayoutStateFlow
import tech.zhifu.app.myhub.ui.state.user.UserState
import tech.zhifu.app.myhub.ui.state.user.initUserStateFlow
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState
import tech.zhifu.app.myhub.ui.state.user.preferences.initUserPreferencesStatFlow

class DashboardViewModel(
    internal val bootstrap: Bootstrap,
    internal val cardRepository: CardRepository,
    override val userRepository: UserRepository,
) : ViewModelContainerHost<DashboardUiState, DashboardSideEffect>(),
    UserState,
    UserPreferencesState,
    LayoutState,
    SearchState {

    val logger: Logger = logger("Dashboard")

    override val container =
        container<DashboardUiState, DashboardSideEffect>(
            initialState = DashboardUiState.Idle,
        ) {
            observeUiStateFlow()
        }
    override val userStateFlow = initUserStateFlow()
    override val userPreferencesStateFlow = initUserPreferencesStatFlow()
    override val layoutStateFlow = initLayoutStateFlow()
    override val searchStateFlow = initSearchStateFlow()

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
            val user = userStateFlow.value ?: return@intent
            val layout = layoutStateFlow.value
            val query = searchStateFlow.value
            cardRepository
                .flowCards(
                    userId = user.id,
                    cursorCardId = null,
                    cursorTitle = null,
                    cursorUpdatedAt = null,
                    orderByUpdated = layout.sortAsDate,
                    orderByTitle = layout.sortAsName,
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
                            items = cards.toPersistentList(),
                            hasMore = cards.size == DashboardUiState.Content.PAGE_SIZE,
                            isLoadingMore = false,
                        )
                    }

                    else -> {
                        DashboardUiState.Content(
                            items = cards.toPersistentList(),
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
            val user = userStateFlow.value ?: return@intent
            val layout = layoutStateFlow.value
            val currentState = state as? DashboardUiState.Content
            val cursor = currentState?.items?.lastOrNull()
            val query = searchStateFlow.value
            cardRepository
                .flowCards(
                    userId = user.id,
                    cursorCardId = cursor?.id,
                    cursorTitle = cursor?.title,
                    cursorUpdatedAt = cursor?.updatedAt,
                    orderByUpdated = layout.sortAsDate,
                    orderByTitle = layout.sortAsName,
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
                            items = currentState.items.addAll(cards),
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
        searchStateFlow.value = query
        if (reset) {
            intent {
                postSideEffect(DashboardSideEffect.ResetSearch)
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeUiStateFlow() = intent {
        userStateFlow
            .onEach { user ->
                if (user == null) {
                    logger.debug { "用户不存在，静默登陆" }
                    bootstrap.initialize("default")
                }
            }
            .launchIn(viewModelScope)
        layoutStateFlow
            .onEach {
                logger.debug { "更新用户偏好发生变化，刷新UI" }
                refresh()
            }
            .launchIn(viewModelScope)
        searchStateFlow
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
