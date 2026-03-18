package tech.zhifu.app.myhub.feature.dashboard

import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.feature.dashboard.content.item.ContentItem
import tech.zhifu.app.myhub.feature.dashboard.content.search.SearchState

sealed class DashboardUiState(
    val state: State,
) {
    data class Loading(
        val hasAuthed: Boolean = false
    ) : DashboardUiState(state = State.LOADING)

    data class Content(
        val user: User,
        val userPreferences: UserPreferences = UserPreferences(""),
        val searchState: SearchState = SearchState(),
        val contentItems: List<ContentItem> = emptyList(),
    ) : DashboardUiState(state = State.CONTENT)

    data class Error(
        val message: String = "",
    ) : DashboardUiState(state = State.ERROR)

    enum class State {
        LOADING, CONTENT, ERROR
    }
}
