package tech.zhifu.app.myhub.feature.dashboard

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.datastore.bootstrap.Bootstrap
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.streamContentItems
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.streamUser
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.streamUserPreferences
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.logger.warn
import tech.zhifu.app.myhub.util.ViewModelContainerHost

class DashboardViewModel(
    val logger: Logger = logger("Dashboard"),
    internal val bootstrap: Bootstrap,
    internal val userRepository: UserRepository,
    internal val cardRepository: CardRepository,
) : ViewModelContainerHost<DashboardUiState, DashboardSideEffect>() {
    private var loadJob: Job? = null

    override val container = container<DashboardUiState, DashboardSideEffect>(
        initialState = DashboardUiState.Loading("资源加载中...")
    ) {
        initLoadData("init")
    }

    fun retry() {
        initLoadData("retry")
    }

    private fun initLoadData(reason: String) {
        logger.debug { "加载数据 as $reason" }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            // 1. 取用户信息
            streamUser()
                .filterNotNull()
                .distinctUntilChangedBy { it.id }
                // 2. 获取用户偏好
                .flatMapLatest { user ->
                    streamUserPreferences(
                        userId = user.id
                    ).map {
                        user to it
                    }
                }
                // 3. 获取 内容数据
                .distinctUntilChangedBy { (user, userPreference) ->
                    // 用户 或着 排序偏好发生变化，重新拉取数据
                    Triple(user.id, userPreference.sortAsDate, userPreference.sortAsName)
                }
                .flatMapLatest { (user, userPreference) ->
                    streamContentItems(
                        userId = user.id,
                        pageIndex = 1,
                        sortAsDate = userPreference.sortAsDate,
                        sortAsName = userPreference.sortAsName,
                        user = user,
                        userPreferences = userPreference,
                    ).map {
                        Triple(user, userPreference, it)
                    }
                }
                .collect {
                    logger.debug { "Init finished." }
                }
        }
    }

    fun loadMoreData() {
        val state = uiState as? DashboardUiState.Content ?: run {
            logger.warn { "当前非Content状态，不能加载更多" }
            return
        }
        if (!state.hasMore) {
            logger.warn { "当前没有更多数据" }
            return
        }
        val last = state.contentItems.lastOrNull() ?: return
        val sortAsDate = state.userPreferences.sortAsDate
        val sortAsName = state.userPreferences.sortAsName
        val orderByUpdated = sortAsDate
        val orderByTitle = !sortAsDate && sortAsName

        viewModelScope.launch {
            streamContentItems(
                userId = state.user.id,
                pageIndex = state.pageIndx + 1,
                pageSize = state.pageSize,
                cursorCardId = last.id,
                cursorTitle = if (orderByTitle) last.title else null,
                cursorUpdatedAt = if (orderByUpdated) last.updatedTimeMs else null,
                sortAsDate = sortAsDate,
                sortAsName = sortAsName,
                user = state.user,
                userPreferences = state.userPreferences
            ).first()
        }
    }
}
