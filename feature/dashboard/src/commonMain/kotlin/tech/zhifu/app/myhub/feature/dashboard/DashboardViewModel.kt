package tech.zhifu.app.myhub.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
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
import tech.zhifu.app.myhub.ui.viewmodel.ViewModelSideEffect
import tech.zhifu.app.myhub.ui.viewmodel.createSideEffectFlow

class DashboardViewModel(
    internal val cardRepository: CardRepository,
    override val userRepository: UserRepository,
) : ViewModel(),
    ContainerHost<DashboardUiState, DashboardSideEffect>,
    ViewModelSideEffect<DashboardSideEffect>,
    UserState,
    UserPreferencesState,
    LayoutState,
    SearchState {
    override val container: Container<DashboardUiState, DashboardSideEffect> =
        container(initialState = DashboardUiState.Idle) {
            observeUiStateFlow()
        }
    override val sideEffect = createSideEffectFlow()
    override val userStateFlow = createUserStateFlow()
    override val userPreferencesStateFlow = createUserPreferencesStatFlow()
    override val layout = createLayoutStateFlow()
    override val searchState = createSearchStateFlow()
    private val refreshState = MutableStateFlow(true)

    fun refresh() {
        refreshState.value = true
    }

    fun loadMore() {
        refreshState.value = false
    }

    @OptIn(FlowPreview::class)
    private fun observeUiStateFlow() = intent {
        // 监听卡片数据变化（例如 AI 发布新卡片），自动触发列表刷新
        combine(
            // 监听：布局、排序的变化
            flow = layout
                .onEach {
                    logger.debug { "布局发生变化，刷新数据！" }
                }
                .map { arrayOf(true) },
            // 监听搜索
            flow2 = searchState
                .debounce(timeoutMillis = 300)
                .onEach {
                    logger.debug { "搜索发生变化，刷新数据！" }
                }
                .distinctUntilChanged()
                .map { arrayOf(true) },
            // 监听刷新
            flow3 = refreshState
                .onEach {
                    logger.debug { "刷新发生变化，刷新数据!" }
                }
        ) { layoutChanged, queryChanged, refreshState ->
            val res = layoutChanged[0] || queryChanged[0] || refreshState
            layoutChanged[0] = false
            queryChanged[0] = false
            res
        }
            .filter { isToRefresh ->
                // 参数校验
                userStateFlow.value ?: run {
                    logger.debug { "用户尚未加载，等待加载" }
                    return@filter false
                }
                // 状态校验
                if (isToRefresh) {
                    val currentState = state as? DashboardUiState.Loading
                    if (currentState != null) {
                        logger.debug { "正在刷新，忽略本次刷新请求" }
                        return@filter false
                    }
                    return@filter true
                } else {
                    val currentState = state as? DashboardUiState.Content
                    if (currentState == null) {
                        logger.debug { "非内容状态，忽略本次加载更多请求" }
                        return@filter false
                    }
                    if (currentState.isLoadingMore) {
                        logger.debug { "正在加载更多，忽略本次加载更多请求" }
                        return@filter false
                    }
                    if (!currentState.hasMore) {
                        logger.debug { "没有更多了，忽略本次加载更多请求" }
                        return@filter false
                    }
                    return@filter true
                }
            }
            .onEach { isToRefresh ->
                // 通知UI更新
                if (isToRefresh) {
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
                } else {
                    reduce {
                        when (val currentState = state) {
                            is DashboardUiState.Content -> {
                                currentState.copy(
                                    isLoadingMore = true
                                )
                            }

                            else -> state
                        }
                    }
                }
            }
            .flatMapLatest { isToRefresh ->
                val user = userStateFlow.value ?: return@flatMapLatest emptyFlow()
                val layout = layout.value
                val cursor =
                    if (isToRefresh) null
                    else (state as? DashboardUiState.Content)?.items?.lastOrNull()
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
                    .map { isToRefresh to it }
            }
            .onEach { (isToRefresh, res) ->
                val cards = res.map { it.toDashboardContentCard() }
                val hasMore = res.size == DashboardUiState.Content.PAGE_SIZE
                if (isToRefresh) {
                    reduce {
                        when (val currentState = state) {
                            is DashboardUiState.Content -> {
                                currentState.copy(
                                    items = cards.toPersistentList(),
                                    hasMore = hasMore,
                                    isLoadingMore = false,
                                )
                            }

                            else -> {
                                DashboardUiState.Content(
                                    items = cards.toPersistentList(),
                                    hasMore = hasMore,
                                    isLoadingMore = false
                                )
                            }
                        }
                    }
                } else {
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
                }
            }
            .catch { e ->
                handleError(e = e, message = "加载异常，请稍后重试")
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
