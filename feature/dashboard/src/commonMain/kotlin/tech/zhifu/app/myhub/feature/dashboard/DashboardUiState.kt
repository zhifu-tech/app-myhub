package tech.zhifu.app.myhub.feature.dashboard

import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.feature.dashboard.content.item.ContentItem
import tech.zhifu.app.myhub.feature.dashboard.content.search.SearchState

sealed class DashboardUiState(
    val state: State,
) {
    data class Loading(
        val message: String,
    ) : DashboardUiState(state = State.LOADING)

    data class Content(
        val user: User,
        val userPreferences: UserPreferences,
        val searchState: SearchState = SearchState(),

        // 分页信息
        val pageIndx: Int = 1,
        val pageSize: Int = 20,
        val hasMore: Boolean = false,
        val contentItems: List<ContentItem> = emptyList(),
    ) : DashboardUiState(state = State.CONTENT)

    data class Error(
        val message: String = "",
    ) : DashboardUiState(state = State.ERROR)

    enum class State {
        LOADING, CONTENT, ERROR
    }
}
