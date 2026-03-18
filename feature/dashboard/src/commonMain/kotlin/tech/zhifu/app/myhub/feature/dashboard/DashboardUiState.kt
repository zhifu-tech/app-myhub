package tech.zhifu.app.myhub.feature.dashboard

import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.feature.dashboard.content.search.SearchState

sealed class DashboardUiState(
    val state: State,
) {
    data class Loading(
        val hasAuthed: Boolean = false
    ) : DashboardUiState(state = State.LOADING)

    data class Content(
        val userPreferences: UserPreferences,
        val searchState: SearchState,
        val searchKeywords: String,
    ) : DashboardUiState(state = State.CONTENT)

    data class Error(
        val message: String = "",
    ) : DashboardUiState(state = State.ERROR)

    enum class State {
        LOADING, CONTENT, ERROR
    }
}
