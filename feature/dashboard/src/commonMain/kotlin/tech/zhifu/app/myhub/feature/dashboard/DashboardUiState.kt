package tech.zhifu.app.myhub.feature.dashboard

import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.feature.dashboard.content.search.SearchState
import tech.zhifu.app.myhub.ui.model.ContentCard

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
        val items: List<ContentCard> = emptyList(),
    ) : DashboardUiState(state = State.CONTENT)

    data class Error(
        val message: String = "",
    ) : DashboardUiState(state = State.ERROR)

    enum class State {
        LOADING, CONTENT, ERROR
    }
}
