package tech.zhifu.app.myhub.feature.dashboard

import tech.zhifu.app.myhub.feature.dashboard.content.card.CardSectionState
import tech.zhifu.app.myhub.feature.dashboard.content.collection.CollectionSectionState
import tech.zhifu.app.myhub.feature.dashboard.content.review.ReviewState
import tech.zhifu.app.myhub.feature.dashboard.content.search.SearchState

sealed class DashboardUiState(
    val state: State,
) {
    object Loading : DashboardUiState(state = State.LOADING)

    data class Content(
        val layoutAsList: Boolean = true,
        val sortAsDate: Boolean = true,

        // FOLLOWING NEED REFINE
        val source: String = "Unknown",
        val isRefreshing: Boolean = false,
        val reviewState: ReviewState? = null,
        val collectionSectionState: CollectionSectionState,
        val cardSectionState: CardSectionState,

        val searchState: SearchState,
    ) : DashboardUiState(state = State.CONTENT)

    data class Error(
        val message: String = "",
    ) : DashboardUiState(state = State.ERROR)

    enum class State {
        LOADING, CONTENT, ERROR
    }
}
