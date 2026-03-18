package tech.zhifu.app.myhub.feature.dashboard

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.datastore.bootstrap.Bootstrap
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.datastore.repository.user.preferences
import tech.zhifu.app.myhub.feature.dashboard.content.item.ContentItem
import tech.zhifu.app.myhub.feature.dashboard.content.item.mockContentItems
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.util.ViewModelContainerHost

class DashboardViewModel(
    val logger: Logger = logger("Dashboard"),
    internal val bootstrap: Bootstrap,
    internal val userRepository: UserRepository,
) : ViewModelContainerHost<DashboardUiState, DashboardSideEffect>() {

    override val container = container<DashboardUiState, DashboardSideEffect>(
        initialState = DashboardUiState.Loading()
    )

    private var loadJob: Job? = null

    init {
        startLoading()
    }

    fun retry() {
        startLoading()
    }

    private fun startLoading() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            intent { reduce { DashboardUiState.Loading() } }
            logger.debug { "DashboardViewModel init" }
            userRepository.streamUser()
                .onEach { user ->
                    if (user == null) {
                        bootstrap.initialize("default")
                    }
                }
                .filterNotNull()
                .distinctUntilChangedBy { it.id }
                .flatMapLatest { user ->
                    val itemsFlow = flowOf(mockContentItems())
                    val preferencesFlow = userRepository.streamUserPreferences(user.id)
                        .map { response ->
                            response.requireData().preferences
                                ?: UserPreferences(user.id)
                        }
                        .distinctUntilChanged()
                    val updates = merge(
                        itemsFlow.map { Update(items = it) },
                        preferencesFlow.map { Update(preferences = it) }
                    )
                    updates.scan(
                        DashboardUiState.Content(
                            user = user,
                            userPreferences = UserPreferences(user.id),
                            contentItems = emptyList()
                        )
                    ) { state, update ->
                        state.copy(
                            user = user,
                            userPreferences = update.preferences ?: state.userPreferences,
                            contentItems = update.items ?: state.contentItems
                        )
                    }
                }
                .catch { e ->
                    logger.error(throwable = e) { "Dashboard load failed" }
                    intent {
                        reduce {
                            DashboardUiState.Error(
                                message = e.message ?: "加载失败，请重试"
                            )
                        }
                    }
                }
                .collectLatest { state ->
                    intent { reduce { state } }
                }
        }
    }
}

private data class Update(
    val preferences: UserPreferences? = null,
    val items: List<ContentItem>? = null,
)
