package tech.zhifu.app.myhub.feature.dashboard

import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.feature.dashboard.content.search.SearchState
import tech.zhifu.app.myhub.ui.model.ContentCard

sealed class DashboardUiState(
    val state: State,
) {
    object Idle : DashboardUiState(state = State.IDLE)

    object Loading : DashboardUiState(state = State.LOADING)

    data class Content(
        val user: User,
        val userPreferences: UserPreferences,
        val searchState: SearchState = SearchState(),
        val items: List<ContentCard> = emptyList(),
        val hasMore: Boolean = false,
        val isLoadingMore: Boolean = false,
        val errorMessage: String = "",
    ) : DashboardUiState(state = State.CONTENT) {
        companion object {
            const val PAGE_SIZE = 20
        }
    }

    data class Error(
        val message: String = "",
    ) : DashboardUiState(state = State.ERROR)

    enum class State {
        IDLE, LOADING, CONTENT, ERROR
    }
}
