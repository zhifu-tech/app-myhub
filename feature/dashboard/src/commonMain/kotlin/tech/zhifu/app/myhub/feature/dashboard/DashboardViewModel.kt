package tech.zhifu.app.myhub.feature.dashboard

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
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
import tech.zhifu.app.myhub.ui.model.ContentCard
import tech.zhifu.app.myhub.ui.model.toDashboardContentCard
import tech.zhifu.app.myhub.util.ViewModelContainerHost

class DashboardViewModel(
    val logger: Logger = logger("Dashboard"),
    internal val bootstrap: Bootstrap,
    internal val userRepository: UserRepository,
    internal val cardRepository: CardRepository,
) : ViewModelContainerHost<DashboardUiState, DashboardSideEffect>() {

    fun retry() {
        actionFlow.tryEmit(Action.Refresh)
    }

    fun loadMore() {
        actionFlow.tryEmit(Action.LoadMore)
    }

    override val container = container<DashboardUiState, DashboardSideEffect>(
        initialState = DashboardUiState.Loading("资源加载中...")
    ) {
        // 启动 Flow -> container 映射
        observeUiStateFlow()
        // 首次刷新
        actionFlow.tryEmit(Action.Refresh)
    }

    private fun observeUiStateFlow() = intent {
        uiStateFlow.collect { newState ->
            reduce { newState }
        }
    }

    private val actionFlow = MutableSharedFlow<Action>(
        extraBufferCapacity = 64
    )

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

    private val internalStateFlow: StateFlow<InternalState> =
        merge(
            actionFlow,
            prefsFlow
                .filterNotNull()
                .distinctUntilChangedBy { prefs ->
                    Triple(prefs.userId, prefs.sortAsName, prefs.sortAsDate)
                }
                .map { Action.Refresh }
        )
            .flatMapLatest { action ->
                flow {
                    emit(Mutation.Loading(action))

                    val user = userFlow.value ?: return@flow
                    val prefs = prefsFlow.value ?: return@flow
                    val currentItems = latestInternalState.value.items
                    val isRefresh = action is Action.Refresh
                    val cursor = if (isRefresh) null else currentItems.lastOrNull()
                    val pageSize = 20

                    cardRepository
                        .flowCards(
                            userId = user.id,
                            cursorCardId = cursor?.id,
                            cursorTitle = cursor?.title,
                            cursorUpdatedAt = cursor?.updatedAt,
                            orderByUpdated = prefs.sortAsDate,
                            orderByTitle = prefs.sortAsName,
                            limit = pageSize
                        )
                        .map { list -> list.map { it.toDashboardContentCard() } }
                        .collect { cards ->
                            emit(
                                value = Mutation.Success(
                                    action = action,
                                    items = cards,
                                    pageSize = pageSize
                                )
                            )
                        }
                }.catch { e ->
                    emit(Mutation.Error(action, e))
                }
            }
            .scan(initial = InternalState()) { state, mutation ->
                when (mutation) {

                    is Mutation.Loading -> {
                        state.copy(
                            isLoading = true,
                            error = null
                        )
                    }

                    is Mutation.Success -> {
                        val isRefresh = mutation.action is Action.Refresh
                        state.copy(
                            isLoading = false,
                            items = if (isRefresh) mutation.items else state.items + mutation.items,
                            hasMore = mutation.items.size >= mutation.pageSize,
                            error = null
                        )
                    }

                    is Mutation.Error -> {
                        state.copy(
                            isLoading = false,
                            error = mutation.error
                        )
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = InternalState()
            )

    private val latestInternalState: StateFlow<InternalState> = internalStateFlow

    private val uiStateFlow: StateFlow<DashboardUiState> =
        combine(
            internalStateFlow,
            userFlow.filterNotNull(),
            prefsFlow.filterNotNull()
        ) { state, user, prefs ->
            when {
                state.isLoading && state.items.isEmpty() -> {
                    DashboardUiState.Loading("加载中...")
                }

                state.error != null && state.items.isEmpty() -> {
                    DashboardUiState.Error(state.error.message ?: "加载失败")
                }

                else -> {
                    DashboardUiState.Content(
                        user = user,
                        userPreferences = prefs,
                        items = state.items
                    )
                }
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DashboardUiState.Loading("初始化中...")
            )
}

private data class InternalState(
    val items: List<ContentCard> = emptyList(),
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
)

sealed interface Action {
    object Refresh : Action
    object LoadMore : Action
}

private sealed interface Mutation {
    data class Loading(
        val action: Action
    ) : Mutation

    data class Success(
        val action: Action,
        val items: List<ContentCard>,
        val pageSize: Int
    ) : Mutation

    data class Error(
        val action: Action,
        val error: Throwable
    ) : Mutation
}
