package tech.zhifu.app.myhub.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.dashboard.content.search.SearchState
import tech.zhifu.app.myhub.feature.dashboard.content.search.createSearchStateFlow
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.model.toDashboardContentCard
import tech.zhifu.app.myhub.ui.state.layout.LayoutState
import tech.zhifu.app.myhub.ui.state.layout.createLayoutStateFlow
import tech.zhifu.app.myhub.ui.state.user.UserState
import tech.zhifu.app.myhub.ui.state.user.createUserStateFlow
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState
import tech.zhifu.app.myhub.ui.state.user.preferences.createUserPreferencesStatFlow

class DashboardViewModel(
    internal val cardRepository: CardRepository,
    override val userRepository: UserRepository,
) : ViewModel(),
    ContainerHost<DashboardUiState, DashboardSideEffect>,
    UserState,
    UserPreferencesState,
    LayoutState,
    SearchState {
    override val container: Container<DashboardUiState, DashboardSideEffect> =
        container(initialState = DashboardUiState.Idle) {
            observeUiStateFlow()
        }
    override val userStateFlow = createUserStateFlow()
    override val userPreferencesStateFlow = createUserPreferencesStatFlow()
    override val layout = createLayoutStateFlow()
    override val searchState = createSearchStateFlow()

    fun refresh() = intent {
        run {
            userStateFlow.value ?: run {
                logger.debug { "用户尚未加载，等待加载" }
                return@intent
            }
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
            val layout = layout.value
            val query = searchState.value
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
            val layout = layout.value
            val currentState = state as? DashboardUiState.Content
            val cursor = currentState?.items?.lastOrNull()
            val query = searchState.value
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

    @OptIn(FlowPreview::class)
    private fun observeUiStateFlow() = intent {
        layout
            .onEach {
                logger.debug { "更新用户偏好发生变化，刷新UI" }
                refresh()
            }
            .launchIn(viewModelScope)
        searchState
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
